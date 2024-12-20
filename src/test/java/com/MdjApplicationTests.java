package com;

import net.bytebuddy.asm.Advice;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.LocalDateTime;

@SpringBootTest
class MdjApplicationTests {

    @Test
    void contextLoads() {
        LocalDate localDateTime = LocalDate.now().withDayOfYear(1);
        System.out.println(localDateTime);
    }

}
