package com.artur114.bytecodegrab.jcomp;

import com.artur114.bytecodegrab.util.AsyncClassTreeBuilder;
import com.artur114.bytecodegrab.util.Icons;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;

import javax.swing.Timer;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.List;
import java.util.*;

public class JClassTree extends JPanel {
    private static final Color COLOR = new Color(239, 239, 239);
    private static final Logger log = LoggerFactory.getLogger(JClassTree.class);
    private final Set<String> loadedClasses = new HashSet<>();
    private final DefaultTreeModel treeModel;
    private AsyncClassTreeBuilder builder;
    private String currentFilter;
    public boolean keepExpanded = false;
    private JPopupMenu popup;
    public final JTree tree;

    public JClassTree() {
        setLayout(new BorderLayout());
        this.treeModel = new DefaultTreeModel(new DefaultMutableTreeNode(new PackageInfo("root")));
        this.tree = new JTree(this.treeModel);
        this.tree.setRootVisible(true);
        this.tree.setShowsRootHandles(true);
        this.tree.setCellRenderer(new ClassTreeCellRenderer());
        this.tree.collapseRow(0);
        this.tree.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                if (popup != null && e.isPopupTrigger() && SwingUtilities.isRightMouseButton(e)) {
                    int row = rowForY(tree, e.getY());
                    boolean outOfBounds = row == -1;

                    if (!outOfBounds) {
                        tree.requestFocusInWindow();
                        tree.setSelectionRow(row);
                        popup.show(tree, e.getX(), e.getY());
                    }
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (popup != null && e.isPopupTrigger() && SwingUtilities.isRightMouseButton(e)) {
                    int row = rowForY(tree, e.getY());
                    boolean outOfBounds = row == -1;

                    if (!outOfBounds) {
                        tree.requestFocusInWindow();
                        tree.setSelectionRow(row);
                        popup.show(tree, e.getX(), e.getY());
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                int row = rowForY(tree, e.getY());
                boolean outOfBounds = row == -1;
                if (SwingUtilities.isLeftMouseButton(e)) {
                    if (outOfBounds) {
                        tree.clearSelection();
                        tree.getParent().requestFocusInWindow();
                    } else {
                        tree.requestFocusInWindow();
                    }
                }
                if (!outOfBounds && popup != null && e.isPopupTrigger() && SwingUtilities.isRightMouseButton(e)) {
                    tree.requestFocusInWindow();
                    tree.setSelectionRow(row);
                    popup.show(tree, e.getX(), e.getY());
                }
            }

            private Rectangle allRowsRectangle(JTree tree) {
                if (tree.getRowCount() <= 0) {
                    return new Rectangle();
                }
                Rectangle ret = tree.getRowBounds(0);
                if (tree.getRowCount() > 1) {
                    for (int i = 1; i != tree.getRowCount(); i++) {
                        ret.add(tree.getRowBounds(i));
                    }
                }
                return ret;
            }

            private int rowForY(JTree tree, int y) {
                if (tree.getRowCount() <= 0) {
                    return -1;
                }
                Rectangle ret = tree.getRowBounds(0);

                int row = y / ret.height;

                if (row > tree.getRowCount()) {
                    return -1;
                }

                return row;
            }
        });

        JScrollPane pane = new JScrollPane(this.tree);
        pane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        this.add(pane, BorderLayout.CENTER);
        this.tree.setBackground(COLOR);

        this.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
    }

    public AsyncClassTreeBuilder removeClassNames(List<String> classNames) {
        classNames.forEach(this.loadedClasses::remove);

        if (this.currentFilter == null || this.currentFilter.isEmpty()) {
            return this.updateTree(this.loadedClasses);
        } else {
            return this.searchBy(this.currentFilter);
        }
    }

    public AsyncClassTreeBuilder addClassNames(List<String> classNames) {
        this.loadedClasses.addAll(classNames);

        if (this.currentFilter == null || this.currentFilter.isEmpty()) {
            return this.updateTree(this.loadedClasses);
        } else {
            return this.searchBy(this.currentFilter);
        }
    }

    public AsyncClassTreeBuilder setClassNames(List<String> classNames) {
        this.loadedClasses.clear();
        this.loadedClasses.addAll(classNames);

        if (this.currentFilter == null || this.currentFilter.isEmpty()) {
            return this.updateTree(this.loadedClasses);
        } else {
            return this.searchBy(this.currentFilter);
        }
    }

    public AsyncClassTreeBuilder updateTree(Set<String> classNames) {
        if (this.builder != null && !this.builder.isDone()) {
            this.builder.cancel(true);
        }

        Timer timer = new Timer(50, e -> {
            if (!this.builder.isDone()) {
                this.tree.setEnabled(false);
            }
        });
        timer.setRepeats(false);
        timer.start();

        this.builder = new AsyncClassTreeBuilder(classNames);
        this.builder.execute();

        this.builder.addDoneListener((root) -> {
            this.tree.setEnabled(true);
            this.treeModel.setRoot(root);
            this.treeModel.reload();

            if (this.keepExpanded) {
                this.expandAll();
            }
        });

        return this.builder;
    }

    public void clear() {
        this.treeModel.setRoot(null);
        this.loadedClasses.clear();
        this.treeModel.reload();
    }

    public void setDisabled(boolean state) {
        this.tree.setEnabled(!state);
    }

    public AsyncClassTreeBuilder searchBy(String className) {
        this.currentFilter = className;

        if (className.isEmpty()) {
            return this.updateTree(this.loadedClasses);
        }

        Set<String> search = new HashSet<>(this.loadedClasses);
        search.removeIf((s -> !s.contains(className)));

        AsyncClassTreeBuilder builder = this.updateTree(search);
        builder.addDoneListener((d) -> {
            if (!this.keepExpanded) {
                this.expandAll();
            }
        });

        return builder;
    }

    public List<String> selectedClasses() {
        TreePath[] paths = this.tree.getSelectionPaths();
        List<String> list = new ArrayList<>();
        if (paths == null) {
            return list;
        }

        for (TreePath path : paths) {
            this.fillClasses(list, (DefaultMutableTreeNode) path.getLastPathComponent());
        }

        return list;
    }

    public List<String> allClasses() {
        return new ArrayList<>(this.loadedClasses);
    }

    public void expandSelected() {
        TreePath[] paths = this.tree.getSelectionPaths();
        if (paths == null) {
            return;
        }
        for (TreePath path : paths) {
            this.expandChild(path, (DefaultMutableTreeNode) path.getLastPathComponent());
        }
    }

    public void collapseSelected() {
        TreePath[] paths = this.tree.getSelectionPaths();
        if (paths == null) {
            return;
        }
        for (TreePath path : paths) {
            this.collapseChild(path, (DefaultMutableTreeNode) path.getLastPathComponent());
        }
    }

    public void expandAll() {
        for (int i = 0; i != this.tree.getRowCount(); i++) {
            this.tree.expandRow(i);
        }
    }

    public void collapseAll() {
        for (int i = this.tree.getRowCount() - 1; i >= 0; i--) {
            TreePath path = this.tree.getPathForRow(i);
            if (path != null && path.getPathCount() > 1) {
                this.tree.collapseRow(i);
            }
        }
    }

    public boolean isSelectedClass() {
        TreePath[] paths = this.tree.getSelectionPaths();
        return paths != null && paths.length >= 1 && ((DefaultMutableTreeNode) paths[0].getLastPathComponent()).getUserObject() instanceof ClassInfo;
    }

    private void expandChild(TreePath path, DefaultMutableTreeNode parent) {
        this.tree.expandPath(path);

        for (DefaultMutableTreeNode node : this.child(parent)) {
            TreePath newPath = path.pathByAddingChild(node);
            this.expandChild(newPath, node);
        }
    }

    private void collapseChild(TreePath path, DefaultMutableTreeNode parent) {
        for (DefaultMutableTreeNode node : this.child(parent)) {
            TreePath newPath = path.pathByAddingChild(node);
            this.collapseChild(newPath, node);
        }

        this.tree.collapsePath(path);
    }

    @Override
    public void setComponentPopupMenu(JPopupMenu popup) {
        this.popup = popup;
    }

    private void fillClasses(List<String> list, DefaultMutableTreeNode node) {
        Object obj = node.getUserObject();

        if (obj instanceof ClassInfo) {
            list.add(((ClassInfo) obj).fullName);
        }

        for (DefaultMutableTreeNode child : this.child(node)) {
            this.fillClasses(list, child);
        }
    }

    private List<DefaultMutableTreeNode> child(DefaultMutableTreeNode node) {
        List<DefaultMutableTreeNode> ret = new ArrayList<>(node.getChildCount());
        for (int i = 0; i != node.getChildCount(); i++) {
            ret.add((DefaultMutableTreeNode) node.getChildAt(i));
        }
        return ret;
    }

    public static class ClassInfo {
        private final String simpleName;
        public final String fullName;

        public ClassInfo(String name) {
            this.simpleName = name.substring(name.lastIndexOf(".") + 1);
            this.fullName = name;
        }

        @Override
        public String toString() {
            return this.simpleName;
        }
    }

    public static class PackageInfo {
        public int classesCount;
        public final String name;

        public PackageInfo(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            String classes = "classes";
            if (this.classesCount == 1) classes = "class";
            return String.format(this.name + " (%s %s)", this.classesCount, classes);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof PackageInfo) {
                return this.name.equals(((PackageInfo) obj).name);
            }
            return false;
        }

        public static PackageInfo merge(Object info, Object childInfo) {
            return merge((PackageInfo) info, (PackageInfo) childInfo);
        }

        public static PackageInfo merge(PackageInfo info, PackageInfo childInfo) {
            return new PackageInfo(info.name + "." + childInfo.name);
        }
    }

    private static class ClassTreeCellRenderer extends DefaultTreeCellRenderer {
        private final Icon packageIcon = Icons.resizeIcon(Icons.icon("folder.png"), 20, 20);
        private final Icon classIcon = Icons.resizeIcon(Icons.icon("java_class.png"), 20, 20);
        private final Icon classIconU = Icons.resizeIcon(Icons.icon("java_class_u.png"), 20, 20);
        private final Icon packageIconD = Icons.resizeIcon(Icons.icon("folder_d.png"), 20, 20);
        private final Icon classIconD = Icons.resizeIcon(Icons.icon("java_class_d.png"), 20, 20);


        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean exp, boolean leaf, int row, boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, sel, exp, leaf, row, hasFocus);
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
            Object userObj = node.getUserObject();
            if (userObj instanceof ClassInfo) {
                this.setDisabledIcon(classIconD);
                if (((ClassInfo) userObj).fullName.startsWith("!")) {
                    this.setIcon(classIconU);
                } else {
                    this.setIcon(classIcon);
                }
            } else {
                this.setDisabledIcon(packageIconD);
                this.setIcon(packageIcon);
            }
            this.setBackgroundNonSelectionColor(COLOR);
            return this;
        }
    }
}