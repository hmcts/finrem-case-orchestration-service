package uk.gov.hmcts.reform.finrem.caseorchestration.integrationtest.noc;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AdditionalHearingDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralEmailService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.HearingDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.HelpWithFeesDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PaperNotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PrdOrganisationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.TransferCourtService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.UpdateSolicitorDetailsService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.assigntojudge.AssignToJudgeCorresponder;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.updatefrc.UpdateFrcCorrespondenceService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.nocworkflows.NoticeOfChangeService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.nocworkflows.UpdateRepresentationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.nocworkflows.UpdateRepresentationWorkflowService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.updatefrc.service.UpdateFrcInfoRespondentDocumentService;

@TestConfiguration
@ComponentScan(basePackages = {"uk.gov.hmcts.reform.finrem.caseorchestration.service.noc",
    "uk.gov.hmcts.reform.finrem.caseorchestration.helper", "uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence"})
public class NocTestConfig {

    @Bean
    public CaseDataService caseDataService() {
        return new CaseDataService(new ObjectMapper());
    }

    @Bean
    public UpdateSolicitorDetailsService solicitorContactDetailsService() {
        return new UpdateSolicitorDetailsService(prdOrganisationService, new ObjectMapper(), caseDataService());
    }

    @MockBean
    private PrdOrganisationService prdOrganisationService;

    @MockBean
    private PaperNotificationService paperNotificationService;
    @MockBean
    private GeneralEmailService generalEmailService;
    @MockBean
    private HelpWithFeesDocumentService helpWithFeesDocumentService;
    @MockBean
    private HearingDocumentService hearingDocumentService;
    @MockBean
    private AdditionalHearingDocumentService additionalHearingDocumentService;
    @MockBean
    private TransferCourtService transferCourtService;
    @MockBean
    private FeatureToggleService featureToggleService;
    @MockBean
    private NoticeOfChangeService noticeOfChangeService;
    @MockBean
    private UpdateRepresentationWorkflowService updateRepresentationWorkflowService;
    @MockBean
    private UpdateRepresentationService updateRepresentationService;
    @MockBean
    UpdateFrcCorrespondenceService updateFrcCorrespondenceService;
    @MockBean
    UpdateFrcInfoRespondentDocumentService updateFrcInfoRespondentDocumentService;
    @MockBean
    AssignToJudgeCorresponder assignToJudgeCorresponder;

}

