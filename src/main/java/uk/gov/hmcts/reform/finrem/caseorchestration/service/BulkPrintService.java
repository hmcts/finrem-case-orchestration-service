package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.client.DocumentClient;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.BulkPrintCoverSheet;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintRequest;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static java.util.Optional.ofNullable;

@Service
@Slf4j
public class BulkPrintService extends AbstractDocumentService {
    public static final String UPLOAD_ORDER = "uploadOrder";
    private static final String DOCUMENT_LINK = "DocumentLink";
    private static final String DOCUMENT_URL = "document_binary_url";
    private static final String VALUE = "value";

    @Autowired
    public BulkPrintService(
        DocumentClient documentClient, DocumentConfiguration config, ObjectMapper objectMapper) {
        super(documentClient, config, objectMapper);
    }

    public void sendForBulkPrint(final CaseDetails caseDetails, final String authorisationToken) {
        log.info(
            "Generating cover sheet {} from {} for bulk print for case id {} ",
            config.getBulkPrintFileName(),
            config.getBulkPrintTemplate(),
            caseDetails.getId().toString());
        addBulkPrintCoverSheet(caseDetails);
        CaseDocument caseDocument =
            generateDocument(
                authorisationToken,
                caseDetails,
                config.getBulkPrintTemplate(),
                config.getBulkPrintFileName());

        List<BulkPrintDocument> bulkPrintDocuments = new ArrayList<>();

        bulkPrintDocuments.add(
            BulkPrintDocument.builder().binaryFileUrl(caseDocument.getDocumentBinaryUrl()).build());

        log.info("extracting order documents from case data  for bulk print. ");
        List<Map> documentList =
            ofNullable(caseDetails.getData().get(UPLOAD_ORDER))
                .map(i -> (List<Map>) i)
                .orElse(new ArrayList<>());

        for (Map<String, Object> document : documentList) {
            Map<String, Object> value = ((Map) document.get(VALUE));
            Map<String, Object> documentLink =
                (Map) ofNullable(getValue(value, DOCUMENT_LINK)).orElse(null);
            if (ofNullable(documentLink).isPresent()) {
                bulkPrintDocuments.add(
                    BulkPrintDocument.builder()
                        .binaryFileUrl(documentLink.get(DOCUMENT_URL).toString())
                        .build());
            }
        }
        log.info(
            " {} order documents with cover sheet are sent bulk print.", bulkPrintDocuments.size());

        bulkPrint(
            BulkPrintRequest.builder()
                .caseId(caseDetails.getId().toString())
                .letterType("FINANCIAL_REMEDY_PACK")
                .bulkPrintDocuments(bulkPrintDocuments)
                .build());
    }

    private Object getValue(Map<String, Object> objectMap, String key) {
        Iterator<Map.Entry<String, Object>> iterator = objectMap.entrySet().iterator();
        Object result = null;
        while (iterator.hasNext()) {
            Map.Entry map = iterator.next();
            if (map.getKey().equals(key)) {
                result = map.getValue();
            }
        }
        return result;
    }

    private void addBulkPrintCoverSheet(CaseDetails caseDetails) {

        Map<String, Object> caseDataMap = caseDetails.getData();
        Map<String, Object> respondentAddress =
            (Map) ofNullable(getValue(caseDataMap, "respondentAddress")).orElse(null);

        if (ofNullable(respondentAddress).isPresent()) {
            BulkPrintCoverSheet bulkPrintCoverSheet =
                BulkPrintCoverSheet.builder()
                    .ccdNumber(caseDetails.getId().toString())
                    .recipientName(
                        getStringValue(caseDataMap, "appRespondentFMName")
                            + " "
                            + getStringValue(caseDataMap, "appRespondentLName"))
                    .addressLine1(getStringValue(respondentAddress, "AddressLine1"))
                    .addressLine2(getStringValue(respondentAddress, "AddressLine2"))
                    .addressLine3(getStringValue(respondentAddress, "AddressLine3"))
                    .county(getStringValue(respondentAddress, "County"))
                    .country(getStringValue(respondentAddress, "Country"))
                    .postTown(getStringValue(respondentAddress, "PostTown"))
                    .postCode(getStringValue(respondentAddress, "PostCode"))
                    .build();
            caseDetails.getData().put("bulkPrintCoverSheet", bulkPrintCoverSheet);
        }
    }

    private String getStringValue(Map<String, Object> objectMap, String key) {
        return ofNullable(getValue(objectMap, key)).map(Object::toString).orElse(StringUtils.EMPTY);
    }
}
