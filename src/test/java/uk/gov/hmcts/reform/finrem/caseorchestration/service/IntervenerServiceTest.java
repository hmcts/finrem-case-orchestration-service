package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Address;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangeOfRepresentationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangedRepresentative;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicRadioList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicRadioListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Element;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Organisation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrganisationPolicy;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RepresentationUpdate;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RepresentationUpdateHistory;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RepresentationUpdateHistoryCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerFour;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerOne;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerThree;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerTwo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerAction;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerChangeDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerType;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID_IN_LONG;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_ORG_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SYSTEM_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_USER_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.organisation;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.STOP_REPRESENTING_CLIENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole.INTVR_SOLICITOR_1;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole.INTVR_SOLICITOR_2;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole.INTVR_SOLICITOR_3;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole.INTVR_SOLICITOR_4;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.INTERVENER_FOUR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.INTERVENER_ONE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.INTERVENER_THREE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.INTERVENER_TWO;

@ExtendWith(MockitoExtension.class)
class IntervenerServiceTest {
    
    private static final String INTERVENER_TEST_EMAIL = "test@test.com";
    private static final String INTERVENER_TEST_EMAIL_CHANGE = "test2@test.com";
    private static final String SOME_ORG_ID = "someOrgId";
    private static final String CHANGE_ORG_ID = "changeOrgId";
    private static final String INTERVENER_USER_ID = "intervenerUserId";

    private static final String INTERVENER_SOL_FIRM = "testFirm";
    private static final String INTERVENER_SOL_REFERENCE = "testReference";

    @Mock
    private AssignCaseAccessService assignCaseAccessService;
    @Mock
    private PrdOrganisationService organisationService;
    @Mock
    private SystemUserService systemUserService;
    @Mock
    private IdamService idamService;
    @InjectMocks
    private IntervenerService service;
    @Mock
    private ChangeOfRepresentationService changeOfRepresentationService;

    @Test
    void givenCase_whenRemoveOperationChosenForIntv1NotRepresented_thenRemoveIntervener() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();

        FinremCaseData finremCaseData = finremCallbackRequest.getCaseDetails().getData();
        IntervenerOne oneWrapper = IntervenerOne
            .builder().intervenerName("One name").intervenerEmail("test@test.com").intervenerRepresented(YesOrNo.NO).build();
        finremCaseData.setIntervenerOne(oneWrapper);
        List<String> errors = new ArrayList<>();
        service.removeIntervenerDetails(oneWrapper, errors, finremCaseData, CASE_ID_IN_LONG);

        assertNull(finremCaseData.getIntervenerOne().getIntervenerName());
        assertNull(finremCaseData.getIntervenerOne().getIntervenerEmail());
        assertNull(finremCaseData.getIntervenerOne().getIntervenerPhone());
        assertThat(errors).isEmpty();
    }

    @Test
    void givenCase_whenRemoveOperationChosenForIntv1Represented_thenRemoveIntervener() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();

        FinremCaseData finremCaseData = finremCallbackRequest.getCaseDetails().getData();
        OrganisationPolicy organisationPolicy = OrganisationPolicy.builder().organisation(
            Organisation.builder().organisationID(SOME_ORG_ID).organisationName(SOME_ORG_ID).build()
        ).build();
        IntervenerOne oneWrapper = IntervenerOne
            .builder()
            .intervenerName("One name")
            .intervenerEmail("test@test.com")
            .intervenerSolEmail("test@test.com")
            .intervenerSolicitorFirm(INTERVENER_SOL_FIRM)
            .intervenerSolicitorReference(INTERVENER_SOL_REFERENCE)
            .intervenerOrganisation(organisationPolicy)
            .intervenerRepresented(YesOrNo.YES).build();
        finremCaseData.setIntervenerOne(oneWrapper);

        when(systemUserService.getSysUserToken()).thenReturn(AUTH_TOKEN);
        when(organisationService.findUserByEmail(INTERVENER_TEST_EMAIL, AUTH_TOKEN)).thenReturn(Optional.of(INTERVENER_USER_ID));
        List<String> errors = new ArrayList<>();
        service.removeIntervenerDetails(oneWrapper, errors, finremCaseData, CASE_ID_IN_LONG);

        assertNull(finremCaseData.getIntervenerOne().getIntervenerName());
        assertNull(finremCaseData.getIntervenerOne().getIntervenerEmail());
        assertNull(finremCaseData.getIntervenerOne().getIntervenerPhone());
        assertNull(finremCaseData.getIntervenerOne().getIntervenerSolicitorFirm());
        assertNull(finremCaseData.getIntervenerOne().getIntervenerSolicitorReference());
        verify(assignCaseAccessService).removeCaseRoleToUser(CASE_ID_IN_LONG, INTERVENER_USER_ID,
            INTVR_SOLICITOR_1.getCcdCode(), SOME_ORG_ID);
        assertThat(errors).isEmpty();
    }

    @Test
    void givenCase_whenRemoveOperationChosenForIntv2NotRepresented_thenRemoveIntervener() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();

        FinremCaseData finremCaseData = finremCallbackRequest.getCaseDetails().getData();
        IntervenerTwo twoWrapper = IntervenerTwo
            .builder().intervenerName("Two name").intervenerEmail("test@test.com").intervenerRepresented(YesOrNo.NO).build();
        finremCaseData.setIntervenerTwo(twoWrapper);
        List<String> errors = new ArrayList<>();
        service.removeIntervenerDetails(twoWrapper, errors, finremCaseData, CASE_ID_IN_LONG);

        assertNull(finremCaseData.getIntervenerTwo().getIntervenerName());
        assertNull(finremCaseData.getIntervenerTwo().getIntervenerEmail());
        assertNull(finremCaseData.getIntervenerTwo().getIntervenerSolicitorFirm());
        assertNull(finremCaseData.getIntervenerTwo().getIntervenerSolicitorReference());
        verify(assignCaseAccessService, never()).removeCaseRoleToUser(any(), any(), any(), any());
        assertThat(errors).isEmpty();
    }

    @Test
    void givenCase_whenRemoveOperationChosenForIntv2Represented_thenRemoveIntervener() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();

        FinremCaseData finremCaseData = finremCallbackRequest.getCaseDetails().getData();
        OrganisationPolicy organisationPolicy = OrganisationPolicy.builder().organisation(
            Organisation.builder().organisationID(SOME_ORG_ID).organisationName(SOME_ORG_ID).build()
        ).build();
        IntervenerTwo twoWrapper = IntervenerTwo
            .builder()
            .intervenerName("Two name")
            .intervenerEmail("test@test.com")
            .intervenerSolEmail("test@test.com")
            .intervenerSolicitorFirm(INTERVENER_SOL_FIRM)
            .intervenerSolicitorReference(INTERVENER_SOL_REFERENCE)
            .intervenerOrganisation(organisationPolicy)
            .intervenerRepresented(YesOrNo.YES).build();
        finremCaseData.setIntervenerTwo(twoWrapper);

        when(systemUserService.getSysUserToken()).thenReturn(AUTH_TOKEN);
        when(organisationService.findUserByEmail(INTERVENER_TEST_EMAIL, AUTH_TOKEN)).thenReturn(Optional.of(INTERVENER_USER_ID));
        List<String> errors = new ArrayList<>();
        service.removeIntervenerDetails(twoWrapper, errors, finremCaseData, CASE_ID_IN_LONG);

        assertNull(finremCaseData.getIntervenerTwo().getIntervenerName());
        assertNull(finremCaseData.getIntervenerTwo().getIntervenerEmail());
        assertNull(finremCaseData.getIntervenerTwo().getIntervenerSolicitorFirm());
        assertNull(finremCaseData.getIntervenerTwo().getIntervenerSolicitorReference());
        verify(assignCaseAccessService).removeCaseRoleToUser(CASE_ID_IN_LONG, INTERVENER_USER_ID,
            INTVR_SOLICITOR_2.getCcdCode(), SOME_ORG_ID);
        assertThat(errors).isEmpty();
    }

    @Test
    void givenCase_whenRemoveOperationChosenForIntv3NotRepesented_thenRemoveIntervener() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();

        FinremCaseData finremCaseData = finremCallbackRequest.getCaseDetails().getData();
        IntervenerThree threeWrapper = IntervenerThree
            .builder().intervenerName("Three name").intervenerEmail("test@test.com").intervenerRepresented(YesOrNo.NO).build();
        finremCaseData.setIntervenerThree(threeWrapper);

        List<String> errors = new ArrayList<>();
        service.removeIntervenerDetails(threeWrapper, errors, finremCaseData, CASE_ID_IN_LONG);

        assertNull(finremCaseData.getIntervenerThree().getIntervenerName());
        assertNull(finremCaseData.getIntervenerThree().getIntervenerEmail());
        assertNull(finremCaseData.getIntervenerThree().getIntervenerSolicitorFirm());
        assertNull(finremCaseData.getIntervenerThree().getIntervenerSolicitorReference());
        assertThat(errors).isEmpty();

    }

    @Test
    void givenCase_whenRemoveOperationChosenForIntv3Repesented_thenRemoveIntervener() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();

        FinremCaseData finremCaseData = finremCallbackRequest.getCaseDetails().getData();
        OrganisationPolicy organisationPolicy = OrganisationPolicy.builder().organisation(
            Organisation.builder().organisationID(SOME_ORG_ID).organisationName(SOME_ORG_ID).build()
        ).build();
        IntervenerThree threeWrapper = IntervenerThree
            .builder().intervenerName("Three name").intervenerEmail("test@test.com")
            .intervenerSolEmail("test@test.com")
            .intervenerSolicitorFirm(INTERVENER_SOL_FIRM)
            .intervenerSolicitorReference(INTERVENER_SOL_REFERENCE)
            .intervenerRepresented(YesOrNo.YES)
            .intervenerOrganisation(organisationPolicy)
            .build();
        finremCaseData.setIntervenerThree(threeWrapper);

        when(systemUserService.getSysUserToken()).thenReturn(AUTH_TOKEN);
        when(organisationService.findUserByEmail(INTERVENER_TEST_EMAIL, AUTH_TOKEN)).thenReturn(Optional.of(INTERVENER_USER_ID));
        List<String> errors = new ArrayList<>();
        service.removeIntervenerDetails(threeWrapper, errors, finremCaseData, CASE_ID_IN_LONG);

        assertNull(finremCaseData.getIntervenerThree().getIntervenerName());
        assertNull(finremCaseData.getIntervenerThree().getIntervenerEmail());
        assertNull(finremCaseData.getIntervenerThree().getIntervenerSolicitorFirm());
        assertNull(finremCaseData.getIntervenerThree().getIntervenerSolicitorReference());
        verify(assignCaseAccessService).removeCaseRoleToUser(CASE_ID_IN_LONG, INTERVENER_USER_ID,
            INTVR_SOLICITOR_3.getCcdCode(), SOME_ORG_ID);
        assertThat(errors).isEmpty();
    }

    @Test
    void givenCase_whenRemoveOperationChosenForIntv4NotRepresented_thenRemoveIntervener() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();

        FinremCaseData finremCaseData = finremCallbackRequest.getCaseDetails().getData();
        IntervenerFour fourWrapper = IntervenerFour
            .builder().intervenerName("Four name").intervenerEmail("test@test.com").intervenerRepresented(YesOrNo.NO).build();
        finremCaseData.setIntervenerFour(fourWrapper);

        List<String> errors = new ArrayList<>();
        service.removeIntervenerDetails(fourWrapper, errors, finremCaseData, CASE_ID_IN_LONG);

        assertNull(finremCaseData.getIntervenerFour().getIntervenerName());
        assertNull(finremCaseData.getIntervenerFour().getIntervenerEmail());
        assertNull(finremCaseData.getIntervenerFour().getIntervenerSolicitorFirm());
        assertNull(finremCaseData.getIntervenerFour().getIntervenerSolicitorReference());
        assertThat(errors).isEmpty();
    }

    @Test
    void givenCase_whenRemoveOperationChosenForIntv4Represented_thenRemoveIntervener() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();

        FinremCaseData finremCaseData = finremCallbackRequest.getCaseDetails().getData();
        OrganisationPolicy organisationPolicy = OrganisationPolicy.builder().organisation(
            Organisation.builder().organisationID(SOME_ORG_ID).organisationName(SOME_ORG_ID).build()
        ).build();
        IntervenerFour fourWrapper = IntervenerFour
            .builder().intervenerName("Four name").intervenerEmail("test@test.com")
            .intervenerRepresented(YesOrNo.YES)
            .intervenerSolEmail("test@test.com")
            .intervenerSolicitorFirm(INTERVENER_SOL_FIRM)
            .intervenerSolicitorReference(INTERVENER_SOL_REFERENCE)
            .intervenerOrganisation(organisationPolicy)
            .build();
        finremCaseData.setIntervenerFour(fourWrapper);

        when(systemUserService.getSysUserToken()).thenReturn(AUTH_TOKEN);
        when(organisationService.findUserByEmail(INTERVENER_TEST_EMAIL, AUTH_TOKEN)).thenReturn(Optional.of(INTERVENER_USER_ID));
        List<String> errors = new ArrayList<>();
        service.removeIntervenerDetails(fourWrapper, errors, finremCaseData, CASE_ID_IN_LONG);

        assertNull(finremCaseData.getIntervenerFour().getIntervenerName());
        assertNull(finremCaseData.getIntervenerFour().getIntervenerEmail());
        assertNull(finremCaseData.getIntervenerFour().getIntervenerSolicitorFirm());
        assertNull(finremCaseData.getIntervenerFour().getIntervenerSolicitorReference());
        verify(assignCaseAccessService).removeCaseRoleToUser(CASE_ID_IN_LONG, INTERVENER_USER_ID,
            INTVR_SOLICITOR_4.getCcdCode(), SOME_ORG_ID);
        assertThat(errors).isEmpty();
    }

    @Test
    void givenContestedCase_whenAddingIntervenerAndIntv1Represent_thenSetIntervenerDateAddedAndDefaultOrg() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseData finremCaseData = finremCallbackRequest.getCaseDetails().getData();
        OrganisationPolicy organisationPolicy = OrganisationPolicy.builder().organisation(
            Organisation.builder().organisationID(SOME_ORG_ID).organisationName(SOME_ORG_ID).build()
        ).build();
        IntervenerOne oneWrapper = IntervenerOne
            .builder().intervenerName("One name").intervenerEmail("test@test.com")
            .intervenerSolEmail("test@test.com")
            .intervenerSolicitorFirm(INTERVENER_SOL_FIRM)
            .intervenerSolicitorReference(INTERVENER_SOL_REFERENCE)
            .intervenerRepresented(YesOrNo.YES)
            .intervenerOrganisation(organisationPolicy).build();
        finremCaseData.setIntervenerOne(oneWrapper);

        DynamicRadioListElement option = DynamicRadioListElement.builder().code(INTERVENER_ONE).build();
        List<DynamicRadioListElement> list = List.of(option);
        DynamicRadioList dynamicRadioList = DynamicRadioList.builder().listItems(list).build();
        finremCaseData.setIntervenersList(dynamicRadioList);
        DynamicRadioListElement option1 = DynamicRadioListElement.builder().code(INTERVENER_ONE).build();
        finremCaseData.getIntervenersList().setValue(option1);

        when(systemUserService.getSysUserToken()).thenReturn(AUTH_TOKEN);
        when(organisationService.findUserByEmail(INTERVENER_TEST_EMAIL, AUTH_TOKEN)).thenReturn(Optional.of(INTERVENER_USER_ID));
        List<String> errors = new ArrayList<>();
        service.updateIntervenerDetails(oneWrapper, errors, finremCallbackRequest);

        assertNotNull(finremCaseData.getIntervenerOne().getIntervenerDateAdded());
        verify(assignCaseAccessService).grantCaseRoleToUser(CASE_ID_IN_LONG, INTERVENER_USER_ID,
            INTVR_SOLICITOR_1.getCcdCode(), SOME_ORG_ID);
        assertThat(errors).isEmpty();
    }

    @Test
    void givenContestedCase_whenAddingIntervenerAndIntv1RepresentWithNullOrgId_thenSetIntervenerDateAddedAndDefaultOrg() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseData finremCaseData = finremCallbackRequest.getCaseDetails().getData();
        OrganisationPolicy organisationPolicy = OrganisationPolicy.builder().organisation(
            Organisation.builder().organisationID(null).organisationName(null).build()
        ).build();
        IntervenerOne oneWrapper = IntervenerOne
            .builder().intervenerName("One name").intervenerEmail("test@test.com")
            .intervenerSolEmail("test@test.com")
            .intervenerSolicitorFirm(INTERVENER_SOL_FIRM)
            .intervenerSolicitorReference(INTERVENER_SOL_REFERENCE)
            .intervenerRepresented(YesOrNo.YES)
            .intervenerOrganisation(organisationPolicy).build();
        finremCaseData.setIntervenerOne(oneWrapper);

        DynamicRadioListElement option = DynamicRadioListElement.builder().code(INTERVENER_ONE).build();
        List<DynamicRadioListElement> list = List.of(option);
        DynamicRadioList dynamicRadioList = DynamicRadioList.builder().listItems(list).build();
        finremCaseData.setIntervenersList(dynamicRadioList);
        DynamicRadioListElement option1 = DynamicRadioListElement.builder().code(INTERVENER_ONE).build();
        finremCaseData.getIntervenersList().setValue(option1);

        when(systemUserService.getSysUserToken()).thenReturn(AUTH_TOKEN);
        when(organisationService.findUserByEmail(INTERVENER_TEST_EMAIL, AUTH_TOKEN)).thenReturn(Optional.of(INTERVENER_USER_ID));
        List<String> errors = new ArrayList<>();
        service.updateIntervenerDetails(oneWrapper, errors, finremCallbackRequest);

        assertNotNull(finremCaseData.getIntervenerOne().getIntervenerDateAdded());
        verify(assignCaseAccessService).grantCaseRoleToUser(CASE_ID_IN_LONG, INTERVENER_USER_ID,
            INTVR_SOLICITOR_1.getCcdCode(), null);
        assertThat(errors).isEmpty();
    }

    @Test
    void givenContestedCase_whenUpdatingIntervener1AndChangedRepresentationYesToNoAndCountryNotProvided_thenShowError() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseData finremCaseData = finremCallbackRequest.getCaseDetails().getData();
        FinremCaseData finremCaseDataBefore = finremCallbackRequest.getCaseDetailsBefore().getData();
        OrganisationPolicy organisationPolicy = OrganisationPolicy.builder().organisation(
            Organisation.builder().organisationID(SOME_ORG_ID).organisationName(SOME_ORG_ID).build()
        ).build();
        IntervenerOne oneWrapper = IntervenerOne
            .builder().intervenerName("One name").intervenerEmail("test@test.com")
            .intervenerSolEmail("test@test.com")
            .intervenerSolicitorFirm(INTERVENER_SOL_FIRM)
            .intervenerSolicitorReference(INTERVENER_SOL_REFERENCE)
            .intervenerRepresented(YesOrNo.YES)
            .intervenerDateAdded(LocalDate.of(2023, 1, 1))
            .intervenerOrganisation(organisationPolicy).build();
        finremCaseDataBefore.setIntervenerOne(oneWrapper);

        IntervenerOne oneWrapper1 = IntervenerOne
            .builder().intervenerName("One name").intervenerEmail("test@test.com")
            .intervenerRepresented(YesOrNo.NO)
            .intervenerResideOutsideUK(YesOrNo.YES)
            .intervenerDateAdded(LocalDate.of(2023, 1, 1))
            .intervenerOrganisation(organisationPolicy).build();
        finremCaseData.setIntervenerOne(oneWrapper1);


        DynamicRadioListElement option = DynamicRadioListElement.builder().code(INTERVENER_ONE).build();
        List<DynamicRadioListElement> list = List.of(option);
        DynamicRadioList dynamicRadioList = DynamicRadioList.builder().listItems(list).build();
        finremCaseData.setIntervenersList(dynamicRadioList);
        DynamicRadioListElement option1 = DynamicRadioListElement.builder().code(INTERVENER_ONE).build();
        finremCaseData.getIntervenersList().setValue(option1);

        when(systemUserService.getSysUserToken()).thenReturn(AUTH_TOKEN);
        when(organisationService.findUserByEmail(INTERVENER_TEST_EMAIL, AUTH_TOKEN)).thenReturn(Optional.of(INTERVENER_USER_ID));
        List<String> errors = new ArrayList<>();
        service.updateIntervenerDetails(oneWrapper1, errors, finremCallbackRequest);

        IntervenerOne intervenerOneWrapper = finremCaseData.getIntervenerOne();

        assertNotNull(intervenerOneWrapper.getIntervenerDateAdded());
        assertNull(intervenerOneWrapper.getIntervenerSolEmail());
        assertNull(intervenerOneWrapper.getIntervenerSolName());
        assertNull(intervenerOneWrapper.getIntervenerSolPhone());
        assertNull(intervenerOneWrapper.getIntervenerSolicitorFirm());
        assertNull(intervenerOneWrapper.getIntervenerSolicitorReference());
        assertNull(intervenerOneWrapper.getIntervenerOrganisation().getOrganisation().getOrganisationID());
        assertNull(intervenerOneWrapper.getIntervenerOrganisation().getOrganisation().getOrganisationName());
        verify(assignCaseAccessService).removeCaseRoleToUser(CASE_ID_IN_LONG, INTERVENER_USER_ID,
            INTVR_SOLICITOR_1.getCcdCode(), SOME_ORG_ID);
        assertThat(errors).contains("If intervener resides outside of UK, please provide the country of residence.");
    }

    @Test
    void givenContestedCase_whenUpdatingIntervener1AndChangedRepresentationYesToNo_thenHandle() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseData finremCaseData = finremCallbackRequest.getCaseDetails().getData();
        FinremCaseData finremCaseDataBefore = finremCallbackRequest.getCaseDetailsBefore().getData();
        OrganisationPolicy organisationPolicy = OrganisationPolicy.builder().organisation(
            Organisation.builder().organisationID(SOME_ORG_ID).organisationName(SOME_ORG_ID).build()
        ).build();
        IntervenerOne oneWrapper = IntervenerOne
            .builder().intervenerName("One name").intervenerEmail("test@test.com")
            .intervenerSolEmail("test@test.com")
            .intervenerSolicitorFirm(INTERVENER_SOL_FIRM)
            .intervenerSolicitorReference(INTERVENER_SOL_REFERENCE)
            .intervenerRepresented(YesOrNo.YES)
            .intervenerDateAdded(LocalDate.of(2023, 1, 1))
            .intervenerOrganisation(organisationPolicy).build();
        finremCaseDataBefore.setIntervenerOne(oneWrapper);

        IntervenerOne oneWrapper1 = IntervenerOne
            .builder().intervenerName("One name").intervenerEmail("test@test.com")
            .intervenerRepresented(YesOrNo.NO)
            .intervenerDateAdded(LocalDate.of(2023, 1, 1))
            .intervenerAddress(Address.builder().addressLine1("1 London Road").build())
            .intervenerOrganisation(organisationPolicy).build();
        finremCaseData.setIntervenerOne(oneWrapper1);


        DynamicRadioListElement option = DynamicRadioListElement.builder().code(INTERVENER_ONE).build();
        List<DynamicRadioListElement> list = List.of(option);
        DynamicRadioList dynamicRadioList = DynamicRadioList.builder().listItems(list).build();
        finremCaseData.setIntervenersList(dynamicRadioList);
        DynamicRadioListElement option1 = DynamicRadioListElement.builder().code(INTERVENER_ONE).build();
        finremCaseData.getIntervenersList().setValue(option1);

        when(systemUserService.getSysUserToken()).thenReturn(AUTH_TOKEN);
        when(organisationService.findUserByEmail(INTERVENER_TEST_EMAIL, AUTH_TOKEN)).thenReturn(Optional.of(INTERVENER_USER_ID));
        List<String> errors = new ArrayList<>();
        service.updateIntervenerDetails(oneWrapper1, errors, finremCallbackRequest);

        IntervenerOne intervenerOne = finremCaseData.getIntervenerOne();

        assertNotNull(intervenerOne.getIntervenerDateAdded());
        assertNull(intervenerOne.getIntervenerSolEmail());
        assertNull(intervenerOne.getIntervenerSolName());
        assertNull(intervenerOne.getIntervenerSolPhone());
        assertNull(intervenerOne.getIntervenerSolicitorFirm());
        assertNull(intervenerOne.getIntervenerSolicitorReference());
        assertNull(intervenerOne.getIntervenerOrganisation().getOrganisation().getOrganisationID());
        assertNull(intervenerOne.getIntervenerOrganisation().getOrganisation().getOrganisationName());
        verify(assignCaseAccessService).removeCaseRoleToUser(CASE_ID_IN_LONG, INTERVENER_USER_ID,
            INTVR_SOLICITOR_1.getCcdCode(), SOME_ORG_ID);
        assertThat(errors).isEmpty();
    }

    @Test
    void givenContestedCase_whenUpdatingIntervener1AndChangedRepresentationSolEmailChanged_thenHandle() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseData finremCaseData = finremCallbackRequest.getCaseDetails().getData();
        FinremCaseData finremCaseDataBefore = finremCallbackRequest.getCaseDetailsBefore().getData();
        OrganisationPolicy organisationPolicy = OrganisationPolicy.builder().organisation(
            Organisation.builder().organisationID(SOME_ORG_ID).organisationName(SOME_ORG_ID).build()
        ).build();

        IntervenerOne oneWrapper = IntervenerOne
            .builder().intervenerName("One name").intervenerEmail("test@test.com")
            .intervenerSolEmail("test@test.com")
            .intervenerSolName("test test.com")
            .intervenerSolPhone("33333333333")
            .intervenerSolicitorFirm(INTERVENER_SOL_FIRM)
            .intervenerSolicitorReference(INTERVENER_SOL_REFERENCE)
            .intervenerRepresented(YesOrNo.YES)
            .intervenerDateAdded(LocalDate.of(2023, 1, 1))
            .intervenerOrganisation(organisationPolicy).build();
        finremCaseDataBefore.setIntervenerOne(oneWrapper);

        IntervenerOne oneWrapper1 = IntervenerOne
            .builder().intervenerName("One name").intervenerEmail("test@test.com")
            .intervenerSolEmail(INTERVENER_TEST_EMAIL_CHANGE)
            .intervenerSolName("test test.com")
            .intervenerSolPhone("33333333333")
            .intervenerSolicitorFirm(INTERVENER_SOL_FIRM)
            .intervenerSolicitorReference(INTERVENER_SOL_REFERENCE)
            .intervenerRepresented(YesOrNo.YES)
            .intervenerDateAdded(LocalDate.of(2023, 1, 1))
            .intervenerOrganisation(organisationPolicy).build();
        finremCaseData.setIntervenerOne(oneWrapper1);


        DynamicRadioListElement option = DynamicRadioListElement.builder().code(INTERVENER_ONE).build();
        List<DynamicRadioListElement> list = List.of(option);
        DynamicRadioList dynamicRadioList = DynamicRadioList.builder().listItems(list).build();
        finremCaseData.setIntervenersList(dynamicRadioList);
        DynamicRadioListElement option1 = DynamicRadioListElement.builder().code(INTERVENER_ONE).build();
        finremCaseData.getIntervenersList().setValue(option1);

        when(systemUserService.getSysUserToken()).thenReturn(AUTH_TOKEN);
        when(organisationService.findUserByEmail(INTERVENER_TEST_EMAIL, AUTH_TOKEN)).thenReturn(Optional.of(INTERVENER_USER_ID));
        when(organisationService.findUserByEmail(INTERVENER_TEST_EMAIL_CHANGE, AUTH_TOKEN)).thenReturn(Optional.of(INTERVENER_USER_ID));
        List<String> errors = new ArrayList<>();
        service.updateIntervenerDetails(oneWrapper1, errors, finremCallbackRequest);

        IntervenerOne intervenerOne = finremCaseData.getIntervenerOne();
        assertNotNull(intervenerOne.getIntervenerDateAdded());
        assertNotNull(intervenerOne.getIntervenerSolEmail());
        assertNotNull(intervenerOne.getIntervenerSolName());
        assertNotNull(intervenerOne.getIntervenerSolPhone());
        assertNotNull(intervenerOne.getIntervenerSolicitorFirm());
        assertNotNull(intervenerOne.getIntervenerSolicitorReference());
        assertNotNull(intervenerOne.getIntervenerOrganisation().getOrganisation().getOrganisationID());
        assertNotNull(intervenerOne.getIntervenerOrganisation().getOrganisation().getOrganisationName());

        verify(assignCaseAccessService).grantCaseRoleToUser(CASE_ID_IN_LONG, INTERVENER_USER_ID,
            INTVR_SOLICITOR_1.getCcdCode(), SOME_ORG_ID);
        verify(assignCaseAccessService).removeCaseRoleToUser(CASE_ID_IN_LONG, INTERVENER_USER_ID,
            INTVR_SOLICITOR_1.getCcdCode(), SOME_ORG_ID);
        assertThat(errors).isEmpty();
    }

    @Test
    void givenContestedCase_whenUpdatingIntervener1AndChangedRepresentationSolOrgChanged_thenHandle() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseData finremCaseData = finremCallbackRequest.getCaseDetails().getData();
        FinremCaseData finremCaseDataBefore = finremCallbackRequest.getCaseDetailsBefore().getData();
        OrganisationPolicy organisationPolicy = OrganisationPolicy.builder().organisation(
            Organisation.builder().organisationID(SOME_ORG_ID).organisationName(SOME_ORG_ID).build()
        ).build();
        OrganisationPolicy organisationPolicyChange = OrganisationPolicy.builder().organisation(
            Organisation.builder().organisationID(CHANGE_ORG_ID).organisationName(CHANGE_ORG_ID).build()
        ).build();
        IntervenerOne oneWrapper = IntervenerOne
            .builder().intervenerName("One name").intervenerEmail("test@test.com")
            .intervenerSolEmail(INTERVENER_TEST_EMAIL)
            .intervenerSolName("test test.com")
            .intervenerSolPhone("33333333333")
            .intervenerSolicitorFirm(INTERVENER_SOL_FIRM)
            .intervenerSolicitorReference(INTERVENER_SOL_REFERENCE)
            .intervenerRepresented(YesOrNo.YES)
            .intervenerDateAdded(LocalDate.of(2023, 1, 1))
            .intervenerOrganisation(organisationPolicy).build();
        finremCaseDataBefore.setIntervenerOne(oneWrapper);

        IntervenerOne oneWrapper1 = IntervenerOne
            .builder().intervenerName("One name").intervenerEmail("test@test.com")
            .intervenerSolEmail(INTERVENER_TEST_EMAIL)
            .intervenerSolName("test test.com")
            .intervenerSolPhone("33333333333")
            .intervenerSolicitorFirm(INTERVENER_SOL_FIRM)
            .intervenerSolicitorReference(INTERVENER_SOL_REFERENCE)
            .intervenerRepresented(YesOrNo.YES)
            .intervenerDateAdded(LocalDate.of(2023, 1, 1))
            .intervenerOrganisation(organisationPolicyChange).build();
        finremCaseData.setIntervenerOne(oneWrapper1);


        DynamicRadioListElement option = DynamicRadioListElement.builder().code(INTERVENER_ONE).build();
        List<DynamicRadioListElement> list = List.of(option);
        DynamicRadioList dynamicRadioList = DynamicRadioList.builder().listItems(list).build();
        finremCaseData.setIntervenersList(dynamicRadioList);
        DynamicRadioListElement option1 = DynamicRadioListElement.builder().code(INTERVENER_ONE).build();
        finremCaseData.getIntervenersList().setValue(option1);

        when(systemUserService.getSysUserToken()).thenReturn(AUTH_TOKEN);
        when(organisationService.findUserByEmail(INTERVENER_TEST_EMAIL, AUTH_TOKEN)).thenReturn(Optional.of(INTERVENER_USER_ID));

        List<String> errors = new ArrayList<>();
        service.updateIntervenerDetails(oneWrapper1, errors, finremCallbackRequest);

        IntervenerOne intervenerOne = finremCaseData.getIntervenerOne();
        assertNotNull(intervenerOne.getIntervenerDateAdded());
        assertNotNull(intervenerOne.getIntervenerSolEmail());
        assertNotNull(intervenerOne.getIntervenerSolName());
        assertNotNull(intervenerOne.getIntervenerSolPhone());
        assertNotNull(intervenerOne.getIntervenerSolicitorFirm());
        assertNotNull(intervenerOne.getIntervenerSolicitorReference());
        assertNotNull(intervenerOne.getIntervenerOrganisation().getOrganisation().getOrganisationID());
        assertNotNull(intervenerOne.getIntervenerOrganisation().getOrganisation().getOrganisationName());

        verify(assignCaseAccessService).grantCaseRoleToUser(CASE_ID_IN_LONG, INTERVENER_USER_ID,
            INTVR_SOLICITOR_1.getCcdCode(), CHANGE_ORG_ID);
        verify(assignCaseAccessService).removeCaseRoleToUser(CASE_ID_IN_LONG, INTERVENER_USER_ID,
            INTVR_SOLICITOR_1.getCcdCode(), SOME_ORG_ID);
        assertThat(errors).isEmpty();
    }

    @Test
    void givenContestedCase_whenUpdatingIntervener1AndChangedRepresentationNoToYes_thenHandle() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseData finremCaseData = finremCallbackRequest.getCaseDetails().getData();
        FinremCaseData finremCaseDataBefore = finremCallbackRequest.getCaseDetailsBefore().getData();
        OrganisationPolicy organisationPolicy = OrganisationPolicy.builder().organisation(
            Organisation.builder().organisationID(SOME_ORG_ID).organisationName(SOME_ORG_ID).build()
        ).build();
        IntervenerOne oneWrapper = IntervenerOne
            .builder().intervenerName("One name").intervenerEmail("test@test.com")
            .intervenerRepresented(YesOrNo.NO)
            .intervenerDateAdded(LocalDate.of(2023, 1, 1))
            .intervenerOrganisation(organisationPolicy).build();
        finremCaseDataBefore.setIntervenerOne(oneWrapper);

        IntervenerOne oneWrapper1 = IntervenerOne
            .builder().intervenerName("One name").intervenerEmail("test@test.com")
            .intervenerRepresented(YesOrNo.YES)
            .intervenerDateAdded(LocalDate.of(2023, 1, 1))
            .intervenerOrganisation(organisationPolicy)
            .intervenerSolName("SOL name")
            .intervenerSolEmail("test@test.com")
            .intervenerSolPhone("112333333")
            .intervenerSolicitorFirm(INTERVENER_SOL_FIRM)
            .intervenerSolicitorReference(INTERVENER_SOL_REFERENCE)
            .build();
        finremCaseData.setIntervenerOne(oneWrapper1);


        DynamicRadioListElement option = DynamicRadioListElement.builder().code(INTERVENER_ONE).build();
        List<DynamicRadioListElement> list = List.of(option);
        DynamicRadioList dynamicRadioList = DynamicRadioList.builder().listItems(list).build();
        finremCaseData.setIntervenersList(dynamicRadioList);
        DynamicRadioListElement option1 = DynamicRadioListElement.builder().code(INTERVENER_ONE).build();
        finremCaseData.getIntervenersList().setValue(option1);

        when(systemUserService.getSysUserToken()).thenReturn(AUTH_TOKEN);
        when(organisationService.findUserByEmail(INTERVENER_TEST_EMAIL, AUTH_TOKEN)).thenReturn(Optional.of(INTERVENER_USER_ID));

        List<String> errors = new ArrayList<>();
        service.updateIntervenerDetails(oneWrapper1, errors, finremCallbackRequest);

        IntervenerOne intervenerOne = finremCaseData.getIntervenerOne();
        assertNotNull(intervenerOne.getIntervenerSolEmail());
        assertNotNull(intervenerOne.getIntervenerSolName());
        assertNotNull(intervenerOne.getIntervenerSolicitorFirm());
        assertNotNull(intervenerOne.getIntervenerSolicitorReference());
        assertNotNull(intervenerOne.getIntervenerOrganisation().getOrganisation().getOrganisationID());
        assertNotNull(intervenerOne.getIntervenerOrganisation().getOrganisation().getOrganisationName());
        verify(assignCaseAccessService).grantCaseRoleToUser(CASE_ID_IN_LONG, INTERVENER_USER_ID,
            INTVR_SOLICITOR_1.getCcdCode(), SOME_ORG_ID);
        assertThat(errors).isEmpty();
    }

    @Test
    void givenContestedCase_whenAddingIntervenerAndIntv1NotRepresent_thenSetIntervenerDateAddedAndDefaultOrg() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseData finremCaseData = finremCallbackRequest.getCaseDetails().getData();
        OrganisationPolicy organisationPolicy = OrganisationPolicy.builder().organisation(
            Organisation.builder().organisationID(null).organisationName(null).build()
        ).build();
        IntervenerOne oneWrapper = IntervenerOne
            .builder().intervenerName("One name").intervenerEmail("test@test.com")
            .intervenerRepresented(YesOrNo.NO)
            .intervenerAddress(Address.builder().addressLine1("1 London Road").build())
            .intervenerOrganisation(organisationPolicy).build();
        finremCaseData.setIntervenerOne(oneWrapper);

        DynamicRadioListElement option = DynamicRadioListElement.builder().code(INTERVENER_ONE).build();
        List<DynamicRadioListElement> list = List.of(option);
        DynamicRadioList dynamicRadioList = DynamicRadioList.builder().listItems(list).build();
        finremCaseData.setIntervenersList(dynamicRadioList);
        DynamicRadioListElement option1 = DynamicRadioListElement.builder().code(INTERVENER_ONE).build();
        finremCaseData.getIntervenersList().setValue(option1);

        List<String> errors = new ArrayList<>();
        service.updateIntervenerDetails(oneWrapper, errors, finremCallbackRequest);

        assertNotNull(finremCaseData.getIntervenerOne().getIntervenerDateAdded());
        assertNotNull(finremCaseData.getIntervenerOne().getIntervenerOrganisation().getOrgPolicyCaseAssignedRole());
        assertThat(errors).isEmpty();
    }

    @Test
    void givenContestedCase_whenAddingIntervenerAndIntv2NotRepresent_thenSetIntervenerDateAddedAndDefaultOrg() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseData finremCaseData = finremCallbackRequest.getCaseDetails().getData();
        OrganisationPolicy organisationPolicy = OrganisationPolicy.builder().organisation(
            Organisation.builder().organisationID(null).organisationName(null).build()
        ).build();
        IntervenerTwo wrapper = IntervenerTwo
            .builder().intervenerName("Two name").intervenerEmail("test@test.com")
            .intervenerRepresented(YesOrNo.NO)
            .intervenerAddress(Address.builder().addressLine1("1 London Road").build())
            .intervenerOrganisation(organisationPolicy).build();
        finremCaseData.setIntervenerTwo(wrapper);

        DynamicRadioListElement option = DynamicRadioListElement.builder().code(INTERVENER_TWO).build();
        List<DynamicRadioListElement> list = List.of(option);
        DynamicRadioList dynamicRadioList = DynamicRadioList.builder().listItems(list).build();
        finremCaseData.setIntervenersList(dynamicRadioList);
        DynamicRadioListElement option1 = DynamicRadioListElement.builder().code(INTERVENER_TWO).build();
        finremCaseData.getIntervenersList().setValue(option1);

        List<String> errors = new ArrayList<>();
        service.updateIntervenerDetails(wrapper, errors, finremCallbackRequest);

        assertNotNull(finremCaseData.getIntervenerTwo().getIntervenerDateAdded());
        assertNotNull(finremCaseData.getIntervenerTwo().getIntervenerOrganisation().getOrgPolicyCaseAssignedRole());
        assertThat(errors).isEmpty();
    }

    @Test
    void givenContestedCase_whenAddingIntervenerAndIntv2Represent_thenSetIntervenerDateAddedAndDefaultOrg() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseData finremCaseData = finremCallbackRequest.getCaseDetails().getData();
        OrganisationPolicy organisationPolicy = OrganisationPolicy.builder().organisation(
            Organisation.builder().organisationID(SOME_ORG_ID).organisationName(SOME_ORG_ID).build()
        ).build();
        IntervenerTwo wrapper = IntervenerTwo
            .builder().intervenerName("Two name").intervenerEmail("test@test.com")
            .intervenerSolEmail("test@test.com")
            .intervenerRepresented(YesOrNo.YES)
            .intervenerSolicitorFirm(INTERVENER_SOL_FIRM)
            .intervenerSolicitorReference(INTERVENER_SOL_REFERENCE)
            .intervenerOrganisation(organisationPolicy).build();
        finremCaseData.setIntervenerTwo(wrapper);

        DynamicRadioListElement option = DynamicRadioListElement.builder().code(INTERVENER_TWO).build();
        List<DynamicRadioListElement> list = List.of(option);
        DynamicRadioList dynamicRadioList = DynamicRadioList.builder().listItems(list).build();
        finremCaseData.setIntervenersList(dynamicRadioList);
        DynamicRadioListElement option1 = DynamicRadioListElement.builder().code(INTERVENER_TWO).build();
        finremCaseData.getIntervenersList().setValue(option1);

        when(systemUserService.getSysUserToken()).thenReturn(AUTH_TOKEN);
        when(organisationService.findUserByEmail(INTERVENER_TEST_EMAIL, AUTH_TOKEN)).thenReturn(Optional.of(INTERVENER_USER_ID));
        List<String> errors = new ArrayList<>();
        service.updateIntervenerDetails(wrapper, errors, finremCallbackRequest);

        assertNotNull(finremCaseData.getIntervenerTwo().getIntervenerDateAdded());
        verify(assignCaseAccessService).grantCaseRoleToUser(CASE_ID_IN_LONG, INTERVENER_USER_ID,
            INTVR_SOLICITOR_2.getCcdCode(), SOME_ORG_ID);
        assertThat(errors).isEmpty();
    }

    @Test
    void givenContestedCase_whenUpdatingIntervener2AndChangedRepresentationYesToNo_thenHandle() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseData finremCaseData = finremCallbackRequest.getCaseDetails().getData();
        FinremCaseData finremCaseDataBefore = finremCallbackRequest.getCaseDetailsBefore().getData();
        OrganisationPolicy organisationPolicy = OrganisationPolicy.builder().organisation(
            Organisation.builder().organisationID(SOME_ORG_ID).organisationName(SOME_ORG_ID).build()
        ).build();
        IntervenerTwo wrapper = IntervenerTwo
            .builder().intervenerName("Two name").intervenerEmail("test@test.com")
            .intervenerSolEmail("test@test.com")
            .intervenerSolName("TwoSol name")
            .intervenerSolicitorFirm(INTERVENER_SOL_FIRM)
            .intervenerSolicitorReference(INTERVENER_SOL_REFERENCE)
            .intervenerDateAdded(LocalDate.of(2023, 1, 1))
            .intervenerRepresented(YesOrNo.YES)
            .intervenerOrganisation(organisationPolicy).build();
        finremCaseDataBefore.setIntervenerTwo(wrapper);

        IntervenerTwo current = IntervenerTwo
            .builder().intervenerName("Two name").intervenerEmail("test@test.com")
            .intervenerRepresented(YesOrNo.NO)
            .intervenerDateAdded(LocalDate.of(2023, 1, 1))
            .intervenerAddress(Address.builder().addressLine1("1 London Road").build())
            .intervenerOrganisation(organisationPolicy).build();
        finremCaseData.setIntervenerTwo(current);

        DynamicRadioListElement option = DynamicRadioListElement.builder().code(INTERVENER_TWO).build();
        List<DynamicRadioListElement> list = List.of(option);
        DynamicRadioList dynamicRadioList = DynamicRadioList.builder().listItems(list).build();
        finremCaseData.setIntervenersList(dynamicRadioList);
        DynamicRadioListElement option1 = DynamicRadioListElement.builder().code(INTERVENER_TWO).build();
        finremCaseData.getIntervenersList().setValue(option1);

        when(systemUserService.getSysUserToken()).thenReturn(AUTH_TOKEN);
        when(organisationService.findUserByEmail(INTERVENER_TEST_EMAIL, AUTH_TOKEN)).thenReturn(Optional.of(INTERVENER_USER_ID));
        List<String> errors = new ArrayList<>();
        service.updateIntervenerDetails(current, errors, finremCallbackRequest);

        IntervenerTwo intervenerTwo = finremCaseData.getIntervenerTwo();
        assertNotNull(intervenerTwo.getIntervenerDateAdded());
        assertNull(intervenerTwo.getIntervenerSolEmail());
        assertNull(intervenerTwo.getIntervenerSolName());
        assertNull(intervenerTwo.getIntervenerSolPhone());
        assertNull(intervenerTwo.getIntervenerSolicitorFirm());
        assertNull(intervenerTwo.getIntervenerSolicitorReference());
        assertNull(intervenerTwo.getIntervenerOrganisation().getOrganisation().getOrganisationID());
        assertNull(intervenerTwo.getIntervenerOrganisation().getOrganisation().getOrganisationName());
        verify(assignCaseAccessService).removeCaseRoleToUser(CASE_ID_IN_LONG, INTERVENER_USER_ID,
            INTVR_SOLICITOR_2.getCcdCode(), SOME_ORG_ID);
        assertThat(errors).isEmpty();
    }

    @Test
    void givenContestedCase_whenUpdatingIntervener2AndChangedRepresentationSolOrgChanged_thenHandle() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseData finremCaseData = finremCallbackRequest.getCaseDetails().getData();
        FinremCaseData finremCaseDataBefore = finremCallbackRequest.getCaseDetailsBefore().getData();
        OrganisationPolicy organisationPolicy = OrganisationPolicy.builder().organisation(
            Organisation.builder().organisationID(SOME_ORG_ID).organisationName(SOME_ORG_ID).build()
        ).build();
        OrganisationPolicy organisationPolicyChange = OrganisationPolicy.builder().organisation(
            Organisation.builder().organisationID(CHANGE_ORG_ID).organisationName(CHANGE_ORG_ID).build()
        ).build();
        IntervenerTwo wrapper = IntervenerTwo
            .builder().intervenerName("Two name").intervenerEmail("test@test.com")
            .intervenerSolEmail(INTERVENER_TEST_EMAIL)
            .intervenerSolName("TwoSol name")
            .intervenerSolPhone("11122111111")
            .intervenerSolicitorFirm(INTERVENER_SOL_FIRM)
            .intervenerSolicitorReference(INTERVENER_SOL_REFERENCE)
            .intervenerDateAdded(LocalDate.of(2023, 1, 1))
            .intervenerRepresented(YesOrNo.YES)
            .intervenerOrganisation(organisationPolicy).build();
        finremCaseDataBefore.setIntervenerTwo(wrapper);

        IntervenerTwo current = IntervenerTwo
            .builder().intervenerName("Two name").intervenerEmail("test@test.com")
            .intervenerSolEmail(INTERVENER_TEST_EMAIL)
            .intervenerSolName("TwoSol name")
            .intervenerSolPhone("11122111111")
            .intervenerSolicitorFirm(INTERVENER_SOL_FIRM)
            .intervenerSolicitorReference(INTERVENER_SOL_REFERENCE)
            .intervenerDateAdded(LocalDate.of(2023, 1, 1))
            .intervenerRepresented(YesOrNo.YES)
            .intervenerOrganisation(organisationPolicyChange).build();
        finremCaseData.setIntervenerTwo(current);

        DynamicRadioListElement option = DynamicRadioListElement.builder().code(INTERVENER_TWO).build();
        List<DynamicRadioListElement> list = List.of(option);
        DynamicRadioList dynamicRadioList = DynamicRadioList.builder().listItems(list).build();
        finremCaseData.setIntervenersList(dynamicRadioList);
        DynamicRadioListElement option1 = DynamicRadioListElement.builder().code(INTERVENER_TWO).build();
        finremCaseData.getIntervenersList().setValue(option1);

        when(systemUserService.getSysUserToken()).thenReturn(AUTH_TOKEN);
        when(organisationService.findUserByEmail(INTERVENER_TEST_EMAIL, AUTH_TOKEN)).thenReturn(Optional.of(INTERVENER_USER_ID));
        List<String> errors = new ArrayList<>();
        service.updateIntervenerDetails(current, errors, finremCallbackRequest);

        IntervenerTwo intervenerTwo = finremCaseData.getIntervenerTwo();
        assertNotNull(intervenerTwo.getIntervenerDateAdded());
        assertNotNull(intervenerTwo.getIntervenerSolEmail());
        assertNotNull(intervenerTwo.getIntervenerSolName());
        assertNotNull(intervenerTwo.getIntervenerSolPhone());
        assertNotNull(intervenerTwo.getIntervenerSolicitorFirm());
        assertNotNull(intervenerTwo.getIntervenerSolicitorReference());
        assertNotNull(intervenerTwo.getIntervenerOrganisation().getOrganisation().getOrganisationID());
        assertNotNull(intervenerTwo.getIntervenerOrganisation().getOrganisation().getOrganisationName());

        verify(assignCaseAccessService).grantCaseRoleToUser(CASE_ID_IN_LONG, INTERVENER_USER_ID,
            INTVR_SOLICITOR_2.getCcdCode(), CHANGE_ORG_ID);
        verify(assignCaseAccessService).removeCaseRoleToUser(CASE_ID_IN_LONG, INTERVENER_USER_ID,
            INTVR_SOLICITOR_2.getCcdCode(), SOME_ORG_ID);
        assertThat(errors).isEmpty();
    }

    @Test
    void givenContestedCase_whenUpdatingIntervenerAndChangedRepresentationSolEmailChanged_thenHandle() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseData finremCaseData = finremCallbackRequest.getCaseDetails().getData();
        FinremCaseData finremCaseDataBefore = finremCallbackRequest.getCaseDetailsBefore().getData();
        OrganisationPolicy organisationPolicy = OrganisationPolicy.builder().organisation(
            Organisation.builder().organisationID(SOME_ORG_ID).organisationName(SOME_ORG_ID).build()
        ).build();

        IntervenerTwo wrapper = IntervenerTwo
            .builder().intervenerName("Two name").intervenerEmail("test@test.com")
            .intervenerSolEmail("test@test.com")
            .intervenerSolName("TwoSol name")
            .intervenerSolPhone("11122111111")
            .intervenerSolicitorFirm(INTERVENER_SOL_FIRM)
            .intervenerSolicitorReference(INTERVENER_SOL_REFERENCE)
            .intervenerDateAdded(LocalDate.of(2023, 1, 1))
            .intervenerRepresented(YesOrNo.YES)
            .intervenerOrganisation(organisationPolicy).build();
        finremCaseDataBefore.setIntervenerTwo(wrapper);

        IntervenerTwo current = IntervenerTwo
            .builder().intervenerName("Two name").intervenerEmail("test@test.com")
            .intervenerSolEmail(INTERVENER_TEST_EMAIL_CHANGE)
            .intervenerSolName("TwoSol name")
            .intervenerSolPhone("11122111111")
            .intervenerSolicitorFirm(INTERVENER_SOL_FIRM)
            .intervenerSolicitorReference(INTERVENER_SOL_REFERENCE)
            .intervenerDateAdded(LocalDate.of(2023, 1, 1))
            .intervenerRepresented(YesOrNo.YES)
            .intervenerOrganisation(organisationPolicy).build();
        finremCaseData.setIntervenerTwo(current);

        DynamicRadioListElement option = DynamicRadioListElement.builder().code(INTERVENER_TWO).build();
        List<DynamicRadioListElement> list = List.of(option);
        DynamicRadioList dynamicRadioList = DynamicRadioList.builder().listItems(list).build();
        finremCaseData.setIntervenersList(dynamicRadioList);
        DynamicRadioListElement option1 = DynamicRadioListElement.builder().code(INTERVENER_TWO).build();
        finremCaseData.getIntervenersList().setValue(option1);

        when(systemUserService.getSysUserToken()).thenReturn(AUTH_TOKEN);
        when(organisationService.findUserByEmail(INTERVENER_TEST_EMAIL, AUTH_TOKEN)).thenReturn(Optional.of(INTERVENER_USER_ID));
        when(organisationService.findUserByEmail(INTERVENER_TEST_EMAIL_CHANGE, AUTH_TOKEN)).thenReturn(Optional.of(INTERVENER_USER_ID));

        List<String> errors = new ArrayList<>();
        service.updateIntervenerDetails(current, errors, finremCallbackRequest);

        IntervenerTwo intervenerTwo = finremCaseData.getIntervenerTwo();
        assertNotNull(intervenerTwo.getIntervenerDateAdded());
        assertNotNull(intervenerTwo.getIntervenerSolEmail());
        assertNotNull(intervenerTwo.getIntervenerSolName());
        assertNotNull(intervenerTwo.getIntervenerSolPhone());
        assertNotNull(intervenerTwo.getIntervenerSolicitorFirm());
        assertNotNull(intervenerTwo.getIntervenerSolicitorReference());
        assertNotNull(intervenerTwo.getIntervenerOrganisation().getOrganisation().getOrganisationID());
        assertNotNull(intervenerTwo.getIntervenerOrganisation().getOrganisation().getOrganisationName());

        verify(assignCaseAccessService).grantCaseRoleToUser(CASE_ID_IN_LONG, INTERVENER_USER_ID,
            INTVR_SOLICITOR_2.getCcdCode(), SOME_ORG_ID);
        verify(assignCaseAccessService).removeCaseRoleToUser(CASE_ID_IN_LONG, INTERVENER_USER_ID,
            INTVR_SOLICITOR_2.getCcdCode(), SOME_ORG_ID);
        assertThat(errors).isEmpty();
    }

    @Test
    void givenContestedCase_whenUpdatingIntervener2AndChangedRepresentationNoToYes_thenHandle() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseData finremCaseData = finremCallbackRequest.getCaseDetails().getData();
        FinremCaseData finremCaseDataBefore = finremCallbackRequest.getCaseDetailsBefore().getData();
        OrganisationPolicy organisationPolicy = OrganisationPolicy.builder().organisation(
            Organisation.builder().organisationID(SOME_ORG_ID).organisationName(SOME_ORG_ID).build()
        ).build();
        IntervenerTwo wrapper = IntervenerTwo
            .builder().intervenerName("Two name").intervenerEmail("test@test.com")
            .intervenerDateAdded(LocalDate.of(2023, 1, 1))
            .intervenerRepresented(YesOrNo.NO)
            .intervenerOrganisation(organisationPolicy).build();
        finremCaseDataBefore.setIntervenerTwo(wrapper);

        IntervenerTwo current = IntervenerTwo
            .builder().intervenerName("Two name").intervenerEmail("test@test.com")
            .intervenerRepresented(YesOrNo.YES)
            .intervenerDateAdded(LocalDate.of(2023, 1, 1))
            .intervenerOrganisation(organisationPolicy)
            .intervenerSolName("SOL name")
            .intervenerSolEmail("test@test.com")
            .intervenerSolPhone("112333333")
            .intervenerSolicitorFirm(INTERVENER_SOL_FIRM)
            .intervenerSolicitorReference(INTERVENER_SOL_REFERENCE).build();
        finremCaseData.setIntervenerTwo(current);

        DynamicRadioListElement option = DynamicRadioListElement.builder().code(INTERVENER_TWO).build();
        List<DynamicRadioListElement> list = List.of(option);
        DynamicRadioList dynamicRadioList = DynamicRadioList.builder().listItems(list).build();
        finremCaseData.setIntervenersList(dynamicRadioList);
        DynamicRadioListElement option1 = DynamicRadioListElement.builder().code(INTERVENER_TWO).build();
        finremCaseData.getIntervenersList().setValue(option1);

        when(systemUserService.getSysUserToken()).thenReturn(AUTH_TOKEN);
        when(organisationService.findUserByEmail(INTERVENER_TEST_EMAIL, AUTH_TOKEN)).thenReturn(Optional.of(INTERVENER_USER_ID));
        List<String> errors = new ArrayList<>();
        service.updateIntervenerDetails(current, errors, finremCallbackRequest);

        IntervenerTwo intervenerTwo = finremCaseData.getIntervenerTwo();
        assertNotNull(intervenerTwo.getIntervenerSolEmail());
        assertNotNull(intervenerTwo.getIntervenerSolName());
        assertNotNull(intervenerTwo.getIntervenerSolicitorFirm());
        assertNotNull(intervenerTwo.getIntervenerSolicitorReference());
        assertNotNull(intervenerTwo.getIntervenerOrganisation().getOrganisation().getOrganisationID());
        assertNotNull(intervenerTwo.getIntervenerOrganisation().getOrganisation().getOrganisationName());
        verify(assignCaseAccessService).grantCaseRoleToUser(CASE_ID_IN_LONG, INTERVENER_USER_ID,
            INTVR_SOLICITOR_2.getCcdCode(), SOME_ORG_ID);
        assertThat(errors).isEmpty();
    }

    @Test
    void givenContestedCase_whenAddingIntervenerAndIntv3NotRepresent_thenSetIntervenerDateAddedAndDefaultOrg() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseData finremCaseData = finremCallbackRequest.getCaseDetails().getData();
        OrganisationPolicy organisationPolicy = OrganisationPolicy.builder().organisation(
            Organisation.builder().organisationID(null).organisationName(null).build()
        ).build();
        IntervenerThree wrapper = IntervenerThree
            .builder().intervenerName("Two name").intervenerEmail("test@test.com")
            .intervenerRepresented(YesOrNo.NO)
            .intervenerAddress(Address.builder().addressLine1("1 London Road").build())
            .intervenerOrganisation(organisationPolicy).build();
        finremCaseData.setIntervenerThree(wrapper);

        DynamicRadioListElement option = DynamicRadioListElement.builder().code(INTERVENER_THREE).build();
        List<DynamicRadioListElement> list = List.of(option);
        DynamicRadioList dynamicRadioList = DynamicRadioList.builder().listItems(list).build();
        finremCaseData.setIntervenersList(dynamicRadioList);
        DynamicRadioListElement option1 = DynamicRadioListElement.builder().code(INTERVENER_THREE).build();
        finremCaseData.getIntervenersList().setValue(option1);
        List<String> errors = new ArrayList<>();
        service.updateIntervenerDetails(wrapper, errors, finremCallbackRequest);

        assertNotNull(finremCaseData.getIntervenerThree().getIntervenerDateAdded());
        assertNotNull(finremCaseData.getIntervenerThree().getIntervenerOrganisation().getOrgPolicyCaseAssignedRole());
        assertThat(errors).isEmpty();
    }

    @Test
    void givenContestedCase_whenAddingIntervenerAndIntv3Represent_thenSetIntervenerDateAddedAndDefaultOrg() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseData finremCaseData = finremCallbackRequest.getCaseDetails().getData();
        OrganisationPolicy organisationPolicy = OrganisationPolicy.builder().organisation(
            Organisation.builder().organisationID(SOME_ORG_ID).organisationName(SOME_ORG_ID).build()
        ).build();
        IntervenerThree wrapper = IntervenerThree
            .builder().intervenerName("Two name").intervenerEmail("test@test.com")
            .intervenerSolEmail("test@test.com")
            .intervenerRepresented(YesOrNo.YES)
            .intervenerSolicitorFirm(INTERVENER_SOL_FIRM)
            .intervenerSolicitorReference(INTERVENER_SOL_REFERENCE)
            .intervenerOrganisation(organisationPolicy).build();
        finremCaseData.setIntervenerThree(wrapper);

        DynamicRadioListElement option = DynamicRadioListElement.builder().code(INTERVENER_THREE).build();
        List<DynamicRadioListElement> list = List.of(option);
        DynamicRadioList dynamicRadioList = DynamicRadioList.builder().listItems(list).build();
        finremCaseData.setIntervenersList(dynamicRadioList);
        DynamicRadioListElement option1 = DynamicRadioListElement.builder().code(INTERVENER_THREE).build();
        finremCaseData.getIntervenersList().setValue(option1);

        when(systemUserService.getSysUserToken()).thenReturn(AUTH_TOKEN);
        when(organisationService.findUserByEmail(INTERVENER_TEST_EMAIL, AUTH_TOKEN)).thenReturn(Optional.of(INTERVENER_USER_ID));

        List<String> errors = new ArrayList<>();
        service.updateIntervenerDetails(wrapper, errors, finremCallbackRequest);

        assertNotNull(finremCaseData.getIntervenerThree().getIntervenerDateAdded());
        verify(assignCaseAccessService).grantCaseRoleToUser(CASE_ID_IN_LONG, INTERVENER_USER_ID,
            INTVR_SOLICITOR_3.getCcdCode(), SOME_ORG_ID);
        assertThat(errors).isEmpty();
    }

    @Test
    void givenContestedCase_whenUpdatingIntervener3AndChangedRepresentationYesToNo_thenHandle() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseData finremCaseData = finremCallbackRequest.getCaseDetails().getData();
        FinremCaseData finremCaseDataBefore = finremCallbackRequest.getCaseDetailsBefore().getData();
        OrganisationPolicy organisationPolicy = OrganisationPolicy.builder().organisation(
            Organisation.builder().organisationID(SOME_ORG_ID).organisationName(SOME_ORG_ID).build()
        ).build();
        IntervenerThree wrapper = IntervenerThree
            .builder().intervenerName("Two name").intervenerEmail("test@test.com")
            .intervenerSolEmail("test@test.com")
            .intervenerSolName("three man")
            .intervenerSolPhone("999999999")
            .intervenerSolicitorFirm(INTERVENER_SOL_FIRM)
            .intervenerSolicitorReference(INTERVENER_SOL_REFERENCE)
            .intervenerDateAdded(LocalDate.of(2023, 1, 1))
            .intervenerRepresented(YesOrNo.YES)
            .intervenerOrganisation(organisationPolicy).build();
        finremCaseDataBefore.setIntervenerThree(wrapper);

        IntervenerThree current = IntervenerThree
            .builder().intervenerName("Two name").intervenerEmail("test@test.com")
            .intervenerDateAdded(LocalDate.of(2023, 1, 1))
            .intervenerAddress(Address.builder().addressLine1("1 London Road").build())
            .intervenerRepresented(YesOrNo.NO)
            .intervenerOrganisation(organisationPolicy).build();
        finremCaseData.setIntervenerThree(current);

        DynamicRadioListElement option = DynamicRadioListElement.builder().code(INTERVENER_THREE).build();
        List<DynamicRadioListElement> list = List.of(option);
        DynamicRadioList dynamicRadioList = DynamicRadioList.builder().listItems(list).build();
        finremCaseData.setIntervenersList(dynamicRadioList);
        DynamicRadioListElement option1 = DynamicRadioListElement.builder().code(INTERVENER_THREE).build();
        finremCaseData.getIntervenersList().setValue(option1);

        when(systemUserService.getSysUserToken()).thenReturn(AUTH_TOKEN);
        when(organisationService.findUserByEmail(INTERVENER_TEST_EMAIL, AUTH_TOKEN)).thenReturn(Optional.of(INTERVENER_USER_ID));

        List<String> errors = new ArrayList<>();
        service.updateIntervenerDetails(current, errors, finremCallbackRequest);

        IntervenerThree intervenerThree = finremCaseData.getIntervenerThree();
        assertNotNull(intervenerThree.getIntervenerDateAdded());
        assertNull(intervenerThree.getIntervenerSolEmail());
        assertNull(intervenerThree.getIntervenerSolName());
        assertNull(intervenerThree.getIntervenerSolPhone());
        assertNull(intervenerThree.getIntervenerSolicitorFirm());
        assertNull(intervenerThree.getIntervenerSolicitorReference());
        assertNull(intervenerThree.getIntervenerOrganisation().getOrganisation().getOrganisationID());
        assertNull(intervenerThree.getIntervenerOrganisation().getOrganisation().getOrganisationName());
        verify(assignCaseAccessService).removeCaseRoleToUser(CASE_ID_IN_LONG, INTERVENER_USER_ID,
            INTVR_SOLICITOR_3.getCcdCode(), SOME_ORG_ID);
        assertThat(errors).isEmpty();
    }

    @Test
    void givenContestedCase_whenUpdatingIntervener3AndChangedRepresentationSolEmailChanges_thenHandle() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseData finremCaseData = finremCallbackRequest.getCaseDetails().getData();
        FinremCaseData finremCaseDataBefore = finremCallbackRequest.getCaseDetailsBefore().getData();
        OrganisationPolicy organisationPolicy = OrganisationPolicy.builder().organisation(
            Organisation.builder().organisationID(SOME_ORG_ID).organisationName(SOME_ORG_ID).build()
        ).build();

        IntervenerThree wrapper = IntervenerThree
            .builder().intervenerName("Two name").intervenerEmail("test@test.com")
            .intervenerSolEmail(INTERVENER_TEST_EMAIL)
            .intervenerSolName("three man")
            .intervenerSolPhone("999999999")
            .intervenerSolicitorFirm(INTERVENER_SOL_FIRM)
            .intervenerSolicitorReference(INTERVENER_SOL_REFERENCE)
            .intervenerDateAdded(LocalDate.of(2023, 1, 1))
            .intervenerRepresented(YesOrNo.YES)
            .intervenerOrganisation(organisationPolicy).build();
        finremCaseDataBefore.setIntervenerThree(wrapper);

        IntervenerThree current = IntervenerThree
            .builder().intervenerName("Two name").intervenerEmail("test@test.com")
            .intervenerSolEmail(INTERVENER_TEST_EMAIL_CHANGE)
            .intervenerSolName("three man")
            .intervenerSolPhone("999999999")
            .intervenerSolicitorFirm(INTERVENER_SOL_FIRM)
            .intervenerSolicitorReference(INTERVENER_SOL_REFERENCE)
            .intervenerDateAdded(LocalDate.of(2023, 1, 1))
            .intervenerRepresented(YesOrNo.YES)
            .intervenerOrganisation(organisationPolicy).build();
        finremCaseData.setIntervenerThree(current);

        DynamicRadioListElement option = DynamicRadioListElement.builder().code(INTERVENER_THREE).build();
        List<DynamicRadioListElement> list = List.of(option);
        DynamicRadioList dynamicRadioList = DynamicRadioList.builder().listItems(list).build();
        finremCaseData.setIntervenersList(dynamicRadioList);
        DynamicRadioListElement option1 = DynamicRadioListElement.builder().code(INTERVENER_THREE).build();
        finremCaseData.getIntervenersList().setValue(option1);

        when(systemUserService.getSysUserToken()).thenReturn(AUTH_TOKEN);
        when(organisationService.findUserByEmail(INTERVENER_TEST_EMAIL, AUTH_TOKEN)).thenReturn(Optional.of(INTERVENER_USER_ID));
        when(organisationService.findUserByEmail(INTERVENER_TEST_EMAIL_CHANGE, AUTH_TOKEN)).thenReturn(Optional.of(INTERVENER_USER_ID));

        List<String> errors = new ArrayList<>();
        service.updateIntervenerDetails(current, errors, finremCallbackRequest);

        IntervenerThree intervenerThree = finremCaseData.getIntervenerThree();
        assertNotNull(intervenerThree.getIntervenerDateAdded());
        assertNotNull(intervenerThree.getIntervenerSolEmail());
        assertNotNull(intervenerThree.getIntervenerSolName());
        assertNotNull(intervenerThree.getIntervenerSolPhone());
        assertNotNull(intervenerThree.getIntervenerSolicitorFirm());
        assertNotNull(intervenerThree.getIntervenerSolicitorReference());
        assertNotNull(intervenerThree.getIntervenerOrganisation().getOrganisation().getOrganisationID());
        assertNotNull(intervenerThree.getIntervenerOrganisation().getOrganisation().getOrganisationName());

        verify(assignCaseAccessService).grantCaseRoleToUser(CASE_ID_IN_LONG, INTERVENER_USER_ID,
            INTVR_SOLICITOR_3.getCcdCode(), SOME_ORG_ID);
        verify(assignCaseAccessService).removeCaseRoleToUser(CASE_ID_IN_LONG, INTERVENER_USER_ID,
            INTVR_SOLICITOR_3.getCcdCode(), SOME_ORG_ID);
        assertThat(errors).isEmpty();
    }

    @Test
    void givenContestedCase_whenUpdatingIntervenerAndChangedRepresentationSolOrgChanges_thenHandle() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseData finremCaseData = finremCallbackRequest.getCaseDetails().getData();
        FinremCaseData finremCaseDataBefore = finremCallbackRequest.getCaseDetailsBefore().getData();
        OrganisationPolicy organisationPolicy = OrganisationPolicy.builder().organisation(
            Organisation.builder().organisationID(SOME_ORG_ID).organisationName(SOME_ORG_ID).build()
        ).build();
        OrganisationPolicy organisationPolicyChange = OrganisationPolicy.builder().organisation(
            Organisation.builder().organisationID(CHANGE_ORG_ID).organisationName(CHANGE_ORG_ID).build()
        ).build();
        IntervenerThree wrapper = IntervenerThree
            .builder().intervenerName("Two name").intervenerEmail("test@test.com")
            .intervenerSolEmail(INTERVENER_TEST_EMAIL)
            .intervenerSolName("three man")
            .intervenerSolPhone("999999999")
            .intervenerSolicitorFirm(INTERVENER_SOL_FIRM)
            .intervenerSolicitorReference(INTERVENER_SOL_REFERENCE)
            .intervenerDateAdded(LocalDate.of(2023, 1, 1))
            .intervenerRepresented(YesOrNo.YES)
            .intervenerOrganisation(organisationPolicy).build();
        finremCaseDataBefore.setIntervenerThree(wrapper);

        IntervenerThree current = IntervenerThree
            .builder().intervenerName("Two name").intervenerEmail("test@test.com")
            .intervenerSolEmail(INTERVENER_TEST_EMAIL)
            .intervenerSolName("three man")
            .intervenerSolPhone("999999999")
            .intervenerSolicitorFirm(INTERVENER_SOL_FIRM)
            .intervenerSolicitorReference(INTERVENER_SOL_REFERENCE)
            .intervenerDateAdded(LocalDate.of(2023, 1, 1))
            .intervenerRepresented(YesOrNo.YES)
            .intervenerOrganisation(organisationPolicyChange).build();
        finremCaseData.setIntervenerThree(current);

        DynamicRadioListElement option = DynamicRadioListElement.builder().code(INTERVENER_THREE).build();
        List<DynamicRadioListElement> list = List.of(option);
        DynamicRadioList dynamicRadioList = DynamicRadioList.builder().listItems(list).build();
        finremCaseData.setIntervenersList(dynamicRadioList);
        DynamicRadioListElement option1 = DynamicRadioListElement.builder().code(INTERVENER_THREE).build();
        finremCaseData.getIntervenersList().setValue(option1);

        when(systemUserService.getSysUserToken()).thenReturn(AUTH_TOKEN);
        when(organisationService.findUserByEmail(INTERVENER_TEST_EMAIL, AUTH_TOKEN)).thenReturn(Optional.of(INTERVENER_USER_ID));

        List<String> errors = new ArrayList<>();
        service.updateIntervenerDetails(current, errors, finremCallbackRequest);

        IntervenerThree intervenerThree = finremCaseData.getIntervenerThree();
        assertNotNull(intervenerThree.getIntervenerDateAdded());
        assertNotNull(intervenerThree.getIntervenerSolEmail());
        assertNotNull(intervenerThree.getIntervenerSolName());
        assertNotNull(intervenerThree.getIntervenerSolPhone());
        assertNotNull(intervenerThree.getIntervenerSolicitorFirm());
        assertNotNull(intervenerThree.getIntervenerSolicitorReference());
        assertNotNull(intervenerThree.getIntervenerOrganisation().getOrganisation().getOrganisationID());
        assertNotNull(intervenerThree.getIntervenerOrganisation().getOrganisation().getOrganisationName());

        verify(assignCaseAccessService).grantCaseRoleToUser(CASE_ID_IN_LONG, INTERVENER_USER_ID,
            INTVR_SOLICITOR_3.getCcdCode(), CHANGE_ORG_ID);
        verify(assignCaseAccessService).removeCaseRoleToUser(CASE_ID_IN_LONG, INTERVENER_USER_ID,
            INTVR_SOLICITOR_3.getCcdCode(), SOME_ORG_ID);
        assertThat(errors).isEmpty();
    }

    @Test
    void givenContestedCase_whenUpdatingIntervener3AndChangedRepresentationNoToYes_thenHandle() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseData finremCaseData = finremCallbackRequest.getCaseDetails().getData();
        FinremCaseData finremCaseDataBefore = finremCallbackRequest.getCaseDetailsBefore().getData();
        OrganisationPolicy organisationPolicy = OrganisationPolicy.builder().organisation(
            Organisation.builder().organisationID(SOME_ORG_ID).organisationName(SOME_ORG_ID).build()
        ).build();
        IntervenerThree wrapper = IntervenerThree
            .builder().intervenerName("Two name").intervenerEmail("test@test.com")
            .intervenerDateAdded(LocalDate.of(2023, 1, 1))
            .intervenerRepresented(YesOrNo.NO)
            .intervenerOrganisation(organisationPolicy).build();
        finremCaseDataBefore.setIntervenerThree(wrapper);

        IntervenerThree current = IntervenerThree
            .builder().intervenerName("Two name").intervenerEmail("test@test.com")
            .intervenerDateAdded(LocalDate.of(2023, 1, 1))
            .intervenerSolEmail("test@test.com")
            .intervenerSolName("three man")
            .intervenerSolPhone("999999999")
            .intervenerSolicitorFirm(INTERVENER_SOL_FIRM)
            .intervenerSolicitorReference(INTERVENER_SOL_REFERENCE)
            .intervenerRepresented(YesOrNo.YES)
            .intervenerOrganisation(organisationPolicy).build();
        finremCaseData.setIntervenerThree(current);

        DynamicRadioListElement option = DynamicRadioListElement.builder().code(INTERVENER_THREE).build();
        List<DynamicRadioListElement> list = List.of(option);
        DynamicRadioList dynamicRadioList = DynamicRadioList.builder().listItems(list).build();
        finremCaseData.setIntervenersList(dynamicRadioList);
        DynamicRadioListElement option1 = DynamicRadioListElement.builder().code(INTERVENER_THREE).build();
        finremCaseData.getIntervenersList().setValue(option1);

        when(systemUserService.getSysUserToken()).thenReturn(AUTH_TOKEN);
        when(organisationService.findUserByEmail(INTERVENER_TEST_EMAIL, AUTH_TOKEN)).thenReturn(Optional.of(INTERVENER_USER_ID));

        List<String> errors = new ArrayList<>();
        service.updateIntervenerDetails(current, errors, finremCallbackRequest);

        IntervenerThree intervenerThree = finremCaseData.getIntervenerThree();
        assertNotNull(intervenerThree.getIntervenerSolEmail());
        assertNotNull(intervenerThree.getIntervenerSolName());
        assertNotNull(intervenerThree.getIntervenerSolicitorFirm());
        assertNotNull(intervenerThree.getIntervenerSolicitorReference());
        assertNotNull(intervenerThree.getIntervenerOrganisation().getOrganisation().getOrganisationID());
        assertNotNull(intervenerThree.getIntervenerOrganisation().getOrganisation().getOrganisationName());
        verify(assignCaseAccessService).grantCaseRoleToUser(CASE_ID_IN_LONG, INTERVENER_USER_ID,
            INTVR_SOLICITOR_3.getCcdCode(), SOME_ORG_ID);
        assertThat(errors).isEmpty();
    }

    @Test
    void givenContestedCase_whenAddingIntervenerAndIntv4NotRepresent_thenSetIntervenerDateAddedAndDefaultOrg() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseData finremCaseData = finremCallbackRequest.getCaseDetails().getData();
        OrganisationPolicy organisationPolicy = OrganisationPolicy.builder().organisation(
            Organisation.builder().organisationID(null).organisationName(null).build()
        ).build();
        IntervenerFour wrapper = IntervenerFour
            .builder().intervenerName("Two name").intervenerEmail("test@test.com")
            .intervenerRepresented(YesOrNo.NO)
            .intervenerAddress(Address.builder().addressLine1("1 London Road").build())
            .intervenerOrganisation(organisationPolicy).build();
        finremCaseData.setIntervenerFour(wrapper);

        DynamicRadioListElement option = DynamicRadioListElement.builder().code(INTERVENER_FOUR).build();
        List<DynamicRadioListElement> list = List.of(option);
        DynamicRadioList dynamicRadioList = DynamicRadioList.builder().listItems(list).build();
        finremCaseData.setIntervenersList(dynamicRadioList);
        DynamicRadioListElement option1 = DynamicRadioListElement.builder().code(INTERVENER_FOUR).build();
        finremCaseData.getIntervenersList().setValue(option1);

        List<String> errors = new ArrayList<>();
        service.updateIntervenerDetails(wrapper, errors, finremCallbackRequest);

        assertNotNull(finremCaseData.getIntervenerFour().getIntervenerDateAdded());
        assertNotNull(finremCaseData.getIntervenerFour().getIntervenerOrganisation().getOrgPolicyCaseAssignedRole());
        assertThat(errors).isEmpty();
    }

    @Test
    void givenContestedCase_whenAddingIntervenerAndIntv4Represent_thenSetIntervenerDateAddedAndDefaultOrg() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseData finremCaseData = finremCallbackRequest.getCaseDetails().getData();
        OrganisationPolicy organisationPolicy = OrganisationPolicy.builder().organisation(
            Organisation.builder().organisationID(SOME_ORG_ID).organisationName(SOME_ORG_ID).build()
        ).build();
        IntervenerFour wrapper = IntervenerFour
            .builder().intervenerName("Two name").intervenerEmail("test@test.com")
            .intervenerRepresented(YesOrNo.YES)
            .intervenerSolEmail("test@test.com")
            .intervenerSolicitorFirm(INTERVENER_SOL_FIRM)
            .intervenerSolicitorReference(INTERVENER_SOL_REFERENCE)
            .intervenerOrganisation(organisationPolicy).build();
        finremCaseData.setIntervenerFour(wrapper);

        DynamicRadioListElement option = DynamicRadioListElement.builder().code(INTERVENER_FOUR).build();
        List<DynamicRadioListElement> list = List.of(option);
        DynamicRadioList dynamicRadioList = DynamicRadioList.builder().listItems(list).build();
        finremCaseData.setIntervenersList(dynamicRadioList);
        DynamicRadioListElement option1 = DynamicRadioListElement.builder().code(INTERVENER_FOUR).build();
        finremCaseData.getIntervenersList().setValue(option1);

        when(systemUserService.getSysUserToken()).thenReturn(AUTH_TOKEN);
        when(organisationService.findUserByEmail(INTERVENER_TEST_EMAIL, AUTH_TOKEN)).thenReturn(Optional.of(INTERVENER_USER_ID));

        List<String> errors = new ArrayList<>();
        service.updateIntervenerDetails(wrapper, errors, finremCallbackRequest);

        assertNotNull(finremCaseData.getIntervenerFour().getIntervenerDateAdded());
        verify(assignCaseAccessService).grantCaseRoleToUser(CASE_ID_IN_LONG, INTERVENER_USER_ID,
            INTVR_SOLICITOR_4.getCcdCode(), SOME_ORG_ID);
        assertThat(errors).isEmpty();
    }

    @Test
    void givenContestedCase_whenUpdatingIntervenerAndChangedRepresentationYesToNo_thenHandle() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseData finremCaseData = finremCallbackRequest.getCaseDetails().getData();
        FinremCaseData finremCaseDataBefore = finremCallbackRequest.getCaseDetailsBefore().getData();
        OrganisationPolicy organisationPolicy = OrganisationPolicy.builder().organisation(
            Organisation.builder().organisationID(SOME_ORG_ID).organisationName(SOME_ORG_ID).build()
        ).build();
        IntervenerFour wrapper = IntervenerFour
            .builder().intervenerName("Two name").intervenerEmail("test@test.com")
            .intervenerRepresented(YesOrNo.YES)
            .intervenerDateAdded(LocalDate.of(2023, 1, 1))
            .intervenerSolEmail("test@test.com")
            .intervenerSolName("tes test")
            .intervenerSolicitorFirm(INTERVENER_SOL_FIRM)
            .intervenerSolicitorReference(INTERVENER_SOL_REFERENCE)
            .intervenerOrganisation(organisationPolicy).build();
        finremCaseDataBefore.setIntervenerFour(wrapper);

        IntervenerFour current = IntervenerFour
            .builder().intervenerName("Two name").intervenerEmail("test@test.com")
            .intervenerRepresented(YesOrNo.NO)
            .intervenerDateAdded(LocalDate.of(2023, 1, 1))
            .intervenerAddress(Address.builder().addressLine1("1 London Road").build())
            .intervenerOrganisation(organisationPolicy).build();
        finremCaseData.setIntervenerFour(current);

        DynamicRadioListElement option = DynamicRadioListElement.builder().code(INTERVENER_FOUR).build();
        List<DynamicRadioListElement> list = List.of(option);
        DynamicRadioList dynamicRadioList = DynamicRadioList.builder().listItems(list).build();
        finremCaseData.setIntervenersList(dynamicRadioList);
        DynamicRadioListElement option1 = DynamicRadioListElement.builder().code(INTERVENER_FOUR).build();
        finremCaseData.getIntervenersList().setValue(option1);

        when(systemUserService.getSysUserToken()).thenReturn(AUTH_TOKEN);
        when(organisationService.findUserByEmail(INTERVENER_TEST_EMAIL, AUTH_TOKEN)).thenReturn(Optional.of(INTERVENER_USER_ID));

        List<String> errors = new ArrayList<>();
        service.updateIntervenerDetails(current, errors, finremCallbackRequest);

        IntervenerFour intervenerFour = finremCaseData.getIntervenerFour();
        assertNotNull(intervenerFour.getIntervenerDateAdded());
        assertNull(intervenerFour.getIntervenerSolEmail());
        assertNull(intervenerFour.getIntervenerSolName());
        assertNull(intervenerFour.getIntervenerSolPhone());
        assertNull(intervenerFour.getIntervenerSolicitorFirm());
        assertNull(intervenerFour.getIntervenerSolicitorReference());
        assertNull(intervenerFour.getIntervenerOrganisation().getOrganisation().getOrganisationID());
        assertNull(intervenerFour.getIntervenerOrganisation().getOrganisation().getOrganisationName());

        verify(assignCaseAccessService).removeCaseRoleToUser(CASE_ID_IN_LONG, INTERVENER_USER_ID,
            INTVR_SOLICITOR_4.getCcdCode(), SOME_ORG_ID);
        assertThat(errors).isEmpty();
    }

    @Test
    void givenContestedCase_whenUpdatingIntervenerAndChangedRepresentationNoToYes_thenHandle() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseData finremCaseData = finremCallbackRequest.getCaseDetails().getData();
        FinremCaseData finremCaseDataBefore = finremCallbackRequest.getCaseDetailsBefore().getData();
        OrganisationPolicy organisationPolicy = OrganisationPolicy.builder().organisation(
            Organisation.builder().organisationID(SOME_ORG_ID).organisationName(SOME_ORG_ID).build()
        ).build();
        IntervenerFour wrapper = IntervenerFour
            .builder().intervenerName("Two name").intervenerEmail("test@test.com")
            .intervenerRepresented(YesOrNo.NO)
            .intervenerDateAdded(LocalDate.of(2023, 1, 1))
            .intervenerAddress(Address.builder().addressLine1("1 London Road").build())
            .intervenerOrganisation(organisationPolicy).build();
        finremCaseDataBefore.setIntervenerFour(wrapper);

        IntervenerFour current = IntervenerFour
            .builder().intervenerName("Two name").intervenerEmail("test@test.com")
            .intervenerRepresented(YesOrNo.YES)
            .intervenerSolEmail("test@test.com")
            .intervenerSolName("tes test")
            .intervenerSolicitorFirm(INTERVENER_SOL_FIRM)
            .intervenerSolicitorReference(INTERVENER_SOL_REFERENCE)
            .intervenerDateAdded(LocalDate.of(2023, 1, 1))
            .intervenerOrganisation(organisationPolicy).build();
        finremCaseData.setIntervenerFour(current);

        DynamicRadioListElement option = DynamicRadioListElement.builder().code(INTERVENER_FOUR).build();
        List<DynamicRadioListElement> list = List.of(option);
        DynamicRadioList dynamicRadioList = DynamicRadioList.builder().listItems(list).build();
        finremCaseData.setIntervenersList(dynamicRadioList);
        DynamicRadioListElement option1 = DynamicRadioListElement.builder().code(INTERVENER_FOUR).build();
        finremCaseData.getIntervenersList().setValue(option1);

        when(systemUserService.getSysUserToken()).thenReturn(AUTH_TOKEN);
        when(organisationService.findUserByEmail(INTERVENER_TEST_EMAIL, AUTH_TOKEN)).thenReturn(Optional.of(INTERVENER_USER_ID));
        List<String> errors = new ArrayList<>();
        service.updateIntervenerDetails(current, errors, finremCallbackRequest);

        IntervenerFour intervenerFour = finremCaseData.getIntervenerFour();
        assertNotNull(intervenerFour.getIntervenerDateAdded());
        assertNotNull(intervenerFour.getIntervenerSolEmail());
        assertNotNull(intervenerFour.getIntervenerSolName());
        assertNotNull(intervenerFour.getIntervenerSolicitorFirm());
        assertNotNull(intervenerFour.getIntervenerSolicitorReference());
        assertNotNull(intervenerFour.getIntervenerOrganisation().getOrganisation().getOrganisationID());
        assertNotNull(intervenerFour.getIntervenerOrganisation().getOrganisation().getOrganisationName());
        verify(assignCaseAccessService).grantCaseRoleToUser(CASE_ID_IN_LONG, INTERVENER_USER_ID,
            INTVR_SOLICITOR_4.getCcdCode(), SOME_ORG_ID);
        assertThat(errors).isEmpty();
    }

    @Test
    void givenContestedCase_whenUpdatingIntervenerAndChangedRepresentationSolEmailChanges_thenHandle() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseData finremCaseData = finremCallbackRequest.getCaseDetails().getData();
        FinremCaseData finremCaseDataBefore = finremCallbackRequest.getCaseDetailsBefore().getData();
        OrganisationPolicy organisationPolicy = OrganisationPolicy.builder().organisation(
            Organisation.builder().organisationID(SOME_ORG_ID).organisationName(SOME_ORG_ID).build()
        ).build();

        IntervenerFour wrapper = IntervenerFour
            .builder().intervenerName("Two name").intervenerEmail("test@test.com")
            .intervenerRepresented(YesOrNo.YES)
            .intervenerDateAdded(LocalDate.of(2023, 1, 1))
            .intervenerSolName("tes test")
            .intervenerSolPhone("122222222222")
            .intervenerSolicitorFirm(INTERVENER_SOL_FIRM)
            .intervenerSolicitorReference(INTERVENER_SOL_REFERENCE)
            .intervenerSolEmail("test@test.com")
            .intervenerOrganisation(organisationPolicy).build();
        finremCaseDataBefore.setIntervenerFour(wrapper);

        IntervenerFour current = IntervenerFour
            .builder().intervenerName("Two name").intervenerEmail("test@test.com")
            .intervenerRepresented(YesOrNo.YES)
            .intervenerDateAdded(LocalDate.of(2023, 1, 1))
            .intervenerSolName("tes test")
            .intervenerSolPhone("122222222222")
            .intervenerSolicitorFirm(INTERVENER_SOL_FIRM)
            .intervenerSolicitorReference(INTERVENER_SOL_REFERENCE)
            .intervenerSolEmail(INTERVENER_TEST_EMAIL_CHANGE)
            .intervenerOrganisation(organisationPolicy).build();
        finremCaseData.setIntervenerFour(current);

        DynamicRadioListElement option = DynamicRadioListElement.builder().code(INTERVENER_FOUR).build();
        List<DynamicRadioListElement> list = List.of(option);
        DynamicRadioList dynamicRadioList = DynamicRadioList.builder().listItems(list).build();
        finremCaseData.setIntervenersList(dynamicRadioList);
        DynamicRadioListElement option1 = DynamicRadioListElement.builder().code(INTERVENER_FOUR).build();
        finremCaseData.getIntervenersList().setValue(option1);

        when(systemUserService.getSysUserToken()).thenReturn(AUTH_TOKEN);
        when(organisationService.findUserByEmail(INTERVENER_TEST_EMAIL, AUTH_TOKEN)).thenReturn(Optional.of(INTERVENER_USER_ID));
        when(organisationService.findUserByEmail(INTERVENER_TEST_EMAIL_CHANGE, AUTH_TOKEN)).thenReturn(Optional.of(INTERVENER_USER_ID));

        List<String> errors = new ArrayList<>();
        service.updateIntervenerDetails(current, errors, finremCallbackRequest);

        IntervenerFour intervenerFour = finremCaseData.getIntervenerFour();
        assertNotNull(intervenerFour.getIntervenerDateAdded());
        assertNotNull(intervenerFour.getIntervenerSolEmail());
        assertNotNull(intervenerFour.getIntervenerSolName());
        assertNotNull(intervenerFour.getIntervenerSolPhone());
        assertNotNull(intervenerFour.getIntervenerSolicitorFirm());
        assertNotNull(intervenerFour.getIntervenerSolicitorReference());
        assertNotNull(intervenerFour.getIntervenerOrganisation().getOrganisation().getOrganisationID());
        assertNotNull(intervenerFour.getIntervenerOrganisation().getOrganisation().getOrganisationName());

        verify(assignCaseAccessService).grantCaseRoleToUser(CASE_ID_IN_LONG, INTERVENER_USER_ID,
            INTVR_SOLICITOR_4.getCcdCode(), SOME_ORG_ID);
        verify(assignCaseAccessService).removeCaseRoleToUser(CASE_ID_IN_LONG, INTERVENER_USER_ID,
            INTVR_SOLICITOR_4.getCcdCode(), SOME_ORG_ID);
        assertThat(errors).isEmpty();
    }

    @Test
    void givenContestedCase_whenUpdatingIntervenerAndChangedRepresentationSolOrgChanged_thenHandle() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseData finremCaseData = finremCallbackRequest.getCaseDetails().getData();
        FinremCaseData finremCaseDataBefore = finremCallbackRequest.getCaseDetailsBefore().getData();
        OrganisationPolicy organisationPolicy = OrganisationPolicy.builder().organisation(
            Organisation.builder().organisationID(SOME_ORG_ID).organisationName(SOME_ORG_ID).build()
        ).build();
        OrganisationPolicy organisationPolicyChange = OrganisationPolicy.builder().organisation(
            Organisation.builder().organisationID(CHANGE_ORG_ID).organisationName(CHANGE_ORG_ID).build()
        ).build();

        IntervenerFour wrapper = IntervenerFour
            .builder().intervenerName("Two name").intervenerEmail("test@test.com")
            .intervenerRepresented(YesOrNo.YES)
            .intervenerDateAdded(LocalDate.of(2023, 1, 1))
            .intervenerSolName("tes test")
            .intervenerSolPhone("122222222222")
            .intervenerSolicitorFirm(INTERVENER_SOL_FIRM)
            .intervenerSolicitorReference(INTERVENER_SOL_REFERENCE)
            .intervenerSolEmail(INTERVENER_TEST_EMAIL)
            .intervenerOrganisation(organisationPolicy).build();
        finremCaseDataBefore.setIntervenerFour(wrapper);

        IntervenerFour current = IntervenerFour
            .builder().intervenerName("Two name").intervenerEmail("test@test.com")
            .intervenerRepresented(YesOrNo.YES)
            .intervenerDateAdded(LocalDate.of(2023, 1, 1))
            .intervenerSolName("tes test")
            .intervenerSolPhone("122222222222")
            .intervenerSolicitorFirm(INTERVENER_SOL_FIRM)
            .intervenerSolicitorReference(INTERVENER_SOL_REFERENCE)
            .intervenerSolEmail(INTERVENER_TEST_EMAIL)
            .intervenerOrganisation(organisationPolicyChange).build();
        finremCaseData.setIntervenerFour(current);

        DynamicRadioListElement option = DynamicRadioListElement.builder().code(INTERVENER_FOUR).build();
        List<DynamicRadioListElement> list = List.of(option);
        DynamicRadioList dynamicRadioList = DynamicRadioList.builder().listItems(list).build();
        finremCaseData.setIntervenersList(dynamicRadioList);
        DynamicRadioListElement option1 = DynamicRadioListElement.builder().code(INTERVENER_FOUR).build();
        finremCaseData.getIntervenersList().setValue(option1);

        when(systemUserService.getSysUserToken()).thenReturn(AUTH_TOKEN);
        when(organisationService.findUserByEmail(INTERVENER_TEST_EMAIL, AUTH_TOKEN)).thenReturn(Optional.of(INTERVENER_USER_ID));

        List<String> errors = new ArrayList<>();
        service.updateIntervenerDetails(current, errors, finremCallbackRequest);

        IntervenerFour intervenerFour = finremCaseData.getIntervenerFour();
        assertNotNull(intervenerFour.getIntervenerDateAdded());
        assertNotNull(intervenerFour.getIntervenerSolEmail());
        assertNotNull(intervenerFour.getIntervenerSolName());
        assertNotNull(intervenerFour.getIntervenerSolPhone());
        assertNotNull(intervenerFour.getIntervenerSolicitorFirm());
        assertNotNull(intervenerFour.getIntervenerSolicitorReference());
        assertNotNull(intervenerFour.getIntervenerOrganisation().getOrganisation().getOrganisationID());
        assertNotNull(intervenerFour.getIntervenerOrganisation().getOrganisation().getOrganisationName());

        verify(assignCaseAccessService).grantCaseRoleToUser(CASE_ID_IN_LONG, INTERVENER_USER_ID,
            INTVR_SOLICITOR_4.getCcdCode(), CHANGE_ORG_ID);
        verify(assignCaseAccessService).removeCaseRoleToUser(CASE_ID_IN_LONG, INTERVENER_USER_ID,
            INTVR_SOLICITOR_4.getCcdCode(), SOME_ORG_ID);
        assertThat(errors).isEmpty();
    }

    @Test
    void givenContestedCase_whenAddingIntervenerWithNonRegisterEmail_thenHandlerThrowException() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseData finremCaseData = finremCallbackRequest.getCaseDetails().getData();
        OrganisationPolicy organisationPolicy = OrganisationPolicy.builder().organisation(
            Organisation.builder().organisationID(SOME_ORG_ID).organisationName(SOME_ORG_ID).build()
        ).build();
        IntervenerFour wrapper = IntervenerFour
            .builder().intervenerName("Two name").intervenerEmail(INTERVENER_TEST_EMAIL)
            .intervenerRepresented(YesOrNo.YES)
            .intervenerSolEmail(INTERVENER_TEST_EMAIL)
            .intervenerOrganisation(organisationPolicy).build();
        finremCaseData.setIntervenerFour(wrapper);

        DynamicRadioListElement option = DynamicRadioListElement.builder().code(INTERVENER_FOUR).build();
        List<DynamicRadioListElement> list = List.of(option);
        DynamicRadioList dynamicRadioList = DynamicRadioList.builder().listItems(list).build();
        finremCaseData.setIntervenersList(dynamicRadioList);
        DynamicRadioListElement option1 = DynamicRadioListElement.builder().code(INTERVENER_FOUR).build();
        finremCaseData.getIntervenersList().setValue(option1);

        when(systemUserService.getSysUserToken()).thenReturn(AUTH_TOKEN);
        List<String> errors = new ArrayList<>();
        service.updateIntervenerDetails(wrapper, errors,  finremCallbackRequest);
        assertThat(errors).contains("Could not find intervener with provided email");
    }

    @Test
    void whenCalled_setIntervenerChangeDetailsForIntervenerOneOnAdding() {
        IntervenerOne intervenerOne = IntervenerOne.builder()
            .intervenerName("Intervener One")
            .build();

        IntervenerChangeDetails result = service.setIntervenerAddedChangeDetails(intervenerOne);
        assertEquals(IntervenerAction.ADDED.toString(), result.getIntervenerAction().toString());
        assertEquals(IntervenerType.INTERVENER_ONE.toString(), result.getIntervenerType().toString());
    }

    @Test
    void whenCalled_setIntervenerChangeDetailsForIntervenerTwoOnAdding() {
        IntervenerTwo intervenerTwo = IntervenerTwo.builder()
            .intervenerName("Intervener Two")
            .build();

        IntervenerChangeDetails result = service.setIntervenerAddedChangeDetails(intervenerTwo);
        assertEquals(IntervenerAction.ADDED.toString(), result.getIntervenerAction().toString());
        assertEquals(IntervenerType.INTERVENER_TWO.toString(), result.getIntervenerType().toString());
    }

    @Test
    void whenCalled_setIntervenerChangeDetailsForIntervenerThreeOnAdding() {
        IntervenerThree intervenerThree = IntervenerThree.builder()
            .intervenerName("Intervener Three")
            .build();

        IntervenerChangeDetails result = service.setIntervenerAddedChangeDetails(intervenerThree);
        assertEquals(IntervenerAction.ADDED.toString(), result.getIntervenerAction().toString());
        assertEquals(IntervenerType.INTERVENER_THREE.toString(), result.getIntervenerType().toString());
    }

    @Test
    void whenCalled_setIntervenerChangeDetailsForIntervenerFourOnAdding() {
        IntervenerFour intervenerFour = IntervenerFour.builder()
            .intervenerName("Intervener Four")
            .build();

        IntervenerChangeDetails result = service.setIntervenerAddedChangeDetails(intervenerFour);
        assertEquals(IntervenerAction.ADDED.toString(), result.getIntervenerAction().toString());
        assertEquals(IntervenerType.INTERVENER_FOUR.toString(), result.getIntervenerType().toString());
    }

    @Test
    void whenCalled_setIntervenerChangeDetailsForIntervenerOnRemoval() {
        IntervenerWrapper intervenerWrapper = IntervenerOne.builder()
            .intervenerName("Intervener One")
            .build();

        IntervenerChangeDetails result = service.setIntervenerRemovedChangeDetails(intervenerWrapper);
        assertEquals(IntervenerAction.REMOVED.toString(), result.getIntervenerAction().toString());
        assertEquals(IntervenerType.INTERVENER_ONE.toString(), result.getIntervenerType().toString());
    }

    @Test
    void whenIntervenerOneSolicitorRemoved_ShouldReturnTrue() {
        IntervenerOne intervenerOneBefore = new IntervenerOne();
        intervenerOneBefore.setIntervenerRepresented(YesOrNo.YES);
        IntervenerOne intervenerOne = new IntervenerOne();
        intervenerOne.setIntervenerRepresented(YesOrNo.NO);
        FinremCaseData finremCaseData = FinremCaseData.builder()
            .intervenerOne(intervenerOne)
            .build();
        FinremCaseData finremCaseDataBefore = FinremCaseData.builder()
            .intervenerOne(intervenerOneBefore)
            .build();
        assertTrue(service.checkIfAnyIntervenerSolicitorRemoved(finremCaseData, finremCaseDataBefore));
    }

    @Test
    void whenIntervenerTwoSolicitorRemoved_ShouldReturnTrue() {
        IntervenerTwo intervenerTwoBefore = new IntervenerTwo();
        intervenerTwoBefore.setIntervenerRepresented(YesOrNo.YES);
        IntervenerTwo intervenerTwo = new IntervenerTwo();
        intervenerTwo.setIntervenerRepresented(YesOrNo.NO);
        FinremCaseData finremCaseData = FinremCaseData.builder()
            .intervenerTwo(intervenerTwo)
            .build();
        FinremCaseData finremCaseDataBefore = FinremCaseData.builder()
            .intervenerTwo(intervenerTwoBefore)
            .build();
        assertTrue(service.checkIfAnyIntervenerSolicitorRemoved(finremCaseData, finremCaseDataBefore));
    }

    @Test
    void whenIntervenerThreeSolicitorRemoved_ShouldReturnTrue() {
        IntervenerThree intervenerThreeBefore = new IntervenerThree();
        intervenerThreeBefore.setIntervenerRepresented(YesOrNo.YES);
        IntervenerThree intervenerThree = new IntervenerThree();
        intervenerThree.setIntervenerRepresented(YesOrNo.NO);
        FinremCaseData finremCaseData = FinremCaseData.builder()
            .intervenerThree(intervenerThree)
            .build();
        FinremCaseData finremCaseDataBefore = FinremCaseData.builder()
            .intervenerThree(intervenerThreeBefore)
            .build();
        assertTrue(service.checkIfAnyIntervenerSolicitorRemoved(finremCaseData, finremCaseDataBefore));
    }

    @Test
    void whenIntervenerFourSolicitorRemoved_ShouldReturnTrue() {
        IntervenerFour intervenerFourBefore = new IntervenerFour();
        intervenerFourBefore.setIntervenerRepresented(YesOrNo.YES);
        IntervenerFour intervenerFour = new IntervenerFour();
        intervenerFour.setIntervenerRepresented(YesOrNo.NO);
        FinremCaseData finremCaseData = FinremCaseData.builder()
            .intervenerFour(intervenerFour)
            .build();
        FinremCaseData finremCaseDataBefore = FinremCaseData.builder()
            .intervenerFour(intervenerFourBefore)
            .build();
        assertTrue(service.checkIfAnyIntervenerSolicitorRemoved(finremCaseData, finremCaseDataBefore));
    }

    @Test
    void whenNoIntervenerSolicitorRemoved_ShouldReturnFalse() {
        FinremCaseData finremCaseData = FinremCaseData.builder().build();
        FinremCaseData finremCaseDataBefore = FinremCaseData.builder().build();
        assertFalse(service.checkIfAnyIntervenerSolicitorRemoved(finremCaseData, finremCaseDataBefore));
    }

    @Test
    void shouldUpdateIntervenerSolicitorStopRepresentingHistory() {
        RepresentationUpdate shouldBeRetained = mock(RepresentationUpdate.class);

        FinremCaseData finremCaseData = FinremCaseData.builder()
            .intervenerOne(IntervenerOne.builder()
                .intervenerRepresented(YesOrNo.NO)
                .intervenerOrganisation(OrganisationPolicy.builder()
                .orgPolicyCaseAssignedRole("[INTVRSOLICITOR1]")
                .build()).build())
            .representationUpdateHistory(new ArrayList<>(
                List.of(RepresentationUpdateHistoryCollection.builder().value(shouldBeRetained).build())
            ))
            .build();
        FinremCaseData originalFinremCaseData = finremCaseData.toBuilder()
            .intervenerOne(IntervenerOne.builder()
                .intervenerRepresented(YesOrNo.YES)
                .intervenerSolName("AAA DDD")
                .intervenerSolEmail("aaa.ddd@gmail.com")
                .intervenerOrganisation(OrganisationPolicy.builder()
                    .organisation(organisation("AAA"))
                    .orgPolicyCaseAssignedRole("[INTVRSOLICITOR1]")
                    .build())
                .build())
            .build();

        when(idamService.getIdamFullName(AUTH_TOKEN)).thenReturn("Jack Neil");

        ArgumentCaptor<ChangeOfRepresentationRequest> captor = ArgumentCaptor.forClass(ChangeOfRepresentationRequest.class);

        RepresentationUpdate newUpdate = mock(RepresentationUpdate.class);
        RepresentationUpdateHistory history = RepresentationUpdateHistory.builder()
            .representationUpdateHistory(List.of(Element.<RepresentationUpdate>builder()
                    .value(newUpdate)
                .build()))
            .build();
        when(changeOfRepresentationService.generateRepresentationUpdateHistory(any(ChangeOfRepresentationRequest.class),
            eq(STOP_REPRESENTING_CLIENT))).thenReturn(history);

        service.updateIntervenerSolicitorStopRepresentingHistory(finremCaseData, originalFinremCaseData, AUTH_TOKEN);

        verify(changeOfRepresentationService).generateRepresentationUpdateHistory(captor.capture(), eq(STOP_REPRESENTING_CLIENT));
        verify(idamService).getIdamFullName(AUTH_TOKEN);

        assertThat(finremCaseData.getRepresentationUpdateHistory())
            .extracting(RepresentationUpdateHistoryCollection::getValue)
            .containsExactly(shouldBeRetained, newUpdate);
        assertThat(captor.getValue()).extracting(ChangeOfRepresentationRequest::getBy).isEqualTo("Jack Neil");
        assertThat(captor.getValue()).extracting(ChangeOfRepresentationRequest::getRemovedRepresentative).isEqualTo(
            ChangedRepresentative.builder()
                .name("AAA DDD")
                .email("aaa.ddd@gmail.com")
                .organisation(organisation("AAA"))
                .build());
    }

    @Test
    void givenCase_whenRemoveIntervenerWithoutStoredUserIdAndUserNotFound_thenLogError() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseData finremCaseData = finremCallbackRequest.getCaseDetails().getData();

        OrganisationPolicy organisationPolicy = OrganisationPolicy.builder().organisation(
            Organisation.builder().organisationID(SOME_ORG_ID).organisationName(SOME_ORG_ID).build()
        ).build();
        IntervenerOne oneWrapper = IntervenerOne
            .builder()
            .intervenerName("One name")
            .intervenerEmail("test@test.com")
            .intervenerSolEmail(INTERVENER_TEST_EMAIL)
            .solUserId(null)
            .intervenerSolicitorFirm(INTERVENER_SOL_FIRM)
            .intervenerSolicitorReference(INTERVENER_SOL_REFERENCE)
            .intervenerOrganisation(organisationPolicy)
            .intervenerRepresented(YesOrNo.YES)
            .build();
        finremCaseData.setIntervenerOne(oneWrapper);

        when(systemUserService.getSysUserToken()).thenReturn(AUTH_TOKEN);
        when(organisationService.findUserByEmail(INTERVENER_TEST_EMAIL, AUTH_TOKEN))
            .thenReturn(Optional.empty());
        List<String> errors = new ArrayList<>();
        service.removeIntervenerDetails(oneWrapper, errors, finremCaseData, CASE_ID_IN_LONG);

        verify(systemUserService).getSysUserToken();
        verify(organisationService).findUserByEmail(INTERVENER_TEST_EMAIL, AUTH_TOKEN);
        verify(assignCaseAccessService, never()).removeCaseRoleToUser(any(), any(), any(), any());
        assertThat(errors).contains("Could not find intervener with provided email");
        assertNull(finremCaseData.getIntervenerOne().getIntervenerName());
    }

    @Test
    void givenValidOrgIdAndEmail_whenRevokeIntervener_thenCaseRoleIsRevoked() {
        OrganisationPolicy organisationPolicy = OrganisationPolicy.builder()
            .organisation(Organisation.builder()
                .organisationID(SOME_ORG_ID)
                .organisationName(SOME_ORG_ID)
                .build())
            .build();

        IntervenerOne intervenerWrapper = IntervenerOne.builder()
            .intervenerOrganisation(organisationPolicy)
            .intervenerSolEmail(INTERVENER_TEST_EMAIL)
            .solUserId(INTERVENER_USER_ID)
            .intervenerRepresented(YesOrNo.YES)
            .build();
        service.revokeIntervener(CASE_ID_IN_LONG, intervenerWrapper);
        verify(assignCaseAccessService).removeCaseRoleToUser(
            CASE_ID_IN_LONG,
            INTERVENER_USER_ID,
            INTVR_SOLICITOR_1.getCcdCode(),
            SOME_ORG_ID
        );
      
    @Nested
    class RevokeIntervenerSolicitorTests {

        @ParameterizedTest
        @ValueSource(strings = {"", " "})
        @NullSource
        void givenMissingIntervenerSolEmail_whenCalled_thenDoNothing(String email) {
            IntervenerOne intervenerOne = IntervenerOne.builder().intervenerSolEmail(email)
                .intervenerOrganisation(OrganisationPolicy.builder().organisation(organisation(TEST_ORG_ID)).build())
                .build();

            service.revokeIntervenerSolicitor(CASE_ID_IN_LONG, intervenerOne);
            verifyNoInteractions(assignCaseAccessService);
            verifyNoInteractions(organisationService);
            verifyNoInteractions(systemUserService);
        }

        @Test
        void givenMissingOrganisationPolicy_whenCalled_thenDoNothing() {
            IntervenerOne intervenerOne = IntervenerOne.builder().intervenerSolEmail(TEST_SOLICITOR_EMAIL)
                .intervenerOrganisation(null)
                .build();

            service.revokeIntervenerSolicitor(CASE_ID_IN_LONG, intervenerOne);
            verifyNoInteractions(assignCaseAccessService);
            verifyNoInteractions(organisationService);
            verifyNoInteractions(systemUserService);
        }

        @ParameterizedTest
        @ValueSource(strings = {"", " "})
        @NullSource
        void givenMissingOrgId_whenCalled_thenDoNothing(String orgId) {
            IntervenerOne intervenerOne = IntervenerOne.builder().intervenerSolEmail(TEST_SOLICITOR_EMAIL)
                .intervenerOrganisation(OrganisationPolicy.builder().organisation(organisation(orgId)).build())
                .build();

            service.revokeIntervenerSolicitor(CASE_ID_IN_LONG, intervenerOne);
            verifyNoInteractions(assignCaseAccessService);
            verifyNoInteractions(organisationService);
            verifyNoInteractions(systemUserService);
        }

        @Test
        void givenMissingOrganisation_whenCalled_thenDoNothing() {
            IntervenerOne intervenerOne = IntervenerOne.builder().intervenerSolEmail(TEST_SOLICITOR_EMAIL)
                .intervenerOrganisation(OrganisationPolicy.builder().organisation(null).build())
                .build();

            service.revokeIntervenerSolicitor(CASE_ID_IN_LONG, intervenerOne);
            verifyNoInteractions(assignCaseAccessService);
            verifyNoInteractions(organisationService);
            verifyNoInteractions(systemUserService);
        }

        @Test
        void givenUserNotFound_whenCalled_thenDoThing() {
            when(systemUserService.getSysUserToken()).thenReturn(TEST_SYSTEM_TOKEN);
            when(organisationService.findUserByEmail(TEST_SOLICITOR_EMAIL, TEST_SYSTEM_TOKEN))
                .thenReturn(Optional.empty());
            IntervenerTwo intervenerTwo = IntervenerTwo.builder().intervenerSolEmail(TEST_SOLICITOR_EMAIL)
                .intervenerOrganisation(OrganisationPolicy.builder().organisation(organisation(TEST_ORG_ID)).build())
                .build();

            service.revokeIntervenerSolicitor(CASE_ID_IN_LONG, intervenerTwo);
            verify(systemUserService).getSysUserToken();
            verify(organisationService).findUserByEmail(TEST_SOLICITOR_EMAIL, TEST_SYSTEM_TOKEN);
            verifyNoInteractions(assignCaseAccessService);
        }

        @Test
        void givenValidIntervenerWrapper_whenCalled_thenRevokeIntervener() {
            when(systemUserService.getSysUserToken()).thenReturn(TEST_SYSTEM_TOKEN);
            when(organisationService.findUserByEmail(TEST_SOLICITOR_EMAIL, TEST_SYSTEM_TOKEN))
                .thenReturn(Optional.of(TEST_USER_ID));
            IntervenerTwo intervenerTwo = IntervenerTwo.builder().intervenerSolEmail(TEST_SOLICITOR_EMAIL)
                .intervenerOrganisation(OrganisationPolicy.builder().organisation(organisation(TEST_ORG_ID)).build())
                .build();

            service.revokeIntervenerSolicitor(CASE_ID_IN_LONG, intervenerTwo);
            verify(systemUserService).getSysUserToken();
            verify(organisationService).findUserByEmail(TEST_SOLICITOR_EMAIL, TEST_SYSTEM_TOKEN);
            verify(assignCaseAccessService).removeCaseRoleToUser(CASE_ID_IN_LONG, TEST_USER_ID,
                INTVR_SOLICITOR_2.getCcdCode(), TEST_ORG_ID);
        }
    }

    private FinremCallbackRequest buildCallbackRequest() {
        return FinremCallbackRequest
            .builder()
            .eventType(EventType.MANAGE_INTERVENERS)
            .caseDetailsBefore(FinremCaseDetails.builder().id(CASE_ID_IN_LONG).caseType(CONTESTED)
                .data(new FinremCaseData()).build())
            .caseDetails(FinremCaseDetails.builder().id(CASE_ID_IN_LONG).caseType(CONTESTED)
                .data(new FinremCaseData()).build())
            .build();
    }
}
