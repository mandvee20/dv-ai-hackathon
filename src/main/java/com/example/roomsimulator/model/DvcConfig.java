package com.example.roomsimulator.model;

public record DvcConfig(String ip, Integer port, HealthConfig health) {

    public DvcConfig {
        ip = ip == null ? "127.0.0.1" : ip;
        port = port == null ? 9010 : port;
        health = health == null ? HealthConfig.healthy() : health;
    }
}
