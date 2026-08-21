# 阶段 2：RealmTopology 境界拓扑统一证据

## 1. 执行范围

- 目标 Module：`cultivation/realm/RealmTopology`
- 实施分支：`tribulation-rework`
- 起始提交：阶段 1 提交 `17389e58c0fae0b9f7174cebf2a4dd717721f8d4`
- 结果提交：由本文件所在 Git 提交记录确定

## 2. 唯一 Interface

`RealmTopology` 现在是境界顺序的唯一解释 Module，公开 Interface 包括：

- `require/find(stableId)`：稳定 ID 解析；
- `mainChain()`：20 个主链境界，不包含散仙旁支；
- `selectionOrder()`：21 个 UI/令牌选择项；
- `relationOf()`：区分主链和散仙旁支；
- `nextMain/previousMain()`：只沿主链转换；
- `progressionIndex()`：稳定等级和子阶段位置；
- `isAtLeast/isBefore()`：统一等级比较；
- `fromLegacyEnumOrdinal()`：仅供旧 NPC/旧账本迁移 Adapter 使用。

散仙的位置被明确定义为渡劫失败旁支：散仙不能满足真仙阈值，真仙可以满足散仙阈值；散仙不参与 `nextMain`。

## 3. 已迁移调用者

- `Realm.logicalOrder/next/prev/progressIndex` 改为委托 `RealmTopology`；
- `CultivationData` 的修为、真元、法术解锁和悟道曲线使用稳定拓扑位置；
- Golden Core、寿命、体质、灵根、法术缩放、渡劫/领域技能和客户端展示使用 `isAtLeast/isBefore`；
- `WanderingCultivatorEntity` 的同步字段和新 NBT 使用 `realmId`；旧 `realmOrd` 只读迁移且非法值回退凡人，不再 `floorMod` 到随机合法境界；
- `EditPlayerStatsPacket` 与属性编辑界面传输稳定 `realmId`；
- 宗门 NPC 生成、勾魂使者成长使用主链位置；
- 物品注册、创造标签、法术说明使用 `selectionOrder`。

## 4. 验收命令与结果

```text
gradlew.bat test
```

结果：`BUILD SUCCESSFUL`，共 7 项测试通过。

新增/覆盖的测试：

1. 主链明确包含 20 个境界，选择顺序包含 21 项；
2. 真仙、玄仙、仙君、仙尊、仙王与半圣的前后关系；
3. 散仙旁支阈值语义；
4. 稳定 ID 解析；
5. 全量业务源码禁止 Realm ordinal 静态检查。

## 5. 尚未完成

- `CultivationData` 的渡劫奖励仍是旧 `List<double[]>` 过渡格式，阶段 6 将替换为固定快照账本；
- `RealmTransition` 尚未成为唯一境界写入口；
- NPC 旧 `realmOrd` 的迁移统计和自动迁移版本字段将在阶段 3 数据契约中补齐；
- 境界比较以外的其他枚举协议（如属性、阵旗等级）不属于本阶段。
