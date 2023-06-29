package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.UploadedDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.applicant.ApplicantCaseSummariesCollectionService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.applicant.ApplicantChronologiesStatementCollectionService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class UploadContestedCaseDocumentsAboutToSubmitHandlerTest {

    public static final String AUTH_TOKEN = "AuthTokien";

    @Mock
    protected UploadedDocumentService uploadedDocumentHelper;
    @Mock
    ApplicantCaseSummariesCollectionService applicantCaseSummariesCollectionService;
    @Mock
    ApplicantChronologiesStatementCollectionService applicantChronologiesStatementCollectionService;
    @InjectMocks
    private UploadContestedCaseDocumentsAboutToSubmitHandler uploadContestedCaseDocumentsHandler;

    private final List<UploadCaseDocumentCollection> screenUploadDocumentList = new ArrayList<>();

    @Before
    public void setup() {
        uploadContestedCaseDocumentsHandler =
            new UploadContestedCaseDocumentsAboutToSubmitHandler(
                new FinremCaseDetailsMapper(new ObjectMapper().registerModule(new JavaTimeModule())),
                Arrays.asList(applicantCaseSummariesCollectionService, applicantChronologiesStatementCollectionService),
                uploadedDocumentHelper);
    }

    @Test
    public void givenACcdCallbackContestedCase_WhenAnAboutToSubmitEventUploadCaseDocument_thenHandlerCanHandle() {
        assertThat(uploadContestedCaseDocumentsHandler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.UPLOAD_CASE_FILES),
            is(true));
    }

    @Test
    public void givenACcdCallbackConsentedCase_WhenAnAboutToSubmitEventUploadCaseDocument_thenHandlerCanNotHandle() {
        assertThat(uploadContestedCaseDocumentsHandler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONSENTED, EventType.UPLOAD_CASE_FILES),
            is(false));
    }


    @Test
    public void givenUploadCaseDocumentForApplicant_When_IsValid_ThenExecuteHandlers() {
        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();

        screenUploadDocumentList.add(createContestedUploadDocumentItem(CaseDocumentType.OTHER,
            CaseDocumentParty.APPLICANT, YesOrNo.YES, YesOrNo.NO, "Other Example"));
        caseDetails.getData().setManageCaseDocumentCollection(screenUploadDocumentList);

        caseDetails.getData().getUploadCaseDocumentWrapper().setUploadCaseDocument(screenUploadDocumentList);
        uploadContestedCaseDocumentsHandler.handle(callbackRequest, AUTH_TOKEN);

        verify(applicantCaseSummariesCollectionService)
            .addManagedDocumentToSelectedCollection(callbackRequest,screenUploadDocumentList);
        verify(applicantChronologiesStatementCollectionService)
            .addManagedDocumentToSelectedCollection(callbackRequest, screenUploadDocumentList);
    }

    private FinremCallbackRequest buildCallbackRequest() {
        FinremCaseData data = FinremCaseData.builder().build();
        FinremCaseDetails caseDetails =
            FinremCaseDetails.builder().data(data).id(123L).build();
        FinremCaseDetails caseDetailsBefore = FinremCaseDetails.builder().data(data).id(123L).build();
        return FinremCallbackRequest.builder().eventType(EventType.UPLOAD_CASE_FILES)
            .caseDetails(caseDetails).caseDetailsBefore(caseDetailsBefore).build();
    }

    protected UploadCaseDocumentCollection createContestedUploadDocumentItem(CaseDocumentType type,
                                                                             CaseDocumentParty party,
                                                                             YesOrNo isConfidential,
                                                                             YesOrNo isFdr,
                                                                             String other) {
        UUID uuid = UUID.randomUUID();

        return UploadCaseDocumentCollection.builder()
            .id(uuid.toString())
            .uploadCaseDocument(UploadCaseDocument
                .builder()
                .caseDocuments(new CaseDocument())
                .caseDocumentType(type)
                .caseDocumentParty(party)
                .caseDocumentConfidential(isConfidential)
                .caseDocumentOther(other)
                .caseDocumentFdr(isFdr)
                .hearingDetails(null)
                .caseDocumentUploadDateTime(LocalDateTime.now())
                .build())
            .build();
    }
}