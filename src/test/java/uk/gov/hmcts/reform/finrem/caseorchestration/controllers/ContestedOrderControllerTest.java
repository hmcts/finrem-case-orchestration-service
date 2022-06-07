package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import org.junit.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;

import static com.jayway.jsonassert.impl.matcher.IsCollectionWithSize.hasSize;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;

@WebMvcTest(ContestedOrderController.class)
public class ContestedOrderControllerTest extends BaseControllerTest {

    private static final String CONTESTED_VALIDATE_HEARING_SUCCESSFULLY_JSON = "/fixtures/contested/validate-hearing-successfully.json";
    private static final String CONTESTED_VALIDATE_HEARING_DATE_JSON = "/fixtures/contested/manage-bundle-validate-hearing-date.json";
    private static final String CONTESTED_VALIDATE_INVALID_DOC_JSON = "/fixtures/contested/manage-bundle-invalidate-document.json";

    @MockBean
    private IdamService idamService;
    @MockBean
    private CaseDataService caseDataService;

    @Test
    public void shouldThrowExceptionWhenHearingDateNotFound() throws Exception {
        when(idamService.isUserRoleAdmin(isA(String.class))).thenReturn(false);
        when(caseDataService.isContestedApplication(any())).thenReturn(true);

        loadRequestContentWith(CONTESTED_VALIDATE_HEARING_DATE_JSON);
        mvc.perform(post("/case-orchestration//contested/validateHearingDate")
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.errors").isArray())
            .andExpect(jsonPath("$.errors", hasSize(1)))
            .andExpect(jsonPath("$.errors", hasItem("Missing hearing date.")));
    }

    @Test
    public void shouldSuccessfullyProcessWhenHearingDateFound() throws Exception {
        when(idamService.isUserRoleAdmin(isA(String.class))).thenReturn(false);
        when(caseDataService.isContestedApplication(any())).thenReturn(true);

        loadRequestContentWith(CONTESTED_VALIDATE_HEARING_SUCCESSFULLY_JSON);
        mvc.perform(post("/case-orchestration//contested/validateHearingDate")
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.hearingDate", is("2019-05-04")));
    }

    @Test
    public void putLastetBundleOnTop() throws Exception {
        when(idamService.isUserRoleAdmin(isA(String.class))).thenReturn(false);
        when(caseDataService.isContestedApplication(any())).thenReturn(true);

        loadRequestContentWith(CONTESTED_VALIDATE_HEARING_DATE_JSON);
        mvc.perform(post("/case-orchestration/contested/sortUploadedHearingBundles")
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.hearingUploadBundle").isArray())
            .andExpect(jsonPath("$.data.hearingUploadBundle[0].value.hearingBundleFdr",
                is("NO")))
            .andExpect(jsonPath("$.data.hearingUploadBundle[0].id",
                is("83922295-dbaa-471f-95ff-93efdf200fab")))
            .andExpect(jsonPath("$.data.hearingUploadBundle[1].value.hearingBundleDate",
                is("2022-08-20")))
            .andExpect(jsonPath("$.data.hearingUploadBundle[1].value.hearingBundleDocuments[0]"
                    + ".value.bundleDocuments.document_filename",
                is("NocLitigantSolicitorAddedLetter.pdf")))
            .andExpect(jsonPath("$.data.hearingUploadBundle[1].value.hearingBundleDocuments[1]"
                    + ".value.bundleDocuments.document_filename",
                is("InterimHearingNotice-1649341720076259.pdf")))
            .andExpect(jsonPath("$.data.hearingUploadBundle[1].id",
                is("d090f7a0-5897-4577-a07f-2137483cb1f9")))
            .andExpect(jsonPath("$.data.hearingUploadBundle[1].value.hearingBundleFdr",
                is("YES")));

    }

    @Test
    public void shouldThrowErrorWhenUploadedDocIsNotPdf() throws Exception {
        when(idamService.isUserRoleAdmin(isA(String.class))).thenReturn(false);
        when(caseDataService.isContestedApplication(any())).thenReturn(true);

        loadRequestContentWith(CONTESTED_VALIDATE_INVALID_DOC_JSON);
        mvc.perform(post("/case-orchestration//contested/sortUploadedHearingBundles")
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.errors").isArray())
            .andExpect(jsonPath("$.errors", hasItem(endsWith("Please upload bundle in pdf format."))));
    }
}
