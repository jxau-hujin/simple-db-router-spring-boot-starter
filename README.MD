# 简易分库分表 Starter

![简易分库分表 Starter](https://typora-1300369948.cos.ap-shanghai.myqcloud.com//halo/img/%E7%AE%80%E6%98%93%E5%88%86%E5%BA%93%E5%88%86%E8%A1%A8%20Starter.png)

## 为什么需要分库分表？

单体应用不足以支持现有或未来业务规模，需要进行拆分来保证业务划分合理性以及数据库稳定性



### 分库



#### 垂直拆分

按照业务类型进行拆分，例如单体电商项目进行垂直拆分后分为 「电商库」「支付库」「物流库」

#### 水平拆分

针对单库进行拆分，例如按照用户所属省份拆分为多个省份库，通过省份来确定最终存储的数据库



### 分表



#### 垂直拆分

如果单表存在热点访问字段，可能阻塞其他非热点字段访问，按照数据库范式进行拆分出热点表，避免阻塞其他数据查询

#### 水平拆分

如果单表数据量过大，500W 以上条记录，考虑对表进行拆分，例如按照哈希取余拆分为多个表，减轻单表压力



## 实现的功能

- 支持分库、分表，支持多种路由策略
- 支持读写分离，支持多种负载均衡算法



## 案例



### 引入 POM 组件

```xml
        <dependency>
            <groupId>cn.edu.jxau</groupId>
            <artifactId>simple-db-router-spring-boot-starter</artifactId>
            <version>0.0.1-SNAPSHOT</version>
        </dependency>
```



### application.yml 配置

```yaml
simple-db-router:
  jdbc:
    datasource:
    	# 分库数量
      dbCount: 2
      # 分表数量
      tbCount: 4
      # 默认路由键
      defaultRouterKey: id
      # 默认数据源
      defaultDataSource: db00
      # 数据源列表
      dbList: db01,db02
      
      db00:
      	# 如果是单节点只需要配置 write
        write:
          driver-class-name: com.mysql.jdbc.Driver
          url: jdbc:mysql://127.0.0.1:3306/db
          username: root
          password: root
        # 读写分离时 读节点列表
        readList: db00Read00,db00Read01
        db00Read00:
          driver-class-name: com.mysql.jdbc.Driver
          url: jdbc:mysql://127.0.0.1:3306/db
          username: root
          password: root
        db00Read01:
          driver-class-name: com.mysql.jdbc.Driver
          url: jdbc:mysql://127.0.0.1:3306/db
          username: root
          password: root
```



### DbRouter 注解使用

```java
@Mapper
// 默认不分表，分表需显示添加
@DbRouter(needSplitTable = true)
public interface IUserDao {

    @DbRouter(splitKey = "userId")
    int insert(User user);

    @DbRouter(splitKey = "userId")
    int updatePassword(User user);

  	// 自定义路由键，选择 READ or WRITE 操作，选择负载均衡算法
    @DbRouter(splitKey = "userId", operationType = Constants.DataSourceType.READ, loadBalance = Constants.LoadBalance.RANDOM)
    User queryByUserId(String userId);
}

```



### 支持声明式事务下分库分表

```java
    @Resource
    private Map<Constants.DbRouterStrategy, IDbRouterStrategy> dbRouterStrategyMap;

    @Override
    public int updatePassword(User user) throws Exception {

        IDbRouterStrategy dbRouter = dbRouterStrategyMap.get(Constants.DbRouterStrategy.HASHCODE);
        dbRouter.doRouter(user.getUserId());
      
        return transactionTemplate.execute(status -> {
            try {
                int v1 = userRepository.updatePassword(user);
                if(v1 == 0) {
                    throw new Exception("更新密码失败");
                }
                int v2 = userRepository.updateModifyCount(user.getUserId());
                if(v2 == 0) {
                    throw new Exception("更新操作数失败");
                }
            } catch (Exception e) {
                logger.error("更新异常 e: {}", e);
                status.setRollbackOnly();
                return -1;
            } finally {
                dbRouter.clear();
            }
            return 1;
        });
    }
```





## 实现细节



### 整体框架

![总体架构图 (1)](https://typora-1300369948.cos.ap-shanghai.myqcloud.com//halo/img/%E6%80%BB%E4%BD%93%E6%9E%B6%E6%9E%84%E5%9B%BE%20(1).png)



### 路由策略



- 支持轮询、随机

分库分表核心类，根据具体策略计算对应的库号及表号

另外也提供手动设置库表号的方法





### 负载均衡算法



- 支持轮询、随机

读写分离情况下，写请求已经在路由策略中确定因此无需再负载均衡

读请求则在动态数据源选择时根据具体算法来计算库号



### 数据源加载及数据结构



通过 Spring 提供的扩展机制 「Aware」使得能感知到 「application.yaml」中配置文件，将磁盘文件加载到内存中



#### 如何保存路由策略以及负载均衡算法计算出的库表号？



- 函数签名添加参数 ❌

分库分表本不属于业务代码，仅仅是因为业务增长而面临的设计架构问题，因此不应该出现在函数的参数列表中

```java
public void function(Object... args, int dbIdx, int tbIdx) {
 	// 非常的不优雅
}
```



- ThreadLocal 保存 ✅

通过 ThreadLocal 优雅的实现了分库分表数据的透传，因为数据绑定在每个 Thread 中，因此可以做到对调用者透明

⚠️⚠️⚠️ 使用 ThreadLocal 一定要记得手动 「remove」数据，否则可能导致脏数据或数据泄漏



### 切面和注解

「DbRouter」注解保存分库分表所需信息

- 路由键
- 是否分表
- 分库分表策略
- 负载均衡算法
- 操作类型（读 or 写）



#### 为什么分表操作需要将注解定义在类上



```java
@Mapper
@DbRouter(needSplitTable = true)
public interface IUserDao {

}
```

在实现分表操作对 「mapper」不侵入的实现时获取是否分表参数比较困难

同时个人认为如果使用分表，那么该 Dao 类下的所有操作都是需要进行分表的，因此定义在类上语义更加明确



「DbRouterJoinPoint」通过 Spring AOP 实现切面编程，动态代理生成目标对象，因此无需在业务代码中进行硬编码



### 动态数据源切换和 Mybatis 插件支持分表



「DynamicDataSource」根据路由策略计算结果从 「DbContextHolder」中获取库表信息

如果为读操作那么还需要进行负载均衡计算

通过继承 「AbstractRoutingDataSource」实现了动态加载数据源，也是分库分表的关键所在



#### 如何优雅的实现分表操作？



当前面临的窘境

```xml
  
		<update id="updatePassword" parameterType="cn.edu.jxau.model.User">
        UPDATE u_#{tbidx}
        SET password = #{password}
        WHERE user_id = #{userId}
    </update>
```



我们希望引入任何组件都能尽可能的对业务方做到无感知，当前分表操作依然在 「mapper」文件中

因此引入 Mybatis 插件机制



```java
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
				// 判断是否需要分表
        if(dbRouter == null || !dbRouter.needSplitTable()) {
            return invocation.proceed();
        }
				// 替换表名
        BoundSql boundSql = statementHandler.getBoundSql();
        String replaceSql = getReplaceSql(boundSql.getSql());

        updateSql(boundSql, replaceSql);

        return invocation.proceed();
    }
```



通过这种方式得到的结果

```xml
  
		<update id="updatePassword" parameterType="cn.edu.jxau.model.User">
        UPDATE u
        SET password = #{password}
        WHERE user_id = #{userId}
    </update>
```



做到了对业务方无感知，很优雅



### 支持声明式事务



场景：在需要事务支持的业务场景下，比如用户抽奖需要落用户抽奖记录以及需要更新该活动剩余参与次数，涉及两张表

如果使用注解式声明事务，那么会切换两次数据源会导致事务的实效
最终结果是 Spring 依赖数据库实现的原子性和一致性就会出现问题

因此需要将切换数据源提前在事务开启前，通过该方案就无需在事务执行中再次进行数据源的切换，保证事务的特性



```java
    @Resource
    private Map<Constants.DbRouterStrategy, IDbRouterStrategy> dbRouterStrategyMap;

    @Override
    public int update(User user) throws Exception {

        IDbRouterStrategy dbRouter = dbRouterStrategyMap.get(Constants.DbRouterStrategy.HASHCODE);
        dbRouter.doRouter(user.getUserId());
      
        return transactionTemplate.execute(status -> {
            try {
                int v1 = userRepository.updateOne(user);
                if(v1 == 0) {
                    throw new Exception("one failure");
                }
                int v2 = userRepository.updateTwo(user.getUserId());
                if(v2 == 0) {
                    throw new Exception("two failure");
                }
            } catch (Exception e) {
                logger.error("更新异常 e: {}", e);
                status.setRollbackOnly();
                return -1;
            } finally {
              	// 务必清除 DbContextHolder 中的内容
                dbRouter.clear();
            }
            return 1;
        });
    }
```



