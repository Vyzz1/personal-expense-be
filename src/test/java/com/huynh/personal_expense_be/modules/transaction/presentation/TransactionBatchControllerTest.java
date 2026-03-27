package com.huynh.personal_expense_be.modules.transaction.presentation;

import com.huynh.personal_expense_be.modules.transaction.application.dto.ImportTransactionCommand;
import com.huynh.personal_expense_be.modules.transaction.application.dto.TransactionBatchResponse;
import com.huynh.personal_expense_be.modules.transaction.application.port.in.GetTransactionBatchUseCase;
import com.huynh.personal_expense_be.modules.transaction.application.port.in.ImportTransactionUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.security.Principal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class TransactionBatchControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ImportTransactionUseCase importTransactionUseCase;

    @Mock
    private GetTransactionBatchUseCase getTransactionBatchUseCase;

    @InjectMocks
    private TransactionBatchController transactionBatchController;

    private final String userId = "user1";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(transactionBatchController).build();
    }

    private Principal mockPrincipal() {
        return () -> userId;
    }

    @Test
    void importTransactions_success() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "transactions.csv",
                MediaType.TEXT_PLAIN_VALUE,
                "date,amount,category,description\n2023-10-01,50.0,Food,Lunch".getBytes());

        TransactionBatchResponse response = new TransactionBatchResponse(
                "batch-123",
                "PROCESSING");

        when(importTransactionUseCase.importTransactions(any(ImportTransactionCommand.class))).thenReturn(response);

        mockMvc.perform(multipart("/api/v1/transactions/batch")
                .file(file)
                .principal(mockPrincipal()))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.message").value("Batch import started"))
                .andExpect(jsonPath("$.data.jobId").value("batch-123"))
                .andExpect(jsonPath("$.data.jobStatus").value("PROCESSING"));

        verify(importTransactionUseCase).importTransactions(any(ImportTransactionCommand.class));
    }

    @Test
    void getBatchImportStatus_success() throws Exception {
        String batchId = "batch-123";
        TransactionBatchResponse response = new TransactionBatchResponse(
                batchId,
                "COMPLETED");

        when(getTransactionBatchUseCase.getBatchImportStatus(eq(batchId))).thenReturn(response);

        mockMvc.perform(get("/api/v1/transactions/batch/{id}", batchId)
                .principal(mockPrincipal()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Batch import status retrieved"))
                .andExpect(jsonPath("$.data.jobId").value(batchId))
                .andExpect(jsonPath("$.data.jobStatus").value("COMPLETED"));

        verify(getTransactionBatchUseCase).getBatchImportStatus(eq(batchId));
    }
}
