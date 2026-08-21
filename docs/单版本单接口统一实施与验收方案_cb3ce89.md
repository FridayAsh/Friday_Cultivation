# Friday_Cultivation 单版本、单 Interface 统一实施与验收方案

> 基线提交：`cb3ce89f7deae3165ea35e01567b180731c21138`
> 实施分支：`tribulation-rework`
> Minecraft / Forge：1.20.1 / 47.2.0
> 文档状态：实施主文档
> 适用范围：从基线冻结、问题修复、数据迁移、接口收敛、回归验证直到最终验收通过

## 1. 文档目的

本文档是 `Friday_Cultivation` 从 `cb3ce89f7deae3165ea35e01567b180731c21138` 开始执行“一个系统只有一个版本、每项业务只有一个权威数据源、所有调用者使用同一个 Interface”的唯一实施与验收依据。

本文档同时完整收录《`cb3ce89_全项目隐藏问题与接口统一审计.md`》内容。实施过程中不得只处理审计中举出的单个现象；必须修复导致该现象的 Module、Interface、Seam、Adapter、生命周期和数据契约问题。

最终目标不是让项目“暂时能运行”，而是让以下事实可以被源码结构、自动测试和部署产物共同证明：

1. 同一业务状态只有一个权威所有者。
2. 同一业务动作只有一个写入 Interface。
3. 同一领域概念只有一套解释规则。
4. 旧实现迁移完成后被删除，不与新实现叠加。
5. 登录、退出、死亡、重生、切维度、卸载和重启不会改变业务语义。
6. 客户端只能提出请求，服务端始终负责权限校验和最终计算。
7. 每条核心规则都有自动测试和最终实机验收证据。

## 2. 强制术语

本文档统一使用以下术语：

- **Module**：拥有一个 Interface 和一套 Implementation 的业务模块，可以是类、包或跨层业务切片。
- **Interface**：调用者正确使用 Module 必须知道的全部内容，包括方法、输入、输出、不变量、顺序约束、错误模式和性能特征。
- **Implementation**：Interface 内部隐藏的实现细节。
- **Seam**：Module 的 Interface 所在位置；调用者只允许跨越该位置使用 Module。
- **Adapter**：位于 Seam 上、把物品、网络、界面或 Forge 事件转换成统一 Interface 请求的具体实现。
- **权威状态**：决定游戏结果的唯一数据。缓存、客户端镜像、展示模型和临时计算结果都不是权威状态。
- **派生状态**：可以由权威状态确定性计算得到的数据，不得再被保存成第二份权威状态。

后续代码、注释和文档不再使用“新版/旧版实现”描述并存逻辑。迁移期可以存在仅用于读取旧存档的内部 Adapter，但它不得成为公开 Interface，也不得参与新数据写入。

## 3. 不可违反的项目规范

### 3.1 单一所有者

每个业务字段必须有且只有一个 Module 负责写入。禁止同一状态同时作为权威数据保存在：

- Capability；
- Player/Entity PersistentData；
- 静态 Map/Set；
- SynchedEntityData；
- NBT 独立字段；
- 客户端本地缓存。

允许同步镜像和缓存，但必须满足：

1. 只能从权威状态生成；
2. 丢失后可以重建；
3. 不参与服务端业务判断；
4. 有明确失效和清理时机。

### 3.2 单一写入 Interface

同一业务允许有多个入口，但入口只能是 Adapter。例如令牌、管理界面、突破事件都可以请求改变境界，但必须全部调用 `RealmTransition.apply(...)`。Adapter 禁止自行复制真元、悟道、散仙计时、生命刷新、同步等 Implementation。

### 3.3 稳定身份与明确顺序

以下场景禁止使用 `enum.ordinal()`、`Enum.values()[index]` 或声明顺序：

- NBT 持久化；
- 网络协议；
- 境界等级比较；
- 境界前后关系；
- 奖励激活阈值；
- 修为/真元成长曲线；
- NPC 境界生成和成长；
- UI 业务排序。

必须使用稳定 ID 和显式领域顺序。旁支必须显式建模，不能通过插入主链或特殊 ordinal 模拟。

### 3.4 完整生命周期

每个玩家、实体、区块和世界状态都必须声明并测试：

- 创建；
- 首次加载；
- 登录；
- 退出；
- 死亡与 Clone；
- 重生；
- 维度切换；
- 实体/区块/世界卸载；
- 服务器关闭；
- 服务器重启；
- 转世或业务主动清理。

### 3.5 字段持久化矩阵

`CultivationData` 及其他持久 Module 的每个字段必须在字段矩阵中明确：

| 属性 | 含义 |
|---|---|
| owner | 唯一所属 Module |
| persisted | 是否写入 NBT |
| cloned | 死亡 Clone 是否复制 |
| synced | 是否同步客户端 |
| resetOnReincarnation | 转世是否清理 |
| resetOnRealmTransition | 境界切换是否清理 |
| default | 新档默认值 |
| migration | 旧档迁移方式 |
| derived | 是否为派生状态 |

新增持久字段时必须同时修改矩阵、Codec/NBT、Clone、同步和测试；缺少任何一项不得提交。

### 3.6 存档版本与迁移

玩家、NPC、区块和世界持久数据必须有稳定 `dataVersion`。迁移遵守：

1. 旧数据只读一次并转换成当前结构；
2. 新逻辑只写当前结构；
3. 禁止同时继续写旧字段和新字段；
4. 无法无损转换时必须记录日志并采用文档规定的确定策略；
5. 禁止静默归零、`floorMod` 映射或把非法值变成另一个合法业务值；
6. 迁移必须幂等，重复加载不会重复发奖或重复扣除。

### 3.7 原子业务事务

突破、渡劫、境界切换、转世、飞行启停等跨字段动作必须遵守：

1. 校验请求和权限；
2. 读取旧状态；
3. 在修改前创建所需快照；
4. 计算完整结果；
5. 一次性提交权威数据；
6. 刷新派生属性；
7. 修正当前生命、灵气和状态；
8. 同步客户端；
9. 最后发送事件、消息、音效和粒子。

计算失败时不得留下半套状态。

### 3.8 服务端权威

每个 C2S Packet 必须重新校验：

- 玩家身份与权限；
- 持有物品或当前菜单；
- 距离与目标有效性；
- 冷却与频率；
- 数值范围；
- 当前状态机阶段；
- 服务端配置；
- 资源消耗是否足够。

客户端只发送意图或选择，不发送最终属性、伤害、奖励和权威状态。

### 3.9 事件和初始化幂等

- 注解自动注册与手动注册只能选择一种。
- 初始化方法必须有唯一调用点。
- 公开注册表不得允许相同内置项重复添加。
- 事件处理必须明确服务端/客户端、START/END Phase 和优先级。
- 同一 tick 重复调用不得导致重复扣费、重复发奖或重复物品转移。

### 3.10 客户端 Seam

通用 Module 不得直接引用 `Minecraft`、Screen、Renderer、LocalPlayer 等客户端类型。客户端行为通过专门客户端 Adapter 接入；专用服务器加载通用类时不得解析客户端符号。

### 3.11 替换而不叠加

每个重构阶段必须在同一阶段完成：

1. 建立新 Interface；
2. 为 Interface 建立测试；
3. 迁移该范围内全部调用者；
4. 删除旧写入口、旧字段、旧 Helper 和失效注释；
5. 加入静态检查，阻止旧形式重新出现。

禁止为了“暂时兼容”长期保留固定返回 0/false/PURE 的公开方法。

### 3.12 构建与部署可追溯

- 每次源代码修改必须 `clean build` 或按风险执行等价验证。
- `build/libs` 与 `jars/Friday_Cultivation-0.1.0.jar` 内容必须一致。
- JAR 必须包含或可追溯到 Git 提交哈希。
- 源码、迁移、测试、文档和 JAR 同一提交交付。
- 只在 `tribulation-rework` 实施；没有用户要求不得创建新分支。
- Commit 必须使用中文，详细记录根因、改动、迁移和验证结果。

## 4. 基线与非目标

### 4.1 基线事实

- 基线提交：`cb3ce89f7deae3165ea35e01567b180731c21138`。
- Java 文件：492。
- 资源文件：2829。
- 网络消息：88 个唯一注册类。
- `clean build` 成功。
- `compileTestJava` 与 `test` 当前为 `NO-SOURCE`。
- 仓库 JAR 与该基线重新构建 JAR 的 3627 个条目内容一致。
- 完整审计发现 P0 8 项、P1 8 项、P2 7 项，共 23 类问题。

### 4.2 非目标

本轮统一工作不授权以下改动：

- 无关玩法重平衡；
- 无关 GUI 重做；
- 无关资源替换；
- 为了重构便利改变既定境界名称或玩法规则；
- 创建第二套“临时新版”并长期保留旧实现；
- 跳过存档迁移，以清档作为解决办法；
- 用客户端限制代替服务端校验。

若修复必须决定尚未明确的玩法数值，应先把选择写入本文档的“决策记录”，再实现，不得在代码中隐式决定。

## 5. 目标 Module 与唯一 Interface

### 5.1 RealmTopology

建议 Seam：`cultivation/realm/RealmTopology.java`

唯一职责：

- 根据稳定 ID 解析境界；
- 返回主链顺序；
- 表示散仙等旁支；
- 提供稳定等级比较；
- 提供主链前后境界；
- 提供 UI 顺序；
- 提供持久化/网络安全解析。

建议最小 Interface：

```java
Realm require(String stableId);
Optional<Realm> find(String stableId);
RealmRelation relationOf(Realm realm);
boolean isAtLeast(Realm actual, Realm required);
Optional<Realm> nextMain(Realm realm);
Optional<Realm> previousMain(Realm realm);
List<Realm> mainChain();
List<Realm> selectionOrder();
```

`RealmRelation` 明确 `MAIN`、`LOOSE_IMMORTAL_BRANCH` 等关系。普通调用者不得读取内部索引。

### 5.2 RealmTransition

建议 Seam：`cultivation/realm/RealmTransition.java`

唯一写入口：

```java
RealmTransitionResult apply(ServerPlayer player, RealmTransitionRequest request);
```

`RealmTransitionRequest` 必须包含原因枚举，例如：

- `BREAKTHROUGH_SUCCESS`
- `TRIBULATION_FAILURE`
- `REALM_TOKEN`
- `REALM_SELECTOR`
- `ADMIN_EDIT`
- `REINCARNATION`

Implementation 内部统一完成子阶段、真元、悟道、散仙、奖励激活、法术、当前生命/灵气、同步和消息所需结果。Adapter 不得再直接调用 `setRealm()+setSubStage()+...`。

### 5.3 PlayerProgressData

建议 Seam：继续由 `CultivationData` 承担，但缩小公开写方法，只暴露高层命令和稳定查询。

职责：

- 玩家修炼权威状态；
- 当前数据版本；
- NBT 往返；
- Clone；
- 客户端同步快照；
- 转世清理；
- 旧档迁移。

外部 Helper 不得直接维护与其重复的 PersistentData。

### 5.4 TribulationSession

建议 Seam：`event/tribulation/TribulationSession.java` 与 `TribulationController.java`

`TribulationSession` 是一次正在进行的渡劫的唯一运行状态，至少包含：

- session ID；
- 来源境界稳定 ID和子阶段稳定 ID；
- 目标境界稳定 ID和子阶段稳定 ID；
- 完整 `TribulationSpec`；
- 当前波、当前道、冷却；
- 是否散仙劫；
- 启动时天骄档位；
- 启动时奖励计算输入快照；
- 数据版本。

伤害、间隔、事件、客户端显示、存档恢复只能读取同一 Session。Realm、Packet 和 Handler 不得分别保存或重新推导运行参数。

### 5.5 TribulationRewardSnapshot

建议 Seam：`event/tribulation/TribulationRewardLedger.java`

每条奖励是固定值，不保存活动百分比乘数：

```java
record TribulationRewardSnapshot(
    String rewardKey,
    String sourceRealmId,
    String unlockRealmId,
    String unlockSubStageId,
    long healthBonus,
    long maxQiBonus,
    int constitutionBonus,
    int physiqueBonus,
    int agilityBonus,
    int spellPowerBonus,
    int qiSeaBonus,
    String tierId,
    int dataVersion
) {}
```

强制规则：

- 在境界变化和普通突破奖励应用前获取快照；
- 按渡劫时属性计算固定增量；
- 一项属性只能在一个结算层应用一次；
- 当前完整进度低于 `unlockRealmId + unlockSubStageId` 时停用，但不破坏历史记录；
- 相同突破里程碑使用稳定 `rewardKey`，重新完成时替换该条记录，不重复叠加；
- 是否包含某一维度必须在奖励维度表明确，禁止注释和 Implementation 不一致；
- 禁止保存 Realm ordinal。

旧 `List<double[]>` 迁移时，由首次加载迁移 Adapter 将当时可观察到的旧效果固化成固定奖励并记录 `legacyMigrated=true`；不得静默删除旧奖励。

### 5.6 FlightState

建议 Seam：`flight/CultivationFlight.java`

唯一权威数据保存在 `CultivationData` 的 FlightState 中：

- 飞行模式；
- 御剑 ItemStack；
- 原物品栏槽位；
- 灵气扣费累计；
- 启动状态；
- 恢复策略。

静态 Map 只能作为无权威、可丢弃的客户端视觉缓存；服务端不得依赖它归还物品。登录、退出、死亡、重启时必须从 Capability 恢复或安全归还物品。

### 5.7 ServerAuthorization

建议 Seam：`network/ServerAuthorization.java`

统一提供开发令牌、管理编辑、菜单操作和移动类 Packet 的权限判断、频率限制与拒绝原因。Packet 只是 Adapter，不能各自发明权限规则。

## 6. 实施总顺序

严格按阶段执行。除“紧急止血阶段”外，不得跨阶段同时维护两套权威状态。

```text
基线冻结
  ↓
紧急止血与测试骨架
  ↓
RealmTopology 唯一顺序
  ↓
PlayerProgressData 数据契约与迁移
  ↓
RealmTransition 唯一写入口
  ↓
TribulationSession 唯一运行态
  ↓
TribulationRewardSnapshot 固定奖励
  ↓
FlightState 唯一状态
  ↓
突破/锻体/元素/极境等剩余双源清理
  ↓
网络、客户端 Seam、缓存生命周期清理
  ↓
删除全部旧 Interface 与静态规则固化
  ↓
全量构建、迁移、客户端/集成/专服验证
  ↓
最终验收通过
```

## 7. 分阶段实施细则

## 阶段 0：冻结基线并建立证据

### 目标

确保所有后续修改都可证明基于 `cb3ce89`，并可以逐阶段回滚。

### 操作

1. 确认分支为 `tribulation-rework`。
2. 确认父提交为 `cb3ce89f7deae3165ea35e01567b180731c21138`。
3. 执行 `gradlew clean build`。
4. 记录 Java、资源、网络注册数量。
5. 保存两个测试存档副本：
   - 新建空白存档；
   - 含境界、渡劫奖励、御剑状态、NPC、区块灵气的旧档。
6. 记录基线 JAR 内容哈希和部署位置。
7. 不创建新分支。

### 完成条件

- 工作树干净；
- 基线构建成功；
- 存档测试夹具可重复使用；
- 所有后续 Commit 都以该提交为祖先。

## 阶段 1：紧急止血与测试骨架

关联：P0-1、P0-2、P0-5。

### 必须先修复

1. 删除 `CultivationFlightEvents` 的第二次注册，只保留注解或手动注册其中一种。
2. RealmSelection/EditPlayerStats 在服务端加入权限校验，未授权请求不修改任何状态。
3. 在完整 FlightState 重构前，登录时不得直接清空可归还的御剑 ItemStack；必须安全归还。

### 测试骨架

新增：

- `src/test/java`：纯计算与 NBT Codec 测试；
- Forge GameTest：玩家生命周期、境界转换、网络授权；
- 静态验证任务：禁止 Realm ordinal 业务使用、重复事件注册和通用包客户端引用。

### 完成条件

- 飞行每秒只扣费一次预期周期；
- 未授权 Packet 被拒绝；
- 服务器重启后御剑物品不丢；
- 测试任务不再是 `NO-SOURCE`。

## 阶段 2：RealmTopology 成为唯一境界解释

关联：P0-3、P1-7。

### 操作

1. 建立 RealmTopology 和稳定主链/旁支模型。
2. 为 21 境界写顺序表测试。
3. 迁移全部等级比较到 `isAtLeast` 等 Interface。
4. 迁移真元、修为、寿命、法术解锁、NPC 生成、勾魂使成长。
5. NPC NBT 改写稳定 `realmId`，读取旧 `realmOrd` 时通过版本化映射迁移。
6. 网络和 UI 传稳定 ID，不传 Realm ordinal。
7. 渡劫奖励阈值暂时通过 RealmTopology 解释，等待阶段 6 固定快照替换。
8. 删除 `LOGICAL_ORDER` 与 `next/prev` 中重复的顺序定义，或让它们成为 RealmTopology 内部唯一数据的简单委托；外部调用者只用 RealmTopology。

### 必须覆盖的顺序测试

- 主链：凡人→锻体→练气→筑基→金丹→元婴→化神→炼虚→合道→大乘→渡劫→真仙→玄仙→仙君→仙尊→仙王→半圣→圣人→半帝→大帝。
- 散仙为渡劫失败旁支，不是主链 `nextMain` 的一步。
- 玄仙低于半圣，仙王低于半圣。
- 真仙奖励在散仙状态下不被错误视为已达到。
- 勾魂使者成长包含新增四个仙境。

### 完成条件

- 业务源码不存在 Realm ordinal 等级比较、持久化和协议传输；
- 所有境界顺序测试通过；
- 旧 NPC 存档迁移后境界不变。

## 阶段 3：PlayerProgressData 数据契约闭合

关联：P0-6、P1-3、P2-5。

### 操作

1. 为 CultivationData 增加 `dataVersion`。
2. 建立字段持久化矩阵并作为仓库文档维护。
3. 让 NBT、Clone 和同步使用同一个内部 Codec/快照模型，避免三套字段清单手工漂移。
4. 补齐：
   - breakthroughHpBonus；
   - breakthroughQiBonus；
   - daoFruitTotalEaten；
   - 渡劫奖励账本；
   - 渡劫 Session；
   - FlightState。
5. 明确临时字段，如 pending UI 消息和随机源，不参与持久化并写入矩阵理由。
6. 为 QiFieldRegistry 增加 Level unload/Server stopped 清理。

### 测试

- 所有持久字段 NBT 往返相等；
- Clone 后应继承字段相等；
- 转世清理字段符合矩阵；
- 未知 ID 产生明确回退和日志，不映射成随机合法值；
- 迁移执行两次结果一致。

### 完成条件

- 字段矩阵无未分类字段；
- NBT/Clone/同步自动测试通过；
- 不再出现一半渡劫状态被恢复。

## 阶段 4：RealmTransition 成为唯一写入口

关联：P0-4、P0-5、P1-4。

### 操作

1. 建立 RealmTransitionRequest/Result/Reason。
2. 把以下调用者迁移为 Adapter：
   - RealmTokenItem；
   - RealmSelectorTokenItem/RealmSelectionPacket；
   - StatEditorScreen/EditPlayerStatsPacket；
   - 普通突破；
   - 渡劫成功；
   - 渡劫失败；
   - 散仙选择；
   - 转世。
3. 在 RealmTransition 内统一处理：
   - Realm/SubStage；
   - 真元基线；
   - 悟道；
   - 修为；
   - 散仙状态与计时；
   - 法术解锁；
   - 奖励激活；
   - Attribute 刷新；
   - 当前生命/灵气；
   - 同步结果。
4. 限制或删除外部可直接调用的 `setRealm`、`setSubStage`。

### 测试

对同一目标状态，从令牌、选择器、管理编辑三种 Adapter 发起后，最终 CultivationData 必须相同；只有权限、消息和原因允许不同。

### 完成条件

- 搜索不到 Adapter 内的复制转换链；
- 所有入口调用唯一 RealmTransition；
- 突破后真正满血、满灵气；
- 失败时无半套状态。

## 阶段 5：TribulationSession 成为唯一运行态

关联：P0-7、P1-1、P1-2。

### 操作

1. Realm/路线选择只负责生成一次未缩放计划。
2. TribulationController 根据天骄档位生成最终完整 Spec。
3. 启动时创建唯一 Session 并持久化。
4. Handler、事件、云效果、伤害、防御链和客户端显示全部读取 Session。
5. 合并两套权重，只保留实际使用的一套配置来源。
6. 删除未使用常量或接入唯一配置。
7. 散仙劫配置只由一个 Module 提供。
8. 删除三整数公开启动 Interface；内部迁移 Adapter 在同阶段移除。
9. `clearTribulation()` 一次清理 Session，不残留 type/ratio。

### 一致性测试

- Started 事件的波数、道数、伤害与真实执行一致；
- 筑基/金丹路线使用其选定 Spec；
- 缩放后的值在事件、客户端和伤害中一致；
- 退出重进后继续同一 Session；
- 完成/失败后 Session 完整清理；
- 散仙劫与普通劫不会互相继承状态。

### 完成条件

- `CultivationData.startTribulation(TribulationSpec)` 或等价唯一 Interface 是唯一入口；
- 不再从 Realm 重新构造活动 Session；
- 旧三整数 Interface 删除。

## 阶段 6：固定快照渡劫奖励账本

关联：P0-8。

### 结算顺序

以“练气突破筑基”为例：

1. 玩家仍处于练气时读取渡劫前权威属性。
2. 计算天骄档位和奖励比例。
3. 把每个受支持维度计算成固定增量。例如渡劫前最终生命为 100、奖励比例 50%，则该条记录的固定生命增量为 50。
4. 记录该奖励对应的目标境界和目标子阶段稳定 ID（筑基初期）。
5. 执行 RealmTransition 进入筑基。
6. 通过唯一奖励账本应用固定增量。
7. 当前境界或同境界子阶段低于筑基初期时该条奖励停用；重新达到该完整进度时按账本规则恢复。
8. 相同突破里程碑重修时替换该里程碑记录，禁止利用令牌反复叠加。

普通突破累计生命/灵气奖励也必须保存最新目标子阶段，不能只保存目标境界；否则同境界中期降到初期时仍会错误生效。

### 奖励维度表

实施前必须在代码和本文档中锁定每个维度的唯一应用层：

| 维度 | 快照来源 | 固定值存储 | 应用位置 | 禁止重复位置 |
|---|---|---|---|---|
| 最大生命 | 渡劫前最终有效最大生命 | healthBonus | 单一 Attribute ADDITION | 真元 Helper 与总生命倍率二次应用 |
| 最大灵气 | 渡劫前最终有效最大灵气 | maxQiBonus | CultivationData 唯一上限计算 | 额外百分比乘数 |
| 体质 | 渡劫前权威点数 | constitutionBonus | 奖励账本查询层 | 修改原始可分配点数并再次乘生命 |
| 筋骨 | 渡劫前权威点数 | physiqueBonus | 奖励账本查询层 | 多 Helper 重复乘算 |
| 身法 | 渡劫前权威点数 | agilityBonus | 奖励账本查询层 | 注释称有、代码却排除 |
| 法伤 | 渡劫前权威点数 | spellPowerBonus | 奖励账本查询层 | 多法伤乘数重复应用 |
| 气海 | 渡劫前权威点数 | qiSeaBonus | 奖励账本查询层 | 再对最终灵气乘同一奖励 |

如果最终设计明确某维度不参与，必须存 0、写清理由并用测试固定，不能只在某个 Helper 中悄悄排除。

### 迁移

旧 `tribulationBonus` 列表缺少历史属性快照，不能重建真实历史值。迁移采用：

1. 首次加载时读取旧百分比和旧最低境界；
2. 使用旧实现当时可观察到的有效结果生成一次固定增量；
3. 用稳定 `legacy:<index>` rewardKey 写入新账本；
4. 写入迁移版本并停止写旧列表；
5. 输出一次迁移日志；
6. 后续加载只读新账本。

### 测试

- 100×50%=50 的固定生命示例；
- 境界或同境界子阶段下降时停用、回升时恢复；
- 不同奖励条目只相加固定值，不连乘；
- 同一里程碑重修不重复叠加；
- 死亡、重登、重启后数值不变；
- 体质生命不会被奖励计算两次；
- 旧档迁移一次且幂等。

### 完成条件

- `List<double[]>`、`activeTribulationMultiplier()` 和 ordinal 阈值删除；
- 所有受支持维度都只应用一次；
- 固定快照回归测试全部通过。

## 阶段 7：FlightState 唯一化

关联：P0-1、P0-2。

### 操作

1. Capability FlightState 成为服务端唯一权威状态。
2. 删除服务端 `SWORD_FLIGHT`、`SWORD_FLIGHT_SLOT`、`FLIGHT_TICKS` 静态权威 Map。
3. 御剑启停、灵气扣费、物品归还通过唯一 CultivationFlight Interface。
4. 登录、退出、死亡、维度切换、服务器关闭时执行明确恢复策略。
5. 客户端只接收同步镜像并渲染。
6. 事件只注册一次。

### 物品安全不变量

任意时刻必须满足：

```text
玩家物品栏中的剑数量 + FlightState 托管的剑数量 = 启动飞行前数量
```

停止、死亡、退出、崩溃恢复和重启都不能复制或吞掉物品。

### 完成条件

- 所有生命周期测试物品守恒；
- 飞行扣费周期准确；
- 客户端断线不影响服务端恢复；
- 静态玩家状态 Map 删除。

## 阶段 8：剩余双源与未实现玩法收敛

关联：P1-3、P1-4、P1-5、P1-6、P2-1。

### 阶段 8 决策记录（2026-08-21）

- 普通突破加成沿用当前已生效的确定性规则：标准生命值 × 5%，大境界倍率 1.0、小境界倍率 0.35，灵气加成为生命加成的 5 倍；不新增随机源，也不增加未在实现中存在的 2000 封顶，避免改变既有存档的玩法数值。
- 元素灵气旧字段没有真实权威数据源，因此不保留空的元素计数模型；FireSwordAura、蓄力火球和 UI 统一读取当前功法/法术元素接口，旧 Deprecated 空方法删除。
- 练气极境字段、入口和 TODO 从唯一玩家数据模型中删除。该功能在基线中不可达且没有完整规则、效果或资源；本轮不伪造一套新玩法，验收以“无残留字段/入口/调用者”为准。
- 散仙劫的波数、道数和单击伤害只由 `LooseImmortalBonusHelper` 按劫波等级提供；`Realm` 不再保留无法表达等级的第二套散仙配置。

### 8.1 锻体生命

- 选择 `bodyTemperingHpInherited` 固定值作为唯一权威状态。
- 旧 PersistentData 只用于一次迁移。
- 迁移完成后删除 `TAG_MAX_BODY_TEMPERING_LEVEL` 的业务读写。
- 转世、Clone、NBT、境界切换按字段矩阵处理。

### 8.2 普通突破奖励

- 明确真实算法是否包含确定性随机和 2000 封顶。
- 代码、注释、UI 使用同一规则来源。
- 奖励应用完成后再计算并补满最终生命/灵气。

### 8.3 元素灵气

在二选一决策后执行：

1. 若保留元素灵气玩法：建立真实权威元素数据和唯一查询 Interface；
2. 若不保留：迁移 FireSwordAura、ChargeableSpell 和 UI 到 SpellElement/Technique 元素体系，并删除旧 Deprecated 空方法。

禁止继续保留返回固定 0/PURE 的运行 Interface。

### 8.4 练气极境

- 将道果物品接入唯一总数；
- 实现进入极境状态和效果；
- 明确第 10 层是否为 SubStage、标记还是独立状态；
- 完成 NBT、Clone、同步、UI 和测试；
- 若项目决定取消该玩法，则完整删除字段、TODO 和 UI 入口。

### 8.5 区块灵气再生

- 保存小数余量，或只按真正结算的完整时间推进时间戳；
- `peek` 改为无副作用查询，或更名为明确结算方法；
- 用高频查询测试证明再生不会被压制。

## 阶段 9：网络、客户端 Seam 与缓存生命周期

关联：P1-8、P2-4、P2-5、P2-6。

### 操作

1. ShadowStep 增加服务端冷却、频率限制、碰撞检测和安全落点。
2. RealmSelectorTokenItem 通过客户端 Adapter 打开 Screen。
3. 扫描所有通用包客户端引用并迁移。
4. QiFieldRegistry 和所有静态世界缓存增加卸载/停服清理。
5. `ModNetwork.PROTOCOL_VERSION` 成为 Channel 创建的唯一协议来源。
6. 对全部 C2S Packet 建立权限与输入校验表。

### 完成条件

- 专用服务器启动无客户端类加载错误；
- 移动 Packet 不能穿墙或刷包；
- 切换存档没有静态世界状态污染；
- 网络协议只有一个版本来源。

## 阶段 10：删除旧 Interface、文档和静态规则固化

关联：P2-2、P2-3、P2-7。

### 删除或收敛

- CapabilityEvents.registerCapability 重复入口；
- ChunkQiCapability.register 重复入口；
- BlockQiMapping；
- tribulationBoltInterval 空兼容方法；
- ZhenyuanBonusHelper 旧零值 Interface；
- 未使用 Realm.progressIndex；
- 旧渡劫百分比和旧飞行 Map；
- 失效“新版/旧版”注释；
- 与真实实现不一致的 README 描述。

### 静态规则

构建必须自动失败于：

- Realm ordinal 用于业务等级、NBT 或网络；
- 自动注册类再次手动注册；
- 通用包引用客户端类；
- Deprecated 空 Interface 仍有调用者；
- 持久字段未进入字段矩阵；
- C2S 管理 Packet 无授权检查；
- JAR 内容与当前构建不一致。

### 完成条件

- 项目搜索结果不存在已禁止形式；
- README、实施文档、字段矩阵与代码一致；
- 删除旧测试，测试只跨越当前唯一 Interface。

## 阶段 11：全量验证、构建和部署

### 自动验证

必须全部通过：

1. `gradlew clean build`
2. 纯计算测试
3. NBT/Clone/迁移测试
4. Forge GameTest
5. 静态单 Interface 检查
6. 资源注册检查
7. 网络注册唯一性检查
8. JAR 内容一致性检查

### 运行验证

必须完成三种环境：

- 客户端单人集成服务器；
- 本地多人连接；
- Forge 专用服务器。

### 旧档迁移验证

至少验证：

- 基线普通玩家；
- 有普通突破加成玩家；
- 有多条渡劫奖励玩家；
- 正在渡劫玩家；
- 正在御剑玩家；
- 散仙玩家；
- 含旧 realmOrd NPC；
- 有区块灵气和阵法存档。

### 部署

1. 构建成功后复制新 JAR 到仓库 `jars/Friday_Cultivation-0.1.0.jar`。
2. 逐条目验证 `build/libs` 与 `jars/` 内容一致。
3. 源码、测试、迁移、文档和 JAR 一起 Commit。
4. 中文详细 Commit。
5. 推送 `tribulation-rework`。
6. 覆盖部署到指定 PCL Forge 实例。
7. 记录部署 JAR SHA-256 和 Git 提交。

## 8. 审计问题—阶段—验收证据矩阵

| 审计编号 | 主要阶段 | 必须产生的验收证据 |
|---|---:|---|
| P0-1 | 1、7 | 单事件注册静态检查；飞行扣费周期测试 |
| P0-2 | 1、7 | 五类生命周期物品守恒测试 |
| P0-3 | 2 | 21 境界主链/旁支测试；零业务 ordinal 扫描 |
| P0-4 | 4 | 三 Adapter 相同结果测试 |
| P0-5 | 1、4、9 | 未授权 Packet 拒绝测试 |
| P0-6 | 3 | NBT/Clone 字段矩阵测试 |
| P0-7 | 5 | Session 事件与实际运行一致测试 |
| P0-8 | 6 | 固定快照、降境界、重修、重启测试 |
| P1-1 | 5 | 权重唯一来源静态检查 |
| P1-2 | 5 | 散仙 Spec 唯一来源测试 |
| P1-3 | 8 | 锻体旧档迁移与转世测试 |
| P1-4 | 4、8 | 突破后最终满血满灵气测试 |
| P1-5 | 8 | 元素系统决策对应的功能测试；零空调用扫描 |
| P1-6 | 8 | 极境可达与效果测试，或完整删除证明 |
| P1-7 | 2 | NPC realmId 迁移；勾魂使完整成长链测试 |
| P1-8 | 9 | 穿墙、刷包、冷却测试 |
| P2-1 | 8 | 高频查询仍正确再生测试 |
| P2-2 | 10 | Capability 唯一注册入口扫描 |
| P2-3 | 10 | Deprecated 空 Interface 零调用扫描 |
| P2-4 | 9 | 专用服务器启动测试 |
| P2-5 | 3、9 | Level unload/切档清理测试 |
| P2-6 | 9 | 协议版本唯一来源扫描 |
| P2-7 | 10 | README/字段矩阵/实现一致性复核 |

## 9. 最终验收清单

以下项目必须全部打勾，任何一项失败都不得宣布完成：

### 结构与 Interface

- [ ] 一个领域 Module 只有一个公开 Interface。
- [ ] 同一状态只有一个权威所有者。
- [ ] 同一动作只有一个写入 Interface。
- [ ] Adapter 不复制业务 Implementation。
- [ ] 没有长期并存的新版/旧版。
- [ ] 没有运行中的 Deprecated 空 Interface。

### 境界与突破

- [ ] 21 境界主链与散仙旁支完全符合明确顺序。
- [ ] 业务、NBT、网络不存在 Realm ordinal。
- [ ] 所有境界入口委托 RealmTransition。
- [ ] 突破后使用最终上限补满生命和灵气。
- [ ] NPC 与勾魂使使用同一 RealmTopology。

### 渡劫

- [ ] 一次渡劫只有一个 TribulationSession。
- [ ] 事件、显示、伤害与 Session 完全一致。
- [ ] 普通劫和散仙劫不共享第二套配置。
- [ ] 固定快照在境界变化前计算。
- [ ] 奖励固定值只应用一次。
- [ ] 降境界按稳定链位置停用奖励。
- [ ] 重修同里程碑不会重复堆叠。
- [ ] 死亡、重登、重启后奖励不变。

### 数据与生命周期

- [ ] 所有持久字段进入字段矩阵。
- [ ] NBT、Clone、同步和迁移测试全部通过。
- [ ] 御剑物品在所有生命周期保持守恒。
- [ ] 静态玩家/世界缓存有明确清理。
- [ ] 旧档迁移幂等且不静默丢数据。

### 网络与环境

- [ ] 所有 C2S Packet 服务端校验。
- [ ] 管理功能不可被普通客户端直接调用。
- [ ] 移动 Packet 有限流和安全落点。
- [ ] 通用 Module 不引用客户端类。
- [ ] 单人、多人、专用服务器全部启动并通过场景验证。

### 构建、JAR 与交付

- [ ] `clean build` 成功。
- [ ] 测试不再是 `NO-SOURCE`。
- [ ] 静态规范任务全部通过。
- [ ] `build/libs` 与 `jars/` 内容一致。
- [ ] Commit 为中文详细说明。
- [ ] 只推送 `tribulation-rework`。
- [ ] PCL 部署 JAR 哈希与提交记录一致。

## 10. 最终验收报告格式

最终完成时必须提交一份验收报告，至少包含：

```text
基线提交：cb3ce89...
最终提交：<hash>
实施分支：tribulation-rework
数据版本：<version>
迁移覆盖版本：<list>
自动测试：<passed/total>
GameTest：<passed/total>
静态检查：<passed/total>
客户端验证：通过/失败
多人验证：通过/失败
专用服务器验证：通过/失败
旧档迁移：通过/失败
build/libs JAR SHA-256：<hash>
jars JAR SHA-256：<hash>
PCL 部署 JAR SHA-256：<hash>
P0：0 未关闭
P1：0 未关闭
P2：0 未关闭或有用户书面接受项
```

验收报告必须逐项链接或引用本文件第 8 节的证据。仅有 `BUILD SUCCESSFUL` 不能作为最终验收通过。

## 11. 中止、回滚和例外

- 某阶段失败时，回滚该阶段 Commit，不得用第二套运行实现兜底。
- 发现新问题时先加入审计编号、阶段和验收矩阵，再修改代码。
- 任何需要保留旧 Interface 的例外必须记录：原因、唯一调用者、删除阶段、自动禁止新增调用的规则。
- 任何无法无损迁移的旧数据必须记录确定策略，禁止静默清空。
- 不得通过删除用户存档、清空 Capability 或要求重新开档完成验收。

---

# 附录 A：`cb3ce89_全项目隐藏问题与接口统一审计.md` 完整内容

以下内容完整收录审计原文，作为本实施文档的问题基线和验收来源。为接入本文目录，仅将原文二、三级标题增加 `A.` / `A-` 前缀，正文、证据、结论和问题编号均不删减。

原文标题：Friday_Cultivation `cb3ce89` 全项目隐藏问题与接口统一审计

审计基线：`tribulation-rework` / `cb3ce89f7deae3165ea35e01567b180731c21138`

审计日期：2026-08-21

## A.1 结论

当前版本可以完整编译，但**不能认定已经做到“一个系统只有一个版本、所有 API 和数据只有一个权威接口”**。

源码层面没有发现重复 FQCN、同名 Java 文件、完全相同的 Java 副本、遗留 `src/main/java1` 之类的旧源码目录，也没有重复网络包注册；真正的问题主要是：同一业务状态存在两套存储、同一业务动作存在多个入口、同一顺序存在多套解释、旧接口与新接口同时存活。

其中飞行、境界链、境界切换、渡劫运行态、渡劫奖励、锻体生命继承六个系统已经确认存在多源覆盖或接口分裂，部分问题会造成双倍执行、物品永久丢失、死亡后加成丢失、高境界真元计算错误、客户端越权改属性等实际后果。

## A.2 已确认没有重复的问题

- Java 文件：492 个。
- 资源文件：2829 个。
- 没有重复文件名、重复 FQCN、package 与路径不一致、完全相同的 Java 文件副本。
- 没有旧源码集目录参与编译；只有 `src/main`。
- 88 个网络消息注册类全部唯一，没有同一个 Packet 注册两次。
- 150 个静态物品注册项全部存在 item model。
- 58 个静态方块注册项全部存在 blockstate。
- 中英文静态物品/方块翻译未发现真实缺项；`bone_block` 使用方块翻译键，不是缺失。
- `build/libs` 与仓库 `jars/` 中 JAR 的 3627 个归档条目逐个解压哈希一致；原始 JAR SHA 不同仅来自 ZIP 时间戳。
- `clean build` 成功。

这些结果只能排除“文件副本型覆盖”，不能排除“逻辑与数据双源型覆盖”。

## A.3 严重问题

### A-P0-1 飞行事件被注册两次，飞行逻辑双倍执行

涉及文件：

- `FridayCultivationMod.java`
- `flight/CultivationFlightEvents.java`

`CultivationFlightEvents` 已有 `@Mod.EventBusSubscriber` 自动注册，主类构造函数又执行：

```java
MinecraftForge.EVENT_BUS.register(CultivationFlightEvents.class);
```

因此同一个 `PlayerTickEvent` 会调用两次 `tickFlight()`。`FLIGHT_TICKS` 每个真实 tick 增加两次，原本每 20 tick 扣一次灵气会变成约每 10 tick 扣一次，飞行灵气消耗翻倍。日志计数也会失真。

这是已确认的“同一系统多版本/多入口重复覆盖”。

### A-P0-2 御剑飞行有两套权威状态，会导致剑永久丢失

涉及文件：

- `flight/CultivationFlightHandler.java`
- `cultivation/CultivationData.java`
- `event/CapabilityEvents.java`

御剑状态同时保存在：

1. `SWORD_FLIGHT` / `SWORD_FLIGHT_SLOT` 静态 Map；
2. `CultivationData.swordFlightStack` / `swordFlightOriginalSlot` Capability。

开始飞行时两边都写，停止飞行时归还物品只读取静态 Map。服务器重启后静态 Map 消失，但 Capability 会从 NBT 恢复剑；登录事件随后直接 `clearSwordFlight()`，没有先归还剑，剑会永久消失。

同一服务器内玩家退出时三个静态 Map 也没有统一清理：

- `SWORD_FLIGHT`
- `SWORD_FLIGHT_SLOT`
- `FLIGHT_TICKS`

这会留下跨玩家会话状态和内存条目。飞行状态必须只保留一个权威来源，不能用 Map 与 Capability 互相兜底。

### A-P0-3 境界顺序同时存在三套语义

涉及文件：

- `cultivation/realm/Realm.java`
- 全项目 25 个直接使用 `Realm.values()` / Realm ordinal 的文件

当前同时存在：

1. 枚举声明顺序；
2. `LOGICAL_ORDER`；
3. `next()/prev()` 主突破链。

三者不一致：

- 枚举声明为了兼容旧 ordinal，把玄仙、仙君、仙尊、仙王追加在散仙之后；
- `LOGICAL_ORDER` 把散仙放在渡劫与真仙之间；
- `next()/prev()` 明确把散仙排除在主突破链之外。

因此 ordinal 既不是主链等级，也不是 UI 顺序。

已确认的实际错误：

- `computeTotalZhenyuanEarned()`、`computeAutomaticZhenyuanAttrPerStat()` 用 ordinal 和 `Realm.values()` 计算真元。玄仙计算时会错误计入半圣、圣人、半帝、大帝、散仙；半圣计算时又完全漏掉逻辑上在它之前的玄仙、仙君、仙尊、仙王。
- `SoulReaperEntity.realmForKills()` 按 ordinal 增长，真仙之后直接进入半圣，完全跳过新增四个仙境。
- 渡劫奖励把最低生效境界保存成 ordinal。玄仙奖励切到逻辑上更高的半圣时会被错误清除；真仙奖励切到逻辑上更低的散仙时反而仍然生效。
- NPC 将 `realmOrd` 写入 NBT。枚举顺序变化会改变旧 NPC 境界；非法值用 `floorMod` 映射成另一个合法境界，而不是拒绝或回退。
- `deterministicCultivationRequirement()` 用 ordinal 选择修为曲线，新增仙境的修为需求被排到大帝、散仙之后，与明确链路相反。

境界系统必须提供唯一的稳定接口，例如稳定 `id`、主链 `progressionIndex`、等级比较 `isAtLeast`、上下境界 `next/previous`、旁支类型；业务代码不得再用 ordinal 表示境界等级或持久化身份。

### A-P0-4 “切换境界”存在三个互不一致的实现

涉及文件：

- `item/RealmTokenItem.java`
- `network/RealmSelectionPacket.java`
- `network/EditPlayerStatsPacket.java`
- `cultivation/CultivationData.java`

三个入口都在做“设置境界”，但后置动作不同：

| 入口 | 真元基线 | 悟道清零 | 散仙状态 | 生命重算 | 子阶段 | 权限校验 |
|---|---:|---:|---:|---:|---:|---:|
| RealmTokenItem | 有 | 无 | 有 | 有 | 第一阶段 | 由持有物品间接限制 |
| RealmSelectionPacket | 有 | 有 | 有 | 有 | 客户端选择 | 无 |
| EditPlayerStatsPacket | 无 | 无 | 无完整处理 | 无显式重算 | ordinal 选择 | 无 |

`CultivationData.setRealm()` 本身只修改部分状态，调用者必须“记得”补真元、子阶段、悟道、生命、散仙计时等动作。这是浅接口，无法保证一致性。

必须建立一个服务端权威的境界转换模块，三个入口只能作为 Adapter，不能各自复制转换流程。

### A-P0-5 两个开发工具网络包允许普通客户端任意修改角色数据

涉及文件：

- `network/RealmSelectionPacket.java`
- `network/EditPlayerStatsPacket.java`

两个 C2S Packet 都没有校验玩家是否：

- 持有对应令牌；
- 创造模式；
- OP；
- 有服务端配置权限。

修改客户端即可直接发送 Packet：

- 任意选择境界和子阶段；
- 任意设置五维、肉身防御、炼丹、炼器、骨龄；
- 触发不完整的境界转换后置状态。

这是服务端权威性漏洞，不只是 UI 问题。

### A-P0-6 CultivationData 死亡复制与 NBT 持久化字段不闭合

`copyFrom()` 未复制以下持久业务数据：

- `breakthroughHpBonus`
- `breakthroughQiBonus`
- `daoFruitTotalEaten`
- `tribulationBonusEntries`
- `tribulationType`
- `tribulationDamageRatio`

后果：

- 玩家死亡/重生后累计突破生命和灵气加成归零；
- 玩家死亡/重生后全部渡劫隐藏奖励丢失；
- 玩家死亡/重生后练气极境所需的道果总数归零；
- 活跃渡劫的劫种与比例伤害状态无法可靠跨死亡复制。

其中 `tribulationType`、`tribulationDamageRatio` 还完全没有写入或读取 NBT，重新登录后会回到默认值；但波数、冷却、当前波雷击数等其他渡劫字段会保留，于是一个渡劫会以“半套旧状态 + 半套默认状态”恢复。

### A-P0-7 新旧渡劫运行接口同时存在，新 Spec 没有成为权威状态

涉及文件：

- `event/tribulation/TribulationSpec.java`
- `cultivation/realm/Realm.java`
- `cultivation/CultivationData.java`
- `event/TribulationHandler.java`
- `network/RequestBreakthroughPacket.java`

表面上已经有 `TribulationSpec` 和 `CultivationData.startTribulation(TribulationSpec)`，但真实突破入口仍执行旧流程：

1. 从 Realm 分别取 `waves/bolts/damage`；
2. 在 Packet 内针对筑基/金丹路线再次覆盖三个整数；
3. 调用旧的三整数 `beginTribulation`；
4. `CultivationData.startTribulation(TribulationSpec)` 没有真实调用者。

随后 `TribulationHandler.currentSpec()` 又从 Realm 重新生成基础 Spec，而不是读取实际启动时的运行 Spec。结果是：

- 筑基、金丹路线自定义波数/伤害与事件里公布的 Spec 可能不同；
- 综合评判缩放后的波数/伤害与 `Started` 事件公布值可能不同；
- 间隔计算、扩展事件和未来劫种读取的不是同一份运行数据；
- `beginTribulation(player, data, strikes)` 传入伤害 0 后又被 `Math.max(1, ...)` 强制成 1，无法按注释回退 Realm 默认伤害；
- 旧三整数启动方法不会重置 `tribulationType` 与 `tribulationDamageRatio`，未来增加新劫种后可能继承上次渡劫状态。

运行态必须保存一份完整、不可拆分的 `TribulationSession/TribulationSpec`，所有伤害、事件、客户端显示、恢复存档都只能读取它。

### A-P0-8 渡劫奖励仍是旧百分比/ordinal 模型，并存在重复放大

奖励存储仍是：

```java
List<double[]> // [percent, minRealm.ordinal]
```

这与新渡劫模块并没有统一，且没有实现固定快照奖励。

已确认问题：

- 奖励使用境界 ordinal 判断激活，受错误境界顺序影响。
- `setRealm()` 只按阈值选择性删除，`demoteOnFailure()` 却清空全部奖励；同一种“境界下降”有两套规则。
- 一次高境界渡劫失败会把更早低境界获得、按当前境界本应继续生效的奖励也全部删除。
- 多条奖励使用连乘复利，但展示接口 `getTribulationBonusPercent()` 只是简单相加，显示与实际值不一致。
- 体质真元先乘渡劫倍率变成生命，再对玩家总生命执行一次渡劫 `MULTIPLY_TOTAL`，体质贡献的生命被二次放大。
- 身法代码明确不乘奖励，但方法注释写“含渡劫隐藏奖励”，接口语义互相矛盾。
- 成功流程先切换境界、添加普通突破奖励，再计算和记录渡劫奖励，没有保存渡劫前属性快照。

该系统当前不是单一奖励接口，而是“旧百分比列表 + 新档位计算器 + AttributeModifier + 真元 Helper”的组合。

## A.4 高优先级问题

### A-P1-1 渡劫综合评判权重有两套常量

- `TribulationConstants`：灵根 0.5、体质 0.3、功法 0.2。
- `TribulationScalingHelper`：灵根 1.2、体质 0.5、功法 0.3。

真实计算使用 Helper 私有常量，Constants 中号称“集中管理”的三项权重没有调用者。修改 Constants 不会改变游戏结果。

同样，`DEFAULT_DAMAGE_RATIO`、`MIN_TOTAL_DAMAGE_MULT`、`MAX_TOTAL_DAMAGE_MULT` 当前没有实际调用者。

### A-P1-2 散仙雷劫配置有两个来源

- `Realm.LOOSE_IMMORTAL` 声明 6 波、每波 10 道、单击 255；
- `LooseImmortalBonusHelper` 按散仙等级返回真实波数、道数、伤害。

当前 `LooseImmortalHandler` 使用 Helper，Realm 的散仙 Spec 是另一套可被其他调用者误用的数据。必须删除其中一个来源或由 Realm 唯一委托到同一个配置模块。

### A-P1-3 锻体继承生命同时保存在 Capability 和 Player PersistentData

涉及文件：

- `CultivationData.bodyTemperingHpInherited`
- `TechniqueEffectHandler.TAG_MAX_BODY_TEMPERING_LEVEL`

注释声称新逻辑读取 `bodyTemperingHpInherited`，但实际生命计算完全读取玩家 PersistentData 中的旧“最高锻体层数”。Capability 字段会复制、保存、加载、更新，却不参与最终生命计算；转世清理也只清旧 Tag，不清 Capability 字段。

这是明确的新旧数据模型并存。必须只保留固定生命快照或最高层数中的一个。

### A-P1-4 普通突破奖励的结算顺序导致突破后不是满状态

`advanceOnSuccess()` 先把当前灵气设为当时上限，再调用 `applyBreakthroughBonus()` 增加新的灵气上限。因此突破结束后当前灵气小于新上限。

生命也类似：`completeBreakthrough()` 调用 `player.setHealth(player.getMaxHealth())` 前没有先刷新新境界、普通突破、渡劫奖励的 AttributeModifier。下一 tick 上限增加后，当前生命仍停留在旧上限。

此外字段注释写“含确定性随机、大帝每次封顶 2000”，真实算法只有固定 5% 与 1.0/0.35 倍率，没有随机和 2000 封顶，文档与代码不一致。

### A-P1-5 旧五行灵气 API 返回固定零值，但仍有真实调用者

`CultivationData` 中以下 Deprecated API 全部是空实现：

- `getElementCount()`
- `getTotalElementQi()`
- `getElementPercent()`
- `getElementDamageBonus()`
- `getElementPowerPercent()`
- `getDominantElement()`

但仍有运行调用：

- `ChargeableSpellHandler` 读取火元素强度，永远得到 0；
- `FireSwordAuraPacket` 读取主元素，永远得到 PURE；
- 旧 UI 方法仍保留整套元素百分比渲染。

这不是兼容接口，而是仍在影响实际玩法的失效接口。必须实现一个真实元素数据源，或让全部调用者迁移到当前法术/功法元素接口后删除旧方法。

### A-P1-6 练气极境是不可到达的空功能

- `canEnterQiExtreme()` 要求 `daoFruitTotalEaten >= 27`；
- `incrementDaoFruitTotalEaten()` 没有任何调用者；
- 道果物品只增加另一字段 `daoFruitEaten`；
- `advanceToQiExtreme()` 是 TODO 空方法；
- `applyQiRefiningEnhancements()` 是 TODO，原样返回基础值。

因此极境条件无法通过正常玩法完成，即使外部强行调用也没有效果。

### A-P1-7 NPC 境界持久化和勾魂使者成长仍依赖 ordinal

`WanderingCultivatorEntity` 将 `realmOrd` 直接写入 NBT；`SoulReaperEntity` 通过 ordinal 加击杀数决定境界。这两处都绕过稳定 `Realm.id` 与显式主链。

这会导致旧存档受枚举调整影响，并让勾魂使者成长跳过玄仙、仙君、仙尊、仙王。

### A-P1-8 ShadowStepPacket 可穿墙且没有服务端频率限制

服务端只检查玩家有暗影步效果，然后把位置直接移动 5 格：

- 没有碰撞射线；
- 没有安全落点搜索；
- 没有冷却或单 tick 限流；
- 客户端可连续发包。

效果持续期间可通过改包客户端高速位移或穿过实体方块。

## A.5 中优先级问题

### A-P2-1 区块灵气再生会丢失小数，再生可能被频繁查询永久压制

`ChunkQiPool.applyRegen()` 把 double 再生量直接截断为 int，同时无条件把 `lastTouchTime` 更新到当前时间。

当一次累计再生小于 1 点时，本次增加 0，但经过时间被清空。`peek()` 名义上是读取，却会修改时间戳。若系统频繁查询，灵气可能长期无法再生。

应保存小数余量，或只按真正结算的完整 tick/完整点数推进时间。

### A-P2-2 Capability 注册存在多个公开入口，只有主类入口实际生效

当前同时存在：

- `FridayCultivationMod.onRegisterCapabilities()`；
- `CapabilityEvents.registerCapability()`；
- `ChunkQiCapability.register()`。

后两者目前没有接线，是死接口。它们不会马上重复注册，但会让维护者误以为可从不同入口注册；未来接线后会造成重复。

### A-P2-3 仍保留无调用或空返回的旧接口

- `BlockQiMapping`：Deprecated 包装层，真实接口是 `BlockQiSpecs`；没有调用者。
- `CultivationData.tribulationBoltInterval()`：固定返回 0，注释称兼容旧调用；没有调用者。
- `ZhenyuanBonusHelper` 的旧气海吸收范围/倍率接口：全部固定返回 0。
- `Realm.progressIndex()`：没有调用者。

为了满足“只能有一个版本”，这些接口不能长期保留为第二套可见 API；应迁移后删除，或明确变成唯一接口的 Adapter 并加测试。

### A-P2-4 客户端边界没有完全隔离

`RealmSelectorTokenItem` 位于通用 item 包，却在方法体中直接引用 `Minecraft` 与客户端 Screen。虽然分支用 `level.isClientSide` 保护，通用类仍带有客户端符号，属于专用服务器类加载风险。

应通过仅客户端类、DistExecutor 或客户端事件入口打开界面。

### A-P2-5 QiFieldRegistry 静态实例没有服务器生命周期清理

`QiFieldRegistry.INSTANCES` 只按 dimension key 缓存，未在 Level unload 或 Server stopped 时移除。若方块实体卸载路径未完整注销，集成服务器在同一 JVM 切换存档时可能残留旧世界对象；即使全部注销，也会永久保留 registry 容器。

### A-P2-6 网络协议版本常量与真实构造参数分裂

`ModNetwork.PROTOCOL_VERSION = "1"` 已声明，但创建 Channel 时重复写了三个字面量 `"1"`，常量本身没有成为唯一来源。修改常量不会修改协议校验。

### A-P2-7 文档与实现存在漂移

- 根工作区 README 仍有旧 13 境界描述，项目代码已是 21 境界。
- 项目 README 把 `activeTribulationMultiplier()` 作为当前奖励接口，但代码中已经同时出现新 Spec/事件/防御模块，实际仍是混合架构。
- 多处“新逻辑”“集中常量”“固定继承”的注释与实际调用路径相反。
- 主类初始化日志仍显示旧名称“小翔的修仙世界”。

## A.6 验证空白

Gradle 输出：

- `compileTestJava NO-SOURCE`
- `test NO-SOURCE`

当前绿色构建只证明源码和资源能编译打包，不能验证：

- 死亡/重生数据不丢；
- 退出重进后御剑物品归还；
- 境界链顺序；
- 降境界奖励失效；
- 渡劫运行 Spec 一致；
- 网络包权限；
- 专用服务器启动。

六个 GameTest 类按既有要求保留为空，本审计没有把它们算作新缺陷；但项目仍需要针对当前新增系统建立自动测试。

## A.7 必须统一成的单一接口

建议按以下边界收敛，不再保留“新版/旧版”称呼：

1. **RealmTopology**：唯一境界 id、主链位置、等级比较、前后境界、旁支关系。外部禁止 Realm ordinal 参与等级、持久化和协议。
2. **RealmTransition**：唯一境界转换事务；内部一次完成境界、子阶段、真元、悟道、散仙状态、奖励生效、生命/灵气刷新和同步。
3. **TribulationSession**：唯一运行 Spec；启动、存档、恢复、事件、显示、伤害都读取同一对象。
4. **TribulationRewardSnapshot**：唯一固定快照奖励账本；使用稳定 realm id、sub-stage id 与完整链位置，不保存 ordinal，不再让 Attribute 与 Helper 重复套倍率。
5. **FlightState**：只存 Capability；静态 Map 不保存玩家权威状态，事件只注册一次，并明确登录/退出/死亡/重启物品归还。
6. **PlayerProgressData**：所有需持久化/死亡继承的数据统一进入 CultivationData，并建立字段矩阵测试：字段必须明确属于 copy、NBT、同步中的哪些集合。
7. **ServerAuthorization**：所有 C2S 开发/管理功能统一走服务端权限策略，UI 和持有物品不能替代服务端校验。

## A.8 完成“单版本、单接口”前的验收条件

- `CultivationFlightEvents` 在事件总线上只有一次注册。
- 飞行系统不存在玩家 UUID 静态权威 Map。
- 业务代码不再用 Realm ordinal 做等级比较、持久化、协议或奖励阈值。
- 三个境界修改入口全部委托同一个转换事务。
- 渡劫开始后只存在一份运行 Spec，事件值与真实伤害/波数一致。
- 渡劫奖励只结算一次，不在真元 Helper 与 MAX_HEALTH Modifier 中重复放大。
- `CultivationData` 的持久字段通过自动测试验证 NBT 往返与 Clone 等价。
- Deprecated 空实现没有运行调用者；无用旧接口删除。
- RealmSelection/EditPlayerStats 有服务端权限校验。
- 至少通过客户端、集成服务器、专用服务器三类启动验证。
- 为境界链、降境界奖励、死亡复制、退出重进御剑、渡劫 Spec、突破后满血满灵气添加回归测试。

在这些条件完成前，不能对当前版本作出“已经保证所有 API、数据只使用同一个接口”的结论。
