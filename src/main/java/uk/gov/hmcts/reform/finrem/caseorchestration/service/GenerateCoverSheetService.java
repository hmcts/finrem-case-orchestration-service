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
import java.util.Optional;

import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.getString;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.getValue;

@Service
@Slf4j
public class GenerateCoverSheetService extends AbstractDocumentService {

    @Autowired
    public GenerateCoverSheetService(
        DocumentClient documentClient, DocumentConfiguration config, ObjectMapper objectMapper) {
        super(documentClient, config, objectMapper);
    }

    public CaseDocument generateCoverSheet(final CaseDetails caseDetails, final String authorisationToken) {
        log.info(
            "Generating cover sheet {} from {} for bulk print for case id {} ",
            config.getBulkPrintFileName(),
            config.getBulkPrintTemplate(),
            caseDetails.getId().toString());
        addBulkPrintCoverSheet(caseDetails);
        return generateDocument(
            authorisationToken,
            caseDetails,
            config.getBulkPrintTemplate(),
            config.getBulkPrintFileName());


    }

    private void addBulkPrintCoverSheet(CaseDetails caseDetails) {
        BulkPrintCoverSheet.BulkPrintCoverSheetBuilder bulkPrintCoverSheetBuilder = BulkPrintCoverSheet.builder()
            .ccdNumber(caseDetails.getId().toString())
            .recipientName(
                getString.apply(caseDetails.getData(), "appRespondentFMName")
                    .concat(" ").concat(getString.apply(caseDetails.getData(), "appRespondentLName")));

        Optional<Object> respondentAddress = getValue.apply(caseDetails.getData(), "respondentAddress");
        Optional<Object> solicitorAddress = getValue.apply(caseDetails.getData(), "solicitorAddress");

        if (solicitorAddress.isPresent()) {
            caseDetails.getData().put("bulkPrintCoverSheet", getBulkPrintCoverSheet(bulkPrintCoverSheetBuilder,
                (Map) solicitorAddress.get()));
        } else if (respondentAddress.isPresent()) {
            caseDetails.getData().put("bulkPrintCoverSheet",  getBulkPrintCoverSheet(bulkPrintCoverSheetBuilder,
                (Map) respondentAddress.get()));
        }
    }

    private BulkPrintCoverSheet getBulkPrintCoverSheet(
        BulkPrintCoverSheet.BulkPrintCoverSheetBuilder bulkPrintCoverSheetBuilder,
        Map<String, Object> address) {
        return bulkPrintCoverSheetBuilder
            .addressLine1(getString.apply(address, "AddressLine1"))
            .addressLine2(getString.apply(address, "AddressLine2"))
            .addressLine3(getString.apply(address, "AddressLine3"))
            .county(getString.apply(address, "County"))
            .country(getString.apply(address, "Country"))
            .postTown(getString.apply(address, "PostTown"))
            .postCode(getString.apply(address, "PostCode"))
            .build();
    }

}
