package com.example.roomsimulator;

import com.example.roomsimulator.config.SimulatorProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(SimulatorProperties.class)
public class RoomSimulatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(RoomSimulatorApplication.class, args);
    }
}
