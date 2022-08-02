package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.ccd.domain.Document;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.InterimHearingBulkPrintDocumentsData;
import uk.gov.hmcts.reform.finrem.ccd.domain.InterimHearingCollection;
import uk.gov.hmcts.reform.finrem.ccd.domain.YesOrNo;
import uk.gov.hmcts.reform.finrem.ccd.domain.wrapper.InterimRegionWrapper;
import uk.gov.hmcts.reform.finrem.ccd.domain.wrapper.InterimWrapper;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.finremCaseDetailsFromResource;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.newDocument;

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
    public void givenContestedPaperCaseWithBeforeMigrationToHearingCollection_WhenModifiedDuringMigration_ThenItShouldSendToBulkPrint() throws IOException {
        FinremCaseDetails caseDetails = finremCaseDetailsFromResource(getResource(BEFORE_MIGRATION_TEST_JSON), mapper);
        FinremCaseDetails caseDetailsBefore = finremCaseDetailsFromResource(getResource(MODIFIED_DURING_MIGRATION_TEST_JSON), mapper);

        when(genericDocumentService.generateDocumentFromPlaceholdersMap(any(), any(), any(), any())).thenReturn(newDocument());
        when(genericDocumentService.convertDocumentIfNotPdfAlready(isA(Document.class), any())).thenReturn(newDocument());

        interimHearingService.submitInterimHearing(caseDetails,caseDetailsBefore, AUTH_TOKEN);

        verify(bulkPrintService).printApplicantDocuments(any(), any(), any());
        verify(bulkPrintService).printRespondentDocuments(any(), any(), any());

        FinremCaseData caseData = caseDetails.getCaseData();
        List<InterimHearingCollection> interimHearingList = caseData.getInterimWrapper().getInterimHearings();

        assertEquals("2000-10-10", interimHearingList.get(0).getValue().getInterimHearingDate());
        assertEquals("15:00", interimHearingList.get(0).getValue().getInterimHearingTime());

        verify(bulkPrintService).printApplicantDocuments(any(), any(), any());
        verify(bulkPrintService).printRespondentDocuments(any(), any(), any());

        verifyNonCollectionData(caseData);
        assertEquals(1, interimHearingList.size());
    }

    @Test
    public void givenContestedPaperCaseWithTwoHearing_WhenExistingHearingModified_ThenItShouldSendBothToBulkPrint() throws IOException {
        FinremCaseDetails caseDetails = finremCaseDetailsFromResource(getResource(ONE_MIGRATED_AND_ONE_ADDED_HEARING_JSON), mapper);
        FinremCaseDetails caseDetailsBefore = finremCaseDetailsFromResource(getResource(ONE_MIGRATED_MODIFIED_AND_ONE_ADDED_HEARING_JSON), mapper);

        when(genericDocumentService.generateDocumentFromPlaceholdersMap(any(), any(), any(), any())).thenReturn(newDocument());
        when(genericDocumentService.convertDocumentIfNotPdfAlready(isA(Document.class), any())).thenReturn(newDocument());

        interimHearingService.submitInterimHearing(caseDetails,caseDetailsBefore, AUTH_TOKEN);

        verify(bulkPrintService).printApplicantDocuments(any(), any(), any());
        verify(bulkPrintService).printRespondentDocuments(any(), any(), any());

        FinremCaseData caseData = caseDetails.getCaseData();
        List<InterimHearingCollection> interimHearingList = caseData.getInterimWrapper().getInterimHearings();

        assertEquals("2000-10-10", interimHearingList.get(0).getValue().getInterimHearingDate());
        assertEquals("2040-10-10", interimHearingList.get(1).getValue().getInterimHearingDate());

        verify(bulkPrintService).printApplicantDocuments(any(), any(), any());
        verify(bulkPrintService).printRespondentDocuments(any(), any(), any());

        verifyNonCollectionData(caseData);
        assertEquals(2, interimHearingList.size());
    }

    @Test
    public void givenContestedPaperCase_WhenOneNewHearingAddedToExistingCase_ThenItShouldSendOnlyNewHEaringDetailsToBulkPrint() throws IOException {
        FinremCaseDetails caseDetails = finremCaseDetailsFromResource(getResource(ONE_MIGRATED_AND_ONE_ADDED_HEARING_JSON), mapper);
        FinremCaseDetails caseDetailsBefore = finremCaseDetailsFromResource(getResource(ONE_MIGRATED_AND_ONE_ADDED_HEARING_JSON), mapper);

        when(genericDocumentService.generateDocumentFromPlaceholdersMap(any(), any(), any(), any())).thenReturn(newDocument());
        when(genericDocumentService.convertDocumentIfNotPdfAlready(isA(Document.class), any())).thenReturn(newDocument());

        interimHearingService.submitInterimHearing(caseDetails,caseDetailsBefore, AUTH_TOKEN);

        verify(bulkPrintService).printApplicantDocuments(any(), any(), any());
        verify(bulkPrintService).printRespondentDocuments(any(), any(), any());

        FinremCaseData caseData = caseDetails.getCaseData();
        List<InterimHearingCollection> interimHearingList = caseData.getInterimWrapper().getInterimHearings();

        assertEquals("2000-10-10", interimHearingList.get(0).getValue().getInterimHearingDate());
        assertEquals("2040-10-10", interimHearingList.get(1).getValue().getInterimHearingDate());
        verifyNonCollectionData(caseData);
        assertEquals(2, interimHearingList.size());
    }

    @Test
    public void givenContestedPaperCase_WhenMultipleHearingAdded_ThenItShouldSendAllToBulkPrint() throws IOException {
        FinremCaseDetails caseDetails = finremCaseDetailsFromResource(getResource(TEST_NEW_JSON), mapper);
        FinremCaseDetails caseDetailsBefore = finremCaseDetailsFromResource(getResource(TEST_NEW_JSON), mapper);

        when(genericDocumentService.generateDocumentFromPlaceholdersMap(any(), any(), any(), any())).thenReturn(newDocument());
        when(genericDocumentService.convertDocumentIfNotPdfAlready(isA(Document.class), any())).thenReturn(newDocument());

        interimHearingService.submitInterimHearing(caseDetails, caseDetailsBefore, AUTH_TOKEN);

        verify(bulkPrintService).printApplicantDocuments(any(), any(), any());
        verify(bulkPrintService).printRespondentDocuments(any(), any(), any());

        List<InterimHearingCollection> interimHearingList = caseDetails.getCaseData().getInterimWrapper().getInterimHearings();

        assertEquals("2010-10-10", interimHearingList.get(0).getValue().getInterimHearingDate());
        assertEquals("2020-10-10", interimHearingList.get(1).getValue().getInterimHearingDate());
        assertEquals("2030-10-10", interimHearingList.get(2).getValue().getInterimHearingDate());

        List<InterimHearingBulkPrintDocumentsData> bulkPrintDocumentsList =
            caseDetails.getCaseData().getInterimWrapper().getInterimHearingDocuments();

        assertEquals(3, bulkPrintDocumentsList.size());
    }

    @Test
    public void givenContestedMultipleHearing_WhenNoConsentToEmail_ThenItShouldSendAllToBulkPrint() throws IOException {
        FinremCaseDetails caseDetails = finremCaseDetailsFromResource(getResource(TEST_NEW_JSON), mapper);

        caseDetails.getCaseData().setPaperApplication(YesOrNo.NO);
        caseDetails.getCaseData().getContactDetailsWrapper().setApplicantSolicitorConsentForEmails(YesOrNo.NO);
        caseDetails.getCaseData().getContactDetailsWrapper().setContestedRespondentRepresented(YesOrNo.NO);

        when(genericDocumentService.generateDocumentFromPlaceholdersMap(any(), any(), any(), any())).thenReturn(newDocument());
        when(genericDocumentService.convertDocumentIfNotPdfAlready(isA(Document.class), any())).thenReturn(newDocument());

        FinremCaseDetails caseDetailsBefore = finremCaseDetailsFromResource(getResource(TEST_NEW_JSON), mapper);
        interimHearingService.submitInterimHearing(caseDetails, caseDetailsBefore, AUTH_TOKEN);

        verify(bulkPrintService).printApplicantDocuments(any(), any(), any());
        verify(bulkPrintService).printRespondentDocuments(any(), any(), any());

        List<InterimHearingCollection> interimHearingList = caseDetails.getCaseData().getInterimWrapper().getInterimHearings();

        assertEquals("2010-10-10", interimHearingList.get(0).getValue().getInterimHearingDate());
        assertEquals("2020-10-10", interimHearingList.get(1).getValue().getInterimHearingDate());
        assertEquals("2030-10-10", interimHearingList.get(2).getValue().getInterimHearingDate());
    }


    @Test
    public void givenContestedPaperCase_WhenSendNotification_ThenItShouldNotSendNotification() throws IOException {
        FinremCaseDetails caseDetails = finremCaseDetailsFromResource(getResource(TEST_NEW_JSON), mapper);
        FinremCaseDetails caseDetailsBefore = finremCaseDetailsFromResource(getResource(TEST_NEW_JSON), mapper);

        caseDetails.getCaseData().setPaperApplication(YesOrNo.YES);

        interimHearingService.sendNotification(caseDetails, caseDetailsBefore);

        verify(caseDataService, never()).isApplicantSolicitorAgreeToReceiveEmails(any());

        verify(notificationService, never()).isRespondentSolicitorEmailCommunicationEnabled(isA(FinremCaseData.class));
        verify(notificationService, never()).sendInterimHearingNotificationEmailToApplicantSolicitor(any(),
            isA(InterimHearingCollection.class));
        verify(notificationService, never()).sendInterimHearingNotificationEmailToRespondentSolicitor(any(),
            isA(InterimHearingCollection.class));
    }

    @Test
    public void givenContestedNotPaperCase_WhenPaperCase_ThenItShouldSendNotification() throws IOException {
        FinremCaseDetails caseDetails = finremCaseDetailsFromResource(getResource(TEST_NEW_JSON), mapper);
        caseDetails.getCaseData().setPaperApplication(YesOrNo.NO);
        caseDetails.getCaseData().getContactDetailsWrapper().setApplicantSolicitorConsentForEmails(YesOrNo.YES);

        when(notificationService.isRespondentSolicitorEmailCommunicationEnabled(isA(FinremCaseData.class))).thenReturn(true);

        FinremCaseDetails caseDetailsBefore = finremCaseDetailsFromResource(getResource(TEST_NEW_JSON), mapper);
        interimHearingService.sendNotification(caseDetails, caseDetailsBefore);

        verify(notificationService, times(3)).isRespondentSolicitorEmailCommunicationEnabled(isA(FinremCaseData.class));
        verify(notificationService, times(3)).sendInterimHearingNotificationEmailToApplicantSolicitor(any(),
            isA(InterimHearingCollection.class));
        verify(notificationService,times(3)).sendInterimHearingNotificationEmailToRespondentSolicitor(any(),
            isA(InterimHearingCollection.class));
    }

    private void verifyNonCollectionData(FinremCaseData data) {
        InterimWrapper interimData = data.getInterimWrapper();
        InterimRegionWrapper interimRegionData = data.getRegionWrapper().getInterimRegionWrapper();
        assertNull(interimData.getInterimHearingType());
        assertNull(interimData.getInterimHearingDate());
        assertNull(interimData.getInterimHearingTime());
        assertNull(interimData.getInterimTimeEstimate());
        assertNull(interimData.getInterimAdditionalInformationAboutHearing());
        assertNull(interimData.getInterimUploadAdditionalDocument());
        assertNull(interimData.getInterimPromptForAnyDocument());
        assertNull(interimData.getInterimHearingDirectionsDocument());
        assertEquals(interimRegionData, new InterimRegionWrapper());
    }
}