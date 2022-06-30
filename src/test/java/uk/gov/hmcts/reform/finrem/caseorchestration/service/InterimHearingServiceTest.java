package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.InterimHearingHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimHearingBulkPrintDocumentsData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimHearingData;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_ADDITIONAL_INFO;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_BEDFORDSHIRE_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_BIRMINGHAM_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_BRISTOL_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_CFC_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_CLEAVELAND_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_DATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_DEVON_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_DOCUMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_DORSET_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_HUMBER_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_KENT_SURREY_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_LANCASHIRE_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_LIVERPOOL_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_LONDON_FRC_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_MANCHESTER_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_MIDLANDS_FRC_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_NEWPORT_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_NORTHEAST_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_NORTHWALES_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_NORTHWEST_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_NOTTINGHAM_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_NWYORKSHIRE_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_PROMPT_FOR_DOCUMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_REGION_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_SOUTHEAST_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_SOUTHWEST_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_SWANSEA_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_THAMESVALLEY_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_TIME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_TIME_ESTIMATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_TYPE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_UPLOADED_DOCUMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_WALES_FRC_COURT_LIST;

public class InterimHearingServiceTest extends BaseServiceTest  {

    @Autowired
    private InterimHearingService interimHearingService;
    @MockBean
    private BulkPrintService bulkPrintService;
    @MockBean
    private GenericDocumentService genericDocumentService;
    @MockBean
    private NotificationService notificationService;
    @MockBean
    private CaseDataService caseDataService;
    @Autowired
    private InterimHearingHelper interimHearingHelper;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String AUTH_TOKEN = "tokien:)";
    private static final String BEFORE_MIGRATION_TEST_JSON =
        "/fixtures/contested/interim-hearing-one-collection.json";
    private static final String MODIFIED_DURING_MIGRATION_TEST_JSON =
        "/fixtures/contested/interim-hearing-one-modified-collection.json";
    private static final String ONE_MIGRATED_AND_ONE_ADDED_HEARING_JSON =
        "/fixtures/contested/interim-hearing-two-collection.json";
    private static final String ONE_MIGRATED_MODIFIED_AND_ONE_ADDED_HEARING_JSON =
        "/fixtures/contested/interim-hearing-two-collection-modified.json";
    private static final String TEST_NEW_JSON = "/fixtures/contested/interim-hearing-three-collection-no-track.json";



    @Test
    public void givenContestedPaperCaseWithBeforeMigrationToHearingCollection_WhenModifiedDuringMigration_ThenItShouldSendToBulkPrint() {
        CaseDetails caseDetails = buildCaseDetails(BEFORE_MIGRATION_TEST_JSON);
        CaseDetails caseDetailsBefore = buildCaseDetails(MODIFIED_DURING_MIGRATION_TEST_JSON);

        when(genericDocumentService.generateDocument(any(), any(), any(), any())).thenReturn(caseDocument());
        when(genericDocumentService.convertDocumentIfNotPdfAlready(any(), any())).thenReturn(caseDocument());

        interimHearingService.submitInterimHearing(caseDetails,caseDetailsBefore, AUTH_TOKEN);

        verify(bulkPrintService).printApplicantDocuments(any(), any(), any());
        verify(bulkPrintService).printRespondentDocuments(any(), any(), any());

        Map<String, Object> caseData = caseDetails.getData();
        List<InterimHearingData> interimHearingList = interimHearingHelper.isThereAnExistingInterimHearing(caseData);

        assertEquals("2000-10-10", interimHearingList.get(0).getValue().getInterimHearingDate());
        assertEquals("15:00", interimHearingList.get(0).getValue().getInterimHearingTime());

        verify(bulkPrintService).printApplicantDocuments(any(), any(), any());
        verify(bulkPrintService).printRespondentDocuments(any(), any(), any());

        verifyNonCollectionData(caseData);
        assertEquals(1, interimHearingList.size());
    }

    @Test
    public void givenContestedPaperCaseWithTwoHearing_WhenExistingHearingModified_ThenItShouldSendBothToBulkPrint() {
        CaseDetails caseDetails = buildCaseDetails(ONE_MIGRATED_AND_ONE_ADDED_HEARING_JSON);
        CaseDetails caseDetailsBefore = buildCaseDetails(ONE_MIGRATED_MODIFIED_AND_ONE_ADDED_HEARING_JSON);

        when(genericDocumentService.generateDocument(any(), any(), any(), any())).thenReturn(caseDocument());
        when(genericDocumentService.convertDocumentIfNotPdfAlready(any(), any())).thenReturn(caseDocument());

        interimHearingService.submitInterimHearing(caseDetails,caseDetailsBefore, AUTH_TOKEN);

        verify(bulkPrintService).printApplicantDocuments(any(), any(), any());
        verify(bulkPrintService).printRespondentDocuments(any(), any(), any());

        Map<String, Object> caseData = caseDetails.getData();
        List<InterimHearingData> interimHearingList = interimHearingHelper.isThereAnExistingInterimHearing(caseData);

        assertEquals("2000-10-10", interimHearingList.get(0).getValue().getInterimHearingDate());
        assertEquals("2040-10-10", interimHearingList.get(1).getValue().getInterimHearingDate());

        verify(bulkPrintService).printApplicantDocuments(any(), any(), any());
        verify(bulkPrintService).printRespondentDocuments(any(), any(), any());

        verifyNonCollectionData(caseData);
        assertEquals(2, interimHearingList.size());
    }

    @Test
    public void givenContestedPaperCase_WhenOneNewHearingAddedToExistingCase_ThenItShouldSendOnlyNewHEaringDetailsToBulkPrint() {
        CaseDetails caseDetails = buildCaseDetails(ONE_MIGRATED_AND_ONE_ADDED_HEARING_JSON);
        CaseDetails caseDetailsBefore = buildCaseDetails(ONE_MIGRATED_AND_ONE_ADDED_HEARING_JSON);

        when(genericDocumentService.generateDocument(any(), any(), any(), any())).thenReturn(caseDocument());
        when(genericDocumentService.convertDocumentIfNotPdfAlready(any(), any())).thenReturn(caseDocument());

        interimHearingService.submitInterimHearing(caseDetails,caseDetailsBefore, AUTH_TOKEN);

        verify(bulkPrintService).printApplicantDocuments(any(), any(), any());
        verify(bulkPrintService).printRespondentDocuments(any(), any(), any());

        Map<String, Object> caseData = caseDetails.getData();
        List<InterimHearingData> interimHearingList = interimHearingHelper.isThereAnExistingInterimHearing(caseData);

        assertEquals("2000-10-10", interimHearingList.get(0).getValue().getInterimHearingDate());
        assertEquals("2040-10-10", interimHearingList.get(1).getValue().getInterimHearingDate());
        verifyNonCollectionData(caseData);
        assertEquals(2, interimHearingList.size());
    }

    @Test
    public void givenContestedPaperCase_WhenMultipleHearingAdded_ThenItShouldSendAllToBulkPrint() {
        CaseDetails caseDetails = buildCaseDetails(TEST_NEW_JSON);
        CaseDetails caseDetailsBefore = buildCaseDetails(TEST_NEW_JSON);

        when(genericDocumentService.generateDocument(any(), any(), any(), any())).thenReturn(caseDocument());
        when(genericDocumentService.convertDocumentIfNotPdfAlready(any(), any())).thenReturn(caseDocument());

        interimHearingService.submitInterimHearing(caseDetails, caseDetailsBefore, AUTH_TOKEN);

        verify(bulkPrintService).printApplicantDocuments(any(), any(), any());
        verify(bulkPrintService).printRespondentDocuments(any(), any(), any());

        List<InterimHearingData> interimHearingList = interimHearingHelper.isThereAnExistingInterimHearing(caseDetails.getData());

        assertEquals("2010-10-10", interimHearingList.get(0).getValue().getInterimHearingDate());
        assertEquals("2020-10-10", interimHearingList.get(1).getValue().getInterimHearingDate());
        assertEquals("2030-10-10", interimHearingList.get(2).getValue().getInterimHearingDate());

        List<InterimHearingBulkPrintDocumentsData> bulkPrintDocumentsList =
            interimHearingHelper.getInterimHearingBulkPrintDocumentList(caseDetails.getData());

        assertEquals(3, bulkPrintDocumentsList.size());
    }

    @Test
    public void givenContestedMultipleHearing_WhenNoConsentToEmail_ThenItShouldSendAllToBulkPrint() {
        CaseDetails caseDetails = buildCaseDetails(TEST_NEW_JSON);

        caseDetails.getData().put("paperApplication", "No");
        caseDetails.getData().put("applicantSolicitorConsentForEmails", "No");
        caseDetails.getData().put("respondentRepresented", "No");

        when(genericDocumentService.generateDocument(any(), any(), any(), any())).thenReturn(caseDocument());
        when(genericDocumentService.convertDocumentIfNotPdfAlready(any(), any())).thenReturn(caseDocument());

        CaseDetails caseDetailsBefore = buildCaseDetails(TEST_NEW_JSON);
        interimHearingService.submitInterimHearing(caseDetails, caseDetailsBefore, AUTH_TOKEN);

        verify(bulkPrintService).printApplicantDocuments(any(), any(), any());
        verify(bulkPrintService).printRespondentDocuments(any(), any(), any());

        List<InterimHearingData> interimHearingList = interimHearingHelper.isThereAnExistingInterimHearing(caseDetails.getData());

        assertEquals("2010-10-10", interimHearingList.get(0).getValue().getInterimHearingDate());
        assertEquals("2020-10-10", interimHearingList.get(1).getValue().getInterimHearingDate());
        assertEquals("2030-10-10", interimHearingList.get(2).getValue().getInterimHearingDate());
    }


    @Test
    public void givenContestedPaperCase_WhenPaperCase_ThenItShouldNotSendNotificaton() {
        CaseDetails caseDetails = buildCaseDetails(TEST_NEW_JSON);
        CaseDetails caseDetailsBefore = buildCaseDetails(TEST_NEW_JSON);

        caseDetails.getData().put("paperApplication", "Yes");

        when(caseDataService.isPaperApplication(any())).thenReturn(true);

        interimHearingService.sendNotification(caseDetails, caseDetailsBefore);

        verify(caseDataService).isPaperApplication(any());
        verify(caseDataService, never()).isApplicantSolicitorAgreeToReceiveEmails(any());

        verify(notificationService, never()).shouldEmailRespondentSolicitor(any());
        verify(notificationService, never()).sendInterimHearingNotificationEmailToApplicantSolicitor(any(), anyMap());
        verify(notificationService, never()).sendInterimHearingNotificationEmailToRespondentSolicitor(any(), anyMap());
    }

    @Test
    public void givenContestedNotPaperCase_WhenPaperCase_ThenItShouldSendNotificaton() {
        CaseDetails caseDetails = buildCaseDetails(TEST_NEW_JSON);
        caseDetails.getData().put("paperApplication", "No");

        when(caseDataService.isPaperApplication(any())).thenReturn(false);
        when(caseDataService.isApplicantSolicitorAgreeToReceiveEmails(any())).thenReturn(true);
        when(notificationService.shouldEmailRespondentSolicitor(any())).thenReturn(true);

        CaseDetails caseDetailsBefore = buildCaseDetails(TEST_NEW_JSON);
        interimHearingService.sendNotification(caseDetails, caseDetailsBefore);

        verify(caseDataService).isPaperApplication(any());

        verify(caseDataService, times(3)).isApplicantSolicitorAgreeToReceiveEmails(any());
        verify(notificationService, times(3)).shouldEmailRespondentSolicitor(any());
        verify(notificationService, times(3)).sendInterimHearingNotificationEmailToApplicantSolicitor(any(), anyMap());
        verify(notificationService,times(3)).sendInterimHearingNotificationEmailToRespondentSolicitor(any(), anyMap());
    }

    private CaseDetails buildCaseDetails(String path)  {
        try (InputStream resourceAsStream = getClass().getResourceAsStream(path)) {
            return objectMapper.readValue(resourceAsStream, CallbackRequest.class).getCaseDetails();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private CaseDetails buildCaseDetailsBefore(String path)  {
        try (InputStream resourceAsStream = getClass().getResourceAsStream(path)) {
            return objectMapper.readValue(resourceAsStream, CallbackRequest.class).getCaseDetailsBefore();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void verifyNonCollectionData(Map<String, Object> data) {
        assertNull(data.get(INTERIM_HEARING_TYPE));
        assertNull(data.get(INTERIM_HEARING_DATE));
        assertNull(data.get(INTERIM_HEARING_TIME));
        assertNull(data.get(INTERIM_HEARING_TIME_ESTIMATE));
        assertNull(data.get(INTERIM_HEARING_REGION_LIST));
        assertNull(data.get(INTERIM_HEARING_CFC_COURT_LIST));
        assertNull(data.get(INTERIM_HEARING_WALES_FRC_COURT_LIST));
        assertNull(data.get(INTERIM_HEARING_LONDON_FRC_COURT_LIST));
        assertNull(data.get(INTERIM_HEARING_DEVON_COURT_LIST));
        assertNull(data.get(INTERIM_HEARING_DORSET_COURT_LIST));
        assertNull(data.get(INTERIM_HEARING_HUMBER_COURT_LIST));
        assertNull(data.get(INTERIM_HEARING_MIDLANDS_FRC_COURT_LIST));
        assertNull(data.get(INTERIM_HEARING_BRISTOL_COURT_LIST));
        assertNull(data.get(INTERIM_HEARING_NEWPORT_COURT_LIST));
        assertNull(data.get(INTERIM_HEARING_NORTHEAST_COURT_LIST));
        assertNull(data.get(INTERIM_HEARING_NORTHWEST_COURT_LIST));
        assertNull(data.get(INTERIM_HEARING_SOUTHEAST_COURT_LIST));
        assertNull(data.get(INTERIM_HEARING_SOUTHWEST_COURT_LIST));
        assertNull(data.get(INTERIM_HEARING_SWANSEA_COURT_LIST));
        assertNull(data.get(INTERIM_HEARING_LIVERPOOL_COURT_LIST));
        assertNull(data.get(INTERIM_HEARING_BIRMINGHAM_COURT_LIST));
        assertNull(data.get(INTERIM_HEARING_CLEAVELAND_COURT_LIST));
        assertNull(data.get(INTERIM_HEARING_KENT_SURREY_COURT_LIST));
        assertNull(data.get(INTERIM_HEARING_LANCASHIRE_COURT_LIST));
        assertNull(data.get(INTERIM_HEARING_MANCHESTER_COURT_LIST));
        assertNull(data.get(INTERIM_HEARING_NORTHWALES_COURT_LIST));
        assertNull(data.get(INTERIM_HEARING_NOTTINGHAM_COURT_LIST));
        assertNull(data.get(INTERIM_HEARING_NWYORKSHIRE_COURT_LIST));
        assertNull(data.get(INTERIM_HEARING_BEDFORDSHIRE_COURT_LIST));
        assertNull(data.get(INTERIM_HEARING_THAMESVALLEY_COURT_LIST));
        assertNull(data.get(INTERIM_HEARING_ADDITIONAL_INFO));
        assertNull(data.get(INTERIM_HEARING_PROMPT_FOR_DOCUMENT));
        assertNull(data.get(INTERIM_HEARING_UPLOADED_DOCUMENT));
        assertNull(data.get(INTERIM_HEARING_DOCUMENT));
    }
}