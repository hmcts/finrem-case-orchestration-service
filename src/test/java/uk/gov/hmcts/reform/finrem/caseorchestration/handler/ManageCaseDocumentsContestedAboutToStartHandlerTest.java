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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.UploadCaseDocumentWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.LegacyConfidentialDocumentsService;

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
        LegacyConfidentialDocumentsService legacyConfidentialDocumentsService =
            new LegacyConfidentialDocumentsService();
        manageCaseDocumentsAboutToStartCaseHandler =
            new ManageCaseDocumentsContestedAboutToStartHandler(
                new FinremCaseDetailsMapper(
                    new ObjectMapper().registerModule(new JavaTimeModule())), legacyConfidentialDocumentsService);
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
        callbackRequest.getCaseDetails().getData().getUploadCaseDocumentWrapper()
            .setAppCorrespondenceDocsCollShared(getTestUploadCaseDocumentCollection());

        GenericAboutToStartOrSubmitCallbackResponse response =
            manageCaseDocumentsAboutToStartCaseHandler.handle(callbackRequest, AUTH_TOKEN);

        assertThat(((FinremCaseData) response.getData()).getManageCaseDocumentCollection().size(), is(2));
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