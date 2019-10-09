package uk.gov.hmcts.reform.finrem.caseorchestration.ccd.datamigration.controller;

import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static java.util.Objects.nonNull;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.JUDGE_ALLOCATED;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/ccd-data-migration")
@Slf4j
public class CcdDataMigrationController {

    public static final String ALLOCATED_COURT_LIST = "allocatedCourtList";
    public static final String ALLOCATED_COURT_LIST_SL = "allocatedCourtListSL";
    public static final String ALLOCATED_COURT_LIST_GA = "allocatedCourtListGA";
    public static final String NOTTINGHAM_COURT_LIST_GA = "nottinghamCourtListGA";
    public static final String CFC_COURT_LIST_GA = "cfcCourtListGA";
    public static final String NOTTINGHAM_COURT_LIST_SL = "nottinghamCourtListSL";
    public static final String CFC_COURT_LIST_SL = "cfcCourtListSL";
    public static final String NOTTINGHAM_COURT_LIST = "nottinghamCourtList";
    public static final String CFC_COURT_LIST = "cfcCourtList";

    @PostMapping(value = "/migrate", consumes = APPLICATION_JSON_VALUE)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Callback was processed successFully or in case of an error message is "
                                                       + "attached to the case", response = CallbackResponse.class)})
    public CallbackResponse migrate(
            @RequestHeader(value = "Authorization") final String authorisationToken,
            @RequestBody @ApiParam("CaseData") final CallbackRequest ccdRequest) {
        log.info("FRMig: ccdMigrationRequest >>> authorisationToken {}, ccdRequest {}",
                authorisationToken, ccdRequest);
        final Map<String, Object> beforeCaseData = ccdRequest.getCaseDetailsBefore().getData();
        final Map<String, Object> caseData = ccdRequest.getCaseDetails().getData();
        boolean migrationRequired = false;
        final Object caseId = ccdRequest.getCaseDetails().getId();
        final Object judgeAllocated = beforeCaseData.get(JUDGE_ALLOCATED);
        log.info("FRMig: {} ,judgeAllocated : {}", caseId, judgeAllocated);
        if (nonNull(judgeAllocated) && !ObjectUtils.isEmpty(judgeAllocated)
                    && judgeAllocated instanceof String) {
            caseData.put(JUDGE_ALLOCATED, Arrays.asList(judgeAllocated));
            log.info("FRMig: {} Migrating judgeAllocated.", caseId);
            migrationRequired = true;
        }

        final Object allocatedCourtList = beforeCaseData.get(ALLOCATED_COURT_LIST);
        log.info("FRMig: {} ,allocatedCourtList :{}", caseId, allocatedCourtList);
        if (nonNull(allocatedCourtList) && !ObjectUtils.isEmpty(allocatedCourtList)
                    && allocatedCourtList instanceof String) {
            courtData(beforeCaseData,caseData, ALLOCATED_COURT_LIST, NOTTINGHAM_COURT_LIST, CFC_COURT_LIST);
            migrationRequired = true;
        }

        final Object allocatedCourtListSL = beforeCaseData.get(ALLOCATED_COURT_LIST_SL);
        log.info("FRMig: {} , allocatedCourtListSL : {}", caseId, allocatedCourtListSL);
        if (nonNull(allocatedCourtListSL) && !ObjectUtils.isEmpty(allocatedCourtListSL)
                    && allocatedCourtListSL instanceof String) {
            final String allocatedCourtListStr = Objects.toString(allocatedCourtListSL);
            log.info("FRMig: {} allocatedCourtListSL ", allocatedCourtListStr);
            if (allocatedCourtListStr.equalsIgnoreCase("nottingham")) {
                log.info("FRMig: {} nottingham ", caseId);
                caseData.put("regionListSL", "midlands");
                migrationRequired = true;
            } else if (allocatedCourtListStr.equalsIgnoreCase("cfc")) {
                log.info("FRMig: {} cfc ", caseId);
                caseData.put("regionListSL", "london");
                caseData.put("allocatedCourtListSL",null);
                caseData.put("londonFRCListSL","cfc");
                migrationRequired = true;
            }
        }

        final Object allocatedCourtListGA = beforeCaseData.get(ALLOCATED_COURT_LIST_GA);
        log.info("FRMig: {} , allocatedCourtListGA {}", caseId, allocatedCourtListGA);
        if (nonNull(allocatedCourtListGA) && !ObjectUtils.isEmpty(allocatedCourtListGA)
                    && allocatedCourtListGA instanceof String) {
            courtData(beforeCaseData,caseData, ALLOCATED_COURT_LIST_GA, NOTTINGHAM_COURT_LIST_GA, CFC_COURT_LIST_GA);
            migrationRequired = true;
        }

        if (migrationRequired) {
            log.info("FRMig: {}, End of case migration {} ", caseId, caseData);
            return AboutToStartOrSubmitCallbackResponse.builder().data(caseData).build();
        } else {
            log.info("FRMig:{} Returning without migration", caseId);
            return AboutToStartOrSubmitCallbackResponse.builder().build();
        }
    }

    private void courtData(final Map<String, Object> beforeCaseDataRO, final Map<String, Object> caseData, final String allocatedCourtListKey,
                           final String nottinghamCourtListKey, final String cfcCourtListKey) {
        log.info("FRMig: migrating {} ", allocatedCourtListKey);
        final Object allocatedCourtList = beforeCaseDataRO.get(allocatedCourtListKey);
        final String allocatedCourtListStr = Objects.toString(allocatedCourtList);
        final Map<String, Object> map = new HashMap<>();
        log.info("FRMig: allocatedCourtListStr {} ", allocatedCourtListStr);
        if (allocatedCourtListStr.equalsIgnoreCase("nottingham")) {
            map.put("region", "midlands");
            map.put("midlandsList", "nottingham");
            map.put("nottinghamCourtList", Objects.toString(beforeCaseDataRO.get(nottinghamCourtListKey)));
            caseData.put(nottinghamCourtListKey,null);
            caseData.put(allocatedCourtListKey, map);
        } else if (allocatedCourtListStr.equalsIgnoreCase("cfc")) {
            map.put("region", "london");
            map.put("londonList", "cfc");
            map.put("cfcCourtList", Objects.toString(beforeCaseDataRO.get(cfcCourtListKey)));
            caseData.put(cfcCourtListKey,null);
            caseData.put(allocatedCourtListKey, map);
        }

    }

}
