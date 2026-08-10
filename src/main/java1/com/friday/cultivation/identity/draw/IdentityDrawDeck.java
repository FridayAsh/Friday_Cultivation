package com.friday.cultivation.identity.draw;

import net.minecraft.network.FriendlyByteBuf;
import java.util.ArrayList;
import java.util.List;

/**
 * 身份抽卡 — 牌组（3张卡牌，最多10轮重抽）
 * 复刻自原模组 com.xiaoxiang.cultivation.cultivation.draw.IdentityDrawDeck
 */
public final class IdentityDrawDeck {
    public static final int DECK_SIZE = 3;
    public static final int MAX_ROUNDS = 10;

    private final List<DrawCard> cards;
    private final int roundsUsed;

    public IdentityDrawDeck(List<DrawCard> cards, int roundsUsed) {
        this.cards = List.copyOf(cards);
        this.roundsUsed = Math.max(0, Math.min(roundsUsed, MAX_ROUNDS));
    }

    public List<DrawCard> cards() {
        return this.cards;
    }

    public int roundsUsed() {
        return this.roundsUsed;
    }

    public int deckSize() {
        return this.cards.size();
    }

    public int roundsRemaining() {
        return MAX_ROUNDS - this.roundsUsed;
    }

    public boolean isRolled() {
        return this.roundsUsed > 0;
    }

    public boolean canRoll() {
        return this.roundsUsed < MAX_ROUNDS;
    }

    public int revealedCount() {
        return this.isRolled() ? this.cards.size() : 0;
    }

    public IdentityDrawDeck withRoll(List<DrawCard> newCards) {
        return new IdentityDrawDeck(newCards, this.roundsUsed + 1);
    }

    public DrawCard cardAt(int index) {
        if (index < 0 || index >= this.cards.size()) {
            return null;
        }
        return this.cards.get(index);
    }

    public boolean canConfirm(int index) {
        return this.isRolled() && index >= 0 && index < this.cards.size();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(this.cards.size());
        for (DrawCard c : this.cards) {
            c.encode(buf);
        }
        buf.writeVarInt(this.roundsUsed);
    }

    public static IdentityDrawDeck decode(FriendlyByteBuf buf) {
        int n = buf.readVarInt();
        ArrayList<DrawCard> list = new ArrayList<>(n);
        for (int i = 0; i < n; ++i) {
            list.add(DrawCard.decode(buf));
        }
        int rounds = buf.readVarInt();
        return new IdentityDrawDeck(list, rounds);
    }
}
