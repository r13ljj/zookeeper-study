package com.jonex.zookeeper.curator;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <pre>
 *
 *  File: ZkClient.java
 *
 *  Copyright (c) 2018, globalegrow.com All Rights Reserved.
 *
 *  Description:
 *  TODO
 *
 *  Revision History
 *  Date,					Who,					What;
 *  2018/4/11				lijunjun				Initial.
 *
 * </pre>
 */
public class ZkClient {

    private final static Logger LOG = LoggerFactory.getLogger(ZkClient.class);

    private static final String ZK_HOST = "";


    private CuratorFramework client;

    private static class ZkClientHolder{
        static ZkClient INSTANCE = new ZkClient();
    }

    private ZkClient() {
        start();
    }

    public static CuratorFramework getClient(){
        return ZkClientHolder.INSTANCE.client;
    }

    private void start(){
        //retry policy
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(3000, 3);
        //fluent style
        client = CuratorFrameworkFactory.builder()
                .connectString(ZK_HOST)
                .connectionTimeoutMs(3000)
                .sessionTimeoutMs(15000)
                //.namespace("/namespace")
                .retryPolicy(retryPolicy)
                .build();
        client.getConnectionStateListenable().addListener(new ConnectionStateListener() {
            public void stateChanged(CuratorFramework curatorFramework, ConnectionState connectionState) {
                LOG.info("connect to zk:{} state:{}", ZK_HOST, connectionState.isConnected());
            }
        });
        client.start();
    }





}
