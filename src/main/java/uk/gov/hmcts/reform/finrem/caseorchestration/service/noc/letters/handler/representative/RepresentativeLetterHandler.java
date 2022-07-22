package uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.letters.handler.representative;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.NoticeType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.documents.SolicitorNocDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.documents.generators.AbstractLetterDetailsGenerator;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.letters.handler.AbstractLetterHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.solicitors.CheckApplicantSolicitorIsDigitalService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.solicitors.CheckRespondentSolicitorIsDigitalService;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.RepresentationUpdate;

import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.SOLICITOR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_EMAIL;

@Slf4j
public class RepresentativeLetterHandler extends AbstractLetterHandler {

    private final CaseDataService caseDataService;
    private final CheckApplicantSolicitorIsDigitalService checkApplicantSolicitorIsDigitalService;
    private final CheckRespondentSolicitorIsDigitalService checkRespondentSolicitorIsDigitalService;

    public RepresentativeLetterHandler(
        AbstractLetterDetailsGenerator noticeOfChangeLetterDetailsGenerator,
        SolicitorNocDocumentService solicitorNocDocumentService,
        BulkPrintService bulkPrintService,
        CaseDataService caseDataService, NoticeType noticeType,
        CheckApplicantSolicitorIsDigitalService checkApplicantSolicitorIsDigitalService,
        CheckRespondentSolicitorIsDigitalService checkRespondentSolicitorIsDigitalService) {
        super(noticeOfChangeLetterDetailsGenerator, solicitorNocDocumentService, bulkPrintService, noticeType, SOLICITOR);
        this.caseDataService = caseDataService;
        this.checkApplicantSolicitorIsDigitalService = checkApplicantSolicitorIsDigitalService;
        this.checkRespondentSolicitorIsDigitalService = checkRespondentSolicitorIsDigitalService;
    }

    @Override
    protected boolean shouldALetterBeSent(RepresentationUpdate representationUpdate,
                                          FinremCaseDetails caseDetailsToUse,
                                          FinremCaseDetails otherCaseDetails) {
        log.info("Now check if solicitor notification letter is required");
        boolean isConsentedApplication = caseDetailsToUse.getCaseData().isConsentedApplication();
        log.info("Check that the solicitor email address is populated for is consented {}", isConsentedApplication);
        return (isApplicantChangeOfRepresentativeWithoutSolicitorEmail(representationUpdate, caseDetailsToUse,
            isConsentedApplication, otherCaseDetails)
            || isRespondentChangeOfRepresentativeWithoutSolcitorEmail(representationUpdate, caseDetailsToUse,
            otherCaseDetails));
    }

    private boolean isApplicantChangeOfRepresentativeWithoutSolicitorEmail(RepresentationUpdate representationUpdate,
                                                                           FinremCaseDetails caseDetailsToUse,
                                                                           boolean isConsentedApplication,
                                                                           FinremCaseDetails otherCaseDetails) {
        return (representationUpdate.getParty().equalsIgnoreCase(COR_APPLICANT))
            && (isConsentedAndSolicitorNonDigitalOrWithoutEmail(representationUpdate,
            caseDetailsToUse, otherCaseDetails, isConsentedApplication)
            || isContestedAndSolicitorNonDigitalOrWithoutEmail(representationUpdate,
            caseDetailsToUse, otherCaseDetails, isConsentedApplication));
    }

    private boolean isRespondentChangeOfRepresentativeWithoutSolcitorEmail(RepresentationUpdate representationUpdate,
                                                                           FinremCaseDetails caseDetailsToUse,
                                                                           FinremCaseDetails otherCaseDetails) {
        return representationUpdate.getParty().equalsIgnoreCase(COR_RESPONDENT)
            && (!isCaseFieldPopulated(caseDetailsToUse.getCaseData().getContactDetailsWrapper().getRespondentSolicitorEmail())
            || !isSolicitorDigital(representationUpdate, otherCaseDetails));
    }

    private boolean isConsentedAndSolicitorNonDigitalOrWithoutEmail(RepresentationUpdate representationUpdate,
                                                                    FinremCaseDetails caseDetailsToUse,
                                                                    FinremCaseDetails otherCaseDetails,
                                                                    boolean isConsentedApplication) {
        return isConsentedApplication
            && (!isCaseFieldPopulated(caseDetailsToUse.getCaseData().getApplicantSolicitorEmail())
            || !isSolicitorDigital(representationUpdate, otherCaseDetails));
    }

    private boolean isContestedAndSolicitorNonDigitalOrWithoutEmail(RepresentationUpdate representationUpdate,
                                                                    FinremCaseDetails caseDetailsToUse,
                                                                    FinremCaseDetails otherCaseDetails,
                                                                    boolean isConsentedApplication) {
        return !isConsentedApplication
            && (!isCaseFieldPopulated(caseDetailsToUse.getCaseData().getApplicantSolicitorEmail())
            || !isSolicitorDigital(representationUpdate, otherCaseDetails));
    }

    private boolean isSolicitorDigital(RepresentationUpdate representationUpdate, FinremCaseDetails otherCaseDetails) {
        return representationUpdate.getParty().equalsIgnoreCase(COR_APPLICANT)
            ? checkApplicantSolicitorIsDigitalService.isSolicitorDigital(otherCaseDetails)
            : checkRespondentSolicitorIsDigitalService.isSolicitorDigital(otherCaseDetails);
    }

}
