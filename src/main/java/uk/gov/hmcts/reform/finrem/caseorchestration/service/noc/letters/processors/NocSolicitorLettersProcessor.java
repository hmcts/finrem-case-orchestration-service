package uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.letters.processors;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RepresentationUpdate;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.noc.NoticeOfChangeLetterDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.letters.generators.NocLetterDetailsGenerator;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.letters.generators.NocLetterGenerator;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.letters.generators.SolicitorNocLetterGenerator;

import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.RESPONDENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.SOLICITOR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService.nullToEmpty;

@Slf4j
public abstract class NocSolicitorLettersProcessor {

    private final NocLetterGenerator litigantNocLetterGenerator;
    private final SolicitorNocLetterGenerator solicitorNocLetterGenerator;
    private final NocLetterDetailsGenerator noticeOfChangeLetterDetailsGenerator;
    private final CaseDataService caseDataService;
    private final NocLetterDetailsGenerator.NoticeType noticeType;

    public static final String COR_APPLICANT = "applicant";

    public NocSolicitorLettersProcessor(
        NocLetterGenerator litigantNocLetterGenerator,
        SolicitorNocLetterGenerator solicitorNocLetterGenerator,
        NocLetterDetailsGenerator noticeOfChangeLetterDetailsGenerator,
        CaseDataService caseDataService,
        NocLetterDetailsGenerator.NoticeType noticeType) {
        this.litigantNocLetterGenerator = litigantNocLetterGenerator;
        this.solicitorNocLetterGenerator = solicitorNocLetterGenerator;
        this.noticeOfChangeLetterDetailsGenerator = noticeOfChangeLetterDetailsGenerator;
        this.caseDataService = caseDataService;
        this.noticeType = noticeType;
    }

    public void processSolicitorAndLitigantLetters(CaseDetails caseDetails, String authToken, RepresentationUpdate representationUpdate) {

        log.info("In the processSolicitorAndLitigantLetters method for case {} and noticeType {}", caseDetails.getId(), noticeType);
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
            litigantNocLetterGenerator.generateNoticeOfLetter(authToken, noticeOfChangeLetterDetailsLitigant);
        }

        log.info("Now check if solicitor notification letter is required");
        boolean isConsentedApplication = caseDataService.isConsentedApplication(caseDetails);
        if (solicitorHasNotProvidedAnEmailAddress(caseDetails, isConsentedApplication)) {
            log.info("Solicitor has not provided an email address so send out letter for isConsented {}", isConsentedApplication);
            NoticeOfChangeLetterDetails noticeOfChangeLetterDetailsSolicitor =
                noticeOfChangeLetterDetailsGenerator.generate(caseDetails, representationUpdate, SOLICITOR,
                    noticeType);
            solicitorNocLetterGenerator.generateNoticeOfLetter(authToken, noticeOfChangeLetterDetailsSolicitor);
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
