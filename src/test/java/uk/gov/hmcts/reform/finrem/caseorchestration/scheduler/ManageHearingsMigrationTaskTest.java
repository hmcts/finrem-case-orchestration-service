package uk.gov.hmcts.reform.finrem.caseorchestration.scheduler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CcdService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SystemUserService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.managehearings.ManageHearingsMigrationService;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;

class ManageHearingsMigrationTaskTest {

    private ManageHearingsMigrationTask underTest;

    private CcdService ccdService;

    private SystemUserService systemUserService;

    private ManageHearingsMigrationService manageHearingsMigrationService;

    private final FinremCaseDetailsMapper finremCaseDetailsMapper = new FinremCaseDetailsMapper(
        new ObjectMapper().registerModule(new JavaTimeModule()));

    @BeforeEach
    void setup() {
        ccdService = mock(CcdService.class);
        systemUserService = mock(SystemUserService.class);
        manageHearingsMigrationService = mock(ManageHearingsMigrationService.class);

        underTest = new ManageHearingsMigrationTask(ccdService, systemUserService,
            finremCaseDetailsMapper, manageHearingsMigrationService);
        underTest.setTaskEnabled(true);
    }

    @Test
    void givenTaskNotEnabledWhenRunThenDoesNothing() {
        underTest.setTaskEnabled(false);
        underTest.run();

        verifyNoInteractions(ccdService);
        verifyNoInteractions(systemUserService);
        verifyNoInteractions(manageHearingsMigrationService);
    }

    @Test
    void givenNoCasesNeedUpdatingWhenRunThenNoUpdatesExecuted() {
        when(systemUserService.getSysUserToken()).thenReturn(AUTH_TOKEN);

        SearchResult searchResult = createSearchResult(Collections.emptyList());
        when(ccdService.esSearchCases(any(CaseType.class), anyString(), anyString())).thenReturn(searchResult);

        underTest.run();

        verify(ccdService, times(1)).esSearchCases(any(CaseType.class), anyString(), anyString());
        verifyNoMoreInteractions(ccdService);
        verifyNoInteractions(manageHearingsMigrationService);
    }

    private SearchResult createSearchResult(List<CaseDetails> cases) {
        return SearchResult.builder()
            .cases(cases)
            .total(cases.size())
            .build();
    }
}
