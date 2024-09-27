package com.example.taskplanner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TaskPlannerApplication {
    public static void main(String[] args) {
        SpringApplication.run(TaskPlannerApplication.class, args);
    }

}
