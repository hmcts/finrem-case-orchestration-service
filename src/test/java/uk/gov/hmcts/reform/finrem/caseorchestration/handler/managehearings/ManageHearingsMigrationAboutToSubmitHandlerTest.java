package uk.gov.hmcts.reform.finrem.caseorchestration.handler.managehearings;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.managehearings.ManageHearingsMigrationService;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.MANAGE_HEARINGS_MIGRATION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class ManageHearingsMigrationAboutToSubmitHandlerTest {

    @InjectMocks
    private ManageHearingsMigrationAboutToSubmitHandler underTest;

    @Mock
    private ManageHearingsMigrationService manageHearingsMigrationService;

    @Test
    void testCanHandle() {
        assertCanHandle(underTest, ABOUT_TO_SUBMIT, CONTESTED, MANAGE_HEARINGS_MIGRATION);
    }

    @ValueSource(booleans = {true, false})
    @ParameterizedTest
    void shouldInvokeManageHearingsMigrationService(boolean wasMigrated) {
        // Arrange
        String caseReference = TestConstants.CASE_ID;
        FinremCaseData caseData = FinremCaseData.builder().build();
        FinremCallbackRequest request = FinremCallbackRequestFactory.from(Long.parseLong(caseReference),
            CONTESTED, caseData);
        when(manageHearingsMigrationService.wasMigrated(caseData)).thenReturn(wasMigrated);

        // Act
        underTest.handle(request, AUTH_TOKEN);

        verify(manageHearingsMigrationService, times(wasMigrated ? 0 : 1)).runManageHearingMigration(caseData, "ui");
        verify(manageHearingsMigrationService, times(wasMigrated ? 1 : 0)).revertManageHearingMigration(caseData);
    }
}
