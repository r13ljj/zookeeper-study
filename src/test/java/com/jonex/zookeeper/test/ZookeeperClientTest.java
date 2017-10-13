package com.jonex.zookeeper.test;

import org.apache.zookeeper.*;

/**
 * @Author jonex [r13ljj@gmail.com]
 * @Date 2017/10/13 15:37
 */
public class ZookeeperClientTest {

    private final static String ZK_SERVER = "localhost:2181";
    private final static int ZK_CONNECTION_TIMEOUT =  3000;

    public static void main(String[] args) throws Exception{
        //连接
        ZooKeeper zooKeeper = new ZooKeeper(ZK_SERVER, ZK_CONNECTION_TIMEOUT, new Watcher() {
            public void process(WatchedEvent watchedEvent) {
                System.out.println("connection watch event:"+watchedEvent.getType());
            }
        });
        //创建节点
        zooKeeper.create("/testRootPath", "testRootData".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        //创建子节点
        zooKeeper.create("/testRootPath/testChildPathOne", "testChildDataOne".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        System.out.println(new String(zooKeeper.getData("/testRootPath",false,null)));
        // 取出子目录节点列表
        System.out.println(zooKeeper.getChildren("/testRootPath",true));
        // 修改子目录节点数据
        zooKeeper.setData("/testRootPath/testChildPathOne", "modifyChildDataOne".getBytes(), -1);
        System.out.println("目录节点状态：["+zooKeeper.exists("/testRootPath",true)+"]");
        // 创建另外一个子目录节点
        zooKeeper.create("/testRootPath/testChildPathTwo", "testChildDataTwo".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        System.out.println(new String(zooKeeper.getData("/testRootPath",false,null)));
        // 删除子目录节点
        zooKeeper.delete("/testRootPath/testChildPathOne", -1);
        zooKeeper.delete("/testRootPath/testChildPathTwo", -1);
        // 删除父目录节点
        zooKeeper.delete("testRootPath", -1);
        // 关闭连接
        zooKeeper.close();
    }

}
