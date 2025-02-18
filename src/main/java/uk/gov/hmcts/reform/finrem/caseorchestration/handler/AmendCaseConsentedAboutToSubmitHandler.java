package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NatureApplication;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Slf4j
@Service
public class AmendCaseConsentedAboutToSubmitHandler extends FinremCallbackHandler {


    @Autowired
    public AmendCaseConsentedAboutToSubmitHandler(FinremCaseDetailsMapper mapper) {
        super(mapper);
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && CaseType.CONSENTED.equals(caseType)
            && (EventType.AMEND_CASE.equals(eventType));
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        FinremCaseData caseData = callbackRequest.getCaseDetails().getData();
        updatePeriodicPaymentData(caseData);
        updatePropertyDetails(caseData);

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().data(caseData).build();
    }

    private void updatePeriodicPaymentData(FinremCaseData caseData) {
        List<NatureApplication> natureOfApplication2 =
            Optional.ofNullable(caseData.getNatureApplicationWrapper().getNatureOfApplication2()).orElse(new ArrayList<>());

        if (!natureOfApplication2.contains(NatureApplication.CONSENTED_PERIODICAL_PAYMENT_ORDER)) {
            removePeriodicPaymentData(caseData);
        } else {
            // if written agreement for order for children
            if (YesOrNo.YES.equals(caseData.getNatureApplicationWrapper().getNatureOfApplication5())) {
                caseData.getNatureApplicationWrapper().setNatureOfApplication6(null);
                caseData.getNatureApplicationWrapper().setNatureOfApplication7(null);
            }
        }
    }

    private void updatePropertyDetails(FinremCaseData caseData) {
        List<NatureApplication> natureOfApplication2 =
            Optional.ofNullable(caseData.getNatureApplicationWrapper().getNatureOfApplication2()).orElse(new ArrayList<>());

        if (!natureOfApplication2.contains(NatureApplication.CONSENTED_PROPERTY_ADJUSTMENT_ORDER)) {
            removePropertyAdjustmentDetails(caseData);
        }
    }

    private void removePeriodicPaymentData(FinremCaseData caseData) {
        caseData.getNatureApplicationWrapper().setNatureOfApplication5(null);
        caseData.getNatureApplicationWrapper().setNatureOfApplication6(null);
        caseData.getNatureApplicationWrapper().setNatureOfApplication7(null);
        caseData.getNatureApplicationWrapper().setOrderForChildrenQuestion1(null);
    }

    private void removePropertyAdjustmentDetails(FinremCaseData caseData) {
        caseData.getNatureApplicationWrapper().setNatureOfApplication3a(null);
        caseData.getNatureApplicationWrapper().setNatureOfApplication3b(null);
    }
}
