package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.ConsentedApplicationHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ConsentOrderWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderApprovedDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.consentorder.ConsentOrderAvailableCorresponder;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ConsentOrderApprovedControllerTest {

    @Mock
    private GenericDocumentService genericDocumentService;
    @Mock
    private DocumentConfiguration documentConfiguration;
    @Mock
    private DocumentHelper documentHelper;

    private ObjectMapper mapper = new ObjectMapper();
    @Mock
    private CaseDataService caseDataService;
    @Mock
    private ConsentedApplicationHelper consentedApplicationHelper;
    @InjectMocks
    private ConsentOrderApprovedDocumentService consentOrderApprovedDocumentService;

    @MockBean
    private ConsentOrderAvailableCorresponder consentOrderAvailableCorresponder;

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
        when(documentHelper.getPensionDocumentsData(any())).thenReturn(singletonList(caseDocument()));

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
        when(documentHelper.getPensionDocumentsData(any())).thenReturn(singletonList(caseDocument()));

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


        ResultActions result = mvc.perform(post(consentOrderApprovedEndpoint())
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE));

        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.data.state", is(CONSENT_ORDER_MADE.toString())));

        verify(consentOrderPrintService).sendConsentOrderToBulkPrint(any(), any());
        verify(notificationService).sendConsentOrderAvailableCtscEmail(any());
        verify(consentOrderAvailableCorresponder).sendCorrespondence(any());
    }

    @Test
    public void shouldUpdateStateToConsentOrderMadeAndBulkPrint_noEmails() throws Exception {
        doValidCaseDataSetUpNoPensionCollection();
        whenServiceGeneratesDocument().thenReturn(caseDocument());
        whenServiceGeneratesNotificationLetter().thenReturn(caseDocument());
        whenAnnexStampingDocument().thenReturn(caseDocument());
        whenStampingDocument().thenReturn(caseDocument());
        whenStampingPensionDocuments().thenReturn(singletonList(pensionDocumentData()));


        ResultActions result = mvc.perform(post(consentOrderApprovedEndpoint())
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE));

        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.data.state", is(CONSENT_ORDER_MADE.toString())));

        verify(consentOrderPrintService).sendConsentOrderToBulkPrint(any(), any());
        verify(notificationService).sendConsentOrderAvailableCtscEmail(any());
        verify(consentOrderAvailableCorresponder).sendCorrespondence(any());
    }

    @Test
    public void shouldNotTriggerConsentOrderApprovedNotificationLetterIfIsNotPaperApplication() throws Exception {
        doValidCaseDataSetUp();

        whenServiceGeneratesDocument().thenReturn(caseDocument());
        whenServiceGeneratesNotificationLetter().thenReturn(caseDocument());
        whenAnnexStampingDocument().thenReturn(caseDocument());
        whenStampingDocument().thenReturn(caseDocument());
        whenStampingPensionDocuments().thenReturn(singletonList(pensionDocumentData()));
        when(documentHelper.getPensionDocumentsData(any())).thenReturn(singletonList(caseDocument()));

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


    @Before
    public void setup() {
        consentOrderApprovedDocumentService = new ConsentOrderApprovedDocumentService(genericDocumentService,
            documentConfiguration, documentHelper, mapper, caseDataService, consentedApplicationHelper);
    }


    @Test
    public void givenFinremCaseDetails_whenAddGeneratedApprConsOrderDocsToCase_thenGenerateAndAddDocsToCase() {
        CaseDocument uploadApproveOrder = CaseDocument.builder().documentFilename("testUploadAppOrder").build();
        FinremCaseData finremCaseData = FinremCaseData.builder().consentOrderWrapper(
                ConsentOrderWrapper.builder().uploadApprovedConsentOrder(uploadApproveOrder).build())
            .build();

        when(genericDocumentService.annexStampDocument(any(), any()))
            .thenReturn(uploadApproveOrder);
        when(genericDocumentService.generateDocument(any(), any(), any(), any()))
            .thenReturn(uploadApproveOrder);
        when(documentHelper.deepCopy(any(), any()))
            .thenReturn(toCaseDetails(FinremCaseDetails.builder().data(finremCaseData).build()));

        FinremCaseDetails finremCaseDetails = FinremCaseDetails.builder().id(1L).data(finremCaseData).build();
        consentOrderApprovedDocumentService.addGeneratedApprovedConsentOrderDocumentsToCase("AUTH_TOKEN",
            finremCaseDetails);

        assertThat(finremCaseDetails.getData().getApprovedOrderCollection().get(0).getApprovedOrder()
                .getConsentOrder().getDocumentFilename(),
            is("testUploadAppOrder"));
    }

    private CaseDetails toCaseDetails(FinremCaseDetails finremCaseDetails) {
        CaseDetails generateDocumentPayload = null;
        try {
            generateDocumentPayload = mapper.readValue(mapper.writeValueAsString(finremCaseDetails), CaseDetails.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return generateDocumentPayload;
    }
}
