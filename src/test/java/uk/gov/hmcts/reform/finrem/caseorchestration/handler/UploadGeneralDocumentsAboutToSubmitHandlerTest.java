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
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.UploadedGeneralDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralUploadedDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralUploadedDocumentData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_UPLOADED_DOCUMENTS;

@RunWith(MockitoJUnitRunner.class)
public class UploadGeneralDocumentsAboutToSubmitHandlerTest {

    public static final String AUTH_TOKEN = "tokien:)";

    ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private UploadGeneralDocumentsAboutToSubmitHandler uploadGeneralDocumentsAboutToSubmitHandler;

    private final List<GeneralUploadedDocumentData> uploadDocumentList = new ArrayList<>();
    private final List<GeneralUploadedDocumentData> existingDocumentList = new ArrayList<>();
    private final List<String> expectedDocumentIdList = new ArrayList<>();
    List<GeneralUploadedDocumentData> handledDocumentList = new ArrayList<>();
    List<String> handledDocumentIdList = new ArrayList<>();

    private final UploadedGeneralDocumentService uploadedGeneralDocumentHelper = new UploadedGeneralDocumentService(objectMapper);

    protected GeneralUploadedDocumentData createGeneralUploadDocumentItem(String type, String emailContent,
                                                                          CaseDocument link, String dateAdded, String comment,
                                                                          String fileName) {
        return GeneralUploadedDocumentData.builder()
            .id(UUID.randomUUID().toString())
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
    public void givenValidCaseData_whenHandleUploadGeneralDocument_thenSortCollectionByDate() {
        CallbackRequest callbackRequest = buildCallbackRequest();
        CaseDocument documentLink = new CaseDocument("/fileUrl", "document.extension", "/binaryUrl", "");

        CaseDetails caseDetailsBefore = callbackRequest.getCaseDetailsBefore();
        GeneralUploadedDocumentData oldDoc = createGeneralUploadDocumentItem(
            "Old", "Old email content", documentLink, "", "Old Example", "oldDocument.filename");
        existingDocumentList.add(oldDoc);
        caseDetailsBefore.getData().put(GENERAL_UPLOADED_DOCUMENTS, existingDocumentList);

        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        GeneralUploadedDocumentData newDoc = createGeneralUploadDocumentItem(
            "New", "New email content", documentLink, "", "New Example", "newDocument.filename");
        uploadDocumentList.addAll(List.of(newDoc, oldDoc));
        caseDetails.getData().put(GENERAL_UPLOADED_DOCUMENTS, uploadDocumentList);

        expectedDocumentIdList.add(newDoc.getId());
        expectedDocumentIdList.add(oldDoc.getId());

        handledDocumentList.addAll(
            (List<GeneralUploadedDocumentData>) uploadGeneralDocumentsAboutToSubmitHandler.handle(
                callbackRequest, AUTH_TOKEN).getData().get(GENERAL_UPLOADED_DOCUMENTS));

        handledDocumentList.forEach(doc -> handledDocumentIdList.add(doc.getId()));

        assertThat(handledDocumentIdList.equals(expectedDocumentIdList), is(true));
    }

    private CallbackRequest buildCallbackRequest() {
        Map<String, Object> caseData = new HashMap<>();
        Map<String, Object> caseDataBefore = new HashMap<>();
        CaseDetails caseDetails = CaseDetails.builder().id(123L).build();
        caseDetails.setData(caseData);
        CaseDetails caseDetailsBefore = CaseDetails.builder().id(123L).build();
        caseDetailsBefore.setData(caseDataBefore);
        return CallbackRequest.builder().eventId(EventType.UPLOAD_GENERAL_DOCUMENT.getCcdType())
            .caseDetails(caseDetails).caseDetailsBefore(caseDetailsBefore).build();
    }
}