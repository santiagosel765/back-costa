package com.ferrisys.service.org;

import com.ferrisys.common.dto.PageResponse;
import com.ferrisys.common.dto.org.BranchDTO;
import com.ferrisys.common.dto.org.CreateDocumentNumberingRequest;
import com.ferrisys.common.dto.org.CreateWarehouseRequest;
import com.ferrisys.common.dto.org.DocumentNumberingDTO;
import com.ferrisys.common.dto.org.UpdateDocumentNumberingRequest;
import com.ferrisys.common.dto.org.UpdateWarehouseRequest;
import com.ferrisys.common.dto.org.UserBranchAssignmentDTO;
import com.ferrisys.common.dto.org.WarehouseDTO;
import java.util.List;
import java.util.UUID;

public interface OrgService {
    PageResponse<BranchDTO> listBranches(int page, int size, String search);
    BranchDTO saveBranch(BranchDTO dto);
    BranchDTO updateBranch(UUID id, BranchDTO dto);
    void deleteBranch(UUID id);

    PageResponse<WarehouseDTO> listWarehouses(UUID branchId, int page, int size, String search);
    WarehouseDTO saveWarehouse(UUID branchId, WarehouseDTO dto);
    WarehouseDTO updateWarehouse(UUID id, WarehouseDTO dto);
    PageResponse<WarehouseDTO> listWarehouses(UUID branchId, Boolean active, int page, int size, String search);
    WarehouseDTO createWarehouse(CreateWarehouseRequest dto);
    WarehouseDTO updateWarehouse(UUID id, UpdateWarehouseRequest dto);
    void deleteWarehouse(UUID id);

    PageResponse<DocumentNumberingDTO> listDocumentNumbering(UUID branchId, UUID documentTypeId, Boolean active, int page, int size, String search);
    DocumentNumberingDTO createDocumentNumbering(CreateDocumentNumberingRequest dto);
    DocumentNumberingDTO updateDocumentNumbering(UUID id, UpdateDocumentNumberingRequest dto);
    void deleteDocumentNumbering(UUID id);
    String previewDocumentNumbering(UUID id);

    PageResponse<UserBranchAssignmentDTO> listUserBranchAssignments(UUID userId, UUID branchId, int page, int size);
    UserBranchAssignmentDTO createUserBranchAssignment(UUID userId, UUID branchId);
    void deleteUserBranchAssignment(UUID id);

    List<BranchDTO> currentUserBranches();

    BranchDTO currentUserBranch();

    void validateCurrentUserBranch(UUID branchId);
}
