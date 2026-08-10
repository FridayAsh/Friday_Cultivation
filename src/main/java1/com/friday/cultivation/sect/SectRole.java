package com.friday.cultivation.sect;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Component;

/**
 * 宗门角色 — 8种
 * 复刻自原模组 com.xiaoxiang.cultivation.cultivation.sect.SectRole
 * 顺序即职位高低：ANCESTOR(老祖) > MASTER(宗主) > ELDER(长老) > INNER(内门) > OUTER(外门) > GUARD(守山) > SERVANT(杂役) > NONE(无)
 */
public enum SectRole {
    ANCESTOR("ancestor", 0, "老祖"),
    MASTER("master", 1, "宗主"),
    ELDER("elder", 2, "长老"),
    INNER_DISCIPLE("inner_disciple", 3, "内门弟子"),
    OUTER_DISCIPLE("outer_disciple", 4, "外门弟子"),
    GUARD_DISCIPLE("guard_disciple", 5, "守山弟子"),
    SERVANT("servant", 6, "杂役"),
    NONE("none", 99, "无");

    private final String id;
    private final int rank;
    private final String chineseName;

    SectRole(String id, int rank, String chineseName) {
        this.id = id;
        this.rank = rank;
        this.chineseName = chineseName;
    }

    public String id() { return id; }
    public int rank() { return rank; }
    public String translationKey() { return "sect_role.friday_cultivation." + id; }

    public Component displayName() {
        return Component.translatableWithFallback(translationKey(), chineseName);
    }

    /** 职位前缀：如 "宗主" 对应的称呼 "宗主·张三" */
    public Component identity(String name) {
        return Component.literal(displayName().getString() + "·" + (name == null ? "" : name));
    }

    /** 此角色是否不低（高于或等于）给定角色 */
    public boolean sameOrHigherThan(SectRole other) {
        return other != null && this.rank <= other.rank;
    }

    public static SectRole byId(String id) {
        for (SectRole r : values()) if (r.id.equals(id)) return r;
        return NONE;
    }
}
