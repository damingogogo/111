# -*- coding: utf-8 -*-
"""用户管理窗口 —— 管理员专用"""
import tkinter as tk
import ttkbootstrap as ttkb
from ttkbootstrap.dialogs import Messagebox
import hashlib
import db
from config import FONT_FAMILY


class UserMgmtWindow(ttkb.Toplevel):
    def __init__(self, master):
        super().__init__(master)
        self.title("用户管理")
        self.geometry("700x500")
        self.transient(master)
        self.grab_set()

        header = ttkb.Frame(self, padding=14)
        header.pack(fill="x")
        ttkb.Label(header, text="用户管理",
                   font=(FONT_FAMILY, 14, "bold")).pack(side="left")
        ttkb.Button(header, text="+ 新增用户", bootstyle="primary",
                    command=self._open_add).pack(side="right")

        cols = ("id", "username", "real_name", "role", "status", "created_at")
        self.tree = ttkb.Treeview(self, columns=cols, show="headings",
                                   selectmode="browse", height=16)
        self.tree.heading("id", text="ID")
        self.tree.heading("username", text="用户名")
        self.tree.heading("real_name", text="姓名")
        self.tree.heading("role", text="角色")
        self.tree.heading("status", text="状态")
        self.tree.heading("created_at", text="创建时间")
        self.tree.column("id", width=50, anchor="center")
        self.tree.column("username", width=120)
        self.tree.column("real_name", width=120)
        self.tree.column("role", width=100, anchor="center")
        self.tree.column("status", width=80, anchor="center")
        self.tree.column("created_at", width=160)

        vsb = ttkb.Scrollbar(self, orient="vertical", command=self.tree.yview)
        self.tree.configure(yscrollcommand=vsb.set)
        self.tree.pack(side="left", fill="both", expand=True, padx=(10, 0), pady=5)
        vsb.pack(side="left", fill="y", pady=5)

        btn_frame = ttkb.Frame(self, padding=(0, 5, 10, 5))
        btn_frame.pack(side="right", fill="y")
        ttkb.Button(btn_frame, text="删除", bootstyle="danger-outline",
                    command=self._delete_selected).pack(pady=2)

        self._load_data()

    def _load_data(self):
        for i in self.tree.get_children():
            self.tree.delete(i)
        try:
            rows = db.list_users()
        except Exception as e:
            Messagebox.show_error(f"加载失败:\n{e}", "错误")
            return
        for r in rows:
            status_text = "启用" if r.get("status") == 1 else "禁用"
            role_text = "管理员" if r.get("role") == "ADMIN" else "管库员"
            self.tree.insert("", "end", iid=str(r["id"]), values=(
                r["id"], r["username"], r.get("real_name", ""),
                role_text, status_text,
                str(r.get("created_at", ""))[:19]
            ))

    def _open_add(self):
        AddUserDialog(self, on_ok=self._load_data)

    def _delete_selected(self):
        sel = self.tree.selection()
        if not sel:
            Messagebox.show_warning("请先选择要删除的用户", "提示")
            return
        uid = int(sel[0])
        username = self.tree.item(sel[0], "values")[1]
        if not Messagebox.yesno(f"确定删除用户 [{username}] 吗？", "确认删除"):
            return
        try:
            db.delete_user(uid)
            self._load_data()
        except Exception as e:
            Messagebox.show_error(f"删除失败:\n{e}", "错误")


class AddUserDialog(ttkb.Toplevel):
    def __init__(self, master, on_ok=None):
        super().__init__(master)
        self.on_ok = on_ok
        self.title("新增用户")
        self.geometry("380x360")
        self.resizable(False, False)
        self.transient(master)
        self.grab_set()

        frame = ttkb.Frame(self, padding=20)
        frame.pack(fill="both", expand=True)

        ttkb.Label(frame, text="用户名", font=(FONT_FAMILY, 10, "bold")).pack(anchor="w")
        self.u_var = ttkb.Entry(frame, font=(FONT_FAMILY, 11))
        self.u_var.pack(fill="x", pady=(4, 10))

        ttkb.Label(frame, text="密码", font=(FONT_FAMILY, 10, "bold")).pack(anchor="w")
        self.p_var = ttkb.Entry(frame, font=(FONT_FAMILY, 11), show="*")
        self.p_var.pack(fill="x", pady=(4, 10))

        ttkb.Label(frame, text="姓名", font=(FONT_FAMILY, 10, "bold")).pack(anchor="w")
        self.n_var = ttkb.Entry(frame, font=(FONT_FAMILY, 11))
        self.n_var.pack(fill="x", pady=(4, 10))

        ttkb.Label(frame, text="角色", font=(FONT_FAMILY, 10, "bold")).pack(anchor="w")
        self.r_var = ttkb.Combobox(frame, values=["管库员", "管理员"], state="readonly")
        self.r_var.set("管库员")
        self.r_var.pack(fill="x", pady=(4, 15))

        ttkb.Button(frame, text="确 定", bootstyle="primary",
                    command=self._ok).pack(fill="x", ipady=4)

    def _ok(self):
        u = self.u_var.get().strip()
        p = self.p_var.get().strip()
        n = self.n_var.get().strip()
        r = "ADMIN" if self.r_var.get() == "管理员" else "KEEPER"
        if not u or not p or not n:
            Messagebox.show_warning("所有字段必填", "提示")
            return
        if len(u) < 3:
            Messagebox.show_warning("用户名至少3个字符", "提示")
            return
        if len(p) < 6:
            Messagebox.show_warning("密码至少6位", "提示")
            return
        try:
            existing = db.find_user_by_username(u)
            if existing:
                Messagebox.show_warning("该用户名已存在", "提示")
                return
            pwd_hash = hashlib.sha256(p.encode()).hexdigest()
            db.add_user(u, pwd_hash, n, r)
            if self.on_ok:
                self.on_ok()
            self.destroy()
        except Exception as e:
            Messagebox.show_error(f"添加失败:\n{e}", "错误")
