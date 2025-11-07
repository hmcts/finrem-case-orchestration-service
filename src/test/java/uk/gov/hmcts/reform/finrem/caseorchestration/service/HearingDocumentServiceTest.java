package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.hearing.FinremFormCandGCorresponder;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;

@ExtendWith(MockitoExtension.class)
class HearingDocumentServiceTest {

    @InjectMocks
    private HearingDocumentService hearingDocumentService;

    @Mock
    private FinremFormCandGCorresponder finremFormCandGCorresponder;

    @Test
    void testSendInitialHearingCorrespondence() {
        FinremCaseDetails mockedFinremCaseDetails = mock(FinremCaseDetails.class);
        hearingDocumentService.sendInitialHearingCorrespondence(mockedFinremCaseDetails, AUTH_TOKEN);
        verify(finremFormCandGCorresponder).sendCorrespondence(mockedFinremCaseDetails, AUTH_TOKEN);
    }

    @Test
    void testAddCourtFields() {
        Map<String, Object> caseData = new HashMap<>();
        CaseDetails caseDetails = mock(CaseDetails.class);
        when(caseDetails.getData()).thenReturn(caseData);

        try (MockedStatic<CaseHearingFunctions> mockedStatic = Mockito.mockStatic(CaseHearingFunctions.class)) {
            Map<String, Object> buildFrcCourtDetailsResult = mock(Map.class);
            mockedStatic.when(() -> CaseHearingFunctions.buildFrcCourtDetails(caseData))
                .thenReturn(buildFrcCourtDetailsResult);

            hearingDocumentService.addCourtFields(caseDetails);
            assertThat(caseDetails.getData().get("courtDetails")).isEqualTo(buildFrcCourtDetailsResult);
        }
    }
}
