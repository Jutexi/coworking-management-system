package com.app.coworking;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class CoworkingApplicationTests {

    @Autowired
    private CoworkingApplication application; // главный бин

    @Test
    void contextLoads() {
        // проверяем, что контекст вообще поднялся
        assertThat(application).isNotNull();
    }

    @Test
    void applicationStarts() {
        // smoke-тест: приложение стартует без ошибок
        CoworkingApplication.main(new String[] {});
    }
}

