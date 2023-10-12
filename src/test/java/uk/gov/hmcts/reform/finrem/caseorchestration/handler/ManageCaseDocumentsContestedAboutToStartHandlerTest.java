package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ConfidentialUploadedDocumentData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadConfidentialDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.UploadCaseDocumentWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.LegacyConfidentialDocumentsService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;

@RunWith(MockitoJUnitRunner.class)
public class ManageCaseDocumentsContestedAboutToStartHandlerTest {

    private ManageCaseDocumentsContestedAboutToStartHandler manageCaseDocumentsAboutToStartCaseHandler;

    @Before
    public void setup() {
        manageCaseDocumentsAboutToStartCaseHandler =
            new ManageCaseDocumentsContestedAboutToStartHandler(
                new FinremCaseDetailsMapper(
                    new ObjectMapper().registerModule(new JavaTimeModule())), new LegacyConfidentialDocumentsService());
    }

    @Test
    public void givenACcdCallbackContestedCase_WhenAnAboutToStartEventManageCaseDocuments_thenHandlerCanHandle() {
        assertThat(manageCaseDocumentsAboutToStartCaseHandler
                .canHandle(CallbackType.ABOUT_TO_START, CaseType.CONTESTED, EventType.MANAGE_CASE_DOCUMENTS),
            is(true));
    }

    @Test
    public void givenACcdCallbackConsentedCase_WhenAnAboutToStartEventManageCaseDocuments_thenHandlerCanNotHandle() {
        assertThat(manageCaseDocumentsAboutToStartCaseHandler
                .canHandle(CallbackType.ABOUT_TO_START, CaseType.CONSENTED, EventType.MANAGE_CASE_DOCUMENTS),
            is(false));
    }

    @Test
    public void givenCallbackRequestWithDocumentCollectionsWhenHandleThenScreenCollectionPopulatedWithAllCollections() {

        FinremCallbackRequest callbackRequest =
            FinremCallbackRequest.builder().caseDetails(generalOrderContestedCaseDetails()).build();
        callbackRequest.getCaseDetails().getData().getUploadCaseDocumentWrapper()
            .setAppCaseSummariesCollection(getTestUploadCaseDocumentCollection());

        GenericAboutToStartOrSubmitCallbackResponse response =
            manageCaseDocumentsAboutToStartCaseHandler.handle(callbackRequest, AUTH_TOKEN);

        assertThat(((FinremCaseData) response.getData()).getManageCaseDocumentCollection().size(), is(1));
    }

    @Test
    public void givenConfidentialFlagMissingInExistingDocumentWhenAboutToStartThenSetConfidentialNo() {

        FinremCallbackRequest callbackRequest =
            FinremCallbackRequest.builder().caseDetails(generalOrderContestedCaseDetails()).build();
        callbackRequest.getCaseDetails().getData().getUploadCaseDocumentWrapper().setConfidentialDocumentCollection(
            List.of(UploadCaseDocumentCollection.builder()
                .uploadCaseDocument(UploadCaseDocument.builder().build()).build()));

        GenericAboutToStartOrSubmitCallbackResponse response =
            manageCaseDocumentsAboutToStartCaseHandler.handle(callbackRequest, AUTH_TOKEN);

        assertThat(((FinremCaseData) response.getData()).getUploadCaseDocumentWrapper()
            .getConfidentialDocumentCollection().get(0).getUploadCaseDocument().getCaseDocumentConfidentiality(),
            is(YesOrNo.NO));
    }

    @Test
    public void givenConfidentialFlagNotMissingInExistingDocumentWhenAboutToStartThenConfidentialFlagNotChanged() {

        FinremCallbackRequest callbackRequest =
            FinremCallbackRequest.builder().caseDetails(generalOrderContestedCaseDetails()).build();
        callbackRequest.getCaseDetails().getData().getUploadCaseDocumentWrapper().setConfidentialDocumentCollection(
            List.of(UploadCaseDocumentCollection.builder()
                .uploadCaseDocument(UploadCaseDocument.builder()
                    .caseDocumentConfidentiality(YesOrNo.YES)
                    .build()).build()));

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response =
            manageCaseDocumentsAboutToStartCaseHandler.handle(callbackRequest, AUTH_TOKEN);

        assertThat(((FinremCaseData) response.getData()).getUploadCaseDocumentWrapper()
                .getConfidentialDocumentCollection().get(0).getUploadCaseDocument().getCaseDocumentConfidentiality(),
            is(YesOrNo.YES));
    }

    @Test
    public void givenExistingLegacyConfidentialDocsWhenAboutToStartThenMapLegacyDocsIntoCaseDocumentConfidential() {

        List<ConfidentialUploadedDocumentData> legacyConfidentialDocList = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        legacyConfidentialDocList.add(ConfidentialUploadedDocumentData.builder()
            .id("Legacy doc id")
            .value(UploadConfidentialDocument.builder()
                .documentType(CaseDocumentType.CARE_PLAN)
                .documentComment("Legacy doc comment")
                .confidentialDocumentUploadDateTime(now).build()).build());
        FinremCallbackRequest callbackRequest =
            FinremCallbackRequest.builder().caseDetails(generalOrderContestedCaseDetails()).build();
        callbackRequest.getCaseDetails().getData().setConfidentialDocumentsUploaded(legacyConfidentialDocList);

        GenericAboutToStartOrSubmitCallbackResponse response =
            manageCaseDocumentsAboutToStartCaseHandler.handle(callbackRequest, AUTH_TOKEN);

        assertThat(((FinremCaseData) response.getData()).getManageCaseDocumentCollection().get(0)
                .getUploadCaseDocument().getHearingDetails(),
            is("Legacy doc comment"));
        assertThat(((FinremCaseData) response.getData()).getManageCaseDocumentCollection().get(0)
                .getUploadCaseDocument().getCaseDocumentUploadDateTime(),
            is(now));
    }

    private List<UploadCaseDocumentCollection> getTestUploadCaseDocumentCollection() {
        return Collections.singletonList(UploadCaseDocumentCollection.builder().build());
    }

    private FinremCaseDetails generalOrderContestedCaseDetails() {
        FinremCaseData caseData =
            FinremCaseData.builder().uploadCaseDocumentWrapper(UploadCaseDocumentWrapper.builder().build()).build();

        return FinremCaseDetails.builder().caseType(CaseType.CONTESTED).data(caseData).build();
    }
}