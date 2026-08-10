/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.AbstractContainerMenu
 *  net.minecraft.world.inventory.DataSlot
 *  net.minecraft.world.inventory.MenuType
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraftforge.network.PacketDistributor
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.friday.cultivation.inventory;

import com.friday.cultivation.block.formation.FormationCorePlateBlock;
import com.friday.cultivation.block.formation.FormationCorePlateBlockEntity;
import com.friday.cultivation.cultivation.ItemTier;
import com.friday.cultivation.cultivation.qi.formation.CoreTier;
import com.friday.cultivation.cultivation.qi.formation.FormationType;
import com.friday.cultivation.network.ModNetwork;
import com.friday.cultivation.network.SyncFormationFlagsPacket;
import com.friday.cultivation.registry.ModMenuTypes;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FormationMenu
extends AbstractContainerMenu {
    private final BlockPos corePos;
    private final Player player;
    @Nullable
    private final FormationCorePlateBlockEntity blockEntityRef;
    private int lastFlagListHash = Integer.MIN_VALUE;
    private final DataSlot qiLow = DataSlot.standalone();
    private final DataSlot qiHigh = DataSlot.standalone();
    private final DataSlot maxQiLow = DataSlot.standalone();
    private final DataSlot maxQiHigh = DataSlot.standalone();
    private final DataSlot activated = DataSlot.standalone();
    private final DataSlot formationTypeOrdinal = DataSlot.standalone();
    private final DataSlot activeRadius = DataSlot.standalone();
    private final DataSlot detectedFlagsCount = DataSlot.standalone();
    private final DataSlot drainPerSecScaled = DataSlot.standalone();
    private final DataSlot sourcesInRange = DataSlot.standalone();
    private final DataSlot coreTierOrdinal = DataSlot.standalone();
    private final DataSlot activeFlagTierOrdinal = DataSlot.standalone();
    private final DataSlot activeFormationCount = DataSlot.standalone();
    private String clientCustomName = "";

    public FormationMenu(int containerId, Inventory inv, BlockPos corePos) {
        super((MenuType)ModMenuTypes.FORMATION.get(), containerId);
        FormationCorePlateBlockEntity fbe;
        this.corePos = corePos;
        this.player = inv.player;
        BlockEntity be = inv.player.level().getBlockEntity(corePos);
        FormationCorePlateBlockEntity formationCorePlateBlockEntity = this.blockEntityRef = be instanceof FormationCorePlateBlockEntity ? (fbe = (FormationCorePlateBlockEntity)be) : null;
        if (this.blockEntityRef != null) {
            this.clientCustomName = this.blockEntityRef.getCustomName();
        }
        this.addDataSlot(this.qiLow);
        this.addDataSlot(this.qiHigh);
        this.addDataSlot(this.maxQiLow);
        this.addDataSlot(this.maxQiHigh);
        this.addDataSlot(this.activated);
        this.addDataSlot(this.formationTypeOrdinal);
        this.addDataSlot(this.activeRadius);
        this.addDataSlot(this.detectedFlagsCount);
        this.addDataSlot(this.drainPerSecScaled);
        this.addDataSlot(this.sourcesInRange);
        this.addDataSlot(this.coreTierOrdinal);
        this.addDataSlot(this.activeFlagTierOrdinal);
        this.addDataSlot(this.activeFormationCount);
        if (this.blockEntityRef != null) {
            this.syncFromBlockEntity();
        }
    }

    public FormationMenu(int containerId, Inventory inv, FriendlyByteBuf buf) {
        this(containerId, inv, buf.readBlockPos());
        if (buf.isReadable()) {
            this.clientCustomName = buf.readUtf(32);
        }
    }

    public String getCustomName() {
        if (this.clientCustomName != null && !this.clientCustomName.isEmpty()) {
            return this.clientCustomName;
        }
        if (this.blockEntityRef != null) {
            return this.blockEntityRef.getCustomName();
        }
        return "";
    }

    public void setClientCustomName(String name) {
        this.clientCustomName = name == null ? "" : name;
    }

    public void broadcastChanges() {
        if (this.blockEntityRef != null) {
            this.syncFromBlockEntity();
            this.sendFlagSyncIfChanged();
        }
        super.broadcastChanges();
    }

    public void sendFlagSync() {
        ServerPlayer serverPlayer;
        block3: {
            block2: {
                Player player = this.player;
                if (!(player instanceof ServerPlayer)) break block2;
                serverPlayer = (ServerPlayer)player;
                if (this.blockEntityRef != null) break block3;
            }
            return;
        }
        List<FormationCorePlateBlockEntity.FlagLinkView> views = this.blockEntityRef.getConnectedFlagViews();
        this.lastFlagListHash = views.hashCode();
        ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> serverPlayer), (Object)SyncFormationFlagsPacket.fromViews(this.corePos, views));
    }

    private void sendFlagSyncIfChanged() {
        if (!(this.player instanceof ServerPlayer) || this.blockEntityRef == null) {
            return;
        }
        List<FormationCorePlateBlockEntity.FlagLinkView> views = this.blockEntityRef.getConnectedFlagViews();
        int hash = views.hashCode();
        if (hash == this.lastFlagListHash) {
            return;
        }
        this.sendFlagSync();
    }

    private void syncFromBlockEntity() {
        long cur = this.blockEntityRef.getCurrentQi();
        long max = this.blockEntityRef.getMaxQi();
        this.qiLow.set((int)(cur & 0xFFFFFFFFL));
        this.qiHigh.set((int)(cur >>> 32));
        this.maxQiLow.set((int)(max & 0xFFFFFFFFL));
        this.maxQiHigh.set((int)(max >>> 32));
        this.activated.set(this.blockEntityRef.isActivated() ? 1 : 0);
        FormationType type = this.blockEntityRef.getActiveFormation();
        this.formationTypeOrdinal.set(type != null ? type.ordinal() : -1);
        this.activeRadius.set(this.blockEntityRef.getActiveRadius());
        this.detectedFlagsCount.set(this.blockEntityRef.scanFlagsCount());
        double drain = this.blockEntityRef.getDrainPerSec();
        this.drainPerSecScaled.set((int)Math.min(Integer.MAX_VALUE, Math.round(drain * 100.0)));
        this.sourcesInRange.set(this.blockEntityRef.getCachedSourcesInRange());
        this.coreTierOrdinal.set(this.blockEntityRef.coreTier().ordinal());
        ItemTier flagTier = this.blockEntityRef.getActiveFlagTier();
        this.activeFlagTierOrdinal.set(flagTier != null ? flagTier.ordinal() : -1);
        this.activeFormationCount.set(this.blockEntityRef.getActiveFormationCount());
    }

    public long getCurrentQi() {
        return (long)this.qiHigh.get() << 32 | (long)this.qiLow.get() & 0xFFFFFFFFL;
    }

    public long getMaxQi() {
        return (long)this.maxQiHigh.get() << 32 | (long)this.maxQiLow.get() & 0xFFFFFFFFL;
    }

    public boolean isActivated() {
        return this.activated.get() == 1;
    }

    @Nullable
    public FormationType getFormationType() {
        int ord = this.formationTypeOrdinal.get();
        if (ord < 0 || ord >= FormationType.values().length) {
            return null;
        }
        return FormationType.values()[ord];
    }

    public int getActiveRadius() {
        return this.activeRadius.get();
    }

    public int getDetectedFlagsCount() {
        return this.detectedFlagsCount.get();
    }

    public double getDrainPerSec() {
        return (double)this.drainPerSecScaled.get() / 100.0;
    }

    public int getSourcesInRange() {
        return this.sourcesInRange.get();
    }

    public CoreTier getCoreTier() {
        int ord = this.coreTierOrdinal.get();
        if (ord < 0 || ord >= CoreTier.values().length) {
            return CoreTier.LOW;
        }
        return CoreTier.values()[ord];
    }

    @Nullable
    public ItemTier getActiveFlagTier() {
        int ord = this.activeFlagTierOrdinal.get();
        if (ord < 0 || ord >= ItemTier.values().length) {
            return null;
        }
        return ItemTier.values()[ord];
    }

    public int getActiveFormationCount() {
        return this.activeFormationCount.get();
    }

    public BlockPos getCorePos() {
        return this.corePos;
    }

    @Nullable
    public FormationCorePlateBlockEntity getBlockEntity() {
        return this.blockEntityRef;
    }

    @NotNull
    public ItemStack quickMoveStack(@NotNull Player p, int slotIndex) {
        return ItemStack.EMPTY;
    }

    public boolean onTake(@NotNull Player p) {
        if (!(this.player.level().getBlockState(this.corePos).getBlock() instanceof FormationCorePlateBlock)) {
            return false;
        }
        return p.distanceToSqr((double)this.corePos.getX() + 0.5, (double)this.corePos.getY() + 0.5, (double)this.corePos.getZ() + 0.5) <= 64.0;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return player.distanceToSqr((double)this.corePos.getX() + 0.5, (double)this.corePos.getY() + 0.5, (double)this.corePos.getZ() + 0.5) <= 64.0;
    }
}
