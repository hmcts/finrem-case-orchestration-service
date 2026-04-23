package uk.gov.hmcts.reform.finrem.caseorchestration.service.intervener;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerAction;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerChangeDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenerateCoverSheetService;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;

@SpringBootTest
class IntervenerCoversheetServiceTest {

    @InjectMocks
    private IntervenerCoversheetService intervenerCoversheetService;

    @Mock
    private GenerateCoverSheetService generateCoverSheetService;

    @Test
    void shouldGenerateAndStoreCoversheetWhenIntervenerAdded() {
        FinremCaseDetails caseDetails = new FinremCaseDetails();
        IntervenerChangeDetails changeDetails = new IntervenerChangeDetails();
        changeDetails.setIntervenerAction(IntervenerAction.ADDED);
        changeDetails.setIntervenerType(IntervenerType.INTERVENER_ONE);

        doNothing().when(generateCoverSheetService)
                .generateAndStoreIntervenerCoversheet(caseDetails, changeDetails.getIntervenerType(), AUTH_TOKEN);

        intervenerCoversheetService.updateIntervenerCoversheet(caseDetails, changeDetails, AUTH_TOKEN);

        verify(generateCoverSheetService).generateAndStoreIntervenerCoversheet(caseDetails,
            changeDetails.getIntervenerType(), AUTH_TOKEN);
    }

    @Test
    void shouldRemoveCoversheetWhenIntervenerRemoved() {
        FinremCaseDetails caseDetails = new FinremCaseDetails();
        IntervenerChangeDetails changeDetails = new IntervenerChangeDetails();
        changeDetails.setIntervenerAction(IntervenerAction.REMOVED);

        doNothing().when(generateCoverSheetService)
                .removeIntervenerCoverSheet(caseDetails, changeDetails, AUTH_TOKEN);

        intervenerCoversheetService.updateIntervenerCoversheet(caseDetails, changeDetails, AUTH_TOKEN);

        verify(generateCoverSheetService).removeIntervenerCoverSheet(caseDetails, changeDetails, AUTH_TOKEN);
    }

    @Test
    void shouldDoNothingWhenIntervenerActionIsNotSupported() {
        FinremCaseDetails caseDetails = new FinremCaseDetails();
        IntervenerChangeDetails changeDetails = new IntervenerChangeDetails();
        changeDetails.setIntervenerAction(null);

        intervenerCoversheetService.updateIntervenerCoversheet(caseDetails, changeDetails, AUTH_TOKEN);

        verifyNoInteractions(generateCoverSheetService);
    }
}
