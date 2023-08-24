package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.interimhearing;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.CourtDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.ContestedAbstractLetterDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDataContested;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimTypeOfHearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.CourtListWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.InterimWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.FrcCourtDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.DocumentTemplateDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.GeneralApplicationInterimHearingNoticeDetails;

import java.time.LocalDate;
import java.util.Optional;

import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService.nullToEmpty;

@Component
public class GeneralApplicationInterimHearingNoticeDetailsMapperContested extends ContestedAbstractLetterDetailsMapper {
    public GeneralApplicationInterimHearingNoticeDetailsMapperContested(CourtDetailsMapper courtDetailsMapper, ObjectMapper objectMapper) {
        super(courtDetailsMapper, objectMapper);
    }

    @Override
    public DocumentTemplateDetails buildDocumentTemplateDetails(FinremCaseDetails<FinremCaseDataContested> caseDetails,
                                                                CourtListWrapper courtList) {
        FinremCaseDataContested caseData = caseDetails.getData();

        final FrcCourtDetails courtDetails = courtDetailsMapper.getCourtDetails(courtList);
        final InterimWrapper interimWrapper = caseData.getInterimWrapper();
        return GeneralApplicationInterimHearingNoticeDetails.builder()
            .ccdCaseNumber(caseDetails.getId())
            .divorceCaseNumber(caseData.getDivorceCaseNumber())
            .applicantName(caseData.getFullApplicantName())
            .respondentName(caseData.getRespondentFullName())
            .courtDetails(courtDetails)
            .hearingVenue(courtDetails.getCourtContactDetailsAsOneLineAddressString())
            .letterDate(String.valueOf(LocalDate.now()))
            .interimHearingDate(String.valueOf(interimWrapper.getInterimHearingDate()))
            .interimHearingTime(interimWrapper.getInterimHearingTime())
            .interimHearingType(nullToEmpty(getHearingType(interimWrapper)))
            .interimAdditionalInformationAboutHearing(interimWrapper.getInterimAdditionalInformationAboutHearing())
            .interimTimeEstimate(interimWrapper.getInterimTimeEstimate())
            .build();
    }

    private String getHearingType(InterimWrapper interimWrapper) {
        return Optional.ofNullable(interimWrapper.getInterimHearingType()).map(InterimTypeOfHearing::getId).orElse(null);
    }
}
