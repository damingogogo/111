# -*- coding: utf-8 -*-
"""进度导出 Tab"""
import tkinter as tk
import ttkbootstrap as ttkb
from ttkbootstrap.dialogs import Messagebox
from tkinter import filedialog

import db
import excel_io
from config import ARRIVAL_STATUS, FONT_FAMILY
from ui_common import ScrollableTreeview, status_row_tag, Card, StatBadge


COLUMNS = ("id", "material_code", "name", "supplier", "arrival_quantity",
           "warehouse_keeper", "acceptance_time", "shelving_time",
           "receipt_number", "status")
HEADINGS = ("ID", "编码", "名称", "供应商", "数量",
            "管库员", "验收时间", "上架时间", "入库单号", "状态")
WIDTHS = (50, 110, 180, 130, 70, 80, 140, 140, 110, 70)


STATUS_STYLE = {
    "待认领": "warning",
    "已认领": "info",
    "已验收": "primary",
    "已上架": "success",
    "已入库": "secondary",
}


class ExportTab(ttkb.Frame):
    def __init__(self, master, status_callback=None):
        super().__init__(master, padding=12)
        self.status_callback = status_callback or (lambda msg: None)
        self.preview_rows = []
        self.badges = {}
        self._build()
        self._preview()

    def _build(self):
        # 状态统计面板
        stats = Card(self, text="状态汇总", padding=10)
        stats.pack(fill="x")
        sf = ttkb.Frame(stats)
        sf.pack(fill="x")
        for s in ARRIVAL_STATUS:
            b = StatBadge(sf, s, bootstyle=STATUS_STYLE.get(s, "primary"))
            b.pack(side="left", padx=(0, 10), pady=2)
            self.badges[s] = b
        total = StatBadge(sf, "合计", bootstyle="dark")
        total.pack(side="left", padx=(0, 10), pady=2)
        self.badges["合计"] = total

        # 筛选卡片
        flt = Card(self, text="筛选条件", padding=12)
        flt.pack(fill="x", pady=(10, 0))

        r1 = ttkb.Frame(flt)
        r1.pack(fill="x", pady=4)
        ttkb.Label(r1, text="关键字:", width=8,
                   anchor="e").pack(side="left", padx=(0, 4))
        self.kw = tk.StringVar()
        ttkb.Entry(r1, textvariable=self.kw, width=18).pack(side="left", padx=4)

        ttkb.Label(r1, text="状态:", width=6,
                   anchor="e").pack(side="left", padx=(10, 4))
        self.status_var = tk.StringVar()
        ttkb.Combobox(r1, textvariable=self.status_var,
                      values=[""] + ARRIVAL_STATUS, width=10,
                      state="readonly").pack(side="left", padx=4)

        ttkb.Label(r1, text="供应商:", width=8,
                   anchor="e").pack(side="left", padx=(10, 4))
        self.supplier_var = tk.StringVar()
        self.supplier_cmb = ttkb.Combobox(r1, textvariable=self.supplier_var, width=16)
        self.supplier_cmb.pack(side="left", padx=4)

        ttkb.Label(r1, text="管库员:", width=8,
                   anchor="e").pack(side="left", padx=(10, 4))
        self.keeper_var = tk.StringVar()
        self.keeper_cmb = ttkb.Combobox(r1, textvariable=self.keeper_var, width=12)
        self.keeper_cmb.pack(side="left", padx=4)

        r2 = ttkb.Frame(flt)
        r2.pack(fill="x", pady=4)
        ttkb.Label(r2, text="登记日期:", width=8,
                   anchor="e").pack(side="left", padx=(0, 4))
        self.date_from = tk.StringVar()
        ttkb.Entry(r2, textvariable=self.date_from, width=14).pack(side="left", padx=2)
        ttkb.Label(r2, text="至").pack(side="left", padx=2)
        self.date_to = tk.StringVar()
        ttkb.Entry(r2, textvariable=self.date_to, width=14).pack(side="left", padx=2)
        ttkb.Label(r2, text="(YYYY-MM-DD)",
                   foreground="#999").pack(side="left", padx=(4, 0))

        ttkb.Button(r2, text="预览", bootstyle="primary",
                    command=self._preview).pack(side="left", padx=(16, 4))
        ttkb.Button(r2, text="重置", bootstyle="secondary-outline",
                    command=self._reset).pack(side="left")

        ttkb.Separator(r2, orient="vertical").pack(side="left", fill="y", padx=12)

        ttkb.Button(r2, text="导出到货/入库进度", bootstyle="success",
                    command=self._export_arrivals).pack(side="left")
        ttkb.Button(r2, text="导出物资基础数据", bootstyle="success-outline",
                    command=self._export_materials).pack(side="left", padx=10)
        ttkb.Button(r2, text="导出入库情况表", bootstyle="info",
                    command=self._export_inbound_status).pack(side="left")
        ttkb.Button(r2, text="导出订单引用表", bootstyle="info-outline",
                    command=self._export_reference_status).pack(side="left", padx=10)

        # 预览列表
        list_card = Card(self, text="预览", padding=8)
        list_card.pack(fill="both", expand=True, pady=(10, 0))
        self.tree_widget = ScrollableTreeview(
            list_card, COLUMNS, HEADINGS, widths=WIDTHS, height=18
        )
        self.tree_widget.pack(fill="both", expand=True)

    def _reset(self):
        self.kw.set("")
        self.status_var.set("")
        self.supplier_var.set("")
        self.keeper_var.set("")
        self.date_from.set("")
        self.date_to.set("")
        self._preview()

    def _filters(self):
        f = {
            "keyword": self.kw.get().strip(),
            "status": self.status_var.get().strip(),
            "supplier": self.supplier_var.get().strip(),
            "keeper": self.keeper_var.get().strip(),
        }
        df = self.date_from.get().strip()
        dt = self.date_to.get().strip()
        if df:
            f["date_from"] = df + " 00:00:00"
        if dt:
            f["date_to"] = dt + " 23:59:59"
        return f

    def _preview(self):
        try:
            rows = db.list_arrivals(self._filters(), limit=10000)
            self.supplier_cmb["values"] = [""] + db.distinct_values("supplier")
            self.keeper_cmb["values"] = [""] + db.distinct_values("warehouse_keeper")
        except Exception as e:
            Messagebox.show_error(str(e), "错误")
            return
        self.preview_rows = rows
        self.tree_widget.clear()
        self.tree_widget.insert_rows(
            rows,
            value_fn=lambda r: (
                r["id"], r["material_code"], r["name"], r["supplier"] or "",
                r["arrival_quantity"], r["warehouse_keeper"] or "",
                r["acceptance_time"].strftime("%Y-%m-%d %H:%M:%S")
                if r.get("acceptance_time") else "",
                r["shelving_time"].strftime("%Y-%m-%d %H:%M:%S")
                if r.get("shelving_time") else "",
                r["receipt_number"] or "",
                r["status"],
            ),
            tag_fn=lambda r: status_row_tag(r["status"]),
        )
        # 更新徽章
        by_status = {s: 0 for s in ARRIVAL_STATUS}
        for r in rows:
            by_status[r["status"]] = by_status.get(r["status"], 0) + 1
        for s, b in self.badges.items():
            if s == "合计":
                b.set_value(len(rows))
            else:
                b.set_value(by_status.get(s, 0))
        self.status_callback(f"预览 {len(rows)} 条")

    def _export_arrivals(self):
        if not self.preview_rows:
            r = Messagebox.yesno("当前预览没有数据，仍要导出空表吗?", "提示")
            if r != "Yes":
                return
        path = filedialog.asksaveasfilename(
            title="导出到货/入库进度",
            defaultextension=".xlsx",
            initialfile="到货入库进度.xlsx",
            filetypes=[("Excel 文件", "*.xlsx")],
        )
        if not path:
            return
        try:
            excel_io.export_arrivals(path, self.preview_rows)
            Messagebox.show_info(
                f"已导出 {len(self.preview_rows)} 条到:\n{path}", "成功"
            )
        except Exception as e:
            Messagebox.show_error(str(e), "失败")

    def _export_materials(self):
        path = filedialog.asksaveasfilename(
            title="导出物资基础数据",
            defaultextension=".xlsx",
            initialfile="物资基础数据.xlsx",
            filetypes=[("Excel 文件", "*.xlsx")],
        )
        if not path:
            return
        try:
            rows = db.list_materials("")
            excel_io.export_materials(path, rows)
            Messagebox.show_info(f"已导出 {len(rows)} 条到:\n{path}", "成功")
        except Exception as e:
            Messagebox.show_error(str(e), "失败")

    def _export_inbound_status(self):
        path = filedialog.asksaveasfilename(
            title="导出已到货物资入库情况表",
            defaultextension=".xlsx",
            initialfile="已到货物资入库情况表.xlsx",
            filetypes=[("Excel 文件", "*.xlsx")],
        )
        if not path:
            return
        try:
            rows = db.inbound_status_report()
            excel_io.export_inbound_status(path, rows)
            Messagebox.show_info(f"已导出 {len(rows)} 条到:\n{path}", "成功")
        except Exception as e:
            Messagebox.show_error(str(e), "失败")

    def _export_reference_status(self):
        path = filedialog.asksaveasfilename(
            title="导出采购订单引用情况表",
            defaultextension=".xlsx",
            initialfile="采购订单引用情况表.xlsx",
            filetypes=[("Excel 文件", "*.xlsx")],
        )
        if not path:
            return
        try:
            rows = db.purchase_order_reference_report()
            excel_io.export_purchase_order_reference(path, rows)
            Messagebox.show_info(f"已导出 {len(rows)} 条到:\n{path}", "成功")
        except Exception as e:
            Messagebox.show_error(str(e), "失败")
