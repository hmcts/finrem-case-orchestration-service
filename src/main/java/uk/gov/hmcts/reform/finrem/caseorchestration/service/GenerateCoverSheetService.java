package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.client.DocumentClient;
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
@Slf4j
public class GenerateCoverSheetService extends AbstractDocumentService {

    @Autowired
    public GenerateCoverSheetService(
        DocumentClient documentClient, DocumentConfiguration config, ObjectMapper objectMapper) {
        super(documentClient, config, objectMapper);
    }

    public CaseDocument generateApplicantCoverSheet(final CaseDetails caseDetails, final String authorisationToken) {
        log.info("Generating Applicant cover sheet {} from {} for bulk print",
                config.getBulkPrintFileName(),
                config.getBulkPrintTemplate());
        prepareApplicantCoverSheet(caseDetails);

        return generateDocument(
                authorisationToken,
                caseDetails,
                config.getBulkPrintTemplate(),
                config.getBulkPrintFileName());
    }

    public CaseDocument generateRespondentCoverSheet(final CaseDetails caseDetails, final String authorisationToken) {
        log.info("Generating Respondent cover sheet {} from {} for bulk print",
            config.getBulkPrintFileName(),
            config.getBulkPrintTemplate());
        prepareRespondentCoverSheet(caseDetails);

        return generateDocument(
            authorisationToken,
            caseDetails,
            config.getBulkPrintTemplate(),
            config.getBulkPrintFileName());
    }

    private void prepareApplicantCoverSheet(CaseDetails caseDetails) {
        BulkPrintCoverSheet.BulkPrintCoverSheetBuilder bulkPrintCoverSheetBuilder = BulkPrintCoverSheet.builder()
            .ccdNumber(caseDetails.getId().toString())
            .recipientName(join(nullToEmpty(caseDetails.getData().get(APPLICANT_FIRST_MIDDLE_NAME)), " ",
                nullToEmpty(caseDetails.getData().get(APPLICANT_LAST_NAME))));

        Map applicantAddress = (Map) caseDetails.getData().get(APPLICANT_ADDRESS);
        Map solicitorAddress = (Map) caseDetails.getData().get(SOLICITOR_ADDRESS);

        if (addressLineOneAndPostCodeAreBothNotEmpty(solicitorAddress)) {
            caseDetails.getData().put(BULK_PRINT_COVER_SHEET, getBulkPrintCoverSheet(bulkPrintCoverSheetBuilder, solicitorAddress));
        } else if (addressLineOneAndPostCodeAreBothNotEmpty(applicantAddress)) {
            caseDetails.getData().put(BULK_PRINT_COVER_SHEET,  getBulkPrintCoverSheet(bulkPrintCoverSheetBuilder, applicantAddress));
        }
    }

    private void prepareRespondentCoverSheet(CaseDetails caseDetails) {
        BulkPrintCoverSheet.BulkPrintCoverSheetBuilder bulkPrintCoverSheetBuilder = BulkPrintCoverSheet.builder()
            .ccdNumber(caseDetails.getId().toString())
            .recipientName(join(nullToEmpty(caseDetails.getData().get(APP_RESPONDENT_FIRST_MIDDLE_NAME)), " ",
                    nullToEmpty(caseDetails.getData().get(APP_RESPONDENT_LAST_NAME))));

        Map respondentAddress = (Map) caseDetails.getData().get(RESPONDENT_ADDRESS);
        Map solicitorAddress = (Map) caseDetails.getData().get(RESP_SOLICITOR_ADDRESS);

        if (addressLineOneAndPostCodeAreBothNotEmpty(solicitorAddress)) {
            caseDetails.getData().put(BULK_PRINT_COVER_SHEET, getBulkPrintCoverSheet(bulkPrintCoverSheetBuilder, solicitorAddress));
        } else if (addressLineOneAndPostCodeAreBothNotEmpty(respondentAddress)) {
            caseDetails.getData().put(BULK_PRINT_COVER_SHEET,  getBulkPrintCoverSheet(bulkPrintCoverSheetBuilder, respondentAddress));
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
