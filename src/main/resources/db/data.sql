-- ============================================================
-- NEPU_FAMS 完整初始化数据脚本
-- 基于东北石油大学官网信息
-- ============================================================

USE nepu_fams;

SET FOREIGN_KEY_CHECKS = 0;

-- 清空所有业务表
TRUNCATE TABLE operation_log;
TRUNCATE TABLE inventory_record;
TRUNCATE TABLE inventory_task;
TRUNCATE TABLE repair_order;
TRUNCATE TABLE scrapplication;
TRUNCATE TABLE borrow_approval;
TRUNCATE TABLE borrow_application;
TRUNCATE TABLE asset;
TRUNCATE TABLE asset_category;
TRUNCATE TABLE sys_user;
TRUNCATE TABLE college;

-- ============================================================
-- 1. 学院（基于东油官网实际院系设置）
-- ============================================================
INSERT INTO college (id, name, code, is_deleted) VALUES
(1, '地球科学学院', 'GEO', 0),
(2, '石油工程学院', 'PE', 0),
(3, '化学化工学院', 'CHEM', 0),
(4, '机械科学与工程学院', 'ME', 0),
(5, '电气信息工程学院', 'EE', 0),
(6, '计算机与信息技术学院', 'CS', 0),
(7, '土木建筑工程学院', 'CIVIL', 0),
(8, '经济管理与人文学院', 'EM', 0);

-- ============================================================
-- 2. 用户（每学院1个管理员+2个普通用户，1个超级管理员）
-- 密码统一为 123456（BCrypt），admin 密码为 admin123
-- ============================================================
INSERT INTO sys_user (id, username, password, real_name, role, college_id, phone, email, status, is_deleted) VALUES
(1,  'admin',          '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '系统管理员', 'SUPER_ADMIN',   NULL, '13900000000', 'admin@nepu.edu.cn',   1, 0),

(2,  'geo_admin',      '$2a$10$f8Jbf4VxK3PhLKVHJPSKpesUfzguNZmhbPFakn9NZHuhG91uJZwAq', '陈志远',     'COLLEGE_ADMIN', 1,    '13901010001', 'geo_admin@nepu.edu.cn',   1, 0),
(3,  'geo_user1',      '$2a$10$f8Jbf4VxK3PhLKVHJPSKpesUfzguNZmhbPFakn9NZHuhG91uJZwAq', '刘晓东',     'USER',          1,    '13901010002', 'liuxd@nepu.edu.cn',       1, 0),
(4,  'geo_user2',      '$2a$10$f8Jbf4VxK3PhLKVHJPSKpesUfzguNZmhbPFakn9NZHuhG91uJZwAq', '王丽华',     'USER',          1,    '13901010003', 'wanglh@nepu.edu.cn',      1, 0),

(5,  'pe_admin',       '$2a$10$f8Jbf4VxK3PhLKVHJPSKpesUfzguNZmhbPFakn9NZHuhG91uJZwAq', '孙建国',     'COLLEGE_ADMIN', 2,    '13901020001', 'pe_admin@nepu.edu.cn',    1, 0),
(6,  'pe_user1',       '$2a$10$f8Jbf4VxK3PhLKVHJPSKpesUfzguNZmhbPFakn9NZHuhG91uJZwAq', '李伟',       'USER',          2,    '13901020002', 'liwei@nepu.edu.cn',       1, 0),
(7,  'pe_user2',       '$2a$10$f8Jbf4VxK3PhLKVHJPSKpesUfzguNZmhbPFakn9NZHuhG91uJZwAq', '张敏',       'USER',          2,    '13901020003', 'zhangmin@nepu.edu.cn',    1, 0),

(8,  'chem_admin',     '$2a$10$f8Jbf4VxK3PhLKVHJPSKpesUfzguNZmhbPFakn9NZHuhG91uJZwAq', '杨海燕',     'COLLEGE_ADMIN', 3,    '13901030001', 'chem_admin@nepu.edu.cn',  1, 0),
(9,  'chem_user1',     '$2a$10$f8Jbf4VxK3PhLKVHJPSKpesUfzguNZmhbPFakn9NZHuhG91uJZwAq', '吴小红',     'USER',          3,    '13901030002', 'wuxh@nepu.edu.cn',        1, 0),
(10, 'chem_user2',     '$2a$10$f8Jbf4VxK3PhLKVHJPSKpesUfzguNZmhbPFakn9NZHuhG91uJZwAq', '赵磊',       'USER',          3,    '13901030003', 'zhaolei@nepu.edu.cn',     1, 0),

(11, 'me_admin',       '$2a$10$f8Jbf4VxK3PhLKVHJPSKpesUfzguNZmhbPFakn9NZHuhG91uJZwAq', '马俊杰',     'COLLEGE_ADMIN', 4,    '13901040001', 'me_admin@nepu.edu.cn',    1, 0),
(12, 'me_user1',       '$2a$10$f8Jbf4VxK3PhLKVHJPSKpesUfzguNZmhbPFakn9NZHuhG91uJZwAq', '高伟',       'USER',          4,    '13901040002', 'gaowei@nepu.edu.cn',      1, 0),
(13, 'me_user2',       '$2a$10$f8Jbf4VxK3PhLKVHJPSKpesUfzguNZmhbPFakn9NZHuhG91uJZwAq', '冯晓',       'USER',          4,    '13901040003', 'fengxiao@nepu.edu.cn',    1, 0),

(14, 'ee_admin',       '$2a$10$f8Jbf4VxK3PhLKVHJPSKpesUfzguNZmhbPFakn9NZHuhG91uJZwAq', '钱国华',     'COLLEGE_ADMIN', 5,    '13901050001', 'ee_admin@nepu.edu.cn',    1, 0),
(15, 'ee_user1',       '$2a$10$f8Jbf4VxK3PhLKVHJPSKpesUfzguNZmhbPFakn9NZHuhG91uJZwAq', '孙佳',       'USER',          5,    '13901050002', 'sunjia@nepu.edu.cn',      1, 0),
(16, 'ee_user2',       '$2a$10$f8Jbf4VxK3PhLKVHJPSKpesUfzguNZmhbPFakn9NZHuhG91uJZwAq', '周强',       'USER',          5,    '13901050003', 'zhouqiang@nepu.edu.cn',   1, 0),

(17, 'cs_admin',       '$2a$10$f8Jbf4VxK3PhLKVHJPSKpesUfzguNZmhbPFakn9NZHuhG91uJZwAq', '王明',       'COLLEGE_ADMIN', 6,    '13901060001', 'cs_admin@nepu.edu.cn',    1, 0),
(18, 'cs_user1',       '$2a$10$f8Jbf4VxK3PhLKVHJPSKpesUfzguNZmhbPFakn9NZHuhG91uJZwAq', '李娜',       'USER',          6,    '13901060002', 'lina@nepu.edu.cn',        1, 0),
(19, 'cs_user2',       '$2a$10$f8Jbf4VxK3PhLKVHJPSKpesUfzguNZmhbPFakn9NZHuhG91uJZwAq', '张涛',       'USER',          6,    '13901060003', 'zhangtao@nepu.edu.cn',    1, 0),

(20, 'civil_admin',    '$2a$10$f8Jbf4VxK3PhLKVHJPSKpesUfzguNZmhbPFakn9NZHuhG91uJZwAq', '何明',       'COLLEGE_ADMIN', 7,    '13901070001', 'civil_admin@nepu.edu.cn', 1, 0),
(21, 'civil_user1',    '$2a$10$f8Jbf4VxK3PhLKVHJPSKpesUfzguNZmhbPFakn9NZHuhG91uJZwAq', '杨光',       'USER',          7,    '13901070002', 'yangguang@nepu.edu.cn',   1, 0),
(22, 'civil_user2',    '$2a$10$f8Jbf4VxK3PhLKVHJPSKpesUfzguNZmhbPFakn9NZHuhG91uJZwAq', '林霞',       'USER',          7,    '13901070003', 'linxia@nepu.edu.cn',      1, 0),

(23, 'em_admin',       '$2a$10$f8Jbf4VxK3PhLKVHJPSKpesUfzguNZmhbPFakn9NZHuhG91uJZwAq', '徐丽',       'COLLEGE_ADMIN', 8,    '13901080001', 'em_admin@nepu.edu.cn',    1, 0),
(24, 'em_user1',       '$2a$10$f8Jbf4VxK3PhLKVHJPSKpesUfzguNZmhbPFakn9NZHuhG91uJZwAq', '黄强',       'USER',          8,    '13901080002', 'huangqiang@nepu.edu.cn',  1, 0),
(25, 'em_user2',       '$2a$10$f8Jbf4VxK3PhLKVHJPSKpesUfzguNZmhbPFakn9NZHuhG91uJZwAq', '吴静',       'USER',          8,    '13901080003', 'wujing@nepu.edu.cn',      1, 0);

-- ============================================================
-- 3. 资产分类（6个一级 + 16个二级）
-- ============================================================
INSERT INTO asset_category (id, name, parent_id, level, sort, is_deleted) VALUES
(1,  '电子设备',     0, 1, 1, 0),
(2,  '办公家具',     0, 1, 2, 0),
(3,  '实验设备',     0, 1, 3, 0),
(4,  '石油专用设备', 0, 1, 4, 0),
(5,  '化工设备',     0, 1, 5, 0),
(6,  '测量仪器',     0, 1, 6, 0),

(7,  '计算机',     1, 2, 1, 0),
(8,  '打印机',     1, 2, 2, 0),
(9,  '投影仪',     1, 2, 3, 0),
(10, '网络设备',   1, 2, 4, 0),
(11, '办公桌',     2, 2, 1, 0),
(12, '办公椅',     2, 2, 2, 0),
(13, '文件柜',     2, 2, 3, 0),
(14, '实验仪器',   3, 2, 1, 0),
(15, '测试设备',   3, 2, 2, 0),
(16, '钻采设备',   4, 2, 1, 0),
(17, '测井设备',   4, 2, 2, 0),
(18, '储运设备',   4, 2, 3, 0),
(19, '反应装置',   5, 2, 1, 0),
(20, '分析仪器',   5, 2, 2, 0),
(21, '地质测量',   6, 2, 1, 0),
(22, '力学测量',   6, 2, 2, 0);

-- ============================================================
-- 4. 资产（每台设备单独编号，89台）
-- 编号规则：学院代码-年份-序号
-- ============================================================

-- ===== 地球科学学院 (college_id=1) 10台 =====
INSERT INTO asset (asset_no, name, category_id, specification, purchase_date, purchase_price, current_value, status, location, college_id, responsible_person, remark, is_deleted) VALUES
('GEO-2024-0001', '偏光显微镜',     14, 'Nikon Eclipse LV100ND',         '2024-03-15', 85000.00,  76500.00,  'IN_USE', '地球科学学院岩矿实验室201',   1, '陈志远', '岩矿鉴定用',       0),
('GEO-2024-0002', '体视显微镜',     14, 'Olympus SZX16',                 '2024-03-15', 32000.00,  28800.00,  'IN_USE', '地球科学学院岩矿实验室201',   1, '陈志远', '微体古生物鉴定',   0),
('GEO-2024-0003', '岩石切割机',     15, 'Buehler Isomet 1000',           '2023-09-20', 25000.00,  21250.00,  'IDLE',   '地球科学学院样品制备室105',   1, '刘晓东', '岩心切割',         0),
('GEO-2024-0004', '薄片磨片机',     15, 'Logitech GTS1',                 '2023-09-20', 38000.00,  32300.00,  'IN_USE', '地球科学学院样品制备室105',   1, '刘晓东', '岩石薄片制备',     0),
('GEO-2024-0005', '地质罗盘',       21, '北京地质仪器厂 DQL-8',          '2024-01-10', 800.00,    720.00,    'IN_USE', '地球科学学院野外装备库103',   1, '王丽华', '野外地质调查',     0),
('GEO-2024-0006', 'GPS手持机',      21, 'Garmin GPSMAP 66s',             '2024-01-10', 3500.00,   3150.00,   'IDLE',   '地球科学学院野外装备库103',   1, '王丽华', '野外定位',         0),
('GEO-2024-0007', '便携式光谱仪',   20, 'Olympus DELTA Premium',         '2024-05-20', 180000.00, 162000.00, 'IN_USE', '地球科学学院地球化学实验室302',1, '陈志远', '元素快速分析',     0),
('GEO-2024-0008', '台式电脑',       7,  '戴尔 OptiPlex 7090 i7/16G/512G','2024-02-20', 6500.00,   5850.00,   'IN_USE', '地球科学学院机房401',         1, '陈志远', '地质建模用',       0),
('GEO-2024-0009', '工作站',         7,  '戴尔 Precision 7960 Xeon/64G/2T','2023-11-15',45000.00,  38250.00,  'IN_USE', '地球科学学院机房401',         1, '陈志远', '三维地震资料处理', 0),
('GEO-2024-0010', '投影仪',         9,  '爱普生 CB-FH52 4000流明',       '2024-04-10', 8500.00,   7650.00,   'IN_USE', '地球科学学院多媒体教室101',   1, '王丽华', '教学用',           0);

-- ===== 石油工程学院 (college_id=2) 12台 =====
INSERT INTO asset (asset_no, name, category_id, specification, purchase_date, purchase_price, current_value, status, location, college_id, responsible_person, remark, is_deleted) VALUES
('PE-2024-0001', '岩心渗透率测定仪', 16, '美国Corelab PMI-200',           '2023-08-15', 320000.00, 272000.00, 'IN_USE', '石油工程学院岩心实验室501',     2, '孙建国', '岩心渗透率测试',   0),
('PE-2024-0002', '岩心孔隙度测定仪', 16, '美国Corelab Poro-200',          '2023-08-15', 280000.00, 238000.00, 'IN_USE', '石油工程学院岩心实验室501',     2, '孙建国', '岩心孔隙度测试',   0),
('PE-2024-0003', '高压釜',           19, '威海化工 500ml/50MPa',          '2024-02-28', 45000.00,  40500.00,  'IN_USE', '石油工程学院提高采收率实验室502',2, '李伟',   '高温高压驱替实验', 0),
('PE-2024-0004', '驱替装置',         16, '海安石油 DQY-200',              '2024-02-28', 180000.00, 162000.00, 'IN_USE', '石油工程学院提高采收率实验室502',2, '李伟',   '化学驱油实验',     0),
('PE-2024-0005', '旋转粘度计',       20, 'Brookfield DV3T-RV',            '2023-10-10', 32000.00,  27200.00,  'IDLE',   '石油工程学院流体实验室503',     2, '李伟',   '流体粘度测量',     0),
('PE-2024-0006', '管道流量计',       18, 'Emerson 8712EM',                '2024-01-20', 28000.00,  25200.00,  'IN_USE', '石油工程学院多相流实验室504',   2, '张敏',   '多相流测量',       0),
('PE-2024-0007', '压力传感器',       22, 'Rosemount 3051S',               '2024-01-20', 12000.00,  10800.00,  'IN_USE', '石油工程学院多相流实验室504',   2, '张敏',   '管道压力监测',     0),
('PE-2024-0008', '储罐液位计',       18, 'E+H Levelflex FMP54',           '2023-12-15', 35000.00,  31500.00,  'IN_USE', '石油工程学院储运实验室505',     2, '张敏',   '储罐液位监测',     0),
('PE-2024-0009', '工作站',           7,  'HP Z8 G4 Xeon/128G/4T',         '2023-07-20', 85000.00,  68000.00,  'IN_USE', '石油工程学院数值模拟室601',    2, '孙建国', '油藏数值模拟',     0),
('PE-2024-0010', '服务器',           10, '戴尔 PowerEdge R750 2U',        '2023-07-20', 120000.00, 96000.00,  'IN_USE', '石油工程学院机房602',          2, '孙建国', '并行计算集群节点', 0),
('PE-2024-0011', '激光打印机',       8,  'HP LaserJet Pro M404dn',        '2024-03-10', 2800.00,   2520.00,   'IN_USE', '石油工程学院办公楼201',        2, '李伟',   '办公打印',         0),
('PE-2024-0012', '投影仪',           9,  '松下 PT-FRZ8000C 8000流明',     '2024-04-05', 25000.00,  22500.00,  'IN_USE', '石油工程学院报告厅101',        2, '张敏',   '学术报告',         0);

-- ===== 化学化工学院 (college_id=3) 12台 =====
INSERT INTO asset (asset_no, name, category_id, specification, purchase_date, purchase_price, current_value, status, location, college_id, responsible_person, remark, is_deleted) VALUES
('CHEM-2024-0001', '气相色谱仪',   20, 'Agilent 7890B',                  '2023-06-15', 280000.00, 238000.00, 'IN_USE', '化学化工学院分析实验室301', 3, '杨海燕', '组分分析',       0),
('CHEM-2024-0002', '液相色谱仪',   20, 'Agilent 1260 Infinity II',       '2023-06-15', 350000.00, 297500.00, 'IN_USE', '化学化工学院分析实验室301', 3, '杨海燕', '高分子分析',     0),
('CHEM-2024-0003', '红外光谱仪',   20, 'Nicolet iS50 FT-IR',             '2023-09-10', 180000.00, 153000.00, 'IN_USE', '化学化工学院分析实验室302', 3, '杨海燕', '官能团分析',     0),
('CHEM-2024-0004', '反应釜',       19, '威海化工 2L/30MPa',              '2024-01-15', 38000.00,  34200.00,  'IN_USE', '化学化工学院催化实验室303', 3, '吴小红', '催化反应',       0),
('CHEM-2024-0005', '旋转蒸发仪',   14, 'BUCHI R-300',                    '2024-01-15', 25000.00,  22500.00,  'IN_USE', '化学化工学院有机实验室304', 3, '吴小红', '溶剂蒸馏',       0),
('CHEM-2024-0006', '马弗炉',       15, '上海一恒 SX2-12-16N 1600℃',      '2023-11-20', 18000.00,  15300.00,  'IN_USE', '化学化工学院高温实验室305', 3, '吴小红', '高温灼烧',       0),
('CHEM-2024-0007', '电子天平',     20, 'Mettler ME204E 220g/0.1mg',      '2024-02-10', 8500.00,   7650.00,   'IN_USE', '化学化工学院天平室306',     3, '杨海燕', '精密称量',       0),
('CHEM-2024-0008', 'pH计',         20, 'Mettler SevenExcellence S400',   '2024-02-10', 12000.00,  10800.00,  'IDLE',   '化学化工学院天平室306',     3, '杨海燕', '酸碱度测量',     0),
('CHEM-2024-0009', '通风柜',       15, '大连大众 1800mm',                '2023-08-30', 15000.00,  12750.00,  'IN_USE', '化学化工学院有机实验室304', 3, '吴小红', '实验排风',       0),
('CHEM-2024-0010', '离心机',       14, '湖南湘仪 H1850R',                '2024-03-20', 22000.00,  19800.00,  'IN_USE', '化学化工学院生化实验室307', 3, '赵磊',   '样品分离',       0),
('CHEM-2024-0011', '台式电脑',     7,  '联想 ThinkCentre M90a i5/8G/256G','2024-03-20', 4500.00,   4050.00,   'IN_USE', '化学化工学院机房401',       3, '赵磊',   '实验数据采集',   0),
('CHEM-2024-0012', '办公桌',       11, '1.4米实木办公桌',                '2023-09-15', 1200.00,   1080.00,   'IN_USE', '化学化工学院办公楼205',     3, '杨海燕', '办公用',         0);

-- ===== 机械科学与工程学院 (college_id=4) 12台 =====
INSERT INTO asset (asset_no, name, category_id, specification, purchase_date, purchase_price, current_value, status, location, college_id, responsible_person, remark, is_deleted) VALUES
('ME-2024-0001', '数控车床',           16, '沈阳机床 CKA6150i',           '2023-05-20', 180000.00, 153000.00, 'IN_USE', '机械学院金工实习车间101',     4, '马俊杰', '教学加工',         0),
('ME-2024-0002', '数控铣床',           16, '大连机床 VMC850',            '2023-05-20', 320000.00, 272000.00, 'IN_USE', '机械学院金工实习车间101',     4, '马俊杰', '教学加工',         0),
('ME-2024-0003', '激光切割机',         15, '大族激光 MARVEL 3000W',      '2024-02-15', 280000.00, 252000.00, 'IN_USE', '机械学院先进制造实验室102',   4, '马俊杰', '板材切割',         0),
('ME-2024-0004', '3D打印机',           15, 'Stratasys F170 FDM',         '2024-02-15', 85000.00,  76500.00,  'IN_USE', '机械学院先进制造实验室102',   4, '马俊杰', '快速成型',         0),
('ME-2024-0005', '万能材料试验机',     22, 'MTS E45.305 300kN',          '2023-10-30', 220000.00, 187000.00, 'IN_USE', '机械学院力学实验室201',       4, '高伟',   '材料拉伸试验',     0),
('ME-2024-0006', '冲击试验机',         22, '美特斯 JBW-300C',            '2023-10-30', 65000.00,  55250.00,  'IN_USE', '机械学院力学实验室201',       4, '高伟',   '冲击韧性测试',     0),
('ME-2024-0007', '硬度计',             22, 'Wilson 432SVD 洛氏/布氏',    '2023-11-15', 45000.00,  38250.00,  'IDLE',   '机械学院力学实验室201',       4, '高伟',   '硬度测试',         0),
('ME-2024-0008', '三坐标测量机',       15, '海克斯康 Global S 7.10.7',   '2024-01-20', 480000.00, 432000.00, 'IN_USE', '机械学院精密测量实验室202',   4, '高伟',   '精密尺寸测量',     0),
('ME-2024-0009', '井架检测仪',         16, '华科检测 HK-9800',           '2023-08-10', 150000.00, 127500.00, 'IN_USE', '机械学院石油井架检测实验室301',4, '冯晓',   '石油井架安全检测', 0),
('ME-2024-0010', '表面粗糙度仪',       22, 'Mitutoyo SJ-410',            '2024-03-10', 35000.00,  31500.00,  'IN_USE', '机械学院精密测量实验室202',   4, '高伟',   '表面粗糙度测量',   0),
('ME-2024-0011', '台式电脑',           7,  '戴尔 OptiPlex 7090 i7/16G/512G','2024-03-10',6500.00,  5850.00,   'IN_USE', '机械学院CAD机房401',          4, '马俊杰', 'CAD制图',          0),
('ME-2024-0012', '投影仪',             9,  '爱普生 CB-X06 3600流明',     '2024-04-20', 4500.00,   4050.00,   'IN_USE', '机械学院多媒体教室302',       4, '冯晓',   '教学用',           0);

-- ===== 电气信息工程学院 (college_id=5) 12台 =====
INSERT INTO asset (asset_no, name, category_id, specification, purchase_date, purchase_price, current_value, status, location, college_id, responsible_person, remark, is_deleted) VALUES
('EE-2024-0001', '数字示波器',                 15, '泰克 MSO64B 6GHz',           '2023-07-15', 280000.00, 238000.00, 'IN_USE', '电气学院电子实验室401',     5, '钱国华', '高速信号采集',           0),
('EE-2024-0002', '示波器',                     15, '泰克 TBS2104 100MHz',        '2024-01-10', 12000.00,  10800.00,  'IN_USE', '电气学院电子实验室402',     5, '钱国华', '教学用示波器',           0),
('EE-2024-0003', '信号发生器',                 15, '泰克 AFG31051 50MHz',        '2024-01-10', 18000.00,  16200.00,  'IN_USE', '电气学院电子实验室402',     5, '钱国华', '信号源',                 0),
('EE-2024-0004', '数字万用表',                 20, 'Fluke 8846A 6.5位',          '2023-09-20', 22000.00,  18700.00,  'IN_USE', '电气学院测量实验室403',     5, '钱国华', '高精度测量',             0),
('EE-2024-0005', '万用表',                     20, 'Fluke 17B+',                 '2024-02-15', 1500.00,   1350.00,   'IDLE',   '电气学院电子实验室402',     5, '钱国华', '教学用万用表',           0),
('EE-2024-0006', '电力系统综合自动化实验台',   14, '浙江天煌 KHD-2',             '2023-11-10', 85000.00,  72250.00,  'IN_USE', '电气学院电力实验室501',     5, '孙佳',   '电力系统实验',           0),
('EE-2024-0007', '电机实验台',                 14, '浙江天煌 DD-1',              '2023-11-10', 65000.00,  55250.00,  'IN_USE', '电气学院电机实验室502',     5, '孙佳',   '电机拖动实验',           0),
('EE-2024-0008', 'PLC实验箱',                  15, '西门子 S7-1200',             '2024-03-15', 8500.00,   7650.00,   'IN_USE', '电气学院控制实验室503',     5, '周强',   'PLC编程实验',            0),
('EE-2024-0009', '变频器',                     15, 'ABB ACS580 5.5kW',           '2024-03-15', 12000.00,  10800.00,  'IN_USE', '电气学院控制实验室503',     5, '周强',   '变频调速实验',           0),
('EE-2024-0010', '交换机',                     10, '华为 S5735-L24T4XE-A',       '2023-08-20', 8500.00,   7225.00,   'IN_USE', '电气学院网络实验室601',     5, '周强',   '网络实验',               0),
('EE-2024-0011', '路由器',                     10, '华为 AR6280',                '2023-08-20', 15000.00,  12750.00,  'IN_USE', '电气学院网络实验室601',     5, '周强',   '网络路由',               0),
('EE-2024-0012', '工作站',                     7,  '戴尔 Precision 5860 i9/32G/1T','2024-04-10', 28000.00, 25200.00, 'IN_USE', '电气学院智能控制实验室602', 5, '孙佳',   '智能控制算法',           0);

-- ===== 计算机与信息技术学院 (college_id=6) 15台 =====
INSERT INTO asset (asset_no, name, category_id, specification, purchase_date, purchase_price, current_value, status, location, college_id, responsible_person, remark, is_deleted) VALUES
('CS-2024-0001', '服务器',     10, '戴尔 PowerEdge R760 2U',           '2023-06-10', 180000.00, 153000.00, 'IN_USE', '计算机学院数据中心B101',       6, '王明', '教学云平台',       0),
('CS-2024-0002', '服务器',     10, '华为 FusionServer Pro 2288H',      '2023-06-10', 150000.00, 127500.00, 'IN_USE', '计算机学院数据中心B101',       6, '王明', '大数据实验平台',   0),
('CS-2024-0003', 'GPU服务器',  10, '戴尔 PowerEdge XE9680 8×A100',     '2024-03-20', 980000.00, 882000.00, 'IN_USE', '计算机学院数据中心B101',       6, '王明', '深度学习训练',     0),
('CS-2024-0004', '工作站',     7,  '戴尔 Precision 7960 Xeon/128G/4T', '2024-03-20', 85000.00,  76500.00,  'IN_USE', '计算机学院AI实验室302',        6, '李娜', 'AI模型训练',       0),
('CS-2024-0005', '台式电脑',   7,  '戴尔 OptiPlex 7090 i7/16G/512G',   '2024-02-15', 6500.00,   5850.00,   'IN_USE', '计算机学院机房401',            6, '李娜', '教学用机',         0),
('CS-2024-0006', '台式电脑',   7,  '戴尔 OptiPlex 7090 i7/16G/512G',   '2024-02-15', 6500.00,   5850.00,   'IN_USE', '计算机学院机房401',            6, '李娜', '教学用机',         0),
('CS-2024-0007', '台式电脑',   7,  '戴尔 OptiPlex 7090 i7/16G/512G',   '2024-02-15', 6500.00,   5850.00,   'IN_USE', '计算机学院机房401',            6, '李娜', '教学用机',         0),
('CS-2024-0008', '台式电脑',   7,  '戴尔 OptiPlex 7090 i7/16G/512G',   '2024-02-15', 6500.00,   5850.00,   'IDLE',   '计算机学院机房401',            6, '李娜', '教学用机',         0),
('CS-2024-0009', '笔记本',     7,  'ThinkPad P16 Gen2 i9/32G/1T',      '2024-04-10', 18000.00,  16200.00,  'IN_USE', '计算机学院网络安全实验室303',  6, '张涛', '安全渗透测试',     0),
('CS-2024-0010', '交换机',     10, '华为 S5735-L48T4XE-A',             '2023-09-10', 15000.00,  12750.00,  'IN_USE', '计算机学院网络实验室304',      6, '张涛', '网络组网实验',     0),
('CS-2024-0011', '防火墙',     10, '华为 USG6525E',                    '2023-09-10', 28000.00,  23800.00,  'IN_USE', '计算机学院网络实验室304',      6, '张涛', '网络安全实验',     0),
('CS-2024-0012', '激光打印机', 8,  'HP LaserJet Pro M404dn',           '2024-01-15', 2800.00,   2520.00,   'IN_USE', '计算机学院办公楼201',          6, '王明', '办公打印',         0),
('CS-2024-0013', '投影仪',     9,  '爱普生 CB-FH52 4000流明',         '2024-04-20', 8500.00,   7650.00,   'IN_USE', '计算机学院多媒体教室101',      6, '李娜', '教学用',           0),
('CS-2024-0014', '办公桌',     11, '1.4米实木办公桌',                  '2023-08-15', 1200.00,   1080.00,   'IN_USE', '计算机学院办公楼201',          6, '王明', '办公用',           0),
('CS-2024-0015', '文件柜',     13, '四节铁皮文件柜',                   '2023-08-15', 800.00,    720.00,    'IN_USE', '计算机学院办公楼201',          6, '王明', '资料存放',         0);

-- ===== 土木建筑工程学院 (college_id=7) 8台 =====
INSERT INTO asset (asset_no, name, category_id, specification, purchase_date, purchase_price, current_value, status, location, college_id, responsible_person, remark, is_deleted) VALUES
('CIVIL-2024-0001', '万能试验机',       22, '济南试金 WDW-1000 1000kN',  '2023-07-20', 180000.00, 153000.00, 'IN_USE', '土建学院结构实验室201', 7, '何明', '混凝土抗压',     0),
('CIVIL-2024-0002', '压力试验机',       22, '无锡建仪 YA-2000 2000kN',   '2023-07-20', 120000.00, 102000.00, 'IN_USE', '土建学院结构实验室201', 7, '何明', '混凝土抗压',     0),
('CIVIL-2024-0003', '全站仪',           21, '徕卡 TS16 R500',           '2024-02-10', 85000.00,  76500.00,  'IN_USE', '土建学院测量实验室202', 7, '何明', '工程测量',       0),
('CIVIL-2024-0004', '水准仪',           21, '苏一光 DSZ2',              '2024-02-10', 3500.00,   3150.00,   'IDLE',   '土建学院测量实验室202', 7, '杨光', '高程测量',       0),
('CIVIL-2024-0005', '经纬仪',           21, '苏一光 J6',                '2024-02-10', 2800.00,   2520.00,   'IDLE',   '土建学院测量实验室202', 7, '杨光', '角度测量',       0),
('CIVIL-2024-0006', '混凝土搅拌机',     15, '山东建仪 HJW-60',          '2023-10-15', 8500.00,   7225.00,   'IN_USE', '土建学院材料实验室203', 7, '林霞', '混凝土制备',     0),
('CIVIL-2024-0007', '台式电脑',         7,  '联想 ThinkCentre M90a i5/8G/256G','2024-03-15',4500.00, 4050.00, 'IN_USE', '土建学院CAD机房301',    7, '何明', 'CAD制图',        0),
('CIVIL-2024-0008', '投影仪',           9,  '爱普生 CB-X06 3600流明',   '2024-04-10', 4500.00,   4050.00,   'IN_USE', '土建学院多媒体教室101', 7, '林霞', '教学用',         0);

-- ===== 经济管理与人文学院 (college_id=8) 8台 =====
INSERT INTO asset (asset_no, name, category_id, specification, purchase_date, purchase_price, current_value, status, location, college_id, responsible_person, remark, is_deleted) VALUES
('EM-2024-0001', '台式电脑',   7,  '联想 ThinkCentre M90a i5/8G/256G', '2024-02-20', 4500.00,  4050.00,  'IN_USE', '经管学院机房401',       8, '徐丽', '教学用机',     0),
('EM-2024-0002', '台式电脑',   7,  '联想 ThinkCentre M90a i5/8G/256G', '2024-02-20', 4500.00,  4050.00,  'IN_USE', '经管学院机房401',       8, '徐丽', '教学用机',     0),
('EM-2024-0003', '台式电脑',   7,  '联想 ThinkCentre M90a i5/8G/256G', '2024-02-20', 4500.00,  4050.00,  'IN_USE', '经管学院机房401',       8, '徐丽', '教学用机',     0),
('EM-2024-0004', '笔记本',     7,  'ThinkPad E14 Gen5 i5/16G/512G',    '2024-03-10', 5500.00,  4950.00,  'IDLE',   '经管学院办公楼201',     8, '黄强', '移动办公',     0),
('EM-2024-0005', '打印机',     8,  'HP LaserJet Pro M404dn',           '2024-01-15', 2800.00,  2520.00,  'IN_USE', '经管学院办公楼201',     8, '黄强', '办公打印',     0),
('EM-2024-0006', '投影仪',     9,  '爱普生 CB-X06 3600流明',           '2024-04-15', 4500.00,  4050.00,  'IN_USE', '经管学院多媒体教室101', 8, '吴静', '教学用',       0),
('EM-2024-0007', '办公桌',     11, '1.4米实木办公桌',                  '2023-09-10', 1200.00,  1080.00,  'IN_USE', '经管学院办公楼201',     8, '徐丽', '办公用',       0),
('EM-2024-0008', '办公椅',     12, '人体工学办公椅',                   '2023-09-10', 600.00,   540.00,   'IN_USE', '经管学院办公楼201',     8, '徐丽', '办公用',       0);

-- ============================================================
-- 5. 操作日志（示例）
-- ============================================================
INSERT INTO operation_log (user_id, username, operation, type, method, params, ip) VALUES
(1, 'admin', '用户登录', 'LOGIN', 'POST', '/api/auth/login', '127.0.0.1');

-- ============================================================
-- 重置自增ID
-- ============================================================
ALTER TABLE college AUTO_INCREMENT = 1;
ALTER TABLE sys_user AUTO_INCREMENT = 1;
ALTER TABLE asset_category AUTO_INCREMENT = 1;
ALTER TABLE asset AUTO_INCREMENT = 1;

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- 验证统计
-- ============================================================
SELECT '学院' AS item, COUNT(*) AS count FROM college WHERE is_deleted=0
UNION ALL SELECT '用户', COUNT(*) FROM sys_user WHERE is_deleted=0
UNION ALL SELECT '资产分类', COUNT(*) FROM asset_category WHERE is_deleted=0
UNION ALL SELECT '资产', COUNT(*) FROM asset WHERE is_deleted=0
UNION ALL SELECT '闲置资产', COUNT(*) FROM asset WHERE is_deleted=0 AND status='IDLE'
UNION ALL SELECT '在用资产', COUNT(*) FROM asset WHERE is_deleted=0 AND status='IN_USE';
