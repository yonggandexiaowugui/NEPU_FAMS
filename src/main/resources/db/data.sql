-- NEPU_FAMS 初始化数据脚本

USE nepu_fams;

-- 学院数据
INSERT INTO college (name, code) VALUES
('计算机学院', 'CS'),
('电气学院', 'EE'),
('机械学院', 'ME');

-- 用户数据（密码使用 BCrypt 加密）
-- admin / admin123
-- college_admin1 / 123456
-- college_admin2 / 123456
-- user1 / 123456
-- user2 / 123456
-- user3 / 123456
INSERT INTO sys_user (username, password, real_name, role, college_id, phone, email, status) VALUES
('admin', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '系统管理员', 'SUPER_ADMIN', NULL, '13800000000', 'admin@nepu.edu.cn', 1),
('college_admin1', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '张院长', 'COLLEGE_ADMIN', 1, '13800000001', 'cs_admin@nepu.edu.cn', 1),
('college_admin2', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '李院长', 'COLLEGE_ADMIN', 2, '13800000002', 'ee_admin@nepu.edu.cn', 1),
('user1', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '王老师', 'USER', 1, '13800000003', 'wang@nepu.edu.cn', 1),
('user2', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '赵老师', 'USER', 2, '13800000004', 'zhao@nepu.edu.cn', 1),
('user3', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '刘老师', 'USER', 3, '13800000005', 'liu@nepu.edu.cn', 1);

-- 资产分类数据（一级分类）
INSERT INTO asset_category (name, parent_id, level, sort) VALUES
('电子设备', 0, 1, 1),
('办公家具', 0, 1, 2),
('实验设备', 0, 1, 3);

-- 资产分类数据（二级分类）
INSERT INTO asset_category (name, parent_id, level, sort) VALUES
('计算机', 1, 2, 1),
('打印机', 1, 2, 2),
('投影仪', 1, 2, 3),
('网络设备', 1, 2, 4),
('办公桌', 2, 2, 1),
('办公椅', 2, 2, 2),
('文件柜', 2, 2, 3),
('实验仪器', 3, 2, 1),
('测试设备', 3, 2, 2);

-- 资产数据（示例）
INSERT INTO asset (asset_no, name, category_id, specification, purchase_date, purchase_price, current_value, status, location, college_id, user_id, responsible_person, remark) VALUES
('CS-2024-0001', '戴尔台式电脑', 4, 'OptiPlex 7020 i7/16G/512G', '2024-01-15', 6500.00, 5850.00, 'IN_USE', '计算机学院实验楼301', 1, 4, '王老师', '教学用电脑'),
('CS-2024-0002', '惠普激光打印机', 5, 'LaserJet Pro M404dn', '2024-02-20', 2800.00, 2520.00, 'IN_USE', '计算机学院办公楼201', 1, 2, '张院长', '办公用打印机'),
('CS-2024-0003', '爱普生投影仪', 6, 'CB-FH52 4000流明', '2024-03-10', 8500.00, 7650.00, 'IDLE', '计算机学院多媒体教室101', 1, NULL, '张院长', '教室用投影仪'),
('EE-2024-0001', '示波器', 11, 'DS1054Z 50MHz 四通道', '2024-01-20', 12000.00, 10800.00, 'IN_USE', '电气学院实验楼501', 2, 5, '赵老师', '实验用示波器'),
('EE-2024-0002', '万用表', 12, 'Fluke 17B+', '2024-02-15', 1500.00, 1350.00, 'IDLE', '电气学院实验楼502', 2, NULL, '赵老师', '数字万用表'),
('ME-2024-0001', '办公桌', 7, '1.4米实木办公桌', '2024-01-10', 1200.00, 1080.00, 'IN_USE', '机械学院办公楼301', 3, 6, '刘老师', '办公用桌');
