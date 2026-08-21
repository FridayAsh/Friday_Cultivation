# 阶段 4：RealmTransition 唯一境界写入口证据

## 1. 执行范围

- 目标 Module：`cultivation/RealmTransition`
- 实施分支：`tribulation-rework`
- 前置提交：阶段 3（以 Git 提交记录为准）

## 2. 唯一 Interface

`RealmTransition` 以 `Request` 描述意图，以 `Result` 返回统一结果。理由、资源恢复策略和奖励策略均为显式值：

- `Reason`：令牌、选择器、管理编辑、普通突破、渡劫成功/失败、散仙选择、转世；
- `ResourcePolicy`：保留、半灵气、变化时半灵气、满灵气；
- `RewardPolicy`：无奖励、小境界突破、大境界突破。

模块统一处理：

- Realm/SubStage 规范化与写入；
- 令牌/选择器的真元基线重建；
- 散仙状态、计时与灵魂状态清理；
- 修为、悟道、当前灵气恢复策略；
- 普通突破的真元/突破加成；
- 境界跌落时真元基线调整和渡劫奖励清理。

`CultivationData.setRealm` 与 `setSubStage` 已收窄为包内实现，业务调用者不再直接写入。

## 3. 已迁移调用者

- `RealmTokenItem`；
- `RealmSelectionPacket`；
- `EditPlayerStatsPacket`；
- `CultivationData.advanceOnSuccess` 普通突破；
- `CultivationData.demoteOnFailure` 失败跌落；
- `CultivationData.becomeLooseImmortal` 散仙转入。

网络层仍只负责权限、消息和同步，境界状态事务由 `RealmTransition` 完成。

## 4. 验收命令与结果

```text
gradlew.bat test
```

结果：`BUILD SUCCESSFUL`，共 13 项测试通过。

新增 `RealmTransitionTest` 覆盖：

1. 令牌切换使用统一半灵气/重建真元后置条件；
2. 普通突破先应用奖励再补满最终灵气；
3. 通过统一跌落入口停用低于目标境界的渡劫奖励。

## 5. 尚未完成

- 渡劫成功/失败的完整 Handler/Session 仍在阶段 5–6 接入；
- 散仙晋升仍是同境界状态更新，后续需纳入同一 Session/奖励事务；
- 旧的渡劫整数状态与固定快照账本尚未删除。
