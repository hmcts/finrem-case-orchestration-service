package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.interimhearing;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.CourtDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.AbstractLetterDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.DocumentTemplateDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.FrcCourtDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.InterimTypeOfHearing;
import uk.gov.hmcts.reform.finrem.ccd.domain.wrapper.CourtListWrapper;
import uk.gov.hmcts.reform.finrem.ccd.domain.wrapper.InterimWrapper;

import java.time.LocalDate;
import java.util.Optional;

import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService.nullToEmpty;

@Component
public class GeneralApplicationInterimHearingNoticeDetailsMapper extends AbstractLetterDetailsMapper {
    public GeneralApplicationInterimHearingNoticeDetailsMapper(CourtDetailsMapper courtDetailsMapper, ObjectMapper objectMapper) {
        super(courtDetailsMapper, objectMapper);
    }

    @Override
    public DocumentTemplateDetails buildDocumentTemplateDetails(FinremCaseDetails caseDetails, CourtListWrapper courtList) {
        FinremCaseData caseData = caseDetails.getCaseData();

        final FrcCourtDetails courtDetails = courtDetailsMapper.getCourtDetails(caseData.getRegionWrapper().getInterimCourtList());
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
