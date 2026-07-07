# NEPU_FAMS

东北石油大学2026小学期SpringBoot后端项目，固定资产管理系统FAMS（Fixed Asset Management System）

## 项目简介

NEPU_FAMS 是一款基于多人协同的高校固定资产管理系统，面向校级管理员、学院管理员、普通师生三类用户，提供资产全生命周期管理。

## 技术栈

### 后端
- **框架**: Spring Boot 4.1.0
- **语言**: Java 17
- **ORM**: MyBatis-Plus 3.5.7
- **数据库**: MySQL 8.0+
- **权限认证**: Sa-Token 1.38.0
- **工具库**: Hutool 5.8.32, EasyExcel 4.0.2

### 前端
- **框架**: Vue 3 + Vite 5
- **UI组件**: Element Plus
- **状态管理**: Pinia
- **路由**: Vue Router 4
- **数据可视化**: ECharts
- **样式**: Sass

## 核心功能

| 模块 | 功能说明 |
|------|---------|
| 用户认证与权限管理 | 三级角色权限控制、数据隔离 |
| 学院管理 | 学院组织机构维护 |
| 资产分类管理 | 多级树形分类结构 |
| 固定资产台账管理 | 资产登记、查询、修改、导出 |
| 资产领用与归还审批 | 三级审批流程（学院初审、校级终审、用户确认） |
| 资产报修与维修工单 | 报修提交、派单、进度跟踪 |
| 资产报废协同审核 | 报废申请与审批 |
| 多人协同资产盘点 | 多人同时录入、自动差异比对 |
| 数据统计与报表 | 多维度统计、可视化图表 |
| 操作日志与审计 | 全操作留痕、可追溯 |

## 快速开始

### 环境要求
- JDK 17+
- Node.js 16+
- MySQL 8.0+
- Maven 3.6+

### 启动步骤

1. **数据库初始化**
```sql
CREATE DATABASE nepu_fams DEFAULT CHARACTER SET utf8mb4;
```
执行 `src/main/resources/db/schema.sql` 和 `src/main/resources/db/data.sql`

如需使用最新的测试数据（8个学院、22个资产分类、89台设备、25个用户），可额外执行 `src/main/resources/db/update_data.sql`

2. **配置数据库**
编辑 `src/main/resources/application.properties`，修改数据库密码

3. **启动后端**
```bash
mvn spring-boot:run
```
后端地址: http://localhost:8088

4. **启动前端**
```bash
cd frontend
npm install
npm run dev
```
前端地址: http://localhost:5173

## 初始账号

### 超级管理员
| 账号 | 密码 | 角色 |
|------|------|------|
| admin | admin123 | 超级管理员 |

### 学院管理员
| 账号 | 密码 | 角色 | 所属学院 |
|------|------|------|---------|
| geo_admin | 123456 | 学院管理员 | 地球科学学院 |
| pe_admin | 123456 | 学院管理员 | 石油工程学院 |
| chem_admin | 123456 | 学院管理员 | 化学化工学院 |
| me_admin | 123456 | 学院管理员 | 机械科学与工程学院 |
| ee_admin | 123456 | 学院管理员 | 电气信息工程学院 |
| cs_admin | 123456 | 学院管理员 | 计算机与信息技术学院 |
| civil_admin | 123456 | 学院管理员 | 土木建筑工程学院 |
| em_admin | 123456 | 学院管理员 | 经济管理与人文学院 |

### 普通用户
每个学院2名普通用户，账号格式为 `{学院代码}_user1`、`{学院代码}_user2`，密码均为 `123456`。

## 项目结构

```
NEPU_FAMS/
├── src/main/java/xyz/wolegelei/nepu_fams/  # 后端代码
├── src/main/resources/                      # 配置文件
│   └── db/                                  # 数据库脚本
├── frontend/                                # 前端代码
├── pom.xml                                  # Maven配置
├── 数据库数据记录.md                        # 数据库数据文档
└── 模块介绍与使用说明.md                    # 详细使用文档
```

## 文档说明

- 详细的模块介绍与使用说明请查看 [模块介绍与使用说明.md](模块介绍与使用说明.md)
- 数据库表结构与测试数据请查看 [数据库数据记录.md](数据库数据记录.md)