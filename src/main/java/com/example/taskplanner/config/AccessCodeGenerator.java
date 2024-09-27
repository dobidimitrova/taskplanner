package com.example.taskplanner.config;

import java.util.UUID;

public class AccessCodeGenerator {
    public static String generateAccessCode() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}
