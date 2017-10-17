package com.jonex.zookeeper.lock;

/**
 * @Author jonex [r13ljj@gmail.com]
 * @Date 2017/10/16 17:30
 */
public interface DistributedLocks {

    public final static String ZK_LOCK_ROOT = "/distributed_locks";

    /**
     * 独占锁
     *
     * @return
     */
    boolean getExclusiveLock();

    boolean getExclusiveLock(long waitTime);

    void releaseExclusiveLock();

    /**
     * 共享锁
     *
     * @return
     */
    boolean getSharedLock();

    boolean getSharedLock(long waitTime);

}
