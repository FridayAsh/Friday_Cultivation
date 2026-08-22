# 阶段 7：FlightState 唯一化证据

## 1. 执行范围

- 目标 Module：`CultivationData` FlightState 与 `CultivationFlightHandler`
- 实施分支：`tribulation-rework`
- 基线：阶段 6 工作区

## 2. 唯一权威状态

服务端飞行状态全部存放在 `CultivationData`：

- 托管剑物品 `swordFlightStack`；
- 原始槽位 `swordFlightOriginalSlot`；
- 消耗计时 `flightTicks`；
- 灵气飞行开关 `qiFlightToggled`。

已删除 `CultivationFlightHandler` 的 `SWORD_FLIGHT`、`SWORD_FLIGHT_SLOT`、`FLIGHT_TICKS` 静态玩家 Map。Handler 只读取/修改 Capability，并负责启停时物品归还。

登录、死亡 Clone、重生和维度切换均从 Capability 恢复；停止飞行时优先取 Capability 托管物品，按原槽位归还，槽位被占用时进入背包/掉落分支。

## 3. 验收

`StageOnePolicyTest` 增加结构检查，确认没有静态玩家权威 Map，计时来自 `data.incrementFlightTicks()`；现有登录恢复测试继续通过。

```text
gradlew.bat test
```

结果：`BUILD SUCCESSFUL`。

## 4. 尚未完成

- 需要在集成服务器上执行物品守恒的死亡、断线、重启场景测试；
- 停服前主动归还在线玩家物品的显式生命周期钩子将在阶段 8–10 的生命周期审计中补充。
