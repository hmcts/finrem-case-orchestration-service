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
import uk.gov.hmcts.reform.finrem.caseorchestration.service.stoprepresentingclient.Representation;
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

        Representation representation = stopRepresentingClientService.buildRepresentation(caseData, userAuthorisation);
        prepareStopRepresentationWrapper(caseData, representation);
        prepareExtraClientAddresses(caseData, representation);

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().data(caseData).build();
    }

    private void prepareStopRepresentationWrapper(FinremCaseData caseData, Representation representation) {
        StopRepresentationWrapper wrapper = caseData.getStopRepresentationWrapper();

        boolean showClientAddressForService = true;
        String label = null;
        String confidentialLabel = null;
        if (representation.isRepresentingApplicant()) {
            label = getApplicantClientAddressLabels()[0];
            confidentialLabel = getApplicantClientAddressLabels()[1];
        } else if (representation.isRepresentingRespondent()) {
            label = getRespondentClientAddressLabels()[0];
            confidentialLabel = getRespondentClientAddressLabels()[1];
        } else if (representation.isRepresentingAnyInterveners()) {
            if (representation.isRepresentingAnyIntervenerBarristers()
                && !stopRepresentingClientService.isIntervenerBarristerFromSameOrganisationAsSolicitor(caseData, representation)) {
                showClientAddressForService  = false;
            } else {
                int index = representation.intervenerIndex();

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

    private void prepareExtraClientAddresses(FinremCaseData caseData, Representation representation) {
        int extraFieldIndex = 1;
        if (shouldCaptureApplicantServiceAddressInExtra(caseData, representation)) {
            setExtraField(caseData, extraFieldIndex++, ExtraAddrType.APPLICANT.getId(), getApplicantClientAddressLabels()[0],
                getApplicantClientAddressLabels()[1]);
        } else if (shouldCaptureRespondentServiceAddressInExtra(caseData, representation)) {
            setExtraField(caseData, extraFieldIndex++, ExtraAddrType.RESPONDENT.getId(), getRespondentClientAddressLabels()[0],
                getRespondentClientAddressLabels()[1]);
        }
        if (shouldCaptureIntervenerOneServiceAddressInExtraField(caseData, representation)) {
            setExtraField(caseData, extraFieldIndex++, ExtraAddrType.INTERVENER1.getId(), getIntervenerClientAddressLabels(1)[0],
                getIntervenerClientAddressLabels(1)[1]);
        }
        if (shouldCaptureIntervenerTwoServiceAddressInExtraField(caseData, representation)) {
            setExtraField(caseData, extraFieldIndex++, ExtraAddrType.INTERVENER2.getId(), getIntervenerClientAddressLabels(2)[0],
                getIntervenerClientAddressLabels(2)[1]);
        }
        if (shouldCaptureIntervenerThreeServiceAddressInExtraField(caseData, representation)) {
            setExtraField(caseData, extraFieldIndex++, ExtraAddrType.INTERVENER3.getId(), getIntervenerClientAddressLabels(3)[0],
                getIntervenerClientAddressLabels(3)[1]);
        }
        if (shouldCaptureIntervenerFourServiceAddressInExtraField(caseData, representation)) {
            setExtraField(caseData, extraFieldIndex, ExtraAddrType.INTERVENER4.getId(), getIntervenerClientAddressLabels(4)[0],
                getIntervenerClientAddressLabels(4)[1]);
        }
    }

    private OrganisationPolicy getCurrentUserOrganisationPolicy(FinremCaseData caseData, Representation representation) {
        if (representation.isRepresentingApplicant()) {
            return caseData.getApplicantOrganisationPolicy();
        }
        if (representation.isRepresentingRespondent()) {
            return caseData.getRespondentOrganisationPolicy();
        }
        if (representation.isRepresentingAnyInterveners() && !representation.isRepresentingAnyIntervenerBarristers()) {
            return getIntervener(caseData, representation).getIntervenerOrganisation();
        }
        return OrganisationPolicy.builder().organisation(getIntervenerBarrister(caseData, representation).getOrganisation())
            .build();
    }

    private boolean shouldCaptureIntervenerOneServiceAddressInExtraField(FinremCaseData caseData, Representation representation) {
        if (Integer.valueOf(1).equals(representation.intervenerIndex())) {
            return false;
        }
        return isSameOrganisation(resolveIntervenerOrganisationPolicy(caseData, 1),
            getCurrentUserOrganisationPolicy(caseData, representation));
    }

    private boolean shouldCaptureIntervenerTwoServiceAddressInExtraField(FinremCaseData caseData, Representation representation) {
        if (Integer.valueOf(2).equals(representation.intervenerIndex())) {
            return false;
        }
        return isSameOrganisation(resolveIntervenerOrganisationPolicy(caseData, 2),
            getCurrentUserOrganisationPolicy(caseData, representation));
    }

    private boolean shouldCaptureIntervenerThreeServiceAddressInExtraField(FinremCaseData caseData, Representation representation) {
        if (Integer.valueOf(3).equals(representation.intervenerIndex())) {
            return false;
        }
        return isSameOrganisation(resolveIntervenerOrganisationPolicy(caseData, 3),
            getCurrentUserOrganisationPolicy(caseData, representation));
    }

    private boolean shouldCaptureIntervenerFourServiceAddressInExtraField(FinremCaseData caseData, Representation representation) {
        if (Integer.valueOf(4).equals(representation.intervenerIndex())) {
            return false;
        }
        return isSameOrganisation(resolveIntervenerOrganisationPolicy(caseData, 4),
            getCurrentUserOrganisationPolicy(caseData, representation));
    }

    private boolean shouldCaptureApplicantServiceAddressInExtra(FinremCaseData caseData, Representation representation) {
        if (!representation.isRepresentingAnyInterveners()) {
            return false;
        }

        OrganisationPolicy applicantOrg = caseData.getApplicantOrganisationPolicy();
        return isSameOrganisation(resolveIntervenerOrganisationPolicy(caseData, representation), applicantOrg);
    }

    private boolean shouldCaptureRespondentServiceAddressInExtra(FinremCaseData caseData, Representation representation) {
        if (!representation.isRepresentingAnyInterveners()) {
            return false;
        }

        OrganisationPolicy respondentOrg = caseData.getRespondentOrganisationPolicy();
        return isSameOrganisation(resolveIntervenerOrganisationPolicy(caseData, representation), respondentOrg);
    }

    private OrganisationPolicy resolveIntervenerOrganisationPolicy(FinremCaseData caseData,
                                                                   Representation representation) {
        if (representation.isRepresentingAnyIntervenerBarristers()) {
            Organisation organisation = getIntervenerBarrister(caseData, representation).getOrganisation();
            return organisation != null
                ? OrganisationPolicy.builder().organisation(organisation).build()
                : null;
        }

        return getIntervener(caseData, representation).getIntervenerOrganisation();
    }

    private OrganisationPolicy resolveIntervenerOrganisationPolicy(FinremCaseData caseData,
                                                                   int intervenerIndex) {
        return getIntervener(caseData, intervenerIndex).getIntervenerOrganisation();
    }

    private IntervenerWrapper getIntervener(FinremCaseData caseData, Representation representation) {
        return caseData.getInterveners().get(representation.intervenerIndex() - 1);
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

    private Barrister getIntervenerBarrister(FinremCaseData caseData, Representation representation) {
        int index = representation.intervenerIndex();
        List<BarristerCollectionItem> items = caseData.getBarristerCollectionWrapper().getIntervenerBarristersByIndex(index);
        return emptyIfNull(items).stream().map(BarristerCollectionItem::getValue)
            .filter(b -> representation.userId().equals(b.getUserId()))
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
