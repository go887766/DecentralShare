# DecentralShare - 去中心化信息分享平台

一款基于 Android 的去中心化信息分享应用，支持用户认证、内容发布、互动评价和金币系统。

## 功能特性

### 1. 用户系统
- 用户注册：生成唯一地址和钱包
- 用户登录：使用地址和密码登录
- 用户中心：查看个人信息、金币余额和发布的内容

### 2. 内容发布
- 支持文字内容发布
- 图片和视频上传功能（预留接口）
- 发帖获得金币奖励

### 3. 金币系统
- 注册赠送 100 金币
- 发布内容获得 10 金币
- 评论获得 2 金币
- 点赞他人内容，作者获得 1 金币

### 4. 互动评价
- 点赞功能
- 评论功能
- 查看帖子详情

### 5. 去中心化设计理念
- 用户身份由唯一地址标识（非区块链钱包，模拟设计）
- 本地优先的数据存储
- 支持 P2P 网络扩展架构

## 技术栈

- **语言**：Java
- **最低 SDK**：API 24 (Android 7.0)
- **目标 SDK**：API 34 (Android 14)
- **UI 框架**：Material Design Components
- **架构**：MVVM 模式
- **数据存储**：SharedPreferences + Gson

## 项目结构

```
app/
├── src/main/
│   ├── java/com/decentralshare/app/
│   │   ├── adapter/          # 适配器
│   │   │   ├── CommentAdapter.java
│   │   │   └── PostAdapter.java
│   │   ├── data/             # 数据管理
│   │   │   └── DataManager.java
│   │   ├── model/            # 数据模型
│   │   │   ├── Comment.java
│   │   │   ├── Post.java
│   │   │   └── User.java
│   │   ├── ui/               # 界面
│   │   │   ├── LoginActivity.java
│   │   │   ├── MainActivity.java
│   │   │   ├── PostDetailActivity.java
│   │   │   ├── RegisterActivity.java
│   │   │   ├── ShareActivity.java
│   │   │   └── UserCenterActivity.java
│   │   └── util/             # 工具类
│   │       └── CryptoUtil.java
│   └── res/                  # 资源文件
```

## 快速开始

### 环境要求
- Android Studio Hedgehog (2023.1.1) 或更高版本
- JDK 8 或更高版本
- Android SDK API 34

### 编译运行
1. 克隆项目到本地
2. 使用 Android Studio 打开项目
3. 等待 Gradle 同步完成
4. 连接 Android 设备或启动模拟器
5. 点击 Run 按钮运行应用

## 去中心化架构说明

### 当前实现（单机版）
当前版本使用本地存储模拟去中心化特性：
- 用户地址采用随机生成的唯一标识
- 所有数据本地加密存储
- 金币系统通过本地规则实现

### 未来扩展方向
1. **P2P 网络层**
   - 集成 Wi-Fi Direct 或 Nearby Connections
   - 节点发现与通信
   - 数据同步机制

2. **分布式存储**
   - 内容分片存储
   - DHT 网络路由
   - 数据冗余备份

3. **共识机制**
   - 简单的 PoS (Proof of Stake)
   - 内容验证投票
   - 防篡改设计

## 贡献指南

欢迎提交 Issue 和 Pull Request！

## 许可证

MIT License
