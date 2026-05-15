# DecentralShare - 去中心化信息分享平台

一款基于 Android 的真正去中心化信息分享应用，完整支持区块链、P2P 网络、分布式存储和混合共识机制。

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
- **挖矿获得 25 金币奖励**

### 4. 互动评价
- 点赞功能
- 评论功能
- 查看帖子详情

### 5. 区块链与共识机制（完整实现）
- **PoW + PoS 混合共识机制**
  - 工作量证明：挖矿需要计算符合难度要求的哈希
  - 权益证明：金币越多，挖矿难度越低
  - 动态难度调整：根据出块时间自动调整

- **区块链数据结构**
  - 创世区块
  - 交易（Transaction）：包含类型、金额、签名
  - 区块（Block）：包含交易列表、哈希、nonce、难度
  - 默克尔树（Merkle Tree）验证

- **完整验证机制**
  - 交易签名验证
  - 区块哈希验证
  - 工作量证明验证
  - 区块链完整性验证
  - 分叉解决（最长链原则）

### 6. 去中心化网络
- **P2P 网络通信**（Google Nearby Connections API）
- **分布式 DHT 存储**
- **数据同步与广播**
- **节点发现与连接**

## 技术栈

- **语言**：Java
- **最低 SDK**：API 24 (Android 7.0)
- **目标 SDK**：API 34 (Android 14)
- **UI 框架**：Material Design Components
- **架构**：MVVM 模式
- **数据存储**：SharedPreferences + Gson
- **P2P 网络**：Google Nearby Connections
- **加密库**：BouncyCastle

## 项目结构

```
app/
├── src/main/
│   ├── java/com/decentralshare/app/
│   │   ├── adapter/          # 适配器
│   │   │   ├── CommentAdapter.java
│   │   │   └── PostAdapter.java
│   │   ├── blockchain/       # 区块链核心
│   │   │   ├── Block.java    # 区块（含 PoW/PoS 挖矿）
│   │   │   ├── Blockchain.java # 区块链管理
│   │   │   └── Transaction.java # 交易模型
│   │   ├── crypto/           # 加密模块
│   │   │   └── CryptoManager.java
│   │   ├── data/             # 数据管理
│   │   │   └── DataManager.java
│   │   ├── model/            # 数据模型
│   │   │   ├── Comment.java
│   │   │   ├── Post.java
│   │   │   └── User.java
│   │   ├── p2p/              # P2P 网络
│   │   │   └── P2PNetworkManager.java
│   │   ├── storage/          # 分布式存储
│   │   │   └── DistributedStorage.java
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

## 共识机制详解

### 混合共识（PoW + PoS）

**工作量证明（Proof of Work）：**
- 挖掘要求：找到一个 nonce，使得区块哈希的前 N 位为 0
- 默认难度：N=3
- 动态调整：每 10 个区块根据平均出块时间调整难度
- 难度范围：2~5

**权益证明（Proof of Stake）：**
- 用户金币越多，挖矿难度越低
- 每 100 金币降低 1 级难度
- 最低难度：N=1

**挖矿奖励：**
- 每挖出一个新区块奖励 25 金币
- 奖励直接记录在区块链上

### 区块链特性

- **区块大小**：最多 5 笔交易
- **创世区块**：自动创建，没有前序区块
- **默克尔树**：快速验证交易完整性
- **分叉解决**：采用最长链原则
- **UTXO 模型**：记录所有账户余额

## 快速开始

### 环境要求
- Android Studio Electric Eel (2022.1.1) 或更高版本
- JDK 8 或更高版本
- Android SDK API 33 (建议)

### 推送到 GitHub
1. 在 GitHub 创建仓库 `DecentralShare`
2. 在项目根目录执行：
```bash
# 添加远程仓库
git remote add origin https://github.com/go887766/DecentralShare.git

# 或者使用 token 认证
git remote set-url origin "https://go887766:YOUR_TOKEN@github.com/go887766/DecentralShare.git"

# 推送代码
git push -u origin main
```

### 使用 Android Studio 构建 APK
1. 使用 Android Studio 打开项目（等待 Gradle 同步完成）
2. 菜单：Build -> Build Bundle(s) / APK(s) -> Build APK(s)
3. 等待构建完成后，APK 文件位于：
   - Debug APK：`app/build/outputs/apk/debug/app-debug.apk`
4. 或者使用命令行：
```bash
# 构建 Debug APK
./gradlew assembleDebug

# 构建 Release APK
./gradlew assembleRelease
```

### 编译运行
1. 使用 Android Studio 打开项目
2. 等待 Gradle 同步完成
3. 连接 Android 设备或启动模拟器
4. 点击 Run 按钮运行应用

## 去中心化架构说明

### 完整去中心化实现（已完成）

**P2P 网络层（已实现）：**
- Google Nearby Connections API 实现节点发现
- P2P 连接与通信
- 数据同步与广播机制

**分布式存储（已实现）：**
- DHT 路由表
- 内容哈希存储
- 本地持久化

**区块链与共识（已实现）：**
- PoW + PoS 混合共识
- 完整的区块链数据结构
- 交易签名与验证
- 创世区块自动创建

### 技术亮点

1. **节点独立运行：
- 每个应用实例都是一个完整节点
- 包含完整的区块链账本
- 本地挖矿功能

2. **防篡改设计：
- 每个区块都包含前序哈希
- 交易签名验证
- Merkle 树完整性验证

## 贡献指南

欢迎提交 Issue 和 Pull Request！

## 许可证

MIT License
