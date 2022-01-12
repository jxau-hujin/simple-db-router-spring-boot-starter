package cn.edu.jxau.db.router;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * @description:
 * @author：gezellighied.h
 * @date: 2022/1/12
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class DbRouterConfig {

    private int dbCount;

    private int tbCount;

    private String defaultRouterKey;

    /**
     * 主库 dbKey -> 从库数量
     */
    private Map<String, Integer> masterToSlaveCount;

}
