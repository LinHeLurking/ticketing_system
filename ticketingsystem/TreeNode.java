package ticketingsystem;

import java.util.concurrent.locks.ReentrantLock;

class TreeNodeBase {
    public int value;
    public int lazyUpdate;

    TreeNodeBase(int value, int lazyUpdateInit) {
        this.value = value;
        this.lazyUpdate = lazyUpdateInit;
    }
}

class TreeNodeLocked extends TreeNodeBase {
    // Added to myself but not broadcast to children
    private final ReentrantLock lock;

    public TreeNodeLocked(int value) {
        super(value, 0);
        this.lock = new ReentrantLock();
    }

    public void lock() {
        lock.lock();
    }

    public void unlock() {
        lock.unlock();
    }
}

