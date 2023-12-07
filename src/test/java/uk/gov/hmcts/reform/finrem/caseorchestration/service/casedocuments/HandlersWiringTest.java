package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseTest;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@TestPropertySource(locations = "/application.properties")
public class HandlersWiringTest extends BaseTest {

    @Autowired
    private List<DocumentHandler> handlers;

    @Test
    public void givenOrder_whenInjected_thenByOrderValue() {
        assertThat(handlers.get(0).collectionType, is(""));

    }
}
