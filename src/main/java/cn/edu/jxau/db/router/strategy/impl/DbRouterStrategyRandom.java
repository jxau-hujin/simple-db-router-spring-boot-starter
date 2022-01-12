package cn.edu.jxau.db.router.strategy.impl;

import cn.edu.jxau.db.router.DbContextHolder;
import cn.edu.jxau.db.router.strategy.AbstractDbRouterStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;

/**
 * @description:
 * @author：gezellighied.h
 * @date: 2022/1/12
 */
public class DbRouterStrategyRandom extends AbstractDbRouterStrategy {

    private Logger logger = LoggerFactory.getLogger(DbRouterStrategyHash.class);

    @Override
    public void doRouter(String val) {
        int dbCount = getDbCount();
        int tbCount = getTbCount();

        SecureRandom secureRandom = new SecureRandom();

        int dbIdx = secureRandom.nextInt(dbCount);
        int tbIdx = secureRandom.nextInt(tbCount);

        DbContextHolder.setDBKey(String.format("%02d", dbIdx));
        DbContextHolder.setTBKey(String.format("%02d", tbIdx));
        logger.info("数据库路由 dbIdx：{} tbIdx：{} val: {}",  dbIdx, tbIdx, val);
    }
}
