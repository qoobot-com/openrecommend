package com.qoobot.openrecommend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * OpenRecommend 应用启动类
 *
 * @author Qoobot Team
 * @since 1.0.0
 */
@SpringBootApplication(scanBasePackages = "com.qoobot.openrecommend")
@EnableAsync
@EnableCaching
@EnableScheduling
public class OpenRecommendApplication {

    public static void main(String[] args) {
        SpringApplication.run(OpenRecommendApplication.class, args);
        System.out.println("""

            ============================================
                OpenRecommend 启动成功！
                访问地址: http://localhost:8080
                API文档: http://localhost:8080/swagger-ui.html
                健康检查: http://localhost:8080/actuator/health
            ============================================
            """);
    }
}
