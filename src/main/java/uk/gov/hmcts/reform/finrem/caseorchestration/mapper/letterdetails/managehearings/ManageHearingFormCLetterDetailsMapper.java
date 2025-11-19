package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.managehearings;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.CourtDetailsConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.Hearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CourtDetailsTemplateFields;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.DocumentTemplateDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.managehearings.FormCLetterDetails;

import java.time.LocalDate;

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
            buildCourtDetailsTemplateFields(caseData.getSelectedHearingCourt());

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
            .build();
    }
}
