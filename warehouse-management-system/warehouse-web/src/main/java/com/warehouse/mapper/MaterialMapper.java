package com.warehouse.mapper;

import com.warehouse.entity.Material;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface MaterialMapper {

    @Select("<script>" +
            "SELECT * FROM materials " +
            "<where>" +
            "<if test='keyword != null and keyword != \"\"'>" +
            "  AND (material_code LIKE CONCAT('%', #{keyword}, '%') OR name LIKE CONCAT('%', #{keyword}, '%') OR model LIKE CONCAT('%', #{keyword}, '%'))" +
            "</if>" +
            "</where>" +
            "ORDER BY id DESC LIMIT #{limit}" +
            "</script>")
    List<Material> list(@Param("keyword") String keyword, @Param("limit") Integer limit);

    @Select("SELECT * FROM materials WHERE TRIM(material_code) = TRIM(#{code}) LIMIT 1")
    Material getByCode(String code);

    @Insert("INSERT INTO materials(material_code, name, model, unit, remark) " +
            "VALUES(TRIM(#{materialCode}), #{name}, #{model}, #{unit}, #{remark})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Material material);

    @Update("UPDATE materials SET material_code = TRIM(#{materialCode}), name = #{name}, " +
            "model = #{model}, unit = #{unit}, remark = #{remark} WHERE id = #{id}")
    int update(Material material);

    @Delete("DELETE FROM materials WHERE id = #{id}")
    int delete(Integer id);

    @Select("SELECT id FROM materials WHERE TRIM(material_code) = TRIM(#{code}) LIMIT 1")
    Integer findIdByCode(String code);

    @Select("SELECT COUNT(*) FROM materials")
    int countAll();

    @Select("SELECT * FROM materials WHERE TRIM(material_code) LIKE CONCAT('%', TRIM(#{code}), '%') LIMIT 1")
    Material fuzzyGetByCode(String code);
}
