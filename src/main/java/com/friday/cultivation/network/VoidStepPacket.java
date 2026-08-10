/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraftforge.network.NetworkEvent$Context
 */
package com.friday.cultivation.network;

import com.friday.cultivation.event.VoidStepHandler;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

public class VoidStepPacket {
    public static final int DIR_BIT_FORWARD = 1;
    public static final int DIR_BIT_BACK = 2;
    public static final int DIR_BIT_LEFT = 4;
    public static final int DIR_BIT_RIGHT = 8;
    public static final int DIR_BIT_SNEAK = 16;
    public static final int DIR_BIT_JUMP = 32;
    private final Op op;
    private final int dirBits;
    private final float yaw;

    public VoidStepPacket(Op op, int dirBits, float yaw) {
        this.op = op;
        this.dirBits = dirBits;
        this.yaw = yaw;
    }

    public VoidStepPacket(Op op) {
        this(op, 0, 0.0f);
    }

    public static void encode(VoidStepPacket msg, FriendlyByteBuf buf) {
        buf.writeByte(msg.op.ordinal());
        if (msg.op == Op.DASH || msg.op == Op.HELD_INPUT) {
            buf.writeByte(msg.dirBits & 0xFF);
            buf.writeFloat(msg.yaw);
        }
    }

    public static VoidStepPacket decode(FriendlyByteBuf buf) {
        Op op;
        int idx = buf.readByte() & 0xFF;
        Op op2 = op = idx >= 0 && idx < Op.VALUES.length ? Op.VALUES[idx] : Op.JUMP_3_BLOCKS;
        if (op == Op.DASH || op == Op.HELD_INPUT) {
            int bits = buf.readByte() & 0xFF;
            float yaw = buf.readFloat();
            return new VoidStepPacket(op, bits, yaw);
        }
        return new VoidStepPacket(op);
    }

    public static void handle(VoidStepPacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) {
                return;
            }
            VoidStepHandler.handlePacket(player, msg.op, msg.dirBits, msg.yaw);
        });
        ctx.setPacketHandled(true);
    }

    public static enum Op {
        JUMP_3_BLOCKS,
        DASH,
        HELD_INPUT;

        public static final Op[] VALUES;

        static {
            VALUES = Op.values();
        }
    }
}

