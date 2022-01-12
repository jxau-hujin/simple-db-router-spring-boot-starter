package cn.edu.jxau.db.router.config;

import cn.edu.jxau.db.router.DbRouterConfig;
import cn.edu.jxau.db.router.DbRouterJoinPoint;
import cn.edu.jxau.db.router.common.Constants;
import cn.edu.jxau.db.router.common.PropertyUtil;
import cn.edu.jxau.db.router.dynamic.DynamicDataSource;
import cn.edu.jxau.db.router.dynamic.SplitTableMyBatisPlugin;
import cn.edu.jxau.db.router.strategy.IDbRouterStrategy;
import cn.edu.jxau.db.router.strategy.impl.DbRouterStrategyHash;
import cn.edu.jxau.db.router.strategy.impl.DbRouterStrategyRandom;
import org.apache.ibatis.plugin.Interceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * @description:
 * @author：gezellighied.h
 * @date: 2022/1/12
 */
@Configuration
public class DataSourceAutoConfig implements EnvironmentAware {

    private Logger logger = LoggerFactory.getLogger(DataSourceAutoConfig.class);

    /**
     * 常量定义
     */
    private static final String CONFIG_PREFIX = "simple-db-router.jdbc.datasource.";

    private static final String DEFAULT_ROUTER_KEY = "defaultRouterKey";

    private static final String DB_LIST = "dbList";

    private static final String SPLIT = ",";

    private static final String DEFAULT_DATA_SOURCE = "defaultDataSource";

    private static final String DB_COUNT = "dbCount";

    private static final String TB_COUNT = "tbCount";


    /**
     * 默认数据源信息
     */
    private Map<String, Object> defaultDataSourceConfig;

    /**
     * 数据源信息
     */
    private Map<String, Map<String, Object>> dataSourceConfig = new HashMap<>();

    /**
     * 分库数量
     */
    private Integer dbCount;

    /**
     * 分表数量
     */
    private Integer tbCount;

    /**
     * 默认分库分表字段
     */
    private String defaultRouterKey;


    @Bean
    public Map<Constants.DbRouterStrategy, IDbRouterStrategy> dbRouterStrategyMap(DbRouterStrategyHash dbRouterStrategyHash, DbRouterStrategyRandom dbRouterStrategyRandom) {
        Map<Constants.DbRouterStrategy, IDbRouterStrategy> dbRouterStrategyMap = new HashMap<>(16);
        dbRouterStrategyMap.put(Constants.DbRouterStrategy.HASHCODE, dbRouterStrategyHash);
        dbRouterStrategyMap.put(Constants.DbRouterStrategy.RANDOM, dbRouterStrategyRandom);
        return dbRouterStrategyMap;
    }

    @Bean
    public DbRouterStrategyHash dbRouterStrategyHash() {
        return new DbRouterStrategyHash();
    }

    @Bean
    public DbRouterStrategyRandom dbRouterStrategyRandom() {
        return new DbRouterStrategyRandom();
    }

    @Bean
    public DbRouterConfig dbRouterConfig() {
        return new DbRouterConfig(dbCount, tbCount, defaultRouterKey);
    }

    @Bean(name = "db-router-point")
    @ConditionalOnMissingBean
    public DbRouterJoinPoint point(DbRouterConfig dbRouterConfig, Map<Constants.DbRouterStrategy, IDbRouterStrategy> dbRouterStrategyMap) {
        return new DbRouterJoinPoint(dbRouterConfig, dbRouterStrategyMap);
    }

    @Bean
    public Interceptor plugin() {
        return new SplitTableMyBatisPlugin();
    }

    @Bean
    public DataSource dataSource() {
        Map<Object, Object> targetDataSources = new HashMap<>(16);
        for (String dbInfo : dataSourceConfig.keySet()) {
            Map<String, Object> objMap = dataSourceConfig.get(dbInfo);
            targetDataSources.put(dbInfo, new DriverManagerDataSource(objMap.get("url").toString(), objMap.get("username").toString(), objMap.get("password").toString()));
        }

        DynamicDataSource dynamicDataSource = new DynamicDataSource();
        dynamicDataSource.setTargetDataSources(targetDataSources);
        dynamicDataSource.setDefaultTargetDataSource(new DriverManagerDataSource(defaultDataSourceConfig.get("url").toString(), defaultDataSourceConfig.get("username").toString(), defaultDataSourceConfig.get("password").toString()));

        return dynamicDataSource;
    }

    @Override
    public void setEnvironment(Environment environment) {

        dbCount = Integer.parseInt(environment.getProperty(CONFIG_PREFIX + DB_COUNT));
        tbCount = Integer.parseInt(environment.getProperty(CONFIG_PREFIX + TB_COUNT));

        defaultRouterKey = environment.getProperty(CONFIG_PREFIX + DEFAULT_ROUTER_KEY);

        String defaultDataSource = environment.getProperty(CONFIG_PREFIX + DEFAULT_DATA_SOURCE);
        defaultDataSourceConfig = PropertyUtil.handle(environment, CONFIG_PREFIX + defaultDataSource, Map.class);


        String dataSources = environment.getProperty(CONFIG_PREFIX + DB_LIST);

        assert dataSources != null;
        for (String dbInfo : dataSources.split(SPLIT)) {
            Map<String, Object> dataSourceProps = PropertyUtil.handle(environment, CONFIG_PREFIX + dbInfo, Map.class);
            dataSourceConfig.put(dbInfo, dataSourceProps);
        }
    }
}
