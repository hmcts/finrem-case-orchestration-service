package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DefaultsConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.helper.DocumentWarningsHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.OnlineFormDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.util.TestLogger;
import uk.gov.hmcts.reform.finrem.caseorchestration.util.TestLogs;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class ConsentOrderInContestedAboutToSubmitHandlerTest {

    @TestLogs
    private final TestLogger logs = new TestLogger(ConsentOrderInContestedAboutToSubmitHandler.class);

    private ConsentOrderInContestedAboutToSubmitHandler handler;

    @Mock
    private BulkPrintDocumentService bulkPrintDocumentService;

    @Mock
    private CaseDataService caseDataService;

    @Mock
    private OnlineFormDocumentService onlineFormDocumentService;

    @Mock
    private DefaultsConfiguration defaultsConfiguration;

    @Mock
    private DocumentWarningsHelper documentWarningsHelper;

    @Mock
    private FinremCaseDetailsMapper finremCaseDetailsMapper;

    @BeforeEach
    void setup() {
        handler = new ConsentOrderInContestedAboutToSubmitHandler(finremCaseDetailsMapper,
            caseDataService,
            onlineFormDocumentService,
            defaultsConfiguration,
            documentWarningsHelper);
    }

    @Test
    void canHandle() {
        assertCanHandle(handler, CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.CONSENT_ORDER);
    }

    @Test
    void givenNonConsentedInContestedCase_whenHandle_thenGenerateMiniFormA() {
        FinremCallbackRequest finremCallbackRequest = buildBaseCallbackRequest();

        CaseDetails caseDetails = mock(CaseDetails.class);
        CaseDocument expectedMiniFormA = mock(CaseDocument.class);

        when(finremCaseDetailsMapper.mapToCaseDetails(finremCallbackRequest.getCaseDetails())).thenReturn(caseDetails);
        when(caseDataService.isConsentedInContestedCase(finremCallbackRequest.getCaseDetails())).thenReturn(false);
        when(onlineFormDocumentService.generateMiniFormA(AUTH_TOKEN, caseDetails)).thenReturn(expectedMiniFormA);
        when(defaultsConfiguration.getAssignedToJudgeDefault()).thenReturn("new_application@mailinator.com");

        // Act
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(finremCallbackRequest, AUTH_TOKEN);

        // Assert
        FinremCaseData actualCaseData = response.getData();
        assertThat(actualCaseData)
            .extracting(FinremCaseData::getMiniFormA, FinremCaseData::getAssignedToJudge)
            .containsExactly(expectedMiniFormA, "new_application@mailinator.com");
        assertThat(logs.getInfos()).contains(format("Defaulting AssignedToJudge fields for Case ID: %s", CASE_ID));
        verify(onlineFormDocumentService, never()).generateConsentedInContestedMiniFormA(caseDetails, AUTH_TOKEN);
    }

    private FinremCallbackRequest buildBaseCallbackRequest() {
        FinremCallbackRequest mockedCallbackRequest = mock(FinremCallbackRequest.class);
        FinremCaseDetails mockedCaseDetails = mock(FinremCaseDetails.class);
        when(mockedCallbackRequest.getCaseDetails()).thenReturn(mockedCaseDetails);

        FinremCaseData mockedCaseData = spy(FinremCaseData.class);
        when(mockedCaseDetails.getData()).thenReturn(mockedCaseData);
        when(mockedCaseDetails.getId()).thenReturn(Long.valueOf(CASE_ID));
        return mockedCallbackRequest;
    }
}
