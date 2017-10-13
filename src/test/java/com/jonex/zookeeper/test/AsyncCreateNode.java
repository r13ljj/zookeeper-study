package com.jonex.zookeeper.test;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;

/**
 * @Author jonex [r13ljj@gmail.com]
 * @Date 2017/10/13 11:00
 */
public class AsyncCreateNode {

    private final static String root = "/zookeeper-study";

    public static void main(String[] args) throws IOException {
        ZooKeeper zooKeeper = new ZooKeeper("localhost:2181", 3000, new AsyncCreateWatcher());
        Stat context = new Stat();
        String path = "asyncCreateNode";
        zooKeeper.create(root,
                path.getBytes(),
                ZooDefs.Ids.OPEN_ACL_UNSAFE,
                CreateMode.EPHEMERAL,
                new AsyncCreateCallback(), context);
        log("=============create node context:"+context);
        zooKeeper.getData(path, true, new AsyncCreateDataCallback(), context);
        log("=============get data context:"+context);
    }


    private static void log(Object msg){
        System.out.print("AsyncCreateNode log: "+msg);
    }

    static class AsyncCreateWatcher implements Watcher {
        public void process(WatchedEvent watchedEvent) {
            log(watchedEvent);
            log("Watcher path="+watchedEvent.getPath());
            log("Watcher type="+watchedEvent.getType());
            log("Watcher state="+watchedEvent.getState());

        }
    }

    static class AsyncCreateCallback implements AsyncCallback.StringCallback{
        public void processResult(int rc, String path, Object ctx, String name) {
            log("StringCallback rc="+rc);
            log("StringCallback path="+path);
            log("StringCallback ctx="+ctx);
            log("StringCallback name="+name);
            KeeperException.Code code = KeeperException.Code.get(rc);
            switch (code) {
                case OK:
                    log("OK="+code);
                    break;
                case NODEEXISTS:
                    log("NODEEXISTS="+code);
                    break;
                case SESSIONEXPIRED:
                    log("SESSIONEXPIRED="+code);
                    break;
                default:
                    log("unknow " + code);
            }
        }
    }

    static class AsyncCreateDataCallback implements AsyncCallback.DataCallback{
        public void processResult(int rc, String path, Object ctx, byte[] bytes, Stat stat) {
            log("DataCallback rc="+rc);
            log("DataCallback path="+path);
            log("DataCallback ctx="+ctx);
            log("DataCallback stat="+stat);
            KeeperException.Code code = KeeperException.Code.get(rc);
            log("code for check " + code);
            switch (code) {
                case OK:
                    break;
                case NONODE:
                    break;
                case NODEEXISTS:
                    break;
                case SESSIONEXPIRED:
                    break;
                default:
            }
        }
    }

}
