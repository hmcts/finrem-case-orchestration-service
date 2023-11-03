package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.generalapplicationinterim;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.CourtDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.ContestedAbstractLetterDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDataContested;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.CourtListWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralApplicationWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CourtDetailsTemplateFields;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.DocumentTemplateDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.GeneralApplicationLetterDetails;

import java.time.LocalDate;

@Component
public class GeneralApplicationLetterDetailsMapperContested extends ContestedAbstractLetterDetailsMapper {

    public GeneralApplicationLetterDetailsMapperContested(CourtDetailsMapper courtDetailsMapper, ObjectMapper objectMapper) {
        super(courtDetailsMapper, objectMapper);
    }

    @Override
    public DocumentTemplateDetails buildDocumentTemplateDetails(FinremCaseDetails<FinremCaseDataContested> caseDetails,
                                                                CourtListWrapper courtList) {
        FinremCaseDataContested caseData = caseDetails.getData();
        final GeneralApplicationWrapper generalApplication = caseData.getGeneralApplicationWrapper();
        final CourtDetailsTemplateFields courtDetails = courtDetailsMapper.getCourtDetails(courtList);
        final String hearingVenue = courtDetailsMapper.getCourtDetails(caseData.getRegionWrapper()
            .getGeneralApplicationCourtList()).getCourtContactDetailsAsOneLineAddressString();

        return GeneralApplicationLetterDetails.builder()
            .ccdCaseNumber(String.valueOf(caseDetails.getId()))
            .divorceCaseNumber(caseData.getDivorceCaseNumber())
            .applicantName(caseDetails.getData().getFullApplicantName())
            .respondentName(caseDetails.getData().getRespondentFullName())
            .courtDetails(courtDetails)
            .hearingVenue(hearingVenue)
            .letterDate(String.valueOf(LocalDate.now()))
            .generalApplicationDirectionsAdditionalInformation(generalApplication
                .getGeneralApplicationDirectionsAdditionalInformation())
            .generalApplicationDirectionsHearingDate(String.valueOf(generalApplication.getGeneralApplicationDirectionsHearingDate()))
            .generalApplicationDirectionsHearingTime(generalApplication.getGeneralApplicationDirectionsHearingTime())
            .generalApplicationDirectionsHearingTimeEstimate(generalApplication.getGeneralApplicationDirectionsHearingTimeEstimate())
            .build();
    }
}
