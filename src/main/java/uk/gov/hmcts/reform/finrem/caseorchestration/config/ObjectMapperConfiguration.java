package uk.gov.hmcts.reform.finrem.caseorchestration.config;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Configuration
@RequiredArgsConstructor
public class ObjectMapperConfiguration {

    private final FeatureToggleService featureToggleService;

    @Bean
    public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        LocalDateTimeDeserializer localDateTimeDeserializer = new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS"));
        javaTimeModule.addDeserializer(LocalDateTime.class, localDateTimeDeserializer);

        ObjectMapper objectMapper = builder.createXmlMapper(false).build();
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.registerModule(javaTimeModule);
        featureToggleSerialisation(objectMapper);

        return objectMapper;
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
