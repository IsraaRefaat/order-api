# Order API Database Schema

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

### Composite Indexes
- `idx_orders_customer_email_status`: Composite index for customer-status queries
- `idx_orders_created_at_status`: Composite index for date-status queries  
- `idx_order_items_order_product`: Composite index for order-product lookups

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

## Views

### v_order_summary
Provides order summaries with item counts and total quantities.

```sql
SELECT 
    o.id, o.customer_email, o.order_total, o.discount_amount, 
    o.final_amount, o.applied_coupon_code, o.status, 
    o.created_at, o.updated_at,
    COUNT(oi.id) as item_count,
    SUM(oi.quantity) as total_quantity
FROM orders o
LEFT JOIN order_items oi ON o.id = oi.order_id
GROUP BY o.id, ...
```

### v_customer_order_stats  
Provides customer order statistics for analytics.

```sql
SELECT 
    customer_email,
    COUNT(*) as total_orders,
    SUM(final_amount) as total_spent,
    AVG(final_amount) as avg_order_value,
    MAX(created_at) as last_order_date,
    MIN(created_at) as first_order_date
FROM orders
GROUP BY customer_email
```

## Stored Functions

### get_order_with_items(order_id_param BIGINT)
Returns complete order details with all associated items in a single query.

## Migration History

### 01-initial-schema.xml
- Created initial `orders` and `order_items` tables
- Created sequences and foreign key constraints
- Set up basic table structure

### 02-replace-coupon-relation.xml  
- Replaced `coupon_id` foreign key with `applied_coupon_code` string
- Updated for microservices architecture (no direct FK to coupon service)

### 03-add-product-id-to-order-items.xml
- Added `product_id` column to `order_items` table for catalog service integration
- Made `customer_name` nullable (not used in current implementation)
- Added basic performance indexes
- Set up NOT NULL constraint on `product_id` for new orders

### 04-catalog-service-integration.xml
- Added data migration for existing records
- Added comprehensive check constraints for data integrity
- Added table and column comments for documentation

### 05-schema-cleanup-and-optimization.xml
- Added composite indexes for complex queries
- Created database views for common query patterns
- Added stored functions for efficient data retrieval

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

- All frequently queried columns have appropriate indexes
- Composite indexes support common query patterns
- Views pre-aggregate data for reporting queries
- Check constraints ensure data integrity at database level
- Foreign key constraints maintain referential integrity

## Maintenance

- Regular VACUUM and ANALYZE on high-traffic tables
- Monitor index usage and add/remove as needed
- Review query performance periodically 
- Consider partitioning `orders` table by date if volume grows large
- Archive old order data to maintain performance

## Backup and Recovery

- Full database backup before major schema changes
- Point-in-time recovery enabled
- Test restore procedures regularly
- Document rollback procedures for each migration