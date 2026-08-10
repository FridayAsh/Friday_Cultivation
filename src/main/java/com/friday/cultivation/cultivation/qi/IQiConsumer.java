/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.phys.Vec3
 */
package com.friday.cultivation.cultivation.qi;

import com.friday.cultivation.cultivation.QiElement;
import net.minecraft.world.phys.Vec3;

public interface IQiConsumer {
    public Vec3 position();

    public double attractRadius();

    public boolean wantsMore();

    public int receiveQi(QiElement var1, int var2);
}

