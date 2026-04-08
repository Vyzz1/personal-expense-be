package com.huynh.personal_expense_be.modules.expense.application.service;

import com.huynh.personal_expense_be.modules.expense.application.dto.*;
import com.huynh.personal_expense_be.modules.expense.application.port.in.DeductMonthlyExpenseUseCase;
import com.huynh.personal_expense_be.modules.expense.application.port.in.RecordMonthlyExpenseUseCase;
import com.huynh.personal_expense_be.modules.expense.application.port.in.UpdateMonthlyExpenseUseCase;
import com.huynh.personal_expense_be.modules.expense.application.port.out.MonthlyExpenseRepositoryPort;
import com.huynh.personal_expense_be.modules.expense.application.port.out.ExpenseNotificationPort;
import com.huynh.personal_expense_be.modules.expense.domain.MonthlyExpense;
import com.huynh.personal_expense_be.shared.dto.SseNotificationEventType;
import com.huynh.personal_expense_be.shared.dto.SseNotificationMessage;
import com.huynh.personal_expense_be.shared.utility.Utility;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class CommandMonthlyExpenseService implements RecordMonthlyExpenseUseCase, DeductMonthlyExpenseUseCase, UpdateMonthlyExpenseUseCase {

    private final MonthlyExpenseRepositoryPort monthlyExpenseRepositoryPort;
    private final ExpenseNotificationPort expenseNotificationPort;

    @Override
    public void recordMonthlyExpense(RecordExpenseCommand command) {
        int month = Utility.getMonthFromInstant(command.occurredAt());
        int year  = Utility.getYearFromInstant(command.occurredAt());

        // Fetch the current row to compute changePercentage in Java
        MonthlyExpense existing = monthlyExpenseRepositoryPort.findByUserIdAndMonth(command.userId(), month, year);

        MonthlyExpense toUpsert;
        if (existing == null) {
            // Brand-new month bucket — look up previous month for % comparison

            toUpsert = MonthlyExpense.builder()
                    .userId(command.userId())
                    .month(month)
                    .year(year)
                    .totalAmount(command.amount())   // delta == first amount
                    .build();
        } else {


            log.info("Existing MonthlyExpense for userId={}, month={}, year={}. Upsert delta={}.",
                    command.userId(), month, year, command.amount());

            // Pass delta (not accumulated total) — upsert adds it onto DB total atomically
            toUpsert = existing.toBuilder()
                    .totalAmount(command.amount())      // ← delta only
                    .build();
        }

        monthlyExpenseRepositoryPort.saveMonthlyExpense(toUpsert);
        notifyExpenseCalculated(command.userId(), month, year);
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

                    me = MonthlyExpense.builder()
                            .userId(userId)
                            .month(key.month())
                            .year(key.year())
                            .totalAmount(amount)    // first/only contribution = delta
                            .build();
                } else {


                    // Pass DELTA as totalAmount — DB upsert adds it atomically
                    me = me.toBuilder()
                            .totalAmount(amount)        // ← delta only
                            .build();
                }

                toSave.add(me);
            }
        }

        // ── Step 3: single bulk save ─────────────────────────────────────────
        monthlyExpenseRepositoryPort.saveAllMonthlyExpenses(toSave);
        
        // Notify users
        for (Map.Entry<String, List<Key>> entry : byUser.entrySet()) {
            String userId = entry.getKey();
            for (Key key : entry.getValue()) {
                notifyExpenseCalculated(userId, key.month(), key.year());
            }
        }
        
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




        // Pass NEGATIVE delta — upsert does total_amount += (-deductAmount)
        MonthlyExpense toUpsert = existing.toBuilder()
                .totalAmount(command.deductAmount().negate())   // ← negative delta
                .build();

        monthlyExpenseRepositoryPort.saveMonthlyExpense(toUpsert);
        notifyExpenseCalculated(command.userId(), month, year);
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


        // Pass DIFF as delta — upsert does total_amount += diff atomically
        MonthlyExpense toUpsert = existing.toBuilder()
                .totalAmount(diff)          // ← delta (newAmount - oldAmount)
                .build();

        monthlyExpenseRepositoryPort.saveMonthlyExpense(toUpsert);
        notifyExpenseCalculated(command.userId(), month, year);
    }

    private void notifyExpenseCalculated(String userId, int month, int year) {
        String message = String.format("Monthly expense for %02d/%d has been updated.", month, year);
        SseNotificationMessage notificationMsg = SseNotificationMessage.builder()
                .id(UUID.randomUUID().toString())
                .eventType(SseNotificationEventType.EXPENSE_CALCULATED)
                .message(message)
                .description("Your aggregated expenses for the specified month have finished calculating.")
                .timestamp(Instant.now())
                .build();
        expenseNotificationPort.sendExpenseNotification(userId, notificationMsg);
    }

}
