package uk.gov.hmcts.reform.finrem.caseorchestration.service.updatefrc.generators;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.CourtDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.AddresseeGeneratorHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.frcupateinfo.UpdateFrcInfoLetterDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.wrapper.CourtListWrapper;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService.nullToEmpty;

@Component
@RequiredArgsConstructor
@Slf4j
public class UpdateFrcInfoLetterDetailsGenerator {

    public static final String LETTER_DATE_FORMAT = "yyyy-MM-dd";
    private final CourtDetailsMapper courtDetailsMapper;
    private final ObjectMapper objectMapper;

    public UpdateFrcInfoLetterDetails generate(FinremCaseDetails caseDetails,
                                               DocumentHelper.PaperNotificationRecipient recipient) {
        return UpdateFrcInfoLetterDetails.builder()
            .courtDetails(getCourtDetailsAsMap(caseDetails))
            .reference(getSolicitorReference(caseDetails, recipient))
            .divorceCaseNumber(caseDetails.getCaseData().getDivorceCaseNumber())
            .applicantName(caseDetails.getCaseData().getFullApplicantName())
            .respondentName(caseDetails.getCaseData().getRespondentFullName())
            .addressee(AddresseeGeneratorHelper.generateAddressee(caseDetails, recipient))
            .caseNumber(String.valueOf(caseDetails.getId()))
            .letterDate(DateTimeFormatter.ofPattern(LETTER_DATE_FORMAT).format(LocalDate.now()))
            .build();
    }

    private Map<String, Object> getCourtDetailsAsMap(FinremCaseDetails caseDetails) {
        CourtListWrapper courtList = caseDetails.getCaseData().getRegionWrapper().getDefaultCourtList();
        return objectMapper.convertValue(courtDetailsMapper.getCourtDetails(courtList),
            TypeFactory.defaultInstance().constructMapType(HashMap.class, String.class, Object.class));
    }

    private String getSolicitorReference(FinremCaseDetails caseDetails,
                                         DocumentHelper.PaperNotificationRecipient recipient) {
        return recipient == APPLICANT
            ? nullToEmpty(caseDetails.getCaseData().getContactDetailsWrapper().getSolicitorReference())
            : nullToEmpty(caseDetails.getCaseData().getContactDetailsWrapper().getRespondentSolicitorReference());
    }
}
