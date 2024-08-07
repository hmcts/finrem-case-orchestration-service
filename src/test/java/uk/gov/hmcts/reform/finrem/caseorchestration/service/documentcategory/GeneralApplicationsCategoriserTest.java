package uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcategory;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.BaseHandlerTestSetup;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicRadioList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicRadioListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationItems;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationSuportingDocumentItems;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationSupportingDocumentData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralApplicationWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralApplicationsCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory.GeneralApplicationsCategoriser;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CASE_LEVEL_ROLE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory.APPLICATIONS_OTHER_APPLICATION_APPLICATION_1;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory.APPLICATIONS_OTHER_APPLICATION_APPLICATION_10;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory.APPLICATIONS_OTHER_APPLICATION_APPLICATION_2;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory.APPLICATIONS_OTHER_APPLICATION_APPLICATION_3;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory.APPLICATIONS_OTHER_APPLICATION_APPLICATION_4;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory.APPLICATIONS_OTHER_APPLICATION_APPLICATION_5;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory.APPLICATIONS_OTHER_APPLICATION_APPLICATION_6;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory.APPLICATIONS_OTHER_APPLICATION_APPLICATION_7;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory.APPLICATIONS_OTHER_APPLICATION_APPLICATION_8;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory.APPLICATIONS_OTHER_APPLICATION_APPLICATION_9;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory.APPLICATIONS_OTHER_APPLICATION_OVERFLOW;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory.APPROVED_ORDERS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory.SYSTEM_DUPLICATES;

class GeneralApplicationsCategoriserTest extends BaseHandlerTestSetup {
    private GeneralApplicationsCategoriser generalApplicationsCategoriser;

    @BeforeEach
    void setUp() {
        FeatureToggleService featureToggleService = mock(FeatureToggleService.class);
        when(featureToggleService.isCaseFileViewEnabled()).thenReturn(true);
        generalApplicationsCategoriser = new GeneralApplicationsCategoriser(featureToggleService);
    }

    @Test
    void testCategorizeDocuments() {
        FinremCaseData finremCaseData = buildFinremCaseData();
        generalApplicationsCategoriser.categorise(finremCaseData);

        List<GeneralApplicationsCollection> generalApplications = finremCaseData.getGeneralApplicationWrapper()
            .getGeneralApplications();

        assertThat(generalApplications.get(0).getValue().getGeneralApplicationDocument().getCategoryId())
            .isEqualTo(APPLICATIONS_OTHER_APPLICATION_APPLICATION_1.getDocumentCategoryId());
        assertThat(generalApplications.get(2).getValue().getGeneralApplicationDocument().getCategoryId())
            .isEqualTo(APPLICATIONS_OTHER_APPLICATION_APPLICATION_3.getDocumentCategoryId());
    }

    @Test
    void testGeneralApplicationOverflowCategory() {
        FinremCaseData finremCaseData = buildFinremCaseData11Applications();
        generalApplicationsCategoriser.categorise(finremCaseData);

        List<GeneralApplicationsCollection> generalApplications = finremCaseData.getGeneralApplicationWrapper()
            .getGeneralApplications();

        assertThat(generalApplications.get(0).getValue().getGeneralApplicationDocument().getCategoryId())
            .isEqualTo(APPLICATIONS_OTHER_APPLICATION_APPLICATION_1.getDocumentCategoryId());
        assertThat(generalApplications.get(1).getValue().getGeneralApplicationDraftOrder().getCategoryId())
            .isEqualTo(APPLICATIONS_OTHER_APPLICATION_APPLICATION_2.getDocumentCategoryId());
        assertThat(generalApplications.get(2).getValue().getGeneralApplicationDirectionsDocument().getCategoryId())
            .isEqualTo(APPROVED_ORDERS.getDocumentCategoryId());
        assertThat(generalApplications.get(3).getValue().getGeneralApplicationDocument().getCategoryId())
            .isEqualTo(APPLICATIONS_OTHER_APPLICATION_APPLICATION_4.getDocumentCategoryId());
        assertThat(generalApplications.get(4).getValue().getGeneralApplicationDocument().getCategoryId())
            .isEqualTo(APPLICATIONS_OTHER_APPLICATION_APPLICATION_5.getDocumentCategoryId());
        assertThat(generalApplications.get(8).getValue().getGeneralApplicationDocument().getCategoryId())
            .isEqualTo(APPLICATIONS_OTHER_APPLICATION_APPLICATION_9.getDocumentCategoryId());
        assertThat(generalApplications.get(9).getValue().getGeneralApplicationDocument().getCategoryId())
            .isEqualTo(APPLICATIONS_OTHER_APPLICATION_APPLICATION_10.getDocumentCategoryId());
        assertThat(generalApplications.get(10).getValue().getGeneralApplicationDocument().getCategoryId())
            .isEqualTo(APPLICATIONS_OTHER_APPLICATION_OVERFLOW.getDocumentCategoryId());
    }

    @Test
    void givenAllPartiesCreateGeneralApplication_whenCategoriseDocuments_thenCategorisedSuccessfully() {
        FinremCaseData caseData = buildFinremCaseDataWithAllDocumentGeneralApplications();
        generalApplicationsCategoriser.categorise(caseData);

        GeneralApplicationWrapper generalApplicationWrapper = caseData.getGeneralApplicationWrapper();

        // Main General Applications collection should be sequenced
        verifyCreateApplicationDocumentsCategoryApplicationsOther(generalApplicationWrapper);

        // All other collections should be System Duplicates
        verifyCreateApplicationDocumentsSystemDuplicates(generalApplicationWrapper.getAppRespGeneralApplications());
        verifyCreateApplicationDocumentsSystemDuplicates(generalApplicationWrapper.getIntervener1GeneralApplications());
        verifyCreateApplicationDocumentsSystemDuplicates(generalApplicationWrapper.getIntervener2GeneralApplications());
        verifyCreateApplicationDocumentsSystemDuplicates(generalApplicationWrapper.getIntervener3GeneralApplications());
        verifyCreateApplicationDocumentsSystemDuplicates(generalApplicationWrapper.getIntervener4GeneralApplications());
    }

    private void verifyCreateApplicationDocumentsCategoryApplicationsOther(GeneralApplicationWrapper generalApplicationWrapper) {
        List<GeneralApplicationsCollection> generalApplicationsCollections = generalApplicationWrapper.getGeneralApplications();
        verifyAllCreateApplicationDocumentsCategory(generalApplicationsCollections.get(0).getValue(),
            APPLICATIONS_OTHER_APPLICATION_APPLICATION_1);
        verifyAllCreateApplicationDocumentsCategory(generalApplicationsCollections.get(1).getValue(),
            APPLICATIONS_OTHER_APPLICATION_APPLICATION_2);
        verifyAllCreateApplicationDocumentsCategory(generalApplicationsCollections.get(2).getValue(),
            APPLICATIONS_OTHER_APPLICATION_APPLICATION_3);
        verifyAllCreateApplicationDocumentsCategory(generalApplicationsCollections.get(3).getValue(),
            APPLICATIONS_OTHER_APPLICATION_APPLICATION_4);
        verifyAllCreateApplicationDocumentsCategory(generalApplicationsCollections.get(4).getValue(),
            APPLICATIONS_OTHER_APPLICATION_APPLICATION_5);
        verifyAllCreateApplicationDocumentsCategory(generalApplicationsCollections.get(5).getValue(),
            APPLICATIONS_OTHER_APPLICATION_APPLICATION_6);
        verifyAllCreateApplicationDocumentsCategory(generalApplicationsCollections.get(6).getValue(),
            APPLICATIONS_OTHER_APPLICATION_APPLICATION_7);
        verifyAllCreateApplicationDocumentsCategory(generalApplicationsCollections.get(7).getValue(),
            APPLICATIONS_OTHER_APPLICATION_APPLICATION_8);
        verifyAllCreateApplicationDocumentsCategory(generalApplicationsCollections.get(8).getValue(),
            APPLICATIONS_OTHER_APPLICATION_APPLICATION_9);
        verifyAllCreateApplicationDocumentsCategory(generalApplicationsCollections.get(9).getValue(),
            APPLICATIONS_OTHER_APPLICATION_APPLICATION_10);
        verifyAllCreateApplicationDocumentsCategory(generalApplicationsCollections.get(10).getValue(),
            APPLICATIONS_OTHER_APPLICATION_OVERFLOW);
    }

    private void verifyCreateApplicationDocumentsSystemDuplicates(
        List<GeneralApplicationsCollection> generalApplications) {
        generalApplications.stream()
            .map(GeneralApplicationsCollection::getValue)
            .forEach(ga -> verifyAllCreateApplicationDocumentsCategory(ga, SYSTEM_DUPLICATES));
    }

    private void verifyAllCreateApplicationDocumentsCategory(GeneralApplicationItems generalApplication,
                                                             DocumentCategory documentCategory) {
        String expectedCategoryId = documentCategory.getDocumentCategoryId();

        assertThat(generalApplication.getGeneralApplicationDocument().getCategoryId()).isEqualTo(expectedCategoryId);
        assertThat(generalApplication.getGeneralApplicationDraftOrder().getCategoryId()).isEqualTo(expectedCategoryId);
        assertThat(generalApplication.getGaSupportDocuments().get(0).getValue().getSupportDocument().getCategoryId())
            .isEqualTo(expectedCategoryId);
    }


    @Test
    void testUncategorizeDocuments() {
        FinremCaseData finremCaseData = buildFinremCaseData();
        generalApplicationsCategoriser.uncategoriseDuplicatedCollections(finremCaseData);

        String expectedCategoryId = SYSTEM_DUPLICATES.getDocumentCategoryId();
        List<GeneralApplicationsCollection> appRespGeneralApplications = finremCaseData.getGeneralApplicationWrapper()
            .getAppRespGeneralApplications();
        assertThat(appRespGeneralApplications.get(0).getValue().getGeneralApplicationDocument().getCategoryId())
            .isEqualTo(expectedCategoryId);
        assertThat(appRespGeneralApplications.get(2).getValue().getGeneralApplicationDocument().getCategoryId())
            .isEqualTo(expectedCategoryId);

        List<GeneralApplicationsCollection> intervener1GeneralApplications = finremCaseData
            .getGeneralApplicationWrapper().getIntervener1GeneralApplications();
        assertThat(intervener1GeneralApplications.get(0).getValue().getGeneralApplicationDocument().getCategoryId())
            .isEqualTo(expectedCategoryId);
        assertThat(intervener1GeneralApplications.get(2).getValue().getGeneralApplicationDocument().getCategoryId())
            .isEqualTo(expectedCategoryId);
    }

    @Test
    void testUncategoriseDuplicatedCollections() {
        FinremCaseData finremCaseData = buildFinremCaseDataNullCollections();
        generalApplicationsCategoriser.uncategoriseDuplicatedCollections(finremCaseData);
        assertThat(finremCaseData.getGeneralApplicationWrapper().getAppRespGeneralApplications()).isNull();
        assertThat(finremCaseData.getGeneralApplicationWrapper().getIntervener1GeneralApplications()).isNull();
        assertThat(finremCaseData.getGeneralApplicationWrapper().getIntervener2GeneralApplications()).isNull();
        assertThat(finremCaseData.getGeneralApplicationWrapper().getIntervener3GeneralApplications()).isNull();
        assertThat(finremCaseData.getGeneralApplicationWrapper().getIntervener4GeneralApplications()).isNull();
        assertThat(finremCaseData.getGeneralApplicationWrapper().getGeneralApplicationIntvrOrders()).isNull();
    }

    private FinremCaseData buildFinremCaseDataNullCollections() {
        return FinremCaseData.builder()
            .generalApplicationWrapper(GeneralApplicationWrapper.builder()
                .generalApplicationCreatedBy("Claire Mumford").generalApplicationPreState("applicationIssued")
                .generalApplications(null)
                .appRespGeneralApplications(null)
                .intervener1GeneralApplications(null)
                .intervener2GeneralApplications(null)
                .intervener3GeneralApplications(null)
                .intervener4GeneralApplications(null)
                .generalApplicationIntvrOrders(null)
                .build()).build();
    }

    private FinremCaseData buildFinremCaseData() {
        return FinremCaseData.builder()
            .generalApplicationWrapper(GeneralApplicationWrapper.builder()
                .generalApplicationCreatedBy("Claire Mumford").generalApplicationPreState("applicationIssued")
                .generalApplications(buildGeneralApplicationsTestData())
                .appRespGeneralApplications(buildGeneralApplicationsTestData())
                .intervener1GeneralApplications(buildGeneralApplicationsTestData())
                .intervener2GeneralApplications(buildGeneralApplicationsTestData())
                .intervener3GeneralApplications(buildGeneralApplicationsTestData())
                .intervener4GeneralApplications(buildGeneralApplicationsTestData())
                .generalApplicationIntvrOrders(buildGeneralApplicationsTestData())
                .build()).build();
    }

    private FinremCaseData buildFinremCaseDataWithAllDocumentGeneralApplications() {
        return FinremCaseData.builder()
            .generalApplicationWrapper(GeneralApplicationWrapper.builder()
                .generalApplicationCreatedBy("Claire Mumford").generalApplicationPreState("applicationIssued")
                .generalApplications(buildGeneralApplicationTestData(11))
                .appRespGeneralApplications(buildGeneralApplicationTestData(3))
                .intervener1GeneralApplications(buildGeneralApplicationTestData(3))
                .intervener2GeneralApplications(buildGeneralApplicationTestData(3))
                .intervener3GeneralApplications(buildGeneralApplicationTestData(3))
                .intervener4GeneralApplications(buildGeneralApplicationTestData(3))
                .generalApplicationIntvrOrders(buildGeneralApplicationTestData(3))
                .build()).build();
    }

    private List<GeneralApplicationsCollection> buildGeneralApplicationsTestData() {
        GeneralApplicationsCollection firstGeneralApplications = buildGeneralApplicationsCollection(
            true, false, false);
        GeneralApplicationsCollection secondGeneralApplications = buildGeneralApplicationsCollection(
            false, false, false);
        GeneralApplicationsCollection thirdGeneralApplications = buildGeneralApplicationsCollection(
            true, false, false);

        List<GeneralApplicationsCollection> generalApplicationsCollection = new ArrayList<>();
        generalApplicationsCollection.add(firstGeneralApplications);
        generalApplicationsCollection.add(secondGeneralApplications);
        generalApplicationsCollection.add(thirdGeneralApplications);

        return generalApplicationsCollection;
    }

    private FinremCaseData buildFinremCaseData11Applications() {
        GeneralApplicationsCollection firstGeneralApplications = buildGeneralApplicationsCollection(
            true, false, false);
        GeneralApplicationsCollection secondGeneralApplications = buildGeneralApplicationsCollection(
            false, true, false);
        GeneralApplicationsCollection thirdGeneralApplications = buildGeneralApplicationsCollection(
            false, false, true);
        GeneralApplicationsCollection fourthGeneralApplications = buildGeneralApplicationsCollection(
            true, false, false);
        GeneralApplicationsCollection fifthGeneralApplications = buildGeneralApplicationsCollection(
            true, false, false);
        GeneralApplicationsCollection sixthGeneralApplications = buildGeneralApplicationsCollection(
            false, false, false);
        GeneralApplicationsCollection seventhGeneralApplications = buildGeneralApplicationsCollection(
            false, false, false);
        GeneralApplicationsCollection eighthGeneralApplications = buildGeneralApplicationsCollection(
            false, false, false);
        GeneralApplicationsCollection ninthGeneralApplications = buildGeneralApplicationsCollection(
            true, false, false);
        GeneralApplicationsCollection tenthGeneralApplications = buildGeneralApplicationsCollection(
            true, false, false);
        GeneralApplicationsCollection eleventhGeneralApplications = buildGeneralApplicationsCollection(
            true, false, false);

        List<GeneralApplicationsCollection> generalApplicationsCollection = new ArrayList<>();
        generalApplicationsCollection.add(firstGeneralApplications);
        generalApplicationsCollection.add(secondGeneralApplications);
        generalApplicationsCollection.add(thirdGeneralApplications);
        generalApplicationsCollection.add(fourthGeneralApplications);
        generalApplicationsCollection.add(fifthGeneralApplications);
        generalApplicationsCollection.add(sixthGeneralApplications);
        generalApplicationsCollection.add(seventhGeneralApplications);
        generalApplicationsCollection.add(eighthGeneralApplications);
        generalApplicationsCollection.add(ninthGeneralApplications);
        generalApplicationsCollection.add(tenthGeneralApplications);
        generalApplicationsCollection.add(eleventhGeneralApplications);

        return FinremCaseData.builder()
            .generalApplicationWrapper(GeneralApplicationWrapper.builder()
                .generalApplicationCreatedBy("Claire Mumford").generalApplicationPreState("applicationIssued")
                .generalApplications(generalApplicationsCollection)
                .appRespGeneralApplications(generalApplicationsCollection)
                .intervener1GeneralApplications(generalApplicationsCollection)
                .build()).build();
    }

    private List<GeneralApplicationsCollection> buildGeneralApplicationTestData(int total) {
        List<GeneralApplicationsCollection> generalApplicationsCollection = new ArrayList<>();
        for (int i = 1; i <= total; i++) {
            generalApplicationsCollection.add(buildGeneralApplicationsCollection(true, true, true, true));
        }

        return generalApplicationsCollection;
    }

    private GeneralApplicationsCollection buildGeneralApplicationsCollection(
        boolean isGeneralApplicationDocumentPresent,
        boolean isGeneralApplicationDraftOrderPresent,
        boolean isGeneralApplicationDirectionsPresent) {
        return buildGeneralApplicationsCollection(isGeneralApplicationDocumentPresent,
            isGeneralApplicationDraftOrderPresent, isGeneralApplicationDirectionsPresent, false);
    }

    private GeneralApplicationsCollection buildGeneralApplicationsCollection(
        boolean isGeneralApplicationDocumentPresent,
        boolean isGeneralApplicationDraftOrderPresent,
        boolean isGeneralApplicationDirectionsPresent,
        boolean isGeneralApplicationSupportingDocsPresent) {

        GeneralApplicationItems generalApplicationItems = buildGeneralApplicationItems();

        if (isGeneralApplicationDocumentPresent) {
            generalApplicationItems.setGeneralApplicationDocument(buildGeneralApplicationDocument());
        }
        if (isGeneralApplicationDraftOrderPresent) {
            generalApplicationItems.setGeneralApplicationDraftOrder(buildGeneralApplicationDocument());
        }
        if (isGeneralApplicationDirectionsPresent) {
            generalApplicationItems.setGeneralApplicationDirectionsDocument(buildGeneralApplicationDocument());
        }
        if (isGeneralApplicationSupportingDocsPresent) {
            generalApplicationItems.setGaSupportDocuments(new ArrayList<>());
            GeneralApplicationSupportingDocumentData supportingDocumentData = GeneralApplicationSupportingDocumentData.builder()
                    .value(GeneralApplicationSuportingDocumentItems.builder()
                        .supportDocument(buildGeneralApplicationDocument())
                            .build())
                        .build();

            generalApplicationItems.getGaSupportDocuments().add(supportingDocumentData);
        }

        return GeneralApplicationsCollection.builder()
            .value(generalApplicationItems)
            .id(UUID.randomUUID())
            .build();
    }

    private GeneralApplicationItems buildGeneralApplicationItems() {
        return GeneralApplicationItems.builder()
            .generalApplicationSender(buildGeneralApplicationSenderDynamicList())
            .generalApplicationCreatedBy("Claire Mumford")
            .generalApplicationHearingRequired("Yes").generalApplicationTimeEstimate("24 hours")
            .generalApplicationSpecialMeasures("Special measure").generalApplicationCreatedDate(
                LocalDate.of(2022, 8, 2))
            .build();
    }

    private DynamicRadioList buildGeneralApplicationSenderDynamicList() {
        List<DynamicRadioListElement> dynamicListElements = List.of(
            buildDynamicListElement(APPLICANT, APPLICANT),
            buildDynamicListElement(RESPONDENT, RESPONDENT),
            buildDynamicListElement(CASE_LEVEL_ROLE, CASE_LEVEL_ROLE)
        );

        return DynamicRadioList.builder()
            .value(dynamicListElements.get(0))
            .listItems(dynamicListElements)
            .build();
    }

    private DynamicRadioListElement buildDynamicListElement(String code, String label) {
        return DynamicRadioListElement.builder()
            .code(code)
            .label(label)
            .build();
    }

    private CaseDocument buildGeneralApplicationDocument() {
        String docName = RandomStringUtils.random(10, true, false);
        return CaseDocument.builder().documentFilename(docName + ".pdf")
            .documentUrl("http://dm-store/documents/b067a2dd-657a-4ed2-98c3-9c3159d1482e")
            .documentBinaryUrl("http://dm-store/documents/b067a2dd-657a-4ed2-98c3-9c3159d1482e/binary").build();
    }
}
