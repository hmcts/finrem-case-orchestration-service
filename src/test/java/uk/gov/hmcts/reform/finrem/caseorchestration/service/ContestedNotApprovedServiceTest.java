package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ContestedApplicationNotApproved;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ContestedApplicationNotApprovedListEntry;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_APPLICATION_NOT_APPROVED;

public class ContestedNotApprovedServiceTest extends BaseServiceTest {

    @Autowired
    private ContestedNotApprovedService contestedNotApprovedService;
    @Autowired
    private ObjectMapper mapper;

    @Test
    public void whenContestedNotApprovedEntryAdded_newEntryAddedToExistingList() {
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource("/fixtures/contested/application-not-approved.json", mapper);
        contestedNotApprovedService.addContestedNotApprovedEntry(caseDetails);

        List<?> applicationNotApprovedList = (List<?>) caseDetails.getData().get(CONTESTED_APPLICATION_NOT_APPROVED);

        assertThat(applicationNotApprovedList, hasSize(2));

        ContestedApplicationNotApproved entryAdded = ((ContestedApplicationNotApprovedListEntry) applicationNotApprovedList.get(1))
            .getContestedApplicationNotApproved();
        assertThat(entryAdded.getReasonForRefusal(), hasItems("FR_ms_refusalReason_12", "FR_ms_refusalReason_8", "FR_ms_refusalReason_1"));
        assertThat(entryAdded.getOthersTextOrders(), is("other reason for not approving the application"));
        assertThat(entryAdded.getJudgeType(), is("District Judge"));
        assertThat(entryAdded.getJudgeName(), is("Her Justice"));
        assertThat(entryAdded.getDateOfOrder(), is(notNullValue()));
        assertThat(entryAdded.getNotApprovedDocument().getDocumentUrl(), is(
            "http://document-management-store:8080/documents/2c9d3381-df6a-4817-aec3-a8a46ca0631e"));
    }
}