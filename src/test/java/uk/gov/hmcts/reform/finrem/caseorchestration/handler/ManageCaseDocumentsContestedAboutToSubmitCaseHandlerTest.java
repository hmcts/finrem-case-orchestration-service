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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ManageCaseDocumentsCollectionType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.UploadedDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.DocumentCollectionService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.FdrDocumentsCollectionService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.applicant.ApplicantChronologiesStatementCollectionService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.applicant.ApplicantOtherDocumentsCollectionService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.respondent.RespondentChronologiesStatementCollectionService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.respondent.RespondentQuestionnairesAnswersCollectionService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ManageCaseDocumentsContestedAboutToSubmitCaseHandlerTest {

    public static final String AUTH_TOKEN = "AuthTokien";
    @Mock
    private UploadedDocumentService uploadedDocumentHelper;
    private RespondentChronologiesStatementCollectionService respondentChronologiesStatementCollectionService;

    private ApplicantChronologiesStatementCollectionService applicantChronologiesStatementCollectionService;
    private ApplicantOtherDocumentsCollectionService applicantOtherDocumentsCollectionService;
    private FdrDocumentsCollectionService fdrDocumentsCollectionService;
    private RespondentQuestionnairesAnswersCollectionService respondentQuestionnairesAnswersCollectionService;
    private ManageCaseDocumentsContestedAboutToSubmitCaseHandler manageCaseDocumentsAboutToSubmitCaseHandler;
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
            new RespondentChronologiesStatementCollectionService();
        applicantOtherDocumentsCollectionService =
            new ApplicantOtherDocumentsCollectionService();
        fdrDocumentsCollectionService =
            new FdrDocumentsCollectionService();
        respondentQuestionnairesAnswersCollectionService =
            new RespondentQuestionnairesAnswersCollectionService();
        applicantChronologiesStatementCollectionService =
            new ApplicantChronologiesStatementCollectionService();

        List<DocumentCollectionService> documentCollectionServices =
            Stream.of(respondentChronologiesStatementCollectionService, applicantOtherDocumentsCollectionService,
                    fdrDocumentsCollectionService, respondentQuestionnairesAnswersCollectionService,
                    applicantChronologiesStatementCollectionService)
                .collect(Collectors.toList());
        FinremCaseDetailsMapper finremCaseDetailsMapper =
            new FinremCaseDetailsMapper(new ObjectMapper().registerModule(new JavaTimeModule()));
        manageCaseDocumentsAboutToSubmitCaseHandler =
            new ManageCaseDocumentsContestedAboutToSubmitCaseHandler(finremCaseDetailsMapper,
                documentCollectionServices, uploadedDocumentHelper);
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
                .getDocumentCollection(ManageCaseDocumentsCollectionType.APP_OTHER_COLLECTION),
            hasSize(4));
        assertThat(caseData.getUploadCaseDocumentWrapper()
                .getDocumentCollection(ManageCaseDocumentsCollectionType.RESP_CHRONOLOGIES_STATEMENTS_COLLECTION),
            hasSize(3));
        assertThat(caseData.getUploadCaseDocumentWrapper()
                .getDocumentCollection(ManageCaseDocumentsCollectionType.CONTESTED_FDR_CASE_DOCUMENT_COLLECTION),
            hasSize(1));
        assertThat(caseData.getManageCaseDocumentCollection(),
            hasSize(0));
        verify(uploadedDocumentHelper, times(1)).deleteRemovedDocuments(any(), any(), any());
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
            .getDocumentCollection(ManageCaseDocumentsCollectionType.APP_OTHER_COLLECTION)
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
        return FinremCaseDetails.builder().id(Long.valueOf(123)).caseType(CaseType.CONTESTED)
            .data(finremCaseData).build();
    }
}