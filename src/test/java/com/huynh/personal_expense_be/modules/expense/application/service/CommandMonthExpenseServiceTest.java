
package com.huynh.personal_expense_be.modules.expense.application.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Collections;

import com.huynh.personal_expense_be.modules.expense.application.port.out.ExpenseNotificationPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import com.huynh.personal_expense_be.modules.expense.application.dto.UpdateExpenseCommand;
import com.huynh.personal_expense_be.modules.expense.application.dto.DeductExpenseCommand;
import com.huynh.personal_expense_be.modules.expense.application.dto.RecordExpenseCommand;
import com.huynh.personal_expense_be.modules.expense.application.port.out.MonthlyExpenseRepositoryPort;
import com.huynh.personal_expense_be.modules.expense.domain.MonthlyExpense;
import com.huynh.personal_expense_be.modules.expense.domain.MonthlyExpenseAnalysis;
import com.huynh.personal_expense_be.shared.utility.Utility;

@ExtendWith(MockitoExtension.class)
public class CommandMonthExpenseServiceTest {

    @Mock
    private MonthlyExpenseRepositoryPort monthlyExpenseRepositoryPort;

    @Mock
    private ExpenseNotificationPort expenseNotificationPort;

    @InjectMocks
    private CommandMonthlyExpenseService commandMonthlyExpenseService;

    private String userId;
    private Instant occurredAt;
    private int month;
    private int year;
    private BigDecimal oldAmount;
    private BigDecimal newAmount;
    private BigDecimal deductAmount;
    private BigDecimal recordAmount;
    private UpdateExpenseCommand updateCommand;
    private DeductExpenseCommand deductCommand;
    private RecordExpenseCommand recordCommand;

    @BeforeEach
    void setUp() {
        userId = "user1";
        occurredAt = LocalDateTime.of(2024, 5, 15, 10, 0).toInstant(ZoneOffset.UTC);
        month = Utility.getMonthFromInstant(occurredAt);
        year = Utility.getYearFromInstant(occurredAt);
        oldAmount = BigDecimal.valueOf(100);
        newAmount = BigDecimal.valueOf(150);
        deductAmount = BigDecimal.valueOf(100);
        recordAmount = BigDecimal.valueOf(100);

        updateCommand = new UpdateExpenseCommand(
                userId,
                newAmount,
                oldAmount,
                occurredAt
        );

        deductCommand = new DeductExpenseCommand(
                userId,
                deductAmount,
                occurredAt
        );

        recordCommand = new RecordExpenseCommand(
                userId,
                recordAmount,
                occurredAt
        );
    }

    @Test
    void updateMonthlyExpense_whenExpenseDoesNotExist_shouldNotSave() {
        // Given
        when(monthlyExpenseRepositoryPort.findByUserIdAndMonth(userId, month, year)).thenReturn(null);

        // When
        commandMonthlyExpenseService.updateMonthlyExpense(updateCommand);

        // Then
        verify(monthlyExpenseRepositoryPort, never()).saveMonthlyExpense(any(MonthlyExpense.class));
    }

    @Test
    void updateMonthlyExpense_whenExpenseExists_shouldSaveWithDiffDelta() {
        // Given
        BigDecimal currentTotalAmount = BigDecimal.valueOf(1000);
        MonthlyExpenseAnalysis existingExpense = 
                 MonthlyExpenseAnalysis.builder()
                .userId(userId)
                .month(month)
                .year(year)
                .totalAmount(currentTotalAmount)
                .build();

        when(monthlyExpenseRepositoryPort.findByUserIdAndMonth(userId, month, year)).thenReturn(existingExpense);

        // When
        commandMonthlyExpenseService.updateMonthlyExpense(updateCommand);

        // Then
        ArgumentCaptor<MonthlyExpense> captor = ArgumentCaptor.forClass(MonthlyExpense.class);
        verify(monthlyExpenseRepositoryPort).saveMonthlyExpense(captor.capture());

        MonthlyExpense savedExpense = captor.getValue();
        // The service passes the difference (newAmount - oldAmount) as totalAmount delta for upsert
        BigDecimal expectedDiff = newAmount.subtract(oldAmount); 
        assertEquals(expectedDiff.doubleValue(), savedExpense.getTotalAmount().doubleValue());
    }

    @Test
    void updateMonthlyExpense_whenExpenseExistsAndNewAmountIsLower_shouldSaveWithNegativeDiff() {
        // Given
        updateCommand = new UpdateExpenseCommand(
                userId,
                BigDecimal.valueOf(50),  // new amount
                BigDecimal.valueOf(200), // old amount
                occurredAt
        );

        BigDecimal currentTotalAmount = BigDecimal.valueOf(1000); 
        MonthlyExpenseAnalysis existingExpense = 
                 MonthlyExpenseAnalysis.builder()
                .userId(userId)
                .month(month)
                .year(year)
                .totalAmount(currentTotalAmount)
                .build();

        when(monthlyExpenseRepositoryPort.findByUserIdAndMonth(userId, month, year)).thenReturn(existingExpense);

        // When
        commandMonthlyExpenseService.updateMonthlyExpense(updateCommand);

        // Then
        ArgumentCaptor<MonthlyExpense> captor = ArgumentCaptor.forClass(MonthlyExpense.class);
        verify(monthlyExpenseRepositoryPort).saveMonthlyExpense(captor.capture());

        MonthlyExpense savedExpense = captor.getValue();
        
        // diff = 50 - 200 = -150
        BigDecimal expectedDiff = BigDecimal.valueOf(-150); 
        assertEquals(expectedDiff.doubleValue(), savedExpense.getTotalAmount().doubleValue());
    }

    @Test
    void deductExpense_whenExpenseDoesNotExist_shouldNotSave() {
        // Given
        when(monthlyExpenseRepositoryPort.findByUserIdAndMonth(userId, month, year)).thenReturn(null);

        // When
        commandMonthlyExpenseService.deductExpense(deductCommand);

        // Then
        verify(monthlyExpenseRepositoryPort, never()).saveMonthlyExpense(any(MonthlyExpense.class));
    }

    @Test
    void deductExpense_whenExpenseExists_shouldSaveWithNegativeDelta() {
        // Given
        BigDecimal currentTotalAmount = BigDecimal.valueOf(1000); 
        MonthlyExpenseAnalysis existingExpense = 
                 MonthlyExpenseAnalysis.builder()
                .userId(userId)
                .month(month)
                .year(year)
                .totalAmount(currentTotalAmount)
                .build();

        when(monthlyExpenseRepositoryPort.findByUserIdAndMonth(userId, month, year)).thenReturn(existingExpense);

        // When
        commandMonthlyExpenseService.deductExpense(deductCommand);

        // Then
        ArgumentCaptor<MonthlyExpense> captor = ArgumentCaptor.forClass(MonthlyExpense.class);
        verify(monthlyExpenseRepositoryPort).saveMonthlyExpense(captor.capture());

        MonthlyExpense savedExpense = captor.getValue();
        
        assertEquals(deductAmount.negate().doubleValue(), savedExpense.getTotalAmount().doubleValue());
    }

    @Test
    void recordMonthlyExpense_whenExistingIsNull_shouldSaveNewRecord() {
        // Given
        when(monthlyExpenseRepositoryPort.findByUserIdAndMonth(userId, month, year)).thenReturn(null);

        // When
        commandMonthlyExpenseService.recordMonthlyExpense(recordCommand);

        // Then
        ArgumentCaptor<MonthlyExpense> captor = ArgumentCaptor.forClass(MonthlyExpense.class);
        verify(monthlyExpenseRepositoryPort).saveMonthlyExpense(captor.capture());

        MonthlyExpense savedExpense = captor.getValue();
        assertEquals(recordAmount.doubleValue(), savedExpense.getTotalAmount().doubleValue());
        assertEquals(userId, savedExpense.getUserId());
        assertEquals(month, savedExpense.getMonth());
        assertEquals(year, savedExpense.getYear());

        verify(expenseNotificationPort).sendExpenseNotification(eq(userId),any());
    }

    @Test
    void recordMonthlyExpense_whenExistingExists_shouldUpdateAndSave() {
        // Given
        MonthlyExpenseAnalysis existingExpense = 
                 MonthlyExpenseAnalysis.builder()
                .userId(userId)
                .month(month)
                .year(year)
                .totalAmount(BigDecimal.valueOf(1000))
                .build();
                
        when(monthlyExpenseRepositoryPort.findByUserIdAndMonth(userId, month, year)).thenReturn(existingExpense);

        // When
        commandMonthlyExpenseService.recordMonthlyExpense(recordCommand);

        // Then
        ArgumentCaptor<MonthlyExpense> captor = ArgumentCaptor.forClass(MonthlyExpense.class);
        verify(monthlyExpenseRepositoryPort).saveMonthlyExpense(captor.capture());

        MonthlyExpense savedExpense = captor.getValue();
        // The service passes the DELTA as totalAmount
        assertEquals(recordAmount.doubleValue(), savedExpense.getTotalAmount().doubleValue());
        verify(expenseNotificationPort).sendExpenseNotification(eq(userId),any());

    }

    @Test
    void recordMonthlyExpenses_whenEmptyList_shouldReturnEarly() {
        // When
        commandMonthlyExpenseService.recordMonthlyExpenses(null);
        commandMonthlyExpenseService.recordMonthlyExpenses(Collections.emptyList());

        // Then
        verify(monthlyExpenseRepositoryPort, never()).saveAllMonthlyExpenses(any());
    }

    @SuppressWarnings("unchecked")
    @Test
    void recordMonthlyExpenses_shouldAggregateAndSaveCorrectly() {
        // Given
        Instant time1 = LocalDateTime.of(2024, 6, 10, 10, 0).toInstant(ZoneOffset.UTC);
        Instant time2 = LocalDateTime.of(2024, 6, 15, 10, 0).toInstant(ZoneOffset.UTC); // same user & month
        Instant time3 = LocalDateTime.of(2024, 7, 20, 10, 0).toInstant(ZoneOffset.UTC); // same user, diff month

        RecordExpenseCommand cmd1 = new RecordExpenseCommand(userId, BigDecimal.valueOf(100), time1); // Jun
        RecordExpenseCommand cmd2 = new RecordExpenseCommand(userId, BigDecimal.valueOf(200), time2); // Jun => combined 300
        RecordExpenseCommand cmd3 = new RecordExpenseCommand(userId, BigDecimal.valueOf(150), time3); // Jul

        List<RecordExpenseCommand> commands = List.of(cmd1, cmd2, cmd3);

        // Setup existing for June (exists) and July (doesn't exist)
        MonthlyExpense existingJune = MonthlyExpense.builder()
                .userId(userId)
                .month(6)
                .year(2024)
                .totalAmount(BigDecimal.valueOf(1000))
                .build();
                
        // Return only June in the bulk fetch
        when(monthlyExpenseRepositoryPort.findAllByUserIdAndMonthYearPairs(eq(userId), any()))
                .thenReturn(List.of(existingJune));

        // When
        commandMonthlyExpenseService.recordMonthlyExpenses(commands);

        // Then
        ArgumentCaptor<List<MonthlyExpense>> captor = ArgumentCaptor.forClass(List.class);
        verify(monthlyExpenseRepositoryPort).saveAllMonthlyExpenses(captor.capture());

        List<MonthlyExpense> savedList = captor.getValue();
        assertEquals(2, savedList.size()); // One for June, one for July

        // Find the June record in the saved list
        MonthlyExpense savedJune = savedList.stream().filter(e -> e.getMonth() == 6).findFirst().get();
        assertEquals(userId, savedJune.getUserId());
        assertEquals(BigDecimal.valueOf(300).doubleValue(), savedJune.getTotalAmount().doubleValue()); // delta (100+200)

        // Find the July record in the saved list
        MonthlyExpense savedJuly = savedList.stream().filter(e -> e.getMonth() == 7).findFirst().get();
        assertEquals(userId, savedJuly.getUserId());
        assertEquals(BigDecimal.valueOf(150).doubleValue(), savedJuly.getTotalAmount().doubleValue()); // delta (150)

        // Verify notifications sent for both months
        verify(expenseNotificationPort, times(2)).sendExpenseNotification(eq(userId),any());

    }
}