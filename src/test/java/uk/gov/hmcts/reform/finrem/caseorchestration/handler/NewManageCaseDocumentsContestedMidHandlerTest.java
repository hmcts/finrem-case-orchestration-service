package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managecasedocuments.ManageCaseDocumentsAction;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ManageCaseDocumentsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.UploadCaseDocumentWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.LegacyConfidentialDocumentsService;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType.MID_EVENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.NEW_MANAGE_CASE_DOCUMENTS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class NewManageCaseDocumentsContestedMidHandlerTest {

    @InjectMocks
    private NewManageCaseDocumentsContestedMidHandler underTest;

    @Mock
    private LegacyConfidentialDocumentsService legacyConfidentialDocumentsService;

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

        FinremCaseData responseData = underTest.handle(FinremCallbackRequestFactory.from(caseData), AUTH_TOKEN)
            .getData();

        assertThat(responseData.getManageCaseDocumentsWrapper().getInputManageCaseDocumentCollection())
            .containsOnly(UploadCaseDocumentCollection.builder()
                .uploadCaseDocument(UploadCaseDocument.builder().build())
                .build());
    }

    @Test
    void givenAmendSelected_whenHandle_thenPopulateExistingDocuments() {
        var expectedDocuments = mock(List.class);
        UploadCaseDocumentWrapper uploadCaseDocumentWrapper = mock(UploadCaseDocumentWrapper.class);
        when(uploadCaseDocumentWrapper.getAllManageableCollections()).thenReturn(expectedDocuments);

        FinremCaseData caseData = FinremCaseData.builder()
            .uploadCaseDocumentWrapper(uploadCaseDocumentWrapper)
            .manageCaseDocumentsWrapper(ManageCaseDocumentsWrapper.builder()
                .manageCaseDocumentsActionSelection(ManageCaseDocumentsAction.AMEND)
                .build())
            .build();

        FinremCaseData responseData = underTest.handle(FinremCallbackRequestFactory.from(caseData), AUTH_TOKEN)
            .getData();
        assertThat(responseData.getManageCaseDocumentsWrapper().getInputManageCaseDocumentCollection())
            .isEqualTo(expectedDocuments);
    }

    @Test
    void givenAmendSelected_whenHandle_thenPopulateLegacyConfidentialCaseDocuments() {
        var expectedDocuments = mock(List.class);
        UploadCaseDocumentWrapper uploadCaseDocumentWrapper = mock(UploadCaseDocumentWrapper.class);
        when(uploadCaseDocumentWrapper.getAllManageableCollections()).thenReturn(expectedDocuments);
        var confidentialDocumentsUploaded = mock(List.class);

        FinremCaseData caseData = FinremCaseData.builder()
            .uploadCaseDocumentWrapper(uploadCaseDocumentWrapper)
            .manageCaseDocumentsWrapper(ManageCaseDocumentsWrapper.builder()
                .manageCaseDocumentsActionSelection(ManageCaseDocumentsAction.AMEND)
                .build())
            .confidentialDocumentsUploaded(confidentialDocumentsUploaded)
            .build();

        var legacyDocuments = mock(List.class);
        when(legacyConfidentialDocumentsService
            .mapLegacyConfidentialDocumentToConfidentialDocumentCollection(confidentialDocumentsUploaded))
            .thenReturn(legacyDocuments);

        FinremCaseData responseData = underTest.handle(FinremCallbackRequestFactory.from(caseData), AUTH_TOKEN)
            .getData();

        verify(expectedDocuments).addAll(legacyDocuments);
        assertThat(responseData.getManageCaseDocumentsWrapper().getInputManageCaseDocumentCollection())
            .isSameAs(expectedDocuments); // guarantee the same reference passed
    }

    @Test
    void givenAmendSelected_whenHandle_thenPopulateMissingConfidentialFlag() {
        UploadCaseDocumentCollection missingConfidentialFlag = uploadDocument();
        UploadCaseDocumentCollection legacyMissingConfidentialFlag = uploadDocument();

        var expectedDocuments = new ArrayList<>(List.of(missingConfidentialFlag));
        UploadCaseDocumentWrapper uploadCaseDocumentWrapper = mock(UploadCaseDocumentWrapper.class);
        when(uploadCaseDocumentWrapper.getAllManageableCollections()).thenReturn(expectedDocuments);
        var confidentialDocumentsUploaded = mock(List.class);

        FinremCaseData caseData = FinremCaseData.builder()
            .uploadCaseDocumentWrapper(uploadCaseDocumentWrapper)
            .manageCaseDocumentsWrapper(ManageCaseDocumentsWrapper.builder()
                .manageCaseDocumentsActionSelection(ManageCaseDocumentsAction.AMEND)
                .build())
            .confidentialDocumentsUploaded(confidentialDocumentsUploaded)
            .build();

        var legacyDocuments = List.of(legacyMissingConfidentialFlag);
        when(legacyConfidentialDocumentsService
            .mapLegacyConfidentialDocumentToConfidentialDocumentCollection(confidentialDocumentsUploaded))
            .thenReturn(legacyDocuments);

        underTest.handle(FinremCallbackRequestFactory.from(caseData), AUTH_TOKEN);

        assertThat(List.of(missingConfidentialFlag, legacyMissingConfidentialFlag))
            .map(UploadCaseDocumentCollection::getUploadCaseDocument)
            .extracting(UploadCaseDocument::getCaseDocumentConfidentiality)
            .containsOnly(YesOrNo.NO);
    }

    private UploadCaseDocumentCollection uploadDocument() {
        return UploadCaseDocumentCollection.builder()
            .uploadCaseDocument(UploadCaseDocument.builder()
                .caseDocumentConfidentiality(null)
                .build())
            .build();
    }
}
