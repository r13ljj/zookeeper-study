package com.jonex.zookeeper.curator.watch;

import com.jonex.zookeeper.curator.ZkClient;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.*;

/**
 * <pre>
 *
 *  File: WatchOperation.java
 *
 *  Copyright (c) 2018, globalegrow.com All Rights Reserved.
 *
 *  Description:
 *  TODO
 *
 *  Revision History
 *  Date,					Who,					What;
 *  2018/4/12				lijunjun				Initial.
 *
 * </pre>
 */
public class WatchOperation {

    public void pathChildrenWatch(String path, boolean cacheData, PathChildrenCacheListener childrenCacheListener)throws Exception{
        PathChildrenCache childrenCache = new PathChildrenCache(ZkClient.getClient(), path, cacheData);
        childrenCache.getListenable().addListener(childrenCacheListener);
    }

    public void clearPathChildWatch(String path, boolean cacheData){
        PathChildrenCache childrenCache = new PathChildrenCache(ZkClient.getClient(), path, cacheData);
        childrenCache.getListenable().clear();
    }

    public void nodeWatch(String path, boolean dataIsCompressed, NodeCacheListener nodeCacheListener)throws Exception{
        NodeCache nodeCache = new NodeCache(ZkClient.getClient(), path, dataIsCompressed);
        nodeCache.getListenable().addListener(nodeCacheListener);
    }

    public void treeWatch(String path, TreeCacheListener treeCacheListener){
        TreeCache treeCache = new TreeCache(ZkClient.getClient(), path);
        treeCache.getListenable().addListener(treeCacheListener);
    }


    public static void main(String[] args) throws Exception{
        WatchOperation operation = new WatchOperation();
        operation.pathChildrenWatch("/root", true, new PathChildrenCacheListener() {
            @Override
            public void childEvent(CuratorFramework curatorFramework, PathChildrenCacheEvent pathChildrenCacheEvent) throws Exception {
                ChildData data = pathChildrenCacheEvent.getData();
                System.out.println("child changed:"+data.getData());
            }
        });
    }


}
