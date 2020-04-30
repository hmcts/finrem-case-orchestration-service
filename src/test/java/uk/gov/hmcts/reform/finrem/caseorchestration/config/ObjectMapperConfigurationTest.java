package uk.gov.hmcts.reform.finrem.caseorchestration.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ApprovedOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasNoJsonPath;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ObjectMapperConfigurationTest {

    @Spy
    FeatureToggleService featureToggleService;

    ObjectMapperConfiguration objectMapperConfiguration;

    @Before
    public void setup() {
        objectMapperConfiguration = new ObjectMapperConfiguration(featureToggleService);
    }

    @Test
    public void createObjectMapper() {
        assertThat(objectMapperConfiguration.objectMapper(Jackson2ObjectMapperBuilder.json()), is(notNullValue()));
    }

    @Test
    public void givenApprovedConsentOrderNotificationLetterFeatureIsDisabled_whenSerialisingApprovedOrder_thenFieldIsNotSerialised() throws JsonProcessingException {
        when(featureToggleService.isApprovedConsentOrderNotificationLetterEnabled()).thenReturn(false);

        ApprovedOrder approvedOrder = ApprovedOrder.builder().build();
        String approvedOrderJson = objectMapperConfiguration.objectMapper(Jackson2ObjectMapperBuilder.json()).writeValueAsString(approvedOrder);
        assertThat(approvedOrderJson, hasNoJsonPath("consentOrderApprovedNotificationLetter"));
    }

    @Test
    public void givenApprovedConsentOrderNotificationLetterFeatureIsEnabled_whenSerialisingApprovedOrder_thenFieldIsSerialised() throws JsonProcessingException {
        when(featureToggleService.isApprovedConsentOrderNotificationLetterEnabled()).thenReturn(true);

        ApprovedOrder approvedOrder = ApprovedOrder.builder().build();
        String approvedOrderJson = objectMapperConfiguration.objectMapper(Jackson2ObjectMapperBuilder.json()).writeValueAsString(approvedOrder);
        assertThat(approvedOrderJson, hasJsonPath("consentOrderApprovedNotificationLetter"));
    }
}
