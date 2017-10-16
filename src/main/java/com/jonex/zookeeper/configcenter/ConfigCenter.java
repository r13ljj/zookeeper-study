package com.jonex.zookeeper.configcenter;

/**
 * @Author jonex [r13ljj@gmail.com]
 * @Date 2017/10/16 17:36
 */
public interface ConfigCenter {

    public final static String CONFIG_CENTER_ZK_ROOT = "/configs";

    void init();

    void addConfig(String key, String config);

    void updateConfig(String key, String config);

    void delConfig();

    void getConfig(String key);

    void getAllConfig();

}
