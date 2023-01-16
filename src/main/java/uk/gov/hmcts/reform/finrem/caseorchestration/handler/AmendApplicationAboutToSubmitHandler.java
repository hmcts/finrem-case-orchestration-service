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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NatureApplication;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.StageReached;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Slf4j
@Service
public class AmendApplicationAboutToSubmitHandler extends FinremCallbackHandler {

    private final ConsentOrderService consentOrderService;

    @Autowired
    public AmendApplicationAboutToSubmitHandler(FinremCaseDetailsMapper mapper,
                                                ConsentOrderService consentOrderService) {
        super(mapper);
        this.consentOrderService = consentOrderService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && CaseType.CONSENTED.equals(caseType)
            && (EventType.AMEND_APP_DETAILS.equals(eventType));
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        log.info("Received request to update consented case with Case ID: {}", caseDetails.getId());

        FinremCaseData caseData = caseDetails.getData();

        updateDivorceDetails(caseData);
        updatePeriodicPaymentData(caseData);
        updatePropertyDetails(caseData);
        updateRespondentSolicitorAddress(caseData);
        updateD81Details(caseData);
        updateApplicantOrSolicitorContactDetails(caseData);
        updateLatestConsentOrder(callbackRequest);

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().data(caseData).build();
    }

    private void updateLatestConsentOrder(FinremCallbackRequest callbackRequest) {
        FinremCaseData caseData = callbackRequest.getCaseDetails().getData();
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


    private void removeApplicantSolicitorAddress(FinremCaseData caseData) {
        caseData.getContactDetailsWrapper().setSolicitorReference(null);
        caseData.getContactDetailsWrapper().setSolicitorName(null);
        caseData.getContactDetailsWrapper().setSolicitorFirm(null);
        caseData.getContactDetailsWrapper().setSolicitorAddress(null);
        caseData.getContactDetailsWrapper().setSolicitorPhone(null);
        caseData.getContactDetailsWrapper().setSolicitorEmail(null);
        caseData.getContactDetailsWrapper().setSolicitorAgreeToReceiveEmails(null);
        caseData.getContactDetailsWrapper().setSolicitorDxNumber(null);

    }

    private void removeRespondentSolicitorAddress(FinremCaseData caseData) {
        caseData.getContactDetailsWrapper().setRespondentSolicitorName(null);
        caseData.getContactDetailsWrapper().setRespondentSolicitorFirm(null);
        caseData.getContactDetailsWrapper().setRespondentSolicitorReference(null);
        caseData.getContactDetailsWrapper().setRespondentSolicitorAddress(null);
        caseData.getContactDetailsWrapper().setRespondentSolicitorPhone(null);
        caseData.getContactDetailsWrapper().setRespondentSolicitorEmail(null);
        caseData.getContactDetailsWrapper().setRespondentSolicitorDxNumber(null);
        caseData.setRespSolNotificationsEmailConsent(null);
    }
}
