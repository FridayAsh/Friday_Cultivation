package com.friday.cultivation.qi;

import com.friday.cultivation.QiElement;

/**
 * 灵气源接口（严格照搬原模组 com.xiaoxiang.cultivation.cultivation.qi.IQiSource）
 */
public interface IQiSource {
    QiElement element();
    int peek();
    int extract(int var1);
}