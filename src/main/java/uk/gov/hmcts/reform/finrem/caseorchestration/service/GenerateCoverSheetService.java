package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.bsp.common.model.document.Addressee;
import uk.gov.hmcts.reform.bsp.common.model.document.CtscContactDetails;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.FrcCourtDetails;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.COURT_DETAILS_ADDRESS_KEY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.COURT_DETAILS_EMAIL_KEY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.COURT_DETAILS_NAME_KEY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.COURT_DETAILS_PHONE_KEY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseHearingFunctions.getCourtDetailsString;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseHearingFunctions.getSelectedCourt;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.addressLineOneAndPostCodeAreBothNotEmpty;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.isConsentedApplication;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.isContestedApplication;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.nullToEmpty;

@Service
@RequiredArgsConstructor
@Slf4j
public class GenerateCoverSheetService {

    private final GenericDocumentService genericDocumentService;
    private final DocumentConfiguration documentConfiguration;
    private final DocumentHelper documentHelper;
    private final ObjectMapper objectMapper;

    private enum AddressFoundInCaseData { SOLICITOR, PARTY, NONE }

    public CaseDocument generateApplicantCoverSheet(final CaseDetails caseDetails, final String authorisationToken) {
        log.info("Generating Applicant cover sheet {} from {} for bulk print", documentConfiguration.getBulkPrintFileName(),
            documentConfiguration.getBulkPrintTemplate());

        return generateCoverSheet(caseDetails, authorisationToken, APPLICANT_ADDRESS,
            isConsentedApplication(caseDetails) ? CONSENTED_SOLICITOR_ADDRESS : CONTESTED_SOLICITOR_ADDRESS,
            isConsentedApplication(caseDetails) ? CONSENTED_SOLICITOR_NAME : CONTESTED_SOLICITOR_NAME,
            APPLICANT_FIRST_MIDDLE_NAME, APPLICANT_LAST_NAME, CommonFunction.isApplicantRepresentedByASolicitor(caseDetails.getData()));
    }

    public CaseDocument generateRespondentCoverSheet(final CaseDetails caseDetails, final String authorisationToken) {
        log.info("Generating Respondent cover sheet {} from {} for bulk print", documentConfiguration.getBulkPrintFileName(),
            documentConfiguration.getBulkPrintTemplate());

        boolean isConsented = CommonFunction.isConsentedApplication(caseDetails);
        return generateCoverSheet(caseDetails, authorisationToken, RESPONDENT_ADDRESS, RESP_SOLICITOR_ADDRESS, RESP_SOLICITOR_NAME,
            isConsented ? CONSENTED_RESPONDENT_FIRST_MIDDLE_NAME : CONTESTED_RESPONDENT_FIRST_MIDDLE_NAME,
            isConsented ? CONSENTED_RESPONDENT_LAST_NAME : CONTESTED_RESPONDENT_LAST_NAME,
            CommonFunction.isRespondentRepresentedByASolicitor(caseDetails.getData()));
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

        if (addressFoundInCaseData != AddressFoundInCaseData.NONE) {
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
            caseData.put(COURT_CONTACT_DETAILS, getCourtContactDetails(caseDetails));
            caseData.put(CASE_NUMBER, nullToEmpty(caseDetails.getId()));
        }
    }

    private String getCourtContactDetails(CaseDetails caseDetails) {
        if (isContestedApplication(caseDetails)) {
            return formatFrcContactDetailsForCoversheet(caseDetails);
        }
        return formatCtscContactDetailsForCoversheet();
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

    private String formatFrcContactDetailsForCoversheet(CaseDetails caseDetails) {

        Map<String, Object> courtDetailsMap;
        try {
            courtDetailsMap  = objectMapper.readValue(getCourtDetailsString(), HashMap.class);
        } catch (IOException | NullPointerException e) {
            throw new RuntimeException("Could not retrieve court contact information for printing. ",e);
        }

        Map<String, Object> data = caseDetails.getData();
        Map<String, Object> courtDetails = (Map<String, Object>) courtDetailsMap.get(data.get(getSelectedCourt(data)));

        FrcCourtDetails coversheetFrcContactDetails = FrcCourtDetails.builder()
            .courtName((String) courtDetails.get(COURT_DETAILS_NAME_KEY))
            .courtAddress((String) courtDetails.get(COURT_DETAILS_ADDRESS_KEY))
            .phoneNumber((String) courtDetails.get(COURT_DETAILS_PHONE_KEY))
            .email((String) courtDetails.get(COURT_DETAILS_EMAIL_KEY))
            .build();

        return String.join("\n", coversheetFrcContactDetails.getCourtName(),
            coversheetFrcContactDetails.getCourtAddress(),
            coversheetFrcContactDetails.getPhoneNumber(),
            coversheetFrcContactDetails.getEmail());
    }

    private AddressFoundInCaseData checkAddress(Map<String, Object> caseData, String partyAddressCcdFieldName,
                                                String solicitorAddressCcdFieldName, boolean isRepresentedByASolicitor) {
        return isRepresentedByASolicitor && addressLineOneAndPostCodeAreBothNotEmpty((Map) caseData.get(solicitorAddressCcdFieldName))
            ? AddressFoundInCaseData.SOLICITOR
            : addressLineOneAndPostCodeAreBothNotEmpty((Map) caseData.get(partyAddressCcdFieldName)) ? AddressFoundInCaseData.PARTY
            : AddressFoundInCaseData.NONE;
    }

    private String partyName(Object partyFirstMiddleName, Object partyLastName) {
        return StringUtils.joinWith(" ", partyFirstMiddleName, partyLastName).trim();
    }
}
