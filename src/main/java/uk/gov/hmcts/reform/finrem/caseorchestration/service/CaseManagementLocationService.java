package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.CourtDetailsParseException;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseLocation;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.nio.charset.StandardCharsets.UTF_8;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CASE_MANAGEMENT_LOCATION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseHearingFunctions.getSelectedCourt;

@Service
@Slf4j
public class CaseManagementLocationService {

    private final String courtIdMappingJsonFile;

    private final ObjectMapper objectMapper;
    private final CaseDataService caseDataService;

    public CaseManagementLocationService(@Value("${courtIdMappingJsonFile}") String courtIdMappingJsonFile,
                                         ObjectMapper objectMapper,
                                         CaseDataService caseDataService) {
        this.courtIdMappingJsonFile = courtIdMappingJsonFile;
        this.objectMapper = objectMapper;
        this.caseDataService = caseDataService;
    }

    public AboutToStartOrSubmitCallbackResponse setCaseManagementLocation(CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Map<String, Object> caseData = caseDetails.getData();
        List<String> errors = new ArrayList<>();

        String selectedCourtId = getSelectedCourtId(caseDetails);

        if (StringUtils.isBlank(selectedCourtId)) {
            errors.add("Selected court data is missing from caseData");
            return AboutToStartOrSubmitCallbackResponse.builder().data(caseData).errors(errors).build();
        }

        try {
            Map<String, Object> courtIdMap = objectMapper.readValue(getCourtIdMappingsString(),
                TypeFactory.defaultInstance().constructMapType(HashMap.class, String.class, Object.class));
            CaseLocation caseManagementLocation = objectMapper.convertValue(courtIdMap.get(selectedCourtId), CaseLocation.class);
            caseData.put(CASE_MANAGEMENT_LOCATION, caseManagementLocation);
        } catch (JsonProcessingException e) {
            errors.add(String.format("Error parsing court Ids: %s", e.getMessage()));
        }

        return AboutToStartOrSubmitCallbackResponse.builder().data(caseData).errors(errors).build();
    }

    private String getCourtIdMappingsString() {
        try (InputStream inputStream = this.getClass().getResourceAsStream(courtIdMappingJsonFile)) {
            return IOUtils.toString(inputStream, UTF_8);
        } catch (IOException e) {
            throw new CourtDetailsParseException();
        }
    }

    private String getSelectedCourtId(CaseDetails caseDetails) {
        Map<String, Object> caseData = caseDetails.getData();

        return caseDataService.isConsentedApplication(caseDetails)
            ? ConsentedCaseHearingFunctions.getSelectedCourt(caseData)
            : Objects.toString(caseData.get(getSelectedCourt(caseData)), StringUtils.EMPTY);
    }
}
