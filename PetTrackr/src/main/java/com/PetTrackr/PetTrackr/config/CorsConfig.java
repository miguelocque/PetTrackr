package com.PetTrackr.PetTrackr.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS configuration to allow frontend React app to call backend API.
 * Allows requests from localhost:5173 (Vite dev server) and localhost:3000 (create-react-app).
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:5173", "http://localhost:3000")
                .allowedMethods("GET", "POST", "PATCH", "DELETE", "PUT", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
