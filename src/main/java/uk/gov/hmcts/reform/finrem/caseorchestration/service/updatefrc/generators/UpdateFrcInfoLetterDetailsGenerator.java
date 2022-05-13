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
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService.nullToEmpty;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseHearingFunctions.buildFrcCourtDetails;

@Component
@RequiredArgsConstructor
@Slf4j
public class UpdateFrcInfoLetterDetailsGenerator {

    private final DocumentHelper documentHelper;
    public static final String LETTER_DATE_FORMAT = "yyyy-MM-dd";
    private final CaseDataService caseDataService;

    public UpdateFrcInfoLetterDetails generate(CaseDetails caseDetails,
                                               DocumentHelper.PaperNotificationRecipient recipient) {
        return UpdateFrcInfoLetterDetails.builder()
            .courtDetails(buildFrcCourtDetails(caseDetails.getData()))
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

        if (recipient == APPLICANT && caseDataService.isApplicantRepresentedByASolicitor(caseData)) {
            log.info("Applicant is represented by a solicitor");
            return buildAddressee(nullToEmpty((caseData.get(CONTESTED_SOLICITOR_NAME))),
                documentHelper.formatAddressForLetterPrinting((Map) caseData.get(CONTESTED_SOLICITOR_ADDRESS)));
        } else if (recipient == RESPONDENT && caseDataService.isRespondentRepresentedByASolicitor(caseDetails.getData())) {
            log.info("Respondent is represented by a solicitor");
            return buildAddressee(nullToEmpty((caseData.get(RESP_SOLICITOR_NAME))),
                documentHelper.formatAddressForLetterPrinting((Map) caseData.get(RESP_SOLICITOR_ADDRESS)));
        } else {
            log.info("{} is not represented by a solicitor", recipient);
            boolean isApplicant = recipient == APPLICANT;
            return buildAddressee(isApplicant
                    ? caseDataService.buildFullApplicantName(caseDetails)
                    : caseDataService.buildFullRespondentName(caseDetails),
                documentHelper.formatAddressForLetterPrinting((Map) caseData.get(isApplicant ? APPLICANT_ADDRESS : RESPONDENT_ADDRESS)));
        }
    }

    private Addressee buildAddressee(String name, String address) {
        return Addressee.builder()
            .name(name)
            .formattedAddress(address)
            .build();
    }
}
