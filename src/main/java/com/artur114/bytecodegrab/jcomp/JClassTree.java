package com.artur114.bytecodegrab.jcomp;

import com.artur114.bytecodegrab.util.AsyncClassTreeBuilder;
import com.artur114.bytecodegrab.util.AsyncTreeManager;
import com.artur114.bytecodegrab.util.Icons;
import com.artur114.bytecodegrab.util.Theme;

import javax.swing.*;

import javax.swing.Timer;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.*;

public class JClassTree extends JPanel {
    private final Set<String> loadedClasses = new HashSet<>();
    private final DefaultTreeModel treeModel;
    private AsyncTreeManager currentManager;
    private AsyncClassTreeBuilder builder;
    private String currentFilter;
    public boolean keepExpanded = false;
    private JPopupMenu popupDef;
    private int lastMouseY = -1;
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
                if (e.isPopupTrigger() && SwingUtilities.isRightMouseButton(e)) {
                    lastMouseY = e.getY();
                    int row = rowForY(e.getY());
                    boolean outOfBounds = row == -1;

                    if (!outOfBounds) {
                        if (popup != null) {
                            tree.requestFocusInWindow();
                            if (!tree.isRowSelected(row)) {
                                tree.setSelectionRow(row);
                            }
                            popup.show(tree, e.getX(), e.getY());
                        }
                    } else {
                        if (popupDef != null) {
                            popupDef.show(tree, e.getX(), e.getY());
                        }
                    }
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.isPopupTrigger() && SwingUtilities.isRightMouseButton(e)) {
                    lastMouseY = e.getY();
                    int row = rowForY(e.getY());
                    boolean outOfBounds = row == -1;

                    if (!outOfBounds) {
                        if (popup != null) {
                            tree.requestFocusInWindow();
                            if (!tree.isRowSelected(row)) {
                                tree.setSelectionRow(row);
                            }
                            popup.show(tree, e.getX(), e.getY());
                        }
                    } else {
                        if (popupDef != null) {
                            popupDef.show(tree, e.getX(), e.getY());
                        }
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                int row = rowForY(e.getY());
                boolean outOfBounds = row == -1;
                if (SwingUtilities.isLeftMouseButton(e)) {
                    if (outOfBounds) {
                        tree.clearSelection();
                        tree.getParent().requestFocusInWindow();
                    } else {
                        tree.requestFocusInWindow();
                    }
                }
                if (e.isPopupTrigger() && SwingUtilities.isRightMouseButton(e)) {
                    lastMouseY = e.getY();
                    if (!outOfBounds) {
                        if (popup != null) {
                            tree.requestFocusInWindow();
                            if (!tree.isRowSelected(row)) {
                                tree.setSelectionRow(row);
                            }
                            popup.show(tree, e.getX(), e.getY());
                        }
                    } else {
                        if (popupDef != null) {
                            popupDef.show(tree, e.getX(), e.getY());
                        }
                    }
                }
            }
        });

        JScrollPane pane = new JScrollPane(this.tree);
        Theme.borderColor().ifPresent(color -> pane.setBorder(BorderFactory.createLineBorder(color)));
        this.add(pane, BorderLayout.CENTER);
        Theme.classThreeColor().ifPresent(this.tree::setBackground);

        this.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
    }

    public AsyncClassTreeBuilder removeClassNames(List<String> classNames) {
        classNames.forEach(this.loadedClasses::remove);

        if (this.currentFilter == null || this.currentFilter.isEmpty()) {
            AsyncClassTreeBuilder build = AsyncClassTreeBuilder.remove(this.notNullRoot(), new HashSet<>(classNames));
            build.execute();
            return this.runBuilder(build);
        } else {
            return this.searchBy(this.currentFilter);
        }
    }

    public AsyncClassTreeBuilder addClassNames(List<String> classNames) {
        classNames.removeAll(this.loadedClasses);
        this.loadedClasses.addAll(classNames);

        if (this.currentFilter == null || this.currentFilter.isEmpty()) {
            AsyncClassTreeBuilder build = AsyncClassTreeBuilder.add(this.notNullRoot(), new HashSet<>(classNames));
            build.execute();
            return this.runBuilder(build);
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
        AsyncClassTreeBuilder build = AsyncClassTreeBuilder.build(classNames);
        build.execute();
        return this.runBuilder(build);
    }

    public void clear() {
        this.treeModel.setRoot(null);
        this.loadedClasses.clear();
        this.treeModel.reload();
    }

    public int rowForY(int y) {
        if (this.tree.getRowCount() <= 0) {
            return -1;
        }
        Rectangle ret = this.tree.getRowBounds(0);

        int row = y /  ret.height;

        if (row >= this.tree.getRowCount()) {
            return -1;
        }

        return row;
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

        if (this.currentManager != null && !this.currentManager.isDone()) {
            this.currentManager.cancel(true);
        }

        this.currentManager = AsyncTreeManager.expand(paths, this.tree);
    }

    public void collapseSelected() {
        TreePath[] paths = this.tree.getSelectionPaths();
        if (paths == null) {
            return;
        }

        if (this.currentManager != null && !this.currentManager.isDone()) {
            this.currentManager.cancel(true);
        }

        this.currentManager = AsyncTreeManager.collapse(paths, this.tree);
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
        if (this.lastMouseY == -1) return false;
        TreePath paths = this.tree.getPathForRow(this.rowForY(this.lastMouseY));
        return paths != null && ((DefaultMutableTreeNode) paths.getLastPathComponent()).getUserObject() instanceof ClassInfo;
    }

    @Override
    public void setComponentPopupMenu(JPopupMenu popup) {
        this.popup = popup;
    }

    public void setDefaultPopupMenu(JPopupMenu popup) {
        this.popupDef = popup;
    }

    private AsyncClassTreeBuilder runBuilder(AsyncClassTreeBuilder builder) {
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
        Set<TreePath> expanded = this.saveExpandedPaths();
        builder.addDoneListener((root) -> {
            this.tree.setEnabled(true);
            if (this.treeModel.getRoot() != root) {
                this.treeModel.setRoot(root);
                this.treeModel.reload();
            } else {
                this.treeModel.reload();
                this.restoreExpandedPaths(expanded);
            }

            if (this.keepExpanded) {
                this.expandAll();
            }
        });

        return this.builder = builder;
    }

    private Set<TreePath> saveExpandedPaths() {
        Set<TreePath> paths = new HashSet<>();
        for (int i = 0; i < this.tree.getRowCount(); i++) {
            if (this.tree.isExpanded(i)) {
                paths.add(this.tree.getPathForRow(i));
            }
        }
        return paths;
    }

    private void restoreExpandedPaths(Set<TreePath> paths) {
        for (TreePath path : paths) {
            try {
                this.tree.expandPath(path);
            } catch (Exception ignored) {}
        }
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

    private DefaultMutableTreeNode notNullRoot() {
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) this.treeModel.getRoot();
        if (root == null) {
            root = new DefaultMutableTreeNode(new JClassTree.PackageInfo("root"));
            this.treeModel.setRoot(root);
        }
        return root;
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
        public final List<String> pack;
        public final String name;
        public int classesCount;

        public PackageInfo(String name) {
            this.pack = Collections.singletonList(name);
            this.name = this.formatPack(this.pack);
        }

        public PackageInfo(List<String> names) {
            this.name = this.formatPack(names);
            this.pack = names;
        }

        public String firstName() {
            return this.pack.get(0);
        }

        public String lastLast() {
            return this.pack.get(this.pack.size() - 1);
        }

        public boolean isCompressed() {
            return this.pack.size() > 1;
        }

        public boolean hasPackage(String name) { // TODO Говно, не работает, починить
            return this.pack.contains(name);
        }

        private String formatPack(List<String> pack) {
            StringBuilder builder = new StringBuilder();
            for (String p : pack) {
                builder.append(p).append('.');
            }
            return builder.substring(0, builder.lastIndexOf("."));
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
            List<String> ret = new ArrayList<>(info.pack);
            ret.addAll(childInfo.pack);
            return new PackageInfo(Collections.unmodifiableList(ret));
        }
    }

    private static class ClassTreeCellRenderer extends DefaultTreeCellRenderer {
        private final Icon packageIcon = Icons.iconQuad("folder", 20);
        private final Icon packageIconD = Icons.iconQuadD("folder", 20);
        private final Icon classIcon = Icons.icon("java_class");
        private final Icon classIconD = Icons.iconD("java_class");


        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean exp, boolean leaf, int row, boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, sel, exp, leaf, row, hasFocus);
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
            Object userObj = node.getUserObject();
            if (userObj instanceof ClassInfo) {
                this.setDisabledIcon(classIconD);
                this.setIcon(classIcon);
            } else {
                this.setDisabledIcon(packageIconD);
                this.setIcon(packageIcon);
            }
            Theme.classThreeColor().ifPresent(this::setBackgroundNonSelectionColor);
            return this;
        }
    }
}