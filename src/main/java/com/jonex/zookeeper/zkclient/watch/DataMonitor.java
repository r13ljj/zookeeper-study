package com.jonex.zookeeper.zkclient.watch;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.util.Arrays;

/**
 * @Author jonex [r13ljj@gmail.com]
 * @Date 2017/10/11 17:17
 */
public class DataMonitor implements Watcher, AsyncCallback.StatCallback{

    ZooKeeper zk;
    String znode;
    Watcher chainedWatcher;
    DataMonitorListener listener;

    boolean dead;
    byte[] prevData;


    public DataMonitor(ZooKeeper zk, String znode, Watcher chainedWatcher, DataMonitorListener listener){
        this.zk = zk;
        this.znode = znode;
        this.chainedWatcher = chainedWatcher;
        this.listener = listener;
        //检查节点是否存在
        zk.exists(znode, true, this, null);
    }


    /**
     * implements StatCallback
     *
     * @param reasonCode
     * @param path
     * @param ctx
     * @param stat
     */
    public void processResult(int reasonCode, String path, Object ctx, Stat stat) {
        boolean exists;
        switch (reasonCode) {
            case KeeperException.Code.Ok:
                exists = true;
                break;
            case KeeperException.Code.NoNode:
                exists = false;
                break;
            case KeeperException.Code.SessionExpired:
            case KeeperException.Code.NoAuth:
                dead = true;
                listener.closing(reasonCode);
                return;
            default:
                // Retry errors
                zk.exists(znode, true, this, null);
                return;
        }

        byte b[] = null;
        if (exists) {
            try {
                b = zk.getData(znode, false, null);
            } catch (KeeperException e) {
                // We don't need to worry about recovering now. The watch
                // callbacks will kick off any exception handling
                e.printStackTrace();
            } catch (InterruptedException e) {
                return;
            }
        }
        if ((b == null && b != prevData)
                || (b != null && !Arrays.equals(prevData, b))) {
            listener.exist(b);
            prevData = b;
        }
    }

    /**
     * implements Watcher
     *
     * @param watchedEvent
     */
    public void process(WatchedEvent watchedEvent) {
        String path = watchedEvent.getPath();
        if (watchedEvent.getType() == Event.EventType.None) {
            switch (watchedEvent.getState()) {
                case SyncConnected:
                    break;
                case Expired:
                    dead = true;
                    listener.closing(KeeperException.Code.SESSIONEXPIRED.intValue());
                    break;
            }
        } else {
            if (path != null && path.equals(znode)) {
                zk.exists(path, true, this, null);
            }
        }
        if (chainedWatcher != null) {
            chainedWatcher.process(watchedEvent);
        }
    }

    public interface  DataMonitorListener {

        void exist(byte[] data);

        void closing(int reasonCode);
    }

}
