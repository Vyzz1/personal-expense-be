package com.huynh.personal_expense_be.modules.transaction.presentation;

import com.huynh.personal_expense_be.modules.transaction.application.dto.CreateTransactionCommand;
import com.huynh.personal_expense_be.modules.transaction.application.dto.GetTransactionCommand;
import com.huynh.personal_expense_be.modules.transaction.application.dto.PageResult;
import com.huynh.personal_expense_be.modules.transaction.application.dto.TransactionResponse;
import com.huynh.personal_expense_be.modules.category.application.dto.CategoryResponse;
import com.huynh.personal_expense_be.modules.transaction.application.port.in.*;
import com.huynh.personal_expense_be.modules.transaction.domain.TransactionType;
import com.huynh.personal_expense_be.modules.transaction.presentation.request.TransactionRequest;
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
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
public class TransactionControllerTest {

        private MockMvc mockMvc;

        @Mock
        private CreateTransactionUseCase createTransactionUseCase;

        @Mock
        private GetListTransactionUseCase getListTransactionUseCase;

        @Mock
        private GetTransactionDetailUseCase getTransactionDetailUseCase;

        @Mock
        private DeleteTransactionUseCase deleteTransactionUseCase;

        @Mock
        private UpdateTransactionUseCase updateTransactionUseCase;

        @InjectMocks
        private TransactionController transactionController;

        private ObjectMapper objectMapper;

        private final String userId = "user1";
        private final UUID transactionId = UUID.randomUUID();
        private final UUID categoryId = UUID.randomUUID();

        @BeforeEach
        void setUp() {
                mockMvc = MockMvcBuilders.standaloneSetup(transactionController).build();
                objectMapper = new ObjectMapper();
        }

        private Principal mockPrincipal() {
                return () -> userId;
        }

        @Test
        void createTransaction_success() throws Exception {
                TransactionRequest request = new TransactionRequest(
                                "Lunch",
                                BigDecimal.valueOf(50.0),
                                categoryId,
                                Instant.now(),
                                TransactionType.EXPENSE);

                TransactionResponse response = new TransactionResponse(
                                transactionId.toString(),
                                "Lunch",
                                BigDecimal.valueOf(50.0),
                                new CategoryResponse(categoryId, "Food", userId, null, Instant.now(), Instant.now()),
                                request.occurredAt(),
                                TransactionType.EXPENSE.name(),
                                Instant.now(),
                                Instant.now());

                when(createTransactionUseCase.createTransaction(any(CreateTransactionCommand.class)))
                                .thenReturn(response);

                mockMvc.perform(post("/api/v1/transactions")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                                .principal(mockPrincipal()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.message").value("Transaction Created !"))
                                .andExpect(jsonPath("$.data.id").value(transactionId.toString()))
                                .andExpect(jsonPath("$.data.description").value("Lunch"));

                verify(createTransactionUseCase).createTransaction(any(CreateTransactionCommand.class));
        }

        @Test
        void getAllTransactions_success() throws Exception {
                TransactionResponse transactionResponse = new TransactionResponse(
                                transactionId.toString(),
                                "Lunch",
                                BigDecimal.valueOf(50.0),
                                new CategoryResponse(categoryId, "Food", userId, null, Instant.now(), Instant.now()),
                                Instant.now(),
                                TransactionType.EXPENSE.name(),
                                Instant.now(),
                                Instant.now());
                PageResult<TransactionResponse> pageResult = PageResult.of(List.of(transactionResponse), 0, 10, 1, 1,
                                true);

                when(getListTransactionUseCase.getListTransaction(any(GetTransactionCommand.class)))
                                .thenReturn(pageResult);

                mockMvc.perform(get("/api/v1/transactions")
                                .param("page", "0")
                                .param("size", "10")
                                .principal(mockPrincipal()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.message").value("Transactions retrieved successfully!"))
                                .andExpect(jsonPath("$.data.content[0].id").value(transactionId.toString()));

                verify(getListTransactionUseCase).getListTransaction(any(GetTransactionCommand.class));
        }

        @Test
        void getTransactionDetail_success() throws Exception {
                TransactionResponse response = new TransactionResponse(
                                transactionId.toString(),
                                "Lunch",
                                BigDecimal.valueOf(50.0),
                                new CategoryResponse(categoryId, "Food", userId, null, Instant.now(), Instant.now()),
                                Instant.now(),
                                TransactionType.EXPENSE.name(),
                                Instant.now(),
                                Instant.now());

                when(getTransactionDetailUseCase.getTransactionDetailById(transactionId, userId)).thenReturn(response);

                mockMvc.perform(get("/api/v1/transactions/{id}", transactionId)
                                .principal(mockPrincipal()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.message").value("Transaction retrieved successfully !"))
                                .andExpect(jsonPath("$.data.id").value(transactionId.toString()));

                verify(getTransactionDetailUseCase).getTransactionDetailById(transactionId, userId);
        }

        @Test
        void deleteTransaction_success() throws Exception {
                mockMvc.perform(delete("/api/v1/transactions/{id}", transactionId)
                                .principal(mockPrincipal()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.message").value("Transaction deleted successfully !"));

                verify(deleteTransactionUseCase).deleteTransactionById(userId, transactionId);
        }

        @Test
        void updateTransaction_success() throws Exception {
                TransactionRequest request = new TransactionRequest(
                                "Updated Lunch",
                                BigDecimal.valueOf(60.0),
                                categoryId,
                                Instant.now(),
                                TransactionType.EXPENSE);

                TransactionResponse response = new TransactionResponse(
                                transactionId.toString(),
                                "Updated Lunch",
                                BigDecimal.valueOf(60.0),
                                new CategoryResponse(categoryId, "Food", userId, null, Instant.now(), Instant.now()),
                                request.occurredAt(),
                                TransactionType.EXPENSE.name(),
                                Instant.now(),
                                Instant.now());

                when(updateTransactionUseCase.updateTransaction(eq(transactionId), any(CreateTransactionCommand.class)))
                                .thenReturn(response);

                mockMvc.perform(put("/api/v1/transactions/{id}", transactionId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                                .principal(mockPrincipal()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.message").value("Transaction updated successfully !"))
                                .andExpect(jsonPath("$.data.description").value("Updated Lunch"));

                verify(updateTransactionUseCase).updateTransaction(eq(transactionId),
                                any(CreateTransactionCommand.class));
        }
}
