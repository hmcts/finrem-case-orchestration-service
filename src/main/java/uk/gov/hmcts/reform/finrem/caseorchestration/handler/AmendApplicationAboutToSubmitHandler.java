package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderService;
import uk.gov.hmcts.reform.finrem.ccd.callback.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.ccd.callback.CallbackRequest;
import uk.gov.hmcts.reform.finrem.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.ccd.domain.CaseType;
import uk.gov.hmcts.reform.finrem.ccd.domain.EventType;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.NatureApplication;
import uk.gov.hmcts.reform.finrem.ccd.domain.StageReached;
import uk.gov.hmcts.reform.finrem.ccd.domain.YesOrNo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.NoCSolicitorDetailsHelper.removeApplicantSolicitorAddress;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.NoCSolicitorDetailsHelper.removeRespondentSolicitorAddress;

@Slf4j
@Service
@RequiredArgsConstructor
public class AmendApplicationAboutToSubmitHandler implements CallbackHandler {

    @Autowired
    private ConsentOrderService consentOrderService;

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && CaseType.CONSENTED.equals(caseType)
            && (EventType.AMEND_APPLICATION_DETAILS.equals(eventType));
    }

    @Override
    public AboutToStartOrSubmitCallbackResponse handle(CallbackRequest callbackRequest,
                                                       String userAuthorisation) {
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        log.info("Received request to update consented case with Case ID: {}", caseDetails.getId());

        FinremCaseData caseData = caseDetails.getCaseData();

        updateDivorceDetails(caseData);
        updatePeriodicPaymentData(caseData);
        updatePropertyDetails(caseData);
        updateRespondentSolicitorAddress(caseData);
        updateD81Details(caseData);
        updateApplicantOrSolicitorContactDetails(caseData);
        updateLatestConsentOrder(callbackRequest);

        return AboutToStartOrSubmitCallbackResponse.builder().data(caseData).build();
    }

    private void updateLatestConsentOrder(CallbackRequest callbackRequest) {
        FinremCaseData caseData = callbackRequest.getCaseDetails().getCaseData();
        caseData.setLatestConsentOrder(consentOrderService.getLatestConsentOrderData(callbackRequest));
    }

    private void updateDivorceDetails(FinremCaseData caseData) {
        if (StageReached.DECREE_NISI.equals(caseData.getDivorceStageReached())) {
            caseData.setDivorceUploadEvidence2(null);
            caseData.setDivorceDecreeAbsoluteDate(null);
        } else {
            caseData.setDivorceUploadEvidence1(null);
            caseData.setDivorceDecreeNisiDate(null);
        }
    }

    private void updatePeriodicPaymentData(FinremCaseData caseData) {
        List<NatureApplication> natureOfApplication2 =
            Optional.ofNullable(caseData.getNatureApplicationWrapper().getNatureOfApplication2()).orElse(new ArrayList<>());

        if (!natureOfApplication2.contains(NatureApplication.PERIODICAL_PAYMENT_ORDER)) {
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

        if (!natureOfApplication2.contains(NatureApplication.PROPERTY_ADJUSTMENT_ORDER)) {
            removePropertyAdjustmentDetails(caseData);
        }
    }

    private void updateD81Details(FinremCaseData caseData) {
        if (YesOrNo.YES.equals(caseData.getD81Question())) {
            caseData.setD81Applicant(null);
            caseData.setD81Respondent(null);
        } else {
            caseData.setD81Joint(null);
        }
    }

    private void updateRespondentSolicitorAddress(FinremCaseData caseData) {
        if (YesOrNo.NO.equals(caseData.getContactDetailsWrapper().getConsentedRespondentRepresented())) {
            removeRespondentSolicitorAddress(caseData);
        } else {
            removeRespondentAddress(caseData);
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


    private void updateApplicantOrSolicitorContactDetails(FinremCaseData caseData) {
        if (YesOrNo.NO.equals(caseData.getContactDetailsWrapper().getApplicantRepresented())) {
            removeApplicantSolicitorAddress(caseData);
        } else {
            removeApplicantAddress(caseData);
        }
    }


    private void removeApplicantAddress(FinremCaseData caseData) {
        caseData.getContactDetailsWrapper().setApplicantAddress(null);
        caseData.getContactDetailsWrapper().setApplicantPhone(null);
        caseData.getContactDetailsWrapper().setApplicantEmail(null);
    }

    private void removeRespondentAddress(FinremCaseData caseData) {
        caseData.getContactDetailsWrapper().setRespondentAddress(null);
        caseData.getContactDetailsWrapper().setRespondentPhone(null);
        caseData.getContactDetailsWrapper().setRespondentEmail(null);
    }
}
