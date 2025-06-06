package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.formc;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.CourtDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.AbstractLetterDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.CourtListWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ListForHearingWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.DocumentTemplateDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.FormCLetterDetails;

import java.time.LocalDate;

@Component
public class FormCLetterDetailsMapper extends AbstractLetterDetailsMapper {
    public FormCLetterDetailsMapper(CourtDetailsMapper courtDetailsMapper, ObjectMapper objectMapper) {
        super(courtDetailsMapper, objectMapper);
    }

    @Override
    public DocumentTemplateDetails buildDocumentTemplateDetails(FinremCaseDetails caseDetails, CourtListWrapper courtList) {
        FinremCaseData caseData = caseDetails.getData();
        ListForHearingWrapper listForHearingWrapper = caseData.getListForHearingWrapper();
        LocalDate hearingDate = listForHearingWrapper.getHearingDate();
        return FormCLetterDetails.builder()
            .applicantFmName(caseData.getContactDetailsWrapper().getApplicantFmName())
            .applicantLName(caseData.getContactDetailsWrapper().getApplicantLname())
            .respondentFmName(caseData.getContactDetailsWrapper().getRespondentFmName())
            .respondentLName(caseData.getContactDetailsWrapper().getRespondentLname())
            .divorceCaseNumber(caseData.getDivorceCaseNumber())
            .courtDetails(courtDetailsMapper.getCourtDetails(courtList))
            .hearingDate(String.valueOf(hearingDate))
            .hearingDateLess35Days(String.valueOf(hearingDate.minusDays(35)))
            .hearingDateLess28Days(String.valueOf(hearingDate.minusDays(28)))
            .hearingDateLess21Days(String.valueOf(hearingDate.minusDays(21)))
            .hearingDateLess14Days(String.valueOf(hearingDate.minusDays(14)))
            .hearingDateLess7Days(String.valueOf(hearingDate.minusDays(7)))
            .solicitorReference(caseData.getContactDetailsWrapper().getSolicitorReference())
            .respondentSolicitorReference(caseData.getContactDetailsWrapper().getRespondentSolicitorReference())
            .additionalInformationAboutHearing(caseData.getListForHearingWrapper().getAdditionalInformationAboutHearing())
            .hearingTime(listForHearingWrapper.getHearingTime())
            .timeEstimate(listForHearingWrapper.getTimeEstimate())
            .formCCreatedDate(String.valueOf(LocalDate.now()))
            .eventDatePlus21Days(String.valueOf(LocalDate.now().plusDays(21)))
            .build();
    }
}
