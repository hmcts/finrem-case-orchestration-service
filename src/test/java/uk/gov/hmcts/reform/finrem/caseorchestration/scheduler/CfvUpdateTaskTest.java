package uk.gov.hmcts.reform.finrem.caseorchestration.scheduler;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.address.LetterAddresseeGeneratorMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.State;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CcdService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SystemUserService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.FdrDocumentsHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.applicant.ApplicantFdrDocumentCategoriser;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.applicant.ApplicantOtherDocumentsHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenerfour.IntervenerFourFdrDocumentCategoriser;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenerfour.IntervenerFourFdrHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenerfour.IntervenerFourOtherDocumentsHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenerone.IntervenerOneFdrDocumentCategoriser;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenerone.IntervenerOneFdrHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenerone.IntervenerOneOtherDocumentsHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenerthree.IntervenerThreeFdrDocumentCategoriser;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenerthree.IntervenerThreeFdrHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenerthree.IntervenerThreeOtherDocumentsHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenertwo.IntervenerTwoFdrDocumentCategoriser;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenertwo.IntervenerTwoFdrHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenertwo.IntervenerTwoOtherDocumentsHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.respondent.RespondentFdrDocumentCategoriser;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.respondent.RespondentOtherDocumentsHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory.GeneralApplicationsCategoriser;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory.SendOrdersCategoriser;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory.UploadedDraftOrderCategoriser;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.csv.CaseReference;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;

@ExtendWith(SpringExtension.class)
@TestPropertySource(properties = {"cron.cfvUpdate.batchSize=1"})
@ContextConfiguration(classes = {
    ObjectMapper.class, FinremCaseDetailsMapper.class,
    CfvUpdateTask.class,
    CfvUpdateTaskDocumentCategoriser.class, DocumentHelper.class, CaseDataService.class,
    UploadedDraftOrderCategoriser.class, GeneralApplicationsCategoriser.class, SendOrdersCategoriser.class,
    ApplicantOtherDocumentsHandler.class, RespondentOtherDocumentsHandler.class,
    IntervenerOneOtherDocumentsHandler.class, IntervenerTwoOtherDocumentsHandler.class,
    IntervenerThreeOtherDocumentsHandler.class, IntervenerFourOtherDocumentsHandler.class,
    ApplicantFdrDocumentCategoriser.class, RespondentFdrDocumentCategoriser.class,
    IntervenerOneFdrDocumentCategoriser.class, IntervenerTwoFdrDocumentCategoriser.class,
    IntervenerThreeFdrDocumentCategoriser.class, IntervenerFourFdrDocumentCategoriser.class,
    FdrDocumentsHandler.class, IntervenerOneFdrHandler.class,
    IntervenerTwoFdrHandler.class, IntervenerThreeFdrHandler.class, IntervenerFourFdrHandler.class
})
class CfvUpdateTaskTest {

    @Autowired
    CfvUpdateTask cfvUpdateTask;

    @Autowired
    FinremCaseDetailsMapper finremCaseDetailsMapper;

    @MockBean
    CcdService ccdService;

    @MockBean
    SystemUserService systemUserService;

    @MockBean
    FeatureToggleService featureToggleService;

    @MockBean
    GenericDocumentService genericDocumentService;

    @MockBean
    LetterAddresseeGeneratorMapper letterAddresseeGeneratorMapper;

    @BeforeEach
    void setup() {
        when(systemUserService.getSysUserToken()).thenReturn(AUTH_TOKEN);
        when(featureToggleService.isCaseFileViewEnabled()).thenReturn(true);
    }

    @Test
    void testGetCaseReferences() {
        FinremCaseData caseData = FinremCaseData.builder().build();
        SearchResult searchResult = createSearchResult(caseData);
        when(ccdService.esSearchCases(any(CaseType.class), anyString(), anyString())).thenReturn(searchResult);

        List<CaseReference> caseReferences = cfvUpdateTask.getCaseReferences();
        assertThat(caseReferences).hasSize(1);
    }

    @Test
    void testExecuteTask() {
        FinremCaseData caseData = FinremCaseData.builder().build();
        FinremCaseDetails caseDetails = FinremCaseDetails.builder()
            .data(caseData)
            .build();

        cfvUpdateTask.executeTask(caseDetails);

        assertThat(caseDetails.getData().getCfvMigrationWrapper().getCfvMigrationVersion()).isEqualTo("1");
    }

    private SearchResult createSearchResult(FinremCaseData caseData) {
        FinremCaseDetails caseDetails = FinremCaseDetails.builder()
            .id(1L)
            .caseType(CaseType.CONTESTED)
            .state(State.APPLICATION_ISSUED)
            .data(caseData)
            .build();

        return SearchResult.builder()
            .cases(List.of(finremCaseDetailsMapper.mapToCaseDetails(caseDetails)))
            .total(1)
            .build();
    }
}
