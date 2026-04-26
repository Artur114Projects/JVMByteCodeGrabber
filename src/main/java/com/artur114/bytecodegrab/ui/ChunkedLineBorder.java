package com.artur114.bytecodegrab.ui;

import javax.swing.border.Border;
import java.awt.*;

public class ChunkedLineBorder implements Border {
    private final Color color;
    private final boolean drawTop;
    private final boolean drawBottom;
    private final boolean drawLeft;
    private final boolean drawRight;
    private final int size;

    public ChunkedLineBorder(Color color, boolean drawTop, boolean drawBottom, boolean drawLeft, boolean drawRight) {
        this(color, 1, drawTop, drawBottom, drawLeft, drawRight);
    }

    public ChunkedLineBorder(Color color, int size, boolean drawTop, boolean drawBottom, boolean drawLeft, boolean drawRight) {
        this.drawBottom = drawBottom;
        this.drawRight = drawRight;
        this.drawLeft = drawLeft;
        this.drawTop = drawTop;
        this.color = color;
        this.size = size;
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(this.color);
        if (this.drawTop) {
            g2.fillRect(x, y, width, this.size);
        }
        if (this.drawLeft) {
            g2.fillRect(x, y, this.size, height);
        }
        if (this.drawBottom) {
            g2.fillRect(x, y + height - this.size, width, this.size);
        }
        if (this.drawRight) {
            g2.fillRect(x + width - this.size, y, this.size, height);
        }
    }
    @Override
    public Insets getBorderInsets(Component c) {
        return new Insets(1, 1, 1, 1);
    }

    @Override
    public boolean isBorderOpaque() {
        return true;
    }
}
