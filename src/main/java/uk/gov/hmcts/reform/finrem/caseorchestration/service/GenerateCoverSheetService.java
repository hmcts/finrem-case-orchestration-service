package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.bsp.common.model.document.Addressee;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;

import java.util.Map;

import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.ADDRESSEE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.CASE_NUMBER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.CTSC_CONTACT_DETAILS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.buildCtscContactDetails;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_FIRST_MIDDLE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_LAST_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_RESPONDENT_FIRST_MIDDLE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.addressLineOneAndPostCodeAreBothNotEmpty;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.nullToEmpty;

@Service
@RequiredArgsConstructor
@Slf4j
public class GenerateCoverSheetService {

    private final GenericDocumentService genericDocumentService;
    private final DocumentConfiguration documentConfiguration;
    private final DocumentHelper documentHelper;

    private enum AddressFoundInCaseData { SOLICITOR, PARTY, NONE }

    public CaseDocument generateApplicantCoverSheet(final CaseDetails caseDetails, final String authorisationToken) {
        log.info("Generating Applicant cover sheet {} from {} for bulk print", documentConfiguration.getBulkPrintFileName(),
            documentConfiguration.getBulkPrintTemplate());

        return generateCoverSheet(caseDetails, authorisationToken, APPLICANT_ADDRESS, SOLICITOR_ADDRESS, SOLICITOR_NAME,
            APPLICANT_FIRST_MIDDLE_NAME, APPLICANT_LAST_NAME);
    }

    public CaseDocument generateRespondentCoverSheet(final CaseDetails caseDetails, final String authorisationToken) {
        log.info("Generating Respondent cover sheet {} from {} for bulk print", documentConfiguration.getBulkPrintFileName(),
            documentConfiguration.getBulkPrintTemplate());

        return generateCoverSheet(caseDetails, authorisationToken, RESPONDENT_ADDRESS, RESP_SOLICITOR_ADDRESS, RESP_SOLICITOR_NAME,
            APP_RESPONDENT_FIRST_MIDDLE_NAME, APP_RESPONDENT_LAST_NAME);
    }

    private CaseDocument generateCoverSheet(CaseDetails caseDetails, String authorisationToken, String partyAddressCcdFieldName,
                                            String solicitorAddressCcdFieldName, String solicitorNameCcdFieldName,
                                            String partyFirstMiddleNameCcdFieldName, String partyLastNameCcdFieldName) {

        CaseDetails caseDetailsCopy = documentHelper.deepCopy(caseDetails, CaseDetails.class);
        prepareCoverSheet(caseDetailsCopy, partyAddressCcdFieldName, solicitorAddressCcdFieldName, solicitorNameCcdFieldName,
            partyFirstMiddleNameCcdFieldName, partyLastNameCcdFieldName);

        return genericDocumentService.generateDocument(authorisationToken, caseDetailsCopy, documentConfiguration.getBulkPrintTemplate(),
            documentConfiguration.getBulkPrintFileName());
    }

    private void prepareCoverSheet(CaseDetails caseDetails, String partyAddressCcdFieldName,
                                   String solicitorAddressCcdFieldName, String solicitorNameCcdFieldName,
                                   String partyFirstMiddleNameCcdFieldName, String partyLastNameCcdFieldName) {
        Map<String, Object> caseData = caseDetails.getData();
        AddressFoundInCaseData addressFoundInCaseData = checkAddress(caseData, partyAddressCcdFieldName, solicitorAddressCcdFieldName);

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
            caseData.put(CTSC_CONTACT_DETAILS, buildCtscContactDetails());
            caseData.put(CASE_NUMBER, nullToEmpty(caseDetails.getId()));
        }
    }

    private AddressFoundInCaseData checkAddress(Map<String, Object> caseData, String partyAddressCcdFieldName,
                                                String solicitorAddressCcdFieldName) {
        return addressLineOneAndPostCodeAreBothNotEmpty((Map) caseData.get(solicitorAddressCcdFieldName)) ? AddressFoundInCaseData.SOLICITOR
            : addressLineOneAndPostCodeAreBothNotEmpty((Map) caseData.get(partyAddressCcdFieldName)) ? AddressFoundInCaseData.PARTY
            : AddressFoundInCaseData.NONE;
    }

    private String partyName(Object partyFirstMiddleName, Object partyLastName) {
        return StringUtils.joinWith(" ", partyFirstMiddleName, partyLastName).trim();
    }
}
