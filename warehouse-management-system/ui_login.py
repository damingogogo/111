# -*- coding: utf-8 -*-
"""登录/注册窗口"""
import tkinter as tk
import ttkbootstrap as ttkb
from ttkbootstrap.dialogs import Messagebox
import hashlib
import hmac
import queue
import threading
import time
import db
from config import FONT_FAMILY

try:
    import bcrypt
except Exception:
    bcrypt = None


class LoginWindow(ttkb.Toplevel):
    def __init__(self, master, on_login_success):
        super().__init__(master)
        self.on_login_success = on_login_success
        self._busy = False
        self.title("登录 - 物资到货入库管理系统")
        self.geometry("420x520")
        self.resizable(False, False)
        # The root window is withdrawn while login is shown. In a frozen GUI
        # build, a transient dialog owned by that hidden root may stay invisible.
        self.grab_set()

        # 居中
        self.update_idletasks()
        x = (self.winfo_screenwidth() - 420) // 2
        y = (self.winfo_screenheight() - 520) // 2
        self.geometry(f"+{x}+{y}")

        self._build_ui()
        self._show_in_front()
        self.username_var.focus_set()
        self.protocol("WM_DELETE_WINDOW", self._on_close)

    def _show_in_front(self):
        try:
            self.deiconify()
            self.lift()
            self.attributes("-topmost", True)
            self.after(500, lambda: self.attributes("-topmost", False))
            self.focus_force()
        except tk.TclError:
            pass

    def _build_ui(self):
        # 标题区
        header = ttkb.Frame(self, bootstyle="primary", padding=(0, 30))
        header.pack(fill="x")
        ttkb.Label(header, text="物资到货入库管理系统",
                   font=(FONT_FAMILY, 16, "bold"),
                   bootstyle="inverse-primary").pack()
        ttkb.Label(header, text="请登录后继续操作",
                   font=(FONT_FAMILY, 10),
                   bootstyle="inverse-primary").pack(pady=(4, 0))

        # 表单区
        form = ttkb.Frame(self, padding=30)
        form.pack(fill="both", expand=True)

        # 登录/注册切换
        self.mode = "login"
        self.mode_btn = ttkb.Button(form, text="还没有账号？去注册",
                                    bootstyle="link",
                                    command=self._toggle_mode)
        self.mode_btn.pack(anchor="e", pady=(0, 10))

        # 用户名
        ttkb.Label(form, text="用户名", font=(FONT_FAMILY, 10, "bold")).pack(anchor="w")
        self.username_var = ttkb.Entry(form, font=(FONT_FAMILY, 11))
        self.username_var.pack(fill="x", pady=(4, 12))

        # 密码
        ttkb.Label(form, text="密码", font=(FONT_FAMILY, 10, "bold")).pack(anchor="w")
        self.password_var = ttkb.Entry(form, font=(FONT_FAMILY, 11), show="*")
        self.password_var.pack(fill="x", pady=(4, 12))

        # 确认密码（注册时显示）
        self.confirm_lbl = ttkb.Label(form, text="确认密码", font=(FONT_FAMILY, 10, "bold"))
        self.confirm_var = ttkb.Entry(form, font=(FONT_FAMILY, 11), show="*")

        # 姓名（注册时显示）
        self.name_lbl = ttkb.Label(form, text="姓名", font=(FONT_FAMILY, 10, "bold"))
        self.name_var = ttkb.Entry(form, font=(FONT_FAMILY, 11))

        # 按钮
        self.action_btn = ttkb.Button(form, text="登 录", bootstyle="primary",
                                      command=self._do_action)
        self.action_btn.pack(fill="x", pady=(10, 0), ipady=6)

        # 回车触发
        self.username_var.bind("<Return>", lambda e: self.password_var.focus_set())
        self.password_var.bind("<Return>", lambda e: self._do_action())
        self.confirm_var.bind("<Return>", lambda e: self._do_action())
        self.name_var.bind("<Return>", lambda e: self._do_action())

    def _toggle_mode(self):
        if self.mode == "login":
            self.mode = "register"
            self.title("注册 - 物资到货入库管理系统")
            self.mode_btn.configure(text="已有账号？去登录")
            self.action_btn.configure(text="注 册")
            self.name_lbl.pack(anchor="w", pady=(4, 0), before=self.action_btn)
            self.name_var.pack(fill="x", pady=(4, 12), before=self.action_btn)
            self.confirm_lbl.pack(anchor="w", pady=(4, 0), before=self.action_btn)
            self.confirm_var.pack(fill="x", pady=(4, 12), before=self.action_btn)
            self.geometry("420x700")
        else:
            self.mode = "login"
            self.title("登录 - 物资到货入库管理系统")
            self.mode_btn.configure(text="还没有账号？去注册")
            self.action_btn.configure(text="登 录")
            self.name_lbl.pack_forget()
            self.name_var.pack_forget()
            self.confirm_lbl.pack_forget()
            self.confirm_var.pack_forget()
            self.geometry("420x520")

    def _hash(self, pwd):
        if bcrypt is not None:
            return bcrypt.hashpw(pwd.encode(), bcrypt.gensalt()).decode()
        return hashlib.sha256(pwd.encode()).hexdigest()

    def _check_password(self, password, stored):
        if not stored:
            return False
        if stored.startswith(("$2a$", "$2b$", "$2y$")):
            if bcrypt is None:
                return False
            try:
                return bcrypt.checkpw(password.encode(), stored.encode())
            except ValueError:
                return False
        expected = hashlib.sha256(password.encode()).hexdigest()
        return hmac.compare_digest(expected, stored)

    def _format_db_error(self, error):
        text = str(error)
        if "1045" in text or "Access denied" in text:
            return "数据库账号没有远程访问权限，请先在服务器 MySQL 授权后再登录或注册。"
        return text

    def _do_action(self):
        if self._busy:
            return
        u = self.username_var.get().strip()
        p = self.password_var.get().strip()
        if not u or not p:
            Messagebox.show_warning("用户名和密码不能为空", "提示", parent=self)
            return

        if self.mode == "login":
            self._do_login(u, p)
        else:
            p2 = self.confirm_var.get().strip()
            name = self.name_var.get().strip()
            if p != p2:
                Messagebox.show_warning("两次输入的密码不一致", "提示", parent=self)
                return
            if not name:
                Messagebox.show_warning("请输入姓名", "提示", parent=self)
                return
            self._do_register(u, p, name)

    def _set_busy(self, busy, text=None):
        self._busy = busy
        state = "disabled" if busy else "normal"
        self.action_btn.configure(state=state)
        if text:
            self.action_btn.configure(text=text)
        elif not busy:
            self.action_btn.configure(text="登 录" if self.mode == "login" else "注 册")
        self.update_idletasks()

    def _run_background(self, work, done, busy_text):
        self._set_busy(True, busy_text)
        result_queue = queue.Queue(maxsize=1)
        started_at = time.monotonic()

        def runner():
            try:
                result = work()
                error = None
            except Exception as exc:
                result = None
                error = exc
            result_queue.put((result, error))

        threading.Thread(target=runner, daemon=True).start()

        def poll_result():
            try:
                result, error = result_queue.get_nowait()
            except queue.Empty:
                if time.monotonic() - started_at > 10:
                    self._set_busy(False)
                    Messagebox.show_error(
                        "数据库连接超时，请检查服务器数据库是否已授权远程访问。",
                        "错误",
                        parent=self,
                    )
                    return
                if self.winfo_exists():
                    self.after(100, poll_result)
                return
            done(result, error)

        self.after(100, poll_result)

    def _do_login(self, username, password):
        def work():
            user = db.find_user_by_username(username)
            if not user:
                return {"error": "用户名不存在"}
            if not self._check_password(password, user["password"]):
                return {"error": "密码错误"}
            if user.get("status") != 1:
                return {"error": "该用户已被禁用"}
            return {"user": user}

        def done(result, error):
            self._set_busy(False)
            if error:
                Messagebox.show_error(f"数据库查询失败:\n{self._format_db_error(error)}", "错误", parent=self)
                return
            if result.get("error"):
                Messagebox.show_error(result["error"], "登录失败", parent=self)
                return
            user = result["user"]
            self.on_login_success({
                "id": user["id"],
                "username": user["username"],
                "real_name": user.get("real_name", ""),
                "role": user.get("role", "KEEPER"),
            })
            self.destroy()

        self._run_background(work, done, "登录中...")

    def _do_register(self, username, password, real_name):
        if len(username) < 3:
            Messagebox.show_warning("用户名至少3个字符", "提示", parent=self)
            return
        if len(password) < 6:
            Messagebox.show_warning("密码至少6位", "提示", parent=self)
            return

        def work():
            existing = db.find_user_by_username(username)
            if existing:
                return {"error": "该用户名已被注册"}
            db.add_user(username, self._hash(password), real_name, "KEEPER")
            return {"ok": True}

        def done(result, error):
            self._set_busy(False)
            if error:
                Messagebox.show_error(f"注册失败:\n{self._format_db_error(error)}", "错误", parent=self)
                return
            if result.get("error"):
                Messagebox.show_warning(result["error"], "提示", parent=self)
                return
            Messagebox.show_info("注册成功，请登录", "提示", parent=self)
            self._toggle_mode()
            self.username_var.delete(0, "end")
            self.password_var.delete(0, "end")
            self.username_var.focus_set()

        self._run_background(work, done, "注册中...")

    def _on_close(self):
        self.master.destroy()
