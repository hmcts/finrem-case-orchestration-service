package uk.gov.hmcts.reform.finrem.caseorchestration.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Configuration
@RequiredArgsConstructor
public class ObjectMapperConfiguration {

    private final FeatureToggleService featureToggleService;

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = JsonMapper
            .builder()
            .addModule(new JavaTimeModule())
            .addModule(new ParameterNamesModule(JsonCreator.Mode.PROPERTIES))
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .build();

        featureToggleSerialisation(objectMapper);

        return objectMapper;
    }

    @SuppressWarnings("java:S3740")
    private void featureToggleSerialisation(ObjectMapper objectMapper) {
        Map<Class, List<String>> fieldsIgnoredDuringSerialisation =
            featureToggleService.getFieldsIgnoredDuringSerialisation();
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
                    public List<BeanPropertyWriter> changeProperties(SerializationConfig config,
                                                                     BeanDescription beanDesc,
                                                                     List<BeanPropertyWriter> beanProperties) {
                        return removeIgnoredFieldsBeanProperties(beanProperties);
                    }
                });
            }
        };
    }

    @SuppressWarnings("java:S3740")
    private List<BeanPropertyWriter> removeIgnoredFieldsBeanProperties(List<BeanPropertyWriter> beanProperties) {
        Map<Class, List<String>> fieldsIgnoredDuringSerialisation =
            featureToggleService.getFieldsIgnoredDuringSerialisation();
        Set<Class> classesWithIgnoredFields = fieldsIgnoredDuringSerialisation.keySet();
        return beanProperties.stream().filter(beanPropertyWriter -> {
            Class beanDeclaringClass = beanPropertyWriter.getMember().getDeclaringClass();
            return !classesWithIgnoredFields.contains(beanDeclaringClass) || !fieldsIgnoredDuringSerialisation
                .get(beanDeclaringClass)
                .contains(beanPropertyWriter.getName());
        }).toList();
    }
}
