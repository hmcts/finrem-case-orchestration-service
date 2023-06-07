package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.Assert;
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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerAction;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerChangeDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerType;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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
        IntervenerOneWrapper oneWrapper = IntervenerOneWrapper
            .builder().intervenerName("One name").intervenerEmail("test@test.com").intervenerRepresented(YesOrNo.NO).build();
        finremCaseData.setIntervenerOneWrapper(oneWrapper);

        service.removeIntervenerDetails(oneWrapper, finremCaseData, CASE_ID);

        assertNull(finremCaseData.getIntervenerOneWrapper().getIntervenerName());
        assertNull(finremCaseData.getIntervenerOneWrapper().getIntervenerEmail());
        assertNull(finremCaseData.getIntervenerOneWrapper().getIntervenerPhone());
    }

    @Test
    public void givenCase_whenRemoveOperationChoosenForIntv1Represented_thenRemoveIntervener() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();

        FinremCaseData finremCaseData = finremCallbackRequest.getCaseDetails().getData();
        OrganisationPolicy organisationPolicy = OrganisationPolicy.builder().organisation(
            Organisation.builder().organisationID(SOME_ORG_ID).organisationName(SOME_ORG_ID).build()
        ).build();
        IntervenerOneWrapper oneWrapper = IntervenerOneWrapper
            .builder()
            .intervenerName("One name")
            .intervenerEmail("test@test.com")
            .intervenerSolEmail("test@test.com")
            .intervenerSolicitorFirm(INTERVENER_SOL_FIRM)
            .intervenerSolicitorReference(INTERVENER_SOL_REFERENCE)
            .intervenerOrganisation(organisationPolicy)
            .intervenerRepresented(YesOrNo.YES).build();
        finremCaseData.setIntervenerOneWrapper(oneWrapper);

        when(systemUserService.getSysUserToken()).thenReturn(AUTH_TOKEN);
        when(organisationService.findUserByEmail(INTERVENER_TEST_EMAIL, AUTH_TOKEN)).thenReturn(Optional.of(INTERVENER_USER_ID));
        service.removeIntervenerDetails(oneWrapper, finremCaseData, CASE_ID);

        assertNull(finremCaseData.getIntervenerOneWrapper().getIntervenerName());
        assertNull(finremCaseData.getIntervenerOneWrapper().getIntervenerEmail());
        assertNull(finremCaseData.getIntervenerOneWrapper().getIntervenerPhone());
        assertNull(finremCaseData.getIntervenerOneWrapper().getIntervenerSolicitorFirm());
        assertNull(finremCaseData.getIntervenerOneWrapper().getIntervenerSolicitorReference());
        verify(assignCaseAccessService).removeCaseRoleToUser(CASE_ID, INTERVENER_USER_ID,
            INTVR_SOLICITOR_1.getValue(), SOME_ORG_ID);
    }


    @Test
    public void givenCase_whenRemoveOperationChoosenForIntv2NotRepresented_thenRemoveintervener() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();

        FinremCaseData finremCaseData = finremCallbackRequest.getCaseDetails().getData();
        IntervenerTwoWrapper twoWrapper = IntervenerTwoWrapper
            .builder().intervenerName("Two name").intervenerEmail("test@test.com").intervenerRepresented(YesOrNo.NO).build();
        finremCaseData.setIntervenerTwoWrapper(twoWrapper);

        service.removeIntervenerDetails(twoWrapper, finremCaseData, CASE_ID);

        assertNull(finremCaseData.getIntervenerTwoWrapper().getIntervenerName());
        assertNull(finremCaseData.getIntervenerTwoWrapper().getIntervenerEmail());
        assertNull(finremCaseData.getIntervenerTwoWrapper().getIntervenerSolicitorFirm());
        assertNull(finremCaseData.getIntervenerTwoWrapper().getIntervenerSolicitorReference());
        verify(assignCaseAccessService, never()).removeCaseRoleToUser(any(), any(), any(), any());
    }

    @Test
    public void givenCase_whenRemoveOperationChoosenForIntv2Represented_thenRemoveIntervener() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();

        FinremCaseData finremCaseData = finremCallbackRequest.getCaseDetails().getData();
        OrganisationPolicy organisationPolicy = OrganisationPolicy.builder().organisation(
            Organisation.builder().organisationID(SOME_ORG_ID).organisationName(SOME_ORG_ID).build()
        ).build();
        IntervenerTwoWrapper twoWrapper = IntervenerTwoWrapper
            .builder()
            .intervenerName("Two name")
            .intervenerEmail("test@test.com")
            .intervenerSolEmail("test@test.com")
            .intervenerSolicitorFirm(INTERVENER_SOL_FIRM)
            .intervenerSolicitorReference(INTERVENER_SOL_REFERENCE)
            .intervenerOrganisation(organisationPolicy)
            .intervenerRepresented(YesOrNo.YES).build();
        finremCaseData.setIntervenerTwoWrapper(twoWrapper);

        when(systemUserService.getSysUserToken()).thenReturn(AUTH_TOKEN);
        when(organisationService.findUserByEmail(INTERVENER_TEST_EMAIL, AUTH_TOKEN)).thenReturn(Optional.of(INTERVENER_USER_ID));
        service.removeIntervenerDetails(twoWrapper, finremCaseData, CASE_ID);

        assertNull(finremCaseData.getIntervenerTwoWrapper().getIntervenerName());
        assertNull(finremCaseData.getIntervenerTwoWrapper().getIntervenerEmail());
        assertNull(finremCaseData.getIntervenerTwoWrapper().getIntervenerSolicitorFirm());
        assertNull(finremCaseData.getIntervenerTwoWrapper().getIntervenerSolicitorReference());
        verify(assignCaseAccessService).removeCaseRoleToUser(CASE_ID, INTERVENER_USER_ID,
            INTVR_SOLICITOR_2.getValue(), SOME_ORG_ID);
    }

    @Test
    public void givenCase_whenRemoveOperationChoosenForIntv3NotRepesented_thenRemoveintervener() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();

        FinremCaseData finremCaseData = finremCallbackRequest.getCaseDetails().getData();
        IntervenerThreeWrapper threeWrapper = IntervenerThreeWrapper
            .builder().intervenerName("Three name").intervenerEmail("test@test.com").intervenerRepresented(YesOrNo.NO).build();
        finremCaseData.setIntervenerThreeWrapper(threeWrapper);

        service.removeIntervenerDetails(threeWrapper, finremCaseData, CASE_ID);

        assertNull(finremCaseData.getIntervenerThreeWrapper().getIntervenerName());
        assertNull(finremCaseData.getIntervenerThreeWrapper().getIntervenerEmail());
        assertNull(finremCaseData.getIntervenerThreeWrapper().getIntervenerSolicitorFirm());
        assertNull(finremCaseData.getIntervenerThreeWrapper().getIntervenerSolicitorReference());

    }

    @Test
    public void givenCase_whenRemoveOperationChoosenForIntv3Repesented_thenRemoveIntervener() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();

        FinremCaseData finremCaseData = finremCallbackRequest.getCaseDetails().getData();
        OrganisationPolicy organisationPolicy = OrganisationPolicy.builder().organisation(
            Organisation.builder().organisationID(SOME_ORG_ID).organisationName(SOME_ORG_ID).build()
        ).build();
        IntervenerThreeWrapper threeWrapper = IntervenerThreeWrapper
            .builder().intervenerName("Three name").intervenerEmail("test@test.com")
            .intervenerSolEmail("test@test.com")
            .intervenerSolicitorFirm(INTERVENER_SOL_FIRM)
            .intervenerSolicitorReference(INTERVENER_SOL_REFERENCE)
            .intervenerRepresented(YesOrNo.YES)
            .intervenerOrganisation(organisationPolicy)
            .build();
        finremCaseData.setIntervenerThreeWrapper(threeWrapper);

        when(systemUserService.getSysUserToken()).thenReturn(AUTH_TOKEN);
        when(organisationService.findUserByEmail(INTERVENER_TEST_EMAIL, AUTH_TOKEN)).thenReturn(Optional.of(INTERVENER_USER_ID));
        service.removeIntervenerDetails(threeWrapper, finremCaseData, CASE_ID);

        assertNull(finremCaseData.getIntervenerThreeWrapper().getIntervenerName());
        assertNull(finremCaseData.getIntervenerThreeWrapper().getIntervenerEmail());
        assertNull(finremCaseData.getIntervenerThreeWrapper().getIntervenerSolicitorFirm());
        assertNull(finremCaseData.getIntervenerThreeWrapper().getIntervenerSolicitorReference());
        verify(assignCaseAccessService).removeCaseRoleToUser(CASE_ID, INTERVENER_USER_ID,
            INTVR_SOLICITOR_3.getValue(), SOME_ORG_ID);
    }

    @Test
    public void givenCase_whenRemoveOperationChoosenForIntv4NotRepresented_thenRemoveintervener() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();

        FinremCaseData finremCaseData = finremCallbackRequest.getCaseDetails().getData();
        IntervenerFourWrapper fourWrapper = IntervenerFourWrapper
            .builder().intervenerName("Four name").intervenerEmail("test@test.com").intervenerRepresented(YesOrNo.NO).build();
        finremCaseData.setIntervenerFourWrapper(fourWrapper);

        service.removeIntervenerDetails(fourWrapper, finremCaseData, CASE_ID);

        assertNull(finremCaseData.getIntervenerFourWrapper().getIntervenerName());
        assertNull(finremCaseData.getIntervenerFourWrapper().getIntervenerEmail());
        assertNull(finremCaseData.getIntervenerFourWrapper().getIntervenerSolicitorFirm());
        assertNull(finremCaseData.getIntervenerFourWrapper().getIntervenerSolicitorReference());
    }

    @Test
    public void givenCase_whenRemoveOperationChoosenForIntv4Represented_thenRemoveIntervener() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();

        FinremCaseData finremCaseData = finremCallbackRequest.getCaseDetails().getData();
        OrganisationPolicy organisationPolicy = OrganisationPolicy.builder().organisation(
            Organisation.builder().organisationID(SOME_ORG_ID).organisationName(SOME_ORG_ID).build()
        ).build();
        IntervenerFourWrapper fourWrapper = IntervenerFourWrapper
            .builder().intervenerName("Four name").intervenerEmail("test@test.com")
            .intervenerRepresented(YesOrNo.YES)
            .intervenerSolEmail("test@test.com")
            .intervenerSolicitorFirm(INTERVENER_SOL_FIRM)
            .intervenerSolicitorReference(INTERVENER_SOL_REFERENCE)
            .intervenerOrganisation(organisationPolicy)
            .build();
        finremCaseData.setIntervenerFourWrapper(fourWrapper);

        when(systemUserService.getSysUserToken()).thenReturn(AUTH_TOKEN);
        when(organisationService.findUserByEmail(INTERVENER_TEST_EMAIL, AUTH_TOKEN)).thenReturn(Optional.of(INTERVENER_USER_ID));
        service.removeIntervenerDetails(fourWrapper, finremCaseData, CASE_ID);

        assertNull(finremCaseData.getIntervenerFourWrapper().getIntervenerName());
        assertNull(finremCaseData.getIntervenerFourWrapper().getIntervenerEmail());
        assertNull(finremCaseData.getIntervenerFourWrapper().getIntervenerSolicitorFirm());
        assertNull(finremCaseData.getIntervenerFourWrapper().getIntervenerSolicitorReference());
        verify(assignCaseAccessService).removeCaseRoleToUser(CASE_ID, INTERVENER_USER_ID,
            INTVR_SOLICITOR_4.getValue(), SOME_ORG_ID);

    }

    @Test
    public void givenContestedCase_whenAddingintervenerAndIntv1Represent_thenSetIntvenerDateAddedAndDefaultOrg() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseData finremCaseData = finremCallbackRequest.getCaseDetails().getData();
        OrganisationPolicy organisationPolicy = OrganisationPolicy.builder().organisation(
            Organisation.builder().organisationID(SOME_ORG_ID).organisationName(SOME_ORG_ID).build()
        ).build();
        IntervenerOneWrapper oneWrapper = IntervenerOneWrapper
            .builder().intervenerName("One name").intervenerEmail("test@test.com")
            .intervenerSolEmail("test@test.com")
            .intervenerSolicitorFirm(INTERVENER_SOL_FIRM)
            .intervenerSolicitorReference(INTERVENER_SOL_REFERENCE)
            .intervenerRepresented(YesOrNo.YES)
            .intervenerOrganisation(organisationPolicy).build();
        finremCaseData.setIntervenerOneWrapper(oneWrapper);

        DynamicRadioListElement option = DynamicRadioListElement.builder().code(INTERVENER_ONE).build();
        List<DynamicRadioListElement> list = List.of(option);
        DynamicRadioList dynamicRadioList = DynamicRadioList.builder().listItems(list).build();
        finremCaseData.setIntervenersList(dynamicRadioList);
        DynamicRadioListElement option1 = DynamicRadioListElement.builder().code(INTERVENER_ONE).build();
        finremCaseData.getIntervenersList().setValue(option1);

        when(systemUserService.getSysUserToken()).thenReturn(AUTH_TOKEN);
        when(organisationService.findUserByEmail(INTERVENER_TEST_EMAIL, AUTH_TOKEN)).thenReturn(Optional.of(INTERVENER_USER_ID));

        service.updateIntervenerDetails(oneWrapper, finremCallbackRequest);

        assertNotNull(finremCaseData.getIntervenerOneWrapper().getIntervenerDateAdded());
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
            .builder().intervenerName("One name").intervenerEmail("test@test.com")
            .intervenerSolEmail("test@test.com")
            .intervenerSolicitorFirm(INTERVENER_SOL_FIRM)
            .intervenerSolicitorReference(INTERVENER_SOL_REFERENCE)
            .intervenerRepresented(YesOrNo.YES)
            .intervenerDateAdded(LocalDate.of(2023, 1, 1))
            .intervenerOrganisation(organisationPolicy).build();
        finremCaseDataBefore.setIntervenerOneWrapper(oneWrapper);

        IntervenerOneWrapper oneWrapper1 = IntervenerOneWrapper
            .builder().intervenerName("One name").intervenerEmail("test@test.com")
            .intervenerRepresented(YesOrNo.NO)
            .intervenerDateAdded(LocalDate.of(2023, 1, 1))
            .intervenerOrganisation(organisationPolicy).build();
        finremCaseData.setIntervenerOneWrapper(oneWrapper1);


        DynamicRadioListElement option = DynamicRadioListElement.builder().code(INTERVENER_ONE).build();
        List<DynamicRadioListElement> list = List.of(option);
        DynamicRadioList dynamicRadioList = DynamicRadioList.builder().listItems(list).build();
        finremCaseData.setIntervenersList(dynamicRadioList);
        DynamicRadioListElement option1 = DynamicRadioListElement.builder().code(INTERVENER_ONE).build();
        finremCaseData.getIntervenersList().setValue(option1);

        when(systemUserService.getSysUserToken()).thenReturn(AUTH_TOKEN);
        when(organisationService.findUserByEmail(INTERVENER_TEST_EMAIL, AUTH_TOKEN)).thenReturn(Optional.of(INTERVENER_USER_ID));

        service.updateIntervenerDetails(oneWrapper1, finremCallbackRequest);

        IntervenerOneWrapper intervenerOneWrapper = finremCaseData.getIntervenerOneWrapper();

        assertNotNull(intervenerOneWrapper.getIntervenerDateAdded());
        assertNull(intervenerOneWrapper.getIntervenerSolEmail());
        assertNull(intervenerOneWrapper.getIntervenerSolName());
        assertNull(intervenerOneWrapper.getIntervenerSolPhone());
        assertNull(intervenerOneWrapper.getIntervenerSolicitorFirm());
        assertNull(intervenerOneWrapper.getIntervenerSolicitorReference());
        assertNull(intervenerOneWrapper.getIntervenerOrganisation().getOrganisation().getOrganisationID());
        assertNull(intervenerOneWrapper.getIntervenerOrganisation().getOrganisation().getOrganisationName());
        verify(assignCaseAccessService).removeCaseRoleToUser(CASE_ID, INTERVENER_USER_ID,
            INTVR_SOLICITOR_1.getValue(), SOME_ORG_ID);
    }

    @Test
    public void givenContestedCase_whenUpdatingIntervener1AndChangedRepresetationSolEmailChanged_thenHandle() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseData finremCaseData = finremCallbackRequest.getCaseDetails().getData();
        FinremCaseData finremCaseDataBefore = finremCallbackRequest.getCaseDetailsBefore().getData();
        OrganisationPolicy organisationPolicy = OrganisationPolicy.builder().organisation(
            Organisation.builder().organisationID(SOME_ORG_ID).organisationName(SOME_ORG_ID).build()
        ).build();

        IntervenerOneWrapper oneWrapper = IntervenerOneWrapper
            .builder().intervenerName("One name").intervenerEmail("test@test.com")
            .intervenerSolEmail("test@test.com")
            .intervenerSolName("test test.com")
            .intervenerSolPhone("33333333333")
            .intervenerSolicitorFirm(INTERVENER_SOL_FIRM)
            .intervenerSolicitorReference(INTERVENER_SOL_REFERENCE)
            .intervenerRepresented(YesOrNo.YES)
            .intervenerDateAdded(LocalDate.of(2023, 1, 1))
            .intervenerOrganisation(organisationPolicy).build();
        finremCaseDataBefore.setIntervenerOneWrapper(oneWrapper);

        IntervenerOneWrapper oneWrapper1 = IntervenerOneWrapper
            .builder().intervenerName("One name").intervenerEmail("test@test.com")
            .intervenerSolEmail(INTERVENER_TEST_EMAIL_CHANGE)
            .intervenerSolName("test test.com")
            .intervenerSolPhone("33333333333")
            .intervenerSolicitorFirm(INTERVENER_SOL_FIRM)
            .intervenerSolicitorReference(INTERVENER_SOL_REFERENCE)
            .intervenerRepresented(YesOrNo.YES)
            .intervenerDateAdded(LocalDate.of(2023, 1, 1))
            .intervenerOrganisation(organisationPolicy).build();
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

        service.updateIntervenerDetails(oneWrapper1, finremCallbackRequest);

        IntervenerOneWrapper intervenerOneWrapper = finremCaseData.getIntervenerOneWrapper();
        assertNotNull(intervenerOneWrapper.getIntervenerDateAdded());
        assertNotNull(intervenerOneWrapper.getIntervenerSolEmail());
        assertNotNull(intervenerOneWrapper.getIntervenerSolName());
        assertNotNull(intervenerOneWrapper.getIntervenerSolPhone());
        assertNotNull(intervenerOneWrapper.getIntervenerSolicitorFirm());
        assertNotNull(intervenerOneWrapper.getIntervenerSolicitorReference());
        assertNotNull(intervenerOneWrapper.getIntervenerOrganisation().getOrganisation().getOrganisationID());
        assertNotNull(intervenerOneWrapper.getIntervenerOrganisation().getOrganisation().getOrganisationName());

        verify(assignCaseAccessService).grantCaseRoleToUser(CASE_ID, INTERVENER_USER_ID,
            INTVR_SOLICITOR_1.getValue(), SOME_ORG_ID);
        verify(assignCaseAccessService).removeCaseRoleToUser(CASE_ID, INTERVENER_USER_ID,
            INTVR_SOLICITOR_1.getValue(), SOME_ORG_ID);
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
        IntervenerOneWrapper oneWrapper = IntervenerOneWrapper
            .builder().intervenerName("One name").intervenerEmail("test@test.com")
            .intervenerSolEmail(INTERVENER_TEST_EMAIL)
            .intervenerSolName("test test.com")
            .intervenerSolPhone("33333333333")
            .intervenerSolicitorFirm(INTERVENER_SOL_FIRM)
            .intervenerSolicitorReference(INTERVENER_SOL_REFERENCE)
            .intervenerRepresented(YesOrNo.YES)
            .intervenerDateAdded(LocalDate.of(2023, 1, 1))
            .intervenerOrganisation(organisationPolicy).build();
        finremCaseDataBefore.setIntervenerOneWrapper(oneWrapper);

        IntervenerOneWrapper oneWrapper1 = IntervenerOneWrapper
            .builder().intervenerName("One name").intervenerEmail("test@test.com")
            .intervenerSolEmail(INTERVENER_TEST_EMAIL)
            .intervenerSolName("test test.com")
            .intervenerSolPhone("33333333333")
            .intervenerSolicitorFirm(INTERVENER_SOL_FIRM)
            .intervenerSolicitorReference(INTERVENER_SOL_REFERENCE)
            .intervenerRepresented(YesOrNo.YES)
            .intervenerDateAdded(LocalDate.of(2023, 1, 1))
            .intervenerOrganisation(organisationPolicyChange).build();
        finremCaseData.setIntervenerOneWrapper(oneWrapper1);


        DynamicRadioListElement option = DynamicRadioListElement.builder().code(INTERVENER_ONE).build();
        List<DynamicRadioListElement> list = List.of(option);
        DynamicRadioList dynamicRadioList = DynamicRadioList.builder().listItems(list).build();
        finremCaseData.setIntervenersList(dynamicRadioList);
        DynamicRadioListElement option1 = DynamicRadioListElement.builder().code(INTERVENER_ONE).build();
        finremCaseData.getIntervenersList().setValue(option1);

        when(systemUserService.getSysUserToken()).thenReturn(AUTH_TOKEN);
        when(organisationService.findUserByEmail(INTERVENER_TEST_EMAIL, AUTH_TOKEN)).thenReturn(Optional.of(INTERVENER_USER_ID));

        service.updateIntervenerDetails(oneWrapper1, finremCallbackRequest);

        IntervenerOneWrapper intervenerOneWrapper = finremCaseData.getIntervenerOneWrapper();
        assertNotNull(intervenerOneWrapper.getIntervenerDateAdded());
        assertNotNull(intervenerOneWrapper.getIntervenerSolEmail());
        assertNotNull(intervenerOneWrapper.getIntervenerSolName());
        assertNotNull(intervenerOneWrapper.getIntervenerSolPhone());
        assertNotNull(intervenerOneWrapper.getIntervenerSolicitorFirm());
        assertNotNull(intervenerOneWrapper.getIntervenerSolicitorReference());
        assertNotNull(intervenerOneWrapper.getIntervenerOrganisation().getOrganisation().getOrganisationID());
        assertNotNull(intervenerOneWrapper.getIntervenerOrganisation().getOrganisation().getOrganisationName());

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
            .builder().intervenerName("One name").intervenerEmail("test@test.com")
            .intervenerRepresented(YesOrNo.NO)
            .intervenerDateAdded(LocalDate.of(2023, 1, 1))
            .intervenerOrganisation(organisationPolicy).build();
        finremCaseDataBefore.setIntervenerOneWrapper(oneWrapper);

        IntervenerOneWrapper oneWrapper1 = IntervenerOneWrapper
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
        finremCaseData.setIntervenerOneWrapper(oneWrapper1);


        DynamicRadioListElement option = DynamicRadioListElement.builder().code(INTERVENER_ONE).build();
        List<DynamicRadioListElement> list = List.of(option);
        DynamicRadioList dynamicRadioList = DynamicRadioList.builder().listItems(list).build();
        finremCaseData.setIntervenersList(dynamicRadioList);
        DynamicRadioListElement option1 = DynamicRadioListElement.builder().code(INTERVENER_ONE).build();
        finremCaseData.getIntervenersList().setValue(option1);

        when(systemUserService.getSysUserToken()).thenReturn(AUTH_TOKEN);
        when(organisationService.findUserByEmail(INTERVENER_TEST_EMAIL, AUTH_TOKEN)).thenReturn(Optional.of(INTERVENER_USER_ID));

        service.updateIntervenerDetails(oneWrapper1, finremCallbackRequest);

        IntervenerOneWrapper intervenerOneWrapper = finremCaseData.getIntervenerOneWrapper();
        assertNotNull(intervenerOneWrapper.getIntervenerSolEmail());
        assertNotNull(intervenerOneWrapper.getIntervenerSolName());
        assertNotNull(intervenerOneWrapper.getIntervenerSolicitorFirm());
        assertNotNull(intervenerOneWrapper.getIntervenerSolicitorReference());
        assertNotNull(intervenerOneWrapper.getIntervenerOrganisation().getOrganisation().getOrganisationID());
        assertNotNull(intervenerOneWrapper.getIntervenerOrganisation().getOrganisation().getOrganisationName());
        verify(assignCaseAccessService).grantCaseRoleToUser(CASE_ID, INTERVENER_USER_ID,
            INTVR_SOLICITOR_1.getValue(), SOME_ORG_ID);
    }

    @Test
    public void givenContestedCase_whenAddingIntervenerAndIntv1NotRepresent_thenSetIntvenerDateAddedAndDefaultOrg() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseData finremCaseData = finremCallbackRequest.getCaseDetails().getData();
        OrganisationPolicy organisationPolicy = OrganisationPolicy.builder().organisation(
            Organisation.builder().organisationID(null).organisationName(null).build()
        ).build();
        IntervenerOneWrapper oneWrapper = IntervenerOneWrapper
            .builder().intervenerName("One name").intervenerEmail("test@test.com")
            .intervenerRepresented(YesOrNo.NO)
            .intervenerOrganisation(organisationPolicy).build();
        finremCaseData.setIntervenerOneWrapper(oneWrapper);

        DynamicRadioListElement option = DynamicRadioListElement.builder().code(INTERVENER_ONE).build();
        List<DynamicRadioListElement> list = List.of(option);
        DynamicRadioList dynamicRadioList = DynamicRadioList.builder().listItems(list).build();
        finremCaseData.setIntervenersList(dynamicRadioList);
        DynamicRadioListElement option1 = DynamicRadioListElement.builder().code(INTERVENER_ONE).build();
        finremCaseData.getIntervenersList().setValue(option1);

        service.updateIntervenerDetails(oneWrapper, finremCallbackRequest);

        assertNotNull(finremCaseData.getIntervenerOneWrapper().getIntervenerDateAdded());
        assertNotNull(finremCaseData.getIntervenerOneWrapper().getIntervenerOrganisation().getOrgPolicyCaseAssignedRole());
    }

    @Test
    public void givenContestedCase_whenAddingIntervenerAndIntv2NotRepresent_thenSetIntvenerDateAddedAndDefaultOrg() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseData finremCaseData = finremCallbackRequest.getCaseDetails().getData();
        OrganisationPolicy organisationPolicy = OrganisationPolicy.builder().organisation(
            Organisation.builder().organisationID(null).organisationName(null).build()
        ).build();
        IntervenerTwoWrapper wrapper = IntervenerTwoWrapper
            .builder().intervenerName("Two name").intervenerEmail("test@test.com")
            .intervenerRepresented(YesOrNo.NO)
            .intervenerOrganisation(organisationPolicy).build();
        finremCaseData.setIntervenerTwoWrapper(wrapper);

        DynamicRadioListElement option = DynamicRadioListElement.builder().code(INTERVENER_TWO).build();
        List<DynamicRadioListElement> list = List.of(option);
        DynamicRadioList dynamicRadioList = DynamicRadioList.builder().listItems(list).build();
        finremCaseData.setIntervenersList(dynamicRadioList);
        DynamicRadioListElement option1 = DynamicRadioListElement.builder().code(INTERVENER_TWO).build();
        finremCaseData.getIntervenersList().setValue(option1);

        service.updateIntervenerDetails(wrapper, finremCallbackRequest);

        assertNotNull(finremCaseData.getIntervenerTwoWrapper().getIntervenerDateAdded());
        assertNotNull(finremCaseData.getIntervenerTwoWrapper().getIntervenerOrganisation().getOrgPolicyCaseAssignedRole());
    }

    @Test
    public void givenContestedCase_whenAddingIntervenerAndIntv2Represent_thenSetIntvenerDateAddedAndDefaultOrg() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseData finremCaseData = finremCallbackRequest.getCaseDetails().getData();
        OrganisationPolicy organisationPolicy = OrganisationPolicy.builder().organisation(
            Organisation.builder().organisationID(SOME_ORG_ID).organisationName(SOME_ORG_ID).build()
        ).build();
        IntervenerTwoWrapper wrapper = IntervenerTwoWrapper
            .builder().intervenerName("Two name").intervenerEmail("test@test.com")
            .intervenerSolEmail("test@test.com")
            .intervenerRepresented(YesOrNo.YES)
            .intervenerSolicitorFirm(INTERVENER_SOL_FIRM)
            .intervenerSolicitorReference(INTERVENER_SOL_REFERENCE)
            .intervenerOrganisation(organisationPolicy).build();
        finremCaseData.setIntervenerTwoWrapper(wrapper);

        DynamicRadioListElement option = DynamicRadioListElement.builder().code(INTERVENER_TWO).build();
        List<DynamicRadioListElement> list = List.of(option);
        DynamicRadioList dynamicRadioList = DynamicRadioList.builder().listItems(list).build();
        finremCaseData.setIntervenersList(dynamicRadioList);
        DynamicRadioListElement option1 = DynamicRadioListElement.builder().code(INTERVENER_TWO).build();
        finremCaseData.getIntervenersList().setValue(option1);

        when(systemUserService.getSysUserToken()).thenReturn(AUTH_TOKEN);
        when(organisationService.findUserByEmail(INTERVENER_TEST_EMAIL, AUTH_TOKEN)).thenReturn(Optional.of(INTERVENER_USER_ID));
        service.updateIntervenerDetails(wrapper, finremCallbackRequest);

        assertNotNull(finremCaseData.getIntervenerTwoWrapper().getIntervenerDateAdded());
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
            .builder().intervenerName("Two name").intervenerEmail("test@test.com")
            .intervenerSolEmail("test@test.com")
            .intervenerSolName("TwoSol name")
            .intervenerSolicitorFirm(INTERVENER_SOL_FIRM)
            .intervenerSolicitorReference(INTERVENER_SOL_REFERENCE)
            .intervenerDateAdded(LocalDate.of(2023, 1, 1))
            .intervenerRepresented(YesOrNo.YES)
            .intervenerOrganisation(organisationPolicy).build();
        finremCaseDataBefore.setIntervenerTwoWrapper(wrapper);

        IntervenerTwoWrapper current = IntervenerTwoWrapper
            .builder().intervenerName("Two name").intervenerEmail("test@test.com")
            .intervenerRepresented(YesOrNo.NO)
            .intervenerDateAdded(LocalDate.of(2023, 1, 1))
            .intervenerOrganisation(organisationPolicy).build();
        finremCaseData.setIntervenerTwoWrapper(current);

        DynamicRadioListElement option = DynamicRadioListElement.builder().code(INTERVENER_TWO).build();
        List<DynamicRadioListElement> list = List.of(option);
        DynamicRadioList dynamicRadioList = DynamicRadioList.builder().listItems(list).build();
        finremCaseData.setIntervenersList(dynamicRadioList);
        DynamicRadioListElement option1 = DynamicRadioListElement.builder().code(INTERVENER_TWO).build();
        finremCaseData.getIntervenersList().setValue(option1);

        when(systemUserService.getSysUserToken()).thenReturn(AUTH_TOKEN);
        when(organisationService.findUserByEmail(INTERVENER_TEST_EMAIL, AUTH_TOKEN)).thenReturn(Optional.of(INTERVENER_USER_ID));
        service.updateIntervenerDetails(current, finremCallbackRequest);

        IntervenerTwoWrapper intervenerTwoWrapper = finremCaseData.getIntervenerTwoWrapper();
        assertNotNull(intervenerTwoWrapper.getIntervenerDateAdded());
        assertNull(intervenerTwoWrapper.getIntervenerSolEmail());
        assertNull(intervenerTwoWrapper.getIntervenerSolName());
        assertNull(intervenerTwoWrapper.getIntervenerSolPhone());
        assertNull(intervenerTwoWrapper.getIntervenerSolicitorFirm());
        assertNull(intervenerTwoWrapper.getIntervenerSolicitorReference());
        assertNull(intervenerTwoWrapper.getIntervenerOrganisation().getOrganisation().getOrganisationID());
        assertNull(intervenerTwoWrapper.getIntervenerOrganisation().getOrganisation().getOrganisationName());
        verify(assignCaseAccessService).removeCaseRoleToUser(CASE_ID, INTERVENER_USER_ID,
            INTVR_SOLICITOR_2.getValue(), SOME_ORG_ID);
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
        IntervenerTwoWrapper wrapper = IntervenerTwoWrapper
            .builder().intervenerName("Two name").intervenerEmail("test@test.com")
            .intervenerSolEmail(INTERVENER_TEST_EMAIL)
            .intervenerSolName("TwoSol name")
            .intervenerSolPhone("11122111111")
            .intervenerSolicitorFirm(INTERVENER_SOL_FIRM)
            .intervenerSolicitorReference(INTERVENER_SOL_REFERENCE)
            .intervenerDateAdded(LocalDate.of(2023, 1, 1))
            .intervenerRepresented(YesOrNo.YES)
            .intervenerOrganisation(organisationPolicy).build();
        finremCaseDataBefore.setIntervenerTwoWrapper(wrapper);

        IntervenerTwoWrapper current = IntervenerTwoWrapper
            .builder().intervenerName("Two name").intervenerEmail("test@test.com")
            .intervenerSolEmail(INTERVENER_TEST_EMAIL)
            .intervenerSolName("TwoSol name")
            .intervenerSolPhone("11122111111")
            .intervenerSolicitorFirm(INTERVENER_SOL_FIRM)
            .intervenerSolicitorReference(INTERVENER_SOL_REFERENCE)
            .intervenerDateAdded(LocalDate.of(2023, 1, 1))
            .intervenerRepresented(YesOrNo.YES)
            .intervenerOrganisation(organisationPolicyChange).build();
        finremCaseData.setIntervenerTwoWrapper(current);

        DynamicRadioListElement option = DynamicRadioListElement.builder().code(INTERVENER_TWO).build();
        List<DynamicRadioListElement> list = List.of(option);
        DynamicRadioList dynamicRadioList = DynamicRadioList.builder().listItems(list).build();
        finremCaseData.setIntervenersList(dynamicRadioList);
        DynamicRadioListElement option1 = DynamicRadioListElement.builder().code(INTERVENER_TWO).build();
        finremCaseData.getIntervenersList().setValue(option1);

        when(systemUserService.getSysUserToken()).thenReturn(AUTH_TOKEN);
        when(organisationService.findUserByEmail(INTERVENER_TEST_EMAIL, AUTH_TOKEN)).thenReturn(Optional.of(INTERVENER_USER_ID));

        service.updateIntervenerDetails(current, finremCallbackRequest);

        IntervenerTwoWrapper intervenerTwoWrapper = finremCaseData.getIntervenerTwoWrapper();
        assertNotNull(intervenerTwoWrapper.getIntervenerDateAdded());
        assertNotNull(intervenerTwoWrapper.getIntervenerSolEmail());
        assertNotNull(intervenerTwoWrapper.getIntervenerSolName());
        assertNotNull(intervenerTwoWrapper.getIntervenerSolPhone());
        assertNotNull(intervenerTwoWrapper.getIntervenerSolicitorFirm());
        assertNotNull(intervenerTwoWrapper.getIntervenerSolicitorReference());
        assertNotNull(intervenerTwoWrapper.getIntervenerOrganisation().getOrganisation().getOrganisationID());
        assertNotNull(intervenerTwoWrapper.getIntervenerOrganisation().getOrganisation().getOrganisationName());

        verify(assignCaseAccessService).grantCaseRoleToUser(CASE_ID, INTERVENER_USER_ID,
            INTVR_SOLICITOR_2.getValue(), CHANGE_ORG_ID);
        verify(assignCaseAccessService).removeCaseRoleToUser(CASE_ID, INTERVENER_USER_ID,
            INTVR_SOLICITOR_2.getValue(), SOME_ORG_ID);
    }


    @Test
    public void givenContestedCase_whenUpdatingIntervenerAndChangedRepresetationSolEmailChanged_thenHandle() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseData finremCaseData = finremCallbackRequest.getCaseDetails().getData();
        FinremCaseData finremCaseDataBefore = finremCallbackRequest.getCaseDetailsBefore().getData();
        OrganisationPolicy organisationPolicy = OrganisationPolicy.builder().organisation(
            Organisation.builder().organisationID(SOME_ORG_ID).organisationName(SOME_ORG_ID).build()
        ).build();

        IntervenerTwoWrapper wrapper = IntervenerTwoWrapper
            .builder().intervenerName("Two name").intervenerEmail("test@test.com")
            .intervenerSolEmail("test@test.com")
            .intervenerSolName("TwoSol name")
            .intervenerSolPhone("11122111111")
            .intervenerSolicitorFirm(INTERVENER_SOL_FIRM)
            .intervenerSolicitorReference(INTERVENER_SOL_REFERENCE)
            .intervenerDateAdded(LocalDate.of(2023, 1, 1))
            .intervenerRepresented(YesOrNo.YES)
            .intervenerOrganisation(organisationPolicy).build();
        finremCaseDataBefore.setIntervenerTwoWrapper(wrapper);

        IntervenerTwoWrapper current = IntervenerTwoWrapper
            .builder().intervenerName("Two name").intervenerEmail("test@test.com")
            .intervenerSolEmail(INTERVENER_TEST_EMAIL_CHANGE)
            .intervenerSolName("TwoSol name")
            .intervenerSolPhone("11122111111")
            .intervenerSolicitorFirm(INTERVENER_SOL_FIRM)
            .intervenerSolicitorReference(INTERVENER_SOL_REFERENCE)
            .intervenerDateAdded(LocalDate.of(2023, 1, 1))
            .intervenerRepresented(YesOrNo.YES)
            .intervenerOrganisation(organisationPolicy).build();
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

        service.updateIntervenerDetails(current, finremCallbackRequest);

        IntervenerTwoWrapper intervenerTwoWrapper = finremCaseData.getIntervenerTwoWrapper();
        assertNotNull(intervenerTwoWrapper.getIntervenerDateAdded());
        assertNotNull(intervenerTwoWrapper.getIntervenerSolEmail());
        assertNotNull(intervenerTwoWrapper.getIntervenerSolName());
        assertNotNull(intervenerTwoWrapper.getIntervenerSolPhone());
        assertNotNull(intervenerTwoWrapper.getIntervenerSolicitorFirm());
        assertNotNull(intervenerTwoWrapper.getIntervenerSolicitorReference());
        assertNotNull(intervenerTwoWrapper.getIntervenerOrganisation().getOrganisation().getOrganisationID());
        assertNotNull(intervenerTwoWrapper.getIntervenerOrganisation().getOrganisation().getOrganisationName());

        verify(assignCaseAccessService).grantCaseRoleToUser(CASE_ID, INTERVENER_USER_ID,
            INTVR_SOLICITOR_2.getValue(), SOME_ORG_ID);
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
            .builder().intervenerName("Two name").intervenerEmail("test@test.com")
            .intervenerDateAdded(LocalDate.of(2023, 1, 1))
            .intervenerRepresented(YesOrNo.NO)
            .intervenerOrganisation(organisationPolicy).build();
        finremCaseDataBefore.setIntervenerTwoWrapper(wrapper);

        IntervenerTwoWrapper current = IntervenerTwoWrapper
            .builder().intervenerName("Two name").intervenerEmail("test@test.com")
            .intervenerRepresented(YesOrNo.YES)
            .intervenerDateAdded(LocalDate.of(2023, 1, 1))
            .intervenerOrganisation(organisationPolicy)
            .intervenerSolName("SOL name")
            .intervenerSolEmail("test@test.com")
            .intervenerSolPhone("112333333")
            .intervenerSolicitorFirm(INTERVENER_SOL_FIRM)
            .intervenerSolicitorReference(INTERVENER_SOL_REFERENCE).build();
        finremCaseData.setIntervenerTwoWrapper(current);

        DynamicRadioListElement option = DynamicRadioListElement.builder().code(INTERVENER_TWO).build();
        List<DynamicRadioListElement> list = List.of(option);
        DynamicRadioList dynamicRadioList = DynamicRadioList.builder().listItems(list).build();
        finremCaseData.setIntervenersList(dynamicRadioList);
        DynamicRadioListElement option1 = DynamicRadioListElement.builder().code(INTERVENER_TWO).build();
        finremCaseData.getIntervenersList().setValue(option1);

        when(systemUserService.getSysUserToken()).thenReturn(AUTH_TOKEN);
        when(organisationService.findUserByEmail(INTERVENER_TEST_EMAIL, AUTH_TOKEN)).thenReturn(Optional.of(INTERVENER_USER_ID));
        service.updateIntervenerDetails(current, finremCallbackRequest);

        IntervenerTwoWrapper intervenerTwoWrapper = finremCaseData.getIntervenerTwoWrapper();
        assertNotNull(intervenerTwoWrapper.getIntervenerSolEmail());
        assertNotNull(intervenerTwoWrapper.getIntervenerSolName());
        assertNotNull(intervenerTwoWrapper.getIntervenerSolicitorFirm());
        assertNotNull(intervenerTwoWrapper.getIntervenerSolicitorReference());
        assertNotNull(intervenerTwoWrapper.getIntervenerOrganisation().getOrganisation().getOrganisationID());
        assertNotNull(intervenerTwoWrapper.getIntervenerOrganisation().getOrganisation().getOrganisationName());
        verify(assignCaseAccessService).grantCaseRoleToUser(CASE_ID, INTERVENER_USER_ID,
            INTVR_SOLICITOR_2.getValue(), SOME_ORG_ID);
    }


    @Test
    public void givenContestedCase_whenAddingIntervenerAndIntv3NotRepresent_thenSetIntvenerDateAddedAndDefaultOrg() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseData finremCaseData = finremCallbackRequest.getCaseDetails().getData();
        OrganisationPolicy organisationPolicy = OrganisationPolicy.builder().organisation(
            Organisation.builder().organisationID(null).organisationName(null).build()
        ).build();
        IntervenerThreeWrapper wrapper = IntervenerThreeWrapper
            .builder().intervenerName("Two name").intervenerEmail("test@test.com")
            .intervenerRepresented(YesOrNo.NO)
            .intervenerOrganisation(organisationPolicy).build();
        finremCaseData.setIntervenerThreeWrapper(wrapper);

        DynamicRadioListElement option = DynamicRadioListElement.builder().code(INTERVENER_THREE).build();
        List<DynamicRadioListElement> list = List.of(option);
        DynamicRadioList dynamicRadioList = DynamicRadioList.builder().listItems(list).build();
        finremCaseData.setIntervenersList(dynamicRadioList);
        DynamicRadioListElement option1 = DynamicRadioListElement.builder().code(INTERVENER_THREE).build();
        finremCaseData.getIntervenersList().setValue(option1);

        service.updateIntervenerDetails(wrapper, finremCallbackRequest);

        assertNotNull(finremCaseData.getIntervenerThreeWrapper().getIntervenerDateAdded());
        assertNotNull(finremCaseData.getIntervenerThreeWrapper().getIntervenerOrganisation().getOrgPolicyCaseAssignedRole());
    }

    @Test
    public void givenContestedCase_whenAddingIntervenerAndIntv3Represent_thenSetIntvenerDateAddedAndDefaultOrg() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseData finremCaseData = finremCallbackRequest.getCaseDetails().getData();
        OrganisationPolicy organisationPolicy = OrganisationPolicy.builder().organisation(
            Organisation.builder().organisationID(SOME_ORG_ID).organisationName(SOME_ORG_ID).build()
        ).build();
        IntervenerThreeWrapper wrapper = IntervenerThreeWrapper
            .builder().intervenerName("Two name").intervenerEmail("test@test.com")
            .intervenerSolEmail("test@test.com")
            .intervenerRepresented(YesOrNo.YES)
            .intervenerSolicitorFirm(INTERVENER_SOL_FIRM)
            .intervenerSolicitorReference(INTERVENER_SOL_REFERENCE)
            .intervenerOrganisation(organisationPolicy).build();
        finremCaseData.setIntervenerThreeWrapper(wrapper);

        DynamicRadioListElement option = DynamicRadioListElement.builder().code(INTERVENER_THREE).build();
        List<DynamicRadioListElement> list = List.of(option);
        DynamicRadioList dynamicRadioList = DynamicRadioList.builder().listItems(list).build();
        finremCaseData.setIntervenersList(dynamicRadioList);
        DynamicRadioListElement option1 = DynamicRadioListElement.builder().code(INTERVENER_THREE).build();
        finremCaseData.getIntervenersList().setValue(option1);

        when(systemUserService.getSysUserToken()).thenReturn(AUTH_TOKEN);
        when(organisationService.findUserByEmail(INTERVENER_TEST_EMAIL, AUTH_TOKEN)).thenReturn(Optional.of(INTERVENER_USER_ID));

        service.updateIntervenerDetails(wrapper, finremCallbackRequest);

        assertNotNull(finremCaseData.getIntervenerThreeWrapper().getIntervenerDateAdded());
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
            .builder().intervenerName("Two name").intervenerEmail("test@test.com")
            .intervenerSolEmail("test@test.com")
            .intervenerSolName("three man")
            .intervenerSolPhone("999999999")
            .intervenerSolicitorFirm(INTERVENER_SOL_FIRM)
            .intervenerSolicitorReference(INTERVENER_SOL_REFERENCE)
            .intervenerDateAdded(LocalDate.of(2023, 1, 1))
            .intervenerRepresented(YesOrNo.YES)
            .intervenerOrganisation(organisationPolicy).build();
        finremCaseDataBefore.setIntervenerThreeWrapper(wrapper);

        IntervenerThreeWrapper current = IntervenerThreeWrapper
            .builder().intervenerName("Two name").intervenerEmail("test@test.com")
            .intervenerDateAdded(LocalDate.of(2023, 1, 1))
            .intervenerRepresented(YesOrNo.NO)
            .intervenerOrganisation(organisationPolicy).build();
        finremCaseData.setIntervenerThreeWrapper(current);

        DynamicRadioListElement option = DynamicRadioListElement.builder().code(INTERVENER_THREE).build();
        List<DynamicRadioListElement> list = List.of(option);
        DynamicRadioList dynamicRadioList = DynamicRadioList.builder().listItems(list).build();
        finremCaseData.setIntervenersList(dynamicRadioList);
        DynamicRadioListElement option1 = DynamicRadioListElement.builder().code(INTERVENER_THREE).build();
        finremCaseData.getIntervenersList().setValue(option1);

        when(systemUserService.getSysUserToken()).thenReturn(AUTH_TOKEN);
        when(organisationService.findUserByEmail(INTERVENER_TEST_EMAIL, AUTH_TOKEN)).thenReturn(Optional.of(INTERVENER_USER_ID));

        service.updateIntervenerDetails(current, finremCallbackRequest);

        IntervenerThreeWrapper intervenerThreeWrapper = finremCaseData.getIntervenerThreeWrapper();
        assertNotNull(intervenerThreeWrapper.getIntervenerDateAdded());
        assertNull(intervenerThreeWrapper.getIntervenerSolEmail());
        assertNull(intervenerThreeWrapper.getIntervenerSolName());
        assertNull(intervenerThreeWrapper.getIntervenerSolPhone());
        assertNull(intervenerThreeWrapper.getIntervenerSolicitorFirm());
        assertNull(intervenerThreeWrapper.getIntervenerSolicitorReference());
        assertNull(intervenerThreeWrapper.getIntervenerOrganisation().getOrganisation().getOrganisationID());
        assertNull(intervenerThreeWrapper.getIntervenerOrganisation().getOrganisation().getOrganisationName());
        verify(assignCaseAccessService).removeCaseRoleToUser(CASE_ID, INTERVENER_USER_ID,
            INTVR_SOLICITOR_3.getValue(), SOME_ORG_ID);
    }

    @Test
    public void givenContestedCase_whenUpdatingIntervener3AndChangedRepresetationSolEmailChanges_thenHandle() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseData finremCaseData = finremCallbackRequest.getCaseDetails().getData();
        FinremCaseData finremCaseDataBefore = finremCallbackRequest.getCaseDetailsBefore().getData();
        OrganisationPolicy organisationPolicy = OrganisationPolicy.builder().organisation(
            Organisation.builder().organisationID(SOME_ORG_ID).organisationName(SOME_ORG_ID).build()
        ).build();

        IntervenerThreeWrapper wrapper = IntervenerThreeWrapper
            .builder().intervenerName("Two name").intervenerEmail("test@test.com")
            .intervenerSolEmail(INTERVENER_TEST_EMAIL)
            .intervenerSolName("three man")
            .intervenerSolPhone("999999999")
            .intervenerSolicitorFirm(INTERVENER_SOL_FIRM)
            .intervenerSolicitorReference(INTERVENER_SOL_REFERENCE)
            .intervenerDateAdded(LocalDate.of(2023, 1, 1))
            .intervenerRepresented(YesOrNo.YES)
            .intervenerOrganisation(organisationPolicy).build();
        finremCaseDataBefore.setIntervenerThreeWrapper(wrapper);

        IntervenerThreeWrapper current = IntervenerThreeWrapper
            .builder().intervenerName("Two name").intervenerEmail("test@test.com")
            .intervenerSolEmail(INTERVENER_TEST_EMAIL_CHANGE)
            .intervenerSolName("three man")
            .intervenerSolPhone("999999999")
            .intervenerSolicitorFirm(INTERVENER_SOL_FIRM)
            .intervenerSolicitorReference(INTERVENER_SOL_REFERENCE)
            .intervenerDateAdded(LocalDate.of(2023, 1, 1))
            .intervenerRepresented(YesOrNo.YES)
            .intervenerOrganisation(organisationPolicy).build();
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

        service.updateIntervenerDetails(current, finremCallbackRequest);

        IntervenerThreeWrapper intervenerThreeWrapper = finremCaseData.getIntervenerThreeWrapper();
        assertNotNull(intervenerThreeWrapper.getIntervenerDateAdded());
        assertNotNull(intervenerThreeWrapper.getIntervenerSolEmail());
        assertNotNull(intervenerThreeWrapper.getIntervenerSolName());
        assertNotNull(intervenerThreeWrapper.getIntervenerSolPhone());
        assertNotNull(intervenerThreeWrapper.getIntervenerSolicitorFirm());
        assertNotNull(intervenerThreeWrapper.getIntervenerSolicitorReference());
        assertNotNull(intervenerThreeWrapper.getIntervenerOrganisation().getOrganisation().getOrganisationID());
        assertNotNull(intervenerThreeWrapper.getIntervenerOrganisation().getOrganisation().getOrganisationName());

        verify(assignCaseAccessService).grantCaseRoleToUser(CASE_ID, INTERVENER_USER_ID,
            INTVR_SOLICITOR_3.getValue(), SOME_ORG_ID);
        verify(assignCaseAccessService).removeCaseRoleToUser(CASE_ID, INTERVENER_USER_ID,
            INTVR_SOLICITOR_3.getValue(), SOME_ORG_ID);
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
        IntervenerThreeWrapper wrapper = IntervenerThreeWrapper
            .builder().intervenerName("Two name").intervenerEmail("test@test.com")
            .intervenerSolEmail(INTERVENER_TEST_EMAIL)
            .intervenerSolName("three man")
            .intervenerSolPhone("999999999")
            .intervenerSolicitorFirm(INTERVENER_SOL_FIRM)
            .intervenerSolicitorReference(INTERVENER_SOL_REFERENCE)
            .intervenerDateAdded(LocalDate.of(2023, 1, 1))
            .intervenerRepresented(YesOrNo.YES)
            .intervenerOrganisation(organisationPolicy).build();
        finremCaseDataBefore.setIntervenerThreeWrapper(wrapper);

        IntervenerThreeWrapper current = IntervenerThreeWrapper
            .builder().intervenerName("Two name").intervenerEmail("test@test.com")
            .intervenerSolEmail(INTERVENER_TEST_EMAIL)
            .intervenerSolName("three man")
            .intervenerSolPhone("999999999")
            .intervenerSolicitorFirm(INTERVENER_SOL_FIRM)
            .intervenerSolicitorReference(INTERVENER_SOL_REFERENCE)
            .intervenerDateAdded(LocalDate.of(2023, 1, 1))
            .intervenerRepresented(YesOrNo.YES)
            .intervenerOrganisation(organisationPolicyChange).build();
        finremCaseData.setIntervenerThreeWrapper(current);

        DynamicRadioListElement option = DynamicRadioListElement.builder().code(INTERVENER_THREE).build();
        List<DynamicRadioListElement> list = List.of(option);
        DynamicRadioList dynamicRadioList = DynamicRadioList.builder().listItems(list).build();
        finremCaseData.setIntervenersList(dynamicRadioList);
        DynamicRadioListElement option1 = DynamicRadioListElement.builder().code(INTERVENER_THREE).build();
        finremCaseData.getIntervenersList().setValue(option1);

        when(systemUserService.getSysUserToken()).thenReturn(AUTH_TOKEN);
        when(organisationService.findUserByEmail(INTERVENER_TEST_EMAIL, AUTH_TOKEN)).thenReturn(Optional.of(INTERVENER_USER_ID));

        service.updateIntervenerDetails(current, finremCallbackRequest);

        IntervenerThreeWrapper intervenerThreeWrapper = finremCaseData.getIntervenerThreeWrapper();
        assertNotNull(intervenerThreeWrapper.getIntervenerDateAdded());
        assertNotNull(intervenerThreeWrapper.getIntervenerSolEmail());
        assertNotNull(intervenerThreeWrapper.getIntervenerSolName());
        assertNotNull(intervenerThreeWrapper.getIntervenerSolPhone());
        assertNotNull(intervenerThreeWrapper.getIntervenerSolicitorFirm());
        assertNotNull(intervenerThreeWrapper.getIntervenerSolicitorReference());
        assertNotNull(intervenerThreeWrapper.getIntervenerOrganisation().getOrganisation().getOrganisationID());
        assertNotNull(intervenerThreeWrapper.getIntervenerOrganisation().getOrganisation().getOrganisationName());

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
            .builder().intervenerName("Two name").intervenerEmail("test@test.com")
            .intervenerDateAdded(LocalDate.of(2023, 1, 1))
            .intervenerRepresented(YesOrNo.NO)
            .intervenerOrganisation(organisationPolicy).build();
        finremCaseDataBefore.setIntervenerThreeWrapper(wrapper);

        IntervenerThreeWrapper current = IntervenerThreeWrapper
            .builder().intervenerName("Two name").intervenerEmail("test@test.com")
            .intervenerDateAdded(LocalDate.of(2023, 1, 1))
            .intervenerSolEmail("test@test.com")
            .intervenerSolName("three man")
            .intervenerSolPhone("999999999")
            .intervenerSolicitorFirm(INTERVENER_SOL_FIRM)
            .intervenerSolicitorReference(INTERVENER_SOL_REFERENCE)
            .intervenerRepresented(YesOrNo.YES)
            .intervenerOrganisation(organisationPolicy).build();
        finremCaseData.setIntervenerThreeWrapper(current);

        DynamicRadioListElement option = DynamicRadioListElement.builder().code(INTERVENER_THREE).build();
        List<DynamicRadioListElement> list = List.of(option);
        DynamicRadioList dynamicRadioList = DynamicRadioList.builder().listItems(list).build();
        finremCaseData.setIntervenersList(dynamicRadioList);
        DynamicRadioListElement option1 = DynamicRadioListElement.builder().code(INTERVENER_THREE).build();
        finremCaseData.getIntervenersList().setValue(option1);

        when(systemUserService.getSysUserToken()).thenReturn(AUTH_TOKEN);
        when(organisationService.findUserByEmail(INTERVENER_TEST_EMAIL, AUTH_TOKEN)).thenReturn(Optional.of(INTERVENER_USER_ID));

        service.updateIntervenerDetails(current, finremCallbackRequest);

        IntervenerThreeWrapper intervenerThreeWrapper = finremCaseData.getIntervenerThreeWrapper();
        assertNotNull(intervenerThreeWrapper.getIntervenerSolEmail());
        assertNotNull(intervenerThreeWrapper.getIntervenerSolName());
        assertNotNull(intervenerThreeWrapper.getIntervenerSolicitorFirm());
        assertNotNull(intervenerThreeWrapper.getIntervenerSolicitorReference());
        assertNotNull(intervenerThreeWrapper.getIntervenerOrganisation().getOrganisation().getOrganisationID());
        assertNotNull(intervenerThreeWrapper.getIntervenerOrganisation().getOrganisation().getOrganisationName());
        verify(assignCaseAccessService).grantCaseRoleToUser(CASE_ID, INTERVENER_USER_ID,
            INTVR_SOLICITOR_3.getValue(), SOME_ORG_ID);
    }

    @Test
    public void givenContestedCase_whenAddingIntervenerAndIntv4NotRepresent_thenSetIntvenerDateAddedAndDefaultOrg() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseData finremCaseData = finremCallbackRequest.getCaseDetails().getData();
        OrganisationPolicy organisationPolicy = OrganisationPolicy.builder().organisation(
            Organisation.builder().organisationID(null).organisationName(null).build()
        ).build();
        IntervenerFourWrapper wrapper = IntervenerFourWrapper
            .builder().intervenerName("Two name").intervenerEmail("test@test.com")
            .intervenerRepresented(YesOrNo.NO)
            .intervenerOrganisation(organisationPolicy).build();
        finremCaseData.setIntervenerFourWrapper(wrapper);

        DynamicRadioListElement option = DynamicRadioListElement.builder().code(INTERVENER_FOUR).build();
        List<DynamicRadioListElement> list = List.of(option);
        DynamicRadioList dynamicRadioList = DynamicRadioList.builder().listItems(list).build();
        finremCaseData.setIntervenersList(dynamicRadioList);
        DynamicRadioListElement option1 = DynamicRadioListElement.builder().code(INTERVENER_FOUR).build();
        finremCaseData.getIntervenersList().setValue(option1);

        service.updateIntervenerDetails(wrapper, finremCallbackRequest);

        assertNotNull(finremCaseData.getIntervenerFourWrapper().getIntervenerDateAdded());
        assertNotNull(finremCaseData.getIntervenerFourWrapper().getIntervenerOrganisation().getOrgPolicyCaseAssignedRole());
    }

    @Test
    public void givenContestedCase_whenAddingIntervenerAndIntv4Represent_thenSetIntvenerDateAddedAndDefaultOrg() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseData finremCaseData = finremCallbackRequest.getCaseDetails().getData();
        OrganisationPolicy organisationPolicy = OrganisationPolicy.builder().organisation(
            Organisation.builder().organisationID(SOME_ORG_ID).organisationName(SOME_ORG_ID).build()
        ).build();
        IntervenerFourWrapper wrapper = IntervenerFourWrapper
            .builder().intervenerName("Two name").intervenerEmail("test@test.com")
            .intervenerRepresented(YesOrNo.YES)
            .intervenerSolEmail("test@test.com")
            .intervenerSolicitorFirm(INTERVENER_SOL_FIRM)
            .intervenerSolicitorReference(INTERVENER_SOL_REFERENCE)
            .intervenerOrganisation(organisationPolicy).build();
        finremCaseData.setIntervenerFourWrapper(wrapper);

        DynamicRadioListElement option = DynamicRadioListElement.builder().code(INTERVENER_FOUR).build();
        List<DynamicRadioListElement> list = List.of(option);
        DynamicRadioList dynamicRadioList = DynamicRadioList.builder().listItems(list).build();
        finremCaseData.setIntervenersList(dynamicRadioList);
        DynamicRadioListElement option1 = DynamicRadioListElement.builder().code(INTERVENER_FOUR).build();
        finremCaseData.getIntervenersList().setValue(option1);

        when(systemUserService.getSysUserToken()).thenReturn(AUTH_TOKEN);
        when(organisationService.findUserByEmail(INTERVENER_TEST_EMAIL, AUTH_TOKEN)).thenReturn(Optional.of(INTERVENER_USER_ID));

        service.updateIntervenerDetails(wrapper, finremCallbackRequest);

        assertNotNull(finremCaseData.getIntervenerFourWrapper().getIntervenerDateAdded());
        verify(assignCaseAccessService).grantCaseRoleToUser(CASE_ID, INTERVENER_USER_ID,
            INTVR_SOLICITOR_4.getValue(), SOME_ORG_ID);
    }

    @Test
    public void givenContestedCase_whenUpdatingIntervenerAndChangedRepresetationYesToNo_thenHandle() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseData finremCaseData = finremCallbackRequest.getCaseDetails().getData();
        FinremCaseData finremCaseDataBefore = finremCallbackRequest.getCaseDetailsBefore().getData();
        OrganisationPolicy organisationPolicy = OrganisationPolicy.builder().organisation(
            Organisation.builder().organisationID(SOME_ORG_ID).organisationName(SOME_ORG_ID).build()
        ).build();
        IntervenerFourWrapper wrapper = IntervenerFourWrapper
            .builder().intervenerName("Two name").intervenerEmail("test@test.com")
            .intervenerRepresented(YesOrNo.YES)
            .intervenerDateAdded(LocalDate.of(2023, 1, 1))
            .intervenerSolEmail("test@test.com")
            .intervenerSolName("tes test")
            .intervenerSolicitorFirm(INTERVENER_SOL_FIRM)
            .intervenerSolicitorReference(INTERVENER_SOL_REFERENCE)
            .intervenerOrganisation(organisationPolicy).build();
        finremCaseDataBefore.setIntervenerFourWrapper(wrapper);

        IntervenerFourWrapper current = IntervenerFourWrapper
            .builder().intervenerName("Two name").intervenerEmail("test@test.com")
            .intervenerRepresented(YesOrNo.NO)
            .intervenerDateAdded(LocalDate.of(2023, 1, 1))
            .intervenerOrganisation(organisationPolicy).build();
        finremCaseData.setIntervenerFourWrapper(current);

        DynamicRadioListElement option = DynamicRadioListElement.builder().code(INTERVENER_FOUR).build();
        List<DynamicRadioListElement> list = List.of(option);
        DynamicRadioList dynamicRadioList = DynamicRadioList.builder().listItems(list).build();
        finremCaseData.setIntervenersList(dynamicRadioList);
        DynamicRadioListElement option1 = DynamicRadioListElement.builder().code(INTERVENER_FOUR).build();
        finremCaseData.getIntervenersList().setValue(option1);

        when(systemUserService.getSysUserToken()).thenReturn(AUTH_TOKEN);
        when(organisationService.findUserByEmail(INTERVENER_TEST_EMAIL, AUTH_TOKEN)).thenReturn(Optional.of(INTERVENER_USER_ID));

        service.updateIntervenerDetails(current, finremCallbackRequest);

        IntervenerFourWrapper intervenerFourWrapper = finremCaseData.getIntervenerFourWrapper();
        assertNotNull(intervenerFourWrapper.getIntervenerDateAdded());
        assertNull(intervenerFourWrapper.getIntervenerSolEmail());
        assertNull(intervenerFourWrapper.getIntervenerSolName());
        assertNull(intervenerFourWrapper.getIntervenerSolPhone());
        assertNull(intervenerFourWrapper.getIntervenerSolicitorFirm());
        assertNull(intervenerFourWrapper.getIntervenerSolicitorReference());
        assertNull(intervenerFourWrapper.getIntervenerOrganisation().getOrganisation().getOrganisationID());
        assertNull(intervenerFourWrapper.getIntervenerOrganisation().getOrganisation().getOrganisationName());

        verify(assignCaseAccessService).removeCaseRoleToUser(CASE_ID, INTERVENER_USER_ID,
            INTVR_SOLICITOR_4.getValue(), SOME_ORG_ID);
    }

    @Test
    public void givenContestedCase_whenUpdatingIntervenerAndChangedRepresetationNoToYes_thenHandle() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseData finremCaseData = finremCallbackRequest.getCaseDetails().getData();
        FinremCaseData finremCaseDataBefore = finremCallbackRequest.getCaseDetailsBefore().getData();
        OrganisationPolicy organisationPolicy = OrganisationPolicy.builder().organisation(
            Organisation.builder().organisationID(SOME_ORG_ID).organisationName(SOME_ORG_ID).build()
        ).build();
        IntervenerFourWrapper wrapper = IntervenerFourWrapper
            .builder().intervenerName("Two name").intervenerEmail("test@test.com")
            .intervenerRepresented(YesOrNo.NO)
            .intervenerDateAdded(LocalDate.of(2023, 1, 1))
            .intervenerOrganisation(organisationPolicy).build();
        finremCaseDataBefore.setIntervenerFourWrapper(wrapper);

        IntervenerFourWrapper current = IntervenerFourWrapper
            .builder().intervenerName("Two name").intervenerEmail("test@test.com")
            .intervenerRepresented(YesOrNo.YES)
            .intervenerSolEmail("test@test.com")
            .intervenerSolName("tes test")
            .intervenerSolicitorFirm(INTERVENER_SOL_FIRM)
            .intervenerSolicitorReference(INTERVENER_SOL_REFERENCE)
            .intervenerDateAdded(LocalDate.of(2023, 1, 1))
            .intervenerOrganisation(organisationPolicy).build();
        finremCaseData.setIntervenerFourWrapper(current);

        DynamicRadioListElement option = DynamicRadioListElement.builder().code(INTERVENER_FOUR).build();
        List<DynamicRadioListElement> list = List.of(option);
        DynamicRadioList dynamicRadioList = DynamicRadioList.builder().listItems(list).build();
        finremCaseData.setIntervenersList(dynamicRadioList);
        DynamicRadioListElement option1 = DynamicRadioListElement.builder().code(INTERVENER_FOUR).build();
        finremCaseData.getIntervenersList().setValue(option1);

        when(systemUserService.getSysUserToken()).thenReturn(AUTH_TOKEN);
        when(organisationService.findUserByEmail(INTERVENER_TEST_EMAIL, AUTH_TOKEN)).thenReturn(Optional.of(INTERVENER_USER_ID));

        service.updateIntervenerDetails(current, finremCallbackRequest);

        IntervenerFourWrapper intervenerFourWrapper = finremCaseData.getIntervenerFourWrapper();
        assertNotNull(intervenerFourWrapper.getIntervenerDateAdded());
        assertNotNull(intervenerFourWrapper.getIntervenerSolEmail());
        assertNotNull(intervenerFourWrapper.getIntervenerSolName());
        assertNotNull(intervenerFourWrapper.getIntervenerSolicitorFirm());
        assertNotNull(intervenerFourWrapper.getIntervenerSolicitorReference());
        assertNotNull(intervenerFourWrapper.getIntervenerOrganisation().getOrganisation().getOrganisationID());
        assertNotNull(intervenerFourWrapper.getIntervenerOrganisation().getOrganisation().getOrganisationName());
        verify(assignCaseAccessService).grantCaseRoleToUser(CASE_ID, INTERVENER_USER_ID,
            INTVR_SOLICITOR_4.getValue(), SOME_ORG_ID);
    }

    @Test
    public void givenContestedCase_whenUpdatingIntervenerAndChangedRepresetationSolEmailChanges_thenHandle() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseData finremCaseData = finremCallbackRequest.getCaseDetails().getData();
        FinremCaseData finremCaseDataBefore = finremCallbackRequest.getCaseDetailsBefore().getData();
        OrganisationPolicy organisationPolicy = OrganisationPolicy.builder().organisation(
            Organisation.builder().organisationID(SOME_ORG_ID).organisationName(SOME_ORG_ID).build()
        ).build();

        IntervenerFourWrapper wrapper = IntervenerFourWrapper
            .builder().intervenerName("Two name").intervenerEmail("test@test.com")
            .intervenerRepresented(YesOrNo.YES)
            .intervenerDateAdded(LocalDate.of(2023, 1, 1))
            .intervenerSolName("tes test")
            .intervenerSolPhone("122222222222")
            .intervenerSolicitorFirm(INTERVENER_SOL_FIRM)
            .intervenerSolicitorReference(INTERVENER_SOL_REFERENCE)
            .intervenerSolEmail("test@test.com")
            .intervenerOrganisation(organisationPolicy).build();
        finremCaseDataBefore.setIntervenerFourWrapper(wrapper);

        IntervenerFourWrapper current = IntervenerFourWrapper
            .builder().intervenerName("Two name").intervenerEmail("test@test.com")
            .intervenerRepresented(YesOrNo.YES)
            .intervenerDateAdded(LocalDate.of(2023, 1, 1))
            .intervenerSolName("tes test")
            .intervenerSolPhone("122222222222")
            .intervenerSolicitorFirm(INTERVENER_SOL_FIRM)
            .intervenerSolicitorReference(INTERVENER_SOL_REFERENCE)
            .intervenerSolEmail(INTERVENER_TEST_EMAIL_CHANGE)
            .intervenerOrganisation(organisationPolicy).build();
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

        service.updateIntervenerDetails(current, finremCallbackRequest);

        IntervenerFourWrapper intervenerFourWrapper = finremCaseData.getIntervenerFourWrapper();
        assertNotNull(intervenerFourWrapper.getIntervenerDateAdded());
        assertNotNull(intervenerFourWrapper.getIntervenerSolEmail());
        assertNotNull(intervenerFourWrapper.getIntervenerSolName());
        assertNotNull(intervenerFourWrapper.getIntervenerSolPhone());
        assertNotNull(intervenerFourWrapper.getIntervenerSolicitorFirm());
        assertNotNull(intervenerFourWrapper.getIntervenerSolicitorReference());
        assertNotNull(intervenerFourWrapper.getIntervenerOrganisation().getOrganisation().getOrganisationID());
        assertNotNull(intervenerFourWrapper.getIntervenerOrganisation().getOrganisation().getOrganisationName());

        verify(assignCaseAccessService).grantCaseRoleToUser(CASE_ID, INTERVENER_USER_ID,
            INTVR_SOLICITOR_4.getValue(), SOME_ORG_ID);
        verify(assignCaseAccessService).removeCaseRoleToUser(CASE_ID, INTERVENER_USER_ID,
            INTVR_SOLICITOR_4.getValue(), SOME_ORG_ID);
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

        IntervenerFourWrapper wrapper = IntervenerFourWrapper
            .builder().intervenerName("Two name").intervenerEmail("test@test.com")
            .intervenerRepresented(YesOrNo.YES)
            .intervenerDateAdded(LocalDate.of(2023, 1, 1))
            .intervenerSolName("tes test")
            .intervenerSolPhone("122222222222")
            .intervenerSolicitorFirm(INTERVENER_SOL_FIRM)
            .intervenerSolicitorReference(INTERVENER_SOL_REFERENCE)
            .intervenerSolEmail(INTERVENER_TEST_EMAIL)
            .intervenerOrganisation(organisationPolicy).build();
        finremCaseDataBefore.setIntervenerFourWrapper(wrapper);

        IntervenerFourWrapper current = IntervenerFourWrapper
            .builder().intervenerName("Two name").intervenerEmail("test@test.com")
            .intervenerRepresented(YesOrNo.YES)
            .intervenerDateAdded(LocalDate.of(2023, 1, 1))
            .intervenerSolName("tes test")
            .intervenerSolPhone("122222222222")
            .intervenerSolicitorFirm(INTERVENER_SOL_FIRM)
            .intervenerSolicitorReference(INTERVENER_SOL_REFERENCE)
            .intervenerSolEmail(INTERVENER_TEST_EMAIL)
            .intervenerOrganisation(organisationPolicyChange).build();
        finremCaseData.setIntervenerFourWrapper(current);

        DynamicRadioListElement option = DynamicRadioListElement.builder().code(INTERVENER_FOUR).build();
        List<DynamicRadioListElement> list = List.of(option);
        DynamicRadioList dynamicRadioList = DynamicRadioList.builder().listItems(list).build();
        finremCaseData.setIntervenersList(dynamicRadioList);
        DynamicRadioListElement option1 = DynamicRadioListElement.builder().code(INTERVENER_FOUR).build();
        finremCaseData.getIntervenersList().setValue(option1);

        when(systemUserService.getSysUserToken()).thenReturn(AUTH_TOKEN);
        when(organisationService.findUserByEmail(INTERVENER_TEST_EMAIL, AUTH_TOKEN)).thenReturn(Optional.of(INTERVENER_USER_ID));

        service.updateIntervenerDetails(current, finremCallbackRequest);

        IntervenerFourWrapper intervenerFourWrapper = finremCaseData.getIntervenerFourWrapper();
        assertNotNull(intervenerFourWrapper.getIntervenerDateAdded());
        assertNotNull(intervenerFourWrapper.getIntervenerSolEmail());
        assertNotNull(intervenerFourWrapper.getIntervenerSolName());
        assertNotNull(intervenerFourWrapper.getIntervenerSolPhone());
        assertNotNull(intervenerFourWrapper.getIntervenerSolicitorFirm());
        assertNotNull(intervenerFourWrapper.getIntervenerSolicitorReference());
        assertNotNull(intervenerFourWrapper.getIntervenerOrganisation().getOrganisation().getOrganisationID());
        assertNotNull(intervenerFourWrapper.getIntervenerOrganisation().getOrganisation().getOrganisationName());

        verify(assignCaseAccessService).grantCaseRoleToUser(CASE_ID, INTERVENER_USER_ID,
            INTVR_SOLICITOR_4.getValue(), CHANGE_ORG_ID);
        verify(assignCaseAccessService).removeCaseRoleToUser(CASE_ID, INTERVENER_USER_ID,
            INTVR_SOLICITOR_4.getValue(), SOME_ORG_ID);
    }

    @Test
    public void givenContestedCase_whenAddingIntervenerWithNonRegisterEmail_thenHandlerThrowException() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseData finremCaseData = finremCallbackRequest.getCaseDetails().getData();
        OrganisationPolicy organisationPolicy = OrganisationPolicy.builder().organisation(
            Organisation.builder().organisationID(SOME_ORG_ID).organisationName(SOME_ORG_ID).build()
        ).build();
        IntervenerFourWrapper wrapper = IntervenerFourWrapper
            .builder().intervenerName("Two name").intervenerEmail(INTERVENER_TEST_EMAIL)
            .intervenerRepresented(YesOrNo.YES)
            .intervenerSolEmail(INTERVENER_TEST_EMAIL)
            .intervenerOrganisation(organisationPolicy).build();
        finremCaseData.setIntervenerFourWrapper(wrapper);

        DynamicRadioListElement option = DynamicRadioListElement.builder().code(INTERVENER_FOUR).build();
        List<DynamicRadioListElement> list = List.of(option);
        DynamicRadioList dynamicRadioList = DynamicRadioList.builder().listItems(list).build();
        finremCaseData.setIntervenersList(dynamicRadioList);
        DynamicRadioListElement option1 = DynamicRadioListElement.builder().code(INTERVENER_FOUR).build();
        finremCaseData.getIntervenersList().setValue(option1);

        when(systemUserService.getSysUserToken()).thenReturn(AUTH_TOKEN);
        Throwable exception = assertThrows(NoSuchUserException.class,
            () -> service.updateIntervenerDetails(wrapper, finremCallbackRequest));
        assertEquals("expecting exception to throw when user not found in am",
            "Could not find the user with email " + INTERVENER_TEST_EMAIL, exception.getMessage());
    }

    @Test
    public void whenCalled_setIntervenerChangeDetailsForIntervenerOneOnAdding() {
        IntervenerOneWrapper intervenerOneWrapper = IntervenerOneWrapper.builder()
            .intervenerName("Intervener One")
            .build();

        IntervenerChangeDetails result = service.setIntervenerAddedChangeDetails(intervenerOneWrapper);
        Assert.assertEquals(IntervenerAction.ADDED.toString(), result.getIntervenerAction().toString());
        Assert.assertEquals(IntervenerType.INTERVENER_ONE.toString(), result.getIntervenerType().toString());
    }

    @Test
    public void whenCalled_setIntervenerChangeDetailsForIntervenerTwoOnAdding() {
        IntervenerTwoWrapper intervenerTwoWrapper = IntervenerTwoWrapper.builder()
            .intervenerName("Intervener Two")
            .build();

        IntervenerChangeDetails result = service.setIntervenerAddedChangeDetails(intervenerTwoWrapper);
        Assert.assertEquals(IntervenerAction.ADDED.toString(), result.getIntervenerAction().toString());
        Assert.assertEquals(IntervenerType.INTERVENER_TWO.toString(), result.getIntervenerType().toString());
    }

    @Test
    public void whenCalled_setIntervenerChangeDetailsForIntervenerThreeOnAdding() {
        IntervenerThreeWrapper intervenerThreeWrapper = IntervenerThreeWrapper.builder()
            .intervenerName("Intervener Three")
            .build();

        IntervenerChangeDetails result = service.setIntervenerAddedChangeDetails(intervenerThreeWrapper);
        Assert.assertEquals(IntervenerAction.ADDED.toString(), result.getIntervenerAction().toString());
        Assert.assertEquals(IntervenerType.INTERVENER_THREE.toString(), result.getIntervenerType().toString());
    }

    @Test
    public void whenCalled_setIntervenerChangeDetailsForIntervenerFourOnAdding() {
        IntervenerFourWrapper intervenerFourWrapper = IntervenerFourWrapper.builder()
            .intervenerName("Intervener Four")
            .build();

        IntervenerChangeDetails result = service.setIntervenerAddedChangeDetails(intervenerFourWrapper);
        Assert.assertEquals(IntervenerAction.ADDED.toString(), result.getIntervenerAction().toString());
        Assert.assertEquals(IntervenerType.INTERVENER_FOUR.toString(), result.getIntervenerType().toString());
    }

    @Test
    public void whenCalled_setIntervenerChangeDetailsForIntervenerOnRemoval() {
        IntervenerWrapper intervenerWrapper = IntervenerOneWrapper.builder()
            .intervenerName("Intervener One")
            .build();

        IntervenerChangeDetails result = service.setIntervenerRemovedChangeDetails(intervenerWrapper);
        Assert.assertEquals(IntervenerAction.REMOVED.toString(), result.getIntervenerAction().toString());
        Assert.assertEquals(IntervenerType.INTERVENER_ONE.toString(), result.getIntervenerType().toString());
    }

    @Test
    public void whenIntervenerOneSolicitorRemoved_ShouldReturnTrue() {
        IntervenerOneWrapper intervenerOneWrapperBefore = new IntervenerOneWrapper();
        intervenerOneWrapperBefore.setIntervenerRepresented(YesOrNo.YES);
        IntervenerOneWrapper intervenerOneWrapper = new IntervenerOneWrapper();
        intervenerOneWrapper.setIntervenerRepresented(YesOrNo.NO);
        FinremCaseData finremCaseData = FinremCaseData.builder()
            .intervenerOneWrapper(intervenerOneWrapper)
            .build();
        FinremCaseData finremCaseDataBefore = FinremCaseData.builder()
            .intervenerOneWrapper(intervenerOneWrapperBefore)
            .build();
        Assert.assertTrue(service.checkIfAnyIntervenerSolicitorRemoved(finremCaseData, finremCaseDataBefore));
    }

    @Test
    public void whenIntervenerTwoSolicitorRemoved_ShouldReturnTrue() {
        IntervenerTwoWrapper intervenerTwoWrapperBefore = new IntervenerTwoWrapper();
        intervenerTwoWrapperBefore.setIntervenerRepresented(YesOrNo.YES);
        IntervenerTwoWrapper intervenerTwoWrapper = new IntervenerTwoWrapper();
        intervenerTwoWrapper.setIntervenerRepresented(YesOrNo.NO);
        FinremCaseData finremCaseData = FinremCaseData.builder()
            .intervenerTwoWrapper(intervenerTwoWrapper)
            .build();
        FinremCaseData finremCaseDataBefore = FinremCaseData.builder()
            .intervenerTwoWrapper(intervenerTwoWrapperBefore)
            .build();
        Assert.assertTrue(service.checkIfAnyIntervenerSolicitorRemoved(finremCaseData, finremCaseDataBefore));
    }

    @Test
    public void whenIntervenerThreeSolicitorRemoved_ShouldReturnTrue() {
        IntervenerThreeWrapper intervenerThreeWrapperBefore = new IntervenerThreeWrapper();
        intervenerThreeWrapperBefore.setIntervenerRepresented(YesOrNo.YES);
        IntervenerThreeWrapper intervenerThreeWrapper = new IntervenerThreeWrapper();
        intervenerThreeWrapper.setIntervenerRepresented(YesOrNo.NO);
        FinremCaseData finremCaseData = FinremCaseData.builder()
            .intervenerThreeWrapper(intervenerThreeWrapper)
            .build();
        FinremCaseData finremCaseDataBefore = FinremCaseData.builder()
            .intervenerThreeWrapper(intervenerThreeWrapperBefore)
            .build();
        Assert.assertTrue(service.checkIfAnyIntervenerSolicitorRemoved(finremCaseData, finremCaseDataBefore));
    }

    @Test
    public void whenIntervenerFourSolicitorRemoved_ShouldReturnTrue() {
        IntervenerFourWrapper intervenerFourWrapperBefore = new IntervenerFourWrapper();
        intervenerFourWrapperBefore.setIntervenerRepresented(YesOrNo.YES);
        IntervenerFourWrapper intervenerFourWrapper = new IntervenerFourWrapper();
        intervenerFourWrapper.setIntervenerRepresented(YesOrNo.NO);
        FinremCaseData finremCaseData = FinremCaseData.builder()
            .intervenerFourWrapper(intervenerFourWrapper)
            .build();
        FinremCaseData finremCaseDataBefore = FinremCaseData.builder()
            .intervenerFourWrapper(intervenerFourWrapperBefore)
            .build();
        Assert.assertTrue(service.checkIfAnyIntervenerSolicitorRemoved(finremCaseData, finremCaseDataBefore));
    }

    @Test
    public void whenNoIntervenerSolicitorRemoved_ShouldReturnFalse() {
        FinremCaseData finremCaseData = FinremCaseData.builder().build();
        FinremCaseData finremCaseDataBefore = FinremCaseData.builder().build();
        Assert.assertFalse(service.checkIfAnyIntervenerSolicitorRemoved(finremCaseData, finremCaseDataBefore));
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
