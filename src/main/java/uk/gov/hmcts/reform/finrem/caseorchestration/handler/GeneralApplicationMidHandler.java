package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralApplicationsCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintDocumentService;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class GeneralApplicationMidHandler extends FinremCallbackHandler {

    private final BulkPrintDocumentService service;

    public GeneralApplicationMidHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                        BulkPrintDocumentService service) {
        super(finremCaseDetailsMapper);
        this.service = service;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.MID_EVENT.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && (EventType.GENERAL_APPLICATION.equals(eventType));
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        String caseId  = String.valueOf(caseDetails.getId());
        log.info("Mid callback event type {} for case id: {}", EventType.GENERAL_APPLICATION, caseDetails.getId());
        FinremCaseData caseData = caseDetails.getData();
        List<String> errors = new ArrayList<>();

        List<GeneralApplicationsCollection> generalApplications = caseData.getGeneralApplicationWrapper().getGeneralApplications();
        if (generalApplications == null || generalApplications.isEmpty()) {
            log.info("Please complete the general application for case Id {}", caseDetails.getId());
            errors.add("Please complete the General Application. No information has been entered for this application.");
        } else {
            generalApplications.forEach(ga -> {
                service.validateEncryptionOnUploadedDocument(ga.getValue().getGeneralApplicationDocument(),
                    caseId, errors, userAuthorisation);
                service.validateEncryptionOnUploadedDocument(ga.getValue().getGeneralApplicationDraftOrder(),
                    caseId, errors, userAuthorisation);
            });
        }

        FinremCaseDetails caseDetailsBefore = callbackRequest.getCaseDetailsBefore();
        FinremCaseData caseDataBefore = caseDetailsBefore.getData();

        List<GeneralApplicationsCollection> generalApplicationsBefore = caseDataBefore.getGeneralApplicationWrapper().getGeneralApplications();

        if (generalApplicationsBefore != null && generalApplications != null && (generalApplicationsBefore.size() == generalApplications.size())) {
            log.info("Please complete the general application for case Id {}", caseDetails.getId());
            errors.add("Any changes to an existing General Applications will not be saved. "
                + "Please add a new General Application in order to progress.");
        }

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(caseData).errors(errors).build();
    }
}
