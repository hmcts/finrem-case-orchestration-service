package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.bulkprint;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bsp.common.model.document.CtscContactDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.ConsentedApplicationHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.CourtDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.LetterDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.CourtListWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.BasicLetterDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.citizenui.CitizenPinInPostService;

import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CTSC_POSTCODE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CTSC_PO_BOX;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CTSC_TOWN;

@Component
public class BulkPrintCoverLetterDetailsMapper extends LetterDetailsMapper {

    private final CitizenPinInPostService citizenPinInPostService;

    public BulkPrintCoverLetterDetailsMapper(ObjectMapper objectMapper,
                                             CourtDetailsMapper courtDetailsMapper,
                                             ConsentedApplicationHelper consentedApplicationHelper,
                                             CitizenPinInPostService citizenPinInPostService) {
        super(objectMapper, courtDetailsMapper, consentedApplicationHelper);
        this.citizenPinInPostService = citizenPinInPostService;
    }

    @Override
    public BasicLetterDetails buildLetterDetails(FinremCaseDetails caseDetails,
                                                 DocumentHelper.PaperNotificationRecipient recipient,
                                                 CourtListWrapper courtList) {
        BasicLetterDetails letterDetails = super.buildLetterDetails(caseDetails, recipient, courtList);
        letterDetails.setCourtContactDetails(formatCtscContactDetailsForCoversheet());
        letterDetails.setApplicantLName(caseDetails.getData().getContactDetailsWrapper().getApplicantLname());
        letterDetails.setRespondentLName(caseDetails.getData().getContactDetailsWrapper().getRespondentLname());
        letterDetails.setAccessCode(
            citizenPinInPostService.findAccessCodeForRecipient(
                caseDetails.getData(), recipient
            ).orElse(null)
        );
        return letterDetails;
    }

    private String formatCtscContactDetailsForCoversheet() {
        CtscContactDetails coversheetCtscContactDetails = CtscContactDetails.builder()
            .serviceCentre("HMCTS Financial Remedy")
            .poBox("PO BOX " + CTSC_PO_BOX)
            .town(CTSC_TOWN)
            .postcode(CTSC_POSTCODE)
            .build();

        return String.join("\n", coversheetCtscContactDetails.getServiceCentre(),
            coversheetCtscContactDetails.getPoBox(),
            coversheetCtscContactDetails.getTown(),
            coversheetCtscContactDetails.getPostcode());
    }
}
