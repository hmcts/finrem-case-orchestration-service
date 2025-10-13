package uk.gov.hmcts.reform.finrem.caseorchestration.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.BirminghamCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CfcCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ClevelandCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HumberCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.KentSurreyCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.LiverpoolCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.LondonCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ManchesterCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NewportCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NottinghamCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NwYorkshireCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.SwanseaCourt;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

@Component
@Getter
public class CourtDetailsConfiguration {
    private final Map<String, CourtDetails> courts;

    private final Map<String, String> midlandsCourtMap = Map.ofEntries(
        Map.entry(NottinghamCourt.CONSENTED_NOTTINGHAM_COUNTY_COURT_AND_FAMILY_COURT.getId(),
            NottinghamCourt.NOTTINGHAM_COUNTY_COURT_AND_FAMILY_COURT.getId()),
        Map.entry(NottinghamCourt.CONSENTED_DERBY_COMBINED_COURT_CENTRE.getId(),
            NottinghamCourt.DERBY_COMBINED_COURT_CENTRE.getId()),
        Map.entry(NottinghamCourt.CONSENTED_LEICESTER_COUNTY_COURT_AND_FAMILY_COURT.getId(),
            NottinghamCourt.LEICESTER_COUNTY_COURT_AND_FAMILY_COURT.getId()),
        Map.entry(NottinghamCourt.CONSENTED_LINCOLN_COUNTY_COURT_AND_FAMILY_COURT.getId(),
            NottinghamCourt.LINCOLN_COUNTY_COURT_AND_FAMILY_COURT.getId()),
        Map.entry(NottinghamCourt.CONSENTED_NORTHAMPTON_CROWN_COUNTY_AND_FAMILY_COURT.getId(),
            NottinghamCourt.NORTHAMPTON_CROWN_COUNTY_AND_FAMILY_COURT.getId()),
        Map.entry(NottinghamCourt.CONSENTED_CHESTERFIELD_COUNTY_COURT.getId(),
            NottinghamCourt.CHESTERFIELD_COUNTY_COURT.getId()),
        Map.entry(NottinghamCourt.CONSENTED_MANSFIELD_MAGISTRATES_AND_COUNTY_COURT.getId(),
            NottinghamCourt.MANSFIELD_MAGISTRATES_AND_COUNTY_COURT.getId()),
        Map.entry(NottinghamCourt.CONSENTED_BOSTON_COUNTY_COURT_AND_FAMILY_COURT.getId(),
            NottinghamCourt.BOSTON_COUNTY_COURT_AND_FAMILY_COURT.getId()),
        Map.entry(BirminghamCourt.CONSENTED_BIRMINGHAM_CIVIL_AND_FAMILY_JUSTICE_CENTRE.getId(),
            BirminghamCourt.BIRMINGHAM_CIVIL_AND_FAMILY_JUSTICE_CENTRE.getId()),
        Map.entry(BirminghamCourt.CONSENTED_COVENTRY_COMBINED_COURT_CENTRE.getId(),
            BirminghamCourt.COVENTRY_COMBINED_COURT_CENTRE.getId()),
        Map.entry(BirminghamCourt.CONSENTED_TELFORD_COUNTY_COURT_AND_FAMILY_COURT.getId(),
            BirminghamCourt.TELFORD_COUNTY_COURT_AND_FAMILY_COURT.getId()),
        Map.entry(BirminghamCourt.CONSENTED_WOLVERHAMPTON_COMBINED_COURT_CENTRE.getId(),
            BirminghamCourt.WOLVERHAMPTON_COMBINED_COURT_CENTRE.getId()),
        Map.entry(BirminghamCourt.CONSENTED_DUDLEY_COUNTY_COURT_AND_FAMILY_COURT.getId(),
            BirminghamCourt.DUDLEY_COUNTY_COURT_AND_FAMILY_COURT.getId()),
        Map.entry(BirminghamCourt.CONSENTED_WALSALL_COUNTY_AND_FAMILY_COURT.getId(),
            BirminghamCourt.WALSALL_COUNTY_AND_FAMILY_COURT.getId()),
        Map.entry(BirminghamCourt.CONSENTED_STOKE_ON_TRENT_COMBINED_COURT.getId(),
            BirminghamCourt.STOKE_ON_TRENT_COMBINED_COURT.getId()),
        Map.entry(BirminghamCourt.CONSENTED_WORCESTER_COMBINED_COURT.getId(),
            BirminghamCourt.WORCESTER_COMBINED_COURT.getId()),
        Map.entry(BirminghamCourt.CONSENTED_STAFFORD_COMBINED_COURT.getId(),
            BirminghamCourt.STAFFORD_COMBINED_COURT.getId()),
        Map.entry(BirminghamCourt.CONSENTED_HEREFORD_COUNTY_COURT_AND_FAMILY_COURT.getId(),
            BirminghamCourt.HEREFORD_COUNTY_COURT_AND_FAMILY_COURT.getId())
    );

    private final Map<String, String> londonCourtMap = Map.ofEntries(
        Map.entry(LondonCourt.CENTRAL_FAMILY_COURT.getId(),
            CfcCourt.CENTRAL_FAMILY_COURT.getId()),
        Map.entry(LondonCourt.WILLESDEN_COUNTY_COURT_AND_FAMILY_COURT.getId(),
            CfcCourt.WILLESDEN_COUNTY_COURT_AND_FAMILY_COURT.getId()),
        Map.entry(LondonCourt.UXBRIDGE_COUNTY_COURT_AND_FAMILY_COURT.getId(),
            CfcCourt.UXBRIDGE_COUNTY_COURT_AND_FAMILY_COURT.getId()),
        Map.entry(LondonCourt.EAST_LONDON_FAMILY_COURT.getId(),
            CfcCourt.EAST_LONDON_FAMILY_COURT.getId()),
        Map.entry(LondonCourt.BRENTFORD_COUNTY_AND_FAMILY_COURT.getId(),
            CfcCourt.BRENTFORD_COUNTY_AND_FAMILY_COURT.getId()),
        Map.entry(LondonCourt.BARNET_CIVIL_AND_FAMILY_COURTS_CENTRE.getId(),
            CfcCourt.BARNET_CIVIL_AND_FAMILY_COURTS_CENTRE.getId()),
        Map.entry(LondonCourt.ROMFORDCOUNTY_AND_FAMILY_COURT.getId(),
            CfcCourt.ROMFORDCOUNTY_AND_FAMILY_COURT.getId()),
        Map.entry(LondonCourt.KINGSTON_UPON_THAMES_COUNTY_COURT_AND_FAMILY_COURT.getId(),
            CfcCourt.KINGSTON_UPON_THAMES_COUNTY_COURT_AND_FAMILY_COURT.getId()),
        Map.entry(LondonCourt.EDMONTON_COUNTY_COURT_AND_FAMILY_COURT.getId(),
            CfcCourt.EDMONTON_COUNTY_COURT_AND_FAMILY_COURT.getId()),
        Map.entry(LondonCourt.CROYDON_COUNTY_COURT_AND_FAMILY_COURT.getId(),
            CfcCourt.CROYDON_COUNTY_COURT_AND_FAMILY_COURT.getId()),
        Map.entry(LondonCourt.BROMLEY_COUNTY_COURT_AND_FAMILY_COURT.getId(),
            CfcCourt.BROMLEY_COUNTY_COURT_AND_FAMILY_COURT.getId()),
        Map.entry(LondonCourt.THE_ROYAL_COURT_OF_JUSTICE.getId(),
            CfcCourt.THE_ROYAL_COURT_OF_JUSTICE.getId())
    );

    private final Map<String, String> northWestCourtMap = Map.ofEntries(
        Map.entry(LiverpoolCourt.CONSENTED_LIVERPOOL_CIVIL_FAMILY_COURT.getId(),
            LiverpoolCourt.LIVERPOOL_CIVIL_FAMILY_COURT.getId()),
        Map.entry(LiverpoolCourt.CONSENTED_CHESTER_CIVIL_FAMILY_JUSTICE.getId(),
            LiverpoolCourt.CHESTER_CIVIL_FAMILY_JUSTICE.getId()),
        Map.entry(LiverpoolCourt.CONSENTED_CREWE_COUNTY_FAMILY_COURT.getId(),
            LiverpoolCourt.CREWE_COUNTY_FAMILY_COURT.getId()),
        Map.entry(LiverpoolCourt.CONSENTED_ST_HELENS_COUNTY_FAMILY_COURT.getId(),
            LiverpoolCourt.ST_HELENS_COUNTY_FAMILY_COURT.getId()),
        Map.entry(LiverpoolCourt.CONSENTED_BIRKENHEAD_COUNTY_FAMILY_COURT.getId(),
            LiverpoolCourt.BIRKENHEAD_COUNTY_FAMILY_COURT.getId()),
        Map.entry(ManchesterCourt.CONSENTED_MANCHESTER_COURT.getId(),
            ManchesterCourt.MANCHESTER_COURT.getId()),
        Map.entry(ManchesterCourt.CONSENTED_STOCKPORT_COURT.getId(),
            ManchesterCourt.STOCKPORT_COURT.getId()),
        Map.entry(ManchesterCourt.CONSENTED_WIGAN_COURT.getId(),
            ManchesterCourt.WIGAN_COURT.getId())
    );

    private final Map<String, String> northEastCourtMap = Map.ofEntries(
        Map.entry(ClevelandCourt.FR_CLEVELAND_LIST_1.getId(),
            ClevelandCourt.FR_CLEVELAND_HC_LIST_1.getId()),
        Map.entry(ClevelandCourt.FR_CLEVELAND_LIST_2.getId(),
            ClevelandCourt.FR_CLEVELAND_HC_LIST_2.getId()),
        Map.entry(ClevelandCourt.FR_CLEVELAND_LIST_3.getId(),
            ClevelandCourt.FR_CLEVELAND_HC_LIST_3.getId()),
        Map.entry(ClevelandCourt.FR_CLEVELAND_LIST_4.getId(),
            ClevelandCourt.FR_CLEVELAND_HC_LIST_4.getId()),
        Map.entry(ClevelandCourt.FR_CLEVELAND_LIST_5.getId(),
            ClevelandCourt.FR_CLEVELAND_HC_LIST_5.getId()),
        Map.entry(ClevelandCourt.FR_CLEVELAND_LIST_6.getId(),
            ClevelandCourt.FR_CLEVELAND_HC_LIST_6.getId()),
        Map.entry(ClevelandCourt.FR_CLEVELAND_LIST_7.getId(),
            ClevelandCourt.FR_CLEVELAND_HC_LIST_7.getId()),
        Map.entry(ClevelandCourt.FR_CLEVELAND_LIST_8.getId(),
            ClevelandCourt.FR_CLEVELAND_HC_LIST_8.getId()),
        Map.entry(ClevelandCourt.FR_CLEVELAND_LIST_9.getId(),
            ClevelandCourt.FR_CLEVELAND_HC_LIST_9.getId()),
        Map.entry(NwYorkshireCourt.CONSENTED_HARROGATE_COURT.getId(),
            NwYorkshireCourt.HARROGATE_COURT.getId()),
        Map.entry(NwYorkshireCourt.CONSENTED_BRADFORD_COURT.getId(),
            NwYorkshireCourt.BRADFORD_COURT.getId()),
        Map.entry(NwYorkshireCourt.CONSENTED_HUDDERSFIELD_COURT.getId(),
            NwYorkshireCourt.HUDDERSFIELD_COURT.getId()),
        Map.entry(NwYorkshireCourt.CONSENTED_WAKEFIELD_COURT.getId(),
            NwYorkshireCourt.WAKEFIELD_COURT.getId()),
        Map.entry(NwYorkshireCourt.CONSENTED_YORK_COURT.getId(),
            NwYorkshireCourt.YORK_COURT.getId()),
        Map.entry(NwYorkshireCourt.CONSENTED_SCARBOROUGH_COURT.getId(),
            NwYorkshireCourt.SCARBOROUGH_COURT.getId()),
        Map.entry(NwYorkshireCourt.CONSENTED_LEEDS_COURT.getId(),
            NwYorkshireCourt.LEEDS_COURT.getId()),
        Map.entry(NwYorkshireCourt.CONSENTED_PRESTON_COURT.getId(),
            NwYorkshireCourt.PRESTON_COURT.getId()),
        Map.entry(HumberCourt.CONSENTED_FR_humberList_1.getId(),
            HumberCourt.FR_humberList_1.getId()),
        Map.entry(HumberCourt.CONSENTED_FR_humberList_2.getId(),
            HumberCourt.FR_humberList_2.getId()),
        Map.entry(HumberCourt.CONSENTED_FR_humberList_3.getId(),
            HumberCourt.FR_humberList_3.getId()),
        Map.entry(HumberCourt.CONSENTED_FR_humberList_4.getId(),
            HumberCourt.FR_humberList_4.getId()),
        Map.entry(HumberCourt.CONSENTED_FR_humberList_5.getId(),
            HumberCourt.FR_humberList_5.getId())
    );

    private final Map<String, String> southEastCourtMap = Map.ofEntries(
        Map.entry(KentSurreyCourt.CONSENTED_FR_kent_surreyList_1.getId(),
            KentSurreyCourt.FR_kent_surreyList_1.getId()),
        Map.entry(KentSurreyCourt.CONSENTED_FR_kent_surreyList_2.getId(),
            KentSurreyCourt.FR_kent_surreyList_2.getId()),
        Map.entry(KentSurreyCourt.CONSENTED_FR_kent_surreyList_3.getId(),
            KentSurreyCourt.FR_kent_surreyList_3.getId()),
        Map.entry(KentSurreyCourt.CONSENTED_FR_kent_surreyList_4.getId(),
            KentSurreyCourt.FR_kent_surreyList_4.getId()),
        Map.entry(KentSurreyCourt.CONSENTED_FR_kent_surreyList_5.getId(),
            KentSurreyCourt.FR_kent_surreyList_5.getId()),
        Map.entry(KentSurreyCourt.CONSENTED_FR_kent_surreyList_6.getId(),
            KentSurreyCourt.FR_kent_surreyList_6.getId()),
        Map.entry(KentSurreyCourt.CONSENTED_FR_kent_surreyList_7.getId(),
            KentSurreyCourt.FR_kent_surreyList_7.getId()),
        Map.entry(KentSurreyCourt.CONSENTED_FR_kent_surreyList_8.getId(),
            KentSurreyCourt.FR_kent_surreyList_8.getId()),
        Map.entry(KentSurreyCourt.CONSENTED_FR_kent_surreyList_9.getId(),
            KentSurreyCourt.FR_kent_surreyList_9.getId()),
        Map.entry(KentSurreyCourt.CONSENTED_FR_kent_surreyList_10.getId(),
            KentSurreyCourt.FR_kent_surreyList_10.getId()),
        Map.entry(KentSurreyCourt.CONSENTED_FR_kent_surreyList_11.getId(),
            KentSurreyCourt.FR_kent_surreyList_11.getId())
    );

    private final Map<String, String> walesCourtMap = Map.ofEntries(
        Map.entry(NewportCourt.CONSENTED_FR_newportList_1.getId(),
            NewportCourt.FR_newportList_1.getId()),
        Map.entry(NewportCourt.CONSENTED_FR_newportList_2.getId(),
            NewportCourt.FR_newportList_2.getId()),
        Map.entry(NewportCourt.CONSENTED_FR_newportList_3.getId(),
            NewportCourt.FR_newportList_3.getId()),
        Map.entry(NewportCourt.CONSENTED_FR_newportList_4.getId(),
            NewportCourt.FR_newportList_4.getId()),
        Map.entry(NewportCourt.CONSENTED_FR_newportList_5.getId(),
            NewportCourt.FR_newportList_5.getId()),
        Map.entry(SwanseaCourt.CONSENTED_FR_swanseaList_1.getId(),
            SwanseaCourt.FR_swanseaList_1.getId()),
        Map.entry(SwanseaCourt.CONSENTED_FR_swanseaList_2.getId(),
            SwanseaCourt.FR_swanseaList_2.getId()),
        Map.entry(SwanseaCourt.CONSENTED_FR_swanseaList_3.getId(),
            SwanseaCourt.FR_swanseaList_3.getId()),
        Map.entry(SwanseaCourt.CONSENTED_FR_swanseaList_4.getId(),
            SwanseaCourt.FR_swanseaList_4.getId()),
        Map.entry(SwanseaCourt.CONSENTED_FR_swanseaList_5.getId(),
            SwanseaCourt.FR_swanseaList_5.getId()),
        Map.entry(SwanseaCourt.CONSENTED_FR_swanseaList_6.getId(),
            SwanseaCourt.FR_swanseaList_6.getId())
    );

    public CourtDetailsConfiguration(ObjectMapper objectMapper) throws IOException {
        try (InputStream inputStream = CourtDetailsConfiguration.class
            .getResourceAsStream("/json/court-details.json")) {
            courts = objectMapper.readValue(inputStream, new TypeReference<>() {
            });
            addConsentedCourts();
        }
    }

    private void addConsentedCourts() {
        addConsentedCourts(midlandsCourtMap);
        addConsentedCourts(londonCourtMap);
        addConsentedCourts(northWestCourtMap);
        addConsentedCourts(northEastCourtMap);
        addConsentedCourts(southEastCourtMap);
        addConsentedCourts(walesCourtMap);
    }

    /**
     * Adds court details for a consented court id by finding its contested equivalent.
     *
     * @param courtMap map of consented to contested court ids
     */
    private void addConsentedCourts(Map<String, String> courtMap) {
        courtMap.forEach((key, value) -> {
            CourtDetails courtDetails = courts.get(value);
            assert courtDetails != null : "No contested court details found for id: " + value;
            courts.put(key, courtDetails);
        });
    }
}
