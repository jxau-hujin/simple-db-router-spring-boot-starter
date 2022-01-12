package cn.edu.jxau.db.router.strategy.impl;

import cn.edu.jxau.db.router.DbContextHolder;
import cn.edu.jxau.db.router.strategy.AbstractDbRouterStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @description:
 * @author：gezellighied.h
 * @date: 2022/1/12
 */
public class DbRouterStrategyHash extends AbstractDbRouterStrategy {

    private Logger logger = LoggerFactory.getLogger(DbRouterStrategyHash.class);

    @Override
    public void doRouter(String val) {
        // 按照分库分表字段的值哈希取模
        int size = getDbCount() * getTbCount();

        int idx = (size - 1) & (val.hashCode() ^ val.hashCode() >>> 16);

        int dbIdx = idx / getTbCount() + 1;
        int tbIdx = idx - getTbCount() * (dbIdx - 1);

        DbContextHolder.setDBKey(DbContextHolder.DB_PREFIX + String.format("%02d", dbIdx));
        DbContextHolder.setTBKey(String.format("%02d", tbIdx));
        logger.info("数据库路由 dbIdx：{} tbIdx：{} val: {}",  dbIdx, tbIdx, val);
    }
}
