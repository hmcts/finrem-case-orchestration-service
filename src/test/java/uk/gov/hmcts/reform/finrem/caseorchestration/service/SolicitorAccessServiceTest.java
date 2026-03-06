package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Organisation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrganisationPolicy;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;

class SolicitorAccessServiceTest {
    @Mock
    private AssignPartiesAccessService assignPartiesAccessService;
    @InjectMocks
    private SolicitorAccessService solicitorAccessService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }


    @SneakyThrows
    @Test
    void givenApplicantSolicitorEmailChanged_ShouldGrantAndRevokesAccess() {
        List<String> errors = new ArrayList<>();
        FinremCallbackRequest finremCallbackRequest =
            buildCallbackRequestApplicantSolicitor("new@email.com", "org1", "old@email.com", "org2");
        FinremCaseData caseData = finremCallbackRequest.getCaseDetails().getData();
        FinremCaseData caseDataBefore = finremCallbackRequest.getCaseDetailsBefore().getData();

        solicitorAccessService.checkAndAssignSolicitorAccess(caseData, caseDataBefore, errors);

        verify(assignPartiesAccessService).grantApplicantSolicitor(caseData);
        verify(assignPartiesAccessService).revokeApplicantSolicitor(caseDataBefore);
    }

    @SneakyThrows
    @Test
    void givenApplicantSolicitorEmailNotChanged_ShouldGrantAndRevokesAccess_doesNotGrantOrRevoke() {
        List<String> errors = new ArrayList<>();
        FinremCallbackRequest finremCallbackRequest =
            buildCallbackRequestApplicantSolicitor("same@email.com", "org1", "same@email.com", "org1");
        FinremCaseData caseData = finremCallbackRequest.getCaseDetails().getData();
        FinremCaseData caseDataBefore = finremCallbackRequest.getCaseDetailsBefore().getData();

        solicitorAccessService.checkAndAssignSolicitorAccess(caseData, caseDataBefore, errors);

        verify(assignPartiesAccessService, never()).grantApplicantSolicitor(caseData);
        verify(assignPartiesAccessService, never()).revokeApplicantSolicitor(caseDataBefore);
    }

    @SneakyThrows
    @Test
    void givenRespondentSolicitorEmailChanged_ShouldGrantAndRevokesAccess() {
        List<String> errors = new ArrayList<>();
        FinremCallbackRequest finremCallbackRequest =
            buildCallbackRequestRespondentSolicitor("new@email.com", "org1", "old@email.com", "org2");
        FinremCaseData caseData = finremCallbackRequest.getCaseDetails().getData();
        FinremCaseData caseDataBefore = finremCallbackRequest.getCaseDetailsBefore().getData();
        caseDataBefore.setCcdCaseId(finremCallbackRequest.getCaseDetails().getCaseIdAsString());

        solicitorAccessService.checkAndAssignSolicitorAccess(caseData, caseDataBefore, errors);

        verify(assignPartiesAccessService).grantRespondentSolicitor(caseData);
        verify(assignPartiesAccessService).revokeRespondentSolicitor(caseDataBefore);
    }

    @SneakyThrows
    @Test
    void givenRespondentSolicitorEmailNotChanged_ShouldNotGrantAndRevokesAccess() {
        List<String> errors = new ArrayList<>();
        FinremCallbackRequest finremCallbackRequest =
            buildCallbackRequestRespondentSolicitor("same@email.com", "org1", "same@email.com", "org1");
        FinremCaseData caseData = finremCallbackRequest.getCaseDetails().getData();
        FinremCaseData caseDataBefore = finremCallbackRequest.getCaseDetailsBefore().getData();

        solicitorAccessService.checkAndAssignSolicitorAccess(caseData, caseDataBefore, errors);

        verify(assignPartiesAccessService, never()).grantRespondentSolicitor(caseData);
        verify(assignPartiesAccessService, never()).revokeRespondentSolicitor(caseDataBefore);
    }

    private FinremCallbackRequest buildCallbackRequestApplicantSolicitor(String applicantSolicitorEmail, String applicantOrganisationId,
                                                       String beforeApplicantSolicitorEmail, String beforeApplicantOrganisationId) {

        ContactDetailsWrapper contactDetailsWrapper = ContactDetailsWrapper.builder()
            .applicantRepresented(YesOrNo.YES)
            .applicantSolicitorEmail(applicantSolicitorEmail)
            .contestedRespondentRepresented(YesOrNo.NO)
            .build();

        ContactDetailsWrapper contactDetailsWrapperBefore = ContactDetailsWrapper.builder()
            .applicantRepresented(YesOrNo.YES)
            .applicantSolicitorEmail(beforeApplicantSolicitorEmail)
            .contestedRespondentRepresented(YesOrNo.NO)
            .build();

        FinremCaseData caseData = FinremCaseData.builder()
            .contactDetailsWrapper(contactDetailsWrapper)
            .applicantOrganisationPolicy(OrganisationPolicy.builder().organisation(Organisation.builder().organisationID(applicantOrganisationId).build()).build())
            .build();

        FinremCaseData caseDataBefore = FinremCaseData.builder()
            .contactDetailsWrapper(contactDetailsWrapperBefore)
            .applicantOrganisationPolicy(OrganisationPolicy.builder().organisation(Organisation.builder().organisationID(beforeApplicantOrganisationId).build()).build())
            .build();

        return FinremCallbackRequest.builder()
            .caseDetailsBefore(FinremCaseDetails.builder().id(123L).caseType(CONTESTED).data(caseData).build())
            .caseDetails(FinremCaseDetails.builder().id(123L).caseType(CONTESTED).data(caseDataBefore).build())
            .build();
    }

    private FinremCallbackRequest buildCallbackRequestRespondentSolicitor(String respondentSolicitorEmail, String respondentOrganisationId,
                                                       String beforeRespondentSolicitorEmail, String beforeRespondentOrganisationId) {

        ContactDetailsWrapper contactDetailsWrapper = ContactDetailsWrapper.builder()
            .applicantRepresented(YesOrNo.NO)
            .contestedRespondentRepresented(YesOrNo.YES)
            .respondentSolicitorEmail(respondentSolicitorEmail)
            .build();

        ContactDetailsWrapper contactDetailsWrapperBefore = ContactDetailsWrapper.builder()
            .applicantRepresented(YesOrNo.NO)
            .contestedRespondentRepresented(YesOrNo.YES)
            .respondentSolicitorEmail(beforeRespondentSolicitorEmail)
            .build();

        FinremCaseData caseData = FinremCaseData.builder()
            .contactDetailsWrapper(contactDetailsWrapper)
            .respondentOrganisationPolicy(OrganisationPolicy.builder().organisation(Organisation.builder().organisationID(respondentOrganisationId).build()).build())
            .build();

        FinremCaseData caseDataBefore = FinremCaseData.builder()
            .contactDetailsWrapper(contactDetailsWrapperBefore)
            .respondentOrganisationPolicy(OrganisationPolicy.builder().organisation(Organisation.builder().organisationID(beforeRespondentOrganisationId).build()).build())
            .build();

        return FinremCallbackRequest.builder()
            .caseDetailsBefore(FinremCaseDetails.builder().id(123L).caseType(CONTESTED).data(caseData).build())
            .caseDetails(FinremCaseDetails.builder().id(123L).caseType(CONTESTED).data(caseDataBefore).build())
            .build();
    }
}
