package cn.edu.jxau.db.router.strategy;

/**
 * @description:
 * @author：gezellighied.h
 * @date: 2022/1/12
 */

public interface IDbRouterStrategy {

    /**
     * 分库分表路由计算
     * @param val
     */
    void doRouter(String val);

    /**
     * 手动设置分库路由
     * @param dbId
     */
    void setDbKey(Integer dbId);

    /**
     * 手动设置分表路由
     * @param tbId
     */
    void setTbKey(Integer tbId);

    /**
     * 获取分库数
     * @return
     */
    int getDbCount();

    /**
     * 获取分表数
     * @return
     */
    int getTbCount();

    /**
     * 清除分库分表路由
     */
    void clear();
}
