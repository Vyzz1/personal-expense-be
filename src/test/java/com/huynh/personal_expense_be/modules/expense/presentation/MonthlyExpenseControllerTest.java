package com.huynh.personal_expense_be.modules.expense.presentation;

import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.huynh.personal_expense_be.modules.expense.application.dto.GetMonthlyExpenseCommand;
import com.huynh.personal_expense_be.modules.expense.application.dto.GetMonthlyExpenseResponse;
import com.huynh.personal_expense_be.modules.expense.application.port.in.GetMonthlyExpenseUseCase;
import com.huynh.personal_expense_be.modules.expense.application.port.in.GetThreeMonthCompareUseCase;



@ExtendWith(MockitoExtension.class)
public class MonthlyExpenseControllerTest  {
    

    @InjectMocks
    private MonthlyExpenseController monthlyExpenseController;

    @Mock
    private GetThreeMonthCompareUseCase getThreeMonthCompareUseCase;

    @Mock
    private GetMonthlyExpenseUseCase getMonthlyExpenseUseCase;



    private MockMvc mockMvc;

    private Principal mockPrincipal;

    @BeforeEach
    void setUp() {

        mockMvc = MockMvcBuilders.standaloneSetup(monthlyExpenseController).build();
        mockPrincipal = mock(Principal.class);
        lenient().when(mockPrincipal.getName()).thenReturn("user1");
    }

    @Test
    void getMonthlyExpense_success() throws Exception {
        // Given
        String userId = "user1";
        GetMonthlyExpenseCommand command = new GetMonthlyExpenseCommand(
            userId,
            5, // month
            2024 // year
        );

        GetMonthlyExpenseResponse expectedResponse = new GetMonthlyExpenseResponse(
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(800),
                BigDecimal.valueOf(25),
                Instant.now(),
                5,
                2024
        ); 

        when(getMonthlyExpenseUseCase.getMonthlyExpense(command)).thenReturn(expectedResponse);

        // Then
        this.mockMvc.perform(get("/api/v1/expenses/monthly")
                .param("month", String.valueOf(command.month()))
                .param("year", String.valueOf(command.year()))
                .principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Monthly expense retrieved successfully"))
                .andExpect(jsonPath("$.data.totalAmount").value(expectedResponse.totalAmount().doubleValue()))
                .andExpect(jsonPath("$.data.previousTotalAmount").value(expectedResponse.previousTotalAmount().doubleValue()))
                .andExpect(jsonPath("$.data.changePercentage").value(expectedResponse.changePercentage().doubleValue()))
                .andExpect(jsonPath("$.data.month").value(expectedResponse.month()))
                .andExpect(jsonPath("$.data.year").value(expectedResponse.year()));
    }

    @Test
    void getThreeMonthCompare_success() throws Exception {
        // Given
        String userId = "user1";
        com.huynh.personal_expense_be.modules.expense.application.dto.GetThreeMonthCompareCommand command = 
            new com.huynh.personal_expense_be.modules.expense.application.dto.GetThreeMonthCompareCommand(
                userId,
                5, // month
                2024 // year
        );

        GetMonthlyExpenseResponse expectedResponse1 = new GetMonthlyExpenseResponse(
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(800),
                BigDecimal.valueOf(25),
                Instant.now(),
                5,
                2024
        );
        GetMonthlyExpenseResponse expectedResponse2 = new GetMonthlyExpenseResponse(
                BigDecimal.valueOf(800),
                BigDecimal.valueOf(600),
                BigDecimal.valueOf(33.33),
                Instant.now(),
                4,
                2024
        );

        when(getThreeMonthCompareUseCase.getThreeMonthCompare(command)).thenReturn(java.util.List.of(expectedResponse1, expectedResponse2));

        // Then
        this.mockMvc.perform(get("/api/v1/expenses/compare")
                .param("month", String.valueOf(command.month()))
                .param("year", String.valueOf(command.year()))
                .principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Three month comparison retrieved successfully"))
                .andExpect(jsonPath("$.data[0].totalAmount").value(expectedResponse1.totalAmount().doubleValue()))
                .andExpect(jsonPath("$.data[0].month").value(expectedResponse1.month()))
                .andExpect(jsonPath("$.data[1].totalAmount").value(expectedResponse2.totalAmount().doubleValue()))
                .andExpect(jsonPath("$.data[1].month").value(expectedResponse2.month()));
    }
    
    
}
