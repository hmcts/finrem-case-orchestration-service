package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.managehearings;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.CourtDetailsConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.VacateOrAdjournReason;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.hearings.VacateOrAdjournedHearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.hearings.VacatedOrAdjournedHearingsCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ManageHearingsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CourtDetailsTemplateFields;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.DocumentTemplateDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.managehearings.HearingNoticeLetterDetails;

import java.time.LocalDate;
import java.util.UUID;

import static java.util.Optional.ofNullable;

@Component
public class VacateOrAdjournNoticeLetterDetailsMapper extends AbstractManageHearingsLetterMapper {

    public VacateOrAdjournNoticeLetterDetailsMapper(CourtDetailsConfiguration courtDetailsConfiguration,
                                                    ObjectMapper objectMapper) {
        super(objectMapper, courtDetailsConfiguration);
    }

    @Override
    public DocumentTemplateDetails buildDocumentTemplateDetails(FinremCaseDetails caseDetails) {
        FinremCaseData caseData = caseDetails.getData();

        ManageHearingsWrapper hearingsWrapper =  caseData.getManageHearingsWrapper();
        UUID workingVacatedHearingId = hearingsWrapper.getWorkingVacatedHearingId();

        VacateOrAdjournedHearing vacatedOrAdjournedHearing =
            ofNullable(hearingsWrapper.getVacatedOrAdjournedHearingsCollectionItemById(workingVacatedHearingId))
                .map(VacatedOrAdjournedHearingsCollectionItem::getValue)
                .orElseThrow(() -> new IllegalArgumentException(
                    String.format("Could not find a vacated hearing with id %s", workingVacatedHearingId)));

        CourtDetailsTemplateFields courtTemplateFields =
            buildCourtDetailsTemplateFields(caseData.getSelectedCourtStringFromCourt(vacatedOrAdjournedHearing.getHearingCourtSelection()));

        VacateOrAdjournReason reasonEnum = vacatedOrAdjournedHearing.getVacateOrAdjournReason();
        String reasonString = VacateOrAdjournReason.OTHER.equals(reasonEnum)
            ? vacatedOrAdjournedHearing.getSpecifyOtherReason()
            : reasonEnum.getDisplayValue();

        return HearingNoticeLetterDetails.builder()
            .ccdCaseNumber(caseDetails.getId().toString())
            .applicantName(caseData.getFullApplicantName())
            .respondentName(caseData.getRespondentFullName())
            .letterDate(LocalDate.now().toString())
            .hearingType(vacatedOrAdjournedHearing.getHearingType().getId())
            .hearingDate(vacatedOrAdjournedHearing.getHearingDate().toString())
            .hearingTime(vacatedOrAdjournedHearing.getHearingTime())
            .courtDetails(courtTemplateFields)
            .hearingVenue(courtTemplateFields.getCourtContactDetailsAsOneLineAddressString())
            .typeOfApplication(getSchedule1OrMatrimonial(caseData))
            .civilPartnership(YesOrNo.getYesOrNo(caseDetails.getData().getCivilPartnership()))
            .vacateHearingReasons(reasonString)
            .vacateOrAdjournAction(vacatedOrAdjournedHearing.getHearingStatus().getDescription())
            .build();
    }
}
