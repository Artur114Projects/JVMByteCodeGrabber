package com.artur114.bytecodegrab.jcomp;

import com.artur114.bytecodegrab.util.ArrayListenBuss;
import com.artur114.bytecodegrab.util.IListenBuss;
import com.artur114.bytecodegrab.util.IListener;

import java.awt.*;

public class JCardContainer {
    private final IListenBuss<IListener<String>, String> cardChangeListenBuss = new ArrayListenBuss<>();
    private final Container container;
    private final CardLayout card;
    private String showedCard;


    public JCardContainer(Container container, CardLayout card) {
        this.container = container;
        this.card = card;
    }

    public JCardContainer(Container container) {
        this(container, (CardLayout) container.getLayout());
    }

    public void addCardChangeListener(IListener<String> listener) {
        this.cardChangeListenBuss.registerListener(listener);
    }

    public void show(String card) {
        this.card.show(this.container, card);
        this.cardChangeListenBuss.listen(card);
        this.showedCard = card;
    }

    public boolean isShowed(String card) {
        return card.equals(this.showedCard);
    }
}
