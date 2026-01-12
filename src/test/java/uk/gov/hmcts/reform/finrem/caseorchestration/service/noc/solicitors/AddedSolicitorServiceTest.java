package uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.solicitors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangeOrganisationApprovalStatus;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangeOrganisationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangedRepresentative;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NoticeOfChangeParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Organisation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrganisationPolicy;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_ORG2_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_ORG_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_ORGANISATION_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_SOLICITOR_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NOC_PARTY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_ORGANISATION_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_EMAIL;

@ExtendWith(MockitoExtension.class)
class AddedSolicitorServiceTest {

    private static final String TEST_APP_ORG_ID = TEST_ORG_ID;
    private static final String TEST_APP_ORG_NAME = "AppTestOrgName";
    private static final String APP_SOL_FORENAME = "Applicant";
    private static final String APP_SOL_SURNAME = "Solicitor";
    private static final String APP_SOL_NAME = APP_SOL_FORENAME + " " + APP_SOL_SURNAME;
    private static final String APP_SOL_EMAIL = "appsolicitor@gmail.com";

    private static final String TEST_RESP_ORG_ID = TEST_ORG2_ID;
    private static final String TEST_RESP_ORG_NAME = "RespTestOrgName";
    private static final String RESP_SOL_FORENAME = "Respondent";
    private static final String RESP_SOL_SURNAME = "Solicitor";
    private static final String RESP_SOL_NAME = RESP_SOL_FORENAME + " " + RESP_SOL_SURNAME;
    private static final String RESP_SOL_EMAIL = "respsolicitor@gmail.com";

    @Mock
    private CaseDataService caseDataService;

    @Mock
    private CheckApplicantSolicitorIsDigitalService checkApplicantSolicitorIsDigitalService;

    @Mock
    private CheckRespondentSolicitorIsDigitalService checkRespondentSolicitorIsDigitalService;

    @InjectMocks
    private AddedSolicitorService addedSolicitorService;

    private CaseDetails caseDetails;

    @BeforeEach
    void setUp() {
        caseDetails = CaseDetails.builder().caseTypeId(CaseType.CONTESTED.getCcdType()).id(Long.valueOf(CASE_ID)).data(new HashMap<>()).build();
    }

    @Test
    void givenAppSolicitor_whenGetAddedSolicitorAsSolicitor_thenGetCorrectAddedSolicitor() {
        DynamicList appSolRoleList = getApplicantCaseRole();

        ChangeOrganisationRequest changeRequest = getApplicantChangeRequest(appSolRoleList);

        UserDetails solicitorToAdd = UserDetails.builder()
            .forename(APP_SOL_FORENAME)
            .surname(APP_SOL_SURNAME)
            .email(APP_SOL_EMAIL)
            .build();

        final ChangedRepresentative addedSolicitor = addedSolicitorService.getAddedSolicitorAsSolicitor(solicitorToAdd, changeRequest);
        assertEquals(APP_SOL_NAME, addedSolicitor.getName());
        assertEquals(APP_SOL_EMAIL, addedSolicitor.getEmail());
        assertEquals(TEST_APP_ORG_ID, addedSolicitor.getOrganisation().getOrganisationID());
        assertEquals(TEST_APP_ORG_NAME, addedSolicitor.getOrganisation().getOrganisationName());
    }

    @Test
    void givenRespSolicitor_whenGetAddedSolicitorAsSolicitor_thenGetCorrectAddedSolicitor() {
        DynamicList respSolRoleList = getRespondentCaseRole();
        ChangeOrganisationRequest changeRequest = getRespondentChangeRequest(respSolRoleList);

        UserDetails solicitorToAdd = UserDetails.builder()
            .forename(RESP_SOL_FORENAME)
            .surname(RESP_SOL_SURNAME)
            .email(RESP_SOL_EMAIL)
            .build();

        final ChangedRepresentative addedSolicitor = addedSolicitorService.getAddedSolicitorAsSolicitor(solicitorToAdd, changeRequest);
        assertEquals(RESP_SOL_NAME, addedSolicitor.getName());
        assertEquals(RESP_SOL_EMAIL, addedSolicitor.getEmail());
        assertEquals(TEST_RESP_ORG_ID, addedSolicitor.getOrganisation().getOrganisationID());
        assertEquals(TEST_RESP_ORG_NAME, addedSolicitor.getOrganisation().getOrganisationName());
    }

    @Test
    void givenAppSolicitorContested_whenGetAddedSolicitorAsCaseworker_thenGetCorrectAddedSolicitor() {
        OrganisationPolicy applicantOrgPolicy = OrganisationPolicy.builder()
            .orgPolicyCaseAssignedRole(APP_SOLICITOR_POLICY)
            .organisation(Organisation.builder().organisationID(TEST_APP_ORG_ID).organisationName(TEST_APP_ORG_NAME).build())
            .build();
        caseDetails.getData().put(CONTESTED_SOLICITOR_NAME, APP_SOL_NAME);
        caseDetails.getData().put(CONTESTED_SOLICITOR_EMAIL, APP_SOL_EMAIL);
        caseDetails.getData().put(NOC_PARTY, APPLICANT);
        caseDetails.getData().put(APPLICANT_ORGANISATION_POLICY, applicantOrgPolicy);

        when(caseDataService.isConsentedApplication(caseDetails)).thenReturn(false);
        when(caseDataService.isLitigantRepresented(caseDetails, true)).thenReturn(false);

        final ChangedRepresentative addedSolicitor = addedSolicitorService.getAddedSolicitorAsCaseworker(caseDetails);
        verify(caseDataService).isLitigantRepresented(caseDetails, true);

        reset(caseDataService);

        FinremCaseData finremCaseData = FinremCaseData.builder()
            .ccdCaseType(CaseType.CONTESTED)
            .contactDetailsWrapper(ContactDetailsWrapper.builder()
                .applicantSolicitorName(APP_SOL_NAME)
                .applicantSolicitorEmail(APP_SOL_EMAIL)
                .nocParty(NoticeOfChangeParty.APPLICANT)
                .build())
            .applicantOrganisationPolicy(applicantOrgPolicy)
            .build();

        when(caseDataService.isLitigantRepresented(finremCaseData, true)).thenReturn(false);
        ChangedRepresentative addedSolicitor2 = addedSolicitorService.getAddedSolicitorAsCaseworker(finremCaseData);
        verify(caseDataService).isLitigantRepresented(finremCaseData, true);

        Stream.of(addedSolicitor, addedSolicitor2).forEach(actual -> {
            assertEquals(APP_SOL_NAME, actual.getName());
            assertEquals(APP_SOL_EMAIL, actual.getEmail());
            assertEquals(TEST_APP_ORG_ID, actual.getOrganisation().getOrganisationID());
            assertEquals(TEST_APP_ORG_NAME, actual.getOrganisation().getOrganisationName());
        });
    }

    @Test
    void givenAppSolicitorConsented_whenGetAddedSolicitorAsCaseworker_thenGetCorrectAddedSolicitor() {
        OrganisationPolicy applicantOrgPolicy = OrganisationPolicy.builder()
            .orgPolicyCaseAssignedRole(APP_SOLICITOR_POLICY)
            .organisation(Organisation.builder().organisationID(TEST_APP_ORG_ID).organisationName(TEST_APP_ORG_NAME).build())
            .build();

        caseDetails.getData().put(CONSENTED_SOLICITOR_NAME, APP_SOL_NAME);
        caseDetails.getData().put(SOLICITOR_EMAIL, APP_SOL_EMAIL);
        caseDetails.getData().put(NOC_PARTY, APPLICANT);
        caseDetails.getData().put(APPLICANT_ORGANISATION_POLICY, applicantOrgPolicy);

        when(caseDataService.isConsentedApplication(caseDetails)).thenReturn(true);
        when(caseDataService.isLitigantRepresented(caseDetails, true)).thenReturn(false);
        final ChangedRepresentative addedSolicitor = addedSolicitorService.getAddedSolicitorAsCaseworker(caseDetails);
        verify(caseDataService).isLitigantRepresented(caseDetails, true);

        reset(caseDataService);

        FinremCaseData finremCaseData = FinremCaseData.builder()
            .ccdCaseType(CaseType.CONSENTED)
            .contactDetailsWrapper(ContactDetailsWrapper.builder()
                .solicitorName(APP_SOL_NAME)
                .solicitorEmail(APP_SOL_EMAIL)
                .nocParty(NoticeOfChangeParty.APPLICANT)
                .build())
            .applicantOrganisationPolicy(applicantOrgPolicy)
            .build();
        when(caseDataService.isLitigantRepresented(finremCaseData, true)).thenReturn(false);
        ChangedRepresentative addedSolicitor2 = addedSolicitorService.getAddedSolicitorAsCaseworker(finremCaseData);
        verify(caseDataService).isLitigantRepresented(finremCaseData, true);

        Stream.of(addedSolicitor, addedSolicitor2).forEach(actual -> {
            assertEquals(APP_SOL_NAME, actual.getName());
            assertEquals(APP_SOL_EMAIL, actual.getEmail());
            assertEquals(TEST_APP_ORG_ID, actual.getOrganisation().getOrganisationID());
            assertEquals(TEST_APP_ORG_NAME, actual.getOrganisation().getOrganisationName());
        });
    }

    @ParameterizedTest
    @EnumSource(CaseType.class)
    void givenRespSolicitor_whenGetAddedSolicitorAsCaseworker_thenGetCorrectAddedSolicitor(CaseType caseType) {
        OrganisationPolicy respondentOrgPolicy = OrganisationPolicy.builder()
            .orgPolicyCaseAssignedRole(RESP_SOLICITOR_POLICY)
            .organisation(Organisation.builder().organisationID(TEST_RESP_ORG_ID).organisationName(TEST_RESP_ORG_NAME).build())
            .build();

        caseDetails.getData().put(RESP_SOLICITOR_NAME, RESP_SOL_NAME);
        caseDetails.getData().put(RESP_SOLICITOR_EMAIL, RESP_SOL_EMAIL);
        caseDetails.getData().put(NOC_PARTY, RESPONDENT);
        caseDetails.getData().put(RESPONDENT_ORGANISATION_POLICY, respondentOrgPolicy);

        when(caseDataService.isLitigantRepresented(caseDetails, false)).thenReturn(false);
        final ChangedRepresentative addedSolicitor = addedSolicitorService.getAddedSolicitorAsCaseworker(caseDetails);
        verify(caseDataService).isLitigantRepresented(caseDetails, false);

        reset(caseDataService);

        FinremCaseData finremCaseData = FinremCaseData.builder()
            .ccdCaseType(caseType)
            .contactDetailsWrapper(ContactDetailsWrapper.builder()
                .respondentSolicitorName(RESP_SOL_NAME)
                .respondentSolicitorEmail(RESP_SOL_EMAIL)
                .nocParty(NoticeOfChangeParty.RESPONDENT)
                .build())
            .respondentOrganisationPolicy(respondentOrgPolicy)
            .build();

        when(caseDataService.isLitigantRepresented(finremCaseData, false)).thenReturn(false);
        ChangedRepresentative addedSolicitor2 = addedSolicitorService.getAddedSolicitorAsCaseworker(finremCaseData);
        verify(caseDataService).isLitigantRepresented(finremCaseData, false);

        Stream.of(addedSolicitor, addedSolicitor2).forEach(actual -> {
            assertEquals(RESP_SOL_NAME, actual.getName());
            assertEquals(RESP_SOL_EMAIL, actual.getEmail());
            assertEquals(TEST_RESP_ORG_ID, actual.getOrganisation().getOrganisationID());
            assertEquals(TEST_RESP_ORG_NAME, actual.getOrganisation().getOrganisationName());
        });
    }

    @Test
    void givenNonDigitalApplicantSolicitor_whenGetAddedSolicitorAsCaseworker_thenGetCorrectAddedSolicitor() {
        OrganisationPolicy applicantOrgPolicy = OrganisationPolicy.builder()
            .orgPolicyCaseAssignedRole(APP_SOLICITOR_POLICY)
            .organisation(Organisation.builder().build())
            .build();

        // CaseDetails
        caseDetails.getData().put(CONTESTED_SOLICITOR_NAME, APP_SOL_NAME);
        caseDetails.getData().put(CONTESTED_SOLICITOR_EMAIL, APP_SOL_EMAIL);
        caseDetails.getData().put(NOC_PARTY, APPLICANT);
        caseDetails.getData().put(APPLICANT_ORGANISATION_POLICY, applicantOrgPolicy);

        when(caseDataService.isConsentedApplication(caseDetails)).thenReturn(false);
        when(caseDataService.isLitigantRepresented(caseDetails, true)).thenReturn(true);
        when(checkApplicantSolicitorIsDigitalService.isSolicitorDigital(caseDetails)).thenReturn(false);
        final ChangedRepresentative addedSolicitor = addedSolicitorService.getAddedSolicitorAsCaseworker(caseDetails);
        verify(caseDataService, times(2)).isConsentedApplication(caseDetails);
        verify(caseDataService).isLitigantRepresented(caseDetails, true);
        verify(checkApplicantSolicitorIsDigitalService).isSolicitorDigital(caseDetails);

        reset(caseDataService, checkApplicantSolicitorIsDigitalService);

        // FinremCaseData
        FinremCaseData finremCaseData = FinremCaseData.builder()
            .ccdCaseType(CaseType.CONTESTED)
            .contactDetailsWrapper(ContactDetailsWrapper.builder()
                .applicantSolicitorName(APP_SOL_NAME)
                .applicantSolicitorEmail(APP_SOL_EMAIL)
                .nocParty(NoticeOfChangeParty.APPLICANT)
                .build())
            .applicantOrganisationPolicy(applicantOrgPolicy)
            .build();

        when(caseDataService.isLitigantRepresented(finremCaseData, true)).thenReturn(true);
        when(checkApplicantSolicitorIsDigitalService.isSolicitorDigital(finremCaseData)).thenReturn(false);
        ChangedRepresentative addedSolicitor2 = addedSolicitorService.getAddedSolicitorAsCaseworker(finremCaseData);
        verify(caseDataService).isLitigantRepresented(finremCaseData, true);
        verify(checkApplicantSolicitorIsDigitalService).isSolicitorDigital(finremCaseData);

        Stream.of(addedSolicitor, addedSolicitor2).forEach(
            actual -> {
                assertEquals(APP_SOL_NAME, actual.getName());
                assertEquals(APP_SOL_EMAIL, actual.getEmail());
                assertNull(actual.getOrganisation());
            }
        );
    }

    @ParameterizedTest
    @EnumSource(CaseType.class)
    void givenNonDigitalRespondentSolicitor_whenGetAddedSolicitorAsCaseworker_thenGetCorrectAddedSolicitor(CaseType caseType) {
        OrganisationPolicy respondentOrgPolicy = OrganisationPolicy.builder()
            .orgPolicyCaseAssignedRole(RESP_SOLICITOR_POLICY)
            .organisation(Organisation.builder().build())
            .build();

        caseDetails.getData().put(RESP_SOLICITOR_NAME, RESP_SOL_NAME);
        caseDetails.getData().put(RESP_SOLICITOR_EMAIL, RESP_SOL_EMAIL);
        caseDetails.getData().put(NOC_PARTY, RESPONDENT);
        caseDetails.getData().put(RESPONDENT_ORGANISATION_POLICY, respondentOrgPolicy);

        when(caseDataService.isLitigantRepresented(caseDetails, false)).thenReturn(true);
        when(checkRespondentSolicitorIsDigitalService.isSolicitorDigital(caseDetails)).thenReturn(false);
        final ChangedRepresentative addedSolicitor = addedSolicitorService.getAddedSolicitorAsCaseworker(caseDetails);
        verify(caseDataService).isLitigantRepresented(caseDetails, false);
        verify(checkRespondentSolicitorIsDigitalService).isSolicitorDigital(caseDetails);

        reset(caseDataService, checkRespondentSolicitorIsDigitalService);
        FinremCaseData finremCaseData = FinremCaseData.builder()
            .ccdCaseType(caseType)
            .contactDetailsWrapper(ContactDetailsWrapper.builder()
                .respondentSolicitorName(RESP_SOL_NAME)
                .respondentSolicitorEmail(RESP_SOL_EMAIL)
                .nocParty(NoticeOfChangeParty.RESPONDENT)
                .build())
            .respondentOrganisationPolicy(respondentOrgPolicy)
            .build();

        when(caseDataService.isLitigantRepresented(finremCaseData, false)).thenReturn(true);
        when(checkRespondentSolicitorIsDigitalService.isSolicitorDigital(finremCaseData)).thenReturn(false);
        final ChangedRepresentative addedSolicitor2 = addedSolicitorService.getAddedSolicitorAsCaseworker(finremCaseData);
        verify(caseDataService).isLitigantRepresented(finremCaseData, false);
        verify(checkRespondentSolicitorIsDigitalService).isSolicitorDigital(finremCaseData);

        Stream.of(addedSolicitor, addedSolicitor2).forEach(
            actual -> {
                assertEquals(RESP_SOL_NAME, actual.getName());
                assertEquals(RESP_SOL_EMAIL, actual.getEmail());
                assertNull(actual.getOrganisation());
            }
        );
    }

    @Test
    void givenApplicantOrganisationPolicyIsNull_whenGetAddedSolicitorAsCaseworker_thenGetCorrectAddedSolicitor() {
        FinremCaseData finremCaseData = FinremCaseData.builder()
            .contactDetailsWrapper(ContactDetailsWrapper.builder().nocParty(NoticeOfChangeParty.APPLICANT).build())
            .applicantOrganisationPolicy(null)
            .build();
        when(caseDataService.isLitigantRepresented(finremCaseData, true)).thenReturn(false);

        assertNull(addedSolicitorService.getAddedSolicitorAsCaseworker(finremCaseData));

        finremCaseData = FinremCaseData.builder()
            .contactDetailsWrapper(ContactDetailsWrapper.builder().nocParty(NoticeOfChangeParty.APPLICANT).build())
            .applicantOrganisationPolicy(OrganisationPolicy.builder().orgPolicyCaseAssignedRole("[APPSOLICITOR]").build())
            .build();
        assertNull(addedSolicitorService.getAddedSolicitorAsCaseworker(finremCaseData));
    }

    private DynamicList getApplicantCaseRole() {
        DynamicListElement appSolRole = DynamicListElement.builder()
            .code(APP_SOLICITOR_POLICY)
            .label(APP_SOLICITOR_POLICY)
            .build();

        return DynamicList
            .builder()
            .value(appSolRole)
            .listItems(List.of(appSolRole))
            .build();
    }

    private DynamicList getRespondentCaseRole() {
        DynamicListElement respSoleRole = DynamicListElement.builder()
            .code(RESP_SOLICITOR_POLICY)
            .label(RESP_SOLICITOR_POLICY)
            .build();

        return DynamicList
            .builder()
            .value(respSoleRole)
            .listItems(List.of(respSoleRole))
            .build();
    }

    private ChangeOrganisationRequest getApplicantChangeRequest(DynamicList applicantRole) {
        return ChangeOrganisationRequest
            .builder()
            .organisationToAdd(Organisation.builder()
                .organisationID(TEST_APP_ORG_ID)
                .organisationName(TEST_APP_ORG_NAME)
                .build())
            .caseRoleId(applicantRole)
            .approvalStatus(ChangeOrganisationApprovalStatus.APPROVED)
            .build();
    }

    private ChangeOrganisationRequest getRespondentChangeRequest(DynamicList respondentRole) {
        return ChangeOrganisationRequest
            .builder()
            .organisationToAdd(Organisation.builder()
                .organisationID(TEST_RESP_ORG_ID)
                .organisationName(TEST_RESP_ORG_NAME)
                .build())
            .caseRoleId(respondentRole)
            .approvalStatus(ChangeOrganisationApprovalStatus.APPROVED)
            .build();
    }
}
