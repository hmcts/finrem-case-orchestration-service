package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.CourtDetailsParseException;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AdditionalDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingDirectionDetail;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingDirectionDetailsCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.JudgeType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadAdditionalDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadAdditionalDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;

public class UploadApprovedOrderServiceTest extends BaseServiceTest {
    private static final String AUTH_TOKEN = "Token -:)";
    private static final String JUDGE_NAME = "TEST_NAME";

    private static final String COURT_DETAILS_PARSE_EXCEPTION_MESSAGE = "Failed to parse court details.";

    @Autowired
    private UploadApprovedOrderService uploadApprovedOrderService;

    @MockBean
    private HearingOrderService hearingOrderService;
    @MockBean
    private ContestedOrderApprovedLetterService contestedOrderApprovedLetterService;
    @MockBean
    private AdditionalHearingDocumentService additionalHearingDocumentService;
    @MockBean
    private ApprovedOrderNoticeOfHearingService approvedOrderNoticeOfHearingService;


    @Test
    public void givenNoExceptions_whenHandleUploadApprovedOrderAboutToSubmit_thenReturnCollectionWithExistingOrder() throws JsonProcessingException {
        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        FinremCaseDetails finremCaseDetails = callbackRequest.getCaseDetails();
        FinremCaseData finremCaseData = finremCaseDetails.getData();


        finremCaseData.setOrderApprovedJudgeType(JudgeType.DISTRICT_JUDGE);
        finremCaseData.setOrderApprovedJudgeName(JUDGE_NAME);
        finremCaseData.setOrderApprovedDate(LocalDate.of(2023,11,11));

        FinremCaseDetails finremCaseDetailsBefore = callbackRequest.getCaseDetailsBefore();
        FinremCaseData finremCaseDataBefore = finremCaseDetailsBefore.getData();

        finremCaseDataBefore.setOrderApprovedJudgeType(JudgeType.DISTRICT_JUDGE);
        finremCaseDataBefore.setOrderApprovedJudgeName(JUDGE_NAME);
        finremCaseDataBefore.setOrderApprovedDate(LocalDate.of(2023,11,11));

        finremCaseData.setUploadHearingOrder(getHearingOrderCollection());
        finremCaseData.setFinalOrderCollection(getHearingOrderCollection());

        finremCaseDataBefore.setFinalOrderCollection(getHearingOrderCollection());
        UploadAdditionalDocument additionalDocument = UploadAdditionalDocument.builder().additionalDocuments(caseDocument())
            .additionalDocumentType(AdditionalDocumentType.OTHER).build();
        UploadAdditionalDocumentCollection collection = UploadAdditionalDocumentCollection.builder().value(additionalDocument).build();
        List<UploadAdditionalDocumentCollection> uploadAdditionalDocument = new ArrayList<>();
        uploadAdditionalDocument.add(collection);
        finremCaseData.setUploadAdditionalDocument(uploadAdditionalDocument);
        finremCaseDataBefore.setUploadAdditionalDocument(uploadAdditionalDocument);

        HearingDirectionDetail.HearingDirectionDetailBuilder hearingYN = HearingDirectionDetail.builder().isAnotherHearingYN(YesOrNo.YES);
        HearingDirectionDetailsCollection detailsCollection = HearingDirectionDetailsCollection.builder().value(hearingYN.build()).build();
        List<HearingDirectionDetailsCollection> latestHearingDirections = new ArrayList<>();
        latestHearingDirections.add(detailsCollection);
        finremCaseData.setHearingDirectionDetailsCollection(latestHearingDirections);


        when(additionalHearingDocumentService
            .getApprovedHearingOrders(finremCaseDetailsBefore, AUTH_TOKEN)).thenReturn(new ArrayList<>());


        List<String> errors = new ArrayList<>();

        uploadApprovedOrderService.processApprovedOrders(callbackRequest, errors, AUTH_TOKEN);

        assertEquals(1, finremCaseData.getUploadHearingOrder().size());
        assertEquals(2, finremCaseData.getUploadAdditionalDocument().size());
        assertEquals(1, finremCaseData.getFinalOrderCollection().size());

        verify(additionalHearingDocumentService)
            .createAndStoreAdditionalHearingDocumentsFromApprovedOrder(AUTH_TOKEN, finremCaseDetails);
        verify(hearingOrderService).appendLatestDraftDirectionOrderToJudgesAmendedDirectionOrders(finremCaseDetails);
        verify(approvedOrderNoticeOfHearingService).createAndStoreHearingNoticeDocumentPack(finremCaseDetails, AUTH_TOKEN);
        verify(additionalHearingDocumentService).getApprovedHearingOrders(finremCaseDetailsBefore, AUTH_TOKEN);
        verify(additionalHearingDocumentService).addToFinalOrderCollection(finremCaseDetails, AUTH_TOKEN);
    }


    @Test
    public void givenNoExceptions_whenHandleAboutToSubmitAndNoNextHearing_thenDoNotGenerateDocumentPack() {
        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        FinremCaseDetails finremCaseDetails = callbackRequest.getCaseDetails();
        FinremCaseData finremCaseData = finremCaseDetails.getData();
        finremCaseData.setUploadHearingOrder(getHearingOrderCollection());

        HearingDirectionDetail.HearingDirectionDetailBuilder hearingYN
            = HearingDirectionDetail.builder().isAnotherHearingYN(YesOrNo.NO);
        HearingDirectionDetailsCollection detailsCollection
            = HearingDirectionDetailsCollection.builder().value(hearingYN.build()).build();
        List<HearingDirectionDetailsCollection> latestHearingDirections = new ArrayList<>();
        latestHearingDirections.add(detailsCollection);
        finremCaseData.setHearingDirectionDetailsCollection(latestHearingDirections);

        List<String> errors = new ArrayList<>();
        uploadApprovedOrderService.processApprovedOrders(callbackRequest, errors, AUTH_TOKEN);

        verify(contestedOrderApprovedLetterService)
            .generateAndStoreContestedOrderApprovedLetter(finremCaseDetails, AUTH_TOKEN);
        verify(additionalHearingDocumentService)
            .createAndStoreAdditionalHearingDocumentsFromApprovedOrder(AUTH_TOKEN, finremCaseDetails);
        verify(hearingOrderService)
            .appendLatestDraftDirectionOrderToJudgesAmendedDirectionOrders(finremCaseDetails);
        verify(approvedOrderNoticeOfHearingService, never())
            .createAndStoreHearingNoticeDocumentPack(finremCaseDetails, AUTH_TOKEN);
    }

    @Test
    public void givenExceptions_whenHandleUploadApprovedOrderAboutToSubmit_thenReturnResponseWithErrors() {
        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        FinremCaseDetails finremCaseDetails = callbackRequest.getCaseDetails();
        FinremCaseData finremCaseData = finremCaseDetails.getData();

        doThrow(new CourtDetailsParseException()).when(additionalHearingDocumentService)
            .createAndStoreAdditionalHearingDocumentsFromApprovedOrder(AUTH_TOKEN, finremCaseDetails);

        finremCaseData.setUploadHearingOrder(getHearingOrderCollection());

        List<String> errors = new ArrayList<>();
        uploadApprovedOrderService.processApprovedOrders(callbackRequest, errors, AUTH_TOKEN);

        assertEquals(1, errors.size());
        assertEquals(COURT_DETAILS_PARSE_EXCEPTION_MESSAGE, errors.get(0));

        verify(contestedOrderApprovedLetterService)
            .generateAndStoreContestedOrderApprovedLetter(finremCaseDetails, AUTH_TOKEN);
    }


    private List<DirectionOrderCollection> getHearingOrderCollection() {
        DirectionOrder directionOrder = DirectionOrder.builder()
            .uploadDraftDocument(caseDocument())
            .isOrderStamped(YesOrNo.YES)
            .orderDateTime(LocalDateTime.now())
            .build();
        DirectionOrderCollection orderCollection = DirectionOrderCollection
            .builder()
            .value(directionOrder)
            .build();
        List<DirectionOrderCollection> uploadHearingOrder = new ArrayList<>();
        uploadHearingOrder.add(orderCollection);
        return uploadHearingOrder;
    }

    private FinremCallbackRequest buildCallbackRequest() {
        return FinremCallbackRequest
            .builder()
            .eventType(EventType.UPLOAD_APPROVED_ORDER)
            .caseDetails(FinremCaseDetails.builder().id(123L).caseType(CONTESTED)
                .data(new FinremCaseData()).build())
            .caseDetailsBefore(FinremCaseDetails.builder().id(123L).caseType(CONTESTED)
                .data(new FinremCaseData()).build())
            .build();
    }
}
