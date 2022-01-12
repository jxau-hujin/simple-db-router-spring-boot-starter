package cn.edu.jxau.db.router.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @description:
 * @author：gezellighied.h
 * @date: 2022/1/12
 */
public class Constants {

    @AllArgsConstructor
    @Getter
    public enum DbRouterStrategy {
        HASHCODE(0, "哈希散列"),
        RANDOM(1, "随机");

        private Integer type;
        private String desc;
    }


    @AllArgsConstructor
    @Getter
    public enum DataSourceType {
        WRITE(0, "写节点"),
        READ(1, "读节点");

        private Integer type;
        private String desc;
    }

    @AllArgsConstructor
    @Getter
    public enum LoadBalance {
        POLL(0, "轮询"),
        RANDOM(1, "随机");


        private Integer type;
        private String desc;
    }
}
