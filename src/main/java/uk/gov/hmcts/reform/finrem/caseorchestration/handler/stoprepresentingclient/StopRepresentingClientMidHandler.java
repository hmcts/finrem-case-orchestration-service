package uk.gov.hmcts.reform.finrem.caseorchestration.handler.stoprepresentingclient;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.CallbackHandlerLogger;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Address;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.StopRepresentationWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.stoprepresentingclient.RepresentativeInContext;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.stoprepresentingclient.StopRepresentingClientService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType.MID_EVENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.STOP_REPRESENTING_CLIENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;

@Slf4j
@Service
public class StopRepresentingClientMidHandler extends FinremCallbackHandler {

    private static final String ERROR_MESSAGE = "You cannot stop representing your client without either client consent or judicial approval. "
        + "You will need to make a general application to apply to come off record using the next step event 'general application";

    private static final String POSTCODE_FIELD_IS_REQUIRED = "%s's postcode field is required";

    private final StopRepresentingClientService stopRepresentingClientService;

    public StopRepresentingClientMidHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                            StopRepresentingClientService stopRepresentingClientService) {
        super(finremCaseDetailsMapper);
        this.stopRepresentingClientService = stopRepresentingClientService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return MID_EVENT.equals(callbackType)
            && Arrays.asList(CONTESTED, CONSENTED).contains(caseType)
            && STOP_REPRESENTING_CLIENT.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        log.info(CallbackHandlerLogger.midEvent(callbackRequest));

        FinremCaseData finremCaseData = callbackRequest.getCaseDetails().getData();

        List<String> errors = new ArrayList<>(calculatePostcodeMissingErrors(finremCaseData, userAuthorisation));

        if (isNotHavingClientConsent(finremCaseData) && isNotHavingJudicialApproval(finremCaseData)) {
            errors.add(ERROR_MESSAGE);
        }

        return response(finremCaseData, null, errors);
    }

    private boolean isNotHavingClientConsent(FinremCaseData finremCaseData) {
        return !YesOrNo.isYes(finremCaseData.getStopRepresentationWrapper().getStopRepClientConsent());
    }

    private boolean isNotHavingJudicialApproval(FinremCaseData finremCaseData) {
        return !YesOrNo.isYes(finremCaseData.getStopRepresentationWrapper().getStopRepJudicialApproval());
    }

    private List<String> calculatePostcodeMissingErrors(FinremCaseData finremCaseData, String userAuthorisation) {
        RepresentativeInContext representativeInContext = stopRepresentingClientService.buildRepresentation(finremCaseData,
            userAuthorisation);

        StopRepresentationWrapper wrapper = finremCaseData.getStopRepresentationWrapper();
        List<String> errors = new ArrayList<>();
        if (isPostcodeMissingInMasterServiceAddress(wrapper)) {
            errors.add(POSTCODE_FIELD_IS_REQUIRED.formatted(resolveMasterParty(representativeInContext)
                .orElseThrow()));
        }
        check(isPostcodeMissingInExtraAddress1(wrapper), wrapper.getExtraClientAddr1Id(), errors);
        check(isPostcodeMissingInExtraAddress2(wrapper), wrapper.getExtraClientAddr2Id(), errors);
        check(isPostcodeMissingInExtraAddress3(wrapper), wrapper.getExtraClientAddr3Id(), errors);
        check(isPostcodeMissingInExtraAddress4(wrapper), wrapper.getExtraClientAddr4Id(), errors);

        return errors;
    }

    private void check(boolean condition, String id, List<String> errors) {
        if (condition) {
            ExtraAddrType.describe(id)
                .ifPresent(desc -> errors.add(POSTCODE_FIELD_IS_REQUIRED.formatted(desc)));
        }
    }

    private boolean isPostcodeMissingInMasterServiceAddress(StopRepresentationWrapper wrapper) {
        return isPostcodeMissing(wrapper.getClientAddressForService());
    }

    private boolean isPostcodeMissingInExtraAddress1(StopRepresentationWrapper wrapper) {
        return isExtraFieldPostcodeMissing(wrapper.getExtraClientAddr1Id(), wrapper.getExtraClientAddr1());
    }

    private boolean isPostcodeMissingInExtraAddress2(StopRepresentationWrapper wrapper) {
        return isExtraFieldPostcodeMissing(wrapper.getExtraClientAddr2Id(), wrapper.getExtraClientAddr2());
    }

    private boolean isPostcodeMissingInExtraAddress3(StopRepresentationWrapper wrapper) {
        return isExtraFieldPostcodeMissing(wrapper.getExtraClientAddr3Id(), wrapper.getExtraClientAddr3());
    }

    private boolean isPostcodeMissingInExtraAddress4(StopRepresentationWrapper wrapper) {
        return isExtraFieldPostcodeMissing(wrapper.getExtraClientAddr4Id(), wrapper.getExtraClientAddr4());
    }

    private boolean isPostcodeMissing(Address address) {
        String postcode = ofNullable(address).map(Address::getPostCode).orElse("");
        return StringUtils.isBlank(postcode);
    }

    private boolean isExtraFieldPostcodeMissing(String id, Address address) {
        if (StringUtils.isBlank(id)) {
            return false;
        }
        return isPostcodeMissing(address);
    }

    private Optional<String> resolveMasterParty(RepresentativeInContext representativeInContext) {
        if (representativeInContext.isApplicantRepresentative()) {
            return Optional.of("Applicant");
        }
        if (representativeInContext.isRespondentRepresentative()) {
            return Optional.of("Respondent");
        }
        if (representativeInContext.isIntervenerRepresentative()) {
            return Optional.of("Intervener %s".formatted(
                Optional.ofNullable(representativeInContext.intervenerIndex()).orElseThrow()
            ));
        }
        return Optional.empty();
    }
}
