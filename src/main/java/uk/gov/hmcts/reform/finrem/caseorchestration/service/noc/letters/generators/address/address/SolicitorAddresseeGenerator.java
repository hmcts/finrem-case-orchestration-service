package uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.letters.generators.address.address;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bsp.common.model.document.Addressee;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;

import java.util.Map;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_NAME;

@Component
@RequiredArgsConstructor
@Slf4j
public class SolicitorAddresseeGenerator implements AddresseeGenerator {

    private final CaseDataService caseDataService;
    private final DocumentHelper documentHelper;

    public Addressee generate(CaseDetails caseDetails) {
        boolean isConsentedApplication = caseDataService.isConsentedApplication(caseDetails);
        log.info("In the generate addressee method for Solicitor for caseType isConsented {}", isConsentedApplication);
        return Addressee.builder()
            .name(isConsentedApplication ? (caseDataService.nullToEmpty(caseDetails.getData().get(CONSENTED_SOLICITOR_NAME))) :
                caseDataService.nullToEmpty(caseDetails.getData().get(CONTESTED_SOLICITOR_NAME)))
            .formattedAddress(documentHelper.formatAddressForLetterPrinting(
                (Map) caseDetails.getData().get(isConsentedApplication ? CONSENTED_SOLICITOR_ADDRESS : CONTESTED_SOLICITOR_ADDRESS))).build();
    }
}
