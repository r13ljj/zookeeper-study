package com.jonex.zookeeper;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @Author jonex [r13ljj@gmail.com]
 * @Date 2017/10/17 11:17
 */
public class ZookeeperManager {

    private final static Logger LOG = LoggerFactory.getLogger(ZookeeperManager.class);

    private final static String ZK_SERVER = "localhost:2181";
    private final static int SESSION_TIMEOUT = 15000;

    private ZooKeeper zk;


    private ZookeeperManager(){
        init(ZK_SERVER);
    }

    private static class ZookeeperManagerHolder {
        private final static ZookeeperManager intance = new ZookeeperManager();
    }

    public static ZookeeperManager instance(){
        return ZookeeperManagerHolder.intance;
    }

    private void init(String zkUrl){
        if (zk != null && (zk.getState() == ZooKeeper.States.CONNECTED || zk.getState() == ZooKeeper.States.CONNECTING)) {
            LOG.warn("zookeeper:{} is connected.", zkUrl);
            return;
        }
        try {
            zk = new ZooKeeper(zkUrl, SESSION_TIMEOUT, new Watcher() {
                public void process(WatchedEvent watchedEvent) {
                    LOG.info("zookeeper client connecting watchedEvent:{}", watchedEvent);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ZooKeeper getZkClient(){
        return zk;
    }

}
