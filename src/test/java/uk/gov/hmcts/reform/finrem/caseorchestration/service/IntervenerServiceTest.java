package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.NoSuchUserException;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.AssertionErrors.assertEquals;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole.INTVR_SOLICITOR_1;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole.INTVR_SOLICITOR_2;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole.INTVR_SOLICITOR_3;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole.INTVR_SOLICITOR_4;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.INTERVENER_FOUR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.INTERVENER_INVALID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.INTERVENER_ONE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.INTERVENER_THREE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.INTERVENER_TWO;

@RunWith(MockitoJUnitRunner.class)
public class IntervenerServiceTest extends BaseServiceTest {

    public static final String AUTH_TOKEN = "tokien:)";
    public static final Long CASE_ID = 123L;

    private static final String INTERVENER_TEST_EMAIL = "test@test.com";
    private static final String INTERVENER_TEST_EMAIL_CHANGE = "test2@test.com";
    private static final String SOME_ORG_ID = "someOrgId";
    private static final String CHANGE_ORG_ID = "changeOrgId";
    private static final String INTERVENER_USER_ID = "intervenerUserId";

    @Mock
    private AssignCaseAccessService assignCaseAccessService;
    @Mock
    private PrdOrganisationService organisationService;
    @Mock
    private SystemUserService systemUserService;

    @InjectMocks
    private IntervenerService service;

    public IntervenerServiceTest() {
    }

    @Test
    public void givenCase_whenRemoveOperationChoosenForIntv1NotRepresented_thenRemoveIntervener1() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();

        FinremCaseData finremCaseData = finremCallbackRequest.getCaseDetails().getData();
        IntervenerOneWrapper oneWrapper = IntervenerOneWrapper
            .builder().intervener1Name("One name").intervener1Email("test@test.com").intervener1Represented(YesOrNo.NO).build();
        finremCaseData.setIntervenerOneWrapper(oneWrapper);

        service.removeIntervenerOneDetails(finremCaseData, CASE_ID);

        assertNull(finremCaseData.getIntervenerOneWrapper().getIntervener1Name());
        assertNull(finremCaseData.getIntervenerOneWrapper().getIntervener1Email());
        assertNull(finremCaseData.getIntervenerOneWrapper().getIntervener1Phone());
    }

    @Test
    public void givenCase_whenRemoveOperationChoosenForIntv1Represented_thenRemoveIntervener1() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();

        FinremCaseData finremCaseData = finremCallbackRequest.getCaseDetails().getData();
        OrganisationPolicy organisationPolicy = OrganisationPolicy.builder().organisation(
            Organisation.builder().organisationID(SOME_ORG_ID).organisationName(SOME_ORG_ID).build()
        ).build();
        IntervenerOneWrapper oneWrapper = IntervenerOneWrapper
            .builder()
            .intervener1Name("One name")
            .intervener1Email("test@test.com")
            .intervener1SolEmail("test@test.com")
            .intervener1Organisation(organisationPolicy)
            .intervener1Represented(YesOrNo.YES).build();
        finremCaseData.setIntervenerOneWrapper(oneWrapper);

        when(systemUserService.getSysUserToken()).thenReturn(AUTH_TOKEN);
        when(organisationService.findUserByEmail(INTERVENER_TEST_EMAIL, AUTH_TOKEN)).thenReturn(Optional.of(INTERVENER_USER_ID));
        service.removeIntervenerOneDetails(finremCaseData, CASE_ID);

        assertNull(finremCaseData.getIntervenerOneWrapper().getIntervener1Name());
        assertNull(finremCaseData.getIntervenerOneWrapper().getIntervener1Email());
        assertNull(finremCaseData.getIntervenerOneWrapper().getIntervener1Phone());
        verify(assignCaseAccessService).removeCaseRoleToUser(CASE_ID, INTERVENER_USER_ID,
            INTVR_SOLICITOR_1.getValue(), SOME_ORG_ID);
    }


    @Test
    public void givenCase_whenRemoveOperationChoosenForIntv2NotRepresented_thenRemoveIntervener2() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();

        FinremCaseData finremCaseData = finremCallbackRequest.getCaseDetails().getData();
        IntervenerTwoWrapper twoWrapper = IntervenerTwoWrapper
            .builder().intervener2Name("Two name").intervener2Email("test@test.com").intervener2Represented(YesOrNo.NO).build();
        finremCaseData.setIntervenerTwoWrapper(twoWrapper);

        service.removeIntervenerTwoDetails(finremCaseData, CASE_ID);

        assertNull(finremCaseData.getIntervenerTwoWrapper().getIntervener2Name());
        assertNull(finremCaseData.getIntervenerTwoWrapper().getIntervener2Email());
        verify(assignCaseAccessService, never()).removeCaseRoleToUser(any(), any(), any(), any());
    }

    @Test
    public void givenCase_whenRemoveOperationChoosenForIntv2Represented_thenRemoveIntervener2() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();

        FinremCaseData finremCaseData = finremCallbackRequest.getCaseDetails().getData();
        OrganisationPolicy organisationPolicy = OrganisationPolicy.builder().organisation(
            Organisation.builder().organisationID(SOME_ORG_ID).organisationName(SOME_ORG_ID).build()
        ).build();
        IntervenerTwoWrapper twoWrapper = IntervenerTwoWrapper
            .builder()
            .intervener2Name("Two name")
            .intervener2Email("test@test.com")
            .intervener2SolEmail("test@test.com")
            .intervener2Organisation(organisationPolicy)
            .intervener2Represented(YesOrNo.YES).build();
        finremCaseData.setIntervenerTwoWrapper(twoWrapper);

        when(systemUserService.getSysUserToken()).thenReturn(AUTH_TOKEN);
        when(organisationService.findUserByEmail(INTERVENER_TEST_EMAIL, AUTH_TOKEN)).thenReturn(Optional.of(INTERVENER_USER_ID));
        service.removeIntervenerTwoDetails(finremCaseData, CASE_ID);

        assertNull(finremCaseData.getIntervenerTwoWrapper().getIntervener2Name());
        assertNull(finremCaseData.getIntervenerTwoWrapper().getIntervener2Email());
        verify(assignCaseAccessService).removeCaseRoleToUser(CASE_ID, INTERVENER_USER_ID,
            INTVR_SOLICITOR_2.getValue(), SOME_ORG_ID);
    }

    @Test
    public void givenCase_whenRemoveOperationChoosenForIntv3NotRepesented_thenRemoveIntervener3() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();

        FinremCaseData finremCaseData = finremCallbackRequest.getCaseDetails().getData();
        IntervenerThreeWrapper threeWrapper = IntervenerThreeWrapper
            .builder().intervener3Name("Three name").intervener3Email("test@test.com").intervener3Represented(YesOrNo.NO).build();
        finremCaseData.setIntervenerThreeWrapper(threeWrapper);

        service.removeIntervenerThreeDetails(finremCaseData, CASE_ID);

        assertNull(finremCaseData.getIntervenerThreeWrapper().getIntervener3Name());
        assertNull(finremCaseData.getIntervenerThreeWrapper().getIntervener3Email());
    }

    @Test
    public void givenCase_whenRemoveOperationChoosenForIntv3Repesented_thenRemoveIntervener3() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();

        FinremCaseData finremCaseData = finremCallbackRequest.getCaseDetails().getData();
        OrganisationPolicy organisationPolicy = OrganisationPolicy.builder().organisation(
            Organisation.builder().organisationID(SOME_ORG_ID).organisationName(SOME_ORG_ID).build()
        ).build();
        IntervenerThreeWrapper threeWrapper = IntervenerThreeWrapper
            .builder().intervener3Name("Three name").intervener3Email("test@test.com")
             .intervener3SolEmail("test@test.com")
            .intervener3Represented(YesOrNo.YES)
            .intervener3Organisation(organisationPolicy)
            .build();
        finremCaseData.setIntervenerThreeWrapper(threeWrapper);

        when(systemUserService.getSysUserToken()).thenReturn(AUTH_TOKEN);
        when(organisationService.findUserByEmail(INTERVENER_TEST_EMAIL, AUTH_TOKEN)).thenReturn(Optional.of(INTERVENER_USER_ID));
        service.removeIntervenerThreeDetails(finremCaseData, CASE_ID);

        assertNull(finremCaseData.getIntervenerThreeWrapper().getIntervener3Name());
        assertNull(finremCaseData.getIntervenerThreeWrapper().getIntervener3Email());
        verify(assignCaseAccessService).removeCaseRoleToUser(CASE_ID, INTERVENER_USER_ID,
            INTVR_SOLICITOR_3.getValue(), SOME_ORG_ID);
    }

    @Test
    public void givenCase_whenRemoveOperationChoosenForIntv4NotRepresented_thenRemoveIntervener4() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();

        FinremCaseData finremCaseData = finremCallbackRequest.getCaseDetails().getData();
        IntervenerFourWrapper fourWrapper = IntervenerFourWrapper
            .builder().intervener4Name("Four name").intervener4Email("test@test.com").intervener4Represented(YesOrNo.NO).build();
        finremCaseData.setIntervenerFourWrapper(fourWrapper);

        service.removeIntervenerFourDetails(finremCaseData, CASE_ID);

        assertNull(finremCaseData.getIntervenerFourWrapper().getIntervener4Name());
        assertNull(finremCaseData.getIntervenerFourWrapper().getIntervener4Email());
    }

    @Test
    public void givenCase_whenRemoveOperationChoosenForIntv4Represented_thenRemoveIntervener4() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();

        FinremCaseData finremCaseData = finremCallbackRequest.getCaseDetails().getData();
        OrganisationPolicy organisationPolicy = OrganisationPolicy.builder().organisation(
            Organisation.builder().organisationID(SOME_ORG_ID).organisationName(SOME_ORG_ID).build()
        ).build();
        IntervenerFourWrapper fourWrapper = IntervenerFourWrapper
            .builder().intervener4Name("Four name").intervener4Email("test@test.com")
            .intervener4Represented(YesOrNo.YES)
            .intervener4SolEmail("test@test.com")
            .intervener4Organisation(organisationPolicy)
            .build();
        finremCaseData.setIntervenerFourWrapper(fourWrapper);

        when(systemUserService.getSysUserToken()).thenReturn(AUTH_TOKEN);
        when(organisationService.findUserByEmail(INTERVENER_TEST_EMAIL, AUTH_TOKEN)).thenReturn(Optional.of(INTERVENER_USER_ID));
        service.removeIntervenerFourDetails(finremCaseData, CASE_ID);

        assertNull(finremCaseData.getIntervenerFourWrapper().getIntervener4Name());
        assertNull(finremCaseData.getIntervenerFourWrapper().getIntervener4Email());
        verify(assignCaseAccessService).removeCaseRoleToUser(CASE_ID, INTERVENER_USER_ID,
            INTVR_SOLICITOR_4.getValue(), SOME_ORG_ID);

    }

    @Test
    public void givenContestedCase_whenAddingIntervener1AndIntv1Represent_thenSetIntvenerDateAddedAndDefaultOrg() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseData finremCaseData = finremCallbackRequest.getCaseDetails().getData();
        OrganisationPolicy organisationPolicy = OrganisationPolicy.builder().organisation(
            Organisation.builder().organisationID(SOME_ORG_ID).organisationName(SOME_ORG_ID).build()
        ).build();
        IntervenerOneWrapper oneWrapper = IntervenerOneWrapper
            .builder().intervener1Name("One name").intervener1Email("test@test.com")
            .intervener1SolEmail("test@test.com")
            .intervener1Represented(YesOrNo.YES)
            .intervener1Organisation(organisationPolicy).build();
        finremCaseData.setIntervenerOneWrapper(oneWrapper);

        DynamicRadioListElement option = DynamicRadioListElement.builder().code(INTERVENER_ONE).build();
        List<DynamicRadioListElement> list = List.of(option);
        DynamicRadioList dynamicRadioList = DynamicRadioList.builder().listItems(list).build();
        finremCaseData.setIntervenersList(dynamicRadioList);
        DynamicRadioListElement option1 = DynamicRadioListElement.builder().code(INTERVENER_ONE).build();
        finremCaseData.getIntervenersList().setValue(option1);

        when(systemUserService.getSysUserToken()).thenReturn(AUTH_TOKEN);
        when(organisationService.findUserByEmail(INTERVENER_TEST_EMAIL, AUTH_TOKEN)).thenReturn(Optional.of(INTERVENER_USER_ID));

        service.setIntvenerDateAddedAndDefaultOrgIfNotRepresented(finremCallbackRequest);

        assertNotNull(finremCaseData.getIntervenerOneWrapper().getIntervener1DateAdded());
        verify(assignCaseAccessService).grantCaseRoleToUser(CASE_ID, INTERVENER_USER_ID,
            INTVR_SOLICITOR_1.getValue(), SOME_ORG_ID);
    }

    @Test
    public void givenContestedCase_whenUpdatingIntervener1AndChangedRepresetationYesToNo_thenHandle() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseData finremCaseData = finremCallbackRequest.getCaseDetails().getData();
        FinremCaseData finremCaseDataBefore = finremCallbackRequest.getCaseDetailsBefore().getData();
        OrganisationPolicy organisationPolicy = OrganisationPolicy.builder().organisation(
            Organisation.builder().organisationID(SOME_ORG_ID).organisationName(SOME_ORG_ID).build()
        ).build();
        IntervenerOneWrapper oneWrapper = IntervenerOneWrapper
            .builder().intervener1Name("One name").intervener1Email("test@test.com")
            .intervener1SolEmail("test@test.com")
            .intervener1Represented(YesOrNo.YES)
            .intervener1DateAdded(LocalDate.of(2023,1,1))
            .intervener1Organisation(organisationPolicy).build();
        finremCaseDataBefore.setIntervenerOneWrapper(oneWrapper);

        IntervenerOneWrapper oneWrapper1 = IntervenerOneWrapper
            .builder().intervener1Name("One name").intervener1Email("test@test.com")
            .intervener1Represented(YesOrNo.NO)
            .intervener1DateAdded(LocalDate.of(2023,1,1))
            .intervener1Organisation(organisationPolicy).build();
        finremCaseData.setIntervenerOneWrapper(oneWrapper1);


        DynamicRadioListElement option = DynamicRadioListElement.builder().code(INTERVENER_ONE).build();
        List<DynamicRadioListElement> list = List.of(option);
        DynamicRadioList dynamicRadioList = DynamicRadioList.builder().listItems(list).build();
        finremCaseData.setIntervenersList(dynamicRadioList);
        DynamicRadioListElement option1 = DynamicRadioListElement.builder().code(INTERVENER_ONE).build();
        finremCaseData.getIntervenersList().setValue(option1);

        when(systemUserService.getSysUserToken()).thenReturn(AUTH_TOKEN);
        when(organisationService.findUserByEmail(INTERVENER_TEST_EMAIL, AUTH_TOKEN)).thenReturn(Optional.of(INTERVENER_USER_ID));

        service.setIntvenerDateAddedAndDefaultOrgIfNotRepresented(finremCallbackRequest);

        IntervenerOneWrapper intervenerOneWrapper = finremCaseData.getIntervenerOneWrapper();

        assertNotNull(intervenerOneWrapper.getIntervener1DateAdded());
        assertNull(intervenerOneWrapper.getIntervener1SolEmail());
        assertNull(intervenerOneWrapper.getIntervener1SolName());
        assertNull(intervenerOneWrapper.getIntervener1SolPhone());
        assertNull(intervenerOneWrapper.getIntervener1Organisation().getOrganisation().getOrganisationID());
        assertNull(intervenerOneWrapper.getIntervener1Organisation().getOrganisation().getOrganisationName());
        verify(assignCaseAccessService).removeCaseRoleToUser(CASE_ID, INTERVENER_USER_ID,
            INTVR_SOLICITOR_1.getValue(), SOME_ORG_ID);
    }


    @Test
    public void givenContestedCase_whenUpdatingIntervener1AndChangedRepresetationSol_thenHandle() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseData finremCaseData = finremCallbackRequest.getCaseDetails().getData();
        FinremCaseData finremCaseDataBefore = finremCallbackRequest.getCaseDetailsBefore().getData();
        OrganisationPolicy organisationPolicy = OrganisationPolicy.builder().organisation(
            Organisation.builder().organisationID(SOME_ORG_ID).organisationName(SOME_ORG_ID).build()
        ).build();
        OrganisationPolicy organisationPolicyChange = OrganisationPolicy.builder().organisation(
            Organisation.builder().organisationID(CHANGE_ORG_ID).organisationName(CHANGE_ORG_ID).build()
        ).build();
        IntervenerOneWrapper oneWrapper = IntervenerOneWrapper
            .builder().intervener1Name("One name").intervener1Email("test@test.com")
            .intervener1SolEmail("test@test.com")
            .intervener1SolName("test test.com")
            .intervener1SolPhone("33333333333")
            .intervener1Represented(YesOrNo.YES)
            .intervener1DateAdded(LocalDate.of(2023,1,1))
            .intervener1Organisation(organisationPolicy).build();
        finremCaseDataBefore.setIntervenerOneWrapper(oneWrapper);

        IntervenerOneWrapper oneWrapper1 = IntervenerOneWrapper
            .builder().intervener1Name("One name").intervener1Email("test@test.com")
            .intervener1SolEmail(INTERVENER_TEST_EMAIL_CHANGE)
            .intervener1SolName("test test.com")
            .intervener1SolPhone("33333333333")
            .intervener1Represented(YesOrNo.YES)
            .intervener1DateAdded(LocalDate.of(2023,1,1))
            .intervener1Organisation(organisationPolicyChange).build();
        finremCaseData.setIntervenerOneWrapper(oneWrapper1);


        DynamicRadioListElement option = DynamicRadioListElement.builder().code(INTERVENER_ONE).build();
        List<DynamicRadioListElement> list = List.of(option);
        DynamicRadioList dynamicRadioList = DynamicRadioList.builder().listItems(list).build();
        finremCaseData.setIntervenersList(dynamicRadioList);
        DynamicRadioListElement option1 = DynamicRadioListElement.builder().code(INTERVENER_ONE).build();
        finremCaseData.getIntervenersList().setValue(option1);

        when(systemUserService.getSysUserToken()).thenReturn(AUTH_TOKEN);
        when(organisationService.findUserByEmail(INTERVENER_TEST_EMAIL, AUTH_TOKEN)).thenReturn(Optional.of(INTERVENER_USER_ID));
        when(organisationService.findUserByEmail(INTERVENER_TEST_EMAIL_CHANGE, AUTH_TOKEN)).thenReturn(Optional.of(INTERVENER_USER_ID));

        service.setIntvenerDateAddedAndDefaultOrgIfNotRepresented(finremCallbackRequest);

        IntervenerOneWrapper intervenerOneWrapper = finremCaseData.getIntervenerOneWrapper();
        assertNotNull(intervenerOneWrapper.getIntervener1DateAdded());
        assertNotNull(intervenerOneWrapper.getIntervener1SolEmail());
        assertNotNull(intervenerOneWrapper.getIntervener1SolName());
        assertNotNull(intervenerOneWrapper.getIntervener1SolPhone());
        assertNotNull(intervenerOneWrapper.getIntervener1Organisation().getOrganisation().getOrganisationID());
        assertNotNull(intervenerOneWrapper.getIntervener1Organisation().getOrganisation().getOrganisationName());

        verify(assignCaseAccessService).grantCaseRoleToUser(CASE_ID, INTERVENER_USER_ID,
            INTVR_SOLICITOR_1.getValue(), CHANGE_ORG_ID);
        verify(assignCaseAccessService).removeCaseRoleToUser(CASE_ID, INTERVENER_USER_ID,
            INTVR_SOLICITOR_1.getValue(), SOME_ORG_ID);
    }


    @Test
    public void givenContestedCase_whenUpdatingIntervener1AndChangedRepresetationNoToYes_thenHandle() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseData finremCaseData = finremCallbackRequest.getCaseDetails().getData();
        FinremCaseData finremCaseDataBefore = finremCallbackRequest.getCaseDetailsBefore().getData();
        OrganisationPolicy organisationPolicy = OrganisationPolicy.builder().organisation(
            Organisation.builder().organisationID(SOME_ORG_ID).organisationName(SOME_ORG_ID).build()
        ).build();
        IntervenerOneWrapper oneWrapper = IntervenerOneWrapper
            .builder().intervener1Name("One name").intervener1Email("test@test.com")
            .intervener1Represented(YesOrNo.NO)
            .intervener1DateAdded(LocalDate.of(2023,1,1))
            .intervener1Organisation(organisationPolicy).build();
        finremCaseDataBefore.setIntervenerOneWrapper(oneWrapper);

        IntervenerOneWrapper oneWrapper1 = IntervenerOneWrapper
            .builder().intervener1Name("One name").intervener1Email("test@test.com")
            .intervener1Represented(YesOrNo.YES)
            .intervener1DateAdded(LocalDate.of(2023,1,1))
            .intervener1Organisation(organisationPolicy)
            .intervener1SolName("SOL name")
            .intervener1SolEmail("test@test.com")
            .intervener1SolPhone("112333333")
            .build();
        finremCaseData.setIntervenerOneWrapper(oneWrapper1);


        DynamicRadioListElement option = DynamicRadioListElement.builder().code(INTERVENER_ONE).build();
        List<DynamicRadioListElement> list = List.of(option);
        DynamicRadioList dynamicRadioList = DynamicRadioList.builder().listItems(list).build();
        finremCaseData.setIntervenersList(dynamicRadioList);
        DynamicRadioListElement option1 = DynamicRadioListElement.builder().code(INTERVENER_ONE).build();
        finremCaseData.getIntervenersList().setValue(option1);

        when(systemUserService.getSysUserToken()).thenReturn(AUTH_TOKEN);
        when(organisationService.findUserByEmail(INTERVENER_TEST_EMAIL, AUTH_TOKEN)).thenReturn(Optional.of(INTERVENER_USER_ID));

        service.setIntvenerDateAddedAndDefaultOrgIfNotRepresented(finremCallbackRequest);

        IntervenerOneWrapper intervenerOneWrapper = finremCaseData.getIntervenerOneWrapper();
        assertNotNull(intervenerOneWrapper.getIntervener1SolEmail());
        assertNotNull(intervenerOneWrapper.getIntervener1SolName());
        assertNotNull(intervenerOneWrapper.getIntervener1Organisation().getOrganisation().getOrganisationID());
        assertNotNull(intervenerOneWrapper.getIntervener1Organisation().getOrganisation().getOrganisationName());
        verify(assignCaseAccessService).grantCaseRoleToUser(CASE_ID, INTERVENER_USER_ID,
            INTVR_SOLICITOR_1.getValue(), SOME_ORG_ID);
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

        service.setIntvenerDateAddedAndDefaultOrgIfNotRepresented(finremCallbackRequest);

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

        service.setIntvenerDateAddedAndDefaultOrgIfNotRepresented(finremCallbackRequest);

        assertNotNull(finremCaseData.getIntervenerTwoWrapper().getIntervener2DateAdded());
        assertNotNull(finremCaseData.getIntervenerTwoWrapper().getIntervener2Organisation().getOrgPolicyCaseAssignedRole());
    }

    @Test
    public void givenContestedCase_whenAddingIntervener2AndIntv2Represent_thenSetIntvenerDateAddedAndDefaultOrg() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseData finremCaseData = finremCallbackRequest.getCaseDetails().getData();
        OrganisationPolicy organisationPolicy = OrganisationPolicy.builder().organisation(
            Organisation.builder().organisationID(SOME_ORG_ID).organisationName(SOME_ORG_ID).build()
        ).build();
        IntervenerTwoWrapper wrapper = IntervenerTwoWrapper
            .builder().intervener2Name("Two name").intervener2Email("test@test.com")
            .intervener2SolEmail("test@test.com")
            .intervener2Represented(YesOrNo.YES)
            .intervener2Organisation(organisationPolicy).build();
        finremCaseData.setIntervenerTwoWrapper(wrapper);

        DynamicRadioListElement option = DynamicRadioListElement.builder().code(INTERVENER_TWO).build();
        List<DynamicRadioListElement> list = List.of(option);
        DynamicRadioList dynamicRadioList = DynamicRadioList.builder().listItems(list).build();
        finremCaseData.setIntervenersList(dynamicRadioList);
        DynamicRadioListElement option1 = DynamicRadioListElement.builder().code(INTERVENER_TWO).build();
        finremCaseData.getIntervenersList().setValue(option1);

        when(systemUserService.getSysUserToken()).thenReturn(AUTH_TOKEN);
        when(organisationService.findUserByEmail(INTERVENER_TEST_EMAIL, AUTH_TOKEN)).thenReturn(Optional.of(INTERVENER_USER_ID));
        service.setIntvenerDateAddedAndDefaultOrgIfNotRepresented(finremCallbackRequest);

        assertNotNull(finremCaseData.getIntervenerTwoWrapper().getIntervener2DateAdded());
        verify(assignCaseAccessService).grantCaseRoleToUser(CASE_ID, INTERVENER_USER_ID,
            INTVR_SOLICITOR_2.getValue(), SOME_ORG_ID);
    }

    @Test
    public void givenContestedCase_whenUpdatingIntervener2AndChangedRepresetationYesToNo_thenHandle() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseData finremCaseData = finremCallbackRequest.getCaseDetails().getData();
        FinremCaseData finremCaseDataBefore = finremCallbackRequest.getCaseDetailsBefore().getData();
        OrganisationPolicy organisationPolicy = OrganisationPolicy.builder().organisation(
            Organisation.builder().organisationID(SOME_ORG_ID).organisationName(SOME_ORG_ID).build()
        ).build();
        IntervenerTwoWrapper wrapper = IntervenerTwoWrapper
            .builder().intervener2Name("Two name").intervener2Email("test@test.com")
            .intervener2SolEmail("test@test.com")
            .intervener2SolName("TwoSol name")
            .intervener2DateAdded(LocalDate.of(2023,1,1))
            .intervener2Represented(YesOrNo.YES)
            .intervener2Organisation(organisationPolicy).build();
        finremCaseDataBefore.setIntervenerTwoWrapper(wrapper);

        IntervenerTwoWrapper current = IntervenerTwoWrapper
            .builder().intervener2Name("Two name").intervener2Email("test@test.com")
            .intervener2Represented(YesOrNo.NO)
            .intervener2DateAdded(LocalDate.of(2023,1,1))
            .intervener2Organisation(organisationPolicy).build();
        finremCaseData.setIntervenerTwoWrapper(current);

        DynamicRadioListElement option = DynamicRadioListElement.builder().code(INTERVENER_TWO).build();
        List<DynamicRadioListElement> list = List.of(option);
        DynamicRadioList dynamicRadioList = DynamicRadioList.builder().listItems(list).build();
        finremCaseData.setIntervenersList(dynamicRadioList);
        DynamicRadioListElement option1 = DynamicRadioListElement.builder().code(INTERVENER_TWO).build();
        finremCaseData.getIntervenersList().setValue(option1);

        when(systemUserService.getSysUserToken()).thenReturn(AUTH_TOKEN);
        when(organisationService.findUserByEmail(INTERVENER_TEST_EMAIL, AUTH_TOKEN)).thenReturn(Optional.of(INTERVENER_USER_ID));
        service.setIntvenerDateAddedAndDefaultOrgIfNotRepresented(finremCallbackRequest);

        IntervenerTwoWrapper intervenerTwoWrapper = finremCaseData.getIntervenerTwoWrapper();
        assertNotNull(intervenerTwoWrapper.getIntervener2DateAdded());
        assertNull(intervenerTwoWrapper.getIntervener2SolEmail());
        assertNull(intervenerTwoWrapper.getIntervener2SolName());
        assertNull(intervenerTwoWrapper.getIntervener2SolPhone());
        assertNull(intervenerTwoWrapper.getIntervener2Organisation().getOrganisation().getOrganisationID());
        assertNull(intervenerTwoWrapper.getIntervener2Organisation().getOrganisation().getOrganisationName());
        verify(assignCaseAccessService).removeCaseRoleToUser(CASE_ID, INTERVENER_USER_ID,
            INTVR_SOLICITOR_2.getValue(), SOME_ORG_ID);
    }

    @Test
    public void givenContestedCase_whenUpdatingIntervener2AndChangedRepresetationSol_thenHandle() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseData finremCaseData = finremCallbackRequest.getCaseDetails().getData();
        FinremCaseData finremCaseDataBefore = finremCallbackRequest.getCaseDetailsBefore().getData();
        OrganisationPolicy organisationPolicy = OrganisationPolicy.builder().organisation(
            Organisation.builder().organisationID(SOME_ORG_ID).organisationName(SOME_ORG_ID).build()
        ).build();
        OrganisationPolicy organisationPolicyChange = OrganisationPolicy.builder().organisation(
            Organisation.builder().organisationID(CHANGE_ORG_ID).organisationName(CHANGE_ORG_ID).build()
        ).build();
        IntervenerTwoWrapper wrapper = IntervenerTwoWrapper
            .builder().intervener2Name("Two name").intervener2Email("test@test.com")
            .intervener2SolEmail("test@test.com")
            .intervener2SolName("TwoSol name")
            .intervener2SolPhone("11122111111")
            .intervener2DateAdded(LocalDate.of(2023,1,1))
            .intervener2Represented(YesOrNo.YES)
            .intervener2Organisation(organisationPolicy).build();
        finremCaseDataBefore.setIntervenerTwoWrapper(wrapper);

        IntervenerTwoWrapper current = IntervenerTwoWrapper
            .builder().intervener2Name("Two name").intervener2Email("test@test.com")
            .intervener2SolEmail(INTERVENER_TEST_EMAIL_CHANGE)
            .intervener2SolName("TwoSol name")
            .intervener2SolPhone("11122111111")
            .intervener2DateAdded(LocalDate.of(2023,1,1))
            .intervener2Represented(YesOrNo.YES)
            .intervener2Organisation(organisationPolicyChange).build();
        finremCaseData.setIntervenerTwoWrapper(current);

        DynamicRadioListElement option = DynamicRadioListElement.builder().code(INTERVENER_TWO).build();
        List<DynamicRadioListElement> list = List.of(option);
        DynamicRadioList dynamicRadioList = DynamicRadioList.builder().listItems(list).build();
        finremCaseData.setIntervenersList(dynamicRadioList);
        DynamicRadioListElement option1 = DynamicRadioListElement.builder().code(INTERVENER_TWO).build();
        finremCaseData.getIntervenersList().setValue(option1);

        when(systemUserService.getSysUserToken()).thenReturn(AUTH_TOKEN);
        when(organisationService.findUserByEmail(INTERVENER_TEST_EMAIL, AUTH_TOKEN)).thenReturn(Optional.of(INTERVENER_USER_ID));
        when(organisationService.findUserByEmail(INTERVENER_TEST_EMAIL_CHANGE, AUTH_TOKEN)).thenReturn(Optional.of(INTERVENER_USER_ID));

        service.setIntvenerDateAddedAndDefaultOrgIfNotRepresented(finremCallbackRequest);

        IntervenerTwoWrapper intervenerTwoWrapper = finremCaseData.getIntervenerTwoWrapper();
        assertNotNull(intervenerTwoWrapper.getIntervener2DateAdded());
        assertNotNull(intervenerTwoWrapper.getIntervener2SolEmail());
        assertNotNull(intervenerTwoWrapper.getIntervener2SolName());
        assertNotNull(intervenerTwoWrapper.getIntervener2SolPhone());
        assertNotNull(intervenerTwoWrapper.getIntervener2Organisation().getOrganisation().getOrganisationID());
        assertNotNull(intervenerTwoWrapper.getIntervener2Organisation().getOrganisation().getOrganisationName());

        verify(assignCaseAccessService).grantCaseRoleToUser(CASE_ID, INTERVENER_USER_ID,
            INTVR_SOLICITOR_2.getValue(), CHANGE_ORG_ID);
        verify(assignCaseAccessService).removeCaseRoleToUser(CASE_ID, INTERVENER_USER_ID,
            INTVR_SOLICITOR_2.getValue(), SOME_ORG_ID);
    }

    @Test
    public void givenContestedCase_whenUpdatingIntervener2AndChangedRepresetationNoToYes_thenHandle() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseData finremCaseData = finremCallbackRequest.getCaseDetails().getData();
        FinremCaseData finremCaseDataBefore = finremCallbackRequest.getCaseDetailsBefore().getData();
        OrganisationPolicy organisationPolicy = OrganisationPolicy.builder().organisation(
            Organisation.builder().organisationID(SOME_ORG_ID).organisationName(SOME_ORG_ID).build()
        ).build();
        IntervenerTwoWrapper wrapper = IntervenerTwoWrapper
            .builder().intervener2Name("Two name").intervener2Email("test@test.com")
            .intervener2DateAdded(LocalDate.of(2023,1,1))
            .intervener2Represented(YesOrNo.NO)
            .intervener2Organisation(organisationPolicy).build();
        finremCaseDataBefore.setIntervenerTwoWrapper(wrapper);

        IntervenerTwoWrapper current = IntervenerTwoWrapper
            .builder().intervener2Name("Two name").intervener2Email("test@test.com")
            .intervener2Represented(YesOrNo.YES)
            .intervener2DateAdded(LocalDate.of(2023,1,1))
            .intervener2Organisation(organisationPolicy)
            .intervener2SolName("SOL name")
            .intervener2SolEmail("test@test.com")
            .intervener2SolPhone("112333333").build();
        finremCaseData.setIntervenerTwoWrapper(current);

        DynamicRadioListElement option = DynamicRadioListElement.builder().code(INTERVENER_TWO).build();
        List<DynamicRadioListElement> list = List.of(option);
        DynamicRadioList dynamicRadioList = DynamicRadioList.builder().listItems(list).build();
        finremCaseData.setIntervenersList(dynamicRadioList);
        DynamicRadioListElement option1 = DynamicRadioListElement.builder().code(INTERVENER_TWO).build();
        finremCaseData.getIntervenersList().setValue(option1);

        when(systemUserService.getSysUserToken()).thenReturn(AUTH_TOKEN);
        when(organisationService.findUserByEmail(INTERVENER_TEST_EMAIL, AUTH_TOKEN)).thenReturn(Optional.of(INTERVENER_USER_ID));
        service.setIntvenerDateAddedAndDefaultOrgIfNotRepresented(finremCallbackRequest);

        IntervenerTwoWrapper intervenerTwoWrapper = finremCaseData.getIntervenerTwoWrapper();
        assertNotNull(intervenerTwoWrapper.getIntervener2SolEmail());
        assertNotNull(intervenerTwoWrapper.getIntervener2SolName());
        assertNotNull(intervenerTwoWrapper.getIntervener2Organisation().getOrganisation().getOrganisationID());
        assertNotNull(intervenerTwoWrapper.getIntervener2Organisation().getOrganisation().getOrganisationName());
        verify(assignCaseAccessService).grantCaseRoleToUser(CASE_ID, INTERVENER_USER_ID,
            INTVR_SOLICITOR_2.getValue(), SOME_ORG_ID);
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

        service.setIntvenerDateAddedAndDefaultOrgIfNotRepresented(finremCallbackRequest);

        assertNotNull(finremCaseData.getIntervenerThreeWrapper().getIntervener3DateAdded());
        assertNotNull(finremCaseData.getIntervenerThreeWrapper().getIntervener3Organisation().getOrgPolicyCaseAssignedRole());
    }

    @Test
    public void givenContestedCase_whenAddingIntervener3AndIntv3Represent_thenSetIntvenerDateAddedAndDefaultOrg() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseData finremCaseData = finremCallbackRequest.getCaseDetails().getData();
        OrganisationPolicy organisationPolicy = OrganisationPolicy.builder().organisation(
            Organisation.builder().organisationID(SOME_ORG_ID).organisationName(SOME_ORG_ID).build()
        ).build();
        IntervenerThreeWrapper wrapper = IntervenerThreeWrapper
            .builder().intervener3Name("Two name").intervener3Email("test@test.com")
            .intervener3SolEmail("test@test.com")
            .intervener3Represented(YesOrNo.YES)
            .intervener3Organisation(organisationPolicy).build();
        finremCaseData.setIntervenerThreeWrapper(wrapper);

        DynamicRadioListElement option = DynamicRadioListElement.builder().code(INTERVENER_THREE).build();
        List<DynamicRadioListElement> list = List.of(option);
        DynamicRadioList dynamicRadioList = DynamicRadioList.builder().listItems(list).build();
        finremCaseData.setIntervenersList(dynamicRadioList);
        DynamicRadioListElement option1 = DynamicRadioListElement.builder().code(INTERVENER_THREE).build();
        finremCaseData.getIntervenersList().setValue(option1);

        when(systemUserService.getSysUserToken()).thenReturn(AUTH_TOKEN);
        when(organisationService.findUserByEmail(INTERVENER_TEST_EMAIL, AUTH_TOKEN)).thenReturn(Optional.of(INTERVENER_USER_ID));

        service.setIntvenerDateAddedAndDefaultOrgIfNotRepresented(finremCallbackRequest);

        assertNotNull(finremCaseData.getIntervenerThreeWrapper().getIntervener3DateAdded());
        verify(assignCaseAccessService).grantCaseRoleToUser(CASE_ID, INTERVENER_USER_ID,
            INTVR_SOLICITOR_3.getValue(), SOME_ORG_ID);
    }

    @Test
    public void givenContestedCase_whenUpdatingIntervener3AndChangedRepresetationYesToNo_thenHandle() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseData finremCaseData = finremCallbackRequest.getCaseDetails().getData();
        FinremCaseData finremCaseDataBefore = finremCallbackRequest.getCaseDetailsBefore().getData();
        OrganisationPolicy organisationPolicy = OrganisationPolicy.builder().organisation(
            Organisation.builder().organisationID(SOME_ORG_ID).organisationName(SOME_ORG_ID).build()
        ).build();
        IntervenerThreeWrapper wrapper = IntervenerThreeWrapper
            .builder().intervener3Name("Two name").intervener3Email("test@test.com")
            .intervener3SolEmail("test@test.com")
            .intervener3SolName("three man")
            .intervener3SolPhone("999999999")
            .intervener3DateAdded(LocalDate.of(2023,1,1))
            .intervener3Represented(YesOrNo.YES)
            .intervener3Organisation(organisationPolicy).build();
        finremCaseDataBefore.setIntervenerThreeWrapper(wrapper);

        IntervenerThreeWrapper current = IntervenerThreeWrapper
            .builder().intervener3Name("Two name").intervener3Email("test@test.com")
            .intervener3DateAdded(LocalDate.of(2023,1,1))
            .intervener3Represented(YesOrNo.NO)
            .intervener3Organisation(organisationPolicy).build();
        finremCaseData.setIntervenerThreeWrapper(current);

        DynamicRadioListElement option = DynamicRadioListElement.builder().code(INTERVENER_THREE).build();
        List<DynamicRadioListElement> list = List.of(option);
        DynamicRadioList dynamicRadioList = DynamicRadioList.builder().listItems(list).build();
        finremCaseData.setIntervenersList(dynamicRadioList);
        DynamicRadioListElement option1 = DynamicRadioListElement.builder().code(INTERVENER_THREE).build();
        finremCaseData.getIntervenersList().setValue(option1);

        when(systemUserService.getSysUserToken()).thenReturn(AUTH_TOKEN);
        when(organisationService.findUserByEmail(INTERVENER_TEST_EMAIL, AUTH_TOKEN)).thenReturn(Optional.of(INTERVENER_USER_ID));

        service.setIntvenerDateAddedAndDefaultOrgIfNotRepresented(finremCallbackRequest);

        IntervenerThreeWrapper intervenerThreeWrapper = finremCaseData.getIntervenerThreeWrapper();
        assertNotNull(intervenerThreeWrapper.getIntervener3DateAdded());
        assertNull(intervenerThreeWrapper.getIntervener3SolEmail());
        assertNull(intervenerThreeWrapper.getIntervener3SolName());
        assertNull(intervenerThreeWrapper.getIntervener3SolPhone());
        assertNull(intervenerThreeWrapper.getIntervener3Organisation().getOrganisation().getOrganisationID());
        assertNull(intervenerThreeWrapper.getIntervener3Organisation().getOrganisation().getOrganisationName());
        verify(assignCaseAccessService).removeCaseRoleToUser(CASE_ID, INTERVENER_USER_ID,
            INTVR_SOLICITOR_3.getValue(), SOME_ORG_ID);
    }

    @Test
    public void givenContestedCase_whenUpdatingIntervener3AndChangedRepresetationSol_thenHandle() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseData finremCaseData = finremCallbackRequest.getCaseDetails().getData();
        FinremCaseData finremCaseDataBefore = finremCallbackRequest.getCaseDetailsBefore().getData();
        OrganisationPolicy organisationPolicy = OrganisationPolicy.builder().organisation(
            Organisation.builder().organisationID(SOME_ORG_ID).organisationName(SOME_ORG_ID).build()
        ).build();
        OrganisationPolicy organisationPolicyChange = OrganisationPolicy.builder().organisation(
            Organisation.builder().organisationID(CHANGE_ORG_ID).organisationName(CHANGE_ORG_ID).build()
        ).build();
        IntervenerThreeWrapper wrapper = IntervenerThreeWrapper
            .builder().intervener3Name("Two name").intervener3Email("test@test.com")
            .intervener3SolEmail("test@test.com")
            .intervener3SolName("three man")
            .intervener3SolPhone("999999999")
            .intervener3DateAdded(LocalDate.of(2023,1,1))
            .intervener3Represented(YesOrNo.YES)
            .intervener3Organisation(organisationPolicy).build();
        finremCaseDataBefore.setIntervenerThreeWrapper(wrapper);

        IntervenerThreeWrapper current = IntervenerThreeWrapper
            .builder().intervener3Name("Two name").intervener3Email("test@test.com")
            .intervener3SolEmail(INTERVENER_TEST_EMAIL_CHANGE)
            .intervener3SolName("three man")
            .intervener3SolPhone("999999999")
            .intervener3DateAdded(LocalDate.of(2023,1,1))
            .intervener3Represented(YesOrNo.YES)
            .intervener3Organisation(organisationPolicyChange).build();
        finremCaseData.setIntervenerThreeWrapper(current);

        DynamicRadioListElement option = DynamicRadioListElement.builder().code(INTERVENER_THREE).build();
        List<DynamicRadioListElement> list = List.of(option);
        DynamicRadioList dynamicRadioList = DynamicRadioList.builder().listItems(list).build();
        finremCaseData.setIntervenersList(dynamicRadioList);
        DynamicRadioListElement option1 = DynamicRadioListElement.builder().code(INTERVENER_THREE).build();
        finremCaseData.getIntervenersList().setValue(option1);

        when(systemUserService.getSysUserToken()).thenReturn(AUTH_TOKEN);
        when(organisationService.findUserByEmail(INTERVENER_TEST_EMAIL, AUTH_TOKEN)).thenReturn(Optional.of(INTERVENER_USER_ID));
        when(organisationService.findUserByEmail(INTERVENER_TEST_EMAIL_CHANGE, AUTH_TOKEN)).thenReturn(Optional.of(INTERVENER_USER_ID));

        service.setIntvenerDateAddedAndDefaultOrgIfNotRepresented(finremCallbackRequest);

        IntervenerThreeWrapper intervenerThreeWrapper = finremCaseData.getIntervenerThreeWrapper();
        assertNotNull(intervenerThreeWrapper.getIntervener3DateAdded());
        assertNotNull(intervenerThreeWrapper.getIntervener3SolEmail());
        assertNotNull(intervenerThreeWrapper.getIntervener3SolName());
        assertNotNull(intervenerThreeWrapper.getIntervener3SolPhone());
        assertNotNull(intervenerThreeWrapper.getIntervener3Organisation().getOrganisation().getOrganisationID());
        assertNotNull(intervenerThreeWrapper.getIntervener3Organisation().getOrganisation().getOrganisationName());

        verify(assignCaseAccessService).grantCaseRoleToUser(CASE_ID, INTERVENER_USER_ID,
            INTVR_SOLICITOR_3.getValue(), CHANGE_ORG_ID);
        verify(assignCaseAccessService).removeCaseRoleToUser(CASE_ID, INTERVENER_USER_ID,
            INTVR_SOLICITOR_3.getValue(), SOME_ORG_ID);
    }

    @Test
    public void givenContestedCase_whenUpdatingIntervener3AndChangedRepresetationNoToYes_thenHandle() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseData finremCaseData = finremCallbackRequest.getCaseDetails().getData();
        FinremCaseData finremCaseDataBefore = finremCallbackRequest.getCaseDetailsBefore().getData();
        OrganisationPolicy organisationPolicy = OrganisationPolicy.builder().organisation(
            Organisation.builder().organisationID(SOME_ORG_ID).organisationName(SOME_ORG_ID).build()
        ).build();
        IntervenerThreeWrapper wrapper = IntervenerThreeWrapper
            .builder().intervener3Name("Two name").intervener3Email("test@test.com")
            .intervener3DateAdded(LocalDate.of(2023,1,1))
            .intervener3Represented(YesOrNo.NO)
            .intervener3Organisation(organisationPolicy).build();
        finremCaseDataBefore.setIntervenerThreeWrapper(wrapper);

        IntervenerThreeWrapper current = IntervenerThreeWrapper
            .builder().intervener3Name("Two name").intervener3Email("test@test.com")
            .intervener3DateAdded(LocalDate.of(2023,1,1))
            .intervener3SolEmail("test@test.com")
            .intervener3SolName("three man")
            .intervener3SolPhone("999999999")
            .intervener3Represented(YesOrNo.YES)
            .intervener3Organisation(organisationPolicy).build();
        finremCaseData.setIntervenerThreeWrapper(current);

        DynamicRadioListElement option = DynamicRadioListElement.builder().code(INTERVENER_THREE).build();
        List<DynamicRadioListElement> list = List.of(option);
        DynamicRadioList dynamicRadioList = DynamicRadioList.builder().listItems(list).build();
        finremCaseData.setIntervenersList(dynamicRadioList);
        DynamicRadioListElement option1 = DynamicRadioListElement.builder().code(INTERVENER_THREE).build();
        finremCaseData.getIntervenersList().setValue(option1);

        when(systemUserService.getSysUserToken()).thenReturn(AUTH_TOKEN);
        when(organisationService.findUserByEmail(INTERVENER_TEST_EMAIL, AUTH_TOKEN)).thenReturn(Optional.of(INTERVENER_USER_ID));

        service.setIntvenerDateAddedAndDefaultOrgIfNotRepresented(finremCallbackRequest);

        IntervenerThreeWrapper intervenerThreeWrapper = finremCaseData.getIntervenerThreeWrapper();
        assertNotNull(intervenerThreeWrapper.getIntervener3SolEmail());
        assertNotNull(intervenerThreeWrapper.getIntervener3SolName());
        assertNotNull(intervenerThreeWrapper.getIntervener3Organisation().getOrganisation().getOrganisationID());
        assertNotNull(intervenerThreeWrapper.getIntervener3Organisation().getOrganisation().getOrganisationName());
        verify(assignCaseAccessService).grantCaseRoleToUser(CASE_ID, INTERVENER_USER_ID,
            INTVR_SOLICITOR_3.getValue(), SOME_ORG_ID);
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

        service.setIntvenerDateAddedAndDefaultOrgIfNotRepresented(finremCallbackRequest);

        assertNotNull(finremCaseData.getIntervenerFourWrapper().getIntervener4DateAdded());
        assertNotNull(finremCaseData.getIntervenerFourWrapper().getIntervener4Organisation().getOrgPolicyCaseAssignedRole());
    }

    @Test
    public void givenContestedCase_whenAddingIntervener4AndIntv4Represent_thenSetIntvenerDateAddedAndDefaultOrg() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseData finremCaseData = finremCallbackRequest.getCaseDetails().getData();
        OrganisationPolicy organisationPolicy = OrganisationPolicy.builder().organisation(
            Organisation.builder().organisationID(SOME_ORG_ID).organisationName(SOME_ORG_ID).build()
        ).build();
        IntervenerFourWrapper wrapper = IntervenerFourWrapper
            .builder().intervener4Name("Two name").intervener4Email("test@test.com")
            .intervener4Represented(YesOrNo.YES)
            .intervener4SolEmail("test@test.com")
            .intervener4Organisation(organisationPolicy).build();
        finremCaseData.setIntervenerFourWrapper(wrapper);

        DynamicRadioListElement option = DynamicRadioListElement.builder().code(INTERVENER_FOUR).build();
        List<DynamicRadioListElement> list = List.of(option);
        DynamicRadioList dynamicRadioList = DynamicRadioList.builder().listItems(list).build();
        finremCaseData.setIntervenersList(dynamicRadioList);
        DynamicRadioListElement option1 = DynamicRadioListElement.builder().code(INTERVENER_FOUR).build();
        finremCaseData.getIntervenersList().setValue(option1);

        when(systemUserService.getSysUserToken()).thenReturn(AUTH_TOKEN);
        when(organisationService.findUserByEmail(INTERVENER_TEST_EMAIL, AUTH_TOKEN)).thenReturn(Optional.of(INTERVENER_USER_ID));

        service.setIntvenerDateAddedAndDefaultOrgIfNotRepresented(finremCallbackRequest);

        assertNotNull(finremCaseData.getIntervenerFourWrapper().getIntervener4DateAdded());
        verify(assignCaseAccessService).grantCaseRoleToUser(CASE_ID, INTERVENER_USER_ID,
            INTVR_SOLICITOR_4.getValue(), SOME_ORG_ID);
    }

    @Test
    public void givenContestedCase_whenUpdatingIntervener4AndChangedRepresetationYesToNo_thenHandle() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseData finremCaseData = finremCallbackRequest.getCaseDetails().getData();
        FinremCaseData finremCaseDataBefore = finremCallbackRequest.getCaseDetailsBefore().getData();
        OrganisationPolicy organisationPolicy = OrganisationPolicy.builder().organisation(
            Organisation.builder().organisationID(SOME_ORG_ID).organisationName(SOME_ORG_ID).build()
        ).build();
        IntervenerFourWrapper wrapper = IntervenerFourWrapper
            .builder().intervener4Name("Two name").intervener4Email("test@test.com")
            .intervener4Represented(YesOrNo.YES)
            .intervener4DateAdded(LocalDate.of(2023,1,1))
            .intervener4SolEmail("test@test.com")
            .intervener4SolName("tes test")
            .intervener4SolEmail("test@test.com")
            .intervener4Organisation(organisationPolicy).build();
        finremCaseDataBefore.setIntervenerFourWrapper(wrapper);

        IntervenerFourWrapper current = IntervenerFourWrapper
            .builder().intervener4Name("Two name").intervener4Email("test@test.com")
            .intervener4Represented(YesOrNo.NO)
            .intervener4DateAdded(LocalDate.of(2023,1,1))
            .intervener4Organisation(organisationPolicy).build();
        finremCaseData.setIntervenerFourWrapper(current);

        DynamicRadioListElement option = DynamicRadioListElement.builder().code(INTERVENER_FOUR).build();
        List<DynamicRadioListElement> list = List.of(option);
        DynamicRadioList dynamicRadioList = DynamicRadioList.builder().listItems(list).build();
        finremCaseData.setIntervenersList(dynamicRadioList);
        DynamicRadioListElement option1 = DynamicRadioListElement.builder().code(INTERVENER_FOUR).build();
        finremCaseData.getIntervenersList().setValue(option1);

        when(systemUserService.getSysUserToken()).thenReturn(AUTH_TOKEN);
        when(organisationService.findUserByEmail(INTERVENER_TEST_EMAIL, AUTH_TOKEN)).thenReturn(Optional.of(INTERVENER_USER_ID));

        service.setIntvenerDateAddedAndDefaultOrgIfNotRepresented(finremCallbackRequest);

        IntervenerFourWrapper intervenerFourWrapper = finremCaseData.getIntervenerFourWrapper();
        assertNotNull(intervenerFourWrapper.getIntervener4DateAdded());
        assertNull(intervenerFourWrapper.getIntervener4SolEmail());
        assertNull(intervenerFourWrapper.getIntervener4SolName());
        assertNull(intervenerFourWrapper.getIntervener4SolPhone());
        assertNull(intervenerFourWrapper.getIntervener4Organisation().getOrganisation().getOrganisationID());
        assertNull(intervenerFourWrapper.getIntervener4Organisation().getOrganisation().getOrganisationName());

        verify(assignCaseAccessService).removeCaseRoleToUser(CASE_ID, INTERVENER_USER_ID,
            INTVR_SOLICITOR_4.getValue(), SOME_ORG_ID);
    }

    @Test
    public void givenContestedCase_whenUpdatingIntervener4AndChangedRepresetationNoToYes_thenHandle() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseData finremCaseData = finremCallbackRequest.getCaseDetails().getData();
        FinremCaseData finremCaseDataBefore = finremCallbackRequest.getCaseDetailsBefore().getData();
        OrganisationPolicy organisationPolicy = OrganisationPolicy.builder().organisation(
            Organisation.builder().organisationID(SOME_ORG_ID).organisationName(SOME_ORG_ID).build()
        ).build();
        IntervenerFourWrapper wrapper = IntervenerFourWrapper
            .builder().intervener4Name("Two name").intervener4Email("test@test.com")
            .intervener4Represented(YesOrNo.NO)
            .intervener4DateAdded(LocalDate.of(2023,1,1))
            .intervener4Organisation(organisationPolicy).build();
        finremCaseDataBefore.setIntervenerFourWrapper(wrapper);

        IntervenerFourWrapper current = IntervenerFourWrapper
            .builder().intervener4Name("Two name").intervener4Email("test@test.com")
            .intervener4Represented(YesOrNo.YES)
            .intervener4SolEmail("test@test.com")
            .intervener4SolName("tes test")
            .intervener4SolEmail("test@test.com")
            .intervener4DateAdded(LocalDate.of(2023,1,1))
            .intervener4Organisation(organisationPolicy).build();
        finremCaseData.setIntervenerFourWrapper(current);

        DynamicRadioListElement option = DynamicRadioListElement.builder().code(INTERVENER_FOUR).build();
        List<DynamicRadioListElement> list = List.of(option);
        DynamicRadioList dynamicRadioList = DynamicRadioList.builder().listItems(list).build();
        finremCaseData.setIntervenersList(dynamicRadioList);
        DynamicRadioListElement option1 = DynamicRadioListElement.builder().code(INTERVENER_FOUR).build();
        finremCaseData.getIntervenersList().setValue(option1);

        when(systemUserService.getSysUserToken()).thenReturn(AUTH_TOKEN);
        when(organisationService.findUserByEmail(INTERVENER_TEST_EMAIL, AUTH_TOKEN)).thenReturn(Optional.of(INTERVENER_USER_ID));

        service.setIntvenerDateAddedAndDefaultOrgIfNotRepresented(finremCallbackRequest);

        IntervenerFourWrapper intervenerFourWrapper = finremCaseData.getIntervenerFourWrapper();
        assertNotNull(intervenerFourWrapper.getIntervener4DateAdded());
        assertNotNull(intervenerFourWrapper.getIntervener4SolEmail());
        assertNotNull(intervenerFourWrapper.getIntervener4SolName());
        assertNotNull(intervenerFourWrapper.getIntervener4Organisation().getOrganisation().getOrganisationID());
        assertNotNull(intervenerFourWrapper.getIntervener4Organisation().getOrganisation().getOrganisationName());
        verify(assignCaseAccessService).grantCaseRoleToUser(CASE_ID, INTERVENER_USER_ID,
            INTVR_SOLICITOR_4.getValue(), SOME_ORG_ID);
    }

    @Test
    public void givenContestedCase_whenUpdatingIntervener4AndChangedRepresetationSol_thenHandle() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseData finremCaseData = finremCallbackRequest.getCaseDetails().getData();
        FinremCaseData finremCaseDataBefore = finremCallbackRequest.getCaseDetailsBefore().getData();
        OrganisationPolicy organisationPolicy = OrganisationPolicy.builder().organisation(
            Organisation.builder().organisationID(SOME_ORG_ID).organisationName(SOME_ORG_ID).build()
        ).build();
        OrganisationPolicy organisationPolicyChange = OrganisationPolicy.builder().organisation(
            Organisation.builder().organisationID(CHANGE_ORG_ID).organisationName(CHANGE_ORG_ID).build()
        ).build();

        IntervenerFourWrapper wrapper = IntervenerFourWrapper
            .builder().intervener4Name("Two name").intervener4Email("test@test.com")
            .intervener4Represented(YesOrNo.YES)
            .intervener4DateAdded(LocalDate.of(2023,1,1))
            .intervener4SolName("tes test")
            .intervener4SolPhone("122222222222")
            .intervener4SolEmail("test@test.com")
            .intervener4Organisation(organisationPolicy).build();
        finremCaseDataBefore.setIntervenerFourWrapper(wrapper);

        IntervenerFourWrapper current = IntervenerFourWrapper
            .builder().intervener4Name("Two name").intervener4Email("test@test.com")
            .intervener4Represented(YesOrNo.YES)
            .intervener4DateAdded(LocalDate.of(2023,1,1))
            .intervener4SolName("tes test")
            .intervener4SolPhone("122222222222")
            .intervener4SolEmail(INTERVENER_TEST_EMAIL_CHANGE)
            .intervener4Organisation(organisationPolicyChange).build();
        finremCaseData.setIntervenerFourWrapper(current);

        DynamicRadioListElement option = DynamicRadioListElement.builder().code(INTERVENER_FOUR).build();
        List<DynamicRadioListElement> list = List.of(option);
        DynamicRadioList dynamicRadioList = DynamicRadioList.builder().listItems(list).build();
        finremCaseData.setIntervenersList(dynamicRadioList);
        DynamicRadioListElement option1 = DynamicRadioListElement.builder().code(INTERVENER_FOUR).build();
        finremCaseData.getIntervenersList().setValue(option1);

        when(systemUserService.getSysUserToken()).thenReturn(AUTH_TOKEN);
        when(organisationService.findUserByEmail(INTERVENER_TEST_EMAIL, AUTH_TOKEN)).thenReturn(Optional.of(INTERVENER_USER_ID));
        when(organisationService.findUserByEmail(INTERVENER_TEST_EMAIL_CHANGE, AUTH_TOKEN)).thenReturn(Optional.of(INTERVENER_USER_ID));

        service.setIntvenerDateAddedAndDefaultOrgIfNotRepresented(finremCallbackRequest);

        IntervenerFourWrapper intervenerFourWrapper = finremCaseData.getIntervenerFourWrapper();
        assertNotNull(intervenerFourWrapper.getIntervener4DateAdded());
        assertNotNull(intervenerFourWrapper.getIntervener4SolEmail());
        assertNotNull(intervenerFourWrapper.getIntervener4SolName());
        assertNotNull(intervenerFourWrapper.getIntervener4SolPhone());
        assertNotNull(intervenerFourWrapper.getIntervener4Organisation().getOrganisation().getOrganisationID());
        assertNotNull(intervenerFourWrapper.getIntervener4Organisation().getOrganisation().getOrganisationName());

        verify(assignCaseAccessService).grantCaseRoleToUser(CASE_ID, INTERVENER_USER_ID,
            INTVR_SOLICITOR_4.getValue(), CHANGE_ORG_ID);
        verify(assignCaseAccessService).removeCaseRoleToUser(CASE_ID, INTERVENER_USER_ID,
            INTVR_SOLICITOR_4.getValue(), SOME_ORG_ID);
    }

    @Test
    public void givenCase_whenInvalidIntervenerReceived_thenThrowError() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseData finremCaseData = finremCallbackRequest.getCaseDetails().getData();

        DynamicRadioListElement option = DynamicRadioListElement.builder().code(INTERVENER_INVALID).build();
        List<DynamicRadioListElement> list = List.of(option);
        DynamicRadioList dynamicRadioList = DynamicRadioList.builder().listItems(list).build();
        finremCaseData.setIntervenersList(dynamicRadioList);
        DynamicRadioListElement option1 = DynamicRadioListElement.builder().code(INTERVENER_INVALID).build();
        finremCaseData.getIntervenersList().setValue(option1);

        assertThatThrownBy(() ->
            service.setIntvenerDateAddedAndDefaultOrgIfNotRepresented(finremCallbackRequest)
        ).isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Invalid intervener selected");
    }

    @Test
    public void givenContestedCase_whenAddingIntervenerWithNonRegisterEmail_thenHandlerThrowException() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseData finremCaseData = finremCallbackRequest.getCaseDetails().getData();
        OrganisationPolicy organisationPolicy = OrganisationPolicy.builder().organisation(
            Organisation.builder().organisationID(SOME_ORG_ID).organisationName(SOME_ORG_ID).build()
        ).build();
        IntervenerFourWrapper wrapper = IntervenerFourWrapper
            .builder().intervener4Name("Two name").intervener4Email(INTERVENER_TEST_EMAIL)
            .intervener4Represented(YesOrNo.YES)
            .intervener4SolEmail(INTERVENER_TEST_EMAIL)
            .intervener4Organisation(organisationPolicy).build();
        finremCaseData.setIntervenerFourWrapper(wrapper);

        DynamicRadioListElement option = DynamicRadioListElement.builder().code(INTERVENER_FOUR).build();
        List<DynamicRadioListElement> list = List.of(option);
        DynamicRadioList dynamicRadioList = DynamicRadioList.builder().listItems(list).build();
        finremCaseData.setIntervenersList(dynamicRadioList);
        DynamicRadioListElement option1 = DynamicRadioListElement.builder().code(INTERVENER_FOUR).build();
        finremCaseData.getIntervenersList().setValue(option1);

        when(systemUserService.getSysUserToken()).thenReturn(AUTH_TOKEN);
        Throwable exception = assertThrows(NoSuchUserException.class,
            () -> service.setIntvenerDateAddedAndDefaultOrgIfNotRepresented(finremCallbackRequest));
        assertEquals("expecting exception to throw when user not found in am",
            "Could not find the user with email " + INTERVENER_TEST_EMAIL, exception.getMessage());
    }

    private FinremCallbackRequest buildCallbackRequest() {
        return FinremCallbackRequest
            .builder()
            .eventType(EventType.MANAGE_INTERVENERS)
            .caseDetailsBefore(FinremCaseDetails.builder().id(123L).caseType(CONTESTED)
                .data(new FinremCaseData()).build())
            .caseDetails(FinremCaseDetails.builder().id(123L).caseType(CONTESTED)
                .data(new FinremCaseData()).build())
            .build();
    }
}