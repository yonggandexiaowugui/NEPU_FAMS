-- NEPU_FAMS 固定资产管理系统数据库建表脚本

CREATE DATABASE IF NOT EXISTS nepu_fams DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE nepu_fams;

DROP TABLE IF EXISTS college;
CREATE TABLE college (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '学院ID',
    name VARCHAR(100) NOT NULL COMMENT '学院名称',
    code VARCHAR(50) NOT NULL UNIQUE COMMENT '学院编码',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除标识 0-未删除 1-已删除',
    INDEX idx_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学院表';

DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码',
    real_name VARCHAR(50) COMMENT '真实姓名',
    role VARCHAR(20) NOT NULL COMMENT '角色：SUPER_ADMIN/COLLEGE_ADMIN/USER',
    college_id BIGINT COMMENT '所属学院ID',
    phone VARCHAR(20) COMMENT '手机号',
    email VARCHAR(100) COMMENT '邮箱',
    avatar VARCHAR(255) COMMENT '头像URL',
    status TINYINT DEFAULT 1 COMMENT '状态：1-正常 0-禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除标识 0-未删除 1-已删除',
    INDEX idx_username (username),
    INDEX idx_role (role),
    INDEX idx_college_id (college_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

DROP TABLE IF EXISTS asset_category;
CREATE TABLE asset_category (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '分类ID',
    name VARCHAR(100) NOT NULL COMMENT '分类名称',
    parent_id BIGINT DEFAULT 0 COMMENT '父分类ID',
    level TINYINT DEFAULT 1 COMMENT '层级',
    sort INT DEFAULT 0 COMMENT '排序',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除标识 0-未删除 1-已删除',
    INDEX idx_parent_id (parent_id),
    INDEX idx_level (level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资产分类表';

DROP TABLE IF EXISTS asset;
CREATE TABLE asset (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '资产ID',
    asset_no VARCHAR(50) NOT NULL UNIQUE COMMENT '资产编号',
    name VARCHAR(200) NOT NULL COMMENT '资产名称',
    category_id BIGINT NOT NULL COMMENT '分类ID',
    specification VARCHAR(500) COMMENT '规格型号',
    purchase_date DATE COMMENT '购置日期',
    purchase_price DECIMAL(12,2) COMMENT '购置价格',
    current_value DECIMAL(12,2) COMMENT '当前价值',
    status VARCHAR(20) DEFAULT 'IDLE' COMMENT '状态：IDLE/IN_USE/REPAIRING/SCRAPPED/LOSS',
    location VARCHAR(200) COMMENT '存放位置',
    college_id BIGINT NOT NULL COMMENT '所属学院ID',
    user_id BIGINT COMMENT '使用人ID',
    responsible_person VARCHAR(50) COMMENT '责任人',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除标识 0-未删除 1-已删除',
    INDEX idx_asset_no (asset_no),
    INDEX idx_category_id (category_id),
    INDEX idx_status (status),
    INDEX idx_college_id (college_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资产表';

DROP TABLE IF EXISTS borrow_application;
CREATE TABLE borrow_application (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '申请ID',
    asset_id BIGINT NOT NULL COMMENT '资产ID',
    user_id BIGINT NOT NULL COMMENT '申请人ID',
    college_id BIGINT NOT NULL COMMENT '所属学院ID',
    purpose VARCHAR(500) COMMENT '领用用途',
    expected_return_date DATE COMMENT '预计归还日期',
    status VARCHAR(30) DEFAULT 'PENDING_COLLEGE' COMMENT '状态：PENDING_COLLEGE/PENDING_SUPER/APPROVED/REJECTED/BORROWED/RETURNED',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除标识 0-未删除 1-已删除',
    INDEX idx_asset_id (asset_id),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_college_id (college_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='领用申请表';

DROP TABLE IF EXISTS borrow_approval;
CREATE TABLE borrow_approval (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '审批记录ID',
    application_id BIGINT NOT NULL COMMENT '申请ID',
    approver_id BIGINT NOT NULL COMMENT '审批人ID',
    approval_status VARCHAR(20) NOT NULL COMMENT '审批状态：APPROVED/REJECTED',
    opinion VARCHAR(500) COMMENT '审批意见',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '审批时间',
    INDEX idx_application_id (application_id),
    INDEX idx_approver_id (approver_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='领用审批记录表';

DROP TABLE IF EXISTS repair_order;
CREATE TABLE repair_order (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '工单ID',
    asset_id BIGINT NOT NULL COMMENT '资产ID',
    user_id BIGINT NOT NULL COMMENT '报修人ID',
    college_id BIGINT NOT NULL COMMENT '所属学院ID',
    fault_description TEXT COMMENT '故障描述',
    remark VARCHAR(500) COMMENT '备注',
    attachment_urls TEXT COMMENT '附件URL，多个用逗号分隔',
    priority VARCHAR(20) DEFAULT 'NORMAL' COMMENT '优先级：LOW/NORMAL/HIGH/URGENT（兼容历史 MEDIUM 映射为 NORMAL）',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态：PENDING/IN_PROGRESS/COMPLETED/SCRAPPED',
    assignee_id BIGINT COMMENT '维修负责人ID',
    repair_result TEXT COMMENT '维修结果',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除标识 0-未删除 1-已删除',
    INDEX idx_asset_id (asset_id),
    INDEX idx_status (status),
    INDEX idx_college_id (college_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='维修工单表';

DROP TABLE IF EXISTS scrapplication;
CREATE TABLE scrapplication (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '申请ID',
    asset_id BIGINT NOT NULL COMMENT '资产ID',
    college_id BIGINT NOT NULL COMMENT '所属学院ID',
    proposer_id BIGINT NOT NULL COMMENT '申请人ID',
    reason TEXT COMMENT '报废原因',
    attachment_urls TEXT COMMENT '附件URL，多个用逗号分隔',
    status VARCHAR(30) DEFAULT 'PENDING_COLLEGE' COMMENT '状态：PENDING_COLLEGE/PENDING_SUPER/APPROVED/REJECTED（兼容历史 PENDING）',
    approver_id BIGINT COMMENT '最近审批人ID',
    approval_opinion VARCHAR(500) COMMENT '最近审批意见',
    college_approver_id BIGINT COMMENT '学院初审人ID',
    college_approval_opinion VARCHAR(500) COMMENT '学院初审意见',
    super_approver_id BIGINT COMMENT '校级复审人ID',
    super_approval_opinion VARCHAR(500) COMMENT '校级复审意见',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除标识 0-未删除 1-已删除',
    INDEX idx_asset_id (asset_id),
    INDEX idx_status (status),
    INDEX idx_college_id (college_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报废申请表';

DROP TABLE IF EXISTS inventory_task;
CREATE TABLE inventory_task (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '任务ID',
    name VARCHAR(200) NOT NULL COMMENT '任务名称',
    college_id BIGINT COMMENT '学院ID（为空表示全校）',
    category_id BIGINT COMMENT '分类ID（为空表示全部分类）',
    start_time DATETIME COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    status VARCHAR(20) DEFAULT 'IN_PROGRESS' COMMENT '状态：IN_PROGRESS/COMPLETED/ARCHIVED',
    creator_id BIGINT NOT NULL COMMENT '创建人ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除标识 0-未删除 1-已删除',
    INDEX idx_status (status),
    INDEX idx_college_id (college_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='盘点任务表';

DROP TABLE IF EXISTS inventory_record;
CREATE TABLE inventory_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '记录ID',
    task_id BIGINT NOT NULL COMMENT '任务ID',
    asset_id BIGINT NOT NULL COMMENT '资产ID',
    asset_no VARCHAR(50) COMMENT '资产编号（冗余）',
    asset_name VARCHAR(200) COMMENT '资产名称（冗余）',
    actual_quantity INT DEFAULT 1 COMMENT '实盘数量',
    remark VARCHAR(500) COMMENT '备注',
    recorder_id BIGINT COMMENT '记录人ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除标识 0-未删除 1-已删除',
    INDEX idx_task_id (task_id),
    INDEX idx_asset_id (asset_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='盘点记录表';

DROP TABLE IF EXISTS operation_log;
CREATE TABLE operation_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '日志ID',
    user_id BIGINT COMMENT '用户ID',
    username VARCHAR(50) COMMENT '用户名',
    operation VARCHAR(100) COMMENT '操作描述',
    type VARCHAR(20) COMMENT '操作类型：LOGIN/LOGOUT/ADD/UPDATE/DELETE/QUERY/APPROVE/EXPORT/OTHER',
    method VARCHAR(200) COMMENT '请求方法',
    params TEXT COMMENT '请求参数',
    ip VARCHAR(50) COMMENT 'IP地址',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    INDEX idx_user_id (user_id),
    INDEX idx_type (type),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';
