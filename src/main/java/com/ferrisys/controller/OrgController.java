package com.ferrisys.controller;

import com.ferrisys.common.api.ApiResponse;
import com.ferrisys.common.dto.PageResponse;
import com.ferrisys.common.dto.org.BranchDTO;
import com.ferrisys.common.dto.org.UserBranchAssignmentDTO;
import com.ferrisys.common.dto.org.WarehouseDTO;
import com.ferrisys.common.exception.impl.BadRequestException;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/org")
@RequiredArgsConstructor
@RequireModule("org")
public class OrgController {

    private final OrgService orgService;

    @GetMapping("/branches")
    public ApiResponse<List<BranchDTO>> branches(@RequestParam(defaultValue = "0") int page,
                                                 @RequestParam(defaultValue = "10") int size,
                                                 @RequestParam(defaultValue = "") String search) {
        PageResponse<BranchDTO> response = orgService.listBranches(page, size, search);
        return ApiResponse.list(response.content(), response.totalElements(), response.page(), response.size(), response.totalPages());
    }

    @PostMapping("/branches")
    public ApiResponse<BranchDTO> createBranch(@RequestBody BranchDTO dto) { return ApiResponse.single(orgService.saveBranch(dto)); }
    @PutMapping("/branches/{id}")
    public ApiResponse<BranchDTO> updateBranch(@PathVariable UUID id, @RequestBody BranchDTO dto) { return ApiResponse.single(orgService.updateBranch(id, dto)); }
    @DeleteMapping("/branches/{id}")
    public ApiResponse<Void> deleteBranch(@PathVariable UUID id) { orgService.deleteBranch(id); return ApiResponse.single(null); }

    @GetMapping("/branches/{branchId}/warehouses")
    public ApiResponse<List<WarehouseDTO>> warehouses(@PathVariable UUID branchId,
                                                      @RequestParam(defaultValue = "0") int page,
                                                      @RequestParam(defaultValue = "10") int size,
                                                      @RequestParam(defaultValue = "") String search) {
        PageResponse<WarehouseDTO> response = orgService.listWarehouses(branchId, page, size, search);
        return ApiResponse.list(response.content(), response.totalElements(), response.page(), response.size(), response.totalPages());
    }

    @PostMapping("/branches/{branchId}/warehouses")
    public ApiResponse<WarehouseDTO> createWarehouse(@PathVariable UUID branchId, @RequestBody WarehouseDTO dto) {
        return ApiResponse.single(orgService.saveWarehouse(branchId, dto));
    }

    @PutMapping("/warehouses/{id}")
    public ApiResponse<WarehouseDTO> updateWarehouse(@PathVariable UUID id, @RequestBody WarehouseDTO dto) {
        return ApiResponse.single(orgService.updateWarehouse(id, dto));
    }

    @DeleteMapping("/warehouses/{id}")
    public ApiResponse<Void> deleteWarehouse(@PathVariable UUID id) {
        orgService.deleteWarehouse(id);
        return ApiResponse.single(null);
    }

    @GetMapping("/user-branch-assignments")
    public ApiResponse<List<UserBranchAssignmentDTO>> listUserBranchAssignments(@RequestParam(required = false) UUID userId,
                                                                                 @RequestParam(required = false) UUID branchId,
                                                                                 @RequestParam(defaultValue = "0") int page,
                                                                                 @RequestParam(defaultValue = "10") int size) {
        if (userId == null && branchId == null) {
            throw new BadRequestException("Debe enviar userId o branchId");
        }
        PageResponse<UserBranchAssignmentDTO> response = orgService.listUserBranchAssignments(userId, branchId, page, size);
        return ApiResponse.list(response.content(), response.totalElements(), response.page(), response.size(), response.totalPages());
    }

    @PostMapping("/user-branch-assignments")
    public ApiResponse<UserBranchAssignmentDTO> createUserBranchAssignment(@RequestBody UserBranchAssignmentDTO dto) {
        return ApiResponse.single(orgService.createUserBranchAssignment(UUID.fromString(dto.userId()), UUID.fromString(dto.branchId())));
    }

    @DeleteMapping("/user-branch-assignments/{id}")
    public ApiResponse<Void> deleteUserBranchAssignment(@PathVariable UUID id) {
        orgService.deleteUserBranchAssignment(id);
        return ApiResponse.single(null);
    }

    @GetMapping("/user/branches")
    public ApiResponse<List<BranchDTO>> currentUserBranches() {
        List<BranchDTO> branches = orgService.currentUserBranches();
        return ApiResponse.single(branches);
    }
}
