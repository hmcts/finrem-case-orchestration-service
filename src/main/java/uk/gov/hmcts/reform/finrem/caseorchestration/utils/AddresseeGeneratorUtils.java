package uk.gov.hmcts.reform.finrem.caseorchestration.utils;

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

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService.nullToEmpty;

@Slf4j
public class AddresseeGeneratorUtils {

    public static final String ADDRESS_MAP = "addressMap";
    public static final String NAME_MAP = "nameMap";

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final List<String> ADDRESS_FIELDS = List.of(
        "AddressLine1",
        "AddressLine2",
        "AddressLine3",
        "County",
        "PostTown",
        "PostCode"
    );

    private AddresseeGeneratorUtils() {
    }

    public static Addressee generateAddressee(FinremCaseDetails caseDetails,
                                              DocumentHelper.PaperNotificationRecipient recipient) {
        return getAddressee(caseDetails.getData(), recipient);
    }

    private static Addressee getAddressee(FinremCaseData caseData,
                                          DocumentHelper.PaperNotificationRecipient recipient) {

        if (caseData == null || recipient == null) {
            return null;
        }

        return switch (recipient) {
            case APPLICANT -> buildAddressee(
                getApplicantName(caseData),
                getApplicantAddress(caseData),
                isApplicantResideOutsideOfUK(caseData)
            );
            case RESPONDENT -> buildAddressee(
                getRespondentName(caseData),
                getRespondentAddress(caseData),
                isRespondentResideOutsideOfUK(caseData)
            );
            case INTERVENER_ONE -> buildAddressee(
                caseData.getIntervenerOne().getIntervenerName(),
                caseData.getIntervenerOne().getIntervenerAddress(),
                isIntervenerResideOutsideOfUK(caseData.getIntervenerOne())
            );
            case INTERVENER_TWO -> buildAddressee(
                caseData.getIntervenerTwo().getIntervenerName(),
                caseData.getIntervenerTwo().getIntervenerAddress(),
                isIntervenerResideOutsideOfUK(caseData.getIntervenerTwo())
            );
            case INTERVENER_THREE -> buildAddressee(
                caseData.getIntervenerThree().getIntervenerName(),
                caseData.getIntervenerThree().getIntervenerAddress(),
                isIntervenerResideOutsideOfUK(caseData.getIntervenerThree())
            );
            case INTERVENER_FOUR -> buildAddressee(
                caseData.getIntervenerFour().getIntervenerName(),
                caseData.getIntervenerFour().getIntervenerAddress(),
                isIntervenerResideOutsideOfUK(caseData.getIntervenerFour())
            );
            default -> null;
        };
    }

    private static Addressee buildAddressee(String name, Address address, boolean outsideUK) {
        return Addressee.builder()
            .name(name)
            .formattedAddress(formatAddressForLetterPrinting(address, outsideUK))
            .build();
    }

    private static String getApplicantName(FinremCaseData caseData) {
        return caseData.isApplicantRepresentedByASolicitor()
            ? caseData.getAppSolicitorName()
            : caseData.getFullApplicantName();
    }

    private static Address getApplicantAddress(FinremCaseData caseData) {
        return caseData.isApplicantRepresentedByASolicitor()
            ? caseData.getAppSolicitorAddress()
            : caseData.getContactDetailsWrapper().getApplicantAddress();
    }

    private static String getRespondentName(FinremCaseData caseData) {
        return caseData.isRespondentRepresentedByASolicitor()
            ? caseData.getRespondentSolicitorName()
            : caseData.getRespondentFullName();
    }

    private static Address getRespondentAddress(FinremCaseData caseData) {
        return caseData.isRespondentRepresentedByASolicitor()
            ? caseData.getContactDetailsWrapper().getRespondentSolicitorAddress()
            : caseData.getContactDetailsWrapper().getRespondentAddress();
    }

    public static String formatAddressForLetterPrinting(Address address, boolean recipientResideOutsideOfUK) {
        Map<String, Object> addressMap = OBJECT_MAPPER.convertValue(
            address,
            OBJECT_MAPPER.getTypeFactory().constructMapType(Map.class, String.class, Object.class)
        );

        return formatAddressForLetterPrinting(addressMap, recipientResideOutsideOfUK);
    }

    private static String formatAddressForLetterPrinting(Map<String, Object> address, boolean isInternational) {
        if (address == null) {
            return "";
        }

        return ADDRESS_FIELDS.stream()
            .map(address::get)
            .filter(Objects::nonNull)
            .map(Object::toString)
            .filter(StringUtils::isNotEmpty)
            .filter(value -> !"null".equals(value))
            .collect(Collectors.joining("\n"));
    }

    public static Map<String, Map<GeneralLetterAddressToType, ?>> getAddressToCaseDataMapping(FinremCaseData data) {
        Map<GeneralLetterAddressToType, Address> addressMap = Map.of(
            GeneralLetterAddressToType.APPLICANT_SOLICITOR, getAddressOrNew(data.getAppSolicitorAddress()),
            GeneralLetterAddressToType.RESPONDENT_SOLICITOR, getAddressOrNew(data.getContactDetailsWrapper().getRespondentSolicitorAddress()),
            GeneralLetterAddressToType.RESPONDENT, getAddressOrNew(data.getContactDetailsWrapper().getRespondentAddress()),
            GeneralLetterAddressToType.OTHER, getAddressOrNew(data.getGeneralLetterWrapper().getGeneralLetterRecipientAddress())
        );

        Map<GeneralLetterAddressToType, String> nameMap = Map.of(
            GeneralLetterAddressToType.APPLICANT_SOLICITOR, nullToEmpty(data.getAppSolicitorName()),
            GeneralLetterAddressToType.RESPONDENT_SOLICITOR, nullToEmpty(data.getRespondentSolicitorName()),
            GeneralLetterAddressToType.RESPONDENT, nullToEmpty(data.getRespondentFullName()),
            GeneralLetterAddressToType.OTHER, nullToEmpty(data.getGeneralLetterWrapper().getGeneralLetterRecipient())
        );

        return Map.of(
            ADDRESS_MAP, addressMap,
            NAME_MAP, nameMap
        );
    }

    public static Address getAddressOrNew(Address address) {
        return Optional.ofNullable(address).orElse(new Address());
    }

    private static boolean isApplicantResideOutsideOfUK(FinremCaseData caseData) {
        ContactDetailsWrapper wrapper = caseData.getContactDetailsWrapper();
        return YesOrNo.YES.equals(wrapper.getApplicantResideOutsideUK());
    }

    private static boolean isRespondentResideOutsideOfUK(FinremCaseData caseData) {
        ContactDetailsWrapper wrapper = caseData.getContactDetailsWrapper();
        return YesOrNo.YES.equals(wrapper.getRespondentResideOutsideUK());
    }

    private static boolean isIntervenerResideOutsideOfUK(IntervenerWrapper intervenerWrapper) {
        return YesOrNo.YES.equals(intervenerWrapper.getIntervenerResideOutsideUK());
    }
}