package com.huynh.personal_expense_be.modules.transaction.presentation;

import com.huynh.personal_expense_be.modules.transaction.application.dto.ImportTransactionCommand;
import com.huynh.personal_expense_be.modules.transaction.application.dto.TransactionBatchResponse;
import com.huynh.personal_expense_be.modules.transaction.application.port.in.GetTransactionBatchUseCase;
import com.huynh.personal_expense_be.modules.transaction.application.port.in.ImportTransactionUseCase;
import com.huynh.personal_expense_be.shared.response.BaseResponse;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/api/v1/transactions/batch")
@RequiredArgsConstructor
@Slf4j
public class TransactionBatchController {

        private final ImportTransactionUseCase importTransactionUseCase;
        private final GetTransactionBatchUseCase getTransactionBatchUseCase;
        private final String UPLOAD_DIR = System.getProperty("java.io.tmpdir");

        @PostMapping
        public ResponseEntity<BaseResponse<TransactionBatchResponse>> importTransactions(
                        @RequestParam("file") MultipartFile file,
                        Principal principal) throws IOException {
                String userId = principal.getName();
                String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
                Path filePath = Paths.get(UPLOAD_DIR, fileName);

                Files.createDirectories(filePath.getParent());
                Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                long storedSize = Files.size(filePath);
                log.info("Stored batch import file. userId={}, originalName={}, storedPath={}, bytes={}",
                                                userId, file.getOriginalFilename(), filePath, storedSize);

                TransactionBatchResponse response = importTransactionUseCase
                                .importTransactions(new ImportTransactionCommand(userId, filePath.toString()));

                return ResponseEntity.accepted()
                                .body(BaseResponse.success("Batch import started", response));
        }

        @GetMapping("/{id}")
        public ResponseEntity<BaseResponse<TransactionBatchResponse>> getBatchImportStatus(
                        @PathVariable("id") String id) {
                TransactionBatchResponse response = getTransactionBatchUseCase.getBatchImportStatus(id);
                return ResponseEntity.ok(BaseResponse.success("Batch import status retrieved", response));
        }

}
