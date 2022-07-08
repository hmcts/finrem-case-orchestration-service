package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.util.TriConsumer;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bsp.common.model.document.Addressee;
import uk.gov.hmcts.reform.bsp.common.model.document.CtscContactDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.CourtDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.FrcCourtDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.wrapper.CourtListWrapper;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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

@Component
@RequiredArgsConstructor
@Builder
public class LetterDetailsMapper {
    private String applicantName;
    private String respondentName;
    private String reference;
    private String caseNumber;
    private String letterDate;
    private FrcCourtDetails courtDetails;
    private Addressee addressee;

    static final String CASE_DETAILS = "caseDetails";
    static final String CASE_DATA = "case_data";


    private CourtDetailsMapper courtDetailsMapper;

    private final TriConsumer<Field, LetterDetails, Map<String, Object>> addMapEntry =
        (field, letterDetails, letterDetailsMap) -> {
        try {
            field.setAccessible(true);
            letterDetailsMap.put(field.getName(), field.get(letterDetails));
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Unable to access letterDetails field");
        }
    };

    public LetterDetails buildLetterDetails(FinremCaseDetails caseDetails,
                                                 DocumentHelper.PaperNotificationRecipient recipient,
                                                 CourtListWrapper courtList) {
        return BasicLetterDetails.builder()
            .applicantName(caseDetails.getCaseData().getFullApplicantName())
            .respondentName(caseDetails.getCaseData().getRespondentFullName())
            .addressee(generateAddressee(caseDetails, recipient))
            .courtDetails(courtDetailsMapper.getCourtDetails(courtList))
            .letterDate(String.valueOf(LocalDate.now()))
            .reference(getReference(caseDetails.getCaseData(), recipient))
            .caseNumber(String.valueOf(caseDetails.getId()))
            .ctscContactDetails(getCtscContactDetails())
            .divorceCaseNumber(caseDetails.getCaseData().getDivorceCaseNumber())
            .build();
    }

    public Map<String, Object> getLetterDetailsAsMap(FinremCaseDetails caseDetails,
                                     DocumentHelper.PaperNotificationRecipient recipient,
                                     CourtListWrapper courtList) {
        LetterDetails letterDetails = buildLetterDetails(caseDetails, recipient, courtList);
        List<Field> letterDetailsFields = Arrays.asList(BasicLetterDetails.class.getDeclaredFields());
        Map<String, Object> letterDetailsMap = new HashMap<>();
        letterDetailsFields.forEach(field -> addMapEntry.accept(field, letterDetails, letterDetailsMap));

        Map<String, Object> caseDetailsMap = new HashMap<>();
        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put(CASE_DATA, letterDetailsMap);
        caseDataMap.put("id", caseDetails.getId());
        caseDetailsMap.put(CASE_DETAILS, caseDataMap);
        return caseDetailsMap;
    }

    private String getReference(FinremCaseData caseData, DocumentHelper.PaperNotificationRecipient recipient) {
        return recipient == DocumentHelper.PaperNotificationRecipient.APPLICANT
            || recipient == DocumentHelper.PaperNotificationRecipient.APP_SOLICITOR
            ? caseData.getContactDetailsWrapper().getSolicitorReference()
            : caseData.getContactDetailsWrapper().getRespondentSolicitorReference();
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
