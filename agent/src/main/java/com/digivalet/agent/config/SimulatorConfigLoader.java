package com.digivalet.agent.config;
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
