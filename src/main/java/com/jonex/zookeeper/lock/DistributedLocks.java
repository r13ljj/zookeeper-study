package com.jonex.zookeeper.lock;

/**
 * @Author jonex [r13ljj@gmail.com]
 * @Date 2017/10/16 17:30
 */
public interface DistributedLocks {

    public final static String ZK_ROOT_LOCK = "/distributed_locks";

    /**
     * 独占锁
     *
     * @return
     */
    boolean getExclusiveLock();

    boolean getExclusiveLock(int waitTime);

    /**
     * 共享锁
     *
     * @return
     */
    boolean getSharedLock();

    boolean getSharedLock(int waitTime);

}
