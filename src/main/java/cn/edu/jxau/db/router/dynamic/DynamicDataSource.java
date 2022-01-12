package cn.edu.jxau.db.router.dynamic;

import cn.edu.jxau.db.router.DbContextHolder;
import cn.edu.jxau.db.router.common.Constants;
import cn.edu.jxau.db.router.load.balance.IDbRouterLoadBalance;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @description:
 * @author：gezellighied.h
 * @date: 2022/1/12
 */
public class DynamicDataSource extends AbstractRoutingDataSource {

    @Resource
    private Map<Constants.LoadBalance, IDbRouterLoadBalance> dbRouterLoadBalanceMap;

    @Override
    protected Object determineCurrentLookupKey() {
        Constants.DataSourceType opType = DbContextHolder.getOpType();
        if(Constants.DataSourceType.WRITE.getType().equals(opType.getType())) {
            return DbContextHolder.getDBKey();
        }
        // 负载均衡算法
        Constants.LoadBalance loadBalanceType = DbContextHolder.getLoadBalance();
        IDbRouterLoadBalance dbRouterLoadBalance = dbRouterLoadBalanceMap.get(loadBalanceType);
        dbRouterLoadBalance.doLoadBalance();

        return DbContextHolder.getDBKey();
    }
}
