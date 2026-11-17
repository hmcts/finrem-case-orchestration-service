
package uk.gov.hmcts.reform.finrem.caseorchestration.service.citizenui;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AccessCodeCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AccessCodeEntry;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ccd.CoreCaseDataService;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID_IN_LONG;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.RESPONDENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.INTERNAL_CHANGE_UPDATE_CASE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.TestConstants.CONTESTED;

@ExtendWith(MockitoExtension.class)
class CitizenPinInPostPersistenceServiceTest {

    @Mock
    private CoreCaseDataService coreCaseDataService;

    @Mock
    private FinremCaseDetailsMapper caseDetailsMapper;

    @Mock
    private CitizenPinInPostService citizenPinInPostService;

    @Mock
    private FeatureToggleService featureToggleService;

    @InjectMocks
    private CitizenPinInPostPersistenceService service;

    private FinremCaseDetails caseDetails;
    private FinremCaseData caseData;

    @BeforeEach
    void setUp() {
        caseDetails = mock(FinremCaseDetails.class);
        caseData = mock(FinremCaseData.class);
    }

    @Test
    void shouldNotPersistPinForConsentedCase() {
        when(caseDetails.getCaseType()).thenReturn(CaseType.CONSENTED);

        service.ensurePinPersisted(caseDetails, RESPONDENT);

        verifyNoInteractions(
            coreCaseDataService,
            caseDetailsMapper,
            citizenPinInPostService,
            featureToggleService
        );
    }

    @Test
    void shouldNotPersistPinForUnsupportedRecipient() {
        when(caseDetails.getCaseType()).thenReturn(CaseType.CONTESTED);

        service.ensurePinPersisted(caseDetails, null);

        verifyNoInteractions(
            coreCaseDataService,
            caseDetailsMapper,
            citizenPinInPostService,
            featureToggleService
        );
    }

    @Test
    void shouldNotPersistPinWhenCitizenUiDisabled() {
        when(caseDetails.getCaseType()).thenReturn(CaseType.CONTESTED);
        when(featureToggleService.isFinremCitizenUiEnabled())
            .thenReturn(false);

        service.ensurePinPersisted(caseDetails, RESPONDENT);

        verifyNoInteractions(
            coreCaseDataService,
            caseDetailsMapper,
            citizenPinInPostService
        );
    }

    @Test
    void shouldNotCallCcdWhenRecipientAlreadyHasActivePin() {
        when(caseDetails.getCaseType()).thenReturn(CaseType.CONTESTED);
        when(featureToggleService.isFinremCitizenUiEnabled()).thenReturn(true);
        when(caseDetails.getData()).thenReturn(caseData);

        AccessCodeEntry activePin = AccessCodeEntry.builder()
            .isValid(YesOrNo.YES)
            .build();

        when(caseData.getApplicantAccessCodes()).thenReturn(
            List.of(AccessCodeCollection.builder()
                .value(activePin)
                .build())
        );

        service.ensurePinPersisted(caseDetails, APPLICANT);

        verifyNoInteractions(coreCaseDataService);
        verifyNoInteractions(citizenPinInPostService);
        verifyNoInteractions(caseDetailsMapper);
    }

    @Test
    void shouldPersistApplicantPinAndUpdateCaseData() {
        givenEligibleCase();

        AtomicReference<Map<String, Object>> updates = stubCcdCallback(APPLICANT, true);

        service.ensurePinPersisted(caseDetails, APPLICANT);

        assertEquals(Map.of("applicantAccessCodes", serializedCodes()), updates.get());

        verify(caseData).setApplicantAccessCodes(Collections.emptyList());
        verify(caseData, never()).setRespondentAccessCodes(any());

        verify(coreCaseDataService).performPostSubmitCallback(
            eq(CaseType.CONTESTED),
            eq(CASE_ID_IN_LONG),
            eq(INTERNAL_CHANGE_UPDATE_CASE.getCcdType()),
            any()
        );
    }

    @Test
    void shouldPersistRespondentPinAndUpdateCaseData() {
        givenEligibleCase();

        AtomicReference<Map<String, Object>> updates =
            stubCcdCallback(RESPONDENT, true);

        service.ensurePinPersisted(caseDetails, RESPONDENT);

        assertEquals(
            Map.of("respondentAccessCodes", serializedCodes()),
            updates.get()
        );

        verify(caseData).setRespondentAccessCodes(Collections.emptyList());
        verify(caseData, never()).setApplicantAccessCodes(any());

        verify(coreCaseDataService).performPostSubmitCallback(
            eq(CaseType.CONTESTED),
            eq(CASE_ID_IN_LONG),
            eq(INTERNAL_CHANGE_UPDATE_CASE.getCcdType()),
            any()
        );
    }

    @Test
    void shouldSkipCcdUpdateWhenNoNewPinIsRequired() {
        givenEligibleCase();

        AtomicReference<Map<String, Object>> updates =
            stubCcdCallback(RESPONDENT, false);

        service.ensurePinPersisted(caseDetails, RESPONDENT);

        assertEquals(Collections.emptyMap(), updates.get());

        verify(caseDetailsMapper, never())
            .finremCaseDataToMap(any());

        // Existing codes are still synchronised onto
        // the original case data.
        verify(caseData)
            .setRespondentAccessCodes(Collections.emptyList());
    }

    private void givenEligibleCase() {
        when(caseDetails.getCaseType()).thenReturn(CaseType.CONTESTED);
        when(caseDetails.getId()).thenReturn(CASE_ID_IN_LONG);
        when(caseDetails.getData()).thenReturn(caseData);

        when(featureToggleService.isFinremCitizenUiEnabled())
            .thenReturn(true);
    }

    private AtomicReference<Map<String, Object>> stubCcdCallback(
        PaperNotificationRecipient recipient,
        boolean pinGenerated
    ) {
        CaseDetails latestCcdCase = mock(CaseDetails.class);

        FinremCaseDetails latestFinremDetails =
            mock(FinremCaseDetails.class);

        when(caseDetailsMapper.mapToFinremCaseDetails(latestCcdCase))
            .thenReturn(latestFinremDetails);

        when(citizenPinInPostService.generatePinIfNeeded(
            latestFinremDetails, recipient
        )).thenReturn(pinGenerated);

        if (pinGenerated) {
            FinremCaseData latestFinremData =
                mock(FinremCaseData.class);

            when(latestFinremDetails.getData())
                .thenReturn(latestFinremData);

            when(caseDetailsMapper.finremCaseDataToMap(latestFinremData))
                .thenReturn(Map.of(
                    recipient == APPLICANT
                        ? "applicantAccessCodes"
                        : "respondentAccessCodes",
                    serializedCodes()
                ));
        }
        CaseDetails updatedCcdCase = mock(CaseDetails.class);

        when(updatedCcdCase.getData()).thenReturn(Map.of());
        when(updatedCcdCase.getCaseTypeId()).thenReturn(CONTESTED);

        FinremCaseData persistedData =
            mock(FinremCaseData.class);
        when(caseDetailsMapper.mapToFinremCaseData(
            updatedCcdCase.getData(),
            CONTESTED
        )).thenReturn(persistedData);

        if (recipient == APPLICANT) {
            when(persistedData.getApplicantAccessCodes())
                .thenReturn(Collections.emptyList());
        } else {
            when(persistedData.getRespondentAccessCodes())
                .thenReturn(Collections.emptyList());
        }

        AtomicReference<Map<String, Object>> capturedUpdates =
            new AtomicReference<>();

        when(coreCaseDataService.performPostSubmitCallback(
            eq(CaseType.CONTESTED),
            eq(CASE_ID_IN_LONG),
            eq(INTERNAL_CHANGE_UPDATE_CASE.getCcdType()),
            any()
        )).thenAnswer(invocation -> {

            Function<CaseDetails, Map<String, Object>> updateFunction =
                invocation.getArgument(3);

            capturedUpdates.set(
                updateFunction.apply(latestCcdCase)
            );

            return updatedCcdCase;
        });

        return capturedUpdates;
    }

    private List<Map<String, Object>> serializedCodes() {
        return List.of(
            Map.of("id", "test-access-code")
        );
    }
}
