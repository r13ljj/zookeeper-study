package com.jonex.zookeeper.configcenter;

import com.jonex.zookeeper.ZookeeperManager;
import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.IZkDataListener;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @Author jonex [r13ljj@gmail.com]
 * @Date 2017/10/17 15:32
 */
public class ConfigCenterWatcher {

    private final static Logger LOG = LoggerFactory.getLogger(ConfigCenterWatcher.class);

    private ConfigCenter configCenter;
    private ConfigCenterListener configCenterListener;


    public ConfigCenterWatcher(ConfigCenter configCenter) {
        this.configCenter = configCenter;
        this.initConfigCenterListener();
    }

    private void initConfigCenterListener(){
        configCenterListener = new ConfigCenterListener();
    }

    public void watcher(){
        ZooKeeper zk = ZookeeperManager.instance().getZkClient();

    }

    private class ConfigCenterListener implements IZkDataListener, IZkChildListener {

        public void handleChildChange(String path, List<String> children) throws Exception {
            LOG.info("config {} child change,start reload configProperties", path);
            configCenter.init();
        }

        public void handleDataChange(String path, Object ctx) throws Exception {
            LOG.info("config {} data change,start reload configProperties", path);
            configCenter.init();
        }

        public void handleDataDeleted(String path) throws Exception {
            LOG.info("config {} data delete,start reload configProperties", path);
            configCenter.init();
        }
    }

}
