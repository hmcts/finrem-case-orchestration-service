package uk.gov.hmcts.reform.finrem.caseorchestration.handler.scanneddocs;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ManageScannedDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ScannedDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ScannedDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;

public class ManageScannedDocsContestedAboutToStartHandlerTest {

    private ManageScannedDocsContestedAboutToStartHandler handler;

    @Before
    public void setup() {
        handler = new ManageScannedDocsContestedAboutToStartHandler(new FinremCaseDetailsMapper(new ObjectMapper()));
    }

    @Test
    public void givenCase_whenEventIsManageScannedDocument_thenCanHandle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_START, CaseType.CONTESTED, EventType.MANAGE_SCANNED_DOCS),
            is(true));
    }

    @Test
    public void givenCase_whenEventIsNotManageScannedDocument_thenCannotHandle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.AMEND_APP_DETAILS),
            is(false));
    }

    @Test
    public void givenCase_isWrongCasetype_thenCannotHandle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_START, CaseType.CONSENTED, EventType.MANAGE_SCANNED_DOCS),
            is(false));
    }

    @Test
    public void shouldMapScannedDocsCollectionToUploadCaseDocumentCollection() {
        LocalDateTime scannedDate = LocalDateTime.of(2020, 1, 1, 1, 1);
        String fileName = "file1";
        String documentUrl = "http://doc1";
        String documentBinaryUrl = "http://doc1/binary";
        String documentFilename = "doc1.pdf";

        FinremCaseData finremCaseData = FinremCaseData.builder().scannedDocuments(List.of(ScannedDocumentCollection.builder().value(
            ScannedDocument.builder()
                .scannedDate(scannedDate)
                .fileName(fileName)
                .url(CaseDocument.builder()
                    .documentUrl(documentUrl)
                    .documentBinaryUrl(documentBinaryUrl)
                    .documentFilename(documentFilename)
                    .build())
                .build()).build())).build();
        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from(finremCaseData);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(callbackRequest, AUTH_TOKEN);

        List<ManageScannedDocumentCollection> collection = response.getData().getManageScannedDocumentCollection();
        assertThat(collection, hasSize(1));
        assertThat(collection.get(0).getManageScannedDocument().getSelectForUpdate(), is(YesOrNo.NO));
        UploadCaseDocument uploadCaseDocument = collection.get(0).getManageScannedDocument().getUploadCaseDocument();
        assertThat(uploadCaseDocument.getFileName(), is(fileName));
        assertThat(uploadCaseDocument.getScannedDate(), is(scannedDate));
        assertThat(uploadCaseDocument.getCaseDocuments().getDocumentUrl(), is(documentUrl));
        assertThat(uploadCaseDocument.getCaseDocuments().getDocumentBinaryUrl(), is(documentBinaryUrl));
        assertThat(uploadCaseDocument.getCaseDocuments().getDocumentFilename(), is(documentFilename));
    }
}
