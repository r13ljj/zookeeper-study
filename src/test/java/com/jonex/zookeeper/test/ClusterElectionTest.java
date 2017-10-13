package com.jonex.zookeeper.test;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

/**
 * @Author jonex [r13ljj@gmail.com]
 * @Date 2017/10/13 16:07
 */
public class ClusterElectionTest {

    private ZooKeeper zooKeeper;

    private Object mutex = new Object();

    public ClusterElectionTest(ZooKeeper zooKeeper) {
        this.zooKeeper = zooKeeper;
    }

    public void findLeader() throws InterruptedException{
        byte[] leader = null;
        try {
            leader = zooKeeper.getData("/leader", true, null);
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (leader != null) {
            following();
        } else {
            String newLeader = null;
            try {
                byte[] leaderIp = InetAddress.getLocalHost().getAddress();
                newLeader = zooKeeper.create("/leader", leaderIp, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (newLeader != null) {
                leader();
            } else {
                mutex.wait();
            }
        }
    }

    //当前节点注册到主节点
    private void following(){
        String newFollower = null;
        try {
            byte[] currentIp = InetAddress.getLocalHost().getAddress();
            newFollower = zooKeeper.create("/followers/"+InetAddress.getLocalHost().getHostAddress(), currentIp, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
            if (newFollower != null) {
                mutex.notifyAll();
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //所有follower节点注册到主节点
    private void leader(){
        try {
            byte[] leaderIp = InetAddress.getLocalHost().getAddress();
            zooKeeper.setData("/followers", leaderIp, -1);//version 为 -1 怎可以匹配任何版本
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
