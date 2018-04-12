package com.jonex.zookeeper.curator.election;

import com.jonex.zookeeper.curator.ZkClient;
import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.apache.curator.framework.recipes.leader.LeaderLatchListener;
import org.apache.curator.framework.recipes.leader.Participant;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * <pre>
 *
 *  File: LeaderElection.java
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
public class LeaderElection {

    /**
     * LeaderSelector:  所有存活的客户端不间断的轮流做Leader
     * LeaderLatch: 一旦选举出Leader，除非有客户端挂掉重新触发选举，否则不会交出领导权
     *
     * 异常处理： LeaderLatch实例可以增加ConnectionStateListener来监听网络连接问题。
     * 当 SUSPENDED 或 LOST 时, leader不再认为自己还是leader。
     * 当LOST后连接重连后RECONNECTED,LeaderLatch会删除先前的ZNode然后重新创建一个。
     * LeaderLatch用户必须考虑导致leadership丢失的连接问题。
     * 强烈推荐你使用ConnectionStateListener。
     *
     * 重要: 推荐处理方式是当收到SUSPENDED 或 LOST时抛出CancelLeadershipException异常.。
     * 这会导致LeaderSelector实例中断并取消执行takeLeadership方法的异常.。
     * 这非常重要， 你必须考虑扩展LeaderSelectorListenerAdapter.
     * LeaderSelectorListenerAdapter提供了推荐的处理逻辑。
     *
     */
    private LeaderLatch leaderLatch;

    public LeaderElection() {
        init();
    }

    private void init(){
        final String ParticipantId = UUID.randomUUID().toString().replaceAll("-", "");
        this.leaderLatch = new LeaderLatch(ZkClient.getClient(), "/leader", ParticipantId);
        this.leaderLatch.addListener(new LeaderLatchListener() {
            @Override
            public void isLeader() {
                System.out.println(ParticipantId+" is leader.");
            }

            @Override
            public void notLeader() {
                System.out.println(ParticipantId+" not leader");
            }
        });
    }

    public void doLeaderSelect(){
        try {

            leaderLatch.start();

            leaderLatch.await(2, TimeUnit.SECONDS);

            if (leaderLatch.hasLeadership()) {
                System.out.println(leaderLatch.getId() + " hasLeadership=true");
            } else {
                System.out.println(leaderLatch.getId() + " hasLeadership=false");
            }

        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    public void quitSelect(){
        try {
            Participant participant = leaderLatch.getLeader();
            if (participant != null)
                leaderLatch.close(LeaderLatch.CloseMode.NOTIFY_LEADER);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



}
