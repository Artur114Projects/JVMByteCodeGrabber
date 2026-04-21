package com.artur114.bytecodegrab.jcomp;

import java.awt.*;

public class JCardContainer {
    private final Container container;
    private final CardLayout card;
    private String showedCard;


    public JCardContainer(Container container, CardLayout card) {
        this.container = container;
        this.card = card;
    }

    public JCardContainer(Container container) {
        this.card = (CardLayout) container.getLayout();
        this.container = container;
    }

    public void show(String card) {
        this.card.show(this.container, card);

        this.showedCard = card;
    }

    public boolean isShowed(String card) {
        return card.equals(showedCard);
    }
}
