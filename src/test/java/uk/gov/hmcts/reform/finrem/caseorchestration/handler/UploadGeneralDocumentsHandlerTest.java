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
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.UploadedGeneralDocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralUploadedDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralUploadedDocumentData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_UPLOADED_DOCUMENTS;

@RunWith(MockitoJUnitRunner.class)
public class UploadGeneralDocumentsHandlerTest {

    public static final String AUTH_TOKEN = "tokien:)";

    ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private UploadGeneralDocumentsAboutToSubmitHandler uploadGeneralDocumentsAboutToSubmitHandler;

    private final List<GeneralUploadedDocumentData> uploadDocumentList = new ArrayList<>();
    private final List<GeneralUploadedDocumentData> existingDocumentList = new ArrayList<>();
    private final List<String> expectedDocumentIdList = new ArrayList<>();
    List<GeneralUploadedDocumentData> handledDocumentList = new ArrayList<>();
    List<String> handledDocumentIdList = new ArrayList<>();

    private final UploadedGeneralDocumentHelper uploadedGeneralDocumentHelper = new UploadedGeneralDocumentHelper(objectMapper);

    protected GeneralUploadedDocumentData createGeneralUploadDocumentItem(String type, String emailContent,
                                                                          CaseDocument link, String dateAdded, String comment,
                                                                          String fileName) {
        int leftLimit = 48;
        int rightLimit = 122;
        int targetStringLength = 10;
        Random random = new Random();

        String documentId = random.ints(leftLimit, rightLimit + 1)
            .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
            .limit(targetStringLength)
            .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
            .toString();

        return GeneralUploadedDocumentData.builder()
            .id(documentId)
            .generalUploadedDocument(GeneralUploadedDocument
                .builder()
                .documentType(type)
                .documentEmailContent(emailContent)
                .documentLink(link)
                .documentDateAdded(dateAdded)
                .documentComment(comment)
                .documentFileName(fileName)
                .build())
            .build();
    }

    @Before
    public void setUpTest() {
        uploadGeneralDocumentsAboutToSubmitHandler = new UploadGeneralDocumentsAboutToSubmitHandler(objectMapper, uploadedGeneralDocumentHelper);
    }

    @Test
    public void givenACcdCallbackContestedCase_WhenAnAboutToSubmitEventUploadGeneralDocument_thenHandlerCanHandle() {
        assertThat(uploadGeneralDocumentsAboutToSubmitHandler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.UPLOAD_GENERAL_DOCUMENT),
            is(true));
    }

    @Test
    public void givenACcdCallbackConsentedCase_WhenAnAboutToSubmitEventUploadGeneralDocument_thenHandlerCanHandle() {
        assertThat(uploadGeneralDocumentsAboutToSubmitHandler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONSENTED, EventType.UPLOAD_GENERAL_DOCUMENT),
            is(true));
    }

    @Test
    public void givenUploadGeneralDocument_When_IsValid_ThenExecuteHandler() {
        CallbackRequest callbackRequest = buildCallbackRequest();
        CaseDocument documentLink = new CaseDocument("/fileUrl", "document.extension", "/binaryUrl");

        // Setup caseDetailsBefore
        CaseDetails caseDetailsBefore = callbackRequest.getCaseDetailsBefore();
        GeneralUploadedDocumentData oldDoc = createGeneralUploadDocumentItem(
            "Old", "Old email content", documentLink, "", "Old Example", "oldDocument.filename");
        existingDocumentList.add(oldDoc);
        caseDetailsBefore.getData().put(GENERAL_UPLOADED_DOCUMENTS, existingDocumentList);

        // Setup caseDetails
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        GeneralUploadedDocumentData newDoc = createGeneralUploadDocumentItem(
            "New", "New email content", documentLink, "", "New Example", "newDocument.filename");
        uploadDocumentList.add(newDoc);
        caseDetails.getData().put(GENERAL_UPLOADED_DOCUMENTS, uploadDocumentList);

        // Setup expected document order (newest first)
        expectedDocumentIdList.add(newDoc.getId());
        expectedDocumentIdList.add(oldDoc.getId());

        // Get results from handler
        handledDocumentList.addAll(
            (List<GeneralUploadedDocumentData>) uploadGeneralDocumentsAboutToSubmitHandler.handle(
                callbackRequest, AUTH_TOKEN).getData().get(GENERAL_UPLOADED_DOCUMENTS));

        // Get document ids from handled documents
        handledDocumentList.forEach(doc -> handledDocumentIdList.add(doc.getId()));

        // Validate results
        assertThat(handledDocumentIdList.equals(expectedDocumentIdList), is(true));
    }

    private CallbackRequest buildCallbackRequest() {
        Map<String, Object> caseData = new HashMap<>();
        Map<String, Object> caseDataBefore = new HashMap<>();
        CaseDetails caseDetails = CaseDetails.builder().id(123L).data(caseData).build();
        CaseDetails caseDetailsBefore = CaseDetails.builder().id(123L).data(caseDataBefore).build();
        return CallbackRequest.builder().eventId(EventType.UPLOAD_GENERAL_DOCUMENT.getCcdType())
            .caseDetails(caseDetails).caseDetailsBefore(caseDetailsBefore).build();
    }
}