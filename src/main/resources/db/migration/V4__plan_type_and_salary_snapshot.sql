-- Categorical compensation + audit snapshot on finalized pay

ALTER TABLE compensation_plans
    ADD COLUMN plan_type VARCHAR(32) NOT NULL DEFAULT 'TARGET_INCENTIVE';

ALTER TABLE salaries
    ADD COLUMN plan_type VARCHAR(32),
    ADD COLUMN plan_snapshot_json TEXT;
