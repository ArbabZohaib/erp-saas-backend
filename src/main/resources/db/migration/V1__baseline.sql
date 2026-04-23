CREATE TABLE organizations (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE organization_modules (
    org_id UUID NOT NULL REFERENCES organizations (id) ON DELETE CASCADE,
    module_code VARCHAR(64) NOT NULL,
    PRIMARY KEY (org_id, module_code)
);

CREATE TABLE app_users (
    id UUID PRIMARY KEY,
    org_id UUID NOT NULL REFERENCES organizations (id) ON DELETE CASCADE,
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(32) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_app_users_org_email UNIQUE (org_id, email)
);

CREATE TABLE customers (
    id UUID PRIMARY KEY,
    org_id UUID NOT NULL REFERENCES organizations (id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(32) NOT NULL,
    phone VARCHAR(64),
    whatsapp_number VARCHAR(64),
    email VARCHAR(255),
    address VARCHAR(1024),
    credit_limit NUMERIC(19, 4),
    payment_terms_days INTEGER NOT NULL,
    assigned_sales_officer_id UUID,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE orders (
    id UUID PRIMARY KEY,
    org_id UUID NOT NULL REFERENCES organizations (id) ON DELETE CASCADE,
    customer_id UUID NOT NULL REFERENCES customers (id) ON DELETE CASCADE,
    order_date DATE NOT NULL,
    due_date DATE NOT NULL,
    total_amount NUMERIC(19, 4) NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE payments (
    id UUID PRIMARY KEY,
    org_id UUID NOT NULL REFERENCES organizations (id) ON DELETE CASCADE,
    order_id UUID NOT NULL REFERENCES orders (id) ON DELETE CASCADE,
    amount NUMERIC(19, 4) NOT NULL,
    payment_date DATE NOT NULL,
    method VARCHAR(32) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE expenses (
    id UUID PRIMARY KEY,
    org_id UUID NOT NULL REFERENCES organizations (id) ON DELETE CASCADE,
    user_id UUID NOT NULL,
    amount NUMERIC(19, 4) NOT NULL,
    category VARCHAR(255) NOT NULL,
    description VARCHAR(2048),
    date DATE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE sales_officers (
    id UUID PRIMARY KEY,
    org_id UUID NOT NULL REFERENCES organizations (id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    user_id UUID,
    territory VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE attendances (
    id UUID PRIMARY KEY,
    org_id UUID NOT NULL REFERENCES organizations (id) ON DELETE CASCADE,
    sales_officer_id UUID NOT NULL REFERENCES sales_officers (id) ON DELETE CASCADE,
    check_in TIMESTAMP WITH TIME ZONE NOT NULL,
    check_out TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE performances (
    id UUID PRIMARY KEY,
    org_id UUID NOT NULL REFERENCES organizations (id) ON DELETE CASCADE,
    sales_officer_id UUID NOT NULL REFERENCES sales_officers (id) ON DELETE CASCADE,
    total_sales NUMERIC(19, 4) NOT NULL,
    incentives NUMERIC(19, 4) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE leave_requests (
    id UUID PRIMARY KEY,
    org_id UUID NOT NULL REFERENCES organizations (id) ON DELETE CASCADE,
    employee_user_id UUID NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    reason VARCHAR(2048),
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE salaries (
    id UUID PRIMARY KEY,
    org_id UUID NOT NULL REFERENCES organizations (id) ON DELETE CASCADE,
    employee_user_id UUID NOT NULL,
    period_month DATE NOT NULL,
    amount NUMERIC(19, 4) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE reminder_rules (
    id UUID PRIMARY KEY,
    org_id UUID NOT NULL REFERENCES organizations (id) ON DELETE CASCADE,
    trigger_type VARCHAR(32) NOT NULL,
    days_offset INTEGER NOT NULL,
    channel VARCHAR(32) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE reminders (
    id UUID PRIMARY KEY,
    org_id UUID NOT NULL REFERENCES organizations (id) ON DELETE CASCADE,
    customer_id UUID NOT NULL REFERENCES customers (id) ON DELETE CASCADE,
    order_id UUID NOT NULL REFERENCES orders (id) ON DELETE CASCADE,
    type VARCHAR(32) NOT NULL,
    scheduled_at TIMESTAMP WITH TIME ZONE NOT NULL,
    status VARCHAR(32) NOT NULL,
    reminder_rule_id UUID REFERENCES reminder_rules (id) ON DELETE SET NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_orders_org_customer ON orders (org_id, customer_id);
CREATE INDEX idx_payments_org_order ON payments (org_id, order_id);
CREATE INDEX idx_reminders_org_sched ON reminders (org_id, scheduled_at);
