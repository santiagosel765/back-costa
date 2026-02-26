package com.ferrisys.service.org;

import com.ferrisys.common.dto.PageResponse;
import com.ferrisys.common.dto.org.BranchDTO;
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
    void deleteWarehouse(UUID id);

    List<BranchDTO> currentUserBranches();
}
