package uk.gov.hmcts.reform.finrem.caseorchestration.handler.amendapplicationdetails;

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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NatureApplication;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NatureOfApplicationSchedule;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Schedule1OrMatrimonialAndCpList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.StageReached;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.MiamWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseFlagsService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.OnlineFormDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.express.ExpressCaseService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.refuge.RefugeWrapperUtils;

import java.util.List;

import static java.util.Optional.ofNullable;
import static org.apache.commons.collections4.ListUtils.emptyIfNull;
import static uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.AMEND_CONTESTED_APP_DETAILS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.AMEND_CONTESTED_PAPER_APP_DETAILS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Schedule1OrMatrimonialAndCpList.MATRIMONIAL_AND_CIVIL_PARTNERSHIP_PROCEEDINGS;

@Slf4j
@Service
public class AmendApplicationDetailsAboutToSubmitHandler extends FinremCallbackHandler {

    private final OnlineFormDocumentService onlineFormDocumentService;

    private final CaseFlagsService caseFlagsService;

    private final FeatureToggleService featureToggleService;

    private final ExpressCaseService expressCaseService;

    public AmendApplicationDetailsAboutToSubmitHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                       OnlineFormDocumentService onlineFormDocumentService,
                                                       CaseFlagsService caseFlagsService,
                                                       FeatureToggleService featureToggleService,
                                                       ExpressCaseService expressCaseService) {
        super(finremCaseDetailsMapper);
        this.onlineFormDocumentService = onlineFormDocumentService;
        this.caseFlagsService = caseFlagsService;
        this.featureToggleService = featureToggleService;
        this.expressCaseService = expressCaseService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return ABOUT_TO_SUBMIT.equals(callbackType) && CONTESTED.equals(caseType)
            && List.of(AMEND_CONTESTED_PAPER_APP_DETAILS, AMEND_CONTESTED_APP_DETAILS).contains(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        log.info(CallbackHandlerLogger.aboutToSubmit(callbackRequest));

        validateCaseData(callbackRequest);

        FinremCaseDetails finremCaseDetails = callbackRequest.getCaseDetails();
        FinremCaseData finremCaseData = finremCaseDetails.getData();

        caseFlagsService.setCaseFlagInformation(finremCaseDetails);
        clearUnwantedDivorceDetailsFields(finremCaseData);
        clearUnwantedRespondentDetails(finremCaseData);
        updatePeriodicPaymentOrder(finremCaseData);
        clearPropertyAdjustmentOrderRelatedFields(finremCaseData);
        updateFastTrackProcedureDetail(finremCaseData);
        updateComplexityDetails(finremCaseData);
        clearReasonForLocalCourt(finremCaseData);
        clearAllocatedToBeHeardAtHighCourtJudgeLevelText(finremCaseData);
        updateMiamDetails(finremCaseData);
        cleanupAdditionalDocuments(finremCaseData);
        generateMiniFormA(finremCaseDetails, userAuthorisation);

        RefugeWrapperUtils.updateApplicantInRefugeTab(finremCaseDetails);
        RefugeWrapperUtils.updateRespondentInRefugeTab(finremCaseDetails);

        if (featureToggleService.isExpressPilotEnabled()) {
            expressCaseService.setExpressCaseEnrollmentStatus(finremCaseDetails.getData());
        }

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(finremCaseData).build();
    }

    private void generateMiniFormA(FinremCaseDetails finremCaseDetails, String userAuthorisation) {
        finremCaseDetails.getData()
            .setMiniFormA(onlineFormDocumentService.generateDraftContestedMiniFormA(userAuthorisation, finremCaseDetails));
    }

    private Schedule1OrMatrimonialAndCpList getTypeOfApplication(FinremCaseData finremCaseData) {
        return ofNullable(finremCaseData.getScheduleOneWrapper().getTypeOfApplication())
            .orElse(MATRIMONIAL_AND_CIVIL_PARTNERSHIP_PROCEEDINGS);
    }

    private boolean isInConnectionToMatrimonialAndCivilPartnershipProceedings(FinremCaseData finremCaseData) {
        return MATRIMONIAL_AND_CIVIL_PARTNERSHIP_PROCEEDINGS.equals(getTypeOfApplication(finremCaseData));
    }

    private boolean isPropertyAdjustmentOrderNotSelected(FinremCaseData finremCaseData) {
        return !emptyIfNull(finremCaseData.getNatureApplicationWrapper().getNatureOfApplicationChecklist())
            .contains(NatureApplication.PROPERTY_ADJUSTMENT_ORDER);
    }

    private void clearAllocatedToBeHeardAtHighCourtJudgeLevelText(FinremCaseData finremCaseData) {
        if (YesOrNo.NO.equals(finremCaseData.getAllocatedToBeHeardAtHighCourtJudgeLevel())) {
            finremCaseData.setAllocatedToBeHeardAtHighCourtJudgeLevelText(null);
        }
    }

    private void clearReasonForLocalCourt(FinremCaseData finremCaseData) {
        if (YesOrNo.NO.equals(finremCaseData.getIsApplicantsHomeCourt())) {
            finremCaseData.setReasonForLocalCourt(null);
        }
    }

    private void removePeriodicalPaymentOrderDetails(FinremCaseData finremCaseData) {
        finremCaseData.setPaymentForChildrenDecision(null);
        removeBenefitsDetails(finremCaseData);
    }

    private void removeBenefitsDetails(FinremCaseData finremCaseData) {
        if (isInConnectionToMatrimonialAndCivilPartnershipProceedings(finremCaseData)) {
            finremCaseData.setBenefitForChildrenDecision(null);
            finremCaseData.setBenefitPaymentChecklist(null);
        } else {
            finremCaseData.setBenefitForChildrenDecisionSchedule(null);
            finremCaseData.setBenefitPaymentChecklistSchedule(null);
        }
    }

    private void removeBenefitPaymentChecklist(FinremCaseData finremCaseData) {
        if (isInConnectionToMatrimonialAndCivilPartnershipProceedings(finremCaseData)) {
            finremCaseData.setBenefitPaymentChecklist(null);
        } else {
            finremCaseData.setBenefitPaymentChecklistSchedule(null);
        }
    }

    private void removeAllMiamExceptionDetails(FinremCaseData finremCaseData) {
        MiamWrapper miamWrapper = finremCaseData.getMiamWrapper();
        miamWrapper.setClaimingExemptionMiam(null);
        miamWrapper.setFamilyMediatorMiam(null);
        miamWrapper.setMiamExemptionsChecklist(null);
        miamWrapper.setMiamDomesticViolenceChecklist(null);
        miamWrapper.setMiamUrgencyReasonChecklist(null);
        miamWrapper.setMiamPreviousAttendanceChecklist(null);
        miamWrapper.setMiamOtherGroundsChecklist(null);
        miamWrapper.setEvidenceUnavailableDomesticAbuseMiam(null);
        miamWrapper.setEvidenceUnavailableUrgencyMiam(null);
        miamWrapper.setEvidenceUnavailablePreviousAttendanceMiam(null);
        miamWrapper.setEvidenceUnavailableOtherGroundsMiam(null);
        miamWrapper.setAdditionalInfoOtherGroundsMiam(null);
    }

    private void clearUnwantedRespondentDetails(FinremCaseData caseData) {
        if (caseData.isRespondentRepresentedByASolicitor()) {
            caseData.getContactDetailsWrapper().setRespondentSolicitorName(null);
            caseData.getContactDetailsWrapper().setRespondentSolicitorFirm(null);
            caseData.getContactDetailsWrapper().setRespondentSolicitorReference(null);
            caseData.getContactDetailsWrapper().setRespondentSolicitorAddress(null);
            caseData.getContactDetailsWrapper().setRespondentSolicitorPhone(null);
            caseData.getContactDetailsWrapper().setRespondentSolicitorEmail(null);
            caseData.getContactDetailsWrapper().setRespondentSolicitorDxNumber(null);
            caseData.setRespSolNotificationsEmailConsent(null);
        } else {
            caseData.getContactDetailsWrapper().setRespondentAddress(null);
            caseData.getContactDetailsWrapper().setRespondentPhone(null);
            caseData.getContactDetailsWrapper().setRespondentEmail(null);
        }
    }

    private void updatePeriodicPaymentOrder(FinremCaseData finremCaseData) {
        boolean isPeriodicalPaymentOrderNotSelected = isInConnectionToMatrimonialAndCivilPartnershipProceedings(finremCaseData)
            ? !emptyIfNull(finremCaseData.getNatureApplicationWrapper().getNatureOfApplicationChecklist())
            .contains(NatureApplication.PERIODICAL_PAYMENT_ORDER)
            : !emptyIfNull(finremCaseData.getScheduleOneWrapper().getNatureOfApplicationChecklistSchedule())
            .contains(NatureOfApplicationSchedule.PERIODICAL_PAYMENT_ORDER);

        if (isPeriodicalPaymentOrderNotSelected) {
            removePeriodicalPaymentOrderDetails(finremCaseData);
        } else {
            updatePeriodicPaymentDetails(finremCaseData);
        }
    }

    private void updatePeriodicPaymentDetails(FinremCaseData finremCaseData) {
        YesOrNo paymentForChildrenDecision = finremCaseData.getPaymentForChildrenDecision();
        YesOrNo benefitForChildrenDecision = finremCaseData.getBenefitForChildrenDecision();

        if (YesOrNo.NO.equals(paymentForChildrenDecision)) {
            removeBenefitsDetails(finremCaseData);
        } else if (YesOrNo.YES.equals(paymentForChildrenDecision)
            && YesOrNo.YES.equals(benefitForChildrenDecision)) {
            removeBenefitPaymentChecklist(finremCaseData);
        }
    }

    private void clearUnwantedDivorceDetailsFields(FinremCaseData caseData) {
        StageReached divorceStageReached = caseData.getDivorceStageReached();

        switch (divorceStageReached) {
            case DECREE_NISI -> {
                caseData.setDivorceUploadEvidence2(null);
                caseData.setDivorceDecreeAbsoluteDate(null);
                caseData.setDivorceUploadPetition(null);
            }
            case DECREE_ABSOLUTE -> {
                caseData.setDivorceUploadEvidence1(null);
                caseData.setDivorceDecreeNisiDate(null);
                caseData.setDivorceUploadPetition(null);
            }
            default -> {
                caseData.setDivorceUploadEvidence1(null);
                caseData.setDivorceDecreeNisiDate(null);
                caseData.setDivorceUploadEvidence2(null);
                caseData.setDivorceDecreeAbsoluteDate(null);
            }
        }
    }

    private void clearPropertyAdjustmentOrderRelatedFields(FinremCaseData finremCaseData) {
        if (isInConnectionToMatrimonialAndCivilPartnershipProceedings(finremCaseData)) {
            if (isPropertyAdjustmentOrderNotSelected(finremCaseData)) {
                clearPropertyAdjustmentOrderFields(finremCaseData);
            } else {
                clearPropertyAdjustmentOrderDetail(finremCaseData);
            }
        }
    }

    private void clearPropertyAdjustmentOrderFields(FinremCaseData finremCaseData) {
        finremCaseData.setPropertyAddress(null);
        finremCaseData.setMortgageDetail(null);
        finremCaseData.setPropertyAdjustmentOrderDetail(null);
    }

    private void clearPropertyAdjustmentOrderDetail(FinremCaseData finremCaseData) {
        if (YesOrNo.NO.equals(finremCaseData.getAdditionalPropertyOrderDecision())) {
            finremCaseData.setPropertyAdjustmentOrderDetail(null);
        }
    }

    private void updateFastTrackProcedureDetail(FinremCaseData finremCaseData) {
        if (YesOrNo.NO.equals(finremCaseData.getFastTrackDecision())) {
            finremCaseData.setFastTrackDecision(null);
        }
    }

    private void updateComplexityDetails(FinremCaseData finremCaseData) {
        if (YesOrNo.NO.equals(finremCaseData.getOtherReasonForComplexity())) {
            finremCaseData.setOtherReasonForComplexityText(null);
        }
    }

    private void removeMiamCertificationDetailsForApplicantAttendedMiam(FinremCaseData finremCaseData) {
        finremCaseData.setSoleTraderName1(null);
        finremCaseData.setFamilyMediatorServiceName1(null);
        finremCaseData.setMediatorRegistrationNumber1(null);
    }

    private void removeMiamCertificationDetails(FinremCaseData finremCaseData) {
        removeMiamCertificationDetailsForApplicantAttendedMiam(finremCaseData);

        finremCaseData.setSoleTraderName(null);
        finremCaseData.setFamilyMediatorServiceName(null);
        finremCaseData.setMediatorRegistrationNumber(null);
        finremCaseData.setUploadMediatorDocument(null);
    }

    private void removeLegacyExemptions(FinremCaseData finremCaseData) {
        MiamWrapper miamWrapper = finremCaseData.getMiamWrapper();
        miamWrapper.setMiamPreviousAttendanceChecklist(null);
        miamWrapper.setMiamOtherGroundsChecklist(null);
    }

    private void updateMiamDetails(FinremCaseData finremCaseData) {
        finremCaseData.getMiamWrapper().setFamilyMediatorMiam(null);

        if (YesOrNo.YES.equals(finremCaseData.getMiamWrapper().getApplicantAttendedMiam())) {
            removeAllMiamExceptionDetails(finremCaseData);
            removeMiamCertificationDetailsForApplicantAttendedMiam(finremCaseData);
        } else {
            removeMiamCertificationDetails(finremCaseData);
        }
        removeLegacyExemptions(finremCaseData);
    }

    private void cleanupAdditionalDocuments(FinremCaseData finremCaseData) {
        if (YesOrNo.NO.equals(finremCaseData.getPromptForAnyDocument())) {
            finremCaseData.setUploadAdditionalDocument(null);
        }
    }
}
