package com.ferrisys.controller;

import com.ferrisys.common.dto.PageResponse;
import com.ferrisys.common.dto.audit.AuditEventRequest;
import com.ferrisys.common.dto.audit.AuditEventResponse;
import com.ferrisys.service.audit.AuditEventService;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/audit/events")
@RequiredArgsConstructor
public class AuditEventController {

    private final AuditEventService auditEventService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('MODULE_CORE_AUTH')")
    public PageResponse<AuditEventResponse> list(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) String entityId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return auditEventService.findEvents(new AuditEventRequest(from, to, action, entityType, entityId, page, size));
    }
}
