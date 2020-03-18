package uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.helper;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.bulk.scan.domain.FormA.ApplicantRepresentPaper.FR_APPLICANT_REPRESENTED_1;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.bulk.scan.domain.FormA.ApplicantRepresentPaper.FR_APPLICANT_REPRESENTED_2;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.bulk.scan.domain.FormA.ApplicantRepresentPaper.FR_APPLICANT_REPRESENTED_3;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.bulk.scan.domain.FormA.NatureOfApplication5b.FR_NATURE_OF_APPLICATION_1;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.bulk.scan.domain.FormA.NatureOfApplication5b.FR_NATURE_OF_APPLICATION_2;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.bulk.scan.domain.FormA.NatureOfApplication5b.FR_NATURE_OF_APPLICATION_3;

public class BulkScanHelper {

    public static final Map<String, String> natureOfApplicationChecklistToCcdFieldNames =
            new ImmutableMap.Builder<String, String>()
                    .put("Periodical Payment Order", "Periodical Payment Order")
                    .put("Lump Sum Order", "Lump Sum Order")
                    .put("Pension Sharing Order", "Pension Sharing Order")
                    .put("Pension Attachment Order", "Pension Attachment Order")
                    .put("Pension Compensation Sharing Order", "Pension Compensation Sharing Order")
                    .put("Pension Compensation Attachment Order", "Pension Compensation Attachment Order")
                    .put("A settlement or a transfer of property", "A settlement or a transfer of property")
                    .put("Property Adjustment Order", "Property Adjustment Order")
                    .build();

    public static final Map<String, String> dischargePeriodicalPaymentSubstituteChecklistToCcdFieldNames =
            new ImmutableMap.Builder<String, String>()
                    .put("a lump sum order", "Lump Sum Order")
                    .put("a property adjustment order", "Property Adjustment Order")
                    .put("a pension sharing order", "Pension Sharing Order")
                    .put("a pension compensation sharing order", "Pension Compensation Sharing Order")
                    .build();

    public static final Map<String, String> orderForChildrenToCcdFieldNames =
            new ImmutableMap.Builder<String, String>()
                    .put("there is a written agreement made before 5 April 1993 about maintenance for the benefit of children",
                            FR_NATURE_OF_APPLICATION_1)
                    .put("there is a written agreement made on or after 5 April 1993 about maintenance for the benefit of children",
                            FR_NATURE_OF_APPLICATION_2)
                    .put("there is no agreement, but the applicant is applying for payments", FR_NATURE_OF_APPLICATION_3)
                    .build();

    public static final Map<String, String> orderForChildrenNoAgreementToCcdFieldNames =
            new ImmutableMap.Builder<String, String>()
                    .put("for a stepchild or stepchildren", "Step Child or Step Children")
                    .put("in addition to child support maintenance already paid under a Child Support Agency assessment",
                            "In addition to child support")
                    .put("to meet expenses arising from a child’s disability", "disability expenses")
                    .put("to meet expenses incurred by a child in being educated or training for work", "training")
                    .put("when either the child or the person with care of the child or the absent parent of the child "
                            + "is not habitually resident in the United Kingdom", "When not habitually resident")
                    .build();

    public static final Map<String, String> provisionMadeForToCcdFieldNames =
            new ImmutableMap.Builder<String, String>()
                    .put("in connection with matrimonial or civil partnership proceedings", "matrimonialOrCivilPartnershipProceedings")
                    .put("under paragraphs 1 or 2 of Schedule 1 to the Children Act 1989", "childrenAct1989")
                    .build();

    public static final Map<String, String> applicantRepresentPaperToCcdFieldNames =
            new ImmutableMap.Builder<String, String>()
                    .put("I am not represented by a solicitor in these proceedings", FR_APPLICANT_REPRESENTED_1)
                    .put("I am not represented by a solicitor in these proceedings but am receiving advice from a solicitor",
                            FR_APPLICANT_REPRESENTED_2)
                    .put("I am represented by a solicitor in these proceedings, who has signed Section 5, and all "
                                    + "documents for my attention should be sent to my solicitor whose details are as follows",
                            FR_APPLICANT_REPRESENTED_3)
                    .build();

}