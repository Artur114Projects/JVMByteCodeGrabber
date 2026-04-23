package com.artur114.bytecodegrab.util;

import com.artur114.bytecodegrab.jcomp.JClassTree;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.*;

public class AsyncClassTreeBuilder extends SwingWorkerListened<DefaultMutableTreeNode, Percent> {
    private final List<String> classNames;

    public AsyncClassTreeBuilder(Set<String> classNames) {
        this.classNames = new ArrayList<>(classNames);
    }

    @Override
    protected DefaultMutableTreeNode doInBackground() throws Exception {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(new JClassTree.PackageInfo("root"));
        this.sortClassNamespace(this.classNames);
        Percent percent = new Percent();

        for (int i = 0; i != this.classNames.size(); i++) {
            this.publish(percent.setPercent(i, this.classNames.size()));
            if (this.isCancelled()) {
                return root;
            }
            String name = this.classNames.get(i);
            String path = this.classPath(name);
            DefaultMutableTreeNode node = this.buildPath(path, root);
            node.add(new DefaultMutableTreeNode(new JClassTree.ClassInfo(name)));
        }

        this.publish(percent.setIndeterminate(true));

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

    private String classPath(String className) {
        int last = className.lastIndexOf(".");

        if (last != -1) {
            return className.substring(0, last);
        } else {
            return "without_package";
        }
    }

    private DefaultMutableTreeNode buildPath(String path, DefaultMutableTreeNode from) {
        DefaultMutableTreeNode current = from;

        for (String node : path.split("\\.")) {
            DefaultMutableTreeNode child = this.childPackage(current, node);
            if (child == null) {
                child = new DefaultMutableTreeNode(new JClassTree.PackageInfo(node));
                current.add(child);
            }

            current = child;
        }

        return current;
    }

    private DefaultMutableTreeNode childPackage(DefaultMutableTreeNode node, String packageName) {
        for (int i = 0; i != node.getChildCount(); i++) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) node.getChildAt(i);
            if (child.getUserObject() instanceof JClassTree.PackageInfo && packageName.equals(((JClassTree.PackageInfo) child.getUserObject()).name)) {
                return child;
            }
        }
        return null;
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

}
