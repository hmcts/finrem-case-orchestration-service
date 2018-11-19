package uk.gov.hmcts.reform.finrem.caseorchestration.model.document;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class DocumentRequestTest {

    public static final Map<String, Object> VALUES = ImmutableMap.of("K", "V");
    public static final String TEMPLATE_NAME = "TEMPLATE";

    @Test
    public void properties() {
        DocumentRequest fixture = documentRequest();
        assertThat(fixture.getTemplate(), is(TEMPLATE_NAME));
        assertThat(fixture.getValues(), is(equalTo(VALUES)));
    }

    private DocumentRequest documentRequest() {
        return DocumentRequest.builder()
                .template(TEMPLATE_NAME)
                .values(VALUES)
                .build();
    }
}