package uk.gov.hmcts.reform.finrem.caseorchestration.handler.amendapplicationdetails;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.provider.Arguments;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseFlagsService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.OnlineFormDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.express.ExpressCaseService;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.AMEND_CONTESTED_APP_DETAILS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.AMEND_CONTESTED_PAPER_APP_DETAILS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.StageReached.DECREE_NISI;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class AmendApplicationDetailsAboutToSubmitHandlerTest {

    @InjectMocks
    private AmendApplicationDetailsAboutToSubmitHandler handler;
    
    @Mock
    private OnlineFormDocumentService onlineFormDocumentService;

    @Mock
    private CaseFlagsService caseFlagsService;

    @Mock
    private FeatureToggleService featureToggleService;

    @Mock
    private ExpressCaseService expressCaseService;

    @Test
    void testCanHandle() {
        assertCanHandle(handler,
            Arguments.of(ABOUT_TO_SUBMIT, CONTESTED, AMEND_CONTESTED_PAPER_APP_DETAILS),
            Arguments.of(ABOUT_TO_SUBMIT, CONTESTED, AMEND_CONTESTED_APP_DETAILS)
        );
    }

    // Migrated from {@code UpdateContestedCaseControllerTest.shouldDeleteNoDecreeAbsoluteWhenDecreeNisiSelectedBySolicitor}.
    @Test
    void givenDecreeNisiSelectedBySolicitor_whenHandled_thenDeleteNoDecreeAbsolute() {
        final CaseDocument generatedMiniFormA = mock(CaseDocument.class);
        final CaseDocument divorceUploadEvidence1 = mock(CaseDocument.class);
        final LocalDate divorceDecreeNisiDate = mock(LocalDate.class);
        final LocalDate divorceDecreeAbsoluteDate = mock(LocalDate.class);
        final LocalDate divorcePetitionIssuedDate = mock(LocalDate.class);

        FinremCaseData finremCaseData = spy(FinremCaseData.class);
        finremCaseData.setDivorceStageReached(DECREE_NISI);
        finremCaseData.setDivorceUploadEvidence1(divorceUploadEvidence1);
        finremCaseData.setDivorceUploadEvidence2(caseDocument("DivorceUploadEvidence2.pdf"));
        finremCaseData.setDivorceDecreeNisiDate(divorceDecreeNisiDate);
        finremCaseData.setDivorceDecreeAbsoluteDate(divorceDecreeAbsoluteDate);
        finremCaseData.setDivorceUploadPetition(caseDocument("DivorceUploadPetition.pdf"));
        finremCaseData.setDivorcePetitionIssuedDate(divorcePetitionIssuedDate);

        FinremCaseDetails finremCaseDetails = mock(FinremCaseDetails.class);
        when(finremCaseDetails.getData()).thenReturn(finremCaseData);
        FinremCallbackRequest finremCallbackRequest = mock(FinremCallbackRequest.class);
        when(finremCallbackRequest.getCaseDetails()).thenReturn(finremCaseDetails);
        when(onlineFormDocumentService.generateDraftContestedMiniFormA(eq(AUTH_TOKEN), eq(finremCaseDetails)))
            .thenReturn(generatedMiniFormA);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(finremCallbackRequest, AUTH_TOKEN);
        assertThat(response.getData()).extracting(
            FinremCaseData::getDivorceUploadEvidence2,
            FinremCaseData::getDivorceDecreeAbsoluteDate,
            FinremCaseData::getDivorceUploadPetition
        ).containsOnlyNulls();
        assertThat(response.getData()).extracting(
            FinremCaseData::getDivorcePetitionIssuedDate,
            FinremCaseData::getDivorceUploadEvidence1,
            FinremCaseData::getDivorceDecreeNisiDate,
            FinremCaseData::getMiniFormA
        ).containsExactly(
            divorcePetitionIssuedDate,
            divorceUploadEvidence1,
            divorceDecreeNisiDate,
            generatedMiniFormA
        );
    }
}
