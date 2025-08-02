# Order API Database Schema

**Author**: Esraa Refaat

This document describes the database schema for the Order API service and the changes made to support catalog service integration.

## Overview

The Order API uses PostgreSQL with Liquibase for database migration management. The schema consists of two main tables: `orders` and `order_items`, along with supporting indexes, constraints, and views.

## Tables

### orders
Main table storing order information.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PRIMARY KEY, NOT NULL | Unique order identifier |
| customer_email | VARCHAR(100) | NOT NULL | Customer email address |
| customer_name | VARCHAR(100) | NULL | Customer name (optional, for display) |
| order_total | DECIMAL(10,2) | NOT NULL, >= 0 | Total amount before discounts |
| discount_amount | DECIMAL(10,2) | NULL, >= 0 | Amount discounted from order total |
| final_amount | DECIMAL(10,2) | NOT NULL, >= 0 | Final amount after applying discounts |
| applied_coupon_code | VARCHAR(100) | NULL | Coupon code applied to this order |
| status | VARCHAR(50) | NOT NULL | Order status (PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED) |
| customer_transaction_id | VARCHAR(255) | NULL | Transaction ID for customer payment |
| merchant_transaction_id | VARCHAR(255) | NULL | Transaction ID for merchant deposit |
| created_at | TIMESTAMP | NOT NULL | Timestamp when order was created |
| updated_at | TIMESTAMP | NOT NULL | Timestamp when order was last updated |

### order_items
Table storing individual products in each order.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PRIMARY KEY, NOT NULL | Unique order item identifier |
| order_id | BIGINT | NOT NULL, FK to orders.id | Reference to parent order |
| product_id | BIGINT | NOT NULL | Product ID from catalog service |
| product_name | VARCHAR(200) | NOT NULL | Product name fetched from catalog service |
| quantity | INTEGER | NOT NULL, > 0 | Quantity of this product in the order |
| unit_price | DECIMAL(10,2) | NOT NULL, >= 0 | Price per unit fetched from catalog service |
| total_price | DECIMAL(10,2) | NOT NULL, >= 0 | Total price for this line item (quantity * unit_price) |

## Sequences

- `SEQ_ORDERS`: Sequence for order IDs, starts at 1, increments by 1
- `SEQ_ORDER_ITEMS`: Sequence for order item IDs, starts at 1, increments by 1

## Indexes

### Performance Indexes
- `idx_orders_customer_email`: Single column index on customer_email for customer order lookups
- `idx_orders_status`: Single column index on status for status-based queries
- `idx_orders_created_at`: Single column index on created_at for date-based queries
- `idx_order_items_product_id`: Single column index on product_id for product-based queries

## Constraints

### Check Constraints
- `chk_order_items_quantity_positive`: Ensures quantity > 0
- `chk_order_items_unit_price_positive`: Ensures unit_price >= 0
- `chk_order_items_total_price_positive`: Ensures total_price >= 0
- `chk_orders_total_positive`: Ensures order_total >= 0
- `chk_orders_final_amount_positive`: Ensures final_amount >= 0
- `chk_orders_discount_amount_positive`: Ensures discount_amount >= 0

### Foreign Key Constraints
- `fk_order_items_order_id`: Links order_items.order_id to orders.id


## Migration History

### 01-initial-schema.xml
- Created initial `orders` and `order_items` tables with sequences
- Initial structure includes `coupon_id` (later replaced)
- Set up foreign key constraints
- Customer name initially required (later made nullable)

### 02-replace-coupon-relation.xml
- Dropped `coupon_id` foreign key column
- Added `applied_coupon_code` VARCHAR(100) column
- Updated for microservices architecture (no direct FK to coupon service)

### 03-add-product-id-to-order-items.xml
- Added `product_id` BIGINT column to `order_items` table (initially nullable)
- Made `customer_name` nullable for orders table
- Added NOT NULL constraint on `product_id` for new records
- Created performance indexes:
  - `idx_order_items_product_id` on order_items.product_id
  - `idx_orders_customer_email` on orders.customer_email
  - `idx_orders_status` on orders.status
  - `idx_orders_created_at` on orders.created_at

### 04-catalog-service-integration.xml
- Data migration: Updated existing order_items with default product_id = 1
- Added comprehensive check constraints for data integrity:
  - `chk_order_items_quantity_positive`: quantity > 0
  - `chk_order_items_unit_price_positive`: unit_price >= 0
  - `chk_order_items_total_price_positive`: total_price >= 0
  - `chk_orders_total_positive`: order_total >= 0
  - `chk_orders_final_amount_positive`: final_amount >= 0
  - `chk_orders_discount_amount_positive`: discount_amount >= 0

### 05-add-transaction-ids.xml
- Added `customer_transaction_id` VARCHAR(255) column to orders table
- Added `merchant_transaction_id` VARCHAR(255) column to orders table
- Both columns nullable to support payment transaction tracking
- Enables audit trail for customer withdrawals and merchant deposits

## Catalog Service Integration Changes

The major schema change was adding support for catalog service integration:

### Before
- Clients provided product name and price in order requests
- No product ID tracking
- Potential for data inconsistency

### After  
- Only product ID required from clients
- Product name and price fetched from catalog service
- Guaranteed data consistency with catalog
- Product ID stored for audit trail and future lookups

### Migration Path
1. Added `product_id` column as nullable
2. Updated existing records with default product IDs
3. Made `product_id` NOT NULL for new records
4. Added indexes for performance
5. Updated application code to use catalog service

## Performance Considerations

- Single column indexes on frequently queried fields (customer_email, status, created_at, product_id)
- Check constraints ensure data integrity at database level
- Foreign key constraints maintain referential integrity
- Auto-increment primary keys for optimal performance

## Data Integrity Features

### Check Constraints
- `chk_order_items_quantity_positive`: Ensures quantity > 0
- `chk_order_items_unit_price_positive`: Ensures unit_price >= 0
- `chk_order_items_total_price_positive`: Ensures total_price >= 0
- `chk_orders_total_positive`: Ensures order_total >= 0
- `chk_orders_final_amount_positive`: Ensures final_amount >= 0
- `chk_orders_discount_amount_positive`: Ensures discount_amount >= 0

### Foreign Key Constraints
- `fk_order_items_order_id`: Links order_items.order_id to orders.id

## Documentation

- Migration rollback procedures included in each changeset
- Clear audit trail of schema evolution through Liquibase changesets