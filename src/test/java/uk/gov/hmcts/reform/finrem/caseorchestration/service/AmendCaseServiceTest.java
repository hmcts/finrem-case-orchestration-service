package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChildDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChildDetailsCollectionElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Schedule1OrMatrimonialAndCpList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;

class AmendCaseServiceTest {

    private AmendCaseService amendCaseService;

    @BeforeEach
    void setUp() {
        amendCaseService = new AmendCaseService();
    }

    @Test
    void givenContestedCase_whenTypeOfApplicationFieldMissing_thenDefaultsToMatrimoninalType() {
        FinremCaseDetails finremCaseDetails = getFinremCaseDataDetails();
        amendCaseService.addApplicationType(finremCaseDetails.getData());

        assertEquals(Schedule1OrMatrimonialAndCpList.MATRIMONIAL_AND_CIVIL_PARTNERSHIP_PROCEEDINGS.getValue(),
            finremCaseDetails.getData().getScheduleOneWrapper().getTypeOfApplication().getValue());
    }

    @Test
    void givenContestedCase_whenTypeOfApplicationFieldIsNotMissing_thenDoNotUpdate() {
        FinremCaseDetails finremCaseDetails = getFinremCaseDataDetails();
        FinremCaseData data = finremCaseDetails.getData();
        data.getScheduleOneWrapper()
            .setTypeOfApplication(Schedule1OrMatrimonialAndCpList.SCHEDULE_1_CHILDREN_ACT_1989);
        amendCaseService.addApplicationType(data);

        assertEquals(Schedule1OrMatrimonialAndCpList.SCHEDULE_1_CHILDREN_ACT_1989.getValue(),
            finremCaseDetails.getData().getScheduleOneWrapper().getTypeOfApplication().getValue());
    }

    @Test
    void givenContestedCase_whenTypeOfApplicationFieldIsMissingForSchedule1App_thenDoNotUpdate() {
        FinremCaseDetails finremCaseDetails = getFinremCaseDataDetails();
        FinremCaseData data = finremCaseDetails.getData();
        List<ChildDetailsCollectionElement> childrenCollection = new ArrayList<>();
        childrenCollection.add(ChildDetailsCollectionElement.builder()
            .childDetails(ChildDetails.builder().childrenLiveInEnglandOrWales(YesOrNo.YES).build()).build());
        data.getScheduleOneWrapper().setChildrenCollection(childrenCollection);

        amendCaseService.addApplicationType(data);

        assertEquals(Schedule1OrMatrimonialAndCpList.SCHEDULE_1_CHILDREN_ACT_1989.getValue(),
            finremCaseDetails.getData().getScheduleOneWrapper().getTypeOfApplication().getValue());
    }

    private FinremCaseDetails getFinremCaseDataDetails() {
        return FinremCaseDetails.builder().id(123L).caseType(CONTESTED)
                .data(new FinremCaseData()).build();
    }
}