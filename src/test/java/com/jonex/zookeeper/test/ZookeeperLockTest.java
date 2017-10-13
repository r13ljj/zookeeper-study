package com.jonex.zookeeper.test;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import java.util.Arrays;
import java.util.List;

/**
 * @Author jonex [r13ljj@gmail.com]
 * @Date 2017/10/13 16:35
 */
public class ZookeeperLockTest {

    private final static String LOCK_ROOT = "/locks";

    private ZooKeeper zooKeeper;

    public ZookeeperLockTest(ZooKeeper zooKeeper) {
        this.zooKeeper = zooKeeper;
    }

    public boolean getLock(){
        try {
            List<String> list = zooKeeper.getChildren(LOCK_ROOT, false);
            String[] nodes = list.toArray(new String[list.size()]);
            Arrays.sort(nodes);
            String myNode = "1";
            if (myNode.equals(nodes[0])) {
                return true;
            }else{
                return waitForLock(nodes[0]);
            }
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean waitForLock(String lower) {
        try {
            Stat stat = zooKeeper.exists(lower, true);
            if (stat != null) {
                return false;
            }else{
                return getLock();
            }
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

}
