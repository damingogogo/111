# -*- coding: utf-8 -*-
"""数据库与系统配置"""

DB_CONFIG = {
    "host": "119.29.152.180",
    "port": 3307,
    "user": "root",
    "password": "QWERqwer123456@@",
    "charset": "utf8mb4",
    "connect_timeout": 3,
    "read_timeout": 5,
    "write_timeout": 5,
    "autocommit": False,
}

DB_NAME = "warehouse_mgmt"

ARRIVAL_STATUS = ["待认领", "已认领", "已验收", "已上架", "已入库"]

# 状态对应的 ttkbootstrap 颜色风格
STATUS_BOOTSTYLE = {
    "待认领": "warning",
    "已认领": "info",
    "已验收": "primary",
    "已上架": "success",
    "已入库": "secondary",
}

# 状态对应的 Treeview 行底色
STATUS_ROW_COLOR = {
    "待认领": "#FFF8E1",
    "已认领": "#E3F2FD",
    "已验收": "#E8F5E9",
    "已上架": "#E0F7F4",
    "已入库": "#ECEFF1",
}

APP_TITLE = "物资到货入库管理系统"
APP_VERSION = "1.1.0"
APP_THEME = "cosmo"      # 可选: cosmo / flatly / litera / yeti / morph
FONT_FAMILY = "微软雅黑"

# 当前登录用户信息（运行时设置）
CURRENT_USER = None  # dict: {id, username, real_name, role}
