package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.UploadCaseDocumentWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.UploadedDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.DocumentHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.FdrDocumentsHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.applicant.ApplicantChronologiesStatementHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.applicant.ApplicantOtherDocumentsHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.respondent.RespondentChronologiesStatementHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.respondent.RespondentQuestionnairesAnswersHandler;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

@RunWith(MockitoJUnitRunner.class)
public class ManageCaseDocumentsContestedAboutToSubmitHandlerTest {

    public static final String AUTH_TOKEN = "AuthTokien";
    @Mock
    private UploadedDocumentService uploadedDocumentHelper;
    private RespondentChronologiesStatementHandler respondentChronologiesStatementCollectionService;

    private ApplicantChronologiesStatementHandler applicantChronologiesStatementCollectionService;
    private ApplicantOtherDocumentsHandler applicantOtherDocumentsCollectionService;
    private FdrDocumentsHandler fdrDocumentsCollectionService;
    private RespondentQuestionnairesAnswersHandler respondentQuestionnairesAnswersCollectionService;
    private ManageCaseDocumentsContestedAboutToSubmitHandler manageCaseDocumentsAboutToSubmitCaseHandler;
    private FinremCaseDetails caseDetails;
    private FinremCaseDetails caseDetailsBefore;
    private FinremCaseData caseData;
    private List<UploadCaseDocumentCollection> screenUploadDocumentList = new ArrayList<>();


    @Before
    public void setUp() {

        caseDetails = buildCaseDetails();
        caseDetailsBefore = buildCaseDetails();
        caseData = caseDetails.getData();

        respondentChronologiesStatementCollectionService =
            new RespondentChronologiesStatementHandler();
        applicantOtherDocumentsCollectionService =
            new ApplicantOtherDocumentsHandler();
        fdrDocumentsCollectionService =
            new FdrDocumentsHandler();
        respondentQuestionnairesAnswersCollectionService =
            new RespondentQuestionnairesAnswersHandler();
        applicantChronologiesStatementCollectionService =
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
                documentHandlers, uploadedDocumentHelper);
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
            FinremCallbackRequest.builder().caseDetails(caseDetails).caseDetailsBefore(caseDetailsBefore).build(),
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

    protected FinremCaseDetails buildCaseDetails() {
        FinremCaseData finremCaseData = FinremCaseData.builder()
            .uploadCaseDocumentWrapper(UploadCaseDocumentWrapper.builder().build())
            .build();
        return FinremCaseDetails.builder().id(123L).caseType(CaseType.CONTESTED)
            .data(finremCaseData).build();
    }
}