USE nepu_fams;

ALTER TABLE asset
    ADD COLUMN model_url VARCHAR(500) NULL COMMENT '3D模型文件相对路径' AFTER responsible_person;

CREATE TABLE IF NOT EXISTS sys_3d_scene (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '3D场景ID',
    scene_name VARCHAR(100) NOT NULL COMMENT '场景名称',
    scene_layout_json JSON COMMENT '场景布局JSON',
    college_id BIGINT COMMENT '所属学院ID',
    create_by BIGINT COMMENT '创建人ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_college_id (college_id),
    INDEX idx_create_by (create_by)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='3D场景表';
