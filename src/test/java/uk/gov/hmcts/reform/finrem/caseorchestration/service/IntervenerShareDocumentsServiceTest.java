package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseAssignmentUserRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDataContested;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.UploadCaseDocumentWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.util.AssertionErrors.assertEquals;
import static org.springframework.test.util.AssertionErrors.assertNull;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty.INTERVENER_ONE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType.APPLICANT_FORM_E;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType.CARE_PLAN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType.CASE_SUMMARY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType.CHRONOLOGY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType.EXPERT_EVIDENCE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType.FORM_H;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType.OTHER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType.QUESTIONNAIRE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType.STATEMENT_AFFIDAVIT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType.TRIAL_BUNDLE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole.RESP_SOLICITOR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.INTERVENER_FOUR_OTHER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.INTERVENER_ONE_OTHER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.INTERVENER_THREE_OTHER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.INTERVENER_TWO_OTHER_COLLECTION;

@ExtendWith(MockitoExtension.class)
class IntervenerShareDocumentsServiceTest {

    private IntervenerShareDocumentsService intervenerShareDocumentsService;
    private final ThreadLocal<UUID> uuid = new ThreadLocal<>();

    @BeforeEach
    void beforeEach() {
        intervenerShareDocumentsService = new IntervenerShareDocumentsService();
        uuid.set(UUID.fromString("38400000-8cf0-11bd-b23e-10b96e4ef00d"));
    }


    @Test
    void intervenerSourceDocumentListWhenDocNotPresent() {

        FinremCallbackRequest request = buildCallbackRequest();
        FinremCaseDetails details = request.getCaseDetails();

        DynamicMultiSelectList list = intervenerShareDocumentsService.intervenerSourceDocumentList(details, "[INTVRSOLICITOR1]");
        assertEquals("document size for sharing", 0, list.getListItems().size());
        assertNull("no document selected from list", list.getValue());
    }

    @Test
    void intervenerOneSourceDocumentListWhenDocPresent() {

        FinremCallbackRequest request = buildCallbackRequest();
        FinremCaseDetails<FinremCaseDataContested> details = request.getCaseDetails();
        FinremCaseDataContested data = details.getData();

        data.getUploadCaseDocumentWrapper().setIntv1Other(getTestDocument(OTHER));
        data.getUploadCaseDocumentWrapper().setIntv1Chronologies(getTestDocument(CHRONOLOGY));
        data.getUploadCaseDocumentWrapper().setIntv1StmtsExhibits(getTestDocument(STATEMENT_AFFIDAVIT));
        data.getUploadCaseDocumentWrapper().setIntv1HearingBundles(getTestDocument(TRIAL_BUNDLE));
        data.getUploadCaseDocumentWrapper().setIntv1FormEsExhibits(getTestDocument(APPLICANT_FORM_E));
        data.getUploadCaseDocumentWrapper().setIntv1Qa(getTestDocument(QUESTIONNAIRE));
        data.getUploadCaseDocumentWrapper().setIntv1Summaries(getTestDocument(CASE_SUMMARY));
        data.getUploadCaseDocumentWrapper().setIntv1FormHs(getTestDocument(FORM_H));
        data.getUploadCaseDocumentWrapper().setIntv1ExpertEvidence(getTestDocument(EXPERT_EVIDENCE));
        data.getUploadCaseDocumentWrapper().setIntv1CorrespDocs(getTestDocument(CARE_PLAN));

        DynamicMultiSelectList sourceDocumentList = new DynamicMultiSelectList();
        List<UploadCaseDocumentCollection> coll = data.getUploadCaseDocumentWrapper().getIntv1Other();
        CaseDocument doc = coll.get(0).getUploadCaseDocument().getCaseDocuments();
        sourceDocumentList.setListItems(singletonList(getSelectedDoc(coll, doc, INTERVENER_ONE_OTHER_COLLECTION)));
        data.setSourceDocumentList(sourceDocumentList);

        DynamicMultiSelectList list = intervenerShareDocumentsService.intervenerSourceDocumentList(details, "[INTVRSOLICITOR1]");
        assertEquals("document size for sharing", 10, list.getListItems().size());
        assertNull(list.getValue());
        list = intervenerShareDocumentsService.intervenerSourceDocumentList(details, "[INTVRBARRISTER1]");
        assertEquals("document size for sharing", 10, list.getListItems().size());
        assertNull(list.getValue());
    }


    @Test
    void intervenerTwoSourceDocumentListWhenDocPresent() {

        FinremCallbackRequest request = buildCallbackRequest();
        FinremCaseDetails<FinremCaseDataContested> details = request.getCaseDetails();
        FinremCaseDataContested data = details.getData();

        data.getUploadCaseDocumentWrapper().setIntv2Other(getTestDocument(OTHER));
        data.getUploadCaseDocumentWrapper().setIntv2Chronologies(getTestDocument(CHRONOLOGY));
        data.getUploadCaseDocumentWrapper().setIntv2StmtsExhibits(getTestDocument(STATEMENT_AFFIDAVIT));
        data.getUploadCaseDocumentWrapper().setIntv2HearingBundles(getTestDocument(TRIAL_BUNDLE));
        data.getUploadCaseDocumentWrapper().setIntv2FormEsExhibits(getTestDocument(APPLICANT_FORM_E));
        data.getUploadCaseDocumentWrapper().setIntv2Qa(getTestDocument(QUESTIONNAIRE));
        data.getUploadCaseDocumentWrapper().setIntv2Summaries(getTestDocument(CASE_SUMMARY));
        data.getUploadCaseDocumentWrapper().setIntv2FormHs(getTestDocument(FORM_H));
        data.getUploadCaseDocumentWrapper().setIntv2ExpertEvidence(getTestDocument(EXPERT_EVIDENCE));
        data.getUploadCaseDocumentWrapper().setIntv2CorrespDocs(getTestDocument(CARE_PLAN));

        DynamicMultiSelectList sourceDocumentList = new DynamicMultiSelectList();
        List<UploadCaseDocumentCollection> coll = data.getUploadCaseDocumentWrapper().getIntv2Other();
        CaseDocument doc = coll.get(0).getUploadCaseDocument().getCaseDocuments();
        sourceDocumentList.setListItems(singletonList(getSelectedDoc(coll, doc, INTERVENER_TWO_OTHER_COLLECTION)));
        data.setSourceDocumentList(sourceDocumentList);

        DynamicMultiSelectList list = intervenerShareDocumentsService.intervenerSourceDocumentList(details, "[INTVRSOLICITOR2]");
        assertEquals("document size for sharing", 10, list.getListItems().size());
        assertNull(list.getValue());
        list = intervenerShareDocumentsService.intervenerSourceDocumentList(details, "[INTVRBARRISTER2]");
        assertEquals("document size for sharing", 10, list.getListItems().size());
        assertNull(list.getValue());
    }

    @Test
    void intervenerThreeSourceDocumentListWhenDocPresent() {

        FinremCallbackRequest request = buildCallbackRequest();
        FinremCaseDetails<FinremCaseDataContested> details = request.getCaseDetails();
        FinremCaseDataContested data = details.getData();

        data.getUploadCaseDocumentWrapper().setIntv3Other(getTestDocument(OTHER));
        data.getUploadCaseDocumentWrapper().setIntv3Chronologies(getTestDocument(CHRONOLOGY));
        data.getUploadCaseDocumentWrapper().setIntv3StmtsExhibits(getTestDocument(STATEMENT_AFFIDAVIT));
        data.getUploadCaseDocumentWrapper().setIntv3HearingBundles(getTestDocument(TRIAL_BUNDLE));
        data.getUploadCaseDocumentWrapper().setIntv3FormEsExhibits(getTestDocument(APPLICANT_FORM_E));
        data.getUploadCaseDocumentWrapper().setIntv3Qa(getTestDocument(QUESTIONNAIRE));
        data.getUploadCaseDocumentWrapper().setIntv3Summaries(getTestDocument(CASE_SUMMARY));
        data.getUploadCaseDocumentWrapper().setIntv3FormHs(getTestDocument(FORM_H));
        data.getUploadCaseDocumentWrapper().setIntv3ExpertEvidence(getTestDocument(EXPERT_EVIDENCE));
        data.getUploadCaseDocumentWrapper().setIntv3CorrespDocs(getTestDocument(CARE_PLAN));

        DynamicMultiSelectList sourceDocumentList = new DynamicMultiSelectList();
        List<UploadCaseDocumentCollection> coll = data.getUploadCaseDocumentWrapper().getIntv3Other();
        CaseDocument doc = coll.get(0).getUploadCaseDocument().getCaseDocuments();
        sourceDocumentList.setListItems(singletonList(getSelectedDoc(coll, doc, INTERVENER_THREE_OTHER_COLLECTION)));
        data.setSourceDocumentList(sourceDocumentList);

        DynamicMultiSelectList list = intervenerShareDocumentsService.intervenerSourceDocumentList(details, "[INTVRSOLICITOR3]");
        assertEquals("document size for sharing", 10, list.getListItems().size());
        assertNull(list.getValue());
        list = intervenerShareDocumentsService.intervenerSourceDocumentList(details, "[INTVRBARRISTER3]");
        assertEquals("document size for sharing", 10, list.getListItems().size());
        assertNull(list.getValue());
    }


    @Test
    void intervenerFourSourceDocumentListWhenDocPresent() {

        FinremCallbackRequest request = buildCallbackRequest();
        FinremCaseDetails<FinremCaseDataContested> details = request.getCaseDetails();
        FinremCaseDataContested data = details.getData();

        data.getUploadCaseDocumentWrapper().setIntv4Other(getTestDocument(OTHER));
        data.getUploadCaseDocumentWrapper().setIntv4Chronologies(getTestDocument(CHRONOLOGY));
        data.getUploadCaseDocumentWrapper().setIntv4StmtsExhibits(getTestDocument(STATEMENT_AFFIDAVIT));
        data.getUploadCaseDocumentWrapper().setIntv4HearingBundles(getTestDocument(TRIAL_BUNDLE));
        data.getUploadCaseDocumentWrapper().setIntv4FormEsExhibits(getTestDocument(APPLICANT_FORM_E));
        data.getUploadCaseDocumentWrapper().setIntv4Qa(getTestDocument(QUESTIONNAIRE));
        data.getUploadCaseDocumentWrapper().setIntv4Summaries(getTestDocument(CASE_SUMMARY));
        data.getUploadCaseDocumentWrapper().setIntv4FormHs(getTestDocument(FORM_H));
        data.getUploadCaseDocumentWrapper().setIntv4ExpertEvidence(getTestDocument(EXPERT_EVIDENCE));
        data.getUploadCaseDocumentWrapper().setIntv4CorrespDocs(getTestDocument(CARE_PLAN));

        DynamicMultiSelectList sourceDocumentList = new DynamicMultiSelectList();
        List<UploadCaseDocumentCollection> coll = data.getUploadCaseDocumentWrapper().getIntv4Other();
        CaseDocument doc = coll.get(0).getUploadCaseDocument().getCaseDocuments();
        sourceDocumentList.setListItems(singletonList(getSelectedDoc(coll, doc, INTERVENER_FOUR_OTHER_COLLECTION)));
        data.setSourceDocumentList(sourceDocumentList);

        DynamicMultiSelectList list = intervenerShareDocumentsService.intervenerSourceDocumentList(details, "[INTVRSOLICITOR4]");
        assertEquals("document size for sharing", 10, list.getListItems().size());
        assertNull(list.getValue());
        list = intervenerShareDocumentsService.intervenerSourceDocumentList(details, "[INTVRBARRISTER4]");
        assertEquals("document size for sharing", 10, list.getListItems().size());
        assertNull(list.getValue());
    }

    @Test
    void getIntervenerOneToOtherSolicitorRoleList() {
        FinremCallbackRequest request = buildCallbackRequest();
        DynamicMultiSelectList list = intervenerShareDocumentsService.getOtherSolicitorRoleList(request.getCaseDetails(),
            getCaseRoleList(), "[INTVRSOLICITOR1]");
        assertEquals("role size for sharing", 11, list.getListItems().size());
    }

    @Test
    void getIntervenerToOtherSolicitorRoleListButNoCaseRoleAvailable() {
        FinremCallbackRequest request = buildCallbackRequest();
        DynamicMultiSelectList list = intervenerShareDocumentsService.getOtherSolicitorRoleList(request.getCaseDetails(),
            new ArrayList<>(), "[INTVRSOLICITOR1]");
        assertEquals("role size for sharing", 0, list.getListItems().size());
        assertNull("no document selected from list", list.getValue());
    }


    @Test
    void shareOneDocumentOnTheirRespectiveCollectionForSelectedSolicitors() {
        FinremCallbackRequest request = buildCallbackRequest();
        FinremCaseDetails<FinremCaseDataContested> details = request.getCaseDetails();
        FinremCaseDataContested data = details.getData();


        data.getUploadCaseDocumentWrapper().setIntv1Other(getTestDocument(OTHER));
        DynamicMultiSelectList sourceDocumentList = new DynamicMultiSelectList();
        List<UploadCaseDocumentCollection> coll = data.getUploadCaseDocumentWrapper().getIntv1Other();
        CaseDocument doc = coll.get(0).getUploadCaseDocument().getCaseDocuments();
        sourceDocumentList.setValue(singletonList(getSelectedDoc(coll, doc, INTERVENER_ONE_OTHER_COLLECTION)));
        data.setSourceDocumentList(sourceDocumentList);

        DynamicMultiSelectList roleList = new DynamicMultiSelectList();
        roleList.setValue(singletonList(getSelectedParty(RESP_SOLICITOR)));
        data.setSolicitorRoleList(roleList);

        intervenerShareDocumentsService.shareSelectedDocumentWithOtherSelectedSolicitors(data);

        UploadCaseDocumentWrapper wrapper = data.getUploadCaseDocumentWrapper();
        assertEquals("one document shared with respondent solicitor", 1,
            wrapper.getRespOtherCollectionShared().size());
    }


    private static DynamicMultiSelectListElement getSelectedDoc(List<UploadCaseDocumentCollection> coll,
                                                                CaseDocument doc,
                                                                CaseDocumentCollectionType type) {
        return DynamicMultiSelectListElement.builder()
            .label(type.getCcdKey() + " -> " + doc.getDocumentFilename())
            .code(coll.get(0).getId() + "#" + type.getCcdKey())
            .build();
    }

    private DynamicMultiSelectListElement getSelectedParty(CaseRole role) {
        return DynamicMultiSelectListElement.builder()
            .label(role.getCcdCode()).code(role.getCcdCode()).build();
    }

    private List<UploadCaseDocumentCollection> getTestDocument(CaseDocumentType documentType) {
        UploadCaseDocument document = UploadCaseDocument.builder()
            .caseDocuments(TestSetUpUtils.caseDocument())
            .caseDocumentType(documentType)
            .caseDocumentParty(INTERVENER_ONE)
            .caseDocumentOther("No")
            .caseDocumentConfidentiality(YesOrNo.NO)
            .hearingDetails("UK 1400 hours")
            .caseDocumentFdr(YesOrNo.NO)
            .caseDocumentUploadDateTime(LocalDateTime.now()).build();
        return List.of(UploadCaseDocumentCollection.builder().id(uuid.get().toString()).uploadCaseDocument(document).build());
    }

    private FinremCallbackRequest buildCallbackRequest() {
        FinremCaseDataContested caseData = new FinremCaseDataContested();
        return FinremCallbackRequest
            .builder()
            .eventType(EventType.SHARE_SELECTED_DOCUMENTS)
            .caseDetailsBefore(FinremCaseDetails.builder().id(123L).caseType(CONTESTED)
                .data(caseData).build())
            .caseDetails(FinremCaseDetails.builder().id(123L).caseType(CONTESTED)
                .data(caseData).build())
            .build();
    }

    private List<CaseAssignmentUserRole> getCaseRoleList() {

        List<String> roleList = List.of("[APPSOLICITOR]", "[APPBARRISTER]", "[RESPSOLICITOR]",
            "[RESPBARRISTER]", "[INTVRSOLICITOR1]", "[INTVRSOLICITOR2]", "[INTVRSOLICITOR3]", "[INTVRSOLICITOR4]",
            "[INTVRBARRISTER1]", "[INTVRBARRISTER2]", "[INTVRBARRISTER3]", "[INTVRBARRISTER4]");
        List<CaseAssignmentUserRole> caseAssignedUserRoleList = new ArrayList<>();
        roleList.forEach(role -> {
            caseAssignedUserRoleList.add(CaseAssignmentUserRole.builder().userId(role).caseRole(role).caseDataId(String.valueOf(123L)).build());
        });
        return caseAssignedUserRoleList;
    }
}