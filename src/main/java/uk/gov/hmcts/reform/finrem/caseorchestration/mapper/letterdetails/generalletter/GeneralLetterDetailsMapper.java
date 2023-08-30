package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.generalletter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bsp.common.model.document.Addressee;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.CourtDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.AbstractLetterDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Address;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.CourtListWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.DocumentTemplateDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.GeneralLetterDetails;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.buildCtscContactDetails;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_SOLICITOR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER1;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER1_SOLICITOR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER2;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER2_SOLICITOR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER3;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER3_SOLICITOR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER4;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER4_SOLICITOR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.OTHER_RECIPIENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_SOLICITOR;

@Component
public class GeneralLetterDetailsMapper extends AbstractLetterDetailsMapper {

    public GeneralLetterDetailsMapper(CourtDetailsMapper courtDetailsMapper, ObjectMapper objectMapper) {
        super(courtDetailsMapper, objectMapper);
    }

    @Override
    public DocumentTemplateDetails buildDocumentTemplateDetails(FinremCaseDetails caseDetails, CourtListWrapper courtList) {

        FinremCaseData caseData = caseDetails.getData();
        return GeneralLetterDetails.builder()
            .applicantFullName(caseData.getFullApplicantName())
            .respondentFullName(caseData.getRespondentFullName())
            .ccdCaseNumber(String.valueOf(caseDetails.getId()))
            .generalLetterCreatedDate(new Date())
            .addressee(getAddressee(caseDetails))
            .solicitorReference(getSolicitorReference(caseData))
            .ctscContactDetails(buildCtscContactDetails())
            .generalLetterBody(caseData.getGeneralLetterWrapper().getGeneralLetterBody())
            .build();
    }

    private Addressee getAddressee(FinremCaseDetails caseDetails) {
        return Addressee.builder()
                .name(getRecipientName(caseDetails))
                .formattedAddress(formatAddressForLetterPrinting(getRecipientAddress(caseDetails)))
                .build();
    }

    public static String formatAddressForLetterPrinting(Address address) {
        return formatAddressForLetterPrinting(new ObjectMapper().convertValue(address, Map.class));
    }

    private static String formatAddressForLetterPrinting(Map<String, Object> address) {
        if (address != null) {
            return Stream.of("AddressLine1", "AddressLine2", "AddressLine3", "County", "PostTown", "PostCode")
                .map(address::get)
                .filter(Objects::nonNull)
                .map(Object::toString)
                .filter(StringUtils::isNotEmpty)
                .filter(s -> !s.equals("null"))
                .collect(Collectors.joining("\n"));
        }
        return "";
    }

    private String getSolicitorReference(FinremCaseData caseData) {
        String recipient = caseData.getGeneralLetterWrapper().getGeneralLetterAddressee().getValue().getCode();

        if (List.of(RESPONDENT, OTHER_RECIPIENT, APPLICANT, INTERVENER1, INTERVENER2, INTERVENER3, INTERVENER4).contains(recipient)) {
            return StringUtils.EMPTY;
        }
        return switch (recipient) {
            case RESPONDENT_SOLICITOR -> caseData.getContactDetailsWrapper().getRespondentSolicitorReference();
            case APPLICANT_SOLICITOR -> caseData.getContactDetailsWrapper().getSolicitorReference();
            case INTERVENER1_SOLICITOR -> caseData.getIntervenerOneWrapper().getIntervenerSolicitorReference();
            case INTERVENER2_SOLICITOR -> caseData.getIntervenerTwoWrapper().getIntervenerSolicitorReference();
            case INTERVENER3_SOLICITOR -> caseData.getIntervenerThreeWrapper().getIntervenerSolicitorReference();
            case INTERVENER4_SOLICITOR -> caseData.getIntervenerFourWrapper().getIntervenerSolicitorReference();
            default -> null;
        };
    }

    private String getRecipientName(FinremCaseDetails caseDetails) {
        FinremCaseData data = caseDetails.getData();
        String generalLetterAddressee = data.getGeneralLetterWrapper().getGeneralLetterAddressee().getValue().getCode();

        return switch (generalLetterAddressee) {
            case APPLICANT_SOLICITOR -> data.getAppSolicitorName();
            case RESPONDENT_SOLICITOR -> data.getRespondentSolicitorName();
            case RESPONDENT -> data.getRespondentFullName();
            case OTHER_RECIPIENT -> data.getGeneralLetterWrapper().getGeneralLetterRecipient();
            case INTERVENER1, INTERVENER1_SOLICITOR -> getIntervenerAddressee(data.getIntervenerOneWrapper(), generalLetterAddressee);
            case INTERVENER2, INTERVENER2_SOLICITOR -> getIntervenerAddressee(data.getIntervenerTwoWrapper(), generalLetterAddressee);
            case INTERVENER3, INTERVENER3_SOLICITOR -> getIntervenerAddressee(data.getIntervenerThreeWrapper(), generalLetterAddressee);
            case INTERVENER4, INTERVENER4_SOLICITOR -> getIntervenerAddressee(data.getIntervenerFourWrapper(), generalLetterAddressee);
            default -> null;
        };
    }

    private String getIntervenerAddressee(IntervenerWrapper wrapper, String generalLetterAddressee) {
        return generalLetterAddressee.equals(INTERVENER1) || generalLetterAddressee.equals(INTERVENER2)
            || generalLetterAddressee.equals(INTERVENER3) || generalLetterAddressee.equals(INTERVENER4)
            ? wrapper.getIntervenerName() : wrapper.getIntervenerSolName();
    }

    private Address getRecipientAddress(FinremCaseDetails caseDetails) {
        FinremCaseData data = caseDetails.getData();
        String letterAddresseeType = data.getGeneralLetterWrapper().getGeneralLetterAddressee().getValue().getCode();

        return switch (letterAddresseeType) {
            case APPLICANT_SOLICITOR -> data.getAppSolicitorAddress();
            case RESPONDENT_SOLICITOR -> data.getContactDetailsWrapper().getRespondentSolicitorAddress();
            case RESPONDENT -> data.getContactDetailsWrapper().getRespondentAddress();
            case APPLICANT -> data.getContactDetailsWrapper().getApplicantAddress();
            case OTHER_RECIPIENT -> data.getGeneralLetterWrapper().getGeneralLetterRecipientAddress();
            case INTERVENER1, INTERVENER1_SOLICITOR -> data.getIntervenerOneWrapper().getIntervenerAddress();
            case INTERVENER2, INTERVENER2_SOLICITOR -> data.getIntervenerTwoWrapper().getIntervenerAddress();
            case INTERVENER3, INTERVENER3_SOLICITOR -> data.getIntervenerThreeWrapper().getIntervenerAddress();
            case INTERVENER4, INTERVENER4_SOLICITOR -> data.getIntervenerFourWrapper().getIntervenerAddress();
            default -> null;
        };
    }
}
