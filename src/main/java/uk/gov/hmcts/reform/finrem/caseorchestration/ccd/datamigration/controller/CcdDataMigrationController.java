package uk.gov.hmcts.reform.finrem.caseorchestration.ccd.datamigration.controller;

import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/ccd-data-migration")
@Slf4j
public class CcdDataMigrationController {

    public static final String REGION = "regionList";
    public static final String REGION_SL = "regionListSL";
    public static final String ALLOCATED_COURT_LIST = "allocatedCourtList";
    public static final String REGION_AC = "region";

    // regions
    public static final String WALES = "wales";
    public static final String LONDON = "london";
    public static final String MIDLANDS = "midlands";
    public static final String NORTHWEST = "northwest";
    public static final String NORTHEAST = "northeast";
    public static final String SOUTHEAST = "southeast";

    // FRC lists
    public static final String WALES_FRC_LIST = "walesFRCList";
    public static final String SOUTHEAST_FRC_LIST = "southEastFRCList";
    public static final String NORTHEAST_FRC_LIST = "northEastFRCList";
    public static final String NORTHWEST_FRC_LIST = "northWestFRCList";
    public static final String LONDON_FRC_LIST = "londonFRCList";
    public static final String MIDLANDS_FRC_LIST = "midlandsFRCList";

    // FRC lists SL
    public static final String WALES_FRC_LIST_SL = "walesFRCListSL";
    public static final String SOUTHEAST_FRC_LIST_SL = "southEastFRCListSL";
    public static final String NORTHEAST_FRC_LIST_SL = "northEastFRCListSL";
    public static final String NORTHWEST_FRC_LIST_SL = "northWestFRCListSL";
    public static final String LONDON_FRC_LIST_SL = "londonFRCListSL";
    public static final String MIDLANDS_FRC_LIST_SL = "midlandsFRCListSL";

    // FRC lists AC
    public static final String WALES_FRC_LIST_AC = "walesList";
    public static final String SOUTHEAST_FRC_LIST_AC = "southEastList";
    public static final String NORTHEAST_FRC_LIST_AC = "northEastList";
    public static final String NORTHWEST_FRC_LIST_AC = "northWestList";
    public static final String LONDON_FRC_LIST_AC = "londonList";
    public static final String MIDLANDS_FRC_LIST_AC = "midlandsList";

    // FRCs
    public static final String KENT = "kentfrc";
    public static final String LIVERPOOL = "liverpool";
    public static final String MANCHESTER = "manchester";
    public static final String CFC = "cfc";
    public static final String NEWPORT = "newport";
    public static final String SWANSEA = "swansea";
    public static final String CLEAVELAND = "cleaveland";
    public static final String NWYORKSHIRE = "nwyorkshire";
    public static final String HSYORKSHIRE = "hsyorkshire";
    public static final String NOTTINGHAM = "nottingham";
    public static final String BIRMINGHAM = "birmingham";

    // Court lists
    public static final String LIVERPOOL_COURT_LIST = "liverpoolCourtList";
    public static final String MANCHESTER_COURT_LIST = "manchesterCourtList";
    public static final String NEWPORT_COURT_LIST = "newportCourtList";
    public static final String CFC_COURT_LIST = "cfcCourtList";
    public static final String SWANSEA_COURT_LIST = "swanseaCourtList";
    public static final String CLEAVELAND_COURT_LIST = "cleavelandCourtList";
    public static final String NWYORKSHIRE_COURT_LIST = "nwyorkshireCourtList";
    public static final String NOTTINGHAM_COURT_LIST = "nottinghamCourtList";
    public static final String BIRMINGHAM_COURT_LIST = "birminghamCourtList";
    public static final String HUMBER_COURT_LIST = "humberCourtList";
    public static final String KENT_SURREY_COURT_LIST = "kentSurreyCourtList";

    // Court lists SL
    public static final String LIVERPOOL_COURT_LIST_SL = "liverpoolCourtListSL";
    public static final String MANCHESTER_COURT_LIST_SL = "manchesterCourtListSL";
    public static final String NEWPORT_COURT_LIST_SL = "newportCourtListSL";
    public static final String CFC_COURT_LIST_SL = "cfcCourtListSL";
    public static final String SWANSEA_COURT_LIST_SL = "swanseaCourtListSL";
    public static final String CLEAVELAND_COURT_LIST_SL = "cleavelandCourtListSL";
    public static final String NWYORKSHIRE_COURT_LIST_SL = "nwyorkshireCourtListSL";
    public static final String NOTTINGHAM_COURT_LIST_SL = "nottinghamCourtListSL";
    public static final String BIRMINGHAM_COURT_LIST_SL = "birminghamCourtListSL";
    public static final String HUMBER_COURT_LIST_SL = "humberCourtListSL";
    public static final String KENT_SURREY_COURT_LIST_SL = "kentSurreyCourtListSL";

    // Court lists SL
    public static final String LIVERPOOL_COURT_LIST_AC = "liverpoolCourtList";
    public static final String MANCHESTER_COURT_LIST_AC = "manchesterCourtList";
    public static final String NEWPORT_COURT_LIST_AC = "newportCourtList";
    public static final String CFC_COURT_LIST_AC = "cfcCourtList";
    public static final String SWANSEA_COURT_LIST_AC = "swanseaCourtList";
    public static final String CLEAVELAND_COURT_LIST_AC = "cleavelandCourtList";
    public static final String NWYORKSHIRE_COURT_LIST_AC = "nwyorkshireCourtList";
    public static final String NOTTINGHAM_COURT_LIST_AC = "nottinghamCourtList";
    public static final String BIRMINGHAM_COURT_LIST_AC = "birminghamCourtList";
    public static final String HUMBER_COURT_LIST_AC = "humberCourtList";
    public static final String KENT_SURREY_COURT_LIST_AC = "kentSurreyCourtList";

    @PostMapping(value = "/migrate", consumes = APPLICATION_JSON_VALUE)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Callback was processed successfully or in case of an error message is attached to the case",
                response = CallbackResponse.class)})
    public CallbackResponse migrate(
            @RequestHeader(value = AUTHORIZATION_HEADER) final String authorisationToken,
            @RequestBody @ApiParam("CaseData") final CallbackRequest ccdRequest) {

        CaseDetails caseDetails = ccdRequest.getCaseDetails();
        log.info("FR case migration request received for case {}", caseDetails.getId());

        AboutToStartOrSubmitCallbackResponse.AboutToStartOrSubmitCallbackResponseBuilder responseBuilder =
            AboutToStartOrSubmitCallbackResponse.builder();

        if (migrationRequired(caseDetails)) {
            Map<String, Object> caseData = migrateCaseData(caseDetails.getData());
            responseBuilder.data(caseData);
        }

        return responseBuilder.build();
    }

    private boolean migrationRequired(CaseDetails caseDetails) {
        Map<String, Object> caseData = caseDetails.getData();
        boolean isContestedCase = isContestedCase(caseDetails);
        boolean hasRegionList = hasRegionList(caseData);
        return isContestedCase && !hasRegionList;
    }

    private boolean isContestedCase(CaseDetails caseDetails) {
        return caseDetails.getCaseTypeId().equals("FinancialRemedyContested");
    }

    private boolean hasRegionList(Map<String, Object> caseData) {
        return caseData.containsKey(REGION);
    }


    private Map<String, Object> migrateCaseData(Map<String, Object> caseData) {
        if (caseData.containsKey(REGION_SL)) {
            caseData.remove(ALLOCATED_COURT_LIST);

            String region = (String) caseData.get(REGION_SL);
            caseData.put(REGION, region);
            caseData.remove(REGION_SL);

            switch (region) {
                case WALES:
                    caseData.put(WALES_FRC_LIST, caseData.get(WALES_FRC_LIST_SL));
                    caseData.remove(WALES_FRC_LIST_SL);

                    switch ((String) caseData.get(WALES_FRC_LIST)) {
                        case NEWPORT:
                            caseData.put(NEWPORT_COURT_LIST, caseData.get(NEWPORT_COURT_LIST_SL));
                            caseData.remove(NEWPORT_COURT_LIST_SL);
                            break;
                        case SWANSEA:
                            caseData.put(SWANSEA_COURT_LIST, caseData.get(SWANSEA_COURT_LIST_SL));
                            caseData.remove(SWANSEA_COURT_LIST_SL);
                            break;
                        default:
                            break;
                    }
                    break;
                case LONDON:
                    caseData.put(LONDON_FRC_LIST, caseData.get(LONDON_FRC_LIST_SL));
                    caseData.remove(LONDON_FRC_LIST_SL);

                    switch ((String) caseData.get(LONDON_FRC_LIST)) {
                        case CFC:
                            caseData.put(CFC_COURT_LIST, caseData.get(CFC_COURT_LIST_SL));
                            caseData.remove(CFC_COURT_LIST_SL);
                            break;
                        default:
                            break;
                    }
                    break;
                case MIDLANDS:
                    caseData.put(MIDLANDS_FRC_LIST, caseData.get(MIDLANDS_FRC_LIST_SL));
                    caseData.remove(MIDLANDS_FRC_LIST_SL);

                    switch ((String) caseData.get(MIDLANDS_FRC_LIST)) {
                        case NOTTINGHAM:
                            caseData.put(NOTTINGHAM_COURT_LIST, caseData.get(NOTTINGHAM_COURT_LIST_SL));
                            caseData.remove(NOTTINGHAM_COURT_LIST_SL);
                            break;
                        case BIRMINGHAM:
                            caseData.put(BIRMINGHAM_COURT_LIST, caseData.get(BIRMINGHAM_COURT_LIST_SL));
                            caseData.remove(BIRMINGHAM_COURT_LIST_SL);
                            break;
                        default:
                            break;
                    }
                    break;
                case NORTHWEST:
                    caseData.put(NORTHWEST_FRC_LIST, caseData.get(NORTHWEST_FRC_LIST_SL));
                    caseData.remove(NORTHWEST_FRC_LIST_SL);

                    switch ((String) caseData.get(NORTHWEST_FRC_LIST)) {
                        case LIVERPOOL:
                            caseData.put(LIVERPOOL_COURT_LIST, caseData.get(LIVERPOOL_COURT_LIST_SL));
                            caseData.remove(LIVERPOOL_COURT_LIST_SL);
                            break;
                        case MANCHESTER:
                            caseData.put(MANCHESTER_COURT_LIST, caseData.get(MANCHESTER_COURT_LIST_SL));
                            caseData.remove(MANCHESTER_COURT_LIST_SL);
                            break;
                        default:
                            break;
                    }
                    break;
                case NORTHEAST:
                    caseData.put(NORTHEAST_FRC_LIST, caseData.get(NORTHEAST_FRC_LIST_SL));
                    caseData.remove(NORTHEAST_FRC_LIST_SL);

                    switch ((String) caseData.get(NORTHEAST_FRC_LIST)) {
                        case CLEAVELAND:
                            caseData.put(CLEAVELAND_COURT_LIST, caseData.get(CLEAVELAND_COURT_LIST_SL));
                            caseData.remove(CLEAVELAND_COURT_LIST_SL);
                            break;
                        case NWYORKSHIRE:
                            caseData.put(NWYORKSHIRE_COURT_LIST, caseData.get(NWYORKSHIRE_COURT_LIST_SL));
                            caseData.remove(NWYORKSHIRE_COURT_LIST_SL);
                            break;
                        case HSYORKSHIRE:
                            caseData.put(HUMBER_COURT_LIST, caseData.get(HUMBER_COURT_LIST_SL));
                            caseData.remove(HUMBER_COURT_LIST_SL);
                            break;
                        default:
                            break;
                    }
                    break;
                case SOUTHEAST:
                    caseData.put(SOUTHEAST_FRC_LIST, caseData.get(SOUTHEAST_FRC_LIST_SL));
                    caseData.remove(SOUTHEAST_FRC_LIST_SL);

                    switch ((String) caseData.get(SOUTHEAST_FRC_LIST)) {
                        case KENT:
                            caseData.put(KENT_SURREY_COURT_LIST, caseData.get(KENT_SURREY_COURT_LIST_SL));
                            caseData.remove(KENT_SURREY_COURT_LIST_SL);
                            break;
                        default:
                            break;
                    }
                    break;
                default:
                    break;
            }
        } else {
            Map<String, Object> allocatedCourtList = (Map<String, Object>) caseData.get(ALLOCATED_COURT_LIST);
            String region = (String) allocatedCourtList.get(REGION_AC);
            caseData.put(REGION, region);

            switch (region) {
                case WALES:
                    caseData.put(WALES_FRC_LIST, allocatedCourtList.get(WALES_FRC_LIST_AC));

                    switch ((String) caseData.get(WALES_FRC_LIST)) {
                        case NEWPORT:
                            caseData.put(NEWPORT_COURT_LIST, allocatedCourtList.get(NEWPORT_COURT_LIST_AC));
                            break;
                        case SWANSEA:
                            caseData.put(SWANSEA_COURT_LIST, allocatedCourtList.get(SWANSEA_COURT_LIST_AC));
                            break;
                        default:
                            break;
                    }
                    break;
                case LONDON:
                    caseData.put(LONDON_FRC_LIST, allocatedCourtList.get(LONDON_FRC_LIST_AC));

                    switch ((String) caseData.get(LONDON_FRC_LIST)) {
                        case CFC:
                            caseData.put(CFC_COURT_LIST, allocatedCourtList.get(CFC_COURT_LIST_AC));
                            break;
                        default:
                            break;
                    }
                    break;
                case MIDLANDS:
                    caseData.put(MIDLANDS_FRC_LIST, allocatedCourtList.get(MIDLANDS_FRC_LIST_AC));

                    switch ((String) caseData.get(MIDLANDS_FRC_LIST)) {
                        case NOTTINGHAM:
                            caseData.put(NOTTINGHAM_COURT_LIST, allocatedCourtList.get(NOTTINGHAM_COURT_LIST_AC));
                            break;
                        case BIRMINGHAM:
                            caseData.put(BIRMINGHAM_COURT_LIST, allocatedCourtList.get(BIRMINGHAM_COURT_LIST_AC));
                            break;
                        default:
                            break;
                    }
                    break;
                case NORTHWEST:
                    caseData.put(NORTHWEST_FRC_LIST, allocatedCourtList.get(NORTHWEST_FRC_LIST_AC));

                    switch ((String) caseData.get(NORTHWEST_FRC_LIST)) {
                        case LIVERPOOL:
                            caseData.put(LIVERPOOL_COURT_LIST, allocatedCourtList.get(LIVERPOOL_COURT_LIST_AC));
                            break;
                        case MANCHESTER:
                            caseData.put(MANCHESTER_COURT_LIST, allocatedCourtList.get(MANCHESTER_COURT_LIST_AC));
                            break;
                        default:
                            break;
                    }
                    break;
                case NORTHEAST:
                    caseData.put(NORTHEAST_FRC_LIST, allocatedCourtList.get(NORTHEAST_FRC_LIST_AC));

                    switch ((String) caseData.get(NORTHEAST_FRC_LIST)) {
                        case CLEAVELAND:
                            caseData.put(CLEAVELAND_COURT_LIST, allocatedCourtList.get(CLEAVELAND_COURT_LIST_AC));
                            break;
                        case NWYORKSHIRE:
                            caseData.put(NWYORKSHIRE_COURT_LIST, allocatedCourtList.get(NWYORKSHIRE_COURT_LIST_AC));
                            break;
                        case HSYORKSHIRE:
                            caseData.put(HUMBER_COURT_LIST, allocatedCourtList.get(HUMBER_COURT_LIST_AC));
                            break;
                        default:
                            break;
                    }
                    break;
                case SOUTHEAST:
                    caseData.put(SOUTHEAST_FRC_LIST, allocatedCourtList.get(SOUTHEAST_FRC_LIST_AC));

                    switch ((String) caseData.get(SOUTHEAST_FRC_LIST)) {
                        case KENT:
                            caseData.put(KENT_SURREY_COURT_LIST, allocatedCourtList.get(KENT_SURREY_COURT_LIST_AC));
                            break;
                        default:
                            break;
                    }
                    break;
                default:
                    break;
            }

            caseData.remove(ALLOCATED_COURT_LIST);
        }
        return caseData;
    }
}
