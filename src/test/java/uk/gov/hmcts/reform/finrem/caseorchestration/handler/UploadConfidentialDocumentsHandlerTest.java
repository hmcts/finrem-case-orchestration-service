package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.UploadedConfidentialDocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ConfidentialUploadedDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ConfidentialUploadedDocumentData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONFIDENTIAL_DOCS_UPLOADED_COLLECTION;

@RunWith(MockitoJUnitRunner.class)
public class UploadConfidentialDocumentsHandlerTest {

    public static final String AUTH_TOKEN = "tokien:)";

    ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private UploadConfidentialDocumentsAboutToSubmitHandler uploadConfidentialDocumentsAboutToSubmitHandler;

    private final List<ConfidentialUploadedDocumentData> uploadDocumentList = new ArrayList<>();
    private final List<ConfidentialUploadedDocumentData> existingDocumentList = new ArrayList<>();
    private final List<String> expectedDocumentIdList = new ArrayList<>();
    List<ConfidentialUploadedDocumentData> handledDocumentList = new ArrayList<>();
    List<String> handledDocumentIdList = new ArrayList<>();

    private final UploadedConfidentialDocumentHelper uploadedConfidentialDocumentHelper = new UploadedConfidentialDocumentHelper(objectMapper);

    protected ConfidentialUploadedDocumentData createConfidentialUploadDocumentItem(String type, CaseDocument link,
                                                                                    String dateAdded, String fileName,
                                                                                    String comment) {
        return ConfidentialUploadedDocumentData.builder()
            .id(UUID.randomUUID().toString())
            .confidentialUploadedDocument(ConfidentialUploadedDocument
                .builder()
                .documentType(type)
                .documentLink(link)
                .documentDateAdded(dateAdded)
                .documentFileName(fileName)
                .documentComment(comment)
                .build())
            .build();
    }

    @Before
    public void setUpTest() {
        uploadConfidentialDocumentsAboutToSubmitHandler = new UploadConfidentialDocumentsAboutToSubmitHandler(
            objectMapper, uploadedConfidentialDocumentHelper);
    }

    @Test
    public void givenACcdCallbackContestedCase_WhenAnAboutToSubmitEventUploadConfidentialDocument_thenHandlerCanHandle() {
        assertThat(uploadConfidentialDocumentsAboutToSubmitHandler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.UPLOAD_CONFIDENTIAL_DOCUMENT),
            is(true));
    }

    @Test
    public void givenACcdCallbackConsentedCase_WhenAnAboutToSubmitEventUploadConfidentialDocument_thenHandlerCanHandle() {
        assertThat(uploadConfidentialDocumentsAboutToSubmitHandler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONSENTED, EventType.UPLOAD_CONFIDENTIAL_DOCUMENT),
            is(true));
    }

    @Test
    public void givenUploadConfidentialDocument_When_IsValid_ThenExecuteHandler() {
        CallbackRequest callbackRequest = buildCallbackRequest();
        CaseDocument documentLink = new CaseDocument("/fileUrl", "document.extension", "/binaryUrl");

        CaseDetails caseDetailsBefore = callbackRequest.getCaseDetailsBefore();
        ConfidentialUploadedDocumentData oldDoc = createConfidentialUploadDocumentItem(
            "Old", documentLink, "", "oldDocument.filename", "Old Example");
        existingDocumentList.add(oldDoc);
        caseDetailsBefore.getData().put(CONFIDENTIAL_DOCS_UPLOADED_COLLECTION, existingDocumentList);

        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        ConfidentialUploadedDocumentData newDoc = createConfidentialUploadDocumentItem(
            "New", documentLink, "", "newDocument.filename", "New Example");
        uploadDocumentList.add(newDoc);
        uploadDocumentList.add(oldDoc);
        caseDetails.getData().put(CONFIDENTIAL_DOCS_UPLOADED_COLLECTION, uploadDocumentList);

        expectedDocumentIdList.add(newDoc.getId());
        expectedDocumentIdList.add(oldDoc.getId());

        handledDocumentList.addAll(
            (List<ConfidentialUploadedDocumentData>) uploadConfidentialDocumentsAboutToSubmitHandler.handle(
                callbackRequest, AUTH_TOKEN).getData().get(CONFIDENTIAL_DOCS_UPLOADED_COLLECTION));

        handledDocumentList.forEach(doc -> handledDocumentIdList.add(doc.getId()));

        assertThat(handledDocumentIdList.equals(expectedDocumentIdList), is(true));
    }

    private CallbackRequest buildCallbackRequest() {
        Map<String, Object> caseData = new HashMap<>();
        Map<String, Object> caseDataBefore = new HashMap<>();
        CaseDetails caseDetails = CaseDetails.builder().id(123L).data(caseData).build();
        CaseDetails caseDetailsBefore = CaseDetails.builder().id(123L).data(caseDataBefore).build();
        return CallbackRequest.builder().eventId(EventType.UPLOAD_CONFIDENTIAL_DOCUMENT.getCcdType())
            .caseDetails(caseDetails).caseDetailsBefore(caseDetailsBefore).build();
    }
}