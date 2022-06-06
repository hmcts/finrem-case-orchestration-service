package uk.gov.hmcts.reform.finrem.caseorchestration.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.fasterxml.jackson.databind.DeserializationFeature.READ_ENUMS_USING_TO_STRING;
import static com.fasterxml.jackson.databind.DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE;
import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_ENUMS_USING_TO_STRING;

@Configuration
@RequiredArgsConstructor
public class ObjectMapperConfiguration {

    private final FeatureToggleService featureToggleService;

    @Bean
    public ObjectMapper objectMapper() {
        Jackson2ObjectMapperBuilder objectMapperBuilder =
            new Jackson2ObjectMapperBuilder()
                .featuresToEnable(READ_ENUMS_USING_TO_STRING)
                .featuresToEnable(READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE)
                .featuresToEnable(WRITE_ENUMS_USING_TO_STRING)
                .serializationInclusion(JsonInclude.Include.NON_ABSENT);

        ObjectMapper mapper = objectMapperBuilder.createXmlMapper(false).build();
        mapper.configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true);
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        featureToggleSerialisation(mapper);

        return mapper;
    }

    private void featureToggleSerialisation(ObjectMapper objectMapper) {
        Map<Class, List<String>> fieldsIgnoredDuringSerialisation = featureToggleService.getFieldsIgnoredDuringSerialisation();
        if (!fieldsIgnoredDuringSerialisation.isEmpty()) {
            objectMapper.registerModule(makeSimpleModuleWithCustomBeanSerializerModifier());
        }
    }

    private SimpleModule makeSimpleModuleWithCustomBeanSerializerModifier() {
        return new SimpleModule() {
            @Override
            public void setupModule(SetupContext context) {
                super.setupModule(context);
                context.addBeanSerializerModifier(new BeanSerializerModifier() {
                    @Override
                    public List<BeanPropertyWriter> changeProperties(SerializationConfig config, BeanDescription beanDesc,
                                                                     List<BeanPropertyWriter> beanProperties) {
                        return removeIgnoredFieldsBeanProperties(beanProperties);
                    }
                });
            }
        };
    }

    private List<BeanPropertyWriter> removeIgnoredFieldsBeanProperties(List<BeanPropertyWriter> beanProperties) {
        Map<Class, List<String>> fieldsIgnoredDuringSerialisation = featureToggleService.getFieldsIgnoredDuringSerialisation();
        Set<Class> classesWithIgnoredFields = fieldsIgnoredDuringSerialisation.keySet();
        return beanProperties.stream()
            .filter(beanPropertyWriter -> {
                Class beanDeclaringClass = beanPropertyWriter.getMember().getDeclaringClass();
                return !classesWithIgnoredFields.contains(beanDeclaringClass)
                    || !fieldsIgnoredDuringSerialisation.get(beanDeclaringClass).contains(beanPropertyWriter.getName());
            })
            .collect(Collectors.toList());
    }
}
