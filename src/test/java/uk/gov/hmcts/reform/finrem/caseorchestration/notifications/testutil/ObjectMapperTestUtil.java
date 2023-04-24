package uk.gov.hmcts.reform.finrem.caseorchestration.notifications.testutil;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ObjectMapperTestUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static String convertObjectToJsonString(final Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}