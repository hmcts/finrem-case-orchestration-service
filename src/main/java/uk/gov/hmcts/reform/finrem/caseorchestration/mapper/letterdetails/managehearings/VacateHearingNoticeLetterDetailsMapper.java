package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.managehearings;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.CourtDetailsConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.Hearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CourtDetailsTemplateFields;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.DocumentTemplateDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.managehearings.HearingNoticeLetterDetails;

import java.time.LocalDate;

@Component
public class VacateHearingNoticeLetterDetailsMapper extends AbstractManageHearingsLetterMapper {

    public VacateHearingNoticeLetterDetailsMapper(CourtDetailsConfiguration courtDetailsConfiguration,
                                                  ObjectMapper objectMapper) {
        super(objectMapper, courtDetailsConfiguration);
    }

    @Override
    public DocumentTemplateDetails buildDocumentTemplateDetails(FinremCaseDetails caseDetails) {
        FinremCaseData caseData = caseDetails.getData();
        Hearing hearing = getWorkingHearing(caseData);

        CourtDetailsTemplateFields courtTemplateFields =
            buildCourtDetailsTemplateFields(caseData.getSelectedHearingCourt());

        return HearingNoticeLetterDetails.builder()
            .ccdCaseNumber(caseDetails.getId().toString())
            .applicantName(caseData.getFullApplicantName())
            .respondentName(caseData.getRespondentFullName())
            .letterDate(LocalDate.now().toString())
            .hearingType(hearing.getHearingType().getId())
            .hearingDate(hearing.getHearingDate().toString())
            .hearingTime(hearing.getHearingTime())
            .courtDetails(courtTemplateFields)
            .hearingVenue(courtTemplateFields.getCourtContactDetailsAsOneLineAddressString())
            .typeOfApplication(getDefaultTypeOfApplicationIfNotPresent(caseData))
            .civilPartnership(YesOrNo.getYesOrNo(caseDetails.getData().getCivilPartnership()))
            .vacateHearingReasons("A reason to vacate") // implemented following DFR-3907
            .build();
    }
}
