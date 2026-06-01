from cx_Freeze import setup, Executable
import sys

build_exe_options = {
    "packages": ["pymysql", "openpyxl", "ttkbootstrap", "PIL", "tkinter"],
    "includes": ["db", "config", "ui_common", "ui_materials", "ui_arrivals", "ui_warehousing", "ui_export", "ui_login", "ui_user_mgmt", "excel_io"],
    "include_files": ["app.ico", "app.png"],
    "excludes": ["matplotlib", "numpy", "pandas", "scipy", "PyQt5", "PyQt6", "PySide2", "PySide6"],
}

base = None
if sys.platform == "win32":
    base = "gui"

executables = [
    Executable("main.py", base=base, target_name="WarehouseMgmt.exe", icon="app.ico")
]

setup(
    name="WarehouseMgmt",
    version="1.2.0",
    description="Warehouse Management System",
    options={"build_exe": build_exe_options},
    executables=executables,
)
