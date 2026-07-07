package com.huynh.personal_expense_be.modules.transaction.presentation;

import com.huynh.personal_expense_be.modules.transaction.application.dto.ImportTransactionCommand;
import com.huynh.personal_expense_be.modules.transaction.application.dto.TransactionBatchResponse;
import com.huynh.personal_expense_be.modules.transaction.application.port.in.GetTransactionBatchUseCase;
import com.huynh.personal_expense_be.modules.transaction.application.port.in.ImportTransactionUseCase;
import com.huynh.personal_expense_be.shared.exception.BadRequestException;
import com.huynh.personal_expense_be.shared.exception.InternalServerErrorException;
import com.huynh.personal_expense_be.shared.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/transactions/batch")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Transaction Batch Import", description = "Bulk import transactions from CSV file")
@SecurityRequirement(name = "bearerAuth")
public class TransactionBatchController {

    private final ImportTransactionUseCase importTransactionUseCase;
    private final GetTransactionBatchUseCase getTransactionBatchUseCase;
    private final String UPLOAD_DIR = System.getProperty("java.io.tmpdir");

    @Operation(summary = "Import transactions from CSV", description = "Upload a CSV file to bulk-import transactions. Returns a batch job ID to track progress.")
    @ApiResponse(responseCode = "202", description = "Batch import started")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BaseResponse<TransactionBatchResponse>> importTransactions(
            @RequestParam("file") MultipartFile file,
            Principal principal) throws IOException {
        String userId = principal.getName();

        String contentType = file.getContentType();
        if (!"text/csv".equals(contentType) && !"application/vnd.ms-excel".equals(contentType)) {
            throw new BadRequestException("Only CSV files are accepted");
        }

        String originalFilename = Optional.ofNullable(file.getOriginalFilename())
                .filter(name -> !name.isBlank())
                .orElse("upload.tmp");

        String safeFilename = Paths.get(originalFilename)
                .getFileName()
                .toString()
                .replaceAll("[^a-zA-Z0-9._-]", "_");

        if (!safeFilename.toLowerCase().endsWith(".csv")) {
            throw new BadRequestException("File extension must be .csv");
        }

        String fileName = UUID.randomUUID() + "_" + safeFilename;
        Path uploadPath = Paths.get(UPLOAD_DIR).toAbsolutePath().normalize();
        Files.createDirectories(uploadPath);

        Path filePath = uploadPath.resolve(fileName).normalize();
        if (!filePath.startsWith(uploadPath)) {
            throw new InternalServerErrorException();
        }
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        long storedSize = Files.size(filePath);
        log.info("Stored batch import file. userId={}, storedBytes={}", userId, storedSize);

        TransactionBatchResponse response = importTransactionUseCase
                .importTransactions(new ImportTransactionCommand(userId, filePath.toString()));

        return ResponseEntity.accepted()
                .body(BaseResponse.success("Batch import started", response));
    }

    @Operation(summary = "Get batch import status", description = "Poll this endpoint with the job ID returned from the import endpoint")
    @ApiResponse(responseCode = "200", description = "Status retrieved")
    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<TransactionBatchResponse>> getBatchImportStatus(
            @PathVariable("id") String id, Principal principal) {
        TransactionBatchResponse response = getTransactionBatchUseCase.getBatchImportStatus(id, principal.getName());
        return ResponseEntity.ok(BaseResponse.success("Batch import status retrieved", response));
    }
}
