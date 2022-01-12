package cn.edu.jxau.db.router.annotation;

import cn.edu.jxau.db.router.common.Constants;

import java.lang.annotation.*;

/**
 * @description:
 * @author：gezellighied.h
 * @date: 2022/1/12
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface DbRouter {

    /**
     * 分库分表字段
     * @return
     */
    String splitKey() default "";

    /**
     * 是否分表
     * @return
     */
    boolean needSplitTable() default false;

    /**
     * 分库分表策略
     * @return
     */
    Constants.DbRouterStrategy strategy() default Constants.DbRouterStrategy.HASHCODE;

    /**
     * 读写分离
     */
    Constants.DataSourceType operationType() default Constants.DataSourceType.WRITE;

    /**
     * 负载均衡算法
     */
    Constants.LoadBalance loadBalance() default Constants.LoadBalance.POLL;
}
