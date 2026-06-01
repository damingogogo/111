package com.warehouse.service;

import com.warehouse.entity.Material;
import com.warehouse.entity.PurchaseOrder;
import com.warehouse.mapper.MaterialMapper;
import com.warehouse.mapper.PurchaseOrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
public class PurchaseOrderService {

    @Autowired
    private PurchaseOrderMapper purchaseOrderMapper;

    @Autowired
    private MaterialMapper materialMapper;

    public int[] batchUpsert(List<PurchaseOrder> rows) {
        int inserted = 0;
        int updated = 0;
        for (PurchaseOrder row : rows) {
            sanitize(row);
            if (row.getOrderNo().isEmpty() || row.getMaterialCode().isEmpty()) {
                continue;
            }
            syncMaterial(row);
            Integer id = purchaseOrderMapper.findId(row);
            if (id == null) {
                purchaseOrderMapper.insert(row);
                inserted++;
            } else {
                row.setId(id);
                purchaseOrderMapper.update(row);
                updated++;
            }
        }
        return new int[]{inserted, updated};
    }

    public List<PurchaseOrder> list(String keyword, Integer limit) {
        if (limit == null || limit <= 0) limit = 5000;
        return purchaseOrderMapper.list(keyword, limit);
    }

    public PurchaseOrder matchOpen(String materialCode, String supplier) {
        if (materialCode == null || materialCode.trim().isEmpty()) return null;
        return purchaseOrderMapper.matchOpen(materialCode.trim(), supplier == null ? "" : supplier.trim());
    }

    public List<Map<String, Object>> inboundReport() {
        return purchaseOrderMapper.inboundReport();
    }

    public List<Map<String, Object>> referenceReport() {
        return purchaseOrderMapper.referenceReport();
    }

    private void syncMaterial(PurchaseOrder row) {
        Integer id = materialMapper.findIdByCode(row.getMaterialCode());
        Material material = new Material();
        material.setMaterialCode(row.getMaterialCode());
        material.setName(row.getName());
        material.setModel(row.getModel());
        material.setUnit(row.getUnit());
        material.setRemark("采购订单导入");
        if (id == null) {
            materialMapper.insert(material);
        } else {
            material.setId(id);
            materialMapper.update(material);
        }
    }

    private void sanitize(PurchaseOrder row) {
        if (row.getOrderNo() == null) row.setOrderNo("");
        if (row.getOrderLine() == null) row.setOrderLine("");
        if (row.getMaterialCode() == null) row.setMaterialCode("");
        if (row.getName() == null) row.setName("");
        if (row.getModel() == null) row.setModel("");
        if (row.getUnit() == null) row.setUnit("");
        if (row.getSupplier() == null) row.setSupplier("");
        if (row.getOrderQuantity() == null) row.setOrderQuantity(BigDecimal.ZERO);
        if (row.getOrderDate() == null) row.setOrderDate("");
        if (row.getDeliveryDate() == null) row.setDeliveryDate("");
        if (row.getRemark() == null) row.setRemark("");
        row.setOrderNo(row.getOrderNo().trim());
        row.setOrderLine(row.getOrderLine().trim());
        row.setMaterialCode(row.getMaterialCode().trim());
    }
}
