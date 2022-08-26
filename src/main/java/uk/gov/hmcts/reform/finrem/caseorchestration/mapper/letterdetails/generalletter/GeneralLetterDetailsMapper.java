package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.generalletter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bsp.common.model.document.Addressee;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.CourtDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.AbstractLetterDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.DocumentTemplateDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.GeneralLetterDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.Address;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.GeneralLetterAddressToType;
import uk.gov.hmcts.reform.finrem.ccd.domain.wrapper.CourtListWrapper;

import java.util.Date;
import java.util.List;

import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.buildCtscContactDetails;
import static uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.AddresseeGeneratorHelper.ADDRESS_MAP;
import static uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.AddresseeGeneratorHelper.NAME_MAP;
import static uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.AddresseeGeneratorHelper.formatAddressForLetterPrinting;
import static uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.AddresseeGeneratorHelper.getAddressToCaseDataMapping;

@Component
public class GeneralLetterDetailsMapper extends AbstractLetterDetailsMapper {

    public GeneralLetterDetailsMapper(CourtDetailsMapper courtDetailsMapper, ObjectMapper objectMapper) {
        super(courtDetailsMapper, objectMapper);
    }

    @Override
    public DocumentTemplateDetails buildDocumentTemplateDetails(FinremCaseDetails caseDetails, CourtListWrapper courtList) {

        FinremCaseData caseData = caseDetails.getCaseData();
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
        FinremCaseData caseData = caseDetails.getCaseData();
        GeneralLetterAddressToType recipient = caseData.getGeneralLetterWrapper().getGeneralLetterAddressTo();
        return Addressee.builder()
                .name((String) getAddressToCaseDataMapping(caseData).get(NAME_MAP).get(recipient))
                .formattedAddress(getFormattedAddress(caseData, recipient))
                .build();
    }

    private String getFormattedAddress(FinremCaseData caseData, GeneralLetterAddressToType recipient) {
        return formatAddressForLetterPrinting((Address) getAddressToCaseDataMapping(caseData).get(ADDRESS_MAP).get(recipient));
    }

    private String getSolicitorReference(FinremCaseData caseData) {
        GeneralLetterAddressToType recipient = caseData.getGeneralLetterWrapper().getGeneralLetterAddressTo();

        if (List.of(GeneralLetterAddressToType.RESPONDENT, GeneralLetterAddressToType.OTHER).contains(recipient)) {
            return StringUtils.EMPTY;
        }

        return recipient.equals(GeneralLetterAddressToType.RESPONDENT_SOLICITOR)
            ? caseData.getContactDetailsWrapper().getRespondentSolicitorReference()
            : caseData.getContactDetailsWrapper().getSolicitorReference();
    }
}
