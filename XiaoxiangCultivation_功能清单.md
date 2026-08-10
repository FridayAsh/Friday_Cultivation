# Xiaoxiang Cultivation（小翔的修仙世界）完整功能清单

> 来源: `JAR包/[小翔的修仙世界] xiaoxiang_cultivation-0.1.1038.jar`
> 分析日期: 2026-08-03
> 目标项目: `Friday_Cultivation` — 复刻此模组

---

## 基本信息
- **modId**: `xiaoxiang_cultivation`
- **版本**: `0.1.1038`
- **MC 版本**: 1.20.1
- **Forge 版本**: 47+
- **类文件**: 约 476 个 class
- **资源文件**: 约 3700+ 个
- **作者**: Xiaoxiang

---

## 一、修仙境界系统（Realm System）— 核心

12 个境界，每个有 4 个子阶段（初期/中期/后期/圆满）：

| # | 境界 | 英文ID | 子阶段 |
|---|------|--------|--------|
| 1 | 凡人 | mortal | early/middle/late/peak |
| 2 | 炼气期 | qi_refining | ↑ |
| 3 | 筑基期 | foundation_building | ↑ |
| 4 | 金丹期 | golden_core | ↑ |
| 5 | 元婴期 | nascent_soul | ↑ |
| 6 | 化神期 | soul_formation | ↑ |
| 7 | 炼虚期 | void_refining | ↑ |
| 8 | 合体期 | body_integration | ↑ |
| 9 | 大乘期 | mahayana | ↑ |
| 10 | 渡劫期 | tribulation_transcendence | ↑ |
| 11 | 地仙（真仙） | true_immortal | ↑ |
| 12 | 散仙 | loose_immortal | 1~9 劫散仙 |

**关键类**: `Realm.java` (enum), `SubStage.java` (enum)
**属性**: maxQi, baseLifespan, tribulationCount, tribulationBoltsPerWave, tribulationStrikeDamage, qiShieldReductionPercent, baseAbsorbMult, isCultivator()

---

## 二、妖兽境界（Beast Realm）

| 境界 | 英文ID |
|------|--------|
| 凡兽 | mortal_beast |
| 妖兵 | spirit_soldier |
| 妖将 | spirit_general |
| 妖帅 | spirit_marshal |
| 妖王 | spirit_king |
| 妖皇 | spirit_emperor |
| 妖帝 | spirit_lord |
| 妖圣 | spirit_saint |

**关键类**: `BeastRealm.java` (enum), `BeastCapability.java`, `BeastCultivationData.java`

---

## 三、灵根系统（Spirit Root）— 25 种

### 天灵根
- 金天灵根 `heavenly_metal`
- 木天灵根 `heavenly_wood`
- 水天灵根 `heavenly_water`
- 火天灵根 `heavenly_fire`
- 土天灵根 `heavenly_earth`
- 天道隐灵根 `heavenly_hidden`
- 先天剑体 `heavenly_sword`（旧，已迁移为体质"剑骨"）

### 变异灵根
- 冰变异灵根 `mutant_ice`
- 雷变异灵根 `mutant_lightning`

### 双灵根（10 种组合）
- 金木 `dual_metal_wood` / 金水 `dual_metal_water` / 金火 `dual_metal_fire` / 金土 `dual_metal_earth`
- 木水 `dual_wood_water` / 木火 `dual_wood_fire` / 木土 `dual_wood_earth`
- 水火 `dual_water_fire` / 水土 `dual_water_earth`
- 火土 `dual_fire_earth`

### 多灵根
- 三灵根 `triple`
- 四灵根 `quadruple`
- 五灵根 `five_root`
- 五行混沌体 `five_element_chaos`（旧数据）

### 特殊
- 无灵根 `none`
- 绝脉石体 `broken_vein_body`

**关键类**: `SpiritRoot.java` (enum, 含 Rarity 和 Bonus)

---

## 四、体质系统（Physique）— 13 种

| 体质 | 英文ID | 稀有度 |
|------|--------|--------|
| 凡体 | mortal_body | 下品 |
| 绝脉石体 | broken_vein_body | - |
| 先天剑体 | innate_sword_body | - |
| 无垢仙体 | immortal_body | - |
| 五行混沌体 | five_element_chaos_body | - |
| 逆五行体 | inverse_five_elements_body | - |
| 天火道体 | heavenly_fire_body | - |
| 玄冰体 | mystic_ice_body | - |
| 剑骨 | sword_bone | - |
| 丹心灵体 | alchemy_heart_body | - |
| 阵脉体 | formation_meridian_body | - |
| 血煞体 | blood_fiend_body | - |
| 混沌体 | chaos_body | - |

**关键类**: `Physique.java` (enum, 含 Rarity 和 Bonus), `PhysiqueBonus.java`, `PhysiqueBonusHelper.java`

---

## 五、筑基/金丹品质（Dao）

### 筑基道 (FoundationDao)
| 品质 | 英文ID |
|------|--------|
| 无 | none |
| 人道筑基 | human |
| 血道筑基 | blood |
| 地道筑基 | earth |
| 天道筑基 | heaven |

### 金丹道 (GoldenCoreDao)
| 品质 | 英文ID |
|------|--------|
| 无 | none |
| 人品金丹 | human |
| 血煞金丹 | blood |
| 地灵金丹 | earth |
| 天道紫丹 | heaven |

**属性**: lifespanBonus, spellDamageMult, spellQiCostMult, hpMult, bloodSpellDamageMult, bloodSpellQiCostMult, bodyDefenseBonus, cultivationEfficiencyBonus, qiRecoveryPerSecondBonus, meleeDamageBonus, tribulationStrikes, tribulationDamage

**关键类**: `FoundationDao.java`, `GoldenCoreDao.java`, `FoundationDaoBonusHelper.java`, `GoldenCoreDaoBonusHelper.java`

---

## 六、法术系统（Spell）— 58 种

### 主动法术 (ACTIVE)

| 法术名 | 英文ID | 属性 | 品级 |
|--------|--------|------|------|
| 火球术 | fireball | 火 | 下品 |
| 大火球术 | great_fireball | 火 | 中品 |
| 万剑归宗 | sword_convergence | 金 | 上品 |
| 天星坠 | star_fall | 火 | 极品 |
| 寒冰箭 | ice_lance | 冰 | 下品 |
| 雷霆术 | lightning_bolt | 雷 | 下品 |
| 掌心雷 | palm_thunder | 雷 | 中品 |
| 风刃斩 | wind_blade | 木 | 下品 |
| 毒雾术 | poison_mist | 木 | 下品 |
| 凋零之触 | wither_touch | 木 | 中品 |
| 飞矢三叠 | arrow_volley | 金 | 下品 |
| 起爆术 | earth_spike | 土 | 下品 |
| 石弹术 | stone_bullet | 土 | 下品 |
| 穿天锥 | heaven_piercing_cone | 土 | 中品 |
| 烈日闪焰 | sun_flare | 火 | 极品 |
| 缩地术 | shadow_step | 无 | 上品 |
| 御风术 | soaring | 无 | 下品 |
| 疾风步 | speed_burst | 无 | 下品 |
| 隐遁术 | invisibility | 无 | 中品 |
| 回春术 | healing_touch | 无 | 下品 |
| 金钟罩 | iron_body | 无 | 下品 |
| 飞剑术 | flying_sword | 金 | 下品 |
| 剑气 | sword_aura | 金 | 中品 |
| 裂天剑气 | sky_splitting_sword_aura | 金 | 极品 |
| 清心术 | clear_mind | 无 | 下品 |
| 清心神咒 | clear_mind_incantation | 无 | 中品 |
| 无垢仙诀 | immortal_incantation | 无 | 极品 |
| 灵气飞行 | qi_flight | 无 | 中品 |
| 灵气传输 | qi_transfer | 无 | 下品 |
| 破妄法眼 | truth_sight_eye | 无 | 中品 |
| 时间停滞 | time_stasis | 无 | 仙品 |
| 太上均命 | taishang_life_balance | 无 | 仙品 |
| 佛怒火莲 | buddha_fire_lotus | 木火 | 仙品 |
| 锁灵术 | spirit_lock | 无 | 上品 |
| 解灵术 | spirit_unlock | 无 | 上品 |
| 勾魂 | soul_hook | 无 | - |
| 金丹自爆 | core_self_destruct | 无 | - |
| 元婴出窍 | nascent_soul_out_of_body | 无 | - |
| 神识外放 | divine_sense | 无 | - |
| 法身显化 | dharma_body_manifestation | 无 | - |
| 虚遁 | void_escape | 无 | - |
| 虚空踏步 | void_step | 无 | - |
| 境界威压 | realm_pressure | 无 | - |
| 御剑飞行 | sword_flight | 金 | 上品 |
| 辟谷 | bigu | 无 | - |
| 灵息养器 | qi_mending | 无 | - |

### 被动法术 (PASSIVE)

| 法术名 | 英文ID | 属性 | 品级 |
|--------|--------|------|------|
| 灵气免伤 | qi_shield | 无 | - |
| 灵气视野 | spirit_vision | 无 | 下品 |
| 阴阳眼 | yin_yang_eye | 无 | 中品 |
| 水中行 | water_affinity | 水 | 下品 |
| 夜视术 | night_eye | 无 | 下品 |
| 火体不侵 | fire_protection | 火 | 下品 |
| 灵气自愈 | slow_regen | 无 | 下品 |
| 御毒体质 | poison_immunity | 无 | 下品 |
| 寒冰筑路 | frost_walker | 冰 | 下品 |
| 追魂印 | soul_mark | 无 | - |
| 嗜血咒 | bloodthirst_curse | 无 | - |
| 幽灵飞行 | ghost_flight | 无 | - |

**关键类**: `Spell.java` (enum), `SpellType.java` (ACTIVE/PASSIVE), `SpellElement.java` (NONE/METAL/WOOD/WATER/FIRE/EARTH/ICE/LIGHTNING/WOOD_FIRE), `ItemTier.java` (LOW/MID/HIGH/SUPREME/IMMORTAL)

---

## 七、功法系统（Technique）— 约 27 种

| 功法 | 英文ID |
|------|--------|
| 功法残篇 | fragment |
| 基础鬼道功法 | ghost_dao_basic |
| 凡躯炼体诀 | basic_body |
| 凡心静气诀 | basic_mind |
| 铁布衫 | iron_skin |
| 金元剑诀 | metal_sword |
| 木灵养生诀 | wood_spring |
| 枯木逢春诀 | deadwood_rebirth |
| 水行流云诀 | water_stream |
| 火阳焚心诀 | fire_yang |
| 土厚山岳诀 | earth_mountain |
| 冰魄凝霜诀 | ice_frost |
| 风步轻灵诀 | wind_step |
| 幽影潜形诀 | shadow_cloak |
| 火神不灭诀 | fire_immortal |
| 金刚不坏诀 | vajra_body |
| 心经聚灵诀 | heart_sutra |
| 剑心通明诀 | sword_heart |
| 九渊吞灵诀 | nine_abyss |
| 斩妖除魔诀 | demon_slayer |
| 玄龟龟息诀 | turtle_shell |
| 神锻炼器诀 | divine_forge |
| 天丹炼药诀 | heavenly_elixir |
| 五行调和诀 | five_element |
| 天仙合道诀 | celestial_immortal |
| 吞天诀 | sky_devouring |
| 五行混沌诀 | five_element_chaos |
| 无垢仙诀 | immortal |
| 青帝长生诀 | qingdi_longevity |

**关键类**: `Technique.java` (enum), `TechniqueBonusHelper.java`, `TechniqueLoadoutHelper.java`, `WeaponBonusHelper.java`

---

## 八、身份系统（Identity）— 23 种初始身份

| 身份 | 英文ID |
|------|--------|
| 凡人之子 | mortal_child |
| 凡人农户 | mortal_farmer |
| 渔夫 | fisherman |
| 山野猎户 | hunter |
| 铁匠学徒 | blacksmith_apprentice |
| 商贾之子 | merchant_child |
| 书院童生 | academy_student |
| 将军之子 | general_child |
| 僧人 | monk |
| 道士 | taoist |
| 弃婴 | abandoned_baby |
| 海盗 | pirate |
| 采药童子 | herb_gatherer |
| 流亡公主 | exiled_princess |
| 破落散修 | lone_cultivator |
| 隐士弟子 | hermit_disciple |
| 落魄世家 | fallen_noble |
| 山贼头目 | bandit_chief |
| 妖兽后裔 | beast_descendant |
| 宗门外门弟子 | sect_outer_disciple |
| 丹鼎派外门弟子 | alchemy_sect_outer |
| 万剑宗外门弟子 | sword_sect_outer |
| 阵师学徒 | formation_apprentice |

**关键类**: `Identity.java`, `IdentityDrawDeck.java`, `IdentityDrawSampler.java`, `IdentityDrawHandler.java`

---

## 九、炼丹系统（Alchemy）

- **炼丹核心方块**: `AlchemyCoreBlock` + `AlchemyCoreBlockEntity`
- **炼丹炉多方块结构**: `AlchemyFurnaceStructure`
- **丹药品级** (PillTier): LOW/MID/HIGH/SUPREME/IMMORTAL
- **炼丹等级** (AlchemyRank): 学徒→1~9级→仙级 (11级)
- **炼丹配方** (AlchemyRecipe/AlchemyRecipes)
- **药效规格** (PillEffectSpec/PillEffectSpecs)
- **丹药类型**:
  - 修为丹 (cultivation_pill)
  - 回灵丹 (qi_recovery_pill)
  - 燃血丹 (blood_burn_pill)
  - 清心丹 (clear_mind_pill)
  - 神行丹 (divine_stride_pill)
  - 回春丹 (rejuvenation_pill)
  - 特殊: 筑基丹/结丹丸/血煞结丹丸/变性丹/回想丹/返老还童丹/生生造化丹 等
- **界面**: `AlchemyScreen.java`, `AlchemyMenu.java`
- **数据包**: `PillEffectSpecLoader.java`

---

## 十、炼器系统（Refining）

- **炼器核心方块**: `RefiningCoreBlock` + `RefiningCoreBlockEntity`
- **炼器炉多方块结构**: `RefiningFurnaceStructure`
- **炼器等级** (RefiningRank): 学徒→1~9级→仙级
- **炼器配方** (RefiningRecipe/RefiningRecipes)
- **界面**: `RefiningScreen.java`, `RefiningMenu.java`

---

## 十一、灵气生态系统（Qi System）

### 核心组件
- **区块灵气池** (ChunkQiPool) — 每个区块的灵气状态
- **灵气元素** (QiElement): 纯/金/木/水/火/土/冰/雷 (8种)
- **区块灵气Capability** (ChunkQiCapability)
- **灵气生态系统** (QiEcosystem)

### 灵脉
- **灵脉核心** (SpiritVeinCoreBlock/BlockEntity): 5级 (low/mid/high/supreme/immortal)
- **灵脉泉眼** (SpiritVeinSpring)
- **灵石矿**: 5级矿石
- **灵石物品**: 下品/中品/上品/极品

### 灵气流
- **灵气源接口** (IQiSource)
- **灵气消耗者接口** (IQiConsumer)
  - 玩家消耗 (PlayerQiConsumer)
  - 修士NPC消耗 (WanderingCultivatorConsumer)
- **灵气领域效果** (IQiFieldEffect/QiFieldRegistry/QiModifier)
- **玩家灵气吸收** (PlayerQiAbsorptionHelper)
- **自然灵气生成器** (NaturalQiSpawner)

### 方块灵气
- **方块灵气规格** (BlockQiSpec/BlockQiSpecs)
- **方块升级规则** (BlockUpgradeRule)
- **方块降级规则** (BlockDegradeRule)
- **数据包加载** (BlockQiSpecLoader)

### 状态
- **区块灵气状态** (BlockQiState)
- **升级特效** (UpgradeFx)
- **降级特效** (DegradeFx)

---

## 十二、阵法系统（Formation）

### 阵法类型 (FormationType) — 7 种
1. 聚灵阵 `QI_GATHERING` — 加速灵气聚集
2. 护宗大阵 `SECT_PROTECTION` — 生成保护罩
3. 枯荣阵 `WITHER_GROWTH` — 加速/抑制植物生长
4. 回春阵 `REJUVENATION` — 生命恢复
5. 禁空阵 `FLIGHT_BAN` — 禁止飞行
6. 迷踪阵 `MAZE` — 迷宫效果
7. 农收阵 `FARM_HARVEST` — 自动收割

### 核心等级 (CoreTier) — 5 级
low/mid/high/supreme/immortal (对应 5 级 ItemTier)

### 方块
- 阵盘 (FormationCorePlateBlock/Entity) — 阵法核心
- 阵旗 (FormationFlagBlock) — 7 类型 × 5 等级 = 35 种旗
- 符文石 (FormationRuneBlock/Entity)
- 护宗屏障 (SectProtectionBarrierBlock)

### 界面
- `FormationScreen.java`
- `FormationMenu.java`

---

## 十三、宗门系统（Sect）

- **宗门角色** (SectRole): 老祖/宗主/长老/内门弟子/外门弟子/守山弟子/杂役/无 (8种)
- **宗门数据保存** (SectSavedData)
- **宗门名生成器** (SectNameGenerator)
- **宗门界面** (SectScreen)
- **宗门加入对话框** (SectJoinDialogueScreen)
- **宗门任务系统** (SectTask)
- **宗门友军伤害开关**
- **宗门护盾防护罩** (SectProtectionDomeHandler)
- **宗门战斗处理** (SectCombatHandler)
- **宗门客户端Hook** (SectClientHooks)
- **宗门令牌** (SectTokenItem)
- **宗门驻地世界生成** (SectSettlementFeature)

---

## 十四、修仙数据系统（CultivationData）

核心玩家 Capability，管理：
- 境界/子阶段 (Realm + SubStage)
- 灵气值 (Qi)
- 寿命 (Lifespan)
- 灵根 (SpiritRoot)
- 体质 (Physique)
- 筑基道 (FoundationDao)
- 金丹道 (GoldenCoreDao)
- 真元分配 (Zhenyuan) — 可分配属性点
- 已学法术列表
- 已装备功法列表
- 宗门归属
- 身份信息
- 修炼效率/加成

**关键类**: `CultivationData.java`, `CultivationCapability.java`, `CultivationBonusCategory.java`

---

## 十五、真元系统（Zhenyuan）

可分配的修仙属性点：
- 法伤 (spell_power) — 每点 +5% 法术基础伤害
- 其他属性（共约 12 种）

**关键类**: `ZhenyuanBonusHelper.java`, `ZhenyuanJumpHandler.java`, `ZhenyuanMiningSpeedHandler.java`

---

## 十六、客户端界面系统

### 界面 (Screens)
1. `CultivationScreen` — 修仙主界面（查看所有修仙数据）
2. `SpellWheelScreen` — 法术轮盘（选择施放法术）
3. `DeathChoiceScreen` — 死亡选择界面
4. `ReincarnationScreen` — 转世界面
5. `LooseImmortalChoiceScreen` — 散仙选择界面
6. `IdentityDrawScreen` — 身份抽卡界面
7. `StatEditorScreen` — 真元属性编辑
8. `AlchemyScreen` — 炼丹界面
9. `RefiningScreen` — 炼器界面
10. `FormationScreen` — 阵法界面
11. `SectScreen` — 宗门界面
12. `SectJoinDialogueScreen` — 加入宗门对话框
13. `WanderingCultivatorScreen` — 修士交易界面
14. `SoulReaperTargetScreen` — 勾魂目标选择
15. `TimeAccelerationChoiceScreen` — 时间加速选择
16. `EditNameScreen` — 编辑名称
17. `XiaoxiangConfigScreen` — 配置界面

### HUD 叠加层
- `CultivationHud` — 境界/修为/真元 HUD
- `QiShieldHudOverlay` — 灵气护盾 HUD
- `SoulHookProgressHud` — 勾魂进度 HUD
- `StarFallProgressHud` — 天星坠进度 HUD

### 客户端视觉效果
- `AmbientQiHandler` — 环境灵气粒子
- `BuddhaFireLotusClientEffects` — 佛怒火莲特效
- `ChargingPreviewHandler` — 蓄力预览
- `DeathSequenceClientEffects` — 死亡序列特效
- `DharmaBodyClientEffects` — 法身特效
- `DivineSenseClientEffects` — 神识外放特效
- `FirstPersonChargeGlowOverlay` — 第一人称蓄力发光
- `FlagGlowDecorator` — 阵旗发光
- `ImmortalNightVisionHandler` — 仙人夜视
- `LifeBalanceVisualHandler` — 太上均命视觉
- `LightningStrikeBurstHandler` — 雷霆爆发
- `NascentSoulBodyVisualHandler` — 元婴出窍视觉
- `PalmThunderVisualHandler` — 掌心雷视觉
- `PillGlowDecorator` — 丹药发光
- `PillItemEntityGlowHandler` — 掉落物丹药发光
- `QiFlightClientHandler` — 灵气飞行
- `QiShieldVisualHandler` — 灵气护盾视觉
- `RealmPressureClientEffects` — 境界威压特效
- `SectClientHooks` — 宗门客户端
- `SoulHookVisualHandler` — 勾魂视觉
- `SoulVisibilityClient` — 灵魂可见性
- `SoulVisualHandler` — 灵魂视觉
- `SpiritLockVisualHandler` — 锁灵视觉
- `SwordFlightClientRenderer` — 御剑飞行渲染
- `TimeStasisClientEffects` — 时间停滞特效
- `TribulationCloudClientEffects` — 天劫云特效
- `VoidEscapeClientEffects` — 虚遁特效
- `VoidStepClientBoostHandler` — 虚空踏步加速
- `VoidStepKeyHandler` — 虚空踏步按键
- `ClientSoulReaperTargetHooks` — 勾魂目标选择

---

## 十七、战斗/伤害系统

- **法术伤害源** (SpellDamageSourceHelper)
- **攻击加成处理** (AttackBonusHandler)
- **体防系统** (BodyDefenseHelper) — 可开关
- **灵气护盾** (QiShieldHandler) — 灵气吸收伤害
- **境界威压** (RealmPressureHandler) — 高境界压制低境界
- **武器加成**: 法术伤害加成/灵气消耗减免
- **体质战斗效果** (PhysiqueCombatEffectHandler)
- **法术缩放** (SpellScalingHelper)
- **法术闪电** (SpellLightningHelper)
- **法术地形破坏** (SpellTerrainDestructionHelper)

---

## 十八、实体系统

### 实体
- 修士 NPC (WanderingCultivatorEntity)
- 灵魂收割者 (SoulReaperEntity)
- 尸体 (CorpseEntity)
- 坐垫实体 (SeatEntity) — 打坐

### 法术投射物
- 火球 (XiaoxiangFireballEntity)
- 大火球 (GreatFireballEntity)
- 寒冰 (IceShellEntity)
- 流星 (MeteorEntity)
- 剑气 (SwordAuraEntity)
- 裂天剑气 (SkySplittingSwordAuraEntity)
- 飞剑 (SwordProjectileEntity)
- 石弹 (StoneBulletEntity)
- 穿天锥 (HeavenPiercingConeEntity)
- 掌心雷球 (PalmThunderOrbEntity)
- 灵气球 (QiOrbEntity)
- 冲击波 (ShockwaveEntity)
- 佛怒火莲 (BuddhaFireLotusEntity)
- 天迹 (SkyTrailEntity)
- 蘑菇云 (MushroomCloudEntity)

### NPC AI
- `CultivatorFlightCombatGoal` — 修士飞行战斗AI
- `CultivatorRangedKitingGoal` — 修士远程风筝AI
- `CultivatorSpellAttackGoal` — 修士法术攻击AI
- `NpcSpellCaster` — NPC法术施放器
- `NpcPassiveSpellHandler` — NPC被动法术

### NPC 相关
- `CultivatorNames` — 修士名称生成
- `CultivatorRealmRoller` — 修士境界随机
- `CultivatorTrades` — 修士交易
- `SpiritStonePayment` — 灵石支付
- `SundryPricing` — 杂物定价

---

## 十九、维度 — 地府 (Difu)

- 独立维度 `xiaoxiang_cultivation:difu`
- 4 种群系:
  - difu_normal — 地府平原
  - difu_soul_canyon — 地府魂谷
  - difu_chains — 地府锁链
  - difu_bone — 地府骨地
- 世界生成:
  - 锁链柱 (difu_chain_pillar)
  - 大锁链 (difu_big_chain)
  - 骨堆 (difu_bone_cluster)
  - 骨骨架 (difu_bone_skeleton)
  - 地府植物 (difu_flora)
  - 下界砖矿脉 (difu_nether_brick_vein)
  - 灵魂斑块 (difu_soul_patch)
  - 地府村庄 (difu_village)
- 奈河桥 (NaiheBridgeBuilder)
- 地府氛围处理 (DifuAmbientHandler)

---

## 二十、死亡/转世/灵魂系统

- 死后化为灵魂状态
- 死亡选择 (DeathChoice): 转世重生/成为散仙/保持灵魂
- 转世重抽身份和体质
- 灵魂钩 (SoulHook) — 勾取灵魂
- 灵魂收割者 — 追杀灵魂
- 灵魂收割令 (SoulReaperToken)
- 阴阳眼 — 看到灵魂
- 灵魂标记 (SoulMark)
- 嗜血诅咒 (BloodthirstCurse)
- 灵魂状态 (SoulStateHandler)
- 还阳

**关键类**: `ReincarnationManager.java`, `SoulHookHandler.java`, `SoulMarkHandler.java`, `SoulReaperOrderHandler.java`, `SoulStateHandler.java`, `LooseImmortalHandler.java`, `GhostDaoHandler.java`

---

## 二十一、物品系统 — 约 100+ 物品

### 武器（4 种 × 5 品级）
- 赤炎剑 (chi_yan_sword) × 5 级
- 寒冰剑 (han_bing_sword) × 5 级
- 青木剑 (qing_mu_sword) × 5 级
- 玄铁剑 (xuan_iron_sword) × 5 级
- 灵剑 (spirit_sword)

### 灵石
- 下品/中品/上品/极品灵石 + 对应矿石

### 阵法物品
- 阵盘 × 5 级 (low/mid/high/supreme/immortal)
- 7 种阵旗 × 5 级 = 35 种

### 灵脉物品
- 灵脉核心 × 5 级
- 灵脉泉眼

### 丹药（约 30+ 种）
各种丹药 × 5 品级

### 法术书（约 50+ 种）
每本法术一本书

### 功法书（约 27 种）
每本功法一本书

### 特殊物品
- 测灵罗盘 (divination_compass)
- 阵法罗盘 (formation_compass)
- 铭阵刻刀 (formation_inscription_knife)
- 筑基秘法 (foundation_secret)
- 轮回命盘 (reincarnation_fate_plate)
- 命格重塑令 (origin_reconfiguration_token)
- 宗门通行玉牌 (sect_token)
- 勾魂索 (soul_hook)
- 勾魂令 (soul_reaper_token)
- 万物真血
- 墨/符纸
- 道基果
- 地煞玄气/天罡清气
- 凝真造化果
- 境界令牌（各境界 × 各子阶段）

### 刷怪蛋
- 各境界修士 (11 种)
- 勾魂使者

**关键类**: `ModItems.java` (注册), `TieredWeapon.java`, `SpellBookItem.java`, `TechniqueBookItem.java`, `PillItem.java`, `CultivationPillItem.java`, `FoundationMaterialItem.java`, `GoldenCoreMaterialItem.java` 等

---

## 二十二、世界生成

- 灵石矿脉 (4 级: low/mid/high/supreme)
- 灵脉泉眼
- 灵草 (HerbBlock + herb_patch)
- 宗门驻地 (SectSettlementFeature)
- 修仙建筑 (CultivationBuildingFeature)
- 地府维度完整世界生成
- 宝箱战利品 (CultivationChestLoot)
- 修士 NPC 自然生成
- 陨石坑 (MeteorCraterCarver)

---

## 二十三、Patchouli 指南书

- 修仙通玄录 (cultivation_compendium)
- 208 个文件的完整内置指南

---

## 二十四、网络系统 — 约 90+ Packet

主要网络包类别：
- 修仙数据同步 (SyncCultivationDataPacket)
- 法术施放/切换/蓄力 (CastSpell/BeginCharge/EndCharge/ToggleSpell/EquipSpell)
- 灵气护盾 (QiShieldHit/QiAbsorbed)
- 阵法同步 (SyncFormationFlags/ToggleFormation)
- 宗门操作 (JoinSect/OpenSectScreen/RequestSectJoinDialogue/SectTaskAction)
- 死亡/转世 (DeathChoice/OpenDeathChoice/ReincarnationChoice/OpenReincarnation/RequestReincarnationScreen)
- 灵魂状态 (SoulState/SoulHook/SoulHookProgress/SoulHookVisual)
- 虚遁/虚空踏步 (VoidEscape/VoidStep)
- 境界威压 (RealmPressureVisual)
- 时间停滞 (TimeStasisDomain/TimeStasisTarget)
- 炼丹/炼器 (ExecuteAlchemy/ExecuteRefining/AutoFillRecipe)
- 修士交易 (ExecuteCultivatorTrade/ExecuteSundryTrade)
- 身份抽卡 (ConfirmIdentityDraw/OpenIdentityDraw/RevealNextCard/OriginRandomized)
- 天劫 (TribulationCloud)
- 客户端效果同步 (各种 Visual 包)

**关键类**: `ModNetwork.java`

---

## 二十五、其他系统

### 键位绑定
- 施放法术 (cast_spell)
- 法术轮盘 (spell_wheel)

### 配置文件
- `ModCommonConfig.java` — 通用配置
- `ModClientConfig.java` — 客户端配置
- `XiaoxiangConfigScreen.java` — 配置界面

### 命令
- `/qi` 命令 (QiCommand)
- `/offlineauth` 命令 (OfflineAuthCommand)

### 离线认证
- `OfflineAuthHandler.java`
- `OfflineAuthStore.java`

### 创造模式物品栏
- `ModCreativeTabs.java`

### 进度系统
- 11 个境界进度 (advancements/realms/)
- 多个引导进度 (advancements/guide/)

### 效果系统 — 12 种效果
- 燃血 (blood_burn)
- 清心 (clear_mind)
- 神行 (divine_stride)
- 重力压制 (gravity_suppression)
- 逆五行 (inverse_five_elements)
- 经脉冻结 (meridian_frozen)
- 雷缚 (palm_thunder_stun)
- 定身 (rooted)
- 缩地成寸 (shadow_step)
- 碎甲 (shatter_armor)
- 时间停滞 (time_stasis)
- 时间流动 (time_stasis_flow)

### 工具类
- `CompactNumberFormat` — 数字格式化
- `CultivationRandomPools` — 随机池
- `QiStorageBlocks` — 灵气存储方块
- `TooltipUtils` — Tooltip 工具

---

## 包结构概览

```
com/xiaoxiang/cultivation/
├── XiaoxiangCultivationMod.java          主类
├── block/                                方块 (5)
│   ├── alchemy/                          炼丹 (6)
│   ├── formation/                        阵法 (25)
│   ├── refining/                         炼器 (6)
│   └── spirit/                           灵脉 (6)
├── client/                               客户端 (89)
│   ├── model/                            模型 (2)
│   ├── particle/                         粒子 (6)
│   ├── renderer/                         渲染器 (28)
│   └── screen/                           界面 (63)
│       └── widget/                       组件 (8)
├── command/                              命令 (5)
├── config/                               配置 (3)
├── cultivation/                          核心修仙 (36)
│   ├── alchemy/                          炼丹 (10)
│   │   └── datapack/                     数据包 (1)
│   ├── beast/                            妖兽 (4)
│   ├── draw/                             抽卡 (4)
│   ├── effect/                           效果 (12)
│   ├── qi/                               灵气 (9)
│   │   ├── consumer/                     消耗者 (2)
│   │   ├── datapack/                     数据包 (2)
│   │   ├── field/                        领域 (3)
│   │   ├── formation/                    阵法 (10)
│   │   ├── source/                       源 (1)
│   │   └── state/                        状态 (13)
│   ├── realm/                            境界 (4)
│   ├── refining/                         炼器 (6)
│   ├── sect/                             宗门 (15)
│   ├── spell/                            法术 (5)
│   └── technique/                        功法 (8)
├── entity/                               实体 (18)
│   └── npc/                              NPC (13)
│       └── ai/                           AI (3)
├── event/                                事件处理器 (103)
├── gametest/                             游戏测试 (3)
├── inventory/                            容器 (8)
├── item/                                 物品 (33)
│   └── weapon/                           武器 (9)
├── loot/                                 战利品 (1)
├── network/                              网络包 (95)
├── registry/                             注册 (15)
├── util/                                 工具 (16)
└── worldgen/                             世界生成 (54)
```

---

## 复刻优先级建议

1. **Phase 1 — 核心**: 境界系统 + 灵气系统 + Capability + 网络同步
2. **Phase 2 — 身份**: 灵根 + 体质 + 身份抽卡 + 筑基/金丹道
3. **Phase 3 — 法术**: 法术系统 + 法术书 + 法术轮盘
4. **Phase 4 — 功法**: 功法系统 + 功法书 + 真元分配
5. **Phase 5 — 战斗**: 伤害系统 + 灵气护盾 + 境界威压
6. **Phase 6 — 炼丹炼器**: 炼丹系统 + 炼器系统
7. **Phase 7 — 阵法**: 阵法系统 + 阵旗/阵盘
8. **Phase 8 — 宗门**: 宗门系统 + NPC 修士
9. **Phase 9 — 地府**: 地府维度 + 死亡/灵魂/转世
10. **Phase 10 — 世界生成**: 灵石矿/灵脉/灵草/建筑
