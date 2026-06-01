# -*- coding: utf-8 -*-
"""到货登记 Tab"""
import tkinter as tk
import ttkbootstrap as ttkb
from ttkbootstrap.dialogs import Messagebox
from tkinter import filedialog

import db
import excel_io
from ui_common import (
    ScrollableTreeview, LabeledEntry, LabeledCombo,
    status_row_tag, show_busy, Card
)
from config import FONT_FAMILY


COLUMNS = ("id", "material_code", "name", "model", "unit", "supplier",
           "arrival_quantity", "package_count", "weight", "status", "arrival_time")
HEADINGS = ("ID", "编码", "名称", "型号", "单位", "供应商",
            "到货数量", "件数", "重量", "状态", "登记时间")
WIDTHS = (50, 110, 180, 110, 60, 130, 80, 60, 80, 70, 140)


class ArrivalsTab(ttkb.Frame):
    def __init__(self, master, status_callback=None):
        super().__init__(master, padding=12)
        self.status_callback = status_callback or (lambda msg: None)
        self._build()
        self._refresh_recent()
        self._refresh_combos()

    def _build(self):
        # 顶部登记表单
        form = Card(self, text="① 登记新到货", padding=14, bootstyle="primary")
        form.pack(fill="x")

        # 第一行：编码 + 选择按钮 + 名称
        row1 = ttkb.Frame(form)
        row1.pack(fill="x", pady=4)

        ttkb.Label(row1, text="物资编码*", width=10,
                   anchor="e").pack(side="left", padx=(0, 6))
        self.code_var = tk.StringVar()
        self.code_combo = ttkb.Combobox(
            row1, textvariable=self.code_var, width=22
        )
        self.code_combo.pack(side="left")
        self.code_combo.bind("<<ComboboxSelected>>", self._on_code_change)
        self.code_combo.bind("<FocusOut>", self._on_code_change)
        self.code_combo.bind("<Return>", self._on_code_change)
        ttkb.Button(row1, text="选择...", bootstyle="info-outline",
                    command=self._open_picker).pack(side="left", padx=6)

        self.name_le = LabeledEntry(row1, "名称", width=28,
                                    label_width=8, readonly=True)
        self.name_le.pack(side="left", padx=(12, 0), fill="x", expand=True)

        # 第二行：型号 + 单位 + 供应商
        row2 = ttkb.Frame(form)
        row2.pack(fill="x", pady=4)
        self.model_le = LabeledEntry(row2, "型号", width=20,
                                     label_width=8, readonly=True)
        self.model_le.pack(side="left")
        self.unit_le = LabeledEntry(row2, "单位", width=8,
                                    label_width=6, readonly=True)
        self.unit_le.pack(side="left", padx=(12, 0))
        self.supplier_co = LabeledCombo(row2, "供应商", width=22, label_width=8)
        self.supplier_co.pack(side="left", padx=(12, 0), fill="x", expand=True)

        # 第三行：数量 + 件数 + 重量 + 备注
        row3 = ttkb.Frame(form)
        row3.pack(fill="x", pady=4)
        self.qty_le = LabeledEntry(row3, "到货数量*", width=10, label_width=10)
        self.qty_le.pack(side="left")
        self.pack_le = LabeledEntry(row3, "件数", width=8, label_width=6)
        self.pack_le.pack(side="left", padx=(12, 0))
        self.weight_le = LabeledEntry(row3, "重量", width=10, label_width=6)
        self.weight_le.pack(side="left", padx=(12, 0))
        self.remark_le = LabeledEntry(row3, "备注", width=30, label_width=6)
        self.remark_le.pack(side="left", padx=(12, 0), fill="x", expand=True)

        # 按钮行
        row4 = ttkb.Frame(form)
        row4.pack(fill="x", pady=(10, 0))
        ttkb.Button(row4, text="✓ 保存登记", bootstyle="success",
                    command=self._save).pack(side="left")
        ttkb.Button(row4, text="清空", bootstyle="secondary-outline",
                    command=self._clear).pack(side="left", padx=10)
        ttkb.Separator(row4, orient="vertical").pack(side="left",
                                                     fill="y", padx=10)
        ttkb.Button(row4, text="Excel 批量导入到货", bootstyle="warning",
                    command=self._import).pack(side="left")
        ttkb.Button(row4, text="下载到货模板", bootstyle="warning-outline",
                    command=self._gen_template).pack(side="left", padx=6)

        # 最近登记
        bottom = Card(self, text="② 最近登记 (最新 50 条)", padding=8)
        bottom.pack(fill="both", expand=True, pady=(12, 0))

        bar = ttkb.Frame(bottom)
        bar.pack(fill="x", pady=(0, 6))
        ttkb.Button(bar, text="⟳ 刷新", bootstyle="info-outline",
                    command=self._refresh_recent).pack(side="left")
        ttkb.Button(bar, text="→ 转到「入库管理」处理选中",
                    bootstyle="primary-outline",
                    command=self._goto_warehousing).pack(side="left", padx=10)

        self.tree_widget = ScrollableTreeview(
            bottom, COLUMNS, HEADINGS, widths=WIDTHS, height=12
        )
        self.tree_widget.pack(fill="both", expand=True)

    # ---------- 数据 ----------
    def _refresh_combos(self):
        try:
            mats = db.list_materials("")
            codes = [m["material_code"] for m in mats]
            self.code_combo["values"] = codes
            suppliers = db.distinct_values("supplier")
            self.supplier_co.set_values(suppliers)
        except Exception as e:
            self.status_callback(f"下拉数据加载失败: {e}")

    def _refresh_recent(self):
        try:
            rows = db.list_arrivals({}, limit=50)
        except Exception as e:
            Messagebox.show_error(f"加载到货记录失败：\n{e}", "错误")
            return
        self.tree_widget.clear()
        self.tree_widget.insert_rows(
            rows,
            value_fn=lambda r: (
                r["id"], r["material_code"], r["name"], r["model"] or "",
                r["unit"] or "", r["supplier"] or "",
                r["arrival_quantity"], r["package_count"], r["weight"],
                r["status"],
                r["arrival_time"].strftime("%Y-%m-%d %H:%M:%S")
                if r.get("arrival_time") else "",
            ),
            tag_fn=lambda r: status_row_tag(r["status"]),
        )
        self.status_callback(f"最近 {len(rows)} 条到货记录")

    # ---------- 物资编码联动 ----------
    def _on_code_change(self, _event=None):
        code = self.code_var.get().strip()
        if not code:
            self.name_le.set("")
            self.model_le.set("")
            self.unit_le.set("")
            return
        try:
            mat = db.get_material_by_code(code)
        except Exception as e:
            self.status_callback(f"查询物资失败: {e}")
            return
        if mat:
            self.name_le.set(mat["name"])
            self.model_le.set(mat.get("model") or "")
            self.unit_le.set(mat.get("unit") or "")
        else:
            self.name_le.set("")
            self.model_le.set("")
            self.unit_le.set("")
            self.status_callback(f"未找到物资编码: {code}")

    def _open_picker(self):
        MaterialPicker(self, on_pick=self._apply_pick)

    def _apply_pick(self, mat):
        self.code_var.set(mat["material_code"])
        self._on_code_change()

    # ---------- 保存 ----------
    def _save(self):
        code = self.code_var.get().strip()
        if not code:
            Messagebox.show_warning("请填写「物资编码」", "提示")
            return
        if not self.name_le.get():
            r = Messagebox.yesno(
                "未匹配到该物资编码的基础信息，是否仍然继续登记?", "提示",
            )
            if r != "Yes":
                return
        try:
            qty = float(self.qty_le.get()) if self.qty_le.get() else 0
            pack = int(float(self.pack_le.get())) if self.pack_le.get() else 0
            weight = float(self.weight_le.get()) if self.weight_le.get() else 0
        except ValueError:
            Messagebox.show_error("数量/件数/重量 必须是数字", "错误")
            return
        if qty <= 0:
            Messagebox.show_warning("请填写「到货数量」", "提示")
            return

        data = {
            "material_code": code,
            "name": self.name_le.get(),
            "model": self.model_le.get(),
            "unit": self.unit_le.get(),
            "supplier": self.supplier_co.get(),
            "arrival_quantity": qty,
            "package_count": pack,
            "weight": weight,
            "remark": self.remark_le.get(),
        }
        try:
            db.add_arrival(data)
        except Exception as e:
            Messagebox.show_error(f"保存失败:\n{e}", "错误")
            return
        self._refresh_recent()
        self._refresh_combos()
        self._clear()
        self.status_callback("登记成功")

    def _clear(self):
        self.code_var.set("")
        self.name_le.set("")
        self.model_le.set("")
        self.unit_le.set("")
        self.supplier_co.set("")
        self.qty_le.set("")
        self.pack_le.set("")
        self.weight_le.set("")
        self.remark_le.set("")

    # ---------- Excel ----------
    def _import(self):
        path = filedialog.askopenfilename(
            title="选择到货登记 Excel",
            filetypes=[("Excel 文件", "*.xlsx *.xls")],
        )
        if not path:
            return
        try:
            show_busy(self, True)
            rows = excel_io.import_arrivals_from_excel(path)
            if not rows:
                Messagebox.show_info("Excel 中没有有效数据", "提示")
                return
            n = db.import_arrivals_batch(rows)
            Messagebox.show_info(f"成功导入 {n} 条到货记录", "导入完成")
            self._refresh_recent()
            self._refresh_combos()
        except Exception as e:
            Messagebox.show_error(str(e), "导入失败")
        finally:
            show_busy(self, False)

    def _gen_template(self):
        path = filedialog.asksaveasfilename(
            title="保存到货 Excel 模板",
            defaultextension=".xlsx",
            initialfile="到货登记模板.xlsx",
            filetypes=[("Excel 文件", "*.xlsx")],
        )
        if not path:
            return
        try:
            excel_io.gen_arrival_template(path)
            Messagebox.show_info(f"模板已生成:\n{path}", "成功")
        except Exception as e:
            Messagebox.show_error(str(e), "失败")

    def _goto_warehousing(self):
        self.event_generate("<<GotoWarehousing>>")


class MaterialPicker(ttkb.Toplevel):
    """搜索并选择物资编码"""

    def __init__(self, master, on_pick):
        super().__init__(master)
        self.on_pick = on_pick
        self.title("选择物资编码")
        self.geometry("820x540")
        self.transient(master)
        self.grab_set()

        body = ttkb.Frame(self, padding=14)
        body.pack(fill="both", expand=True)

        ttkb.Label(body, text="选择物资编码",
                   font=(FONT_FAMILY, 13, "bold")).pack(anchor="w", pady=(0, 8))

        bar = ttkb.Frame(body)
        bar.pack(fill="x", pady=(0, 8))
        ttkb.Label(bar, text="搜索:").pack(side="left")
        self.kw = tk.StringVar()
        e = ttkb.Entry(bar, textvariable=self.kw, width=30)
        e.pack(side="left", padx=4)
        e.bind("<Return>", lambda _e: self._load())
        ttkb.Button(bar, text="查询", bootstyle="primary",
                    command=self._load).pack(side="left")

        self.tree_widget = ScrollableTreeview(
            body,
            ("material_code", "name", "model", "unit", "remark"),
            ("物资编码", "名称", "型号", "单位", "备注"),
            widths=(140, 240, 140, 80, 200),
            height=18,
        )
        self.tree_widget.pack(fill="both", expand=True)
        self.tree_widget.tree.bind("<Double-1>", lambda _e: self._confirm())

        btns = ttkb.Frame(body)
        btns.pack(fill="x", pady=(10, 0))
        ttkb.Button(btns, text="确定", bootstyle="success",
                    width=10, command=self._confirm).pack(side="right")
        ttkb.Button(btns, text="取消", bootstyle="secondary-outline",
                    width=10, command=self.destroy).pack(side="right", padx=10)

        e.focus_set()
        self._load()

    def _load(self):
        try:
            rows = db.list_materials(self.kw.get().strip())
        except Exception as e:
            Messagebox.show_error(str(e), "错误", parent=self)
            return
        self.tree_widget.clear()
        for r in rows:
            self.tree_widget.tree.insert(
                "", "end", iid=r["material_code"],
                values=(r["material_code"], r["name"], r["model"] or "",
                        r["unit"] or "", r["remark"] or "")
            )

    def _confirm(self):
        iid = self.tree_widget.selected_iid()
        if not iid:
            Messagebox.show_info("请先选择一行", "提示", parent=self)
            return
        mat = db.get_material_by_code(iid)
        if not mat:
            Messagebox.show_error("未找到该物资", "错误", parent=self)
            return
        self.on_pick(mat)
        self.destroy()
