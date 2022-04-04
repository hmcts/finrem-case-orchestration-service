package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangeOfRepresentation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangeOfRepresentatives;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangedRepresentative;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Organisation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrganisationPolicy;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_ORGANISATION_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CHANGE_OF_REPRESENTATIVES;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_RESPONDENT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_RESPONDENT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NOC_PARTY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_ORGANISATION_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_NAME;

@Service
@RequiredArgsConstructor
@Slf4j
public class NoticeOfChangeService {

    private static final String APPLICANT = "applicant";
    private static final String RESPONDENT = "respondent";
    private static final String NOTICE_OF_CHANGE = "Notice of Change";
    private static final String NATURE_OF_CHANGE = "natureOfRepresentationChange";
    private static final String REMOVED_VALUE = "removing";
    private static final String REPLACING_VALUE = "replacing";
    private static final String PREVIOUS_APP_POLICY = "ApplicantPreviousRepresentative";
    private static final String PREVIOUS_RESP_POLICY = "RespondentPreviousRepresentative";

    private CaseDataService caseDataService;
    private IdamService idamService;
    private ObjectMapper objectMapper;

    @Autowired
    public NoticeOfChangeService(CaseDataService caseDataService, IdamService idamService) {
        this.caseDataService = caseDataService;
        this.idamService = idamService;
        this.objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public Map<String, Object> updateRepresentation(CaseDetails caseDetails,
                                                    String authorizationToken) {

        Map<String,Object> caseData = caseDetails.getData();

        ChangedRepresentative changedRepresentative = generateChangedRepresentative(caseDetails);

        ChangeOfRepresentation changeOfRepresentation = generateChangeOfRepresentation(caseDetails, authorizationToken,
            changedRepresentative);

        ChangeOfRepresentatives changeOfRepresentatives = updateChangeOfRepresentatives(caseDetails, changeOfRepresentation);

        caseData.put(CHANGE_OF_REPRESENTATIVES, changeOfRepresentatives);

        return caseData;
    }

    public Map<String, Object> savePreviousOrganisation(CaseDetails caseDetails) {

        Map<String, Object> caseData = caseDetails.getData();
        boolean isApplicant = ((String) caseData.get(NOC_PARTY)).equalsIgnoreCase(APPLICANT);
        boolean hasSolicitor = isApplicant ? caseDataService.isApplicantRepresentedByASolicitor(caseData)
            : caseDataService.isRespondentRepresentedByASolicitor(caseData);

        if (hasSolicitor) {
            log.info("Party concerned has solicitor, moving to previous organisation field for caseID {}", caseDetails.getId());
            ChangedRepresentative changedRepresentative = generateChangedRepresentative(caseDetails);

            caseData.put(isApplicant ? PREVIOUS_APP_POLICY : PREVIOUS_RESP_POLICY, changedRepresentative);

            if (caseData.get(NATURE_OF_CHANGE).equals(REMOVED_VALUE)) {
                log.info("Nature of representation change is removing, setting " +
                    "isRepresented to No for party for caseID {}", caseDetails.getId());
                caseData = setIsRepresentedFieldToNo(caseDetails);
            }
        }

        return caseData;
    }

    private Map<String, Object> setIsRepresentedFieldToNo(CaseDetails caseDetails) {

        Map<String, Object> caseData = caseDetails.getData();
        boolean isApplicant = ((String) caseData.get(NOC_PARTY)).equalsIgnoreCase(APPLICANT);
        String respRepresented = caseDataService.isConsentedApplication(caseDetails)
            ? CONSENTED_RESPONDENT_REPRESENTED
            : CONTESTED_RESPONDENT_REPRESENTED;

        caseData.put(isApplicant ? APPLICANT_REPRESENTED : respRepresented, NO_VALUE);

        return caseData;
    }

    private ChangedRepresentative generateChangedRepresentative(CaseDetails caseDetails) {

        Map<String, Object> caseData = caseDetails.getData();
        boolean isApplicant = ((String) caseData.get(NOC_PARTY)).equalsIgnoreCase(APPLICANT);

        log.info("Generating changed representative for caseID{}", caseDetails.getId());
        OrganisationPolicy representativeOrgPolicy = getOrganisationPolicy(caseData, isApplicant);

        Organisation representativeOrg = representativeOrgPolicy.getOrganisation();


        return isApplicant
            ? ChangedRepresentative.builder()
            .name(caseDataService.getApplicantSolicitorName(caseDetails))
            .email(caseDataService.getApplicantSolicitorEmail(caseDetails))
            .organisation(representativeOrg)
            .build()
            : ChangedRepresentative.builder()
            .name((String) caseData.get(RESP_SOLICITOR_NAME))
            .email((String) caseData.get(RESP_SOLICITOR_EMAIL))
            .organisation(representativeOrg)
            .build();
    }

    private ChangeOfRepresentation generateChangeOfRepresentation(CaseDetails caseDetails,
                                                                  String authorizationToken,
                                                                  ChangedRepresentative changedRepresentative) {

        Map<String, Object> caseData = caseDetails.getData();
        boolean isApplicant = caseData.get(NOC_PARTY).equals(APPLICANT);
        String party = isApplicant ? APPLICANT : RESPONDENT;
        String clientName = isApplicant ? caseDataService.buildFullApplicantName(caseDetails) : caseDataService.buildFullRespondentName(caseDetails);
        boolean isRemoved = caseData.get(NATURE_OF_CHANGE).equals(REMOVED_VALUE);

        log.info("Generating change of representation for caseID {}", caseDetails.getId());
        return ChangeOfRepresentation.builder()
                .party(party)
                .clientName(clientName)
                .date(LocalDate.now())
                .by(idamService.getIdamFullName(authorizationToken))
                .via(NOTICE_OF_CHANGE)
                .added(!isRemoved ? changedRepresentative : null)
                .removed(getRemovedRepresentative(caseData, changedRepresentative))
                .build();
    }

    private ChangedRepresentative getRemovedRepresentative(Map<String, Object> caseData, ChangedRepresentative changedRepresentative) {
        boolean isApplicant = caseData.get(NOC_PARTY).equals(APPLICANT);

        if (caseData.get(NATURE_OF_CHANGE).equals(REMOVED_VALUE)) {
            return changedRepresentative;
        }

        if (caseData.get(NATURE_OF_CHANGE).equals(REPLACING_VALUE)) {
            return objectMapper.convertValue(isApplicant ? caseData.get(PREVIOUS_APP_POLICY)
                : caseData.get(PREVIOUS_RESP_POLICY), ChangedRepresentative.class);
        }

        return null;
    }

    private ChangeOfRepresentatives updateChangeOfRepresentatives(CaseDetails caseDetails, ChangeOfRepresentation latestRepresentationChange) {
        Map<String, Object> caseData = caseDetails.getData();

        log.info("Updating Change of Representatives field for caseID {}", caseDetails.getId());

        if (Optional.ofNullable(caseData.get(CHANGE_OF_REPRESENTATIVES)).isEmpty()) {
            return ChangeOfRepresentatives.builder()
                .changeOfRepresentation(List.of(latestRepresentationChange))
                .build();
        } else {
            ChangeOfRepresentatives changeOfRepresentatives = convertToChangeOfRepresentatives(caseData.get(CHANGE_OF_REPRESENTATIVES));
            changeOfRepresentatives.addChangeOfRepresentation(latestRepresentationChange);
            return changeOfRepresentatives;
        }
    }

    private ChangeOfRepresentatives convertToChangeOfRepresentatives(Object object) {
        return objectMapper.convertValue(object, new TypeReference<>() {});
    }

    private OrganisationPolicy getOrganisationPolicy(Map<String, Object> caseData, boolean isApplicant) {

        return isApplicant
            ? objectMapper.convertValue(caseData.get(APPLICANT_ORGANISATION_POLICY), OrganisationPolicy.class)
            : objectMapper.convertValue(caseData.get(RESPONDENT_ORGANISATION_POLICY), OrganisationPolicy.class);
    }

}
