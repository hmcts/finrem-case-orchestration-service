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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CourtDetailsTemplateFields;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.DocumentTemplateDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.managehearings.FormCLetterDetails;

import java.time.LocalDate;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;

@Component
public class ManageHearingFormCLetterDetailsMapper extends AbstractManageHearingsLetterMapper {

    public ManageHearingFormCLetterDetailsMapper(CourtDetailsConfiguration courtDetailsConfiguration,
                                                 ObjectMapper objectMapper) {
        super(objectMapper, courtDetailsConfiguration);
    }

    @Override
    public DocumentTemplateDetails buildDocumentTemplateDetails(FinremCaseDetails caseDetails) {
        FinremCaseData caseData = caseDetails.getData();
        Hearing hearing = getWorkingHearing(caseData);
        ContactDetailsWrapper contactDetails = caseData.getContactDetailsWrapper();

        CourtDetailsTemplateFields courtTemplateFields =
            buildCourtDetailsTemplateFields(caseData.getSelectedHearingCourt(), caseData.getCcdCaseType());

        LocalDate hearingDate = hearing.getHearingDate();
        LocalDate formCCreatedDate = LocalDate.now();

        return FormCLetterDetails.builder()
            .caseNumber(caseDetails.getCaseIdAsString())
            .courtDetails(courtTemplateFields)
            .applicantFmName(contactDetails.getApplicantFmName())
            .applicantLName(contactDetails.getApplicantLname())
            .respondentFmName(contactDetails.getRespondentFmName())
            .respondentLName(contactDetails.getRespondentLname())
            .solicitorReference(contactDetails.getSolicitorReference())
            .rSolicitorReference(contactDetails.getRespondentSolicitorReference())
            .hearingDate(hearing.getHearingDate().toString())
            .hearingTime(hearing.getHearingTime())
            .timeEstimate(hearing.getHearingTimeEstimate())
            .hearingDateLess35Days(String.valueOf(hearingDate.minusDays(35)))
            .hearingDateLess28Days(String.valueOf(hearingDate.minusDays(28)))
            .hearingDateLess21Days(String.valueOf(hearingDate.minusDays(21)))
            .hearingDateLess14Days(String.valueOf(hearingDate.minusDays(14)))
            .hearingDateLess7Days(String.valueOf(hearingDate.minusDays(7)))
            .attendance(hearing.getHearingMode() != null ? hearing.getHearingMode().getDisplayValue() : "")
            .additionalInformationAboutHearing(hearing.getAdditionalHearingInformation() != null ? hearing.getAdditionalHearingInformation() : "")
            .formCCreatedDate(String.valueOf(formCCreatedDate))
            .formCCreatedDatePlus28Days(String.valueOf(formCCreatedDate.plusDays(28)))
            .eventDatePlus21Days(String.valueOf(formCCreatedDate.plusDays(21)))
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
