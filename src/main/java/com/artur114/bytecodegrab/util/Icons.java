package com.artur114.bytecodegrab.util;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class Icons {
    public static Icon icon(String path) {
        try {
            if (!path.startsWith("assets/bytecodegrab/icon")) {
                path = "assets/bytecodegrab/icon/" + path;
            }
            return new ImageIcon(ImageIO.read(ClassLoader.getSystemResource(path)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Icon iconQuad(String path, int size) {
        return resizeIcon(icon(path), size, size);
    }

    public static Image image(String path) {
        try {
            if (!path.startsWith("assets/bytecodegrab/icon")) {
                path = "assets/bytecodegrab/icon/" + path;
            }
            return ImageIO.read(ClassLoader.getSystemResource(path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static ImageIcon resizeIcon(Icon icon, int width, int height) {
        if (icon instanceof ImageIcon) {
            Image image = ((ImageIcon) icon).getImage();
            Image resized = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            return new ImageIcon(resized);
        } else {
            // Если это не ImageIcon (например, из UIManager), рисуем в BufferedImage
            BufferedImage buffered = new BufferedImage(
                    icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics g = buffered.getGraphics();
            icon.paintIcon(null, g, 0, 0);
            g.dispose();
            Image resized = buffered.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            return new ImageIcon(resized);
        }
    }
}
