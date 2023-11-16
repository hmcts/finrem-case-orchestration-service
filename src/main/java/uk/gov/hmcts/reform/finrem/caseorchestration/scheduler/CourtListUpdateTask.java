package uk.gov.hmcts.reform.finrem.caseorchestration.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseEventDetail;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.CourtDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CcdService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SystemUserService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.csv.CaseReferenceCsvLoader;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

@Component
@Slf4j
public class CourtListUpdateTask extends BaseTask {

    @Value("${cron.courtListUpdate.enabled:false}")
    private boolean isCourtListUpdateTaskEnabled;

    private final CourtDetailsMapper courtDetailsMapper;
    private final CcdService ccdService;

    private final SystemUserService systemUserService;

    private final FinremCaseDetailsMapper finremCaseDetailsMapper;

    @Autowired
    protected CourtListUpdateTask(CaseReferenceCsvLoader csvLoader,
                                  CcdService ccdService,
                                  SystemUserService systemUserService,
                                  FinremCaseDetailsMapper finremCaseDetailsMapper,
                                  CourtDetailsMapper courtDetailsMapper) {
        super(csvLoader, ccdService, systemUserService, finremCaseDetailsMapper);
        this.courtDetailsMapper = courtDetailsMapper;
        this.ccdService = ccdService;
        this.systemUserService = systemUserService;
        this.finremCaseDetailsMapper = finremCaseDetailsMapper;
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
    protected void executeTask(FinremCaseDetails finremCaseDetails) {
        FinremCaseData finremCaseData = finremCaseDetails.getData();
        List<CaseEventDetail> caseEventDetails = ccdService.getCcdEventDetailsOnCase(systemUserService.getSysUserToken(),
            finremCaseData);

        Collections.sort(caseEventDetails, (o1, o2) -> o2.getCreatedDate()
            .compareTo(o1.getCreatedDate()));


        if (CollectionUtils.isNotEmpty(caseEventDetails)) {

            int caseEventDetailsIndex = IntStream.range(0, caseEventDetails.size())
                .filter(index -> (EventType.UPDATE_FRC_INFORMATION.getCcdType().equals(caseEventDetails.get(index).getEventName())
                                || EventType.AMEND_CONTESTED_APP_DETAILS.getCcdType().equals(caseEventDetails.get(index).getEventName())
                                || EventType.GIVE_ALLOCATION_DIRECTIONS.getCcdType().equals(caseEventDetails.get(index).getEventName())))
                .findFirst().getAsInt();


            Map<String, Object> caseDetailsBefore = caseEventDetails.get(caseEventDetailsIndex + 1).getData();
            FinremCaseData finremCaseDataBefore = finremCaseDetailsMapper
                .mapToFinremCaseData(caseDetailsBefore, getCaseType().getCcdType());
            finremCaseData.getRegionWrapper()
                .setAllocatedRegionWrapper(
                    courtDetailsMapper.getLatestAllocatedCourt(
                        finremCaseDataBefore.getRegionWrapper().getAllocatedRegionWrapper(),
                        finremCaseData.getRegionWrapper().getAllocatedRegionWrapper(),
                        false));
        }
    }
}
