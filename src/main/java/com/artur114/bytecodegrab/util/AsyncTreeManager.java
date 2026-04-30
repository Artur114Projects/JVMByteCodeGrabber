package com.artur114.bytecodegrab.util;

import javax.swing.*;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class AsyncTreeManager extends SwingWorkerListened<Void, List<TreePath>> {
    private final boolean reverseCollect;
    private final Predicate<TreePath> shouldCollect;
    private final Consumer<TreePath> applyState;
    private final TreePath[] toExpand;
    private int batchSize = 25;
    private int sleepTime = 25;

    private AsyncTreeManager(TreePath[] toExpand, boolean reverseCollect, Consumer<TreePath> applyState, Predicate<TreePath> shouldCollect) {
        this.reverseCollect = reverseCollect;
        this.shouldCollect = shouldCollect;
        this.applyState = applyState;
        this.toExpand = toExpand;
    }

    public AsyncTreeManager setBatchSize(int batchSize) {
        this.batchSize = batchSize; return this;
    }

    public AsyncTreeManager setSleepTime(int sleepTime) {
        this.sleepTime = sleepTime; return this;
    }

    @Override
    protected Void doInBackground() throws Exception {
        List<TreePath> paths = new ArrayList<>();
        for (TreePath path : this.toExpand) {
            this.collectPaths(path, paths);
        }

        for (int i = 0; i < paths.size(); i += this.batchSize) {
            if (isCancelled()) break;
            int end = Math.min(i + this.batchSize, paths.size());
            List<TreePath> batch = paths.subList(i, end);
            this.publish(batch);
            Thread.sleep(this.sleepTime);
        }

        return null;
    }

    @Override
    protected void process(List<List<TreePath>> chunks) {
        if (isCancelled()) {
            return;
        }
        for (List<TreePath> batch : chunks) {
            for (TreePath path : batch) {
                this.applyState.accept(path);
            }
        }
    }

    private void collectPaths(TreePath parentPath, List<TreePath> collector) {
        TreeNode node = (TreeNode) parentPath.getLastPathComponent();
        if (this.reverseCollect && !this.shouldCollect.test(parentPath)) {
            return;
        }
        if (!this.reverseCollect) {
            if (this.shouldCollect.test(parentPath)) {
                collector.add(parentPath);
            }
        }
        for (int i = 0; i < node.getChildCount(); i++) {
            TreeNode child = node.getChildAt(i);
            if (child.getChildCount() > 0) {
                this.collectPaths(parentPath.pathByAddingChild(child), collector);
            }
        }
        if (this.reverseCollect) {
            collector.add(parentPath);
        }
    }

    public static AsyncTreeManager expand(TreePath[] paths, JTree tree) {
        AsyncTreeManager manager = new AsyncTreeManager(paths, false, tree::expandPath, tree::isCollapsed);
        manager.execute();
        return manager;
    }

    public static AsyncTreeManager collapse(TreePath[] paths, JTree tree) {
        AsyncTreeManager manager = new AsyncTreeManager(paths, true, tree::collapsePath, tree::isExpanded);
        manager.execute();
        return manager;
    }

    public static AsyncTreeManager custom(TreePath[] paths, boolean reverseCollect, Consumer<TreePath> applyState, Predicate<TreePath> shouldCollect) {
        AsyncTreeManager manager = new AsyncTreeManager(paths, reverseCollect, applyState, shouldCollect);
        manager.execute();
        return manager;
    }
}
