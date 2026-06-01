package com.warehouse.mapper;

import com.warehouse.entity.PurchaseOrder;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface PurchaseOrderMapper {

    @Select("SELECT id FROM purchase_orders WHERE order_no=#{orderNo} AND order_line=#{orderLine} AND material_code=#{materialCode} LIMIT 1")
    Integer findId(PurchaseOrder order);

    @Insert("INSERT INTO purchase_orders(order_no, order_line, material_code, name, model, unit, supplier, order_quantity, order_date, delivery_date, remark) " +
            "VALUES(#{orderNo}, #{orderLine}, #{materialCode}, #{name}, #{model}, #{unit}, #{supplier}, #{orderQuantity}, #{orderDate}, #{deliveryDate}, #{remark})")
    int insert(PurchaseOrder order);

    @Update("UPDATE purchase_orders SET name=#{name}, model=#{model}, unit=#{unit}, supplier=#{supplier}, " +
            "order_quantity=#{orderQuantity}, order_date=#{orderDate}, delivery_date=#{deliveryDate}, remark=#{remark} WHERE id=#{id}")
    int update(PurchaseOrder order);

    @Select("<script>" +
            "SELECT * FROM purchase_orders WHERE 1=1 " +
            "<if test='keyword != null and keyword != \"\"'>" +
            "AND (order_no LIKE CONCAT('%', #{keyword}, '%') OR material_code LIKE CONCAT('%', #{keyword}, '%') OR name LIKE CONCAT('%', #{keyword}, '%') OR supplier LIKE CONCAT('%', #{keyword}, '%')) " +
            "</if>" +
            "ORDER BY id DESC LIMIT #{limit}" +
            "</script>")
    List<PurchaseOrder> list(@Param("keyword") String keyword, @Param("limit") Integer limit);

    @Select("<script>" +
            "SELECT po.*, COALESCE(SUM(a.arrival_quantity), 0) AS referenced_quantity " +
            "FROM purchase_orders po " +
            "LEFT JOIN arrivals a ON a.purchase_order_no=po.order_no AND a.purchase_order_line=po.order_line AND a.material_code=po.material_code " +
            "WHERE po.material_code=#{materialCode} " +
            "<if test='supplier != null and supplier != \"\"'>AND (po.supplier=#{supplier} OR po.supplier='') </if>" +
            "GROUP BY po.id HAVING po.order_quantity &gt; referenced_quantity " +
            "ORDER BY po.order_date='', po.order_date, po.id LIMIT 1" +
            "</script>")
    PurchaseOrder matchOpen(@Param("materialCode") String materialCode, @Param("supplier") String supplier);

    @Select("SELECT po.order_no, po.order_line, po.material_code, po.name, po.model, po.unit, po.supplier, po.order_quantity, " +
            "COALESCE(SUM(a.arrival_quantity), 0) AS arrival_quantity, " +
            "COALESCE(SUM(CASE WHEN a.status='已入库' THEN a.arrival_quantity ELSE 0 END), 0) AS stored_quantity, " +
            "GREATEST(po.order_quantity - COALESCE(SUM(CASE WHEN a.status='已入库' THEN a.arrival_quantity ELSE 0 END), 0), 0) AS not_stored_quantity, " +
            "CASE WHEN COALESCE(SUM(CASE WHEN a.status='已入库' THEN a.arrival_quantity ELSE 0 END), 0) <= 0 THEN '未入库' " +
            "WHEN COALESCE(SUM(CASE WHEN a.status='已入库' THEN a.arrival_quantity ELSE 0 END), 0) >= po.order_quantity THEN '已全部入库' ELSE '部分入库' END AS inbound_status " +
            "FROM purchase_orders po LEFT JOIN arrivals a ON a.purchase_order_no=po.order_no AND a.purchase_order_line=po.order_line AND a.material_code=po.material_code " +
            "GROUP BY po.id ORDER BY po.order_no, po.order_line, po.material_code")
    List<Map<String, Object>> inboundReport();

    @Select("SELECT po.order_no, po.order_line, po.material_code, po.name, po.model, po.unit, po.supplier, po.order_quantity, " +
            "COALESCE(SUM(a.arrival_quantity), 0) AS referenced_quantity, " +
            "GREATEST(po.order_quantity - COALESCE(SUM(a.arrival_quantity), 0), 0) AS unreferenced_quantity, " +
            "CASE WHEN COALESCE(SUM(a.arrival_quantity), 0) <= 0 THEN '未引用' " +
            "WHEN COALESCE(SUM(a.arrival_quantity), 0) >= po.order_quantity THEN '已全部引用' ELSE '部分引用' END AS reference_status " +
            "FROM purchase_orders po LEFT JOIN arrivals a ON a.purchase_order_no=po.order_no AND a.purchase_order_line=po.order_line AND a.material_code=po.material_code " +
            "GROUP BY po.id ORDER BY po.order_no, po.order_line, po.material_code")
    List<Map<String, Object>> referenceReport();
}
