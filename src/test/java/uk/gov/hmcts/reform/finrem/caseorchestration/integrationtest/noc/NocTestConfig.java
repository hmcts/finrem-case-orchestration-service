package uk.gov.hmcts.reform.finrem.caseorchestration.integrationtest.noc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AdditionalHearingDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralEmailService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.HearingDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.HelpWithFeesDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PaperNotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.TransferCourtService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.NocLetterNotificationService;

@Configuration
@ComponentScan(basePackages = {"uk.gov.hmcts.reform.finrem.caseorchestration.service.noc",
    "uk.gov.hmcts.reform.finrem.caseorchestration.helper"})
public class NocTestConfig {

    @Bean
    public CaseDataService caseDataService() {
        return new CaseDataService();
    }

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


}

