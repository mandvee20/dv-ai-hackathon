package com.digivalet.agent.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Mandvee Vatsa
 * @date Sep 29, 2026
 */
@Configuration
public class JacksonConfig
{

   @Bean
   public ObjectMapper objectMapper()
   {
      return new ObjectMapper();
   }
}
