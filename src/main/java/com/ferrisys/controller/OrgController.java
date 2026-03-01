package com.ferrisys.controller;

import com.ferrisys.common.api.ApiResponse;
import com.ferrisys.common.dto.PageResponse;
import com.ferrisys.common.dto.org.BranchDTO;
import com.ferrisys.common.dto.org.CreateDocumentNumberingRequest;
import com.ferrisys.common.dto.org.CreateWarehouseRequest;
import com.ferrisys.common.dto.org.DocumentNumberingDTO;
import com.ferrisys.common.dto.org.UpdateDocumentNumberingRequest;
import com.ferrisys.common.dto.org.UpdateWarehouseRequest;
import com.ferrisys.common.dto.org.UserBranchAssignmentDTO;
import com.ferrisys.common.dto.org.WarehouseDTO;
import com.ferrisys.config.license.RequireModule;
import com.ferrisys.service.org.OrgService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/v1/org")
@RequiredArgsConstructor
@RequireModule("org")
public class OrgController {

    private final OrgService orgService;

    @GetMapping("/branches")
    public ApiResponse<List<BranchDTO>> branches(@RequestParam(defaultValue = "1") int page,
                                                 @RequestParam(defaultValue = "10") int size,
                                                 @RequestParam(defaultValue = "") String search) {
        PageResponse<BranchDTO> response = orgService.listBranches(page, size, search);
        return ApiResponse.list(response.content(), response.totalElements(), response.page(), response.size(), response.totalPages());
    }

    @PostMapping("/branches")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<BranchDTO> createBranch(@RequestBody BranchDTO dto) { return ApiResponse.single(orgService.saveBranch(dto)); }
    @PutMapping("/branches/{id}")
    public ApiResponse<BranchDTO> updateBranch(@PathVariable UUID id, @RequestBody BranchDTO dto) { return ApiResponse.single(orgService.updateBranch(id, dto)); }
    @DeleteMapping("/branches/{id}")
    public ApiResponse<Void> deleteBranch(@PathVariable UUID id) { orgService.deleteBranch(id); return ApiResponse.single(null); }

    @GetMapping("/branches/{branchId}/warehouses")
    public ApiResponse<List<WarehouseDTO>> warehouses(@PathVariable UUID branchId,
                                                      @RequestParam(defaultValue = "1") int page,
                                                      @RequestParam(defaultValue = "10") int size,
                                                      @RequestParam(defaultValue = "") String search) {
        PageResponse<WarehouseDTO> response = orgService.listWarehouses(branchId, page, size, search);
        return ApiResponse.list(response.content(), response.totalElements(), response.page(), response.size(), response.totalPages());
    }

    @PostMapping("/branches/{branchId}/warehouses")
    public ApiResponse<WarehouseDTO> createWarehouse(@PathVariable UUID branchId, @RequestBody WarehouseDTO dto) {
        return ApiResponse.single(orgService.saveWarehouse(branchId, dto));
    }

    @DeleteMapping("/warehouses/{id}")
    public ApiResponse<Void> deleteWarehouse(@PathVariable UUID id) {
        orgService.deleteWarehouse(id);
        return ApiResponse.single(null);
    }

    @GetMapping("/warehouses")
    public ApiResponse<List<WarehouseDTO>> listWarehouses(@RequestParam(defaultValue = "1") int page,
                                                          @RequestParam(defaultValue = "10") int size,
                                                          @RequestParam(defaultValue = "") String search,
                                                          @RequestParam(required = false) UUID branchId,
                                                          @RequestParam(required = false) Boolean active) {
        PageResponse<WarehouseDTO> response = orgService.listWarehouses(branchId, active, page, size, search);
        return ApiResponse.list(response.content(), response.totalElements(), response.page(), response.size(), response.totalPages());
    }

    @PostMapping("/warehouses")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<WarehouseDTO> createWarehouse(@RequestBody CreateWarehouseRequest dto) {
        return ApiResponse.single(orgService.createWarehouse(dto));
    }

    @PutMapping("/warehouses/{id}")
    public ApiResponse<WarehouseDTO> updateWarehouseV2(@PathVariable UUID id, @RequestBody UpdateWarehouseRequest dto) {
        return ApiResponse.single(orgService.updateWarehouse(id, dto));
    }

    @GetMapping("/document-numbering")
    public ApiResponse<List<DocumentNumberingDTO>> listDocumentNumbering(@RequestParam(defaultValue = "1") int page,
                                                                          @RequestParam(defaultValue = "10") int size,
                                                                          @RequestParam(defaultValue = "") String search,
                                                                          @RequestParam(required = false) UUID branchId,
                                                                          @RequestParam(required = false) UUID documentTypeId,
                                                                          @RequestParam(required = false) Boolean active) {
        PageResponse<DocumentNumberingDTO> response = orgService.listDocumentNumbering(branchId, documentTypeId, active, page, size, search);
        return ApiResponse.list(response.content(), response.totalElements(), response.page(), response.size(), response.totalPages());
    }

    @PostMapping("/document-numbering")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<DocumentNumberingDTO> createDocumentNumbering(@RequestBody CreateDocumentNumberingRequest dto) {
        return ApiResponse.single(orgService.createDocumentNumbering(dto));
    }

    @PutMapping("/document-numbering/{id}")
    public ApiResponse<DocumentNumberingDTO> updateDocumentNumbering(@PathVariable UUID id,
                                                                      @RequestBody UpdateDocumentNumberingRequest dto) {
        return ApiResponse.single(orgService.updateDocumentNumbering(id, dto));
    }

    @DeleteMapping("/document-numbering/{id}")
    public ApiResponse<Void> deleteDocumentNumbering(@PathVariable UUID id) {
        orgService.deleteDocumentNumbering(id);
        return ApiResponse.single(null);
    }

    @GetMapping("/document-numbering/{id}/preview")
    public ApiResponse<String> previewDocumentNumbering(@PathVariable UUID id) {
        return ApiResponse.single(orgService.previewDocumentNumbering(id));
    }

    @GetMapping({"/user-branch-assignments", "/assignments"})
    public ApiResponse<List<UserBranchAssignmentDTO>> listUserBranchAssignments(@RequestParam(required = false) UUID userId,
                                                                                 @RequestParam(required = false) UUID branchId,
                                                                                 @RequestParam(defaultValue = "1") int page,
                                                                                 @RequestParam(defaultValue = "10") int size) {
        PageResponse<UserBranchAssignmentDTO> response = orgService.listUserBranchAssignments(userId, branchId, page, size);
        return ApiResponse.list(response.content(), response.totalElements(), response.page(), response.size(), response.totalPages());
    }

    @PostMapping({"/user-branch-assignments", "/assignments"})
    public ApiResponse<UserBranchAssignmentDTO> createUserBranchAssignment(@RequestBody UserBranchAssignmentDTO dto) {
        return ApiResponse.single(orgService.createUserBranchAssignment(UUID.fromString(dto.userId()), UUID.fromString(dto.branchId())));
    }

    @DeleteMapping({"/user-branch-assignments/{id}", "/assignments/{id}"})
    public ApiResponse<Void> deleteUserBranchAssignment(@PathVariable UUID id) {
        orgService.deleteUserBranchAssignment(id);
        return ApiResponse.single(null);
    }

    @GetMapping("/me/branches")
    public ApiResponse<List<BranchDTO>> currentUserBranches() {
        List<BranchDTO> branches = orgService.currentUserBranches();
        return ApiResponse.single(branches);
    }

    @GetMapping("/user/branches")
    public ApiResponse<List<BranchDTO>> currentUserBranchesLegacy() {
        return currentUserBranches();
    }

    @GetMapping("/me/branch")
    public ApiResponse<BranchDTO> currentUserBranch() {
        BranchDTO branch = orgService.currentUserBranch();
        return ApiResponse.single(branch);
    }

    @GetMapping("/me/branches/{branchId}/validate")
    public ApiResponse<Void> validateCurrentUserBranch(@PathVariable UUID branchId) {
        orgService.validateCurrentUserBranch(branchId);
        return ApiResponse.single(null);
    }
}
