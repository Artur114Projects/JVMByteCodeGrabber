package com.artur114.bytecodegrab.view;

import com.artur114.bytecodegrab.frame.JGrabFrame;
import com.artur114.bytecodegrab.jcomp.*;
import com.artur114.bytecodegrab.main.Application;
import com.artur114.bytecodegrab.ui.FlatButtonBorderExt;
import com.artur114.bytecodegrab.util.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;
import java.util.List;

public class CodeGrabPanel extends JPanel {
    private static final Logger LOGGER = LogManager.getLogger("View/CodeGrabPanel");
    private final IListenBuss<IListener<IGrabStartData>, IGrabStartData> grabListenBuss = new ArrayEDITListenBuss<>();
    private final IListenBuss<IListener<ActionEvent>, ActionEvent> disconnectListenBuss = new ArrayEDITListenBuss<>();
    private final IListenBuss<IListener<ActionEvent>, ActionEvent> refreshListenBuss = new ArrayEDITListenBuss<>();
    private final IListenBuss<IListener<Void>, Void> grabAbortListenBuss = new ArrayEDITListenBuss<>();
    private JCardContainer inputCard;
    private JProgressBar bar;
    public final JClassTree inputTree;
    public final JClassTree grabTree;
    private JGrabFrame grabFrame;

    public CodeGrabPanel() {
        this.setLayout(new BorderLayout());
        JSplitPane splitPane = new JSplitPane();

        splitPane.setRightComponent(this.initGrabPanel(this.grabTree = new JClassTree()));
        splitPane.setLeftComponent(this.initInputPanel(this.inputTree = new JClassTree()));

        this.add(splitPane, BorderLayout.CENTER);
        splitPane.setBorder(BorderFactory.createEmptyBorder());
        splitPane.setDividerLocation(240);

        this.addDisconnectListener(e -> {
            this.inputTree.clear();
            this.grabTree.clear();
        });

        this.inputCard.show("bar");
    }

    private JPanel initGrabPanel(JClassTree grabTree) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(grabTree, BorderLayout.CENTER);
        grabTree.tree.setRootVisible(false);

        JPanel topGrab = new JPanel();
        topGrab.setLayout(new BoxLayout(topGrab, BoxLayout.X_AXIS));

        JButtonsPane buttons = new JButtonsPane(EnumAxis.X_AXIS);
        buttons.createButton(Icons.iconQuad("clear", 14), button -> {
            button.addActionListener(e -> grabTree.clear());
            button.setToolTipText("Clear classes");
        });
        buttons.addSplitter(1);
        buttons.createButton(Icons.iconQuad("remove", 14), button -> {
            button.addActionListener(e -> grabTree.removeClassNames(grabTree.selectedClasses()));
            button.setToolTipText("Remove selected");
        });
        buttons.addSplitter(1);
        buttons.createButton(Icons.iconQuad("expand", 14), button -> {
            button.addActionListener(e -> grabTree.expandSelected());
            button.setToolTipText("Expand selected");
        });
        buttons.addSplitter(1);
        buttons.createButton(Icons.iconQuad("collapse", 14), button -> {
            button.addActionListener(e -> grabTree.collapseAll());
            button.setToolTipText("Collapse all");
        });
        buttons.addSplitter(1);
        buttons.createButton(Icons.iconQuad("grab", 14), button -> {
            button.addActionListener(e -> {
                if (!this.getClassesToGrab().isEmpty()) {
                    this.showGrabDialog();
                } else {
                    JOptionPane.showMessageDialog(Application.application(), "Please add classes to grab pane", "Grabber", JOptionPane.WARNING_MESSAGE);
                }
            });
            button.setToolTipText("Grab classes");
        });
        buttons.configure(button -> {
            button.setBorder(new FlatButtonBorderExt().setFocusWidth(0));
            button.setPreferredSize(new Dimension(20, 18));
            button.setFocusable(false);
        });
        buttons.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 0));
        topGrab.add(buttons, BorderLayout.WEST);
        JCTreeSearchPanel searchPanel = new JCTreeSearchPanel(grabTree);
        searchPanel.setBorder(BorderFactory.createCompoundBorder(searchPanel.getBorder(), BorderFactory.createEmptyBorder(0, 0, 1, 0)));
        topGrab.add(searchPanel, BorderLayout.CENTER);
        panel.add(topGrab, BorderLayout.NORTH);

        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem remove = new JMenuItem("Remove", Icons.iconQuad("remove", 14));
        popupMenu.add(remove);


        remove.addActionListener(e -> this.grabTree.removeClassNames(this.grabTree.selectedClasses()));
        JMenuItem expand = new JMenuItem("Expand", Icons.iconQuad("expand", 16));
        expand.addActionListener(e -> grabTree.expandSelected());
        JMenuItem collapse = new JMenuItem("Collapse", Icons.iconQuad("collapse", 16));
        collapse.addActionListener(e -> grabTree.collapseSelected());
        JPopupMenu.Separator separatorEx = new JPopupMenu.Separator();
        popupMenu.add(separatorEx);
        popupMenu.add(expand);
        popupMenu.add(collapse);
        popupMenu.addSeparator();

        JMenuItem copyPackage = new JMenuItem("Copy package", Icons.iconQuad("copy", 16));
        copyPackage.addActionListener(e -> {
            List<String> list = grabTree.selectedClasses();
            if (list.size() == 1) {
                String selected = list.get(0);
                String toCopy = selected.substring(0, selected.lastIndexOf("."));
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(toCopy), null);
            }
        });
        JMenuItem copyName = new JMenuItem("Copy class name", Icons.iconQuad("copy", 16));
        copyName.addActionListener(e -> {
            List<String> list = grabTree.selectedClasses();
            if (list.size() == 1) {
                String selected = list.get(0);
                String toCopy = selected.substring(selected.lastIndexOf(".") + 1);
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(toCopy), null);
            }
        });
        JMenuItem copyFullName = new JMenuItem("Copy full class name", Icons.iconQuad("copy", 16));
        copyFullName.addActionListener(e -> {
            List<String> list = grabTree.selectedClasses();
            if (list.size() == 1) {
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(list.get(0)), null);
            }
        });
        JPopupMenu.Separator separator = new JPopupMenu.Separator();
        popupMenu.add(copyFullName);
        popupMenu.add(copyName);
        popupMenu.add(copyPackage);
        popupMenu.add(separator);

        JMenuItem clear = new JMenuItem("Clear", Icons.iconQuad("clear", 16));
        clear.addActionListener(e -> grabTree.clear());
        JMenuItem grabItem = new JMenuItem("Grab classes", Icons.iconQuad("grab", 16));
        grabItem.addActionListener(e -> {
            if (!this.getClassesToGrab().isEmpty()) {
                this.showGrabDialog();
            } else {
                JOptionPane.showMessageDialog(Application.application(), "Please add classes to grab pane", "Grabber", JOptionPane.WARNING_MESSAGE);
            }
        });

        popupMenu.add(grabItem);
        popupMenu.add(clear);

        popupMenu.pack();

        popupMenu.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                boolean stateCopy = grabTree.isSelectedClass();
                copyPackage.setVisible(stateCopy);
                copyFullName.setVisible(stateCopy);
                copyName.setVisible(stateCopy);
                separator.setVisible(stateCopy);
                boolean stateExpand = !stateCopy;
                expand.setVisible(stateExpand);
                collapse.setVisible(stateExpand);
                separatorEx.setVisible(stateExpand);
            }
            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {}
            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {}
        });

        JPopupMenu popupMenuDef = new JPopupMenu();
        JMenuItem clearDef = new JMenuItem("Clear", Icons.iconQuad("clear", 16));
        clearDef.addActionListener(e -> grabTree.clear());
        JMenuItem grabItemDef = new JMenuItem("Grab classes", Icons.iconQuad("grab", 16));
        grabItemDef.addActionListener(e -> {
            if (!this.getClassesToGrab().isEmpty()) {
                this.showGrabDialog();
            } else {
                JOptionPane.showMessageDialog(Application.application(), "Please add classes to grab pane", "Grabber", JOptionPane.WARNING_MESSAGE);
            }
        });

        popupMenuDef.add(grabItemDef);
        popupMenuDef.add(clearDef);

        grabTree.setDefaultPopupMenu(popupMenuDef);
        grabTree.setComponentPopupMenu(popupMenu);
        grabTree.tree.setDragEnabled(true);
        grabTree.tree.setTransferHandler(new TreeDropHandler());

        return panel;
    }

    private JPanel initInputPanel(JClassTree inputTree) {
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));

        JButtonsPane buttons = new JButtonsPane(EnumAxis.X_AXIS);
        buttons.createButton(Icons.iconQuad("refresh", 14), button -> {
            button.addActionListener(this.refreshListenBuss::listen);
            button.setToolTipText("Refresh VM loaded classes");
        });
        buttons.addSplitter(1);
        buttons.createButton(Icons.iconQuad("disconnect", 14), button -> {
            button.addActionListener(this.disconnectListenBuss::listen);
            button.setToolTipText("Disconnect from VM");
        });
        buttons.addSplitter(1);
        buttons.createButton(Icons.iconQuad("expand", 14), button -> {
            button.addActionListener(e -> inputTree.expandSelected());
            button.setToolTipText("Expand selected");
        });
        buttons.addSplitter(1);
        buttons.createButton(Icons.iconQuad("collapse", 14), button -> {
            button.addActionListener(e -> inputTree.collapseAll());
            button.setToolTipText("Collapse all");
        });
        buttons.addSplitter(1);
        buttons.createButton(Icons.iconQuad("pointer_right", 14), button -> {
            button.addActionListener(e -> this.grabTree.addClassNames(this.inputTree.selectedClasses()));
            button.setToolTipText("Add to grab");
        });
        buttons.configure(button -> {
            button.setBorder(new FlatButtonBorderExt().setFocusWidth(0));
            button.setPreferredSize(new Dimension(20, 18));
            button.setFocusable(false);
        });
        buttons.setBorder(BorderFactory.createEmptyBorder(1, 1, 2, 0));
//        topPanel.setBorder(BorderFactory.createLineBorder(Color.RED));
        topPanel.add(buttons);
        JCTreeSearchPanel panelSearch = new JCTreeSearchPanel(inputTree);
        panelSearch.setBorder(BorderFactory.createCompoundBorder(panelSearch.getBorder(), BorderFactory.createEmptyBorder(0, 0, 1, 0)));

        CardLayout card = new CardLayout();
        JPanel panelMulti = new JPanel(card);

        JPanel panelBar = new JPanel();
        panelBar.setLayout(new BorderLayout());
        JProgressBar bar = new JProgressBar(0, 100000);
        bar.setPreferredSize(new Dimension(0, 0));
        panelBar.add(bar, BorderLayout.CENTER);
        panelBar.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 1));

        panelMulti.add(panelSearch, "search");
        panelMulti.add(panelBar, "bar");

        topPanel.add(panelMulti);

        JPanel panelInput = new JPanel(new BorderLayout());
        panelInput.add(topPanel, BorderLayout.NORTH);
        panelInput.add(inputTree, BorderLayout.CENTER);

        this.inputCard = new JCardContainer(panelMulti, card);
        this.bar = bar;

        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem addToGrab = new JMenuItem("Add to grab", Icons.iconQuad("pointer_right", 14));
        addToGrab.addActionListener(e -> this.grabTree.addClassNames(this.inputTree.selectedClasses()));
        JMenuItem expand = new JMenuItem("Expand", Icons.iconQuad("expand", 16));
        expand.addActionListener(e -> inputTree.expandSelected());
        JMenuItem collapse = new JMenuItem("Collapse", Icons.iconQuad("collapse", 16));
        collapse.addActionListener(e -> inputTree.collapseSelected());
        popupMenu.add(addToGrab);
        JPopupMenu.Separator separatorEx = new JPopupMenu.Separator();
        popupMenu.add(separatorEx);
        popupMenu.add(expand);
        popupMenu.add(collapse);
        popupMenu.addSeparator();

        JMenuItem copyPackage = new JMenuItem("Copy package", Icons.iconQuad("copy", 16));
        copyPackage.addActionListener(e -> {
            List<String> list = inputTree.selectedClasses();
            if (list.size() == 1) {
                String selected = list.get(0);
                String toCopy = selected.substring(0, selected.lastIndexOf("."));
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(toCopy), null);
            }
        });
        JMenuItem copyName = new JMenuItem("Copy class name", Icons.iconQuad("copy", 16));
        copyName.addActionListener(e -> {
            List<String> list = inputTree.selectedClasses();
            if (list.size() == 1) {
                String selected = list.get(0);
                String toCopy = selected.substring(selected.lastIndexOf(".") + 1);
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(toCopy), null);
            }
        });
        JMenuItem copyFullName = new JMenuItem("Copy full class name", Icons.iconQuad("copy", 16));
        copyFullName.addActionListener(e -> {
            List<String> list = inputTree.selectedClasses();
            if (list.size() == 1) {
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(list.get(0)), null);
            }
        });
        JPopupMenu.Separator separator = new JPopupMenu.Separator();
        popupMenu.add(copyFullName);
        popupMenu.add(copyName);
        popupMenu.add(copyPackage);
        popupMenu.add(separator);

        JMenuItem reload = new JMenuItem("Refresh all", Icons.iconQuad("refresh", 16));
        reload.addActionListener(this.refreshListenBuss::listen);
        JMenuItem disconnect = new JMenuItem("Disconnect from VM", Icons.iconQuad("disconnect", 16));
        disconnect.addActionListener(this.disconnectListenBuss::listen);

        popupMenu.add(disconnect);
        popupMenu.add(reload);

        popupMenu.pack();

        popupMenu.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                boolean stateCopy = inputTree.isSelectedClass();
                copyPackage.setVisible(stateCopy);
                copyFullName.setVisible(stateCopy);
                copyName.setVisible(stateCopy);
                separator.setVisible(stateCopy);
                boolean stateExpand = !stateCopy;
                expand.setVisible(stateExpand);
                collapse.setVisible(stateExpand);
                separatorEx.setVisible(stateExpand);
            }
            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {}
            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {}
        });

        JPopupMenu popupMenuDef = new JPopupMenu();
        JMenuItem reloadDef = new JMenuItem("Refresh all", Icons.iconQuad("refresh", 16));
        reloadDef.addActionListener(this.refreshListenBuss::listen);
        JMenuItem disconnectDef = new JMenuItem("Disconnect from VM", Icons.iconQuad("disconnect", 16));
        disconnectDef.addActionListener(this.disconnectListenBuss::listen);
        popupMenuDef.add(disconnectDef);
        popupMenuDef.add(reloadDef);

        inputTree.setDefaultPopupMenu(popupMenuDef);
        inputTree.setComponentPopupMenu(popupMenu);
        inputTree.tree.setDragEnabled(true);
        inputTree.tree.setTransferHandler(new TreeDragHandler());

        return panelInput;
    }

    public void addDisconnectListener(IListener<ActionEvent> listener) {
        this.disconnectListenBuss.registerListener(listener);
    }

    public void addRefreshListener(IListener<ActionEvent> listener) {
        this.refreshListenBuss.registerListener(listener);
    }

    public void addAbortListener(IListener<Void> listener) {
        this.grabAbortListenBuss.registerListener(listener);
    }

    public void addGrabListener(IListener<IGrabStartData> listener) {
        this.grabListenBuss.registerListener(listener);
    }

    public void grabDone() {
        if (this.grabFrame != null) {
            this.grabFrame.onDone();
        }
    }

    public void setGrabTimeLeft(long time) {
        if (this.grabFrame != null) {
            this.grabFrame.setLeftTime(time);
        }
    }

    public void setGrabState(String state) {
        if (this.grabFrame != null) {
            this.grabFrame.setState(state);
        }
    }

    public void setGrabProgress(Percent percent) {
        if (this.grabFrame != null) {
            this.grabFrame.setProgress(percent);
        }
    }

    public List<String> getClassesToGrab() {
        return this.grabTree.allClasses();
    }

    public void setProgress(Percent progress) {
        if (progress.isIndeterminate()) {
            this.bar.setValue(0);
            this.bar.setIndeterminate(true);
        } else {
            this.bar.setIndeterminate(false);
            this.bar.setValue(progress.x100kI());
        }
    }

    public void showProgressBar(boolean state) {
        this.inputTree.setDisabled(state);
        this.showProgressBarPrivate(state);
    }

    public void loadClasses(List<String> classes) {
        LOGGER.info("Loaded {} classes", classes.size());
        this.bar.setValue(0);
        this.showProgressBarPrivate(true);
        AsyncClassTreeBuilder builder = this.inputTree.setClassNames(classes);
        builder.addProcessListener(chunks -> this.setProgress(chunks.get(chunks.size() - 1)));
        builder.addDoneListener(result -> this.showProgressBarPrivate(false));
    }

    private void showProgressBarPrivate(boolean state) {
        if (state) {
            if (!this.inputCard.isShowed("bar")) {
                this.inputCard.show("bar");
            }
        } else {
            if (!this.inputCard.isShowed("search")) {
                this.inputCard.show("search");
            }
        }
    }

    private void showGrabDialog() {
        JGrabFrame frame = new JGrabFrame(Application.application());
        frame.addFrameCloseListener(value -> this.grabFrame = null);
        frame.addAbortListener(this.grabAbortListenBuss::listen);
        frame.addGrabListener(this.grabListenBuss::listen);
        Application.application().setEnabled(false);

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                frame.setVisible(false);
                SwingUtilities.invokeLater(() -> {
                    Application.application().setEnabled(true);
                    Application.application().toFront();
                });
            }
        });

        frame.view();
        this.grabFrame = frame;
    }

    private static class TreeDragHandler extends TransferHandler {
        @Override
        public int getSourceActions(JComponent c) {
            return TransferHandler.COPY;
        }

        @Override
        protected Transferable createTransferable(JComponent c) {
            JClassTree classTree = (JClassTree) SwingUtilities.getAncestorOfClass(JClassTree.class, c);

            if (classTree == null) {
                return null;
            }

            List<String> classNames = classTree.selectedClasses();

            if (classNames.isEmpty()) {
                return null;
            }

            return new StringSelection(String.join("\n", classNames));
        }
    }

    private static class TreeDropHandler extends TransferHandler {
        @Override
        public boolean canImport(TransferSupport support) {
            JClassTree targetPanel = (JClassTree) SwingUtilities.getAncestorOfClass(JClassTree.class, support.getComponent());
            if (support.isDataFlavorSupported(DataFlavor.stringFlavor) && targetPanel != null) {
                support.setShowDropLocation(false);
                return true;
            }

            return false;
        }

        @Override
        public boolean importData(TransferSupport support) {
            if (!this.canImport(support)) {
                return false;
            }
            try {
                JClassTree targetPanel = (JClassTree) SwingUtilities.getAncestorOfClass(JClassTree.class, support.getComponent());
                String data = (String) support.getTransferable().getTransferData(DataFlavor.stringFlavor);
                List<String> classNames = Arrays.asList(data.split("\n"));


                if (targetPanel == null) {
                    return false;
                }

                targetPanel.addClassNames(classNames);

                return true;
            } catch (Exception e) {
                e.printStackTrace(System.err);
            }
            return false;
        }
    }
}
