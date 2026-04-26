package com.artur114.bytecodegrab.ui;

import javax.swing.border.Border;
import java.awt.*;

public class RoundedBorder implements Border {
    private final boolean withoutInsets;
    private final Color color;
    private final int radius;

    public RoundedBorder(Color color, int radius) {
        this(color, radius, false);
    }

    public RoundedBorder(Color color, int radius, boolean withoutInsets) {
        this.withoutInsets = withoutInsets;
        this.radius = radius;
        this.color = color;
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(this.color);
        g2.drawRoundRect(x, y, width - 1, height - 1, this.radius, this.radius);
    }

    @Override
    public Insets getBorderInsets(Component c) {
        if (this.withoutInsets) {
            return new Insets(1, 1, 1, 1);
        }
        return new Insets(this.radius / 2, this.radius / 2, this.radius / 2, this.radius / 2);
    }

    @Override
    public boolean isBorderOpaque() {
        return true;
    }
}