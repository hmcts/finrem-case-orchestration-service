package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Organisation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrganisationPolicy;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.NocLetterNotificationService;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
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

    static Stream<org.junit.jupiter.params.provider.Arguments> applicantSolicitorScenarios() {
        return Stream.of(
            Arguments.of("new@email.com", "org1", "old@email.com", "org2", true),
            Arguments.of("same@email.com", "org1", "same@email.com", "org1", false)
        );
    }

    @SneakyThrows
    @ParameterizedTest
    @MethodSource("applicantSolicitorScenarios")
    void applicantSolicitorAccessIsGrantedOrRevoked(String currentEmail, String currentOrgId, String previousEmail,
                                                    String previousOrgId, boolean shouldGrantRevoke) {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequestApplicantSolicitor(currentEmail, currentOrgId,
            previousEmail, previousOrgId);
        FinremCaseData caseData = finremCallbackRequest.getCaseDetails().getData();
        FinremCaseData caseDataBefore = finremCallbackRequest.getCaseDetailsBefore().getData();

        solicitorAccessService.checkAndAssignSolicitorAccess(caseData, caseDataBefore);

        if (shouldGrantRevoke) {
            verify(assignPartiesAccessService).grantApplicantSolicitor(caseData);
            verify(assignPartiesAccessService).revokeApplicantSolicitor(caseDataBefore);
        } else {
            verify(assignPartiesAccessService, never()).grantApplicantSolicitor(caseData);
            verify(assignPartiesAccessService, never()).revokeApplicantSolicitor(caseDataBefore);
        }
    }

    static Stream<org.junit.jupiter.params.provider.Arguments> respondentSolicitorScenarios() {
        return Stream.of(
            Arguments.of("new@email.com", "org1", "old@email.com", "org2", true),
            Arguments.of("same@email.com", "org1", "same@email.com", "org1", false)
        );
    }

    @SneakyThrows
    @ParameterizedTest
    @MethodSource("respondentSolicitorScenarios")
    void respondentSolicitorAccessIsGrantedOrRevoked(String currentEmail, String currentOrgId, String previousEmail,
                                                     String previousOrgId, boolean shouldGrantRevoke) {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequestRespondentSolicitor(currentEmail,
            currentOrgId, previousEmail, previousOrgId);
        FinremCaseData caseData = finremCallbackRequest.getCaseDetails().getData();
        FinremCaseData caseDataBefore = finremCallbackRequest.getCaseDetailsBefore().getData();

        solicitorAccessService.checkAndAssignSolicitorAccess(caseData, caseDataBefore);

        if (shouldGrantRevoke) {
            verify(assignPartiesAccessService).grantRespondentSolicitor(caseData);
            verify(assignPartiesAccessService).revokeRespondentSolicitor(caseDataBefore);
        } else {
            verify(assignPartiesAccessService, never()).grantRespondentSolicitor(caseData);
            verify(assignPartiesAccessService, never()).revokeRespondentSolicitor(caseDataBefore);
        }
    }

    @SneakyThrows
    @Test
    void applicantSolicitorAccessIsGrantedOnlyWhenApplicantBecomesRepresented() {
        // previous applicant was not represented, now is represented
        ContactDetailsWrapper contactDetailsWrapper = ContactDetailsWrapper.builder()
            .applicantRepresented(YesOrNo.YES)
            .applicantSolicitorEmail("new@email.com")
            .contestedRespondentRepresented(YesOrNo.NO)
            .build();
        ContactDetailsWrapper contactDetailsWrapperBefore = ContactDetailsWrapper.builder()
            .applicantRepresented(YesOrNo.NO)
            .applicantSolicitorEmail(null)
            .contestedRespondentRepresented(YesOrNo.NO)
            .build();
        FinremCaseData caseData = FinremCaseData.builder()
            .contactDetailsWrapper(contactDetailsWrapper)
            .applicantOrganisationPolicy(OrganisationPolicy.builder().organisation(Organisation.builder()
                .organisationID("org1").build()).build())
            .build();
        FinremCaseData caseDataBefore = FinremCaseData.builder()
            .contactDetailsWrapper(contactDetailsWrapperBefore)
            .applicantOrganisationPolicy(OrganisationPolicy.builder().organisation(Organisation.builder()
                .organisationID(null).build()).build())
            .build();
        solicitorAccessService.checkAndAssignSolicitorAccess(caseData, caseDataBefore);
        verify(assignPartiesAccessService).grantApplicantSolicitor(caseData);
        verify(assignPartiesAccessService, never()).revokeApplicantSolicitor(caseDataBefore);
    }

    @SneakyThrows
    @Test
    void applicantSolicitorAccessIsRevokedOnlyWhenApplicantNoLongerRepresented() {
        // previous applicant was represented, now is not represented
        ContactDetailsWrapper contactDetailsWrapper = ContactDetailsWrapper.builder()
            .applicantRepresented(YesOrNo.NO)
            .applicantSolicitorEmail(null)
            .contestedRespondentRepresented(YesOrNo.NO)
            .build();
        ContactDetailsWrapper contactDetailsWrapperBefore = ContactDetailsWrapper.builder()
            .applicantRepresented(YesOrNo.YES)
            .applicantSolicitorEmail("old@email.com")
            .contestedRespondentRepresented(YesOrNo.NO)
            .build();
        FinremCaseData caseData = FinremCaseData.builder()
            .contactDetailsWrapper(contactDetailsWrapper)
            .applicantOrganisationPolicy(OrganisationPolicy.builder().organisation(Organisation.builder()
                .organisationID(null).build()).build())
            .build();
        FinremCaseData caseDataBefore = FinremCaseData.builder()
            .contactDetailsWrapper(contactDetailsWrapperBefore)
            .applicantOrganisationPolicy(OrganisationPolicy.builder().organisation(Organisation.builder()
                .organisationID("org1").build()).build())
            .build();
        solicitorAccessService.checkAndAssignSolicitorAccess(caseData, caseDataBefore);
        verify(assignPartiesAccessService, never()).grantApplicantSolicitor(caseData);
        verify(assignPartiesAccessService).revokeApplicantSolicitor(caseDataBefore);
    }

    @SneakyThrows
    @Test
    void respondentSolicitorAccessIsGrantedOnlyWhenRespondentBecomesRepresented() {
        // previous respondent was not represented, now is represented
        ContactDetailsWrapper contactDetailsWrapper = ContactDetailsWrapper.builder()
            .applicantRepresented(YesOrNo.NO)
            .contestedRespondentRepresented(YesOrNo.YES)
            .respondentSolicitorEmail("new@email.com")
            .build();
        ContactDetailsWrapper contactDetailsWrapperBefore = ContactDetailsWrapper.builder()
            .applicantRepresented(YesOrNo.NO)
            .contestedRespondentRepresented(YesOrNo.NO)
            .respondentSolicitorEmail(null)
            .build();
        FinremCaseData caseData = FinremCaseData.builder()
            .contactDetailsWrapper(contactDetailsWrapper)
            .respondentOrganisationPolicy(OrganisationPolicy.builder().organisation(Organisation.builder()
                .organisationID("org1").build()).build())
            .build();
        FinremCaseData caseDataBefore = FinremCaseData.builder()
            .contactDetailsWrapper(contactDetailsWrapperBefore)
            .respondentOrganisationPolicy(OrganisationPolicy.builder().organisation(Organisation.builder()
                .organisationID(null).build()).build())
            .build();
        solicitorAccessService.checkAndAssignSolicitorAccess(caseData, caseDataBefore);
        verify(assignPartiesAccessService).grantRespondentSolicitor(caseData);
        verify(assignPartiesAccessService, never()).revokeRespondentSolicitor(caseDataBefore);
    }

    @SneakyThrows
    @Test
    void respondentSolicitorAccessIsRevokedOnlyWhenRespondentNoLongerRepresented() {
        // previous respondent was represented, now is not represented
        ContactDetailsWrapper contactDetailsWrapper = ContactDetailsWrapper.builder()
            .applicantRepresented(YesOrNo.NO)
            .contestedRespondentRepresented(YesOrNo.NO)
            .respondentSolicitorEmail(null)
            .build();
        ContactDetailsWrapper contactDetailsWrapperBefore = ContactDetailsWrapper.builder()
            .applicantRepresented(YesOrNo.NO)
            .contestedRespondentRepresented(YesOrNo.YES)
            .respondentSolicitorEmail("old@email.com")
            .build();
        FinremCaseData caseData = FinremCaseData.builder()
            .contactDetailsWrapper(contactDetailsWrapper)
            .respondentOrganisationPolicy(OrganisationPolicy.builder().organisation(Organisation.builder()
                .organisationID(null).build()).build())
            .build();
        FinremCaseData caseDataBefore = FinremCaseData.builder()
            .contactDetailsWrapper(contactDetailsWrapperBefore)
            .respondentOrganisationPolicy(OrganisationPolicy.builder().organisation(Organisation.builder()
                .organisationID("org1").build()).build())
            .build();
        solicitorAccessService.checkAndAssignSolicitorAccess(caseData, caseDataBefore);
        verify(assignPartiesAccessService, never()).grantRespondentSolicitor(caseData);
        verify(assignPartiesAccessService).revokeRespondentSolicitor(caseDataBefore);
    }

    @SneakyThrows
    @Test
    void sendNoticeOfChangeNotificationsCaseworker_sendsNotificationsWhenRequired() {
        // Arrange
        FinremCallbackRequest callbackRequest = mock(FinremCallbackRequest.class);
        FinremCaseDetails caseDetails = mock(FinremCaseDetails.class);
        FinremCaseDetails caseDetailsBefore = mock(FinremCaseDetails.class);
        NotificationService notificationService = mock(NotificationService.class);
        NocLetterNotificationService nocLetterNotificationService = mock(NocLetterNotificationService.class);
        FinremCaseDetailsMapper finremCaseDetailsMapper = mock(FinremCaseDetailsMapper.class);
        AssignPartiesAccessService mockAssignPartiesAccessService = mock(AssignPartiesAccessService.class);

        ContactDetailsWrapper contactDetailsWrapper = ContactDetailsWrapper.builder()
            .updateIncludesRepresentativeChange(YesOrNo.YES)
            .build();
        FinremCaseData caseData = FinremCaseData.builder()
            .contactDetailsWrapper(contactDetailsWrapper)
            .build();
        when(callbackRequest.getCaseDetails()).thenReturn(caseDetails);
        when(callbackRequest.getCaseDetailsBefore()).thenReturn(caseDetailsBefore);
        when(caseDetails.getData()).thenReturn(caseData);

        SolicitorAccessService service = new SolicitorAccessService(mockAssignPartiesAccessService, notificationService,
            nocLetterNotificationService, finremCaseDetailsMapper);
        // Act
        service.sendNoticeOfChangeNotificationsCaseworker(callbackRequest, AUTH_TOKEN);

        // Assert
        verify(notificationService, times(1)).sendNoticeOfChangeEmailCaseworker(any(FinremCaseDetails.class));
        verify(nocLetterNotificationService, times(1)).sendNoticeOfChangeLetters(any(), any(), eq(AUTH_TOKEN));
        assertNull(contactDetailsWrapper.getUpdateIncludesRepresentativeChange());
        assertNull(contactDetailsWrapper.getNocParty());
    }

    @SneakyThrows
    @Test
    void sendNoticeOfChangeNotificationsCaseworker_doesNotSendNotificationsWhenNotRequired() {
        FinremCallbackRequest callbackRequest = mock(FinremCallbackRequest.class);
        FinremCaseDetails caseDetails = mock(FinremCaseDetails.class);
        NotificationService notificationService = mock(NotificationService.class);
        NocLetterNotificationService nocLetterNotificationService = mock(NocLetterNotificationService.class);
        FinremCaseDetailsMapper finremCaseDetailsMapper = mock(FinremCaseDetailsMapper.class);
        AssignPartiesAccessService mockAssignPartiesAccessService = mock(AssignPartiesAccessService.class);

        ContactDetailsWrapper contactDetailsWrapper = ContactDetailsWrapper.builder()
            .updateIncludesRepresentativeChange(YesOrNo.NO)
            .build();
        FinremCaseData caseData = FinremCaseData.builder()
            .contactDetailsWrapper(contactDetailsWrapper)
            .build();
        when(callbackRequest.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getData()).thenReturn(caseData);

        SolicitorAccessService service = new SolicitorAccessService(mockAssignPartiesAccessService, notificationService,
            nocLetterNotificationService, finremCaseDetailsMapper);
        service.sendNoticeOfChangeNotificationsCaseworker(callbackRequest, "auth");

        verify(notificationService, never()).sendNoticeOfChangeEmailCaseworker(any(CaseDetails.class));
        verify(nocLetterNotificationService, never()).sendNoticeOfChangeLetters(any(), any(), any());
        assertEquals(YesOrNo.NO, contactDetailsWrapper.getUpdateIncludesRepresentativeChange());
    }

    @SneakyThrows
    @Test
    void sendNoticeOfChangeNotificationsCaseworker_handlesNullContactDetailsWrapper() {
        FinremCallbackRequest callbackRequest = mock(FinremCallbackRequest.class);
        FinremCaseDetails caseDetails = mock(FinremCaseDetails.class);
        NotificationService notificationService = mock(NotificationService.class);
        NocLetterNotificationService nocLetterNotificationService = mock(NocLetterNotificationService.class);
        FinremCaseDetailsMapper finremCaseDetailsMapper = mock(FinremCaseDetailsMapper.class);
        AssignPartiesAccessService mockAssignPartiesAccessService = mock(AssignPartiesAccessService.class);

        FinremCaseData caseData = FinremCaseData.builder().contactDetailsWrapper(null).build();
        when(callbackRequest.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getData()).thenReturn(caseData);

        SolicitorAccessService service = new SolicitorAccessService(mockAssignPartiesAccessService, notificationService,
            nocLetterNotificationService, finremCaseDetailsMapper);
        service.sendNoticeOfChangeNotificationsCaseworker(callbackRequest, AUTH_TOKEN);

        verify(notificationService, never()).sendNoticeOfChangeEmailCaseworker(any(CaseDetails.class));
        verify(nocLetterNotificationService, never()).sendNoticeOfChangeLetters(any(), any(), any());
    }

    private FinremCallbackRequest buildCallbackRequestApplicantSolicitor(String applicantSolicitorEmail,
                                                                         String applicantOrganisationId,
                                                                         String beforeApplicantSolicitorEmail,
                                                                         String beforeApplicantOrganisationId) {

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
            .applicantOrganisationPolicy(OrganisationPolicy.builder().organisation(Organisation.builder()
                .organisationID(applicantOrganisationId).build()).build())
            .build();

        FinremCaseData caseDataBefore = FinremCaseData.builder()
            .contactDetailsWrapper(contactDetailsWrapperBefore)
            .applicantOrganisationPolicy(OrganisationPolicy.builder().organisation(Organisation.builder()
                .organisationID(beforeApplicantOrganisationId).build()).build())
            .build();

        return FinremCallbackRequest.builder()
            .caseDetailsBefore(FinremCaseDetails.builder().id(123L).caseType(CONTESTED).data(caseData).build())
            .caseDetails(FinremCaseDetails.builder().id(123L).caseType(CONTESTED).data(caseDataBefore).build())
            .build();
    }

    private FinremCallbackRequest buildCallbackRequestRespondentSolicitor(String respondentSolicitorEmail,
                                                                          String respondentOrganisationId,
                                                                          String beforeRespondentSolicitorEmail,
                                                                          String beforeRespondentOrganisationId) {

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
            .respondentOrganisationPolicy(OrganisationPolicy.builder().organisation(Organisation.builder()
                .organisationID(respondentOrganisationId).build()).build())
            .build();

        FinremCaseData caseDataBefore = FinremCaseData.builder()
            .contactDetailsWrapper(contactDetailsWrapperBefore)
            .respondentOrganisationPolicy(OrganisationPolicy.builder().organisation(Organisation.builder()
                .organisationID(beforeRespondentOrganisationId).build()).build())
            .build();

        return FinremCallbackRequest.builder()
            .caseDetailsBefore(FinremCaseDetails.builder().id(123L).caseType(CONTESTED).data(caseData).build())
            .caseDetails(FinremCaseDetails.builder().id(123L).caseType(CONTESTED).data(caseDataBefore).build())
            .build();
    }
}
