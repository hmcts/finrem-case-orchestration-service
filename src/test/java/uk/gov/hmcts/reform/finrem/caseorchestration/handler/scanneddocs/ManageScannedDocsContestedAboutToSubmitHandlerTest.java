package uk.gov.hmcts.reform.finrem.caseorchestration.handler.scanneddocs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectListElement;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ManageScannedDocsContestedAboutToSubmitHandlerTest {

    public static final String AUTH_TOKEN = "AuthTokien";
    public static final String DOCUMENT_URL_TEST = "document/url/test";

    private ManageScannedDocsContestedAboutToSubmitHandler handler;

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
                documentHandlers);
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
        caseData.setScannedDocuments(setupScannedDocuments(5));
        caseData.setScannedDocsToUpdate(setupScannedDocsToUpdate(5));

        caseData.setEvidenceHandled(YesOrNo.YES);
        FinremCallbackRequest callbackRequest = FinremCallbackRequest.builder().caseDetails(caseDetails).build();
        handler.handle(callbackRequest, AUTH_TOKEN);

        assertThat(caseDetails.getData().getManageScannedDocumentCollection().size(), is(5));

        documentHandlers.forEach(documentCollectionService ->
            verify(documentCollectionService)
                .replaceManagedDocumentsInCollectionType(callbackRequest, caseData.getManageScannedDocumentCollection(), false));

        assertThat(caseDetails.getData().getScannedDocuments().size(), is(0));
        assertThat(caseDetails.getData().getScannedDocsToUpdate(), nullValue());
    }

    @Test
    public void givenNotAllDocumentSelectedForUpdate_thenOnlyRemoveSelectedDocuments() {
        caseData.setScannedDocuments(setupScannedDocuments(5));
        caseData.setScannedDocsToUpdate(setupScannedDocsToUpdate(4));
        caseData.setEvidenceHandled(YesOrNo.YES);
        FinremCallbackRequest callbackRequest = FinremCallbackRequest.builder().caseDetails(caseDetails).build();
        handler.handle(callbackRequest, AUTH_TOKEN);

        assertThat(caseDetails.getData().getManageScannedDocumentCollection().size(), is(5));

        documentHandlers.forEach(documentCollectionService ->
            verify(documentCollectionService).replaceManagedDocumentsInCollectionType(callbackRequest,
                caseData.getManageScannedDocumentCollection(), false));

        assertThat(caseDetails.getData().getScannedDocuments().size(), is(1));
        assertThat(caseDetails.getData().getScannedDocsToUpdate(), nullValue());
    }

    @Test
    public void givenAdministrativeDocsAreAdded_ThenDefaultsAreSetCorrectly() {
        caseDetails = buildCaseDetailsForAdministrativeDocuments();
        caseData = caseDetails.getData();
        caseData.setEvidenceHandled(YesOrNo.YES);
        FinremCallbackRequest callbackRequest = FinremCallbackRequest.builder().caseDetails(caseDetails).build();
        handler.handle(callbackRequest, AUTH_TOKEN);

        assertThat(caseDetails.getData().getManageScannedDocumentCollection().size(), is(4));

        documentHandlers.forEach(documentCollectionService ->
            verify(documentCollectionService)
                .replaceManagedDocumentsInCollectionType(callbackRequest, caseData.getManageScannedDocumentCollection(), false));

        assertThat(caseDetails.getData().getScannedDocuments().size(), is(0));
        assertThat(caseDetails.getData().getScannedDocsToUpdate(), nullValue());
    }

    private List<UploadCaseDocumentCollection> setUpAddedDocuments() {
        List<UploadCaseDocumentCollection> screenUploadDocumentList = new ArrayList<>();
        screenUploadDocumentList.add(createContestedUploadDocumentItem(CaseDocumentType.STATEMENT_OF_ISSUES,
            CaseDocumentParty.RESPONDENT, YesOrNo.NO, YesOrNo.NO, "1"));
        screenUploadDocumentList.add(createContestedUploadDocumentItem(CaseDocumentType.CHRONOLOGY,
            CaseDocumentParty.RESPONDENT, YesOrNo.NO, YesOrNo.NO, "2"));
        screenUploadDocumentList.add(createContestedUploadDocumentItem(CaseDocumentType.FORM_G,
            CaseDocumentParty.RESPONDENT, YesOrNo.NO, YesOrNo.NO, "3"));
        screenUploadDocumentList.add(createContestedUploadDocumentItem(CaseDocumentType.CHRONOLOGY,
            CaseDocumentParty.APPLICANT, YesOrNo.NO, YesOrNo.YES, "4"));
        screenUploadDocumentList.add(createContestedUploadDocumentItem(CaseDocumentType.FORM_G,
            CaseDocumentParty.APPLICANT, YesOrNo.NO, YesOrNo.NO, "5"));
        return screenUploadDocumentList;
    }

    private List<UploadCaseDocumentCollection> setUpAdministrativeDocuments() {
        List<UploadCaseDocumentCollection> screenUploadDocumentList = new ArrayList<>();
        screenUploadDocumentList.add(createContestedUploadDocumentItem(CaseDocumentType.ATTENDANCE_SHEETS,
            null, null, null, "1"));
        screenUploadDocumentList.add(createContestedUploadDocumentItem(CaseDocumentType.JUDICIAL_NOTES,
            null, null, null, "2"));
        screenUploadDocumentList.add(createContestedUploadDocumentItem(CaseDocumentType.WITNESS_SUMMONS,
            null, null, null, "3"));
        screenUploadDocumentList.add(createContestedUploadDocumentItem(CaseDocumentType.TRANSCRIPT,
            null, null, null, "4"));
        return screenUploadDocumentList;
    }

    private UploadCaseDocumentCollection createContestedUploadDocumentItem(CaseDocumentType type,
                                                                           CaseDocumentParty party,
                                                                           YesOrNo isConfidential,
                                                                           YesOrNo isFdr,
                                                                           String id) {
        return UploadCaseDocumentCollection.builder()
            .id(id)
            .uploadCaseDocument(UploadCaseDocument
                .builder()
                .caseDocuments(CaseDocument.builder().documentUrl(DOCUMENT_URL_TEST).build())
                .caseDocumentType(type)
                .caseDocumentParty(party)
                .caseDocumentConfidentiality(isConfidential)
                .caseDocumentFdr(isFdr)
                .hearingDetails(null)
                .caseDocumentUploadDateTime(LocalDateTime.now())
                .build())
            .build();
    }

    private FinremCaseDetails buildCaseDetails() {
        FinremCaseData finremCaseData = FinremCaseData.builder()
            .manageScannedDocumentCollection(setUpAddedDocuments())
            .build();
        return FinremCaseDetails.builder().id(123L).caseType(CaseType.CONTESTED)
            .data(finremCaseData).build();
    }

    private FinremCaseDetails buildCaseDetailsForAdministrativeDocuments() {
        FinremCaseData finremCaseData = FinremCaseData.builder()
            .scannedDocuments(setupScannedDocuments(4))
            .manageScannedDocumentCollection(setUpAdministrativeDocuments())
            .scannedDocsToUpdate(setupScannedDocsToUpdate(4))
            .build();
        return FinremCaseDetails.builder().id(123L).caseType(CaseType.CONTESTED)
            .data(finremCaseData).build();
    }

    private DynamicMultiSelectList setupScannedDocsToUpdate(int numSelected) {
        List<DynamicMultiSelectListElement> selectedItems = new ArrayList<>();
        for (int i = 1; i <= numSelected; i++) {
            selectedItems.add(DynamicMultiSelectListElement.builder().code(String.valueOf(i)).build());
        }
        return DynamicMultiSelectList.builder()
            .value(selectedItems)
            .build();
    }

    private List<ScannedDocumentCollection> setupScannedDocuments(int numDocuments) {
        List<ScannedDocumentCollection> scannedDocumentCollections = new ArrayList<>();

        for (int i = 1; i <= numDocuments; i++) {
            scannedDocumentCollections.add(ScannedDocumentCollection.builder()
                .id(String.valueOf(i))
                .value(
                    ScannedDocument.builder()
                        .scannedDate(LocalDateTime.now())
                        .fileName("file1")
                        .url(CaseDocument.builder()
                            .documentUrl(DOCUMENT_URL_TEST)
                            .build())
                        .build())
                .build());
        }

        return scannedDocumentCollections;
    }
}
