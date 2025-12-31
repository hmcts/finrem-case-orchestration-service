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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Barrister;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.BarristerCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Organisation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrganisationPolicy;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.StopRepresentationWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.stoprepresentingclient.Representation;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.stoprepresentingclient.StopRepresentingClientService;

import java.util.Arrays;
import java.util.List;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.STOP_REPRESENTING_CLIENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;

@Slf4j
@Service
public class StopRepresentingClientAboutToStartHandler extends FinremCallbackHandler {

    private final StopRepresentingClientService stopRepresentingClientService;

    private static String[] getApplicantClientAddressLabels() {
        return new String[] {
            "Client's address for service (Applicant)",
            "Keep the Applicant's contact details private from the Respondent?"
        };
    }

    private static String[] getRespondentClientAddressLabels() {
        return new String[] {
            "Client's address for service (Respondent)",
            "Keep the Respondent's contact details private from the Applicant?"
        };
    }

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
            confidentialLabel = getApplicantClientAddressLabels()[1];
        } else if (representation.isRepresentingRespondent()) {
            label += " (Respondent)";
            confidentialLabel = getRespondentClientAddressLabels()[1];
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

        // Extra 1
        if (shouldCaptureApplicantServiceAddressInExtra(caseData, representation)) {
            wrapper.setExtraClientAddr1Label(getApplicantClientAddressLabels()[0]);
            wrapper.setExtraClientAddr1ConfidentialLabel(getApplicantClientAddressLabels()[1]);
        }
        if (shouldCaptureRespondentServiceAddressInExtra(caseData, representation)) {
            wrapper.setExtraClientAddr1Label(getRespondentClientAddressLabels()[0]);
            wrapper.setExtraClientAddr1ConfidentialLabel(getRespondentClientAddressLabels()[1]);
        }
    }

    private boolean shouldCaptureApplicantServiceAddressInExtra(FinremCaseData caseData,
                                                                Representation representation) {
        if (!representation.isRepresentingAnyInterveners()) {
            return false;
        }

        OrganisationPolicy applicantOrg = caseData.getApplicantOrganisationPolicy();
        return isSameOrganisation(resolveIntervenerOrganisationPolicy(caseData, representation), applicantOrg);
    }

    private boolean shouldCaptureRespondentServiceAddressInExtra(FinremCaseData caseData,
                                                                 Representation representation) {
        if (!representation.isRepresentingAnyInterveners()) {
            return false;
        }

        OrganisationPolicy respondentOrg = caseData.getRespondentOrganisationPolicy();
        return isSameOrganisation(resolveIntervenerOrganisationPolicy(caseData, representation), respondentOrg);
    }

    private OrganisationPolicy resolveIntervenerOrganisationPolicy(FinremCaseData caseData,
                                                                   Representation representation) {
        if (representation.isRepresentingAnyIntervenerBarristers()) {
            Organisation organisation = getInterevenerBarrister(caseData, representation).getOrganisation();
            return organisation != null
                ? OrganisationPolicy.builder().organisation(organisation).build()
                : null;
        }

        return getIntervener(caseData, representation).getIntervenerOrganisation();
    }

    private IntervenerWrapper getIntervener(FinremCaseData caseData, Representation representation) {
        return caseData.getInterveners().get(representation.intervenerIndex() - 1);
    }

    private boolean isSameOrganisation(OrganisationPolicy organisationPolicy1, OrganisationPolicy organisationPolicy2) {
        return stopRepresentingClientService.isSameOrganisation(
            ofNullable(organisationPolicy1)
                .map(OrganisationPolicy::getOrganisation).orElse(null),
            ofNullable(organisationPolicy2)
                .map(OrganisationPolicy::getOrganisation).orElse(null));
    }

    private Barrister getInterevenerBarrister(FinremCaseData caseData, Representation representation) {
        int index = representation.intervenerIndex();
        List<BarristerCollectionItem> items = caseData.getBarristerCollectionWrapper().getIntervenerBarristersByIndex(index);
        return items.stream().map(BarristerCollectionItem::getValue)
            .filter(b -> representation.userId().equals(b.getUserId()))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException(format("%s - Cannot find organisation of intervener barrister",
                caseData.getCcdCaseId())));
    }
}
