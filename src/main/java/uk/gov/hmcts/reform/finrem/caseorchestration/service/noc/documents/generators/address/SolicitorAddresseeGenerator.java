package uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.documents.generators.address;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bsp.common.model.document.Addressee;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangedRepresentative;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.UpdateSolicitorDetailsService;

@Component
@RequiredArgsConstructor
@Slf4j
public class SolicitorAddresseeGenerator implements AddresseeGenerator {

    private final DocumentHelper documentHelper;
    private final UpdateSolicitorDetailsService solicitorContactDetailsService;

    public Addressee generate(CaseDetails caseDetails, ChangedRepresentative changedRepresentative) {
        log.info("In the generate addressee method for Solicitor");

        return Addressee.builder()
            .name(changedRepresentative.getName())
            .formattedAddress(documentHelper.formatAddressForLetterPrinting(
                (solicitorContactDetailsService.convertOrganisationAddressToSolicitorAddress(
                    changedRepresentative.getOrganisation().getOrganisationID())))).build();
    }
}
