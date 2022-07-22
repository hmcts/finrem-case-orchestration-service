package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.additionalhearing;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.CourtDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.AbstractLetterDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.DocumentTemplateDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.FrcCourtDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.wrapper.CourtListWrapper;

import java.util.Date;

@Component
public class AdditionalHearingDetailsMapper extends AbstractLetterDetailsMapper {

    public AdditionalHearingDetailsMapper(CourtDetailsMapper courtDetailsMapper, ObjectMapper objectMapper) {
        super(courtDetailsMapper, objectMapper);
    }

    @Override
    public DocumentTemplateDetails buildDocumentTemplateDetails(FinremCaseDetails caseDetails, CourtListWrapper courtList) {
        FrcCourtDetails courtDetails = courtDetailsMapper.getCourtDetails(courtList);

        return AdditionalHearingDetails.builder()
            .ccdCaseNumber(String.valueOf(caseDetails.getId()))
            .divorceCaseNumber(caseDetails.getCaseData().getDivorceCaseNumber())
            .applicantName(caseDetails.getCaseData().getFullApplicantName())
            .respondentName(caseDetails.getCaseData().getRespondentFullName())
            .courtName(courtDetails.getCourtName())
            .courtAddress(courtDetails.getCourtAddress())
            .courtEmail(courtDetails.getEmail())
            .courtPhone(courtDetails.getPhoneNumber())
            .hearingDate(String.valueOf(caseDetails.getCaseData().getHearingDate()))
            .hearingType(caseDetails.getCaseData().getHearingType().getId())
            .hearingVenue(courtDetails.getCourtName())
            .hearingLength(caseDetails.getCaseData().getTimeEstimate())
            .hearingTime(caseDetails.getCaseData().getHearingTime())
            .additionalHearingDated(new Date())
            .build();
    }
}
