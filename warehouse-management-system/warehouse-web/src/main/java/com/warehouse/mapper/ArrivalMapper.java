package com.warehouse.mapper;

import com.warehouse.entity.Arrival;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface ArrivalMapper {

    @Insert("INSERT INTO arrivals(material_code, name, model, unit, purchase_order_no, purchase_order_line, `source`, supplier, waybill_no, arrival_quantity, " +
            "packaging, package_count, weight, remark, status) " +
            "VALUES(#{materialCode}, #{name}, #{model}, #{unit}, #{purchaseOrderNo}, #{purchaseOrderLine}, #{source}, #{supplier}, #{waybillNo}, #{arrivalQuantity}, " +
            "#{packaging}, #{packageCount}, #{weight}, #{remark}, '待认领')")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Arrival arrival);

    @Select("<script>" +
            "SELECT * FROM arrivals WHERE 1=1 " +
            "<if test='keyword != null and keyword != \"\"'>" +
            "  AND (material_code LIKE CONCAT('%', #{keyword}, '%') OR name LIKE CONCAT('%', #{keyword}, '%') OR receipt_number LIKE CONCAT('%', #{keyword}, '%') OR waybill_no LIKE CONCAT('%', #{keyword}, '%') OR `source` LIKE CONCAT('%', #{keyword}, '%') OR purchase_order_no LIKE CONCAT('%', #{keyword}, '%'))" +
            "</if>" +
            "<if test='status != null and status != \"\"'>" +
            "  AND status = #{status}" +
            "</if>" +
            "<if test='supplier != null and supplier != \"\"'>" +
            "  AND supplier LIKE CONCAT('%', #{supplier}, '%')" +
            "</if>" +
            "<if test='keeper != null and keeper != \"\"'>" +
            "  AND warehouse_keeper LIKE CONCAT('%', #{keeper}, '%')" +
            "</if>" +
            "<if test='dateFrom != null and dateFrom != \"\"'>" +
            "  AND arrival_time &gt;= #{dateFrom}" +
            "</if>" +
            "<if test='dateTo != null and dateTo != \"\"'>" +
            "  AND arrival_time &lt;= #{dateTo}" +
            "</if>" +
            "ORDER BY id DESC LIMIT #{limit}" +
            "</script>")
    List<Arrival> list(@Param("keyword") String keyword, @Param("status") String status,
                       @Param("supplier") String supplier, @Param("keeper") String keeper,
                       @Param("dateFrom") String dateFrom, @Param("dateTo") String dateTo,
                       @Param("limit") Integer limit);

    @Select("SELECT * FROM arrivals WHERE id = #{id}")
    Arrival getById(Integer id);

    @Update("<script>" +
            "UPDATE arrivals " +
            "<set>" +
            "<if test='materialCode != null'>material_code = #{materialCode},</if>" +
            "<if test='name != null'>name = #{name},</if>" +
            "<if test='model != null'>model = #{model},</if>" +
            "<if test='unit != null'>unit = #{unit},</if>" +
            "<if test='purchaseOrderNo != null'>purchase_order_no = #{purchaseOrderNo},</if>" +
            "<if test='purchaseOrderLine != null'>purchase_order_line = #{purchaseOrderLine},</if>" +
            "<if test='source != null'>`source` = #{source},</if>" +
            "<if test='supplier != null'>supplier = #{supplier},</if>" +
            "<if test='waybillNo != null'>waybill_no = #{waybillNo},</if>" +
            "<if test='arrivalQuantity != null'>arrival_quantity = #{arrivalQuantity},</if>" +
            "<if test='packaging != null'>packaging = #{packaging},</if>" +
            "<if test='packageCount != null'>package_count = #{packageCount},</if>" +
            "<if test='weight != null'>weight = #{weight},</if>" +
            "<if test='warehouseKeeper != null'>warehouse_keeper = #{warehouseKeeper},</if>" +
            "<if test='acceptanceTime != null'>acceptance_time = #{acceptanceTime},</if>" +
            "<if test='shelvingTime != null'>shelving_time = #{shelvingTime},</if>" +
            "<if test='receiptNumber != null'>receipt_number = #{receiptNumber},</if>" +
            "<if test='status != null'>status = #{status},</if>" +
            "<if test='remark != null'>remark = #{remark},</if>" +
            "</set>" +
            "WHERE id = #{id}" +
            "</script>")
    int update(Arrival arrival);

    @Delete("DELETE FROM arrivals WHERE id = #{id}")
    int delete(Integer id);

    @Select("SELECT DISTINCT ${column} FROM arrivals WHERE ${column} != '' ORDER BY ${column}")
    List<String> distinctValues(@Param("column") String column);

    @Select("SELECT status, COUNT(*) AS cnt FROM arrivals GROUP BY status")
    List<Map<String, Object>> statusCount();
}
