package cn.edu.jxau.db.router.dynamic;

import cn.edu.jxau.db.router.DbContextHolder;
import cn.edu.jxau.db.router.annotation.DbRouter;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.DefaultReflectorFactory;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @description:
 * @author：gezellighied.h
 * @date: 2022/1/12
 */
@Intercepts({
        @Signature(type = StatementHandler.class,
        method = "prepare",
        args = {Connection.class, Integer.class})
})
public class SplitTableMyBatisPlugin implements Interceptor {

    private Logger logger = LoggerFactory.getLogger(SplitTableMyBatisPlugin.class);

    private Pattern pattern = Pattern.compile("(from|into|update)[\\s]+(\\w+)", Pattern.CASE_INSENSITIVE);


    @Override
    public Object intercept(Invocation invocation) throws Throwable {

        StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
        MetaObject metaObject = MetaObject.forObject(statementHandler,
                SystemMetaObject.DEFAULT_OBJECT_FACTORY,
                SystemMetaObject.DEFAULT_OBJECT_WRAPPER_FACTORY,
                new DefaultReflectorFactory());
        MappedStatement mappedStatement = (MappedStatement)
                metaObject.getValue("delegate.mappedStatement");

        String id = mappedStatement.getId();
        DbRouter dbRouter = getDbRouter(id);

        if(dbRouter == null || !dbRouter.needSplitTable()) {
            return invocation.proceed();
        }

        BoundSql boundSql = statementHandler.getBoundSql();
        String replaceSql = getReplaceSql(boundSql.getSql());

        updateSql(boundSql, replaceSql);

        return invocation.proceed();
    }

    private String getReplaceSql(String sql) throws Exception {
        Matcher matcher = pattern.matcher(sql);
        String tableName = null;
        if (matcher.find()) {
            tableName = matcher.group().trim();
        }
        assert null != tableName;
        if(tableName == null) {
            logger.error("not match tableName! sql: {}", sql);
            throw new Exception("not match tableName!");
        }

        return matcher.replaceAll(tableName + "_" + DbContextHolder.getTBKey());
    }

    private void updateSql(BoundSql boundSql, String replaceSql) throws NoSuchFieldException, IllegalAccessException {
        Field field = boundSql.getClass().getDeclaredField("sql");
        field.setAccessible(true);
        field.set(boundSql, replaceSql);
        field.setAccessible(false);
    }

    private DbRouter getDbRouter(String id) throws ClassNotFoundException, NoSuchFieldException {
        String className = id.substring(0, id.lastIndexOf("."));
        Class<?> clazz = Class.forName(className);
        return clazz.getAnnotation(DbRouter.class);
    }


}
