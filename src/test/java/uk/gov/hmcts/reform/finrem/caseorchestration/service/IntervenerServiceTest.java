package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Address;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicRadioList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicRadioListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Organisation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrganisationPolicy;
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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
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

    private static final String INTERVENER_SOL_FIRM = "testFirm";
    private static final String INTERVENER_SOL_REFERENCE = "testReference";

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
    public void givenCase_whenRemoveOperationChoosenForIntv1NotRepresented_thenRemoveIntervener() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();

        FinremCaseData finremCaseData = finremCallbackRequest.getCaseDetails().getData();
        IntervenerOne oneWrapper = IntervenerOne
            .builder().intervenerName("One name").intervenerEmail("test@test.com").intervenerRepresented(YesOrNo.NO).build();
        finremCaseData.setIntervenerOne(oneWrapper);
        List<String> errors = new ArrayList<>();
        service.removeIntervenerDetails(oneWrapper, errors, finremCaseData, CASE_ID);

        assertNull(finremCaseData.getIntervenerOne().getIntervenerName());
        assertNull(finremCaseData.getIntervenerOne().getIntervenerEmail());
        assertNull(finremCaseData.getIntervenerOne().getIntervenerPhone());
        assertTrue(errors.isEmpty());
    }

    @Test
    public void givenCase_whenRemoveOperationChoosenForIntv1Represented_thenRemoveIntervener() {
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
        service.removeIntervenerDetails(oneWrapper, errors, finremCaseData, CASE_ID);

        assertNull(finremCaseData.getIntervenerOne().getIntervenerName());
        assertNull(finremCaseData.getIntervenerOne().getIntervenerEmail());
        assertNull(finremCaseData.getIntervenerOne().getIntervenerPhone());
        assertNull(finremCaseData.getIntervenerOne().getIntervenerSolicitorFirm());
        assertNull(finremCaseData.getIntervenerOne().getIntervenerSolicitorReference());
        verify(assignCaseAccessService).removeCaseRoleToUser(CASE_ID, INTERVENER_USER_ID,
            INTVR_SOLICITOR_1.getCcdCode(), SOME_ORG_ID);
        assertTrue(errors.isEmpty());
    }


    @Test
    public void givenCase_whenRemoveOperationChoosenForIntv2NotRepresented_thenRemoveintervener() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();

        FinremCaseData finremCaseData = finremCallbackRequest.getCaseDetails().getData();
        IntervenerTwo twoWrapper = IntervenerTwo
            .builder().intervenerName("Two name").intervenerEmail("test@test.com").intervenerRepresented(YesOrNo.NO).build();
        finremCaseData.setIntervenerTwo(twoWrapper);
        List<String> errors = new ArrayList<>();
        service.removeIntervenerDetails(twoWrapper, errors, finremCaseData, CASE_ID);

        assertNull(finremCaseData.getIntervenerTwo().getIntervenerName());
        assertNull(finremCaseData.getIntervenerTwo().getIntervenerEmail());
        assertNull(finremCaseData.getIntervenerTwo().getIntervenerSolicitorFirm());
        assertNull(finremCaseData.getIntervenerTwo().getIntervenerSolicitorReference());
        verify(assignCaseAccessService, never()).removeCaseRoleToUser(any(), any(), any(), any());
        assertTrue(errors.isEmpty());
    }

    @Test
    public void givenCase_whenRemoveOperationChoosenForIntv2Represented_thenRemoveIntervener() {
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
        service.removeIntervenerDetails(twoWrapper, errors, finremCaseData, CASE_ID);

        assertNull(finremCaseData.getIntervenerTwo().getIntervenerName());
        assertNull(finremCaseData.getIntervenerTwo().getIntervenerEmail());
        assertNull(finremCaseData.getIntervenerTwo().getIntervenerSolicitorFirm());
        assertNull(finremCaseData.getIntervenerTwo().getIntervenerSolicitorReference());
        verify(assignCaseAccessService).removeCaseRoleToUser(CASE_ID, INTERVENER_USER_ID,
            INTVR_SOLICITOR_2.getCcdCode(), SOME_ORG_ID);
        assertTrue(errors.isEmpty());
    }

    @Test
    public void givenCase_whenRemoveOperationChoosenForIntv3NotRepesented_thenRemoveintervener() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();

        FinremCaseData finremCaseData = finremCallbackRequest.getCaseDetails().getData();
        IntervenerThree threeWrapper = IntervenerThree
            .builder().intervenerName("Three name").intervenerEmail("test@test.com").intervenerRepresented(YesOrNo.NO).build();
        finremCaseData.setIntervenerThree(threeWrapper);

        List<String> errors = new ArrayList<>();
        service.removeIntervenerDetails(threeWrapper, errors, finremCaseData, CASE_ID);

        assertNull(finremCaseData.getIntervenerThree().getIntervenerName());
        assertNull(finremCaseData.getIntervenerThree().getIntervenerEmail());
        assertNull(finremCaseData.getIntervenerThree().getIntervenerSolicitorFirm());
        assertNull(finremCaseData.getIntervenerThree().getIntervenerSolicitorReference());
        assertTrue(errors.isEmpty());

    }

    @Test
    public void givenCase_whenRemoveOperationChoosenForIntv3Repesented_thenRemoveIntervener() {
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
        service.removeIntervenerDetails(threeWrapper, errors, finremCaseData, CASE_ID);

        assertNull(finremCaseData.getIntervenerThree().getIntervenerName());
        assertNull(finremCaseData.getIntervenerThree().getIntervenerEmail());
        assertNull(finremCaseData.getIntervenerThree().getIntervenerSolicitorFirm());
        assertNull(finremCaseData.getIntervenerThree().getIntervenerSolicitorReference());
        verify(assignCaseAccessService).removeCaseRoleToUser(CASE_ID, INTERVENER_USER_ID,
            INTVR_SOLICITOR_3.getCcdCode(), SOME_ORG_ID);
        assertTrue(errors.isEmpty());
    }

    @Test
    public void givenCase_whenRemoveOperationChoosenForIntv4NotRepresented_thenRemoveintervener() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();

        FinremCaseData finremCaseData = finremCallbackRequest.getCaseDetails().getData();
        IntervenerFour fourWrapper = IntervenerFour
            .builder().intervenerName("Four name").intervenerEmail("test@test.com").intervenerRepresented(YesOrNo.NO).build();
        finremCaseData.setIntervenerFour(fourWrapper);

        List<String> errors = new ArrayList<>();
        service.removeIntervenerDetails(fourWrapper, errors, finremCaseData, CASE_ID);

        assertNull(finremCaseData.getIntervenerFour().getIntervenerName());
        assertNull(finremCaseData.getIntervenerFour().getIntervenerEmail());
        assertNull(finremCaseData.getIntervenerFour().getIntervenerSolicitorFirm());
        assertNull(finremCaseData.getIntervenerFour().getIntervenerSolicitorReference());
        assertTrue(errors.isEmpty());
    }

    @Test
    public void givenCase_whenRemoveOperationChoosenForIntv4Represented_thenRemoveIntervener() {
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
        service.removeIntervenerDetails(fourWrapper, errors, finremCaseData, CASE_ID);

        assertNull(finremCaseData.getIntervenerFour().getIntervenerName());
        assertNull(finremCaseData.getIntervenerFour().getIntervenerEmail());
        assertNull(finremCaseData.getIntervenerFour().getIntervenerSolicitorFirm());
        assertNull(finremCaseData.getIntervenerFour().getIntervenerSolicitorReference());
        verify(assignCaseAccessService).removeCaseRoleToUser(CASE_ID, INTERVENER_USER_ID,
            INTVR_SOLICITOR_4.getCcdCode(), SOME_ORG_ID);
        assertTrue(errors.isEmpty());
    }

    @Test
    public void givenContestedCase_whenAddingintervenerAndIntv1Represent_thenSetIntvenerDateAddedAndDefaultOrg() {
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
        verify(assignCaseAccessService).grantCaseRoleToUser(CASE_ID, INTERVENER_USER_ID,
            INTVR_SOLICITOR_1.getCcdCode(), SOME_ORG_ID);
        assertTrue(errors.isEmpty());
    }

    @Test
    public void givenContestedCase_whenUpdatingIntervener1AndChangedRepresetationYesToNoAndCountryNotProvided_thenShowError() {
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
        IntervenerChangeDetails intervenerChangeDetails = service.updateIntervenerDetails(oneWrapper1, errors, finremCallbackRequest);

        IntervenerOne intervenerOneWrapper = finremCaseData.getIntervenerOne();

        assertNotNull(intervenerOneWrapper.getIntervenerDateAdded());
        assertNull(intervenerOneWrapper.getIntervenerSolEmail());
        assertNull(intervenerOneWrapper.getIntervenerSolName());
        assertNull(intervenerOneWrapper.getIntervenerSolPhone());
        assertNull(intervenerOneWrapper.getIntervenerSolicitorFirm());
        assertNull(intervenerOneWrapper.getIntervenerSolicitorReference());
        assertNull(intervenerOneWrapper.getIntervenerOrganisation().getOrganisation().getOrganisationID());
        assertNull(intervenerOneWrapper.getIntervenerOrganisation().getOrganisation().getOrganisationName());
        verify(assignCaseAccessService).removeCaseRoleToUser(CASE_ID, INTERVENER_USER_ID,
            INTVR_SOLICITOR_1.getCcdCode(), SOME_ORG_ID);
        assertFalse(errors.isEmpty());
        assertTrue(errors.contains("If intervener resides outside of UK, please provide the country of residence."));
    }

    @Test
    public void givenContestedCase_whenUpdatingIntervener1AndChangedRepresetationYesToNo_thenHandle() {
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
        verify(assignCaseAccessService).removeCaseRoleToUser(CASE_ID, INTERVENER_USER_ID,
            INTVR_SOLICITOR_1.getCcdCode(), SOME_ORG_ID);
        assertTrue(errors.isEmpty());
    }

    @Test
    public void givenContestedCase_whenUpdatingIntervener1AndChangedRepresetationSolEmailChanged_thenHandle() {
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

        verify(assignCaseAccessService).grantCaseRoleToUser(CASE_ID, INTERVENER_USER_ID,
            INTVR_SOLICITOR_1.getCcdCode(), SOME_ORG_ID);
        verify(assignCaseAccessService).removeCaseRoleToUser(CASE_ID, INTERVENER_USER_ID,
            INTVR_SOLICITOR_1.getCcdCode(), SOME_ORG_ID);
        assertTrue(errors.isEmpty());
    }

    @Test
    public void givenContestedCase_whenUpdatingIntervener1AndChangedRepresetationSolOrgChanged_thenHandle() {
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

        verify(assignCaseAccessService).grantCaseRoleToUser(CASE_ID, INTERVENER_USER_ID,
            INTVR_SOLICITOR_1.getCcdCode(), CHANGE_ORG_ID);
        verify(assignCaseAccessService).removeCaseRoleToUser(CASE_ID, INTERVENER_USER_ID,
            INTVR_SOLICITOR_1.getCcdCode(), SOME_ORG_ID);
        assertTrue(errors.isEmpty());
    }


    @Test
    public void givenContestedCase_whenUpdatingIntervener1AndChangedRepresetationNoToYes_thenHandle() {
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
        verify(assignCaseAccessService).grantCaseRoleToUser(CASE_ID, INTERVENER_USER_ID,
            INTVR_SOLICITOR_1.getCcdCode(), SOME_ORG_ID);
        assertTrue(errors.isEmpty());
    }

    @Test
    public void givenContestedCase_whenAddingIntervenerAndIntv1NotRepresent_thenSetIntvenerDateAddedAndDefaultOrg() {
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
        assertTrue(errors.isEmpty());
    }

    @Test
    public void givenContestedCase_whenAddingIntervenerAndIntv2NotRepresent_thenSetIntvenerDateAddedAndDefaultOrg() {
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
        assertTrue(errors.isEmpty());
    }

    @Test
    public void givenContestedCase_whenAddingIntervenerAndIntv2Represent_thenSetIntvenerDateAddedAndDefaultOrg() {
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
        verify(assignCaseAccessService).grantCaseRoleToUser(CASE_ID, INTERVENER_USER_ID,
            INTVR_SOLICITOR_2.getCcdCode(), SOME_ORG_ID);
        assertTrue(errors.isEmpty());
    }

    @Test
    public void givenContestedCase_whenUpdatingIntervener2AndChangedRepresetationYesToNo_thenHandle() {
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
        verify(assignCaseAccessService).removeCaseRoleToUser(CASE_ID, INTERVENER_USER_ID,
            INTVR_SOLICITOR_2.getCcdCode(), SOME_ORG_ID);
        assertTrue(errors.isEmpty());
    }

    @Test
    public void givenContestedCase_whenUpdatingIntervener2AndChangedRepresetationSolOrgChanged_thenHandle() {
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

        verify(assignCaseAccessService).grantCaseRoleToUser(CASE_ID, INTERVENER_USER_ID,
            INTVR_SOLICITOR_2.getCcdCode(), CHANGE_ORG_ID);
        verify(assignCaseAccessService).removeCaseRoleToUser(CASE_ID, INTERVENER_USER_ID,
            INTVR_SOLICITOR_2.getCcdCode(), SOME_ORG_ID);
        assertTrue(errors.isEmpty());
    }


    @Test
    public void givenContestedCase_whenUpdatingIntervenerAndChangedRepresetationSolEmailChanged_thenHandle() {
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

        verify(assignCaseAccessService).grantCaseRoleToUser(CASE_ID, INTERVENER_USER_ID,
            INTVR_SOLICITOR_2.getCcdCode(), SOME_ORG_ID);
        verify(assignCaseAccessService).removeCaseRoleToUser(CASE_ID, INTERVENER_USER_ID,
            INTVR_SOLICITOR_2.getCcdCode(), SOME_ORG_ID);
        assertTrue(errors.isEmpty());
    }

    @Test
    public void givenContestedCase_whenUpdatingIntervener2AndChangedRepresetationNoToYes_thenHandle() {
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
        verify(assignCaseAccessService).grantCaseRoleToUser(CASE_ID, INTERVENER_USER_ID,
            INTVR_SOLICITOR_2.getCcdCode(), SOME_ORG_ID);
        assertTrue(errors.isEmpty());
    }


    @Test
    public void givenContestedCase_whenAddingIntervenerAndIntv3NotRepresent_thenSetIntvenerDateAddedAndDefaultOrg() {
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
        assertTrue(errors.isEmpty());
    }

    @Test
    public void givenContestedCase_whenAddingIntervenerAndIntv3Represent_thenSetIntvenerDateAddedAndDefaultOrg() {
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
        verify(assignCaseAccessService).grantCaseRoleToUser(CASE_ID, INTERVENER_USER_ID,
            INTVR_SOLICITOR_3.getCcdCode(), SOME_ORG_ID);
        assertTrue(errors.isEmpty());
    }

    @Test
    public void givenContestedCase_whenUpdatingIntervener3AndChangedRepresetationYesToNo_thenHandle() {
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
        verify(assignCaseAccessService).removeCaseRoleToUser(CASE_ID, INTERVENER_USER_ID,
            INTVR_SOLICITOR_3.getCcdCode(), SOME_ORG_ID);
        assertTrue(errors.isEmpty());
    }

    @Test
    public void givenContestedCase_whenUpdatingIntervener3AndChangedRepresetationSolEmailChanges_thenHandle() {
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

        verify(assignCaseAccessService).grantCaseRoleToUser(CASE_ID, INTERVENER_USER_ID,
            INTVR_SOLICITOR_3.getCcdCode(), SOME_ORG_ID);
        verify(assignCaseAccessService).removeCaseRoleToUser(CASE_ID, INTERVENER_USER_ID,
            INTVR_SOLICITOR_3.getCcdCode(), SOME_ORG_ID);
        assertTrue(errors.isEmpty());
    }

    @Test
    public void givenContestedCase_whenUpdatingIntervenerAndChangedRepresetationSolOrgChanges_thenHandle() {
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

        verify(assignCaseAccessService).grantCaseRoleToUser(CASE_ID, INTERVENER_USER_ID,
            INTVR_SOLICITOR_3.getCcdCode(), CHANGE_ORG_ID);
        verify(assignCaseAccessService).removeCaseRoleToUser(CASE_ID, INTERVENER_USER_ID,
            INTVR_SOLICITOR_3.getCcdCode(), SOME_ORG_ID);
        assertTrue(errors.isEmpty());
    }

    @Test
    public void givenContestedCase_whenUpdatingIntervener3AndChangedRepresetationNoToYes_thenHandle() {
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
        verify(assignCaseAccessService).grantCaseRoleToUser(CASE_ID, INTERVENER_USER_ID,
            INTVR_SOLICITOR_3.getCcdCode(), SOME_ORG_ID);
        assertTrue(errors.isEmpty());
    }

    @Test
    public void givenContestedCase_whenAddingIntervenerAndIntv4NotRepresent_thenSetIntvenerDateAddedAndDefaultOrg() {
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
        assertTrue(errors.isEmpty());
    }

    @Test
    public void givenContestedCase_whenAddingIntervenerAndIntv4Represent_thenSetIntvenerDateAddedAndDefaultOrg() {
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
        verify(assignCaseAccessService).grantCaseRoleToUser(CASE_ID, INTERVENER_USER_ID,
            INTVR_SOLICITOR_4.getCcdCode(), SOME_ORG_ID);
        assertTrue(errors.isEmpty());
    }

    @Test
    public void givenContestedCase_whenUpdatingIntervenerAndChangedRepresetationYesToNo_thenHandle() {
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

        verify(assignCaseAccessService).removeCaseRoleToUser(CASE_ID, INTERVENER_USER_ID,
            INTVR_SOLICITOR_4.getCcdCode(), SOME_ORG_ID);
        assertTrue(errors.isEmpty());
    }

    @Test
    public void givenContestedCase_whenUpdatingIntervenerAndChangedRepresetationNoToYes_thenHandle() {
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
        verify(assignCaseAccessService).grantCaseRoleToUser(CASE_ID, INTERVENER_USER_ID,
            INTVR_SOLICITOR_4.getCcdCode(), SOME_ORG_ID);
        assertTrue(errors.isEmpty());
    }

    @Test
    public void givenContestedCase_whenUpdatingIntervenerAndChangedRepresetationSolEmailChanges_thenHandle() {
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

        verify(assignCaseAccessService).grantCaseRoleToUser(CASE_ID, INTERVENER_USER_ID,
            INTVR_SOLICITOR_4.getCcdCode(), SOME_ORG_ID);
        verify(assignCaseAccessService).removeCaseRoleToUser(CASE_ID, INTERVENER_USER_ID,
            INTVR_SOLICITOR_4.getCcdCode(), SOME_ORG_ID);
        assertTrue(errors.isEmpty());
    }

    @Test
    public void givenContestedCase_whenUpdatingIntervenerAndChangedRepresetationSolOrgChanged_thenHandle() {
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

        verify(assignCaseAccessService).grantCaseRoleToUser(CASE_ID, INTERVENER_USER_ID,
            INTVR_SOLICITOR_4.getCcdCode(), CHANGE_ORG_ID);
        verify(assignCaseAccessService).removeCaseRoleToUser(CASE_ID, INTERVENER_USER_ID,
            INTVR_SOLICITOR_4.getCcdCode(), SOME_ORG_ID);
        assertTrue(errors.isEmpty());
    }

    @Test
    public void givenContestedCase_whenAddingIntervenerWithNonRegisterEmail_thenHandlerThrowException() {
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
        String error = String.format("Could not find intervener with provided email for caseId 123");
        assertEquals("expecting exception to throw when user not found in am", error, errors.get(0));
        assertFalse(errors.isEmpty());
    }

    @Test
    public void whenCalled_setIntervenerChangeDetailsForIntervenerOneOnAdding() {
        IntervenerOne intervenerOne = IntervenerOne.builder()
            .intervenerName("Intervener One")
            .build();

        IntervenerChangeDetails result = service.setIntervenerAddedChangeDetails(intervenerOne);
        Assert.assertEquals(IntervenerAction.ADDED.toString(), result.getIntervenerAction().toString());
        Assert.assertEquals(IntervenerType.INTERVENER_ONE.toString(), result.getIntervenerType().toString());
    }

    @Test
    public void whenCalled_setIntervenerChangeDetailsForIntervenerTwoOnAdding() {
        IntervenerTwo intervenerTwo = IntervenerTwo.builder()
            .intervenerName("Intervener Two")
            .build();

        IntervenerChangeDetails result = service.setIntervenerAddedChangeDetails(intervenerTwo);
        Assert.assertEquals(IntervenerAction.ADDED.toString(), result.getIntervenerAction().toString());
        Assert.assertEquals(IntervenerType.INTERVENER_TWO.toString(), result.getIntervenerType().toString());
    }

    @Test
    public void whenCalled_setIntervenerChangeDetailsForIntervenerThreeOnAdding() {
        IntervenerThree intervenerThree = IntervenerThree.builder()
            .intervenerName("Intervener Three")
            .build();

        IntervenerChangeDetails result = service.setIntervenerAddedChangeDetails(intervenerThree);
        Assert.assertEquals(IntervenerAction.ADDED.toString(), result.getIntervenerAction().toString());
        Assert.assertEquals(IntervenerType.INTERVENER_THREE.toString(), result.getIntervenerType().toString());
    }

    @Test
    public void whenCalled_setIntervenerChangeDetailsForIntervenerFourOnAdding() {
        IntervenerFour intervenerFour = IntervenerFour.builder()
            .intervenerName("Intervener Four")
            .build();

        IntervenerChangeDetails result = service.setIntervenerAddedChangeDetails(intervenerFour);
        Assert.assertEquals(IntervenerAction.ADDED.toString(), result.getIntervenerAction().toString());
        Assert.assertEquals(IntervenerType.INTERVENER_FOUR.toString(), result.getIntervenerType().toString());
    }

    @Test
    public void whenCalled_setIntervenerChangeDetailsForIntervenerOnRemoval() {
        IntervenerWrapper intervenerWrapper = IntervenerOne.builder()
            .intervenerName("Intervener One")
            .build();

        IntervenerChangeDetails result = service.setIntervenerRemovedChangeDetails(intervenerWrapper);
        Assert.assertEquals(IntervenerAction.REMOVED.toString(), result.getIntervenerAction().toString());
        Assert.assertEquals(IntervenerType.INTERVENER_ONE.toString(), result.getIntervenerType().toString());
    }

    @Test
    public void whenIntervenerOneSolicitorRemoved_ShouldReturnTrue() {
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
        Assert.assertTrue(service.checkIfAnyIntervenerSolicitorRemoved(finremCaseData, finremCaseDataBefore));
    }

    @Test
    public void whenIntervenerTwoSolicitorRemoved_ShouldReturnTrue() {
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
        Assert.assertTrue(service.checkIfAnyIntervenerSolicitorRemoved(finremCaseData, finremCaseDataBefore));
    }

    @Test
    public void whenIntervenerThreeSolicitorRemoved_ShouldReturnTrue() {
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
        Assert.assertTrue(service.checkIfAnyIntervenerSolicitorRemoved(finremCaseData, finremCaseDataBefore));
    }

    @Test
    public void whenIntervenerFourSolicitorRemoved_ShouldReturnTrue() {
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
        Assert.assertTrue(service.checkIfAnyIntervenerSolicitorRemoved(finremCaseData, finremCaseDataBefore));
    }

    @Test
    public void whenNoIntervenerSolicitorRemoved_ShouldReturnFalse() {
        FinremCaseData finremCaseData = FinremCaseData.builder().build();
        FinremCaseData finremCaseDataBefore = FinremCaseData.builder().build();
        assertFalse(service.checkIfAnyIntervenerSolicitorRemoved(finremCaseData, finremCaseDataBefore));
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
