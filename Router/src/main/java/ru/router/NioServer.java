package ru.router;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class NioServer {

    public static void main(String[] args) {
        SpringApplication.run(NioServer.class, args);
    }
}