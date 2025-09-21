    └── KycStatus.java

## 3. Entity Classes

### 3.1 Base Entity
```java
package com.aisyahbank.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
```

### 3.2 User Entity
```java
package com.aisyahbank.entity;

import com.aisyahbank.enums.UserRole;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
public class User extends BaseEntity {
    
    @NotBlank
    @Size(min = 3, max = 50)
    @Column(unique = true, nullable = false, length = 50)
    private String username;
    
    @NotBlank
    @Email
    @Column(unique = true, nullable = false, length = 100)
    private String email;
    
    @NotBlank
    @Size(min = 8)
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;
    
    @NotBlank
    @Size(max = 100)
    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;
    
    @Column(name = "phone_number", length = 20)
    private String phoneNumber;
    
    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;
    
    @Column(name = "national_id", unique = true, length = 20)
    private String nationalId;
    
    @Column(columnDefinition = "TEXT")
    private String address;
    
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private UserRole role = UserRole.CUSTOMER;
    
    @Column(length = 20)
    private String status = "ACTIVE";
    
    @Column(name = "email_verified")
    private Boolean emailVerified = false;
    
    @Column(name = "phone_verified")
    private Boolean phoneVerified = false;
    
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private Customer customer;
    
    // Constructors
    public User() {}
    
    public User(String username, String email, String passwordHash, String fullName) {
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
    }
    
    // Getters and setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    
    public String getNationalId() { return nationalId; }
    public void setNationalId(String nationalId) { this.nationalId = nationalId; }
    
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    
    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public Boolean getEmailVerified() { return emailVerified; }
    public void setEmailVerified(Boolean emailVerified) { this.emailVerified = emailVerified; }
    
    public Boolean getPhoneVerified() { return phoneVerified; }
    public void setPhoneVerified(Boolean phoneVerified) { this.phoneVerified = phoneVerified; }
    
    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }
}
```

### 3.3 Customer Entity
```java
package com.aisyahbank.entity;

import com.aisyahbank.enums.KycStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "customers")
public class Customer extends BaseEntity {
    
    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", unique = true)
    private User user;
    
    @NotBlank
    @Column(name = "customer_number", unique = true, nullable = false, length = 20)
    private String customerNumber;
    
    @Column(name = "customer_type", length = 20)
    private String customerType = "INDIVIDUAL";
    
    @Column(length = 100)
    private String occupation;
    
    @Column(name = "monthly_income", precision = 15, scale = 2)
    private BigDecimal monthlyIncome;
    
    @Column(name = "source_of_income", length = 100)
    private String sourceOfIncome;
    
    @Column(name = "risk_profile", length = 20)
    private String riskProfile = "LOW";
    
    @Enumerated(EnumType.STRING)
    @Column(name = "kyc_status", length = 20)
    private KycStatus kycStatus = KycStatus.PENDING;
    
    @Column(name = "kyc_verified_at")
    private LocalDateTime kycVerifiedAt;
    
    @Column(name = "emergency_contact_name", length = 100)
    private String emergencyContactName;
    
    @Column(name = "emergency_contact_phone", length = 20)
    private String emergencyContactPhone;
    
    @Column(name = "emergency_contact_relation", length = 50)
    private String emergencyContactRelation;
    
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Account> accounts = new ArrayList<>();
    
    // Constructors
    public Customer() {}
    
    public Customer(User user, String customerNumber) {
        this.user = user;
        this.customerNumber = customerNumber;
    }
    
    // Getters and setters
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    
    public String getCustomerNumber() { return customerNumber; }
    public void setCustomerNumber(String customerNumber) { this.customerNumber = customerNumber; }
    
    public String getCustomerType() { return customerType; }
    public void setCustomerType(String customerType) { this.customerType = customerType; }
    
    public String getOccupation() { return occupation; }
    public void setOccupation(String occupation) { this.occupation = occupation; }
    
    public BigDecimal getMonthlyIncome() { return monthlyIncome; }
    public void setMonthlyIncome(BigDecimal monthlyIncome) { this.monthlyIncome = monthlyIncome; }
    
    public String getSourceOfIncome() { return sourceOfIncome; }
    public void setSourceOfIncome(String sourceOfIncome) { this.sourceOfIncome = sourceOfIncome; }
    
    public String getRiskProfile() { return riskProfile; }
    public void setRiskProfile(String riskProfile) { this.riskProfile = riskProfile; }
    
    public KycStatus getKycStatus() { return kycStatus; }
    public void setKycStatus(KycStatus kycStatus) { this.kycStatus = kycStatus; }
    
    public LocalDateTime getKycVerifiedAt() { return kycVerifiedAt; }
    public void setKycVerifiedAt(LocalDateTime kycVerifiedAt) { this.kycVerifiedAt = kycVerifiedAt; }
    
    public String getEmergencyContactName() { return emergencyContactName; }
    public void setEmergencyContactName(String emergencyContactName) { this.emergencyContactName = emergencyContactName; }
    
    public String getEmergencyContactPhone() { return emergencyContactPhone; }
    public void setEmergencyContactPhone(String emergencyContactPhone) { this.emergencyContactPhone = emergencyContactPhone; }
    
    public String getEmergencyContactRelation() { return emergencyContactRelation; }
    public void setEmergencyContactRelation(String emergencyContactRelation) { this.emergencyContactRelation = emergencyContactRelation; }
    
    public List<Account> getAccounts() { return accounts; }
    public void setAccounts(List<Account> accounts) { this.accounts = accounts; }
}
```

### 3.4 Account Entity
```java
package com.aisyahbank.entity;

import com.aisyahbank.enums.AccountStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "accounts")
public class Account extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_type_id", nullable = false)
    private AccountType accountType;
    
    @NotBlank
    @Column(name = "account_number", unique = true, nullable = false, length = 20)
    private String accountNumber;
    
    @NotBlank
    @Column(name = "account_name", nullable = false, length = 100)
    private String accountName;
    
    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    @Column(precision = 15, scale = 2, nullable = false)
    private BigDecimal balance = BigDecimal.ZERO;
    
    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    @Column(name = "available_balance", precision = 15, scale = 2, nullable = false)
    private BigDecimal availableBalance = BigDecimal.ZERO;
    
    @Column(length = 3)
    private String currency = "IDR";
    
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private AccountStatus status = AccountStatus.ACTIVE;
    
    @Column(name = "opened_date")
    private LocalDate openedDate = LocalDate.now();
    
    @Column(name = "closed_date")
    private LocalDate closedDate;
    
    @Column(name = "last_transaction_date")
    private LocalDateTime lastTransactionDate;
    
    @Column(name = "profit_sharing_percentage", precision = 5, scale = 4)
    private BigDecimal profitSharingPercentage;
    
    @Column(name = "is_dormant")
    private Boolean isDormant = false;
    
    @Column(name = "dormant_since")
    private LocalDate dormantSince;
    
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Transaction> transactions = new ArrayList<>();
    
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ProfitSharing> profitSharings = new ArrayList<>();
    
    // Constructors
    public Account() {}
    
    public Account(Customer customer, AccountType accountType, String accountNumber, String accountName) {
        this.customer = customer;
        this.accountType = accountType;
        this.accountNumber = accountNumber;
        this.accountName = accountName;
    }
    
    // Business methods
    public void credit(BigDecimal amount) {
        this.balance = this.balance.add(amount);
        this.availableBalance = this.availableBalance.add(amount);
        this.lastTransactionDate = LocalDateTime.now();
    }
    
    public void debit(BigDecimal amount) {
        if (this.availableBalance.compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient balance");
        }
        this.balance = this.balance.subtract(amount);
        this.availableBalance = this.availableBalance.subtract(amount);
        this.lastTransactionDate = LocalDateTime.now();
    }
    
    public boolean hasSufficientBalance(BigDecimal amount) {
        return this.availableBalance.compareTo(amount) >= 0;
    }
    
    // Getters and setters
    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }
    
    public AccountType getAccountType() { return accountType; }
    public void setAccountType(AccountType accountType) { this.accountType = accountType; }
    
    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
    
    public String getAccountName() { return accountName; }
    public void setAccountName(String accountName) { this.accountName = accountName; }
    
    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
    
    public BigDecimal getAvailableBalance() { return availableBalance; }
    public void setAvailableBalance(BigDecimal availableBalance) { this.availableBalance = availableBalance; }
    
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    
    public AccountStatus getStatus() { return status; }
    public void setStatus(AccountStatus status) { this.status = status; }
    
    public LocalDate getOpenedDate() { return openedDate; }
    public void setOpenedDate(LocalDate openedDate) { this.openedDate = openedDate; }
    
    public LocalDate getClosedDate() { return closedDate; }
    public void setClosedDate(LocalDate closedDate) { this.closedDate = closedDate; }
    
    public LocalDateTime getLastTransactionDate() { return lastTransactionDate; }
    public void setLastTransactionDate(LocalDateTime lastTransactionDate) { this.lastTransactionDate = lastTransactionDate; }
    
    public BigDecimal getProfitSharingPercentage() { return profitSharingPercentage; }
    public void setProfitSharingPercentage(BigDecimal profitSharingPercentage) { this.profitSharingPercentage = profitSharingPercentage; }
    
    public Boolean getIsDormant() { return isDormant; }
    public void setIsDormant(Boolean isDormant) { this.isDormant = isDormant; }
    
    public LocalDate getDormantSince() { return dormantSince; }
    public void setDormantSince(LocalDate dormantSince) { this.dormantSince = dormantSince; }
    
    public List<Transaction> getTransactions() { return transactions; }
    public void setTransactions(List<Transaction> transactions) { this.transactions = transactions; }
    
    public List<ProfitSharing> getProfitSharings() { return profitSharings; }
    public void setProfitSharings(List<ProfitSharing> profitSharings) { this.profitSharings = profitSharings; }
}
```

## 4. Enums

### 4.1 UserRole Enum
```java
package com.aisyahbank.enums;

public enum UserRole {
    ADMIN,
    MANAGER,
    TELLER,
    CUSTOMER
}
```

### 4.2 AccountStatus Enum
```java
package com.aisyahbank.enums;

public enum AccountStatus {
    ACTIVE,
    SUSPENDED,
    CLOSED,
    FROZEN
}
```

### 4.3 TransactionType Enum
```java
package com.aisyahbank.enums;

public enum TransactionType {
    CREDIT,
    DEBIT
}
```

### 4.4 KycStatus Enum
```java
package com.aisyahbank.enums;

public enum KycStatus {
    PENDING,
    UNDER_REVIEW,
    VERIFIED,
    REJECTED,
    EXPIRED
}
```

## 5. DTOs (Data Transfer Objects)

### 5.1 Request DTOs

#### Authentication Request
```java
package com.aisyahbank.dto.request.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class LoginRequest {
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;
    
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;
    
    // Getters and setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
```

#### User Registration Request
```java
package com.aisyahbank.dto.request.auth;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

public class RegisterRequest {
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email format is invalid")
    private String email;
    
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]",
             message = "Password must contain at least one lowercase, one uppercase, one digit, and one special character")
    private String password;
    
    @NotBlank(message = "Full name is required")
    @Size(max = 100, message = "Full name cannot exceed 100 characters")
    private String fullName;
    
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
    private String phoneNumber;
    
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;
    
    @Pattern(regexp = "^[0-9]{16}$", message = "National ID must be 16 digits")
    private String nationalId;
    
    @Size(max = 500, message = "Address cannot exceed 500 characters")
    private String address;
    
    // Getters and setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    
    public String getNationalId() { return nationalId; }
    public void setNationalId(String nationalId) { this.nationalId = nationalId; }
    
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
}
```

#### Account Creation Request
```java
package com.aisyahbank.dto.request.account;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public class AccountCreationRequest {
    @NotNull(message = "Account type ID is required")
    private Long accountTypeId;
    
    @NotBlank(message = "Account name is required")
    @Size(max = 100, message = "Account name cannot exceed 100 characters")
    private String accountName;
    
    @NotNull(message = "Initial deposit is required")
    @DecimalMin(value = "0.01", message = "Initial deposit must be greater than 0")
    private BigDecimal initialDeposit;
    
    @Size(max = 500, message = "Purpose cannot exceed 500 characters")
    private String purpose;
    
    // Getters and setters
    public Long getAccountTypeId() { return accountTypeId; }
    public void setAccountTypeId(Long accountTypeId) { this.accountTypeId = accountTypeId; }
    
    public String getAccountName() { return accountName; }
    public void setAccountName(String accountName) { this.accountName = accountName; }
    
    public BigDecimal getInitialDeposit() { return initialDeposit; }
    public void setInitialDeposit(BigDecimal initialDeposit) { this.initialDeposit = initialDeposit; }
    
    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }
}
```

### 5.2 Response DTOs

#### Authentication Response
```java
package com.aisyahbank.dto.response.auth;

import com.aisyahbank.enums.UserRole;

public class LoginResponse {
    private String token;
    private String refreshToken;
    private String tokenType = "Bearer";
    private Long expiresIn;
    private UserInfo user;
    
    public LoginResponse(String token, String refreshToken, Long expiresIn, UserInfo user) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
        this.user = user;
    }
    
    // Getters and setters
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    
    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
    
    public String getTokenType() { return tokenType; }
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }
    
    public Long getExpiresIn() { return expiresIn; }
    public void setExpiresIn(Long expiresIn) { this.expiresIn = expiresIn; }
    
    public UserInfo getUser() { return user; }
    public void setUser(UserInfo user) { this.user = user; }
    
    public static class UserInfo {
        private Long id;
        private String username;
        private String email;
        private String fullName;
        private UserRole role;
        
        // Constructors, getters and setters
        public UserInfo() {}
        
        public UserInfo(Long id, String username, String email, String fullName, UserRole role) {
            this.id = id;
            this.username = username;
            this.email = email;
            this.fullName = fullName;
            this.role = role;
        }
        
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        
        public UserRole getRole() { return role; }
        public void setRole(UserRole role) { this.role = role; }
    }
}
```

## 6. Service Layer Implementation

### 6.1 Authentication Service
```java
package com.aisyahbank.service;

import com.aisyahbank.config.security.JwtTokenProvider;
import com.aisyahbank.dto.request.auth.LoginRequest;
import com.aisyahbank.dto.request.auth.RegisterRequest;
import com.aisyahbank.dto.response.auth.LoginResponse;
import com.aisyahbank.entity.Customer;
import com.aisyahbank.entity.User;
import com.aisyahbank.enums.UserRole;
import com.aisyahbank.exception.BusinessException;
import com.aisyahbank.repository.UserRepository;
import com.aisyahbank.util.AccountNumberGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthService {
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtTokenProvider tokenProvider;
    
    @Autowired
    private EmailService emailService;
    
    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getUsername(),
                request.getPassword()
            )
        );
        
        User user = userRepository.findByUsername(request.getUsername())
            .orElseThrow(() -> new BusinessException("User not found"));
        
        String token = tokenProvider.generateToken(authentication);
        String refreshToken = tokenProvider.generateRefreshToken(authentication);
        Long expiresIn = tokenProvider.getExpirationTime();
        
        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getFullName(),
            user.getRole()
        );
        
        return new LoginResponse(token, refreshToken, expiresIn, userInfo);
    }
    
    public void register(RegisterRequest request) {
        // Check if username exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("Username already exists");
        }
        
        // Check if email exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email already exists");
        }
        
        // Check if national ID exists
        if (request.getNationalId() != null && 
            userRepository.existsByNationalId(request.getNationalId())) {
            throw new BusinessException("National ID already exists");
        }
        
        // Create user
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setDateOfBirth(request.getDateOfBirth());
        user.setNationalId(request.getNationalId());
        user.setAddress(request.getAddress());
        user.setRole(UserRole.CUSTOMER);
        
        User savedUser = userRepository.save(user);
        
        // Create customer profile
        Customer customer = new Customer();
        customer.setUser(savedUser);
        customer.setCustomerNumber(AccountNumberGenerator.generateCustomerNumber());
        savedUser.setCustomer(customer);
        
        userRepository.save(savedUser);
        
        // Send welcome email
        emailService.sendWelcomeEmail(savedUser.getEmail(), savedUser.getFullName());
    }
    
    public LoginResponse refreshToken(String refreshToken) {
        if (!tokenProvider.validateToken(refreshToken)) {
            throw new BusinessException("Invalid refresh token");
        }
        
        String username = tokenProvider.getUsernameFromToken(refreshToken);
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new BusinessException("User not found"));
        
        Authentication authentication = new UsernamePasswordAuthenticationToken(
            user.getUsername(), null, null);
        
        String newToken = tokenProvider.generateToken(authentication);
        String newRefreshToken = tokenProvider.generateRefreshToken(authentication);
        Long expiresIn = tokenProvider.getExpirationTime();
        
        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getFullName(),
            user.getRole()
        );
        
        return new LoginResponse(newToken, newRefreshToken, expiresIn, userInfo);
    }
}
```

### 6.2 Account Service
```java
package com.aisyahbank.service;

import com.aisyahbank.dto.request.account.AccountCreationRequest;
import com.aisyahbank.dto.response.account.AccountResponse;
import com.aisyahbank.entity.Account;
import com.aisyahbank.entity.AccountType;
import com.aisyahbank.entity.Customer;
import com.aisyahbank.entity.Transaction;
import com.aisyahbank.entity.TransactionCategory;
import com.aisyahbank.enums.AccountStatus;
import com.aisyahbank.enums.TransactionType;
import com.aisyahbank.exception.BusinessException;
import com.aisyahbank.exception.ResourceNotFoundException;
import com.aisyahbank.mapper.AccountMapper;
import com.aisyahbank.repository.AccountRepository;
import com.aisyahbank.repository.AccountTypeRepository;
import com.aisyahbank.repository.CustomerRepository;
import com.aisyahbank.repository.TransactionCategoryRepository;
import com.aisyahbank.util.AccountNumberGenerator;
import com.aisyahbank.util.TransactionNumberGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class AccountService {
    
    @Autowired
    private AccountRepository accountRepository;
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private AccountTypeRepository accountTypeRepository;
    
    @Autowired
    private TransactionCategoryRepository transactionCategoryRepository;
    
    @Autowired
    private AccountMapper accountMapper;
    
    @Autowired
    private TransactionService transactionService;
    
    public AccountResponse createAccount(Long customerId, AccountCreationRequest request) {
        Customer customer = customerRepository.findById(customerId)
            .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
        
        AccountType accountType = accountTypeRepository.findById(request.getAccountTypeId())
            .orElseThrow(() -> new ResourceNotFoundException("Account type not found"));
        
        // Validate minimum balance
        if (request.getInitialDeposit().compareTo(accountType.getMinimumBalance()) < 0) {
            throw new BusinessException("Initial deposit must be at least " + 
                accountType.getMinimumBalance());
        }
        
        // Generate account number
        String accountNumber = AccountNumberGenerator.generateAccountNumber(
            accountType.getTypeCode());
        
        // Create account
        Account account = new Account();
        account.setCustomer(customer);
        account.setAccountType(accountType);
        account.setAccountNumber(accountNumber);
        account.setAccountName(request.getAccountName());
        account.setBalance(request.getInitialDeposit());
        account.setAvailableBalance(request.getInitialDeposit());
        account.setProfitSharingPercentage(accountType.getProfitSharingRatio());
        
        Account savedAccount = accountRepository.save(account);
        
        // Create initial deposit transaction
        if (request.getInitialDeposit().compareTo(BigDecimal.ZERO) > 0) {
            createInitialDepositTransaction(savedAccount, request.getInitialDeposit());
        }
        
        return accountMapper.toResponse(savedAccount);
    }
    
    private void createInitialDepositTransaction(Account account, BigDecimal amount) {
        TransactionCategory depositCategory = transactionCategoryRepository
            .findByCategoryCode("DEP")
            .orElseThrow(() -> new BusinessException("Deposit category not found"));
        
        Transaction transaction = new Transaction();
        transaction.setTransactionNumber(TransactionNumberGenerator.generate());
        transaction.setAccount(account);
        transaction.setTransactionCategory(depositCategory);
        transaction.setAmount(amount);
        transaction.setTransactionType(TransactionType.CREDIT);
        transaction.setDescription("Initial deposit");
        transaction.setBalanceBefore(BigDecimal.ZERO);
        transaction.setBalanceAfter(amount);
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setStatus("COMPLETED");
        transaction.setChannel("SYSTEM");
        
        transactionService.saveTransaction(transaction);
    }
    
    public AccountResponse getAccountById(Long accountId) {
        Account account = accountRepository.findById(accountId)
            .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
        return accountMapper.toResponse(account);
    }
    
    public AccountResponse getAccountByNumber(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
            .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
        return accountMapper.toResponse(account);
    }
    
    public List<AccountResponse> getAccountsByCustomerId(Long customerId) {
        List<Account> accounts = accountRepository.findByCustomerId(customerId);
        return accountMapper.toResponseList(accounts);
    }
    
    public Page<AccountResponse> getAllAccounts(Pageable pageable) {
        Page<Account> accounts = accountRepository.findAll(pageable);
        return accounts.map(accountMapper::toResponse);
    }
    
    public AccountResponse updateAccountStatus(Long accountId, AccountStatus status) {
        Account account = accountRepository.findById(accountId)
            .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
        
        account.setStatus(status);
        if (status == AccountStatus.CLOSED) {
            account.setClosedDate(LocalDateTime.now().toLocalDate());
        }
        
        Account updatedAccount = accountRepository.save(account);
        return accountMapper.toResponse(updatedAccount);
    }
    
    public BigDecimal getAccountBalance(Long accountId) {
        Account account = accountRepository.findById(accountId)
            .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
        return account.getBalance();
    }
    
    public void freezeAccount(Long accountId, String reason) {
        Account account = accountRepository.findById(accountId)
            .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
        
        account.setStatus(AccountStatus.FROZEN);
        accountRepository.save(account);
        
        // Log freeze action
        // auditService.logAccountFreeze(accountId, reason);
    }
    
    public void unfreezeAccount(Long accountId) {
        Account account = accountRepository.findById(accountId)
            .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
        
        account.setStatus(AccountStatus.ACTIVE);
        accountRepository.save(account);
        
        // Log unfreeze action
        // auditService.logAccountUnfreeze(accountId);
    }
}
```

### 6.3 Transaction Service
```java
package com.aisyahbank.service;

import com.aisyahbank.dto.request.transaction.DepositRequest;
import com.aisyahbank.dto.request.transaction.TransferRequest;
import com.aisyahbank.dto.request.transaction.WithdrawalRequest;
import com.aisyahbank.dto.response.transaction.TransactionResponse;
import com.aisyahbank.entity.Account;
import com.aisyahbank.entity.Transaction;
import com.aisyahbank.entity.TransactionCategory;
import com.aisyahbank.enums.AccountStatus;
import com.aisyahbank.enums.TransactionType;
import com.aisyahbank.exception.BusinessException;
import com.aisyahbank.exception.InsufficientBalanceException;
import com.aisyahbank.exception.ResourceNotFoundException;
import com.aisyahbank.mapper.TransactionMapper;
import com.aisyahbank.repository.AccountRepository;
import com.aisyahbank.repository.TransactionCategoryRepository;
import com.aisyahbank.repository.TransactionRepository;
import com.aisyahbank.util.TransactionNumberGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class TransactionService {
    
    @Autowired
    private TransactionRepository transactionRepository;
    
    @Autowired
    private AccountRepository accountRepository;
    
    @Autowired
    private TransactionCategoryRepository transactionCategoryRepository;
    
    @Autowired
    private TransactionMapper transactionMapper;
    
    @Autowired
    private EmailService emailService;
    
    public TransactionResponse deposit(DepositRequest request) {
        Account account = accountRepository.findByAccountNumber(request.getAccountNumber())
            .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
        
        validateAccountForTransaction(account);
        
        TransactionCategory category = transactionCategoryRepository
            .findByCategoryCode("DEP")
            .orElseThrow(() -> new BusinessException("Deposit category not found"));
        
        BigDecimal balanceBefore = account.getBalance();
        account.credit(request.getAmount());
        
        Transaction transaction = createTransaction(
            account, category, request.getAmount(), TransactionType.CREDIT,
            "Deposit - " + request.getDescription(), balanceBefore, account.getBalance()
        );
        
        Transaction savedTransaction = transactionRepository.save(transaction);
        accountRepository.save(account);
        
        // Send notification email
        sendTransactionNotification(account, savedTransaction);
        
        return transactionMapper.toResponse(savedTransaction);
    }
    
    public TransactionResponse withdrawal(WithdrawalRequest request) {
        Account account = accountRepository.findByAccountNumber(request.getAccountNumber())
            .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
        
        validateAccountForTransaction(account);
        
        if (!account.hasSufficientBalance(request.getAmount())) {
            throw new InsufficientBalanceException("Insufficient balance for withdrawal");
        }
        
        TransactionCategory category = transactionCategoryRepository
            .findByCategoryCode("WD")
            .orElseThrow(() -> new BusinessException("Withdrawal category not found"));
        
        BigDecimal balanceBefore = account.getBalance();
        account.debit(request.getAmount());
        
        Transaction transaction = createTransaction(
            account, category, request.getAmount(), TransactionType.DEBIT,
            "Withdrawal - " + request.getDescription(), balanceBefore, account.getBalance()
        );
        
        Transaction savedTransaction = transactionRepository.save(transaction);
        accountRepository.save(account);
        
        // Send notification email
        sendTransactionNotification(account, savedTransaction);
        
        return transactionMapper.toResponse(savedTransaction);
    }
    
    public TransactionResponse transfer(TransferRequest request) {
        Account fromAccount = accountRepository.findByAccountNumber(request.getFromAccountNumber())
            .orElseThrow(() -> new ResourceNotFoundException("Source account not found"));
        
        Account toAccount = accountRepository.findByAccountNumber(request.getToAccountNumber())
            .orElseThrow(() -> new ResourceNotFoundException("Destination account not found"));
        
        validateAccountForTransaction(fromAccount);
        validateAccountForTransaction(toAccount);
        
        if (!fromAccount.hasSufficientBalance(request.getAmount())) {
            throw new InsufficientBalanceException("Insufficient balance for transfer");
        }
        
        // Get transaction categories
        TransactionCategory transferOutCategory = transactionCategoryRepository
            .findByCategoryCode("TRF_OUT")
            .orElseThrow(() -> new BusinessException("Transfer out category not found"));
        
        TransactionCategory transferInCategory = transactionCategoryRepository
            .findByCategoryCode("TRF_IN")
            .orElseThrow(() -> new BusinessException("Transfer in category not found"));
        
        // Debit from source account
        BigDecimal fromBalanceBefore = fromAccount.getBalance();
        fromAccount.debit(request.getAmount());
        
        Transaction debitTransaction = createTransaction(
            fromAccount, transferOutCategory, request.getAmount(), TransactionType.DEBIT,
            "Transfer to " + toAccount.getAccountName() + " - " + request.getDescription(),
            fromBalanceBefore, fromAccount.getBalance()
        );
        debitTransaction.setToAccountId(toAccount.getId());
        debitTransaction.setToAccountNumber(toAccount.getAccountNumber());
        debitTransaction.setToAccountName(toAccount.getAccountName());
        
        // Credit to destination account
        BigDecimal toBalanceBefore = toAccount.getBalance();
        toAccount.credit(request.getAmount());
        
        Transaction creditTransaction = createTransaction(
            toAccount, transferInCategory, request.getAmount(), TransactionType.CREDIT,
            "Transfer from " + fromAccount.getAccountName() + " - " + request.getDescription(),
            toBalanceBefore, toAccount.getBalance()
        );
        creditTransaction.setToAccountId(fromAccount.getId());
        creditTransaction.setToAccountNumber(fromAccount.getAccountNumber());
        creditTransaction.setToAccountName(fromAccount.getAccountName());
        
        // Save transactions and accounts
        Transaction savedDebitTransaction = transactionRepository.save(debitTransaction);
        transactionRepository.save(creditTransaction);
        
        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);
        
        // Send notifications
        sendTransactionNotification(fromAccount, savedDebitTransaction);
        sendTransactionNotification(toAccount, creditTransaction);
        
        return transactionMapper.toResponse(savedDebitTransaction);
    }
    
    private Transaction createTransaction(Account account, TransactionCategory category,
                                       BigDecimal amount, TransactionType type,
                                       String description, BigDecimal balanceBefore,
                                       BigDecimal balanceAfter) {
        Transaction transaction = new Transaction();
        transaction.setTransactionNumber(TransactionNumberGenerator.generate());
        transaction.setAccount(account);
        transaction.setTransactionCategory(category);
        transaction.setAmount(amount);
        transaction.setTransactionType(type);
        transaction.setDescription(description);
        transaction.setBalanceBefore(balanceBefore);
        transaction.setBalanceAfter(balanceAfter);
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setValueDate(LocalDateTime.now().toLocalDate());
        transaction.setStatus("COMPLETED");
        transaction.setChannel("WEB");
        
        return transaction;
    }
    
    private void validateAccountForTransaction(Account account) {
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new BusinessException("Account is not active");
        }
        
        if (account.getIsDormant()) {
            throw new BusinessException("Account is dormant");
        }
    }
    
    private void sendTransactionNotification(Account account, Transaction transaction) {
        try {
            String customerEmail = account.getCustomer().getUser().getEmail();
            String customerName = account.getCustomer().getUser().getFullName();
            
            emailService.sendTransactionNotification(
                customerEmail, customerName, transaction
            );
        } catch (Exception e) {
            // Log error but don't fail transaction
            System.err.println("Failed to send transaction notification: " + e.getMessage());
        }
    }
    
    public TransactionResponse getTransactionById(Long transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
            .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));
        return transactionMapper.toResponse(transaction);
    }
    
    public TransactionResponse getTransactionByNumber(String transactionNumber) {
        Transaction transaction = transactionRepository.findByTransactionNumber(transactionNumber)
            .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));
        return transactionMapper.toResponse(transaction);
    }
    
    public Page<TransactionResponse> getTransactionsByAccountId(Long accountId, Pageable pageable) {
        Page<Transaction> transactions = transactionRepository
            .findByAccountIdOrderByTransactionDateDesc(accountId, pageable);
        return transactions.map(transactionMapper::toResponse);
    }
    
    public List<TransactionResponse> getTransactionHistory(String accountNumber, 
                                                         LocalDateTime startDate, 
                                                         LocalDateTime endDate) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
            .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
        
        List<Transaction> transactions = transactionRepository
            .findByAccountIdAndTransactionDateBetweenOrderByTransactionDateDesc(
                account.getId(), startDate, endDate);
        
        return transactionMapper.toResponseList(transactions);
    }
    
    public void saveTransaction(Transaction transaction) {
        transactionRepository.save(transaction);
    }
}
```

## 7. Exception Handling

### 7.1 Global Exception Handler
```java
package com.aisyahbank.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException ex, WebRequest request) {
        
        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.NOT_FOUND.value(),
            ex.getMessage(),
            LocalDateTime.now(),
            request.getDescription(false)
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }
    
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            BusinessException ex, WebRequest request) {
        
        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            ex.getMessage(),
            LocalDateTime.now(),
            request.getDescription(false)
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
    
    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientBalanceException(
            InsufficientBalanceException ex, WebRequest request) {
        
        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            ex.getMessage(),
            LocalDateTime.now(),
            request.getDescription(false)
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        ValidationErrorResponse errorResponse = new ValidationErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Validation failed",
            LocalDateTime.now(),
            errors
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(
            Exception ex, WebRequest request) {
        
        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Internal server error occurred",
            LocalDateTime.now(),
            request.getDescription(false)
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    // Error response classes
    public static class ErrorResponse {
        private int status;
        private String message;
        private LocalDateTime timestamp;
        private String path;
        
        public ErrorResponse(int status, String message, LocalDateTime timestamp, String path) {
            this.status = status;
            this.message = message;
            this.timestamp = timestamp;
            this.path = path;
        }
        
        // Getters and setters
        public int getStatus() { return status; }
        public void setStatus(int status) { this.status = status; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
        
        public String getPath() { return path; }
        public void setPath(String path) { this.path = path; }
    }
    
    public static class ValidationErrorResponse extends ErrorResponse {
        private Map<String, String> errors;
        
        public ValidationErrorResponse(int status, String message, LocalDateTime timestamp, 
                                     Map<String, String> errors) {
            super(status, message, timestamp, null);
            this.errors = errors;
        }
        
        public Map<String, String> getErrors() { return errors; }
        public void setErrors(Map<String, String> errors) { this.errors = errors; }
    }
}
```

## 8. Security Configuration

### 8.1 JWT Token Provider
```java
package com.aisyahbank.config.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Component
public class JwtTokenProvider {
    
    @Value("${jwt.secret}")
    private String jwtSecret;
    
    @Value("${jwt.expiration}")
    private Long jwtExpiration;
    
    @Value("${jwt.refresh-expiration}")
    private Long refreshExpiration;
    
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }
    
    public String generateToken(Authentication authentication) {
        String username = authentication.getName();
        Instant now = Instant.now();
        Instant expiryDate = now.plus(jwtExpiration, ChronoUnit.SECONDS);
        
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiryDate))
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }
    
    public String generateRefreshToken(Authentication authentication) {
        String username = authentication.getName();
        Instant now = Instant.now();
        Instant expiryDate = now.plus(refreshExpiration, ChronoUnit.SECONDS);
        
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiryDate))
                .claim("type", "refresh")
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }
    
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        
        return claims.getSubject();
    }
    
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
    
    public Long getExpirationTime() {
        return jwtExpiration;
    }
}
```

## 9. Controllers

### 9.1 Auth Controller
```java
package com.aisyahbank.controller;

import com.aisyahbank.dto.request.auth.LoginRequest;
import com.aisyahbank.dto.request.auth.RegisterRequest;
import com.aisyahbank.dto.response.auth.LoginResponse;
import com.aisyahbank.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Authentication management APIs")
public class AuthController {
    
    @Autowired
    private AuthService authService;
    
    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user and return JWT token")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/register")
    @Operation(summary = "User registration", description = "Register new customer account")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.ok("User registered successfully");
    }
    
    @PostMapping("/refresh")
    @Operation(summary = "Refresh token", description = "Generate new access token using refresh token")
    public ResponseEntity<LoginResponse> refreshToken(@RequestParam String refreshToken) {
        LoginResponse response = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(response);
    }
}
```

## 10. Testing Configuration

### 10.1 Test Configuration
```java
package com.aisyahbank.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@TestConfiguration
public class TestConfig {
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

### 10.2 Integration Test Example
```java
package com.aisyahbank.controller;

import com.aisyahbank.dto.request.auth.LoginRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
class AuthControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Test
    void loginWithValidCredentials_ShouldReturnToken() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("admin123");
        
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.user.username").value("admin"));
    }
}
```

## 11. Deployment Configuration

### 11.1 Dockerfile
```dockerfile
FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/bank-syariah-aisyah-1.0.0.jar app.jar

EXPOSE 8080

ENV JAVA_OPTS="-Xmx512m -Xms256m"

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
```

### 11.2 Docker Compose
```yaml
version: '3.8'

services:
  database:
    image: postgres:15
    environment:
      POSTGRES_DB: bank_syariah_aisyah
      POSTGRES_USER: aisyah_user
      POSTGRES_PASSWORD: aisyah2024!
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

  backend:
    build: .
    ports:
      - "8080:8080"
    environment:
      DB_USERNAME: aisyah_user
      DB_PASSWORD: aisyah2024!
      JWT_SECRET: aisyahBankSecretKey2024!@#$%^&*()VeryLongSecretKey
      ALLOWED_ORIGINS: http://localhost:3000
    depends_on:
      - database
    volumes:
      - ./logs:/app/logs

volumes:
  postgres_data:
```

### 11.3 Production Application Properties
```yaml
# application-prod.yml
server:
  port: 8080
  servlet:
    context-path: /api/v1

spring:
  profiles:
    active: production
    
  datasource:
    url: jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:bank_syariah_aisyah}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
      
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    
  security:
    require-ssl: true

logging:
  level:
    com.aisyahbank: INFO
    org.springframework.security: WARN
    org.hibernate.SQL: WARN
  file:
    name: /app/logs/aisyah-bank.log
    
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
```

## 12. Utilities and Helpers

### 12.1 Account Number Generator
```java
package com.aisyahbank.util;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class AccountNumberGenerator {
    
    private static final String CUSTOMER_PREFIX = "CUST";
    
    public static String generateCustomerNumber() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int random = ThreadLocalRandom.current().nextInt(1000, 9999);
        return CUSTOMER_PREFIX + timestamp + random;
    }
    
    public static String generateAccountNumber(String typeCode) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int random = ThreadLocalRandom.current().nextInt(100000, 999999);
        return typeCode + timestamp + random;
    }
}
```

### 12.2 Transaction Number Generator
```java
package com.aisyahbank.util;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class TransactionNumberGenerator {
    
    private static final String TRX_PREFIX = "TRX";
    
    public static String generate() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int random = ThreadLocalRandom.current().nextInt(1000, 9999);
        return TRX_PREFIX + timestamp + random;
    }
}
```

## 13. Email Service Implementation

```java
package com.aisyahbank.service;

import com.aisyahbank.entity.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

@Service
public class EmailService {
    
    @Autowired
    private JavaMailSender emailSender;
    
    @Value("${spring.mail.username}")
    private String fromEmail;
    
    @Value("${app.name}")
    private String appName;
    
    @Async
    public void sendWelcomeEmail(String toEmail, String customerName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Welcome to " + appName);
            message.setText(buildWelcomeEmailContent(customerName));
            
            emailSender.send(message);
        } catch (Exception e) {
            System.err.println("Failed to send welcome email: " + e.getMessage());
        }
    }
    
    @Async
    public void sendTransactionNotification(String toEmail, String customerName, Transaction transaction) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Transaction Notification - " + appName);
            message.setText(buildTransactionNotificationContent(customerName, transaction));
            
            emailSender.send(message);
        } catch (Exception e) {
            System.err.println("Failed to send transaction notification: " + e.getMessage());
        }
    }
    
    private String buildWelcomeEmailContent(String customerName) {
        return String.format("""
            Dear %s,
            
            Welcome to %s!
            
            Your account has been successfully created. You can now access our Islamic banking services 
            that comply with Sharia principles.
            
            Thank you for choosing %s for your banking needs.
            
            Best regards,
            %s Team
            """, customerName, appName, appName, appName);
    }
    
    private String buildTransactionNotificationContent(String customerName, Transaction transaction) {
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        
        return String.format("""
            Dear %s,
            
            A transaction has been processed on your account:
            
            Transaction Number: %s
            Account Number: %s
            Type: %s
            Amount: %s
            Description: %s
            Date: %s
            Balance After Transaction: %s
            
            If you did not authorize this transaction, please contact us immediately.
            
            Best regards,
            %s Team
            """, 
            customerName,
            transaction.getTransactionNumber(),
            transaction.getAccount().getAccountNumber(),
            transaction.getTransactionType(),
            currencyFormat.format(transaction.getAmount()),
            transaction.getDescription(),
            transaction.getTransactionDate(),
            currencyFormat.format(transaction.getBalanceAfter()),
            appName);
    }
}
```

## 14. MapStruct Mappers

### 14.1 Account Mapper
```java
package com.aisyahbank.mapper;

import com.aisyahbank.dto.response.account.AccountResponse;
import com.aisyahbank.entity.Account;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AccountMapper {
    
    @Mapping(source = "customer.customerNumber", target = "customerNumber")
    @Mapping(source = "customer.user.fullName", target = "customerName")
    @Mapping(source = "accountType.typeName", target = "accountTypeName")
    @Mapping(source = "accountType.typeCode", target = "accountTypeCode")
    AccountResponse toResponse(Account account);
    
    List<AccountResponse> toResponseList(List<Account> accounts);
}
```

### 14.2 Transaction Mapper
```java
package com.aisyahbank.mapper;

import com.aisyahbank.dto.response.transaction.TransactionResponse;
import com.aisyahbank.entity.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TransactionMapper {
    
    @Mapping(source = "account.accountNumber", target = "accountNumber")
    @Mapping(source = "account.accountName", target = "accountName")
    @Mapping(source = "transactionCategory.categoryName", target = "categoryName")
    TransactionResponse toResponse(Transaction transaction);
    
    List<TransactionResponse> toResponseList(List<Transaction> transactions);
}
```

## 15. Advanced Features

### 15.1 Profit Sharing Service
```java
package com.aisyahbank.service;

import com.aisyahbank.entity.Account;
import com.aisyahbank.entity.ProfitSharing;
import com.aisyahbank.repository.AccountRepository;
import com.aisyahbank.repository.ProfitSharingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
@Transactional
public class ProfitSharingService {
    
    @Autowired
    private AccountRepository accountRepository;
    
    @Autowired
    private ProfitSharingRepository profitSharingRepository;
    
    @Autowired
    private TransactionService transactionService;
    
    // Run monthly on 25th at 2 AM
    @Scheduled(cron = "0 0 2 25 * ?")
    public void calculateMonthlyProfitSharing() {
        YearMonth currentMonth = YearMonth.now().minusMonths(1);
        calculateProfitSharingForMonth(currentMonth);
    }
    
    public void calculateProfitSharingForMonth(YearMonth month) {
        List<Account> eligibleAccounts = accountRepository.findMudharabahAccounts();
        
        for (Account account : eligibleAccounts) {
            if (profitSharingRepository.existsByAccountIdAndPeriodMonthAndPeriodYear(
                    account.getId(), month.getMonthValue(), month.getYear())) {
                continue; // Already calculated
            }
            
            BigDecimal averageBalance = calculateAverageBalance(account, month);
            if (averageBalance.compareTo(BigDecimal.ZERO) <= 0) {
                continue; // Skip accounts with zero or negative average balance
            }
            
            // Calculate profit based on bank's total profit and account's share
            BigDecimal totalProfit = getBankTotalProfit(month);
            BigDecimal accountShare = calculateAccountShare(averageBalance, totalProfit);
            
            BigDecimal customerShare = accountShare
                .multiply(account.getProfitSharingPercentage())
                .setScale(2, RoundingMode.HALF_UP);
            
            BigDecimal bankShare = accountShare.subtract(customerShare);
            
            // Create profit sharing record
            ProfitSharing profitSharing = new ProfitSharing();
            profitSharing.setAccount(account);
            profitSharing.setPeriodMonth(month.getMonthValue());
            profitSharing.setPeriodYear(month.getYear());
            profitSharing.setAverageBalance(averageBalance);
            profitSharing.setTotalProfit(accountShare);
            profitSharing.setSharingPercentage(account.getProfitSharingPercentage());
            profitSharing.setCustomerShare(customerShare);
            profitSharing.setBankShare(bankShare);
            profitSharing.setStatus("CALCULATED");
            
            profitSharingRepository.save(profitSharing);
        }
    }
    
    public void distributeProfitSharing(YearMonth month) {
        List<ProfitSharing> pendingProfitSharings = profitSharingRepository
            .findByPeriodMonthAndPeriodYearAndStatus(
                month.getMonthValue(), month.getYear(), "CALCULATED");
        
        for (ProfitSharing profitSharing : pendingProfitSharings) {
            // Credit customer share to account
            Account account = profitSharing.getAccount();
            account.credit(profitSharing.getCustomerShare());
            accountRepository.save(account);
            
            // Create transaction record
            // createProfitSharingTransaction(account, profitSharing);
            
            // Update status
            profitSharing.setStatus("DISTRIBUTED");
            profitSharing.setDistributionDate(LocalDate.now());
            profitSharingRepository.save(profitSharing);
        }
    }
    
    private BigDecimal calculateAverageBalance(Account account, YearMonth month) {
        // Implementation to calculate average daily balance for the month
        // This is a simplified version
        return account.getBalance();
    }
    
    private BigDecimal getBankTotalProfit(YearMonth month) {
        // Implementation to get bank's total profit for the month
        // This should be calculated based on actual bank operations
        return new BigDecimal("1000000"); // Example value
    }
    
    private BigDecimal calculateAccountShare(BigDecimal averageBalance, BigDecimal totalProfit) {
        // Simplified calculation - in reality this would be more complex
        // involving total deposits, investment returns, etc.
        return totalProfit.multiply(new BigDecimal("0.001")); // 0.1% example
    }
}
```

### 15.2 Report Service
```java
package com.aisyahbank.service;

import com.aisyahbank.dto.response.report.AccountSummaryReport;
import com.aisyahbank.dto.response.report.TransactionReport;
import com.aisyahbank.repository.AccountRepository;
import com.aisyahbank.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportService {
    
    @Autowired
    private AccountRepository accountRepository;
    
    @Autowired
    private TransactionRepository transactionRepository;
    
    public AccountSummaryReport generateAccountSummaryReport() {
        Long totalAccounts = accountRepository.count();
        Long activeAccounts = accountRepository.countByStatus("ACTIVE");
        BigDecimal totalBalance = accountRepository.sumAllBalances();
        
        Map<String, Long> accountsByType = accountRepository
            .findAccountCountByType()
            .stream()
            .collect(Collectors.toMap(
                result -> (String) result[0],
                result -> (Long) result[1]
            ));
        
        AccountSummaryReport report = new AccountSummaryReport();
        report.setTotalAccounts(totalAccounts);
        report.setActiveAccounts(activeAccounts);
        report.setTotalBalance(totalBalance);
        report.setAccountsByType(accountsByType);
        report.setGeneratedAt(LocalDateTime.now());
        
        return report;
    }
    
    public TransactionReport generateTransactionReport(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        
        Long totalTransactions = transactionRepository.countByDateRange(startDateTime, endDateTime);
        BigDecimal totalAmount = transactionRepository.sumAmountByDateRange(startDateTime, endDateTime);
        
        Map<String, BigDecimal> transactionsByType = transactionRepository
            .findTransactionSummaryByType(startDateTime, endDateTime)
            .stream()
            .collect(Collectors.toMap(
                result -> (String) result[0],
                result -> (BigDecimal) result[1]
            ));
        
        TransactionReport report = new TransactionReport();
        report.setStartDate(startDate);
        report.setEndDate(endDate);
        report.setTotalTransactions(totalTransactions);
        report.setTotalAmount(totalAmount);
        report.setTransactionsByType(transactionsByType);
        report.setGeneratedAt(LocalDateTime.now());
        
        return report;
    }
}
```

## 16. Security Enhancements

### 16.1 Rate Limiting
```java
package com.aisyahbank.config.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitingInterceptor implements HandlerInterceptor {
    
    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, 
                           Object handler) throws Exception {
        
        String clientIp = getClientIpAddress(request);
        Bucket bucket = getBucket(clientIp);
        
        if (bucket.tryConsume(1)) {
            return true;
        } else {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.getWriter().write("Rate limit exceeded");
            return false;
        }
    }
    
    private Bucket getBucket(String clientIp) {
        return buckets.computeIfAbsent(clientIp, this::createBucket);
    }
    
    private Bucket createBucket(String clientIp) {
        // Allow 100 requests per minute
        Bandwidth bandwidth = Bandwidth.classic(100, Refill.intervally(100, Duration.ofMinutes(1)));
        return Bucket4j.builder().addLimit(bandwidth).build();
    }
    
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedForHeader = request.getHeader("X-Forwarded-For");
        if (xForwardedForHeader == null) {
            return request.getRemoteAddr();
        } else {
            return xForwardedForHeader.split(",")[0].trim();
        }
    }
}
```

## 17. Monitoring and Health Checks

### 17.1 Custom Health Indicator
```java
package com.aisyahbank.config.health;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuator.health.Health;
import org.springframework.boot.actuator.health.HealthIndicator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Component
public class DatabaseHealthIndicator implements HealthIndicator {
    
    @Autowired
    private DataSource dataSource;
    
    @Override
    public Health health() {
        try (Connection connection = dataSource.getConnection()) {
            if (connection.isValid(1)) {
                return Health.up()
                    .withDetail("database", "Available")
                    .withDetail("validationQuery", "Connection is valid")
                    .build();
            } else {
                return Health.down()
                    .withDetail("database", "Not responding")
                    .build();
            }
        } catch (SQLException e) {
            return Health.down()
                .withDetail("database", "Connection failed")
                .withDetail("error", e.getMessage())
                .build();
        }
    }
}
```

## 18. Application Main Class

```java
package com.aisyahbank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableJpaAuditing
@EnableCaching
@EnableAsync
@EnableScheduling
@EnableTransactionManagement
public class AisyahBankApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(AisyahBankApplication.class, args);
    }
}
```

## 19. Build and Run Instructions

### 19.1 Development Setup
```bash
# Clone repository
git clone <repository-url>
cd bank-syariah-aisyah

# Setup database (PostgreSQL must be running)
psql -U postgres -c "CREATE DATABASE bank_syariah_aisyah;"
psql -U postgres -c "CREATE USER aisyah_user WITH ENCRYPTED PASSWORD 'aisyah2024!';"
psql -U postgres -c "GRANT ALL PRIVILEGES ON DATABASE bank_syariah_aisyah TO aisyah_user;"

# Build application
mvn clean install

# Run application
mvn spring-boot:run

# Or run with specific profile
mvn spring-boot:run -Dspring-boot.run.profiles=development
```

### 19.2 Production Deployment
```bash
# Build Docker image
docker build -t aisyah-bank-backend .

# Run with Docker Compose
docker-compose up -d

# Or deploy to cloud platform
# (AWS, GCP, Azure specific instructions)
```

## 20. API Documentation

The API documentation is automatically generated using SpringDoc OpenAPI and available at:
- Swagger UI: `http://localhost:8080/api/v1/swagger-ui.html`
- API Docs JSON: `http://localhost:8080/api/v1/api-docs`

## 21. Security Best Practices Implemented

1. **JWT Token Security**: Secure token generation with long secret keys
2. **Password Encryption**: BCrypt for password hashing
3. **Input Validation**: Comprehensive validation using Bean Validation
4. **SQL Injection Prevention**: Using JPA/Hibernate parameterized queries
5. **Rate Limiting**: Request throttling to prevent abuse
6. **CORS Configuration**: Properly configured CORS policies
7. **Error Handling**: Secure error messages without sensitive information
8. **Audit Logging**: Track all sensitive operations
9. **SSL/TLS**: Force HTTPS in production
10. **Database Security**: Connection encryption and proper permissions

This completes the comprehensive backend guide for Bank Syariah Aisyah application. The implementation includes all essential features for a modern Islamic banking system with proper security, validation, and monitoring.# Backend Guide - Bank Syariah Aisyah Application (Spring Boot)

## 1. Project Setup

### 1.1 pom.xml Configuration
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" 
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
        <relativePath/>
    </parent>
    
    <groupId>com.aisyahbank</groupId>
    <artifactId>bank-syariah-aisyah</artifactId>
    <version>1.0.0</version>
    <name>Bank Syariah Aisyah</name>
    <description>Islamic Banking Application Backend</description>
    
    <properties>
        <java.version>17</java.version>
        <jjwt.version>0.11.5</jjwt.version>
        <mapstruct.version>1.5.5.Final</mapstruct.version>
        <springdoc.version>2.2.0</springdoc.version>
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
        
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-cache</artifactId>
        </dependency>
        
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-mail</artifactId>
        </dependency>
        
        <!-- Database -->
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>
        
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-core</artifactId>
        </dependency>
        
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <scope>test</scope>
        </dependency>
        
        <!-- Security & JWT -->
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-api</artifactId>
            <version>${jjwt.version}</version>
        </dependency>
        
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-impl</artifactId>
            <version>${jjwt.version}</version>
            <scope>runtime</scope>
        </dependency>
        
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-jackson</artifactId>
            <version>${jjwt.version}</version>
            <scope>runtime</scope>
        </dependency>
        
        <!-- Mapping -->
        <dependency>
            <groupId>org.mapstruct</groupId>
            <artifactId>mapstruct</artifactId>
            <version>${mapstruct.version}</version>
        </dependency>
        
        <!-- Documentation -->
        <dependency>
            <groupId>org.springdoc</groupId>
            <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
            <version>${springdoc.version}</version>
        </dependency>
        
        <!-- Utilities -->
        <dependency>
            <groupId>org.apache.commons</groupId>
            <artifactId>commons-lang3</artifactId>
        </dependency>
        
        <dependency>
            <groupId>org.apache.commons</groupId>
            <artifactId>commons-csv</artifactId>
            <version>1.10.0</version>
        </dependency>
        
        <!-- Test Dependencies -->
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
        
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>junit-jupiter</artifactId>
            <scope>test</scope>
        </dependency>
        
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>postgresql</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
    
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
            
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <configuration>
                    <annotationProcessorPaths>
                        <path>
                            <groupId>org.mapstruct</groupId>
                            <artifactId>mapstruct-processor</artifactId>
                            <version>${mapstruct.version}</version>
                        </path>
                    </annotationProcessorPaths>
                </configuration>
            </plugin>
            
            <plugin>
                <groupId>org.flywaydb</groupId>
                <artifactId>flyway-maven-plugin</artifactId>
                <configuration>
                    <url>jdbc:postgresql://localhost:5432/bank_syariah_aisyah</url>
                    <user>aisyah_user</user>
                    <password>aisyah2024!</password>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

### 1.2 Application Configuration

#### application.yml
```yaml
server:
  port: 8080
  servlet:
    context-path: /api/v1

spring:
  application:
    name: bank-syariah-aisyah
    
  profiles:
    active: development
    
  datasource:
    url: jdbc:postgresql://localhost:5432/bank_syariah_aisyah
    username: ${DB_USERNAME:aisyah_user}
    password: ${DB_PASSWORD:aisyah2024!}
    driver-class-name: org.postgresql.Driver
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      idle-timeout: 300000
      connection-timeout: 20000
      
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
        jdbc:
          time_zone: Asia/Jakarta
          
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true
    
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: ${JWT_ISSUER:http://localhost:8080}
          
  cache:
    type: caffeine
    caffeine:
      spec: maximumSize=1000,expireAfterWrite=300s
      
  mail:
    host: ${MAIL_HOST:smtp.gmail.com}
    port: ${MAIL_PORT:587}
    username: ${MAIL_USERNAME}
    password: ${MAIL_PASSWORD}
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true

# JWT Configuration
jwt:
  secret: ${JWT_SECRET:aisyahBankSecretKey2024!@#$%^&*()VeryLongSecretKey}
  expiration: ${JWT_EXPIRATION:86400} # 24 hours
  refresh-expiration: ${JWT_REFRESH_EXPIRATION:604800} # 7 days

# Application specific configs
app:
  name: Bank Syariah Aisyah
  version: 1.0.0
  description: Islamic Banking Application
  contact:
    name: Aisyah Bank Support
    email: support@aisyahbank.com
    url: https://www.aisyahbank.com
  
  cors:
    allowed-origins: ${ALLOWED_ORIGINS:http://localhost:3000,http://localhost:3001}
    allowed-methods: GET,POST,PUT,DELETE,OPTIONS
    allowed-headers: "*"
    
  file:
    upload-dir: ${FILE_UPLOAD_DIR:uploads}
    max-file-size: ${MAX_FILE_SIZE:5MB}
    
  transaction:
    daily-limit: ${DAILY_TRANSACTION_LIMIT:10000000}
    
# Logging
logging:
  level:
    com.aisyahbank: DEBUG
    org.springframework.security: DEBUG
    org.hibernate.SQL: DEBUG
  pattern:
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
  file:
    name: logs/aisyah-bank.log

# Management endpoints
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: when_authorized
  info:
    env:
      enabled: true

# Springdoc OpenAPI
springdoc:
  api-docs:
    path: /api-docs
  swagger-ui:
    path: /swagger-ui.html
    operationsSorter: method
    tagsSorter: alpha
```

## 2. Project Structure

```
src/main/java/com/aisyahbank/
├── AisyahBankApplication.java
├── config/
│   ├── CacheConfig.java
│   ├── CorsConfig.java
│   ├── DatabaseConfig.java
│   ├── JacksonConfig.java
│   ├── OpenApiConfig.java
│   └── security/
│       ├── SecurityConfig.java
│       ├── JwtAuthenticationFilter.java
│       ├── JwtTokenProvider.java
│       └── CustomUserDetailsService.java
├── controller/
│   ├── AuthController.java
│   ├── CustomerController.java
│   ├── AccountController.java
│   ├── TransactionController.java
│   └── ReportController.java
├── dto/
│   ├── request/
│   │   ├── auth/
│   │   ├── customer/
│   │   ├── account/
│   │   └── transaction/
│   └── response/
│       ├── auth/
│       ├── customer/
│       ├── account/
│       └── transaction/
├── entity/
│   ├── User.java
│   ├── Customer.java
│   ├── Account.java
│   ├── AccountType.java
│   ├── Transaction.java
│   ├── TransactionCategory.java
│   ├── ProfitSharing.java
│   ├── Branch.java
│   ├── AuditLog.java
│   └── SystemParameter.java
├── repository/
│   ├── UserRepository.java
│   ├── CustomerRepository.java
│   ├── AccountRepository.java
│   ├── TransactionRepository.java
│   └── ProfitSharingRepository.java
├── service/
│   ├── AuthService.java
│   ├── UserService.java
│   ├── CustomerService.java
│   ├── AccountService.java
│   ├── TransactionService.java
│   ├── ProfitSharingService.java
│   ├── EmailService.java
│   └── ReportService.java
├── mapper/
│   ├── UserMapper.java
│   ├── CustomerMapper.java
│   ├── AccountMapper.java
│   └── TransactionMapper.java
├── exception/
│   ├── GlobalExceptionHandler.java
│   ├── BusinessException.java
│   ├── ResourceNotFoundException.java
│   ├── InsufficientBalanceException.java
│   └── UnauthorizedException.java
├── util/
│   ├── AccountNumberGenerator.java
│   ├── TransactionNumberGenerator.java
│   ├── DateUtils.java
│   └── ValidationUtils.java
└── enums/
    ├── AccountStatus.java
    ├── TransactionType.java
    ├── UserRole.java
    └── KycStatus.java