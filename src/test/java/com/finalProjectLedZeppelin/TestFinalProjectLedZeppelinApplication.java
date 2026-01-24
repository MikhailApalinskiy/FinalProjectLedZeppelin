package com.finalProjectLedZeppelin;

import org.springframework.boot.SpringApplication;

public class TestFinalProjectLedZeppelinApplication {

	public static void main(String[] args) {
		SpringApplication.from(FinalProjectLedZeppelinApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
