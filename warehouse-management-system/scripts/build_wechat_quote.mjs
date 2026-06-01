import fs from "node:fs/promises";
import path from "node:path";
import { SpreadsheetFile, Workbook } from "@oai/artifact-tool";

const rootDir = process.cwd();
const outputDir = path.join(rootDir, "outputs", "wechat_miniprogram_quote");
const outputFileName = process.env.QUOTE_OUTPUT_NAME || "出入库系统微信小程序功能报价单.xlsx";
const outputPath = path.join(outputDir, outputFileName);

await fs.mkdir(outputDir, { recursive: true });

const workbook = Workbook.create();
const quote = workbook.worksheets.add("软件报价单");
const notes = workbook.worksheets.add("费用说明");

const quoteDate = "2026-05-21";
const items = [
  [1, "年度费用", "域名/服务器/SSL证书年度服务包", "包含域名注册或续费、云服务器基础资源、SSL证书申请与部署", "年", 1, 800, null, "800元/年"],
  [2, "年度费用", "微信小程序认证费", "微信小程序主体认证/年审费用", "年", 1, 300, null, "300元/年"],
  [3, "一次性费用", "微信小程序开发服务费", "包含来源、运单号、数量、包装、件数录入，以及语音输入数字和大模型识别接口对接", "项", 1, 500, null, "一次性"],
  [4, "客户自费", "大模型识别接口调用费", "语音识别/数字识别涉及的大模型调用费用由客户自行开通并按量支付", "按量", 0, 0, null, "客户自费，不计入合计"],
];

quote.showGridLines = false;
notes.showGridLines = false;

quote.getRange("A1:I1").merge();
quote.getRange("A1").values = [["出入库系统微信小程序功能报价单"]];
quote.getRange("A1").format = {
  fill: "#1F4E78",
  font: { bold: true, color: "#FFFFFF", size: 18 },
  horizontalAlignment: "center",
  verticalAlignment: "center",
};
quote.getRange("A1:I1").format.rowHeightPx = 42;

quote.getRange("A3:I5").values = [
  ["客户名称", "________________", "", "", "", "报价日期", quoteDate, "", ""],
  ["项目名称", "出入库系统微信小程序功能", "", "", "", "报价有效期", "30天", "", ""],
  ["报价编号", "WXMINI-20260521-001", "", "", "", "币种", "人民币（元）", "", ""],
];
quote.getRange("A3:A5").format = { font: { bold: true, color: "#17365D" }, fill: "#D9EAF7" };
quote.getRange("F3:F5").format = { font: { bold: true, color: "#17365D" }, fill: "#D9EAF7" };
quote.getRange("B3:D5").merge(true);
quote.getRange("G3:I5").merge(true);
quote.getRange("A3:I5").format = {
  borders: {
    all: { style: "continuous", color: "#B7C9D6", weight: "thin" },
  },
  verticalAlignment: "center",
};

quote.getRange("A7:I7").values = [[
  "序号", "费用类型", "项目名称", "服务内容", "计费周期", "数量", "单价", "小计", "备注",
]];
quote.getRange("A8:I11").values = items;
quote.getRange("H8").formulas = [["=F8*G8"]];
quote.getRange("H8:H11").fillDown();

quote.getRange("A7:I7").format = {
  fill: "#5B9BD5",
  font: { bold: true, color: "#FFFFFF" },
  horizontalAlignment: "center",
  verticalAlignment: "center",
};
quote.getRange("A8:I11").format = {
  fill: "#FFFFFF",
  borders: { all: { style: "continuous", color: "#D9E2F3", weight: "thin" } },
  verticalAlignment: "center",
  wrapText: true,
};
quote.getRange("A8:B11").format.horizontalAlignment = "center";
quote.getRange("E8:F11").format.horizontalAlignment = "center";
quote.getRange("G8:H11").format.numberFormat = "¥#,##0";

const table = quote.tables.add("A7:I11", true, "QuoteItems");
table.style = "TableStyleMedium2";
table.showFilterButton = false;

quote.getRange("G12:H15").values = [
  ["年度费用小计", null],
  ["一次性费用小计", null],
  ["首年应付合计", null],
  ["次年起年度续费参考", null],
];
quote.getRange("H12").formulas = [["=SUMIF($B$8:$B$11,\"年度费用\",$H$8:$H$11)"]];
quote.getRange("H13").formulas = [["=SUMIF($B$8:$B$11,\"一次性费用\",$H$8:$H$11)"]];
quote.getRange("H14").formulas = [["=H12+H13"]];
quote.getRange("H15").formulas = [["=H12"]];
quote.getRange("G12:H15").format = {
  borders: { all: { style: "continuous", color: "#B7C9D6", weight: "thin" } },
  verticalAlignment: "center",
};
quote.getRange("G12:G15").format = { fill: "#EAF3F8", font: { bold: true, color: "#17365D" } };
quote.getRange("H12:H15").format = { numberFormat: "¥#,##0", horizontalAlignment: "right" };
quote.getRange("G14:H14").format = {
  fill: "#FFF2CC",
  font: { bold: true, color: "#7F6000" },
  borders: { all: { style: "continuous", color: "#D6B656", weight: "thin" } },
};

quote.getRange("K3:M3").merge();
quote.getRange("K3").values = [["费用汇总"]];
quote.getRange("K4:M7").values = [
  ["首年合计", null, "年费 + 开发服务费"],
  ["年度续费合计", null, "域名/服务器/证书 + 认证"],
  ["一次性开发服务费", null, "仅首年收取"],
  ["后续年度参考", null, "按实际续费政策执行"],
];
quote.getRange("L4").formulas = [["=H14"]];
quote.getRange("L5").formulas = [["=H12"]];
quote.getRange("L6").formulas = [["=H13"]];
quote.getRange("L7").formulas = [["=H15"]];
quote.getRange("K3:M3").format = {
  fill: "#17365D",
  font: { bold: true, color: "#FFFFFF" },
  horizontalAlignment: "center",
};
quote.getRange("K4:M7").format = {
  fill: "#F7FBFD",
  borders: { all: { style: "continuous", color: "#B7C9D6", weight: "thin" } },
};
quote.getRange("K4:K7").format = { font: { bold: true, color: "#17365D" } };
quote.getRange("L4:L7").format = { numberFormat: "¥#,##0", font: { bold: true }, horizontalAlignment: "right" };
quote.getRange("K4:M4").format = { fill: "#E2F0D9" };

quote.getRange("A17:I23").values = [
  ["报价说明", "", "", "", "", "", "", "", ""],
  ["1. 域名、服务器、SSL证书按 800 元/年计；微信小程序认证费按 300 元/年计。", "", "", "", "", "", "", "", ""],
  ["2. 开发服务费 500 元包含来源、运单号、数量、包装、件数录入、语音输入数字及大模型识别接口对接。", "", "", "", "", "", "", "", ""],
  ["3. 大模型识别接口调用费用由客户自费，按实际平台用量或账单结算，不计入本报价合计。", "", "", "", "", "", "", "", ""],
  ["4. 首年合计 = 年度费用小计 + 一次性费用小计；次年起参考年度续费合计。", "", "", "", "", "", "", "", ""],
  ["5. 本报价默认未含税；如需开票或额外第三方服务，费用另行确认。", "", "", "", "", "", "", "", ""],
  ["确认签字：____________________        日期：____________________", "", "", "", "", "", "", "", ""],
];
quote.getRange("A17:I17").merge();
quote.getRange("A18:I23").merge(true);
quote.getRange("A17").format = {
  fill: "#1F4E78",
  font: { bold: true, color: "#FFFFFF" },
};
quote.getRange("A18:I23").format = {
  fill: "#F8FBFD",
  wrapText: true,
  verticalAlignment: "top",
  borders: { all: { style: "continuous", color: "#D9E2F3", weight: "thin" } },
};

notes.getRange("A1:D1").merge();
notes.getRange("A1").values = [["费用说明与假设"]];
notes.getRange("A1").format = {
  fill: "#1F4E78",
  font: { bold: true, color: "#FFFFFF", size: 16 },
  horizontalAlignment: "center",
};
notes.getRange("A3:D3").values = [["费用项", "金额", "周期", "说明"]];
notes.getRange("A4:D6").values = [
  ["域名/服务器/SSL证书", 800, "年", "域名服务器证书合并计价，按年续费"],
  ["微信小程序认证费", 300, "年", "微信平台认证/年审相关费用"],
  ["开发服务费", 500, "一次性", "含来源、运单号、数量、包装、件数、语音输入数字及大模型识别接口对接"],
];
notes.getRange("A8:D11").values = [
  ["交付内容", "微信小程序出入库功能报价清单、来源/运单号/数量/包装/件数录入、语音输入数字、大模型识别接口对接、部署协助", "", ""],
  ["不包含内容", "复杂定制、硬件设备、短信/地图/支付等第三方接口费用；大模型调用费用由客户自费", "", ""],
  ["付款建议", "首年费用建议一次性支付；年度费用到期前续费", "", ""],
  ["备注", "费用可按实际采购发票或平台政策变化调整", "", ""],
];
notes.getRange("A3:D3").format = {
  fill: "#5B9BD5",
  font: { bold: true, color: "#FFFFFF" },
  horizontalAlignment: "center",
};
notes.getRange("A4:D6").format = {
  borders: { all: { style: "continuous", color: "#D9E2F3", weight: "thin" } },
  verticalAlignment: "center",
  wrapText: true,
};
notes.getRange("B4:B6").format = { numberFormat: "¥#,##0", horizontalAlignment: "right" };
notes.getRange("A8:A11").format = { fill: "#EAF3F8", font: { bold: true, color: "#17365D" } };
notes.getRange("A8:D11").format = {
  borders: { all: { style: "continuous", color: "#D9E2F3", weight: "thin" } },
  wrapText: true,
  verticalAlignment: "top",
};

const widths = {
  A: 58, B: 100, C: 210, D: 310, E: 82, F: 62, G: 132, H: 110, I: 110,
  J: 24, K: 118, L: 98, M: 210,
};
for (const [col, width] of Object.entries(widths)) {
  quote.getRange(`${col}1:${col}40`).format.columnWidthPx = width;
}
quote.getRange("A7:I11").format.rowHeightPx = 42;
quote.getRange("A17:I23").format.rowHeightPx = 26;
quote.freezePanes.freezeRows(7);

const noteWidths = { A: 150, B: 320, C: 90, D: 460 };
for (const [col, width] of Object.entries(noteWidths)) {
  notes.getRange(`${col}1:${col}30`).format.columnWidthPx = width;
}
notes.getRange("A3:D11").format.rowHeightPx = 32;
notes.freezePanes.freezeRows(3);

const inspectQuote = await workbook.inspect({
  kind: "table",
  range: "软件报价单!A1:M23",
  include: "values,formulas",
  tableMaxRows: 24,
  tableMaxCols: 13,
});
console.log(inspectQuote.ndjson);

const errors = await workbook.inspect({
  kind: "match",
  searchTerm: "#REF!|#DIV/0!|#VALUE!|#NAME\\?|#N/A",
  options: { useRegex: true, maxResults: 50 },
  summary: "formula error scan",
});
console.log(errors.ndjson);

for (const sheetName of ["软件报价单", "费用说明"]) {
  const preview = await workbook.render({ sheetName, autoCrop: "all", scale: 1, format: "png" });
  await fs.writeFile(path.join(outputDir, `${sheetName}.png`), new Uint8Array(await preview.arrayBuffer()));
}

const xlsx = await SpreadsheetFile.exportXlsx(workbook);
await xlsx.save(outputPath);
console.log(JSON.stringify({ outputPath }));
