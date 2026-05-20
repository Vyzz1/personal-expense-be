package com.huynh.personal_expense_be.modules.budget.infrastructure.worker;

import com.huynh.personal_expense_be.modules.budget.application.port.out.BudgetPersistencePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.time.ZoneId;

@Component
@RequiredArgsConstructor
@Slf4j
public class BudgetExpiryWorker {

    private static final String DEFAULT_EXPIRE_CRON = "0 5 0 * * *";
    private static final String DEFAULT_EXPIRE_ZONE = "Asia/Ho_Chi_Minh";

    private final BudgetPersistencePort budgetPersistencePort;

    @Scheduled(
            cron = "${app.budget.expire-cron:" + DEFAULT_EXPIRE_CRON + "}",
            zone = "${app.budget.expire-zone:" + DEFAULT_EXPIRE_ZONE + "}"
    )
    @Transactional
    public void expireOldBudgets() {
        String currentPeriod = YearMonth.now(ZoneId.of(DEFAULT_EXPIRE_ZONE)).toString();
        int affected = budgetPersistencePort.expireBudgetsBeforePeriod(currentPeriod);
        log.info("Budget expiry worker completed. Current period={}, expiredBudgets={}", currentPeriod, affected);
    }
}
