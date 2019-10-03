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
        log.info("FR Migration: ccdMigrationRequest >>> authorisationToken {}, ccdRequest {}",
                authorisationToken, ccdRequest);
        final Map<String, Object> caseData = ccdRequest.getCaseDetails().getData();
        boolean migrationRequired = false;
        final Object caseId = ccdRequest.getCaseDetails().getId();
        final Object judgeAllocated = caseData.get(JUDGE_ALLOCATED);
        log.info("FR Migration: {} ,judgeAllocated : {}", caseId, judgeAllocated);
        if (nonNull(judgeAllocated) && !ObjectUtils.isEmpty(judgeAllocated)) {
            if (judgeAllocated instanceof String) {
                caseData.put(JUDGE_ALLOCATED, Arrays.asList(judgeAllocated));
            }
            log.info("FR Migration: {} Migrating judgeAllocated.", caseId);
            migrationRequired = true;
        }

        final Object allocatedCourtList = caseData.get(ALLOCATED_COURT_LIST);
        log.info("FR Migration: {} ,allocatedCourtList :{}", caseId, allocatedCourtList);
        if (nonNull(allocatedCourtList) && !ObjectUtils.isEmpty(allocatedCourtList)) {
            if (allocatedCourtList instanceof String) {
                courtData(caseData, ALLOCATED_COURT_LIST, NOTTINGHAM_COURT_LIST, CFC_COURT_LIST);
            }
            migrationRequired = true;
        }

        final Object allocatedCourtListSL = caseData.get(ALLOCATED_COURT_LIST_SL);
        log.info("FR Migration: {} , allocatedCourtListSL : {}", caseId, allocatedCourtListSL);
        if (nonNull(allocatedCourtListSL) && !ObjectUtils.isEmpty(allocatedCourtListSL)) {
            if (allocatedCourtListSL instanceof String) {
                courtData(caseData, ALLOCATED_COURT_LIST_SL, NOTTINGHAM_COURT_LIST_SL, CFC_COURT_LIST_SL);
            }
            migrationRequired = true;
        }

        final Object allocatedCourtListGA = caseData.get(ALLOCATED_COURT_LIST_GA);
        log.info("FR Migration: {} , allocatedCourtListGA {}", caseId, allocatedCourtListGA);
        if (nonNull(allocatedCourtListGA) && !ObjectUtils.isEmpty(allocatedCourtListGA)) {
            if (allocatedCourtListGA instanceof String) {
                courtData(caseData, ALLOCATED_COURT_LIST_GA, NOTTINGHAM_COURT_LIST_GA, CFC_COURT_LIST_GA);
            }
            migrationRequired = true;
        }

        if (migrationRequired) {
            log.info("FR Migration: {}, End of case migration {} ", caseId, caseData);
            return AboutToStartOrSubmitCallbackResponse.builder().data(caseData).build();
        } else {
            log.info("FR Migration:{} Returning without migration", caseId);
            return AboutToStartOrSubmitCallbackResponse.builder().build();
        }
    }

    private void courtData(final Map<String, Object> caseData, final String allocatedCourtListKey,
                           final String nottinghamCourtListKey, final String cfcCourtListKey) {
        log.info("FR Migration: migrating {} ", allocatedCourtListKey);
        final Object allocatedCourtList = caseData.get(allocatedCourtListKey);
        final String allocatedCourtListStr = Objects.toString(allocatedCourtList);
        final Map<String, Object> map = new HashMap<>();
        if (allocatedCourtListStr.equalsIgnoreCase("nottingham")) {
            map.put("region", "midlands");
            map.put("midlandsList", "nottingham");
            map.put("nottinghamCourtList", Objects.toString(caseData.get(nottinghamCourtListKey)));
            caseData.remove(nottinghamCourtListKey);
        } else if (allocatedCourtListStr.equalsIgnoreCase("cfc")) {
            map.put("region", "london");
            map.put("londonList", "cfc");
            map.put("cfcCourtList", Objects.toString(caseData.get(cfcCourtListKey)));
            caseData.remove(cfcCourtListKey);
        }
        caseData.put(allocatedCourtListKey, map);
    }

}
