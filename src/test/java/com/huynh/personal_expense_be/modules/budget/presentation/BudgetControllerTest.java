package com.huynh.personal_expense_be.modules.budget.presentation;

import com.huynh.personal_expense_be.modules.budget.application.dto.BudgetResponse;
import com.huynh.personal_expense_be.modules.budget.application.dto.CreateBudgetCommand;
import com.huynh.personal_expense_be.modules.budget.application.dto.UpdateBudgetCommand;
import com.huynh.personal_expense_be.modules.budget.application.port.in.CreateBudgetUseCase;
import com.huynh.personal_expense_be.modules.budget.application.port.in.DeleteBudgetUseCase;
import com.huynh.personal_expense_be.modules.budget.application.port.in.GetBudgetUseCase;
import com.huynh.personal_expense_be.modules.budget.application.port.in.UpdateBudgetUseCase;
import com.huynh.personal_expense_be.modules.transaction.application.dto.PageResult;
import com.huynh.personal_expense_be.modules.budget.presentation.request.CreateBudgetRequest;
import com.huynh.personal_expense_be.modules.budget.presentation.request.UpdateBudgetRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import com.fasterxml.jackson.databind.ObjectMapper;
@ExtendWith(MockitoExtension.class)
public class BudgetControllerTest {



    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Mock
    private CreateBudgetUseCase createBudgetUseCase;

    @Mock
    private GetBudgetUseCase getBudgetUseCase;

    @Mock
    private DeleteBudgetUseCase deleteBudgetUseCase;

    @Mock
    private UpdateBudgetUseCase updateBudgetUseCase;

    private Principal mockPrincipal;

    @InjectMocks
    private BudgetController budgetController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(budgetController).build();
        mockPrincipal = mock(Principal.class);
        objectMapper = new ObjectMapper();
        lenient().when(mockPrincipal.getName()).thenReturn("user1");
    }

    @Test
    void create_success() throws Exception {
        UUID categoryId = UUID.randomUUID();
        CreateBudgetRequest request = new CreateBudgetRequest("Food", categoryId, new BigDecimal("500.0"), 60.0f);

        BudgetResponse response = new BudgetResponse(UUID.randomUUID(), "Food", new BigDecimal("500.0"), BigDecimal.ZERO, null, "ACTIVE", "2026-05", null, null, 60.0f);

        when(createBudgetUseCase.createBudget(any(CreateBudgetCommand.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/budgets")
                        .principal(mockPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Budget created"))
                .andExpect(jsonPath("$.data.name").value("Food"))
                .andExpect(jsonPath("$.data.limitAmount").value(500.0));

        verify(createBudgetUseCase).createBudget(any(CreateBudgetCommand.class));
    }

    @Test
    void getAll_success() throws Exception {
        UUID id = UUID.randomUUID();
        BudgetResponse response = new BudgetResponse(id, "Food", new BigDecimal("500.0"), BigDecimal.ZERO, null, "ACTIVE", "2026-05", null, null, 60.0f);

        PageResult<BudgetResponse> pageResult = PageResult.of(List.of(response), 0, 10, 1, 1, true);
        when(getBudgetUseCase.getListBudget(any())).thenReturn(pageResult);

                mockMvc.perform(get("/api/v1/budgets")
                        .principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Budgets retrieved"))
                .andExpect(jsonPath("$.data.content[0].name").value("Food"));

        verify(getBudgetUseCase).getListBudget(any());
    }

    @Test
    void getById_success() throws Exception {
        UUID id = UUID.randomUUID();
        BudgetResponse response = new BudgetResponse(id, "Food", new BigDecimal("500.0"), BigDecimal.ZERO, null, "ACTIVE", "2026-05", null, null, 60.0f);

        when(getBudgetUseCase.getBudgetById("user1", id)).thenReturn(response);

        mockMvc.perform(get("/api/v1/budgets/{id}", id)
                        .principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Budget retrieved"))
                .andExpect(jsonPath("$.data.name").value("Food"));

        verify(getBudgetUseCase).getBudgetById("user1", id);
    }

    @Test
    void getByPeriod_success() throws Exception {
        UUID id = UUID.randomUUID();
        String period = "2026-05";
        BudgetResponse response = new BudgetResponse(id, "Food", new BigDecimal("500.0"), BigDecimal.ZERO, null, "ACTIVE", period, null, null, 60.0f);

        when(getBudgetUseCase.getBudgetsByPeriod("user1", period)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/budgets/period")
                        .param("period", period)
                        .principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Budgets by period"))
                .andExpect(jsonPath("$.data[0].period").value(period));

        verify(getBudgetUseCase).getBudgetsByPeriod("user1", period);
    }

    @Test
    void delete_success() throws Exception {
        UUID id = UUID.randomUUID();

        doNothing().when(deleteBudgetUseCase).deleteBudget("user1", id);

        mockMvc.perform(delete("/api/v1/budgets/{id}", id)
                        .principal(mockPrincipal))
                .andExpect(status().isAccepted());

        verify(deleteBudgetUseCase).deleteBudget("user1", id);
    }

    @Test
    void update_success() throws Exception {
        UUID id = UUID.randomUUID();
        UpdateBudgetRequest request = new UpdateBudgetRequest("Food Updated", new BigDecimal("600.0"), 70.0f);
        BudgetResponse response = new BudgetResponse(id, "Food Updated", new BigDecimal("600.0"), BigDecimal.ZERO, null, "ACTIVE", "2026-05", null, null, 60.0f);

        when(updateBudgetUseCase.updateBudget(any(UpdateBudgetCommand.class))).thenReturn(response);

        mockMvc.perform(put("/api/v1/budgets/{id}", id)
                        .principal(mockPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Budget updated"))
                .andExpect(jsonPath("$.data.name").value("Food Updated"))
                .andExpect(jsonPath("$.data.limitAmount").value(600.0));

        verify(updateBudgetUseCase).updateBudget(any(UpdateBudgetCommand.class));
    }
}
