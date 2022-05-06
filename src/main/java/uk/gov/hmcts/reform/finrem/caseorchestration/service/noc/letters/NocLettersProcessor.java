package uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.letters;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RepresentationUpdate;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.noc.NoticeOfChangeLetterDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.NoticeType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.documents.NocDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.documents.SolicitorNocDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.documents.generators.NocLetterDetailsGenerator;

import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.RESPONDENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.SOLICITOR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService.nullToEmpty;

@Slf4j
public abstract class NocLettersProcessor {

    private final NocDocumentService nocDocumentService;
    private final SolicitorNocDocumentService solicitorNocDocumentService;
    private final NocLetterDetailsGenerator noticeOfChangeLetterDetailsGenerator;
    private final CaseDataService caseDataService;
    private final NoticeType noticeType;
    private final BulkPrintService bulkPrintService;

    public static final String COR_APPLICANT = "Applicant";

    public NocLettersProcessor(
        NocDocumentService nocDocumentService,
        SolicitorNocDocumentService solicitorNocDocumentService,
        NocLetterDetailsGenerator noticeOfChangeLetterDetailsGenerator,
        CaseDataService caseDataService,
        BulkPrintService bulkPrintService,
        NoticeType noticeType) {
        this.nocDocumentService = nocDocumentService;
        this.solicitorNocDocumentService = solicitorNocDocumentService;
        this.noticeOfChangeLetterDetailsGenerator = noticeOfChangeLetterDetailsGenerator;
        this.caseDataService = caseDataService;
        this.bulkPrintService = bulkPrintService;
        this.noticeType = noticeType;
    }

    public void processSolicitorAndLitigantLetters(CaseDetails caseDetails, String authToken, RepresentationUpdate representationUpdate) {

        log.info("In the processSolicitorAndLitigantLetters method for case {} and noticeType {}", caseDetails.getId(), noticeType);
        sendLitigantLetter(caseDetails, authToken, representationUpdate);
        sendSolicitorLetter(caseDetails, authToken, representationUpdate);
    }


    private void sendLitigantLetter(CaseDetails caseDetails, String authToken, RepresentationUpdate representationUpdate) {
        boolean isApplicantCheck = isApplicant(representationUpdate);
        NoticeOfChangeLetterDetails noticeOfChangeLetterDetailsLitigant = null;
        if (isApplicantCheck && isEmailAddressNotProvided(caseDetails, APPLICANT_EMAIL)) {
            log.info("The litigant is an applicant and the email address is not provided");
            noticeOfChangeLetterDetailsLitigant =
                noticeOfChangeLetterDetailsGenerator.generate(caseDetails, representationUpdate, APPLICANT,
                    noticeType);
        } else if (!isApplicantCheck && isEmailAddressNotProvided(caseDetails, RESPONDENT_EMAIL)) {
            log.info("The litigant is a respondent and the email address is not provided");
            noticeOfChangeLetterDetailsLitigant =
                noticeOfChangeLetterDetailsGenerator.generate(caseDetails, representationUpdate, RESPONDENT,
                    noticeType);
        }
        if (noticeOfChangeLetterDetailsLitigant != null) {
            log.info("Letter is required so generate");
            CaseDocument caseDocument = nocDocumentService.generateNoticeOfChangeLetter(authToken, noticeOfChangeLetterDetailsLitigant);
            log.info("Generated the litigant case document now send to bulk print");
            bulkPrintService.sendDocumentForPrint(caseDocument, caseDetails);
        }
    }

    private void sendSolicitorLetter(CaseDetails caseDetails, String authToken, RepresentationUpdate representationUpdate) {
        log.info("Now check if solicitor notification letter is required");
        boolean isConsentedApplication = caseDataService.isConsentedApplication(caseDetails);
        if (solicitorHasNotProvidedAnEmailAddress(caseDetails, isConsentedApplication)) {
            log.info("Solicitor has not provided an email address so send out letter for isConsented {}", isConsentedApplication);
            NoticeOfChangeLetterDetails noticeOfChangeLetterDetailsSolicitor =
                noticeOfChangeLetterDetailsGenerator.generate(caseDetails, representationUpdate, SOLICITOR,
                    noticeType);
            CaseDocument caseDocument = solicitorNocDocumentService.generateNoticeOfChangeLetter(authToken, noticeOfChangeLetterDetailsSolicitor);
            log.info("Generated the solicitor case document now send to bulk print");
            bulkPrintService.sendDocumentForPrint(caseDocument, caseDetails);
        }
    }

    private boolean isApplicant(RepresentationUpdate representationUpdate) {
        return representationUpdate.getParty().equals(COR_APPLICANT);
    }

    private boolean isEmailAddressNotProvided(CaseDetails caseDetails, String emailField) {
        return StringUtils.isEmpty(nullToEmpty(caseDetails.getData().get(emailField)));
    }

    private boolean solicitorHasNotProvidedAnEmailAddress(CaseDetails caseDetails, boolean isConsentedApplication) {
        return (isConsentedApplication && isEmailAddressNotProvided(caseDetails, SOLICITOR_EMAIL)
            || (!isConsentedApplication && isEmailAddressNotProvided(caseDetails, CONTESTED_SOLICITOR_EMAIL)));
    }
}
