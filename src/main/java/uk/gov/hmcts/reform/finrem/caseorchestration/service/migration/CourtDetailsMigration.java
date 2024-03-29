package uk.gov.hmcts.reform.finrem.caseorchestration.service.migration;

import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LONDON;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LONDON_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MIDLANDS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MIDLANDS_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NORTHEAST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NORTHEAST_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NORTHWEST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NORTHWEST_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.REGION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOUTHEAST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOUTHEAST_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.WALES;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.WALES_FRC_LIST;

public class CourtDetailsMigration implements MigrationHandler {

    public static final String REGION_SL = "regionListSL";
    public static final String ALLOCATED_COURT_LIST = "allocatedCourtList";
    public static final String ALLOCATED_COURT_LIST_GA = "allocatedCourtListGA";
    public static final String REGION_AC = "region";
    public static final String EMPTY_STRING = "";
    // FRC lists SL
    public static final String WALES_FRC_LIST_SL = "walesFRCListSL";
    public static final String SOUTHEAST_FRC_LIST_SL = "southEastFRCListSL";
    public static final String NORTHEAST_FRC_LIST_SL = "northEastFRCListSL";
    public static final String NORTHWEST_FRC_LIST_SL = "northWestFRCListSL";
    public static final String LONDON_FRC_LIST_SL = "londonFRCListSL";
    public static final String MIDLANDS_FRC_LIST_SL = "allocatedCourtListSL";
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
    private final CaseDataService caseDataService = new CaseDataService(new ObjectMapper());

    public Map<String, Object> migrate(CaseDetails caseDetails) {
        if (migrationRequired(caseDetails)) {
            return migrateCaseData(caseDetails.getData());
        }

        return Collections.emptyMap();
    }

    private boolean migrationRequired(CaseDetails caseDetails) {
        Map<String, Object> caseData = caseDetails.getData();
        return caseDataService.isContestedApplication(caseDetails) && !hasRegionList(caseData) && hasCourtDetails(caseData);
    }

    private boolean hasRegionList(Map<String, Object> caseData) {
        return caseData.containsKey(REGION);
    }

    private boolean hasCourtDetails(Map<String, Object> caseData) {
        return caseData.containsKey(REGION_SL) || hasAllocatedCourtDetails(caseData) || hasAllocatedCourtDetailsGA(caseData);
    }

    private boolean hasAllocatedCourtDetails(Map<String, Object> caseData) {
        if (caseData.containsKey(ALLOCATED_COURT_LIST)) {
            try {
                Map<String, Object> allocatedCourtList = (Map<String, Object>) caseData.getOrDefault(ALLOCATED_COURT_LIST, new HashMap<>());
                return allocatedCourtList.containsKey(REGION_AC);
            } catch (ClassCastException e) {
                return false;
            }
        }

        return false;
    }

    private boolean hasAllocatedCourtDetailsGA(Map<String, Object> caseData) {
        if (caseData.containsKey(ALLOCATED_COURT_LIST_GA)) {
            try {
                Map<String, Object> allocatedCourtList = (Map<String, Object>) caseData.getOrDefault(ALLOCATED_COURT_LIST_GA, new HashMap<>());
                return allocatedCourtList.containsKey(REGION_AC);
            } catch (ClassCastException e) {
                return false;
            }
        }

        return false;
    }

    public Map<String, Object> migrateCaseData(Map<String, Object> caseData) {
        if (caseData.containsKey(REGION_SL)) {
            Map<String, Object> caseDataRegion = setRegionSlCourtData(caseData);
            if (!caseDataRegion.isEmpty()) {
                return caseDataRegion;
            }
        } else if (caseData.containsKey(ALLOCATED_COURT_LIST) || caseData.containsKey(ALLOCATED_COURT_LIST_GA)) {
            Map<String, Object> caseDataAllocated = setAllocatedCourtData(caseData);
            if (!caseDataAllocated.isEmpty()) {
                return caseDataAllocated;
            }
        }

        caseData.remove(ALLOCATED_COURT_LIST);
        caseData.remove(ALLOCATED_COURT_LIST_GA);

        return caseData;
    }

    private static Map<String, Object> setAllocatedCourtData(Map<String, Object> caseData) {
        String allocatedListKey = getAllocatedListKey(caseData);
        Map<String, Object> allocatedCourtList = (Map<String, Object>) caseData.getOrDefault(allocatedListKey, new HashMap<>());
        String region = (String) allocatedCourtList.getOrDefault(REGION_AC, EMPTY_STRING);
        caseData.put(REGION, region);

        switch (region) {
            case WALES -> {
                caseData.put(WALES_FRC_LIST, allocatedCourtList.getOrDefault(WALES_FRC_LIST_AC, EMPTY_STRING));
                switch ((String) caseData.getOrDefault(WALES_FRC_LIST, EMPTY_STRING)) {
                    case NEWPORT ->
                        caseData.put(NEWPORT_COURT_LIST, allocatedCourtList.getOrDefault(NEWPORT_COURT_LIST_AC, EMPTY_STRING));
                    case SWANSEA ->
                        caseData.put(SWANSEA_COURT_LIST, allocatedCourtList.getOrDefault(SWANSEA_COURT_LIST_AC, EMPTY_STRING));
                    default -> {
                        return caseData;
                    }
                }
            }
            case LONDON -> {
                caseData.put(LONDON_FRC_LIST, allocatedCourtList.getOrDefault(LONDON_FRC_LIST_AC, EMPTY_STRING));
                String dataOrDefault = (String) caseData.getOrDefault(LONDON_FRC_LIST, EMPTY_STRING);
                if (dataOrDefault.equals(CFC)) {
                    caseData.put(CFC_COURT_LIST, allocatedCourtList.getOrDefault(CFC_COURT_LIST_AC, EMPTY_STRING));
                } else {
                    return caseData;
                }
            }
            case MIDLANDS -> {
                caseData.put(MIDLANDS_FRC_LIST, allocatedCourtList.getOrDefault(MIDLANDS_FRC_LIST_AC, EMPTY_STRING));
                switch ((String) caseData.getOrDefault(MIDLANDS_FRC_LIST, EMPTY_STRING)) {
                    case NOTTINGHAM ->
                        caseData.put(NOTTINGHAM_COURT_LIST, allocatedCourtList.getOrDefault(NOTTINGHAM_COURT_LIST_AC, EMPTY_STRING));
                    case BIRMINGHAM ->
                        caseData.put(BIRMINGHAM_COURT_LIST, allocatedCourtList.getOrDefault(BIRMINGHAM_COURT_LIST_AC, EMPTY_STRING));
                    default -> {
                        return caseData;
                    }
                }
            }
            case NORTHWEST -> {
                caseData.put(NORTHWEST_FRC_LIST, allocatedCourtList.getOrDefault(NORTHWEST_FRC_LIST_AC, EMPTY_STRING));
                switch ((String) caseData.getOrDefault(NORTHWEST_FRC_LIST, EMPTY_STRING)) {
                    case LIVERPOOL ->
                        caseData.put(LIVERPOOL_COURT_LIST, allocatedCourtList.getOrDefault(LIVERPOOL_COURT_LIST_AC, EMPTY_STRING));
                    case MANCHESTER ->
                        caseData.put(MANCHESTER_COURT_LIST, allocatedCourtList.getOrDefault(MANCHESTER_COURT_LIST_AC, EMPTY_STRING));
                    default -> {
                        return caseData;
                    }
                }
            }
            case NORTHEAST -> {
                caseData.put(NORTHEAST_FRC_LIST, allocatedCourtList.getOrDefault(NORTHEAST_FRC_LIST_AC, EMPTY_STRING));
                switch ((String) caseData.getOrDefault(NORTHEAST_FRC_LIST, EMPTY_STRING)) {
                    case CLEAVELAND ->
                        caseData.put(CLEAVELAND_COURT_LIST, allocatedCourtList.getOrDefault(CLEAVELAND_COURT_LIST_AC, EMPTY_STRING));
                    case NWYORKSHIRE ->
                        caseData.put(NWYORKSHIRE_COURT_LIST, allocatedCourtList.getOrDefault(NWYORKSHIRE_COURT_LIST_AC, EMPTY_STRING));
                    case HSYORKSHIRE ->
                        caseData.put(HUMBER_COURT_LIST, allocatedCourtList.getOrDefault(HUMBER_COURT_LIST_AC, EMPTY_STRING));
                    default -> {
                        return caseData;
                    }
                }
            }
            case SOUTHEAST -> {
                caseData.put(SOUTHEAST_FRC_LIST, allocatedCourtList.getOrDefault(SOUTHEAST_FRC_LIST_AC, EMPTY_STRING));
                String dataOrDefault = (String) caseData.getOrDefault(SOUTHEAST_FRC_LIST, EMPTY_STRING);
                if (dataOrDefault.equals(KENT)) {
                    caseData.put(KENT_SURREY_COURT_LIST, allocatedCourtList.getOrDefault(KENT_SURREY_COURT_LIST_AC, EMPTY_STRING));
                } else {
                    return caseData;
                }
            }
            default -> {
                return caseData;
            }
        }
        return Collections.emptyMap();
    }

    private static String getAllocatedListKey(Map<String, Object> caseData) {
        return caseData.containsKey(ALLOCATED_COURT_LIST_GA) ? ALLOCATED_COURT_LIST_GA : ALLOCATED_COURT_LIST;
    }

    private static Map<String, Object> setRegionSlCourtData(Map<String, Object> caseData) {
        String region = (String) caseData.getOrDefault(REGION_SL, EMPTY_STRING);
        caseData.put(REGION, region);
        caseData.remove(REGION_SL);

        switch (region) {
            case WALES -> {
                caseData.put(WALES_FRC_LIST, caseData.getOrDefault(WALES_FRC_LIST_SL, EMPTY_STRING));
                caseData.remove(WALES_FRC_LIST_SL);
                switch ((String) caseData.getOrDefault(WALES_FRC_LIST, EMPTY_STRING)) {
                    case NEWPORT -> {
                        caseData.put(NEWPORT_COURT_LIST, caseData.getOrDefault(NEWPORT_COURT_LIST_SL, EMPTY_STRING));
                        caseData.remove(NEWPORT_COURT_LIST_SL);
                    }
                    case SWANSEA -> {
                        caseData.put(SWANSEA_COURT_LIST, caseData.getOrDefault(SWANSEA_COURT_LIST_SL, EMPTY_STRING));
                        caseData.remove(SWANSEA_COURT_LIST_SL);
                    }
                    default -> {
                        return caseData;
                    }
                }
            }
            case LONDON -> {
                caseData.put(LONDON_FRC_LIST, caseData.getOrDefault(LONDON_FRC_LIST_SL, EMPTY_STRING));
                caseData.remove(LONDON_FRC_LIST_SL);
                String dataOrDefault = (String) caseData.getOrDefault(LONDON_FRC_LIST, EMPTY_STRING);
                if (dataOrDefault.equals(CFC)) {
                    caseData.put(CFC_COURT_LIST, caseData.getOrDefault(CFC_COURT_LIST_SL, EMPTY_STRING));
                    caseData.remove(CFC_COURT_LIST_SL);
                } else {
                    return caseData;
                }
            }
            case MIDLANDS -> {
                caseData.put(MIDLANDS_FRC_LIST, caseData.getOrDefault(MIDLANDS_FRC_LIST_SL, EMPTY_STRING));
                caseData.remove(MIDLANDS_FRC_LIST_SL);
                switch ((String) caseData.getOrDefault(MIDLANDS_FRC_LIST, EMPTY_STRING)) {
                    case NOTTINGHAM -> {
                        caseData.put(NOTTINGHAM_COURT_LIST, caseData.getOrDefault(NOTTINGHAM_COURT_LIST_SL, EMPTY_STRING));
                        caseData.remove(NOTTINGHAM_COURT_LIST_SL);
                    }
                    case BIRMINGHAM -> {
                        caseData.put(BIRMINGHAM_COURT_LIST, caseData.getOrDefault(BIRMINGHAM_COURT_LIST_SL, EMPTY_STRING));
                        caseData.remove(BIRMINGHAM_COURT_LIST_SL);
                    }
                    default -> {
                        return caseData;
                    }
                }
            }
            case NORTHWEST -> {
                caseData.put(NORTHWEST_FRC_LIST, caseData.getOrDefault(NORTHWEST_FRC_LIST_SL, EMPTY_STRING));
                caseData.remove(NORTHWEST_FRC_LIST_SL);
                switch ((String) caseData.getOrDefault(NORTHWEST_FRC_LIST, EMPTY_STRING)) {
                    case LIVERPOOL -> {
                        caseData.put(LIVERPOOL_COURT_LIST, caseData.getOrDefault(LIVERPOOL_COURT_LIST_SL, EMPTY_STRING));
                        caseData.remove(LIVERPOOL_COURT_LIST_SL);
                    }
                    case MANCHESTER -> {
                        caseData.put(MANCHESTER_COURT_LIST, caseData.getOrDefault(MANCHESTER_COURT_LIST_SL, EMPTY_STRING));
                        caseData.remove(MANCHESTER_COURT_LIST_SL);
                    }
                    default -> {
                        return caseData;
                    }
                }
            }
            case NORTHEAST -> {
                caseData.put(NORTHEAST_FRC_LIST, caseData.getOrDefault(NORTHEAST_FRC_LIST_SL, EMPTY_STRING));
                caseData.remove(NORTHEAST_FRC_LIST_SL);
                switch ((String) caseData.getOrDefault(NORTHEAST_FRC_LIST, EMPTY_STRING)) {
                    case CLEAVELAND -> {
                        caseData.put(CLEAVELAND_COURT_LIST, caseData.getOrDefault(CLEAVELAND_COURT_LIST_SL, EMPTY_STRING));
                        caseData.remove(CLEAVELAND_COURT_LIST_SL);
                    }
                    case NWYORKSHIRE -> {
                        caseData.put(NWYORKSHIRE_COURT_LIST, caseData.getOrDefault(NWYORKSHIRE_COURT_LIST_SL, EMPTY_STRING));
                        caseData.remove(NWYORKSHIRE_COURT_LIST_SL);
                    }
                    case HSYORKSHIRE -> {
                        caseData.put(HUMBER_COURT_LIST, caseData.getOrDefault(HUMBER_COURT_LIST_SL, EMPTY_STRING));
                        caseData.remove(HUMBER_COURT_LIST_SL);
                    }
                    default -> {
                        return caseData;
                    }
                }
            }
            case SOUTHEAST -> {
                caseData.put(SOUTHEAST_FRC_LIST, caseData.getOrDefault(SOUTHEAST_FRC_LIST_SL, EMPTY_STRING));
                caseData.remove(SOUTHEAST_FRC_LIST_SL);
                String dataOrDefault = (String) caseData.getOrDefault(SOUTHEAST_FRC_LIST, EMPTY_STRING);
                if (dataOrDefault.equals(KENT)) {
                    caseData.put(KENT_SURREY_COURT_LIST, caseData.getOrDefault(KENT_SURREY_COURT_LIST_SL, EMPTY_STRING));
                    caseData.remove(KENT_SURREY_COURT_LIST_SL);
                } else {
                    return caseData;
                }
            }
            default -> {
                return caseData;
            }
        }
        return Collections.emptyMap();
    }
}
