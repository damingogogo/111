# -*- coding: utf-8 -*-
"""入库管理 Tab"""
import tkinter as tk
import ttkbootstrap as ttkb
from ttkbootstrap.dialogs import Messagebox, Querybox

import db
from config import ARRIVAL_STATUS
import config
from ui_common import (
    ScrollableTreeview, LabeledEntry, LabeledCombo,
    DateTimeEntry, status_row_tag, now_str, Card
)


COLUMNS = ("id", "material_code", "name", "model", "unit", "supplier",
           "arrival_quantity", "package_count", "weight",
           "warehouse_keeper", "acceptance_time", "shelving_time",
           "receipt_number", "status", "arrival_time")
HEADINGS = ("ID", "编码", "名称", "型号", "单位", "供应商",
            "数量", "件数", "重量",
            "管库员", "验收时间", "上架时间",
            "入库单号", "状态", "登记时间")
WIDTHS = (50, 110, 160, 100, 60, 120, 70, 50, 70,
          80, 140, 140, 110, 70, 140)


class WarehousingTab(ttkb.Frame):
    def __init__(self, master, status_callback=None):
        super().__init__(master, padding=12)
        self.status_callback = status_callback or (lambda msg: None)
        self.current_id = None
        self._default_keeper = ""
        self._build()
        self.refresh()

    def _build(self):
        # 筛选卡片
        flt = Card(self, text="筛选条件", padding=12)
        flt.pack(fill="x")

        r1 = ttkb.Frame(flt)
        r1.pack(fill="x", pady=4)
        ttkb.Label(r1, text="关键字:", width=8,
                   anchor="e").pack(side="left", padx=(0, 4))
        self.kw = tk.StringVar()
        e = ttkb.Entry(r1, textvariable=self.kw, width=18)
        e.pack(side="left", padx=4)
        e.bind("<Return>", lambda _e: self.refresh())

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

        ttkb.Button(r2, text="查询", bootstyle="primary",
                    command=self.refresh).pack(side="left", padx=(16, 4))
        ttkb.Button(r2, text="重置", bootstyle="secondary-outline",
                    command=self._reset).pack(side="left", padx=4)
        ttkb.Button(r2, text="⟳ 刷新", bootstyle="info-outline",
                    command=self.refresh).pack(side="left", padx=4)

        # 列表卡片
        list_card = Card(self, text="到货记录", padding=8)
        list_card.pack(fill="both", expand=True, pady=(10, 0))

        self.tree_widget = ScrollableTreeview(
            list_card, COLUMNS, HEADINGS, widths=WIDTHS, height=12
        )
        self.tree_widget.pack(fill="both", expand=True)
        self.tree_widget.tree.bind("<<TreeviewSelect>>", self._on_select)

        # 编辑卡片
        edit = Card(self, text="编辑选中记录", padding=12, bootstyle="primary")
        edit.pack(fill="x", pady=(10, 0))

        e1 = ttkb.Frame(edit)
        e1.pack(fill="x", pady=4)
        self.keeper_le = LabeledCombo(e1, "管库员", width=18, label_width=8)
        self.keeper_le.pack(side="left")
        ttkb.Button(e1, text="认领=自己", bootstyle="info",
                    command=self._self_claim).pack(side="left", padx=8)

        self.receipt_le = LabeledEntry(e1, "入库单号", width=20, label_width=8)
        self.receipt_le.pack(side="left", padx=(12, 0))

        self.status_le = LabeledCombo(e1, "状态", width=10, label_width=6)
        self.status_le.combo.config(values=ARRIVAL_STATUS, state="readonly")
        self.status_le.pack(side="left", padx=(12, 0))

        e2 = ttkb.Frame(edit)
        e2.pack(fill="x", pady=4)
        self.acc_dt = DateTimeEntry(e2, "验收完成时间", label_width=12)
        self.acc_dt.pack(side="left")
        self.shelf_dt = DateTimeEntry(e2, "上架时间", label_width=10)
        self.shelf_dt.pack(side="left", padx=(12, 0))

        e3 = ttkb.Frame(edit)
        e3.pack(fill="x", pady=(10, 0))
        ttkb.Button(e3, text="✓ 保存修改", bootstyle="success",
                    command=self._save).pack(side="left")
        ttkb.Button(e3, text="完成验收(写入当前时间)", bootstyle="info-outline",
                    command=self._mark_acceptance).pack(side="left", padx=8)
        ttkb.Button(e3, text="完成上架(写入当前时间)", bootstyle="info-outline",
                    command=self._mark_shelving).pack(side="left")
        ttkb.Button(e3, text="✕ 删除记录", bootstyle="danger-outline",
                    command=self._delete).pack(side="right")

    # ---------- 数据加载 ----------
    def _reset(self):
        self.kw.set("")
        self.status_var.set("")
        self.supplier_var.set("")
        self.keeper_var.set("")
        self.date_from.set("")
        self.date_to.set("")
        self.refresh()

    def refresh(self):
        filters = {
            "keyword": self.kw.get().strip(),
            "status": self.status_var.get().strip(),
            "supplier": self.supplier_var.get().strip(),
            "keeper": self.keeper_var.get().strip(),
        }
        df = self.date_from.get().strip()
        dt = self.date_to.get().strip()
        if df:
            filters["date_from"] = df + " 00:00:00"
        if dt:
            filters["date_to"] = dt + " 23:59:59"
        try:
            rows = db.list_arrivals(filters, limit=5000)
        except Exception as e:
            Messagebox.show_error(f"加载失败：\n{e}", "错误")
            return
        self.tree_widget.clear()
        self.tree_widget.insert_rows(
            rows,
            value_fn=lambda r: (
                r["id"], r["material_code"], r["name"], r["model"] or "",
                r["unit"] or "", r["supplier"] or "",
                r["arrival_quantity"], r["package_count"], r["weight"],
                r["warehouse_keeper"] or "",
                r["acceptance_time"].strftime("%Y-%m-%d %H:%M:%S")
                if r.get("acceptance_time") else "",
                r["shelving_time"].strftime("%Y-%m-%d %H:%M:%S")
                if r.get("shelving_time") else "",
                r["receipt_number"] or "",
                r["status"],
                r["arrival_time"].strftime("%Y-%m-%d %H:%M:%S")
                if r.get("arrival_time") else "",
            ),
            tag_fn=lambda r: status_row_tag(r["status"]),
        )

        try:
            self.supplier_cmb["values"] = [""] + db.distinct_values("supplier")
            keepers = db.distinct_values("warehouse_keeper")
            self.keeper_cmb["values"] = [""] + keepers
            self.keeper_le.set_values(keepers)
        except Exception:
            pass

        self.status_callback(f"入库管理 共 {len(rows)} 条")
        if not rows:
            self._clear_edit()
        else:
            first = self.tree_widget.tree.get_children()
            if first:
                self.tree_widget.tree.selection_set(first[0])
                self.tree_widget.tree.focus(first[0])

    # ---------- 选中编辑 ----------
    def _on_select(self, _e=None):
        iid = self.tree_widget.selected_iid()
        if not iid:
            return
        try:
            row = db.get_arrival(int(iid))
        except Exception as e:
            Messagebox.show_error(str(e), "错误")
            return
        if not row:
            return
        self.current_id = row["id"]
        self.keeper_le.set(row.get("warehouse_keeper") or "")
        self.receipt_le.set(row.get("receipt_number") or "")
        self.status_le.set(row.get("status") or "")
        self.acc_dt.set(row.get("acceptance_time"))
        self.shelf_dt.set(row.get("shelving_time"))

    def _clear_edit(self):
        self.current_id = None
        self.keeper_le.set("")
        self.receipt_le.set("")
        self.status_le.set("")
        self.acc_dt.set(None)
        self.shelf_dt.set(None)

    # ---------- 业务动作 ----------
    def _self_claim(self):
        user = config.CURRENT_USER
        if user and user.get("role") == "KEEPER":
            # 管库员自动使用自己的姓名
            name = user.get("real_name") or user.get("username", "")
            self.keeper_le.set(name)
            self.status_callback(f"已自动填入管库员: {name}")
        else:
            # 管理员可以手动输入任意管库员
            name = self._default_keeper
            if not name:
                name = Querybox.get_string(
                    "请输入管库员姓名:",
                    "认领", parent=self,
                )
                if not name:
                    return
                self._default_keeper = name.strip()
            self.keeper_le.set(self._default_keeper)

    def _save(self):
        if not self.current_id:
            Messagebox.show_info("请先选中一条记录", "提示")
            return
        data = {
            "warehouse_keeper": self.keeper_le.get(),
            "acceptance_time": self.acc_dt.get(),
            "shelving_time": self.shelf_dt.get(),
            "receipt_number": self.receipt_le.get(),
        }
        st = self.status_le.get()
        if st:
            data["status"] = st
        try:
            db.update_arrival(self.current_id, data)
        except Exception as e:
            Messagebox.show_error(str(e), "错误")
            return
        self.refresh()

    def _mark_acceptance(self):
        if not self.current_id:
            Messagebox.show_info("请先选中一条记录", "提示")
            return
        self.acc_dt.set(now_str())
        self._save()

    def _mark_shelving(self):
        if not self.current_id:
            Messagebox.show_info("请先选中一条记录", "提示")
            return
        self.shelf_dt.set(now_str())
        self._save()

    def _delete(self):
        ids = [int(x) for x in self.tree_widget.tree.selection()]
        if not ids:
            Messagebox.show_info("请先选中一行或多行", "提示")
            return
        r = Messagebox.yesno(f"确定删除 {len(ids)} 条记录?", "确认")
        if r != "Yes":
            return
        try:
            for aid in ids:
                db.delete_arrival(aid)
        except Exception as e:
            Messagebox.show_error(str(e), "错误")
            return
        self.refresh()

    def select_by_id(self, aid):
        """供其它 Tab 跳转过来时定位"""
        self.refresh()
        try:
            self.tree_widget.tree.selection_set(str(aid))
            self.tree_widget.tree.focus(str(aid))
            self.tree_widget.tree.see(str(aid))
        except tk.TclError:
            pass
