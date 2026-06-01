package com.warehouse.service;

import com.warehouse.entity.Material;
import com.warehouse.mapper.MaterialMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MaterialService {

    @Autowired
    private MaterialMapper materialMapper;

    public List<Material> list(String keyword, Integer limit) {
        if (limit == null || limit <= 0) limit = 5000;
        return materialMapper.list(keyword, limit);
    }

    public Material getByCode(String code) {
        if (code == null) return null;
        code = code.trim();
        if (code.isEmpty()) return null;
        Material m = materialMapper.getByCode(code);
        if (m == null) {
            m = materialMapper.fuzzyGetByCode(code);
        }
        return m;
    }

    public int add(Material material) {
        sanitize(material);
        return materialMapper.insert(material);
    }

    public int update(Material material) {
        return materialMapper.update(material);
    }

    public int delete(Integer id) {
        return materialMapper.delete(id);
    }

    public int[] batchUpsert(List<Material> materials) {
        int inserted = 0, updated = 0;
        for (Material m : materials) {
            if (m.getMaterialCode() == null || m.getMaterialCode().trim().isEmpty()) continue;
            m.setMaterialCode(m.getMaterialCode().trim());
            m.setName(m.getName() == null ? "" : m.getName().trim());
            m.setModel(m.getModel() == null ? "" : m.getModel().trim());
            m.setUnit(m.getUnit() == null ? "" : m.getUnit().trim());
            m.setRemark(m.getRemark() == null ? "" : m.getRemark().trim());
            Integer existingId = materialMapper.findIdByCode(m.getMaterialCode());
            if (existingId != null) {
                m.setId(existingId);
                materialMapper.update(m);
                updated++;
            } else {
                materialMapper.insert(m);
                inserted++;
            }
        }
        return new int[]{inserted, updated};
    }

    public int countAll() {
        return materialMapper.countAll();
    }

    private void sanitize(Material m) {
        if (m.getMaterialCode() != null) m.setMaterialCode(m.getMaterialCode().trim());
        if (m.getName() != null) m.setName(m.getName().trim());
        if (m.getModel() != null) m.setModel(m.getModel().trim());
        if (m.getUnit() != null) m.setUnit(m.getUnit().trim());
        if (m.getRemark() != null) m.setRemark(m.getRemark().trim());
    }
}
