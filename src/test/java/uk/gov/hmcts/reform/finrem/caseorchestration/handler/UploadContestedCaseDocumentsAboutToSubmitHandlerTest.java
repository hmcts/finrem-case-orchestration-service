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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseAssignedUserRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseAssignedUserRolesResource;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.UploadCaseDocumentWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseAssignedRoleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.UploadedDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.CaseDocumentsHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.DocumentHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.FdrDocumentsHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.IntervenerOneFdrHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.applicant.ApplicantChronologiesStatementHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenerfour.IntervenerFourChronologiesStatementHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenerone.IntervenerOneChronologiesStatementHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenerthree.IntervenerThreeChronologiesStatementHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenertwo.IntervenerTwoChronologiesStatementHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.respondent.RespondentChronologiesStatementHandler;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UploadContestedCaseDocumentsAboutToSubmitHandlerTest {

    public static final String AUTH_TOKEN = "AuthTokien";

    private static final String USER_ID = "testUserId";
    public static final String CASE_ID = "1234567890";

    @Mock
    protected UploadedDocumentService uploadedDocumentHelper;
    @Mock
    private CaseAssignedRoleService caseAssignedRoleService;
    @Mock
    private FeatureToggleService featureToggleService;

    private ApplicantChronologiesStatementHandler applicantChronologiesStatementHandler;
    private RespondentChronologiesStatementHandler respondentChronologiesStatementHandler;

    private IntervenerOneChronologiesStatementHandler intervenerOneChronologiesStatementHandler;
    private IntervenerTwoChronologiesStatementHandler intervenerTwoChronologiesStatementHandler;

    private IntervenerThreeChronologiesStatementHandler intervenerThreeChronologiesStatementHandler;

    private IntervenerFourChronologiesStatementHandler intervenerFourChronologiesStatementHandler;

    private IntervenerOneFdrHandler intervenerOneFdrHandler;

    private CaseDocumentsHandler caseDocumentHandler;
    private FdrDocumentsHandler fdrDocumentsHandler;
    private UploadContestedCaseDocumentsAboutToSubmitHandler uploadContestedCaseDocumentsHandler;
    private FinremCaseDetails caseDetails;
    private FinremCaseDetails caseDetailsBefore;
    private FinremCaseData caseData;
    private final List<UploadCaseDocumentCollection> screenUploadDocumentList = new ArrayList<>();



    @Before
    public void setup() {
        caseDetails = buildCaseDetails();
        caseDetailsBefore = buildCaseDetails();
        caseData = caseDetails.getData();

        applicantChronologiesStatementHandler = new ApplicantChronologiesStatementHandler();
        respondentChronologiesStatementHandler = new RespondentChronologiesStatementHandler();
        intervenerOneChronologiesStatementHandler = new IntervenerOneChronologiesStatementHandler();
        intervenerTwoChronologiesStatementHandler = new IntervenerTwoChronologiesStatementHandler();
        intervenerThreeChronologiesStatementHandler = new IntervenerThreeChronologiesStatementHandler();
        intervenerFourChronologiesStatementHandler = new IntervenerFourChronologiesStatementHandler();
        intervenerOneFdrHandler = new IntervenerOneFdrHandler();
        caseDocumentHandler = new CaseDocumentsHandler();
        fdrDocumentsHandler = new FdrDocumentsHandler();


        List<DocumentHandler> documentHandlers =
            Stream.of(applicantChronologiesStatementHandler, respondentChronologiesStatementHandler,
                    intervenerOneChronologiesStatementHandler, intervenerTwoChronologiesStatementHandler,
                    intervenerThreeChronologiesStatementHandler, intervenerFourChronologiesStatementHandler,
                    intervenerOneFdrHandler, caseDocumentHandler, fdrDocumentsHandler)
                .collect(Collectors.toList());
        FinremCaseDetailsMapper finremCaseDetailsMapper =
            new FinremCaseDetailsMapper(new ObjectMapper().registerModule(new JavaTimeModule()));
        uploadContestedCaseDocumentsHandler =
            new UploadContestedCaseDocumentsAboutToSubmitHandler(finremCaseDetailsMapper,
                documentHandlers, uploadedDocumentHelper, caseAssignedRoleService, featureToggleService);

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
    public void givenAnNonConfidentialUploadCaseFile_WhenFdr_thenFdrCollectionsSet() {
        screenUploadDocumentList.add(createContestedUploadDocumentItem(CaseDocumentType.CHRONOLOGY,
            null, YesOrNo.NO, YesOrNo.YES, null));
        screenUploadDocumentList.add(createContestedUploadDocumentItem(CaseDocumentType.FORM_G,
            null, YesOrNo.NO, YesOrNo.YES, null));

        when(caseAssignedRoleService.getCaseAssignedUserRole(CASE_ID, AUTH_TOKEN))
            .thenReturn(getCaseAssignedUserRolesResource(CaseRole.APP_SOLICITOR.getCcdCode()));

        caseDetails.getData().setManageCaseDocumentCollection(screenUploadDocumentList);

        uploadContestedCaseDocumentsHandler.handle(
            FinremCallbackRequest.builder().caseDetails(caseDetails).caseDetailsBefore(caseDetailsBefore).build(),
            AUTH_TOKEN);

        assertThat(caseData.getUploadCaseDocumentWrapper()
                .getDocumentCollectionPerType(CaseDocumentCollectionType.CONTESTED_FDR_CASE_DOCUMENT_COLLECTION),
            hasSize(2));
        assertThat(caseData.getManageCaseDocumentCollection(),
            hasSize(0));
    }

    @Test
    public void givenAnNonConfidentialUploadCaseFile_WhenActiveUserIsApplicantAndScreenPartyIsNull_thenApplicantCollectionsSet() {
        screenUploadDocumentList.add(createContestedUploadDocumentItem(CaseDocumentType.CHRONOLOGY,
            null, YesOrNo.NO, YesOrNo.NO, null));
        screenUploadDocumentList.add(createContestedUploadDocumentItem(CaseDocumentType.FORM_G,
            null, YesOrNo.NO, YesOrNo.NO, null));

        when(caseAssignedRoleService.getCaseAssignedUserRole(CASE_ID, AUTH_TOKEN))
            .thenReturn(getCaseAssignedUserRolesResource(CaseRole.APP_SOLICITOR.getCcdCode()));

        caseDetails.getData().setManageCaseDocumentCollection(screenUploadDocumentList);

        uploadContestedCaseDocumentsHandler.handle(
            FinremCallbackRequest.builder().caseDetails(caseDetails).caseDetailsBefore(caseDetailsBefore).build(),
            AUTH_TOKEN);

        assertThat(caseData.getUploadCaseDocumentWrapper()
                .getDocumentCollectionPerType(CaseDocumentCollectionType.APP_CHRONOLOGIES_STATEMENTS_COLLECTION),
            hasSize(2));
        assertThat(caseData.getManageCaseDocumentCollection(),
            hasSize(0));
    }

    @Test
    public void givenAnNonConfidentialUploadCaseFile_WhenActiveUserIsRespondentAndScreenPartyIsNull_thenRespondentCollectionsSet() {
        screenUploadDocumentList.add(createContestedUploadDocumentItem(CaseDocumentType.CHRONOLOGY,
            null, YesOrNo.NO, YesOrNo.NO, null));
        screenUploadDocumentList.add(createContestedUploadDocumentItem(CaseDocumentType.FORM_G,
            null, YesOrNo.NO, YesOrNo.NO, null));

        when(caseAssignedRoleService.getCaseAssignedUserRole(CASE_ID, AUTH_TOKEN))
            .thenReturn(getCaseAssignedUserRolesResource(CaseRole.RESP_SOLICITOR.getCcdCode()));

        caseDetails.getData().setManageCaseDocumentCollection(screenUploadDocumentList);

        uploadContestedCaseDocumentsHandler.handle(
            FinremCallbackRequest.builder().caseDetails(caseDetails).caseDetailsBefore(caseDetailsBefore).build(),
            AUTH_TOKEN);

        assertThat(caseData.getUploadCaseDocumentWrapper()
                .getDocumentCollectionPerType(CaseDocumentCollectionType.RESP_CHRONOLOGIES_STATEMENTS_COLLECTION),
            hasSize(2));
        assertThat(caseData.getManageCaseDocumentCollection(),
            hasSize(0));
    }

    @Test
    public void givenAnNonConfidentialUploadCaseFile_WhenActiveUserIsIntervSol1AndScreenPartyIsNull_thenIntervSol1CollectionsSet() {
        screenUploadDocumentList.add(createContestedUploadDocumentItem(CaseDocumentType.CHRONOLOGY,
            null, YesOrNo.NO, YesOrNo.NO, null));
        screenUploadDocumentList.add(createContestedUploadDocumentItem(CaseDocumentType.FORM_G,
            null, YesOrNo.NO, YesOrNo.NO, null));

        when(caseAssignedRoleService.getCaseAssignedUserRole(CASE_ID, AUTH_TOKEN))
            .thenReturn(getCaseAssignedUserRolesResource(CaseRole.INTVR_SOLICITOR_1.getCcdCode()));

        caseDetails.getData().setManageCaseDocumentCollection(screenUploadDocumentList);

        uploadContestedCaseDocumentsHandler.handle(
            FinremCallbackRequest.builder().caseDetails(caseDetails).caseDetailsBefore(caseDetailsBefore).build(),
            AUTH_TOKEN);

        assertThat(caseData.getUploadCaseDocumentWrapper()
                .getDocumentCollectionPerType(
                    CaseDocumentCollectionType.INTERVENER_ONE_CHRONOLOGIES_STATEMENTS_COLLECTION),
            hasSize(2));
        assertThat(caseData.getManageCaseDocumentCollection(),
            hasSize(0));
    }


    @Test
    public void givenAnNonConfidentialUploadCaseFile_WhenActiveUserIsIntervSol2AndScreenPartyIsNull_thenIntervSol2CollectionsSet() {
        screenUploadDocumentList.add(createContestedUploadDocumentItem(CaseDocumentType.CHRONOLOGY,
            null, YesOrNo.NO, YesOrNo.NO, null));
        screenUploadDocumentList.add(createContestedUploadDocumentItem(CaseDocumentType.FORM_G,
            null, YesOrNo.NO, YesOrNo.NO, null));

        when(caseAssignedRoleService.getCaseAssignedUserRole(CASE_ID, AUTH_TOKEN))
            .thenReturn(getCaseAssignedUserRolesResource(CaseRole.INTVR_SOLICITOR_2.getCcdCode()));

        caseDetails.getData().setManageCaseDocumentCollection(screenUploadDocumentList);

        uploadContestedCaseDocumentsHandler.handle(
            FinremCallbackRequest.builder().caseDetails(caseDetails).caseDetailsBefore(caseDetailsBefore).build(),
            AUTH_TOKEN);

        assertThat(caseData.getUploadCaseDocumentWrapper()
                .getDocumentCollectionPerType(CaseDocumentCollectionType.INTERVENER_TWO_CHRONOLOGIES_STATEMENTS_COLLECTION),
            hasSize(2));
        assertThat(caseData.getManageCaseDocumentCollection(),
            hasSize(0));
    }

    @Test
    public void givenAnNonConfidentialUploadCaseFile_WhenActiveUserIsIntervSol3AndScreenPartyIsNull_thenIntervSol3CollectionsSet() {
        screenUploadDocumentList.add(createContestedUploadDocumentItem(CaseDocumentType.CHRONOLOGY,
            null, YesOrNo.NO, YesOrNo.NO, null));
        screenUploadDocumentList.add(createContestedUploadDocumentItem(CaseDocumentType.FORM_G,
            null, YesOrNo.NO, YesOrNo.NO, null));

        when(caseAssignedRoleService.getCaseAssignedUserRole(CASE_ID, AUTH_TOKEN))
            .thenReturn(getCaseAssignedUserRolesResource(CaseRole.INTVR_SOLICITOR_3.getCcdCode()));

        caseDetails.getData().setManageCaseDocumentCollection(screenUploadDocumentList);

        uploadContestedCaseDocumentsHandler.handle(
            FinremCallbackRequest.builder().caseDetails(caseDetails).caseDetailsBefore(caseDetailsBefore).build(),
            AUTH_TOKEN);

        assertThat(caseData.getUploadCaseDocumentWrapper()
                .getDocumentCollectionPerType(CaseDocumentCollectionType.INTERVENER_THREE_CHRONOLOGIES_STATEMENTS_COLLECTION),
            hasSize(2));
        assertThat(caseData.getManageCaseDocumentCollection(),
            hasSize(0));
    }

    @Test
    public void givenAnNonConfidentialUploadCaseFile_WhenActiveUserIsIntervSol4AndScreenPartyIsNull_thenIntervSol4CollectionsSet() {
        screenUploadDocumentList.add(createContestedUploadDocumentItem(CaseDocumentType.CHRONOLOGY,
            null, YesOrNo.NO, YesOrNo.NO, null));
        screenUploadDocumentList.add(createContestedUploadDocumentItem(CaseDocumentType.FORM_G,
            null, YesOrNo.NO, YesOrNo.NO, null));

        when(caseAssignedRoleService.getCaseAssignedUserRole(CASE_ID, AUTH_TOKEN))
            .thenReturn(getCaseAssignedUserRolesResource(CaseRole.INTVR_SOLICITOR_4.getCcdCode()));

        caseDetails.getData().setManageCaseDocumentCollection(screenUploadDocumentList);

        uploadContestedCaseDocumentsHandler.handle(
            FinremCallbackRequest.builder().caseDetails(caseDetails).caseDetailsBefore(caseDetailsBefore).build(),
            AUTH_TOKEN);

        assertThat(caseData.getUploadCaseDocumentWrapper()
                .getDocumentCollectionPerType(CaseDocumentCollectionType.INTERVENER_FOUR_CHRONOLOGIES_STATEMENTS_COLLECTION),
            hasSize(2));
        assertThat(caseData.getManageCaseDocumentCollection(),
            hasSize(0));
    }

    @Test
    public void givenAnNonConfidentialUploadCaseFile_WhenActiveUserIsCaseWorkerScreenPartyIsNull_thenCaseCollectionsSet() {
        screenUploadDocumentList.add(createContestedUploadDocumentItem(CaseDocumentType.CHRONOLOGY,
            null, YesOrNo.NO, YesOrNo.NO, null));
        screenUploadDocumentList.add(createContestedUploadDocumentItem(CaseDocumentType.FORM_G,
            null, YesOrNo.NO, YesOrNo.NO, null));

        when(caseAssignedRoleService.getCaseAssignedUserRole(CASE_ID, AUTH_TOKEN))
            .thenReturn(getCaseAssignedUserRolesResource(CaseRole.CASEWORKER.getCcdCode()));

        caseDetails.getData().setManageCaseDocumentCollection(screenUploadDocumentList);

        uploadContestedCaseDocumentsHandler.handle(
            FinremCallbackRequest.builder().caseDetails(caseDetails).caseDetailsBefore(caseDetailsBefore).build(),
            AUTH_TOKEN);

        assertThat(caseData.getUploadCaseDocumentWrapper()
                .getDocumentCollectionPerType(CaseDocumentCollectionType.CONTESTED_UPLOADED_DOCUMENTS),
            hasSize(2));
        assertThat(caseData.getManageCaseDocumentCollection(),
            hasSize(0));
    }

    @Test
    public void givenAnFdrCaseFile_WhenActiveUserIsIntervener1_thenIntv1FdrCollectionsSet() {
        screenUploadDocumentList.add(createContestedUploadDocumentItem(CaseDocumentType.CHRONOLOGY,
            null, YesOrNo.NO, YesOrNo.YES, null));
        screenUploadDocumentList.add(createContestedUploadDocumentItem(CaseDocumentType.FORM_G,
            null, YesOrNo.NO, YesOrNo.NO, null));

        when(caseAssignedRoleService.getCaseAssignedUserRole(CASE_ID, AUTH_TOKEN))
            .thenReturn(getCaseAssignedUserRolesResource(CaseRole.INTVR_SOLICITOR_1.getCcdCode()));

        caseDetails.getData().setManageCaseDocumentCollection(screenUploadDocumentList);

        uploadContestedCaseDocumentsHandler.handle(
            FinremCallbackRequest.builder().caseDetails(caseDetails).caseDetailsBefore(caseDetailsBefore).build(),
            AUTH_TOKEN);

        assertThat(caseData.getUploadCaseDocumentWrapper()
                .getDocumentCollectionPerType(
                    CaseDocumentCollectionType.INTERVENER_ONE_CHRONOLOGIES_STATEMENTS_COLLECTION),
            hasSize(1));
        assertThat(caseData.getUploadCaseDocumentWrapper()
                .getDocumentCollectionPerType(CaseDocumentCollectionType.INTERVENER_ONE_FDR_DOCS_COLLECTION),
            hasSize(1));
        assertThat(caseData.getManageCaseDocumentCollection(),
            hasSize(0));
    }

    @Test
    public void givenAnNonConfidentialUploadCaseFile_WhenActiveUserIsApplicantAndScreenPartyIsRespondent_thenApplicantCollectionsSet() {
        screenUploadDocumentList.add(createContestedUploadDocumentItem(CaseDocumentType.CHRONOLOGY,
            CaseDocumentParty.RESPONDENT, YesOrNo.NO, YesOrNo.NO, null));
        screenUploadDocumentList.add(createContestedUploadDocumentItem(CaseDocumentType.FORM_G,
            CaseDocumentParty.RESPONDENT, YesOrNo.NO, YesOrNo.NO, null));

        when(caseAssignedRoleService.getCaseAssignedUserRole(CASE_ID, AUTH_TOKEN))
            .thenReturn(getCaseAssignedUserRolesResource(CaseRole.APP_SOLICITOR.getCcdCode()));

        caseDetails.getData().setManageCaseDocumentCollection(screenUploadDocumentList);

        uploadContestedCaseDocumentsHandler.handle(
            FinremCallbackRequest.builder().caseDetails(caseDetails).caseDetailsBefore(caseDetailsBefore).build(),
            AUTH_TOKEN);

        assertThat(caseData.getUploadCaseDocumentWrapper()
                .getDocumentCollectionPerType(CaseDocumentCollectionType.APP_CHRONOLOGIES_STATEMENTS_COLLECTION),
            hasSize(2));
        assertThat(caseData.getManageCaseDocumentCollection(),
            hasSize(0));
    }

    @Test
    public void givenUploadFileTrialBundleSelected_WhenAboutToSubmit_ThenShowTrialBundleErrorMessage() {

        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();

        screenUploadDocumentList.add(createContestedUploadDocumentItem(CaseDocumentType.TRIAL_BUNDLE,
            null, YesOrNo.YES, YesOrNo.NO, "Other Example"));
        caseDetails.getData().setManageCaseDocumentCollection(screenUploadDocumentList);

        caseDetails.getData().getUploadCaseDocumentWrapper().setUploadCaseDocument(screenUploadDocumentList);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData>
            response = uploadContestedCaseDocumentsHandler.handle(callbackRequest, AUTH_TOKEN);

        assertThat(response.getErrors().size(), is(1));
        assertThat(response.getErrors().iterator().next(),
            is(UploadContestedCaseDocumentsAboutToSubmitHandler.TRIAL_BUNDLE_SELECTED_ERROR));
    }

    @Test
    public void givenUploadFileWithoutTrialBundle_WhenAboutToSubmit_ThenNoErrors() {

        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();

        when(caseAssignedRoleService.getCaseAssignedUserRole(CASE_ID, AUTH_TOKEN))
            .thenReturn(getCaseAssignedUserRolesResource(CaseRole.APP_SOLICITOR.getCcdCode()));

        screenUploadDocumentList.add(createContestedUploadDocumentItem(CaseDocumentType.OTHER,
            null, YesOrNo.YES, YesOrNo.NO, "Other Example"));
        caseDetails.getData().setManageCaseDocumentCollection(screenUploadDocumentList);

        caseDetails.getData().getUploadCaseDocumentWrapper().setUploadCaseDocument(screenUploadDocumentList);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData>
            response = uploadContestedCaseDocumentsHandler.handle(callbackRequest, AUTH_TOKEN);

        assertThat(response.getErrors().size(), is(0));
    }

    @Test
    public void givenUploadFileNoDocSelected_WhenAboutToSubmit_ThenShowNoDocErrorMessage() {

        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();

        caseDetails.getData().setManageCaseDocumentCollection(screenUploadDocumentList);

        caseDetails.getData().getUploadCaseDocumentWrapper().setUploadCaseDocument(screenUploadDocumentList);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData>
            response = uploadContestedCaseDocumentsHandler.handle(callbackRequest, AUTH_TOKEN);

        assertThat(response.getErrors().size(), is(1));
        assertThat(response.getErrors().iterator().next(),
            is(UploadContestedCaseDocumentsAboutToSubmitHandler.NO_DOCUMENT_ERROR));
    }

    private FinremCallbackRequest buildCallbackRequest() {
        FinremCaseData data = FinremCaseData.builder().build();
        FinremCaseDetails caseDetails =
            FinremCaseDetails.builder().data(data).id(Long.valueOf(CASE_ID)).build();
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

    protected FinremCaseDetails buildCaseDetails() {
        FinremCaseData finremCaseData = FinremCaseData.builder()
            .uploadCaseDocumentWrapper(UploadCaseDocumentWrapper.builder().build())
            .build();
        return FinremCaseDetails.builder().id(Long.valueOf(CASE_ID)).caseType(CaseType.CONTESTED)
            .data(finremCaseData).build();
    }

    private CaseAssignedUserRolesResource getCaseAssignedUserRolesResource(String caseRole) {
        return CaseAssignedUserRolesResource.builder()
            .caseAssignedUserRoles(List.of(CaseAssignedUserRole.builder()
                .userId(USER_ID)
                .caseRole(caseRole)
                .caseDataId(CASE_ID)
                .build()))
            .build();
    }
}
