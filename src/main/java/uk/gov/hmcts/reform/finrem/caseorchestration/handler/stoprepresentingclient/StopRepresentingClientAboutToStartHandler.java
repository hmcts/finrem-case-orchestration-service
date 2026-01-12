package uk.gov.hmcts.reform.finrem.caseorchestration.handler.stoprepresentingclient;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
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
import uk.gov.hmcts.reform.finrem.caseorchestration.service.stoprepresentingclient.RepresentativeInContext;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.stoprepresentingclient.StopRepresentingClientService;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;
import static org.apache.commons.collections4.ListUtils.emptyIfNull;
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

    private static String[] getIntervenerClientAddressLabels(int index) {
        return new String[] {
            format("Client's address for service (Intervener %s)", index),
            format("Keep the Intervener %s's contact details private from the Applicant & Respondent?", index)
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

        RepresentativeInContext representativeInContext = stopRepresentingClientService.buildRepresentation(caseData, userAuthorisation);
        prepareStopRepresentationWrapper(caseData, representativeInContext);
        prepareExtraClientAddresses(caseData, representativeInContext);

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().data(caseData).build();
    }

    private void prepareStopRepresentationWrapper(FinremCaseData caseData, RepresentativeInContext representativeInContext) {
        StopRepresentationWrapper wrapper = caseData.getStopRepresentationWrapper();

        boolean showClientAddressForService = true;
        String label = null;
        String confidentialLabel = null;
        if (representativeInContext.isApplicationRepresentative()) {
            label = getApplicantClientAddressLabels()[0];
            confidentialLabel = getApplicantClientAddressLabels()[1];
        } else if (representativeInContext.isRespondentRepresentative()) {
            label = getRespondentClientAddressLabels()[0];
            confidentialLabel = getRespondentClientAddressLabels()[1];
        } else if (representativeInContext.isIntervenerRepresentative()) {
            if (representativeInContext.isIntervenerBarrister()
                && !stopRepresentingClientService.isIntervenerBarristerFromSameOrganisationAsSolicitor(caseData, representativeInContext)) {
                showClientAddressForService  = false;
            } else {
                int index = representativeInContext.intervenerIndex();

                label = getIntervenerClientAddressLabels(index)[0];
                confidentialLabel = getIntervenerClientAddressLabels(index)[1];
            }
        } else {
            throw new UnsupportedOperationException(format("%s - It supports applicant/respondent representatives only",
                caseData.getCcdCaseId()));
        }

        wrapper.setClientAddressForServiceConfidentialLabel(confidentialLabel);
        wrapper.setClientAddressForServiceLabel(label);
        wrapper.setShowClientAddressForService(YesOrNo.forValue(showClientAddressForService));
    }

    private void prepareExtraClientAddresses(FinremCaseData caseData, RepresentativeInContext representativeInContext) {
        int extraFieldIndex = 1;
        if (shouldCaptureApplicantServiceAddressInExtra(caseData, representativeInContext)) {
            setExtraField(caseData, extraFieldIndex++, ExtraAddrType.APPLICANT.getId(), getApplicantClientAddressLabels()[0],
                getApplicantClientAddressLabels()[1]);
        } else if (shouldCaptureRespondentServiceAddressInExtra(caseData, representativeInContext)) {
            setExtraField(caseData, extraFieldIndex++, ExtraAddrType.RESPONDENT.getId(), getRespondentClientAddressLabels()[0],
                getRespondentClientAddressLabels()[1]);
        }
        if (shouldCaptureIntervenerOneServiceAddressInExtraField(caseData, representativeInContext)) {
            setExtraField(caseData, extraFieldIndex++, ExtraAddrType.INTERVENER1.getId(), getIntervenerClientAddressLabels(1)[0],
                getIntervenerClientAddressLabels(1)[1]);
        }
        if (shouldCaptureIntervenerTwoServiceAddressInExtraField(caseData, representativeInContext)) {
            setExtraField(caseData, extraFieldIndex++, ExtraAddrType.INTERVENER2.getId(), getIntervenerClientAddressLabels(2)[0],
                getIntervenerClientAddressLabels(2)[1]);
        }
        if (shouldCaptureIntervenerThreeServiceAddressInExtraField(caseData, representativeInContext)) {
            setExtraField(caseData, extraFieldIndex++, ExtraAddrType.INTERVENER3.getId(), getIntervenerClientAddressLabels(3)[0],
                getIntervenerClientAddressLabels(3)[1]);
        }
        if (shouldCaptureIntervenerFourServiceAddressInExtraField(caseData, representativeInContext)) {
            setExtraField(caseData, extraFieldIndex, ExtraAddrType.INTERVENER4.getId(), getIntervenerClientAddressLabels(4)[0],
                getIntervenerClientAddressLabels(4)[1]);
        }
    }

    private OrganisationPolicy getCurrentUserOrganisationPolicy(FinremCaseData caseData, RepresentativeInContext representativeInContext) {
        if (representativeInContext.isApplicationRepresentative()) {
            return caseData.getApplicantOrganisationPolicy();
        }
        if (representativeInContext.isRespondentRepresentative()) {
            return caseData.getRespondentOrganisationPolicy();
        }
        if (representativeInContext.isIntervenerRepresentative() && !representativeInContext.isIntervenerBarrister()) {
            return getIntervener(caseData, representativeInContext).getIntervenerOrganisation();
        }
        return OrganisationPolicy.builder().organisation(getIntervenerBarrister(caseData, representativeInContext).getOrganisation())
            .build();
    }

    private boolean shouldCaptureIntervenerOneServiceAddressInExtraField(FinremCaseData caseData, RepresentativeInContext representativeInContext) {
        if (Integer.valueOf(1).equals(representativeInContext.intervenerIndex())) {
            return false;
        }
        return isSameOrganisation(resolveIntervenerOrganisationPolicy(caseData, 1),
            getCurrentUserOrganisationPolicy(caseData, representativeInContext));
    }

    private boolean shouldCaptureIntervenerTwoServiceAddressInExtraField(FinremCaseData caseData, RepresentativeInContext representativeInContext) {
        if (Integer.valueOf(2).equals(representativeInContext.intervenerIndex())) {
            return false;
        }
        return isSameOrganisation(resolveIntervenerOrganisationPolicy(caseData, 2),
            getCurrentUserOrganisationPolicy(caseData, representativeInContext));
    }

    private boolean shouldCaptureIntervenerThreeServiceAddressInExtraField(FinremCaseData caseData, RepresentativeInContext representativeInContext) {
        if (Integer.valueOf(3).equals(representativeInContext.intervenerIndex())) {
            return false;
        }
        return isSameOrganisation(resolveIntervenerOrganisationPolicy(caseData, 3),
            getCurrentUserOrganisationPolicy(caseData, representativeInContext));
    }

    private boolean shouldCaptureIntervenerFourServiceAddressInExtraField(FinremCaseData caseData, RepresentativeInContext representativeInContext) {
        if (Integer.valueOf(4).equals(representativeInContext.intervenerIndex())) {
            return false;
        }
        return isSameOrganisation(resolveIntervenerOrganisationPolicy(caseData, 4),
            getCurrentUserOrganisationPolicy(caseData, representativeInContext));
    }

    private boolean shouldCaptureApplicantServiceAddressInExtra(FinremCaseData caseData, RepresentativeInContext representativeInContext) {
        if (!representativeInContext.isIntervenerRepresentative()) {
            return false;
        }

        OrganisationPolicy applicantOrg = caseData.getApplicantOrganisationPolicy();
        return isSameOrganisation(resolveIntervenerOrganisationPolicy(caseData, representativeInContext), applicantOrg);
    }

    private boolean shouldCaptureRespondentServiceAddressInExtra(FinremCaseData caseData, RepresentativeInContext representativeInContext) {
        if (!representativeInContext.isIntervenerRepresentative()) {
            return false;
        }

        OrganisationPolicy respondentOrg = caseData.getRespondentOrganisationPolicy();
        return isSameOrganisation(resolveIntervenerOrganisationPolicy(caseData, representativeInContext), respondentOrg);
    }

    private OrganisationPolicy resolveIntervenerOrganisationPolicy(FinremCaseData caseData,
                                                                   RepresentativeInContext representativeInContext) {
        if (representativeInContext.isIntervenerBarrister()) {
            Organisation organisation = getIntervenerBarrister(caseData, representativeInContext).getOrganisation();
            return organisation != null
                ? OrganisationPolicy.builder().organisation(organisation).build()
                : null;
        }

        return getIntervener(caseData, representativeInContext).getIntervenerOrganisation();
    }

    private OrganisationPolicy resolveIntervenerOrganisationPolicy(FinremCaseData caseData,
                                                                   int intervenerIndex) {
        return getIntervener(caseData, intervenerIndex).getIntervenerOrganisation();
    }

    private IntervenerWrapper getIntervener(FinremCaseData caseData, RepresentativeInContext representativeInContext) {
        return caseData.getInterveners().get(representativeInContext.intervenerIndex() - 1);
    }

    private IntervenerWrapper getIntervener(FinremCaseData caseData, int index) {
        return caseData.getInterveners().get(index - 1);
    }

    private boolean isSameOrganisation(OrganisationPolicy organisationPolicy1, OrganisationPolicy organisationPolicy2) {
        return Organisation.isSameOrganisation(
            ofNullable(organisationPolicy1)
                .map(OrganisationPolicy::getOrganisation).orElse(null),
            ofNullable(organisationPolicy2)
                .map(OrganisationPolicy::getOrganisation).orElse(null));
    }

    private Barrister getIntervenerBarrister(FinremCaseData caseData, RepresentativeInContext representativeInContext) {
        int index = representativeInContext.intervenerIndex();
        List<BarristerCollectionItem> items = caseData.getBarristerCollectionWrapper().getIntervenerBarristersByIndex(index);
        return emptyIfNull(items).stream().map(BarristerCollectionItem::getValue)
            .filter(b -> representativeInContext.userId().equals(b.getUserId()))
            .findFirst()
            .orElse(Barrister.builder().build());
    }

    @SuppressWarnings("java:S4512")// Property names are fixed and index is whitelisted (not user-controlled)
    private void setExtraField(FinremCaseData caseData, int extraFieldIndex, String... values) {
        StopRepresentationWrapper wrapper = caseData.getStopRepresentationWrapper();

        // Validate index – prevents arbitrary property access
        if (extraFieldIndex < 1 || extraFieldIndex > 4) {
            throw new IllegalArgumentException("Invalid extraFieldIndex: " + extraFieldIndex);
        }

        // Validate input size
        if (values == null || values.length < 3) {
            throw new IllegalArgumentException("Expected 3 values for extra field");
        }

        try {
            BeanUtils.setProperty(wrapper, format("extraClientAddr%sId", extraFieldIndex), values[0]);
            BeanUtils.setProperty(wrapper, format("extraClientAddr%sLabel", extraFieldIndex), values[1]);
            BeanUtils.setProperty(wrapper, format("extraClientAddr%sConfidentialLabel", extraFieldIndex), values[2]);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException(
                String.format("%s - Fail to set field for index = %s",
                    caseData.getCcdCaseId(), extraFieldIndex),
                e
            );
        }
    }
}
