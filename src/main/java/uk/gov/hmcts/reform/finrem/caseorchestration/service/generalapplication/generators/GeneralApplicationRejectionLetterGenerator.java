package uk.gov.hmcts.reform.finrem.caseorchestration.service.generalapplication.generators;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.generalapplication.GeneralApplicationRejectionLetterDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseHearingFunctions;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.generator.BaseContestedLetterDetailsGenerator;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_RESPONDENT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.DIVORCE_CASE_NUMBER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_REJECT_REASON;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseHearingFunctions.getCourtDetailsString;

@Component
@Slf4j
public class GeneralApplicationRejectionLetterGenerator extends BaseContestedLetterDetailsGenerator {

    public static final String LETTER_DATE_FORMAT = "yyyy-MM-dd";
    private final ObjectMapper objectMapper;

    public GeneralApplicationRejectionLetterGenerator(ObjectMapper objectMapper,
                                                      CaseDataService caseDataService,
                                                      DocumentHelper documentHelper) {
        super(caseDataService, documentHelper);
        this.objectMapper = objectMapper;
    }

    @Override
    public GeneralApplicationRejectionLetterDetails generate(CaseDetails caseDetails,
                                                             DocumentHelper.PaperNotificationRecipient recipient) {
        try {
            return GeneralApplicationRejectionLetterDetails.builder()
                .divorceCaseNumber(Objects.toString(caseDetails.getData().get(DIVORCE_CASE_NUMBER), ""))
                .applicantName(caseDataService.buildFullApplicantName(caseDetails))
                .respondentName(caseDataService.buildFullRespondentName(caseDetails))
                .addressee(getAddressee(caseDetails, recipient))
                .caseNumber(caseDetails.getId().toString())
                .letterDate(DateTimeFormatter.ofPattern(LETTER_DATE_FORMAT).format(LocalDate.now()))
                .courtDetails(getCourtDetails(caseDetails))
                .generalApplicationRejectionReason((String) caseDetails.getData().get(GENERAL_APPLICATION_REJECT_REASON))
                .reference((String) getReference(caseDetails, recipient))
                .build();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private Object getReference(CaseDetails caseDetails, DocumentHelper.PaperNotificationRecipient recipient) {
        if (isApplicantLitigant(caseDetails, recipient) || isRespondentLitigant(caseDetails, recipient)) {
            return "";
        }
        return recipient == DocumentHelper.PaperNotificationRecipient.APPLICANT
            ? caseDetails.getData().get("solicitorReference") : caseDetails.getData().get("rSolicitorReference");
    }

    private boolean isApplicantLitigant(CaseDetails caseDetails, DocumentHelper.PaperNotificationRecipient recipient) {
        return recipient == DocumentHelper.PaperNotificationRecipient.APPLICANT
            && NO_VALUE.equalsIgnoreCase((String) caseDetails.getData().get(APPLICANT_REPRESENTED));
    }

    private boolean isRespondentLitigant(CaseDetails caseDetails, DocumentHelper.PaperNotificationRecipient recipient) {
        return recipient == DocumentHelper.PaperNotificationRecipient.RESPONDENT
            && NO_VALUE.equalsIgnoreCase((String) caseDetails.getData().get(CONTESTED_RESPONDENT_REPRESENTED));
    }

    private Map<String,Object> getCourtDetails(CaseDetails caseDetails) throws JsonProcessingException {
        Map<String, Object> data = caseDetails.getData();
        Map<String, Object> courtDetailsMap = objectMapper.readValue(getCourtDetailsString(), HashMap.class);
        return (Map<String, Object>) courtDetailsMap.get(data.get(CaseHearingFunctions.getSelectedCourt(data)));
    }
}
