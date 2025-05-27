package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.managehearings;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.CourtDetailsConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.CourtDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.AbstractLetterDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.Hearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.CourtListWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CourtDetailsTemplateFields;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.DocumentTemplateDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.ManageHearingsNoticeDetails;

import java.time.LocalDate;

import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseHearingFunctions.buildHearingFrcCourtDetails;

@Component
public class HearingNoticeMapper extends AbstractLetterDetailsMapper {

    private final CourtDetailsConfiguration courtDetailsConfiguration;

    public HearingNoticeMapper(CourtDetailsMapper courtDetailsMapper,
                               CourtDetailsConfiguration courtDetailsConfiguration,
                               ObjectMapper objectMapper) {
        super(courtDetailsMapper, objectMapper);
        this.courtDetailsConfiguration = courtDetailsConfiguration;
    }

    @Override
    public DocumentTemplateDetails buildDocumentTemplateDetails(FinremCaseDetails caseDetails, CourtListWrapper courtList) {
        FinremCaseData caseData = caseDetails.getData();
        Hearing hearing = caseData.getManageHearingsWrapper().getWorkingHearing();

        return ManageHearingsNoticeDetails.builder()
            .ccdCaseNumber(caseDetails.getId().toString())
            .applicantName(caseDetails.getData().getFullApplicantName())
            .respondentName(caseDetails.getData().getRespondentFullName())
            .letterDate(LocalDate.now().toString())
            .hearingType(hearing.getHearingType().toString())
            .hearingDate(hearing.getHearingDate().toString())
            .hearingTime(hearing.getHearingTime())
            .hearingTimeEstimate(hearing.getHearingTimeEstimate())
            .courtDetails(buildHearingFrcCourtDetails(caseData))
            .hearingVenue(courtDetailsConfiguration.getCourts().get(caseData.getSelectedHearingCourt()).getCourtAddress())
            .attendance(hearing.getHearingMode() != null ? hearing.getHearingMode().getDisplayValue() : "")
            .additionalHearingInformation(hearing.getAdditionalHearingInformation() != null ? hearing.getAdditionalHearingInformation() : "")
            .build();
    }
}
