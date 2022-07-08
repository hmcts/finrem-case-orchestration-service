package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.bulkprint;

import uk.gov.hmcts.reform.bsp.common.model.document.CtscContactDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.LetterDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.LetterDetailsMapper;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.wrapper.CourtListWrapper;

import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CTSC_POSTCODE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CTSC_PO_BOX;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CTSC_TOWN;


public class BulkPrintCoverLetterDetailsMapper extends LetterDetailsMapper {

    @Override
    public LetterDetails buildLetterDetails(FinremCaseDetails caseDetails,
                                            DocumentHelper.PaperNotificationRecipient recipient,
                                            CourtListWrapper courtList) {
        BulkPrintLetterDetails letterDetails =
            (BulkPrintLetterDetails) super.buildLetterDetails(caseDetails, recipient, courtList);
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
