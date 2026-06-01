package com.warehouse.service;

import com.warehouse.entity.Arrival;
import com.warehouse.entity.PurchaseOrder;
import com.warehouse.mapper.ArrivalMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ArrivalService {

    @Autowired
    private ArrivalMapper arrivalMapper;

    @Autowired
    private PurchaseOrderService purchaseOrderService;

    public List<Arrival> list(String keyword, String status, String supplier, String keeper,
                              String dateFrom, String dateTo, Integer limit) {
        if (limit == null || limit <= 0) limit = 5000;
        return arrivalMapper.list(keyword, status, supplier, keeper, dateFrom, dateTo, limit);
    }

    public Arrival getById(Integer id) {
        return arrivalMapper.getById(id);
    }

    public int add(Arrival arrival) {
        sanitize(arrival);
        arrival.setStatus("待认领");
        return arrivalMapper.insert(arrival);
    }

    public int update(Arrival arrival) {
        sanitize(arrival);
        if (arrival.getStatus() == null) {
            Arrival existing = arrivalMapper.getById(arrival.getId());
            if (existing != null) {
                arrival.setStatus(deriveStatus(existing, arrival));
            }
        }
        return arrivalMapper.update(arrival);
    }

    public int delete(Integer id) {
        return arrivalMapper.delete(id);
    }

    public int claim(Integer id, String keeper) {
        Arrival a = new Arrival();
        a.setId(id);
        a.setWarehouseKeeper(keeper.trim());
        a.setStatus("已认领");
        return arrivalMapper.update(a);
    }

    public int accept(Integer id) {
        return accept(id, null);
    }

    public int accept(Integer id, String acceptanceTime) {
        Arrival a = new Arrival();
        a.setId(id);
        a.setAcceptanceTime(parseDateTimeOrNow(acceptanceTime));
        a.setStatus("已验收");
        return arrivalMapper.update(a);
    }

    public int shelf(Integer id) {
        return shelf(id, null);
    }

    public int shelf(Integer id, String shelvingTime) {
        Arrival a = new Arrival();
        a.setId(id);
        a.setShelvingTime(parseDateTimeOrNow(shelvingTime));
        a.setStatus("已上架");
        return arrivalMapper.update(a);
    }

    public int store(Integer id, String receiptNo) {
        Arrival a = new Arrival();
        a.setId(id);
        a.setReceiptNumber(receiptNo.trim());
        a.setStatus("已入库");
        return arrivalMapper.update(a);
    }

    public int batchImport(List<Arrival> rows) {
        int n = 0;
        for (Arrival r : rows) {
            if (r.getMaterialCode() == null || r.getMaterialCode().trim().isEmpty()) continue;
            sanitize(r);
            r.setStatus(deriveStatus(null, r));
            arrivalMapper.insert(r);
            n++;
        }
        return n;
    }

    public List<String> distinctSuppliers() {
        return arrivalMapper.distinctValues("supplier");
    }

    public List<String> distinctKeepers() {
        return arrivalMapper.distinctValues("warehouse_keeper");
    }

    public Map<String, Object> stats() {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> rows = arrivalMapper.statusCount();
        Map<String, Integer> byStatus = new HashMap<>();
        for (Map<String, Object> row : rows) {
            String status = (String) row.get("status");
            Number cnt = (Number) row.get("cnt");
            byStatus.put(status, cnt.intValue());
        }
        result.put("byStatus", byStatus);
        return result;
    }

    private void sanitize(Arrival a) {
        if (a.getName() == null) a.setName("");
        if (a.getModel() == null) a.setModel("");
        if (a.getUnit() == null) a.setUnit("");
        if (a.getPurchaseOrderNo() == null) a.setPurchaseOrderNo("");
        if (a.getPurchaseOrderLine() == null) a.setPurchaseOrderLine("");
        if (a.getSource() == null) a.setSource("");
        if (a.getSupplier() == null) a.setSupplier("");
        if (a.getWaybillNo() == null) a.setWaybillNo("");
        if (a.getPackaging() == null) a.setPackaging("");
        if (a.getRemark() == null) a.setRemark("");
        if (a.getWarehouseKeeper() == null) a.setWarehouseKeeper("");
        if (a.getReceiptNumber() == null) a.setReceiptNumber("");
        if (a.getArrivalQuantity() == null) a.setArrivalQuantity(BigDecimal.ZERO);
        if (a.getPackageCount() == null) a.setPackageCount(0);
        if (a.getWeight() == null) a.setWeight(BigDecimal.ZERO);
        if (a.getPurchaseOrderNo().trim().isEmpty()) {
            PurchaseOrder po = purchaseOrderService.matchOpen(a.getMaterialCode(), a.getSupplier());
            if (po != null) {
                a.setPurchaseOrderNo(po.getOrderNo());
                a.setPurchaseOrderLine(po.getOrderLine());
                if (a.getName().isEmpty()) a.setName(po.getName());
                if (a.getModel().isEmpty()) a.setModel(po.getModel());
                if (a.getUnit().isEmpty()) a.setUnit(po.getUnit());
                if (a.getSupplier().isEmpty()) a.setSupplier(po.getSupplier());
            }
        }
    }

    private String deriveStatus(Arrival existing, Arrival update) {
        String receipt = update.getReceiptNumber() != null ? update.getReceiptNumber() : "";
        if (existing != null && receipt.isEmpty()) receipt = existing.getReceiptNumber() != null ? existing.getReceiptNumber() : "";
        if (!receipt.isEmpty()) return "已入库";

        LocalDateTime shelf = update.getShelvingTime();
        if (shelf == null && existing != null) shelf = existing.getShelvingTime();
        if (shelf != null) return "已上架";

        LocalDateTime accept = update.getAcceptanceTime();
        if (accept == null && existing != null) accept = existing.getAcceptanceTime();
        if (accept != null) return "已验收";

        String keeper = update.getWarehouseKeeper() != null ? update.getWarehouseKeeper() : "";
        if (existing != null && keeper.isEmpty()) keeper = existing.getWarehouseKeeper() != null ? existing.getWarehouseKeeper() : "";
        if (!keeper.isEmpty()) return "已认领";

        return "待认领";
    }

    private LocalDateTime parseDateTimeOrNow(String value) {
        if (value == null || value.trim().isEmpty()) {
            return LocalDateTime.now();
        }
        String text = value.trim().replace('T', ' ');
        String[] patterns = {
                "yyyy-MM-dd HH:mm:ss",
                "yyyy-MM-dd HH:mm",
                "yyyy/MM/dd HH:mm:ss",
                "yyyy/MM/dd HH:mm"
        };
        for (String pattern : patterns) {
            try {
                return LocalDateTime.parse(text, DateTimeFormatter.ofPattern(pattern));
            } catch (DateTimeParseException e) {
                // try next pattern
            }
        }
        return LocalDateTime.now();
    }
}
