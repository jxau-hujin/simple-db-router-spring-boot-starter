/**
 * @description:
 * @author：gezellighied.h
 * @date: 2022/1/12
 */
package cn.edu.jxau;

// 继承 AbstractRoutingDataSource 动态数据源类

// 分库分表注解 注解本身函数代表分库，另外定义是否分表字段

// 通过定义类变量 ThreadLocal 存储 db、tb 序号

// 通过 AOP 实现分库分表

// 通过策略模式选择分库分表策略

// 通过 Mybatis Interceptor 拦截并修改需要分表的 SQL 语句

