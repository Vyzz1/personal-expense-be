package com.huynh.personal_expense_be.modules.expense.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.huynh.personal_expense_be.modules.expense.application.dto.GetMonthlyExpenseCommand;
import com.huynh.personal_expense_be.modules.expense.application.dto.GetMonthlyExpenseResponse;
import com.huynh.personal_expense_be.modules.expense.application.dto.GetThreeMonthCompareCommand;
import com.huynh.personal_expense_be.modules.expense.application.port.out.MonthlyExpenseRepositoryPort;
import com.huynh.personal_expense_be.modules.expense.domain.MonthlyExpense;

@ExtendWith(MockitoExtension.class)
public class QueryMonthExpenseServiceTest {
    
    @Mock
    private MonthlyExpenseRepositoryPort monthlyExpenseRepositoryPort;

    @InjectMocks
    private QueryMonthExpenseService queryMonthExpenseService;

    private String userId;
    private int month;
    private int year;
    private MonthlyExpense mockExpense;

    @BeforeEach
    void setUp() {
        userId = "user1";
        month = 5;
        year = 2024;
        
        mockExpense = MonthlyExpense.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .month(month)
                .year(year)
                .totalAmount(BigDecimal.valueOf(1000))
                .previousTotalAmount(BigDecimal.valueOf(800))
                .changePercentage(BigDecimal.valueOf(25.0))
                .updatedAt(Instant.now())
                .build();
    }

    @Test
    void getMonthlyExpense_whenExpenseExists_returnsResponse() {
        // Given
        GetMonthlyExpenseCommand command = new GetMonthlyExpenseCommand(userId, month, year);
        when(monthlyExpenseRepositoryPort.findByUserIdAndMonth(userId, month, year)).thenReturn(mockExpense);

        // When
        GetMonthlyExpenseResponse response = queryMonthExpenseService.getMonthlyExpense(command);

        // Then
        assertNotNull(response);
        assertEquals(BigDecimal.valueOf(1000), response.totalAmount());
        assertEquals(BigDecimal.valueOf(800), response.previousTotalAmount());
        assertEquals(BigDecimal.valueOf(25.0), response.changePercentage());
        assertEquals(month, response.month());
        assertEquals(year, response.year());
        assertEquals(mockExpense.getUpdatedAt(), response.lastCalculatedAt());
    }

    @Test
    void getMonthlyExpense_whenExpenseIsNull_returnsDefaultResponse() {
        // Given
        GetMonthlyExpenseCommand command = new GetMonthlyExpenseCommand(userId, month, year);
        when(monthlyExpenseRepositoryPort.findByUserIdAndMonth(userId, month, year)).thenReturn(null);

        // When
        GetMonthlyExpenseResponse response = queryMonthExpenseService.getMonthlyExpense(command);

        // Then
        assertNotNull(response);
        assertEquals(BigDecimal.ZERO, response.totalAmount());
        assertEquals(BigDecimal.ZERO, response.previousTotalAmount());
        assertEquals(BigDecimal.ZERO, response.changePercentage());
        assertEquals(month, response.month());
        assertEquals(year, response.year());
        assertNull(response.lastCalculatedAt());
    }

    @Test
    void getThreeMonthCompare_returnsExpenseList() {
        // Given
        GetThreeMonthCompareCommand command = new GetThreeMonthCompareCommand(userId, month, year);
        
        MonthlyExpense priorExpense = MonthlyExpense.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .month(4)
                .year(year)
                .totalAmount(BigDecimal.valueOf(800))
                .previousTotalAmount(BigDecimal.valueOf(600))
                .changePercentage(BigDecimal.valueOf(33.33))
                .updatedAt(Instant.now())
                .build();

        when(monthlyExpenseRepositoryPort.findThreeMonthCompare(userId, month, year))
                .thenReturn(List.of(priorExpense, mockExpense));

        // When
        List<GetMonthlyExpenseResponse> responses = queryMonthExpenseService.getThreeMonthCompare(command);

        // Then
        assertNotNull(responses);
        assertEquals(2, responses.size());
        
        assertEquals(BigDecimal.valueOf(800), responses.get(0).totalAmount());
        assertEquals(4, responses.get(0).month());
        
        assertEquals(BigDecimal.valueOf(1000), responses.get(1).totalAmount());
        assertEquals(month, responses.get(1).month());
    }
}
