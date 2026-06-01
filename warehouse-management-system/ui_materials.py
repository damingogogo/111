# -*- coding: utf-8 -*-
"""物资基础信息 Tab"""
import tkinter as tk
import ttkbootstrap as ttkb
from ttkbootstrap.dialogs import Messagebox
from tkinter import filedialog

import db
import excel_io
from ui_common import (
    ScrollableTreeview, LabeledEntry, Card, show_busy
)
from config import FONT_FAMILY


COLUMNS = ("id", "material_code", "name", "model", "unit", "remark", "updated_at")
HEADINGS = ("ID", "物资编码", "名称", "型号", "单位", "备注", "更新时间")
WIDTHS = (50, 130, 220, 140, 70, 200, 150)


class MaterialsTab(ttkb.Frame):
    def __init__(self, master, status_callback=None):
        super().__init__(master, padding=12)
        self.status_callback = status_callback or (lambda msg: None)
        self._build()
        self.refresh()

    def _build(self):
        # 顶部工具栏卡片
        tools = Card(self, text="操作", padding=10)
        tools.pack(fill="x")

        row = ttkb.Frame(tools)
        row.pack(fill="x")

        ttkb.Label(row, text="搜索:").pack(side="left")
        self.search_var = tk.StringVar()
        e = ttkb.Entry(row, textvariable=self.search_var, width=24)
        e.pack(side="left", padx=(4, 4))
        e.bind("<Return>", lambda _e: self.refresh())
        ttkb.Button(row, text="查询", bootstyle="primary",
                    command=self.refresh).pack(side="left")
        ttkb.Button(row, text="清空", bootstyle="secondary-outline",
                    command=self._clear_search).pack(side="left", padx=4)

        ttkb.Separator(row, orient="vertical").pack(side="left", fill="y", padx=10)

        ttkb.Button(row, text="＋ 新增", bootstyle="success",
                    command=self._add).pack(side="left")
        ttkb.Button(row, text="编辑", bootstyle="info",
                    command=self._edit).pack(side="left", padx=4)
        ttkb.Button(row, text="删除", bootstyle="danger-outline",
                    command=self._delete).pack(side="left")

        ttkb.Separator(row, orient="vertical").pack(side="left", fill="y", padx=10)

        ttkb.Button(row, text="Excel 导入", bootstyle="warning",
                    command=self._import).pack(side="left")
        ttkb.Button(row, text="采购订单导入", bootstyle="warning",
                    command=self._import_purchase_orders).pack(side="left", padx=(4, 0))
        ttkb.Button(row, text="下载模板", bootstyle="warning-outline",
                    command=self._gen_template).pack(side="left", padx=4)
        ttkb.Button(row, text="全部导出", bootstyle="primary-outline",
                    command=self._export_all).pack(side="left")

        # 列表卡片
        list_card = Card(self, text="物资列表", padding=8)
        list_card.pack(fill="both", expand=True, pady=(10, 0))

        self.tree_widget = ScrollableTreeview(
            list_card, COLUMNS, HEADINGS, widths=WIDTHS, height=22
        )
        self.tree_widget.pack(fill="both", expand=True)
        self.tree_widget.tree.bind("<Double-1>", lambda _e: self._edit())

    def _clear_search(self):
        self.search_var.set("")
        self.refresh()

    def refresh(self):
        kw = self.search_var.get().strip()
        try:
            rows = db.list_materials(kw)
        except Exception as e:
            Messagebox.show_error(f"加载物资失败：\n{e}", "错误")
            return
        self.tree_widget.clear()
        self.tree_widget.insert_rows(
            rows,
            value_fn=lambda r: (
                r["id"], r["material_code"], r["name"], r["model"] or "",
                r["unit"] or "", r["remark"] or "",
                r["updated_at"].strftime("%Y-%m-%d %H:%M:%S") if r.get("updated_at") else "",
            ),
        )
        self.status_callback(f"物资基础数据 共 {len(rows)} 条")

    def _selected_row(self):
        iid = self.tree_widget.selected_iid()
        if not iid:
            return None
        return db.get_material_by_code(
            self.tree_widget.tree.item(iid, "values")[1]
        )

    def _add(self):
        MaterialEditor(self, None, on_saved=self.refresh)

    def _edit(self):
        row = self._selected_row()
        if not row:
            Messagebox.show_info("请先选中一行", "提示")
            return
        MaterialEditor(self, row, on_saved=self.refresh)

    def _delete(self):
        ids = self.tree_widget.tree.selection()
        if not ids:
            Messagebox.show_info("请先选中一行或多行", "提示")
            return
        r = Messagebox.yesno(f"确认删除选中的 {len(ids)} 条物资?", "确认")
        if r != "Yes":
            return
        try:
            for iid in ids:
                db.delete_material(int(iid))
        except Exception as e:
            Messagebox.show_error(str(e), "错误")
            return
        self.refresh()

    def _import(self):
        path = filedialog.askopenfilename(
            title="选择物资基础数据 Excel",
            filetypes=[("Excel 文件", "*.xlsx *.xls"), ("所有文件", "*.*")],
        )
        if not path:
            return
        try:
            show_busy(self, True)
            rows = excel_io.import_materials_from_excel(path)
            if not rows:
                Messagebox.show_info("Excel 中没有有效数据", "提示")
                return
            ins, upd = db.upsert_materials_batch(rows)
            Messagebox.show_info(
                f"新增 {ins} 条，更新 {upd} 条，共处理 {ins + upd} 条",
                "导入完成",
            )
            self.refresh()
        except Exception as e:
            Messagebox.show_error(str(e), "导入失败")
        finally:
            show_busy(self, False)

    def _gen_template(self):
        path = filedialog.asksaveasfilename(
            title="保存物资基础数据模板",
            defaultextension=".xlsx",
            initialfile="物资基础数据模板.xlsx",
            filetypes=[("Excel 文件", "*.xlsx")],
        )
        if not path:
            return
        try:
            excel_io.gen_material_template(path)
            Messagebox.show_info(f"模板已生成:\n{path}", "成功")
        except Exception as e:
            Messagebox.show_error(str(e), "失败")

    def _import_purchase_orders(self):
        path = filedialog.askopenfilename(
            title="选择采购订单综合查询信息 Excel",
            filetypes=[("Excel 文件", "*.xlsx *.xls"), ("所有文件", "*.*")],
        )
        if not path:
            return
        try:
            show_busy(self, True)
            rows = excel_io.import_purchase_orders_from_excel(path)
            if not rows:
                Messagebox.show_info("Excel 中没有有效采购订单数据", "提示")
                return
            ins, upd = db.upsert_purchase_orders_batch(rows)
            Messagebox.show_info(
                f"采购订单导入完成：新增 {ins} 条，更新 {upd} 条；物资档案已同步。",
                "导入完成",
            )
            self.refresh()
        except Exception as e:
            Messagebox.show_error(str(e), "采购订单导入失败")
        finally:
            show_busy(self, False)

    def _export_all(self):
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


class MaterialEditor(ttkb.Toplevel):
    def __init__(self, master, row, on_saved):
        super().__init__(master)
        self.on_saved = on_saved
        self.row = row
        self.title("编辑物资" if row else "新增物资")
        self.transient(master)
        self.resizable(False, False)
        self.grab_set()

        frm = ttkb.Frame(self, padding=20)
        frm.pack(fill="both", expand=True)

        header = ttkb.Label(
            frm, text=("编辑物资基础信息" if row else "新增物资基础信息"),
            font=(FONT_FAMILY, 13, "bold"),
        )
        header.pack(anchor="w", pady=(0, 12))

        self.code = LabeledEntry(frm, "物资编码*", width=32, label_width=10)
        self.name = LabeledEntry(frm, "名称*", width=32, label_width=10)
        self.model = LabeledEntry(frm, "型号", width=32, label_width=10)
        self.unit = LabeledEntry(frm, "单位", width=32, label_width=10)
        self.remark = LabeledEntry(frm, "备注", width=32, label_width=10)
        for w in (self.code, self.name, self.model, self.unit, self.remark):
            w.pack(fill="x", pady=5)

        if row:
            self.code.set(row["material_code"])
            self.name.set(row["name"])
            self.model.set(row.get("model") or "")
            self.unit.set(row.get("unit") or "")
            self.remark.set(row.get("remark") or "")

        bar = ttkb.Frame(frm)
        bar.pack(fill="x", pady=(16, 0))
        ttkb.Button(bar, text="保存", bootstyle="success",
                    width=10, command=self._save).pack(side="right")
        ttkb.Button(bar, text="取消", bootstyle="secondary-outline",
                    width=10, command=self.destroy).pack(side="right", padx=10)

        self.update_idletasks()
        # 居中
        w, h = self.winfo_width(), self.winfo_height()
        sw = self.winfo_screenwidth()
        sh = self.winfo_screenheight()
        self.geometry(f"+{(sw - w) // 2}+{(sh - h) // 2}")

    def _save(self):
        code = self.code.get()
        name = self.name.get()
        if not code or not name:
            Messagebox.show_warning("「物资编码」和「名称」必填", "提示", parent=self)
            return
        try:
            if self.row:
                db.update_material(
                    self.row["id"], code, name,
                    self.model.get(), self.unit.get(), self.remark.get(),
                )
            else:
                db.add_material(code, name, self.model.get(),
                                self.unit.get(), self.remark.get())
        except Exception as e:
            Messagebox.show_error(str(e), "错误", parent=self)
            return
        self.on_saved()
        self.destroy()
