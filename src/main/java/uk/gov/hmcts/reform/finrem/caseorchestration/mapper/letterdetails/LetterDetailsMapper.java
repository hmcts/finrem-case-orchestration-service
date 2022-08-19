package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bsp.common.model.document.CtscContactDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.ConsentedApplicationHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.CourtDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.BasicLetterDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.wrapper.CourtListWrapper;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CTSC_CARE_OF;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CTSC_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CTSC_OPENING_HOURS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CTSC_PHONE_NUMBER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CTSC_POSTCODE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CTSC_PO_BOX;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CTSC_SERVICE_CENTRE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CTSC_TOWN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.AddresseeGeneratorHelper.generateAddressee;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService.nullToEmpty;

@Component
@RequiredArgsConstructor
public class LetterDetailsMapper {
    private static final String CASE_DETAILS = "caseDetails";
    private static final String CASE_DATA = "case_data";

    private final ObjectMapper objectMapper;
    private final CourtDetailsMapper courtDetailsMapper;
    private final ConsentedApplicationHelper consentedApplicationHelper;

    public BasicLetterDetails buildLetterDetails(FinremCaseDetails caseDetails,
                                                 DocumentHelper.PaperNotificationRecipient recipient,
                                                 CourtListWrapper courtList) {
        return BasicLetterDetails.builder()
            .applicantName(caseDetails.getCaseData().getFullApplicantName())
            .respondentName(caseDetails.getCaseData().getRespondentFullName())
            .addressee(generateAddressee(caseDetails, recipient))
            .courtDetails(courtDetailsMapper.getCourtDetails(courtList))
            .ctscContactDetails(getCtscContactDetails())
            .letterDate(String.valueOf(LocalDate.now()))
            .reference(getReference(caseDetails.getCaseData(), recipient))
            .caseNumber(String.valueOf(caseDetails.getId()))
            .divorceCaseNumber(caseDetails.getCaseData().getDivorceCaseNumber())
            .orderType(consentedApplicationHelper.getOrderType(caseDetails.getCaseData()))
            .build();
    }

    public Map<String, Object> getLetterDetailsAsMap(FinremCaseDetails caseDetails,
                                     DocumentHelper.PaperNotificationRecipient recipient,
                                     CourtListWrapper courtList) {
        Map<String, Object> documentTemplateDetails =
            objectMapper.convertValue(buildLetterDetails(caseDetails, recipient, courtList),
                TypeFactory.defaultInstance().constructMapType(HashMap.class, String.class, Object.class));

        Map<String, Object> caseDetailsMap = Map.of(
            CASE_DATA, documentTemplateDetails,
            "id", caseDetails.getId());

        return Map.of(CASE_DETAILS, caseDetailsMap);
    }

    private String getReference(FinremCaseData caseData, DocumentHelper.PaperNotificationRecipient recipient) {
        return recipient == DocumentHelper.PaperNotificationRecipient.APPLICANT
            || recipient == DocumentHelper.PaperNotificationRecipient.APP_SOLICITOR
            ? nullToEmpty(caseData.getContactDetailsWrapper().getSolicitorReference())
            : nullToEmpty(caseData.getContactDetailsWrapper().getRespondentSolicitorReference());
    }

    private CtscContactDetails getCtscContactDetails() {
        return CtscContactDetails.builder()
            .serviceCentre(CTSC_SERVICE_CENTRE)
            .careOf(CTSC_CARE_OF)
            .poBox(CTSC_PO_BOX)
            .town(CTSC_TOWN)
            .postcode(CTSC_POSTCODE)
            .emailAddress(CTSC_EMAIL_ADDRESS)
            .phoneNumber(CTSC_PHONE_NUMBER)
            .openingHours(CTSC_OPENING_HOURS)
            .build();
    }

}
