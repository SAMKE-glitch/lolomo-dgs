# SQL: A Comprehensive Guide

## Table of Contents
- [Introduction](#introduction)
- [Why SQL?](#why-sql)
- [Data Types and Constraints](#data-types-and-constraints)
- [Keys and Relationships](#keys-and-relationships)
- [Indexing in SQL](#indexing-in-sql)
- [SQL Joins](#sql-joins)
- [Advanced SQL Features](#advanced-sql-features)
- [Best Practices](#best-practices)

## Introduction

SQL (Structured Query Language) is a standardized language for managing relational databases. It's designed for:
- Data Definition (DDL)
- Data Manipulation (DML)
- Data Control (DCL)
- Transaction Control (TCL)

### Key Features
- ACID Compliance (Atomicity, Consistency, Isolation, Durability)
- Structured Data Storage
- Complex Query Support
- Referential Integrity
- Transaction Management
- Robust Security Model

## Why SQL?

### Advantages
1. **Data Integrity**
    - Strong data consistency
    - Referential integrity
    - ACID compliance
    - Constraint enforcement

2. **Standardization**
    - Universal language across different RDBMS
    - Portable skills
    - Well-documented standards
    - Extensive tooling support

3. **Complex Queries**
    - Powerful JOIN operations
    - Advanced aggregation
    - Window functions
    - Common Table Expressions (CTEs)

4. **Security**
    - Fine-grained access control
    - Row-level security
    - Role-based permissions
    - Audit capabilities

### Use Cases
- Enterprise Applications
- Financial Systems
- Inventory Management
- Complex Reporting
- Data Warehousing
- Transaction Processing

## Data Types and Constraints

### Common Data Types
```sql
-- Numeric Types
INTEGER
DECIMAL(10,2)
BIGINT
FLOAT

-- String Types
CHAR(n)
VARCHAR(n)
TEXT

-- Date/Time Types
DATE
TIME
TIMESTAMP
INTERVAL

-- Other Types
BOOLEAN
BINARY
JSON
XML
```

### Constraints
```sql
-- Primary Key
CREATE TABLE users (
    id INTEGER PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE
);

-- Foreign Key
CREATE TABLE orders (
    order_id INTEGER PRIMARY KEY,
    user_id INTEGER,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Check Constraint
CREATE TABLE products (
    id INTEGER PRIMARY KEY,
    price DECIMAL(10,2) CHECK (price > 0),
    status VARCHAR(20) CHECK (status IN ('active', 'inactive'))
);

-- Default Values
CREATE TABLE posts (
    id INTEGER PRIMARY KEY,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_published BOOLEAN DEFAULT FALSE
);
```

## Keys and Relationships

### Types of Keys

1. **Primary Key**
```sql
-- Single Column Primary Key
CREATE TABLE employees (
    emp_id INTEGER PRIMARY KEY,
    name VARCHAR(100)
);

-- Composite Primary Key
CREATE TABLE order_items (
    order_id INTEGER,
    product_id INTEGER,
    quantity INTEGER,
    PRIMARY KEY (order_id, product_id)
);
```

2. **Foreign Key**
```sql
CREATE TABLE orders (
    order_id INTEGER PRIMARY KEY,
    customer_id INTEGER,
    FOREIGN KEY (customer_id) 
        REFERENCES customers(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);
```

3. **Unique Key**
```sql
CREATE TABLE users (
    id INTEGER PRIMARY KEY,
    email VARCHAR(100) UNIQUE,
    phone VARCHAR(20) UNIQUE
);
```

4. **Composite Keys**
```sql
CREATE TABLE enrollments (
    student_id INTEGER,
    course_id INTEGER,
    semester VARCHAR(20),
    grade CHAR(2),
    PRIMARY KEY (student_id, course_id, semester)
);
```

## Indexing in SQL

### Types of Indexes

1. **B-tree Index (Default)**
```sql
-- Simple Index
CREATE INDEX idx_lastname 
ON employees(last_name);

-- Composite Index
CREATE INDEX idx_name 
ON employees(last_name, first_name);
```

2. **Unique Index**
```sql
CREATE UNIQUE INDEX idx_email 
ON users(email);
```

3. **Partial Index**
```sql
-- Index only active users
CREATE INDEX idx_active_users 
ON users(username) 
WHERE status = 'active';
```

4. **Covering Index**
```sql
-- Index includes all needed columns
CREATE INDEX idx_user_email 
ON users(id, email, status);
```

### Index Usage Examples

```sql
-- Good use of index
SELECT * FROM users 
WHERE email = 'user@example.com';

-- Index might not be used
SELECT * FROM users 
WHERE LOWER(email) = 'user@example.com';

-- Partial index usage
SELECT * FROM users 
WHERE status = 'active' 
  AND created_at > '2023-01-01';
```

## SQL Joins

### Types of Joins

1. **INNER JOIN**
```sql
SELECT o.order_id, c.name
FROM orders o
INNER JOIN customers c 
    ON o.customer_id = c.id;
```

2. **LEFT JOIN**
```sql
SELECT c.name, o.order_id
FROM customers c
LEFT JOIN orders o 
    ON c.id = o.customer_id;
```

3. **RIGHT JOIN**
```sql
SELECT c.name, o.order_id
FROM orders o
RIGHT JOIN customers c 
    ON o.customer_id = c.id;
```

4. **FULL OUTER JOIN**
```sql
SELECT c.name, o.order_id
FROM customers c
FULL OUTER JOIN orders o 
    ON c.id = o.customer_id;
```

5. **CROSS JOIN**
```sql
SELECT p.name, c.color
FROM products p
CROSS JOIN colors c;
```

6. **Self JOIN**
```sql
SELECT e1.name as employee, e2.name as manager
FROM employees e1
LEFT JOIN employees e2 
    ON e1.manager_id = e2.id;
```

### Advanced Join Techniques

1. **Multiple Joins**
```sql
SELECT o.order_id, c.name, p.product_name
FROM orders o
JOIN customers c ON o.customer_id = c.id
JOIN order_items oi ON o.order_id = oi.order_id
JOIN products p ON oi.product_id = p.id;
```

2. **Conditional Joins**
```sql
SELECT o.order_id, c.name
FROM orders o
LEFT JOIN customers c 
    ON o.customer_id = c.id
    AND c.status = 'active';
```

## Advanced SQL Features

### Window Functions

1. **ROW_NUMBER()**
```sql
SELECT 
    name,
    department,
    salary,
    ROW_NUMBER() OVER (
        PARTITION BY department 
        ORDER BY salary DESC
    ) as salary_rank
FROM employees;
```

2. **LAG() and LEAD()**
```sql
SELECT 
    date,
    amount,
    LAG(amount) OVER (ORDER BY date) as prev_amount,
    LEAD(amount) OVER (ORDER BY date) as next_amount
FROM transactions;
```

### Common Table Expressions (CTE)

```sql
WITH recursive_cte AS (
    -- Base case
    SELECT id, name, manager_id, 1 as level
    FROM employees
    WHERE manager_id IS NULL
    
    UNION ALL
    
    -- Recursive case
    SELECT e.id, e.name, e.manager_id, rc.level + 1
    FROM employees e
    JOIN recursive_cte rc ON e.manager_id = rc.id
)
SELECT * FROM recursive_cte;
```

### CASE Statements

1. **Simple CASE**
```sql
SELECT 
    order_id,
    CASE status
        WHEN 'P' THEN 'Pending'
        WHEN 'S' THEN 'Shipped'
        WHEN 'D' THEN 'Delivered'
        ELSE 'Unknown'
    END as order_status
FROM orders;
```

2. **Searched CASE**
```sql
SELECT 
    name,
    salary,
    CASE 
        WHEN salary < 30000 THEN 'Low'
        WHEN salary < 60000 THEN 'Medium'
        ELSE 'High'
    END as salary_category
FROM employees;
```

### Grouping Operations

1. **Basic GROUP BY**
```sql
SELECT 
    department,
    COUNT(*) as employee_count,
    AVG(salary) as avg_salary
FROM employees
GROUP BY department;
```

2. **HAVING Clause**
```sql
SELECT 
    department,
    COUNT(*) as employee_count
FROM employees
GROUP BY department
HAVING COUNT(*) > 10;
```

3. **GROUPING SETS**
```sql
SELECT 
    department,
    job_title,
    COUNT(*),
    AVG(salary)
FROM employees
GROUP BY GROUPING SETS (
    (department, job_title),
    (department),
    (job_title),
    ()
);
```

### Transactions

```sql
BEGIN TRANSACTION;

INSERT INTO orders (customer_id, amount)
VALUES (1, 100.00);

UPDATE inventory
SET quantity = quantity - 1
WHERE product_id = 123;

COMMIT;
```

## Best Practices

1. **Index Design**
    - Create indexes based on query patterns
    - Avoid over-indexing
    - Monitor index usage
    - Regular maintenance

2. **Query Optimization**
    - Use appropriate joins
    - Avoid SELECT *
    - Use WHERE clauses effectively
    - Consider query execution plan

3. **Data Integrity**
    - Use appropriate constraints
    - Implement proper foreign keys
    - Validate data at application level
    - Regular data quality checks

4. **Performance**
    - Regular statistics updates
    - Proper index maintenance
    - Query optimization
    - Regular monitoring 