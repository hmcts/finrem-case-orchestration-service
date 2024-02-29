package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.UploadedGeneralDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadGeneralDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadGeneralDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadGeneralDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.UploadCaseDocumentWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory.UploadGeneralDocumentsCategoriser;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@RunWith(MockitoJUnitRunner.class)
public class UploadGeneralDocumentsAboutToSubmitHandlerTest {

    public static final String AUTH_TOKEN = "tokien:)";
    public static final String CASE_ID = "1234567890";
    private FinremCaseDetails caseDetails;
    private FinremCaseDetails caseDetailsBefore;

    private FinremCaseData caseData;
    ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    @Mock
    private UploadGeneralDocumentsCategoriser uploadGeneralDocumentsCategoriser;
    private UploadGeneralDocumentsAboutToSubmitHandler uploadGeneralDocumentsAboutToSubmitHandler;

    private final List<UploadGeneralDocumentCollection> uploadDocumentList = new ArrayList<>();
    private final List<UploadGeneralDocumentCollection> existingDocumentList = new ArrayList<>();
    private final List<UploadGeneralDocumentCollection> expectedDocumentIdList = new ArrayList<>();
    List<UploadGeneralDocumentCollection> handledDocumentIdList = new ArrayList<>();

    private UploadedGeneralDocumentService uploadedGeneralDocumentHelper;

    @Before
    public void setUpTest() {
        caseDetails = buildCaseDetails();
        caseDetailsBefore = buildCaseDetails();
        caseData = caseDetails.getData();
        FinremCaseDetailsMapper finremCaseDetailsMapper =
            new FinremCaseDetailsMapper(objectMapper);
        uploadedGeneralDocumentHelper = new UploadedGeneralDocumentService(objectMapper);
        uploadGeneralDocumentsAboutToSubmitHandler =
            new UploadGeneralDocumentsAboutToSubmitHandler(finremCaseDetailsMapper,
                uploadedGeneralDocumentHelper, uploadGeneralDocumentsCategoriser);
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

        CaseDocument documentLink = new CaseDocument("/fileUrl", "document.extension",
            "/binaryUrl", "");

        UploadGeneralDocumentCollection oldDoc = createGeneralUploadDocumentItem(
            UploadGeneralDocumentType.LETTER_EMAIL_FROM_APPLICANT,
            "Old email content", documentLink, LocalDate.now().minusDays(1),
            "Old Example", "oldDocument.filename");
        existingDocumentList.add(oldDoc);
        caseDetailsBefore.getData().setUploadGeneralDocuments(existingDocumentList);

        UploadGeneralDocumentCollection newDoc = createGeneralUploadDocumentItem(
            UploadGeneralDocumentType.LETTER_EMAIL_FROM_RESPONDENT, "New email content",
            documentLink, LocalDate.now(), "New Example", "newDocument.filename");
        uploadDocumentList.addAll(List.of(newDoc, oldDoc));
        caseDetails.getData().setUploadGeneralDocuments(uploadDocumentList);

        expectedDocumentIdList.add(newDoc);
        expectedDocumentIdList.add(oldDoc);

        handledDocumentIdList.addAll(uploadGeneralDocumentsAboutToSubmitHandler.handle(
                FinremCallbackRequest.builder().caseDetails(caseDetails).caseDetailsBefore(caseDetailsBefore).build(),
                AUTH_TOKEN).getData().getUploadGeneralDocuments());

        assertThat(handledDocumentIdList.equals(expectedDocumentIdList), is(true));
    }

    protected FinremCaseDetails buildCaseDetails() {
        FinremCaseData finremCaseData = FinremCaseData.builder()
            .uploadCaseDocumentWrapper(UploadCaseDocumentWrapper.builder().build())
            .build();
        return FinremCaseDetails.builder().id(Long.valueOf(CASE_ID)).caseType(CaseType.CONTESTED)
            .data(finremCaseData).build();
    }

    protected UploadGeneralDocumentCollection createGeneralUploadDocumentItem(UploadGeneralDocumentType type, String emailContent,
                                                                              CaseDocument link, LocalDate dateAdded, String comment,
                                                                              String fileName) {
        return UploadGeneralDocumentCollection.builder()
            .value(UploadGeneralDocument
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
}