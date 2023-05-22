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
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.intervener.IntervenerFourToIntervenerDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.intervener.IntervenerOneToIntervenerDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.intervener.IntervenerThreeToIntervenerDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.intervener.IntervenerTwoToIntervenerDetailsMapper;
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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerAction;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerChangeDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerDetails;
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
    @Mock
    private IntervenerOneToIntervenerDetailsMapper intervenerOneToIntervenerDetailsMapper;
    @Mock
    private IntervenerTwoToIntervenerDetailsMapper intervenerTwoToIntervenerDetailsMapper;
    @Mock
    private IntervenerThreeToIntervenerDetailsMapper intervenerThreeToIntervenerDetailsMapper;
    @Mock
    private IntervenerFourToIntervenerDetailsMapper intervenerFourToIntervenerDetailsMapper;
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
            .intervener1SolicitorFirm(INTERVENER_SOL_FIRM)
            .intervener1SolicitorReference(INTERVENER_SOL_REFERENCE)
            .intervener1Organisation(organisationPolicy)
            .intervener1Represented(YesOrNo.YES).build();
        finremCaseData.setIntervenerOneWrapper(oneWrapper);

        when(systemUserService.getSysUserToken()).thenReturn(AUTH_TOKEN);
        when(organisationService.findUserByEmail(INTERVENER_TEST_EMAIL, AUTH_TOKEN)).thenReturn(Optional.of(INTERVENER_USER_ID));
        service.removeIntervenerOneDetails(finremCaseData, CASE_ID);

        assertNull(finremCaseData.getIntervenerOneWrapper().getIntervener1Name());
        assertNull(finremCaseData.getIntervenerOneWrapper().getIntervener1Email());
        assertNull(finremCaseData.getIntervenerOneWrapper().getIntervener1Phone());
        assertNull(finremCaseData.getIntervenerOneWrapper().getIntervener1SolicitorFirm());
        assertNull(finremCaseData.getIntervenerOneWrapper().getIntervener1SolicitorReference());
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
        assertNull(finremCaseData.getIntervenerTwoWrapper().getIntervener2SolicitorFirm());
        assertNull(finremCaseData.getIntervenerTwoWrapper().getIntervener2SolicitorReference());
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
            .intervener2SolicitorFirm(INTERVENER_SOL_FIRM)
            .intervener2SolicitorReference(INTERVENER_SOL_REFERENCE)
            .intervener2Organisation(organisationPolicy)
            .intervener2Represented(YesOrNo.YES).build();
        finremCaseData.setIntervenerTwoWrapper(twoWrapper);

        when(systemUserService.getSysUserToken()).thenReturn(AUTH_TOKEN);
        when(organisationService.findUserByEmail(INTERVENER_TEST_EMAIL, AUTH_TOKEN)).thenReturn(Optional.of(INTERVENER_USER_ID));
        service.removeIntervenerTwoDetails(finremCaseData, CASE_ID);

        assertNull(finremCaseData.getIntervenerTwoWrapper().getIntervener2Name());
        assertNull(finremCaseData.getIntervenerTwoWrapper().getIntervener2Email());
        assertNull(finremCaseData.getIntervenerTwoWrapper().getIntervener2SolicitorFirm());
        assertNull(finremCaseData.getIntervenerTwoWrapper().getIntervener2SolicitorReference());
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
        assertNull(finremCaseData.getIntervenerThreeWrapper().getIntervener3SolicitorFirm());
        assertNull(finremCaseData.getIntervenerThreeWrapper().getIntervener3SolicitorReference());

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
            .intervener3SolicitorFirm(INTERVENER_SOL_FIRM)
            .intervener3SolicitorReference(INTERVENER_SOL_REFERENCE)
            .intervener3Represented(YesOrNo.YES)
            .intervener3Organisation(organisationPolicy)
            .build();
        finremCaseData.setIntervenerThreeWrapper(threeWrapper);

        when(systemUserService.getSysUserToken()).thenReturn(AUTH_TOKEN);
        when(organisationService.findUserByEmail(INTERVENER_TEST_EMAIL, AUTH_TOKEN)).thenReturn(Optional.of(INTERVENER_USER_ID));
        service.removeIntervenerThreeDetails(finremCaseData, CASE_ID);

        assertNull(finremCaseData.getIntervenerThreeWrapper().getIntervener3Name());
        assertNull(finremCaseData.getIntervenerThreeWrapper().getIntervener3Email());
        assertNull(finremCaseData.getIntervenerThreeWrapper().getIntervener3SolicitorFirm());
        assertNull(finremCaseData.getIntervenerThreeWrapper().getIntervener3SolicitorReference());
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
        assertNull(finremCaseData.getIntervenerFourWrapper().getIntervener4SolicitorFirm());
        assertNull(finremCaseData.getIntervenerFourWrapper().getIntervener4SolicitorReference());
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
            .intervener4SolicitorFirm(INTERVENER_SOL_FIRM)
            .intervener4SolicitorReference(INTERVENER_SOL_REFERENCE)
            .intervener4Organisation(organisationPolicy)
            .build();
        finremCaseData.setIntervenerFourWrapper(fourWrapper);

        when(systemUserService.getSysUserToken()).thenReturn(AUTH_TOKEN);
        when(organisationService.findUserByEmail(INTERVENER_TEST_EMAIL, AUTH_TOKEN)).thenReturn(Optional.of(INTERVENER_USER_ID));
        service.removeIntervenerFourDetails(finremCaseData, CASE_ID);

        assertNull(finremCaseData.getIntervenerFourWrapper().getIntervener4Name());
        assertNull(finremCaseData.getIntervenerFourWrapper().getIntervener4Email());
        assertNull(finremCaseData.getIntervenerFourWrapper().getIntervener4SolicitorFirm());
        assertNull(finremCaseData.getIntervenerFourWrapper().getIntervener4SolicitorReference());
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
            .intervener1SolicitorFirm(INTERVENER_SOL_FIRM)
            .intervener1SolicitorReference(INTERVENER_SOL_REFERENCE)
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

        service.updateIntervenerOneDetails(finremCallbackRequest);

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
            .intervener1SolicitorFirm(INTERVENER_SOL_FIRM)
            .intervener1SolicitorReference(INTERVENER_SOL_REFERENCE)
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

        service.updateIntervenerOneDetails(finremCallbackRequest);

        IntervenerOneWrapper intervenerOneWrapper = finremCaseData.getIntervenerOneWrapper();

        assertNotNull(intervenerOneWrapper.getIntervener1DateAdded());
        assertNull(intervenerOneWrapper.getIntervener1SolEmail());
        assertNull(intervenerOneWrapper.getIntervener1SolName());
        assertNull(intervenerOneWrapper.getIntervener1SolPhone());
        assertNull(intervenerOneWrapper.getIntervener1SolicitorFirm());
        assertNull(intervenerOneWrapper.getIntervener1SolicitorReference());
        assertNull(intervenerOneWrapper.getIntervener1Organisation().getOrganisation().getOrganisationID());
        assertNull(intervenerOneWrapper.getIntervener1Organisation().getOrganisation().getOrganisationName());
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
            .builder().intervener1Name("One name").intervener1Email("test@test.com")
            .intervener1SolEmail("test@test.com")
            .intervener1SolName("test test.com")
            .intervener1SolPhone("33333333333")
            .intervener1SolicitorFirm(INTERVENER_SOL_FIRM)
            .intervener1SolicitorReference(INTERVENER_SOL_REFERENCE)
            .intervener1Represented(YesOrNo.YES)
            .intervener1DateAdded(LocalDate.of(2023,1,1))
            .intervener1Organisation(organisationPolicy).build();
        finremCaseDataBefore.setIntervenerOneWrapper(oneWrapper);

        IntervenerOneWrapper oneWrapper1 = IntervenerOneWrapper
            .builder().intervener1Name("One name").intervener1Email("test@test.com")
            .intervener1SolEmail(INTERVENER_TEST_EMAIL_CHANGE)
            .intervener1SolName("test test.com")
            .intervener1SolPhone("33333333333")
            .intervener1SolicitorFirm(INTERVENER_SOL_FIRM)
            .intervener1SolicitorReference(INTERVENER_SOL_REFERENCE)
            .intervener1Represented(YesOrNo.YES)
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
        when(organisationService.findUserByEmail(INTERVENER_TEST_EMAIL_CHANGE, AUTH_TOKEN)).thenReturn(Optional.of(INTERVENER_USER_ID));

        service.updateIntervenerOneDetails(finremCallbackRequest);

        IntervenerOneWrapper intervenerOneWrapper = finremCaseData.getIntervenerOneWrapper();
        assertNotNull(intervenerOneWrapper.getIntervener1DateAdded());
        assertNotNull(intervenerOneWrapper.getIntervener1SolEmail());
        assertNotNull(intervenerOneWrapper.getIntervener1SolName());
        assertNotNull(intervenerOneWrapper.getIntervener1SolPhone());
        assertNotNull(intervenerOneWrapper.getIntervener1SolicitorFirm());
        assertNotNull(intervenerOneWrapper.getIntervener1SolicitorReference());
        assertNotNull(intervenerOneWrapper.getIntervener1Organisation().getOrganisation().getOrganisationID());
        assertNotNull(intervenerOneWrapper.getIntervener1Organisation().getOrganisation().getOrganisationName());

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
            .builder().intervener1Name("One name").intervener1Email("test@test.com")
            .intervener1SolEmail(INTERVENER_TEST_EMAIL)
            .intervener1SolName("test test.com")
            .intervener1SolPhone("33333333333")
            .intervener1SolicitorFirm(INTERVENER_SOL_FIRM)
            .intervener1SolicitorReference(INTERVENER_SOL_REFERENCE)
            .intervener1Represented(YesOrNo.YES)
            .intervener1DateAdded(LocalDate.of(2023,1,1))
            .intervener1Organisation(organisationPolicy).build();
        finremCaseDataBefore.setIntervenerOneWrapper(oneWrapper);

        IntervenerOneWrapper oneWrapper1 = IntervenerOneWrapper
            .builder().intervener1Name("One name").intervener1Email("test@test.com")
            .intervener1SolEmail(INTERVENER_TEST_EMAIL)
            .intervener1SolName("test test.com")
            .intervener1SolPhone("33333333333")
            .intervener1SolicitorFirm(INTERVENER_SOL_FIRM)
            .intervener1SolicitorReference(INTERVENER_SOL_REFERENCE)
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

        service.updateIntervenerOneDetails(finremCallbackRequest);

        IntervenerOneWrapper intervenerOneWrapper = finremCaseData.getIntervenerOneWrapper();
        assertNotNull(intervenerOneWrapper.getIntervener1DateAdded());
        assertNotNull(intervenerOneWrapper.getIntervener1SolEmail());
        assertNotNull(intervenerOneWrapper.getIntervener1SolName());
        assertNotNull(intervenerOneWrapper.getIntervener1SolPhone());
        assertNotNull(intervenerOneWrapper.getIntervener1SolicitorFirm());
        assertNotNull(intervenerOneWrapper.getIntervener1SolicitorReference());
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
            .intervener1SolicitorFirm(INTERVENER_SOL_FIRM)
            .intervener1SolicitorReference(INTERVENER_SOL_REFERENCE)
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

        service.updateIntervenerOneDetails(finremCallbackRequest);

        IntervenerOneWrapper intervenerOneWrapper = finremCaseData.getIntervenerOneWrapper();
        assertNotNull(intervenerOneWrapper.getIntervener1SolEmail());
        assertNotNull(intervenerOneWrapper.getIntervener1SolName());
        assertNotNull(intervenerOneWrapper.getIntervener1SolicitorFirm());
        assertNotNull(intervenerOneWrapper.getIntervener1SolicitorReference());
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

        service.updateIntervenerOneDetails(finremCallbackRequest);

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

        service.updateIntervenerTwoDetails(finremCallbackRequest);

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
            .intervener2SolicitorFirm(INTERVENER_SOL_FIRM)
            .intervener2SolicitorReference(INTERVENER_SOL_REFERENCE)
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
        service.updateIntervenerTwoDetails(finremCallbackRequest);

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
            .intervener2SolicitorFirm(INTERVENER_SOL_FIRM)
            .intervener2SolicitorReference(INTERVENER_SOL_REFERENCE)
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
        service.updateIntervenerTwoDetails(finremCallbackRequest);

        IntervenerTwoWrapper intervenerTwoWrapper = finremCaseData.getIntervenerTwoWrapper();
        assertNotNull(intervenerTwoWrapper.getIntervener2DateAdded());
        assertNull(intervenerTwoWrapper.getIntervener2SolEmail());
        assertNull(intervenerTwoWrapper.getIntervener2SolName());
        assertNull(intervenerTwoWrapper.getIntervener2SolPhone());
        assertNull(intervenerTwoWrapper.getIntervener2SolicitorFirm());
        assertNull(intervenerTwoWrapper.getIntervener2SolicitorReference());
        assertNull(intervenerTwoWrapper.getIntervener2Organisation().getOrganisation().getOrganisationID());
        assertNull(intervenerTwoWrapper.getIntervener2Organisation().getOrganisation().getOrganisationName());
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
            .builder().intervener2Name("Two name").intervener2Email("test@test.com")
            .intervener2SolEmail(INTERVENER_TEST_EMAIL)
            .intervener2SolName("TwoSol name")
            .intervener2SolPhone("11122111111")
            .intervener2SolicitorFirm(INTERVENER_SOL_FIRM)
            .intervener2SolicitorReference(INTERVENER_SOL_REFERENCE)
            .intervener2DateAdded(LocalDate.of(2023,1,1))
            .intervener2Represented(YesOrNo.YES)
            .intervener2Organisation(organisationPolicy).build();
        finremCaseDataBefore.setIntervenerTwoWrapper(wrapper);

        IntervenerTwoWrapper current = IntervenerTwoWrapper
            .builder().intervener2Name("Two name").intervener2Email("test@test.com")
            .intervener2SolEmail(INTERVENER_TEST_EMAIL)
            .intervener2SolName("TwoSol name")
            .intervener2SolPhone("11122111111")
            .intervener2SolicitorFirm(INTERVENER_SOL_FIRM)
            .intervener2SolicitorReference(INTERVENER_SOL_REFERENCE)
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

        service.updateIntervenerTwoDetails(finremCallbackRequest);

        IntervenerTwoWrapper intervenerTwoWrapper = finremCaseData.getIntervenerTwoWrapper();
        assertNotNull(intervenerTwoWrapper.getIntervener2DateAdded());
        assertNotNull(intervenerTwoWrapper.getIntervener2SolEmail());
        assertNotNull(intervenerTwoWrapper.getIntervener2SolName());
        assertNotNull(intervenerTwoWrapper.getIntervener2SolPhone());
        assertNotNull(intervenerTwoWrapper.getIntervener2SolicitorFirm());
        assertNotNull(intervenerTwoWrapper.getIntervener2SolicitorReference());
        assertNotNull(intervenerTwoWrapper.getIntervener2Organisation().getOrganisation().getOrganisationID());
        assertNotNull(intervenerTwoWrapper.getIntervener2Organisation().getOrganisation().getOrganisationName());

        verify(assignCaseAccessService).grantCaseRoleToUser(CASE_ID, INTERVENER_USER_ID,
            INTVR_SOLICITOR_2.getValue(), CHANGE_ORG_ID);
        verify(assignCaseAccessService).removeCaseRoleToUser(CASE_ID, INTERVENER_USER_ID,
            INTVR_SOLICITOR_2.getValue(), SOME_ORG_ID);
    }


    @Test
    public void givenContestedCase_whenUpdatingIntervener2AndChangedRepresetationSolEmailChanged_thenHandle() {
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
            .intervener2SolPhone("11122111111")
            .intervener2SolicitorFirm(INTERVENER_SOL_FIRM)
            .intervener2SolicitorReference(INTERVENER_SOL_REFERENCE)
            .intervener2DateAdded(LocalDate.of(2023,1,1))
            .intervener2Represented(YesOrNo.YES)
            .intervener2Organisation(organisationPolicy).build();
        finremCaseDataBefore.setIntervenerTwoWrapper(wrapper);

        IntervenerTwoWrapper current = IntervenerTwoWrapper
            .builder().intervener2Name("Two name").intervener2Email("test@test.com")
            .intervener2SolEmail(INTERVENER_TEST_EMAIL_CHANGE)
            .intervener2SolName("TwoSol name")
            .intervener2SolPhone("11122111111")
            .intervener2SolicitorFirm(INTERVENER_SOL_FIRM)
            .intervener2SolicitorReference(INTERVENER_SOL_REFERENCE)
            .intervener2DateAdded(LocalDate.of(2023,1,1))
            .intervener2Represented(YesOrNo.YES)
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
        when(organisationService.findUserByEmail(INTERVENER_TEST_EMAIL_CHANGE, AUTH_TOKEN)).thenReturn(Optional.of(INTERVENER_USER_ID));

        service.updateIntervenerTwoDetails(finremCallbackRequest);

        IntervenerTwoWrapper intervenerTwoWrapper = finremCaseData.getIntervenerTwoWrapper();
        assertNotNull(intervenerTwoWrapper.getIntervener2DateAdded());
        assertNotNull(intervenerTwoWrapper.getIntervener2SolEmail());
        assertNotNull(intervenerTwoWrapper.getIntervener2SolName());
        assertNotNull(intervenerTwoWrapper.getIntervener2SolPhone());
        assertNotNull(intervenerTwoWrapper.getIntervener2SolicitorFirm());
        assertNotNull(intervenerTwoWrapper.getIntervener2SolicitorReference());
        assertNotNull(intervenerTwoWrapper.getIntervener2Organisation().getOrganisation().getOrganisationID());
        assertNotNull(intervenerTwoWrapper.getIntervener2Organisation().getOrganisation().getOrganisationName());

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
            .intervener2SolPhone("112333333")
            .intervener2SolicitorFirm(INTERVENER_SOL_FIRM)
            .intervener2SolicitorReference(INTERVENER_SOL_REFERENCE).build();
        finremCaseData.setIntervenerTwoWrapper(current);

        DynamicRadioListElement option = DynamicRadioListElement.builder().code(INTERVENER_TWO).build();
        List<DynamicRadioListElement> list = List.of(option);
        DynamicRadioList dynamicRadioList = DynamicRadioList.builder().listItems(list).build();
        finremCaseData.setIntervenersList(dynamicRadioList);
        DynamicRadioListElement option1 = DynamicRadioListElement.builder().code(INTERVENER_TWO).build();
        finremCaseData.getIntervenersList().setValue(option1);

        when(systemUserService.getSysUserToken()).thenReturn(AUTH_TOKEN);
        when(organisationService.findUserByEmail(INTERVENER_TEST_EMAIL, AUTH_TOKEN)).thenReturn(Optional.of(INTERVENER_USER_ID));
        service.updateIntervenerTwoDetails(finremCallbackRequest);

        IntervenerTwoWrapper intervenerTwoWrapper = finremCaseData.getIntervenerTwoWrapper();
        assertNotNull(intervenerTwoWrapper.getIntervener2SolEmail());
        assertNotNull(intervenerTwoWrapper.getIntervener2SolName());
        assertNotNull(intervenerTwoWrapper.getIntervener2SolicitorFirm());
        assertNotNull(intervenerTwoWrapper.getIntervener2SolicitorReference());
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

        service.updateIntervenerThreeDetails(finremCallbackRequest);

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
            .intervener3SolicitorFirm(INTERVENER_SOL_FIRM)
            .intervener3SolicitorReference(INTERVENER_SOL_REFERENCE)
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

        service.updateIntervenerThreeDetails(finremCallbackRequest);

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
            .intervener3SolicitorFirm(INTERVENER_SOL_FIRM)
            .intervener3SolicitorReference(INTERVENER_SOL_REFERENCE)
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

        service.updateIntervenerThreeDetails(finremCallbackRequest);

        IntervenerThreeWrapper intervenerThreeWrapper = finremCaseData.getIntervenerThreeWrapper();
        assertNotNull(intervenerThreeWrapper.getIntervener3DateAdded());
        assertNull(intervenerThreeWrapper.getIntervener3SolEmail());
        assertNull(intervenerThreeWrapper.getIntervener3SolName());
        assertNull(intervenerThreeWrapper.getIntervener3SolPhone());
        assertNull(intervenerThreeWrapper.getIntervener3SolicitorFirm());
        assertNull(intervenerThreeWrapper.getIntervener3SolicitorReference());
        assertNull(intervenerThreeWrapper.getIntervener3Organisation().getOrganisation().getOrganisationID());
        assertNull(intervenerThreeWrapper.getIntervener3Organisation().getOrganisation().getOrganisationName());
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
            .builder().intervener3Name("Two name").intervener3Email("test@test.com")
            .intervener3SolEmail(INTERVENER_TEST_EMAIL)
            .intervener3SolName("three man")
            .intervener3SolPhone("999999999")
            .intervener3SolicitorFirm(INTERVENER_SOL_FIRM)
            .intervener3SolicitorReference(INTERVENER_SOL_REFERENCE)
            .intervener3DateAdded(LocalDate.of(2023,1,1))
            .intervener3Represented(YesOrNo.YES)
            .intervener3Organisation(organisationPolicy).build();
        finremCaseDataBefore.setIntervenerThreeWrapper(wrapper);

        IntervenerThreeWrapper current = IntervenerThreeWrapper
            .builder().intervener3Name("Two name").intervener3Email("test@test.com")
            .intervener3SolEmail(INTERVENER_TEST_EMAIL_CHANGE)
            .intervener3SolName("three man")
            .intervener3SolPhone("999999999")
            .intervener3SolicitorFirm(INTERVENER_SOL_FIRM)
            .intervener3SolicitorReference(INTERVENER_SOL_REFERENCE)
            .intervener3DateAdded(LocalDate.of(2023,1,1))
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
        when(organisationService.findUserByEmail(INTERVENER_TEST_EMAIL_CHANGE, AUTH_TOKEN)).thenReturn(Optional.of(INTERVENER_USER_ID));

        service.updateIntervenerThreeDetails(finremCallbackRequest);

        IntervenerThreeWrapper intervenerThreeWrapper = finremCaseData.getIntervenerThreeWrapper();
        assertNotNull(intervenerThreeWrapper.getIntervener3DateAdded());
        assertNotNull(intervenerThreeWrapper.getIntervener3SolEmail());
        assertNotNull(intervenerThreeWrapper.getIntervener3SolName());
        assertNotNull(intervenerThreeWrapper.getIntervener3SolPhone());
        assertNotNull(intervenerThreeWrapper.getIntervener3SolicitorFirm());
        assertNotNull(intervenerThreeWrapper.getIntervener3SolicitorReference());
        assertNotNull(intervenerThreeWrapper.getIntervener3Organisation().getOrganisation().getOrganisationID());
        assertNotNull(intervenerThreeWrapper.getIntervener3Organisation().getOrganisation().getOrganisationName());

        verify(assignCaseAccessService).grantCaseRoleToUser(CASE_ID, INTERVENER_USER_ID,
            INTVR_SOLICITOR_3.getValue(), SOME_ORG_ID);
        verify(assignCaseAccessService).removeCaseRoleToUser(CASE_ID, INTERVENER_USER_ID,
            INTVR_SOLICITOR_3.getValue(), SOME_ORG_ID);
    }

    @Test
    public void givenContestedCase_whenUpdatingIntervener3AndChangedRepresetationSolOrgChanges_thenHandle() {
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
            .intervener3SolEmail(INTERVENER_TEST_EMAIL)
            .intervener3SolName("three man")
            .intervener3SolPhone("999999999")
            .intervener3SolicitorFirm(INTERVENER_SOL_FIRM)
            .intervener3SolicitorReference(INTERVENER_SOL_REFERENCE)
            .intervener3DateAdded(LocalDate.of(2023,1,1))
            .intervener3Represented(YesOrNo.YES)
            .intervener3Organisation(organisationPolicy).build();
        finremCaseDataBefore.setIntervenerThreeWrapper(wrapper);

        IntervenerThreeWrapper current = IntervenerThreeWrapper
            .builder().intervener3Name("Two name").intervener3Email("test@test.com")
            .intervener3SolEmail(INTERVENER_TEST_EMAIL)
            .intervener3SolName("three man")
            .intervener3SolPhone("999999999")
            .intervener3SolicitorFirm(INTERVENER_SOL_FIRM)
            .intervener3SolicitorReference(INTERVENER_SOL_REFERENCE)
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

        service.updateIntervenerThreeDetails(finremCallbackRequest);

        IntervenerThreeWrapper intervenerThreeWrapper = finremCaseData.getIntervenerThreeWrapper();
        assertNotNull(intervenerThreeWrapper.getIntervener3DateAdded());
        assertNotNull(intervenerThreeWrapper.getIntervener3SolEmail());
        assertNotNull(intervenerThreeWrapper.getIntervener3SolName());
        assertNotNull(intervenerThreeWrapper.getIntervener3SolPhone());
        assertNotNull(intervenerThreeWrapper.getIntervener3SolicitorFirm());
        assertNotNull(intervenerThreeWrapper.getIntervener3SolicitorReference());
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
            .intervener3SolicitorFirm(INTERVENER_SOL_FIRM)
            .intervener3SolicitorReference(INTERVENER_SOL_REFERENCE)
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

        service.updateIntervenerThreeDetails(finremCallbackRequest);

        IntervenerThreeWrapper intervenerThreeWrapper = finremCaseData.getIntervenerThreeWrapper();
        assertNotNull(intervenerThreeWrapper.getIntervener3SolEmail());
        assertNotNull(intervenerThreeWrapper.getIntervener3SolName());
        assertNotNull(intervenerThreeWrapper.getIntervener3SolicitorFirm());
        assertNotNull(intervenerThreeWrapper.getIntervener3SolicitorReference());
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

        service.updateIntervenerFourDetails(finremCallbackRequest);

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
            .intervener4SolicitorFirm(INTERVENER_SOL_FIRM)
            .intervener4SolicitorReference(INTERVENER_SOL_REFERENCE)
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

        service.updateIntervenerFourDetails(finremCallbackRequest);

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
            .intervener4SolicitorFirm(INTERVENER_SOL_FIRM)
            .intervener4SolicitorReference(INTERVENER_SOL_REFERENCE)
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

        service.updateIntervenerFourDetails(finremCallbackRequest);

        IntervenerFourWrapper intervenerFourWrapper = finremCaseData.getIntervenerFourWrapper();
        assertNotNull(intervenerFourWrapper.getIntervener4DateAdded());
        assertNull(intervenerFourWrapper.getIntervener4SolEmail());
        assertNull(intervenerFourWrapper.getIntervener4SolName());
        assertNull(intervenerFourWrapper.getIntervener4SolPhone());
        assertNull(intervenerFourWrapper.getIntervener4SolicitorFirm());
        assertNull(intervenerFourWrapper.getIntervener4SolicitorReference());
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
            .intervener4SolicitorFirm(INTERVENER_SOL_FIRM)
            .intervener4SolicitorReference(INTERVENER_SOL_REFERENCE)
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

        service.updateIntervenerFourDetails(finremCallbackRequest);

        IntervenerFourWrapper intervenerFourWrapper = finremCaseData.getIntervenerFourWrapper();
        assertNotNull(intervenerFourWrapper.getIntervener4DateAdded());
        assertNotNull(intervenerFourWrapper.getIntervener4SolEmail());
        assertNotNull(intervenerFourWrapper.getIntervener4SolName());
        assertNotNull(intervenerFourWrapper.getIntervener4SolicitorFirm());
        assertNotNull(intervenerFourWrapper.getIntervener4SolicitorReference());
        assertNotNull(intervenerFourWrapper.getIntervener4Organisation().getOrganisation().getOrganisationID());
        assertNotNull(intervenerFourWrapper.getIntervener4Organisation().getOrganisation().getOrganisationName());
        verify(assignCaseAccessService).grantCaseRoleToUser(CASE_ID, INTERVENER_USER_ID,
            INTVR_SOLICITOR_4.getValue(), SOME_ORG_ID);
    }

    @Test
    public void givenContestedCase_whenUpdatingIntervener4AndChangedRepresetationSolEmailChanges_thenHandle() {
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
            .intervener4SolName("tes test")
            .intervener4SolPhone("122222222222")
            .intervener4SolicitorFirm(INTERVENER_SOL_FIRM)
            .intervener4SolicitorReference(INTERVENER_SOL_REFERENCE)
            .intervener4SolEmail("test@test.com")
            .intervener4Organisation(organisationPolicy).build();
        finremCaseDataBefore.setIntervenerFourWrapper(wrapper);

        IntervenerFourWrapper current = IntervenerFourWrapper
            .builder().intervener4Name("Two name").intervener4Email("test@test.com")
            .intervener4Represented(YesOrNo.YES)
            .intervener4DateAdded(LocalDate.of(2023,1,1))
            .intervener4SolName("tes test")
            .intervener4SolPhone("122222222222")
            .intervener4SolicitorFirm(INTERVENER_SOL_FIRM)
            .intervener4SolicitorReference(INTERVENER_SOL_REFERENCE)
            .intervener4SolEmail(INTERVENER_TEST_EMAIL_CHANGE)
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
        when(organisationService.findUserByEmail(INTERVENER_TEST_EMAIL_CHANGE, AUTH_TOKEN)).thenReturn(Optional.of(INTERVENER_USER_ID));

        service.updateIntervenerFourDetails(finremCallbackRequest);

        IntervenerFourWrapper intervenerFourWrapper = finremCaseData.getIntervenerFourWrapper();
        assertNotNull(intervenerFourWrapper.getIntervener4DateAdded());
        assertNotNull(intervenerFourWrapper.getIntervener4SolEmail());
        assertNotNull(intervenerFourWrapper.getIntervener4SolName());
        assertNotNull(intervenerFourWrapper.getIntervener4SolPhone());
        assertNotNull(intervenerFourWrapper.getIntervener4SolicitorFirm());
        assertNotNull(intervenerFourWrapper.getIntervener4SolicitorReference());
        assertNotNull(intervenerFourWrapper.getIntervener4Organisation().getOrganisation().getOrganisationID());
        assertNotNull(intervenerFourWrapper.getIntervener4Organisation().getOrganisation().getOrganisationName());

        verify(assignCaseAccessService).grantCaseRoleToUser(CASE_ID, INTERVENER_USER_ID,
            INTVR_SOLICITOR_4.getValue(), SOME_ORG_ID);
        verify(assignCaseAccessService).removeCaseRoleToUser(CASE_ID, INTERVENER_USER_ID,
            INTVR_SOLICITOR_4.getValue(), SOME_ORG_ID);
    }

    @Test
    public void givenContestedCase_whenUpdatingIntervener4AndChangedRepresetationSolOrgChanged_thenHandle() {
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
            .intervener4SolicitorFirm(INTERVENER_SOL_FIRM)
            .intervener4SolicitorReference(INTERVENER_SOL_REFERENCE)
            .intervener4SolEmail(INTERVENER_TEST_EMAIL)
            .intervener4Organisation(organisationPolicy).build();
        finremCaseDataBefore.setIntervenerFourWrapper(wrapper);

        IntervenerFourWrapper current = IntervenerFourWrapper
            .builder().intervener4Name("Two name").intervener4Email("test@test.com")
            .intervener4Represented(YesOrNo.YES)
            .intervener4DateAdded(LocalDate.of(2023,1,1))
            .intervener4SolName("tes test")
            .intervener4SolPhone("122222222222")
            .intervener4SolicitorFirm(INTERVENER_SOL_FIRM)
            .intervener4SolicitorReference(INTERVENER_SOL_REFERENCE)
            .intervener4SolEmail(INTERVENER_TEST_EMAIL)
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

        service.updateIntervenerFourDetails(finremCallbackRequest);

        IntervenerFourWrapper intervenerFourWrapper = finremCaseData.getIntervenerFourWrapper();
        assertNotNull(intervenerFourWrapper.getIntervener4DateAdded());
        assertNotNull(intervenerFourWrapper.getIntervener4SolEmail());
        assertNotNull(intervenerFourWrapper.getIntervener4SolName());
        assertNotNull(intervenerFourWrapper.getIntervener4SolPhone());
        assertNotNull(intervenerFourWrapper.getIntervener4SolicitorFirm());
        assertNotNull(intervenerFourWrapper.getIntervener4SolicitorReference());
        assertNotNull(intervenerFourWrapper.getIntervener4Organisation().getOrganisation().getOrganisationID());
        assertNotNull(intervenerFourWrapper.getIntervener4Organisation().getOrganisation().getOrganisationName());

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
            () -> service.updateIntervenerFourDetails(finremCallbackRequest));
        assertEquals("expecting exception to throw when user not found in am",
            "Could not find the user with email " + INTERVENER_TEST_EMAIL, exception.getMessage());
    }

    @Test
    public void whenCalled_setIntervenerChangeDetailsForIntervenerOneOnAdding() {
        IntervenerOneWrapper intervenerOneWrapper = IntervenerOneWrapper.builder()
            .intervener1Name("Intervener One")
            .build();
        FinremCaseData finremCaseData = FinremCaseData.builder()
            .intervenerOneWrapper(intervenerOneWrapper)
            .build();
        IntervenerDetails intervenerDetails = IntervenerDetails.builder().intervenerName("Intervener One").build();
        when(intervenerOneToIntervenerDetailsMapper.mapToIntervenerDetails(intervenerOneWrapper)).thenReturn(intervenerDetails);

        IntervenerChangeDetails result = service.setIntervenerOneAddedChangeDetails(finremCaseData);
        Assert.assertEquals(IntervenerAction.ADDED.toString(), result.getIntervenerAction().toString());
        Assert.assertEquals(IntervenerType.INTERVENER_ONE.toString(), result.getIntervenerType().toString());
    }

    @Test
    public void whenCalled_setIntervenerChangeDetailsForIntervenerOneOnRemoving() {
        IntervenerOneWrapper intervenerOneWrapper = IntervenerOneWrapper.builder()
            .intervener1Name("Intervener One")
            .build();
        FinremCaseData finremCaseData = FinremCaseData.builder()
            .intervenerOneWrapper(intervenerOneWrapper)
            .build();
        IntervenerDetails intervenerDetails = IntervenerDetails.builder().intervenerName("Intervener One").build();
        when(intervenerOneToIntervenerDetailsMapper.mapToIntervenerDetails(intervenerOneWrapper)).thenReturn(intervenerDetails);

        IntervenerChangeDetails result = service.setIntervenerOneRemovedChangeDetails(finremCaseData);
        Assert.assertEquals(IntervenerAction.REMOVED.toString(), result.getIntervenerAction().toString());
        Assert.assertEquals(IntervenerType.INTERVENER_ONE.toString(), result.getIntervenerType().toString());
    }

    @Test
    public void whenCalled_setIntervenerChangeDetailsForIntervenerTwoOnAdding() {
        IntervenerTwoWrapper intervenerTwoWrapper = IntervenerTwoWrapper.builder()
            .intervener2Name("Intervener Two")
            .build();
        FinremCaseData finremCaseData = FinremCaseData.builder()
            .intervenerTwoWrapper(intervenerTwoWrapper)
            .build();
        IntervenerDetails intervenerDetails = IntervenerDetails.builder().intervenerName("Intervener Two").build();
        when(intervenerTwoToIntervenerDetailsMapper.mapToIntervenerDetails(intervenerTwoWrapper)).thenReturn(intervenerDetails);

        IntervenerChangeDetails result = service.setIntervenerTwoAddedChangeDetails(finremCaseData);
        Assert.assertEquals(IntervenerAction.ADDED.toString(), result.getIntervenerAction().toString());
        Assert.assertEquals(IntervenerType.INTERVENER_TWO.toString(), result.getIntervenerType().toString());
    }

    @Test
    public void whenCalled_setIntervenerChangeDetailsForIntervenerTwoOnRemoving() {
        IntervenerTwoWrapper intervenerTwoWrapper = IntervenerTwoWrapper.builder()
            .intervener2Name("Intervener Two")
            .build();
        FinremCaseData finremCaseData = FinremCaseData.builder()
            .intervenerTwoWrapper(intervenerTwoWrapper)
            .build();
        IntervenerDetails intervenerDetails = IntervenerDetails.builder().intervenerName("Intervener Two").build();
        when(intervenerTwoToIntervenerDetailsMapper.mapToIntervenerDetails(intervenerTwoWrapper)).thenReturn(intervenerDetails);

        IntervenerChangeDetails result = service.setIntervenerTwoRemovedChangeDetails(finremCaseData);
        Assert.assertEquals(IntervenerAction.REMOVED.toString(), result.getIntervenerAction().toString());
        Assert.assertEquals(IntervenerType.INTERVENER_TWO.toString(), result.getIntervenerType().toString());
    }

    @Test
    public void whenCalled_setIntervenerChangeDetailsForIntervenerThreeOnAdding() {
        IntervenerThreeWrapper intervenerThreeWrapper = IntervenerThreeWrapper.builder()
            .intervener3Name("Intervener Three")
            .build();
        FinremCaseData finremCaseData = FinremCaseData.builder()
            .intervenerThreeWrapper(intervenerThreeWrapper)
            .build();
        IntervenerDetails intervenerDetails = IntervenerDetails.builder().intervenerName("Intervener Three").build();
        when(intervenerThreeToIntervenerDetailsMapper.mapToIntervenerDetails(intervenerThreeWrapper)).thenReturn(intervenerDetails);

        IntervenerChangeDetails result = service.setIntervenerThreeAddedChangeDetails(finremCaseData);
        Assert.assertEquals(IntervenerAction.ADDED.toString(), result.getIntervenerAction().toString());
        Assert.assertEquals(IntervenerType.INTERVENER_THREE.toString(), result.getIntervenerType().toString());
    }

    @Test
    public void whenCalled_setIntervenerChangeDetailsForIntervenerThreeOnRemoving() {
        IntervenerThreeWrapper intervenerThreeWrapper = IntervenerThreeWrapper.builder()
            .intervener3Name("Intervener Three")
            .build();
        FinremCaseData finremCaseData = FinremCaseData.builder()
            .intervenerThreeWrapper(intervenerThreeWrapper)
            .build();
        IntervenerDetails intervenerDetails = IntervenerDetails.builder().intervenerName("Intervener Three").build();
        when(intervenerThreeToIntervenerDetailsMapper.mapToIntervenerDetails(intervenerThreeWrapper)).thenReturn(intervenerDetails);

        IntervenerChangeDetails result = service.setIntervenerThreeRemovedChangeDetails(finremCaseData);
        Assert.assertEquals(IntervenerAction.REMOVED.toString(), result.getIntervenerAction().toString());
        Assert.assertEquals(IntervenerType.INTERVENER_THREE.toString(), result.getIntervenerType().toString());
    }

    @Test
    public void whenCalled_setIntervenerChangeDetailsForIntervenerFourOnAdding() {
        IntervenerFourWrapper intervenerFourWrapper = IntervenerFourWrapper.builder()
            .intervener4Name("Intervener Four")
            .build();
        FinremCaseData finremCaseData = FinremCaseData.builder()
            .intervenerFourWrapper(intervenerFourWrapper)
            .build();
        IntervenerDetails intervenerDetails = IntervenerDetails.builder().intervenerName("Intervener Four").build();
        when(intervenerFourToIntervenerDetailsMapper.mapToIntervenerDetails(intervenerFourWrapper)).thenReturn(intervenerDetails);

        IntervenerChangeDetails result = service.setIntervenerFourAddedChangeDetails(finremCaseData);
        Assert.assertEquals(IntervenerAction.ADDED.toString(), result.getIntervenerAction().toString());
        Assert.assertEquals(IntervenerType.INTERVENER_FOUR.toString(), result.getIntervenerType().toString());
    }

    @Test
    public void whenCalled_setIntervenerChangeDetailsForIntervenerFourOnRemoving() {
        IntervenerFourWrapper intervenerFourWrapper = IntervenerFourWrapper.builder()
            .intervener4Name("Intervener Four")
            .build();
        FinremCaseData finremCaseData = FinremCaseData.builder()
            .intervenerFourWrapper(intervenerFourWrapper)
            .build();
        IntervenerDetails intervenerDetails = IntervenerDetails.builder().intervenerName("Intervener Four").build();
        when(intervenerFourToIntervenerDetailsMapper.mapToIntervenerDetails(intervenerFourWrapper)).thenReturn(intervenerDetails);

        IntervenerChangeDetails result = service.setIntervenerFourRemovedChangeDetails(finremCaseData);
        Assert.assertEquals(IntervenerAction.REMOVED.toString(), result.getIntervenerAction().toString());
        Assert.assertEquals(IntervenerType.INTERVENER_FOUR.toString(), result.getIntervenerType().toString());
    }

    @Test
    public void whenIntervenerOneSolicitorRemoved_ShouldReturnTrue() {
        IntervenerOneWrapper intervenerOneWrapperBefore = IntervenerOneWrapper.builder()
            .intervener1Represented(YesOrNo.YES)
            .build();
        IntervenerOneWrapper intervenerOneWrapper = IntervenerOneWrapper.builder()
            .intervener1Represented(YesOrNo.NO)
            .build();
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
        IntervenerTwoWrapper intervenerTwoWrapperBefore = IntervenerTwoWrapper.builder()
            .intervener2Represented(YesOrNo.YES)
            .build();
        IntervenerTwoWrapper intervenerTwoWrapper = IntervenerTwoWrapper.builder()
            .intervener2Represented(YesOrNo.NO)
            .build();
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
        IntervenerThreeWrapper intervenerThreeWrapperBefore = IntervenerThreeWrapper.builder()
            .intervener3Represented(YesOrNo.YES)
            .build();
        IntervenerThreeWrapper intervenerThreeWrapper = IntervenerThreeWrapper.builder()
            .intervener3Represented(YesOrNo.NO)
            .build();
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
        IntervenerFourWrapper intervenerFourWrapperBefore = IntervenerFourWrapper.builder()
            .intervener4Represented(YesOrNo.YES)
            .build();
        IntervenerFourWrapper intervenerFourWrapper = IntervenerFourWrapper.builder()
            .intervener4Represented(YesOrNo.NO)
            .build();
        FinremCaseData finremCaseData = FinremCaseData.builder()
            .intervenerFourWrapper(intervenerFourWrapper)
            .build();
        FinremCaseData finremCaseDataBefore = FinremCaseData.builder()
            .intervenerFourWrapper(intervenerFourWrapperBefore)
            .build();
        Assert.assertTrue(service.checkIfAnyIntervenerSolicitorRemoved(finremCaseData, finremCaseDataBefore));
    }

    @Test
    public void whenNoIntervenerRemoved_ShouldReturnFalse() {
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