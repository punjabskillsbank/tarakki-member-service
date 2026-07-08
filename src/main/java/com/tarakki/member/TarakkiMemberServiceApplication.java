package com.tarakki.member;

import com.tarakki.common.config.CorsConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Import;


@SpringBootApplication
@EntityScan(basePackages = {"com.tarakki.member.entity"})
@Import(CorsConfig.class)
public class TarakkiMemberServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(TarakkiMemberServiceApplication.class, args);
	}

}
