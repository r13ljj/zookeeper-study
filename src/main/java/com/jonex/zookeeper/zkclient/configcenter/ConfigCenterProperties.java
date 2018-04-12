package com.jonex.zookeeper.zkclient.configcenter;

import com.jonex.zookeeper.zkclient.ZookeeperManager;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author jonex [r13ljj@gmail.com]
 * @Date 2017/10/18 13:52
 */
public class ConfigCenterProperties implements ConfigCenter {

    private final static Logger LOG = LoggerFactory.getLogger(ConfigCenterProperties.class);

    private volatile Map<String, String> configProperties = new ConcurrentHashMap<String, String>();

    private ConfigCenterWatcher configCenterWatcher;

    public ConfigCenterProperties(){
        configCenterWatcher = new ConfigCenterWatcher(this);
        this.init();
    }


    public void init() {
        ZooKeeper zk = ZookeeperManager.instance().getZkClient();
        try {
            if (zk.exists(CONFIG_CENTER_ZK_ROOT, false) == null) {
                zk.create(CONFIG_CENTER_ZK_ROOT, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
            LOG.info("start to init configProperties");
            configProperties = this.getAllConfig();
            LOG.info("init configProperties over");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addConfig(String key, String config) {
        ZooKeeper zk = ZookeeperManager.instance().getZkClient();
        try {
            String concatKey = this.concatKey(key);
            zk.create(concatKey, config.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            configCenterWatcher.watcher(key);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateConfig(String key, String config) {
        ZooKeeper zk = ZookeeperManager.instance().getZkClient();
        try {
            String concatKey = this.concatKey(key);
            zk.setData(concatKey, config.getBytes(), -1);
            configCenterWatcher.watcher(key);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void delConfig(String key) {
        ZooKeeper zk = ZookeeperManager.instance().getZkClient();
        try {
            String concatKey = this.concatKey(key);
            zk.delete(concatKey, -1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getConfig(String key) {
        ZooKeeper zk = ZookeeperManager.instance().getZkClient();
        String config = null;
        try {
            String concatKey = this.concatKey(key);
            byte[] data = zk.getData(concatKey, false,null);
            config = new String(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return config;
    }

    public Map<String, String> getAllConfig() {
        if (configProperties != null) {
            return configProperties;
        }
        Map<String, String> currentConfigProperties = new ConcurrentHashMap<String, String>();
        ZooKeeper zk = ZookeeperManager.instance().getZkClient();
        try {
            List<String> children = zk.getChildren(CONFIG_CENTER_ZK_ROOT, false);
            for(String key : children){
                byte[] data = zk.getData(this.concatKey(key), false,null);
                configProperties.put(key, new String(data));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return currentConfigProperties;
    }

    private String concatKey(String key){
        return CONFIG_CENTER_ZK_ROOT.concat("/").concat(key);
    }
}
