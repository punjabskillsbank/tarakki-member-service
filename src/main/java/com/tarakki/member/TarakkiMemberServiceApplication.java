package com.tarakki.member;

import com.tarakki.member.dto.MemberDTO;
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
