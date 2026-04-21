package com.tarakki.member;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;


@SpringBootApplication
@EntityScan(basePackages = "com.tarakki.common.entity")
public class TarakkiMemberServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(TarakkiMemberServiceApplication.class, args);
	}

}
