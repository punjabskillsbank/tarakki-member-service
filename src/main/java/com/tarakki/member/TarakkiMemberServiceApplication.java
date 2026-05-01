package com.tarakki.member;

import com.tarakki.member.dto.MemberDTO;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;

import static org.springframework.data.jpa.domain.AbstractPersistable_.id;


@SpringBootApplication
@EntityScan(basePackages = "com.tarakki.common.entity")
public class TarakkiMemberServiceApplication {

	public static void main(String[] args) {

        SpringApplication.run(TarakkiMemberServiceApplication.class, args);


	}

}
