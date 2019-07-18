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
        Optional<Object> respondentAddressObj = getValue.apply(caseDetails.getData(), "respondentAddress");

        if (respondentAddressObj.isPresent()) {
            Map<String, Object> respondentAddress = (Map) respondentAddressObj.get();
            BulkPrintCoverSheet bulkPrintCoverSheet =
                BulkPrintCoverSheet.builder()
                    .ccdNumber(caseDetails.getId().toString())
                    .recipientName(
                        getString.apply(respondentAddress, "appRespondentFMName")
                            + " "
                            + getString.apply(respondentAddress, "appRespondentLName"))
                    .addressLine1(getString.apply(respondentAddress, "AddressLine1"))
                    .addressLine2(getString.apply(respondentAddress, "AddressLine2"))
                    .addressLine3(getString.apply(respondentAddress, "AddressLine3"))
                    .county(getString.apply(respondentAddress, "County"))
                    .country(getString.apply(respondentAddress, "Country"))
                    .postTown(getString.apply(respondentAddress, "PostTown"))
                    .postCode(getString.apply(respondentAddress, "PostCode"))
                    .build();
            caseDetails.getData().put("bulkPrintCoverSheet", bulkPrintCoverSheet);
        }
    }

}
