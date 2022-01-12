package cn.edu.jxau.db.router;

import cn.edu.jxau.db.router.common.Constants;

/**
 * @description:
 * @author：gezellighied.h
 * @date: 2022/1/12
 */
public class DbContextHolder {

    public static final String DB_PREFIX = "db";

    public static final String READ_DB_PREFIX = "Read";

    private static final ThreadLocal<String> dbKey = new ThreadLocal<String>();

    private static final ThreadLocal<String> tbKey = new ThreadLocal<String>();

    private static final ThreadLocal<Constants.DataSourceType> opType = new ThreadLocal<>();

    private static final ThreadLocal<Constants.LoadBalance> loadBalance = new ThreadLocal<>();

    public static String getDBKey(){
        return dbKey.get();
    }

    public static void setDBKey(String dbKeyIdx){
        dbKey.set(dbKeyIdx);
    }

    public static void clearDBKey(){
        dbKey.remove();
    }

    public static String getTBKey(){
        return tbKey.get();
    }

    public static void setTBKey(String tbKeyIdx){
        tbKey.set(tbKeyIdx);
    }

    public static void clearTBKey(){
        tbKey.remove();
    }

    public static Constants.DataSourceType getOpType(){
        return opType.get();
    }

    public static void setOpType(Constants.DataSourceType opTypeVal){
        opType.set(opTypeVal);
    }

    public static void clearOpType() {
        opType.remove();
    }

    public static Constants.LoadBalance getLoadBalance(){
        return loadBalance.get();
    }

    public static void setLoadBalance(Constants.LoadBalance loadBalanceVal){
        loadBalance.set(loadBalanceVal);
    }

    public static void clearLoadBalance() {
        loadBalance.remove();
    }
}
