package com.ferrisys;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.ferrisys")
@EntityScan(basePackages = "com.ferrisys.common.entity")
@EnableJpaRepositories(basePackages = "com.ferrisys.repository")
public class FerrisysServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(FerrisysServiceApplication.class, args);
	}
}
