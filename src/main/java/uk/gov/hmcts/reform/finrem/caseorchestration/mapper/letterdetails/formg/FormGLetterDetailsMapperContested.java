package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.formg;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.CourtDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.ContestedAbstractLetterDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDataContested;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.CourtListWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.DocumentTemplateDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.FormGLetterDetails;

@Component
public class FormGLetterDetailsMapperContested extends ContestedAbstractLetterDetailsMapper {
    public FormGLetterDetailsMapperContested(CourtDetailsMapper courtDetailsMapper, ObjectMapper objectMapper) {
        super(courtDetailsMapper, objectMapper);
    }

    @Override
    public DocumentTemplateDetails buildDocumentTemplateDetails(FinremCaseDetails<FinremCaseDataContested> caseDetails,
                                                                CourtListWrapper courtList) {
        FinremCaseDataContested caseData = caseDetails.getData();
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
