package com.jonex.zookeeper.curator.transaction;

import com.jonex.zookeeper.curator.ZkClient;
import org.apache.curator.framework.api.transaction.CuratorTransaction;
import org.apache.curator.framework.api.transaction.CuratorTransactionFinal;
import org.apache.curator.framework.api.transaction.CuratorTransactionResult;
import org.apache.zookeeper.CreateMode;

import java.util.Collection;

/**
 * <pre>
 *
 *  File: TransactionOperation.java
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
public class TransactionOperation {

    public void testTransaction()throws Exception{
        ZkClient.getClient().inTransaction().check().forPath("path")
                .and()
                .create().withMode(CreateMode.EPHEMERAL).forPath("path","data".getBytes())
                .and()
                .setData().withVersion(10086).forPath("path","data2".getBytes())
                .and()
                .commit();

    }

    public CuratorTransaction beginTransaction()throws Exception{
        CuratorTransaction transaction = ZkClient.getClient().inTransaction();
        return transaction;
    }

    public CuratorTransactionFinal createWithTransaction(CuratorTransaction transaction, String path)throws Exception{
        return transaction.create().withMode(CreateMode.EPHEMERAL_SEQUENTIAL).forPath(path).and();
    }

    public CuratorTransactionFinal deleteWithTransaction(CuratorTransaction transaction, String path)throws Exception{
        return null;
    }

    public CuratorTransactionFinal setDataWithTransaction(CuratorTransaction transaction, String path)throws Exception{
        return null;
    }

    public CuratorTransactionFinal checkWithTransaction(CuratorTransaction transaction, String path)throws Exception{
        return null;
    }

    public Collection<CuratorTransactionResult> commitTransaction(CuratorTransactionFinal transactionFinal)throws Exception{
        return transactionFinal.commit();
    }

}
