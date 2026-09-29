package com.digivalet.agent.config;

import java.io.IOException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

/**
 * @author Mandvee Vatsa
 * @date Sep 29, 2026
 */
@Component
public class SimulatorConfigLoader {

    private final SimulatorConfig config;

    public SimulatorConfigLoader(ObjectMapper objectMapper) throws IOException {

        ClassPathResource resource =
                new ClassPathResource("simulator-config.json");

        this.config = objectMapper.readValue(
                resource.getInputStream(),
                SimulatorConfig.class
        );
    }

    public SimulatorConfig getConfig() {
        return config;
    }
}
