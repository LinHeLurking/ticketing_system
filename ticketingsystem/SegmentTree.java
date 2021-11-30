package ticketingsystem;

import java.util.ArrayList;

public class SegmentTree {
    private final int treeLeft, treeRight;
    private final TreeNode[] treeNode;

    SegmentTree(int treeLeft, int treeRight, int initVal) {
        this.treeLeft = treeLeft;
        this.treeRight = treeRight;
        this.treeNode = new TreeNode[4 * (treeRight - treeLeft + 1) + 5];
        for (int i = 0; i < 4 * (treeRight - treeLeft + 1) + 5; ++i) {
            treeNode[i] = new TreeNode(initVal, 0);
        }
    }

    private void pushDown(int root) {
        if (treeNode[root].lazyUpdate != 0) {
            int offset = treeNode[root].lazyUpdate;
            treeNode[root << 1].lazyUpdate += offset;
            treeNode[root << 1 | 1].lazyUpdate += offset;
            treeNode[root << 1].value += offset;
            treeNode[root << 1 | 1].value += offset;
            treeNode[root].lazyUpdate = 0;
        }
    }

    public int[] splitQuery(int segLeft, int segRight) {
        ArrayList<Integer> result = new ArrayList<>();
        splitQuery(1, treeLeft, treeRight, segLeft, segRight, result);
        int[] ret = new int[result.size()];
        for (int i = 0; i < result.size(); ++i) {
            ret[i] = result.get(i);
        }
        return ret;
    }

    private void splitQuery(int root, int nodeLeft, int nodeRight, int segLeft, int segRight, ArrayList<Integer> result) {
        if (segLeft > segRight || segLeft > nodeRight || segRight < nodeLeft) {
            return;
        } else if (segLeft == nodeLeft && segRight == nodeRight && nodeRight == nodeLeft) {
            result.add(treeNode[root].value);
        } else {
            pushDown(root);
            int nodeMid = (nodeLeft + nodeRight) >> 1;
            splitQuery(root << 1, nodeLeft, nodeMid, segLeft, Math.min(segRight, nodeMid), result);
            splitQuery(root << 1 | 1, nodeMid + 1, nodeRight, Math.max(segLeft, nodeMid + 1), segRight, result);
        }
    }

    public int query(int segLeft, int segRight) {
        return query(1, treeLeft, treeRight, segLeft, segRight);
    }

    private int query(int root, int nodeLeft, int nodeRight, int segLeft, int segRight) {
        if (segLeft > segRight || segLeft > nodeRight || segRight < nodeLeft) {
            return Integer.MAX_VALUE;
        } else if (segLeft == nodeLeft && segRight == nodeRight) {
            return treeNode[root].value;
        } else {
            pushDown(root);
            int nodeMid = (nodeLeft + nodeRight) >> 1;
            int l = query(root << 1, nodeLeft, nodeMid, segLeft, Math.min(segRight, nodeMid));
            int r = query(root << 1 | 1, nodeMid + 1, nodeRight, Math.max(segLeft, nodeMid + 1), segRight);
            return Math.min(l, r);
        }
    }


    public int update(int segLeft, int segRight, int offset) {
        return update(1, treeLeft, treeRight, segLeft, segRight, offset);
    }

    private int update(int root, int nodeLeft, int nodeRight, int segLeft, int segRight, int offset) {
        if (segLeft > segRight || segLeft > nodeRight || segRight < nodeLeft) {
            return Integer.MAX_VALUE;
        } else if (segLeft == nodeLeft && segRight == nodeRight) {
            treeNode[root].lazyUpdate += offset;
            treeNode[root].value += offset;
            return treeNode[root].value;
        } else {
            pushDown(root);
            int nodeMid = (nodeLeft + nodeRight) >> 1;
            update(root << 1, nodeLeft, nodeMid, segLeft, Math.min(segRight, nodeMid), offset);
            update(root << 1 | 1, nodeMid + 1, nodeRight, Math.max(segLeft, nodeMid + 1), segRight, offset);
            treeNode[root].value = Math.min(treeNode[root << 1].value, treeNode[root << 1 | 1].value);
            return treeNode[root].value;
        }
    }
}

