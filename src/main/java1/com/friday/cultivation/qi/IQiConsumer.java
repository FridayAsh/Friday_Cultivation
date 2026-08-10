package com.friday.cultivation.qi;

import com.friday.cultivation.QiElement;
import net.minecraft.world.phys.Vec3;

/**
 * 灵气消费者接口 - 接受/拒收灵气的实体/方块实体。
 * 完全照搬原 mod: xiaoxiang.cultivation.cultivation.qi.IQiConsumer
 */
public interface IQiConsumer {
    /** 是否希望更多灵气 */
    boolean wantsMore();

    /** 吸引半径 */
    double attractRadius();

    /** 位置 */
    Vec3 position();

    /** 接收灵气，返回实际接收量 */
    int receiveQi(QiElement element, int amount);

    /** 优先级（默认 0） */
    default int priority() {
        return 0;
    }
}
