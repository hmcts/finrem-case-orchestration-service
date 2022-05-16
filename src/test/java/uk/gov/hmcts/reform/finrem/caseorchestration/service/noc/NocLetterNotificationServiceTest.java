package uk.gov.hmcts.reform.finrem.caseorchestration.service.noc;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.letters.handler.litigant.respondent.SolicitorAddedRespondentLetterHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.letters.handler.litigant.respondent.SolicitorRemovedRespondentLetterHandler;

import java.util.Arrays;

import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDetailsFromResource;

@RunWith(MockitoJUnitRunner.class)
public class NocLetterNotificationServiceTest {

    protected static final String AUTH_TOKEN = "authToken";
    NocLetterNotificationService noticeOfChangeLetterNotificationService;

    @Mock
    private SolicitorRemovedRespondentLetterHandler solicitorRemovedRespondentLetterHandler;
    @Mock
    private SolicitorAddedRespondentLetterHandler solicitorAddedRespondentLetterHandler;

    private CaseDetails caseDetails;
    private CaseDetails caseDetailsBefore;


    @Before
    public void setUpTest() {
        noticeOfChangeLetterNotificationService =
            new NocLetterNotificationService(Arrays.asList(solicitorAddedRespondentLetterHandler, solicitorRemovedRespondentLetterHandler));
    }

    @Test
    public void shouldCallLetterHandlersCorrectly() {

        caseDetails = caseDetailsFromResource("/fixtures/noticeOfChange/contested/noc/noc-letter-notifications-add-and-revoke.json",
            new ObjectMapper());
        caseDetailsBefore =
            caseDetailsFromResource("/fixtures/noticeOfChange/contested/noc/noc-letter-notifications-add-and-revoke-before.json",
                new ObjectMapper());

        noticeOfChangeLetterNotificationService.sendNoticeOfChangeLetters(caseDetails, caseDetailsBefore, AUTH_TOKEN);

        verify(solicitorRemovedRespondentLetterHandler).handle(caseDetails, caseDetailsBefore, AUTH_TOKEN);
        verify(solicitorAddedRespondentLetterHandler).handle(caseDetails, caseDetailsBefore, AUTH_TOKEN);
    }


}