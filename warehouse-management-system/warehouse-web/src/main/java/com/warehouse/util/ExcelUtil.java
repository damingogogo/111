package com.warehouse.util;

import com.warehouse.entity.Arrival;
import com.warehouse.entity.Material;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ExcelUtil {

    private static final String[] MATERIAL_HEADERS = {"物资编码", "名称", "型号", "单位", "备注"};
    private static final String[] ARRIVAL_HEADERS = {"物资编码", "名称", "型号", "单位", "来源", "供应商", "运单号",
            "到货数量", "包装", "件数", "重量", "管库员", "验收完成时间", "上架时间", "入库单号", "到货登记时间", "状态", "备注"};

    private static CellStyle headerStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        font.setFontName("微软雅黑");
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private static CellStyle bodyStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setFontName("微软雅黑");
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private static void writeHeader(Row row, String[] headers, CellStyle style) {
        for (int i = 0; i < headers.length; i++) {
            Cell cell = row.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(style);
        }
    }

    private static void writeBodyCell(Row row, int col, Object value, CellStyle style) {
        Cell cell = row.createCell(col);
        if (value == null) {
            cell.setCellValue("");
        } else if (value instanceof Number) {
            cell.setCellValue(((Number) value).doubleValue());
        } else {
            cell.setCellValue(value.toString());
        }
        cell.setCellStyle(style);
    }

    // ============ 模板 ============

    public static void writeMaterialTemplate(OutputStream out) throws IOException {
        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet("物资基础");
        Row headerRow = sheet.createRow(0);
        headerRow.setHeightInPoints(28);
        writeHeader(headerRow, MATERIAL_HEADERS, headerStyle(wb));

        CellStyle body = bodyStyle(wb);
        Row r1 = sheet.createRow(1);
        writeBodyCell(r1, 0, "WZ-0001", body);
        writeBodyCell(r1, 1, "镀锌钢管", body);
        writeBodyCell(r1, 2, "DN50", body);
        writeBodyCell(r1, 3, "米", body);
        writeBodyCell(r1, 4, "示例数据", body);

        Row r2 = sheet.createRow(2);
        writeBodyCell(r2, 0, "WZ-0002", body);
        writeBodyCell(r2, 1, "六角螺栓", body);
        writeBodyCell(r2, 2, "M12x60", body);
        writeBodyCell(r2, 3, "颗", body);
        writeBodyCell(r2, 4, "", body);

        for (int i = 0; i < MATERIAL_HEADERS.length; i++) {
            sheet.setColumnWidth(i, 18);
        }
        wb.write(out);
        wb.close();
    }

    public static void writeArrivalTemplate(OutputStream out) throws IOException {
        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet("到货登记");
        Row headerRow = sheet.createRow(0);
        headerRow.setHeightInPoints(28);
        writeHeader(headerRow, ARRIVAL_HEADERS, headerStyle(wb));

        CellStyle body = bodyStyle(wb);
        Row r1 = sheet.createRow(1);
        writeBodyCell(r1, 0, "WZ-0001", body);
        writeBodyCell(r1, 1, "镀锌钢管", body);
        writeBodyCell(r1, 2, "DN50", body);
        writeBodyCell(r1, 3, "米", body);
        writeBodyCell(r1, 4, "项目现场", body);
        writeBodyCell(r1, 5, "示例供应商", body);
        writeBodyCell(r1, 6, "SF123456789", body);
        writeBodyCell(r1, 7, 100, body);
        writeBodyCell(r1, 8, "纸箱", body);
        writeBodyCell(r1, 9, 5, body);
        writeBodyCell(r1, 10, 320, body);
        writeBodyCell(r1, 11, "", body);
        writeBodyCell(r1, 12, "", body);
        writeBodyCell(r1, 13, "", body);
        writeBodyCell(r1, 14, "", body);
        writeBodyCell(r1, 15, "", body);
        writeBodyCell(r1, 16, "待认领", body);
        writeBodyCell(r1, 17, "示例", body);

        for (int i = 0; i < ARRIVAL_HEADERS.length; i++) {
            sheet.setColumnWidth(i, 16);
        }
        wb.write(out);
        wb.close();
    }

    // ============ 读取 ============

    public static List<Material> readMaterials(InputStream in) throws IOException {
        Workbook wb = new XSSFWorkbook(in);
        Sheet sheet = wb.getSheetAt(0);
        List<Material> list = new ArrayList<>();

        Row headerRow = sheet.getRow(0);
        if (headerRow == null) return list;

        java.util.Map<String, Integer> colMap = new java.util.HashMap<>();
        for (Cell cell : headerRow) {
            String val = getCellString(cell);
            if (val != null) colMap.put(val.trim(), cell.getColumnIndex());
        }

        Integer codeIdx = colMap.get("物资编码");
        Integer nameIdx = colMap.get("名称");
        if (codeIdx == null || nameIdx == null) {
            throw new IllegalArgumentException("Excel缺少必填列「物资编码」或「名称」");
        }

        for (int r = 1; r <= sheet.getLastRowNum(); r++) {
            Row row = sheet.getRow(r);
            if (row == null) continue;
            String code = getCellString(row.getCell(codeIdx));
            if (code == null || code.trim().isEmpty()) continue;
            Material m = new Material();
            m.setMaterialCode(code.trim());
            m.setName(getCellString(row.getCell(nameIdx)));
            m.setModel(getCellString(row.getCell(colMap.getOrDefault("型号", -1))));
            m.setUnit(getCellString(row.getCell(colMap.getOrDefault("单位", -1))));
            m.setRemark(getCellString(row.getCell(colMap.getOrDefault("备注", -1))));
            list.add(m);
        }
        wb.close();
        return list;
    }

    public static List<Arrival> readArrivals(InputStream in) throws IOException {
        Workbook wb = new XSSFWorkbook(in);
        Sheet sheet = wb.getSheetAt(0);
        List<Arrival> list = new ArrayList<>();

        Row headerRow = sheet.getRow(0);
        if (headerRow == null) return list;

        java.util.Map<String, Integer> colMap = new java.util.HashMap<>();
        for (Cell cell : headerRow) {
            String val = getCellString(cell);
            if (val != null) colMap.put(val.trim(), cell.getColumnIndex());
        }

        Integer codeIdx = colMap.get("物资编码");
        if (codeIdx == null) {
            throw new IllegalArgumentException("Excel缺少必填列「物资编码」");
        }

        for (int r = 1; r <= sheet.getLastRowNum(); r++) {
            Row row = sheet.getRow(r);
            if (row == null) continue;
            String code = getCellString(row.getCell(codeIdx));
            if (code == null || code.trim().isEmpty()) continue;
            Arrival a = new Arrival();
            a.setMaterialCode(code.trim());
            a.setName(getCellString(row.getCell(colMap.getOrDefault("名称", -1))));
            a.setModel(getCellString(row.getCell(colMap.getOrDefault("型号", -1))));
            a.setUnit(getCellString(row.getCell(colMap.getOrDefault("单位", -1))));
            a.setSource(getCellString(row.getCell(colMap.getOrDefault("来源", -1))));
            a.setSupplier(getCellString(row.getCell(colMap.getOrDefault("供应商", -1))));
            a.setWaybillNo(getCellString(row.getCell(colMap.getOrDefault("运单号", -1))));
            a.setArrivalQuantity(getCellBigDecimal(row.getCell(colMap.getOrDefault("到货数量", -1))));
            a.setPackaging(getCellString(row.getCell(colMap.getOrDefault("包装", -1))));
            a.setPackageCount(getCellInt(row.getCell(colMap.getOrDefault("件数", -1))));
            a.setWeight(getCellBigDecimal(row.getCell(colMap.getOrDefault("重量", -1))));
            a.setWarehouseKeeper(getCellString(row.getCell(colMap.getOrDefault("管库员", -1))));
            a.setAcceptanceTime(parseDateTime(getCellString(row.getCell(colMap.getOrDefault("验收完成时间", -1)))));
            a.setShelvingTime(parseDateTime(getCellString(row.getCell(colMap.getOrDefault("上架时间", -1)))));
            a.setReceiptNumber(getCellString(row.getCell(colMap.getOrDefault("入库单号", -1))));
            a.setRemark(getCellString(row.getCell(colMap.getOrDefault("备注", -1))));
            list.add(a);
        }
        wb.close();
        return list;
    }

    // ============ 导出 ============

    public static void writeMaterials(List<Material> list, OutputStream out) throws IOException {
        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet("物资基础");
        Row headerRow = sheet.createRow(0);
        headerRow.setHeightInPoints(28);
        writeHeader(headerRow, MATERIAL_HEADERS, headerStyle(wb));

        CellStyle body = bodyStyle(wb);
        int rowIdx = 1;
        for (Material m : list) {
            Row row = sheet.createRow(rowIdx++);
            writeBodyCell(row, 0, m.getMaterialCode(), body);
            writeBodyCell(row, 1, m.getName(), body);
            writeBodyCell(row, 2, m.getModel(), body);
            writeBodyCell(row, 3, m.getUnit(), body);
            writeBodyCell(row, 4, m.getRemark(), body);
        }
        for (int i = 0; i < MATERIAL_HEADERS.length; i++) {
            sheet.setColumnWidth(i, 18);
        }
        wb.write(out);
        wb.close();
    }

    public static void writeArrivals(List<Arrival> list, OutputStream out) throws IOException {
        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet("到货入库进度");
        Row headerRow = sheet.createRow(0);
        headerRow.setHeightInPoints(28);
        writeHeader(headerRow, ARRIVAL_HEADERS, headerStyle(wb));

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        CellStyle body = bodyStyle(wb);
        int rowIdx = 1;
        for (Arrival a : list) {
            Row row = sheet.createRow(rowIdx++);
            writeBodyCell(row, 0, a.getMaterialCode(), body);
            writeBodyCell(row, 1, a.getName(), body);
            writeBodyCell(row, 2, a.getModel(), body);
            writeBodyCell(row, 3, a.getUnit(), body);
            writeBodyCell(row, 4, a.getSource(), body);
            writeBodyCell(row, 5, a.getSupplier(), body);
            writeBodyCell(row, 6, a.getWaybillNo(), body);
            writeBodyCell(row, 7, a.getArrivalQuantity(), body);
            writeBodyCell(row, 8, a.getPackaging(), body);
            writeBodyCell(row, 9, a.getPackageCount(), body);
            writeBodyCell(row, 10, a.getWeight(), body);
            writeBodyCell(row, 11, a.getWarehouseKeeper(), body);
            writeBodyCell(row, 12, a.getAcceptanceTime() != null ? a.getAcceptanceTime().format(dtf) : "", body);
            writeBodyCell(row, 13, a.getShelvingTime() != null ? a.getShelvingTime().format(dtf) : "", body);
            writeBodyCell(row, 14, a.getReceiptNumber(), body);
            writeBodyCell(row, 15, a.getArrivalTime() != null ? a.getArrivalTime().format(dtf) : "", body);
            writeBodyCell(row, 16, a.getStatus(), body);
            writeBodyCell(row, 17, a.getRemark(), body);
        }
        for (int i = 0; i < ARRIVAL_HEADERS.length; i++) {
            sheet.setColumnWidth(i, 16);
        }
        wb.write(out);
        wb.close();
    }

    // ============ 工具方法 ============

    private static String getCellString(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                }
                double d = cell.getNumericCellValue();
                if (d == Math.floor(d)) {
                    return String.valueOf((long) d);
                }
                return String.valueOf(d);
            case BLANK:
                return "";
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return cell.toString().trim();
        }
    }

    private static BigDecimal getCellBigDecimal(Cell cell) {
        if (cell == null) return BigDecimal.ZERO;
        try {
            if (cell.getCellType() == CellType.NUMERIC) {
                return BigDecimal.valueOf(cell.getNumericCellValue());
            }
            String s = getCellString(cell);
            if (s.isEmpty()) return BigDecimal.ZERO;
            return new BigDecimal(s);
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    private static Integer getCellInt(Cell cell) {
        if (cell == null) return 0;
        try {
            if (cell.getCellType() == CellType.NUMERIC) {
                return (int) cell.getNumericCellValue();
            }
            String s = getCellString(cell);
            if (s.isEmpty()) return 0;
            return (int) Double.parseDouble(s);
        } catch (Exception e) {
            return 0;
        }
    }

    private static LocalDateTime parseDateTime(String s) {
        if (s == null || s.trim().isEmpty()) return null;
        s = s.trim();
        String[] patterns = {
                "yyyy-MM-dd HH:mm:ss",
                "yyyy-MM-dd HH:mm",
                "yyyy-MM-dd",
                "yyyy/MM/dd HH:mm:ss",
                "yyyy/MM/dd HH:mm",
                "yyyy/MM/dd"
        };
        for (String p : patterns) {
            try {
                if (p.contains("HH")) {
                    return LocalDateTime.parse(s, DateTimeFormatter.ofPattern(p));
                } else {
                    return LocalDate.parse(s, DateTimeFormatter.ofPattern(p)).atStartOfDay();
                }
            } catch (DateTimeParseException e) {
                // try next
            }
        }
        return null;
    }
}
