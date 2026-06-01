# -*- mode: python ; coding: utf-8 -*-
"""PyInstaller spec —— 物资到货入库管理系统"""
from PyInstaller.utils.hooks import collect_all

datas, binaries, hiddenimports = [], [], []

# 收集 ttkbootstrap 的主题资源等
for pkg in ("ttkbootstrap",):
    d, b, h = collect_all(pkg)
    datas += d
    binaries += b
    hiddenimports += h

# 附带应用图标(运行时也能读到)
datas += [("app.ico", "."), ("app.png", ".")]

# 额外保险
hiddenimports += [
    "pymysql", "pymysql.cursors",
    "bcrypt",
    "openpyxl", "openpyxl.cell._writer",
    "PIL", "PIL.Image",
]

a = Analysis(
    ["main.py"],
    pathex=[],
    binaries=binaries,
    datas=datas,
    hiddenimports=hiddenimports,
    hookspath=[],
    hooksconfig={},
    runtime_hooks=[],
    excludes=[
        "matplotlib", "numpy", "pandas",
        "scipy", "PyQt5", "PyQt6", "PySide2", "PySide6",
        "IPython", "jupyter", "notebook",
        "test", "unittest",
    ],
    noarchive=False,
)
pyz = PYZ(a.pure)

exe = EXE(
    pyz,
    a.scripts,
    a.binaries,
    a.datas,
    [],
    name="WarehouseMgmt",
    debug=False,
    bootloader_ignore_signals=False,
    strip=False,
    upx=True,
    upx_exclude=[],
    runtime_tmpdir=None,
    console=False,
    disable_windowed_traceback=False,
    argv_emulation=False,
    target_arch=None,
    codesign_identity=None,
    entitlements_file=None,
    icon="app.ico",
)
