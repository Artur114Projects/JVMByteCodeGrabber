package com.artur114.bytecodegrab.view;

import com.artur114.bytecodegrab.frame.JGrabFrame;
import com.artur114.bytecodegrab.frame.JSettingsFrame;
import com.artur114.bytecodegrab.jcomp.JButtonsPane;
import com.artur114.bytecodegrab.main.Application;
import com.artur114.bytecodegrab.ui.FlatButtonBorderExt;
import com.artur114.bytecodegrab.util.EnumAxis;
import com.artur114.bytecodegrab.util.Icons;
import com.artur114.bytecodegrab.util.Theme;
import com.sun.tools.attach.VirtualMachineDescriptor;
import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

public class JvmListPanel extends JPanel {
    private final DefaultListModel<VirtualMachineDescriptor> listModel;
    private final JList<VirtualMachineDescriptor> jvmList;
    private final CardLayout layout = new CardLayout();
    private IVirtualMachinesProvider provider;
    private final JButton settings;
    private final JButton refresh;
    private final JPanel topPanel;
    private JProgressBar lastBar;
    private Timer loadTimer;

    public JvmListPanel() {
        setLayout(new BorderLayout());
        this.listModel = new DefaultListModel<>();
        this.jvmList = new JList<VirtualMachineDescriptor>(this.listModel) {
            private JPopupMenu popup = null;

            @Override
            public void setComponentPopupMenu(JPopupMenu popup) {
                this.popup = popup;
            }

            @Override
            protected void processMouseEvent(MouseEvent e) {
                if (!this.isEnabled()) {
                    return;
                }
                Rectangle r = this.getCellBounds(0, this.getLastVisibleIndex());
                boolean outOfBounds = !r.contains(e.getPoint());
                if (e.getID() == MouseEvent.MOUSE_PRESSED) {
                    if (outOfBounds) {
                        this.clearSelection();
                        this.getParent().requestFocusInWindow();
                    } else {
                        this.requestFocusInWindow();
                        this.setSelectedIndex(this.locationToIndex(e.getPoint()));
                    }
                }
                if (this.popup != null && !outOfBounds && e.isPopupTrigger()) {
                    if (SwingUtilities.isRightMouseButton(e)) {
                        this.popup.show(this, e.getX(), e.getY());
                    }
                }
            }
            @Override
            protected void processMouseMotionEvent(MouseEvent e) {}
        };

        this.jvmList.setCellRenderer(new JvmListCellRenderer());

        JPanel top = new JPanel(new BorderLayout());
        JButtonsPane buttonsPane = new JButtonsPane(EnumAxis.X_AXIS);
        buttonsPane.addSplitter(1);
        JButton settings = buttonsPane.createButton(Icons.iconQuad("settings", 14), button -> {
            button.setToolTipText("App settings");
            button.addActionListener(e -> this.showAppSettings());
        });
        buttonsPane.addSplitter(1);
        JButton refresh = buttonsPane.createButton(Icons.iconQuad("refresh", 14), button -> {
            button.setToolTipText("Refresh JVM's");
            button.addActionListener(e -> this.refreshJvmList());
        });
        buttonsPane.configure(button -> {
            button.setBorder(new FlatButtonBorderExt().setFocusWidth(0));
            button.setMinimumSize(new Dimension(18, 18));
            button.setPreferredSize(new Dimension(18, 18));
            button.setMaximumSize(new Dimension(18, 18));
            button.setFocusable(false);
        });
        top.add(buttonsPane, BorderLayout.WEST);

        this.refresh = refresh;
        this.settings = settings;
        this.topPanel = new JPanel(this.layout);
        this.topPanel.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 1));

        JPanel panel1 = new JPanel();
        panel1.setLayout(new BoxLayout(panel1, BoxLayout.X_AXIS));
        panel1.add(Box.createHorizontalGlue());
        panel1.add(new JLabel() {
            @Override
            public String getText() {
                int size = listModel.getSize();
                if (size != 1) {
                    return String.format("Loaded %s JVM's", size);
                } else {
                    return "Loaded 1 JVM";
                }
            }
        });
        panel1.add(Box.createHorizontalGlue());
        Theme.borderColor().ifPresent(color -> panel1.setBorder(BorderFactory.createLineBorder(color)));

        JProgressBar bar = new JProgressBar();
        bar.setIndeterminate(true);

        this.topPanel.add(bar, "bar");
        this.topPanel.add(panel1, "lod");

        top.add(this.topPanel, BorderLayout.CENTER);
        add(top, BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(this.jvmList);

        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem copyPid = new JMenuItem("Copy PID", Icons.iconQuad("copy", 16));
        copyPid.addActionListener(e -> {
            VirtualMachineDescriptor selected = getSelectedJvm();
            if (selected != null) {
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(selected.id()), null);
            }
        });
        JMenuItem copyName = new JMenuItem("Copy VM Name", Icons.iconQuad("copy", 16));
        copyName.addActionListener(e -> {
            VirtualMachineDescriptor selected = getSelectedJvm();
            if (selected != null) {
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(selected.displayName()), null);
            }
        });
        popupMenu.add(copyPid);
        popupMenu.add(copyName);
        popupMenu.pack();

        this.jvmList.setComponentPopupMenu(popupMenu);
        ListSelectionModel model = this.jvmList.getSelectionModel();
        model.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        scroll.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
        Theme.borderColor().ifPresent(color -> scroll.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1), BorderFactory.createLineBorder(color))));
        this.add(scroll, BorderLayout.CENTER);
        Theme.jvmListThreeColor().ifPresent(this.jvmList::setBackground);
    }

    private void load(List<VirtualMachineDescriptor> vms) {
        this.listModel.clear();

        vms.sort(Comparator.comparing(VirtualMachineDescriptor::displayName));

        for (VirtualMachineDescriptor vm : vms) {
            this.listModel.addElement(vm);
        }

        if (this.loadTimer != null) {
            this.loadTimer.stop();
        }

        this.loadTimer = new Timer(500, e -> this.layout.show(this.topPanel, "lod"));
        this.loadTimer.setRepeats(false);
        this.loadTimer.start();

        this.repaint();
    }

    public void refreshJvmList() {
        JProgressBar bar = new JProgressBar();
        bar.setIndeterminate(true);
        if (this.lastBar != null) {
            this.topPanel.remove(this.lastBar);
        }
        this.topPanel.add(bar, "bar");
        this.layout.show(this.topPanel, "bar");
        this.lastBar = bar;

        this.provider.request((list) -> SwingUtilities.invokeLater(() -> this.load(list)));
    }

    public void setDisable(boolean state) {
        this.jvmList.setEnabled(!state);
        this.refresh.setEnabled(!state);
        this.settings.setEnabled(!state);
    }

    public VirtualMachineDescriptor getSelectedJvm() {
        return jvmList.getSelectedValue();
    }

    public void setProvider(IVirtualMachinesProvider provider) {
        this.provider = provider;
    }

    public void addSelectionListener(ListSelectionListener listener) {
        this.jvmList.addListSelectionListener(listener);
    }

    private void showAppSettings() {
        JSettingsFrame frame = new JSettingsFrame(Application.application());
        Application.application().setEnabled(false);

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                Application.application().setEnabled(true);
                Application.application().toFront();
            }
        });

        frame.view();
    }

    public interface IVirtualMachinesProvider {
        void request(Consumer<List<VirtualMachineDescriptor>> callBack);
    }

    private static class JvmListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            if (value instanceof VirtualMachineDescriptor) {
                VirtualMachineDescriptor vm = (VirtualMachineDescriptor) value;
                String text = String.format("%s (pid %s)", this.nameFor(vm.displayName()), vm.id());
                label.setText(text);
                label.setIcon(this.iconFor(vm.displayName()));
                label.setDisabledIcon(this.iconDisabledFor(vm.displayName()));
            }

            return label;
        }

        private Icon iconFor(String machine) {
            if (machine.contains("GradleDaemon") || machine.contains("org.gradle.launcher")) {
                return Icons.iconQuad("gradle", 16);
            }
            if (machine.contains("com.intellij.idea")) {
                return Icons.iconQuad("idea", 16);
            }
            if (machine.contains("bytecodegrab.main.Main") || machine.toLowerCase().contains("bcg")) {
                return Icons.iconQuad("icon_vm", 16);
            }
            if (machine.contains("prismlauncher") || machine.contains("minecraft") || machine.contains("minecraftforge")) {
                return Icons.iconQuad("mc", 16);
            }
            return UIManager.getIcon("FileView.computerIcon");
        }

        private Icon iconDisabledFor(String machine) {
            if (machine.contains("GradleDaemon") || machine.contains("org.gradle.launcher")) {
                return Icons.iconQuadD("gradle", 16);
            }
            if (machine.contains("com.intellij.idea")) {
                return Icons.iconQuadD("idea", 16);
            }
            if (machine.contains("bytecodegrab.main.Main") || machine.toLowerCase().contains("bcg")) {
                return Icons.iconQuadD("icon_vm", 16);
            }
            if (machine.contains("prismlauncher") || machine.contains("minecraft") || machine.contains("minecraftforge")) {
                return Icons.iconQuadD("mc", 16);
            }
            return UIManager.getIcon("FileView.computerIcon");
        }

        private String nameFor(String machine) {
            if (machine.contains("GradleDaemon") || machine.contains("org.gradle.launcher")) {
                return "Gradle";
            }
            if (machine.contains("com.intellij.idea")) {
                return "IntelliJ IDEA";
            }
            if (machine.contains("bytecodegrab.main.Main") || machine.toLowerCase().contains("bcg")) {
                return "Byte Code Grabber";
            }
            if (machine.contains("prismlauncher") || machine.contains("minecraft") || machine.contains("minecraftforge")) {
                return "Minecraft";
            }
            if (machine.contains(File.separator)) {
                return machine.substring(machine.lastIndexOf(File.separator) + 1);
            }
            return machine;
        }
    }
}