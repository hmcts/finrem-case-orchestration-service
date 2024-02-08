package uk.gov.hmcts.reform.finrem.caseorchestration.handler.scanneddocs;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ScannedDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ScannedDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ScannedDocumentType;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;

class ManageScannedDocsContestedAboutToStartHandlerTest {

    private ManageScannedDocsContestedAboutToStartHandler handler;

    @BeforeEach
    public void setup() {
        handler = new ManageScannedDocsContestedAboutToStartHandler(new FinremCaseDetailsMapper(new ObjectMapper()));
    }

    @Test
    void givenCase_whenEventIsManageScannedDocument_thenCanHandle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_START, CaseType.CONTESTED, EventType.MANAGE_SCANNED_DOCS)).isTrue();
    }

    @Test
    void givenCase_whenEventIsNotManageScannedDocument_thenCannotHandle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.AMEND_APP_DETAILS)).isFalse();
    }

    @Test
    void givenCase_isWrongCasetype_thenCannotHandle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_START, CaseType.CONSENTED, EventType.MANAGE_SCANNED_DOCS)).isFalse();
    }

    @Test
    void givenCaseWithNoScannedDocs_whenHandleCalled_thenReturnsError() {
        FinremCallbackRequest request = FinremCallbackRequestFactory.create();

        var response  = handler.handle(request, AUTH_TOKEN);

        List<String> errors = response.getErrors();
        assertThat(errors.size()).isEqualTo(1);
        assertThat(errors.get(0)).isEqualTo("There are no scanned documents available");
    }

    @Test
    void givenCaseWithScannedDocs_whenHandleCalled_thenPopulatesMultiSelectList() {
        FinremCaseData caseData = FinremCaseData.builder()
            .scannedDocuments(List.of(
                createScannedDocument("167", "AAA111", ScannedDocumentType.CHERISHED),
                createScannedDocument("453", "BBB222", ScannedDocumentType.OTHER),
                createScannedDocument("797", "CCC333", ScannedDocumentType.FORM)
            ))
            .build();

        FinremCallbackRequest request = FinremCallbackRequestFactory.from(caseData);
        var response = handler.handle(request, AUTH_TOKEN);

        FinremCaseData responseCaseData = response.getData();
        DynamicMultiSelectList selectList = responseCaseData.getScannedDocsToUpdate();
        assertThat(selectList.getListItems().size()).isEqualTo(3);
        assertThat(selectList.getValue()).isNull();

        List<DynamicMultiSelectListElement> selectListItems = selectList.getListItems();
        assertThat(selectListItems.get(0).getCode()).isEqualTo("167");
        // TODO assertThat(selectListItems.get(0).getLabel()).isEqualTo("167");
        assertThat(selectListItems.get(1).getCode()).isEqualTo("453");
        // TODO assertThat(selectListItems.get(0).getLabel()).isEqualTo("167");
        assertThat(selectListItems.get(2).getCode()).isEqualTo("797");
        // TODO assertThat(selectListItems.get(0).getLabel()).isEqualTo("167");
    }

    private ScannedDocumentCollection createScannedDocument(String id, String controlNumber,
                                                            ScannedDocumentType documentType) {
        ScannedDocument scannedDocument = ScannedDocument.builder()
            .controlNumber(controlNumber)
            .type(documentType)
            .url(CaseDocument.builder().build())
            .build();
        return ScannedDocumentCollection.builder()
            .id(id)
            .value(scannedDocument)
            .build();
    }
}
