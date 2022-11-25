package uk.gov.hmcts.reform.finrem.caseorchestration.integrationtest.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.ConsentedApplicationHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.GeneralApplicationHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.InterimHearingHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.UploadedConfidentialDocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.UploadedDocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.UploadedGeneralDocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.InterimHearingItemMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ApprovedOrderNoticeOfHearingService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseAssignedRoleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseManagementLocationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CcdService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentHearingService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderApprovedDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralApplicationDirectionsService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralApplicationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralOrderService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.HearingDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.InterimHearingService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ManageCaseDocumentsService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.OnStartDefaultValueService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PaperNotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.RefusalOrderDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.UploadApprovedOrderService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.barristers.BarristerValidationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.barristers.ManageBarristerService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.CaseDocumentHandler;

import java.util.List;

@TestConfiguration
@ComponentScan(basePackages = {"uk.gov.hmcts.reform.finrem.caseorchestration.handler"})
public class HandlerConflictsTestConfiguration {

    @MockBean
    CaseAssignedRoleService caseAssignedRoleService;

    @MockBean
    ManageBarristerService manageBarristerService;

    @MockBean
    BarristerValidationService barristerValidationService;

    @MockBean
    UploadApprovedOrderService uploadApprovedOrderService;

    @MockBean
    ApprovedOrderNoticeOfHearingService approvedOrderNoticeOfHearingService;

    @MockBean
    ConsentedApplicationHelper helper;

    @MockBean
    ConsentOrderService consentOrderService;

    @MockBean
    ConsentOrderApprovedDocumentService consentOrderApprovedDocumentService;

    @MockBean
    GenericDocumentService genericDocumentService;

    @MockBean
    ConsentOrderPrintService consentOrderPrintService;

    @MockBean
    DocumentHelper documentHelper;

    @MockBean
    ObjectMapper objectMapper;

    @MockBean
    CaseDataService caseDataService;

    @MockBean
    NotificationService notificationService;

    @MockBean
    CaseManagementLocationService caseManagementLocationService;

    @MockBean
    GeneralApplicationHelper generalApplicationHelper;

    @MockBean
    GeneralApplicationService generalApplicationService;

    @MockBean
    GeneralApplicationDirectionsService generalApplicationDirectionsService;

    @MockBean
    ConsentHearingService consentHearingService;

    @MockBean
    InterimHearingHelper interimHearingHelper;

    @MockBean
    InterimHearingItemMapper interimHearingItemMapper;

    @MockBean
    InterimHearingService interimHearingService;

    @MockBean
    OnStartDefaultValueService onStartDefaultValueService;

    @MockBean
    ManageCaseDocumentsService manageCaseDocumentsService;

    @MockBean
    UploadedDocumentHelper uploadedDocumentHelper;

    @MockBean
    RefusalOrderDocumentService refusalOrderDocumentService;

    @MockBean
    PaperNotificationService paperNotificationService;

    @MockBean
    BulkPrintService bulkPrintService;

    @MockBean
    GeneralOrderService generalOrderService;

    @MockBean
    FeatureToggleService featureToggleService;

    @MockBean
    CcdService ccdService;

    @MockBean
    IdamService idamService;

    @MockBean
    UploadedConfidentialDocumentHelper uploadedConfidentialDocumentHelper;

    @MockBean
    List<CaseDocumentHandler> caseDocumentHandlers;

    @MockBean
    UploadedGeneralDocumentHelper uploadedGeneralDocumentHelper;

    @MockBean
    FinremCaseDetailsMapper finremCaseDetailsMapper;

    @MockBean
    HearingDocumentService hearingDocumentService;
}
