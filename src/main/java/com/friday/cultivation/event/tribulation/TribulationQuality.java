package com.friday.cultivation.event.tribulation;

/**
 * 可扩展品阶评分接口。
 * 任何品阶枚举（功法 ItemTier / 体质 Physique.Rarity）实现此接口后，
 * 新增枚举值自动接入综合评判系统，无需重写逻辑。
 */
public interface TribulationQuality {
    /** 品阶归一化评分 0.0~1.0（越高越好） */
    double qualityIndex();
}
