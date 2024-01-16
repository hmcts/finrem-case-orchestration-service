package uk.gov.hmcts.reform.finrem.caseorchestration.handler.documentcatergory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.scheduler.CfvMigrationTask;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
public class InitiateCfvMigrationAboutToSubmitHandler extends FinremCallbackHandler {

    private final CfvMigrationTask cfvMigrationTask;

    public InitiateCfvMigrationAboutToSubmitHandler(FinremCaseDetailsMapper finremCaseDetailsMapper, CfvMigrationTask cfvMigrationTask) {
        super(finremCaseDetailsMapper);
        this.cfvMigrationTask = cfvMigrationTask;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && (EventType.INITIATE_CFV_MIGRATION.equals(eventType));
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequestWithFinremCaseDetails,
                                                                              String userAuthorisation) {

        log.info("Received request to initiate CFV migration for case with Case ID: {}",
            callbackRequestWithFinremCaseDetails.getCaseDetails().getId());

        ExecutorService executor = Executors.newFixedThreadPool(1);
        try {
            executor.submit(() -> {
                cfvMigrationTask.run();
            });
        }
        catch (RuntimeException e) {
            log.error("Error occurred while running CFV migration task", e);
            e.printStackTrace();
        }
        finally {
            executor.shutdown();
        }
        log.info("Migrate data called");

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(callbackRequestWithFinremCaseDetails.getCaseDetails().getData()).build();
    }
}
