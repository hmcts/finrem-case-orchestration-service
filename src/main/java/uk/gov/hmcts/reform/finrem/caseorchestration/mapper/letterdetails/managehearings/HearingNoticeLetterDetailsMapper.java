package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.managehearings;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.CourtDetailsConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.Hearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ManageHearingsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CourtDetailsTemplateFields;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.DocumentTemplateDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.managehearings.HearingNoticeLetterDetails;

import java.time.LocalDate;
import java.util.Optional;

@Component
public class HearingNoticeLetterDetailsMapper extends AbstractManageHearingsLetterMapper {

    public HearingNoticeLetterDetailsMapper(CourtDetailsConfiguration courtDetailsConfiguration,
                                            ObjectMapper objectMapper) {
        super(objectMapper, courtDetailsConfiguration);
    }

    @Override
    public DocumentTemplateDetails buildDocumentTemplateDetails(FinremCaseDetails caseDetails) {
        FinremCaseData caseData = caseDetails.getData();

        Hearing hearing = Optional.ofNullable(caseData.getManageHearingsWrapper())
            .map(ManageHearingsWrapper::getWorkingHearing)
            .orElseThrow(() -> new IllegalArgumentException("Working hearing is null"));

        CourtDetailsTemplateFields courtTemplateFields =
            buildCourtDetailsTemplateFields(caseData.getSelectedHearingCourt());

        return HearingNoticeLetterDetails.builder()
            .ccdCaseNumber(caseDetails.getId().toString())
            .applicantName(caseData.getFullApplicantName())
            .respondentName(caseData.getRespondentFullName())
            .letterDate(LocalDate.now().toString())
            .hearingType(hearing.getHearingType().toString())
            .hearingDate(hearing.getHearingDate().toString())
            .hearingTime(hearing.getHearingTime())
            .hearingTimeEstimate(hearing.getHearingTimeEstimate())
            .courtDetails(courtTemplateFields)
            .hearingVenue(courtTemplateFields.getCourtContactDetailsAsOneLineAddressString())
            .attendance(hearing.getHearingMode() != null ? hearing.getHearingMode().getDisplayValue() : "")
            .additionalHearingInformation(hearing.getAdditionalHearingInformation() != null ? hearing.getAdditionalHearingInformation() : "")
            .build();
    }
}
