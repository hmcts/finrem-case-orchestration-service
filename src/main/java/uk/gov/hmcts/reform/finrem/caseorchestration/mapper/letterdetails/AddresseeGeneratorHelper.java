package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.reform.bsp.common.model.document.Addressee;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.ccd.domain.Address;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.GeneralLetterAddressToType;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService.nullToEmpty;

public class AddresseeGeneratorHelper {

    public static final String ADDRESS_MAP = "addressMap";
    public static final String NAME_MAP = "nameMap";

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
        return caseData.isApplicantRepresentedByASolicitor()
            ? caseData.getApplicantSolicitorName()
            : caseData.getFullApplicantName();

    }

    private static Address getAppAddress(FinremCaseData caseData) {
        return caseData.isApplicantRepresentedByASolicitor()
            ? caseData.getApplicantSolicitorAddress()
            : caseData.getContactDetailsWrapper().getApplicantAddress();

    }

    private static Addressee getRespondentAddressee(FinremCaseData caseData) {
        return Addressee.builder()
            .formattedAddress(formatAddressForLetterPrinting(getRespAddress(caseData)))
            .name(getRespName(caseData))
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

    public static Map<String, Map<GeneralLetterAddressToType, ?>> getAddressToCaseDataMapping(FinremCaseData data) {
        Map<GeneralLetterAddressToType, Address> generalLetterAddressToValueToAddress = Map.of(
            GeneralLetterAddressToType.APPLICANT_SOLICITOR, getAddressOrNew(data.getApplicantSolicitorAddress()),
            GeneralLetterAddressToType.RESPONDENT_SOLICITOR, getAddressOrNew(data.getContactDetailsWrapper().getRespondentSolicitorAddress()),
            GeneralLetterAddressToType.RESPONDENT, getAddressOrNew(data.getContactDetailsWrapper().getRespondentAddress()),
            GeneralLetterAddressToType.OTHER, getAddressOrNew(data.getGeneralLetterWrapper().getGeneralLetterRecipientAddress()));

        Map<GeneralLetterAddressToType, String> generalLetterAddressToName = Map.of(
            GeneralLetterAddressToType.APPLICANT_SOLICITOR, nullToEmpty(data.getApplicantSolicitorName()),
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
}
