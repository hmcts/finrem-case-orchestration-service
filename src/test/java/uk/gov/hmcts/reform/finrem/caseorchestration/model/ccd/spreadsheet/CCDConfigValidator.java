package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.spreadsheet;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AccessingAllClassesInPackage;

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
    protected static final int ROW_HEADERS = 2;
    private List<String> ccdFieldsToIgnore = Arrays.asList("Label", "OrderSummary", "CaseHistoryViewer", "CasePaymentHistoryViewer");
    private List<String> fixedListValues = Arrays.asList(FIXED_LIST, FIXED_RADIO_LIST);
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
        Map.entry("DynamicList", "String")
    );

    private Map<String, String> specialFieldTypes = Map.ofEntries(
        Map.entry("currentUserCaseRole", "CaseRole")
    );

    public List<String> validateCCDConfigAgainstClassStructure(File configFile, Class baseClassToCompareWith)
        throws IOException, InvalidFormatException {

        Workbook workbook = new XSSFWorkbook(configFile);
        Sheet caseFieldSheet = workbook.getSheet(CASE_FIELD_SHEET);
        Sheet complexTypeSheet = workbook.getSheet(COMPLEX_TYPES_SHEET);
        Sheet fixedListSheet = workbook.getSheet(FIXED_LISTS_SHEET);
        List<CcdFieldAttributes> caseFields = collateCaseFields(caseFieldSheet);

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
                        validationErrors.addAll(validateCCDField(complexTypeSheet, fixedListSheet, ccdFieldAttributes, found, field));
                        break;
                    } else {
                        if (hasMatchingAnnotationForField(field, ccdFieldAttributes.getFieldId())) {
                            found = true;
                            log.info(
                                "Found annotation for CCD Field Id: {} and Field Type: {}", ccdFieldAttributes.getFieldId(),
                                ccdFieldAttributes.getFieldType());
                            validationErrors.addAll(validateCCDField(complexTypeSheet, fixedListSheet, ccdFieldAttributes, found, field));
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

    private boolean hasMatchingAnnotationForField(Field field, String ccdFieldId) {
        JsonProperty annotation = field.getAnnotation(JsonProperty.class);
        return annotation != null && annotation.value().equals(ccdFieldId);
    }

    private List<String> validateCCDField(Sheet complexTypeSheet, Sheet fixedListSheet, CcdFieldAttributes ccdFieldAttributes, boolean found,
                                          Field field) {

        List<String> errors = new ArrayList<>();

        if (isNotASpecialFieldType(ccdFieldAttributes, field) && (isaHighLevelCaseField(complexTypeSheet, ccdFieldAttributes)
            && fieldDoesNotHaveAValidMapping(ccdFieldAttributes, field))) {
            errors.add("CCD Field Id: " + ccdFieldAttributes.getFieldId() + " Field Type: " + ccdFieldAttributes.getFieldType()
                + " does not match " + field.getType().getSimpleName());
        } else {
            if (isComplexType(complexTypeSheet, ccdFieldAttributes.getFieldType())) {
                log.info("Complex Type: {}", ccdFieldAttributes.getFieldType());
                errors.addAll(
                    validateComplexField(fixedListSheet, getComplexType(complexTypeSheet, ccdFieldAttributes.getFieldType()),
                        field.getType()));
            } else if (ccdFieldAttributes.getFieldType().equals(COLLECTION)) {
                errors.addAll(validateCollectionField(complexTypeSheet, fixedListSheet, ccdFieldAttributes, field));
            } else if (ccdFieldAttributes.getFieldType().equals(MULTI_SELECT_LIST) || ccdFieldAttributes.getFieldType().equals(FIXED_LIST)
                || ccdFieldAttributes.getFieldType().equals(FIXED_RADIO_LIST)) {
                errors.addAll(validateFixedListCaseField(fixedListSheet, ccdFieldAttributes, field));
            }
        }
        return errors;
    }

    private boolean fieldDoesNotHaveAValidMapping(CcdFieldAttributes ccdFieldAttributes, Field field) {
        return fieldTypesMap.get(ccdFieldAttributes.getFieldType()) == null
            || !fieldTypesMap.get(ccdFieldAttributes.getFieldType()).equals(field.getType().getSimpleName());
    }

    private boolean isaHighLevelCaseField(Sheet complexTypeSheet, CcdFieldAttributes ccdFieldAttributes) {
        return !isComplexType(complexTypeSheet, ccdFieldAttributes.getFieldType())
            && !fixedListValues.contains(ccdFieldAttributes.getFieldType());
    }

    private boolean isNotASpecialFieldType(CcdFieldAttributes ccdFieldAttributes, Field field) {
        return !(specialFieldTypes.get(ccdFieldAttributes.getFieldId()) != null
            && specialFieldTypes.get(ccdFieldAttributes.getFieldId()).equals(field.getType().getSimpleName()));
    }

    private List<String> validateCollectionField(Sheet complexTypeSheet, Sheet fixedListSheet, CcdFieldAttributes ccdFieldAttributes, Field field) {

        log.info("CCD Field Id: {} with Field Type: {} is a collection of type {} with field type parameter of {}", ccdFieldAttributes.getFieldId(),
            ccdFieldAttributes.getFieldType(), field.getGenericType().getTypeName(), ccdFieldAttributes.getFieldTypeParameter());
        List<CcdFieldAttributes> complexTypeFields = getComplexType(complexTypeSheet, ccdFieldAttributes.getFieldTypeParameter());
        Type type = field.getGenericType();
        log.info("type: {}", type.getTypeName());
        ParameterizedType stringListType = (ParameterizedType) type;
        Class<?> listClass = (Class<?>) stringListType.getActualTypeArguments()[0];
        log.info("The type of the List class is: {} ", listClass.getName());
        Field[] declaredFields = listClass.getDeclaredFields();
        List<String> collectionErrors = new ArrayList<>();
        Arrays.stream(declaredFields).filter(f -> f.getName().equals("value")).findFirst()  // get the value field
            .ifPresent(f -> {
                collectionErrors.addAll(validateComplexField(fixedListSheet, complexTypeFields, f.getType()));
            });
        return collectionErrors;
    }

    private List<String> validateComplexField(Sheet fixedListSheet, List<CcdFieldAttributes> complexTypeFields,
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
            Arrays.stream(frClass.getDeclaredFields())
                .filter(vf -> c.getListElementCode().equals(vf.getName()) || hasMatchingAnnotationForField(vf, c.getListElementCode())).findFirst()
                .ifPresentOrElse(vf -> {
                    log.info("Matching on {} complex type field: {}", frClass.getName(), vf.getName());
                    if (fieldTypesMap.get(c.getFieldType()) == null
                        || !fieldTypesMap.get(c.getFieldType()).equals(vf.getType().getSimpleName())) {
                        if (fixedListValues.contains(c.getFieldType())) {
                            log.info("In a fixedlist field with ccd parameter type {} and field type {}", c.getFieldTypeParameter(),
                                vf.getType().getSimpleName());
                            if (!vf.getType().getSimpleName().equals("String")) {
                                complexTypeErrors.addAll(validateFixedList(fixedListSheet, vf.getType(), c.getFieldTypeParameter()));
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


    private List<String> validateFixedListCaseField(Sheet fixedListSheet, CcdFieldAttributes ccdFieldAttributes, Field field) {

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
            return validateFixedList(fixedListSheet, valueType, ccdFieldAttributes.getFieldTypeParameter());
        }
    }

    private List<String> validateFixedList(Sheet fixedListSheet, Class fixedListClass, String fixedListParameterName) {

        List<String> fixedListErrors = new ArrayList<>();
        if (alreadyProcessedCcdFields.contains(fixedListParameterName)) {
            return fixedListErrors;
        } else {
            alreadyProcessedCcdFields.add(fixedListParameterName);
        }
        log.info("Validating fixed list class {} with parameter name {}", fixedListClass.getName(), fixedListParameterName);
        List<CcdFieldAttributes> fixedListFields = getFixedList(fixedListSheet, fixedListParameterName);
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
                fieldAttributes.setFieldTypeParameter(row.getCell(7).getStringCellValue());
                if (!ccdFieldsToIgnore.contains(fieldAttributes.getFieldType())) {
                    caseFields.add(fieldAttributes);
                }
            }
            i++;
        }
        return caseFields;
    }

    private boolean isComplexType(Sheet complexTypeSheet, String fieldType) {
        int i = 0;
        for (Row row : complexTypeSheet) {
            if (i > ROW_HEADERS) {
                if (row.getCell(2) != null && row.getCell(2).getStringCellValue().equals(fieldType)) {
                    return true;
                }
            }
            i++;
        }
        return false;
    }

    private List<CcdFieldAttributes> getComplexType(Sheet complexTypeSheet, String fieldTypeParameter) {
        List<CcdFieldAttributes> caseFields = new ArrayList<>();
        int i = 0;
        for (Row row : complexTypeSheet) {
            if (i > ROW_HEADERS) {
                if (row.getCell(2).getStringCellValue().equals(fieldTypeParameter)) {
                    CcdFieldAttributes fieldAttributes = new CcdFieldAttributes();
                    fieldAttributes.setFieldId(row.getCell(2).getStringCellValue());
                    fieldAttributes.setListElementCode(row.getCell(3).getStringCellValue());
                    fieldAttributes.setFieldType(row.getCell(4).getStringCellValue());
                    fieldAttributes.setFieldTypeParameter(row.getCell(5).getStringCellValue());
                    if (!ccdFieldsToIgnore.contains(fieldAttributes.getFieldType())) {
                        caseFields.add(fieldAttributes);
                    }
                }
            }
            i++;
        }
        return caseFields;
    }

    private List<CcdFieldAttributes> getFixedList(Sheet fixedListSheet, String fieldTypeParameter) {
        List<CcdFieldAttributes> caseFields = new ArrayList<>();
        int i = 0;
        for (Row row : fixedListSheet) {
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
        return caseFields;
    }

}
