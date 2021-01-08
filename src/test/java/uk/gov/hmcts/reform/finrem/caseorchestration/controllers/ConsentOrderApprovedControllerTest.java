package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import org.junit.Test;
import org.mockito.stubbing.OngoingStubbing;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.PensionCollectionData;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderApprovedDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import java.util.List;

import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.BINARY_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.DOC_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.FILE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.PENSION_TYPE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.feignError;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.pensionDocumentData;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.DOCUMENT_BINARY_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ConsentedStatus.CONSENT_ORDER_MADE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LATEST_CONSENT_ORDER;

@WebMvcTest(ConsentOrderApprovedController.class)
public class ConsentOrderApprovedControllerTest extends BaseControllerTest {

    @MockBean private ConsentOrderApprovedDocumentService consentOrderApprovedDocumentService;
    @MockBean private GenericDocumentService genericDocumentService;
    @MockBean private ConsentOrderPrintService consentOrderPrintService;
    @MockBean private NotificationService notificationService;
    @MockBean private FeatureToggleService featureToggleService;

    public String consentOrderApprovedEndpoint() {
        return "/case-orchestration/documents/consent-order-approved";
    }

    public String contestedConsentOrderApprovedEndpoint() {
        return "/case-orchestration/consent-in-contested/consent-order-approved";
    }

    public String contestedConsentSendOrderEndpoint() {
        return "/case-orchestration/consent-in-contested/send-order";
    }

    @Test
    public void consentOrderApproved400Error() throws Exception {
        doEmptyCaseDataSetUp();

        mvc.perform(post(consentOrderApprovedEndpoint())
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void consentOrderApproved500Error() throws Exception {
        doValidCaseDataSetUp();
        whenServiceGeneratesDocument().thenThrow(feignError());

        mvc.perform(post(consentOrderApprovedEndpoint())
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isInternalServerError());
    }

    @Test
    public void latestConsentOrderIsMissing() throws Exception {
        doMissingLatestConsentOrder();
        whenServiceGeneratesDocument().thenReturn(caseDocument());
        whenServiceGeneratesNotificationLetter().thenReturn(caseDocument());
        whenAnnexStampingDocument().thenReturn(caseDocument());
        whenStampingDocument().thenReturn(caseDocument());
        whenStampingPensionDocuments().thenReturn(singletonList(pensionDocumentData()));

        ResultActions result = mvc.perform(post(consentOrderApprovedEndpoint())
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE));

        result.andExpect(status().isOk());
        result.andExpect(jsonPath("$.data", not(hasKey(LATEST_CONSENT_ORDER))));
    }

    @Test
    public void consentOrderApprovedSuccess() throws Exception {
        doValidCaseDataSetUp();
        whenServiceGeneratesDocument().thenReturn(caseDocument());
        whenServiceGeneratesNotificationLetter().thenReturn(caseDocument());
        whenAnnexStampingDocument().thenReturn(caseDocument());
        whenStampingDocument().thenReturn(caseDocument());
        whenStampingPensionDocuments().thenReturn(singletonList(pensionDocumentData()));

        ResultActions result = mvc.perform(post(consentOrderApprovedEndpoint())
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE));

        result.andExpect(status().isOk());
        assertLetter(result);
        assertConsentOrder(result);
        assertPensionDocs(result);
    }

    @Test
    public void consentOrderApprovedSuccessForPaperApplication() throws Exception {
        doValidCaseDataSetUpForPaperApplication();
        whenServiceGeneratesDocument().thenReturn(caseDocument());
        whenServiceGeneratesNotificationLetter().thenReturn(caseDocument());
        whenAnnexStampingDocument().thenReturn(caseDocument());
        whenStampingDocument().thenReturn(caseDocument());
        whenStampingPensionDocuments().thenReturn(singletonList(pensionDocumentData()));

        ResultActions result = mvc.perform(post(consentOrderApprovedEndpoint())
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE));

        result.andExpect(status().isOk());
        assertLetter(result);
        assertConsentOrder(result);
        assertPensionDocs(result);
    }

    @Test
    public void shouldUpdateStateToConsentOrderMadeAndBulkPrint() throws Exception {
        doValidCaseDataSetUpNoPensionCollection();
        whenServiceGeneratesDocument().thenReturn(caseDocument());
        whenServiceGeneratesNotificationLetter().thenReturn(caseDocument());
        whenAnnexStampingDocument().thenReturn(caseDocument());
        whenStampingDocument().thenReturn(caseDocument());
        whenStampingPensionDocuments().thenReturn(singletonList(pensionDocumentData()));
        when(featureToggleService.isRespondentJourneyEnabled()).thenReturn(true);
        when(notificationService.shouldEmailRespondentSolicitor(any())).thenReturn(true);
        when(notificationService.shouldEmailApplicantSolicitor(any())).thenReturn(true);

        ResultActions result = mvc.perform(post(consentOrderApprovedEndpoint())
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE));

        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.data.state", is(CONSENT_ORDER_MADE.toString())));

        verify(consentOrderPrintService).sendConsentOrderToBulkPrint(any(), any());
        verify(notificationService).sendConsentOrderAvailableCtscEmail(any());
        verify(notificationService).sendConsentOrderAvailableEmailToApplicantSolicitor(any());
        verify(notificationService).sendConsentOrderAvailableEmailToRespondentSolicitor(any());
    }

    @Test
    public void shouldUpdateStateToConsentOrderMadeAndBulkPrint_respJounryToggledOff() throws Exception {
        doValidCaseDataSetUpNoPensionCollection();
        whenServiceGeneratesDocument().thenReturn(caseDocument());
        whenServiceGeneratesNotificationLetter().thenReturn(caseDocument());
        whenAnnexStampingDocument().thenReturn(caseDocument());
        whenStampingDocument().thenReturn(caseDocument());
        whenStampingPensionDocuments().thenReturn(singletonList(pensionDocumentData()));
        when(featureToggleService.isRespondentJourneyEnabled()).thenReturn(false);
        when(notificationService.shouldEmailRespondentSolicitor(any())).thenReturn(true);
        when(notificationService.shouldEmailApplicantSolicitor(any())).thenReturn(true);

        ResultActions result = mvc.perform(post(consentOrderApprovedEndpoint())
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE));

        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.data.state", is(CONSENT_ORDER_MADE.toString())));

        verify(consentOrderPrintService).sendConsentOrderToBulkPrint(any(), any());
        verify(notificationService).sendConsentOrderAvailableCtscEmail(any());
        verify(notificationService).sendConsentOrderAvailableEmailToApplicantSolicitor(any());
        verify(notificationService, never()).sendConsentOrderAvailableEmailToRespondentSolicitor(any());
    }

    @Test
    public void shouldUpdateStateToConsentOrderMadeAndBulkPrint_noEmails() throws Exception {
        doValidCaseDataSetUpNoPensionCollection();
        whenServiceGeneratesDocument().thenReturn(caseDocument());
        whenServiceGeneratesNotificationLetter().thenReturn(caseDocument());
        whenAnnexStampingDocument().thenReturn(caseDocument());
        whenStampingDocument().thenReturn(caseDocument());
        whenStampingPensionDocuments().thenReturn(singletonList(pensionDocumentData()));
        when(featureToggleService.isRespondentJourneyEnabled()).thenReturn(true);
        when(notificationService.shouldEmailRespondentSolicitor(any())).thenReturn(false);
        when(notificationService.shouldEmailApplicantSolicitor(any())).thenReturn(false);

        ResultActions result = mvc.perform(post(consentOrderApprovedEndpoint())
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE));

        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.data.state", is(CONSENT_ORDER_MADE.toString())));

        verify(consentOrderPrintService).sendConsentOrderToBulkPrint(any(), any());
        verify(notificationService).sendConsentOrderAvailableCtscEmail(any());
        verify(notificationService, never()).sendConsentOrderAvailableEmailToApplicantSolicitor(any());
        verify(notificationService, never()).sendConsentOrderAvailableEmailToRespondentSolicitor(any());
    }

    @Test
    public void shouldNotTriggerConsentOrderApprovedNotificationLetterIfIsNotPaperApplication() throws Exception {
        doValidCaseDataSetUp();

        whenServiceGeneratesDocument().thenReturn(caseDocument());
        whenServiceGeneratesNotificationLetter().thenReturn(caseDocument());
        whenAnnexStampingDocument().thenReturn(caseDocument());
        whenStampingDocument().thenReturn(caseDocument());
        whenStampingPensionDocuments().thenReturn(singletonList(pensionDocumentData()));

        ResultActions result = mvc.perform(post(consentOrderApprovedEndpoint())
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE));

        result.andExpect(status().isOk());
        assertLetter(result);
        assertConsentOrder(result);
        assertPensionDocs(result);
        verify(consentOrderApprovedDocumentService, never()).generateApprovedConsentOrderCoverLetter(any(), any());
    }

    @Test
    public void consentInContestedConsentOrderApprovedShouldProcessDocuments() throws Exception {
        doValidCaseDataSetUp();

        mvc.perform(post(contestedConsentOrderApprovedEndpoint())
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk());

        verify(consentOrderApprovedDocumentService, times(1)).stampAndPopulateContestedConsentApprovedOrderCollection(any(), eq(AUTH_TOKEN));
        verify(consentOrderApprovedDocumentService, times(1)).generateAndPopulateConsentOrderLetter(any(), eq(AUTH_TOKEN));
    }

    @Test
    public void consentInContestedConsentOrderApprovedShouldProcessPensionDocs() throws Exception {
        doValidConsentInContestWithPensionData();

        mvc.perform(post(contestedConsentOrderApprovedEndpoint())
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk());

        verify(consentOrderApprovedDocumentService, times(1)).stampAndPopulateContestedConsentApprovedOrderCollection(any(), eq(AUTH_TOKEN));
    }

    @Test
    public void consentInContestedSendOrderShouldPrintDocsWhenNotApproved() throws Exception {
        doValidCaseDataSetUp();

        mvc.perform(post(contestedConsentSendOrderEndpoint())
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk());

        verify(consentOrderApprovedDocumentService, never()).generateApprovedConsentOrderLetter(any(), eq(AUTH_TOKEN));
        verify(consentOrderPrintService, times(1)).sendConsentOrderToBulkPrint(any(), eq(AUTH_TOKEN));
    }

    @Test
    public void consentInContestedSendOrderShouldPrintDocsWhenApproved() throws Exception {
        doValidConsentOrderApprovedSetup();
        when(consentOrderApprovedDocumentService.generateApprovedConsentOrderLetter(any(), eq(AUTH_TOKEN)))
            .thenReturn(caseDocument());

        ResultActions result = mvc.perform(post(contestedConsentSendOrderEndpoint())
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE));

        result.andExpect(status().isOk());
        verify(consentOrderPrintService, times(1)).sendConsentOrderToBulkPrint(any(), eq(AUTH_TOKEN));
    }

    private OngoingStubbing<CaseDocument> whenServiceGeneratesDocument() {
        return when(consentOrderApprovedDocumentService.generateApprovedConsentOrderLetter(isA(CaseDetails.class), eq(AUTH_TOKEN)));
    }

    private OngoingStubbing<CaseDocument> whenServiceGeneratesNotificationLetter() {
        return when(consentOrderApprovedDocumentService.generateApprovedConsentOrderCoverLetter(isA(CaseDetails.class), eq(AUTH_TOKEN)));
    }

    private OngoingStubbing<CaseDocument> whenAnnexStampingDocument() {
        return when(genericDocumentService.annexStampDocument(isA(CaseDocument.class), eq(AUTH_TOKEN)));
    }

    private OngoingStubbing<CaseDocument> whenStampingDocument() {
        return when(genericDocumentService.stampDocument(isA(CaseDocument.class), eq(AUTH_TOKEN)));
    }

    private OngoingStubbing<List<PensionCollectionData>> whenStampingPensionDocuments() {
        return when(consentOrderApprovedDocumentService.stampPensionDocuments(any(), eq(AUTH_TOKEN)));
    }

    private void assertLetter(ResultActions result) throws Exception {
        String path = "$.data.approvedOrderCollection[0].value.orderLetter.";
        result.andExpect(jsonPath(path + "document_url", is(DOC_URL)))
            .andExpect(jsonPath(path + "document_filename", is(FILE_NAME)))
            .andExpect(jsonPath(path + DOCUMENT_BINARY_URL, is(BINARY_URL)));
    }

    private void assertConsentOrder(ResultActions result) throws Exception {
        assertDocument(result, "$.data.approvedOrderCollection[0].value.consentOrder.");
    }

    private void assertPensionDocs(ResultActions result) throws Exception {
        String path = "$.data.approvedOrderCollection[0].value.pensionDocuments[0].value.";
        String docPath = "$.data.approvedOrderCollection[0].value.pensionDocuments[0].value.uploadedDocument.";
        result.andExpect(jsonPath(path + "typeOfDocument", is(PENSION_TYPE)));
        assertDocument(result, docPath);
    }

    private void assertDocument(ResultActions result, String path) throws Exception {
        result.andExpect(jsonPath(path + "document_url", is(DOC_URL)))
            .andExpect(jsonPath(path + "document_filename", is(FILE_NAME)))
            .andExpect(jsonPath(path + DOCUMENT_BINARY_URL, is(BINARY_URL)));
    }
}
