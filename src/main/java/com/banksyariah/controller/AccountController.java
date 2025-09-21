// src/main/java/com/banksyariah/controller/AccountController.java
package com.banksyariah.controller;

import com.banksyariah.dto.request.AccountCreationRequest;
import com.banksyariah.dto.request.DepositRequest;
import com.banksyariah.dto.request.TransferRequest;
import com.banksyariah.dto.response.AccountResponse;
import com.banksyariah.dto.response.ApiResponse;
import com.banksyariah.dto.response.TransactionResponse;
import com.banksyariah.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
@Tag(name = "Account Management", description = "API for managing bank accounts")
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('TELLER')")
    @Operation(summary = "Create new account")
    public ResponseEntity<ApiResponse<AccountResponse>> createAccount(@Valid @RequestBody AccountCreationRequest request) {
        AccountResponse response = accountService.createAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Account created successfully", response));
    }

    @PostMapping("/deposit")
    @PreAuthorize("hasRole('TELLER') or hasRole('ADMIN')")
    @Operation(summary = "Deposit money to account")
    public ResponseEntity<ApiResponse<TransactionResponse>> deposit(@Valid @RequestBody DepositRequest request) {
        TransactionResponse response = accountService.deposit(request);
        return ResponseEntity.ok(ApiResponse.success("Deposit successful", response));
    }

    @PostMapping("/transfer")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Transfer money between accounts")
    public ResponseEntity<ApiResponse<TransactionResponse>> transfer(@Valid @RequestBody TransferRequest request) {
        TransactionResponse response = accountService.transfer(request);
        return ResponseEntity.ok(ApiResponse.success("Transfer successful", response));
    }
}