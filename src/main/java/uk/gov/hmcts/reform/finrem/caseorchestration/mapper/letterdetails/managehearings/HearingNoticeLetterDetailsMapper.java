package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.managehearings;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.CourtDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.CourtDetailsConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.KentSurreyCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.hearings.Hearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CourtDetailsTemplateFields;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.DocumentTemplateDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.managehearings.HearingNoticeLetterDetails;

import java.time.LocalDate;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;

@Component
public class HearingNoticeLetterDetailsMapper extends AbstractManageHearingsLetterMapper {

    public HearingNoticeLetterDetailsMapper(CourtDetailsConfiguration courtDetailsConfiguration,
                                            ObjectMapper objectMapper) {
        super(objectMapper, courtDetailsConfiguration);
    }

    @Override
    public DocumentTemplateDetails buildDocumentTemplateDetails(FinremCaseDetails caseDetails) {
        FinremCaseData caseData = caseDetails.getData();
        Hearing hearing = getWorkingHearing(caseData);

        CourtDetailsTemplateFields courtTemplateFields =
            buildCourtDetailsTemplateFields(caseData.getSelectedHearingCourt(), caseData.getCcdCaseType());

        return HearingNoticeLetterDetails.builder()
            .ccdCaseNumber(caseDetails.getCaseIdAsString())
            .applicantName(caseData.getFullApplicantName())
            .respondentName(caseData.getRespondentFullName())
            .letterDate(LocalDate.now().toString())
            .hearingType(hearing.getHearingType().getId())
            .hearingDate(hearing.getHearingDate().toString())
            .hearingTime(hearing.getHearingTime())
            .hearingTimeEstimate(hearing.getHearingTimeEstimate())
            .courtDetails(courtTemplateFields)
            .hearingVenue(courtTemplateFields.getCourtContactDetailsAsOneLineAddressString())
            .attendance(hearing.getHearingMode() != null ? hearing.getHearingMode().getDisplayValue() : "")
            .additionalHearingInformation(hearing.getAdditionalHearingInformation() != null ? hearing.getAdditionalHearingInformation() : "")
            .typeOfApplication(getSchedule1OrMatrimonial(caseData))
            .civilPartnership(YesOrNo.getYesOrNo(caseDetails.getData().getCivilPartnership()))
            .build();
    }

    /**
     * Builds the court details template fields for the given court selection and case type.
     * Central FRC contact details are only set for contested cases in Kent and Surrey courts.
     *
     * @param courtSelection the selected court identifier
     * @param caseType the type of case (e.g., "Contested", "Consented")
     * @return a {@link CourtDetailsTemplateFields} object with the court details
     */
    private CourtDetailsTemplateFields buildCourtDetailsTemplateFields(String courtSelection, CaseType caseType) {
        if (courtSelection == null || courtSelection.isBlank()) {
            throw new IllegalArgumentException("courtSelection must be provided and not blank");
        }

        CourtDetails courtDetails = courtDetailsConfiguration.getCourts().get(courtSelection);

        CourtDetailsTemplateFields.CourtDetailsTemplateFieldsBuilder builder = CourtDetailsTemplateFields.builder()
            .courtName(courtDetails.getCourtName())
            .courtAddress(courtDetails.getCourtAddress())
            .phoneNumber(courtDetails.getPhoneNumber())
            .email(courtDetails.getEmail());

        if (CONTESTED.equals(caseType) && KentSurreyCourt.contains(courtSelection)) {
            builder.centralFRCCourtAddress(OrchestrationConstants.CTSC_FRC_COURT_ADDRESS)
                .centralFRCCourtEmail(OrchestrationConstants.FRC_KENT_SURREY_COURT_EMAIL_ADDRESS);
        }

        return builder.build();
    }
}
