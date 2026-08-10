/*
 * Decompiled with CFR 0.152.
 */
package com.friday.cultivation.cultivation.qi;

import com.friday.cultivation.cultivation.QiElement;

public interface IQiSource {
    public QiElement element();

    public int peek();

    public int extract(int var1);
}

