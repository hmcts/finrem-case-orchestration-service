package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.bsp.common.model.document.Addressee;
import uk.gov.hmcts.reform.bsp.common.model.document.CtscContactDetails;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.InvalidCaseDataException;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;

import java.util.Map;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.COURT_CONTACT_DETAILS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CTSC_POSTCODE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CTSC_PO_BOX;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CTSC_TOWN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.ADDRESSEE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.CASE_NUMBER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_FIRST_MIDDLE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_LAST_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_RESPONDENT_FIRST_MIDDLE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_RESPONDENT_FIRST_MIDDLE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_NAME;

@Service
@RequiredArgsConstructor
@Slf4j
public class GenerateCoverSheetService {

    private final GenericDocumentService genericDocumentService;
    private final DocumentConfiguration documentConfiguration;
    private final DocumentHelper documentHelper;
    private final CaseDataService caseDataService;

    public CaseDocument generateApplicantCoverSheet(final CaseDetails caseDetails, final String authorisationToken) {
        log.info("Generating Applicant cover sheet {} from {} for bulk print", documentConfiguration.getBulkPrintFileName(),
            documentConfiguration.getBulkPrintTemplate());

        return generateCoverSheet(caseDetails, authorisationToken, APPLICANT_ADDRESS,
            caseDataService.isConsentedApplication(caseDetails) ? CONSENTED_SOLICITOR_ADDRESS : CONTESTED_SOLICITOR_ADDRESS,
            caseDataService.isConsentedApplication(caseDetails) ? CONSENTED_SOLICITOR_NAME : CONTESTED_SOLICITOR_NAME,
            APPLICANT_FIRST_MIDDLE_NAME, APPLICANT_LAST_NAME,
            caseDataService.isApplicantRepresentedByASolicitor(caseDetails.getData()));
    }

    public CaseDocument generateRespondentCoverSheet(final CaseDetails caseDetails, final String authorisationToken) {
        log.info("Generating Respondent cover sheet {} from {} for bulk print", documentConfiguration.getBulkPrintFileName(),
            documentConfiguration.getBulkPrintTemplate());

        boolean isConsented = caseDataService.isConsentedApplication(caseDetails);
        return generateCoverSheet(caseDetails, authorisationToken, RESPONDENT_ADDRESS, RESP_SOLICITOR_ADDRESS, RESP_SOLICITOR_NAME,
            isConsented ? CONSENTED_RESPONDENT_FIRST_MIDDLE_NAME : CONTESTED_RESPONDENT_FIRST_MIDDLE_NAME,
            isConsented ? CONSENTED_RESPONDENT_LAST_NAME : CONTESTED_RESPONDENT_LAST_NAME,
            caseDataService.isRespondentRepresentedByASolicitor(caseDetails.getData()));
    }

    private CaseDocument generateCoverSheet(CaseDetails caseDetails, String authorisationToken, String partyAddressCcdFieldName,
                                            String solicitorAddressCcdFieldName, String solicitorNameCcdFieldName,
                                            String partyFirstMiddleNameCcdFieldName, String partyLastNameCcdFieldName,
                                            boolean isRepresentedByASolicitor) {

        CaseDetails caseDetailsCopy = documentHelper.deepCopy(caseDetails, CaseDetails.class);
        prepareCoverSheet(caseDetailsCopy, partyAddressCcdFieldName, solicitorAddressCcdFieldName, solicitorNameCcdFieldName,
            partyFirstMiddleNameCcdFieldName, partyLastNameCcdFieldName, isRepresentedByASolicitor);

        return genericDocumentService.generateDocument(authorisationToken, caseDetailsCopy, documentConfiguration.getBulkPrintTemplate(),
            documentConfiguration.getBulkPrintFileName());
    }

    private void prepareCoverSheet(CaseDetails caseDetails, String partyAddressCcdFieldName,
                                   String solicitorAddressCcdFieldName, String solicitorNameCcdFieldName,
                                   String partyFirstMiddleNameCcdFieldName, String partyLastNameCcdFieldName,
                                   boolean isRepresentedByASolicitor) {
        Map<String, Object> caseData = caseDetails.getData();
        AddressFoundInCaseData addressFoundInCaseData = checkAddress(caseData, partyAddressCcdFieldName, solicitorAddressCcdFieldName,
            isRepresentedByASolicitor);

        if (addressFoundInCaseData == AddressFoundInCaseData.NONE) {
            String offendingCcdField = isRepresentedByASolicitor ? solicitorAddressCcdFieldName : partyAddressCcdFieldName;
            throw new InvalidCaseDataException(BAD_REQUEST.value(), "CCD address field " + offendingCcdField
                + " needs to contain both first line of address and postcode");
        } else {
            boolean sendToSolicitor = addressFoundInCaseData == AddressFoundInCaseData.SOLICITOR;

            Addressee addressee = Addressee.builder()
                .name(sendToSolicitor
                    ? (String) caseData.get(solicitorNameCcdFieldName)
                    : partyName(caseData.get(partyFirstMiddleNameCcdFieldName), caseData.get(partyLastNameCcdFieldName)))
                .formattedAddress(documentHelper.formatAddressForLetterPrinting(sendToSolicitor
                    ? (Map) caseData.get(solicitorAddressCcdFieldName)
                    : (Map) caseData.get(partyAddressCcdFieldName)))
                .build();
            caseData.put(ADDRESSEE, addressee);
            caseData.put(COURT_CONTACT_DETAILS, formatCtscContactDetailsForCoversheet());
            caseData.put(CASE_NUMBER, caseDataService.nullToEmpty(caseDetails.getId()));
        }
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

    private AddressFoundInCaseData checkAddress(Map<String, Object> caseData, String partyAddressCcdFieldName,
                                                String solicitorAddressCcdFieldName, boolean isRepresentedByASolicitor) {
        return isRepresentedByASolicitor && caseDataService.addressLineOneAndPostCodeAreBothNotEmpty((Map) caseData.get(solicitorAddressCcdFieldName))
            ? AddressFoundInCaseData.SOLICITOR
            : caseDataService.addressLineOneAndPostCodeAreBothNotEmpty((Map) caseData.get(partyAddressCcdFieldName)) ? AddressFoundInCaseData.PARTY
            : AddressFoundInCaseData.NONE;
    }

    private String partyName(Object partyFirstMiddleName, Object partyLastName) {
        return StringUtils.joinWith(" ", partyFirstMiddleName, partyLastName).trim();
    }

    private enum AddressFoundInCaseData {
        SOLICITOR, PARTY, NONE
    }
}
