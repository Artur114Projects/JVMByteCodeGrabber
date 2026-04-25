package com.artur114.bytecodegrab.util;

import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class Icons {
    private static final Logger LOGGER = LogManager.getLogger("Assets/Icons");
    private static final Gson gson = new Gson();
    private static Icons icons;

    private final Map<String, Image> cachedImagesDe = new HashMap<>();
    private final Map<String, Image> cachedImages = new HashMap<>();
    private final ITreme treme;

    private Icons(ITreme treme) {
        this.treme = treme;
    }

    private Icon iconI(String path, boolean deactivated) {
        return new ImageIcon(imageI(path, deactivated));
    }

    private Icon iconQuadI(String path, int size, boolean deactivated) {
        return this.resizeIconI(this.iconI(path, deactivated), size, size);
    }

    private Image imageI(String path, boolean deactivated) {
        Map<String, Image> imageMap = deactivated ? this.cachedImagesDe : this.cachedImages;
        if (imageMap.containsKey(path)) {
            return imageMap.get(path);
        }
        try {
            path = this.normalise(path);

            IconMeta meta = this.tryLoadMeta(path);
            Image ret = null;

            if (meta != null) {
                ret = this.loadImageFromConfig(meta, deactivated);
            }

            if (ret == null) {
                ret = this.loadImage(path + ".png");
            }

            if (ret != null) {
                if (deactivated) {
                    this.cachedImagesDe.put(path, ret);
                } else {
                    this.cachedImages.put(path, ret);
                }
            }

            return ret;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Image[] imagesI(boolean deactivated, String... paths) {
        return null;
    }

    private ImageIcon resizeIconI(Icon icon, int width, int height) {
        if (icon instanceof ImageIcon) {
            Image image = ((ImageIcon) icon).getImage();
            Image resized = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            return new ImageIcon(resized);
        } else {
            BufferedImage buffered = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics g = buffered.getGraphics();
            icon.paintIcon(null, g, 0, 0);
            g.dispose();
            Image resized = buffered.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            return new ImageIcon(resized);
        }
    }

    private Image loadImageFromConfig(IconMeta meta, boolean deactivated) {
        IconMeta.IconJ iconJ = meta.themes.get("all");

        if (iconJ == null) {
            iconJ = meta.themes.get(this.treme.name());
        }

        if (iconJ == null) {
            return null;
        }

        String path = iconJ.all != null ? iconJ.all : deactivated ? iconJ.deIconPath : iconJ.iconPath;

        if (path == null || path.isEmpty()) {
            return null;
        }

        try {
            return this.loadImage(this.normalise(path));
        } catch (IOException e) {
            e.printStackTrace(System.err);
            return null;
        }
    }

    private IconMeta tryLoadMeta(String path) {
        URL url = this.resource(path + ".json");

        if (url == null) {
            return null;
        }

        try (InputStreamReader isr = new InputStreamReader(url.openStream(), StandardCharsets.UTF_8)) {
            return gson.fromJson(isr, IconMeta.class);
        } catch (IOException e) {
            e.printStackTrace(System.err);
            return null;
        }
    }

    private Image loadImage(String path) throws IOException {
        URL url = this.resource(path);

        if (url == null) {
            throw new IOException();
        }

        try (InputStream is = url.openStream()) {
            return ImageIO.read(is);
        }
    }

    private String normalise(String path) {
        if (!path.startsWith("/assets/bytecodegrab/icon/")) {
            path = "/assets/bytecodegrab/icon/" + path;
        }
        return path;
    }

    private URL resource(String path) {
        return Icons.class.getResource(path);
    }

    public static void newIcons(ITreme treme) {
        icons = new Icons(treme);
    }

    public static Icon icon(String path) {
        if (icons != null) {
            return icons.iconI(path, false);
        } else {
            throw new IllegalStateException("Icons cannot be used before it is initialized");
        }
    }

    public static Icon iconD(String path) {
        if (icons != null) {
            return icons.iconI(path, true);
        } else {
            throw new IllegalStateException("Icons cannot be used before it is initialized");
        }
    }

    public static Icon iconQuad(String path, int size) {
        if (icons != null) {
            return icons.iconQuadI(path, size, false);
        } else {
            throw new IllegalStateException("Icons cannot be used before it is initialized");
        }
    }

    public static Icon iconQuadD(String path, int size) {
        if (icons != null) {
            return icons.iconQuadI(path, size, true);
        } else {
            throw new IllegalStateException("Icons cannot be used before it is initialized");
        }
    }

    public static Image image(String path) {
        if (icons != null) {
            return icons.imageI(path, false);
        } else {
            throw new IllegalStateException("Icons cannot be used before it is initialized");
        }
    }

    public static Image imageDe(String path) {
        if (icons != null) {
            return icons.imageI(path, true);
        } else {
            throw new IllegalStateException("Icons cannot be used before it is initialized");
        }
    }

    public static Image[] images(String... paths) {
        if (icons != null) {
            return icons.imagesI(false, paths);
        } else {
            throw new IllegalStateException("Icons cannot be used before it is initialized");
        }
    }

    public static Image[] imagesDe(String... paths) {
        if (icons != null) {
            return icons.imagesI(true, paths);
        } else {
            throw new IllegalStateException("Icons cannot be used before it is initialized");
        }
    }

    public static ImageIcon resizeIcon(Icon icon, int width, int height) {
        if (icons != null) {
            return icons.resizeIconI(icon, width, height);
        } else {
            throw new IllegalStateException("Icons cannot be used before it is initialized");
        }
    }
}
