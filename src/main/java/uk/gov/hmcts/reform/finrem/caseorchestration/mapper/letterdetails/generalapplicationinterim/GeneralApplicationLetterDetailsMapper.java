package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.generalapplicationinterim;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.CourtDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.AbstractLetterDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.DocumentTemplateDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.FrcCourtDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.wrapper.CourtListWrapper;

import java.time.LocalDate;

@Component
public class GeneralApplicationLetterDetailsMapper extends AbstractLetterDetailsMapper {

    public GeneralApplicationLetterDetailsMapper(CourtDetailsMapper courtDetailsMapper, ObjectMapper objectMapper) {
        super(courtDetailsMapper, objectMapper);
    }

    @Override
    public DocumentTemplateDetails buildDocumentTemplateDetails(FinremCaseDetails caseDetails,
                                                                CourtListWrapper courtList) {
        final FrcCourtDetails courtDetails = courtDetailsMapper.getCourtDetails(courtList);

        return GeneralApplicationLetterDetails.builder()
            .ccdCaseNumber(String.valueOf(caseDetails.getId()))
            .applicantName(caseDetails.getCaseData().getFullApplicantName())
            .respondentName(caseDetails.getCaseData().getRespondentFullName())
            .courtDetails(courtDetails)
            .hearingVenue(courtDetails.getCourtContactDetailsAsOneLineAddressString())
            .letterDate(String.valueOf(LocalDate.now()))
            .build();
    }
}
