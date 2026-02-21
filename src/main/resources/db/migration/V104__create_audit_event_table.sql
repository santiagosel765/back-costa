CREATE TABLE IF NOT EXISTS audit_event (
    id UUID PRIMARY KEY,
    tenant_id UUID,
    actor VARCHAR(255) NOT NULL,
    actor_user_id UUID,
    action VARCHAR(120) NOT NULL,
    entity_type VARCHAR(120),
    entity_id VARCHAR(255),
    request_id VARCHAR(120),
    trace_id VARCHAR(120),
    ip_address VARCHAR(64),
    user_agent VARCHAR(512),
    payload_json JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_audit_event_tenant_id ON audit_event (tenant_id);
CREATE INDEX IF NOT EXISTS idx_audit_event_created_at ON audit_event (created_at);
CREATE INDEX IF NOT EXISTS idx_audit_event_action ON audit_event (action);
