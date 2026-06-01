# -*- coding: utf-8 -*-
"""通用 GUI 工具 —— 基于 ttkbootstrap"""
import tkinter as tk
import ttkbootstrap as ttkb
from ttkbootstrap.constants import *  # noqa
from datetime import datetime

from config import FONT_FAMILY, STATUS_ROW_COLOR


def now_str():
    return datetime.now().strftime("%Y-%m-%d %H:%M:%S")


# ============ Treeview 包装 ============

class ScrollableTreeview(ttkb.Frame):
    """带纵横滚动条 + 行底色 + 状态色块的 Treeview"""

    def __init__(self, master, columns, headings, widths=None,
                 bootstyle="primary", height=14, **kwargs):
        super().__init__(master)
        self.columns = columns
        self.tree = ttkb.Treeview(
            self, columns=columns, show="headings",
            selectmode="extended", bootstyle=bootstyle, height=height, **kwargs
        )
        for c, h in zip(columns, headings):
            anchor = "w"
            self.tree.heading(c, text=h, anchor=anchor)
        if widths:
            for c, w in zip(columns, widths):
                self.tree.column(c, width=w, anchor="w", stretch=False)

        vsb = ttkb.Scrollbar(self, orient="vertical",
                             command=self.tree.yview, bootstyle="round")
        hsb = ttkb.Scrollbar(self, orient="horizontal",
                             command=self.tree.xview, bootstyle="round")
        self.tree.configure(yscrollcommand=vsb.set, xscrollcommand=hsb.set)

        self.tree.grid(row=0, column=0, sticky="nsew")
        vsb.grid(row=0, column=1, sticky="ns")
        hsb.grid(row=1, column=0, sticky="ew")
        self.grid_rowconfigure(0, weight=1)
        self.grid_columnconfigure(0, weight=1)

        # 行底色配置
        self.tree.tag_configure("oddrow", background="#FAFBFC")
        self.tree.tag_configure("evenrow", background="#FFFFFF")
        for status, color in STATUS_ROW_COLOR.items():
            self.tree.tag_configure(f"st_{status}", background=color)

    def clear(self):
        for i in self.tree.get_children():
            self.tree.delete(i)

    def insert_rows(self, rows, value_fn, tag_fn=None):
        for idx, row in enumerate(rows):
            base = "evenrow" if idx % 2 == 0 else "oddrow"
            tag = (tag_fn(row),) if tag_fn else ()
            tags = (base,) + tag
            self.tree.insert("", "end", iid=str(row.get("id", idx)),
                             values=value_fn(row), tags=tags)

    def selected_iid(self):
        sel = self.tree.selection()
        return sel[0] if sel else None


def status_row_tag(status):
    return f"st_{status}" if status in STATUS_ROW_COLOR else ""


# ============ Card / 卡片容器 ============

class Card(ttkb.Labelframe):
    """卡片容器,内部有自动 padding"""

    def __init__(self, master, text="", padding=12, bootstyle="default", **kw):
        super().__init__(master, text=text, padding=padding,
                         bootstyle=bootstyle, **kw)


# ============ 输入组合 ============

class LabeledEntry(ttkb.Frame):
    def __init__(self, master, label, width=20, label_width=10,
                 readonly=False, **kw):
        super().__init__(master)
        self.var = tk.StringVar()
        ttkb.Label(self, text=label, width=label_width,
                   anchor="e").pack(side="left", padx=(0, 6))
        self.entry = ttkb.Entry(self, textvariable=self.var, width=width, **kw)
        if readonly:
            self.entry.configure(state="readonly")
        self.entry.pack(side="left", fill="x", expand=True)

    def get(self):
        return self.var.get().strip()

    def set(self, value):
        if self.entry["state"] == "readonly":
            self.entry.configure(state="normal")
            self.var.set("" if value is None else str(value))
            self.entry.configure(state="readonly")
        else:
            self.var.set("" if value is None else str(value))


class LabeledCombo(ttkb.Frame):
    def __init__(self, master, label, values=None, width=18,
                 label_width=10, **kw):
        super().__init__(master)
        self.var = tk.StringVar()
        ttkb.Label(self, text=label, width=label_width,
                   anchor="e").pack(side="left", padx=(0, 6))
        self.combo = ttkb.Combobox(self, textvariable=self.var,
                                   values=values or [], width=width, **kw)
        self.combo.pack(side="left", fill="x", expand=True)

    def get(self):
        return self.var.get().strip()

    def set(self, value):
        self.var.set("" if value is None else str(value))

    def set_values(self, values):
        self.combo["values"] = values


class DateTimeEntry(ttkb.Frame):
    def __init__(self, master, label, width=22, label_width=12):
        super().__init__(master)
        self.var = tk.StringVar()
        ttkb.Label(self, text=label, width=label_width,
                   anchor="e").pack(side="left", padx=(0, 6))
        self.entry = ttkb.Entry(self, textvariable=self.var, width=width)
        self.entry.pack(side="left", fill="x", expand=True)
        ttkb.Button(self, text="现在", width=5, bootstyle="info-outline",
                    command=self._fill_now).pack(side="left", padx=(4, 2))
        ttkb.Button(self, text="清空", width=5, bootstyle="secondary-outline",
                    command=lambda: self.var.set("")).pack(side="left")

    def _fill_now(self):
        self.var.set(now_str())

    def get(self):
        v = self.var.get().strip()
        return v or None

    def set(self, value):
        if value is None:
            self.var.set("")
        elif hasattr(value, "strftime"):
            self.var.set(value.strftime("%Y-%m-%d %H:%M:%S"))
        else:
            self.var.set(str(value))


# ============ 头部统计徽章 ============

class StatBadge(ttkb.Frame):
    """卡片式统计徽章: 标题 + 大字数值"""

    def __init__(self, master, title, bootstyle="primary"):
        super().__init__(master, padding=(14, 8))
        self.configure(borderwidth=1, relief="solid")
        self.bootstyle = bootstyle
        self.title = title

        self.title_lbl = ttkb.Label(
            self, text=title, font=(FONT_FAMILY, 9),
            foreground="#666",
        )
        self.title_lbl.pack(anchor="w")

        self.value_var = tk.StringVar(value="0")
        color = self._color_for(bootstyle)
        self.value_lbl = ttkb.Label(
            self, textvariable=self.value_var,
            font=(FONT_FAMILY, 18, "bold"),
            foreground=color,
        )
        self.value_lbl.pack(anchor="w")

    @staticmethod
    def _color_for(bootstyle):
        return {
            "primary": "#2C7BE5",
            "success": "#28A745",
            "info":    "#17A2B8",
            "warning": "#FD7E14",
            "danger":  "#DC3545",
            "secondary": "#6C757D",
            "dark":    "#343A40",
        }.get(bootstyle, "#212529")

    def set_value(self, val):
        self.value_var.set(str(val))


def show_busy(widget, on=True):
    try:
        widget.config(cursor="watch" if on else "")
        widget.update_idletasks()
    except Exception:
        pass
