package com.friday.cultivation.network;

import com.friday.cultivation.registry.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * 服务端开发/管理功能的唯一授权 Module。
 *
 * <p>网络包是 Adapter，只能把请求交给这里判断，不能自行复制权限规则。
 * 普通玩家的客户端界面、篡改后的 Packet 字段和客户端持有的界面状态都不是授权依据。</p>
 */
public final class ServerAuthorization {
    private static final int ADMIN_PERMISSION_LEVEL = 2;

    private ServerAuthorization() {
    }

    /** 境界选择是开发令牌功能：OP 或持有境界选择令牌才可执行。 */
    public static boolean canSelectRealm(ServerPlayer player) {
        return player != null
                && (player.hasPermissions(ADMIN_PERMISSION_LEVEL) || hasRealmSelectorToken(player));
    }

    /** 属性编辑会直接改写多项权威数据，只允许 OP。 */
    public static boolean canEditPlayerStats(ServerPlayer player) {
        return player != null && player.hasPermissions(ADMIN_PERMISSION_LEVEL);
    }

    public static void reject(ServerPlayer player, String operationKey) {
        if (player != null) {
            player.displayClientMessage(
                    Component.translatable("message.friday_cultivation.authorization." + operationKey),
                    true
            );
        }
    }

    private static boolean hasRealmSelectorToken(ServerPlayer player) {
        ItemStack token = new ItemStack(ModItems.REALM_SELECTOR_TOKEN.get());
        return player.getInventory().contains(token);
    }
}
