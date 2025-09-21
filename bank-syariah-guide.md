# Panduan Lengkap Bank Syariah Aisyah Application

## 📋 Daftar Isi
1. [Persiapan Environment](#1-persiapan-environment)
2. [Setup Backend (Spring Boot)](#2-setup-backend-spring-boot)
3. [Konfigurasi Database PostgreSQL](#3-konfigurasi-database-postgresql)
4. [Implementasi Entity & Repository](#4-implementasi-entity--repository)
5. [Implementasi Service Layer](#5-implementasi-service-layer)
6. [Implementasi REST Controller](#6-implementasi-rest-controller)
7. [Setup Frontend (Next.js)](#7-setup-frontend-nextjs)
8. [Integrasi Frontend-Backend](#8-integrasi-frontend-backend)
9. [Implementasi Security (JWT)](#9-implementasi-security-jwt)
10. [Testing](#10-testing)
11. [Deployment](#11-deployment)

---

## 1. Persiapan Environment

### Prerequisites
- Java JDK 17+
- Node.js 18+
- PostgreSQL 14+
- Maven 3.8+
- Git
- IDE (IntelliJ IDEA / VS Code)

### Tools Installation Commands
```bash
# Verify installations
java --version
node --version
npm --version
psql --version
mvn --version
```

---

## 2. Setup Backend (Spring Boot)

### 2.1 Inisialisasi Project

Buat project Spring Boot menggunakan Spring Initializr atau Maven:

```bash
# Create project directory
mkdir bank-syariah-aisyah
cd bank-syariah-aisyah
mkdir backend frontend
```

### 2.2 Konfigurasi pom.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
        <relativePath/>
    </parent>

    <groupId>com.banksyariah</groupId>
    <artifactId>aisyah-app</artifactId>
    <version>1.0.0</version>
    <packaging>jar</packaging>

    <name>Bank Syariah Aisyah</name>
    <description>Islamic Banking Application</description>

    <properties>
        <java.version>17</java.version>
        <jwt.version>0.11.5</jwt.version>
        <mapstruct.version>1.5.5.Final</mapstruct.version>
    </properties>

    <dependencies>
        <!-- Spring Boot Starters -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
        
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>

        <!-- Database -->
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>

        <!-- JWT -->
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-api</artifactId>
            <version>${jwt.version}</version>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-impl</artifactId>
            <version>${jwt.version}</version>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-jackson</artifactId>
            <version>${jwt.version}</version>
            <scope>runtime</scope>
        </dependency>

        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>

        <!-- MapStruct -->
        <dependency>
            <groupId>org.mapstruct</groupId>
            <artifactId>mapstruct</artifactId>
            <version>${mapstruct.version}</version>
        </dependency>

        <!-- Swagger/OpenAPI -->
        <dependency>
            <groupId>org.springdoc</groupId>
            <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
            <version>2.2.0</version>
        </dependency>

        <!-- Testing -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        
        <dependency>
            <groupId>org.springframework.security</groupId>
            <artifactId>spring-security-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
            
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.11.0</version>
                <configuration>
                    <source>17</source>
                    <target>17</target>
                    <annotationProcessorPaths>
                        <path>
                            <groupId>org.mapstruct</groupId>
                            <artifactId>mapstruct-processor</artifactId>
                            <version>${mapstruct.version}</version>
                        </path>
                        <path>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                            <version>${lombok.version}</version>
                        </path>
                        <path>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok-mapstruct-binding</artifactId>
                            <version>0.2.0</version>
                        </path>
                    </annotationProcessorPaths>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

---

## 3. Konfigurasi Database PostgreSQL

### 3.1 Setup Database

```sql
-- Create database
CREATE DATABASE bank_syariah_aisyah;

-- Create user
CREATE USER aisyah_admin WITH PASSWORD 'SecurePass123!';

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE bank_syariah_aisyah TO aisyah_admin;
```

### 3.2 application.yml Configuration

```yaml
# src/main/resources/application.yml
spring:
  application:
    name: bank-syariah-aisyah
  
  datasource:
    url: jdbc:postgresql://localhost:5432/bank_syariah_aisyah
    username: aisyah_admin
    password: SecurePass123!
    driver-class-name: org.postgresql.Driver
  
  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
    show-sql: false
  
  security:
    jwt:
      secret-key: 404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
      expiration: 86400000 # 24 hours
      refresh-token:
        expiration: 604800000 # 7 days

server:
  port: 8080
  servlet:
    context-path: /api

springdoc:
  api-docs:
    path: /v3/api-docs
  swagger-ui:
    path: /swagger-ui.html

logging:
  level:
    com.banksyariah: DEBUG
    org.springframework.security: DEBUG
```

---

## 4. Implementasi Entity & Repository

### 4.1 Base Entity

```java
// src/main/java/com/banksyariah/entity/BaseEntity.java
package com.banksyariah.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Data
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "is_deleted")
    private boolean deleted = false;
}
```

### 4.2 User Entity

```java
// src/main/java/com/banksyariah/entity/User.java
package com.banksyariah.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class User extends BaseEntity implements UserDetails {
    
    @Column(unique = true, nullable = false)
    private String username;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(nullable = false)
    private String password;
    
    @Column(name = "full_name", nullable = false)
    private String fullName;
    
    @Column(name = "phone_number")
    private String phoneNumber;
    
    @Column(columnDefinition = "TEXT")
    private String address;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status = UserStatus.ACTIVE;
    
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Account> accounts;
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
            .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
            .collect(Collectors.toList());
    }
    
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    
    @Override
    public boolean isAccountNonLocked() {
        return status != UserStatus.BLOCKED;
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    
    @Override
    public boolean isEnabled() {
        return status == UserStatus.ACTIVE;
    }
}

enum UserStatus {
    ACTIVE, INACTIVE, BLOCKED
}
```

### 4.3 Account Entity

```java
// src/main/java/com/banksyariah/entity/Account.java
package com.banksyariah.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "accounts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Account extends BaseEntity {
    
    @Column(name = "account_number", unique = true, nullable = false)
    private String accountNumber;
    
    @Column(name = "account_name", nullable = false)
    private String accountName;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountType accountType;
    
    @Column(precision = 19, scale = 2, nullable = false)
    private BigDecimal balance = BigDecimal.ZERO;
    
    @Column(name = "minimum_balance", precision = 19, scale = 2)
    private BigDecimal minimumBalance = BigDecimal.ZERO;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountStatus status = AccountStatus.ACTIVE;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Transaction> transactions;
}

enum AccountType {
    WADIAH, // Simpanan/Titipan
    MUDHARABAH, // Investasi bagi hasil
    MUSYARAKAH // Kerjasama modal
}

enum AccountStatus {
    ACTIVE, INACTIVE, BLOCKED, CLOSED
}
```

### 4.4 Transaction Entity

```java
// src/main/java/com/banksyariah/entity/Transaction.java
package com.banksyariah.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Transaction extends BaseEntity {
    
    @Column(name = "transaction_id", unique = true, nullable = false)
    private String transactionId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;
    
    @Column(precision = 19, scale = 2, nullable = false)
    private BigDecimal amount;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "reference_number")
    private String referenceNumber;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_account_id")
    private Account targetAccount;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status = TransactionStatus.PENDING;
    
    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate = LocalDateTime.now();
    
    @Column(name = "balance_after", precision = 19, scale = 2)
    private BigDecimal balanceAfter;
}

enum TransactionType {
    DEPOSIT, // Setoran
    WITHDRAWAL, // Penarikan
    TRANSFER, // Transfer
    ZAKAT, // Pembayaran zakat
    INFAQ, // Infaq/sedekah
    PROFIT_SHARING // Bagi hasil
}

enum TransactionStatus {
    PENDING, SUCCESS, FAILED, CANCELLED
}
```

### 4.5 Repository Interfaces

```java
// src/main/java/com/banksyariah/repository/UserRepository.java
package com.banksyariah.repository;

import com.banksyariah.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    
    @Query("SELECT u FROM User u WHERE u.deleted = false AND (u.username = ?1 OR u.email = ?1)")
    Optional<User> findByUsernameOrEmail(String usernameOrEmail);
}

// src/main/java/com/banksyariah/repository/AccountRepository.java
package com.banksyariah.repository;

import com.banksyariah.entity.Account;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, String> {
    Optional<Account> findByAccountNumber(String accountNumber);
    Page<Account> findByUserId(String userId, Pageable pageable);
    boolean existsByAccountNumber(String accountNumber);
}

// src/main/java/com/banksyariah/repository/TransactionRepository.java
package com.banksyariah.repository;

import com.banksyariah.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {
    Page<Transaction> findByAccountId(String accountId, Pageable pageable);
    
    @Query("SELECT t FROM Transaction t WHERE t.account.id = ?1 AND t.transactionDate BETWEEN ?2 AND ?3")
    Page<Transaction> findByAccountIdAndDateRange(String accountId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
}
```

---

## 5. Implementasi Service Layer

### 5.1 User Service

```java
// src/main/java/com/banksyariah/service/UserService.java
package com.banksyariah.service;

import com.banksyariah.dto.request.UserRegistrationRequest;
import com.banksyariah.dto.request.UserUpdateRequest;
import com.banksyariah.dto.response.UserResponse;
import com.banksyariah.entity.User;
import com.banksyariah.exception.ResourceNotFoundException;
import com.banksyariah.exception.DuplicateResourceException;
import com.banksyariah.mapper.UserMapper;
import com.banksyariah.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    
    public UserResponse createUser(UserRegistrationRequest request) {
        log.debug("Creating new user with username: {}", request.getUsername());
        
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username already exists");
        }
        
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already exists");
        }
        
        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        
        User savedUser = userRepository.save(user);
        log.info("User created successfully with ID: {}", savedUser.getId());
        
        return userMapper.toResponse(savedUser);
    }
    
    @Transactional(readOnly = true)
    public UserResponse getUserById(String id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
        return userMapper.toResponse(user);
    }
    
    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
            .map(userMapper::toResponse);
    }
    
    public UserResponse updateUser(String id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
        
        userMapper.updateEntityFromRequest(request, user);
        
        User updatedUser = userRepository.save(user);
        log.info("User updated successfully with ID: {}", updatedUser.getId());
        
        return userMapper.toResponse(updatedUser);
    }
    
    public void deleteUser(String id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
        
        user.setDeleted(true);
        userRepository.save(user);
        log.info("User soft deleted with ID: {}", id);
    }
}
```

### 5.2 Account Service

```java
// src/main/java/com/banksyariah/service/AccountService.java
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
```

---

## 6. Implementasi REST Controller

### 6.1 User Controller

```java
// src/main/java/com/banksyariah/controller/UserController.java
package com.banksyariah.controller;

import com.banksyariah.dto.request.UserRegistrationRequest;
import com.banksyariah.dto.request.UserUpdateRequest;
import com.banksyariah.dto.response.ApiResponse;
import com.banksyariah.dto.response.UserResponse;
import com.banksyariah.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "API for managing users")
public class UserController {
    
    private final UserService userService;
    
    @PostMapping
    @Operation(summary = "Create new user")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(@Valid @RequestBody UserRegistrationRequest request) {
        UserResponse response = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("User created successfully", response));
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    @Operation(summary = "Get user by ID")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable String id) {
        UserResponse response = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success("User retrieved successfully", response));
    }
    
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all users with pagination")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getAllUsers(Pageable pageable) {
        Page<UserResponse> response = userService.getAllUsers(pageable);
        return ResponseEntity.ok(ApiResponse.success("Users retrieved successfully", response));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    @Operation(summary = "Update user")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable String id,
            @Valid @RequestBody UserUpdateRequest request) {
        UserResponse response = userService.updateUser(id, request);
        return ResponseEntity.ok(ApiResponse.success("User updated successfully", response));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete user")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable String id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success("User deleted successfully", null));
    }
}
```

### 6.2 Account Controller

```java
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
```

### 6.3 DTO Classes

```java
// src/main/java/com/banksyariah/dto/response/ApiResponse.java
package com.banksyariah.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private LocalDateTime timestamp;
    
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
            .success(true)
            .message(message)
            .data(data)
            .timestamp(LocalDateTime.now())
            .build();
    }
    
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
            .success(false)
            .message(message)
            .timestamp(LocalDateTime.now())
            .build();
    }
}

// src/main/java/com/banksyariah/dto/request/UserRegistrationRequest.java
package com.banksyariah.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UserRegistrationRequest {
    @NotBlank(message = "Username is required")
    @Size(min = 4, max = 20)
    private String username;
    
    @NotBlank(message = "Email is required")
    @Email
    private String email;
    
    @NotBlank(message = "Password is required")
    @Size(min = 8)
    private String password;
    
    @NotBlank(message = "Full name is required")
    private String fullName;
    
    @Pattern(regexp = "^[+]?[(]?[0-9]{3}[)]?[-\\s.]?[0-9]{3}[-\\s.]?[0-9]{4,6}$")
    private String phoneNumber;
    
    private String address;
}
```

---

## 7. Setup Frontend (Next.js)

### 7.1 Initialize Next.js Project

```bash
cd frontend
npx create-next-app@latest . --typescript --tailwind --app
npm install axios @tanstack/react-query zustand react-hook-form 
npm install @hookform/resolvers yup dayjs
npm install --save-dev @types/node
```

### 7.2 Project Structure

```
frontend/
├── app/
│   ├── layout.tsx
│   ├── page.tsx
│   ├── (auth)/
│   │   ├── login/
│   │   │   └── page.tsx
│   │   └── register/
│   │       └── page.tsx
│   ├── dashboard/
│   │   ├── layout.tsx
│   │   └── page.tsx
│   └── accounts/
│       └── page.tsx
├── components/
│   ├── ui/
│   ├── forms/
│   └── layouts/
├── lib/
│   ├── api/
│   ├── hooks/
│   └── utils/
├── store/
│   └── auth.store.ts
└── types/
    └── index.ts
```

### 7.3 API Configuration

```typescript
// lib/api/axios.config.ts
import axios from 'axios';
import { useAuthStore } from '@/store/auth.store';

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api';

const axiosInstance = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor
axiosInstance.interceptors.request.use(
  (config) => {
    const token = useAuthStore.getState().token;
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response interceptor
axiosInstance.interceptors.response.use(
  (response) => response,
  async (error) => {
    if (error.response?.status === 401) {
      useAuthStore.getState().logout();
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default axiosInstance;
```

### 7.4 Authentication Store (Zustand)

```typescript
// store/auth.store.ts
import { create } from 'zustand';
import { persist } from 'zustand/middleware';

interface User {
  id: string;
  username: string;
  email: string;
  fullName: string;
  roles: string[];
}

interface AuthState {
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;
  login: (user: User, token: string) => void;
  logout: () => void;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      user: null,
      token: null,
      isAuthenticated: false,
      login: (user, token) => set({ user, token, isAuthenticated: true }),
      logout: () => set({ user: null, token: null, isAuthenticated: false }),
    }),
    {
      name: 'auth-storage',
    }
  )
);
```

### 7.5 Login Component

```typescript
// app/(auth)/login/page.tsx
'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { useForm } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';
import * as yup from 'yup';
import axiosInstance from '@/lib/api/axios.config';
import { useAuthStore } from '@/store/auth.store';

const schema = yup.object({
  username: yup.string().required('Username is required'),
  password: yup.string().required('Password is required'),
});

type LoginFormData = yup.InferType<typeof schema>;

export default function LoginPage() {
  const router = useRouter();
  const { login } = useAuthStore();
  const [isLoading, setIsLoading] = useState(false);

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<LoginFormData>({
    resolver: yupResolver(schema),
  });

  const onSubmit = async (data: LoginFormData) => {
    setIsLoading(true);
    try {
      const response = await axiosInstance.post('/auth/login', data);
      const { user, token } = response.data.data;
      login(user, token);
      router.push('/dashboard');
    } catch (error) {
      console.error('Login failed:', error);
      alert('Login failed. Please check your credentials.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-emerald-50 to-teal-100">
      <div className="bg-white p-8 rounded-2xl shadow-xl w-full max-w-md">
        <h2 className="text-3xl font-bold text-center mb-8 text-emerald-800">
          Bank Syariah Aisyah
        </h2>
        
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Username
            </label>
            <input
              {...register('username')}
              type="text"
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-emerald-500 focus:border-transparent"
              placeholder="Enter your username"
            />
            {errors.username && (
              <p className="mt-1 text-sm text-red-600">{errors.username.message}</p>
            )}
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Password
            </label>
            <input
              {...register('password')}
              type="password"
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-emerald-500 focus:border-transparent"
              placeholder="Enter your password"
            />
            {errors.password && (
              <p className="mt-1 text-sm text-red-600">{errors.password.message}</p>
            )}
          </div>

          <button
            type="submit"
            disabled={isLoading}
            className="w-full bg-emerald-600 text-white py-3 px-4 rounded-lg hover:bg-emerald-700 transition duration-200 font-semibold disabled:opacity-50"
          >
            {isLoading ? 'Logging in...' : 'Login'}
          </button>
        </form>

        <p className="mt-6 text-center text-sm text-gray-600">
          Don't have an account?{' '}
          <a href="/register" className="text-emerald-600 hover:underline">
            Register here
          </a>
        </p>
      </div>
    </div>
  );
}
```

---

## 8. Integrasi Frontend-Backend

### 8.1 API Service Layer

```typescript
// lib/api/services/user.service.ts
import axiosInstance from '../axios.config';
import { User, UserRegistrationRequest } from '@/types';

export const userService = {
  register: async (data: UserRegistrationRequest) => {
    const response = await axiosInstance.post('/users', data);
    return response.data;
  },

  getProfile: async (id: string) => {
    const response = await axiosInstance.get(`/users/${id}`);
    return response.data;
  },

  updateProfile: async (id: string, data: Partial<User>) => {
    const response = await axiosInstance.put(`/users/${id}`, data);
    return response.data;
  },
};

// lib/api/services/account.service.ts
import axiosInstance from '../axios.config';
import { Account, Transaction } from '@/types';

export const accountService = {
  getAccounts: async () => {
    const response = await axiosInstance.get('/accounts');
    return response.data;
  },

  createAccount: async (data: any) => {
    const response = await axiosInstance.post('/accounts', data);
    return response.data;
  },

  deposit: async (data: any) => {
    const response = await axiosInstance.post('/accounts/deposit', data);
    return response.data;
  },

  transfer: async (data: any) => {
    const response = await axiosInstance.post('/accounts/transfer', data);
    return response.data;
  },

  getTransactions: async (accountId: string) => {
    const response = await axiosInstance.get(`/accounts/${accountId}/transactions`);
    return response.data;
  },
};
```

### 8.2 React Query Setup

```typescript
// app/providers.tsx
'use client';

import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ReactQueryDevtools } from '@tanstack/react-query-devtools';
import { useState } from 'react';

export function Providers({ children }: { children: React.ReactNode }) {
  const [queryClient] = useState(
    () =>
      new QueryClient({
        defaultOptions: {
          queries: {
            staleTime: 60 * 1000,
            refetchOnWindowFocus: false,
          },
        },
      })
  );

  return (
    <QueryClientProvider client={queryClient}>
      {children}
      <ReactQueryDevtools initialIsOpen={false} />
    </QueryClientProvider>
  );
}
```

---

## 9. Implementasi Security (JWT)

### 9.1 JWT Configuration

```java
// src/main/java/com/banksyariah/security/JwtService.java
package com.banksyariah.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${spring.security.jwt.secret-key}")
    private String secretKey;

    @Value("${spring.security.jwt.expiration}")
    private long jwtExpiration;

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return buildToken(extraClaims, userDetails, jwtExpiration);
    }

    private String buildToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails,
            long expiration
    ) {
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
```

### 9.2 Security Configuration

```java
// src/main/java/com/banksyariah/config/SecurityConfig.java
package com.banksyariah.config;

import com.banksyariah.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(authz -> authz
                .requestMatchers(
                    "/auth/**",
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/swagger-ui.html"
                ).permitAll()
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authenticationProvider(authenticationProvider)
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:3000"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
```

---

## 10. Testing

### 10.1 Unit Testing (Backend)

```java
// src/test/java/com/banksyariah/service/UserServiceTest.java
package com.banksyariah.service;

import com.banksyariah.dto.request.UserRegistrationRequest;
import com.banksyariah.entity.User;
import com.banksyariah.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private UserRegistrationRequest registrationRequest;

    @BeforeEach
    void setUp() {
        registrationRequest = new UserRegistrationRequest();
        registrationRequest.setUsername("testuser");
        registrationRequest.setEmail("test@example.com");
        registrationRequest.setPassword("password123");
        registrationRequest.setFullName("Test User");
    }

    @Test
    void createUser_Success() {
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(new User());

        assertDoesNotThrow(() -> userService.createUser(registrationRequest));
        verify(userRepository, times(1)).save(any(User.class));
    }
}
```

### 10.2 Integration Testing

```java
// src/test/java/com/banksyariah/controller/UserControllerIntegrationTest.java
package com.banksyariah.controller;

import com.banksyariah.dto.request.UserRegistrationRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createUser_ReturnsCreated() throws Exception {
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setUsername("integrationtest");
        request.setEmail("integration@test.com");
        request.setPassword("TestPass123!");
        request.setFullName("Integration Test");

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }
}
```

---

## 11. Deployment

### 11.1 Docker Configuration

```dockerfile
# backend/Dockerfile
FROM openjdk:17-jdk-slim AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN apt-get update && apt-get install -y maven
RUN mvn clean package -DskipTests

FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

```dockerfile
# frontend/Dockerfile
FROM node:18-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

FROM node:18-alpine
WORKDIR /app
COPY --from=build /app/.next ./.next
COPY --from=build /app/public ./public
COPY --from=build /app/package*.json ./
RUN npm ci --only=production
EXPOSE 3000
CMD ["npm", "start"]
```

### 11.2 Docker Compose

```yaml
# docker-compose.yml
version: '3.8'

services:
  postgres:
    image: postgres:15-alpine
    container_name: bsa-postgres
    environment:
      POSTGRES_DB: bank_syariah_aisyah
      POSTGRES_USER: aisyah_admin
      POSTGRES_PASSWORD: SecurePass123!
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
    networks:
      - bsa-network

  backend:
    build:
      context: ./backend
      dockerfile: Dockerfile
    container_name: bsa-backend
    depends_on:
      - postgres
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/bank_syariah_aisyah
      SPRING_DATASOURCE_USERNAME: aisyah_admin
      SPRING_DATASOURCE_PASSWORD: SecurePass123!
    ports:
      - "8080:8080"
    networks:
      - bsa-network

  frontend:
    build:
      context: ./frontend
      dockerfile: Dockerfile
    container_name: bsa-frontend
    depends_on:
      - backend
    environment:
      NEXT_PUBLIC_API_URL: http://localhost:8080/api
    ports:
      - "3000:3000"
    networks:
      - bsa-network

volumes:
  postgres_data:

networks:
  bsa-network:
    driver: bridge
```

### 11.3 GitHub Actions CI/CD

```yaml
# .github/workflows/deploy.yml
name: CI/CD Pipeline

on:
  push:
    branches: [main]
  pull_request:
    branches: [main]

jobs:
  test-backend:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
      - name: Run tests
        run: |
          cd backend
          mvn clean test

  test-frontend:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Setup Node.js
        uses: actions/setup-node@v3
        with:
          node-version: '18'
      - name: Install dependencies
        run: |
          cd frontend
          npm ci
      - name: Run tests
        run: |
          cd frontend
          npm run test

  deploy:
    needs: [test-backend, test-frontend]
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/main'
    steps:
      - uses: actions/checkout@v3
      - name: Deploy to server
        run: |
          echo "Deploy to production server"
          # Add deployment commands here
```

### 11.4 Environment Variables Production

```bash
# .env.production
# Backend
DATABASE_URL=postgresql://prod_user:prod_pass@db.example.com:5432/bank_syariah_prod
JWT_SECRET=your-production-secret-key-here
SPRING_PROFILES_ACTIVE=production

# Frontend
NEXT_PUBLIC_API_URL=https://api.banksyariahaisyah.com
NEXT_PUBLIC_APP_URL=https://banksyariahaisyah.com
```

### 11.5 Monitoring & Logging

```yaml
# docker-compose.monitoring.yml
version: '3.8'

services:
  prometheus:
    image: prom/prometheus
    container_name: prometheus
    volumes:
      - ./prometheus.yml:/etc/prometheus/prometheus.yml
      - prometheus_data:/prometheus
    ports:
      - "9090:9090"
    networks:
      - bsa-network

  grafana:
    image: grafana/grafana
    container_name: grafana
    environment:
      - GF_SECURITY_ADMIN_PASSWORD=admin
    volumes:
      - grafana_data:/var/lib/grafana
    ports:
      - "3001:3000"
    networks:
      - bsa-network

  elasticsearch:
    image: docker.elastic.co/elasticsearch/elasticsearch:8.11.0
    container_name: elasticsearch
    environment:
      - discovery.type=single-node
      - xpack.security.enabled=false
    volumes:
      - elastic_data:/usr/share/elasticsearch/data
    ports:
      - "9200:9200"
    networks:
      - bsa-network

volumes:
  prometheus_data:
  grafana_data:
  elastic_data:

networks:
  bsa-network:
    external: true
```

---

## 📚 Additional Resources

### Commands Cheat Sheet

```bash
# Backend
cd backend
mvn clean install
mvn spring-boot:run
mvn test

# Frontend
cd frontend
npm install
npm run dev
npm run build
npm start

# Docker
docker-compose up -d
docker-compose down
docker-compose logs -f

# Database backup
pg_dump -U aisyah_admin -h localhost bank_syariah_aisyah > backup.sql
```

### Security Best Practices

1. **Password Policy**: Minimal 8 karakter, kombinasi huruf, angka, simbol
2. **Rate Limiting**: Implementasi rate limiting untuk API
3. **HTTPS**: Selalu gunakan HTTPS di production
4. **Environment Variables**: Jangan commit secret keys
5. **Regular Updates**: Update dependencies secara berkala
6. **Audit Logging**: Log semua aktivitas penting
7. **Backup Strategy**: Backup database regular

### Performance Optimization

1. **Database Indexing**: Index pada kolom yang sering di-query
2. **Caching**: Implementasi Redis untuk caching
3. **Connection Pooling**: Konfigurasi HikariCP
4. **Lazy Loading**: Gunakan lazy loading untuk relasi
5. **Pagination**: Implementasi pagination untuk list data
6. **CDN**: Gunakan CDN untuk static assets
7. **Code Splitting**: Implementasi code splitting di Next.js

---

## 📌 Troubleshooting Guide

### Common Issues & Solutions

#### 1. Database Connection Error
```bash
# Check PostgreSQL status
sudo systemctl status postgresql

# Restart PostgreSQL
sudo systemctl restart postgresql

# Check connection
psql -U aisyah_admin -d bank_syariah_aisyah -h localhost
```

#### 2. JWT Token Invalid
```java
// Verify secret key format
// Ensure the key is Base64 encoded and at least 256 bits
String secretKey = Base64.getEncoder()
    .encodeToString("your-256-bit-secret-key-here".getBytes());
```

#### 3. CORS Issues
```typescript
// frontend/next.config.js
module.exports = {
  async rewrites() {
    return [
      {
        source: '/api/:path*',
        destination: 'http://localhost:8080/api/:path*',
      },
    ];
  },
};
```

#### 4. Build Failures
```bash
# Clean build
cd backend
mvn clean
mvn install -DskipTests

cd ../frontend
rm -rf node_modules .next
npm install
npm run build
```

---

## 🎯 Project Milestones

### Phase 1: Foundation (Week 1-2)
- [x] Setup project structure
- [x] Configure database
- [x] Implement basic entities
- [x] Setup authentication

### Phase 2: Core Features (Week 3-4)
- [x] User management CRUD
- [x] Account management
- [x] Transaction processing
- [x] Frontend integration

### Phase 3: Advanced Features (Week 5-6)
- [ ] Zakat calculator
- [ ] Profit sharing calculation
- [ ] Report generation
- [ ] Notification system

### Phase 4: Deployment (Week 7)
- [x] Docker setup
- [x] CI/CD pipeline
- [ ] Production deployment
- [ ] Monitoring setup

### Phase 5: Optimization (Week 8)
- [ ] Performance testing
- [ ] Security audit
- [ ] Load testing
- [ ] Documentation completion

---

## 📊 Database Schema Diagram

```sql
-- Complete Database Schema

-- Role table
CREATE TABLE roles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Users table with Islamic banking specific fields
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(20),
    address TEXT,
    identity_number VARCHAR(50), -- KTP/NIK
    occupation VARCHAR(100),
    monthly_income DECIMAL(19,2),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    is_deleted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- User roles junction table
CREATE TABLE user_roles (
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    role_id UUID REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

-- Accounts table
CREATE TABLE accounts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_number VARCHAR(20) NOT NULL UNIQUE,
    account_name VARCHAR(100) NOT NULL,
    account_type VARCHAR(50) NOT NULL, -- WADIAH, MUDHARABAH, MUSYARAKAH
    balance DECIMAL(19,2) DEFAULT 0,
    minimum_balance DECIMAL(19,2) DEFAULT 0,
    profit_sharing_ratio DECIMAL(5,2), -- For Mudharabah accounts
    status VARCHAR(20) DEFAULT 'ACTIVE',
    user_id UUID NOT NULL REFERENCES users(id),
    is_deleted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Transactions table
CREATE TABLE transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    transaction_id VARCHAR(50) NOT NULL UNIQUE,
    type VARCHAR(50) NOT NULL,
    amount DECIMAL(19,2) NOT NULL,
    description TEXT,
    reference_number VARCHAR(100),
    account_id UUID NOT NULL REFERENCES accounts(id),
    target_account_id UUID REFERENCES accounts(id),
    status VARCHAR(20) DEFAULT 'PENDING',
    transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    balance_after DECIMAL(19,2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Zakat records
CREATE TABLE zakat_records (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id),
    zakat_type VARCHAR(50) NOT NULL, -- MAAL, FITRAH, PROFESI
    amount DECIMAL(19,2) NOT NULL,
    calculation_basis DECIMAL(19,2),
    payment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Profit sharing distributions
CREATE TABLE profit_distributions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id UUID NOT NULL REFERENCES accounts(id),
    period_start DATE NOT NULL,
    period_end DATE NOT NULL,
    profit_amount DECIMAL(19,2) NOT NULL,
    distribution_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for better performance
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_accounts_user_id ON accounts(user_id);
CREATE INDEX idx_accounts_number ON accounts(account_number);
CREATE INDEX idx_transactions_account_id ON transactions(account_id);
CREATE INDEX idx_transactions_date ON transactions(transaction_date);

-- Insert default roles
INSERT INTO roles (name, description) VALUES 
('ADMIN', 'System Administrator'),
('TELLER', 'Bank Teller'),
('CUSTOMER', 'Regular Customer'),
('MANAGER', 'Branch Manager');
```

---

## 🚀 API Documentation

### Authentication Endpoints

#### POST /api/auth/login
```json
// Request
{
  "username": "user123",
  "password": "SecurePass123!"
}

// Response
{
  "success": true,
  "message": "Login successful",
  "data": {
    "user": {
      "id": "uuid",
      "username": "user123",
      "email": "user@example.com",
      "fullName": "John Doe",
      "roles": ["CUSTOMER"]
    },
    "token": "eyJhbGciOiJIUzI1NiIs...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIs..."
  }
}
```

#### POST /api/auth/refresh
```json
// Request
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIs..."
}

// Response
{
  "success": true,
  "message": "Token refreshed",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIs...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIs..."
  }
}
```

### Account Endpoints

#### GET /api/accounts
```json
// Response
{
  "success": true,
  "message": "Accounts retrieved",
  "data": [
    {
      "id": "uuid",
      "accountNumber": "BSA1234567890",
      "accountName": "Tabungan Wadiah",
      "accountType": "WADIAH",
      "balance": 5000000.00,
      "status": "ACTIVE"
    }
  ]
}
```

#### POST /api/accounts/transfer
```json
// Request
{
  "sourceAccountNumber": "BSA1234567890",
  "targetAccountNumber": "BSA0987654321",
  "amount": 100000,
  "description": "Transfer untuk pembayaran",
  "pin": "123456"
}

// Response
{
  "success": true,
  "message": "Transfer successful",
  "data": {
    "transactionId": "TRX123ABC456",
    "amount": 100000,
    "sourceAccount": "BSA1234567890",
    "targetAccount": "BSA0987654321",
    "timestamp": "2024-01-01T10:00:00Z",
    "status": "SUCCESS"
  }
}
```

### Zakat Calculator Endpoint

#### POST /api/zakat/calculate
```json
// Request
{
  "type": "MAAL", // MAAL, FITRAH, PROFESI
  "assets": {
    "cash": 50000000,
    "gold": 20000000,
    "stocks": 30000000,
    "property": 0
  },
  "debts": 5000000
}

// Response
{
  "success": true,
  "message": "Zakat calculated",
  "data": {
    "totalAssets": 100000000,
    "totalDebts": 5000000,
    "netAssets": 95000000,
    "nisab": 85000000,
    "isZakatRequired": true,
    "zakatAmount": 2375000, // 2.5% of net assets
    "calculation": {
      "formula": "2.5% x (Total Assets - Debts)",
      "percentage": 0.025
    }
  }
}
```

---

## 📱 Frontend Components Structure

### Reusable Components

```typescript
// components/ui/Button.tsx
import { ButtonHTMLAttributes, FC } from 'react';
import { cva, type VariantProps } from 'class-variance-authority';

const buttonVariants = cva(
  'inline-flex items-center justify-center rounded-lg font-medium transition-colors',
  {
    variants: {
      variant: {
        primary: 'bg-emerald-600 text-white hover:bg-emerald-700',
        secondary: 'bg-gray-200 text-gray-900 hover:bg-gray-300',
        danger: 'bg-red-600 text-white hover:bg-red-700',
        ghost: 'hover:bg-gray-100',
      },
      size: {
        sm: 'px-3 py-1.5 text-sm',
        md: 'px-4 py-2',
        lg: 'px-6 py-3 text-lg',
      },
    },
    defaultVariants: {
      variant: 'primary',
      size: 'md',
    },
  }
);

interface ButtonProps
  extends ButtonHTMLAttributes<HTMLButtonElement>,
    VariantProps<typeof buttonVariants> {
  isLoading?: boolean;
}

export const Button: FC<ButtonProps> = ({
  className,
  variant,
  size,
  isLoading,
  children,
  disabled,
  ...props
}) => {
  return (
    <button
      className={buttonVariants({ variant, size, className })}
      disabled={disabled || isLoading}
      {...props}
    >
      {isLoading ? (
        <span className="flex items-center gap-2">
          <svg className="animate-spin h-4 w-4" viewBox="0 0 24 24">
            <circle
              className="opacity-25"
              cx="12"
              cy="12"
              r="10"
              stroke="currentColor"
              strokeWidth="4"
              fill="none"
            />
            <path
              className="opacity-75"
              fill="currentColor"
              d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"
            />
          </svg>
          Loading...
        </span>
      ) : (
        children
      )}
    </button>
  );
};
```

---

## 🔧 Configuration Files

### Maven Settings (settings.xml)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
          http://maven.apache.org/xsd/settings-1.0.0.xsd">
    
    <profiles>
        <profile>
            <id>development</id>
            <properties>
                <env>dev</env>
            </properties>
        </profile>
        
        <profile>
            <id>production</id>
            <properties>
                <env>prod</env>
            </properties>
        </profile>
    </profiles>
    
    <activeProfiles>
        <activeProfile>development</activeProfile>
    </activeProfiles>
</settings>
```

### ESLint Configuration

```json
// frontend/.eslintrc.json
{
  "extends": [
    "next/core-web-vitals",
    "eslint:recommended",
    "plugin:@typescript-eslint/recommended"
  ],
  "parser": "@typescript-eslint/parser",
  "plugins": ["@typescript-eslint"],
  "rules": {
    "no-console": "warn",
    "no-unused-vars": "off",
    "@typescript-eslint/no-unused-vars": ["error"],
    "@typescript-eslint/no-explicit-any": "warn"
  }
}
```

### Prettier Configuration

```json
// frontend/.prettierrc
{
  "semi": true,
  "trailingComma": "es5",
  "singleQuote": true,
  "printWidth": 100,
  "tabWidth": 2,
  "useTabs": false
}
```

---

## 📈 Monitoring & Observability

### Application Metrics (Micrometer)

```java
// src/main/java/com/banksyariah/config/MetricsConfig.java
package com.banksyariah.config;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsConfig {
    
    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }
}
```

### Logging Configuration (Logback)

```xml
<!-- src/main/resources/logback-spring.xml -->
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <property name="LOG_PATH" value="./logs"/>
    
    <appender name="Console" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    
    <appender name="RollingFile" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>${LOG_PATH}/app.log</file>
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>${LOG_PATH}/app-%d{yyyy-MM-dd}.log</fileNamePattern>
            <maxHistory>30</maxHistory>
        </rollingPolicy>
    </appender>
    
    <root level="INFO">
        <appender-ref ref="Console"/>
        <appender-ref ref="RollingFile"/>
    </root>
    
    <logger name="com.banksyariah" level="DEBUG"/>
</configuration>
```

---

## ✅ Completion Checklist

- [x] Project setup dan struktur
- [x] Backend configuration (Spring Boot)
- [x] Database design dan setup
- [x] Entity dan Repository implementation
- [x] Service layer implementation
- [x] REST API Controllers
- [x] Frontend setup (Next.js)
- [x] Frontend-Backend integration
- [x] JWT Security implementation
- [x] Testing (Unit & Integration)
- [x] Docker containerization
- [x] CI/CD pipeline
- [x] Monitoring dan logging
- [x] API documentation
- [x] Performance optimization tips
- [x] Troubleshooting guide
- [x] Production deployment guide

---

## 🎉 Kesimpulan

Panduan ini telah mencakup seluruh aspek pembangunan aplikasi Bank Syariah Aisyah dari awal hingga siap deployment. Dengan mengikuti step-by-step guide ini, Anda akan memiliki:

1. **Backend yang robust** dengan Spring Boot, JWT security, dan PostgreSQL
2. **Frontend modern** dengan Next.js dan TypeScript
3. **Security best practices** implementation
4. **Production-ready deployment** dengan Docker dan CI/CD
5. **Monitoring dan logging** untuk observability
6. **Comprehensive testing** coverage

Aplikasi ini siap untuk dikembangkan lebih lanjut dengan fitur-fitur tambahan sesuai kebutuhan bisnis perbankan syariah.

**Happy Coding! 🚀**