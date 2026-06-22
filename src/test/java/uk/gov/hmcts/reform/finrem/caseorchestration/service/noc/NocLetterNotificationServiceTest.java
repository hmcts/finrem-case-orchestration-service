package uk.gov.hmcts.reform.finrem.caseorchestration.service.noc;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.letters.handler.litigant.respondent.SolicitorAddedRespondentLetterHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.letters.handler.litigant.respondent.SolicitorRemovedRespondentLetterHandler;

import java.util.Arrays;

import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestObjectMapperFactory.createObjectMapper;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDetailsFromResource;

@ExtendWith(MockitoExtension.class)
class NocLetterNotificationServiceTest {
    NocLetterNotificationService noticeOfChangeLetterNotificationService;

    @Mock
    private SolicitorRemovedRespondentLetterHandler solicitorRemovedRespondentLetterHandler;
    @Mock
    private SolicitorAddedRespondentLetterHandler solicitorAddedRespondentLetterHandler;

    private final ObjectMapper objectMapper = createObjectMapper();

    @BeforeEach
    void setUpTest() {
        noticeOfChangeLetterNotificationService =
            new NocLetterNotificationService(Arrays.asList(solicitorAddedRespondentLetterHandler, solicitorRemovedRespondentLetterHandler));
    }

    @Test
    void shouldCallLetterHandlersCorrectly() {
        CaseDetails caseDetails = caseDetailsFromResource("/fixtures/noticeOfChange/contested/noc/noc-letter-notifications-add-and-revoke.json",
            objectMapper);
        CaseDetails caseDetailsBefore = caseDetailsFromResource("/fixtures/noticeOfChange/contested/noc/noc-letter-notifications-add-and-revoke-before.json",
            objectMapper);

        noticeOfChangeLetterNotificationService.sendNoticeOfChangeLetters(caseDetails, caseDetailsBefore, AUTH_TOKEN);

        verify(solicitorRemovedRespondentLetterHandler).handle(caseDetails, caseDetailsBefore, AUTH_TOKEN);
        verify(solicitorAddedRespondentLetterHandler).handle(caseDetails, caseDetailsBefore, AUTH_TOKEN);
    }
}
