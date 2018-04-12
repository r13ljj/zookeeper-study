package com.jonex.zookeeper.zkclient.watch;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @Author jonex [r13ljj@gmail.com]
 * @Date 2017/10/11 17:20
 */
public class Executor implements Watcher, Runnable, DataMonitor.DataMonitorListener {

    String filename;
    String[] exec;

    ZooKeeper zk;
    DataMonitor dataMonitor;

    Process child;

    public Executor(String hostPort, String znode, String filename, String[] exec)throws IOException{
        zk = new ZooKeeper(hostPort, 3000, this);
        dataMonitor = new DataMonitor(zk, znode, null, this);
        this.filename = filename;
        this.exec = exec;
    }

    /**
     * implement DataMonitorListener
     *
     * @param data
     */
    public void exist(byte[] data) {
        if (data == null) {
            if (child != null) {
                System.out.println("Killing child");
                child.destroy();
                try {
                    child.waitFor();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            child = null;
        } else {
            if (child != null) {
                System.out.println("stopping child");
                child.destroy();
                try {
                    child.waitFor();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            try {
                FileOutputStream fos = new FileOutputStream(filename);
                fos.write(data);
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                System.out.println("starting child");
                child = Runtime.getRuntime().exec(exec);
                new StreamWriter(child.getInputStream(), System.out).start();
                new StreamWriter(child.getErrorStream(), System.out).start();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    /**
     * implement DataMonitorListener
     *
     * @param reasonCode
     */
    public void closing(int reasonCode) {
        synchronized (this) {
            notifyAll();
        }
    }

    /**
     * implement Runnable
     *
     */
    public void run() {
        try {
            synchronized (this) {
                while (!dataMonitor.dead) {
                    wait();
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * implement Watcher
     *
     * @param watchedEvent
     */
    public void process(WatchedEvent watchedEvent) {
        dataMonitor.process(watchedEvent);
    }

    public static void main(String[] args) {
        if (args.length < 4) {
            System.err.println("USAGE: Executor hostPort znode filename program [args ...]");
            System.exit(2);
        }
        String hostPort = args[0];
        String znode = args[1];
        String filename = args[2];
        String exec[] = new String[args.length - 3];
        System.arraycopy(args, 3, exec, 0, exec.length);
        try {
            new Executor(hostPort, znode, filename, exec).run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    static class StreamWriter extends Thread{
        InputStream is;
        OutputStream os;

        public StreamWriter(InputStream is, OutputStream os) {
            this.is = is;
            this.os = os;
        }

        @Override
        public void run() {
            byte[] buffer = new byte[80];
            int readBytes = 0;
            try {
                while((readBytes = is.read(buffer)) > 0){
                    os.write(buffer, 0, readBytes);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
