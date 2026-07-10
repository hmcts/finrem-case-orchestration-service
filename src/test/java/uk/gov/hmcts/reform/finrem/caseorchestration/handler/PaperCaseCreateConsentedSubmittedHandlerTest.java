package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CreateCaseService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.globalsearch.GlobalSearchService;

import java.io.InputStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestObjectMapperFactory.createObjectMapper;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class PaperCaseCreateConsentedSubmittedHandlerTest {

    @InjectMocks
    private PaperCaseCreateConsentedSubmittedHandler handler;

    @Mock
    private CreateCaseService createCaseService;

    @Mock
    private GlobalSearchService globalSearchService;

    private final ObjectMapper objectMapper = createObjectMapper();

    @Test
    void testCanHandle() {
        assertCanHandle(handler, CallbackType.SUBMITTED, CaseType.CONSENTED, EventType.NEW_PAPER_CASE);
    }

    @Test
    void givenACcdCallbackSolicitorCreateContestedCase_WhenHandle_thenAddSupplementary() {
        CallbackRequest callbackRequest =
            CallbackRequest.builder().caseDetails(getCase()).build();

        handler.handle(callbackRequest, AUTH_TOKEN);

        verify(createCaseService).setSupplementaryData(callbackRequest, AUTH_TOKEN);
        verify(globalSearchService, times(1)).setGlobalSearchDataByMap(anyMap());
    }

    private CaseDetails getCase() {
        try (InputStream resourceAsStream = getClass()
            .getResourceAsStream("/fixtures/contested/validate-hearing-with-fastTrackDecision.json")) {
            return objectMapper.readValue(resourceAsStream, CallbackRequest.class).getCaseDetails();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
