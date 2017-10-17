package com.jonex.zookeeper.lock;

import com.jonex.zookeeper.ZookeeperManager;
import org.apache.zookeeper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * --排他锁，简单地说就是多个客户端同时去竞争创建同一个临时子节点，Zookeeper能够保证只有一个客户端创建成功，那么这个创建成功的客户端就获得排他锁。
 * 正常情况下，这个客户端执行完业务逻辑会删除这个节点，也就是释放了锁。如果该客户端宕机了，那么这个临时节点会被自动删除，锁也会被释放
 *
 * --共享锁，涉及到是读操作还是写操作的问题。所有的客户端都会到某个节点，例如：/shared_lock 下创建一个临时顺序节点，
 * 如果是读请求，就会创建诸如 /shared_lock/192.168.0.1-R-0000000001 的节点，如果是写操作，则创建诸如 /shared_lock/192.168.0.1-W-0000000001 的节点。
 * 是否获取到共享锁，从以下四个步骤来判断：
 * 1、创建完节点后，获取/shared_lock节点下的所有子节点，并对该节点注册子节点变更的watcher监听。
 * 2、确定自己的节点序号在所有子节点中的顺序。
 * 3、对于读请求：
 *      如果没有比自己序号小的子节点，或是所有比自己序号小的子节点都是去请求，那么表明自己已经成功获取到了共享锁，同时开始执行读取逻辑。
 *      如果比自己序号小的子节点中有写请求，那么就需要进入等待。
 *    对于写请求：
 *      如果自己不是序号最小的子节点，那么就需要进入等待。
 * 4、接收到Watcher通知后，重复步骤1。
 *
 * @Author jonex [r13ljj@gmail.com]
 * @Date 2017/10/17 11:06
 */
public class DistributedLocksImpl implements DistributedLocks {

    private final static Logger LOG = LoggerFactory.getLogger(DistributedLocksImpl.class);

    private final static String EXCLUSIVE_PATH = "/exclusive_lock";
    private final static String SHARED_PATH = "/shared_lock";

    public DistributedLocksImpl() {
        createLockRoot();
        createSharedLockPath();
    }

    /**
     * 分布式锁根目录
     */
    private void createLockRoot(){
        try {
            ZooKeeper zk = ZookeeperManager.instance().getZkClient();
            if (zk.exists(DistributedLocks.ZK_LOCK_ROOT, null) == null) {
                zk.create(DistributedLocks.ZK_LOCK_ROOT, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 共享锁目录
     */
    private void createSharedLockPath(){
        ZooKeeper zk = ZookeeperManager.instance().getZkClient();
        try {
            if (zk.exists(DistributedLocks.ZK_LOCK_ROOT+SHARED_PATH, null) == null) {
                zk.create(ZK_LOCK_ROOT+SHARED_PATH, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //no block
    public boolean getExclusiveLock() {
        ZooKeeper zk = ZookeeperManager.instance().getZkClient();
        try {
            if (zk.exists(ZK_LOCK_ROOT+EXCLUSIVE_PATH, null) != null) {
                return false;
            }
            zk.create(ZK_LOCK_ROOT+EXCLUSIVE_PATH, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean getExclusiveLock(long waitTime) {
        ZooKeeper zk = ZookeeperManager.instance().getZkClient();
        try {
            if (zk.exists(ZK_LOCK_ROOT+EXCLUSIVE_PATH, null) == null) {
                zk.create(ZK_LOCK_ROOT+EXCLUSIVE_PATH, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
                return true;
            }
            long startTime = System.currentTimeMillis();
            while(System.currentTimeMillis()-startTime < waitTime){
                //保证有三次重试；降低cpu空转
                Thread.sleep(waitTime/3);
                if (zk.exists(ZK_LOCK_ROOT+EXCLUSIVE_PATH, null) == null) {
                    zk.create(ZK_LOCK_ROOT+EXCLUSIVE_PATH, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public void releaseExclusiveLock() {
        ZooKeeper zk = ZookeeperManager.instance().getZkClient();
        try {
            if (zk.exists(ZK_LOCK_ROOT+EXCLUSIVE_PATH, null) == null) {
                //version=-1 接收任何版本数据
                zk.delete(ZK_LOCK_ROOT + EXCLUSIVE_PATH, -1, new AsyncCallback.VoidCallback() {
                    public void processResult(int rc, String path, Object ctx) {
                        LOG.info("relase exclusive lock, delete path:{}", path);
                    }
                }, null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean getSharedLock() {
        ZooKeeper zk = ZookeeperManager.instance().getZkClient();
        try{
            String current = zk.create(ZK_LOCK_ROOT+SHARED_PATH+"/lock_", new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
            List<String> nodes = zk.getChildren(ZK_LOCK_ROOT+SHARED_PATH, false);
            int[] locks = new int[nodes.size()];
            for (int i=0; i<nodes.size(); i++) {
                locks[i] = Integer.valueOf(nodes.get(i).substring(nodes.get(i).lastIndexOf("_")+1));
            }
            Arrays.sort(locks);
            if (Integer.valueOf(current.substring(current.lastIndexOf("_")+1)) == locks[0]) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean getSharedLock(long waitTime) {
        return false;
    }
}
