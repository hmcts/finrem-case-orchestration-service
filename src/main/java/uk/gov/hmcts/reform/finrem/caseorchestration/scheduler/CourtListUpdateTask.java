package uk.gov.hmcts.reform.finrem.caseorchestration.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.CourtDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CcdService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SystemUserService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.csv.CaseReferenceKeyValue;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.csv.CaseReferenceKeyValueCsvLoader;

@Component
@Slf4j
public class CourtListUpdateTask extends SpecializedBaseTask {

    @Value("${cron.courtListUpdate.enabled:false}")
    private boolean isCourtListUpdateTaskEnabled;

    @Autowired
    protected CourtListUpdateTask(CaseReferenceKeyValueCsvLoader csvLoader,
                                  CcdService ccdService,
                                  SystemUserService systemUserService,
                                  FinremCaseDetailsMapper finremCaseDetailsMapper,
                                  CourtDetailsMapper courtDetailsMapper) {
        super(csvLoader, ccdService, systemUserService, finremCaseDetailsMapper);
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
        return CaseType.CONTESTED;
    }

    @Override
    protected String getSummary() {
        return "Update Court list DFR-2488";
    }

    @Override
    protected void executeTask(CaseDetails caseDetails, CaseReferenceKeyValue caseReferenceKeyValue) {
        String frcValue = (String) caseDetails.getData().get(caseReferenceKeyValue.getPreviousFRCKey());
        String courtValue = (String) caseDetails.getData().get(caseReferenceKeyValue.getPreviousCourtListKey());

        if (frcValue != null && courtValue != null) {
            caseDetails.getData().put(caseReferenceKeyValue.getPreviousFRCKey(), null);
            caseDetails.getData().put(caseReferenceKeyValue.getPreviousCourtListKey(), null);
        }

    }


}
