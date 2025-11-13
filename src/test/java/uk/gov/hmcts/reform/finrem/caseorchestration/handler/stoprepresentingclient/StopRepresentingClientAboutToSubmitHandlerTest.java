package uk.gov.hmcts.reform.finrem.caseorchestration.handler.stoprepresentingclient;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.provider.Arguments;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.STOP_REPRESENTING_CLIENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class StopRepresentingClientAboutToSubmitHandlerTest {

    private StopRepresentingClientAboutToSubmitHandler underTest;
    @Mock
    private FinremCaseDetailsMapper finremCaseDetailsMapper;

    @BeforeEach
    public void setup() {
        underTest = new StopRepresentingClientAboutToSubmitHandler(finremCaseDetailsMapper);
    }

    @Test
    void testCanHandle() {
        assertCanHandle(underTest,
            Arguments.of(ABOUT_TO_SUBMIT, CONTESTED, STOP_REPRESENTING_CLIENT),
            Arguments.of(ABOUT_TO_SUBMIT, CONSENTED, STOP_REPRESENTING_CLIENT));
    }

    @Test
    void givenSessionWrapperExists_whenHandled_thenRemoveTemporaryFields() {
        FinremCaseDetails finremCaseDetails = mock(FinremCaseDetails.class);

        Map<String, Object> mapWithSessionWrapperProperties = new HashMap<>();
        mapWithSessionWrapperProperties.put("loginAsApplicantSolicitor", YesOrNo.YES);
        mapWithSessionWrapperProperties.put("loginAsRespondentSolicitor", YesOrNo.YES);

        CaseDetails caseDetails = CaseDetails.builder().data(mapWithSessionWrapperProperties).build();
        when(finremCaseDetailsMapper.mapToCaseDetails(finremCaseDetails)).thenReturn(caseDetails);

        FinremCaseDetails newFinremCaseDetails = mock(FinremCaseDetails.class);
        FinremCaseData resultFinremCaseData = mock(FinremCaseData.class);
        when(newFinremCaseDetails.getData()).thenReturn(resultFinremCaseData);
        when(finremCaseDetailsMapper.mapToFinremCaseDetails(CaseDetails.builder().data(Map.of()).build()))
            .thenReturn(newFinremCaseDetails);

        assertThat(
            underTest.handle(FinremCallbackRequest.builder().caseDetails(finremCaseDetails).build(), AUTH_TOKEN).getData()
        ).isEqualTo(resultFinremCaseData);
    }
}
