package uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.documents.generators.address;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bsp.common.model.document.Addressee;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangedRepresentative;

import java.util.Map;

import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CASE_TYPE_ID_CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService.nullToEmpty;

@Component
@RequiredArgsConstructor
@Slf4j
public class SolicitorAddresseeGenerator implements AddresseeGenerator {

    private final DocumentHelper documentHelper;

    public Addressee generate(CaseDetails caseDetails,
                              ChangedRepresentative changedRepresentative,
                              String party) {
        log.info("In the generate addressee method for Solicitor");

        return Addressee.builder()
            .name(changedRepresentative.getName())
            .formattedAddress(documentHelper.formatAddressForLetterPrinting(
                ((Map) caseDetails.getData().get(getSolicitorAddressKey(party, caseDetails))))).build();
    }

    private String getSolicitorAddressKey(String party, CaseDetails caseDetails) {
        String applicantSolicitorAddressKey = getApplicantSolicitorAddressKey(caseDetails);
        return party.equalsIgnoreCase(APPLICANT)
            ? applicantSolicitorAddressKey : RESP_SOLICITOR_ADDRESS;
    }

    private String getApplicantSolicitorAddressKey(CaseDetails caseDetails) {
        return CASE_TYPE_ID_CONSENTED.equalsIgnoreCase(nullToEmpty(caseDetails.getCaseTypeId()))
            ? CONSENTED_SOLICITOR_ADDRESS : CONTESTED_SOLICITOR_ADDRESS;
    }
}
