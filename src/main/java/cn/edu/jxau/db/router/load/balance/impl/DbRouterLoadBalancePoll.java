package cn.edu.jxau.db.router.load.balance.impl;

import cn.edu.jxau.db.router.DbContextHolder;
import cn.edu.jxau.db.router.DbRouterConfig;
import cn.edu.jxau.db.router.load.balance.IDbRouterLoadBalance;

import javax.annotation.Resource;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @description:
 * @author：gezellighied.h
 * @date: 2022/1/13
 */
public class DbRouterLoadBalancePoll implements IDbRouterLoadBalance {

    @Resource
    private DbRouterConfig dbRouterConfig;

    private static AtomicInteger atomicInteger = new AtomicInteger(0);

    @Override
    public void doLoadBalance() {
        String dbKey = DbContextHolder.getDBKey();
        dbKey = dbKey.replace(DbContextHolder.DB_PREFIX, "");
        dbKey = DbContextHolder.DB_PREFIX + dbKey + DbContextHolder.READ_DB_PREFIX + String.format("%02d", Math.abs((atomicInteger.getAndAdd(1) % dbRouterConfig.getMasterToSlaveCount().get(DbContextHolder.getDBKey()))));
        DbContextHolder.setDBKey(dbKey);
    }
}
