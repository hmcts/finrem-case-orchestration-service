package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.reform.bsp.common.model.document.Addressee;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Address;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralLetterAddressToType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerWrapper;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService.nullToEmpty;

@Slf4j
public class AddresseeGeneratorHelper {

    public static final String ADDRESS_MAP = "addressMap";
    public static final String NAME_MAP = "nameMap";

    private AddresseeGeneratorHelper() {
    }

    public static Addressee generateAddressee(FinremCaseDetails caseDetails,
                                              DocumentHelper.PaperNotificationRecipient recipient) {

        return getAddressee(caseDetails.getData(), recipient);
    }

    private static Addressee getAddressee(FinremCaseData caseData,
                                          DocumentHelper.PaperNotificationRecipient recipient) {
        if (recipient == DocumentHelper.PaperNotificationRecipient.APPLICANT) {
            return getApplicantAddressee(caseData);
        } else if (recipient == DocumentHelper.PaperNotificationRecipient.RESPONDENT) {
            return getRespondentAddressee(caseData);
        } else if (recipient == DocumentHelper.PaperNotificationRecipient.INTERVENER_ONE) {
            return getIntervenerAddressee(caseData.getIntervenerOne());
        } else if (recipient == DocumentHelper.PaperNotificationRecipient.INTERVENER_TWO) {
            return getIntervenerAddressee(caseData.getIntervenerTwo());
        } else if (recipient == DocumentHelper.PaperNotificationRecipient.INTERVENER_THREE) {
            return getIntervenerAddressee(caseData.getIntervenerThree());
        } else if (recipient == DocumentHelper.PaperNotificationRecipient.INTERVENER_FOUR) {
            return getIntervenerAddressee(caseData.getIntervenerFour());
        } else {
            return null;
        }
    }

    private static Addressee getApplicantAddressee(FinremCaseData caseData) {
        return Addressee.builder()
            .name(getAppName(caseData))
            .formattedAddress(formatAddressForLetterPrinting(getAppAddress(caseData),
                isApplicantResideOutsideOfUK(caseData)))
            .build();
    }

    private static String getAppName(FinremCaseData caseData) {
        return caseData.isApplicantRepresentedByASolicitor()
            ? caseData.getAppSolicitorName()
            : caseData.getFullApplicantName();
    }

    private static Address getAppAddress(FinremCaseData caseData) {
        return caseData.isApplicantRepresentedByASolicitor()
            ? caseData.getAppSolicitorAddress()
            : caseData.getContactDetailsWrapper().getApplicantAddress();

    }

    private static Addressee getRespondentAddressee(FinremCaseData caseData) {
        return Addressee.builder()
            .name(getRespName(caseData))
            .formattedAddress(formatAddressForLetterPrinting(getRespAddress(caseData),
                isRespondentResideOutsideOfUK(caseData)))
            .build();
    }

    private static String getRespName(FinremCaseData caseData) {
        return caseData.isRespondentRepresentedByASolicitor()
            ? caseData.getRespondentSolicitorName()
            : caseData.getRespondentFullName();
    }

    private static Address getRespAddress(FinremCaseData caseData) {
        return caseData.isRespondentRepresentedByASolicitor()
            ? caseData.getContactDetailsWrapper().getRespondentSolicitorAddress()
            : caseData.getContactDetailsWrapper().getRespondentAddress();
    }

    private static Addressee getIntervenerAddressee(IntervenerWrapper intervenerWrapper) {
        return Addressee.builder()
            .name(intervenerWrapper.getIntervenerName())
            .formattedAddress(formatAddressForLetterPrinting(intervenerWrapper.getIntervenerAddress(),
                isIntervenerResideOutsideOfUK(intervenerWrapper)))
            .build();
    }

    public static String formatAddressForLetterPrinting(Address address, boolean recipientResideOutsideOfUK) {
        return formatAddressForLetterPrinting(new ObjectMapper().convertValue(address, Map.class), recipientResideOutsideOfUK);
    }

    private static String formatAddressForLetterPrinting(Map<String, Object> address, boolean isInternational) {
        if (address != null) {
            Stream<String> addressLines = Stream.of("AddressLine1", "AddressLine2", "AddressLine3",
                "County", "PostTown", "PostCode", isInternational ? "Country" : "");
            return addressLines.map(address::get)
                .filter(Objects::nonNull)
                .map(Object::toString)
                .filter(StringUtils::isNotEmpty)
                .filter(s -> !s.equals("null"))
                .collect(Collectors.joining("\n"));
        }
        return "";
    }

    @SuppressWarnings("java:S1452")
    public static Map<String, Map<GeneralLetterAddressToType, ?>> getAddressToCaseDataMapping(FinremCaseData data) {
        Map<GeneralLetterAddressToType, Address> generalLetterAddressToValueToAddress = Map.of(
            GeneralLetterAddressToType.APPLICANT_SOLICITOR, getAddressOrNew(data.getAppSolicitorAddress()),
            GeneralLetterAddressToType.RESPONDENT_SOLICITOR, getAddressOrNew(data.getContactDetailsWrapper().getRespondentSolicitorAddress()),
            GeneralLetterAddressToType.RESPONDENT, getAddressOrNew(data.getContactDetailsWrapper().getRespondentAddress()),
            GeneralLetterAddressToType.OTHER, getAddressOrNew(data.getGeneralLetterWrapper().getGeneralLetterRecipientAddress()));

        Map<GeneralLetterAddressToType, String> generalLetterAddressToName = Map.of(
            GeneralLetterAddressToType.APPLICANT_SOLICITOR, nullToEmpty(data.getAppSolicitorName()),
            GeneralLetterAddressToType.RESPONDENT_SOLICITOR, nullToEmpty(data.getRespondentSolicitorName()),
            GeneralLetterAddressToType.RESPONDENT, nullToEmpty(data.getRespondentFullName()),
            GeneralLetterAddressToType.OTHER, nullToEmpty(data.getGeneralLetterWrapper().getGeneralLetterRecipient()));

        return Map.of(
            ADDRESS_MAP, generalLetterAddressToValueToAddress,
            NAME_MAP, generalLetterAddressToName);
    }

    public static Address getAddressOrNew(Address address) {
        return Optional.ofNullable(address).orElse(new Address());
    }

    private static boolean isApplicantResideOutsideOfUK(FinremCaseData caseData) {
        ContactDetailsWrapper wrapper = caseData.getContactDetailsWrapper();
        YesOrNo applicantResideOutsideUK = wrapper.getApplicantResideOutsideUK();
        return YesOrNo.YES.equals(applicantResideOutsideUK);
    }

    private static boolean isRespondentResideOutsideOfUK(FinremCaseData caseData) {
        ContactDetailsWrapper wrapper = caseData.getContactDetailsWrapper();
        YesOrNo respondentResideOutsideUK = wrapper.getRespondentResideOutsideUK();
        return YesOrNo.YES.equals(respondentResideOutsideUK);
    }

    private static boolean isIntervenerResideOutsideOfUK(IntervenerWrapper intervenerWrapper) {
        YesOrNo intervenerResideOutsideUK = intervenerWrapper.getIntervenerResideOutsideUK();
        return YesOrNo.YES.equals(intervenerResideOutsideUK);
    }
}
