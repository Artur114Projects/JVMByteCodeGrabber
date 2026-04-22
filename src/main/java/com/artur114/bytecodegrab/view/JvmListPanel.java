package com.artur114.bytecodegrab.view;

import com.artur114.bytecodegrab.util.Icons;
import com.sun.tools.attach.VirtualMachineDescriptor;
import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
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
        JPanel panelB = new JPanel(new BorderLayout());
        JButton button = new JButton(Icons.resizeIcon(Icons.icon("refresh.png"), 14, 14));
        button.setPreferredSize(new Dimension(20, 18));
        button.setFocusable(false);
        button.setToolTipText("Refresh JVM's");
        button.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshJvmList();
            }
        });
        panelB.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 0));
        panelB.add(button, BorderLayout.CENTER);
        top.add(panelB, BorderLayout.WEST);

        this.refresh = button;
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
        panel1.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        JProgressBar bar = new JProgressBar();
        bar.setIndeterminate(true);

        this.topPanel.add(bar, "bar");
        this.topPanel.add(panel1, "lod");

        top.add(this.topPanel, BorderLayout.CENTER);
        add(top, BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(this.jvmList);

        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem copyPid = new JMenuItem("Copy PID", Icons.iconQuad("copy.png", 16));
        copyPid.addActionListener(e -> {
            VirtualMachineDescriptor selected = getSelectedJvm();
            if (selected != null) {
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(selected.id()), null);
            }
        });
        JMenuItem copyName = new JMenuItem("Copy VM Name", Icons.iconQuad("copy.png", 16));
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

        scroll.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1), BorderFactory.createLineBorder(Color.LIGHT_GRAY)));
        this.add(scroll, BorderLayout.CENTER);
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
                return Icons.iconQuad("gradle.png", 16);
            }
            if (machine.contains("com.intellij.idea")) {
                return Icons.iconQuad("idea.png", 16);
            }
            if (machine.contains("bytecodegrab.main.Main") || machine.toLowerCase().contains("bytecodegrabber")) {
                return Icons.iconQuad("icon_black_vm.png", 16);
            }
            if (machine.contains("prismlauncher") || machine.contains("minecraft") || machine.contains("minecraftforge")) {
                return Icons.iconQuad("mc.png", 16);
            }
            return UIManager.getIcon("FileView.computerIcon");
        }

        private Icon iconDisabledFor(String machine) {
            if (machine.contains("GradleDaemon") || machine.contains("org.gradle.launcher")) {
                return Icons.iconQuad("gradle_d.png", 14);
            }
            if (machine.contains("com.intellij.idea")) {
                return Icons.iconQuad("idea_d.png", 14);
            }
            if (machine.contains("bytecodegrab.main.Main") || machine.toLowerCase().contains("bytecodegrabber")) {
                return Icons.iconQuad("icon_black_vm_d.png", 16);
            }
            if (machine.contains("prismlauncher") || machine.contains("minecraft") || machine.contains("minecraftforge")) {
                return Icons.iconQuad("mc_d.png", 16);
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
            if (machine.contains("bytecodegrab.main.Main") || machine.toLowerCase().contains("bytecodegrabber")) {
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