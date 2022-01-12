package cn.edu.jxau.db.router;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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

}
