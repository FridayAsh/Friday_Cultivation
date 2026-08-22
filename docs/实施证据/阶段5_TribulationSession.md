# 阶段 5：TribulationSession 唯一运行态证据

## 1. 执行范围

- 目标 Module：`event/tribulation/TribulationSession` 与 `CultivationData` 活动态
- 实施分支：`tribulation-rework`
- 基线：阶段 4 提交 `45825b3`（完整哈希以 Git 记录为准）

## 2. 唯一运行态

`TribulationSession` 保存一次渡劫启动时的完整上下文：Session ID、路线、来源/目标境界与子阶段稳定 ID、天骄档位 ID，以及完整 `TribulationSpec`（波数、每波道数、固定伤害、伤害比例、道间隔和劫种）。启动后：

- `CultivationData.startTribulation(TribulationSpec, boolean)` 是唯一实际建 Session 入口；
- `TribulationHandler.currentSpec`、伤害、波次间隔和事件均读取 Session；
- Session 写入 NBT，Clone 和同步沿用同一 `CultivationData` Codec；旧 Session 缺失的新字段使用显式安全默认值；
- `clearTribulation` 一次清理 Session、旧镜像字段、劫种和伤害比例，避免半套状态复活；
- 旧三整数启动方法已删除，所有启动调用者先构造完整 `TribulationSpec`。

## 3. 迁移

旧存档只有整数渡劫字段时，加载后按旧字段创建一次 Session；新存档只写 `tribulationSession`，运行中不再从 Realm 重新生成活动计划。

## 4. 验收

```text
gradlew.bat test
```

结果：`BUILD SUCCESSFUL`。覆盖 Session 的 NBT 往返、Clone、未知劫种回退和完整清理。

## 5. 尚未完成

- 普通路线与散仙路线均已由 Handler 构造完整 `TribulationSpec` 后进入唯一入口；
- 客户端云显示包尚未携带完整 Session 字段，当前持续时间由同一 Spec 计算；
- 散仙配置来源与固定奖励账本分别在后续阶段收敛。
