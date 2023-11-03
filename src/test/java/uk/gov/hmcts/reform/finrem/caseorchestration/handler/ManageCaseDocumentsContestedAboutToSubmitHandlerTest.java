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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDataContested;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.UploadCaseDocumentWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.UploadedDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.DocumentHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.FdrDocumentsHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.applicant.ApplicantChronologiesStatementHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.applicant.ApplicantOtherDocumentsHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.respondent.RespondentChronologiesStatementHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.respondent.RespondentQuestionnairesAnswersHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement.EvidenceManagementDeleteService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.handler.ManageCaseDocumentsContestedAboutToSubmitHandler.CHOOSE_A_DIFFERENT_PARTY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.handler.ManageCaseDocumentsContestedAboutToSubmitHandler.INTERVENER_1;
import static uk.gov.hmcts.reform.finrem.caseorchestration.handler.ManageCaseDocumentsContestedAboutToSubmitHandler.INTERVENER_2;
import static uk.gov.hmcts.reform.finrem.caseorchestration.handler.ManageCaseDocumentsContestedAboutToSubmitHandler.INTERVENER_3;
import static uk.gov.hmcts.reform.finrem.caseorchestration.handler.ManageCaseDocumentsContestedAboutToSubmitHandler.INTERVENER_4;

@RunWith(MockitoJUnitRunner.class)
public class ManageCaseDocumentsContestedAboutToSubmitHandlerTest {

    public static final String AUTH_TOKEN = "AuthTokien";
    public static final String DOCUMENT_URL_TEST = "document/url/test";
    @Mock
    private UploadedDocumentService uploadedDocumentHelper;

    @Mock
    private EvidenceManagementDeleteService evidenceManagementDeleteService;

    @Mock
    private FeatureToggleService featureToggleService;
    private ManageCaseDocumentsContestedAboutToSubmitHandler manageCaseDocumentsAboutToSubmitCaseHandler;
    private FinremCaseDetails<FinremCaseDataContested> caseDetails;
    private FinremCaseDetails<FinremCaseDataContested> caseDetailsBefore;
    private FinremCaseDataContested caseData;
    private final List<UploadCaseDocumentCollection> screenUploadDocumentList = new ArrayList<>();


    @Before
    public void setUp() {

        caseDetails = buildCaseDetails();
        caseDetailsBefore = buildCaseDetails();
        caseData = caseDetails.getData();

        RespondentChronologiesStatementHandler respondentChronologiesStatementCollectionService =
            new RespondentChronologiesStatementHandler();
        ApplicantOtherDocumentsHandler applicantOtherDocumentsCollectionService =
            new ApplicantOtherDocumentsHandler();
        FdrDocumentsHandler fdrDocumentsCollectionService =
            new FdrDocumentsHandler();
        RespondentQuestionnairesAnswersHandler respondentQuestionnairesAnswersCollectionService =
            new RespondentQuestionnairesAnswersHandler();
        ApplicantChronologiesStatementHandler applicantChronologiesStatementCollectionService =
            new ApplicantChronologiesStatementHandler();

        List<DocumentHandler> documentHandlers =
            Stream.of(respondentChronologiesStatementCollectionService, applicantOtherDocumentsCollectionService,
                    fdrDocumentsCollectionService, respondentQuestionnairesAnswersCollectionService,
                    applicantChronologiesStatementCollectionService)
                .collect(Collectors.toList());
        FinremCaseDetailsMapper finremCaseDetailsMapper =
            new FinremCaseDetailsMapper(new ObjectMapper().registerModule(new JavaTimeModule()));
        manageCaseDocumentsAboutToSubmitCaseHandler =
            new ManageCaseDocumentsContestedAboutToSubmitHandler(finremCaseDetailsMapper,
                documentHandlers, uploadedDocumentHelper, evidenceManagementDeleteService, featureToggleService);
    }

    @Test
    public void givenACcdCallbackContestedCase_WhenAnAboutToSubmitEventManageCaseDocuments_thenHandlerCanHandle() {
        assertThat(manageCaseDocumentsAboutToSubmitCaseHandler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.MANAGE_CASE_DOCUMENTS),
            is(true));
    }

    @Test
    public void givenACcdCallbackConsentedCase_WhenAnAboutToSubmitEventManageCaseDocuments_thenHandlerCanNotHandle() {
        assertThat(manageCaseDocumentsAboutToSubmitCaseHandler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONSENTED, EventType.MANAGE_CASE_DOCUMENTS),
            is(false));
    }

    @Test
    public void givenAManagedCaseWithCasesAddedAndRemoved_WhenAnAboutToSubmitEventManageCaseDocuments_thenCollectionsSetAndManagedEmpty() {
        setUpRemovedDocuments();
        setUpAddedDocuments();

        caseDetails.getData().setManageCaseDocumentCollection(screenUploadDocumentList);

        manageCaseDocumentsAboutToSubmitCaseHandler.handle(
            FinremCallbackRequest.<FinremCaseDataContested>builder()
                .caseDetails(caseDetails).caseDetailsBefore(caseDetailsBefore).build(),
            AUTH_TOKEN);

        assertThat(caseData.getUploadCaseDocumentWrapper()
                .getDocumentCollectionPerType(CaseDocumentCollectionType.APP_OTHER_COLLECTION),
            hasSize(4));
        assertThat(caseData.getUploadCaseDocumentWrapper()
                .getDocumentCollectionPerType(CaseDocumentCollectionType.RESP_CHRONOLOGIES_STATEMENTS_COLLECTION),
            hasSize(3));
        assertThat(caseData.getUploadCaseDocumentWrapper()
                .getDocumentCollectionPerType(CaseDocumentCollectionType.CONTESTED_FDR_CASE_DOCUMENT_COLLECTION),
            hasSize(1));
        assertThat(caseData.getManageCaseDocumentCollection(),
            hasSize(0));
    }

    @Test
    public void givenAManagedCaseWithCasesAddedAndRemovedDeleteFlagOn_WhenHandle_thenDeleteServiceCalled() {
        setUpRemovedDocuments();
        setUpAddedDocuments();

        caseDetails.getData().setManageCaseDocumentCollection(screenUploadDocumentList);

        when(featureToggleService.isSecureDocEnabled()).thenReturn(true);
        manageCaseDocumentsAboutToSubmitCaseHandler.handle(
            FinremCallbackRequest.<FinremCaseDataContested>builder()
                .caseDetails(caseDetails).caseDetailsBefore(caseDetailsBefore).build(),
            AUTH_TOKEN);

        verify(evidenceManagementDeleteService, times(1)).delete(DOCUMENT_URL_TEST, AUTH_TOKEN);
    }

    @Test
    public void givenACaseWithoutIntervenersAndManagedDocIntoIntv1_WhenHandle_thenThrowValidationError() {
        setUpRemovedDocuments();
        setUpAddedDocuments();

        caseDetails.getData().setManageCaseDocumentCollection(screenUploadDocumentList);
        caseDetails.getData().getManageCaseDocumentCollection().get(0).getUploadCaseDocument()
            .setCaseDocumentParty(CaseDocumentParty.INTERVENER_ONE);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseDataContested> response = manageCaseDocumentsAboutToSubmitCaseHandler.handle(
            FinremCallbackRequest.builder().caseDetails(caseDetails).caseDetailsBefore(caseDetailsBefore).build(),
            AUTH_TOKEN);

        assertEquals(response.getWarnings().get(0), INTERVENER_1 + CHOOSE_A_DIFFERENT_PARTY);
    }

    @Test
    public void givenACaseWithoutIntervenersAndManagedDocIntoIntv2_WhenHandle_thenThrowValidationError() {
        setUpRemovedDocuments();
        setUpAddedDocuments();

        caseDetails.getData().setManageCaseDocumentCollection(screenUploadDocumentList);
        caseDetails.getData().getManageCaseDocumentCollection().get(0).getUploadCaseDocument()
            .setCaseDocumentParty(CaseDocumentParty.INTERVENER_TWO);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseDataContested> response = manageCaseDocumentsAboutToSubmitCaseHandler.handle(
            FinremCallbackRequest.builder().caseDetails(caseDetails).caseDetailsBefore(caseDetailsBefore).build(),
            AUTH_TOKEN);

        assertEquals(response.getWarnings().get(0), INTERVENER_2 + CHOOSE_A_DIFFERENT_PARTY);
    }


    @Test
    public void givenACaseWithoutIntervenersAndManagedDocIntoIntv2WhichIsNull_WhenHandle_thenThrowValidationError() {
        setUpRemovedDocuments();
        setUpAddedDocuments();

        caseDetails.getData().setManageCaseDocumentCollection(screenUploadDocumentList);
        caseDetails.getData().getManageCaseDocumentCollection().get(0).getUploadCaseDocument()
            .setCaseDocumentParty(null);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = manageCaseDocumentsAboutToSubmitCaseHandler.handle(
            FinremCallbackRequest.builder().caseDetails(caseDetails).caseDetailsBefore(caseDetailsBefore).build(),
            AUTH_TOKEN);

        assertTrue(response.getWarnings().isEmpty());
    }

    @Test
    public void givenACaseWithoutIntervenersAndManagedDocIntoIntv3_WhenHandle_thenThrowValidationError() {
        setUpRemovedDocuments();
        setUpAddedDocuments();

        caseDetails.getData().setManageCaseDocumentCollection(screenUploadDocumentList);
        caseDetails.getData().getManageCaseDocumentCollection().get(0).getUploadCaseDocument()
            .setCaseDocumentParty(CaseDocumentParty.INTERVENER_THREE);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseDataContested> response = manageCaseDocumentsAboutToSubmitCaseHandler.handle(
            FinremCallbackRequest.builder().caseDetails(caseDetails).caseDetailsBefore(caseDetailsBefore).build(),
            AUTH_TOKEN);

        assertEquals(response.getWarnings().get(0), INTERVENER_3 + CHOOSE_A_DIFFERENT_PARTY);
    }

    @Test
    public void givenACaseWithoutIntervenersAndManagedDocIntoIntv4_WhenHandle_thenThrowValidationError() {
        setUpRemovedDocuments();
        setUpAddedDocuments();

        caseDetails.getData().setManageCaseDocumentCollection(screenUploadDocumentList);
        caseDetails.getData().getManageCaseDocumentCollection().get(0).getUploadCaseDocument()
            .setCaseDocumentParty(CaseDocumentParty.INTERVENER_FOUR);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseDataContested> response = manageCaseDocumentsAboutToSubmitCaseHandler.handle(
            FinremCallbackRequest.builder().caseDetails(caseDetails).caseDetailsBefore(caseDetailsBefore).build(),
            AUTH_TOKEN);

        assertEquals(response.getWarnings().get(0), INTERVENER_4 + CHOOSE_A_DIFFERENT_PARTY);
    }

    private void setUpAddedDocuments() {
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
    }

    private void setUpRemovedDocuments() {
        List<UploadCaseDocumentCollection> beforeEventDocList = new ArrayList<>();
        UploadCaseDocumentCollection removedDoc = createContestedUploadDocumentItem(CaseDocumentType.OTHER,
            CaseDocumentParty.APPLICANT, YesOrNo.NO, YesOrNo.NO, "Other Example");
        beforeEventDocList.add(removedDoc);
        beforeEventDocList.add(createContestedUploadDocumentItem(CaseDocumentType.FORM_B,
            CaseDocumentParty.APPLICANT, YesOrNo.NO, YesOrNo.NO, null));
        beforeEventDocList.add(createContestedUploadDocumentItem(CaseDocumentType.FORM_F,
            CaseDocumentParty.APPLICANT, YesOrNo.NO, YesOrNo.NO, null));
        beforeEventDocList.add(createContestedUploadDocumentItem(CaseDocumentType.CARE_PLAN,
            CaseDocumentParty.APPLICANT, YesOrNo.NO, YesOrNo.NO, null));
        beforeEventDocList.add(createContestedUploadDocumentItem(CaseDocumentType.PENSION_PLAN,
            CaseDocumentParty.APPLICANT, YesOrNo.NO, YesOrNo.NO, null));
        caseData.getUploadCaseDocumentWrapper()
            .getDocumentCollectionPerType(CaseDocumentCollectionType.APP_OTHER_COLLECTION)
            .addAll(beforeEventDocList);
        caseDetailsBefore.getData().getUploadCaseDocumentWrapper()
            .getDocumentCollectionPerType(CaseDocumentCollectionType.APP_OTHER_COLLECTION)
            .addAll(beforeEventDocList);
        screenUploadDocumentList.addAll(beforeEventDocList);
        screenUploadDocumentList.remove(removedDoc);
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

    protected FinremCaseDetails<FinremCaseDataContested> buildCaseDetails() {
        FinremCaseDataContested finremCaseData = FinremCaseDataContested.builder()
            .uploadCaseDocumentWrapper(UploadCaseDocumentWrapper.builder().build())
            .build();
        return FinremCaseDetails.<FinremCaseDataContested>builder().id(123L).caseType(CaseType.CONTESTED)
            .data(finremCaseData).build();
    }
}