package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.generalletter;

import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.hmcts.reform.bsp.common.model.document.Addressee;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.CourtDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.AbstractLetterDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.AddresseeGeneratorHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.DocumentTemplateDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.GeneralLetterAddressToType;
import uk.gov.hmcts.reform.finrem.ccd.domain.wrapper.CourtListWrapper;

import java.util.Date;
import java.util.List;

import static uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.AddresseeGeneratorHelper.formatAddressForLetterPrinting;

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
            .build();
    }

    private Addressee getAddressee(FinremCaseDetails caseDetails) {
        FinremCaseData caseData = caseDetails.getCaseData();
        GeneralLetterAddressToType recipient = caseData.getGeneralLetterWrapper().getGeneralLetterAddressTo();

        if (recipient.equals(GeneralLetterAddressToType.APPLICANT_SOLICITOR)) {
            return AddresseeGeneratorHelper.generateAddressee(caseDetails, DocumentHelper.PaperNotificationRecipient.APPLICANT);
        } else if (recipient.equals(GeneralLetterAddressToType.OTHER)) {
            return getOtherAddressee(caseData);
        }

        return AddresseeGeneratorHelper.generateAddressee(caseDetails, DocumentHelper.PaperNotificationRecipient.RESPONDENT);
    }

    private Addressee getOtherAddressee(FinremCaseData caseData) {
        return Addressee.builder()
            .name(caseData.getGeneralLetterWrapper().getGeneralLetterRecipient())
            .formattedAddress(formatAddressForLetterPrinting(caseData.getGeneralLetterWrapper()
                .getGeneralLetterRecipientAddress()))
            .build();
    }

    private String getSolicitorReference(FinremCaseData caseData) {
        GeneralLetterAddressToType recipient = caseData.getGeneralLetterWrapper().getGeneralLetterAddressTo();

        if (List.of(GeneralLetterAddressToType.RESPONDENT, GeneralLetterAddressToType.OTHER).contains(recipient)) {
            return "";
        }

        return recipient.equals(GeneralLetterAddressToType.RESPONDENT_SOLICITOR)
            ? caseData.getContactDetailsWrapper().getRespondentSolicitorReference()
            : caseData.getContactDetailsWrapper().getSolicitorReference();
    }
}
