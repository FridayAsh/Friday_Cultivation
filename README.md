# Friday Cultivation（Friday修仙）—— 完整项目介绍

> Minecraft 1.20.1 / Forge 47.2.0 修仙模组，完整复刻「小翔的修仙世界」修仙体系。
> 本文档系统介绍项目全部系统、特色、核心文件与调用的 API。

---

## 〇、项目总览

- **Mod ID**：`friday_cultivation`
- **版本**：0.1.0
- **主类**：`FridayCultivationMod`（`com.friday.cultivation`）
- **代码规模**：约 500 个 Java 文件，覆盖原模组全部 475 个顶层类
- **核心机制**：Forge Capability（玩家/区块/妖兽数据）、Forge 事件总线、自定义网络包、Patchouli 文档、数据包（炼丹/炼器/灵气配方）

### 主类注册流程（FridayCultivationMod）
- 注册：方块、物品、实体、方块实体、效果、粒子、菜单、配方、战利品、世界生成、创造标签
- Capability：`CultivationData`（玩家）、`BeastCultivationData`（妖兽）、`ChunkQiPool`（区块灵气）
- 数据包加载：`PillEffectSpecLoader`（丹药）、`BlockQiSpecLoader`（方块灵气）
- 网络：`ModNetwork` 注册全部网络包
- 配置：`ModCommonConfig` / `ModClientConfig`
- 客户端：`ClientSetup` 统一注册渲染/按键/粒子/覆盖层

### 项目治理与统一实施

- [单版本、单 Interface 统一实施与验收方案（cb3ce89 基线）](docs/单版本单接口统一实施与验收方案_cb3ce89.md)：从基线冻结、接口收敛、旧档迁移、自动测试、三类运行验证，到构建、部署和最终验收的完整执行文档；附录完整纳入 `cb3ce89_全项目隐藏问题与接口统一审计.md` 的 23 项问题。

---

## 一、境界体系（Realm System）

**特色**：21 境界完整突破链，数字层/四阶段/专属子阶段混合，标准生命值，雷劫体系。

**核心文件**：
- `cultivation/realm/Realm.java`：21 境界枚举（凡人→锻体→练气→筑基→金丹→元婴→化神→炼虚→合道→大乘→渡劫→散仙→真仙→玄仙→仙君→仙尊→仙王→半圣→圣人→半帝→大帝），含 `standardMaxHealth()`（标准生命值）、`tribulationSpec()`（雷劫配置）、`next()/prev()`（突破链）、`maxQi()`（灵气上限）、`baseLifespan()`（寿命）
- `cultivation/realm/SubStage.java`：子阶段（数字层/四阶段/专属档）
- `cultivation/realm/BeastRealm.java`：妖兽独立境界

**调用 API**：`Realm.values()`、`realm.standardMaxHealth()`、`realm.tribulationCount()/tribulationBoltsPerWave()/tribulationStrikeDamage()`、`realm.next()/prev()`

---

## 二、渡劫系统（Tribulation System）

**特色**：数据驱动劫谱、劫种抽象、防御链、事件钩子、天骄档位综合评判、按里程碑保存的固定快照奖励。

**核心文件**（`event/tribulation/`）：
- `TribulationSpec.java`：数据驱动劫谱（波数/道数/伤害/间隔/劫种）
- `TribulationType.java`：劫种抽象接口 + 雷劫实现（闪电生成/伤害结算）
- `TribulationDefense.java`：防御链（宗门护盾/雷灵根减免，可插拔注册）
- `TribulationEvents.java`：渡劫生命周期事件（Started/BoltStrike/WaveEnd/Succeeded/Failed）
- `TribulationTier.java`：天骄档位（凡尘→君临万道，难度倍率/奖励百分比/颜色）
- `TribulationScalingHelper.java`：综合评判（灵根/体质/功法品质 → 档位）
- `TribulationQuality.java`：可扩展品阶接口（新增枚举自动接入）
- `TribulationConstants.java`：全部硬编码常量集中
- `TribulationSession.java`：一次渡劫的唯一运行态与存档恢复
- `TribulationBonusSnapshot.java`：按目标境界固定数值的奖励账本；降境界时停用，恢复境界时重新生效

**主处理**：`event/TribulationHandler.java`（渡劫主循环、雷击、成功/失败、突破奖励）

**调用 API**：`MinecraftForge.EVENT_BUS.post()`（事件）、`TribulationDefense.applyAll()`（防御）、`TribulationScalingHelper.tier()`（档位）、`CultivationData.getTribulationBonusSnapshots()`（固定快照账本）

---

## 三、修炼系统（Cultivation System）

**特色**：灵气吸收、修为/悟道、真元五维、突破奖励。

**核心文件**：
- `cultivation/CultivationData.java`：玩家核心数据（境界/修为/灵气/真元/悟道/突破加成/渡劫固定快照账本），Capability 存储
- `cultivation/CultivationCapability.java`：Capability 注册与获取
- `cultivation/ZhenyuanBonusHelper.java`：真元五维加成（体质→生命/盔甲/韧性、筋骨→攻击/挖掘、身法→移速/跳跃、法伤→法术伤害、气海→灵气上限/回复）
- `cultivation/qi/`：灵气生态（`QiEcosystem`、`PlayerQiAbsorptionHelper`、`ChunkQiPool`、`BlockQiSpec`）
- `cultivation/qi/consumer/`：灵气消费者（`PlayerQiConsumer`、`WanderingCultivatorConsumer`）
- `cultivation/qi/field/`：灵气场效果（`IQiFieldEffect`、`QiFieldRegistry`、`QiModifier`）
- `cultivation/qi/state/`：区块灵气状态（`ChunkQiCapability`、`ChunkQiPool`、`QiUpgradeTickHandler`）

**调用 API**：`CultivationCapability.get(player)`、`data.getRealm()/getCurrentQi()/getMaxQi()`、`ZhenyuanBonusHelper.constitutionHpBonus()`、`PlayerQiConsumer.cultivationEfficiencyPerParticle()`

---

## 四、灵根体系（Spirit Root）

**特色**：25 种灵根（五行单灵根/双灵根/变异/混沌），影响吸收效率、法术、突破奖励。

**核心文件**：
- `cultivation/SpiritRoot.java`：灵根枚举
- `cultivation/SpiritRootBonus.java`：灵根加成数据
- `cultivation/SpiritRootBonusHelper.java`：灵根加成计算（吸收倍率、法术倍率、生命）

**调用 API**：`data.getSpiritRoot()`、`SpiritRootBonusHelper.qiAbsorptionMultiplier()`、`SpiritRootBonusHelper.hpBonus()`

---

## 五、体质体系（Physique）

**特色**：13 种体质（先天剑体/无垢仙体/混沌体/绝脉石体等），特殊属性与玩法规则。

**核心文件**：
- `cultivation/Physique.java`：体质枚举 + Rarity（LOW~SPECIAL）
- `cultivation/PhysiqueBonus.java`：体质加成数据
- `cultivation/PhysiqueBonusHelper.java`：体质加成计算（生命倍率/近战/法术/灵气上限）

**调用 API**：`data.getPhysique()`、`PhysiqueBonusHelper.hpMultiplier()`、`PhysiqueBonusHelper.grantChaosBodyMinorBreakthroughSpell()`

---

## 六、法术体系（Spell）

**特色**：58 种法术（飞行/剑术/战斗/控制/辅助/神通），按境界解锁。

**核心文件**：
- `cultivation/spell/Spell.java`：法术枚举（58 种）
- `cultivation/spell/SpellElement.java` / `SpellType.java`：法术元素/类型
- `cultivation/spell/SpellWheelLayout.java`：法术轮盘布局
- `network/CastSpellPacket.java`：施法网络包
- `event/PassiveSpellHandler.java`：被动法术
- `event/ChargeableSpellHandler.java`：蓄力法术

**调用 API**：`data.hasSpell()/isSpellEnabled()`、`SpellScalingHelper`（法术缩放）、`SpellDamageSourceHelper`（伤害源）

---

## 七、功法体系（Technique）

**特色**：27 种功法（木系长寿/火系焚身/金系剑诀/帝法等），装备提供属性加成。

**核心文件**：
- `cultivation/technique/Technique.java`：功法枚举 + Bonus
- `cultivation/technique/TechniqueBonusHelper.java`：功法加成计算
- `cultivation/technique/TechniqueLoadoutHelper.java`：功法装配
- `cultivation/technique/WeaponBonusHelper.java`：武器加成
- `event/TechniqueEffectHandler.java`：功法效果（生命/移速/击退抗性/夜视等）

**调用 API**：`TechniqueBonusHelper.equippedOf()`、`TechniqueBonusHelper.maxHpBonus()`、`data.getEquippedTechniqueId()`

---

## 八、身份体系（Identity）

**特色**：23 种初始身份，决定初始属性/灵根/体质/寿命/轮回表现。

**核心文件**：
- `cultivation/Identity.java`：身份枚举
- `cultivation/draw/`：身份抽取（`DrawCard`、`IdentityDrawDeck`、`IdentityDrawSampler`）
- `event/IdentityDrawHandler.java`：身份抽取处理
- `client/screen/IdentityDrawScreen.java`：抽取界面

**调用 API**：`IdentityDrawSampler`（抽取）、`data.setIdentity()`、`ConfirmIdentityDrawPacket`

---

## 九、炼丹系统（Alchemy）

**特色**：筑基丹/金丹丹/血气丹等，五级品质，炼丹炉方块，数据包配方。

**核心文件**：
- `cultivation/alchemy/`：`AlchemyRank`、`AlchemyRecipe`、`AlchemyRecipes`、`PillEffectSpec`、`PillTier`
- `cultivation/alchemy/datapack/PillEffectSpecLoader.java`：丹药数据包加载
- `block/alchemy/`：`AlchemyCoreBlock`、`AlchemyCoreBlockEntity`、`AlchemyFurnaceStructure`
- `inventory/AlchemyMenu.java`、`client/screen/AlchemyScreen.java`
- `network/ExecuteAlchemyPacket.java`

**调用 API**：`AlchemyRecipes`（配方）、`AlchemyCoreBlockEntity.deductQi()`、`PillEffectSpecLoader`（数据包）

---

## 十、炼器系统（Refining）

**特色**：玄铁剑/青木剑等法宝，五级品质，炼器炉，数据包配方。

**核心文件**：
- `cultivation/refining/`：`RefiningRank`、`RefiningRecipe`、`RefiningRecipes`
- `block/refining/`：`RefiningCoreBlock`、`RefiningCoreBlockEntity`、`RefiningFurnaceStructure`
- `inventory/RefiningMenu.java`、`client/screen/RefiningScreen.java`
- `network/ExecuteRefiningPacket.java`、`SetRefiningAutoRetryPacket.java`

**调用 API**：`RefiningRecipes`、`RefiningCoreBlockEntity.deductQi()`、`RefiningScreen`（自动重试）

---

## 十一、阵法体系（Formation）

**特色**：7 类阵法（聚灵/护盾/时停/长生/禁飞/迷宫/丰收），五级阵盘，阵法核心/符文。

**核心文件**：
- `block/formation/`：13 个阵法方块（`FormationCorePlateBlockEntity`、`FormationRuneBlockEntity`、各 Flag 方块）
- `cultivation/qi/formation/`：`FormationType`、`CoreTier`
- `cultivation/qi/field/`：`IQiFieldEffect`、`QiFieldRegistry`
- `inventory/FormationMenu.java`、`client/screen/FormationScreen.java`
- `event/FormationRuneHandler.java`、`event/FormationMeridianBodyHandler.java`
- `client/ClientFormationRangePreview.java`、`client/FormationSurveyRenderer.java`

**调用 API**：`FormationType`（阵法类型）、`FormationCorePlateBlockEntity.getCurrentQi()`、`QiFieldRegistry`（灵气场）

---

## 十二、宗门系统（Sect）

**特色**：宗门令牌/护盾/建筑/角色/任务/NPC。

**核心文件**：
- `cultivation/sect/`：`SectNameGenerator`、`SectRole`、`SectSavedData`
- `item/SectTokenItem.java`：宗门令牌
- `block/formation/SectProtectionFlagBlock.java`、`SectProtectionBarrierBlock.java`
- `event/SectProtectionDomeHandler.java`：宗门护盾
- `event/SectCombatHandler.java`：宗门战斗
- `client/screen/SectScreen.java`、`SectJoinDialogueScreen.java`
- `network/`：`JoinSectPacket`、`SectTaskActionPacket`、`OpenSectScreenPacket` 等

**调用 API**：`SectSavedData`（宗门数据）、`SectProtectionDomeHandler.domeContaining()`、`SectTokenItem.playerHasTokenForCore()`

---

## 十三、地府与轮回（Difu & Reincarnation）

**特色**：死亡进地府、灵魂状态、转世重生、索命使、牛头马面、寿元。

**核心文件**：
- `event/SoulStateHandler.java`：灵魂状态/地府传送
- `event/ReincarnationManager.java`：转世管理
- `event/SoulReaperOrderHandler.java`：索命使
- `entity/npc/SoulReaperEntity.java`：索命使实体
- `client/screen/DeathChoiceScreen.java`、`ReincarnationScreen.java`
- `network/`：`DeathChoicePacket`、`ReincarnationChoicePacket`、`SoulStatePacket` 等
- `worldgen/DifuVillageFeature.java`：地府村庄

**调用 API**：`SoulStateHandler.resolveDeathChoice()`、`data.isSoulState()`、`ReincarnationManager`

---

## 十四、妖兽体系（Beast）

**特色**：独立妖兽境界（凡兽→妖圣），妖兽可修炼成长。

**核心文件**：
- `cultivation/beast/`：`BeastCapability`、`BeastCultivationData`
- `cultivation/realm/BeastRealm.java`：妖兽境界
- `event/BeastCultivationHandler.java`：妖兽修炼

**调用 API**：`BeastCapability.get()`、`BeastCultivationData`

---

## 十五、NPC 体系（Entity）

**特色**：游历修士（对话/交易/战斗/御剑）、索命使、法术投射物。

**核心文件**：
- `entity/npc/WanderingCultivatorEntity.java`：游历修士（3767 行，超大 NPC）
- `entity/npc/`：`CultivatorTrades`、`NpcSpellCaster`、`CultivatorRealmRoller`、`SoulReaperEntity`、`CorpseEntity`
- `entity/npc/ai/`：`CultivatorFlightCombatGoal`、`CultivatorRangedKitingGoal`、`CultivatorSpellAttackGoal`
- `entity/`：16 个法术投射物（`SwordProjectileEntity`、`MeteorEntity`、`BuddhaFireLotusEntity`、`ShockwaveEntity` 等）
- `client/screen/WanderingCultivatorScreen.java`：修士交互界面
- `client/renderer/WanderingCultivatorRenderer.java`：修士渲染

**调用 API**：`WanderingCultivatorEntity`（NPC 数据）、`NpcSpellCaster`（NPC 施法）、`CultivatorTrades`（交易）

---

## 十六、物品与装备（Item）

**特色**：36+ 物品（丹药/法宝/灵石/书籍/阵旗/令牌/生物蛋）。

**核心文件**：
- `item/`：29 个物品（`PillItem`、`SpiritStoneItem`、`SpellBookItem`、`TechniqueBookItem`、`SectTokenItem`、`RealmTokenItem` 等）
- `item/weapon/`：7 个武器（`TieredWeapon`、`ChiYanSwordItem`、`HanBingSwordItem`、`QingMuSwordItem`、`XuanIronSwordItem`、`SpiritSwordItem`、`SoulHookItem`）
- `registry/ModItems.java`：物品注册

**调用 API**：`ModItems.XXX.get()`、`TieredWeapon`（武器品质）、`PillItem`（丹药效果）

---

## 十七、网络系统（Network）

**特色**：89 个网络包，覆盖突破/面板/转世/飞行/阵法/宗门/交易。

**核心文件**：
- `network/ModNetwork.java`：网络通道注册
- `network/`：89 个 Packet（`RequestBreakthroughPacket`、`CastSpellPacket`、`SyncCultivationDataPacket`、`RealmSelectionPacket`、`SpendZhenyuanPacket` 等）

**调用 API**：`ModNetwork.CHANNEL.send()`、`PacketDistributor`、`ctx.enqueueWork()`

---

## 十八、世界生成（World Gen）

**特色**：地府维度、宗门建筑、灵脉、灵石矿脉。

**核心文件**：
- `worldgen/`：`SectSettlementFeature`（宗门建筑）、`DifuVillageFeature`（地府村庄）、`NaiheBridgeBuilder`（奈何桥）、`SpiritVeinSpringFeature`（灵脉）、`CultivationBuildingFeature`
- `registry/ModDimensions.java`：地府维度
- `registry/ModFeatures.java`：世界生成注册

**调用 API**：`ModFeatures`（注册）、`SectSettlementFeature`（宗门生成）、`ModDimensions.DIFU`

---

## 十九、客户端界面（Client UI）

**特色**：修仙主面板、HUD、修士交互、境界编辑器、令牌选择。

**核心文件**：
- `client/screen/CultivationScreen.java`：修仙主面板（3933 行，属性/灵根/体质/法术/功法/宗门/突破）
- `client/CultivationHud.java`：左上角 HUD（境界/修为/灵气/悟道/状态）
- `client/EntityStatusHudRenderer.java`：生物头顶生命条（GUI 空间渲染，兼容光影）
- `client/screen/`：19 个界面（`AlchemyScreen`、`FormationScreen`、`SectScreen`、`WanderingCultivatorScreen`、`DeathChoiceScreen` 等）
- `client/screen/widget/`：7 个自定义控件（`CinnabarButton`、`BambooTabButton`、`DrawCardWidget` 等）
- `client/ClientSetup.java`：客户端统一注册

**调用 API**：`GuiGraphics`、`RenderGuiOverlayEvent`、`Screen`、`CinnabarButton`

---

## 二十、命令与配置（Command & Config）

**特色**：灵气命令、离线认证、通用/客户端配置。

**核心文件**：
- `command/QiCommand.java`：灵气命令
- `command/OfflineAuthCommand.java`：离线认证
- `config/ModCommonConfig.java`：通用配置
- `config/ModClientConfig.java`：客户端配置（HUD 位置等）

**调用 API**：`Commands.literal()`、`ModCommonConfig`、`ModClientConfig.hudPosition()`

---

## 二十一、音效与特效（Effects & Particles）

**特色**：12+ 状态效果、3 种粒子、大量客户端特效。

**核心文件**：
- `cultivation/effect/`：12 个效果（`BloodBurnEffect`、`TimeStasisEffect`、`RootedEffect`、`GravitySuppressionEffect` 等）
- `client/particle/`：3 种粒子（`AmbientQiParticle`、`BreakthroughParticle`、`QiAbsorbParticle`）
- `client/`：大量特效（`TimeStasisClientEffects`、`RealmPressureClientEffects`、`SoulVisualHandler`、`QiShieldVisualHandler` 等）
- `registry/ModEffects.java`、`ModParticles.java`：注册

**调用 API**：`MobEffect`、`ParticleProvider`、`RenderLevelStageEvent`、`RenderSystem`

---

## 二十二、飞行系统（Flight）

**特色**：御剑飞行、灵气飞行、虚遁。

**核心文件**：
- `flight/`：`CultivationFlightHandler`、`CultivationFlightEvents`、`CultivationFlightClientHandler`、`CultivationSwordFlightRenderer`
- `network/QiFlightTogglePacket.java`：飞行切换
- `event/VoidEscapeHandler.java`：虚遁

**调用 API**：`data.isQiFlightToggled()`、`CultivationFlightHandler`、`VoidEscapeHandler.exit()`

---

## 二十三、工具与辅助（Util）

**特色**：格式化、随机池、伤害辅助、地形破坏。

**核心文件**：
- `util/`：`CompactNumberFormat`、`CultivationRandomPools`、`SpellDamageSourceHelper`、`SpellLightningHelper`、`SpellScalingHelper`、`SpellTerrainDestructionHelper`、`TooltipUtils`、`QiStorageBlocks`、`ShimmerColors`、`OfflineAuthStore`

**调用 API**：`TooltipUtils`（物品提示）、`SpellScalingHelper`（法术缩放）、`SpellTerrainDestructionHelper`（地形破坏）

---

## 二十四、战利品与数据包（Loot & Datapack）

**特色**：自定义战利品、数据包配方。

**核心文件**：
- `loot/AddItemLootModifier.java`：战利品修改
- `registry/ModLootModifiers.java`：注册
- `cultivation/alchemy/datapack/PillEffectSpecLoader.java`：丹药数据包
- `cultivation/qi/datapack/BlockQiSpecLoader.java`：方块灵气数据包

**调用 API**：`LootModifier`、`AddReloadListenerEvent`、`PreparableReloadListener`

---

## 二十五、注册中心（Registry）

**特色**：统一注册所有内容。

**核心文件**（`registry/`）：
- `ModBlocks`、`ModItems`、`ModEntities`、`ModBlockEntities`、`ModEffects`、`ModParticles`、`ModMenuTypes`、`ModRecipes`、`ModCreativeTabs`、`ModDimensions`、`ModFeatures`、`ModLootModifiers`

**调用 API**：`DeferredRegister`、`RegistryObject`、`ForgeRegistries`

---

## 目录结构总览

```
src/main/java/com/friday/cultivation/
├── FridayCultivationMod.java      # 主类
├── block/                         # 方块（炼丹/炼器/阵法/灵脉/杂项）
├── client/                        # 客户端（HUD/特效/渲染/界面/粒子）
│   ├── model/ renderer/ particle/ screen/ screen/widget/
├── command/ config/               # 命令与配置
├── cultivation/                   # 核心数据（境界/灵根/体质/修炼/真元/法术/功法）
│   ├── realm/ qi/ alchemy/ refining/ beast/ draw/ effect/
│   ├── spell/ technique/ sect/
├── entity/                        # 实体（投射物/NPC）
│   └── npc/ ai/
├── event/                         # 事件处理（58 个）
│   └── tribulation/               # 渡劫系统（8 个）
├── flight/ gametest/ inventory/ item/ item/weapon/
├── loot/ network/ registry/ util/ worldgen/
src/main/resources/
├── assets/friday_cultivation/     # 贴图/模型/lang/Patchouli
└── data/                          # 数据包（配方/战利品）
```

---

## 版权说明

本项目为「小翔的修仙世界」的**学习复刻版本**，仅供学习交流使用。
