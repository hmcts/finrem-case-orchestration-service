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
import static org.mockito.Mockito.doThrow;
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
    private IntervenerService intervenerService;
    @Mock
    private ChangeOfRepresentationService changeOfRepresentationService;
    @Mock
    private AssignPartiesAccessService assignPartiesAccessService;

    @Test
    void givenCase_whenRemoveOperationChosenForIntv1NotRepresented_thenRemoveIntervener() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();

        FinremCaseData finremCaseData = finremCallbackRequest.getCaseDetails().getData();
        IntervenerOne oneWrapper = IntervenerOne
            .builder().intervenerName("One name").intervenerEmail("test@test.com").intervenerRepresented(YesOrNo.NO).build();
        finremCaseData.setIntervenerOne(oneWrapper);
        List<String> errors = new ArrayList<>();
        intervenerService.removeIntervenerDetails(oneWrapper, errors, finremCaseData, CASE_ID_IN_LONG);

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
        intervenerService.removeIntervenerDetails(oneWrapper, errors, finremCaseData, CASE_ID_IN_LONG);

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
        intervenerService.removeIntervenerDetails(twoWrapper, errors, finremCaseData, CASE_ID_IN_LONG);

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
        intervenerService.removeIntervenerDetails(twoWrapper, errors, finremCaseData, CASE_ID_IN_LONG);

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
        intervenerService.removeIntervenerDetails(threeWrapper, errors, finremCaseData, CASE_ID_IN_LONG);

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
        intervenerService.removeIntervenerDetails(threeWrapper, errors, finremCaseData, CASE_ID_IN_LONG);

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
        intervenerService.removeIntervenerDetails(fourWrapper, errors, finremCaseData, CASE_ID_IN_LONG);

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
        intervenerService.removeIntervenerDetails(fourWrapper, errors, finremCaseData, CASE_ID_IN_LONG);

        assertNull(finremCaseData.getIntervenerFour().getIntervenerName());
        assertNull(finremCaseData.getIntervenerFour().getIntervenerEmail());
        assertNull(finremCaseData.getIntervenerFour().getIntervenerSolicitorFirm());
        assertNull(finremCaseData.getIntervenerFour().getIntervenerSolicitorReference());
        verify(assignCaseAccessService).removeCaseRoleToUser(CASE_ID_IN_LONG, INTERVENER_USER_ID,
            INTVR_SOLICITOR_4.getCcdCode(), SOME_ORG_ID);
        assertThat(errors).isEmpty();
    }

    @Test
    void givenContestedCase_whenAddingIntervenerAndIntv1Represent_thenSetIntervenerDateAddedAndDefaultOrg()
        throws UserNotFoundInOrganisationApiException {
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

        List<String> errors = new ArrayList<>();
        intervenerService.updateIntervenerDetails(oneWrapper, errors, finremCallbackRequest);

        assertNotNull(finremCaseData.getIntervenerOne().getIntervenerDateAdded());
        verify(assignPartiesAccessService).grantIntervenerSolicitor(CASE_ID_IN_LONG, oneWrapper);
        assertThat(errors).isEmpty();
    }

    @Test
    void givenContestedCase_whenAddingIntervenerAndIntv1RepresentWithNullOrgId_thenSetIntervenerDateAddedAndDefaultOrg()
        throws UserNotFoundInOrganisationApiException {
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

        List<String> errors = new ArrayList<>();
        intervenerService.updateIntervenerDetails(oneWrapper, errors, finremCallbackRequest);

        assertNotNull(finremCaseData.getIntervenerOne().getIntervenerDateAdded());
        verify(assignPartiesAccessService).grantIntervenerSolicitor(CASE_ID_IN_LONG, oneWrapper);
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
        intervenerService.updateIntervenerDetails(oneWrapper1, errors, finremCallbackRequest);

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
        intervenerService.updateIntervenerDetails(oneWrapper1, errors, finremCallbackRequest);

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
    void givenContestedCase_whenUpdatingIntervener1AndChangedRepresentationSolEmailChanged_thenHandle()
        throws UserNotFoundInOrganisationApiException {
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
        List<String> errors = new ArrayList<>();
        intervenerService.updateIntervenerDetails(oneWrapper1, errors, finremCallbackRequest);

        IntervenerOne intervenerOne = finremCaseData.getIntervenerOne();
        assertNotNull(intervenerOne.getIntervenerDateAdded());
        assertNotNull(intervenerOne.getIntervenerSolEmail());
        assertNotNull(intervenerOne.getIntervenerSolName());
        assertNotNull(intervenerOne.getIntervenerSolPhone());
        assertNotNull(intervenerOne.getIntervenerSolicitorFirm());
        assertNotNull(intervenerOne.getIntervenerSolicitorReference());
        assertNotNull(intervenerOne.getIntervenerOrganisation().getOrganisation().getOrganisationID());
        assertNotNull(intervenerOne.getIntervenerOrganisation().getOrganisation().getOrganisationName());

        verify(assignPartiesAccessService).grantIntervenerSolicitor(CASE_ID_IN_LONG, finremCaseData.getIntervenerOne());
        verify(assignCaseAccessService).removeCaseRoleToUser(CASE_ID_IN_LONG, INTERVENER_USER_ID,
            INTVR_SOLICITOR_1.getCcdCode(), SOME_ORG_ID);
        assertThat(errors).isEmpty();
    }

    @Test
    void givenContestedCase_whenUpdatingIntervener1AndChangedRepresentationSolOrgChanged_thenHandle() throws UserNotFoundInOrganisationApiException {
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
        intervenerService.updateIntervenerDetails(oneWrapper1, errors, finremCallbackRequest);

        IntervenerOne intervenerOne = finremCaseData.getIntervenerOne();
        assertNotNull(intervenerOne.getIntervenerDateAdded());
        assertNotNull(intervenerOne.getIntervenerSolEmail());
        assertNotNull(intervenerOne.getIntervenerSolName());
        assertNotNull(intervenerOne.getIntervenerSolPhone());
        assertNotNull(intervenerOne.getIntervenerSolicitorFirm());
        assertNotNull(intervenerOne.getIntervenerSolicitorReference());
        assertNotNull(intervenerOne.getIntervenerOrganisation().getOrganisation().getOrganisationID());
        assertNotNull(intervenerOne.getIntervenerOrganisation().getOrganisation().getOrganisationName());

        verify(assignPartiesAccessService).grantIntervenerSolicitor(CASE_ID_IN_LONG, intervenerOne);
        verify(assignCaseAccessService).removeCaseRoleToUser(CASE_ID_IN_LONG, INTERVENER_USER_ID,
            INTVR_SOLICITOR_1.getCcdCode(), SOME_ORG_ID);
        assertThat(errors).isEmpty();
    }

    @Test
    void givenContestedCase_whenUpdatingIntervener1AndChangedRepresentationNoToYes_thenHandle()
        throws UserNotFoundInOrganisationApiException {
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

        List<String> errors = new ArrayList<>();
        intervenerService.updateIntervenerDetails(oneWrapper1, errors, finremCallbackRequest);

        IntervenerOne intervenerOne = finremCaseData.getIntervenerOne();
        assertNotNull(intervenerOne.getIntervenerSolEmail());
        assertNotNull(intervenerOne.getIntervenerSolName());
        assertNotNull(intervenerOne.getIntervenerSolicitorFirm());
        assertNotNull(intervenerOne.getIntervenerSolicitorReference());
        assertNotNull(intervenerOne.getIntervenerOrganisation().getOrganisation().getOrganisationID());
        assertNotNull(intervenerOne.getIntervenerOrganisation().getOrganisation().getOrganisationName());
        verify(assignPartiesAccessService).grantIntervenerSolicitor(CASE_ID_IN_LONG, intervenerOne);
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
        intervenerService.updateIntervenerDetails(oneWrapper, errors, finremCallbackRequest);

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
        intervenerService.updateIntervenerDetails(wrapper, errors, finremCallbackRequest);

        assertNotNull(finremCaseData.getIntervenerTwo().getIntervenerDateAdded());
        assertNotNull(finremCaseData.getIntervenerTwo().getIntervenerOrganisation().getOrgPolicyCaseAssignedRole());
        assertThat(errors).isEmpty();
    }

    @Test
    void givenContestedCase_whenAddingIntervenerAndIntv2Represent_thenSetIntervenerDateAddedAndDefaultOrg()
        throws UserNotFoundInOrganisationApiException {
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

        List<String> errors = new ArrayList<>();
        intervenerService.updateIntervenerDetails(wrapper, errors, finremCallbackRequest);

        assertNotNull(finremCaseData.getIntervenerTwo().getIntervenerDateAdded());
        verify(assignPartiesAccessService).grantIntervenerSolicitor(CASE_ID_IN_LONG, wrapper);
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
        intervenerService.updateIntervenerDetails(current, errors, finremCallbackRequest);

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
    void givenContestedCase_whenUpdatingIntervener2AndChangedRepresentationSolOrgChanged_thenHandle()
        throws UserNotFoundInOrganisationApiException {
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
        intervenerService.updateIntervenerDetails(current, errors, finremCallbackRequest);

        IntervenerTwo intervenerTwo = finremCaseData.getIntervenerTwo();
        assertNotNull(intervenerTwo.getIntervenerDateAdded());
        assertNotNull(intervenerTwo.getIntervenerSolEmail());
        assertNotNull(intervenerTwo.getIntervenerSolName());
        assertNotNull(intervenerTwo.getIntervenerSolPhone());
        assertNotNull(intervenerTwo.getIntervenerSolicitorFirm());
        assertNotNull(intervenerTwo.getIntervenerSolicitorReference());
        assertNotNull(intervenerTwo.getIntervenerOrganisation().getOrganisation().getOrganisationID());
        assertNotNull(intervenerTwo.getIntervenerOrganisation().getOrganisation().getOrganisationName());

        verify(assignPartiesAccessService).grantIntervenerSolicitor(CASE_ID_IN_LONG, intervenerTwo);
        verify(assignCaseAccessService).removeCaseRoleToUser(CASE_ID_IN_LONG, INTERVENER_USER_ID,
            INTVR_SOLICITOR_2.getCcdCode(), SOME_ORG_ID);
        assertThat(errors).isEmpty();
    }

    @Test
    void givenContestedCase_whenUpdatingIntervenerAndChangedRepresentationSolEmailChanged_thenHandle()
        throws UserNotFoundInOrganisationApiException {
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

        List<String> errors = new ArrayList<>();
        intervenerService.updateIntervenerDetails(current, errors, finremCallbackRequest);

        IntervenerTwo intervenerTwo = finremCaseData.getIntervenerTwo();
        assertNotNull(intervenerTwo.getIntervenerDateAdded());
        assertNotNull(intervenerTwo.getIntervenerSolEmail());
        assertNotNull(intervenerTwo.getIntervenerSolName());
        assertNotNull(intervenerTwo.getIntervenerSolPhone());
        assertNotNull(intervenerTwo.getIntervenerSolicitorFirm());
        assertNotNull(intervenerTwo.getIntervenerSolicitorReference());
        assertNotNull(intervenerTwo.getIntervenerOrganisation().getOrganisation().getOrganisationID());
        assertNotNull(intervenerTwo.getIntervenerOrganisation().getOrganisation().getOrganisationName());

        verify(assignPartiesAccessService).grantIntervenerSolicitor(CASE_ID_IN_LONG, current);
        verify(assignCaseAccessService).removeCaseRoleToUser(CASE_ID_IN_LONG, INTERVENER_USER_ID,
            INTVR_SOLICITOR_2.getCcdCode(), SOME_ORG_ID);
        assertThat(errors).isEmpty();
    }

    @Test
    void givenContestedCase_whenUpdatingIntervener2AndChangedRepresentationNoToYes_thenHandle()
        throws UserNotFoundInOrganisationApiException {
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

        List<String> errors = new ArrayList<>();
        intervenerService.updateIntervenerDetails(current, errors, finremCallbackRequest);

        IntervenerTwo intervenerTwo = finremCaseData.getIntervenerTwo();
        assertNotNull(intervenerTwo.getIntervenerSolEmail());
        assertNotNull(intervenerTwo.getIntervenerSolName());
        assertNotNull(intervenerTwo.getIntervenerSolicitorFirm());
        assertNotNull(intervenerTwo.getIntervenerSolicitorReference());
        assertNotNull(intervenerTwo.getIntervenerOrganisation().getOrganisation().getOrganisationID());
        assertNotNull(intervenerTwo.getIntervenerOrganisation().getOrganisation().getOrganisationName());
        verify(assignPartiesAccessService).grantIntervenerSolicitor(CASE_ID_IN_LONG, intervenerTwo);
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
        intervenerService.updateIntervenerDetails(wrapper, errors, finremCallbackRequest);

        assertNotNull(finremCaseData.getIntervenerThree().getIntervenerDateAdded());
        assertNotNull(finremCaseData.getIntervenerThree().getIntervenerOrganisation().getOrgPolicyCaseAssignedRole());
        assertThat(errors).isEmpty();
    }

    @Test
    void givenContestedCase_whenAddingIntervenerAndIntv3Represent_thenSetIntervenerDateAddedAndDefaultOrg()
        throws UserNotFoundInOrganisationApiException {
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

        List<String> errors = new ArrayList<>();
        intervenerService.updateIntervenerDetails(wrapper, errors, finremCallbackRequest);

        assertNotNull(finremCaseData.getIntervenerThree().getIntervenerDateAdded());
        verify(assignPartiesAccessService).grantIntervenerSolicitor(CASE_ID_IN_LONG, wrapper);
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
        intervenerService.updateIntervenerDetails(current, errors, finremCallbackRequest);

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
    void givenContestedCase_whenUpdatingIntervener3AndChangedRepresentationSolEmailChanges_thenHandle()
        throws UserNotFoundInOrganisationApiException {
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

        List<String> errors = new ArrayList<>();
        intervenerService.updateIntervenerDetails(current, errors, finremCallbackRequest);

        IntervenerThree intervenerThree = finremCaseData.getIntervenerThree();
        assertNotNull(intervenerThree.getIntervenerDateAdded());
        assertNotNull(intervenerThree.getIntervenerSolEmail());
        assertNotNull(intervenerThree.getIntervenerSolName());
        assertNotNull(intervenerThree.getIntervenerSolPhone());
        assertNotNull(intervenerThree.getIntervenerSolicitorFirm());
        assertNotNull(intervenerThree.getIntervenerSolicitorReference());
        assertNotNull(intervenerThree.getIntervenerOrganisation().getOrganisation().getOrganisationID());
        assertNotNull(intervenerThree.getIntervenerOrganisation().getOrganisation().getOrganisationName());

        verify(assignPartiesAccessService).grantIntervenerSolicitor(CASE_ID_IN_LONG, finremCaseData.getIntervenerThree());
        verify(assignCaseAccessService).removeCaseRoleToUser(CASE_ID_IN_LONG, INTERVENER_USER_ID,
            INTVR_SOLICITOR_3.getCcdCode(), SOME_ORG_ID);
        assertThat(errors).isEmpty();
    }

    @Test
    void givenContestedCase_whenUpdatingIntervenerAndChangedRepresentationSolOrgChanges_thenHandle() throws UserNotFoundInOrganisationApiException {
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
        intervenerService.updateIntervenerDetails(current, errors, finremCallbackRequest);

        IntervenerThree intervenerThree = finremCaseData.getIntervenerThree();
        assertNotNull(intervenerThree.getIntervenerDateAdded());
        assertNotNull(intervenerThree.getIntervenerSolEmail());
        assertNotNull(intervenerThree.getIntervenerSolName());
        assertNotNull(intervenerThree.getIntervenerSolPhone());
        assertNotNull(intervenerThree.getIntervenerSolicitorFirm());
        assertNotNull(intervenerThree.getIntervenerSolicitorReference());
        assertNotNull(intervenerThree.getIntervenerOrganisation().getOrganisation().getOrganisationID());
        assertNotNull(intervenerThree.getIntervenerOrganisation().getOrganisation().getOrganisationName());

        verify(assignPartiesAccessService).grantIntervenerSolicitor(CASE_ID_IN_LONG, intervenerThree);
        verify(assignCaseAccessService).removeCaseRoleToUser(CASE_ID_IN_LONG, INTERVENER_USER_ID,
            INTVR_SOLICITOR_3.getCcdCode(), SOME_ORG_ID);
        assertThat(errors).isEmpty();
    }

    @Test
    void givenContestedCase_whenUpdatingIntervener3AndChangedRepresentationNoToYes_thenHandle() throws UserNotFoundInOrganisationApiException {
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

        List<String> errors = new ArrayList<>();
        intervenerService.updateIntervenerDetails(current, errors, finremCallbackRequest);

        IntervenerThree intervenerThree = finremCaseData.getIntervenerThree();
        assertNotNull(intervenerThree.getIntervenerSolEmail());
        assertNotNull(intervenerThree.getIntervenerSolName());
        assertNotNull(intervenerThree.getIntervenerSolicitorFirm());
        assertNotNull(intervenerThree.getIntervenerSolicitorReference());
        assertNotNull(intervenerThree.getIntervenerOrganisation().getOrganisation().getOrganisationID());
        assertNotNull(intervenerThree.getIntervenerOrganisation().getOrganisation().getOrganisationName());
        verify(assignPartiesAccessService).grantIntervenerSolicitor(CASE_ID_IN_LONG, intervenerThree);
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
        intervenerService.updateIntervenerDetails(wrapper, errors, finremCallbackRequest);

        assertNotNull(finremCaseData.getIntervenerFour().getIntervenerDateAdded());
        assertNotNull(finremCaseData.getIntervenerFour().getIntervenerOrganisation().getOrgPolicyCaseAssignedRole());
        assertThat(errors).isEmpty();
    }

    @Test
    void givenContestedCase_whenAddingIntervenerAndIntv4Represent_thenSetIntervenerDateAddedAndDefaultOrg()
        throws UserNotFoundInOrganisationApiException {
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

        List<String> errors = new ArrayList<>();
        intervenerService.updateIntervenerDetails(wrapper, errors, finremCallbackRequest);

        assertNotNull(finremCaseData.getIntervenerFour().getIntervenerDateAdded());
        verify(assignPartiesAccessService).grantIntervenerSolicitor(CASE_ID_IN_LONG, wrapper);
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
        intervenerService.updateIntervenerDetails(current, errors, finremCallbackRequest);

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
    void givenContestedCase_whenUpdatingIntervenerAndChangedRepresentationNoToYes_thenHandle() throws UserNotFoundInOrganisationApiException {
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

        List<String> errors = new ArrayList<>();
        intervenerService.updateIntervenerDetails(current, errors, finremCallbackRequest);

        IntervenerFour intervenerFour = finremCaseData.getIntervenerFour();
        assertNotNull(intervenerFour.getIntervenerDateAdded());
        assertNotNull(intervenerFour.getIntervenerSolEmail());
        assertNotNull(intervenerFour.getIntervenerSolName());
        assertNotNull(intervenerFour.getIntervenerSolicitorFirm());
        assertNotNull(intervenerFour.getIntervenerSolicitorReference());
        assertNotNull(intervenerFour.getIntervenerOrganisation().getOrganisation().getOrganisationID());
        assertNotNull(intervenerFour.getIntervenerOrganisation().getOrganisation().getOrganisationName());
        verify(assignPartiesAccessService).grantIntervenerSolicitor(CASE_ID_IN_LONG, intervenerFour);
        assertThat(errors).isEmpty();
    }

    @Test
    void givenContestedCase_whenUpdatingIntervenerAndChangedRepresentationSolEmailChanges_thenHandle() throws UserNotFoundInOrganisationApiException {
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

        List<String> errors = new ArrayList<>();
        intervenerService.updateIntervenerDetails(current, errors, finremCallbackRequest);

        IntervenerFour intervenerFour = finremCaseData.getIntervenerFour();
        assertNotNull(intervenerFour.getIntervenerDateAdded());
        assertNotNull(intervenerFour.getIntervenerSolEmail());
        assertNotNull(intervenerFour.getIntervenerSolName());
        assertNotNull(intervenerFour.getIntervenerSolPhone());
        assertNotNull(intervenerFour.getIntervenerSolicitorFirm());
        assertNotNull(intervenerFour.getIntervenerSolicitorReference());
        assertNotNull(intervenerFour.getIntervenerOrganisation().getOrganisation().getOrganisationID());
        assertNotNull(intervenerFour.getIntervenerOrganisation().getOrganisation().getOrganisationName());

        verify(assignPartiesAccessService).grantIntervenerSolicitor(CASE_ID_IN_LONG, intervenerFour);
        verify(assignCaseAccessService).removeCaseRoleToUser(CASE_ID_IN_LONG, INTERVENER_USER_ID,
            INTVR_SOLICITOR_4.getCcdCode(), SOME_ORG_ID);
        assertThat(errors).isEmpty();
    }

    @Test
    void givenContestedCase_whenUpdatingIntervenerAndChangedRepresentationSolOrgChanged_thenHandle()
        throws UserNotFoundInOrganisationApiException {
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
        intervenerService.updateIntervenerDetails(current, errors, finremCallbackRequest);

        IntervenerFour intervenerFour = finremCaseData.getIntervenerFour();
        assertNotNull(intervenerFour.getIntervenerDateAdded());
        assertNotNull(intervenerFour.getIntervenerSolEmail());
        assertNotNull(intervenerFour.getIntervenerSolName());
        assertNotNull(intervenerFour.getIntervenerSolPhone());
        assertNotNull(intervenerFour.getIntervenerSolicitorFirm());
        assertNotNull(intervenerFour.getIntervenerSolicitorReference());
        assertNotNull(intervenerFour.getIntervenerOrganisation().getOrganisation().getOrganisationID());
        assertNotNull(intervenerFour.getIntervenerOrganisation().getOrganisation().getOrganisationName());

        verify(assignPartiesAccessService).grantIntervenerSolicitor(CASE_ID_IN_LONG, current);
        verify(assignCaseAccessService).removeCaseRoleToUser(CASE_ID_IN_LONG, INTERVENER_USER_ID,
            INTVR_SOLICITOR_4.getCcdCode(), SOME_ORG_ID);
        assertThat(errors).isEmpty();
    }

    @Test
    void givenContestedCase_whenAddingIntervenerWithNonRegisterEmail_thenHandlerThrowException() throws UserNotFoundInOrganisationApiException {
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

        doThrow(new UserNotFoundInOrganisationApiException())
            .when(assignPartiesAccessService).grantIntervenerSolicitor(CASE_ID_IN_LONG, wrapper);

        List<String> errors = new ArrayList<>();
        intervenerService.updateIntervenerDetails(wrapper, errors,  finremCallbackRequest);
        assertThat(errors).contains("Could not find intervener with provided email");
    }

    @Test
    void whenCalled_setIntervenerChangeDetailsForIntervenerOneOnAdding() {
        IntervenerOne intervenerOne = IntervenerOne.builder()
            .intervenerName("Intervener One")
            .build();

        IntervenerChangeDetails result = intervenerService.setIntervenerAddedChangeDetails(intervenerOne);
        assertEquals(IntervenerAction.ADDED.toString(), result.getIntervenerAction().toString());
        assertEquals(IntervenerType.INTERVENER_ONE.toString(), result.getIntervenerType().toString());
    }

    @Test
    void whenCalled_setIntervenerChangeDetailsForIntervenerTwoOnAdding() {
        IntervenerTwo intervenerTwo = IntervenerTwo.builder()
            .intervenerName("Intervener Two")
            .build();

        IntervenerChangeDetails result = intervenerService.setIntervenerAddedChangeDetails(intervenerTwo);
        assertEquals(IntervenerAction.ADDED.toString(), result.getIntervenerAction().toString());
        assertEquals(IntervenerType.INTERVENER_TWO.toString(), result.getIntervenerType().toString());
    }

    @Test
    void whenCalled_setIntervenerChangeDetailsForIntervenerThreeOnAdding() {
        IntervenerThree intervenerThree = IntervenerThree.builder()
            .intervenerName("Intervener Three")
            .build();

        IntervenerChangeDetails result = intervenerService.setIntervenerAddedChangeDetails(intervenerThree);
        assertEquals(IntervenerAction.ADDED.toString(), result.getIntervenerAction().toString());
        assertEquals(IntervenerType.INTERVENER_THREE.toString(), result.getIntervenerType().toString());
    }

    @Test
    void whenCalled_setIntervenerChangeDetailsForIntervenerFourOnAdding() {
        IntervenerFour intervenerFour = IntervenerFour.builder()
            .intervenerName("Intervener Four")
            .build();

        IntervenerChangeDetails result = intervenerService.setIntervenerAddedChangeDetails(intervenerFour);
        assertEquals(IntervenerAction.ADDED.toString(), result.getIntervenerAction().toString());
        assertEquals(IntervenerType.INTERVENER_FOUR.toString(), result.getIntervenerType().toString());
    }

    @Test
    void whenCalled_setIntervenerChangeDetailsForIntervenerOnRemoval() {
        IntervenerWrapper intervenerWrapper = IntervenerOne.builder()
            .intervenerName("Intervener One")
            .build();

        IntervenerChangeDetails result = intervenerService.setIntervenerRemovedChangeDetails(intervenerWrapper);
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
        assertTrue(intervenerService.checkIfAnyIntervenerSolicitorRemoved(finremCaseData, finremCaseDataBefore));
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
        assertTrue(intervenerService.checkIfAnyIntervenerSolicitorRemoved(finremCaseData, finremCaseDataBefore));
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
        assertTrue(intervenerService.checkIfAnyIntervenerSolicitorRemoved(finremCaseData, finremCaseDataBefore));
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
        assertTrue(intervenerService.checkIfAnyIntervenerSolicitorRemoved(finremCaseData, finremCaseDataBefore));
    }

    @Test
    void whenNoIntervenerSolicitorRemoved_ShouldReturnFalse() {
        FinremCaseData finremCaseData = FinremCaseData.builder().build();
        FinremCaseData finremCaseDataBefore = FinremCaseData.builder().build();
        assertFalse(intervenerService.checkIfAnyIntervenerSolicitorRemoved(finremCaseData, finremCaseDataBefore));
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

        intervenerService.updateIntervenerSolicitorStopRepresentingHistory(finremCaseData, originalFinremCaseData, AUTH_TOKEN);

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

    @Nested
    class RevokeIntervenerSolicitorTests {

        @ParameterizedTest
        @ValueSource(strings = {"", " "})
        @NullSource
        void givenMissingIntervenerSolEmail_whenCalled_thenDoNothing(String email) {
            IntervenerOne intervenerOne = IntervenerOne.builder().intervenerSolEmail(email)
                .intervenerOrganisation(OrganisationPolicy.builder().organisation(organisation(TEST_ORG_ID)).build())
                .build();

            intervenerService.revokeIntervenerSolicitor(CASE_ID_IN_LONG, intervenerOne);
            verifyNoInteractions(assignCaseAccessService);
            verifyNoInteractions(organisationService);
            verifyNoInteractions(systemUserService);
        }

        @Test
        void givenMissingOrganisationPolicy_whenCalled_thenDoNothing() {
            IntervenerOne intervenerOne = IntervenerOne.builder().intervenerSolEmail(TEST_SOLICITOR_EMAIL)
                .intervenerOrganisation(null)
                .build();

            intervenerService.revokeIntervenerSolicitor(CASE_ID_IN_LONG, intervenerOne);
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

            intervenerService.revokeIntervenerSolicitor(CASE_ID_IN_LONG, intervenerOne);
            verifyNoInteractions(assignCaseAccessService);
            verifyNoInteractions(organisationService);
            verifyNoInteractions(systemUserService);
        }

        @Test
        void givenMissingOrganisation_whenCalled_thenDoNothing() {
            IntervenerOne intervenerOne = IntervenerOne.builder().intervenerSolEmail(TEST_SOLICITOR_EMAIL)
                .intervenerOrganisation(OrganisationPolicy.builder().organisation(null).build())
                .build();

            intervenerService.revokeIntervenerSolicitor(CASE_ID_IN_LONG, intervenerOne);
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

            intervenerService.revokeIntervenerSolicitor(CASE_ID_IN_LONG, intervenerTwo);
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

            intervenerService.revokeIntervenerSolicitor(CASE_ID_IN_LONG, intervenerTwo);
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
