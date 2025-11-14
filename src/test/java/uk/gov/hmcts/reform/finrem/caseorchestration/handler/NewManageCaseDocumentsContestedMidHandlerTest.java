package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managecasedocuments.ManageCaseDocumentsAction;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ManageCaseDocumentsWrapper;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType.MID_EVENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.NEW_MANAGE_CASE_DOCUMENTS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class NewManageCaseDocumentsContestedMidHandlerTest {

    @InjectMocks
    private NewManageCaseDocumentsContestedMidHandler underTest;

    @Test
    void testCanHandle() {
        assertCanHandle(underTest, MID_EVENT, CONTESTED, NEW_MANAGE_CASE_DOCUMENTS);
    }

    @Test
    void givenAddNewSelected_whenHandle_thenPopulateAnEmptyEntry() {
        FinremCaseData caseData = FinremCaseData.builder()
            .manageCaseDocumentsWrapper(ManageCaseDocumentsWrapper.builder()
                .manageCaseDocumentsActionSelection(ManageCaseDocumentsAction.ADD_NEW)
                .build())
            .build();

        FinremCallbackRequest request = FinremCallbackRequestFactory.from(NEW_MANAGE_CASE_DOCUMENTS,
            FinremCaseDetails.builder().data(caseData));

        FinremCaseData responseData = underTest.handle(request, AUTH_TOKEN).getData();

        assertThat(responseData.getManageCaseDocumentsWrapper().getInputManageCaseDocumentCollection())
            .containsOnly(UploadCaseDocumentCollection.builder()
                .uploadCaseDocument(UploadCaseDocument.builder().build())
                .build());
    }
}
