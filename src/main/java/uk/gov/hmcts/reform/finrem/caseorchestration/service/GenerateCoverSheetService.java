package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintCoverSheet;

import java.util.Map;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_FIRST_MIDDLE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_LAST_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_RESPONDENT_FIRST_MIDDLE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BULK_PRINT_COVER_SHEET;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_SOLICITOR_FIRM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_FIRM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.addressLineOneAndPostCodeAreBothNotEmpty;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.nullToEmpty;

@Service
@RequiredArgsConstructor
@Slf4j
public class GenerateCoverSheetService {

    private final GenericDocumentService genericDocumentService;
    private final DocumentConfiguration documentConfiguration;
    
    private enum AddressFoundInCaseData { SOLICITOR, PARTY, NONE }

    public CaseDocument generateApplicantCoverSheet(final CaseDetails caseDetails, final String authorisationToken) {
        log.info("Generating Applicant cover sheet {} from {} for bulk print", documentConfiguration.getBulkPrintFileName(),
            documentConfiguration.getBulkPrintTemplate());

        prepareCoverSheet(caseDetails, APPLICANT_ADDRESS, SOLICITOR_ADDRESS, SOLICITOR_NAME, SOLICITOR_FIRM,
            APPLICANT_FIRST_MIDDLE_NAME, APPLICANT_LAST_NAME);

        return genericDocumentService.generateDocument(authorisationToken, caseDetails, documentConfiguration.getBulkPrintTemplate(),
            documentConfiguration.getBulkPrintFileName());
    }

    public CaseDocument generateRespondentCoverSheet(final CaseDetails caseDetails, final String authorisationToken) {
        log.info("Generating Respondent cover sheet {} from {} for bulk print", documentConfiguration.getBulkPrintFileName(),
            documentConfiguration.getBulkPrintTemplate());

        prepareCoverSheet(caseDetails, RESPONDENT_ADDRESS, RESP_SOLICITOR_ADDRESS, RESP_SOLICITOR_NAME, RESPONDENT_SOLICITOR_FIRM,
            APP_RESPONDENT_FIRST_MIDDLE_NAME, APP_RESPONDENT_LAST_NAME);

        return genericDocumentService.generateDocument(authorisationToken, caseDetails, documentConfiguration.getBulkPrintTemplate(),
            documentConfiguration.getBulkPrintFileName());
    }

    private void prepareCoverSheet(CaseDetails caseDetails, String partyAddressCcdFieldName, String solicitorAddressCcdFieldName,
                                   String solicitorNameCcdFieldName, String solicitorFirmCcdFieldName,
                                   String partyFirstMiddleNameCcdFieldName, String partyLastNameCcdFieldName) {
        Map<String, Object> caseData = caseDetails.getData();
        AddressFoundInCaseData addressFoundInCaseData = checkAddress(caseData, partyAddressCcdFieldName, solicitorAddressCcdFieldName);

        if (addressFoundInCaseData != AddressFoundInCaseData.NONE) {
            boolean sendToSolicitor = addressFoundInCaseData == AddressFoundInCaseData.SOLICITOR;

            populateCaseDataWithBulkPrintCoverSheet(caseDetails,
                sendToSolicitor ? solicitorName(caseData.get(solicitorNameCcdFieldName), caseData.get(solicitorFirmCcdFieldName))
                    : partyName(caseData.get(partyFirstMiddleNameCcdFieldName), caseData.get(partyLastNameCcdFieldName)),
                sendToSolicitor ? (Map) caseData.get(solicitorAddressCcdFieldName) : (Map) caseData.get(partyAddressCcdFieldName));
        }
    }

    private AddressFoundInCaseData checkAddress(Map<String, Object> caseData, String partyAddressCcdFieldName,
                                                String solicitorAddressCcdFieldName) {
        return addressLineOneAndPostCodeAreBothNotEmpty((Map) caseData.get(solicitorAddressCcdFieldName)) ? AddressFoundInCaseData.SOLICITOR
            : addressLineOneAndPostCodeAreBothNotEmpty((Map) caseData.get(partyAddressCcdFieldName)) ? AddressFoundInCaseData.PARTY
            : AddressFoundInCaseData.NONE;
    }

    private String solicitorName(Object solicitorName, Object solicitorFirm) {
        return StringUtils.joinWith("\n", solicitorName, solicitorFirm).trim();
    }

    private String partyName(Object partyFirstMiddleName, Object partyLastName) {
        return StringUtils.joinWith(" ", partyFirstMiddleName, partyLastName).trim();
    }

    private void populateCaseDataWithBulkPrintCoverSheet(CaseDetails caseDetails, String name, Map address) {
        BulkPrintCoverSheet.BulkPrintCoverSheetBuilder bulkPrintCoverSheetBuilder = BulkPrintCoverSheet.builder()
            .ccdNumber(caseDetails.getId().toString())
            .recipientName(name);
        fillBulkPrintCoverSheetWithAddress(bulkPrintCoverSheetBuilder, address);
        caseDetails.getData().put(BULK_PRINT_COVER_SHEET, bulkPrintCoverSheetBuilder.build());
    }

    private void fillBulkPrintCoverSheetWithAddress(BulkPrintCoverSheet.BulkPrintCoverSheetBuilder bulkPrintCoverSheetBuilder,
                                                    Map<String, Object> address) {
        bulkPrintCoverSheetBuilder
            .addressLine1(nullToEmpty(address.get("AddressLine1")))
            .addressLine2(nullToEmpty(address.get("AddressLine2")))
            .addressLine3(nullToEmpty(address.get("AddressLine3")))
            .county(nullToEmpty(address.get("County")))
            .country(nullToEmpty(address.get("Country")))
            .postTown(nullToEmpty(address.get("PostTown")))
            .postCode(nullToEmpty(address.get("PostCode")))
            .build();
    }
}
