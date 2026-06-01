package com.warehouse.controller;

import com.warehouse.entity.Arrival;
import com.warehouse.entity.Material;
import com.warehouse.entity.PurchaseOrder;
import com.warehouse.service.ArrivalService;
import com.warehouse.service.MaterialService;
import com.warehouse.service.PurchaseOrderService;
import com.warehouse.util.ExcelUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ApiController {

    @Autowired
    private MaterialService materialService;

    @Autowired
    private ArrivalService arrivalService;

    @Autowired
    private PurchaseOrderService purchaseOrderService;

    // ================== 物资基础信息 ==================

    @GetMapping("/materials")
    public List<Material> listMaterials(@RequestParam(defaultValue = "") String keyword) {
        return materialService.list(keyword, 5000);
    }

    @GetMapping("/materials/{code}")
    public Material getMaterial(@PathVariable String code) {
        return materialService.getByCode(code);
    }

    @PostMapping("/materials")
    public ResponseEntity<?> addMaterial(@RequestBody Material material) {
        materialService.add(material);
        return ResponseEntity.ok(Map.of("success", true, "id", material.getId()));
    }

    @PutMapping("/materials/{id}")
    public ResponseEntity<?> updateMaterial(@PathVariable Integer id, @RequestBody Material material) {
        material.setId(id);
        materialService.update(material);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @DeleteMapping("/materials/{id}")
    public ResponseEntity<?> deleteMaterial(@PathVariable Integer id) {
        materialService.delete(id);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @PostMapping("/materials/import")
    public ResponseEntity<?> importMaterials(@RequestParam("file") MultipartFile file) throws Exception {
        List<Material> list = ExcelUtil.readMaterials(file.getInputStream());
        int[] result = materialService.batchUpsert(list);
        return ResponseEntity.ok(Map.of("success", true, "inserted", result[0], "updated", result[1]));
    }

    @GetMapping("/materials/template")
    public void materialTemplate(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                "attachment;filename=" + URLEncoder.encode("物资基础数据模板.xlsx", "UTF-8"));
        ExcelUtil.writeMaterialTemplate(response.getOutputStream());
    }

    @GetMapping("/materials/export")
    public void exportMaterials(@RequestParam(defaultValue = "") String keyword,
                                 HttpServletResponse response) throws IOException {
        List<Material> list = materialService.list(keyword, 5000);
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                "attachment;filename=" + URLEncoder.encode("物资基础数据.xlsx", "UTF-8"));
        ExcelUtil.writeMaterials(list, response.getOutputStream());
    }

    // ================== 采购订单 ==================

    @GetMapping("/purchase-orders")
    public List<PurchaseOrder> listPurchaseOrders(@RequestParam(defaultValue = "") String keyword) {
        return purchaseOrderService.list(keyword, 5000);
    }

    @PostMapping("/purchase-orders/import")
    public ResponseEntity<?> importPurchaseOrders(@RequestParam("file") MultipartFile file) throws Exception {
        List<PurchaseOrder> list = ExcelUtil.readPurchaseOrders(file.getInputStream());
        int[] result = purchaseOrderService.batchUpsert(list);
        return ResponseEntity.ok(Map.of("success", true, "inserted", result[0], "updated", result[1]));
    }

    @GetMapping("/purchase-orders/match")
    public PurchaseOrder matchPurchaseOrder(@RequestParam(defaultValue = "") String materialCode,
                                            @RequestParam(defaultValue = "") String supplier) {
        return purchaseOrderService.matchOpen(materialCode, supplier);
    }

    // ================== 到货登记 ==================

    @GetMapping("/arrivals")
    public List<Arrival> listArrivals(@RequestParam(defaultValue = "") String keyword,
                                       @RequestParam(defaultValue = "") String status,
                                       @RequestParam(defaultValue = "") String supplier,
                                       @RequestParam(defaultValue = "") String keeper,
                                       @RequestParam(defaultValue = "") String dateFrom,
                                       @RequestParam(defaultValue = "") String dateTo) {
        return arrivalService.list(keyword, status, supplier, keeper, dateFrom, dateTo, 5000);
    }

    @GetMapping("/arrivals/{id}")
    public Arrival getArrival(@PathVariable Integer id) {
        return arrivalService.getById(id);
    }

    @PostMapping("/arrivals")
    public ResponseEntity<?> addArrival(@RequestBody Arrival arrival) {
        arrivalService.add(arrival);
        return ResponseEntity.ok(Map.of("success", true, "id", arrival.getId()));
    }

    @PutMapping("/arrivals/{id}")
    public ResponseEntity<?> updateArrival(@PathVariable Integer id, @RequestBody Arrival arrival) {
        arrival.setId(id);
        arrivalService.update(arrival);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @DeleteMapping("/arrivals/{id}")
    public ResponseEntity<?> deleteArrival(@PathVariable Integer id) {
        arrivalService.delete(id);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @PostMapping("/arrivals/import")
    public ResponseEntity<?> importArrivals(@RequestParam("file") MultipartFile file) throws Exception {
        List<Arrival> list = ExcelUtil.readArrivals(file.getInputStream());
        int n = arrivalService.batchImport(list);
        return ResponseEntity.ok(Map.of("success", true, "count", n));
    }

    @GetMapping("/arrivals/template")
    public void arrivalTemplate(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                "attachment;filename=" + URLEncoder.encode("到货登记模板.xlsx", "UTF-8"));
        ExcelUtil.writeArrivalTemplate(response.getOutputStream());
    }

    @PostMapping("/arrivals/{id}/claim")
    public ResponseEntity<?> claimArrival(@PathVariable Integer id, @RequestBody Map<String, String> body) {
        arrivalService.claim(id, body.getOrDefault("keeper", ""));
        return ResponseEntity.ok(Map.of("success", true));
    }

    @PostMapping("/arrivals/{id}/accept")
    public ResponseEntity<?> acceptArrival(@PathVariable Integer id,
                                           @RequestBody(required = false) Map<String, String> body) {
        String acceptanceTime = body != null ? body.getOrDefault("acceptanceTime", "") : "";
        arrivalService.accept(id, acceptanceTime);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @PostMapping("/arrivals/{id}/shelf")
    public ResponseEntity<?> shelfArrival(@PathVariable Integer id,
                                          @RequestBody(required = false) Map<String, String> body) {
        String shelvingTime = body != null ? body.getOrDefault("shelvingTime", "") : "";
        arrivalService.shelf(id, shelvingTime);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @PostMapping("/arrivals/{id}/store")
    public ResponseEntity<?> storeArrival(@PathVariable Integer id, @RequestBody Map<String, String> body) {
        arrivalService.store(id, body.getOrDefault("receiptNumber", ""));
        return ResponseEntity.ok(Map.of("success", true));
    }

    // ================== 公共 ==================

    @GetMapping("/distinct/suppliers")
    public List<String> distinctSuppliers() {
        return arrivalService.distinctSuppliers();
    }

    @GetMapping("/distinct/keepers")
    public List<String> distinctKeepers() {
        return arrivalService.distinctKeepers();
    }

    @GetMapping("/stats")
    public Map<String, Object> stats() {
        Map<String, Object> result = new HashMap<>();
        result.put("materials", materialService.countAll());
        result.putAll(arrivalService.stats());
        return result;
    }

    @PostMapping("/export/arrivals")
    public void exportArrivals(@RequestBody Map<String, String> params, HttpServletResponse response) throws IOException {
        writeArrivalExport(params, response);
    }

    @GetMapping("/export/arrivals")
    public void exportArrivalsGet(@RequestParam(defaultValue = "") String keyword,
                                  @RequestParam(defaultValue = "") String status,
                                  @RequestParam(defaultValue = "") String supplier,
                                  @RequestParam(defaultValue = "") String keeper,
                                  @RequestParam(defaultValue = "") String dateFrom,
                                  @RequestParam(defaultValue = "") String dateTo,
                                  HttpServletResponse response) throws IOException {
        Map<String, String> params = new HashMap<>();
        params.put("keyword", keyword);
        params.put("status", status);
        params.put("supplier", supplier);
        params.put("keeper", keeper);
        params.put("dateFrom", dateFrom);
        params.put("dateTo", dateTo);
        writeArrivalExport(params, response);
    }

    @GetMapping("/export/inbound-status")
    public void exportInboundStatus(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                "attachment;filename=" + URLEncoder.encode("已到货物资入库情况表.xlsx", "UTF-8"));
        ExcelUtil.writeInboundReport(purchaseOrderService.inboundReport(), response.getOutputStream());
    }

    @GetMapping("/export/purchase-order-reference")
    public void exportPurchaseOrderReference(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                "attachment;filename=" + URLEncoder.encode("采购订单引用情况表.xlsx", "UTF-8"));
        ExcelUtil.writeReferenceReport(purchaseOrderService.referenceReport(), response.getOutputStream());
    }

    private void writeArrivalExport(Map<String, String> params, HttpServletResponse response) throws IOException {
        String keyword = params.getOrDefault("keyword", "");
        String status = params.getOrDefault("status", "");
        String supplier = params.getOrDefault("supplier", "");
        String keeper = params.getOrDefault("keeper", "");
        String dateFrom = params.getOrDefault("dateFrom", "");
        String dateTo = params.getOrDefault("dateTo", "");
        List<Arrival> list = arrivalService.list(keyword, status, supplier, keeper, dateFrom, dateTo, 5000);
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                "attachment;filename=" + URLEncoder.encode("到货入库进度.xlsx", "UTF-8"));
        ExcelUtil.writeArrivals(list, response.getOutputStream());
    }
}
