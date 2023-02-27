package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicRadioList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicRadioListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Organisation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrganisationPolicy;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerFourWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerOneWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerThreeWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerTwoWrapper;

import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.INTERVENER_FOUR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.INTERVENER_ONE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.INTERVENER_THREE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.INTERVENER_TWO;

@RunWith(MockitoJUnitRunner.class)
public class IntervenerServiceTest extends BaseServiceTest {

    public static final String AUTH_TOKEN = "tokien:)";

    @InjectMocks
    private IntervenerService service;

    public IntervenerServiceTest() {
    }

    @Test
    public void givenCase_whenRemoveOperationChoosenForIntv1_thenRemoveIntervener1() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();

        FinremCaseData finremCaseData = finremCallbackRequest.getCaseDetails().getData();
        IntervenerOneWrapper oneWrapper = IntervenerOneWrapper
            .builder().intervener1Name("One name").intervener1Email("test@test.com").build();
        finremCaseData.setIntervenerOneWrapper(oneWrapper);

        service.removeIntervenerOneDetails(finremCaseData);

        assertNull(finremCaseData.getIntervenerOneWrapper().getIntervener1Name());
        assertNull(finremCaseData.getIntervenerOneWrapper().getIntervener1Email());
        assertNull(finremCaseData.getIntervenerOneWrapper().getIntervener1Phone());
    }


    @Test
    public void givenCase_whenRemoveOperationChoosenForIntv2_thenRemoveIntervener2() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();

        FinremCaseData finremCaseData = finremCallbackRequest.getCaseDetails().getData();
        IntervenerTwoWrapper twoWrapper = IntervenerTwoWrapper
            .builder().intervener2Name("Two name").intervener2Email("test@test.com").build();
        finremCaseData.setIntervenerTwoWrapper(twoWrapper);

        service.removeIntervenerTwoDetails(finremCaseData);

        assertNull(finremCaseData.getIntervenerTwoWrapper().getIntervener2Name());
        assertNull(finremCaseData.getIntervenerTwoWrapper().getIntervener2Email());
    }

    @Test
    public void givenCase_whenRemoveOperationChoosenForIntv3_thenRemoveIntervener3() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();

        FinremCaseData finremCaseData = finremCallbackRequest.getCaseDetails().getData();
        IntervenerThreeWrapper threeWrapper = IntervenerThreeWrapper
            .builder().intervener3Name("Three name").intervener3Email("test@test.com").build();
        finremCaseData.setIntervenerThreeWrapper(threeWrapper);

        service.removeIntervenerThreeDetails(finremCaseData);

        assertNull(finremCaseData.getIntervenerThreeWrapper().getIntervener3Name());
        assertNull(finremCaseData.getIntervenerThreeWrapper().getIntervener3Email());
    }


    @Test
    public void givenCase_whenRemoveOperationChoosenForIntv4_thenRemoveIntervener4() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();

        FinremCaseData finremCaseData = finremCallbackRequest.getCaseDetails().getData();
        IntervenerFourWrapper fourWrapper = IntervenerFourWrapper
            .builder().intervener4Name("Four name").intervener4Email("test@test.com").build();
        finremCaseData.setIntervenerFourWrapper(fourWrapper);

        service.removeIntervenerFourDetails(finremCaseData);

        assertNull(finremCaseData.getIntervenerFourWrapper().getIntervener4Name());
        assertNull(finremCaseData.getIntervenerFourWrapper().getIntervener4Email());
    }

    @Test
    public void givenContestedCase_whenAddingIntervener1AndIntv1NotRepresent_thenSetIntvenerDateAddedAndDefaultOrg() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseData finremCaseData = finremCallbackRequest.getCaseDetails().getData();
        OrganisationPolicy organisationPolicy = OrganisationPolicy.builder().organisation(
            Organisation.builder().organisationID(null).organisationName(null).build()
        ).build();
        IntervenerOneWrapper oneWrapper = IntervenerOneWrapper
            .builder().intervener1Name("One name").intervener1Email("test@test.com")
            .intervener1Represented(YesOrNo.NO)
            .intervener1Organisation(organisationPolicy).build();
        finremCaseData.setIntervenerOneWrapper(oneWrapper);

        DynamicRadioListElement option = DynamicRadioListElement.builder().code(INTERVENER_ONE).build();
        List<DynamicRadioListElement> list = List.of(option);
        DynamicRadioList dynamicRadioList = DynamicRadioList.builder().listItems(list).build();
        finremCaseData.setIntervenersList(dynamicRadioList);
        DynamicRadioListElement option1 = DynamicRadioListElement.builder().code(INTERVENER_ONE).build();
        finremCaseData.getIntervenersList().setValue(option1);

        service.setIntvenerDateAddedAndDefaultOrgIfNotRepresented(finremCaseData,123L);

        assertNotNull(finremCaseData.getIntervenerOneWrapper().getIntervener1DateAdded());
        assertNotNull(finremCaseData.getIntervenerOneWrapper().getIntervener1Organisation().getOrgPolicyCaseAssignedRole());
    }

    @Test
    public void givenContestedCase_whenAddingIntervener2AndIntv2NotRepresent_thenSetIntvenerDateAddedAndDefaultOrg() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseData finremCaseData = finremCallbackRequest.getCaseDetails().getData();
        OrganisationPolicy organisationPolicy = OrganisationPolicy.builder().organisation(
            Organisation.builder().organisationID(null).organisationName(null).build()
        ).build();
        IntervenerTwoWrapper wrapper = IntervenerTwoWrapper
            .builder().intervener2Name("Two name").intervener2Email("test@test.com")
            .intervener2Represented(YesOrNo.NO)
            .intervener2Organisation(organisationPolicy).build();
        finremCaseData.setIntervenerTwoWrapper(wrapper);

        DynamicRadioListElement option = DynamicRadioListElement.builder().code(INTERVENER_TWO).build();
        List<DynamicRadioListElement> list = List.of(option);
        DynamicRadioList dynamicRadioList = DynamicRadioList.builder().listItems(list).build();
        finremCaseData.setIntervenersList(dynamicRadioList);
        DynamicRadioListElement option1 = DynamicRadioListElement.builder().code(INTERVENER_TWO).build();
        finremCaseData.getIntervenersList().setValue(option1);

        service.setIntvenerDateAddedAndDefaultOrgIfNotRepresented(finremCaseData,123L);

        assertNotNull(finremCaseData.getIntervenerTwoWrapper().getIntervener2DateAdded());
        assertNotNull(finremCaseData.getIntervenerTwoWrapper().getIntervener2Organisation().getOrgPolicyCaseAssignedRole());
    }


    @Test
    public void givenContestedCase_whenAddingIntervener3AndIntv3NotRepresent_thenSetIntvenerDateAddedAndDefaultOrg() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseData finremCaseData = finremCallbackRequest.getCaseDetails().getData();
        OrganisationPolicy organisationPolicy = OrganisationPolicy.builder().organisation(
            Organisation.builder().organisationID(null).organisationName(null).build()
        ).build();
        IntervenerThreeWrapper wrapper = IntervenerThreeWrapper
            .builder().intervener3Name("Two name").intervener3Email("test@test.com")
            .intervener3Represented(YesOrNo.NO)
            .intervener3Organisation(organisationPolicy).build();
        finremCaseData.setIntervenerThreeWrapper(wrapper);

        DynamicRadioListElement option = DynamicRadioListElement.builder().code(INTERVENER_THREE).build();
        List<DynamicRadioListElement> list = List.of(option);
        DynamicRadioList dynamicRadioList = DynamicRadioList.builder().listItems(list).build();
        finremCaseData.setIntervenersList(dynamicRadioList);
        DynamicRadioListElement option1 = DynamicRadioListElement.builder().code(INTERVENER_THREE).build();
        finremCaseData.getIntervenersList().setValue(option1);

        service.setIntvenerDateAddedAndDefaultOrgIfNotRepresented(finremCaseData,123L);

        assertNotNull(finremCaseData.getIntervenerThreeWrapper().getIntervener3DateAdded());
        assertNotNull(finremCaseData.getIntervenerThreeWrapper().getIntervener3Organisation().getOrgPolicyCaseAssignedRole());
    }

    @Test
    public void givenContestedCase_whenAddingIntervener4AndIntv4NotRepresent_thenSetIntvenerDateAddedAndDefaultOrg() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseData finremCaseData = finremCallbackRequest.getCaseDetails().getData();
        OrganisationPolicy organisationPolicy = OrganisationPolicy.builder().organisation(
            Organisation.builder().organisationID(null).organisationName(null).build()
        ).build();
        IntervenerFourWrapper wrapper = IntervenerFourWrapper
            .builder().intervener4Name("Two name").intervener4Email("test@test.com")
            .intervener4Represented(YesOrNo.NO)
            .intervener4Organisation(organisationPolicy).build();
        finremCaseData.setIntervenerFourWrapper(wrapper);

        DynamicRadioListElement option = DynamicRadioListElement.builder().code(INTERVENER_FOUR).build();
        List<DynamicRadioListElement> list = List.of(option);
        DynamicRadioList dynamicRadioList = DynamicRadioList.builder().listItems(list).build();
        finremCaseData.setIntervenersList(dynamicRadioList);
        DynamicRadioListElement option1 = DynamicRadioListElement.builder().code(INTERVENER_FOUR).build();
        finremCaseData.getIntervenersList().setValue(option1);

        service.setIntvenerDateAddedAndDefaultOrgIfNotRepresented(finremCaseData,123L);

        assertNotNull(finremCaseData.getIntervenerFourWrapper().getIntervener4DateAdded());
        assertNotNull(finremCaseData.getIntervenerFourWrapper().getIntervener4Organisation().getOrgPolicyCaseAssignedRole());
    }


    private FinremCallbackRequest buildCallbackRequest() {
        return FinremCallbackRequest
            .builder()
            .eventType(EventType.MANAGE_INTERVENERS)
            .caseDetails(FinremCaseDetails.builder().id(123L).caseType(CONTESTED)
                .data(new FinremCaseData()).build())
            .build();
    }
}