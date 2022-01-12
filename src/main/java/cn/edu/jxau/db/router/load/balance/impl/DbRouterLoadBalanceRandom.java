package cn.edu.jxau.db.router.load.balance.impl;

import cn.edu.jxau.db.router.DbContextHolder;
import cn.edu.jxau.db.router.DbRouterConfig;
import cn.edu.jxau.db.router.load.balance.IDbRouterLoadBalance;

import javax.annotation.Resource;
import java.security.SecureRandom;

/**
 * @description:
 * @author：gezellighied.h
 * @date: 2022/1/13
 */
public class DbRouterLoadBalanceRandom implements IDbRouterLoadBalance {

    @Resource
    private DbRouterConfig dbRouterConfig;

    @Override
    public void doLoadBalance() {
        SecureRandom secureRandom = new SecureRandom();
        int random = secureRandom.nextInt(dbRouterConfig.getMasterToSlaveCount().get(DbContextHolder.getDBKey()));

        String dbKey = DbContextHolder.getDBKey();
        dbKey = dbKey.replace(DbContextHolder.DB_PREFIX, "");
        dbKey = DbContextHolder.DB_PREFIX + dbKey + DbContextHolder.READ_DB_PREFIX + String.format("%2d", random);
        DbContextHolder.setDBKey(dbKey);
    }
}
