package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.formc;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.CourtDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.ContestedAbstractLetterDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDataContested;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.CourtListWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.DocumentTemplateDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.FormCLetterDetails;


import java.time.LocalDate;

@Component
public class FormCLetterDetailsMapperContested extends ContestedAbstractLetterDetailsMapper {
    public FormCLetterDetailsMapperContested(CourtDetailsMapper courtDetailsMapper, ObjectMapper objectMapper) {
        super(courtDetailsMapper, objectMapper);
    }

    @Override
    public DocumentTemplateDetails buildDocumentTemplateDetails(FinremCaseDetails<FinremCaseDataContested> caseDetails,
                                                                CourtListWrapper courtList) {
        FinremCaseDataContested caseData = caseDetails.getData();
        LocalDate hearingDate = caseData.getHearingDate();
        return FormCLetterDetails.builder()
            .applicantFmName(caseData.getContactDetailsWrapper().getApplicantFmName())
            .applicantLName(caseData.getContactDetailsWrapper().getApplicantLname())
            .respondentFmName(caseData.getContactDetailsWrapper().getRespondentFmName())
            .respondentLName(caseData.getContactDetailsWrapper().getRespondentLname())
            .divorceCaseNumber(caseData.getDivorceCaseNumber())
            .courtDetails(courtDetailsMapper.getCourtDetails(courtList))
            .hearingDate(String.valueOf(hearingDate))
            .hearingDateLess35Days(String.valueOf(hearingDate.minusDays(35)))
            .hearingDateLess14Days(String.valueOf(hearingDate.minusDays(14)))
            .solicitorReference(caseData.getContactDetailsWrapper().getSolicitorReference())
            .respondentSolicitorReference(caseData.getContactDetailsWrapper().getRespondentSolicitorReference())
            .additionalInformationAboutHearing(caseData.getAdditionalInformationAboutHearing())
            .hearingTime(caseData.getHearingTime())
            .timeEstimate(caseData.getTimeEstimate())
            .formCCreatedDate(String.valueOf(LocalDate.now()))
            .eventDatePlus21Days(String.valueOf(LocalDate.now().plusDays(21)))
            .build();
    }
}
