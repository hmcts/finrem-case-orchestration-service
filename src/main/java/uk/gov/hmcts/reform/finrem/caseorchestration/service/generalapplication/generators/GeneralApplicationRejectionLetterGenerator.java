package uk.gov.hmcts.reform.finrem.caseorchestration.service.generalapplication.generators;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bsp.common.model.document.Addressee;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.frcupateinfo.UpdateFrcInfoLetterDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.generalapplication.GeneralApplicationRejectionLetterDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.generator.BaseContestedLetterDetailsGenerator;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Objects;

import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.RESPONDENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.*;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService.nullToEmpty;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseHearingFunctions.buildFrcCourtDetails;

@Component
@RequiredArgsConstructor
@Slf4j
public class GeneralApplicationRejectionLetterGenerator extends BaseContestedLetterDetailsGenerator {

    public static final String LETTER_DATE_FORMAT = "yyyy-MM-dd";

    @Override
    public GeneralApplicationRejectionLetterDetails generate(CaseDetails caseDetails,
                                                             DocumentHelper.PaperNotificationRecipient recipient) {
        return GeneralApplicationRejectionLetterDetails.builder()
            .divorceCaseNumber(Objects.toString(caseDetails.getData().get(DIVORCE_CASE_NUMBER)))
            .applicantLName(caseDetails.getData().get(APPLICANT_LAST_NAME).toString())
            .respondentLName(caseDetails.getData().get(CONTESTED_RESPONDENT_LAST_NAME).toString())
            .addressee(getAddressee(caseDetails, recipient))
            .caseNumber(caseDetails.getId().toString())
            .letterDate(DateTimeFormatter.ofPattern(LETTER_DATE_FORMAT).format(LocalDate.now()))
            .build();
    }
}
