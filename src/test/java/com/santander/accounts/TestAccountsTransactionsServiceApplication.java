package com.santander.accounts;

import org.springframework.boot.SpringApplication;

public class TestAccountsTransactionsServiceApplication {

	public static void main(String[] args) {
		SpringApplication.from(AccountsTransactionsServiceApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
