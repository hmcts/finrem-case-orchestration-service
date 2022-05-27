package uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.documents.generators.address;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bsp.common.model.document.Addressee;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangedRepresentative;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;

import java.util.Map;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_RESPONDENT_FIRST_MIDDLE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_RESPONDENT_FIRST_MIDDLE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_ADDRESS;

@Component
@RequiredArgsConstructor
@Slf4j
public class RespondentAddresseeGenerator implements AddresseeGenerator {

    private final CaseDataService caseDataService;
    private final DocumentHelper documentHelper;

    public Addressee generate(CaseDetails caseDetails, ChangedRepresentative changedRepresentative, String party) {
        boolean isConsentedApplication = caseDataService.isConsentedApplication(caseDetails);
        log.info("In the generate addressee method for Respondent for caseType isConsented {}", isConsentedApplication);
        return Addressee.builder()
            .name(caseDataService.buildFullName(caseDetails.getData(),
                isConsentedApplication ? CONSENTED_RESPONDENT_FIRST_MIDDLE_NAME : CONTESTED_RESPONDENT_FIRST_MIDDLE_NAME,
                isConsentedApplication ? CONSENTED_RESPONDENT_LAST_NAME : CONTESTED_RESPONDENT_LAST_NAME))
            .formattedAddress(documentHelper.formatAddressForLetterPrinting((Map) caseDetails.getData().get(RESPONDENT_ADDRESS))).build();
    }
}
