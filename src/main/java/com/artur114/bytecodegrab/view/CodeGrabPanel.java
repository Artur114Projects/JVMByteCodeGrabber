package com.artur114.bytecodegrab.view;

import com.artur114.bytecodegrab.jcomp.JCardContainer;
import com.artur114.bytecodegrab.jcomp.JClassTree;
import com.artur114.bytecodegrab.jcomp.JGrabFrame;
import com.artur114.bytecodegrab.main.Application;
import com.artur114.bytecodegrab.util.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Arrays;
import java.util.List;

public class CodeGrabPanel extends JPanel {
    private static final Logger LOGGER = LogManager.getLogger("View/CodeGrabPanel");
    private final IListenBuss<IListener<IGrabStartData>, IGrabStartData> grabListenBuss = new ArrayListenBuss<>();
    private final IListenBuss<IListener<Void>, Void> grabAbortListenBuss = new ArrayListenBuss<>();
    private JCardContainer inputCard;
    private JButton disconnectButton;
    private JButton refreshButton;
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
        splitPane.setDividerLocation(250);

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
        JButton buttonClear = new JButton(Icons.resizeIcon(Icons.icon("clear.png"), 14, 14));
        buttonClear.addActionListener(e -> grabTree.clear());
        buttonClear.setPreferredSize(new Dimension(20, 18));
        buttonClear.setFocusable(false);
        buttonClear.setToolTipText("Clear classes");

        JButton buttonRemove = new JButton(Icons.resizeIcon(Icons.icon("remove.png"), 14, 14));
        buttonRemove.setPreferredSize(new Dimension(20, 18));
        buttonRemove.setFocusable(false);
        buttonRemove.addActionListener(e -> grabTree.removeClassNames(grabTree.selectedClasses()));
        buttonRemove.setToolTipText("Remove selected");

        JButton buttonExpand = new JButton(Icons.resizeIcon(Icons.icon("expand.png"), 14, 14));
        buttonExpand.setPreferredSize(new Dimension(20, 18));
        buttonExpand.setFocusable(false);
        buttonExpand.addActionListener(e -> grabTree.expandSelected());
        buttonExpand.setToolTipText("Expand selected");

        JButton buttonCollapse = new JButton(Icons.resizeIcon(Icons.icon("collapse.png"), 14, 14));
        buttonCollapse.setPreferredSize(new Dimension(20, 18));
        buttonCollapse.setFocusable(false);
        buttonCollapse.addActionListener(e -> grabTree.collapseAll());
        buttonCollapse.setToolTipText("Collapse all");

        JPanel buttons = new JPanel();
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));
        buttons.add(buttonClear);
        buttons.add(buttonRemove);
        buttons.add(buttonExpand);
        buttons.add(buttonCollapse);
        buttons.setBorder(BorderFactory.createEmptyBorder(1, 1, 0, 0));
        topGrab.add(buttons, BorderLayout.WEST);

        JTextField search = new JTextField();
        search.setFont(search.getFont().deriveFont(11f));
        search.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                performSearch();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                performSearch();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {}

            private void performSearch() {
                String query = search.getText().trim();
                grabTree.searchBy(query);
            }
        });
        search.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1), BorderFactory.createLineBorder(Color.LIGHT_GRAY)));

        JPanel panel1 = new JPanel();
        panel1.setLayout(new BoxLayout(panel1, BoxLayout.X_AXIS));

        panel1.add(Box.createHorizontalStrut(4));
        JLabel label = new JLabel("Search: ");
        label.setFont(label.getFont().deriveFont(11f));
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 1, 0));
        panel1.add(label);
        panel1.add(search);
        panel1.setBorder(BorderFactory.createEmptyBorder(1, 0, 1, 0));

        topGrab.add(panel1, BorderLayout.CENTER);


        panel.add(topGrab, BorderLayout.NORTH);

        JPanel bottom = new JPanel(new BorderLayout());
        JTextField field = new JTextField();
        field.setFont(field.getFont().deriveFont(11f));
        field.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1), BorderFactory.createLineBorder(Color.LIGHT_GRAY)));
        bottom.add(field, BorderLayout.CENTER);

        JPanel panelGrab = new JPanel(new BorderLayout());
        JButton grab = new JButton("grab");
        grab.addActionListener(e -> {
            if (this.getClassesToGrab().isEmpty()) {
                JOptionPane.showMessageDialog(Application.application(), "Please add classes to grab pane", "Grabber", JOptionPane.WARNING_MESSAGE);
                return;
            }



            File file = new File(field.getText());

            if (file.getParentFile() == null || !file.getParentFile().exists()) {
                JFileChooser chooser = new JFileChooser();
                chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                chooser.setMultiSelectionEnabled(false);
                chooser.removeChoosableFileFilter(chooser.getChoosableFileFilters()[0]);
                chooser.addChoosableFileFilter(new FileFilter() {
                    @Override
                    public boolean accept(File f) {
                        return f.isDirectory() || f.getName().endsWith(".jar") || f.getName().endsWith(".zip");
                    }

                    @Override
                    public String getDescription() {
                        return "All formats (*.jar, *.zip, dir)";
                    }
                });
                chooser.addChoosableFileFilter(new FileFilter() {
                    @Override
                    public boolean accept(File f) {
                        return f.isDirectory() || f.getName().endsWith(".jar");
                    }

                    @Override
                    public String getDescription() {
                        return "jar files (*.jar)";
                    }
                });
                chooser.addChoosableFileFilter(new FileFilter() {
                    @Override
                    public boolean accept(File f) {
                        return f.isDirectory() || f.getName().endsWith(".zip");
                    }

                    @Override
                    public String getDescription() {
                        return "zip files (*.zip)";
                    }
                });
                chooser.addChoosableFileFilter(new FileFilter() {
                    @Override
                    public boolean accept(File f) {
                        return f.isDirectory();
                    }

                    @Override
                    public String getDescription() {
                        return "dirs";
                    }
                });

                int i = chooser.showDialog(Application.application(), "Set output");

                if (i == 0) {
                    field.setText(chooser.getSelectedFile().toString());

                    this.showGrabDialog(new File(field.getText()));
                }


            } else {
                this.showGrabDialog(file);
            }
        });
        grab.setPreferredSize(new Dimension(60, 19));
        panelGrab.setBorder(BorderFactory.createEmptyBorder(1, 0, 1, 1));
        panelGrab.add(grab, BorderLayout.CENTER);

        bottom.add(panelGrab, BorderLayout.EAST);
        bottom.setBorder(BorderFactory.createEmptyBorder(0, 0, 1, 0));
        panel.add(bottom, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel initInputPanel(JClassTree inputTree) {
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));

        JButton buttonRefresh = new JButton(Icons.resizeIcon(Icons.icon("refresh.png"), 14, 14));
        buttonRefresh.setPreferredSize(new Dimension(20, 18));
        buttonRefresh.setFocusable(false);
        buttonRefresh.setToolTipText("Refresh VM loaded classes");

        JButton buttonDisconnect = new JButton(Icons.resizeIcon(Icons.icon("disconnect.png"), 14, 14));
        buttonDisconnect.setPreferredSize(new Dimension(20, 18));
        buttonDisconnect.setFocusable(false);
        buttonDisconnect.setToolTipText("Disconnect from VM");

        JButton buttonExpand = new JButton(Icons.resizeIcon(Icons.icon("expand.png"), 14, 14));
        buttonExpand.setPreferredSize(new Dimension(20, 18));
        buttonExpand.setFocusable(false);
        buttonExpand.addActionListener(e -> inputTree.expandSelected());
        buttonExpand.setToolTipText("Expand selected");

        JButton buttonCollapse = new JButton(Icons.resizeIcon(Icons.icon("collapse.png"), 14, 14));
        buttonCollapse.setPreferredSize(new Dimension(20, 18));
        buttonCollapse.setFocusable(false);
        buttonCollapse.addActionListener(e -> inputTree.collapseAll());
        buttonCollapse.setToolTipText("Collapse all");

        JButton buttonAddToGrab = new JButton(Icons.resizeIcon(Icons.icon("pointer_right.png"), 14, 14));
        buttonAddToGrab.setPreferredSize(new Dimension(20, 18));
        buttonAddToGrab.setFocusable(false);
        buttonAddToGrab.addActionListener(e -> this.grabTree.addClassNames(this.inputTree.selectedClasses()));
        buttonAddToGrab.setToolTipText("Add to grab");

        JPanel buttons = new JPanel();
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));
        buttons.add(buttonRefresh);
        buttons.add(buttonDisconnect);
        buttons.add(buttonExpand);
        buttons.add(buttonCollapse);
        buttons.add(buttonAddToGrab);
        buttons.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 0));
        topPanel.add(buttons);


        JTextField searchField = new JTextField();
        searchField.setFont(searchField.getFont().deriveFont(11f));
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                performSearch();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                performSearch();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {}

            private void performSearch() {
                String query = searchField.getText().trim();
                inputTree.searchBy(query);
            }
        });
        searchField.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1), BorderFactory.createLineBorder(Color.LIGHT_GRAY)));

        CardLayout card = new CardLayout();
        JPanel panelMulti = new JPanel(card);

        JPanel panelSearch = new JPanel();
        panelSearch.setLayout(new BoxLayout(panelSearch, BoxLayout.X_AXIS));

        panelSearch.add(Box.createHorizontalStrut(4));
        JLabel label = new JLabel("Search: ");
        label.setFont(label.getFont().deriveFont(11f));
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 1, 0));
        panelSearch.add(label);
        panelSearch.add(searchField);
        panelSearch.setBorder(BorderFactory.createEmptyBorder(1, 0, 1, 0));


        JPanel panelBar = new JPanel();
        panelBar.setLayout(new BorderLayout());
        JProgressBar bar = new JProgressBar(0, 100000);
        bar.setPreferredSize(new Dimension(0, 0));
        panelBar.add(bar, BorderLayout.CENTER);
        panelBar.setBorder(BorderFactory.createEmptyBorder(2, 2, 1, 1));

        panelMulti.add(panelSearch, "search");
        panelMulti.add(panelBar, "bar");

        topPanel.add(panelMulti);

        JPanel panelInput = new JPanel(new BorderLayout());
        panelInput.add(topPanel, BorderLayout.NORTH);
        panelInput.add(inputTree, BorderLayout.CENTER);

        this.inputCard = new JCardContainer(panelMulti, card);
        this.disconnectButton = buttonDisconnect;
        this.refreshButton = buttonRefresh;
        this.bar = bar;

        return panelInput;
    }

    public void addDisconnectListener(ActionListener listener) {
        this.disconnectButton.addActionListener(listener);
    }

    public void addRefreshListener(ActionListener listener) {
        this.refreshButton.addActionListener(listener);
    }

    public void addGrabListener(IListener<IGrabStartData> listener) {
        this.grabListenBuss.registerListener(listener);
    }

    public void grabDone() {
        if (this.grabFrame != null) {
            this.grabFrame.onDone();
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
            this.bar.setValue(progress.x100k());
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

    private void showGrabDialog(File file) {
        JGrabFrame frame = new JGrabFrame(Application.application(), file);
        frame.addFrameCloseListener(value -> this.grabFrame = null);
        frame.addAbortListener(this.grabAbortListenBuss::listen);
        frame.addGrabListener(this.grabListenBuss::listen);
        Application.application().setEnabled(false);

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                Application.application().setEnabled(true);
                Application.application().toFront();
            }
        });

        frame.setVisible(true);
        this.grabFrame = frame;
    }
}
