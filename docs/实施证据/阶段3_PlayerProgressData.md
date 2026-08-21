# 阶段 3：PlayerProgressData 数据契约闭合证据

## 1. 执行范围

- 目标 Module：`cultivation/CultivationData` 与 `cultivation/qi/field/QiFieldRegistry`
- 实施分支：`tribulation-rework`
- 基线：`cb3ce89f7deae3165ea35e01567b180731c21138`
- 前置提交：阶段 2 `6052d65c9e0b1b06f941e1051d9365fab91bd8f2`

## 2. 数据契约变更

`CultivationData` 新增 `CURRENT_DATA_VERSION = 2` 和 `dataVersion`。新写入的 NBT 固定包含版本号，旧存档缺失版本号时按 `0` 读取，完成当前迁移后写回当前版本。

本阶段补齐并验证以下此前在 Clone/NBT 之间漂移的字段：

- `breakthroughHpBonus`；
- `breakthroughQiBonus`；
- 道基果累计极境字段已在阶段 8 决定取消并完整删除，避免留下未实现状态；
- `tribulationBonusLedger`（固定快照对象按 rewardKey 深复制）；
- `tribulationType`（稳定 ID）；
- `tribulationDamageRatio`。

渡劫类型通过 `TribulationType.byId` 解码。未知 ID 明确回退当前唯一实现 `LIGHTNING`，不使用 ordinal 或随机合法映射。

同步包沿用 `CultivationData.serializeNBT()`，因此本阶段的持久化 Codec seam 同时覆盖存档与客户端同步；`PlayerEvent.Clone` 通过 `copyFrom` 覆盖同一字段矩阵。

## 3. 生命周期清理

`QiFieldRegistry` 增加：

- `clear(ServerLevel)`：世界卸载时删除该维度容器；
- `clearAll()`：服务器停止时清空全部静态实例。

两个入口集中挂在 `CapabilityEvents` 的 Forge 事件订阅中，避免重复注册新的生命周期监听器。

## 4. 验收命令与结果

```text
gradlew.bat test
```

结果：`BUILD SUCCESSFUL`，共 10 项测试通过。

新增测试 `CultivationDataPersistenceTest` 覆盖：

1. NBT 往返后突破加成、道基果累计、渡劫劫种/比例和奖励账本保持一致；
2. Clone 后上述字段与原数据一致；
3. 未知渡劫类型 ID 使用明确安全回退。

## 5. 尚未完成

- 固定快照账本已在阶段 6 工作区完成，等待本批次构建交付；
- `RealmTransition` 尚未成为唯一境界写入口；
- 完整字段矩阵文档、转世清理矩阵和统一 Snapshot 类型将在后续阶段继续收敛。
