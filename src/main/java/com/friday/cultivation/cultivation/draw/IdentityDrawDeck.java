/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 */
package com.friday.cultivation.cultivation.draw;

import com.friday.cultivation.cultivation.draw.DrawCard;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.FriendlyByteBuf;

public final class IdentityDrawDeck {
    public static final int DECK_SIZE = 3;
    public static final int MAX_ROUNDS = 10;
    private final List<DrawCard> cards;
    private final int roundsUsed;

    public IdentityDrawDeck(List<DrawCard> cards, int roundsUsed) {
        this.cards = List.copyOf(cards);
        this.roundsUsed = Math.max(0, Math.min(roundsUsed, 10));
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
        return 10 - this.roundsUsed;
    }

    public boolean isRolled() {
        return this.roundsUsed > 0;
    }

    public boolean canRoll() {
        return this.roundsUsed < 10;
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
        ArrayList<DrawCard> list = new ArrayList<DrawCard>(n);
        for (int i = 0; i < n; ++i) {
            list.add(DrawCard.decode(buf));
        }
        int rounds = buf.readVarInt();
        return new IdentityDrawDeck(list, rounds);
    }
}

