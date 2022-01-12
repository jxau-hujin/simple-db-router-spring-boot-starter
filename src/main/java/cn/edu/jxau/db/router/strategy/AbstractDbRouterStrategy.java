package cn.edu.jxau.db.router.strategy;

import cn.edu.jxau.db.router.DbContextHolder;
import cn.edu.jxau.db.router.DbRouterConfig;

import javax.annotation.Resource;

/**
 * @description:
 * @author：gezellighied.h
 * @date: 2022/1/12
 */
public abstract class AbstractDbRouterStrategy implements IDbRouterStrategy {

    @Resource
    protected DbRouterConfig dbRouterConfig;

    @Override
    public void clear() {
        DbContextHolder.clearDBKey();
        DbContextHolder.clearTBKey();
    }

    @Override
    public void setDbKey(Integer dbId) {
        DbContextHolder.setDBKey(String.format("%02d", dbId));
    }

    @Override
    public void setTbKey(Integer tbId) {
        DbContextHolder.setTBKey(String.format("%03d", tbId));
    }

    @Override
    public int getDbCount() {
        return dbRouterConfig.getDbCount();
    }

    @Override
    public int getTbCount() {
        return dbRouterConfig.getTbCount();
    }

}
