package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrganisationPolicy;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Organisation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SolicitorAccessServiceTest {
    @Mock
    private AssignPartiesAccessService assignPartiesAccessService;
    @InjectMocks
    private SolicitorAccessService solicitorAccessService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void addApplicantSolicitor_grantsAccess() {
        FinremCaseData caseData = mock(FinremCaseData.class);
        solicitorAccessService.addApplicantSolicitor(caseData);
        verify(assignPartiesAccessService).grantApplicantSolicitor(caseData);
    }

    @Test
    void removeApplicantSolicitor_revokesAccess() {
        FinremCaseData caseData = mock(FinremCaseData.class);
        solicitorAccessService.removeApplicantSolicitor(caseData);
        verify(assignPartiesAccessService).revokeApplicantSolicitor(caseData);
    }

    @Test
    void updateApplicantSolicitor_grantsAndRevokesAccessWhenChanged() {
        FinremCaseData caseData = FinremCaseData.builder()
            .contactDetailsWrapper(ContactDetailsWrapper.builder().applicantRepresented(YesOrNo.YES).applicantSolicitorEmail("new@email.com").build())
            .applicantOrganisationPolicy(OrganisationPolicy.builder().organisation(Organisation.builder().organisationID("org1").build()).build())
            .build();
        FinremCaseData caseDataBefore = FinremCaseData.builder()
            .contactDetailsWrapper(ContactDetailsWrapper.builder().applicantRepresented(YesOrNo.YES).applicantSolicitorEmail("old@email.com").build())
            .applicantOrganisationPolicy(OrganisationPolicy.builder().organisation(Organisation.builder().organisationID("org2").build()).build())
            .build();
        solicitorAccessService.updateApplicantSolicitor(caseData, caseDataBefore);
        verify(assignPartiesAccessService).grantApplicantSolicitor(caseData);
        verify(assignPartiesAccessService).revokeApplicantSolicitor(caseDataBefore);
    }

    @Test
    void addRespondentSolicitor_grantsAccess() {
        FinremCaseData caseData = mock(FinremCaseData.class);
        solicitorAccessService.addRespondentSolicitor(caseData);
        verify(assignPartiesAccessService).grantRespondentSolicitor(caseData);
    }

    @Test
    void removeRespondentSolicitor_revokesAccess() {
        FinremCaseData caseData = mock(FinremCaseData.class);
        solicitorAccessService.removeRespondentSolicitor(caseData);
        verify(assignPartiesAccessService).revokeRespondentSolicitor(caseData);
    }

    @Test
    void updateRespondentSolicitor_grantsAndRevokesAccessWhenChanged() {
        FinremCaseData caseData = FinremCaseData.builder()
            .contactDetailsWrapper(ContactDetailsWrapper.builder().contestedRespondentRepresented(YesOrNo.YES).respondentSolicitorEmail("new@email.com").build())
            .respondentOrganisationPolicy(OrganisationPolicy.builder().organisation(Organisation.builder().organisationID("org1").build()).build())
            .build();
        FinremCaseData caseDataBefore = FinremCaseData.builder()
            .contactDetailsWrapper(ContactDetailsWrapper.builder().contestedRespondentRepresented(YesOrNo.YES).respondentSolicitorEmail("old@email.com").build())
            .respondentOrganisationPolicy(OrganisationPolicy.builder().organisation(Organisation.builder().organisationID("org2").build()).build())
            .build();
        solicitorAccessService.updateRespondentSolicitor(caseData, caseDataBefore);
        verify(assignPartiesAccessService).grantRespondentSolicitor(caseData);
        verify(assignPartiesAccessService).revokeRespondentSolicitor(caseDataBefore);
    }

    @Test
    void hasApplicantSolicitorChanged_returnsTrueWhenEmailOrOrgChanged() {
        FinremCaseData caseData = FinremCaseData.builder()
            .contactDetailsWrapper(ContactDetailsWrapper.builder().applicantSolicitorEmail("new@email.com").build())
            .applicantOrganisationPolicy(OrganisationPolicy.builder().organisation(Organisation.builder().organisationID("org1").build()).build())
            .build();
        FinremCaseData caseDataBefore = FinremCaseData.builder()
            .contactDetailsWrapper(ContactDetailsWrapper.builder().applicantSolicitorEmail("old@email.com").build())
            .applicantOrganisationPolicy(OrganisationPolicy.builder().organisation(Organisation.builder().organisationID("org2").build()).build())
            .build();
        assertTrue(SolicitorAccessService.hasApplicantSolicitorChanged(caseData, caseDataBefore));
    }

    @Test
    void hasApplicantSolicitorChanged_returnsFalseWhenNoChange() {
        FinremCaseData caseData = FinremCaseData.builder()
            .contactDetailsWrapper(ContactDetailsWrapper.builder().applicantSolicitorEmail("same@email.com").build())
            .applicantOrganisationPolicy(OrganisationPolicy.builder().organisation(Organisation.builder().organisationID("org1").build()).build())
            .build();
        FinremCaseData caseDataBefore = FinremCaseData.builder()
            .contactDetailsWrapper(ContactDetailsWrapper.builder().applicantSolicitorEmail("same@email.com").build())
            .applicantOrganisationPolicy(OrganisationPolicy.builder().organisation(Organisation.builder().organisationID("org1").build()).build())
            .build();
        assertFalse(SolicitorAccessService.hasApplicantSolicitorChanged(caseData, caseDataBefore));
    }

    @Test
    void hasRespondentSolicitorChanged_returnsTrueWhenEmailOrOrgChanged() {
        FinremCaseData caseData = FinremCaseData.builder()
            .contactDetailsWrapper(ContactDetailsWrapper.builder().respondentSolicitorEmail("new@email.com").build())
            .respondentOrganisationPolicy(OrganisationPolicy.builder().organisation(Organisation.builder().organisationID("org1").build()).build())
            .build();
        FinremCaseData caseDataBefore = FinremCaseData.builder()
            .contactDetailsWrapper(ContactDetailsWrapper.builder().respondentSolicitorEmail("old@email.com").build())
            .respondentOrganisationPolicy(OrganisationPolicy.builder().organisation(Organisation.builder().organisationID("org2").build()).build())
            .build();
        assertTrue(SolicitorAccessService.hasRespondentSolicitorChanged(caseData, caseDataBefore));
    }

    @Test
    void hasRespondentSolicitorChanged_returnsFalseWhenNoChange() {
        FinremCaseData caseData = FinremCaseData.builder()
            .contactDetailsWrapper(ContactDetailsWrapper.builder().respondentSolicitorEmail("same@email.com").build())
            .respondentOrganisationPolicy(OrganisationPolicy.builder().organisation(Organisation.builder().organisationID("org1").build()).build())
            .build();
        FinremCaseData caseDataBefore = FinremCaseData.builder()
            .contactDetailsWrapper(ContactDetailsWrapper.builder().respondentSolicitorEmail("same@email.com").build())
            .respondentOrganisationPolicy(OrganisationPolicy.builder().organisation(Organisation.builder().organisationID("org1").build()).build())
            .build();
        assertFalse(SolicitorAccessService.hasRespondentSolicitorChanged(caseData, caseDataBefore));
    }
}