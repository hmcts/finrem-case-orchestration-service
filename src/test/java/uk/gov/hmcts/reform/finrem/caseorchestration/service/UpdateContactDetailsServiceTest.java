package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Address;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NoticeOfChangeParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Organisation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrganisationPolicy;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateContactDetailsServiceTest {

    @InjectMocks
    private UpdateContactDetailsService service;

    @Mock
    private CaseDataService caseDataService;

    @Test
    void shouldPersistOrgPolicies_withFinremCaseData() {
        FinremCaseData originalData = FinremCaseData.builder()
            .applicantOrganisationPolicy(OrganisationPolicy
                .builder()
                .organisation(Organisation
                    .builder()
                    .organisationID("App ORG ID")
                    .organisationName("App ORG NAME")
                    .build())
                .build()
            ).respondentOrganisationPolicy(
                OrganisationPolicy
                    .builder()
                    .organisation(Organisation
                        .builder()
                        .organisationID("Resp ORG ID")
                        .organisationName("Resp ORG NAME")
                        .build())
                    .build()
            )
            .build();

        FinremCaseData caseData = FinremCaseData.builder().build();

        service.persistOrgPolicies(caseData, originalData);

        assertEquals(caseData.getApplicantOrganisationPolicy(), originalData.getApplicantOrganisationPolicy());
        assertEquals(caseData.getRespondentOrganisationPolicy(), originalData.getRespondentOrganisationPolicy());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("clearingScenarios")
    void shouldClearFields_whenScenarioRequiresClearing(String scenarioName,
                                                        CaseType caseType,
                                                        FinremCaseData caseData) {

        if (caseData.getContactDetailsWrapper().getNocParty() == NoticeOfChangeParty.RESPONDENT) {
            when(caseDataService.isRespondentRepresentedByASolicitor(caseData)).thenReturn(false);
        }

        service.handleRepresentationChange(caseData, caseType);

        if (caseData.getContactDetailsWrapper().getNocParty() == NoticeOfChangeParty.APPLICANT) {
            if (caseType == CaseType.CONTESTED) {
                assertContestedApplicantCleared(caseData);
            } else {
                assertConsentedApplicantCleared(caseData);
            }
        } else {
            assertRespondentCleared(caseData);
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("nonClearingScenarios")
    void shouldNotClearFields_whenScenarioDoesNotRequireClearing(String scenarioName,
                                                                 CaseType caseType,
                                                                 FinremCaseData caseData) {

        if (caseData.getContactDetailsWrapper().getNocParty() == NoticeOfChangeParty.RESPONDENT) {
            when(caseDataService.isRespondentRepresentedByASolicitor(caseData)).thenReturn(true);
        }

        ContactDetailsWrapper contactDetails = caseData.getContactDetailsWrapper();
        final Address initialRespondentAddress = contactDetails.getRespondentSolicitorAddress();
        final Address initialApplicantAddressContested = contactDetails.getApplicantSolicitorAddress();
        final Address initialApplicantAddressConsented = contactDetails.getSolicitorAddress();

        service.handleRepresentationChange(caseData, caseType);

        assertSame(initialRespondentAddress, contactDetails.getRespondentSolicitorAddress());
        assertSame(initialApplicantAddressContested, contactDetails.getApplicantSolicitorAddress());
        assertSame(initialApplicantAddressConsented, contactDetails.getSolicitorAddress());
    }

    static Stream<Arguments> clearingScenarios() {
        return Stream.of(
            Arguments.of("CONTESTED - Applicant NoC + applicant NO", CaseType.CONTESTED,
                createCaseData(NoticeOfChangeParty.APPLICANT, CaseType.CONTESTED, YesOrNo.NO, YesOrNo.YES)),
            Arguments.of("CONSENTED - Applicant NoC + applicant NO", CaseType.CONSENTED,
                createCaseData(NoticeOfChangeParty.APPLICANT, CaseType.CONSENTED, YesOrNo.NO, YesOrNo.YES)),
            Arguments.of("CONTESTED - Respondent NoC + respondent NO", CaseType.CONTESTED,
                createCaseData(NoticeOfChangeParty.RESPONDENT, CaseType.CONTESTED, YesOrNo.YES, YesOrNo.NO)),
            Arguments.of("CONSENTED - Respondent NoC + respondent NO", CaseType.CONSENTED,
                createCaseData(NoticeOfChangeParty.RESPONDENT, CaseType.CONSENTED, YesOrNo.YES, YesOrNo.NO))
        );
    }

    static Stream<Arguments> nonClearingScenarios() {
        return Stream.of(
            Arguments.of("CONTESTED - Applicant NoC + applicant YES", CaseType.CONTESTED,
                createCaseData(NoticeOfChangeParty.APPLICANT, CaseType.CONTESTED, YesOrNo.YES, YesOrNo.YES)),
            Arguments.of("CONSENTED - Applicant NoC + applicant YES", CaseType.CONSENTED,
                createCaseData(NoticeOfChangeParty.APPLICANT, CaseType.CONSENTED, YesOrNo.YES, YesOrNo.YES)),
            Arguments.of("CONTESTED - Respondent NoC + respondent YES", CaseType.CONTESTED,
                createCaseData(NoticeOfChangeParty.RESPONDENT, CaseType.CONTESTED, YesOrNo.YES, YesOrNo.YES)),
            Arguments.of("CONSENTED - Respondent NoC + respondent YES", CaseType.CONSENTED,
                createCaseData(NoticeOfChangeParty.RESPONDENT, CaseType.CONSENTED, YesOrNo.YES, YesOrNo.YES))
        );
    }

    private static FinremCaseData createCaseData(NoticeOfChangeParty party, CaseType caseType,
                                                 YesOrNo applicantRepresented, YesOrNo respondentRepresented) {
        ContactDetailsWrapper contactDetails = ContactDetailsWrapper.builder()
            .nocParty(party)
            .applicantRepresented(applicantRepresented)
            .applicantSolicitorName("A name")
            .applicantSolicitorFirm("A firm")
            .applicantSolicitorAddress(createAddress("A address"))
            .applicantSolicitorPhone("123")
            .applicantSolicitorEmail("a@test.com")
            .applicantSolicitorConsentForEmails(YesOrNo.YES)
            .solicitorName("S name")
            .solicitorFirm("S firm")
            .solicitorAddress(createAddress("S address"))
            .solicitorPhone("456")
            .solicitorEmail("s@test.com")
            .solicitorAgreeToReceiveEmails(YesOrNo.YES)
            .respondentSolicitorName("R name")
            .respondentSolicitorFirm("R firm")
            .respondentSolicitorAddress(createAddress("R address"))
            .respondentSolicitorPhone("789")
            .respondentSolicitorEmail("r@test.com")
            .respondentSolicitorDxNumber("DX123")
            .solicitorReference("REF")
            .build();

        if (caseType == CaseType.CONTESTED) {
            contactDetails.setContestedRespondentRepresented(respondentRepresented);
        } else {
            contactDetails.setConsentedRespondentRepresented(respondentRepresented);
        }

        return FinremCaseData.builder()
            .contactDetailsWrapper(contactDetails)
            .applicantOrganisationPolicy(new OrganisationPolicy())
            .respondentOrganisationPolicy(new OrganisationPolicy())
            .respSolNotificationsEmailConsent(YesOrNo.YES)
            .build();
    }

    private static Address createAddress(String line1) {
        return Address.builder()
            .addressLine1(line1)
            .addressLine2("Line 2")
            .addressLine3("Line 3")
            .county("London")
            .country("England")
            .postTown("London")
            .postCode("SE1")
            .build();
    }

    private static void assertContestedApplicantCleared(FinremCaseData data) {
        ContactDetailsWrapper contactDetailsWrapper = data.getContactDetailsWrapper();

        assertNull(contactDetailsWrapper.getApplicantSolicitorName());
        assertNull(contactDetailsWrapper.getApplicantSolicitorFirm());
        assertNull(contactDetailsWrapper.getApplicantSolicitorAddress());
        assertNull(contactDetailsWrapper.getApplicantSolicitorPhone());
        assertNull(contactDetailsWrapper.getApplicantSolicitorEmail());
        assertNull(contactDetailsWrapper.getApplicantSolicitorConsentForEmails());
        assertNull(contactDetailsWrapper.getSolicitorReference());
        assertNull(data.getApplicantOrganisationPolicy());
    }

    private static void assertConsentedApplicantCleared(FinremCaseData data) {
        ContactDetailsWrapper contactDetailsWrapper = data.getContactDetailsWrapper();

        assertNull(contactDetailsWrapper.getSolicitorName());
        assertNull(contactDetailsWrapper.getSolicitorFirm());
        assertNull(contactDetailsWrapper.getSolicitorAddress());
        assertNull(contactDetailsWrapper.getSolicitorPhone());
        assertNull(contactDetailsWrapper.getSolicitorEmail());
        assertNull(contactDetailsWrapper.getSolicitorAgreeToReceiveEmails());
        assertNull(contactDetailsWrapper.getSolicitorReference());
        assertNull(data.getApplicantOrganisationPolicy());
    }

    private static void assertRespondentCleared(FinremCaseData data) {
        ContactDetailsWrapper contactDetailsWrapper = data.getContactDetailsWrapper();

        assertNull(contactDetailsWrapper.getRespondentSolicitorName());
        assertNull(contactDetailsWrapper.getRespondentSolicitorFirm());
        assertNull(contactDetailsWrapper.getRespondentSolicitorAddress());
        assertNull(contactDetailsWrapper.getRespondentSolicitorPhone());
        assertNull(contactDetailsWrapper.getRespondentSolicitorEmail());
        assertNull(contactDetailsWrapper.getRespondentSolicitorDxNumber());

        assertNull(data.getRespSolNotificationsEmailConsent());
        assertNull(data.getRespondentOrganisationPolicy());
    }
}
