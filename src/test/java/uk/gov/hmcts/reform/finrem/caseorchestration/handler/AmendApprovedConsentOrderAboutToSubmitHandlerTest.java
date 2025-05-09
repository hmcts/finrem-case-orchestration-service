package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ApprovedOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ConsentOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ConsentOrderWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory.ApprovedConsentOrderDocumentCategoriser;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.StampType.FAMILY_COURT_STAMP;

@ExtendWith(MockitoExtension.class)
class AmendApprovedConsentOrderAboutToSubmitHandlerTest extends BaseHandlerTestSetup {

    public static final String AUTH_TOKEN = "tokien:)";
    private AmendApprovedConsentOrderAboutToSubmitHandler aboutToSubmitHandler;
    private FinremCallbackRequest callbackRequest;
    @Mock
    private FinremCaseDetailsMapper finremCaseDetailsMapper;
    @Mock
    private FeatureToggleService featureToggleService;
    @Mock
    private GenericDocumentService genericDocumentService;
    @Mock
    private DocumentHelper documentHelper;

    private static final CaseDocument ORIGINAL_LETTER = CaseDocument.builder().documentUrl("originalLetter.docx").build();
    private static final CaseDocument ORIGINAL_ORDER = CaseDocument.builder().documentUrl("originalOrder.docx").build();

    private static final CaseDocument MODIFIED_LETTER = CaseDocument.builder().documentUrl("modifiedLetter.docx").build();
    private static final CaseDocument MODIFIED_ORDER = CaseDocument.builder().documentUrl("modifiedOrder.docx").build();
    private static final CaseDocument STAMPED_LETTER = CaseDocument.builder().documentFilename("stampedLetter.pdf").build();
    private static final CaseDocument STAMPED_ORDER = CaseDocument.builder().documentFilename("stampedOrder.pdf").build();

    @BeforeEach
    public void init() {
        ApprovedConsentOrderDocumentCategoriser approvedConsentOrderCategoriser = new ApprovedConsentOrderDocumentCategoriser(featureToggleService);
        aboutToSubmitHandler = new AmendApprovedConsentOrderAboutToSubmitHandler(finremCaseDetailsMapper, approvedConsentOrderCategoriser,
            genericDocumentService, documentHelper);
        callbackRequest = buildCallbackRequest(EventType.AMEND_CONTESTED_APPROVED_CONSENT_ORDER);
    }

    @Test
    void givenACcdCallbackConsentedCase_whenAboutToSubmitEvent_thenHandlerCannotHandle() {
        assertThat(aboutToSubmitHandler.canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONSENTED,
            EventType.AMEND_CONTESTED_APPROVED_CONSENT_ORDER), equalTo(false));
    }

    @Test
    void givenACcdCallbackContestedCase_whenAboutToSubmitEvent_thenHandlerCanHandle() {
        assertThat(aboutToSubmitHandler.canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED,
            EventType.AMEND_CONTESTED_APPROVED_CONSENT_ORDER), equalTo(true));
    }

    @Test
    void givenACcdCallbackConsentedCase_whenAboutToStartEvent_thenHandlerCannotHandle() {
        assertThat(aboutToSubmitHandler.canHandle(CallbackType.ABOUT_TO_START, CaseType.CONTESTED,
            EventType.AMEND_CONTESTED_APPROVED_CONSENT_ORDER), equalTo(false));
    }

    @Test
    void givenUserHasModifiedCollection_whenHandle_ShouldUpdateWithNewDocumentAndStamp() {
        when(featureToggleService.isCaseFileViewEnabled()).thenReturn(true);
        when(documentHelper.getStampType(any(FinremCaseData.class))).thenReturn(FAMILY_COURT_STAMP);
        mockDocumentStamping(MODIFIED_LETTER, STAMPED_LETTER);
        mockDocumentStamping(MODIFIED_ORDER, STAMPED_ORDER);

        FinremCaseData currentData = FinremCaseData.builder()
            .consentOrderWrapper(
                ConsentOrderWrapper.builder()
                    .contestedConsentedApprovedOrders(List.of(
                        ConsentOrderCollection.builder()
                            .approvedOrder(ApprovedOrder.builder()
                                .orderLetter(MODIFIED_LETTER)
                                .consentOrder(MODIFIED_ORDER)
                                .build())
                            .build())
                    )
                    .build())
            .build();

        FinremCaseData previousData = FinremCaseData.builder()
            .consentOrderWrapper(
                ConsentOrderWrapper.builder()
                    .contestedConsentedApprovedOrders(List.of(
                        ConsentOrderCollection.builder()
                            .approvedOrder(ApprovedOrder.builder()
                                .orderLetter(ORIGINAL_LETTER)
                                .consentOrder(ORIGINAL_ORDER)
                                .build())
                            .build())
                    )
                    .build())
            .build();

        FinremCallbackRequest callbackRequest = FinremCallbackRequest.builder()
            .caseDetails(FinremCaseDetails.builder()
                .id(Long.valueOf(CASE_ID))
                .data(currentData)
                .build())
            .caseDetailsBefore(FinremCaseDetails.builder()
                .id(Long.valueOf(CASE_ID))
                .data(previousData)
                .build())
            .build();

        aboutToSubmitHandler.handle(callbackRequest, AUTH_TOKEN);

        List<ConsentOrderCollection> response = callbackRequest.getCaseDetails().getData()
            .getConsentOrderWrapper().getContestedConsentedApprovedOrders();

        verify(genericDocumentService, times(2)).stampDocument(any(CaseDocument.class), eq(AUTH_TOKEN), eq(FAMILY_COURT_STAMP), anyString());

        //Check if the original documents are replaced with stamped documents
        assertEquals(STAMPED_ORDER, callbackRequest.getCaseDetails().getData().getConsentOrderWrapper()
            .getContestedConsentedApprovedOrders().get(0).getApprovedOrder().getConsentOrder());
        assertEquals(STAMPED_LETTER, callbackRequest.getCaseDetails().getData().getConsentOrderWrapper()
            .getContestedConsentedApprovedOrders().get(0).getApprovedOrder().getOrderLetter());

        //Check if category ids are set
        assertEquals(DocumentCategory.APPROVED_ORDERS_CONSENT_ORDER_TO_FINALISE_PROCEEDINGS.getDocumentCategoryId(),
            response.get(0).getApprovedOrder().getConsentOrder().getCategoryId());
        assertEquals(DocumentCategory.APPROVED_ORDERS_CONSENT_ORDER_TO_FINALISE_PROCEEDINGS.getDocumentCategoryId(),
            response.get(0).getApprovedOrder().getOrderLetter().getCategoryId());
    }

    @Test
    void givenUserHasAddedMultipleNewOrders_whenHandle_ShouldUpdateWithNewDocumentsAndStamp() {
        when(featureToggleService.isCaseFileViewEnabled()).thenReturn(true);
        when(documentHelper.getStampType(any(FinremCaseData.class))).thenReturn(FAMILY_COURT_STAMP);
        final CaseDocument newLetter = CaseDocument.builder().documentUrl("newLetter.docx").build();
        final CaseDocument newOrder = CaseDocument.builder().documentUrl("newOrder.docx").build();
        mockDocumentStamping(newLetter, STAMPED_LETTER);
        mockDocumentStamping(newOrder, STAMPED_ORDER);


        FinremCaseData currentData = FinremCaseData.builder()
            .consentOrderWrapper(
                ConsentOrderWrapper.builder()
                    .contestedConsentedApprovedOrders(List.of(
                        ConsentOrderCollection.builder()
                            .approvedOrder(ApprovedOrder.builder()
                                .orderLetter(ORIGINAL_LETTER)
                                .consentOrder(ORIGINAL_ORDER)
                                .build())
                            .build(),
                        ConsentOrderCollection.builder()
                            .approvedOrder(ApprovedOrder.builder()
                                .orderLetter(newLetter)
                                .consentOrder(newOrder)
                                .build())
                            .build())
                    )
                    .build())
            .build();

        FinremCaseData previousData = FinremCaseData.builder()
            .consentOrderWrapper(
                ConsentOrderWrapper.builder()
                    .contestedConsentedApprovedOrders(List.of(
                        ConsentOrderCollection.builder()
                            .approvedOrder(ApprovedOrder.builder()
                                .orderLetter(ORIGINAL_LETTER)
                                .consentOrder(ORIGINAL_ORDER)
                                .build())
                            .build())
                    )
                    .build())
            .build();

        FinremCallbackRequest callbackRequest = FinremCallbackRequest.builder()
            .caseDetails(FinremCaseDetails.builder()
                .id(Long.valueOf(CASE_ID))
                .data(currentData)
                .build())
            .caseDetailsBefore(FinremCaseDetails.builder()
                .id(Long.valueOf(CASE_ID))
                .data(previousData)
                .build())
            .build();

        aboutToSubmitHandler.handle(callbackRequest, AUTH_TOKEN);

        List<ConsentOrderCollection> response = callbackRequest.getCaseDetails().getData()
            .getConsentOrderWrapper().getContestedConsentedApprovedOrders();

        verify(genericDocumentService, times(2)).stampDocument(any(CaseDocument.class), eq(AUTH_TOKEN), eq(FAMILY_COURT_STAMP), anyString());

        assertEquals(ORIGINAL_ORDER, callbackRequest.getCaseDetails().getData().getConsentOrderWrapper()
            .getContestedConsentedApprovedOrders().get(0).getApprovedOrder().getConsentOrder());
        assertEquals(ORIGINAL_LETTER, callbackRequest.getCaseDetails().getData().getConsentOrderWrapper()
            .getContestedConsentedApprovedOrders().get(0).getApprovedOrder().getOrderLetter());

        //Check if the new order and letter are stamped
        assertEquals(STAMPED_ORDER, callbackRequest.getCaseDetails().getData().getConsentOrderWrapper()
            .getContestedConsentedApprovedOrders().get(1).getApprovedOrder().getConsentOrder());
        assertEquals(STAMPED_LETTER, callbackRequest.getCaseDetails().getData().getConsentOrderWrapper()
            .getContestedConsentedApprovedOrders().get(1).getApprovedOrder().getOrderLetter());

        //Check if category ids are set
        assertEquals(DocumentCategory.APPROVED_ORDERS_CONSENT_ORDER_TO_FINALISE_PROCEEDINGS.getDocumentCategoryId(),
            response.get(0).getApprovedOrder().getConsentOrder().getCategoryId());
        assertEquals(DocumentCategory.APPROVED_ORDERS_CONSENT_ORDER_TO_FINALISE_PROCEEDINGS.getDocumentCategoryId(),
            response.get(0).getApprovedOrder().getOrderLetter().getCategoryId());
        assertEquals(DocumentCategory.APPROVED_ORDERS_CONSENT_ORDER_TO_FINALISE_PROCEEDINGS.getDocumentCategoryId(),
            response.get(1).getApprovedOrder().getConsentOrder().getCategoryId());
        assertEquals(DocumentCategory.APPROVED_ORDERS_CONSENT_ORDER_TO_FINALISE_PROCEEDINGS.getDocumentCategoryId(),
            response.get(1).getApprovedOrder().getOrderLetter().getCategoryId());
    }

    @Test
    void givenUserHasNotModified_whenHandle_ShouldNotCallAnything() {
        when(featureToggleService.isCaseFileViewEnabled()).thenReturn(true);
        when(documentHelper.getStampType(any(FinremCaseData.class))).thenReturn(FAMILY_COURT_STAMP);

        FinremCaseData currentData = FinremCaseData.builder()
            .consentOrderWrapper(
                ConsentOrderWrapper.builder()
                    .contestedConsentedApprovedOrders(List.of(
                        ConsentOrderCollection.builder()
                            .approvedOrder(ApprovedOrder.builder()
                                .orderLetter(ORIGINAL_LETTER)
                                .consentOrder(ORIGINAL_ORDER)
                                .build())
                            .build())
                    )
                    .build())
            .build();

        FinremCaseData previousData = FinremCaseData.builder()
            .consentOrderWrapper(
                ConsentOrderWrapper.builder()
                    .contestedConsentedApprovedOrders(List.of(
                        ConsentOrderCollection.builder()
                            .approvedOrder(ApprovedOrder.builder()
                                .orderLetter(ORIGINAL_LETTER)
                                .consentOrder(ORIGINAL_ORDER)
                                .build())
                            .build())
                    )
                    .build())
            .build();

        FinremCallbackRequest callbackRequest = FinremCallbackRequest.builder()
            .caseDetails(FinremCaseDetails.builder()
                .id(Long.valueOf(CASE_ID))
                .data(currentData)
                .build())
            .caseDetailsBefore(FinremCaseDetails.builder()
                .id(Long.valueOf(CASE_ID))
                .data(previousData)
                .build())
            .build();

        aboutToSubmitHandler.handle(callbackRequest, AUTH_TOKEN);

        verify(genericDocumentService, never()).stampDocument(any(CaseDocument.class), eq(AUTH_TOKEN), eq(FAMILY_COURT_STAMP), anyString());
        assertEquals(ORIGINAL_ORDER, callbackRequest.getCaseDetails().getData().getConsentOrderWrapper()
            .getContestedConsentedApprovedOrders().get(0).getApprovedOrder().getConsentOrder());
        assertEquals(ORIGINAL_LETTER, callbackRequest.getCaseDetails().getData().getConsentOrderWrapper()
            .getContestedConsentedApprovedOrders().get(0).getApprovedOrder().getOrderLetter());
    }

    private ConsentOrderCollection getApprovedOrderWithLetter(String letterFile, String letterUrl, String letterBinary) {
        ApprovedOrder approvedOrder = ApprovedOrder.builder()
            .orderLetter(getCaseDocument(letterFile, letterUrl, letterBinary))
            .build();
        return ConsentOrderCollection.builder().approvedOrder(approvedOrder).build();
    }

    private CaseDocument getCaseDocument(String file, String url, String binary) {
        return TestSetUpUtils.caseDocument(url, file, binary);
    }

    private void mockDocumentStamping(CaseDocument originalDocument, CaseDocument stampedDocument) {
        when(genericDocumentService.stampDocument(originalDocument, AUTH_TOKEN, FAMILY_COURT_STAMP, CASE_ID))
            .thenReturn(stampedDocument);
    }
}
