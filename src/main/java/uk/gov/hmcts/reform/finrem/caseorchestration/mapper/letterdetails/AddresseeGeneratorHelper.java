package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.reform.bsp.common.model.document.Addressee;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.ccd.domain.Address;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService.nullToEmpty;

public class AddresseeGeneratorHelper {

    public static Addressee generateAddressee(FinremCaseDetails caseDetails,
                                              DocumentHelper.PaperNotificationRecipient recipient) {
        return getAddressee(caseDetails.getCaseData(), recipient);
    }

    private static Addressee getAddressee(FinremCaseData caseData,
                                   DocumentHelper.PaperNotificationRecipient recipient) {
        return recipient == DocumentHelper.PaperNotificationRecipient.APPLICANT
            ? getApplicantAddressee(caseData)
            : getRespondentAddressee(caseData);
    }

    private static Addressee getApplicantAddressee(FinremCaseData caseData) {
        return Addressee.builder()
            .formattedAddress(formatAddressForLetterPrinting(getAppAddress(caseData)))
            .name(getAppName(caseData))
            .build();
    }

    private static String getAppName(FinremCaseData caseData) {
        return !nullToEmpty(caseData.getContactDetailsWrapper().getApplicantAddress()).isEmpty()
            ? caseData.getFullApplicantName()
            : caseData.getApplicantSolicitorName();
    }

    private static Address getAppAddress(FinremCaseData caseData) {
        return !nullToEmpty(caseData.getContactDetailsWrapper().getApplicantAddress()).isEmpty()
            ? caseData.getContactDetailsWrapper().getApplicantAddress()
            : caseData.getApplicantSolicitorAddress();
    }

    private static Addressee getRespondentAddressee(FinremCaseData caseData) {
        return Addressee.builder()
            .formattedAddress(formatAddressForLetterPrinting(getRespAddress(caseData)))
            .name(getRespName(caseData))
            .build();
    }

    private static String getRespName(FinremCaseData caseData) {
        return nullToEmpty(caseData.getContactDetailsWrapper().getRespondentAddress()).isEmpty()
            ? caseData.getRespondentFullName()
            : caseData.getRespondentSolicitorName();
    }

    private static Address getRespAddress(FinremCaseData caseData) {
        return !nullToEmpty(caseData.getContactDetailsWrapper().getRespondentAddress()).isEmpty()
            ? caseData.getContactDetailsWrapper().getRespondentAddress()
            : caseData.getContactDetailsWrapper().getRespondentSolicitorAddress();
    }

    public static String formatAddressForLetterPrinting(Address address) {
        return formatAddressForLetterPrinting(new ObjectMapper().convertValue(address, Map.class));
    }

    private static String formatAddressForLetterPrinting(Map<String, Object> address) {
        if (address != null) {
            return Stream.of("AddressLine1", "AddressLine2", "County", "PostTown", "PostCode")
                .map(address::get)
                .filter(Objects::nonNull)
                .map(Object::toString)
                .filter(StringUtils::isNotEmpty)
                .filter(s -> !s.equals("null"))
                .collect(Collectors.joining("\n"));
        }
        return "";
    }
}
