package uk.gov.hmcts.reform.finrem.caseorchestration.integrationtest.noc;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.BaseControllerTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.NotificationsController;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.CourtDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.notificationrequest.DraftOrdersNotificationRequestMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.notificationrequest.ManageHearingsNotificationRequestMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.address.ApplicantLetterAddresseeGenerator;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.address.IntervenerFourLetterAddresseeGenerator;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.address.IntervenerOneLetterAddresseeGenerator;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.address.IntervenerThreeLetterAddresseeGenerator;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.address.IntervenerTwoLetterAddresseeGenerator;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.address.LetterAddresseeGeneratorMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.address.RespondentLetterAddresseeGenerator;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.service.EmailService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AdditionalHearingDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignCaseAccessService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignedToJudgeDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderApprovedDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderNotApprovedDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.DocumentOrderingService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralEmailService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.HearingDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.HelpWithFeesDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.InternationalPostalService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PaperNotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.TransferCourtService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.managehearing.ManageHearingsCorresponder;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.updatefrc.UpdateFrcCorrespondenceService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.express.ExpressCaseService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.NocLetterNotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.nocworkflows.NoticeOfChangeService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.nocworkflows.UpdateRepresentationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.nocworkflows.UpdateRepresentationWorkflowService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.updatefrc.service.UpdateFrcInfoRespondentDocumentService;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDetailsFromResource;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT;

@RunWith(SpringRunner.class)
@WebMvcTest(NotificationsController.class)
@ContextConfiguration(classes = {NocTestConfig.class, DocumentConfiguration.class, FinremCaseDetailsMapper.class,
    LetterAddresseeGeneratorMapper.class, ApplicantLetterAddresseeGenerator.class,
    RespondentLetterAddresseeGenerator.class, IntervenerOneLetterAddresseeGenerator.class, IntervenerTwoLetterAddresseeGenerator.class,
    IntervenerThreeLetterAddresseeGenerator.class, IntervenerFourLetterAddresseeGenerator.class,
    InternationalPostalService.class, ManageHearingsNotificationRequestMapper.class})
public class NocLettersNotificationsControllerTest extends BaseControllerTest {

    @Autowired
    private NocLetterNotificationService nocLetterNotificationService;
    @Autowired
    private NotificationsController notificationsController;
    @MockitoBean
    private NotificationService notificationService;
    @MockitoBean
    private BulkPrintService bulkPrintService;
    @MockitoBean
    GenericDocumentService genericDocumentServiceMock;
    @MockitoBean
    private AssignCaseAccessService assignCaseAccessService;
    @MockitoBean
    private AssignedToJudgeDocumentService assignedToJudgeDocumentService;
    @MockitoBean
    private ConsentOrderApprovedDocumentService consentOrderApprovedDocumentService;
    @MockitoBean
    private ConsentOrderNotApprovedDocumentService consentOrderNotApprovedDocumentService;
    @MockitoBean
    private ConsentOrderPrintService consentOrderPrintService;
    @MockitoBean
    private DocumentOrderingService documentOrderingService;
    @Autowired
    private DocumentConfiguration documentConfiguration;
    @MockitoBean
    private EmailService emailService;
    @MockitoBean
    private CourtDetailsMapper courtDetailsMapper;
    @MockitoBean
    private InternationalPostalService postalService;
    @MockitoBean
    private PaperNotificationService paperNotificationService;
    @MockitoBean
    private GeneralEmailService generalEmailService;
    @MockitoBean
    private HelpWithFeesDocumentService helpWithFeesDocumentService;
    @MockitoBean
    private HearingDocumentService hearingDocumentService;
    @MockitoBean
    private AdditionalHearingDocumentService additionalHearingDocumentService;
    @MockitoBean
    private TransferCourtService transferCourtService;
    @MockitoBean
    private FeatureToggleService featureToggleService;
    @MockitoBean
    private NoticeOfChangeService noticeOfChangeService;
    @MockitoBean
    private UpdateRepresentationWorkflowService updateRepresentationWorkflowService;
    @MockitoBean
    private UpdateRepresentationService updateRepresentationService;
    @MockitoBean
    UpdateFrcCorrespondenceService updateFrcCorrespondenceService;
    @MockitoBean
    UpdateFrcInfoRespondentDocumentService updateFrcInfoRespondentDocumentService;
    @MockitoBean
    DraftOrdersNotificationRequestMapper draftOrdersNotificationRequestMapper;
    @MockitoBean
    ManageHearingsCorresponder manageHearingsCorresponder;
    @MockitoBean
    ExpressCaseService expressCaseService;

    @Captor
    ArgumentCaptor<Map> placeholdersMapArgumentCaptor;

    private CaseDetails caseDetails;

    private CaseDetails caseDetailsBefore;

    @Test
    public void shouldCallNotificationsServiceCorrectly() {
        when(bulkPrintService.getRecipient(DocumentHelper.PaperNotificationRecipient.APPLICANT.toString())).thenReturn(APPLICANT);
        CaseDocument litigantSolicitorAddedCaseDocument = CaseDocument.builder().documentFilename("docFileNameAdded").build();
        when(genericDocumentServiceMock.generateDocumentFromPlaceholdersMap(anyString(), anyMap(),
            eq(documentConfiguration.getNocLetterNotificationLitigantSolicitorAddedTemplate()),
            eq(documentConfiguration.getNocLetterNotificationLitigantSolicitorAddedFileName()), eq(CASE_ID))).thenReturn(
            litigantSolicitorAddedCaseDocument);

        CaseDocument litigantSolicitorRemovedCaseDocument = CaseDocument.builder().documentFilename("docFileNameRemoved").build();
        when(genericDocumentServiceMock.generateDocumentFromPlaceholdersMap(anyString(), anyMap(),
            eq(documentConfiguration.getNocLetterNotificationLitigantSolicitorRevokedTemplate()),
            eq(documentConfiguration.getNocLetterNotificationLitigantSolicitorRevokedFileName()), eq(CASE_ID))).thenReturn(
            litigantSolicitorRemovedCaseDocument);

        notificationsController.sendNoticeOfChangeNotifications(AUTH_TOKEN, buildCallbackRequest());

        verify(notificationService).sendNoticeOfChangeEmail(caseDetails);
        verify(genericDocumentServiceMock).generateDocumentFromPlaceholdersMap(eq(AUTH_TOKEN), placeholdersMapArgumentCaptor.capture(),
            eq(documentConfiguration.getNocLetterNotificationLitigantSolicitorAddedTemplate()),
            eq(documentConfiguration.getNocLetterNotificationLitigantSolicitorAddedFileName()), eq(CASE_ID));

        Map letterAddedDetailsMap = placeholdersMapArgumentCaptor.getValue();

        assertNotificationLetterDetails(letterAddedDetailsMap);

        verify(genericDocumentServiceMock).generateDocumentFromPlaceholdersMap(eq(AUTH_TOKEN), placeholdersMapArgumentCaptor.capture(),
            eq(documentConfiguration.getNocLetterNotificationLitigantSolicitorRevokedTemplate()),
            eq(documentConfiguration.getNocLetterNotificationLitigantSolicitorRevokedFileName()), eq(CASE_ID));

        verify(bulkPrintService).sendDocumentForPrint(litigantSolicitorAddedCaseDocument, caseDetails, APPLICANT, AUTH_TOKEN);
        verify(bulkPrintService).sendDocumentForPrint(litigantSolicitorRemovedCaseDocument, caseDetails, APPLICANT, AUTH_TOKEN);
        verify(postalService).isApplicantResideOutsideOfUK(caseDetails.getData());
    }

    @Test
    public void shouldCallNotificationServiceCorrectlyNonDigitalSolicitorRemoved() {
        CaseDocument litigantSolicitorAddedCaseDocument = CaseDocument.builder().documentFilename("docFileNameAdded").build();
        when(bulkPrintService.getRecipient(DocumentHelper.PaperNotificationRecipient.APPLICANT.toString())).thenReturn(APPLICANT);
        when(genericDocumentServiceMock.generateDocumentFromPlaceholdersMap(anyString(), anyMap(),
            eq(documentConfiguration.getNocLetterNotificationLitigantSolicitorAddedTemplate()),
            eq(documentConfiguration.getNocLetterNotificationLitigantSolicitorAddedFileName()), eq(CASE_ID))).thenReturn(
            litigantSolicitorAddedCaseDocument);

        notificationsController.sendNoticeOfChangeNotifications(AUTH_TOKEN, buildNonDigitalCallbackRequest());

        verify(notificationService).sendNoticeOfChangeEmail(caseDetails);
        verify(genericDocumentServiceMock).generateDocumentFromPlaceholdersMap(eq(AUTH_TOKEN), placeholdersMapArgumentCaptor.capture(),
            eq(documentConfiguration.getNocLetterNotificationLitigantSolicitorAddedTemplate()),
            eq(documentConfiguration.getNocLetterNotificationLitigantSolicitorAddedFileName()), eq(CASE_ID));

        Map letterAddedDetailsMap = placeholdersMapArgumentCaptor.getValue();

        assertNotificationLetterDetails(letterAddedDetailsMap);

        verify(bulkPrintService).sendDocumentForPrint(litigantSolicitorAddedCaseDocument, caseDetails, APPLICANT, AUTH_TOKEN);
    }

    @Override
    protected CallbackRequest buildCallbackRequest() {
        caseDetails = caseDetailsFromResource("/fixtures/noticeOfChange/contested/noc/noc-letter-notifications-add-and-revoke.json",
            new ObjectMapper());
        caseDetailsBefore =
            caseDetailsFromResource("/fixtures/noticeOfChange/contested/noc/noc-letter-notifications-add-and-revoke-before.json",
                new ObjectMapper());
        return CallbackRequest.builder().caseDetails(caseDetails).caseDetailsBefore(caseDetailsBefore).build();
    }

    protected CallbackRequest buildNonDigitalCallbackRequest() {
        caseDetails = caseDetailsFromResource("/fixtures/noticeOfChange/contested/noc/"
                + "noc-letter-notifications-add-and-revoke-non-digital.json",
            new ObjectMapper());
        caseDetailsBefore =
            caseDetailsFromResource(
                "/fixtures/noticeOfChange/contested/noc/noc-letter-notifications-add-and-revoke-non-digital-before.json",
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
