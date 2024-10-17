package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.spreadsheet;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.State;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

@Slf4j
@SuppressWarnings("unchecked")
public class CCDConfigValidator {

    protected static final String CASE_FIELD_SHEET = "CaseField";
    protected static final String COMPLEX_TYPES_SHEET = "ComplexTypes";
    protected static final String FIXED_LISTS_SHEET = "FixedLists";
    protected static final String COLLECTION = "Collection";
    protected static final String MULTI_SELECT_LIST = "MultiSelectList";
    protected static final String FIXED_RADIO_LIST = "FixedRadioList";
    protected static final String FIXED_LIST = "FixedList";

    protected static final String INTERVENER_CT = "FR_intervener";
    protected static final String REFUSAL_ORDER_CT = "FR_orderRefusalOrder";
    protected static final int ROW_HEADERS = 2;
    private List<String> ccdFieldsTypeToIgnore = Arrays.asList("Label", "OrderSummary", "CaseHistoryViewer",
        "CasePaymentHistoryViewer", "FlagLauncher", "ComponentLauncher");

    private List<String> ccdFieldsToIgnore = Arrays.asList("caseDocumentConfidential");
    protected static final String STATE_SHEET = "State";
    protected static final String DYNAMIC_LIST = "DynamicList";
    protected static final String DYNAMIC_RADIO_LIST = "DynamicRadioList";
    private List<String> finremCaseDataFieldsToIgnore = List.of("ccdCaseId", "courtDetails", "d11", "isCfvCategoriesAppliedFlag");
    private List<String> fixedListValues = Arrays.asList(FIXED_LIST, FIXED_RADIO_LIST, INTERVENER_CT, REFUSAL_ORDER_CT);
    private List<String> alreadyProcessedCcdFields = new ArrayList<>();

    private Map<String, String> fieldTypesMap = Map.ofEntries(
        Map.entry("Text", "String"),
        Map.entry("AddressUK", "Address"),
        Map.entry("AddressGlobalUK", "Address"),
        Map.entry("Email", "String"),
        Map.entry("Telephone", "String"),
        Map.entry("TextArea", "String"),
        Map.entry("Document", "CaseDocument"),
        Map.entry("YesOrNo", "YesOrNo"),
        Map.entry("MoneyGBP", "BigDecimal"),
        Map.entry("Date", "LocalDate"),
        Map.entry("ChangeOrganisationRequest", "ChangeOrganisationRequest"),
        Map.entry("OrganisationPolicy", "OrganisationPolicy"),
        Map.entry(COLLECTION, "List"),
        Map.entry(MULTI_SELECT_LIST, "List"),
        Map.entry(DYNAMIC_LIST, "DynamicList"),
        Map.entry(DYNAMIC_RADIO_LIST, "DynamicRadioList"),
        Map.entry("FR_ct_draftDirectionOrder", "DraftDirectionOrder"),
        Map.entry("Flags", "CaseFlag"),
        Map.entry("FR_uploadAgreedDraftOrder", "UploadAgreedDraftOrder"),
        Map.entry("FR_uploadSuggestedDraftOrder", "UploadSuggestedDraftOrder")
    );

    private Map<String, String> specialFieldTypes = Map.ofEntries(
        Map.entry("currentUserCaseRole", "CaseRole")
    );

    public List<String> validateCaseFields(List<File> configFiles, Class baseClassToCompareWith)
        throws IOException, InvalidFormatException {

        List<Workbook> workbooks = Arrays.asList(new XSSFWorkbook(configFiles.get(0)), new XSSFWorkbook(configFiles.get(1)));
        List<Sheet> complexTypeSheets =
            new ArrayList<>(Arrays.asList(workbooks.get(0).getSheet(COMPLEX_TYPES_SHEET), workbooks.get(1).getSheet(COMPLEX_TYPES_SHEET)));
        List<Sheet> fixedListSheets =
            new ArrayList<>(Arrays.asList(workbooks.get(0).getSheet(FIXED_LISTS_SHEET), workbooks.get(1).getSheet(FIXED_LISTS_SHEET)));
        List<CcdFieldAttributes> caseFields = collateCaseFields(workbooks.get(0).getSheet(CASE_FIELD_SHEET));
        List<String> validationErrors =
            validateCaseFieldsAgainstClassStructure(baseClassToCompareWith, complexTypeSheets.get(0), fixedListSheets.get(0), caseFields);
        caseFields.addAll(collateCaseFields(workbooks.get(1).getSheet(CASE_FIELD_SHEET)));
        List<String> errors = validateClassStructureAgainstCaseFields(baseClassToCompareWith, complexTypeSheets, fixedListSheets, caseFields);
        errors.forEach(error -> {
            if (!validationErrors.contains(error)) {
                validationErrors.add(error);
            }
        });
        return validationErrors;
    }

    public List<String> validateStates(File configFile)
        throws IOException, InvalidFormatException {

        Workbook workbook = new XSSFWorkbook(configFile);
        Sheet stateSheet = workbook.getSheet(STATE_SHEET);
        List<StateAttributes> stateAttributes = collateStates(stateSheet);

        List<String> validationErrors = validateStatesAgainstClassStructure(stateAttributes);
        return validationErrors;
    }

    private List<String> validateStatesAgainstClassStructure(List<StateAttributes> stateAttributes) {
        List<String> validationErrors = new ArrayList<>();
        for (StateAttributes stateAttribute : stateAttributes) {
            log.info("Validating state: {}", stateAttribute.getId());
            try {
                State.forValue(stateAttribute.getId());
                log.info("State {} is valid", stateAttribute.getId());
            } catch (IllegalArgumentException e) {
                validationErrors.add(String.format("State %s is not defined in the State enum", stateAttribute.getId()));
            }
        }
        return validationErrors;
    }


    private List<String> validateCaseFieldsAgainstClassStructure(Class baseClassToCompareWith, Sheet complexTypeSheet,
                                                                 Sheet fixedListSheet,
                                                                 List<CcdFieldAttributes> caseFields) {
        Set<Class> finremCaseDataClasses = new HashSet<>(Arrays.asList(baseClassToCompareWith));
        finremCaseDataClasses.addAll(new AccessingAllClassesInPackage().getWrappedClassesForClass(baseClassToCompareWith));

        List<String> validationErrors = new ArrayList<>();
        for (CcdFieldAttributes ccdFieldAttributes : caseFields) {
            log.info("Looking for CCD Field Id: {} and Field Type: {}", ccdFieldAttributes.getFieldId(), ccdFieldAttributes.getFieldType());
            boolean found = false;
            for (Class clazz : finremCaseDataClasses) {
                for (Field field : clazz.getDeclaredFields()) {
                    if (field.getName().equals(ccdFieldAttributes.getFieldId())) {
                        found = true;
                        log.info("Found CCD Field Id: {} and Field Type: {}", ccdFieldAttributes.getFieldId(), ccdFieldAttributes.getFieldType());
                        List<String> errors =
                            validateCCDField(Arrays.asList(complexTypeSheet), Arrays.asList(fixedListSheet), ccdFieldAttributes, found, field);
                        errors.forEach(error -> {
                            if (!validationErrors.contains(error)) {
                                validationErrors.add(error);
                            }
                        });
                        break;
                    } else {
                        if (hasMatchingAnnotationForField(field, ccdFieldAttributes.getFieldId())) {
                            found = true;
                            log.info(
                                "Found annotation for CCD Field Id: {} and Field Type: {}", ccdFieldAttributes.getFieldId(),
                                ccdFieldAttributes.getFieldType());
                            List<String> errors =
                                validateCCDField(Arrays.asList(complexTypeSheet), Arrays.asList(fixedListSheet), ccdFieldAttributes, found, field);
                            errors.forEach(error -> {
                                if (!validationErrors.contains(error)) {
                                    validationErrors.add(error);
                                }
                            });
                            break;
                        }
                    }
                }
            }
            if (!found) {
                validationErrors.add("No FinremCaseData Field Found for CCD Field Id: " + ccdFieldAttributes.getFieldId() + " Field Type: "
                    + ccdFieldAttributes.getFieldType());
            }
        }
        return validationErrors;
    }

    private List<String> validateClassStructureAgainstCaseFields(Class baseClassToCompareWith, List<Sheet> complexTypeSheets,
                                                                 List<Sheet> fixedListSheets, List<CcdFieldAttributes> caseFields) {
        Set<Class> finremCaseDataClasses = new HashSet<>(Arrays.asList(baseClassToCompareWith));
        finremCaseDataClasses.addAll(new AccessingAllClassesInPackage().getWrappedClassesForClass(baseClassToCompareWith));

        List<String> validationErrors = new ArrayList<>();
        for (Class clazz : finremCaseDataClasses) {
            for (Field field : clazz.getDeclaredFields()) {
                if (finremCaseDataFieldsToIgnore.contains(field.getName())) {
                    continue;
                }
                log.info("Looking for FinremCaseData Field Id: {} and Field Type: {}", field.getName(), field.getType());
                boolean found = false;
                for (CcdFieldAttributes ccdFieldAttributes : caseFields) {
                    if (field.getAnnotation(JsonUnwrapped.class) != null || field.getAnnotation(JsonIgnore.class) != null) {
                        found = true;
                        log.info("FinremCaseData Field Id: {} and Field Type: {} are annotated with JsonUnwrapped or JsonIgnore", field.getName(),
                            field.getType());
                        break;
                    }
                    if (ccdFieldAttributes.getFieldId().equals(field.getName())) {
                        found = true;
                        log.info("Found FinremCaseData Field Id: {} and Field Type: {}", field.getName(), field.getType());
                        List<String> errors = validateCCDField(complexTypeSheets, fixedListSheets, ccdFieldAttributes, found, field);
                        errors.forEach(error -> {
                            if (!validationErrors.contains(error)) {
                                validationErrors.add(error);
                            }
                        });
                        break;
                    } else {
                        if (hasMatchingAnnotationForField(field, ccdFieldAttributes.getFieldId())) {
                            found = true;
                            log.info("Found annotation for FinremCaseData Field Id: {} and Field Type: {}", field.getName(), field.getType());
                            List<String> errors = validateCCDField(complexTypeSheets, fixedListSheets, ccdFieldAttributes, found, field);
                            errors.forEach(error -> {
                                if (!validationErrors.contains(error)) {
                                    validationErrors.add(error);
                                }
                            });
                            break;
                        }
                    }
                }
                if (!found) {
                    validationErrors.add("No CCD Field Found for FinremCaseData Field Name: " + field.getName() + " Field Type: " + field.getType());
                }
            }
        }
        return validationErrors;
    }

    private boolean hasMatchingAnnotationForField(Field field, String ccdFieldId) {
        JsonProperty annotation = field.getAnnotation(JsonProperty.class);
        return annotation != null && annotation.value().equals(ccdFieldId);
    }

    private List<String> validateCCDField(List<Sheet> complexTypeSheets, List<Sheet> fixedListSheets, CcdFieldAttributes ccdFieldAttributes,
                                          boolean found, Field field) {

        List<String> errors = new ArrayList<>();

        if (isNotASpecialFieldType(ccdFieldAttributes, field) && (isaHighLevelCaseField(complexTypeSheets, ccdFieldAttributes)
            && fieldDoesNotHaveAValidMapping(ccdFieldAttributes, field))) {
            errors.add("CCD Field Id: " + ccdFieldAttributes.getFieldId() + " Field Type: " + ccdFieldAttributes.getFieldType()
                + " does not match " + field.getType().getSimpleName());
        } else {
            if (isComplexType(complexTypeSheets, ccdFieldAttributes.getFieldType())) {
                log.info("Complex Type: {}", ccdFieldAttributes.getFieldType());
                errors.addAll(
                    validateComplexField(fixedListSheets, getComplexType(complexTypeSheets, ccdFieldAttributes.getFieldType()),
                        field.getType()));
            } else if (ccdFieldAttributes.getFieldType().equals(COLLECTION)) {
                errors.addAll(validateCollectionField(complexTypeSheets, fixedListSheets, ccdFieldAttributes, field));
            } else if (ccdFieldAttributes.getFieldType().equals(MULTI_SELECT_LIST) || ccdFieldAttributes.getFieldType().equals(FIXED_LIST)
                || ccdFieldAttributes.getFieldType().equals(FIXED_RADIO_LIST)) {
                errors.addAll(validateFixedListCaseField(fixedListSheets, ccdFieldAttributes, field));
            }
        }
        return errors;
    }

    private boolean fieldDoesNotHaveAValidMapping(CcdFieldAttributes ccdFieldAttributes, Field field) {
        return fieldTypesMap.get(ccdFieldAttributes.getFieldType()) == null
            || !fieldTypesMap.get(ccdFieldAttributes.getFieldType()).equals(field.getType().getSimpleName());
    }

    private boolean isaHighLevelCaseField(List<Sheet> complexTypeSheets, CcdFieldAttributes ccdFieldAttributes) {
        return isComplexType(complexTypeSheets, ccdFieldAttributes.getFieldType())
            && !fixedListValues.contains(ccdFieldAttributes.getFieldType());
    }

    private boolean isNotASpecialFieldType(CcdFieldAttributes ccdFieldAttributes, Field field) {
        return !(specialFieldTypes.get(ccdFieldAttributes.getFieldId()) != null
            && specialFieldTypes.get(ccdFieldAttributes.getFieldId()).equals(field.getType().getSimpleName()));
    }

    private List<String> validateCollectionField(List<Sheet> complexTypeSheets, List<Sheet> fixedListSheets, CcdFieldAttributes ccdFieldAttributes,
                                                 Field field) {
        log.info("CCD Field Id: {} with Field Type: {} is a collection of type {} with field type parameter of {}", ccdFieldAttributes.getFieldId(),
            ccdFieldAttributes.getFieldType(), field.getGenericType().getTypeName(), ccdFieldAttributes.getFieldTypeParameter());
        List<CcdFieldAttributes> complexTypeFields = getComplexType(complexTypeSheets, ccdFieldAttributes.getFieldTypeParameter());
        Type type = field.getGenericType();
        log.info("type: {}", type.getTypeName());
        ParameterizedType stringListType = (ParameterizedType) type;
        Class<?> listClass = (Class<?>) stringListType.getActualTypeArguments()[0];
        log.info("The type of the List class is: {} ", listClass.getName());
        Field[] declaredFields = listClass.getDeclaredFields();
        List<String> collectionErrors = new ArrayList<>();
        Arrays.stream(declaredFields).filter(f -> f.getName().equals("value")
                || hasMatchingAnnotationForField(f, "value"))
            .findFirst()  // get the value field
            .ifPresentOrElse(f -> {
                collectionErrors.addAll(validateComplexField(fixedListSheets, complexTypeFields, f.getType()));
            }, () -> {
                collectionErrors.add("No value field found for collection field: " + ccdFieldAttributes.getFieldId());
            });
        return collectionErrors;
    }

    private List<String> validateComplexField(List<Sheet> fixedListSheets, List<CcdFieldAttributes> complexTypeFields,
                                              Class<?> frClass) {

        log.info("Validate ComplexType - ct collection is : {}", frClass.getName());
        List<String> complexTypeErrors = new ArrayList<>();
        if (alreadyProcessedCcdFields.contains(frClass.getName())) {
            return complexTypeErrors;
        } else {
            alreadyProcessedCcdFields.add(frClass.getName());
        }
        complexTypeFields.stream().forEach(c -> {
            log.info("Matching on field in complex type: {} with type: {}", c.getListElementCode(), c.getFieldType());
            Arrays.stream(getAllDeclaredFields(frClass))
                .filter(vf -> c.getListElementCode().equals(vf.getName()) || hasMatchingAnnotationForField(vf, c.getListElementCode())).findFirst()
                .ifPresentOrElse(vf -> {
                    log.info("Matching on {} complex type field: {}", frClass.getName(), vf.getName());
                    if (fieldTypesMap.get(c.getFieldType()) == null
                        || !fieldTypesMap.get(c.getFieldType()).equals(vf.getType().getSimpleName())) {
                        if (fixedListValues.contains(c.getFieldType())) {
                            log.info("In a fixedlist field with ccd parameter type {} and field type {}", c.getFieldTypeParameter(),
                                vf.getType().getSimpleName());
                            if (!vf.getType().getSimpleName().equals("String")) {
                                complexTypeErrors.addAll(validateFixedList(fixedListSheets, vf.getType(), c.getFieldTypeParameter()));
                            } else {
                                log.info("Fixed list is a string");
                            }
                        }

                    }
                }, () -> {
                    complexTypeErrors.add(
                        "No matching field found for " + c.getListElementCode() + " and type " + c.getFieldType() + " in " + frClass.getName());
                });
        });
        return complexTypeErrors;
    }

    private static Field[] getAllDeclaredFields(Class<?> frClass) {
        List<Field[]> fields = new ArrayList<>();
        fields.add(frClass.getDeclaredFields());
        fields.add(getJsonUnwrappedFields(frClass));

        if (frClass.getSuperclass() != null) {
            fields.add(getAllDeclaredFields(frClass.getSuperclass()));
        }

        return fields.stream()
            .flatMap(Stream::of)
            .toArray(Field[]::new);
    }

    private static Field[] getJsonUnwrappedFields(Class<?> clazz) {
        return Arrays.stream(clazz.getDeclaredFields())
            .filter(f -> f.isAnnotationPresent(JsonUnwrapped.class))
            .map(f -> f.getType().getDeclaredFields())
            .flatMap(Stream::of)
            .toArray(Field[]::new);
    }

    private List<String> validateFixedListCaseField(List<Sheet> fixedListSheets, CcdFieldAttributes ccdFieldAttributes, Field field) {

        log.info("Validate FixedList CCD Field Id: {} Field Type: {} is a collection of type {} with field type parameter of {}",
            ccdFieldAttributes.getFieldId(), ccdFieldAttributes.getFieldType(), field.getGenericType().getTypeName(),
            ccdFieldAttributes.getFieldTypeParameter());
        Type type = field.getGenericType();
        log.info("type: {}", type.getTypeName());
        Class<?> valueType;
        if (type instanceof ParameterizedType) {
            ParameterizedType stringListType = (ParameterizedType) type;
            Class<?> listClass = (Class<?>) stringListType.getActualTypeArguments()[0];
            log.info("The type of the List class is: {}", listClass.getName());
            valueType = (Class<?>) ((ParameterizedType) type).getActualTypeArguments()[0];
        } else {
            valueType = (Class<?>) type;
        }

        if (valueType.getSimpleName().equals("String")) {
            log.info("Fixed list is a string");
            return Collections.EMPTY_LIST;
        } else {
            log.info("In a fixedlist field with ccd parameter type {} and field type {}", ccdFieldAttributes.getFieldTypeParameter(),
                valueType.getSimpleName());
            return validateFixedList(fixedListSheets, valueType, ccdFieldAttributes.getFieldTypeParameter());
        }
    }

    private List<String> validateFixedList(List<Sheet> fixedListSheets, Class fixedListClass, String fixedListParameterName) {

        List<String> fixedListErrors = new ArrayList<>();
        if (alreadyProcessedCcdFields.contains(fixedListParameterName)) {
            return fixedListErrors;
        } else {
            alreadyProcessedCcdFields.add(fixedListParameterName);
        }
        log.info("Validating fixed list class {} with parameter name {}", fixedListClass.getName(), fixedListParameterName);
        List<CcdFieldAttributes> fixedListFields = getFixedList(fixedListSheets, fixedListParameterName);
        Method method = findAnnotatedMethod(fixedListClass, JsonValue.class);
        AtomicInteger successCounter = new AtomicInteger(0);
        AtomicInteger errorCounter = new AtomicInteger(0);

        fixedListFields.forEach(f -> {
            Arrays.stream(fixedListClass.getEnumConstants()).filter(e -> validateEnumValue(method, e, f)).findFirst().ifPresentOrElse(e -> {
                log.info("Found enum constant {} for fixedList {} ", e.toString(), f.getFieldId());
                successCounter.incrementAndGet();
            }, () -> {
                fixedListErrors.add(
                    "No enum found for fixed list field " + f.getListElementCode() + " in class " + fixedListClass.getName() + " for fixedList "
                        + fixedListParameterName);
                errorCounter.incrementAndGet();
            });
        });

        if (successCounter.get() != fixedListFields.size()) {
            fixedListErrors.add("Fixed list field count does not match enum count for fixedList " + fixedListParameterName);
        }
        return fixedListErrors;
    }

    private boolean validateEnumValue(Method method, Object e, CcdFieldAttributes f) {
        if (e.toString().equals(f.getListElementCode())) {
            return true;
        }
        if (method != null) {
            return invokeJsonValueMethod(method, e, f);
        } else {
            try {
                JsonProperty annotation = e.getClass().getField(((Enum) e).name()).getAnnotation(JsonProperty.class);
                if (annotation != null) {
                    return annotation.value().equals(f.getListElementCode());
                }
                return false;
            } catch (NoSuchFieldException ex) {
                return false;
            }
        }
    }

    private boolean invokeJsonValueMethod(Method m, Object e, CcdFieldAttributes f) {
        try {
            return m.invoke(e).equals(f.getListElementCode());
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
        } catch (InvocationTargetException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public static Method findAnnotatedMethod(Class<?> clazz, Class<? extends Annotation> annotationClass) {
        for (Method method : clazz.getMethods()) {
            if (method.isAnnotationPresent(annotationClass)) {
                return (method);
            }
        }
        return (null);
    }

    private List<CcdFieldAttributes> collateCaseFields(Sheet sheet) {
        List<CcdFieldAttributes> caseFields = new ArrayList<>();
        int i = 0;
        for (Row row : sheet) {
            if (i > ROW_HEADERS) {
                CcdFieldAttributes fieldAttributes = new CcdFieldAttributes();
                fieldAttributes.setFieldId(row.getCell(3).getStringCellValue());
                fieldAttributes.setFieldType(row.getCell(6).getStringCellValue());
                fieldAttributes.setFieldTypeParameter(row.getCell(8).getStringCellValue());
                if (!ccdFieldsTypeToIgnore.contains(fieldAttributes.getFieldType())
                    && !ccdFieldsToIgnore.contains(fieldAttributes.getListElementCode())) {
                    caseFields.add(fieldAttributes);
                }
            }
            i++;
        }
        return caseFields;
    }

    private List<StateAttributes> collateStates(Sheet sheet) {
        List<StateAttributes> stateFields = new ArrayList<>();
        int i = 0;
        for (Row row : sheet) {
            if (i > ROW_HEADERS) {
                StateAttributes stateAttributes = new StateAttributes();
                stateAttributes.setId(row.getCell(3).getStringCellValue());
                stateAttributes.setName(row.getCell(4).getStringCellValue());
                stateFields.add(stateAttributes);
            }
            i++;
        }
        return stateFields;
    }

    private boolean isComplexType(List<Sheet> complexTypeSheets, String fieldType) {
        int i = 0;
        for (int j = 0; j < complexTypeSheets.size(); j++) {
            for (Row row : complexTypeSheets.get(j)) {
                if (i > ROW_HEADERS) {
                    if (row.getCell(2) != null && row.getCell(2).getStringCellValue().equals(fieldType)) {
                        return true;
                    }
                }
                i++;
            }
        }
        return false;
    }

    private List<CcdFieldAttributes> getComplexType(List<Sheet> complexTypeSheets, String fieldTypeParameter) {
        List<CcdFieldAttributes> caseFields = new ArrayList<>();
        int i = 0;
        for (int j = 0; j < complexTypeSheets.size(); j++) {
            for (Row row : complexTypeSheets.get(j)) {
                if (i > ROW_HEADERS) {
                    if (row.getCell(2).getStringCellValue().equals(fieldTypeParameter)) {
                        CcdFieldAttributes fieldAttributes = new CcdFieldAttributes();
                        fieldAttributes.setFieldId(row.getCell(2).getStringCellValue());
                        fieldAttributes.setListElementCode(row.getCell(3).getStringCellValue());
                        fieldAttributes.setFieldType(row.getCell(4).getStringCellValue());
                        fieldAttributes.setFieldTypeParameter(row.getCell(5).getStringCellValue());
                        if (!ccdFieldsTypeToIgnore.contains(fieldAttributes.getFieldType())
                            && !ccdFieldsToIgnore.contains(fieldAttributes.getListElementCode())) {
                            caseFields.add(fieldAttributes);
                        }
                    }
                }
                i++;
            }
        }
        return caseFields;
    }

    private List<CcdFieldAttributes> getFixedList(List<Sheet> fixedListSheets, String fieldTypeParameter) {
        List<CcdFieldAttributes> caseFields = new ArrayList<>();
        int i = 0;
        for (int j = 0; j < fixedListSheets.size(); j++) {
            for (Row row : fixedListSheets.get(j)) {
                if (i > ROW_HEADERS) {
                    if (row.getCell(2).getStringCellValue().equals(fieldTypeParameter)) {
                        CcdFieldAttributes fieldAttributes = new CcdFieldAttributes();
                        fieldAttributes.setFieldId(row.getCell(2).getStringCellValue());
                        fieldAttributes.setListElementCode(row.getCell(3).getStringCellValue());
                        fieldAttributes.setListElementLabel(row.getCell(4).getStringCellValue());
                        caseFields.add(fieldAttributes);
                    }
                }
                i++;
            }
        }
        return caseFields;
    }

}
