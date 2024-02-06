package uk.gov.hmcts.reform.finrem.caseorchestration.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.ConsentedHearingHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.CourtDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ConsentedHearingDataWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CcdService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SystemUserService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.csv.CaseReferenceKeyValue;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.csv.CaseReferenceKeyValueCsvLoader;

import java.lang.reflect.Field;
import java.util.List;

@Component
@Slf4j
public class CourtListUpdateTask extends SpecializedBaseTask {

    @Value("${cron.courtListUpdate.enabled:false}")
    private boolean isCourtListUpdateTaskEnabled;

    private ConsentedHearingHelper consentedHearingHelper;

    @Autowired
    protected CourtListUpdateTask(CaseReferenceKeyValueCsvLoader csvLoader,
                                  CcdService ccdService,
                                  SystemUserService systemUserService,
                                  FinremCaseDetailsMapper finremCaseDetailsMapper,
                                  CourtDetailsMapper courtDetailsMapper,
                                  ConsentedHearingHelper consentedHearingHelper) {
        super(csvLoader, ccdService, systemUserService, finremCaseDetailsMapper);
        this.consentedHearingHelper = consentedHearingHelper;
    }

    @Override
    protected String getCaseListFileName() {
        return "courtListUpdateCaseReferenceList.csv";
    }

    @Override
    protected String getTaskName() {
        return "CourtListUpdateTask";
    }

    @Override
    protected boolean isTaskEnabled() {
        return isCourtListUpdateTaskEnabled;
    }

    @Override
    protected CaseType getCaseType() {
        return CaseType.CONSENTED;
    }

    @Override
    protected String getSummary() {
        return "Update Court list DFR-2653";
    }

    @Override
    protected void executeTask(FinremCaseDetails caseDetails, CaseReferenceKeyValue caseReferenceKeyValue) {
        List<ConsentedHearingDataWrapper> listOfHearings = consentedHearingHelper.getHearings(caseDetails.getData());

        ConsentedHearingDataWrapper hearing = listOfHearings.stream().filter(ch -> caseReferenceKeyValue
                .getPreviousFRCKey().equals(ch.getId())).findFirst().orElse(null);

        if (hearing != null) {
            log.info("Found matching hearing id {} for case id ()", hearing.getId(), caseDetails.getId());
            try {
                // setting frc
                Field fieldFrc = hearing.getValue().getClass().getDeclaredField(caseReferenceKeyValue.getPreviousFRCValue());
                FieldUtils.writeField(hearing.getValue(), fieldFrc.getName(), null, true);
                // setting court
                Field fieldCourt = hearing.getValue().getClass().getDeclaredField(caseReferenceKeyValue.getPreviousCourtListKey());
                FieldUtils.writeField(hearing.getValue(), fieldCourt.getName(), null, true);

                caseDetails.getData().setListForHearings(listOfHearings);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                log.error("No field found error", e.getMessage());
                e.printStackTrace();
            }
        }

    }


}
