package com.artur114.bytecodegrab.frame;

import com.artur114.bytecodegrab.conf.AppConfig;
import com.artur114.bytecodegrab.main.Application;
import com.artur114.bytecodegrab.main.Bootstrap;
import com.artur114.bytecodegrab.ui.ChunkedLineBorder;
import com.artur114.bytecodegrab.ui.FlatBorderExt;
import com.artur114.bytecodegrab.ui.RoundedBorder;
import com.artur114.bytecodegrab.util.ArrayListenBuss;
import com.artur114.bytecodegrab.util.IListenBuss;
import com.artur114.bytecodegrab.util.IListener;
import com.artur114.bytecodegrab.util.Icons;
import com.formdev.flatlaf.ui.FlatBorder;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JSettingsFrame extends JFrame {
    private final AppConfig config;
    private final int primalValue;
    private int selected = -1;

    public JSettingsFrame(Application parent) {
        this.config = parent.appData;
        this.primalValue = this.config.themeConfig().selectedTheme;
        this.setSize(500, 250);
        this.setLocationRelativeTo(parent);
        this.setTitle("BCG Settings");
        this.setIconImage(Icons.image("settings"));
        this.setDefaultCloseOperation(JGrabFrame.DISPOSE_ON_CLOSE);

        JButton buttonApply = new JButton("Apply");
        JButton buttonRestart = new JButton("Restart");
        buttonApply.setEnabled(false);
        buttonRestart.setEnabled(false);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(Box.createVerticalStrut(2));

        JPanel paneLabel = new JPanel();
        paneLabel.setLayout(new BoxLayout(paneLabel, BoxLayout.X_AXIS));
        paneLabel.add(Box.createHorizontalStrut(24));
        JLabel label = new JLabel("Themes");
        label.setFont(label.getFont().deriveFont(16.0F));
        paneLabel.add(label);
        paneLabel.add(Box.createHorizontalGlue());
        panel.add(paneLabel);

        JPanel panelTheme = new JPanel();
        panelTheme.setLayout(new BoxLayout(panelTheme, BoxLayout.X_AXIS));
        panelTheme.add(Box.createHorizontalStrut(24));
        List<ThemeEntry> themeEntries = new ArrayList<>(Arrays.asList(
            new ThemeEntry("Default to system", "all/ui_preview_system"),
            new ThemeEntry("Dark theme", "all/ui_preview_dark"),
            new ThemeEntry("Light theme", "all/ui_preview_light")
        ));
        ButtonGroup group = new ButtonGroup();
        for (int i = 0; i != 3; i++) {
            ThemeEntry entry = themeEntries.get(i);
            if (i != 0) {
                panelTheme.add(Box.createHorizontalGlue());
            }
            if (i == config.themeConfig().selectedTheme) {
                this.selected = i;
                entry.button.setSelected(true);
                entry.updateBorder();
            }
            panelTheme.add(entry);
            group.add(entry.button);
            entry.addSelectListener(value -> {
                for (ThemeEntry e : themeEntries) {
                    e.updateBorder();
                }
                this.selected = themeEntries.indexOf(value);
                buttonApply.setEnabled(this.primalValue != this.selected);
                buttonRestart.setEnabled(this.primalValue != this.selected);
                value.requestFocusInWindow();
            });
        }
        panelTheme.add(Box.createHorizontalStrut(24));
        panel.add(panelTheme);
        if (this.selected == -1) {
            this.config.themeConfig().selectedTheme = 0;
            this.selected = 0;
            this.config.save();
        }

        JPanel panelApply = new JPanel();
        panelApply.setLayout(new BoxLayout(panelApply, BoxLayout.X_AXIS));
        panelApply.add(Box.createHorizontalStrut(24));
        JLabel labelInfo = new JLabel("Changes are applied after restart", Icons.resizeIcon(UIManager.getIcon("OptionPane.informationIcon"), 14, 14), SwingConstants.LEFT);
        panelApply.add(labelInfo);
        panelApply.add(Box.createHorizontalGlue());
        JButton buttonCancel = new JButton("Cancel");
        buttonCancel.addActionListener(e -> this.dispose());
        buttonApply.addActionListener(e -> {
            this.config.themeConfig().selectedTheme = this.selected;
            this.config.save();
            this.dispose();
        });
        buttonRestart.addActionListener(e -> {
            this.config.themeConfig().selectedTheme = this.selected;
            this.config.save();

            if (Bootstrap.bootstrap().tryRelaunch()) {
                System.exit(0);
            } else {
                JOptionPane.showMessageDialog(this, "An error occurred during a relaunch attempt.\nPlease restart application manually.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        panelApply.add(buttonCancel);
        panelApply.add(Box.createHorizontalStrut(4));
        panelApply.add(buttonApply);
        panelApply.add(Box.createHorizontalStrut(4));
        panelApply.add(buttonRestart);
        panelApply.add(Box.createHorizontalStrut(24));
        panel.add(panelApply);


        panel.add(Box.createVerticalGlue());
        this.getRootPane().setDefaultButton(buttonRestart);

        this.add(panel);
    }

    public void view() {
        this.setVisible(true);
        this.getRootPane().getDefaultButton().requestFocusInWindow();
    }

    private static class ThemeEntry extends JPanel {
        private final IListenBuss<IListener<ThemeEntry>, ThemeEntry> selectListenBuss = new ArrayListenBuss<>();
        private final JRadioButton button;
        public ThemeEntry(String name, String icon) {
            this.setLayout(new BorderLayout());
            JLabel label = new JLabel(Icons.icon(icon, 686 / 5, 393 / 5));
            this.add(label, BorderLayout.CENTER);
            label.setBorder(BorderFactory.createEmptyBorder(0, 0, 1, 0));
            JRadioButton button = new JRadioButton(name);
            JPanel panelButton = new JPanel(new BorderLayout());
            panelButton.add(button, BorderLayout.CENTER);
            this.add(panelButton, BorderLayout.SOUTH);
            this.setBorder(new FlatBorderExt());
            this.setPreferredSize(new Dimension(686 / 5, 393 / 5 + 24));
            this.setMaximumSize(new Dimension(686 / 5, 393 / 5 + 24));
            this.setBorder(new RoundedBorder(this.getBackground().darker(), 8, true));
            this.setBorder(BorderFactory.createCompoundBorder(this.getBorder(), BorderFactory.createEmptyBorder(0, 0, 4, 0)));

            button.setFocusable(false);
            button.addActionListener(e -> {
                this.updateBorder();
                this.selectListenBuss.listen(this);
            });
            panelButton.setBorder(new ChunkedLineBorder(this.getBackground().darker(), 1, true, false, false, false));
            panelButton.setPreferredSize(new Dimension(686 / 5, 20));
            panelButton.setMaximumSize(new Dimension(686 / 5, 20));
            ThemeEntry t = this;
            this.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    button.setSelected(true);
                    updateBorder();
                    selectListenBuss.listen(t);
                }
            });

            this.button = button;
        }

        public JRadioButton button() {
            return this.button;
        }

        private void addSelectListener(IListener<ThemeEntry> listener) {
            this.selectListenBuss.registerListener(listener);
        }

        private void updateBorder() {
            Color borderColor;
            if (button.isSelected()) {
                borderColor = new Color(0x0062d9);
            } else {
                borderColor = this.getBackground().darker();
            }
            this.setBorder(new RoundedBorder(borderColor, 8, true));
            this.setBorder(BorderFactory.createCompoundBorder(this.getBorder(), BorderFactory.createEmptyBorder(0, 0, 4, 0)));
        }
    }
}