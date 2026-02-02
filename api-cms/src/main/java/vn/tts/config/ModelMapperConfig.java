package vn.tts.config;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import vn.tts.model.dto.RoleDto;
import vn.tts.model.response.auth.RoleResponse;

@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration()
                .setAmbiguityIgnored(true);

        return mapper;
    }
}
