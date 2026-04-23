CREATE TABLE compensation_plans (
    id UUID PRIMARY KEY,
    org_id UUID NOT NULL REFERENCES organizations (id) ON DELETE CASCADE,
    employee_user_id UUID NOT NULL,
    base_salary NUMERIC(19, 4) NOT NULL,
    target_amount NUMERIC(19, 4) NOT NULL,
    incentive_percent NUMERIC(7, 4) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE UNIQUE INDEX uk_comp_plan_org_employee ON compensation_plans (org_id, employee_user_id);

ALTER TABLE salaries
    ADD COLUMN base_amount NUMERIC(19, 4) NOT NULL DEFAULT 0,
    ADD COLUMN target_amount NUMERIC(19, 4) NOT NULL DEFAULT 0,
    ADD COLUMN achieved_amount NUMERIC(19, 4) NOT NULL DEFAULT 0,
    ADD COLUMN incentive_amount NUMERIC(19, 4) NOT NULL DEFAULT 0,
    ADD COLUMN status VARCHAR(32) NOT NULL DEFAULT 'DRAFT';

CREATE UNIQUE INDEX uk_salary_org_employee_period ON salaries (org_id, employee_user_id, period_month);
