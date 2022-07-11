package uk.gov.hmcts.reform.finrem.caseorchestration.integrationtest.noc;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.BaseControllerTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.NotificationsController;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.NocLetterNotificationService;
import uk.gov.hmcts.reform.finrem.ccd.callback.CallbackRequest;
import uk.gov.hmcts.reform.finrem.ccd.domain.Document;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.finremCaseDetailsFromResource;

@RunWith(SpringRunner.class)
@WebMvcTest(NotificationsController.class)
@ContextConfiguration(classes = {NocTestConfig.class, DocumentConfiguration.class})
public class NocLettersNotificationsControllerTest extends BaseControllerTest {

    @Autowired
    private NocLetterNotificationService nocLetterNotificationService;
    @Autowired
    private NotificationsController notificationsController;
    @MockBean
    private NotificationService notificationService;
    @MockBean
    private BulkPrintService bulkPrintService;
    @MockBean
    GenericDocumentService genericDocumentServiceMock;
    @Autowired
    private DocumentConfiguration documentConfiguration;
    @Captor
    ArgumentCaptor<Map> placeholdersMapArgumentCaptor;

    private FinremCaseDetails caseDetails;
    private FinremCaseDetails caseDetailsBefore;


    @Test
    public void shouldCallNotificationsServiceCorrectly() {

        Document litigantSolicitorAddedCaseDocument = Document.builder().filename("docFileNameAdded").build();
        when(genericDocumentServiceMock.generateDocumentFromPlaceholdersMap(anyString(), anyMap(),
            eq(documentConfiguration.getNocLetterNotificationLitigantSolicitorAddedTemplate()),
            eq(documentConfiguration.getNocLetterNotificationLitigantSolicitorAddedFileName()))).thenReturn(
            litigantSolicitorAddedCaseDocument);

        Document litigantSolicitorRemovedCaseDocument = Document.builder().filename("docFileNameRemoved").build();
        when(genericDocumentServiceMock.generateDocumentFromPlaceholdersMap(anyString(), anyMap(),
            eq(documentConfiguration.getNocLetterNotificationLitigantSolicitorRevokedTemplate()),
            eq(documentConfiguration.getNocLetterNotificationLitigantSolicitorRevokedFileName()))).thenReturn(
            litigantSolicitorRemovedCaseDocument);

        notificationsController.sendNoticeOfChangeNotifications("authToken", buildLatestCallbackRequest());

        verify(notificationService).sendNoticeOfChangeEmail(caseDetails);
        verify(genericDocumentServiceMock).generateDocumentFromPlaceholdersMap(eq("authToken"), placeholdersMapArgumentCaptor.capture(),
            eq(documentConfiguration.getNocLetterNotificationLitigantSolicitorAddedTemplate()),
            eq(documentConfiguration.getNocLetterNotificationLitigantSolicitorAddedFileName()));

        Map letterAddedDetailsMap = placeholdersMapArgumentCaptor.getValue();

        assertNotificationLetterDetails(letterAddedDetailsMap);

        verify(genericDocumentServiceMock).generateDocumentFromPlaceholdersMap(eq("authToken"), placeholdersMapArgumentCaptor.capture(),
            eq(documentConfiguration.getNocLetterNotificationLitigantSolicitorRevokedTemplate()),
            eq(documentConfiguration.getNocLetterNotificationLitigantSolicitorRevokedFileName()));

        verify(bulkPrintService).sendDocumentForPrint(litigantSolicitorAddedCaseDocument, caseDetails);
        verify(bulkPrintService).sendDocumentForPrint(litigantSolicitorRemovedCaseDocument, caseDetails);

    }

    @Test
    public void shouldCallNotificationServiceCorrectlyNonDigitalSolicitorRemoved() {
        CaseDocument litigantSolicitorAddedCaseDocument = CaseDocument.builder().documentFilename("docFileNameAdded").build();
        when(genericDocumentServiceMock.generateDocumentFromPlaceholdersMap(anyString(), anyMap(),
            eq(documentConfiguration.getNocLetterNotificationLitigantSolicitorAddedTemplate()),
            eq(documentConfiguration.getNocLetterNotificationLitigantSolicitorAddedFileName()))).thenReturn(
            litigantSolicitorAddedCaseDocument);

        notificationsController.sendNoticeOfChangeNotifications("authToken", buildNonDigitalCallbackRequest());

        verify(notificationService).sendNoticeOfChangeEmail(caseDetails);
        verify(genericDocumentServiceMock).generateDocumentFromPlaceholdersMap(eq("authToken"), placeholdersMapArgumentCaptor.capture(),
            eq(documentConfiguration.getNocLetterNotificationLitigantSolicitorAddedTemplate()),
            eq(documentConfiguration.getNocLetterNotificationLitigantSolicitorAddedFileName()));

        Map letterAddedDetailsMap = placeholdersMapArgumentCaptor.getValue();

        assertNotificationLetterDetails(letterAddedDetailsMap);

        verify(bulkPrintService).sendDocumentForPrint(litigantSolicitorAddedCaseDocument, caseDetails);
    }

    protected CallbackRequest buildLatestCallbackRequest() {
        caseDetails = finremCaseDetailsFromResource("/fixtures/noticeOfChange/contested/noc/noc-letter-notifications-add-and-revoke.json",
            new ObjectMapper());
        caseDetailsBefore =
            finremCaseDetailsFromResource("/fixtures/noticeOfChange/contested/noc/noc-letter-notifications-add-and-revoke-before.json",
                new ObjectMapper());
        return CallbackRequest.builder().caseDetails(caseDetails).caseDetailsBefore(caseDetailsBefore).build();
    }

    protected CallbackRequest buildNonDigitalCallbackRequest() {
        caseDetails = finremCaseDetailsFromResource("/fixtures/noticeOfChange/contested/noc/"
                + "noc-letter-notifications-add-and-revoke-non-digital.json",
            new ObjectMapper());
        caseDetailsBefore =
            finremCaseDetailsFromResource("/fixtures/noticeOfChange/contested/noc/noc-letter-notifications"
                    + "-add-and-revoke-non-digital-before.json",
                new ObjectMapper());
        return CallbackRequest.builder().caseDetails(caseDetails).caseDetailsBefore(caseDetailsBefore).build();
    }


    private void assertNotificationLetterDetails(Map letterAddedDetailsMap) {
        Map caseDetails = (Map) letterAddedDetailsMap.get("caseDetails");
        Map caseData = (Map) caseDetails.get("case_data");
        String caseNumber = caseData.get("caseNumber").toString();
        assertThat(caseNumber, is(this.caseDetails.getId().toString()));
        String applicantName = caseData.get("applicantName").toString();
        assertThat(applicantName, is("Poor Guy"));

    }
}
