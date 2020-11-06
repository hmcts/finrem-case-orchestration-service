package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;

import java.util.Collection;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_ORDER_COLLECTION;

public class MoveCollectionServiceTest {
    MoveCollectionService moveCollectionService = new MoveCollectionService();

    @Test
    public void shouldSuccessfullyMoveValues() throws Exception {
        Map<String, Object> caseData = TestSetUpUtils.caseDataWithUploadHearingOrder();

        moveCollectionService.moveCollection(caseData,HEARING_ORDER_COLLECTION, "uploadHearingOrderRO");

        assertThat(((Collection<CaseDocument>)caseData.get("uploadHearingOrderRO")), hasSize(3));
        assertThat(caseData.get(HEARING_ORDER_COLLECTION), nullValue());
    }

    @Test
    public void shouldSuccessfullyMoveValuesToNewCollections() throws Exception {
        Map<String, Object> caseData = TestSetUpUtils.caseDataWithUploadHearingOrder();
        caseData.put("uploadHearingOrderRO", null);
        moveCollectionService.moveCollection(caseData,HEARING_ORDER_COLLECTION, "uploadHearingOrderRO");

        assertThat(((Collection<CaseDocument>)caseData.get("uploadHearingOrderRO")), hasSize(1));
        assertThat(caseData.get(HEARING_ORDER_COLLECTION), nullValue());
    }

    @Test
    public void shouldDoNothingWithNonArraySourceValueMove() throws Exception {
        Map<String, Object> caseData = TestSetUpUtils.caseDataWithUploadHearingOrder();
        caseData.put(HEARING_ORDER_COLLECTION, "nonarrayValue");
        moveCollectionService.moveCollection(caseData, HEARING_ORDER_COLLECTION, "uploadHearingOrderRO");

        assertThat(((Collection<CaseDocument>)caseData.get("uploadHearingOrderRO")), hasSize(2));
        assertThat(caseData.get(HEARING_ORDER_COLLECTION), is("nonarrayValue"));
    }

    @Test
    public void shouldDoNothingWithNonArrayDestinationValueMove() throws Exception {
        Map<String, Object> caseData = TestSetUpUtils.caseDataWithUploadHearingOrder();
        caseData.put("uploadHearingOrderRO", "nonarrayValue");
        moveCollectionService.moveCollection(caseData, HEARING_ORDER_COLLECTION, "uploadHearingOrderRO");

        assertThat(caseData.get("uploadHearingOrderRO"), is("nonarrayValue"));
        assertThat(((Collection<CaseDocument>)caseData.get(HEARING_ORDER_COLLECTION)), hasSize(1));
    }

    @Test
    public void shouldDoNothingWhenSourceIsEmptyMove() throws Exception {
        Map<String, Object> caseData = TestSetUpUtils.caseDataWithUploadHearingOrder();
        caseData.put(HEARING_ORDER_COLLECTION, null);
        moveCollectionService.moveCollection(caseData, HEARING_ORDER_COLLECTION, "uploadHearingOrderRO");

        assertThat(((Collection<CaseDocument>)caseData.get("uploadHearingOrderRO")), hasSize(2));
        assertThat(caseData.get(HEARING_ORDER_COLLECTION), nullValue());
    }
}
