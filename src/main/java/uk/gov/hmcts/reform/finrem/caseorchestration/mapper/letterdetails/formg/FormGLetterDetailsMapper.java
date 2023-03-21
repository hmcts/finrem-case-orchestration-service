package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.formg;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.CourtDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.AbstractLetterDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.CourtListWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.DocumentTemplateDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.FormGLetterDetails;

@Component
public class FormGLetterDetailsMapper extends AbstractLetterDetailsMapper {
    public FormGLetterDetailsMapper(CourtDetailsMapper courtDetailsMapper, ObjectMapper objectMapper) {
        super(courtDetailsMapper, objectMapper);
    }

    @Override
    public DocumentTemplateDetails buildDocumentTemplateDetails(FinremCaseDetails caseDetails, CourtListWrapper courtList) {
        FinremCaseData caseData = caseDetails.getData();
        return FormGLetterDetails.builder()
            .applicantFmName(caseData.getContactDetailsWrapper().getApplicantFmName())
            .applicantLName(caseData.getContactDetailsWrapper().getApplicantLname())
            .respondentFmName(caseData.getContactDetailsWrapper().getRespondentFmName())
            .respondentLName(caseData.getContactDetailsWrapper().getRespondentLname())
            .divorceCaseNumber(caseData.getDivorceCaseNumber())
            .courtDetails(courtDetailsMapper.getCourtDetails(courtList))
            .hearingDate(String.valueOf(caseData.getHearingDate()))
            .solicitorReference(caseData.getContactDetailsWrapper().getSolicitorReference())
            .respondentSolicitorReference(caseData.getContactDetailsWrapper().getRespondentSolicitorReference())
            .hearingTime(caseData.getHearingTime())
            .build();
    }
}
