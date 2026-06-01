# -*- coding: utf-8 -*-
"""数据库操作层 —— 自动建库建表 + CRUD"""
import pymysql
from contextlib import contextmanager
from datetime import datetime
from config import DB_CONFIG, DB_NAME


def _server_connect():
    """连接到 MySQL 服务器(不指定库)"""
    return pymysql.connect(**DB_CONFIG)


def _db_connect():
    """连接到目标数据库"""
    cfg = dict(DB_CONFIG)
    cfg["database"] = DB_NAME
    return pymysql.connect(**cfg)


@contextmanager
def get_cursor():
    conn = _db_connect()
    try:
        cur = conn.cursor(pymysql.cursors.DictCursor)
        yield cur, conn
        conn.commit()
    except Exception:
        conn.rollback()
        raise
    finally:
        conn.close()


def init_database():
    """启动时调用：自动创建数据库与表"""
    conn = _server_connect()
    try:
        with conn.cursor() as cur:
            cur.execute(
                f"CREATE DATABASE IF NOT EXISTS `{DB_NAME}` "
                "DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci"
            )
        conn.commit()
    except Exception:
        # 部署环境常用受限账号，仅允许操作 warehouse_mgmt 库；
        # 数据库已存在时没有 CREATE DATABASE 权限也可以继续。
        pass
    finally:
        conn.close()

    conn = _db_connect()
    try:
        with conn.cursor() as cur:
            cur.execute(
                """
                CREATE TABLE IF NOT EXISTS materials (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    material_code VARCHAR(64) NOT NULL UNIQUE COMMENT '物资编码',
                    name VARCHAR(255) NOT NULL COMMENT '名称',
                    model VARCHAR(255) DEFAULT '' COMMENT '型号',
                    unit VARCHAR(32) DEFAULT '' COMMENT '单位',
                    remark VARCHAR(500) DEFAULT '' COMMENT '备注',
                    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                    INDEX idx_name (name)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物资基础信息'
                """
            )
            cur.execute(
                """
                CREATE TABLE IF NOT EXISTS arrivals (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    material_code VARCHAR(64) NOT NULL COMMENT '物资编码',
                    name VARCHAR(255) DEFAULT '' COMMENT '名称(快照)',
                    model VARCHAR(255) DEFAULT '' COMMENT '型号',
                    unit VARCHAR(32) DEFAULT '' COMMENT '单位',
                    purchase_order_no VARCHAR(128) DEFAULT '' COMMENT '采购订单号',
                    purchase_order_line VARCHAR(64) DEFAULT '' COMMENT '采购订单行号',
                    source VARCHAR(255) DEFAULT '' COMMENT '来源',
                    supplier VARCHAR(255) DEFAULT '' COMMENT '供应商',
                    waybill_no VARCHAR(128) DEFAULT '' COMMENT '运单号',
                    arrival_quantity DECIMAL(18,3) DEFAULT 0 COMMENT '到货数量',
                    packaging VARCHAR(255) DEFAULT '' COMMENT '包装',
                    package_count INT DEFAULT 0 COMMENT '件数',
                    weight DECIMAL(18,3) DEFAULT 0 COMMENT '重量',
                    warehouse_keeper VARCHAR(64) DEFAULT '' COMMENT '管库员',
                    acceptance_time DATETIME NULL COMMENT '验收完成时间',
                    shelving_time DATETIME NULL COMMENT '上架时间',
                    receipt_number VARCHAR(64) DEFAULT '' COMMENT '入库单号',
                    arrival_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '到货登记时间',
                    status VARCHAR(16) DEFAULT '待认领' COMMENT '状态',
                    remark VARCHAR(500) DEFAULT '' COMMENT '备注',
                    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                    INDEX idx_code (material_code),
                    INDEX idx_purchase_order (purchase_order_no, purchase_order_line),
                    INDEX idx_status (status),
                    INDEX idx_supplier (supplier),
                    INDEX idx_keeper (warehouse_keeper),
                    INDEX idx_arrival_time (arrival_time)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='到货登记与入库管理'
                """
            )
            cur.execute(
                """
                CREATE TABLE IF NOT EXISTS purchase_orders (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    order_no VARCHAR(128) NOT NULL COMMENT '采购订单号',
                    order_line VARCHAR(64) DEFAULT '' COMMENT '行号',
                    material_code VARCHAR(64) NOT NULL COMMENT '物资编码',
                    name VARCHAR(255) DEFAULT '' COMMENT '名称',
                    model VARCHAR(255) DEFAULT '' COMMENT '型号',
                    unit VARCHAR(32) DEFAULT '' COMMENT '单位',
                    supplier VARCHAR(255) DEFAULT '' COMMENT '供应商',
                    order_quantity DECIMAL(18,3) DEFAULT 0 COMMENT '订单数量',
                    order_date VARCHAR(64) DEFAULT '' COMMENT '订单日期',
                    delivery_date VARCHAR(64) DEFAULT '' COMMENT '交货日期',
                    remark VARCHAR(500) DEFAULT '' COMMENT '备注',
                    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                    UNIQUE KEY uk_order_line_material (order_no, order_line, material_code),
                    INDEX idx_po_material (material_code),
                    INDEX idx_po_supplier (supplier)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='采购订单基础数据'
                """
            )
            _add_column_if_missing(cur, "arrivals", "purchase_order_no",
                                   "purchase_order_no VARCHAR(128) DEFAULT '' COMMENT '采购订单号' AFTER unit")
            _add_column_if_missing(cur, "arrivals", "purchase_order_line",
                                   "purchase_order_line VARCHAR(64) DEFAULT '' COMMENT '采购订单行号' AFTER purchase_order_no")
            _add_column_if_missing(cur, "arrivals", "source",
                                   "source VARCHAR(255) DEFAULT '' COMMENT '来源' AFTER purchase_order_line")
            _add_column_if_missing(cur, "arrivals", "waybill_no",
                                   "waybill_no VARCHAR(128) DEFAULT '' COMMENT '运单号' AFTER supplier")
            _add_column_if_missing(cur, "arrivals", "packaging",
                                   "packaging VARCHAR(255) DEFAULT '' COMMENT '包装' AFTER arrival_quantity")
            cur.execute(
                """
                CREATE TABLE IF NOT EXISTS users (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    username VARCHAR(64) NOT NULL UNIQUE COMMENT '用户名',
                    password VARCHAR(255) NOT NULL COMMENT '密码',
                    real_name VARCHAR(64) DEFAULT '' COMMENT '姓名',
                    role VARCHAR(32) DEFAULT 'KEEPER' COMMENT '角色:ADMIN管理员 KEEPER管库员',
                    status TINYINT DEFAULT 1 COMMENT '状态:1启用 0禁用',
                    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户'
                """
            )
            # 插入默认管理员（如果不存在）
            cur.execute("SELECT COUNT(*) FROM users WHERE username = 'admin'")
            if cur.fetchone()[0] == 0:
                import hashlib
                pwd = hashlib.sha256('admin123'.encode()).hexdigest()
                cur.execute(
                    "INSERT INTO users(username, password, real_name, role, status) VALUES (%s, %s, %s, %s, 1)",
                    ('admin', pwd, '系统管理员', 'ADMIN')
                )
        conn.commit()
    finally:
        conn.close()


def _add_column_if_missing(cur, table, column, ddl):
    cur.execute(
        """
        SELECT COUNT(*) AS c
        FROM information_schema.columns
        WHERE table_schema=%s AND table_name=%s AND column_name=%s
        """,
        (DB_NAME, table, column),
    )
    row = cur.fetchone()
    count = row[0] if not isinstance(row, dict) else row["c"]
    if count == 0:
        cur.execute(f"ALTER TABLE `{table}` ADD COLUMN {ddl}")


# ====================== 物资基础数据 ======================

def list_materials(keyword: str = "", limit: int = 5000):
    sql = "SELECT * FROM materials"
    params = ()
    if keyword:
        sql += " WHERE material_code LIKE %s OR name LIKE %s OR model LIKE %s"
        kw = f"%{keyword}%"
        params = (kw, kw, kw)
    sql += " ORDER BY id DESC LIMIT %s"
    params = params + (limit,)
    with get_cursor() as (cur, _):
        cur.execute(sql, params)
        return cur.fetchall()


def get_material_by_code(code: str):
    with get_cursor() as (cur, _):
        # 先精确匹配(带TRIM)，再模糊匹配
        cur.execute("SELECT * FROM materials WHERE TRIM(material_code)=%s LIMIT 1", (code.strip(),))
        row = cur.fetchone()
        if not row:
            cur.execute("SELECT * FROM materials WHERE TRIM(material_code) LIKE %s LIMIT 1", (f"%{code.strip()}%",))
            row = cur.fetchone()
        return row


def add_material(code, name, model="", unit="", remark=""):
    with get_cursor() as (cur, _):
        cur.execute(
            "INSERT INTO materials(material_code, name, model, unit, remark) "
            "VALUES(%s,%s,%s,%s,%s)",
            (code, name, model, unit, remark),
        )
        return cur.lastrowid


def update_material(mid, code, name, model="", unit="", remark=""):
    with get_cursor() as (cur, _):
        cur.execute(
            "UPDATE materials SET material_code=%s, name=%s, model=%s, unit=%s, remark=%s "
            "WHERE id=%s",
            (code, name, model, unit, remark, mid),
        )


def delete_material(mid):
    with get_cursor() as (cur, _):
        cur.execute("DELETE FROM materials WHERE id=%s", (mid,))


def upsert_materials_batch(rows):
    """批量导入(按编码 upsert)，返回 (新增, 更新) 数量"""
    inserted = updated = 0
    with get_cursor() as (cur, _):
        for r in rows:
            code = (r.get("material_code") or "").strip()
            if not code:
                continue
            name = (r.get("name") or "").strip()
            model = (r.get("model") or "").strip()
            unit = (r.get("unit") or "").strip()
            remark = (r.get("remark") or "").strip()
            cur.execute("SELECT id FROM materials WHERE material_code=%s", (code,))
            row = cur.fetchone()
            if row:
                cur.execute(
                    "UPDATE materials SET name=%s, model=%s, unit=%s, remark=%s WHERE id=%s",
                    (name, model, unit, remark, row["id"]),
                )
                updated += 1
            else:
                cur.execute(
                    "INSERT INTO materials(material_code, name, model, unit, remark) "
                    "VALUES(%s,%s,%s,%s,%s)",
                    (code, name, model, unit, remark),
                )
                inserted += 1
    return inserted, updated


# ====================== 采购订单 ======================

def upsert_purchase_orders_batch(rows):
    """导入采购订单，并同步物资基础档案。返回 (新增订单, 更新订单)。"""
    inserted = updated = 0
    with get_cursor() as (cur, _):
        for r in rows:
            order_no = (r.get("order_no") or "").strip()
            material_code = (r.get("material_code") or "").strip()
            if not order_no or not material_code:
                continue
            order_line = (r.get("order_line") or "").strip()
            name = (r.get("name") or "").strip()
            model = (r.get("model") or "").strip()
            unit = (r.get("unit") or "").strip()
            supplier = (r.get("supplier") or "").strip()
            order_quantity = r.get("order_quantity") or 0
            order_date = (r.get("order_date") or "").strip()
            delivery_date = (r.get("delivery_date") or "").strip()
            remark = (r.get("remark") or "").strip()

            cur.execute("SELECT id FROM materials WHERE material_code=%s", (material_code,))
            mat = cur.fetchone()
            if mat:
                cur.execute(
                    "UPDATE materials SET name=%s, model=%s, unit=%s "
                    "WHERE material_code=%s",
                    (name, model, unit, material_code),
                )
            else:
                cur.execute(
                    "INSERT INTO materials(material_code, name, model, unit, remark) "
                    "VALUES(%s,%s,%s,%s,%s)",
                    (material_code, name, model, unit, "采购订单导入"),
                )

            cur.execute(
                "SELECT id FROM purchase_orders "
                "WHERE order_no=%s AND order_line=%s AND material_code=%s",
                (order_no, order_line, material_code),
            )
            po = cur.fetchone()
            if po:
                cur.execute(
                    "UPDATE purchase_orders SET name=%s, model=%s, unit=%s, supplier=%s, "
                    "order_quantity=%s, order_date=%s, delivery_date=%s, remark=%s "
                    "WHERE id=%s",
                    (name, model, unit, supplier, order_quantity, order_date,
                     delivery_date, remark, po["id"]),
                )
                updated += 1
            else:
                cur.execute(
                    "INSERT INTO purchase_orders(order_no, order_line, material_code, name, model, unit, "
                    "supplier, order_quantity, order_date, delivery_date, remark) "
                    "VALUES(%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s)",
                    (order_no, order_line, material_code, name, model, unit,
                     supplier, order_quantity, order_date, delivery_date, remark),
                )
                inserted += 1
    return inserted, updated


def match_purchase_order(material_code, supplier=""):
    """按物资编码/供应商找一条仍有未引用数量的采购订单。"""
    if not material_code:
        return None
    supplier = (supplier or "").strip()
    base = """
        SELECT po.*,
               COALESCE(SUM(a.arrival_quantity), 0) AS referenced_quantity
        FROM purchase_orders po
        LEFT JOIN arrivals a
          ON a.purchase_order_no = po.order_no
         AND a.purchase_order_line = po.order_line
         AND a.material_code = po.material_code
        WHERE po.material_code=%s
    """
    params = [material_code]
    if supplier:
        base += " AND (po.supplier=%s OR po.supplier='')"
        params.append(supplier)
    base += """
        GROUP BY po.id
        HAVING po.order_quantity > referenced_quantity
        ORDER BY po.order_date='', po.order_date, po.id
        LIMIT 1
    """
    with get_cursor() as (cur, _):
        cur.execute(base, params)
        return cur.fetchone()


def list_purchase_orders(keyword="", limit=5000):
    sql = "SELECT * FROM purchase_orders WHERE 1=1"
    params = []
    if keyword:
        kw = f"%{keyword}%"
        sql += " AND (order_no LIKE %s OR material_code LIKE %s OR name LIKE %s OR supplier LIKE %s)"
        params += [kw, kw, kw, kw]
    sql += " ORDER BY id DESC LIMIT %s"
    params.append(limit)
    with get_cursor() as (cur, _):
        cur.execute(sql, params)
        return cur.fetchall()


def purchase_order_reference_report():
    sql = """
        SELECT po.order_no, po.order_line, po.material_code, po.name, po.model, po.unit,
               po.supplier, po.order_quantity,
               COALESCE(SUM(a.arrival_quantity), 0) AS referenced_quantity,
               GREATEST(po.order_quantity - COALESCE(SUM(a.arrival_quantity), 0), 0) AS unreferenced_quantity,
               CASE
                   WHEN COALESCE(SUM(a.arrival_quantity), 0) <= 0 THEN '未引用'
                   WHEN COALESCE(SUM(a.arrival_quantity), 0) >= po.order_quantity THEN '已全部引用'
                   ELSE '部分引用'
               END AS reference_status
        FROM purchase_orders po
        LEFT JOIN arrivals a
          ON a.purchase_order_no = po.order_no
         AND a.purchase_order_line = po.order_line
         AND a.material_code = po.material_code
        GROUP BY po.id
        ORDER BY po.order_no, po.order_line, po.material_code
    """
    with get_cursor() as (cur, _):
        cur.execute(sql)
        return cur.fetchall()


def inbound_status_report():
    sql = """
        SELECT po.order_no, po.order_line, po.material_code, po.name, po.model, po.unit,
               po.supplier, po.order_quantity,
               COALESCE(SUM(a.arrival_quantity), 0) AS arrival_quantity,
               COALESCE(SUM(CASE WHEN a.status='已入库' THEN a.arrival_quantity ELSE 0 END), 0) AS stored_quantity,
               GREATEST(po.order_quantity - COALESCE(SUM(CASE WHEN a.status='已入库' THEN a.arrival_quantity ELSE 0 END), 0), 0) AS not_stored_quantity,
               CASE
                   WHEN COALESCE(SUM(CASE WHEN a.status='已入库' THEN a.arrival_quantity ELSE 0 END), 0) <= 0 THEN '未入库'
                   WHEN COALESCE(SUM(CASE WHEN a.status='已入库' THEN a.arrival_quantity ELSE 0 END), 0) >= po.order_quantity THEN '已全部入库'
                   ELSE '部分入库'
               END AS inbound_status
        FROM purchase_orders po
        LEFT JOIN arrivals a
          ON a.purchase_order_no = po.order_no
         AND a.purchase_order_line = po.order_line
         AND a.material_code = po.material_code
        GROUP BY po.id
        ORDER BY po.order_no, po.order_line, po.material_code
    """
    with get_cursor() as (cur, _):
        cur.execute(sql)
        return cur.fetchall()


# ====================== 到货 / 入库 ======================

def add_arrival(data: dict):
    _ensure_purchase_order_ref(data)
    with get_cursor() as (cur, _):
        cur.execute(
            """INSERT INTO arrivals
            (material_code, name, model, unit, purchase_order_no, purchase_order_line,
             source, supplier, waybill_no, arrival_quantity, packaging,
             package_count, weight, remark, status)
            VALUES(%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,'待认领')""",
            (
                data.get("material_code", ""),
                data.get("name", ""),
                data.get("model", ""),
                data.get("unit", ""),
                data.get("purchase_order_no", ""),
                data.get("purchase_order_line", ""),
                data.get("source", ""),
                data.get("supplier", ""),
                data.get("waybill_no", ""),
                data.get("arrival_quantity") or 0,
                data.get("packaging", ""),
                data.get("package_count") or 0,
                data.get("weight") or 0,
                data.get("remark", ""),
            ),
        )
        return cur.lastrowid


def list_arrivals(filters: dict = None, limit: int = 5000):
    filters = filters or {}
    sql = "SELECT * FROM arrivals WHERE 1=1"
    params = []
    if filters.get("keyword"):
        sql += (" AND (material_code LIKE %s OR name LIKE %s OR receipt_number LIKE %s "
                "OR purchase_order_no LIKE %s OR waybill_no LIKE %s OR source LIKE %s)")
        kw = f"%{filters['keyword']}%"
        params += [kw, kw, kw, kw, kw, kw]
    if filters.get("status"):
        sql += " AND status=%s"
        params.append(filters["status"])
    if filters.get("supplier"):
        sql += " AND supplier LIKE %s"
        params.append(f"%{filters['supplier']}%")
    if filters.get("keeper"):
        sql += " AND warehouse_keeper LIKE %s"
        params.append(f"%{filters['keeper']}%")
    if filters.get("date_from"):
        sql += " AND arrival_time >= %s"
        params.append(filters["date_from"])
    if filters.get("date_to"):
        sql += " AND arrival_time <= %s"
        params.append(filters["date_to"])
    sql += " ORDER BY id DESC LIMIT %s"
    params.append(limit)
    with get_cursor() as (cur, _):
        cur.execute(sql, params)
        return cur.fetchall()


def get_arrival(aid):
    with get_cursor() as (cur, _):
        cur.execute("SELECT * FROM arrivals WHERE id=%s", (aid,))
        return cur.fetchone()


def update_arrival(aid, data: dict):
    """更新到货记录(部分字段)；并按业务规则自动推进 status"""
    allowed = {
        "material_code", "name", "model", "unit", "purchase_order_no",
        "purchase_order_line", "source", "supplier", "waybill_no",
        "arrival_quantity", "packaging", "package_count", "weight",
        "warehouse_keeper", "acceptance_time", "shelving_time",
        "receipt_number", "remark", "status",
    }
    _ensure_purchase_order_ref(data)
    fields = {k: v for k, v in data.items() if k in allowed}
    if not fields:
        return

    # 业务规则:根据已填字段推算状态(若调用方未显式指定 status)
    if "status" not in fields:
        with get_cursor() as (cur, _):
            cur.execute("SELECT * FROM arrivals WHERE id=%s", (aid,))
            cur_row = cur.fetchone()
        if cur_row:
            merged = dict(cur_row)
            merged.update(fields)
            fields["status"] = _derive_status(merged)

    sets = ", ".join(f"`{k}`=%s" for k in fields)
    params = list(fields.values()) + [aid]
    with get_cursor() as (cur, _):
        cur.execute(f"UPDATE arrivals SET {sets} WHERE id=%s", params)


def _derive_status(row: dict) -> str:
    if row.get("receipt_number"):
        return "已入库"
    if row.get("shelving_time"):
        return "已上架"
    if row.get("acceptance_time"):
        return "已验收"
    if row.get("warehouse_keeper"):
        return "已认领"
    return "待认领"


def delete_arrival(aid):
    with get_cursor() as (cur, _):
        cur.execute("DELETE FROM arrivals WHERE id=%s", (aid,))


def claim_arrival(aid, keeper):
    update_arrival(aid, {"warehouse_keeper": keeper})


def complete_acceptance(aid, when=None):
    when = when or datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    update_arrival(aid, {"acceptance_time": when})


def complete_shelving(aid, when=None):
    when = when or datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    update_arrival(aid, {"shelving_time": when})


def set_receipt_number(aid, receipt_no):
    update_arrival(aid, {"receipt_number": receipt_no})


def import_arrivals_batch(rows):
    """从 Excel 批量导入到货记录"""
    n = 0
    with get_cursor() as (cur, _):
        for r in rows:
            code = (r.get("material_code") or "").strip()
            if not code:
                continue
            _ensure_purchase_order_ref(r)
            cur.execute(
                """INSERT INTO arrivals
                (material_code, name, model, unit, purchase_order_no, purchase_order_line,
                 source, supplier, waybill_no, arrival_quantity, packaging,
                 package_count, weight, warehouse_keeper, acceptance_time,
                 shelving_time, receipt_number, remark, status)
                VALUES(%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s)""",
                (
                    code,
                    (r.get("name") or "").strip(),
                    (r.get("model") or "").strip(),
                    (r.get("unit") or "").strip(),
                    (r.get("purchase_order_no") or "").strip(),
                    (r.get("purchase_order_line") or "").strip(),
                    (r.get("source") or "").strip(),
                    (r.get("supplier") or "").strip(),
                    (r.get("waybill_no") or "").strip(),
                    r.get("arrival_quantity") or 0,
                    (r.get("packaging") or "").strip(),
                    r.get("package_count") or 0,
                    r.get("weight") or 0,
                    (r.get("warehouse_keeper") or "").strip(),
                    r.get("acceptance_time") or None,
                    r.get("shelving_time") or None,
                    (r.get("receipt_number") or "").strip(),
                    (r.get("remark") or "").strip(),
                    _derive_status(r),
                ),
            )
            n += 1
    return n


def _ensure_purchase_order_ref(data):
    if not data or (data.get("purchase_order_no") or "").strip():
        return
    po = match_purchase_order(
        (data.get("material_code") or "").strip(),
        (data.get("supplier") or "").strip(),
    )
    if not po:
        return
    data["purchase_order_no"] = po.get("order_no") or ""
    data["purchase_order_line"] = po.get("order_line") or ""
    if not data.get("name"):
        data["name"] = po.get("name") or ""
    if not data.get("model"):
        data["model"] = po.get("model") or ""
    if not data.get("unit"):
        data["unit"] = po.get("unit") or ""
    if not data.get("supplier"):
        data["supplier"] = po.get("supplier") or ""


def distinct_values(column):
    """供下拉框使用：返回某列的去重值"""
    safe = {
        "supplier": "supplier",
        "warehouse_keeper": "warehouse_keeper",
    }
    col = safe.get(column)
    if not col:
        return []
    with get_cursor() as (cur, _):
        cur.execute(
            f"SELECT DISTINCT {col} FROM arrivals WHERE {col} <> '' ORDER BY {col}"
        )
        return [r[col] for r in cur.fetchall()]


def stats_summary():
    """简单统计"""
    with get_cursor() as (cur, _):
        cur.execute("SELECT COUNT(*) AS c FROM materials")
        mat_count = cur.fetchone()["c"]
        cur.execute(
            "SELECT status, COUNT(*) AS c FROM arrivals GROUP BY status"
        )
        rows = cur.fetchall()
    by_status = {r["status"]: r["c"] for r in rows}
    return {"materials": mat_count, "by_status": by_status}


# ====================== 用户管理 ======================

def find_user_by_username(username):
    with get_cursor() as (cur, _):
        cur.execute("SELECT * FROM users WHERE username = %s", (username,))
        return cur.fetchone()


def add_user(username, password, real_name, role="KEEPER"):
    with get_cursor() as (cur, _):
        cur.execute(
            "INSERT INTO users(username, password, real_name, role, status) "
            "VALUES(%s, %s, %s, %s, 1)",
            (username, password, real_name, role),
        )
        return cur.lastrowid


def list_users():
    with get_cursor() as (cur, _):
        cur.execute("SELECT id, username, real_name, role, status, created_at FROM users ORDER BY id DESC")
        return cur.fetchall()


def delete_user(uid):
    with get_cursor() as (cur, _):
        cur.execute("DELETE FROM users WHERE id = %s", (uid,))
