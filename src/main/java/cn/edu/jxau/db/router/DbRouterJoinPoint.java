package cn.edu.jxau.db.router;

import cn.edu.jxau.db.router.annotation.DbRouter;
import cn.edu.jxau.db.router.common.Constants;
import cn.edu.jxau.db.router.strategy.IDbRouterStrategy;
import org.apache.commons.beanutils.BeanUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ObjectUtils;

import java.util.Map;

/**
 * @description:
 * @author：gezellighied.h
 * @date: 2022/1/12
 */
@Aspect
public class DbRouterJoinPoint {

    private Logger logger = LoggerFactory.getLogger(DbRouterJoinPoint.class);

    private DbRouterConfig dbRouterConfig;

    private Map<Constants.DbRouterStrategy, IDbRouterStrategy> dbRouterStrategyMap;

    public DbRouterJoinPoint(DbRouterConfig dbRouterConfig, Map<Constants.DbRouterStrategy, IDbRouterStrategy> dbRouterStrategyMap) {
        this.dbRouterConfig = dbRouterConfig;
        this.dbRouterStrategyMap = dbRouterStrategyMap;
    }

    @Pointcut("@annotation(cn.edu.jxau.db.router.annotation.DbRouter)")
    public void aopPoint() {

    }

    @Around("aopPoint() && @annotation(dbRouter)")
    public Object doRouter(ProceedingJoinPoint jp, DbRouter dbRouter) throws Throwable {

        String dbKey = dbRouter.splitKey();
        dbKey = ObjectUtils.isEmpty(dbKey) ? dbRouterConfig.getDefaultRouterKey() : dbKey;

        if(ObjectUtils.isEmpty(dbKey)) {
            throw new RuntimeException("annotation DBRouter key is null！");
        }

        Constants.DbRouterStrategy strategyEnum = dbRouter.strategy();

        String dbKeyAttr = getAttrValue(dbKey, jp.getArgs());
        IDbRouterStrategy strategy = dbRouterStrategyMap.get(strategyEnum);

        strategy.doRouter(dbKeyAttr);

        try {
            return jp.proceed();
        } finally {
            strategy.clear();
        }
    }

    public String getAttrValue(String attr, Object[] args) {
        if (1 == args.length) {
            Object arg = args[0];
            if (arg instanceof String) {
                return arg.toString();
            }
        }

        String filedValue = null;
        for (Object arg : args) {
            try {
                if (!ObjectUtils.isEmpty(filedValue)) {
                    break;
                }
                filedValue = BeanUtils.getProperty(arg, attr);
            } catch (Exception e) {
                logger.error("获取路由属性值失败 attr：{}", attr, e);
            }
        }
        return filedValue;
    }
}
