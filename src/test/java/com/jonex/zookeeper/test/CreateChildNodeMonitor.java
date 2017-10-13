package com.jonex.zookeeper.test;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.List;

/**
 * @Author jonex [r13ljj@gmail.com]
 * @Date 2017/10/13 11:43
 */
public class CreateChildNodeMonitor {

    private final static String root = "/zookeeper-study";

    public static void main(String[] args) throws Exception{
        ZooKeeper zooKeeper = new ZooKeeper("localhost:2181", 3000, new ChildNodeCreateWatcher());
        Stat context = new Stat();
        zooKeeper.getChildren(root, new ChildNodeChangeWatcher(), new ChildNodeCallback(), context);
        log("=========getChildrend context:"+context);
        zooKeeper.create(root, "child".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
        Thread.sleep(2000);
        zooKeeper.create(root, "child".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
        Thread.sleep(2000);
        zooKeeper.create(root, "child".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
        Thread.sleep(2000);
    }

    private static void log(Object msg){
        System.out.print("CreateChildNodeMonitor log: "+msg);
    }

    static class ChildNodeCreateWatcher implements Watcher{
        public void process(WatchedEvent watchedEvent) {
            log("create Watcher watchedEvent="+watchedEvent);
            log("create Watcher path="+watchedEvent.getPath());
            log("create Watcher type="+watchedEvent.getType());
            log("create Watcher state="+watchedEvent.getState());
        }
    }

    static class ChildNodeChangeWatcher implements Watcher {
        public void process(WatchedEvent watchedEvent) {
            log("child Watcher watchedEvent="+watchedEvent);
            if (watchedEvent.getType() == Event.EventType.NodeChildrenChanged) {
                log("child ["+watchedEvent.getPath()+"] changed");
            }
        }
    }

    static class ChildNodeCallback implements AsyncCallback.ChildrenCallback {
        public void processResult(int rc, String path, Object ctx, List<String> children) {
            log("path="+path);
            log("children="+children);
        }
    }

}
