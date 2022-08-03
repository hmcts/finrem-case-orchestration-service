package uk.gov.hmcts.reform.finrem.caseorchestration.integrationtest.noc;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.ResourceUtils;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.BaseControllerTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.NotificationsController;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.NocLetterNotificationService;
import uk.gov.hmcts.reform.finrem.ccd.domain.Document;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@WebMvcTest(NotificationsController.class)
@ContextConfiguration(classes = {NocTestConfig.class, DocumentConfiguration.class})
public class NocLettersNotificationsControllerTest extends BaseControllerTest {

    private static final String DIGITAL_CALLBACK
        = "/fixtures/noticeOfChange/contested/noc/noc-letter-notifications-add-and-revoke.json";
    private static final String NON_DIGITAL_CALLBACK
        = "/fixtures/noticeOfChange/contested/noc/noc-letter-notifications-add-and-revoke-non-digital.json";

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
    @Captor
    ArgumentCaptor<FinremCaseDetails> finremCaseDetailsArgumentCaptor;

    @Test
    public void shouldCallNotificationsServiceCorrectly() throws IOException {

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

        notificationsController.sendNoticeOfChangeNotifications("authToken", getCallbackRequestString(DIGITAL_CALLBACK));

        verify(notificationService).sendNoticeOfChangeEmail(finremCaseDetailsArgumentCaptor.capture());
        verify(genericDocumentServiceMock).generateDocumentFromPlaceholdersMap(eq("authToken"), placeholdersMapArgumentCaptor.capture(),
            eq(documentConfiguration.getNocLetterNotificationLitigantSolicitorAddedTemplate()),
            eq(documentConfiguration.getNocLetterNotificationLitigantSolicitorAddedFileName()));

        Map letterAddedDetailsMap = placeholdersMapArgumentCaptor.getValue();

        assertNotificationLetterDetails(letterAddedDetailsMap);

        verify(genericDocumentServiceMock).generateDocumentFromPlaceholdersMap(eq("authToken"), placeholdersMapArgumentCaptor.capture(),
            eq(documentConfiguration.getNocLetterNotificationLitigantSolicitorRevokedTemplate()),
            eq(documentConfiguration.getNocLetterNotificationLitigantSolicitorRevokedFileName()));

        verify(bulkPrintService).sendDocumentForPrint(eq(litigantSolicitorAddedCaseDocument), finremCaseDetailsArgumentCaptor.capture());
        verify(bulkPrintService).sendDocumentForPrint(eq(litigantSolicitorRemovedCaseDocument), finremCaseDetailsArgumentCaptor.capture());

    }

    @Test
    public void shouldCallNotificationServiceCorrectlyNonDigitalSolicitorRemoved() throws IOException {
        Document litigantSolicitorAddedCaseDocument = Document.builder().filename("docFileNameAdded").build();
        when(genericDocumentServiceMock.generateDocumentFromPlaceholdersMap(anyString(), anyMap(),
            eq(documentConfiguration.getNocLetterNotificationLitigantSolicitorAddedTemplate()),
            eq(documentConfiguration.getNocLetterNotificationLitigantSolicitorAddedFileName()))).thenReturn(
            litigantSolicitorAddedCaseDocument);

        notificationsController.sendNoticeOfChangeNotifications("authToken", getCallbackRequestString(NON_DIGITAL_CALLBACK));

        verify(notificationService).sendNoticeOfChangeEmail(finremCaseDetailsArgumentCaptor.capture());
        verify(genericDocumentServiceMock).generateDocumentFromPlaceholdersMap(eq("authToken"), placeholdersMapArgumentCaptor.capture(),
            eq(documentConfiguration.getNocLetterNotificationLitigantSolicitorAddedTemplate()),
            eq(documentConfiguration.getNocLetterNotificationLitigantSolicitorAddedFileName()));

        Map letterAddedDetailsMap = placeholdersMapArgumentCaptor.getValue();

        assertNotificationLetterDetails(letterAddedDetailsMap);

        verify(bulkPrintService).sendDocumentForPrint(eq(litigantSolicitorAddedCaseDocument), finremCaseDetailsArgumentCaptor.capture());
    }

    private void assertNotificationLetterDetails(Map letterAddedDetailsMap) {
        Map caseDetails = (Map) letterAddedDetailsMap.get("caseDetails");
        Map caseData = (Map) caseDetails.get("case_data");
        String caseNumber = caseData.get("caseNumber").toString();
        assertThat(caseNumber, is("123"));
        String applicantName = caseData.get("applicantName").toString();
        assertThat(applicantName, is("Poor Guy"));

    }

    private String getCallbackRequestString(String filePath) throws IOException {
        File file = ResourceUtils.getFile(this.getClass().getResource(filePath));
        return new String(Files.readAllBytes(file.toPath()));
    }
}
