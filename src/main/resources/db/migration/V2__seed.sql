-- Demo data. All seeded users share password: "password" (bcrypt below)
-- Org: f0000001-0001-0001-0001-000000000001

INSERT INTO organizations (id, name, created_at, updated_at)
VALUES ('f0000001-0001-0001-0001-000000000001', 'Acme Demo', NOW(), NOW());

INSERT INTO organization_modules (org_id, module_code) VALUES
    ('f0000001-0001-0001-0001-000000000001', 'CUSTOMERS'),
    ('f0000001-0001-0001-0001-000000000001', 'ORDERS'),
    ('f0000001-0001-0001-0001-000000000001', 'PAYMENTS'),
    ('f0000001-0001-0001-0001-000000000001', 'EXPENSES'),
    ('f0000001-0001-0001-0001-000000000001', 'SALES'),
    ('f0000001-0001-0001-0001-000000000001', 'HR'),
    ('f0000001-0001-0001-0001-000000000001', 'REMINDERS'),
    ('f0000001-0001-0001-0001-000000000001', 'ANALYTICS'),
    ('f0000001-0001-0001-0001-000000000001', 'BILLING'),
    ('f0000001-0001-0001-0001-000000000001', 'NOTIFICATIONS');

INSERT INTO app_users (id, org_id, email, password_hash, role, enabled, created_at, updated_at) VALUES
    ('f0000002-0002-0002-0002-000000000002', 'f0000001-0001-0001-0001-000000000001', 'admin@acme.demo',
     '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'ADMIN', TRUE, NOW(), NOW()),
    ('f0000003-0003-0003-0003-000000000003', 'f0000001-0001-0001-0001-000000000001', 'manager@acme.demo',
     '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'MANAGER', TRUE, NOW(), NOW()),
    ('f0000004-0004-0004-0004-000000000004', 'f0000001-0001-0001-0001-000000000001', 'sales@acme.demo',
     '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'SALES_OFFICER', TRUE, NOW(), NOW());

INSERT INTO sales_officers (id, org_id, name, user_id, territory, created_at, updated_at)
VALUES ('f0000005-0005-0005-0005-000000000005', 'f0000001-0001-0001-0001-000000000001', 'Field Rep',
        'f0000004-0004-0004-0004-000000000004', 'North', NOW(), NOW());

INSERT INTO customers (id, org_id, name, type, phone, whatsapp_number, email, address, credit_limit,
                       payment_terms_days, assigned_sales_officer_id, created_at, updated_at)
VALUES ('f1000001-0001-0001-0001-000000000001', 'f0000001-0001-0001-0001-000000000001', 'Globex Retail',
        'RETAILER', '+10000000001', '+10000000001', 'billing@globex.example', '1 Market St', 50000.0000, 30,
        'f0000005-0005-0005-0005-000000000005', NOW(), NOW());

INSERT INTO orders (id, org_id, customer_id, order_date, due_date, total_amount, status, created_at, updated_at)
VALUES ('f2000001-0001-0001-0001-000000000001', 'f0000001-0001-0001-0001-000000000001',
        'f1000001-0001-0001-0001-000000000001', DATE '2026-03-19', DATE '2026-04-18', 10000.0000, 'PENDING',
        NOW(), NOW());

INSERT INTO payments (id, org_id, order_id, amount, payment_date, method, created_at, updated_at)
VALUES ('f3000001-0001-0001-0001-000000000001', 'f0000001-0001-0001-0001-000000000001',
        'f2000001-0001-0001-0001-000000000001', 3500.0000, DATE '2026-04-10', 'BANK_TRANSFER', NOW(), NOW());

UPDATE orders SET status = 'PARTIAL' WHERE id = 'f2000001-0001-0001-0001-000000000001';

INSERT INTO reminder_rules (id, org_id, trigger_type, days_offset, channel, created_at, updated_at) VALUES
    ('f4000001-0001-0001-0001-000000000001', 'f0000001-0001-0001-0001-000000000001', 'BEFORE_DUE', -2, 'EMAIL', NOW(), NOW()),
    ('f4000002-0002-0002-0002-000000000002', 'f0000001-0001-0001-0001-000000000001', 'AFTER_DUE', 1, 'SMS', NOW(), NOW());

INSERT INTO expenses (id, org_id, user_id, amount, category, description, date, created_at, updated_at)
VALUES ('f5000001-0001-0001-0001-000000000001', 'f0000001-0001-0001-0001-000000000001',
        'f0000002-0002-0002-0002-000000000002', 120.5000, 'TRAVEL', 'Client visit', DATE '2026-04-01', NOW(), NOW());
