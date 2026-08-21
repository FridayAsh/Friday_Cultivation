# 阶段 9：网络、客户端 Seam 与缓存生命周期证据

基线：`cb3ce89f7deae3165ea35e01567b180731c21138`

## 已完成

1. `ShadowStepPacket` 在服务端统一执行 5 tick 冷却、方向白名单、世界边界、实体包围盒无碰撞检查；客户端不能通过连续发包穿墙或加速。
2. `RealmSelectorTokenItem` 通过 `ClientRealmSelectionHooks` 调用界面；`SyncCultivatorOffersPacket`、`SyncCultivatorInventoryPacket`、`SyncFormationFlagsPacket` 通过客户端 Hook + `DistExecutor` 处理显示，网络包本身不直接加载 `Minecraft`/Screen。
3. `CompactNumberFormat` 的语言读取改为 `ClientLanguageHooks` Seam，专用服务器路径默认 `en_us`，不依赖客户端类。
4. `QiFieldRegistry` 已有 `LevelEvent.Unload` 和 `ServerStoppedEvent` 清理；区块灵气、阵法和核心实体仍由各自实体卸载路径注销，避免静态注册表长期持有旧世界对象。
5. `ModNetwork` 的通道创建、客户端接受和服务端接受全部使用同一个 `PROTOCOL_VERSION` 常量。

## 自动验证

- `gradlew test`：22 个测试通过。
- 源码扫描：网络包不再存在真实 `import net.minecraft.client...`；剩余客户端引用仅位于明确标注 `Dist.CLIENT` 的客户端处理类或反编译注释。
- `ShadowStepPacket` 的服务端碰撞/边界/冷却逻辑在源码中均为单一入口。

## 运行验证待执行

客户端单人、多人连接和专用服务器启动需要在 Minecraft 实例中执行；构建阶段只记录代码与自动测试证据，不将 `BUILD SUCCESSFUL` 误写成实机通过。
