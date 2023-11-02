package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.additionalhearing;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.CourtDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.AbstractLetterDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.CourtListWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CourtDetailsTemplateFields;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.AdditionalHearingDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.DocumentTemplateDetails;

import java.util.Date;

@Component
public class AdditionalHearingDetailsMapper extends AbstractLetterDetailsMapper {

    public AdditionalHearingDetailsMapper(CourtDetailsMapper courtDetailsMapper, ObjectMapper objectMapper) {
        super(courtDetailsMapper, objectMapper);
    }

    @Override
    public DocumentTemplateDetails buildDocumentTemplateDetails(FinremCaseDetails caseDetails, CourtListWrapper courtList) {
        CourtDetailsTemplateFields courtDetails = courtDetailsMapper.getCourtDetails(courtList);

        return AdditionalHearingDetails.builder()
            .ccdCaseNumber(String.valueOf(caseDetails.getId()))
            .divorceCaseNumber(caseDetails.getData().getDivorceCaseNumber())
            .applicantName(caseDetails.getData().getFullApplicantName())
            .respondentName(caseDetails.getData().getRespondentFullName())
            .courtName(courtDetails.getCourtName())
            .courtAddress(courtDetails.getCourtAddress())
            .courtEmail(courtDetails.getEmail())
            .courtPhone(courtDetails.getPhoneNumber())
            .hearingDate(String.valueOf(caseDetails.getData().getHearingDate()))
            .hearingType(caseDetails.getData().getHearingType().getId())
            .hearingVenue(courtDetails.getCourtName())
            .hearingLength(caseDetails.getData().getTimeEstimate())
            .hearingTime(caseDetails.getData().getHearingTime())
            .additionalHearingDated(new Date())
            .build();
    }
}
