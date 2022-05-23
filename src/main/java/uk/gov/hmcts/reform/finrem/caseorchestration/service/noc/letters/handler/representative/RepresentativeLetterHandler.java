package uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.letters.handler.representative;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RepresentationUpdate;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.NoticeType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.documents.SolicitorNocDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.documents.generators.NocLetterDetailsGenerator;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.letters.handler.AbstractLetterHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.solicitors.CheckApplicantSolicitorIsDigitalService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.solicitors.CheckRespondentSolicitorIsDigitalService;

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
        NocLetterDetailsGenerator noticeOfChangeLetterDetailsGenerator,
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
    protected boolean shouldALetterBeSent(RepresentationUpdate representationUpdate, CaseDetails caseDetailsToUse) {
        log.info("Now check if solicitor notification letter is required");
        boolean isConsentedApplication = caseDataService.isConsentedApplication(caseDetailsToUse);
        log.info("Check that the solicitor email address is populated for is consented {}", isConsentedApplication);
        return (isApplicantChangeOfRepresentativeWithoutSolicitorEmail(representationUpdate, caseDetailsToUse, isConsentedApplication)
            || isRespondentChangeOfRepresentativeWithoutSolcitorEmail(representationUpdate, caseDetailsToUse));
    }

    private boolean isApplicantChangeOfRepresentativeWithoutSolicitorEmail(RepresentationUpdate representationUpdate, CaseDetails caseDetailsToUse,
                                                                           boolean isConsentedApplication) {
        return (representationUpdate.getParty().equals(COR_APPLICANT))
            && (isConsentedApplication && !isCaseFieldPopulated(caseDetailsToUse, SOLICITOR_EMAIL)
                || (!isConsentedApplication && !isCaseFieldPopulated(caseDetailsToUse, CONTESTED_SOLICITOR_EMAIL))
                || (!isSolicitorDigital(caseDetailsToUse, representationUpdate)));
    }

    private boolean isRespondentChangeOfRepresentativeWithoutSolcitorEmail(RepresentationUpdate representationUpdate, CaseDetails caseDetailsToUse) {
        return representationUpdate.getParty().equals(COR_RESPONDENT)
            && (!isCaseFieldPopulated(caseDetailsToUse, RESP_SOLICITOR_EMAIL)
            || !isSolicitorDigital(caseDetailsToUse, representationUpdate));
    }

    private boolean isSolicitorDigital(CaseDetails caseDetails, RepresentationUpdate representationUpdate) {
        return representationUpdate.getParty().equals(COR_APPLICANT)
            ? checkApplicantSolicitorIsDigitalService.isSolicitorDigital(caseDetails)
            : checkRespondentSolicitorIsDigitalService.isSolicitorDigital(caseDetails);
    }
}
