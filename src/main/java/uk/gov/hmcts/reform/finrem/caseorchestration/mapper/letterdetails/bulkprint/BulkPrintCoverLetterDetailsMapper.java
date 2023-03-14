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

import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CTSC_POSTCODE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CTSC_PO_BOX;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CTSC_TOWN;

@Component
public class BulkPrintCoverLetterDetailsMapper extends LetterDetailsMapper {

    public BulkPrintCoverLetterDetailsMapper(ObjectMapper objectMapper,
                                             CourtDetailsMapper courtDetailsMapper,
                                             ConsentedApplicationHelper consentedApplicationHelper) {
        super(objectMapper, courtDetailsMapper, consentedApplicationHelper);
    }

    @Override
    public BasicLetterDetails buildLetterDetails(FinremCaseDetails caseDetails,
                                                 DocumentHelper.PaperNotificationRecipient recipient,
                                                 CourtListWrapper courtList) {
        BasicLetterDetails letterDetails = super.buildLetterDetails(caseDetails, recipient, courtList);
        letterDetails.setCourtContactDetails(formatCtscContactDetailsForCoversheet());

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
