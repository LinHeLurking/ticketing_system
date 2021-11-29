package ticketingsystem;

public class SegmentTree {
    private final int treeLeft, treeRight;
    private final TreeNodeBase[] treeNode;
    public static final int MIN = 0, SUM = 1;
    private final int combineMode;

    SegmentTree(int treeLeft, int treeRight, int initVal, int combineMode) {
        this.treeLeft = treeLeft;
        this.treeRight = treeRight;
        this.treeNode = new TreeNodeBase[4 * (treeRight - treeLeft + 1) + 5];
        this.combineMode = combineMode;
        for (int i = 0; i < 4 * (treeRight - treeLeft + 1) + 5; ++i) {
            treeNode[i] = new TreeNodeBase(initVal, 0);
        }
    }

    private void pushDown(int root) {
        if (treeNode[root].lazyUpdate != 0) {
            int offset = treeNode[root].lazyUpdate;
            treeNode[root].lazyUpdate = 0;
            treeNode[root << 1].lazyUpdate += offset;
            treeNode[root << 1 | 1].lazyUpdate += offset;
            int treeMid = (treeLeft + treeRight) >> 1;
            if (combineMode == MIN) {
                treeNode[root << 1].value += offset;
                treeNode[root << 1 | 1].value += offset;
            } else if (combineMode == SUM) {
                treeNode[root << 1].value += offset * (treeMid - treeLeft + 1);
                treeNode[root << 1 | 1].value += offset * (treeRight - treeMid);
            }
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
            if (combineMode == MIN) {
                return Math.min(l, r);
            } else if (combineMode == SUM) {
                return l + r;
            } else {
                return -1; // Impossible
            }
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
            int ret;
            if (combineMode == MIN) {
                treeNode[root].value = Math.min(treeNode[root << 1].value, treeNode[root << 1 | 1].value);
            } else if (combineMode == SUM) {
                treeNode[root].value = treeNode[root << 1].value + treeNode[root << 1 | 1].value;
            }
            ret = treeNode[root].value;
            return ret;
        }
    }

    
    public int boundedUpdate(int segLeft, int segRight, int offset) {
        int val = update(1, treeLeft, treeRight, segLeft, segRight, offset);
        if (val < 0) {
            update(1, treeLeft, treeRight, segLeft, segRight, -offset);
        }
        return val;
    }
}

