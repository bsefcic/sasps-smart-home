package ro.upb.sasps.smarthome.server;

import java.util.Arrays;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import ro.upb.sasps.smarthome.baseline.BaselineHome;
import ro.upb.sasps.smarthome.contracts.SmartHome;
import ro.upb.sasps.smarthome.patterns.PatternsHome;

@Configuration(proxyBeanMethods = false)
public class HomeConfiguration {
  @Bean
  SmartHome smartHome(@Value("${smart-home.variant}") String variant) {
    return switch (variant) {
      case "baseline" -> new BaselineHome();
      case "patterns" -> new PatternsHome();
      default ->
          throw new IllegalArgumentException("smart-home.variant must be baseline or patterns");
    };
  }

  @Bean
  WebMvcConfigurer cors(@Value("${smart-home.cors.allowed-origins}") String origins) {
    String[] allowed = Arrays.stream(origins.split(",")).map(String::trim).toArray(String[]::new);
    return new WebMvcConfigurer() {
      @Override
      public void addCorsMappings(CorsRegistry registry) {
        registry
            .addMapping("/api/**")
            .allowedOrigins(allowed)
            .allowedMethods("GET", "POST", "OPTIONS")
            .allowedHeaders("Content-Type");
      }
    };
  }
}
