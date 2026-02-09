package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Address;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NoticeOfChangeParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Organisation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrganisationPolicy;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(MockitoExtension.class)
class UpdateContactDetailsServiceTest {

    private final UpdateContactDetailsService service = new UpdateContactDetailsService();

    @MockitoBean
    UpdateContactDetailsService updateContactDetailsService;

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

    @ParameterizedTest
    @MethodSource("finremCaseDataParameters")
    void shouldRemoveApplicantSolicitorDetails_withFinremCaseData(CaseType caseType,
                                                                  FinremCaseData finremCaseData,
                                                                  List<Function<FinremCaseData, Object>> propertiesToRemove) {

        service.handleRepresentationChange(finremCaseData, caseType);

        for (Function<FinremCaseData, Object> propertyGetter : propertiesToRemove) {
            Object value = propertyGetter.apply(finremCaseData);
            assertNull(value);
        }
    }

    public static Stream<Arguments> finremCaseDataParameters() {
        return Stream.of(
            Arguments.of(
                CaseType.CONTESTED,
                getContestedApplicantFinremCaseData(),
                List.<Function<FinremCaseData, Object>>of(
                    data -> data.getContactDetailsWrapper().getApplicantSolicitorName(),
                    data -> data.getContactDetailsWrapper().getApplicantSolicitorAddress(),
                    data -> data.getContactDetailsWrapper().getApplicantSolicitorPhone(),
                    data -> data.getContactDetailsWrapper().getApplicantSolicitorEmail(),
                    data -> data.getContactDetailsWrapper().getApplicantSolicitorConsentForEmails(),
                    data -> data.getContactDetailsWrapper().getSolicitorReference(),
                    FinremCaseData::getApplicantOrganisationPolicy
                )
            ),
            Arguments.of(
                CaseType.CONTESTED,
                getContestedApplicantNullRepresentationFinremCaseData(),
                List.<Function<FinremCaseData, Object>>of(
                    data -> data.getContactDetailsWrapper().getApplicantSolicitorName(),
                    data -> data.getContactDetailsWrapper().getApplicantSolicitorAddress(),
                    data -> data.getContactDetailsWrapper().getApplicantSolicitorPhone(),
                    data -> data.getContactDetailsWrapper().getApplicantSolicitorEmail(),
                    data -> data.getContactDetailsWrapper().getApplicantSolicitorConsentForEmails(),
                    data -> data.getContactDetailsWrapper().getSolicitorReference(),
                    FinremCaseData::getApplicantOrganisationPolicy
                )
            ),
            Arguments.of(
                CaseType.CONSENTED,
                getConsentedApplicantFinremCaseData(),
                List.<Function<FinremCaseData, Object>>of(
                    data -> data.getContactDetailsWrapper().getSolicitorName(),
                    data -> data.getContactDetailsWrapper().getSolicitorFirm(),
                    data -> data.getContactDetailsWrapper().getSolicitorAddress(),
                    data -> data.getContactDetailsWrapper().getSolicitorPhone(),
                    data -> data.getContactDetailsWrapper().getSolicitorAddress(),
                    data -> data.getContactDetailsWrapper().getSolicitorEmail(),
                    data -> data.getContactDetailsWrapper().getSolicitorAgreeToReceiveEmails(),
                    data -> data.getContactDetailsWrapper().getSolicitorReference(),
                    FinremCaseData::getApplicantOrganisationPolicy
                )
            ),
            Arguments.of(
                CaseType.CONTESTED,
                getContestedRespondentRepresentedFinremCaseData(),
                List.<Function<FinremCaseData, Object>>of(
                    data -> data.getContactDetailsWrapper().getRespondentSolicitorAddress(),
                    data -> data.getContactDetailsWrapper().getRespondentSolicitorPhone()
                )
            ),
            Arguments.of(
                CaseType.CONSENTED,
                getConsentedRespondentRepresentedFinremCaseData(),
                List.<Function<FinremCaseData, Object>>of(
                    data -> data.getContactDetailsWrapper().getRespondentSolicitorAddress(),
                    data -> data.getContactDetailsWrapper().getRespondentSolicitorPhone()
                )
            ),
            Arguments.of(
                CaseType.CONTESTED,
                getContestedNotRespondentRepresentedFinremCaseData(),
                List.<Function<FinremCaseData, Object>>of(
                    data -> data.getContactDetailsWrapper().getRespondentSolicitorName(),
                    data -> data.getContactDetailsWrapper().getRespondentSolicitorFirm(),
                    data -> data.getContactDetailsWrapper().getRespondentSolicitorAddress(),
                    data -> data.getContactDetailsWrapper().getRespondentSolicitorPhone(),
                    data -> data.getContactDetailsWrapper().getRespondentSolicitorEmail(),
                    data -> data.getContactDetailsWrapper().getRespondentSolicitorDxNumber(),
                    FinremCaseData::getRespSolNotificationsEmailConsent,
                    FinremCaseData::getRespondentOrganisationPolicy
                )
            ),
            Arguments.of(
                CaseType.CONTESTED,
                getContestedNullRespondentRepresentedFinremCaseData(),
                List.<Function<FinremCaseData, Object>>of(
                    data -> data.getContactDetailsWrapper().getRespondentSolicitorName(),
                    data -> data.getContactDetailsWrapper().getRespondentSolicitorFirm(),
                    data -> data.getContactDetailsWrapper().getRespondentSolicitorAddress(),
                    data -> data.getContactDetailsWrapper().getRespondentSolicitorPhone(),
                    data -> data.getContactDetailsWrapper().getRespondentSolicitorEmail(),
                    data -> data.getContactDetailsWrapper().getRespondentSolicitorDxNumber(),
                    FinremCaseData::getRespSolNotificationsEmailConsent,
                    FinremCaseData::getRespondentOrganisationPolicy
                )
            ),
            Arguments.of(
                CaseType.CONSENTED,
                getConsentedNotRespondentRepresentedFinremCaseData(),
                List.<Function<FinremCaseData, Object>>of(
                    data -> data.getContactDetailsWrapper().getRespondentSolicitorName(),
                    data -> data.getContactDetailsWrapper().getRespondentSolicitorFirm(),
                    data -> data.getContactDetailsWrapper().getRespondentSolicitorAddress(),
                    data -> data.getContactDetailsWrapper().getRespondentSolicitorPhone(),
                    data -> data.getContactDetailsWrapper().getRespondentSolicitorEmail(),
                    data -> data.getContactDetailsWrapper().getRespondentSolicitorDxNumber(),
                    FinremCaseData::getRespSolNotificationsEmailConsent,
                    FinremCaseData::getRespondentOrganisationPolicy
                )
            )
        );
    }

    private static FinremCaseData getContestedApplicantFinremCaseData() {
        return FinremCaseData.builder()
            .applicantOrganisationPolicy(OrganisationPolicy
                .builder()
                .organisation(Organisation
                    .builder()
                    .organisationID("App ORG ID")
                    .organisationName("App ORG NAME")
                    .build())
                .build())
            .contactDetailsWrapper(ContactDetailsWrapper.builder()
                .nocParty(NoticeOfChangeParty.APPLICANT)
                .applicantRepresented(YesOrNo.NO)
                .applicantSolicitorName("Sol name")
                .applicantSolicitorFirm("sol firm")
                .applicantSolicitorAddress(Address.builder().addressLine1("Some Address").build())
                .applicantSolicitorEmail("some@email.com")
                .applicantSolicitorPhone("0123456789")
                .applicantSolicitorConsentForEmails(YesOrNo.YES)
                .solicitorReference("Sol ref")
                .build()
            ).build();
    }

    private static FinremCaseData getContestedApplicantNullRepresentationFinremCaseData() {
        return FinremCaseData.builder()
            .applicantOrganisationPolicy(OrganisationPolicy
                .builder()
                .organisation(Organisation
                    .builder()
                    .organisationID("App ORG ID")
                    .organisationName("App ORG NAME")
                    .build())
                .build())
            .contactDetailsWrapper(ContactDetailsWrapper.builder()
                .nocParty(NoticeOfChangeParty.APPLICANT)
                .applicantRepresented(null)
                .applicantSolicitorName("Sol name")
                .applicantSolicitorFirm("sol firm")
                .applicantSolicitorAddress(Address.builder().addressLine1("Some Address").build())
                .applicantSolicitorEmail("some@email.com")
                .applicantSolicitorPhone("0123456789")
                .applicantSolicitorConsentForEmails(YesOrNo.YES)
                .solicitorReference("Sol ref")
                .build()
            ).build();
    }

    private static FinremCaseData getConsentedApplicantFinremCaseData() {
        return FinremCaseData.builder()
            .applicantOrganisationPolicy(OrganisationPolicy
                .builder()
                .organisation(Organisation
                    .builder()
                    .organisationID("App ORG ID")
                    .organisationName("App ORG NAME")
                    .build())
                .build())
            .contactDetailsWrapper(ContactDetailsWrapper.builder()
                .nocParty(NoticeOfChangeParty.APPLICANT)
                .applicantRepresented(YesOrNo.NO)
                .solicitorName("Sol name")
                .solicitorFirm("sol firm")
                .solicitorAddress(Address.builder().addressLine1("Some Address").build())
                .solicitorFirm("0123456789")
                .solicitorEmail("some@email.com")
                .solicitorAgreeToReceiveEmails(YesOrNo.YES)
                .solicitorReference("Sol ref")
                .build()
            ).build();
    }

    private static FinremCaseData getContestedRespondentRepresentedFinremCaseData() {
        return FinremCaseData.builder()
            .respondentOrganisationPolicy(OrganisationPolicy
                .builder()
                .organisation(Organisation
                    .builder()
                    .organisationID("Resp ORG ID")
                    .organisationName("Resp ORG NAME")
                    .build())
                .build())
            .respSolNotificationsEmailConsent(YesOrNo.YES)
            .contactDetailsWrapper(ContactDetailsWrapper.builder()
                .nocParty(NoticeOfChangeParty.RESPONDENT)
                .contestedRespondentRepresented(YesOrNo.YES)
                .respondentSolicitorName("Sol name")
                .respondentSolicitorFirm("sol firm")
                .respondentSolicitorAddress(Address.builder().addressLine1("Some Address").build())
                .respondentSolicitorFirm("0123456789")
                .respondentSolicitorEmail("some@email.com")
                .respondentSolicitorDxNumber("DXnumber")
                .build()
            ).build();
    }

    private static FinremCaseData getConsentedRespondentRepresentedFinremCaseData() {
        return FinremCaseData.builder()
            .respondentOrganisationPolicy(OrganisationPolicy
                .builder()
                .organisation(Organisation
                    .builder()
                    .organisationID("Resp ORG ID")
                    .organisationName("Resp ORG NAME")
                    .build())
                .build())
            .respSolNotificationsEmailConsent(YesOrNo.YES)
            .contactDetailsWrapper(ContactDetailsWrapper.builder()
                .nocParty(NoticeOfChangeParty.RESPONDENT)
                .consentedRespondentRepresented(YesOrNo.YES)
                .respondentSolicitorName("Sol name")
                .respondentSolicitorFirm("sol firm")
                .respondentSolicitorAddress(Address.builder().addressLine1("Some Address").build())
                .respondentSolicitorFirm("0123456789")
                .respondentSolicitorEmail("some@email.com")
                .respondentSolicitorDxNumber("DXnumber")
                .build()
            ).build();
    }

    private static FinremCaseData getContestedNotRespondentRepresentedFinremCaseData() {
        return FinremCaseData.builder()
            .respondentOrganisationPolicy(OrganisationPolicy
                .builder()
                .organisation(Organisation
                    .builder()
                    .organisationID("Resp ORG ID")
                    .organisationName("Resp ORG NAME")
                    .build())
                .build())
            .respSolNotificationsEmailConsent(YesOrNo.YES)
            .contactDetailsWrapper(ContactDetailsWrapper.builder()
                .nocParty(NoticeOfChangeParty.RESPONDENT)
                .contestedRespondentRepresented(YesOrNo.NO)
                .respondentSolicitorName("Sol name")
                .respondentSolicitorFirm("sol firm")
                .respondentSolicitorAddress(Address.builder().addressLine1("Some Address").build())
                .respondentSolicitorFirm("0123456789")
                .respondentSolicitorEmail("some@email.com")
                .respondentSolicitorDxNumber("DXnumber")
                .build()
            ).build();
    }

    private static FinremCaseData getContestedNullRespondentRepresentedFinremCaseData() {
        return FinremCaseData.builder()
            .respondentOrganisationPolicy(OrganisationPolicy
                .builder()
                .organisation(Organisation
                    .builder()
                    .organisationID("Resp ORG ID")
                    .organisationName("Resp ORG NAME")
                    .build())
                .build())
            .respSolNotificationsEmailConsent(YesOrNo.YES)
            .contactDetailsWrapper(ContactDetailsWrapper.builder()
                .nocParty(NoticeOfChangeParty.RESPONDENT)
                .contestedRespondentRepresented(null)
                .respondentSolicitorName("Sol name")
                .respondentSolicitorFirm("sol firm")
                .respondentSolicitorAddress(Address.builder().addressLine1("Some Address").build())
                .respondentSolicitorFirm("0123456789")
                .respondentSolicitorEmail("some@email.com")
                .respondentSolicitorDxNumber("DXnumber")
                .build()
            ).build();
    }

    private static FinremCaseData getConsentedNotRespondentRepresentedFinremCaseData() {
        return FinremCaseData.builder()
            .respondentOrganisationPolicy(OrganisationPolicy
                .builder()
                .organisation(Organisation
                    .builder()
                    .organisationID("Resp ORG ID")
                    .organisationName("Resp ORG NAME")
                    .build())
                .build())
            .respSolNotificationsEmailConsent(YesOrNo.YES)
            .contactDetailsWrapper(ContactDetailsWrapper.builder()
                .nocParty(NoticeOfChangeParty.RESPONDENT)
                .consentedRespondentRepresented(YesOrNo.NO)
                .respondentSolicitorName("Sol name")
                .respondentSolicitorFirm("sol firm")
                .respondentSolicitorAddress(Address.builder().addressLine1("Some Address").build())
                .respondentSolicitorFirm("0123456789")
                .respondentSolicitorEmail("some@email.com")
                .respondentSolicitorDxNumber("DXnumber")
                .build()
            ).build();
    }
}

