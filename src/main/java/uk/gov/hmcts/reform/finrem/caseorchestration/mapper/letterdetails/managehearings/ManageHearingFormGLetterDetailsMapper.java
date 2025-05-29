package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.managehearings;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.client.DataStoreClient;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.CourtDetailsConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.CourtDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.AbstractLetterDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.Hearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.CourtListWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CourtDetailsTemplateFields;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.DocumentTemplateDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.FormGLetterDetails;

import java.time.LocalDate;

@Component
public class ManageHearingFormGLetterDetailsMapper extends AbstractLetterDetailsMapper {

    private final CourtDetailsConfiguration courtDetailsConfiguration;
    private final DataStoreClient dataStoreClient;

    public ManageHearingFormGLetterDetailsMapper(CourtDetailsMapper courtDetailsMapper, ObjectMapper objectMapper, CourtDetailsConfiguration courtDetailsConfiguration, DataStoreClient dataStoreClient) {
        super(courtDetailsMapper, objectMapper);
        this.courtDetailsConfiguration = courtDetailsConfiguration;
        this.dataStoreClient = dataStoreClient;
    }

    @Override
    public DocumentTemplateDetails buildDocumentTemplateDetails(FinremCaseDetails caseDetails, CourtListWrapper courtList) {
        FinremCaseData caseData = caseDetails.getData();
        Hearing hearing = caseData.getManageHearingsWrapper().getWorkingHearing();
        ContactDetailsWrapper contactDetails = caseData.getContactDetailsWrapper();

        CourtDetailsTemplateFields courtTemplateFields =
            courtDetailsConfiguration.buildCourtDetailsTemplateFields(caseData.getSelectedHearingCourt());

        return FormGLetterDetails.builder()
            .applicantFmName(contactDetails.getApplicantFmName())
            .applicantLName(contactDetails.getApplicantLname())
            .respondentFmName(contactDetails.getRespondentFmName())
            .respondentLName(contactDetails.getRespondentLname())
            .divorceCaseNumber(caseData.getDivorceCaseNumber())
            .caseNumber(caseDetails.getId().toString())
            .courtDetails(courtTemplateFields)
            .hearingDate(String.valueOf(hearing.getHearingDate()))
            .solicitorReference(contactDetails.getSolicitorReference())
            .respondentSolicitorReference(contactDetails.getRespondentSolicitorReference())
            .hearingTime(hearing.getHearingTime())
            .build();
    }
}
