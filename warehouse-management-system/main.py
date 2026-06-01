# -*- coding: utf-8 -*-
"""物资到货入库管理系统 —— 主入口"""
import os
import sys
import traceback
import tkinter as tk
import ttkbootstrap as ttkb
from ttkbootstrap.dialogs import Messagebox

from config import (
    APP_TITLE, APP_VERSION, APP_THEME, FONT_FAMILY,
    DB_CONFIG, DB_NAME, ARRIVAL_STATUS, CURRENT_USER,
)
import db
from ui_common import StatBadge
from ui_materials import MaterialsTab
from ui_arrivals import ArrivalsTab
from ui_warehousing import WarehousingTab
from ui_export import ExportTab
from ui_login import LoginWindow


def _resource(path):
    """打包后能找到外部资源 (PyInstaller --add-data)"""
    base = getattr(sys, "_MEIPASS", os.path.dirname(os.path.abspath(__file__)))
    return os.path.join(base, path)


class App(ttkb.Toplevel):
    def __init__(self, master, user_info):
        super().__init__(master)
        self.title(f"{APP_TITLE}  v{APP_VERSION}")
        self.geometry("1360x820")
        self.minsize(1180, 720)
        self.protocol("WM_DELETE_WINDOW", self._quit_app)
        try:
            self.state("zoomed")
        except tk.TclError:
            pass

        # 保存当前登录用户
        import config
        config.CURRENT_USER = user_info
        self.current_user = user_info
        self.is_admin = user_info.get("role") == "ADMIN"

        # 应用图标
        icon = _resource("app.ico")
        if os.path.exists(icon):
            try:
                self.iconbitmap(icon)
            except Exception:
                pass

        # 全局字体
        self._setup_fonts()

        # 数据库
        try:
            db.init_database()
        except Exception as e:
            Messagebox.show_error(
                f"连接 MySQL {DB_CONFIG['host']}:{DB_CONFIG['port']} 失败:\n\n{e}",
                "数据库连接失败",
            )
            self._quit_app()
            sys.exit(1)

        # 头部
        self._build_header()
        # 状态栏(先建,Tab 构建时会用 set_status)
        self._build_statusbar()
        # 内容
        self._build_body()
        # 菜单
        self._build_menu()

        # 跨 Tab 事件
        self.tab_arrivals.bind("<<GotoWarehousing>>", self._goto_warehousing)
        self.nb.bind("<<NotebookTabChanged>>", self._on_tab_changed)

        self.set_status(f"已登录: {user_info.get('real_name', user_info['username'])} ({'管理员' if self.is_admin else '管库员'})")
        self.after(50, self._refresh_badges)

    # ---------- 样式 ----------
    def _setup_fonts(self):
        from tkinter import font as tkfont
        try:
            for name in ("TkDefaultFont", "TkTextFont", "TkHeadingFont",
                         "TkMenuFont", "TkFixedFont"):
                f = tkfont.nametofont(name)
                f.configure(family=FONT_FAMILY, size=10)
        except Exception:
            pass

        style = ttkb.Style()
        style.configure("Treeview", rowheight=26, font=(FONT_FAMILY, 10))
        style.configure("Treeview.Heading",
                        font=(FONT_FAMILY, 10, "bold"), padding=4)
        # 自定义 Tab 样式
        style.configure("TNotebook.Tab",
                        font=(FONT_FAMILY, 10, "bold"), padding=(18, 8))
        style.configure("TLabelframe.Label",
                        font=(FONT_FAMILY, 10, "bold"))

    # ---------- 头部 ----------
    def _build_header(self):
        header = ttkb.Frame(self, bootstyle="primary", padding=(20, 14))
        header.pack(fill="x")

        left = ttkb.Frame(header, bootstyle="primary")
        left.pack(side="left", fill="y")

        ttkb.Label(
            left, text=APP_TITLE,
            font=(FONT_FAMILY, 17, "bold"),
            bootstyle="inverse-primary",
        ).pack(anchor="w")
        ttkb.Label(
            left, text=f"v{APP_VERSION}    物资到货登记 · 认领 · 验收 · 上架 · 入库 一站式管理",
            font=(FONT_FAMILY, 9),
            bootstyle="inverse-primary",
        ).pack(anchor="w", pady=(2, 0))

        # 右侧用户信息 + 徽章
        right = ttkb.Frame(header, bootstyle="primary")
        right.pack(side="right")

        # 用户标签
        role_text = "管理员" if self.is_admin else "管库员"
        ttkb.Label(
            right,
            text=f"{self.current_user.get('real_name', self.current_user['username'])} ({role_text})",
            font=(FONT_FAMILY, 9),
            bootstyle="inverse-primary",
        ).pack(side="left", padx=(0, 10))

        self.badges = {}
        styles = {
            "物资": "light",
            "待认领": "warning",
            "已认领": "info",
            "已验收": "primary",
            "已上架": "success",
            "已入库": "secondary",
        }
        for key in ["物资", "待认领", "已认领", "已验收", "已上架", "已入库"]:
            b = StatBadge(right, key, bootstyle=styles.get(key, "primary"))
            b.pack(side="left", padx=4)
            self.badges[key] = b

    # ---------- 内容区 ----------
    def _build_body(self):
        body = ttkb.Frame(self, padding=(8, 8, 8, 0))
        body.pack(fill="both", expand=True)

        self.nb = ttkb.Notebook(body, bootstyle="primary")
        self.nb.pack(fill="both", expand=True)

        self.tab_materials = MaterialsTab(self.nb, status_callback=self.set_status)
        self.tab_arrivals = ArrivalsTab(self.nb, status_callback=self.set_status)
        self.tab_warehousing = WarehousingTab(self.nb, status_callback=self.set_status)
        self.tab_export = ExportTab(self.nb, status_callback=self.set_status)

        self.nb.add(self.tab_materials, text="① 物资基础信息")
        self.nb.add(self.tab_arrivals, text="② 到货登记")
        self.nb.add(self.tab_warehousing, text="③ 入库管理")
        self.nb.add(self.tab_export, text="④ 进度导出")

    # ---------- 状态栏 ----------
    def _build_statusbar(self):
        status = ttkb.Frame(self, bootstyle="secondary", padding=(10, 4))
        status.pack(side="bottom", fill="x")
        self.status_var = tk.StringVar(value="就绪")
        ttkb.Label(
            status, textvariable=self.status_var,
            bootstyle="inverse-secondary",
            anchor="w",
        ).pack(side="left")
        ttkb.Label(
            status,
            text=f"  MySQL: {DB_CONFIG['host']}:{DB_CONFIG['port']} / 库: {DB_NAME}  ",
            bootstyle="inverse-secondary",
            anchor="e",
        ).pack(side="right")

    # ---------- 菜单 ----------
    def _build_menu(self):
        menubar = tk.Menu(self)
        m_file = tk.Menu(menubar, tearoff=0)
        m_file.add_command(label="刷新所有数据", command=self._refresh_all)
        m_file.add_separator()
        m_file.add_command(label="退出", command=self._quit_app)
        menubar.add_cascade(label="操作", menu=m_file)

        # 管理员才能看到用户管理
        if self.is_admin:
            m_admin = tk.Menu(menubar, tearoff=0)
            m_admin.add_command(label="用户管理", command=self._open_user_mgmt)
            menubar.add_cascade(label="管理", menu=m_admin)

        m_help = tk.Menu(menubar, tearoff=0)
        m_help.add_command(label="使用说明", command=self._show_help)
        m_help.add_command(label="关于", command=self._show_about)
        menubar.add_cascade(label="帮助", menu=m_help)
        self.config(menu=menubar)

    # ---------- 行为 ----------
    def _refresh_all(self):
        try:
            self.tab_materials.refresh()
            self.tab_arrivals._refresh_recent()
            self.tab_arrivals._refresh_combos()
            self.tab_warehousing.refresh()
            self.tab_export._preview()
            self._refresh_badges()
            self.set_status("已刷新")
        except Exception as e:
            self.set_status(f"刷新出错: {e}")

    def _on_tab_changed(self, _e):
        idx = self.nb.index("current")
        if idx == 1:
            self.tab_arrivals._refresh_combos()
            self.tab_arrivals._refresh_recent()
        elif idx == 2:
            self.tab_warehousing.refresh()
        elif idx == 3:
            self.tab_export._preview()
        self._refresh_badges()

    def _refresh_badges(self):
        try:
            s = db.stats_summary()
        except Exception as e:
            self.set_status(f"统计失败: {e}")
            return
        self.badges["物资"].set_value(s["materials"])
        for st in ARRIVAL_STATUS:
            self.badges[st].set_value(s["by_status"].get(st, 0))

    def _goto_warehousing(self, _e):
        sel = self.tab_arrivals.tree_widget.tree.selection()
        self.nb.select(self.tab_warehousing)
        if sel:
            self.tab_warehousing.select_by_id(int(sel[0]))

    def set_status(self, msg):
        self.status_var.set(msg)

    def _quit_app(self):
        try:
            self.master.destroy()
        except Exception:
            self.destroy()

    def _show_help(self):
        text = (
            "操作步骤:\n\n"
            "① 物资基础信息\n"
            "    导入或手动添加物资编码 / 名称 / 型号 / 单位。\n"
            "    支持 Excel 批量导入(可先「下载模板」)。\n\n"
            "② 到货登记\n"
            "    录入物资编码,自动联动名称/型号/单位。\n"
            "    手动填写供应商、到货数量、件数、重量。\n"
            "    也支持 Excel 批量导入到货记录。\n\n"
            "③ 入库管理\n"
            "    管库员认领自己负责的到货记录。\n"
            "    填写「验收完成时间」「上架时间」「入库单号」。\n"
            "    状态自动推进: 待认领 → 已认领 → 已验收 → 已上架 → 已入库。\n\n"
            "④ 进度导出\n"
            "    按筛选条件预览并导出 Excel,用于汇报或留档。\n"
        )
        win = ttkb.Toplevel(self)
        win.title("使用说明")
        win.geometry("680x520")
        win.transient(self)
        frame = ttkb.Frame(win, padding=14)
        frame.pack(fill="both", expand=True)
        ttkb.Label(frame, text="使用说明",
                   font=(FONT_FAMILY, 14, "bold")).pack(anchor="w", pady=(0, 8))
        t = tk.Text(frame, wrap="word", font=(FONT_FAMILY, 10),
                    bg="#F8F9FA", relief="flat", padx=10, pady=10)
        t.insert("1.0", text)
        t.config(state="disabled")
        t.pack(fill="both", expand=True)

    def _show_about(self):
        user = self.current_user
        role = "管理员" if self.is_admin else "管库员"
        Messagebox.show_info(
            f"{APP_TITLE}  v{APP_VERSION}\n\n"
            f"当前用户: {user.get('real_name', user['username'])} ({role})\n\n"
            "技术栈: Python + ttkbootstrap + MySQL + openpyxl\n"
            f"数据库: {DB_CONFIG['host']}:{DB_CONFIG['port']} / {DB_NAME}",
            "关于",
        )

    def _open_user_mgmt(self):
        """管理员打开用户管理窗口"""
        from ui_user_mgmt import UserMgmtWindow
        UserMgmtWindow(self)


def excepthook(t, v, tb):
    try:
        Messagebox.show_error(
            "".join(traceback.format_exception(t, v, tb))[:4000],
            "未捕获错误",
        )
    except Exception:
        pass
    sys.__excepthook__(t, v, tb)


def main():
    sys.excepthook = excepthook
    root = ttkb.Window(themename=APP_THEME)
    root.withdraw()

    user_info = {"role": None}

    def on_login(user):
        user_info.update(user)

    login_win = LoginWindow(root, on_login)
    root.wait_window(login_win)

    if user_info["role"] is None:
        root.destroy()
        sys.exit(0)

    App(root, user_info)
    root.mainloop()


if __name__ == "__main__":
    main()
