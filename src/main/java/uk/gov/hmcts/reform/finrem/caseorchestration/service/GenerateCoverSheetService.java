package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.client.DocumentClient;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.BulkPrintCoverSheet;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;

import java.util.Map;

import static org.apache.commons.lang3.StringUtils.join;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BULK_PRINT_COVER_SHEET;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.nullToEmpty;

@Service
@Slf4j
public class GenerateCoverSheetService extends AbstractDocumentService {

    @Autowired
    public GenerateCoverSheetService(
        DocumentClient documentClient, DocumentConfiguration config, ObjectMapper objectMapper) {
        super(documentClient, config, objectMapper);
    }

    public CaseDocument generateRespondentCoverSheet(final CaseDetails caseDetails, final String authorisationToken) {
        log.info(
            "Generating Respondent cover sheet {} from {} for bulk print for case id {} ",
            config.getBulkPrintFileName(),
            config.getBulkPrintTemplate(),
            caseDetails.getId().toString());
        prepareRespondentCoverSheet(caseDetails);
        return generateDocument(
            authorisationToken,
            caseDetails,
            config.getBulkPrintTemplate(),
            config.getBulkPrintFileName());
    }

    public CaseDocument generateApplicantCoverSheet(final CaseDetails caseDetails, final String authorisationToken) {
        log.info(
            "Generating Applicant cover sheet {} from {} for bulk print for case id {} ",
            config.getBulkPrintFileName(),
            config.getBulkPrintTemplate(),
            caseDetails.getId().toString());
        prepareApplicantCoverSheet(caseDetails);
        return generateDocument(
            authorisationToken,
            caseDetails,
            config.getBulkPrintTemplate(),
            config.getBulkPrintFileName());
    }

    private void prepareApplicantCoverSheet(CaseDetails caseDetails) {
        BulkPrintCoverSheet.BulkPrintCoverSheetBuilder bulkPrintCoverSheetBuilder = BulkPrintCoverSheet.builder()
            .ccdNumber(caseDetails.getId().toString())
            .recipientName(join(nullToEmpty(caseDetails.getData().get("applicantFMName")), " ",
                nullToEmpty(caseDetails.getData().get("applicantLName"))));

        Object respondentAddress = caseDetails.getData().get("applicantAddress");
        Object solicitorAddress = caseDetails.getData().get("solicitorAddress");

        if (solicitorAddress != null) {
            caseDetails.getData().put(BULK_PRINT_COVER_SHEET, getBulkPrintCoverSheet(bulkPrintCoverSheetBuilder, (Map) solicitorAddress));
        } else if (respondentAddress != null) {
            caseDetails.getData().put(BULK_PRINT_COVER_SHEET,  getBulkPrintCoverSheet(bulkPrintCoverSheetBuilder, (Map) respondentAddress));
        }
    }

    private void prepareRespondentCoverSheet(CaseDetails caseDetails) {
        BulkPrintCoverSheet.BulkPrintCoverSheetBuilder bulkPrintCoverSheetBuilder = BulkPrintCoverSheet.builder()
            .ccdNumber(caseDetails.getId().toString())
            .recipientName(join(nullToEmpty(caseDetails.getData().get("appRespondentFMName")), " ",
                    nullToEmpty(caseDetails.getData().get("appRespondentLName"))));

        Object respondentAddress = caseDetails.getData().get("respondentAddress");
        Object solicitorAddress = caseDetails.getData().get("rSolicitorAddress");

        if (solicitorAddress != null || !solicitorAddress.toString().equals("")) {
            caseDetails.getData().put(BULK_PRINT_COVER_SHEET, getBulkPrintCoverSheet(bulkPrintCoverSheetBuilder, (Map) solicitorAddress));
        } else if (respondentAddress != null || !respondentAddress.toString().equals("")) {
            caseDetails.getData().put(BULK_PRINT_COVER_SHEET,  getBulkPrintCoverSheet(bulkPrintCoverSheetBuilder, (Map) respondentAddress));
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
