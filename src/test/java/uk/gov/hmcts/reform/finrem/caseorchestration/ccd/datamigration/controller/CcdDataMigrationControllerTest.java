package uk.gov.hmcts.reform.finrem.caseorchestration.ccd.datamigration.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.finrem.caseorchestration.CaseOrchestrationApplication;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.datamigration.model.prod.ProdCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.datamigration.model.v1.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.datamigration.service.MigrationService;

import javax.ws.rs.core.MediaType;

import java.io.File;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(CcdDataMigrationController.class)
@ContextConfiguration(classes = CaseOrchestrationApplication.class)
public class CcdDataMigrationControllerTest {

    private static final String CCD_DATA_MIGRATION_URL = "/ccd-data-migration/migrate";
    private static final String AUTH_TOKEN = "Bearer eyJhbGJbpjciOiJIUzI1NiJ9";
    private static final String CASE_WITH_REDUNDANT_ADDRESS_LINES =
            "/fixtures/migration/caseWithRedundantAddressLines.json";
    private static final String CASE_WITHOUT__REDUNDANT_ADDRESS_LINES =
            "/fixtures/migration/caseWithRedundantAddressLines.json";

    @Autowired
    private WebApplicationContext applicationContext;

    @MockBean
    private MigrationService migrationService;
    private MockMvc mockMvc;
    private ProdCaseDetails prodCaseDetails;
    private CaseDetails caseDetails;
    private JsonNode requestContent;

    private ObjectMapper objectMapper;


    @Before
    public void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.webAppContextSetup(applicationContext).build();
        prodCaseDetails = new ProdCaseDetails();
        prodCaseDetails.setCaseId("111111");
        caseDetails = new CaseDetails();
        caseDetails.setCaseId("111111");
    }

    @Test
    public void shouldRemoveRedundantAddressFields_whenRedundantFieldsAreAvailable() throws Exception {
        buildCcdRequest(CASE_WITH_REDUNDANT_ADDRESS_LINES);
        when(migrationService.migrateTov1(any(ProdCaseDetails.class))).thenReturn(caseDetails);
        mockMvc.perform(post(CCD_DATA_MIGRATION_URL)
                .content(requestContent.toString())
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errors", hasSize(0)))
                .andExpect(jsonPath("$.warnings", hasSize(0)));
        verify(migrationService, times(1)).migrateTov1(any(ProdCaseDetails.class));
    }

    @Test
    public void shouldRemoveRedundantAddressFields_whenRedundantFieldsAreNotAvailable() throws Exception {
        buildCcdRequest(CASE_WITHOUT__REDUNDANT_ADDRESS_LINES);
        when(migrationService.migrateTov1(any(ProdCaseDetails.class))).thenReturn(caseDetails);
        mockMvc.perform(post(CCD_DATA_MIGRATION_URL)
                .content(requestContent.toString())
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errors", hasSize(0)))
                .andExpect(jsonPath("$.warnings", hasSize(0)));
        verify(migrationService, times(1)).migrateTov1(any(ProdCaseDetails.class));
    }

    private void buildCcdRequest(String path) throws Exception {
        requestContent = objectMapper.readTree(new File(getClass()
                .getResource(path).toURI()));
    }


}