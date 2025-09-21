package com.banksyariah.service;

import com.banksyariah.dto.request.AccountCreationRequest;
import com.banksyariah.dto.request.DepositRequest;
import com.banksyariah.dto.request.TransferRequest;
import com.banksyariah.dto.response.AccountResponse;
import com.banksyariah.dto.response.TransactionResponse;
import com.banksyariah.entity.*;
import com.banksyariah.exception.InsufficientBalanceException;
import com.banksyariah.exception.ResourceNotFoundException;
import com.banksyariah.mapper.AccountMapper;
import com.banksyariah.mapper.TransactionMapper;
import com.banksyariah.repository.AccountRepository;
import com.banksyariah.repository.TransactionRepository;
import com.banksyariah.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final AccountMapper accountMapper;
    private final TransactionMapper transactionMapper;

    public AccountResponse createAccount(AccountCreationRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Account account = Account.builder()
                .accountNumber(generateAccountNumber())
                .accountName(request.getAccountName())
                .accountType(request.getAccountType())
                .balance(BigDecimal.ZERO)
                .minimumBalance(request.getMinimumBalance())
                .user(user)
                .status(AccountStatus.ACTIVE)
                .build();

        Account savedAccount = accountRepository.save(account);
        log.info("Account created with number: {}", savedAccount.getAccountNumber());

        return accountMapper.toResponse(savedAccount);
    }

    public TransactionResponse deposit(DepositRequest request) {
        Account account = accountRepository.findByAccountNumber(request.getAccountNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        BigDecimal newBalance = account.getBalance().add(request.getAmount());
        account.setBalance(newBalance);

        Transaction transaction = Transaction.builder()
                .transactionId(generateTransactionId())
                .type(TransactionType.DEPOSIT)
                .amount(request.getAmount())
                .description(request.getDescription())
                .account(account)
                .status(TransactionStatus.SUCCESS)
                .transactionDate(LocalDateTime.now())
                .balanceAfter(newBalance)
                .build();

        accountRepository.save(account);
        Transaction savedTransaction = transactionRepository.save(transaction);

        log.info("Deposit successful. Transaction ID: {}", savedTransaction.getTransactionId());
        return transactionMapper.toResponse(savedTransaction);
    }

    public TransactionResponse transfer(TransferRequest request) {
        Account sourceAccount = accountRepository.findByAccountNumber(request.getSourceAccountNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Source account not found"));

        Account targetAccount = accountRepository.findByAccountNumber(request.getTargetAccountNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Target account not found"));

        if (sourceAccount.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientBalanceException("Insufficient balance");
        }

        // Debit source account
        sourceAccount.setBalance(sourceAccount.getBalance().subtract(request.getAmount()));

        // Credit target account
        targetAccount.setBalance(targetAccount.getBalance().add(request.getAmount()));

        // Create transaction record
        Transaction transaction = Transaction.builder()
                .transactionId(generateTransactionId())
                .type(TransactionType.TRANSFER)
                .amount(request.getAmount())
                .description(request.getDescription())
                .account(sourceAccount)
                .targetAccount(targetAccount)
                .status(TransactionStatus.SUCCESS)
                .transactionDate(LocalDateTime.now())
                .balanceAfter(sourceAccount.getBalance())
                .build();

        accountRepository.save(sourceAccount);
        accountRepository.save(targetAccount);
        Transaction savedTransaction = transactionRepository.save(transaction);

        log.info("Transfer successful. Transaction ID: {}", savedTransaction.getTransactionId());
        return transactionMapper.toResponse(savedTransaction);
    }

    private String generateAccountNumber() {
        return "BSA" + System.currentTimeMillis();
    }

    private String generateTransactionId() {
        return "TRX" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
    }
}