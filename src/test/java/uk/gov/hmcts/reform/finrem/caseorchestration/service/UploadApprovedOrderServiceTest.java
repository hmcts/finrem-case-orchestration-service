package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.CourtDetailsParseException;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DraftDirectionDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DraftDirectionOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ANOTHER_HEARING_TO_BE_LISTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_ORDER_APPROVED_DATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_ORDER_APPROVED_JUDGE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_ORDER_APPROVED_JUDGE_TYPE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.DRAFT_DIRECTION_DETAILS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.DRAFT_DIRECTION_DETAILS_COLLECTION_RO;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_ORDER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LATEST_DRAFT_DIRECTION_ORDER;

public class UploadApprovedOrderServiceTest extends BaseServiceTest {
    private static final String JUDGE_TYPE = "TEST_TYPE";
    private static final String JUDGE_NAME = "TEST_NAME";
    private static final String APPROVED_DATE = "DATE";

    private static final String DRAFT_DIRECTION_ORDER_FILENAME = "draftDirectionOrder";
    private static final String ORDER_URL = "orderUrl";
    private static final String ORDER_BINARY_URL = "orderBinaryUrl";

    private static final String COURT_DETAILS_PARSE_EXCEPTION_MESSAGE = "Failed to parse court details.";

    private static final String TEST_PURPOSE = "Testing";

    @MockBean
    private HearingOrderService hearingOrderService;
    @MockBean
    private CaseDataService caseDataService;
    @MockBean
    private ContestedOrderApprovedLetterService contestedOrderApprovedLetterService;
    @MockBean
    private AdditionalHearingDocumentService additionalHearingDocumentService;
    @MockBean
    private GenericDocumentService genericDocumentService;
    @Autowired
    private UploadApprovedOrderService uploadApprovedOrderService;

    private CaseDetails caseDetails;

    private DraftDirectionOrder draftDirectionOrder;

    @Before
    public void setUp() {
        caseDetails = buildCaseDetails();
        caseDetails.getData().put(CONTESTED_ORDER_APPROVED_JUDGE_TYPE, JUDGE_TYPE);
        caseDetails.getData().put(CONTESTED_ORDER_APPROVED_JUDGE_NAME, JUDGE_NAME);
        caseDetails.getData().put(CONTESTED_ORDER_APPROVED_DATE, APPROVED_DATE);

        draftDirectionOrder = DraftDirectionOrder.builder()
            .purposeOfDocument(TEST_PURPOSE)
            .uploadDraftDocument(CaseDocument.builder()
                .documentUrl(ORDER_URL)
                .documentBinaryUrl(ORDER_BINARY_URL)
                .documentFilename(DRAFT_DIRECTION_ORDER_FILENAME).build())
            .build();

        mapper = new ObjectMapper();
    }

    @Test
    public void givenDraftDirectionCollectionTailIsPresent_whenHandleLatestDraftDirectionOrder_thenAddToCaseData() {
        when(hearingOrderService.draftDirectionOrderCollectionTail(caseDetails))
            .thenReturn(Optional.of(draftDirectionOrder));

        Map<String, Object> caseData = uploadApprovedOrderService.handleLatestDraftDirectionOrder(caseDetails);

        DraftDirectionOrder actualDirectionOrder = mapper.convertValue(caseData.get(LATEST_DRAFT_DIRECTION_ORDER),
            DraftDirectionOrder.class);

        assertEquals(actualDirectionOrder, draftDirectionOrder);
    }

    @Test
    public void givenDraftDirectionTailNotPresent_whenHandleLatestDraftDirectionOrder_thenLatestOrderKeyIsAbsent() {
        when(hearingOrderService.draftDirectionOrderCollectionTail(caseDetails))
            .thenReturn(Optional.empty());

        Map<String, Object> caseData = uploadApprovedOrderService.handleLatestDraftDirectionOrder(caseDetails);

        assertFalse(caseData.containsKey(LATEST_DRAFT_DIRECTION_ORDER));
    }

    @Test
    public void givenNoExceptions_whenHandleUploadApprovedOrderAboutToSubmit_thenReturnValidatedResponse() throws JsonProcessingException {
        caseDetails.getData().put(HEARING_ORDER_COLLECTION, getDirectionOrderCollection());
        uploadApprovedOrderService.handleUploadApprovedOrderAboutToSubmit(caseDetails, AUTH_TOKEN);

        verify(hearingOrderService, times(1))
            .updateCaseDataForLatestHearingOrderCollection(any(), any());
        verify(genericDocumentService, times(1)).convertDocumentIfNotPdfAlready(any(), eq(AUTH_TOKEN));
        verify(contestedOrderApprovedLetterService, times(1))
            .generateAndStoreContestedOrderApprovedLetter(caseDetails, AUTH_TOKEN);
        verify(caseDataService, times(1))
            .moveCollection(caseDetails.getData(), DRAFT_DIRECTION_DETAILS_COLLECTION, DRAFT_DIRECTION_DETAILS_COLLECTION_RO);
        verify(additionalHearingDocumentService, times(1))
            .createAndStoreAdditionalHearingDocuments(AUTH_TOKEN, caseDetails);
        verify(hearingOrderService, times(1))
            .appendLatestDraftDirectionOrderToJudgesAmendedDirectionOrders(caseDetails);
    }

    @Test
    public void givenExceptions_whenHandleUploadApprovedOrderAboutToSubmit_thenReturnResponseWithErrors() throws JsonProcessingException {
        doThrow(new CourtDetailsParseException()).when(additionalHearingDocumentService)
            .createAndStoreAdditionalHearingDocuments(AUTH_TOKEN, caseDetails);

        caseDetails.getData().put(HEARING_ORDER_COLLECTION, getDirectionOrderCollection());
        AboutToStartOrSubmitCallbackResponse response = uploadApprovedOrderService
            .handleUploadApprovedOrderAboutToSubmit(caseDetails, AUTH_TOKEN);

        assertThat(response.getErrors(), hasSize(1));
        assertEquals(response.getErrors().get(0), COURT_DETAILS_PARSE_EXCEPTION_MESSAGE);

        verify(hearingOrderService, times(1))
            .updateCaseDataForLatestHearingOrderCollection(any(), any());
        verify(genericDocumentService, times(1)).convertDocumentIfNotPdfAlready(any(), eq(AUTH_TOKEN));
        verify(contestedOrderApprovedLetterService, times(1))
            .generateAndStoreContestedOrderApprovedLetter(caseDetails, AUTH_TOKEN);
        verify(caseDataService, times(1))
            .moveCollection(caseDetails.getData(), DRAFT_DIRECTION_DETAILS_COLLECTION, DRAFT_DIRECTION_DETAILS_COLLECTION_RO);
    }

    @Test
    public void givenDirectionDetailsTailIsPresent_whenSetIsFinalHearingMidEvent_thenSetLatestDirectionOrderIsFinalField() {
        List<Element<DraftDirectionDetails>> draftDirectionDetailsCollection = new ArrayList<>();
        draftDirectionDetailsCollection.add(Element.element(UUID.randomUUID(), DraftDirectionDetails.builder()
                .isFinal(YES_VALUE)
                .isAnotherHearing(YES_VALUE)
                .typeOfHearing("TEST_TYPE")
            .build()));

        caseDetails.getData().put(DRAFT_DIRECTION_DETAILS_COLLECTION, draftDirectionDetailsCollection);

        Map<String, Object> caseData = uploadApprovedOrderService.setIsFinalHearingFieldMidEvent(caseDetails);
        assertTrue(caseData.containsKey(ANOTHER_HEARING_TO_BE_LISTED));
        assertEquals(caseData.get(ANOTHER_HEARING_TO_BE_LISTED), YES_VALUE);
    }

    @Test
    public void givenDirectionDetailsTailNotPresent_whenSetIsFinalHearingMidEvent_thenSetLatestDirectionOrderIsFinalToNo() {
        Map<String, Object> caseData = uploadApprovedOrderService.setIsFinalHearingFieldMidEvent(caseDetails);
        assertTrue(caseData.containsKey(ANOTHER_HEARING_TO_BE_LISTED));
        assertEquals(caseData.get(ANOTHER_HEARING_TO_BE_LISTED), NO_VALUE);
    }

    private List<Element<DirectionOrder>> getDirectionOrderCollection() {
        DirectionOrder directionOrder = DirectionOrder.builder()
            .uploadDraftDocument(CaseDocument.builder()
                .documentFilename("directionOrder.pdf").documentBinaryUrl("aBinaryurl").build()).build();

        return List.of(Element.element(UUID.randomUUID(), directionOrder));
    }
}
