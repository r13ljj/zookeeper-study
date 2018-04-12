package com.jonex.zookeeper.curator.node;

import com.jonex.zookeeper.curator.ZkClient;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;

import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * <pre>
 *
 *  File: NodeOperation.java
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
public class NodeOperation {

    public void createNode(String path, String data)throws Exception{
        //ZkClient.getClient().create().forPath(path, data.getBytes("UTF-8"));
        //ZkClient.getClient().create().withMode(CreateMode.EPHEMERAL_SEQUENTIAL).forPath(path, data.getBytes("UTF-8"));
        ZkClient.getClient().create()
                .creatingParentContainersIfNeeded() //递归创建所需父节点
                .withMode(CreateMode.EPHEMERAL_SEQUENTIAL).forPath(path, data.getBytes("UTF-8"));
    }

    public void asyncCreateNode(String path, String data)throws Exception{
        Executor executor = Executors.newFixedThreadPool(2);
        ZkClient.getClient().create()
                .creatingParentsIfNeeded()
                .withMode(CreateMode.EPHEMERAL)
                .inBackground((curatorFramework, curatorEvent) -> {System.out.println(String.format("eventType:%s,resultCode:%s",curatorEvent.getType(),curatorEvent.getResultCode()));
                },executor)
                .forPath(path, data.getBytes());
    }

    public void deleteNode(String path)throws Exception{
        //ZkClient.getClient().delete().forPath(path);
        //删除子节点
        //ZkClient.getClient().delete().deletingChildrenIfNeeded().forPath(path);
        //指定版本
        //ZkClient.getClient().delete().withVersion(1001).forPath(path);
        //强制保证删除
        ZkClient.getClient().delete().guaranteed().forPath(path);

    }

    public Object getNodeData(String path)throws Exception{
        //byte[] data = ZkClient.getClient().getData().forPath(path);
        //读取到stat对象
        Stat stat = new Stat();
        ZkClient.getClient().getData().storingStatIn(stat).forPath("path");
        OutputStream ops = null;//TODO
        DataOutput output = new DataOutputStream(ops);
        stat.write(output);
        return null;
    }

    public void updateData(String path, String data)throws Exception{
        //ZkClient.getClient().setData().forPath(path, data.getBytes());
        ZkClient.getClient().setData().withVersion(1001).forPath(path, data.getBytes());
    }

    public void existNode(String path)throws Exception{
        ZkClient.getClient().checkExists().forPath(path);
    }

    public void getChildren(String path)throws Exception{
        List<String> children = ZkClient.getClient().getChildren().forPath(path);
    }

}
