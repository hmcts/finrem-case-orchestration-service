package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionDetailsCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionDetailsCollectionData;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_SOLICITOR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.DIRECTION_DETAILS_COLLECTION_CT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_ORDER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_SOLICITOR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_RESPONSIBLE_FOR_DRAFTING_ORDER;

public class CaseDataServiceTest extends BaseServiceTest {

    @Autowired CaseDataService caseDataService;

    @Test
    public void isRespondentSolicitorResponsibleToDraftOrder_shouldReturnTrue() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(SOLICITOR_RESPONSIBLE_FOR_DRAFTING_ORDER, RESPONDENT_SOLICITOR);
        assertTrue(caseDataService.isRespondentSolicitorResponsibleToDraftOrder(caseData));
    }

    @Test
    public void isRespondentSolicitorResponsibleToDraftOrder_appSolicitor() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(SOLICITOR_RESPONSIBLE_FOR_DRAFTING_ORDER, APPLICANT_SOLICITOR);
        assertFalse(caseDataService.isRespondentSolicitorResponsibleToDraftOrder(caseData));
    }

    @Test
    public void isRespondentSolicitorResponsibleToDraftOrder_fieldNotExist() {
        Map<String, Object> caseData = new HashMap<>();
        assertFalse(caseDataService.isRespondentSolicitorResponsibleToDraftOrder(caseData));
    }

    @Test
    public void shouldSuccessfullyMoveValues() {
        Map<String, Object> caseData = TestSetUpUtils.caseDataWithUploadHearingOrder();

        caseDataService.moveCollection(caseData, HEARING_ORDER_COLLECTION, "uploadHearingOrderRO");

        assertThat(((Collection<CaseDocument>)caseData.get("uploadHearingOrderRO")), hasSize(3));
        assertThat(caseData.get(HEARING_ORDER_COLLECTION), Matchers.nullValue());
    }

    @Test
    public void shouldSuccessfullyMoveValuesToNewCollections() {
        Map<String, Object> caseData = TestSetUpUtils.caseDataWithUploadHearingOrder();
        caseData.put("uploadHearingOrderRO", null);
        caseDataService.moveCollection(caseData, HEARING_ORDER_COLLECTION, "uploadHearingOrderRO");

        assertThat(((Collection<CaseDocument>)caseData.get("uploadHearingOrderRO")), hasSize(1));
        assertThat(caseData.get(HEARING_ORDER_COLLECTION), Matchers.nullValue());
    }

    @Test
    public void shouldDoNothingWithNonArraySourceValueMove() {
        Map<String, Object> caseData = TestSetUpUtils.caseDataWithUploadHearingOrder();
        caseData.put(HEARING_ORDER_COLLECTION, "nonarrayValue");
        caseDataService.moveCollection(caseData, HEARING_ORDER_COLLECTION, "uploadHearingOrderRO");

        assertThat(((Collection<CaseDocument>)caseData.get("uploadHearingOrderRO")), hasSize(2));
        assertThat(caseData.get(HEARING_ORDER_COLLECTION), Matchers.is("nonarrayValue"));
    }

    @Test
    public void shouldDoNothingWithNonArrayDestinationValueMove() {
        Map<String, Object> caseData = TestSetUpUtils.caseDataWithUploadHearingOrder();
        caseData.put("uploadHearingOrderRO", "nonarrayValue");
        caseDataService.moveCollection(caseData, HEARING_ORDER_COLLECTION, "uploadHearingOrderRO");

        assertThat(caseData.get("uploadHearingOrderRO"), Matchers.is("nonarrayValue"));
        assertThat(((Collection<CaseDocument>)caseData.get(HEARING_ORDER_COLLECTION)), hasSize(1));
    }

    @Test
    public void shouldDoNothingWhenSourceIsEmptyMove() {
        Map<String, Object> caseData = TestSetUpUtils.caseDataWithUploadHearingOrder();
        caseData.put(HEARING_ORDER_COLLECTION, null);
        caseDataService.moveCollection(caseData, HEARING_ORDER_COLLECTION, "uploadHearingOrderRO");

        assertThat(((Collection<CaseDocument>)caseData.get("uploadHearingOrderRO")), hasSize(2));
        assertThat(caseData.get(HEARING_ORDER_COLLECTION), Matchers.nullValue());
    }

    @Test
    public void hasAnotherHearing_shouldReturnTrue() {
        Map<String, Object> caseData = new HashMap<>();
        DirectionDetailsCollection directionDetailsCollection = DirectionDetailsCollection.builder().isAnotherHearingYN(YES_VALUE).build();
        DirectionDetailsCollectionData directionDetailsCollectionData
            = DirectionDetailsCollectionData.builder().directionDetailsCollection(directionDetailsCollection).build();
        List<DirectionDetailsCollectionData> directionDetailsCollectionList = Arrays.asList(directionDetailsCollectionData);
        caseData.put(DIRECTION_DETAILS_COLLECTION_CT, directionDetailsCollectionList);

        assertTrue(caseDataService.hasAnotherHearing(caseData));
    }

    @Test
    public void hasAnotherHearing_noDirectionDetails() {
        Map<String, Object> caseData = new HashMap<>();
        List<DirectionDetailsCollectionData> directionDetailsCollectionList = Arrays.asList();
        caseData.put(DIRECTION_DETAILS_COLLECTION_CT, directionDetailsCollectionList);

        assertFalse(caseDataService.hasAnotherHearing(caseData));
    }

    @Test
    public void hasAnotherHearing_noNextHearing() {
        Map<String, Object> caseData = new HashMap<>();
        DirectionDetailsCollection directionDetailsCollection = DirectionDetailsCollection.builder().isAnotherHearingYN(NO_VALUE).build();
        DirectionDetailsCollectionData directionDetailsCollectionData
            = DirectionDetailsCollectionData.builder().directionDetailsCollection(directionDetailsCollection).build();
        List<DirectionDetailsCollectionData> directionDetailsCollectionList = Arrays.asList(directionDetailsCollectionData);
        caseData.put(DIRECTION_DETAILS_COLLECTION_CT, directionDetailsCollectionList);

        assertFalse(caseDataService.hasAnotherHearing(caseData));
    }
}
