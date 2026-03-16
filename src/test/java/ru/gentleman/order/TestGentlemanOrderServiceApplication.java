package ru.gentleman.order;

import org.springframework.boot.SpringApplication;

public class TestGentlemanOrderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.from(GentlemanOrderServiceApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
