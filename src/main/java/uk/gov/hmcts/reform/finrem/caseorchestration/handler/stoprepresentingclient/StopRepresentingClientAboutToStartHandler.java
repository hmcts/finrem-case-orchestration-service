package uk.gov.hmcts.reform.finrem.caseorchestration.handler.stoprepresentingclient;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.CallbackHandlerLogger;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.StopRepresentationWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.stoprepresentingclient.Representation;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.stoprepresentingclient.StopRepresentingClientService;

import java.util.Arrays;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.STOP_REPRESENTING_CLIENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;

@Slf4j
@Service
public class StopRepresentingClientAboutToStartHandler extends FinremCallbackHandler {

    private final StopRepresentingClientService stopRepresentingClientService;

    public StopRepresentingClientAboutToStartHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                     StopRepresentingClientService stopRepresentingClientService) {
        super(finremCaseDetailsMapper);
        this.stopRepresentingClientService = stopRepresentingClientService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return ABOUT_TO_START.equals(callbackType)
            && Arrays.asList(CONTESTED, CONSENTED).contains(caseType)
            && STOP_REPRESENTING_CLIENT.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        log.info(CallbackHandlerLogger.aboutToStart(callbackRequest));

        FinremCaseData caseData = callbackRequest.getCaseDetails().getData();

        Representation representation = stopRepresentingClientService.buildRepresentation(caseData, userAuthorisation);
        prepareStopRepresentationWrapper(callbackRequest.getCaseDetails().getData(), representation);

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(callbackRequest.getCaseDetails().getData())
            .build();
    }

    private void prepareStopRepresentationWrapper(FinremCaseData caseData, Representation representation) {
        StopRepresentationWrapper wrapper = caseData.getStopRepresentationWrapper();

        boolean showClientAddressForService = true;
        String label = "Client's address for service";
        String confidentialLabel = null;
        if (representation.isRepresentingApplicant()) {
            label += " (Applicant)";
            confidentialLabel = "Keep the Applicant's contact details private from the Respondent?";
        } else if (representation.isRepresentingRespondent()) {
            label += " (Respondent)";
            confidentialLabel = "Keep the Respondent's contact details private from the Applicant?";
        } else if (representation.isRepresentingAnyInterveners()) {
            int index = representation.intervenerIndex();
            if (representation.isRepresentingAnyIntervenerBarristers()
                && !stopRepresentingClientService.isGoingToRemoveIntervenerSolicitorAccess(caseData, representation)) {
                showClientAddressForService  = false;
                label = null;
            } else {
                label += format(" (Intervener %s)", index);
                confidentialLabel = format("Keep the Intervener %s's contact details private from the Applicant & Respondent?", index);
            }
        } else {
            throw new UnsupportedOperationException(format("%s - It supports applicant/respondent representatives only",
                caseData.getCcdCaseId()));
        }

        wrapper.setClientAddressForServiceConfidentialLabel(confidentialLabel);
        wrapper.setClientAddressForServiceLabel(label);
        wrapper.setShowClientAddressForService(YesOrNo.forValue(showClientAddressForService));
    }
}
