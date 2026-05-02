package com.artur114.bytecodegrab.util;

import com.artur114.bytecodegrab.jcomp.JClassTree;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.*;

public class AsyncClassTreeBuilder extends SwingWorkerListened<DefaultMutableTreeNode, Percent> {
    private final DefaultMutableTreeNode root;
    private final List<String> classes;
    private final Mode mode;

    private AsyncClassTreeBuilder(@Nullable DefaultMutableTreeNode root, Set<String> classes, Mode mode) {
        this.classes = new ArrayList<>(classes);
        this.root = root;
        this.mode = mode;

        if (mode != Mode.BUILD) {
            Objects.requireNonNull(this.root);
        }
    }

    @Override
    protected DefaultMutableTreeNode doInBackground() throws Exception {
        switch (this.mode) {
            case ADD:
                return this.add();
            case REMOVE:
                return this.remove();
            case BUILD:
                return this.build();
            default:
                throw new IllegalStateException("Someone faked universe!");
        }
    }

    private DefaultMutableTreeNode add() {
        DefaultMutableTreeNode root = this.root;
        this.sortClassNamespace(this.classes);
        Percent percent = new Percent();

        for (int i = 0; i != this.classes.size(); i++) {
            this.publish(percent.setPercent(i, this.classes.size()));
            if (this.isCancelled()) {
                return root;
            }
            String name = this.classes.get(i);
            String path = this.classPath(name);
            DefaultMutableTreeNode node = this.buildPath(path, root);
            node.add(new DefaultMutableTreeNode(new JClassTree.ClassInfo(name)));
        }

        this.publish(percent.setIndeterminate(true));

        return this.postProcess(root);
    }

    private DefaultMutableTreeNode remove() {
        DefaultMutableTreeNode root = this.root;
        this.sortClassNamespace(this.classes);
        Percent percent = new Percent();

        for (int i = 0; i != this.classes.size(); i++) {
            this.publish(percent.setPercent(i, this.classes.size()));
            if (this.isCancelled()) {
                return root;
            }
            String name = this.classes.get(i);
            String path = this.classPath(name);
            DefaultMutableTreeNode node = this.hasPath(path, root);
            if (node != null) {
                for (DefaultMutableTreeNode child : this.child(node)) {
                    if (child.getUserObject() instanceof JClassTree.ClassInfo && ((JClassTree.ClassInfo) child.getUserObject()).fullName.equals(name)) {
                        node.remove(child);
                    }
                }
            }
        }

        this.publish(percent.setIndeterminate(true));

        return this.postProcess(root);
    }

    private DefaultMutableTreeNode build() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(new JClassTree.PackageInfo("root"));
        this.sortClassNamespace(this.classes);
        Percent percent = new Percent();

        for (int i = 0; i != this.classes.size(); i++) {
            this.publish(percent.setPercent(i, this.classes.size()));
            if (this.isCancelled()) {
                return root;
            }
            String name = this.classes.get(i);
            String path = this.classPath(name);
            DefaultMutableTreeNode node = this.buildPath(path, root);
            node.add(new DefaultMutableTreeNode(new JClassTree.ClassInfo(name)));
        }

        this.publish(percent.setIndeterminate(true));

        return this.postProcess(root);
    }

    private DefaultMutableTreeNode postProcess(DefaultMutableTreeNode root) {
        if (!this.validateBranch(root)) {
            return root;
        }

        ArrayDeque<DefaultMutableTreeNode> queue = new ArrayDeque<>();
        queue.addLast(root);

        while (!queue.isEmpty()) {
            DefaultMutableTreeNode node = queue.pollFirst();
            DefaultMutableTreeNode nodeParent = (DefaultMutableTreeNode) node.getParent();
            List<DefaultMutableTreeNode> childNodes = this.child(node);

            if (this.isCancelled()) {
                return root;
            }

            if (nodeParent == null) {
                for (DefaultMutableTreeNode child : childNodes) {
                    queue.addLast(child);
                }
                continue;
            }

            if (childNodes.size() == 1 && !(childNodes.get(0).getUserObject() instanceof JClassTree.ClassInfo)) {
                DefaultMutableTreeNode newNode = this.compress(nodeParent, node, childNodes.get(0));
                for (DefaultMutableTreeNode child : this.child(newNode)) {
                    queue.addLast(child);
                }
                queue.addLast(nodeParent);
                continue;
            }

            for (DefaultMutableTreeNode child : childNodes) {
                queue.addLast(child);
            }
        }

        this.classesCount(root);

        return root;
    }

    private void sortClassNamespace(List<String> list) {
        list.sort(Comparator.comparingInt(this::classWeight));
    }

    private int classWeight(String str) {
        int ret = 0;
        for (int i = 0; i != str.length(); i++) {
            if (str.charAt(i) == '.') {
                ret++;
            }
        }
        return -ret;
    }

    private int classesCount(DefaultMutableTreeNode node) {
        Object user = node.getUserObject();

        if (user instanceof JClassTree.ClassInfo) {
            return 1;
        }

        if (user instanceof JClassTree.PackageInfo) {
            int ret = 0;
            for (DefaultMutableTreeNode child : this.child(node)) {
                ret += this.classesCount(child);
            }
            ((JClassTree.PackageInfo) user).classesCount = ret;
            return ret;
        }

        return 0;
    }

    private boolean validateBranch(DefaultMutableTreeNode node) {
        for (DefaultMutableTreeNode child : this.child(node)) {
            if (child.getUserObject() instanceof JClassTree.PackageInfo && child.isLeaf()) {
                node.remove(child); continue;
            }

            if (!this.validateBranch(child)) {
                node.remove(child);
            }
        }

        return node.getUserObject() instanceof JClassTree.ClassInfo || !node.isLeaf();
    }

    private String classPath(String className) {
        int last = className.lastIndexOf(".");

        if (last != -1) {
            return className.substring(0, last);
        } else {
            return "without_package";
        }
    }

    public DefaultMutableTreeNode branch(DefaultMutableTreeNode from, List<String> context) {
        JClassTree.PackageInfo info = (JClassTree.PackageInfo) from.getUserObject();
        if (!info.isCompressed()) {
            DefaultMutableTreeNode ret = new DefaultMutableTreeNode(context.get(context.size() - 1));
            from.add(ret);
            return ret;
        }
        int index = -1;
        for (int i = 0; i != context.size(); i++) {
            if (context.get(i).equals(info.firstName())) {
                index = i; break;
            }
        }
        if (index == -1) {
            return from;
        }
        List<String> normalised = context.subList(index, context.size());
        int hasIndex = 0;
        String nodeName = "";
        for (int i = 0; i != normalised.size(); i++) {
            if (!info.hasPackage(normalised.get(i))) {
                nodeName = normalised.get(i); hasIndex = i; break;
            }
        }

        if (nodeName.isEmpty() && normalised.size() <= info.pack.size()) {
            return from;
        }

        if (hasIndex >= info.pack.size()) {
            DefaultMutableTreeNode ret = new DefaultMutableTreeNode(new JClassTree.PackageInfo(nodeName));
            from.add(ret);
            return ret;
        }

        DefaultMutableTreeNode norm = new DefaultMutableTreeNode(new JClassTree.PackageInfo(info.pack.subList(0, hasIndex)));
        this.replaceNode(from, norm);
        this.insertNext(norm, new DefaultMutableTreeNode(new JClassTree.PackageInfo(info.pack.subList(hasIndex, info.pack.size()))));
        DefaultMutableTreeNode ret = new DefaultMutableTreeNode(new JClassTree.PackageInfo(nodeName));
        norm.add(ret);

        return ret;
    }

    private DefaultMutableTreeNode hasPath(String path, DefaultMutableTreeNode from) {
        List<String> buildContext = new ArrayList<>();
        DefaultMutableTreeNode current = from;

        for (String node : path.split("\\.")) {
            buildContext.add(node);
            current = this.childPackage(current, buildContext);
            if (current == null) {
                return null;
            }
        }

        return current;
    }

    private DefaultMutableTreeNode buildPath(String path, DefaultMutableTreeNode from) {
        List<String> buildContext = new ArrayList<>();
        DefaultMutableTreeNode current = from;

        for (String node : path.split("\\.")) {
            buildContext.add(node); current = this.childPackageNNull(current, buildContext);
        }

        return current;
    }

    private @NotNull DefaultMutableTreeNode childPackageNNull(DefaultMutableTreeNode node, List<String> context) {
        String packageName = context.get(context.size() - 1);
        DefaultMutableTreeNode n = this.childPackage(node, context);
        if (n != null) return n;
        JClassTree.PackageInfo nodeInfo = ((JClassTree.PackageInfo) node.getUserObject());
        if (nodeInfo.isCompressed() && context.size() > 1 && nodeInfo.hasPackage(context.get(context.size() - 2))) {
            return this.branch(node, context);
        } else {
            DefaultMutableTreeNode ret = new DefaultMutableTreeNode(new JClassTree.PackageInfo(packageName));
            node.add(ret);
            return ret;
        }
    }

    private @Nullable DefaultMutableTreeNode childPackage(DefaultMutableTreeNode node, List<String> context) {
        String packageName = context.get(context.size() - 1);
        if (node.getUserObject() instanceof JClassTree.PackageInfo && ((JClassTree.PackageInfo) node.getUserObject()).hasPackage(packageName)) {
            return node;
        }
        for (int i = 0; i != node.getChildCount(); i++) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) node.getChildAt(i);
            if (child.getUserObject() instanceof JClassTree.PackageInfo && ((JClassTree.PackageInfo) child.getUserObject()).hasPackage(packageName)) {
                return child;
            }
        }
        return null;
    }

    private DefaultMutableTreeNode replaceNode(DefaultMutableTreeNode node, DefaultMutableTreeNode to) {
        List<DefaultMutableTreeNode> child = this.child(node);
        node.removeAllChildren();
        DefaultMutableTreeNode parent = ((DefaultMutableTreeNode) node.getParent());
        if (parent != null) {
            parent.remove(node);
            parent.add(to);
        }
        for (DefaultMutableTreeNode childChild : child) {
            to.add(childChild);
        }
        return to;
    }

    private DefaultMutableTreeNode insertNext(DefaultMutableTreeNode node, DefaultMutableTreeNode next) {
        List<DefaultMutableTreeNode> child = this.child(node);
        node.removeAllChildren();
        node.add(next);
        for (DefaultMutableTreeNode childChild : child) {
            next.add(childChild);
        }
        return next;
    }

    private DefaultMutableTreeNode compress(DefaultMutableTreeNode nodeParent, DefaultMutableTreeNode node, DefaultMutableTreeNode child) {
        nodeParent.remove(node);
        DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(JClassTree.PackageInfo.merge(node.getUserObject(), child.getUserObject()));
        nodeParent.add(newNode);
        for (DefaultMutableTreeNode childChild : this.child(child)) {
            newNode.add(childChild);
        }
        return newNode;
    }

    private List<DefaultMutableTreeNode> child(DefaultMutableTreeNode node) {
        List<DefaultMutableTreeNode> ret = new ArrayList<>(node.getChildCount());
        for (int i = 0; i != node.getChildCount(); i++) {
            ret.add((DefaultMutableTreeNode) node.getChildAt(i));
        }
        return ret;
    }

    private enum Mode {
        BUILD, ADD, REMOVE;
    }

    public static AsyncClassTreeBuilder build(Set<String> classNames) {
        return new AsyncClassTreeBuilder(null, classNames, Mode.BUILD);
    }

    public static AsyncClassTreeBuilder remove(DefaultMutableTreeNode root, Set<String> classNames) {
        return new AsyncClassTreeBuilder(root, classNames, Mode.REMOVE);
    }

    public static AsyncClassTreeBuilder add(DefaultMutableTreeNode root, Set<String> classNames) {
        return new AsyncClassTreeBuilder(root, classNames, Mode.ADD);
    }
}
