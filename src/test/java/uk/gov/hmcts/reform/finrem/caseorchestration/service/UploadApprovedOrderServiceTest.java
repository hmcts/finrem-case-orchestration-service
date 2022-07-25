package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.CourtDetailsParseException;
import uk.gov.hmcts.reform.finrem.ccd.callback.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.ccd.domain.DirectionOrder;
import uk.gov.hmcts.reform.finrem.ccd.domain.DirectionOrderCollection;
import uk.gov.hmcts.reform.finrem.ccd.domain.Document;
import uk.gov.hmcts.reform.finrem.ccd.domain.DocumentCollection;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.HearingDirectionDetail;
import uk.gov.hmcts.reform.finrem.ccd.domain.HearingDirectionDetailsCollection;
import uk.gov.hmcts.reform.finrem.ccd.domain.JudgeType;
import uk.gov.hmcts.reform.finrem.ccd.domain.YesOrNo;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;

public class UploadApprovedOrderServiceTest extends BaseServiceTest {
    private static final JudgeType JUDGE_TYPE = JudgeType.DEPUTY_DISTRICT_JUDGE;
    private static final String JUDGE_NAME = "TEST_NAME";
    private static final LocalDate APPROVED_DATE = LocalDate.of(2020, 1, 1);

    private static final String COURT_DETAILS_PARSE_EXCEPTION_MESSAGE = "Failed to parse court details.";

    @MockBean
    private HearingOrderService hearingOrderService;
    @MockBean
    private ContestedOrderApprovedLetterService contestedOrderApprovedLetterService;
    @MockBean
    private AdditionalHearingDocumentService additionalHearingDocumentService;
    @MockBean
    private GenericDocumentService genericDocumentService;
    @MockBean
    private ApprovedOrderNoticeOfHearingService approvedOrderNoticeOfHearingService;
    @Autowired
    private UploadApprovedOrderService uploadApprovedOrderService;

    private FinremCaseDetails caseDetails;

    @Before
    public void setUp() {
        caseDetails = buildFinremCaseDetails();
        caseDetails.getCaseData().setOrderApprovedJudgeType(JUDGE_TYPE);
        caseDetails.getCaseData().setOrderApprovedJudgeName(JUDGE_NAME);
        caseDetails.getCaseData().setOrderApprovedDate(APPROVED_DATE);

        mapper = new ObjectMapper();
    }

    @Test
    public void givenAboutToStart_whenPrepareFieldsForApprovedLetter_thenRemoveFields() {
        caseDetails.getCaseData().setHearingNoticeDocumentPack(
            Collections.singletonList(DocumentCollection.builder()
                .value(Document.builder().build())
                .build()));
        FinremCaseData caseData = uploadApprovedOrderService.prepareFieldsForOrderApprovedCoverLetter(caseDetails);

        assertThat(caseData.getOrderApprovedJudgeType(), is(nullValue()));
        assertThat(caseData.getOrderApprovedJudgeName(), is(nullValue()));
        assertThat(caseData.getOrderApprovedDate(), is(nullValue()));
        assertThat(caseData.getHearingNoticeDocumentPack(), is(nullValue()));
    }

    @Test
    public void givenNoExceptions_whenHandleUploadApprovedOrderAboutToSubmit_thenReturnValidatedResponse() {
        caseDetails.getCaseData().setUploadHearingOrder(getDirectionOrderCollection());
        setHearingDirectionDetailsCollection(YesOrNo.YES);
        uploadApprovedOrderService.handleUploadApprovedOrderAboutToSubmit(caseDetails, AUTH_TOKEN);

        verify(hearingOrderService, times(1))
            .updateCaseDataForLatestHearingOrderCollection(any(), any());
        verify(genericDocumentService, times(1)).convertDocumentIfNotPdfAlready(isA(Document.class), eq(AUTH_TOKEN));
        verify(contestedOrderApprovedLetterService, times(1))
            .generateAndStoreContestedOrderApprovedLetter(caseDetails, AUTH_TOKEN);
        verify(additionalHearingDocumentService, times(1))
            .createAndStoreAdditionalHearingDocumentsFromApprovedOrder(AUTH_TOKEN, caseDetails);
        verify(hearingOrderService, times(1))
            .appendLatestDraftDirectionOrderToJudgesAmendedDirectionOrders(caseDetails);
        verify(approvedOrderNoticeOfHearingService, times(1))
            .createAndStoreHearingNoticeDocumentPack(caseDetails, AUTH_TOKEN);
    }

    @Test
    public void givenNoExceptions_whenHandleAboutToSubmitAndNoNextHearing_thenDoNotGenerateDocumentPack() {
        caseDetails.getCaseData().setUploadHearingOrder(getDirectionOrderCollection());
        setHearingDirectionDetailsCollection(YesOrNo.NO);
        uploadApprovedOrderService.handleUploadApprovedOrderAboutToSubmit(caseDetails, AUTH_TOKEN);

        verify(hearingOrderService, times(1))
            .updateCaseDataForLatestHearingOrderCollection(any(), any());
        verify(genericDocumentService, times(1)).convertDocumentIfNotPdfAlready(isA(Document.class), eq(AUTH_TOKEN));
        verify(contestedOrderApprovedLetterService, times(1))
            .generateAndStoreContestedOrderApprovedLetter(caseDetails, AUTH_TOKEN);
        verify(additionalHearingDocumentService, times(1))
            .createAndStoreAdditionalHearingDocumentsFromApprovedOrder(AUTH_TOKEN, caseDetails);
        verify(hearingOrderService, times(1))
            .appendLatestDraftDirectionOrderToJudgesAmendedDirectionOrders(caseDetails);
        verify(approvedOrderNoticeOfHearingService, never())
            .createAndStoreHearingNoticeDocumentPack(caseDetails, AUTH_TOKEN);
    }

    @Test
    public void givenExceptions_whenHandleUploadApprovedOrderAboutToSubmit_thenReturnResponseWithErrors() {
        doThrow(new CourtDetailsParseException()).when(additionalHearingDocumentService)
            .createAndStoreAdditionalHearingDocumentsFromApprovedOrder(AUTH_TOKEN, caseDetails);
        caseDetails.getCaseData().setUploadHearingOrder(getDirectionOrderCollection());

        AboutToStartOrSubmitCallbackResponse response = uploadApprovedOrderService
            .handleUploadApprovedOrderAboutToSubmit(caseDetails, AUTH_TOKEN);

        assertThat(response.getErrors(), hasSize(1));
        assertEquals(response.getErrors().get(0), COURT_DETAILS_PARSE_EXCEPTION_MESSAGE);

        verify(hearingOrderService, times(1))
            .updateCaseDataForLatestHearingOrderCollection(any(), any());
        verify(genericDocumentService, times(1)).convertDocumentIfNotPdfAlready(isA(Document.class), eq(AUTH_TOKEN));
        verify(contestedOrderApprovedLetterService, times(1))
            .generateAndStoreContestedOrderApprovedLetter(caseDetails, AUTH_TOKEN);
    }

    private List<DirectionOrderCollection> getDirectionOrderCollection() {
        DirectionOrder directionOrder = DirectionOrder.builder()
            .uploadDraftDocument(Document.builder()
                .filename("directionOrder.pdf").binaryUrl("aBinaryurl").build()).build();

        return Collections.singletonList(DirectionOrderCollection.builder().value(directionOrder).build());
    }

    private void setHearingDirectionDetailsCollection(YesOrNo value) {
        caseDetails.getCaseData().setHearingDirectionDetailsCollection(buildAdditionalHearingDetailsCollection(value));
    }

    private List<HearingDirectionDetailsCollection> buildAdditionalHearingDetailsCollection(YesOrNo value) {
        return List.of(HearingDirectionDetailsCollection.builder().value(HearingDirectionDetail.builder()
            .isAnotherHearingYN(value).build()).build());
    }
}
