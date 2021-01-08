package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.InvalidCaseDataException;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CollectionElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DraftDirectionOrder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.BINARY_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.FILE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.DRAFT_DIRECTION_ORDER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_ORDER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LATEST_DRAFT_HEARING_ORDER;

public class HearingOrderServiceTest extends BaseServiceTest {

    private static final String DRAFT_DIRECTION_ORDER_BIN_URL = "anyDraftDirectionOrderBinaryUrl";
    private static final String FILENAME_ENDING_WITH_DOCX = "filename_ending_with.docx";

    @Autowired HearingOrderService hearingOrderService;

    @MockBean private GenericDocumentService genericDocumentService;

    @Test
    public void convertPdfDocument() {
        when(genericDocumentService.stampDocument(any(), eq(AUTH_TOKEN))).thenReturn(caseDocument());

        List<CollectionElement<DraftDirectionOrder>> draftDirectionOrderCollection = new ArrayList<>();
        draftDirectionOrderCollection.add(CollectionElement.<DraftDirectionOrder>builder()
            .value(DraftDirectionOrder.builder().uploadDraftDocument(CaseDocument.builder()
                .documentBinaryUrl(DRAFT_DIRECTION_ORDER_BIN_URL)
                .documentFilename(FILENAME_ENDING_WITH_DOCX)
                .build()).build()).build());

        Map<String, Object> caseData = prepareCaseData(draftDirectionOrderCollection);
        CaseDetails caseDetails = CaseDetails.builder().data(caseData).build();

        hearingOrderService.convertToPdfAndStampAndStoreLatestDraftHearingOrder(caseDetails, AUTH_TOKEN);

        verify(genericDocumentService, times(1)).stampDocument(any(), eq(AUTH_TOKEN));

        CaseDocument latestDraftHearingOrder = (CaseDocument) caseData.get(LATEST_DRAFT_HEARING_ORDER);
        assertThat(latestDraftHearingOrder, is(notNullValue()));
        assertThat(latestDraftHearingOrder.getDocumentFilename(), is(FILE_NAME));
        assertThat(latestDraftHearingOrder.getDocumentBinaryUrl(), is(BINARY_URL));

        List<CollectionElement<DirectionOrder>> hearingOrderCollection = (List<CollectionElement<DirectionOrder>>) caseDetails.getData()
            .get(HEARING_ORDER_COLLECTION);
        assertThat(hearingOrderCollection, hasSize(1));
        assertThat(hearingOrderCollection.get(0).getValue().getUploadDraftDocument().getDocumentBinaryUrl(), is(BINARY_URL));
    }

    @Test(expected = InvalidCaseDataException.class)
    public void throwsExceptionIfNoDocumentFound() {
        CaseDetails emptyCaseDetails = CaseDetails.builder().data(new HashMap<>()).build();

        hearingOrderService.convertToPdfAndStampAndStoreLatestDraftHearingOrder(emptyCaseDetails, AUTH_TOKEN);
    }

    private Map<String, Object> prepareCaseData(List<CollectionElement<DraftDirectionOrder>> draftDirectionOrders) {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(DRAFT_DIRECTION_ORDER_COLLECTION, draftDirectionOrders);
        caseData.put(HEARING_ORDER_COLLECTION, null);
        return convertCaseDataToStringRepresentation(caseData);
    }
}
