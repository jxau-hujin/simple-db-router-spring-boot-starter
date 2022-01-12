package cn.edu.jxau.db.router.dynamic;

import cn.edu.jxau.db.router.DbContextHolder;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * @description:
 * @author：gezellighied.h
 * @date: 2022/1/12
 */
public class DynamicDataSource extends AbstractRoutingDataSource {

    @Override
    protected Object determineCurrentLookupKey() {
        return DbContextHolder.getDBKey();
    }
}
