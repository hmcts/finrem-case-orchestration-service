package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingOrderCollectionData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingOrderDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderApprovedDocumentService;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.util.CollectionUtils.isEmpty;
import static org.springframework.util.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FINAL_ORDER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_ORDER_COLLECTION;

@Slf4j
@RestController
@RequestMapping(value = "/case-orchestration")
public class FinalOrderController implements BaseController {
    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private ConsentOrderApprovedDocumentService service;

    @PostMapping(path = "/contested/send-order", consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Handles Consent order approved generation. Serves as a callback from CCD")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Callback was processed successFully or in case of an error message is "
                                                       + "attached to the case",
                    response = AboutToStartOrSubmitCallbackResponse.class),
            @ApiResponse(code = 400, message = "Bad Request"),
            @ApiResponse(code = 500, message = "Internal Server Error")
        })

    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> stampFinalOrder(
            @RequestHeader(value = AUTHORIZATION_HEADER) String authToken,
            @NotNull @RequestBody @ApiParam("CaseData") CallbackRequest callback) {

        validateCaseData(callback);
        log.info("stampFinalOrder called with case data = {}",
                callback.getCaseDetails().getData());
        Map<String, Object> caseData = callback.getCaseDetails().getData();
        List<HearingOrderCollectionData> hearingOrderCollectionData = getHearingOrderDocuments(caseData);
        if (hearingOrderCollectionData != null && !hearingOrderCollectionData.isEmpty()) {
            CaseDocument latestHearingOrder = hearingOrderCollectionData
                                                      .get(hearingOrderCollectionData.size() - 1)
                                                      .getHearingOrderDocuments().getUploadDraftDocument();
            log.info("FinalOrderController called with latestHearingOrder = {}",
                    latestHearingOrder);

            stampAndAddToCollection(caseData, latestHearingOrder, authToken);
        }

        return ResponseEntity.ok(
                AboutToStartOrSubmitCallbackResponse.builder()
                        .data(caseData)
                        .errors(ImmutableList.of())
                        .warnings(ImmutableList.of())
                        .build());
    }

    private void stampAndAddToCollection(Map<String, Object> caseData, CaseDocument latestHearingOrder,
                                         String authToken) {
        if (!isEmpty(latestHearingOrder)) {
            CaseDocument stampedDocs = service.stampDocument(latestHearingOrder, authToken);
            log.info(" stampedDocs = {}", stampedDocs);

            List<HearingOrderCollectionData> finalOrderCollection = getFinalOrderDocuments(caseData);
            log.info(" existing = {}", finalOrderCollection);

            if (finalOrderCollection == null) {
                finalOrderCollection = new ArrayList<>();
            }

            finalOrderCollection.add(HearingOrderCollectionData.builder()
                .hearingOrderDocuments(HearingOrderDocument
                    .builder()
                    .uploadDraftDocument(stampedDocs)
                    .build())
                .build());
            log.info("finalOrderCollection = {}", finalOrderCollection);
            caseData.put(FINAL_ORDER_COLLECTION, finalOrderCollection);
            log.info("stampFinalOrder end.");
        }

    }

    private List<HearingOrderCollectionData> getHearingOrderDocuments(Map<String, Object> caseData) {
        return mapper.convertValue(caseData.get(HEARING_ORDER_COLLECTION),
            new TypeReference<List<HearingOrderCollectionData>>() {
            });
    }

    private List<HearingOrderCollectionData> getFinalOrderDocuments(Map<String, Object> caseData) {
        return mapper.convertValue(caseData.get(FINAL_ORDER_COLLECTION),
            new TypeReference<List<HearingOrderCollectionData>>() {
            });
    }

}
