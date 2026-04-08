package kz.kbtu.yarmaq.zombiemanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class ZombieManagerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ZombieManagerApplication.class, args);
	}

}
