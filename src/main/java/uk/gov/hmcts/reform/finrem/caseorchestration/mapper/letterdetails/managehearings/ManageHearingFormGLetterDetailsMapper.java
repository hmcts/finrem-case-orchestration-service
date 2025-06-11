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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.FormGLetterDetails;

@Component
public class ManageHearingFormGLetterDetailsMapper extends AbstractManageHearingsLetterMapper {

    public ManageHearingFormGLetterDetailsMapper(ObjectMapper objectMapper,
                                                 CourtDetailsConfiguration courtDetailsConfiguration) {
        super(objectMapper, courtDetailsConfiguration);
    }

    @Override
    public DocumentTemplateDetails buildDocumentTemplateDetails(FinremCaseDetails caseDetails) {
        FinremCaseData caseData = caseDetails.getData();
        Hearing hearing = getWorkingHearing(caseData);
        ContactDetailsWrapper contactDetails = caseData.getContactDetailsWrapper();

        CourtDetailsTemplateFields courtTemplateFields =
            buildCourtDetailsTemplateFields(caseData.getSelectedHearingCourt());

        return FormGLetterDetails.builder()
            .applicantFmName(contactDetails.getApplicantFmName())
            .applicantLName(contactDetails.getApplicantLname())
            .respondentFmName(contactDetails.getRespondentFmName())
            .respondentLName(contactDetails.getRespondentLname())
            .caseNumber(caseData.getDivorceCaseNumber())
            .courtDetails(courtTemplateFields)
            .hearingDate(String.valueOf(hearing.getHearingDate()))
            .solicitorReference(contactDetails.getSolicitorReference())
            .respondentSolicitorReference(contactDetails.getRespondentSolicitorReference())
            .hearingTime(hearing.getHearingTime())
            .build();
    }
}
