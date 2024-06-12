package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
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
import uk.gov.hmcts.reform.finrem.caseorchestration.service.DocumentUploadServiceV2;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory.UploadGeneralDocumentsCategoriser;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentchecker.DocumentCheckerService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.util.TestResource.buildCaseDocument;

@RunWith(MockitoJUnitRunner.class)
public class UploadDocumentContestedAboutToSubmitHandlerTest {

    public static final String AUTH_TOKEN = "token:)";
    public static final String CASE_ID = "1234567890";

    ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    @Mock
    private DocumentUploadServiceV2 documentUploadService;
    @Mock
    private DocumentCheckerService documentCheckerService;
    @Mock
    private UploadGeneralDocumentsCategoriser uploadGeneralDocumentsCategoriser;

    private UploadDocumentContestedAboutToSubmitHandler underTest;

    @Before
    public void setUpTest() {
        FinremCaseDetailsMapper finremCaseDetailsMapper = new FinremCaseDetailsMapper(objectMapper);
        underTest = new UploadDocumentContestedAboutToSubmitHandler(finremCaseDetailsMapper, documentCheckerService, documentUploadService,
            uploadGeneralDocumentsCategoriser);
    }

    @Test
    public void givenACcdCallbackContestedCase_WhenAnAboutToSubmitEventUploadGeneralDocument_thenHandlerCanHandle() {
        assertThat(underTest.canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.UPLOAD_DOCUMENT_CONTESTED), is(true));
    }

    @Test
    public void givenACcdCallbackConsentedCase_WhenAnAboutToSubmitEventUploadGeneralDocument_thenHandlerCanHandle() {
        assertThat(underTest.canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONSENTED, EventType.UPLOAD_DOCUMENT_CONTESTED), is(false));
    }

    @Test
    public void givenValidCaseData_whenWarningAreDetected_thenPopulateWarnings() {
        List<String> expectedWarnings = List.of("warnings");
        when(documentUploadService.getNewUploadGeneralDocuments(any(), any())).thenReturn(List.of(
            UploadGeneralDocumentCollection.builder()
                .value(UploadGeneralDocument.builder().build())
                .build()
        ));
        when(documentCheckerService.getWarnings(any(), any(), any())).thenReturn(expectedWarnings);

        FinremCaseDetails finremCaseDetails = buildCaseDetails();
        finremCaseDetails.getData().setUploadGeneralDocuments(List.of(
            createGeneralUploadDocumentItem(
            UploadGeneralDocumentType.LETTER_EMAIL_FROM_RESPONDENT, "New email content",
                buildCaseDocument("/fileUrl", "document.extension",
                    "/binaryUrl", ""), LocalDate.now(), "New Example", "newDocument.filename")
        ));

        FinremCaseDetails finremCaseDetailsBefore = buildCaseDetails();

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = underTest.handle(
            FinremCallbackRequest.builder()
                .caseDetails(finremCaseDetails)
                .caseDetailsBefore(finremCaseDetailsBefore).build(),
            AUTH_TOKEN);
        assertThat(response.getWarnings(), is(expectedWarnings));
    }

    @Test
    public void givenValidCaseData_whenHandleUploadGeneralDocument_thenSortCollectionByDateAndCategoriserInvoked() {
        CaseDocument documentLink = buildCaseDocument("/fileUrl", "document.extension",
            "/binaryUrl", "");

        FinremCaseDetails finremCaseDetails = buildCaseDetails();
        FinremCaseDetails finremCaseDetailsBefore = buildCaseDetails();

        UploadGeneralDocumentCollection oldDoc = createGeneralUploadDocumentItem(
            UploadGeneralDocumentType.LETTER_EMAIL_FROM_APPLICANT,
            "Old email content", documentLink, LocalDate.now().minusDays(1),
            "Old Example", "oldDocument.filename");

        finremCaseDetails.getData().setUploadGeneralDocuments(List.of(oldDoc));

        UploadGeneralDocumentCollection newDoc = createGeneralUploadDocumentItem(
            UploadGeneralDocumentType.LETTER_EMAIL_FROM_RESPONDENT, "New email content",
            documentLink, LocalDate.now(), "New Example", "newDocument.filename");
        finremCaseDetails.getData().setUploadGeneralDocuments(List.of(newDoc, oldDoc));

        List<UploadGeneralDocumentCollection> expectedDocumentIdList = new ArrayList<>();
        expectedDocumentIdList.add(newDoc);
        expectedDocumentIdList.add(oldDoc);

        List<UploadGeneralDocumentCollection> actual = underTest.handle(
            FinremCallbackRequest.builder().caseDetails(finremCaseDetails)
                .caseDetailsBefore(finremCaseDetailsBefore).build(),
            AUTH_TOKEN).getData().getUploadGeneralDocuments();

        assertThat(actual, is(expectedDocumentIdList));
        verify(uploadGeneralDocumentsCategoriser).categorise(finremCaseDetails.getData());
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