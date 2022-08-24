package uk.gov.hmcts.reform.finrem.caseorchestration.service.updatefrc.generators;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.frcupateinfo.UpdateFrcInfoLetterDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.generator.BaseContestedLetterDetailsGenerator;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.DIVORCE_CASE_NUMBER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService.nullToEmpty;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseHearingFunctions.buildFrcCourtDetails;

@Component
@Slf4j
public class UpdateFrcInfoLetterDetailsGenerator extends BaseContestedLetterDetailsGenerator {

    public static final String LETTER_DATE_FORMAT = "yyyy-MM-dd";

    public UpdateFrcInfoLetterDetailsGenerator(CaseDataService caseDataService, DocumentHelper documentHelper) {
        super(caseDataService, documentHelper);
    }

    @Override
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

    private String getSolicitorReference(CaseDetails caseDetails,
                                         DocumentHelper.PaperNotificationRecipient recipient) {
        return nullToEmpty(caseDetails.getData().get(getSolicitorReferenceKey(recipient)));
    }

    private String getSolicitorReferenceKey(DocumentHelper.PaperNotificationRecipient recipient) {
        return recipient == APPLICANT ? SOLICITOR_REFERENCE : RESP_SOLICITOR_REFERENCE;
    }
}
