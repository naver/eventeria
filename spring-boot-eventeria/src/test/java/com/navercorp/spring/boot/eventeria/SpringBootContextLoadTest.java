package com.navercorp.spring.boot.eventeria;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.navercorp.spring.boot.eventeria.config.MessageConfiguration;

@SpringBootTest(classes = MessageConfiguration.class)
public class SpringBootContextLoadTest {

	@Test
	void contextLoads() {
	}
}
