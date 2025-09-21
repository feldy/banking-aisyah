# Database Design - Bank Syariah Aisyah Application

## 1. Database Setup

### 1.1 PostgreSQL Installation
```bash
# Ubuntu/Debian
sudo apt update
sudo apt install postgresql postgresql-contrib

# Windows (download dari postgresql.org)
# macOS
brew install postgresql
```

### 1.2 Database Creation
```sql
-- Connect sebagai postgres user
sudo -u postgres psql

-- Create database
CREATE DATABASE bank_syariah_aisyah;

-- Create user
CREATE USER aisyah_user WITH ENCRYPTED PASSWORD 'aisyah2024!';

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE bank_syariah_aisyah TO aisyah_user;

-- Connect to new database
\c bank_syariah_aisyah;

-- Grant schema privileges
GRANT ALL ON SCHEMA public TO aisyah_user;
```

## 2. Database Schema Design

### 2.1 Core Tables

#### Users Table
```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(20),
    date_of_birth DATE,
    national_id VARCHAR(20) UNIQUE,
    address TEXT,
    role VARCHAR(20) DEFAULT 'CUSTOMER',
    status VARCHAR(20) DEFAULT 'ACTIVE',
    email_verified BOOLEAN DEFAULT FALSE,
    phone_verified BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT
);
```

#### Customers Table
```sql
CREATE TABLE customers (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    customer_number VARCHAR(20) UNIQUE NOT NULL,
    customer_type VARCHAR(20) DEFAULT 'INDIVIDUAL', -- INDIVIDUAL, CORPORATE
    occupation VARCHAR(100),
    monthly_income DECIMAL(15,2),
    source_of_income VARCHAR(100),
    risk_profile VARCHAR(20) DEFAULT 'LOW', -- LOW, MEDIUM, HIGH
    kyc_status VARCHAR(20) DEFAULT 'PENDING', -- PENDING, VERIFIED, REJECTED
    kyc_verified_at TIMESTAMP,
    emergency_contact_name VARCHAR(100),
    emergency_contact_phone VARCHAR(20),
    emergency_contact_relation VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### Account Types Table
```sql
CREATE TABLE account_types (
    id BIGSERIAL PRIMARY KEY,
    type_code VARCHAR(10) UNIQUE NOT NULL,
    type_name VARCHAR(50) NOT NULL,
    description TEXT,
    minimum_balance DECIMAL(15,2) DEFAULT 0,
    maintenance_fee DECIMAL(10,2) DEFAULT 0,
    profit_sharing_ratio DECIMAL(5,4), -- untuk mudharabah
    is_savings BOOLEAN DEFAULT TRUE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insert default account types
INSERT INTO account_types (type_code, type_name, description, minimum_balance, profit_sharing_ratio) VALUES
('WAD', 'Wadiah', 'Akad Wadiah - Titipan', 50000.00, NULL),
('MUD', 'Mudharabah', 'Akad Mudharabah - Bagi Hasil', 100000.00, 0.6000),
('MUS', 'Musyarakah', 'Akad Musyarakah - Kerjasama', 500000.00, 0.7000);
```

#### Accounts Table
```sql
CREATE TABLE accounts (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL REFERENCES customers(id),
    account_type_id BIGINT NOT NULL REFERENCES account_types(id),
    account_number VARCHAR(20) UNIQUE NOT NULL,
    account_name VARCHAR(100) NOT NULL,
    balance DECIMAL(15,2) DEFAULT 0.00,
    available_balance DECIMAL(15,2) DEFAULT 0.00,
    currency VARCHAR(3) DEFAULT 'IDR',
    status VARCHAR(20) DEFAULT 'ACTIVE', -- ACTIVE, SUSPENDED, CLOSED
    opened_date DATE DEFAULT CURRENT_DATE,
    closed_date DATE,
    last_transaction_date TIMESTAMP,
    profit_sharing_percentage DECIMAL(5,4),
    is_dormant BOOLEAN DEFAULT FALSE,
    dormant_since DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### Transaction Categories Table
```sql
CREATE TABLE transaction_categories (
    id BIGSERIAL PRIMARY KEY,
    category_code VARCHAR(10) UNIQUE NOT NULL,
    category_name VARCHAR(50) NOT NULL,
    description TEXT,
    is_credit BOOLEAN, -- TRUE for credit, FALSE for debit
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insert default categories
INSERT INTO transaction_categories (category_code, category_name, is_credit) VALUES
('DEP', 'Deposit', TRUE),
('WD', 'Withdrawal', FALSE),
('TRF_IN', 'Transfer In', TRUE),
('TRF_OUT', 'Transfer Out', FALSE),
('PROFIT', 'Profit Sharing', TRUE),
('FEE', 'Fee Charge', FALSE),
('ZAKAT', 'Zakat Payment', FALSE);
```

#### Transactions Table
```sql
CREATE TABLE transactions (
    id BIGSERIAL PRIMARY KEY,
    transaction_number VARCHAR(30) UNIQUE NOT NULL,
    account_id BIGINT NOT NULL REFERENCES accounts(id),
    transaction_category_id BIGINT NOT NULL REFERENCES transaction_categories(id),
    amount DECIMAL(15,2) NOT NULL,
    transaction_type VARCHAR(10) NOT NULL, -- DEBIT, CREDIT
    description TEXT,
    reference_number VARCHAR(50),
    balance_before DECIMAL(15,2) NOT NULL,
    balance_after DECIMAL(15,2) NOT NULL,
    transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    value_date DATE DEFAULT CURRENT_DATE,
    status VARCHAR(20) DEFAULT 'COMPLETED', -- PENDING, COMPLETED, FAILED, REVERSED
    processed_by BIGINT REFERENCES users(id),
    channel VARCHAR(20) DEFAULT 'TELLER', -- TELLER, ATM, MOBILE, WEB
    location VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- For transfer transactions
    to_account_id BIGINT REFERENCES accounts(id),
    to_account_number VARCHAR(20),
    to_account_name VARCHAR(100)
);
```

#### Profit Sharing Table
```sql
CREATE TABLE profit_sharing (
    id BIGSERIAL PRIMARY KEY,
    account_id BIGINT NOT NULL REFERENCES accounts(id),
    period_month INTEGER NOT NULL,
    period_year INTEGER NOT NULL,
    average_balance DECIMAL(15,2) NOT NULL,
    total_profit DECIMAL(15,2) NOT NULL,
    sharing_percentage DECIMAL(5,4) NOT NULL,
    customer_share DECIMAL(15,2) NOT NULL,
    bank_share DECIMAL(15,2) NOT NULL,
    distribution_date DATE,
    status VARCHAR(20) DEFAULT 'PENDING', -- PENDING, DISTRIBUTED, CANCELLED
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE(account_id, period_month, period_year)
);
```

### 2.2 Supporting Tables

#### Branches Table
```sql
CREATE TABLE branches (
    id BIGSERIAL PRIMARY KEY,
    branch_code VARCHAR(10) UNIQUE NOT NULL,
    branch_name VARCHAR(100) NOT NULL,
    address TEXT NOT NULL,
    city VARCHAR(50) NOT NULL,
    province VARCHAR(50) NOT NULL,
    postal_code VARCHAR(10),
    phone_number VARCHAR(20),
    email VARCHAR(100),
    manager_name VARCHAR(100),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### Audit Log Table
```sql
CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,
    table_name VARCHAR(50) NOT NULL,
    record_id BIGINT NOT NULL,
    action VARCHAR(10) NOT NULL, -- INSERT, UPDATE, DELETE
    old_values JSONB,
    new_values JSONB,
    changed_by BIGINT REFERENCES users(id),
    changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ip_address INET,
    user_agent TEXT
);
```

#### System Parameters Table
```sql
CREATE TABLE system_parameters (
    id BIGSERIAL PRIMARY KEY,
    param_key VARCHAR(100) UNIQUE NOT NULL,
    param_value TEXT NOT NULL,
    param_type VARCHAR(20) DEFAULT 'STRING', -- STRING, NUMBER, BOOLEAN, JSON
    description TEXT,
    is_editable BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insert default parameters
INSERT INTO system_parameters (param_key, param_value, param_type, description) VALUES
('DAILY_TRANSACTION_LIMIT', '10000000', 'NUMBER', 'Daily transaction limit per customer'),
('PROFIT_SHARING_DAY', '25', 'NUMBER', 'Day of month for profit sharing calculation'),
('MINIMUM_BALANCE_ALERT', '10000', 'NUMBER', 'Minimum balance for alert'),
('ZAKAT_PERCENTAGE', '0.025', 'NUMBER', 'Zakat percentage (2.5%)');
```

## 3. Indexes and Constraints

```sql
-- Performance indexes
CREATE INDEX idx_accounts_customer_id ON accounts(customer_id);
CREATE INDEX idx_accounts_account_number ON accounts(account_number);
CREATE INDEX idx_transactions_account_id ON transactions(account_id);
CREATE INDEX idx_transactions_date ON transactions(transaction_date);
CREATE INDEX idx_transactions_number ON transactions(transaction_number);
CREATE INDEX idx_customers_user_id ON customers(user_id);
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);

-- Composite indexes for common queries
CREATE INDEX idx_transactions_account_date ON transactions(account_id, transaction_date);
CREATE INDEX idx_profit_sharing_period ON profit_sharing(period_year, period_month);

-- Check constraints
ALTER TABLE accounts ADD CONSTRAINT chk_balance_positive CHECK (balance >= 0);
ALTER TABLE transactions ADD CONSTRAINT chk_amount_positive CHECK (amount > 0);
ALTER TABLE profit_sharing ADD CONSTRAINT chk_percentage_valid CHECK (sharing_percentage BETWEEN 0 AND 1);
```

## 4. Functions and Triggers

### 4.1 Update Timestamp Function
```sql
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Apply to tables with updated_at column
CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
    
CREATE TRIGGER update_customers_updated_at BEFORE UPDATE ON customers
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
    
CREATE TRIGGER update_accounts_updated_at BEFORE UPDATE ON accounts
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
```

### 4.2 Account Number Generation Function
```sql
CREATE OR REPLACE FUNCTION generate_account_number(account_type_code VARCHAR)
RETURNS VARCHAR AS $$
DECLARE
    next_seq INTEGER;
    account_num VARCHAR(20);
BEGIN
    -- Get next sequence number
    SELECT COALESCE(MAX(CAST(SUBSTRING(account_number FROM 4) AS INTEGER)), 0) + 1
    INTO next_seq
    FROM accounts a
    JOIN account_types at ON a.account_type_id = at.id
    WHERE at.type_code = account_type_code;
    
    -- Format: TypeCode + 10 digit number
    account_num := account_type_code || LPAD(next_seq::TEXT, 10, '0');
    
    RETURN account_num;
END;
$$ LANGUAGE plpgsql;
```

## 5. Sample Data

```sql
-- Insert sample branches
INSERT INTO branches (branch_code, branch_name, address, city, province) VALUES
('001', 'Cabang Utama Jakarta', 'Jl. Sudirman No. 123', 'Jakarta', 'DKI Jakarta'),
('002', 'Cabang Bandung', 'Jl. Asia Afrika No. 456', 'Bandung', 'Jawa Barat'),
('003', 'Cabang Surabaya', 'Jl. Pemuda No. 789', 'Surabaya', 'Jawa Timur');

-- Insert sample admin user
INSERT INTO users (username, email, password_hash, full_name, role) VALUES
('admin', 'admin@aisyahbank.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye', 'Administrator', 'ADMIN');
```

## 6. Database Views

### 6.1 Account Summary View
```sql
CREATE VIEW v_account_summary AS
SELECT 
    a.id,
    a.account_number,
    a.account_name,
    at.type_name as account_type,
    c.customer_number,
    u.full_name as customer_name,
    a.balance,
    a.status,
    a.opened_date,
    COUNT(t.id) as total_transactions,
    MAX(t.transaction_date) as last_transaction_date
FROM accounts a
JOIN account_types at ON a.account_type_id = at.id
JOIN customers c ON a.customer_id = c.id
JOIN users u ON c.user_id = u.id
LEFT JOIN transactions t ON a.id = t.account_id
GROUP BY a.id, a.account_number, a.account_name, at.type_name, 
         c.customer_number, u.full_name, a.balance, a.status, a.opened_date;
```

### 6.2 Monthly Transaction Summary View
```sql
CREATE VIEW v_monthly_transaction_summary AS
SELECT 
    a.account_number,
    a.account_name,
    DATE_TRUNC('month', t.transaction_date) as month_year,
    tc.category_name,
    SUM(CASE WHEN t.transaction_type = 'CREDIT' THEN t.amount ELSE 0 END) as total_credit,
    SUM(CASE WHEN t.transaction_type = 'DEBIT' THEN t.amount ELSE 0 END) as total_debit,
    COUNT(*) as transaction_count
FROM transactions t
JOIN accounts a ON t.account_id = a.id
JOIN transaction_categories tc ON t.transaction_category_id = tc.id
WHERE t.status = 'COMPLETED'
GROUP BY a.account_number, a.account_name, DATE_TRUNC('month', t.transaction_date), tc.category_name
ORDER BY month_year DESC, a.account_number;
```

## 7. Backup and Maintenance

### 7.1 Backup Script
```bash
#!/bin/bash
# backup_db.sh
DB_NAME="bank_syariah_aisyah"
BACKUP_DIR="/var/backups/postgresql"
DATE=$(date +%Y%m%d_%H%M%S)

pg_dump -U aisyah_user -h localhost $DB_NAME | gzip > $BACKUP_DIR/aisyah_backup_$DATE.sql.gz

# Keep only last 30 days backup
find $BACKUP_DIR -name "aisyah_backup_*.sql.gz" -mtime +30 -delete
```

### 7.2 Maintenance Queries
```sql
-- Check table sizes
SELECT 
    schemaname,
    tablename,
    pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) as size
FROM pg_tables 
WHERE schemaname = 'public'
ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC;

-- Check index usage
SELECT 
    schemaname,
    tablename,
    indexname,
    idx_tup_read,
    idx_tup_fetch
FROM pg_stat_user_indexes
ORDER BY idx_tup_read DESC;

-- Vacuum and analyze
VACUUM ANALYZE;
```

## 8. Security Considerations

1. **Row Level Security (RLS)**
   - Enable RLS for sensitive tables
   - Customers can only see their own data

2. **Data Encryption**
   - Encrypt sensitive fields like national_id
   - Use PostgreSQL pgcrypto extension

3. **Audit Trail**
   - All data changes logged in audit_logs table
   - Include IP address and user agent

4. **Backup Security**
   - Encrypt backup files
   - Store in secure location
   - Regular restore testing

## 9. Performance Optimization

1. **Partitioning**
   - Partition transactions table by date
   - Partition audit_logs by date

2. **Connection Pooling**
   - Use connection pooling (PgBouncer)
   - Configure appropriate pool sizes

3. **Query Optimization**
   - Regular ANALYZE operations
   - Monitor slow queries
   - Optimize indexes based on query patterns