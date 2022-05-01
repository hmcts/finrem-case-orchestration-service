package uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.letters.generators.address.address;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bsp.common.model.document.Addressee;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;

import java.util.Map;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_FIRST_MIDDLE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_LAST_NAME;

@Component
@RequiredArgsConstructor
@Slf4j
public class ApplicantAddresseeGenerator implements AddresseeGenerator {

    private final CaseDataService caseDataService;
    private final DocumentHelper documentHelper;

    public Addressee generate(CaseDetails caseDetails) {
        log.info("In the generate addressee method for Applicant for caseType");
        return Addressee.builder()
            .name(caseDataService.buildFullName(caseDetails.getData(), APPLICANT_FIRST_MIDDLE_NAME, APPLICANT_LAST_NAME))
            .formattedAddress(documentHelper.formatAddressForLetterPrinting((Map) caseDetails.getData().get(APPLICANT_ADDRESS))).build();
    }
}
