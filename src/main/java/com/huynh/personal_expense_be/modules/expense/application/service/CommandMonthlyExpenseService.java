package com.huynh.personal_expense_be.modules.expense.application.service;

import com.huynh.personal_expense_be.modules.expense.application.dto.*;
import com.huynh.personal_expense_be.modules.expense.application.port.in.DeductMonthlyExpenseUseCase;
import com.huynh.personal_expense_be.modules.expense.application.port.in.RecordMonthlyExpenseUseCase;
import com.huynh.personal_expense_be.modules.expense.application.port.in.UpdateMonthlyExpenseUseCase;
import com.huynh.personal_expense_be.modules.expense.application.port.out.MonthlyExpenseRepositoryPort;
import com.huynh.personal_expense_be.modules.expense.domain.MonthlyExpense;
import com.huynh.personal_expense_be.shared.utility.Utility;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class CommandMonthlyExpenseService implements RecordMonthlyExpenseUseCase, DeductMonthlyExpenseUseCase, UpdateMonthlyExpenseUseCase {

    private final MonthlyExpenseRepositoryPort monthlyExpenseRepositoryPort;

    @Override
    public void recordMonthlyExpense(RecordExpenseCommand command) {
        int month = Utility.getMonthFromInstant(command.occurredAt());
        int year  = Utility.getYearFromInstant(command.occurredAt());

        // Fetch the current row to compute changePercentage in Java
        MonthlyExpense existing = monthlyExpenseRepositoryPort.findByUserIdAndMonth(command.userId(), month, year);

        MonthlyExpense toUpsert;
        if (existing == null) {
            // Brand-new month bucket — look up previous month for % comparison
            MonthlyExpense previous = monthlyExpenseRepositoryPort.findByUserIdAndMonth(
                    command.userId(), month == 1 ? 12 : month - 1, month == 1 ? year - 1 : year);

            toUpsert = MonthlyExpense.builder()
                    .userId(command.userId())
                    .month(month)
                    .year(year)
                    .totalAmount(command.amount())   // delta == first amount
                    .build();
            toUpsert.withPrevious(previous);
        } else {
            BigDecimal newTotal = existing.getTotalAmount().add(command.amount());
            BigDecimal prevTotal = existing.getPreviousTotalAmount() != null
                    ? existing.getPreviousTotalAmount() : BigDecimal.ZERO;
            BigDecimal changePct = prevTotal.compareTo(BigDecimal.ZERO) == 0
                    ? BigDecimal.ZERO
                    : newTotal.subtract(prevTotal)
                              .divide(prevTotal, 4, java.math.RoundingMode.HALF_UP)
                              .multiply(BigDecimal.valueOf(100));

            log.info("Existing MonthlyExpense for userId={}, month={}, year={}. Upsert delta={}.",
                    command.userId(), month, year, command.amount());

            // Pass delta (not accumulated total) — upsert adds it onto DB total atomically
            toUpsert = existing.toBuilder()
                    .totalAmount(command.amount())      // ← delta only
                    .changePercentage(changePct)
                    .build();
        }

        monthlyExpenseRepositoryPort.saveMonthlyExpense(toUpsert);
    }

    /**
     * Optimized bulk processing:
     * 1. Group & aggregate commands by (userId, month, year) in-memory
     * 2. For each userId, fetch all relevant MonthlyExpense rows in ONE query
     * 3. Merge amounts in-memory
     * 4. Bulk-save all results
     */
    @Override
    public void recordMonthlyExpenses(List<RecordExpenseCommand> commands) {
        if (commands == null || commands.isEmpty()) return;

        // ── Step 1: aggregate amount per (userId, month, year) key ──────────
        record Key(String userId, int month, int year) {}

        Map<Key, BigDecimal> aggregated = new LinkedHashMap<>();
        for (RecordExpenseCommand cmd : commands) {
            int month = Utility.getMonthFromInstant(cmd.occurredAt());
            int year  = Utility.getYearFromInstant(cmd.occurredAt());
            Key key   = new Key(cmd.userId(), month, year);
            aggregated.merge(key, cmd.amount(), BigDecimal::add);
        }

        log.info("recordMonthlyExpenses: {} commands aggregated into {} (userId/month/year) buckets",
                commands.size(), aggregated.size());

        // ── Step 2: group keys by userId to minimise DB round-trips ─────────
        Map<String, List<Key>> byUser = aggregated.keySet().stream()
                .collect(Collectors.groupingBy(Key::userId));

        List<MonthlyExpense> toSave = new ArrayList<>(aggregated.size());

        for (Map.Entry<String, List<Key>> entry : byUser.entrySet()) {
            String       userId = entry.getKey();
            List<Key>    keys   = entry.getValue();

            // Collect distinct (month, year) pairs for this user
            List<int[]> monthYearPairs = keys.stream()
                    .map(k -> new int[]{k.month(), k.year()})
                    .toList();

            // Single query to fetch all existing rows for this user
            List<MonthlyExpense> existing = monthlyExpenseRepositoryPort
                    .findAllByUserIdAndMonthYearPairs(userId, monthYearPairs);

            Map<String, MonthlyExpense> existingMap = existing.stream()
                    .collect(Collectors.toMap(
                            me -> me.getMonth() + "_" + me.getYear(),
                            me -> me
                    ));

            for (Key key : keys) {
                BigDecimal amount  = aggregated.get(key);
                String     mapKey  = key.month() + "_" + key.year();
                MonthlyExpense me  = existingMap.get(mapKey);

                if (me == null) {
                    // No existing record — fetch previous month in-memory or via DB
                    int prevMonth = key.month() == 1 ? 12 : key.month() - 1;
                    int prevYear  = key.month() == 1 ? key.year() - 1 : key.year();
                    String prevKey = prevMonth + "_" + prevYear;

                    MonthlyExpense previous = existingMap.get(prevKey);
                    if (previous == null) {
                        previous = monthlyExpenseRepositoryPort.findByUserIdAndMonth(userId, prevMonth, prevYear);
                    }

                    me = MonthlyExpense.builder()
                            .userId(userId)
                            .month(key.month())
                            .year(key.year())
                            .totalAmount(amount)    // first/only contribution = delta
                            .build();
                    me.withPrevious(previous);
                } else {
                    BigDecimal newTotal  = me.getTotalAmount().add(amount);
                    BigDecimal prevTotal = me.getPreviousTotalAmount() != null
                            ? me.getPreviousTotalAmount() : BigDecimal.ZERO;
                    BigDecimal changePct = prevTotal.compareTo(BigDecimal.ZERO) == 0
                            ? BigDecimal.ZERO
                            : newTotal.subtract(prevTotal)
                                      .divide(prevTotal, 4, java.math.RoundingMode.HALF_UP)
                                      .multiply(BigDecimal.valueOf(100));

                    log.info("Batch upsert: userId={}, month={}, year={}, delta={}.",
                            userId, key.month(), key.year(), amount);

                    // Pass DELTA as totalAmount — DB upsert adds it atomically
                    me = me.toBuilder()
                            .totalAmount(amount)        // ← delta only
                            .changePercentage(changePct)
                            .build();
                }

                toSave.add(me);
            }
        }

        // ── Step 3: single bulk save ─────────────────────────────────────────
        monthlyExpenseRepositoryPort.saveAllMonthlyExpenses(toSave);
        log.info("recordMonthlyExpenses: saved {} MonthlyExpense records", toSave.size());
    }

    @Override
    public void deductExpense(DeductExpenseCommand command) {
        int month = Utility.getMonthFromInstant(command.occurredAt());
        int year  = Utility.getYearFromInstant(command.occurredAt());

        MonthlyExpense existing = monthlyExpenseRepositoryPort.findByUserIdAndMonth(command.userId(), month, year);

        if (existing == null) {
            log.warn("No MonthlyExpense found for userId: {}, month: {}, year: {}. Cannot deduct expense.",
                    command.userId(), month, year);
            return;
        }

        log.info("Deducting amount for userId={}, month={}, year={}, delta={}.",
                command.userId(), month, year, command.deductAmount());

        BigDecimal newTotal  = existing.getTotalAmount().subtract(command.deductAmount());
        BigDecimal prevTotal = existing.getPreviousTotalAmount() != null
                ? existing.getPreviousTotalAmount() : BigDecimal.ZERO;
        BigDecimal changePct = prevTotal.compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO
                : newTotal.subtract(prevTotal)
                          .divide(prevTotal, 4, java.math.RoundingMode.HALF_UP)
                          .multiply(BigDecimal.valueOf(100));

        // Pass NEGATIVE delta — upsert does total_amount += (-deductAmount)
        MonthlyExpense toUpsert = existing.toBuilder()
                .totalAmount(command.deductAmount().negate())   // ← negative delta
                .changePercentage(changePct)
                .build();

        monthlyExpenseRepositoryPort.saveMonthlyExpense(toUpsert);
    }

    @Override
    public void updateMonthlyExpense(UpdateExpenseCommand command) {
        int month = Utility.getMonthFromInstant(command.occurredAt());
        int year  = Utility.getYearFromInstant(command.occurredAt());

        MonthlyExpense existing = monthlyExpenseRepositoryPort.findByUserIdAndMonth(command.userId(), month, year);

        if (existing == null) {
            log.warn("No MonthlyExpense found for userId: {}, month: {}, year: {}. Cannot update expense.",
                    command.userId(), month, year);
            return;
        }

        log.info("Updating MonthlyExpense for userId={}, month={}, year={}, oldAmount={}, newAmount={}.",
                command.userId(), month, year, command.oldAmount(), command.newAmount());

        BigDecimal diff      = command.newAmount().subtract(command.oldAmount()); // can be negative
        BigDecimal newTotal  = existing.getTotalAmount().add(diff);
        BigDecimal prevTotal = existing.getPreviousTotalAmount() != null
                ? existing.getPreviousTotalAmount() : BigDecimal.ZERO;
        BigDecimal changePct = prevTotal.compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO
                : newTotal.subtract(prevTotal)
                          .divide(prevTotal, 4, java.math.RoundingMode.HALF_UP)
                          .multiply(BigDecimal.valueOf(100));

        // Pass DIFF as delta — upsert does total_amount += diff atomically
        MonthlyExpense toUpsert = existing.toBuilder()
                .totalAmount(diff)          // ← delta (newAmount - oldAmount)
                .changePercentage(changePct)
                .build();

        monthlyExpenseRepositoryPort.saveMonthlyExpense(toUpsert);
    }

}
