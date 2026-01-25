package com.sitepen.issuetracker.config;

@Configuration
@EnableMongoAuditing
public class MongoConfig {
    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }
}
