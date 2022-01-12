package cn.edu.jxau;

import cn.edu.jxau.db.router.annotation.DbRouter;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@DbRouter(needSplitTable = true)
class SimpleDbRouterSpringBootStarterApplicationTests {

    @Test
    void contextLoads() {
    }
}
