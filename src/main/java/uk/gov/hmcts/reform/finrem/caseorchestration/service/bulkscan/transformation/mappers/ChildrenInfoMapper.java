package uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.transformation.mappers;

import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.reform.bsp.common.model.shared.in.OcrDataField;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChildInfo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ComplexTypeCollection;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.bsp.common.mapper.GenericMapper.getValueFromOcrDataFields;
import static uk.gov.hmcts.reform.bsp.common.utils.BulkScanCommonHelper.transformFormDateIntoCcdDate;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.transformation.mappers.ChildrenInfoMapper.Fields.COUNTRY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.transformation.mappers.ChildrenInfoMapper.Fields.DOB;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.transformation.mappers.ChildrenInfoMapper.Fields.GENDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.transformation.mappers.ChildrenInfoMapper.Fields.NAME_OF_CHILD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.transformation.mappers.ChildrenInfoMapper.Fields.RELATION_TO_APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.transformation.mappers.ChildrenInfoMapper.Fields.RELATION_TO_RESPONDENT;

public class ChildrenInfoMapper {

    public static class Fields {
        public static final String NAME_OF_CHILD = "NameOfChild";
        public static final String GENDER = "GenderChild";
        public static final String DOB = "DateOfBirthChild";
        public static final String RELATION_TO_APPLICANT = "RelationshipToApplicantChild";
        public static final String RELATION_TO_RESPONDENT = "RelationshipToRespondentChild";
        public static final String COUNTRY = "CountryOfResidenceChild";
    }

    private ChildrenInfoMapper() {
        // don't
    }

    public static void applyMappings(List<OcrDataField> ocrDataFields, Map<String, Object> modifiedMap) {
        ComplexTypeCollection<ChildInfo> children = new ComplexTypeCollection<>();
        for (int i = 1; isChildInfoPopulated(i, ocrDataFields); i++) {
            children.addItem(mapChild(i, ocrDataFields));
        }

        if (!children.isEmpty()) {
            modifiedMap.put("childrenInfo", children);
        }
    }

    private static boolean isChildInfoPopulated(int i, List<OcrDataField> ocrDataFields) {
        return ocrDataFields.stream().anyMatch(item -> item.getName().equalsIgnoreCase(NAME_OF_CHILD + i));
    }

    private static ChildInfo mapChild(int index, List<OcrDataField> ocrDataFields) {
        String dob = transformFormDateIntoCcdDate(
            "childInfo" + index + ".dateOfBirth",
            getValueOrEmptyString(index, ocrDataFields, DOB)
        );

        return ChildInfo.builder()
            .name(getValueOrEmptyString(index, ocrDataFields, NAME_OF_CHILD))
            .dateOfBirth(dob)
            .gender(getValueFromOcrDataFields(GENDER + index, ocrDataFields).orElse("notGiven"))
            .relationshipToApplicant(getValueOrEmptyString(index, ocrDataFields, RELATION_TO_APPLICANT))
            .relationshipToRespondent(getValueOrEmptyString(index, ocrDataFields, RELATION_TO_RESPONDENT))
            .countryOfResidence(getValueOrEmptyString(index, ocrDataFields, COUNTRY))
            .build();
    }

    private static String getValueOrEmptyString(int index, List<OcrDataField> ocrDataFields, String fieldPrefix) {
        return getValueFromOcrDataFields(fieldPrefix + index, ocrDataFields).orElse(StringUtils.EMPTY);
    }
}
