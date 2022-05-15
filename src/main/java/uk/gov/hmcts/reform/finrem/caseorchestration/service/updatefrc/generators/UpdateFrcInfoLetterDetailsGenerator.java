package uk.gov.hmcts.reform.finrem.caseorchestration.service.updatefrc.generators;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bsp.common.model.document.Addressee;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.frcupateinfo.UpdateFrcInfoLetterDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Objects;

import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.RESPONDENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.DIVORCE_CASE_NUMBER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService.nullToEmpty;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseHearingFunctions.buildFrcCourtDetails;

@Component
@RequiredArgsConstructor
@Slf4j
public class UpdateFrcInfoLetterDetailsGenerator {

    private final DocumentHelper documentHelper;
    private final CaseDataService caseDataService;
    public static final String LETTER_DATE_FORMAT = "yyyy-MM-dd";

    public UpdateFrcInfoLetterDetails generate(CaseDetails caseDetails,
                                               DocumentHelper.PaperNotificationRecipient recipient) {
        return UpdateFrcInfoLetterDetails.builder()
            .courtDetails(buildFrcCourtDetails(caseDetails.getData()))
            .reference(getSolicitorReference(caseDetails, recipient))
            .divorceCaseNumber(Objects.toString(caseDetails.getData().get(DIVORCE_CASE_NUMBER)))
            .applicantName(documentHelper.getApplicantFullName(caseDetails))
            .respondentName(documentHelper.getRespondentFullNameContested(caseDetails))
            .addressee(getAddressee(caseDetails, recipient))
            .caseNumber(caseDetails.getId().toString())
            .letterDate(DateTimeFormatter.ofPattern(LETTER_DATE_FORMAT).format(LocalDate.now()))
            .build();
    }

    private Addressee getAddressee(CaseDetails caseDetails, DocumentHelper.PaperNotificationRecipient recipient) {
        Map<String, Object> caseData = caseDetails.getData();

        if (isApplicantSolicitor(caseData, recipient)) {
            log.info("Recipient is Applicant's Solicitor");
            return buildAddressee(nullToEmpty((caseData.get(CONTESTED_SOLICITOR_NAME))),
                getSolicitorFormattedAddress(caseDetails, CONTESTED_SOLICITOR_ADDRESS));
        } else if (isRespondentSolicitor(caseData, recipient)) {
            log.info("Recipient is Respondent's Solicitor");
            return buildAddressee(nullToEmpty((caseData.get(RESP_SOLICITOR_NAME))),
                getSolicitorFormattedAddress(caseDetails, RESP_SOLICITOR_ADDRESS));
        } else {
            log.info("Recipient is {}", recipient);
            return buildAddressee(getLitigantName(caseDetails, recipient),
                getLitigantFormattedAddress(caseDetails, recipient));
        }
    }

    private Addressee buildAddressee(String name, String address) {
        return Addressee.builder()
            .name(name)
            .formattedAddress(address)
            .build();
    }

    private String getLitigantName(CaseDetails caseDetails, DocumentHelper.PaperNotificationRecipient recipient) {
        boolean isApplicant = recipient == APPLICANT;
        return isApplicant
            ? caseDataService.buildFullApplicantName(caseDetails)
            : caseDataService.buildFullRespondentName(caseDetails);
    }

    private String getLitigantFormattedAddress(CaseDetails caseDetails,
                                               DocumentHelper.PaperNotificationRecipient recipient) {
        Map<String, Object> caseData = caseDetails.getData();
        boolean isApplicant = recipient == APPLICANT;
        return documentHelper.formatAddressForLetterPrinting((Map) caseData.get(isApplicant ? APPLICANT_ADDRESS : RESPONDENT_ADDRESS));
    }

    private String getSolicitorFormattedAddress(CaseDetails caseDetails, String addressKey) {
        return documentHelper.formatAddressForLetterPrinting((Map) caseDetails.getData().get(addressKey));
    }

    private boolean isApplicantSolicitor(Map<String, Object> caseData,
                                         DocumentHelper.PaperNotificationRecipient recipient) {
        return recipient == APPLICANT && caseDataService.isApplicantRepresentedByASolicitor(caseData);
    }

    private boolean isRespondentSolicitor(Map<String, Object> caseData,
                                          DocumentHelper.PaperNotificationRecipient recipient) {
        return recipient == RESPONDENT && caseDataService.isRespondentRepresentedByASolicitor(caseData);
    }

    private String getSolicitorReference(CaseDetails caseDetails,
                                         DocumentHelper.PaperNotificationRecipient recipient) {
        return Objects.toString(nullToEmpty(caseDetails.getData().get(recipient == APPLICANT
            ? SOLICITOR_REFERENCE
            : RESP_SOLICITOR_REFERENCE)));
    }
}
