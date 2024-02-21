package uk.gov.hmcts.reform.finrem.caseorchestration.handler.scanneddocs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ManageScannedDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ManageScannedDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ScannedDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ScannedDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.CaseDocumentsHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.DocumentHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.FdrDocumentsHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.applicant.ApplicantChronologiesStatementHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.applicant.ApplicantFdrDocumentCategoriser;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.applicant.ApplicantOtherDocumentsHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.respondent.RespondentChronologiesStatementHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.respondent.RespondentFdrDocumentCategoriser;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.respondent.RespondentQuestionnairesAnswersHandler;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ManageScannedDocsContestedAboutToSubmitHandlerTest {

    private static final String AUTH_TOKEN = "AuthTokien";
    private static final String DOCUMENT_URL_TEST = "document/url/test";

    private ManageScannedDocsContestedAboutToSubmitHandler handler;

    @Before
    public void setUp() {
        FeatureToggleService featureToggleService = mock(FeatureToggleService.class);
        when(featureToggleService.isCaseFileViewEnabled()).thenReturn(true);

        List<DocumentHandler> documentHandlers = List.of(
            new RespondentChronologiesStatementHandler(featureToggleService),
            new ApplicantOtherDocumentsHandler(featureToggleService),
            new FdrDocumentsHandler(featureToggleService, new ApplicantFdrDocumentCategoriser(),
                new RespondentFdrDocumentCategoriser()),
            new RespondentQuestionnairesAnswersHandler(featureToggleService),
            new ApplicantChronologiesStatementHandler(featureToggleService),
            new CaseDocumentsHandler(featureToggleService)
        );

        FinremCaseDetailsMapper finremCaseDetailsMapper = new FinremCaseDetailsMapper(
            new ObjectMapper().registerModule(new JavaTimeModule()));
        handler = new ManageScannedDocsContestedAboutToSubmitHandler(finremCaseDetailsMapper, documentHandlers);
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
        FinremCaseDetails caseDetails = buildCaseDetails();
        FinremCallbackRequest callbackRequest = FinremCallbackRequest.builder().caseDetails(caseDetails).build();

        handler.handle(callbackRequest, AUTH_TOKEN);

        assertThat(caseDetails.getData().getScannedDocuments(), empty());
        assertThat(caseDetails.getData().getManageScannedDocumentCollection(), nullValue());
    }

    @Test
    public void givenAdministrativeDocsAreAdded_ThenDefaultsAreSetCorrectly() {
        FinremCaseDetails caseDetails = buildCaseDetailsForAdministrativeDocuments();
        FinremCallbackRequest callbackRequest = FinremCallbackRequest.builder().caseDetails(caseDetails).build();

        handler.handle(callbackRequest, AUTH_TOKEN);

        assertThat(caseDetails.getData().getScannedDocuments(), empty());
        assertThat(caseDetails.getData().getManageScannedDocumentCollection(), nullValue());
    }

    @Test
    public void givenNotAllDocsSelectedForUpdate_thenOnlyRemoveSelected() {
        FinremCaseDetails caseDetails = buildCaseDetailsForPartialUpdate();
        FinremCallbackRequest callbackRequest = FinremCallbackRequest.builder().caseDetails(caseDetails).build();

        handler.handle(callbackRequest, AUTH_TOKEN);

        assertThat(caseDetails.getData().getScannedDocuments().size(), is(1));
        assertThat(caseDetails.getData().getManageScannedDocumentCollection(), nullValue());
    }

    private List<ManageScannedDocumentCollection> setUpAddedDocuments() {
        return List.of(
            createContestedUploadDocumentItem("1", CaseDocumentType.STATEMENT_OF_ISSUES,
                CaseDocumentParty.RESPONDENT, YesOrNo.NO, YesOrNo.NO),
            createContestedUploadDocumentItem("2", CaseDocumentType.CHRONOLOGY,
                CaseDocumentParty.RESPONDENT, YesOrNo.NO, YesOrNo.NO),
            createContestedUploadDocumentItem("3", CaseDocumentType.FORM_G,
                CaseDocumentParty.RESPONDENT, YesOrNo.NO, YesOrNo.NO),
            createContestedUploadDocumentItem("4", CaseDocumentType.CHRONOLOGY,
                CaseDocumentParty.APPLICANT, YesOrNo.NO, YesOrNo.YES),
            createContestedUploadDocumentItem("5", CaseDocumentType.FORM_G,
                CaseDocumentParty.APPLICANT, YesOrNo.NO, YesOrNo.NO)
        );
    }

    private List<ManageScannedDocumentCollection> setUpAdministrativeDocuments() {
        return List.of(
            createContestedUploadDocumentItem("1", CaseDocumentType.ATTENDANCE_SHEETS,
                null, null, null),
            createContestedUploadDocumentItem("2", CaseDocumentType.JUDICIAL_NOTES,
                null, null, null),
            createContestedUploadDocumentItem("3", CaseDocumentType.WITNESS_SUMMONS,
                null, null, null),
            createContestedUploadDocumentItem("4", CaseDocumentType.TRANSCRIPT,
                null, null, null)
        );
    }

    private ManageScannedDocumentCollection createContestedUploadDocumentItem(String id, CaseDocumentType type,
        CaseDocumentParty party, YesOrNo isConfidential, YesOrNo isFdr) {
        return ManageScannedDocumentCollection.builder()
            .id(id)
            .manageScannedDocument(ManageScannedDocument.builder()
                .selectForUpdate(YesOrNo.YES)
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
                .build())
            .build();
    }

    private FinremCaseDetails buildCaseDetails() {
        FinremCaseData finremCaseData = FinremCaseData.builder()
            .scannedDocuments(setupScannedDocuments(5))
            .manageScannedDocumentCollection(setUpAddedDocuments())
            .evidenceHandled(YesOrNo.YES)
            .build();
        return FinremCaseDetails.builder().id(123L).caseType(CaseType.CONTESTED)
            .data(finremCaseData).build();
    }

    private FinremCaseDetails buildCaseDetailsForAdministrativeDocuments() {
        FinremCaseData finremCaseData = FinremCaseData.builder()
            .scannedDocuments(setupScannedDocuments(4))
            .manageScannedDocumentCollection(setUpAdministrativeDocuments())
            .evidenceHandled(YesOrNo.YES)
            .build();
        return FinremCaseDetails.builder().id(123L).caseType(CaseType.CONTESTED)
            .data(finremCaseData).build();
    }

    private FinremCaseDetails buildCaseDetailsForPartialUpdate() {
        List<ManageScannedDocumentCollection> manageScannedDocumentCollections = setUpAddedDocuments();
        manageScannedDocumentCollections.get(0).getManageScannedDocument().setSelectForUpdate(YesOrNo.NO);

        FinremCaseData finremCaseData = FinremCaseData.builder()
            .scannedDocuments(setupScannedDocuments(5))
            .manageScannedDocumentCollection(manageScannedDocumentCollections)
            .evidenceHandled(YesOrNo.YES)
            .build();
        return FinremCaseDetails.builder().id(123L).caseType(CaseType.CONTESTED)
            .data(finremCaseData).build();
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
