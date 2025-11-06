package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ConfidentialUploadedDocumentData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.UploadCaseDocumentWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.LegacyConfidentialDocumentsService;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.MANAGE_CASE_DOCUMENTS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class ManageCaseDocumentsContestedAboutToStartHandlerTest {

    @Mock
    private LegacyConfidentialDocumentsService legacyConfidentialDocumentsService;
    
    @InjectMocks
    private ManageCaseDocumentsContestedAboutToStartHandler manageCaseDocumentsAboutToStartCaseHandler;

    @Test
    void testCanHandle() {
        assertCanHandle(manageCaseDocumentsAboutToStartCaseHandler,
            CallbackType.ABOUT_TO_START, CaseType.CONTESTED, MANAGE_CASE_DOCUMENTS);
    }

    @Test
    void givenAnyCase_whenHandle_thenPopulateAllManageableCollections() {
        List<UploadCaseDocumentCollection> allManageableCollections = mock(List.class);
        UploadCaseDocumentWrapper mockedUploadCaseDocumentWrapper = mock(UploadCaseDocumentWrapper.class);
        lenient().when(mockedUploadCaseDocumentWrapper.getAllManageableCollections()).thenReturn(allManageableCollections);
        FinremCaseData caseData = FinremCaseData.builder()
            .uploadCaseDocumentWrapper(mockedUploadCaseDocumentWrapper)
            .build();
        FinremCallbackRequest request = FinremCallbackRequestFactory.from(MANAGE_CASE_DOCUMENTS,
            FinremCaseDetails.builder().data(caseData));

        FinremCaseData responseData = manageCaseDocumentsAboutToStartCaseHandler.handle(request, AUTH_TOKEN).getData();

        assertThat(responseData.getManageCaseDocumentsWrapper().getManageCaseDocumentCollection())
            .isEqualTo(allManageableCollections);
    }

    @Test
    void givenAnyCase_whenHandle_thenMigrateLegacyConfidentialCaseDocumentFormat() {
        List<UploadCaseDocumentCollection> allManageableCollections = mock(List.class);
        UploadCaseDocumentWrapper mockedUploadCaseDocumentWrapper = mock(UploadCaseDocumentWrapper.class);
        lenient().when(mockedUploadCaseDocumentWrapper.getAllManageableCollections()).thenReturn(allManageableCollections);

        List<ConfidentialUploadedDocumentData> mockedConfidentialDocumentsUploaded = mock(List.class);
        List<UploadCaseDocumentCollection> mockedUploadCaseDocumentCollections = mock(List.class);
        lenient().when(legacyConfidentialDocumentsService
            .mapLegacyConfidentialDocumentToConfidentialDocumentCollection(mockedConfidentialDocumentsUploaded))
            .thenReturn(mockedUploadCaseDocumentCollections);

        FinremCaseData caseData = FinremCaseData.builder()
            .uploadCaseDocumentWrapper(mockedUploadCaseDocumentWrapper)
            .confidentialDocumentsUploaded(mockedConfidentialDocumentsUploaded)
            .build();
        FinremCallbackRequest request = FinremCallbackRequestFactory.from(MANAGE_CASE_DOCUMENTS,
            FinremCaseDetails.builder().data(caseData));

        manageCaseDocumentsAboutToStartCaseHandler.handle(request, AUTH_TOKEN);

        verify(allManageableCollections).addAll(mockedUploadCaseDocumentCollections);
        verify(mockedConfidentialDocumentsUploaded).clear();
        verify(legacyConfidentialDocumentsService)
            .mapLegacyConfidentialDocumentToConfidentialDocumentCollection(mockedConfidentialDocumentsUploaded);
    }

    @Test
    void givenAnyCase_whenHandle_thenPopulateMissingConfidentialFlag() {
        List<UploadCaseDocumentCollection> allManageableCollections = new ArrayList<>();
        allManageableCollections.add(toUploadCaseDocumentCollection(null));
        allManageableCollections.add(toUploadCaseDocumentCollection(YesOrNo.YES));
        allManageableCollections.add(toUploadCaseDocumentCollection(YesOrNo.NO));
        allManageableCollections.add(toUploadCaseDocumentCollection(null));

        UploadCaseDocumentWrapper mockedUploadCaseDocumentWrapper = mock(UploadCaseDocumentWrapper.class);
        lenient().when(mockedUploadCaseDocumentWrapper.getAllManageableCollections()).thenReturn(allManageableCollections);

        FinremCaseData caseData = FinremCaseData.builder()
            .uploadCaseDocumentWrapper(mockedUploadCaseDocumentWrapper)
            .build();
        FinremCallbackRequest request = FinremCallbackRequestFactory.from(MANAGE_CASE_DOCUMENTS,
            FinremCaseDetails.builder().data(caseData));

        FinremCaseData responseData = manageCaseDocumentsAboutToStartCaseHandler.handle(request, AUTH_TOKEN).getData();

        assertThat(responseData.getManageCaseDocumentsWrapper().getManageCaseDocumentCollection())
            .map(UploadCaseDocumentCollection::getUploadCaseDocument)
            .map(UploadCaseDocument::getCaseDocumentConfidentiality)
            .containsExactly(YesOrNo.NO, YesOrNo.YES, YesOrNo.NO, YesOrNo.NO);
    }

    private UploadCaseDocumentCollection toUploadCaseDocumentCollection(YesOrNo confidentiality) {
        return UploadCaseDocumentCollection.builder()
            .uploadCaseDocument(UploadCaseDocument.builder().caseDocumentConfidentiality(confidentiality).build())
            .build();
    }
}
