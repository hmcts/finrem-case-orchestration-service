package uk.gov.hmcts.reform.finrem.caseorchestration.service.generator;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.bsp.common.model.document.Addressee;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ParentLetterDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;

import java.util.Map;

import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.RESPONDENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService.nullToEmpty;

@Slf4j
public abstract class BaseContestedLetterDetailsGenerator {

    protected CaseDataService caseDataService;
    protected DocumentHelper documentHelper;

    public abstract ParentLetterDetails generate(CaseDetails caseDetails,
                                                 DocumentHelper.PaperNotificationRecipient recipient);

    protected Addressee getAddressee(CaseDetails caseDetails, DocumentHelper.PaperNotificationRecipient recipient) {
        Map<String, Object> caseData = caseDetails.getData();

        if (isApplicantSolicitor(caseData, recipient)) {
            log.info("Recipient is Applicant's Solicitor");
            return buildAddressee(nullToEmpty(caseData.get(CONTESTED_SOLICITOR_NAME)),
                getSolicitorFormattedAddress(caseDetails, CONTESTED_SOLICITOR_ADDRESS));
        } else if (isRespondentSolicitor(caseData, recipient)) {
            log.info("Recipient is Respondent's Solicitor");
            return buildAddressee(nullToEmpty(caseData.get(RESP_SOLICITOR_NAME)),
                getSolicitorFormattedAddress(caseDetails, RESP_SOLICITOR_ADDRESS));
        }

        log.info("Recipient is {}", recipient);
        return buildAddressee(getLitigantName(caseDetails, recipient), getLitigantFormattedAddress(caseDetails, recipient));
    }

    private Addressee buildAddressee(String name, String address) {
        return Addressee.builder()
            .name(name)
            .formattedAddress(address)
            .build();
    }

    private String getLitigantName(CaseDetails caseDetails, DocumentHelper.PaperNotificationRecipient recipient) {
        return recipient == APPLICANT
            ? caseDataService.buildFullApplicantName(caseDetails)
            : caseDataService.buildFullRespondentName(caseDetails);
    }

    private String getLitigantFormattedAddress(CaseDetails caseDetails,
                                               DocumentHelper.PaperNotificationRecipient recipient) {
        Map<String, Object> caseData = caseDetails.getData();
        return documentHelper.formatAddressForLetterPrinting((Map) caseData.get(getLitigantAddressKey(recipient)));
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

    private String getLitigantAddressKey(DocumentHelper.PaperNotificationRecipient recipient) {
        return recipient == APPLICANT ? APPLICANT_ADDRESS : RESPONDENT_ADDRESS;
    }
}
