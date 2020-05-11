package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintCoverSheet;

import java.util.Map;

import static org.apache.commons.lang3.StringUtils.join;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_FIRST_MIDDLE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_LAST_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_RESPONDENT_FIRST_MIDDLE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BULK_PRINT_COVER_SHEET;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.addressLineOneAndPostCodeAreBothNotEmpty;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.nullToEmpty;

@Service
@RequiredArgsConstructor
@Slf4j
public class GenerateCoverSheetService {

    private final GenericDocumentService genericDocumentService;
    private final DocumentConfiguration documentConfiguration;

    public CaseDocument generateApplicantCoverSheet(final CaseDetails caseDetails, final String authorisationToken) {
        log.info("Generating Applicant cover sheet {} from {} for bulk print",
            documentConfiguration.getBulkPrintFileName(),
            documentConfiguration.getBulkPrintTemplate());
        prepareApplicantCoverSheet(caseDetails);

        return genericDocumentService.generateDocument(
            authorisationToken,
            caseDetails,
            documentConfiguration.getBulkPrintTemplate(),
            documentConfiguration.getBulkPrintFileName());
    }

    public CaseDocument generateRespondentCoverSheet(final CaseDetails caseDetails, final String authorisationToken) {
        log.info("Generating Respondent cover sheet {} from {} for bulk print",
            documentConfiguration.getBulkPrintFileName(),
            documentConfiguration.getBulkPrintTemplate());
        prepareRespondentCoverSheet(caseDetails);

        return genericDocumentService.generateDocument(
            authorisationToken,
            caseDetails,
            documentConfiguration.getBulkPrintTemplate(),
            documentConfiguration.getBulkPrintFileName());
    }

    private void prepareApplicantCoverSheet(CaseDetails caseDetails) {
        populateBulkPrintCoverSheet(caseDetails, APPLICANT_FIRST_MIDDLE_NAME, APPLICANT_LAST_NAME, APPLICANT_ADDRESS, SOLICITOR_ADDRESS);
    }

    private void prepareRespondentCoverSheet(CaseDetails caseDetails) {
        populateBulkPrintCoverSheet(caseDetails, APP_RESPONDENT_FIRST_MIDDLE_NAME, APP_RESPONDENT_LAST_NAME, RESPONDENT_ADDRESS, RESP_SOLICITOR_ADDRESS);
    }

    private void populateBulkPrintCoverSheet(CaseDetails caseDetails, String partyFirstMiddleNameCcdFieldName, String partyLastNameCcdFieldName,
                                             String partyAddressCcdFieldName, String partySolicitorAddressCcdFieldName) {
        BulkPrintCoverSheet.BulkPrintCoverSheetBuilder bulkPrintCoverSheetBuilder = BulkPrintCoverSheet.builder()
            .ccdNumber(caseDetails.getId().toString())
            .recipientName(join(nullToEmpty(caseDetails.getData().get(partyFirstMiddleNameCcdFieldName)), " ",
                nullToEmpty(caseDetails.getData().get(partyLastNameCcdFieldName))));

        Map partyAddress = (Map) caseDetails.getData().get(partyAddressCcdFieldName);
        Map partySolicitorAddress = (Map) caseDetails.getData().get(partySolicitorAddressCcdFieldName);

        if (addressLineOneAndPostCodeAreBothNotEmpty(partySolicitorAddress)) {
            caseDetails.getData().put(BULK_PRINT_COVER_SHEET, getBulkPrintCoverSheet(bulkPrintCoverSheetBuilder, partySolicitorAddress));
        } else if (addressLineOneAndPostCodeAreBothNotEmpty(partyAddress)) {
            caseDetails.getData().put(BULK_PRINT_COVER_SHEET, getBulkPrintCoverSheet(bulkPrintCoverSheetBuilder, partyAddress));
        }
    }

    private BulkPrintCoverSheet getBulkPrintCoverSheet(BulkPrintCoverSheet.BulkPrintCoverSheetBuilder bulkPrintCoverSheetBuilder,
                                                       Map<String, Object> address) {
        return bulkPrintCoverSheetBuilder
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
