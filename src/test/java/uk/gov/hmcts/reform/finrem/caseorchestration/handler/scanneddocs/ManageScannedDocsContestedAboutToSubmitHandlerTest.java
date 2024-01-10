package uk.gov.hmcts.reform.finrem.caseorchestration.handler.scanneddocs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ScannedDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ScannedDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.DocumentHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.FdrDocumentsHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.applicant.ApplicantChronologiesStatementHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.applicant.ApplicantOtherDocumentsHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.respondent.RespondentChronologiesStatementHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.respondent.RespondentQuestionnairesAnswersHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.validation.ManageDocumentsHandlerValidator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@RunWith(MockitoJUnitRunner.class)
public class ManageScannedDocsContestedAboutToSubmitHandlerTest {

    public static final String AUTH_TOKEN = "AuthTokien";
    public static final String DOCUMENT_URL_TEST = "document/url/test";

    private ManageScannedDocsContestedAboutToSubmitHandler handler;

    @Mock
    private ManageDocumentsHandlerValidator manageDocumentsHandlerValidator;
    @Mock
    RespondentChronologiesStatementHandler respondentChronologiesStatementCollectionService;
    @Mock
    ApplicantOtherDocumentsHandler applicantOtherDocumentsCollectionService;
    @Mock
    FdrDocumentsHandler fdrDocumentsCollectionService;
    @Mock
    RespondentQuestionnairesAnswersHandler respondentQuestionnairesAnswersCollectionService;
    @Mock
    ApplicantChronologiesStatementHandler applicantChronologiesStatementCollectionService;

    private FinremCaseDetails caseDetails;
    private FinremCaseData caseData;
    private List<DocumentHandler> documentHandlers;

    @Before
    public void setUp() {
        caseDetails = buildCaseDetails();
        caseData = caseDetails.getData();

        documentHandlers = Stream.of(respondentChronologiesStatementCollectionService, applicantOtherDocumentsCollectionService,
                fdrDocumentsCollectionService, respondentQuestionnairesAnswersCollectionService,
                applicantChronologiesStatementCollectionService)
            .collect(Collectors.toList());

        FinremCaseDetailsMapper finremCaseDetailsMapper =
            new FinremCaseDetailsMapper(new ObjectMapper().registerModule(new JavaTimeModule()));
        handler =
            new ManageScannedDocsContestedAboutToSubmitHandler(finremCaseDetailsMapper,
                documentHandlers, manageDocumentsHandlerValidator);
    }


    @Test
    public void givenACcdCallbackContestedCase_WhenAnAboutToSubmitEventManageCaseDocuments_thenHandlerCanHandle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.MANAGE_SCANNED_DOCS),
            is(true));
    }

    @Test
    public void givenACcdCallbackConsentedCase_WhenAnAboutToSubmitEventManageCaseDocuments_thenHandlerCanNotHandle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONSENTED, EventType.MANAGE_SCANNED_DOCS),
            is(false));
    }

    @Test
    public void shouldHandleCorrectly() {
        List<ScannedDocumentCollection> scannedDocumentCollections = new ArrayList<>();
        scannedDocumentCollections.add(ScannedDocumentCollection.builder().value(
            ScannedDocument.builder()
                .scannedDate(LocalDateTime.now())
                .fileName("file1")
                .url(CaseDocument.builder()
                    .documentUrl(DOCUMENT_URL_TEST)
                    .build())
                .build()).build());

        caseData.setScannedDocuments(scannedDocumentCollections);
        caseData.setEvidenceHandled(YesOrNo.YES);
        FinremCallbackRequest callbackRequest = FinremCallbackRequest.builder().caseDetails(caseDetails).build();
        handler.handle(callbackRequest, AUTH_TOKEN);

        assertThat(caseDetails.getData().getManageScannedDocumentCollection().size(), is(5));

        documentHandlers.forEach(documentCollectionService ->
            Mockito.verify(documentCollectionService)
                .replaceManagedDocumentsInCollectionType(callbackRequest, caseData.getManageScannedDocumentCollection(), false));

        assertThat(caseDetails.getData().getScannedDocuments().size(), is(0));
    }


    private List<UploadCaseDocumentCollection> setUpAddedDocuments() {
        List<UploadCaseDocumentCollection> screenUploadDocumentList = new ArrayList<>();
        screenUploadDocumentList.add(createContestedUploadDocumentItem(CaseDocumentType.STATEMENT_OF_ISSUES,
            CaseDocumentParty.RESPONDENT, YesOrNo.NO, YesOrNo.NO, null));
        screenUploadDocumentList.add(createContestedUploadDocumentItem(CaseDocumentType.CHRONOLOGY,
            CaseDocumentParty.RESPONDENT, YesOrNo.NO, YesOrNo.NO, null));
        screenUploadDocumentList.add(createContestedUploadDocumentItem(CaseDocumentType.FORM_G,
            CaseDocumentParty.RESPONDENT, YesOrNo.NO, YesOrNo.NO, null));
        screenUploadDocumentList.add(createContestedUploadDocumentItem(CaseDocumentType.CHRONOLOGY,
            CaseDocumentParty.APPLICANT, YesOrNo.NO, YesOrNo.YES, null));
        screenUploadDocumentList.add(createContestedUploadDocumentItem(CaseDocumentType.FORM_G,
            CaseDocumentParty.APPLICANT, YesOrNo.NO, YesOrNo.NO, null));
        return screenUploadDocumentList;
    }


    private UploadCaseDocumentCollection createContestedUploadDocumentItem(CaseDocumentType type,
                                                                           CaseDocumentParty party,
                                                                           YesOrNo isConfidential,
                                                                           YesOrNo isFdr,
                                                                           String other) {
        UUID uuid = UUID.randomUUID();

        return UploadCaseDocumentCollection.builder()
            .id(uuid.toString())
            .uploadCaseDocument(UploadCaseDocument
                .builder()
                .caseDocuments(CaseDocument.builder().documentUrl(DOCUMENT_URL_TEST).build())
                .caseDocumentType(type)
                .caseDocumentParty(party)
                .caseDocumentConfidentiality(isConfidential)
                .caseDocumentOther(other)
                .caseDocumentFdr(isFdr)
                .hearingDetails(null)
                .caseDocumentUploadDateTime(LocalDateTime.now())
                .build())
            .build();
    }

    protected FinremCaseDetails buildCaseDetails() {
        FinremCaseData finremCaseData = FinremCaseData.builder()
            .manageScannedDocumentCollection(setUpAddedDocuments())
            .build();
        return FinremCaseDetails.builder().id(123L).caseType(CaseType.CONTESTED)
            .data(finremCaseData).build();
    }
}
