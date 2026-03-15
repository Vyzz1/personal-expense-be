package com.huynh.personal_expense_be.modules.transaction.presentation;

import com.huynh.personal_expense_be.modules.transaction.application.dto.ImportTransactionCommand;
import com.huynh.personal_expense_be.modules.transaction.application.port.in.ImportTransactionUseCase;
import com.huynh.personal_expense_be.shared.response.BaseResponse;
import lombok.RequiredArgsConstructor;
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
import java.security.Principal;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/transactions/batch")
@RequiredArgsConstructor
public class TransactionBatchController {

    private final ImportTransactionUseCase importTransactionUseCase;
    private final String UPLOAD_DIR = System.getProperty("java.io.tmpdir");


    @PostMapping
    public ResponseEntity<BaseResponse<?>> importTransactions(@RequestParam("file")MultipartFile file, Principal principal) throws IOException {
        String userId = principal.getName();
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path filePath = Paths.get(UPLOAD_DIR, fileName);
        Files.copy(file.getInputStream(), filePath);

        importTransactionUseCase.importTransactions(new ImportTransactionCommand(userId, filePath.toString()));

        return ResponseEntity.accepted().body(BaseResponse.noData("File uploaded successfully. Import process started."));
    }

}
