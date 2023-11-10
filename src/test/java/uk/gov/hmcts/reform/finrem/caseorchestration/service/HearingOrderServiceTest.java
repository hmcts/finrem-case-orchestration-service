package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.InvalidCaseDataException;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CollectionElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DraftDirectionOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DraftDirectionOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.State;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DraftDirectionWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory.DraftOrderDocumentCategoriser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.BINARY_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.FILE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.DRAFT_DIRECTION_ORDER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FINAL_ORDER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_ORDER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.JUDGES_AMENDED_DIRECTION_ORDER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LATEST_DRAFT_DIRECTION_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LATEST_DRAFT_HEARING_ORDER;

@ExtendWith(MockitoExtension.class)
public class HearingOrderServiceTest extends BaseServiceTest {

    private static final String DRAFT_DIRECTION_ORDER_BIN_URL = "anyDraftDirectionOrderBinaryUrl";
    private static final String FILENAME_ENDING_WITH_DOCX = "filename_ending_with.docx";

    @Mock
    private FinremCaseDetailsMapper finremCaseDetailsMapper;

    @Autowired
    private HearingOrderService hearingOrderService;

    @MockBean
    private GenericDocumentService genericDocumentService;

    @MockBean
    private FeatureToggleService featureToggleService;

    @Autowired
    private DocumentHelper documentHelper;

    @Test
    public void convertPdfDocument() {
        when(featureToggleService.isCaseFileViewEnabled()).thenReturn(true);
        when(genericDocumentService.stampDocument(any(), eq(AUTH_TOKEN), eq(StampType.FAMILY_COURT_STAMP), any()))
            .thenReturn(caseDocument());

        Map<String, Object> mappedCaseData = prepareCaseData(makeDraftDirectionOrderCollectionWithOneElement());
        CaseDetails mappedCaseDetails = CaseDetails.builder().state(State.PREPARE_FOR_HEARING.getStateId())
            .caseTypeId(CaseType.CONTESTED.getCcdType()).id(123L).data(mappedCaseData).build();

        FinremCaseData finremCaseData = prepareCaseDataFinrem();
        FinremCaseDetails finremCaseDetails = FinremCaseDetails.builder().state(State.PREPARE_FOR_HEARING)
            .caseType(CaseType.CONTESTED).id(123L).data(finremCaseData).build();

        when(finremCaseDetailsMapper.mapToFinremCaseDetails(any())).thenReturn(finremCaseDetails);
        when(finremCaseDetailsMapper.mapToCaseDetails(any())).thenReturn(mappedCaseDetails);

        Map<String, Object> caseData = prepareCaseData(makeDraftDirectionOrderCollectionWithOneElement());
        CaseDetails caseDetails = CaseDetails.builder().state(State.PREPARE_FOR_HEARING.getStateId())
            .caseTypeId(CaseType.CONTESTED.getCcdType()).id(123L).data(caseData).build();

        hearingOrderService.convertToPdfAndStampAndStoreLatestDraftHearingOrder(caseDetails, AUTH_TOKEN);

        verify(genericDocumentService).stampDocument(any(), eq(AUTH_TOKEN), eq(StampType.FAMILY_COURT_STAMP), any());

        CaseDocument latestDraftHearingOrder = (CaseDocument) caseData.get(LATEST_DRAFT_HEARING_ORDER);
        assertThat(latestDraftHearingOrder, is(notNullValue()));
        assertThat(latestDraftHearingOrder.getDocumentFilename(), is(FILE_NAME));
        assertThat(latestDraftHearingOrder.getDocumentBinaryUrl(), is(BINARY_URL));

        List<CollectionElement<DirectionOrder>> hearingOrderCollection = convertToListOfDirectionOrder(
            hearingOrderService.convertToPdfAndStampAndStoreLatestDraftHearingOrder(caseDetails, AUTH_TOKEN).getData()
            .get(HEARING_ORDER_COLLECTION));
        assertThat(hearingOrderCollection, hasSize(2));
        assertThat(hearingOrderCollection.get(0).getValue().getUploadDraftDocument().getDocumentBinaryUrl(), is(BINARY_URL));

        hearingOrderCollection.forEach(order -> assertThat(order.getValue().getUploadDraftDocument().getCategoryId(),is(DocumentCategory.HEARING_NOTICES.getDocumentCategoryId())));

        List<CollectionElement<DirectionOrder>> finalOrderCollection = convertToListOfDirectionOrder(
            hearingOrderService.convertToPdfAndStampAndStoreLatestDraftHearingOrder(caseDetails, AUTH_TOKEN).getData()
            .get(FINAL_ORDER_COLLECTION));
        finalOrderCollection.forEach(order -> assertThat(order.getValue().getUploadDraftDocument().getCategoryId(),is(DocumentCategory.APPROVED_ORDERS_CASE.getDocumentCategoryId())));
    }

    @Test(expected = InvalidCaseDataException.class)
    public void throwsExceptionIfNoDocumentFound() {
        CaseDetails emptyCaseDetails = CaseDetails.builder().data(new HashMap<>()).build();

        hearingOrderService.convertToPdfAndStampAndStoreLatestDraftHearingOrder(emptyCaseDetails, AUTH_TOKEN);
    }

    @Test
    public void whenLatestDraftDirOrderIsSameAsLastDraftOrder_itDoesntOverrideIt() {
        List<CollectionElement<DraftDirectionOrder>> draftDirectionOrders = makeDraftDirectionOrderCollectionWithOneElement();
        Map<String, Object> caseData = prepareCaseData(draftDirectionOrders);
        caseData.put(LATEST_DRAFT_DIRECTION_ORDER, DraftDirectionOrder.builder()
            .uploadDraftDocument(draftDirectionOrders.get(0).getValue().getUploadDraftDocument())
            .purposeOfDocument(draftDirectionOrders.get(0).getValue().getPurposeOfDocument())
            .build());

        assertThat(hearingOrderService.latestDraftDirectionOrderOverridesSolicitorCollection(
            CaseDetails.builder().id(123L)
                .data(caseData).build(), AUTH_TOKEN), is(true));
    }

    @Test
    public void whenLatestDraftDirOrderIsDifferentThanLastDraftOrder_itDoesOverrideIt() {
        Map<String, Object> caseData = prepareCaseData(makeDraftDirectionOrderCollectionWithOneElement());
        caseData.put(LATEST_DRAFT_DIRECTION_ORDER, DraftDirectionOrder.builder()
            .uploadDraftDocument(caseDocument())
            .purposeOfDocument("some other purpose")
            .build());

        assertThat(hearingOrderService.latestDraftDirectionOrderOverridesSolicitorCollection(
            CaseDetails.builder().id(123L)
                .data(caseData).build(), AUTH_TOKEN), is(true));
    }

    @Test
    public void appendLatestDraftDirectionOrderToJudgesAmendedDirectionOrders() {
        Map<String, Object> caseData = new HashMap<>();
        DraftDirectionOrder latestDraftDirectionOrder = makeDraftDirectionOrder();
        caseData.put(LATEST_DRAFT_DIRECTION_ORDER, latestDraftDirectionOrder);

        hearingOrderService.appendLatestDraftDirectionOrderToJudgesAmendedDirectionOrders(CaseDetails.builder().data(caseData).build());

        assertThat(((List<CollectionElement>) caseData.get(JUDGES_AMENDED_DIRECTION_ORDER_COLLECTION)).get(0).getValue(),
            is(latestDraftDirectionOrder));
    }

    private List<CollectionElement<DraftDirectionOrder>> makeDraftDirectionOrderCollectionWithOneElement() {
        List<CollectionElement<DraftDirectionOrder>> draftDirectionOrderCollection = new ArrayList<>();

        draftDirectionOrderCollection.add(CollectionElement.<DraftDirectionOrder>builder()
            .value(makeDraftDirectionOrder())
            .build());

        return draftDirectionOrderCollection;
    }

    private DraftDirectionOrder makeDraftDirectionOrder() {
        return DraftDirectionOrder.builder().uploadDraftDocument(CaseDocument.builder()
            .documentBinaryUrl(DRAFT_DIRECTION_ORDER_BIN_URL)
            .documentFilename(FILENAME_ENDING_WITH_DOCX)
            .build()).build();
    }

    private Map<String, Object> prepareCaseData(List<CollectionElement<DraftDirectionOrder>> draftDirectionOrders) {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(DRAFT_DIRECTION_ORDER_COLLECTION, draftDirectionOrders);
        caseData.put(HEARING_ORDER_COLLECTION, null);
        return convertCaseDataToStringRepresentation(caseData);
    }

    private FinremCaseData prepareCaseDataFinrem() {
        DraftDirectionOrder draftDirectionOrder = DraftDirectionOrder.builder().uploadDraftDocument(CaseDocument.builder()
            .documentBinaryUrl(DRAFT_DIRECTION_ORDER_BIN_URL)
            .documentFilename(FILENAME_ENDING_WITH_DOCX).build()).build();

        List<DraftDirectionOrderCollection> draftDirectionOrderCollection = List.of(DraftDirectionOrderCollection.builder()
            .value(draftDirectionOrder).build());
        DraftDirectionWrapper wrapper = DraftDirectionWrapper.builder().draftDirectionOrderCollection(draftDirectionOrderCollection).build();
        FinremCaseData caseData = FinremCaseData.builder().draftDirectionWrapper(wrapper).build();

        return caseData;
    }

    private List<CollectionElement<DirectionOrder>> convertToListOfDirectionOrder(Object value) {
        return new ObjectMapper().convertValue(value, new TypeReference<>() {
        });
    }
}