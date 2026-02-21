package com.ferrisys.service.audit;

import com.ferrisys.common.dto.PageResponse;
import com.ferrisys.common.dto.audit.AuditEventRequest;
import com.ferrisys.common.dto.audit.AuditEventResponse;

public interface AuditEventService {

    void publish(AuditEventPublishRequest event);

    PageResponse<AuditEventResponse> findEvents(AuditEventRequest request);
}
