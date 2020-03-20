package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.client.DocumentClient;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.BulkPrintCoverSheet;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;

import java.util.Map;

import static org.apache.commons.lang3.StringUtils.join;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.APP_ADDRESS_CCD_FIELD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.APP_FIRST_AND_MIDDLE_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.APP_LAST_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.APP_RESP_FIRST_AND_MIDDLE_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.APP_RESP_LAST_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.APP_SOLICITOR_ADDRESS_CCD_FIELD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.RESPONDENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.RESP_ADDRESS_CCD_FIELD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.RESP_SOLICITOR_ADDRESS_CCD_FIELD;
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

    public CaseDocument generateApplicantCoverSheet(final CaseDetails caseDetails, final String authorisationToken) {
        log.info(
            "Generating Applicant cover sheet {} from {} for bulk print for case id {} ",
            config.getBulkPrintFileName(),
            config.getBulkPrintTemplate(),
            caseDetails.getId().toString());
        prepareCoverSheet(APPLICANT, caseDetails);

        return generateDocument(
            authorisationToken,
            caseDetails,
            config.getBulkPrintTemplate(),
            config.getBulkPrintFileName());
    }

    public CaseDocument generateRespondentCoverSheet(final CaseDetails caseDetails, final String authorisationToken) {
        log.info("Generating Respondent cover sheet {} from {} for bulk print for case id {} ",
                config.getBulkPrintFileName(),
                config.getBulkPrintTemplate(),
                caseDetails.getId().toString());
        prepareCoverSheet(RESPONDENT, caseDetails);

        return generateDocument(
                authorisationToken,
                caseDetails,
                config.getBulkPrintTemplate(),
                config.getBulkPrintFileName());
    }

    private void prepareCoverSheet(String user, CaseDetails caseDetails) {

        Map<String, Object> caseData = caseDetails.getData();
        Map solicitorAddress;
        Map userAddress;

        if (user.equals("Applicant")) {
            solicitorAddress = (Map) caseData.get(APP_SOLICITOR_ADDRESS_CCD_FIELD);
            userAddress = (Map) caseData.get(APP_ADDRESS_CCD_FIELD);
        } else {
            solicitorAddress = (Map) caseData.get(RESP_SOLICITOR_ADDRESS_CCD_FIELD);
            userAddress = (Map) caseData.get(RESP_ADDRESS_CCD_FIELD);
        }

        BulkPrintCoverSheet.BulkPrintCoverSheetBuilder bulkPrintCoverSheetBuilder = BulkPrintCoverSheet.builder()
                .ccdNumber(caseDetails.getId().toString())
                .recipientName(join(nullToEmpty(caseData.get(APP_RESP_FIRST_AND_MIDDLE_NAME_CCD_FIELD)), " ",
                        nullToEmpty(caseDetails.getData().get(APP_RESP_LAST_NAME_CCD_FIELD))));

        if (addressLineOneAndPostCodeAreBothNotEmpty(solicitorAddress)) {
            caseData.put(BULK_PRINT_COVER_SHEET, getBulkPrintCoverSheet(bulkPrintCoverSheetBuilder, solicitorAddress));
        } else if (addressLineOneAndPostCodeAreBothNotEmpty(userAddress)) {
            caseData.put(BULK_PRINT_COVER_SHEET,  getBulkPrintCoverSheet(bulkPrintCoverSheetBuilder, userAddress));
        }
    }

    private boolean addressLineOneAndPostCodeAreBothNotEmpty(Map address) {
        return  ObjectUtils.isNotEmpty(address) && StringUtils.isNotBlank((String) address.get("AddressLine1"))
                && StringUtils.isNotBlank((String) address.get("PostCode"));
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
