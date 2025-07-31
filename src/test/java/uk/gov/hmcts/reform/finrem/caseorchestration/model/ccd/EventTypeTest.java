package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.CallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertTrue;

@Slf4j
public class EventTypeTest {

    @Test
    public void givenEventHandler_whenMoreThanOneEventMatches_thenThrowError() {

        List<String> errors = new ArrayList<>();
        List<Class<?>> handlerClasses = InterfaceImplementingClasses.findClassesImplementingInterface(CallbackHandler.class,
            "uk.gov.hmcts.reform.finrem.caseorchestration.handler");

        for (EventType event : EventType.values()) {
            for (CaseType caseType : CaseType.values()) {
                for (CallbackType callbackType : CallbackType.values()) {
                    List<Class> matchingClasses = new ArrayList<>();
                    findMatchingClasses(event, caseType, callbackType, handlerClasses, matchingClasses);
                    if (matchingClasses.size() > 1) {
                        errors.add("The combination of event type " + event + ", case type " + caseType
                            + " and callback type " + callbackType + " has been found in more than one class " + matchingClasses);
                    }
                }
            }
        }
        errors.forEach(log::error);
        assertTrue(errors.isEmpty());
    }

    @Test
    public void givenEventType_whenEventTypeNotUnique_ThenThrowError() {
        List<String> errors = new ArrayList<>();
        List<EventType> eventTypeList = new ArrayList<>(Arrays.stream(EventType.values()).toList());
        eventTypeList.forEach(x -> {
            try {
                validateEventTypes(x, eventTypeList, errors);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        errors.forEach(log::error);
        assertTrue(errors.isEmpty());
    }

    private void validateEventTypes(EventType event, List<EventType> allEvents, List<String> errors) {
        if (isEnumTypeMatching(event.toString(), allEvents)) {
            errors.add("The event type " + event + " is duplicated in the EventType enum.");
        }
        if (isEnumCcdTypeMatching(event.getCcdType(), allEvents)) {
            errors.add("The event ccd type " + event.getCcdType() + " is duplicated in the EventType enum.");
        }
    }

    private boolean isEnumTypeMatching(String enumType, List<EventType> allEvents) {
        return allEvents.stream().filter(x -> x.toString().equalsIgnoreCase(enumType)).count() > 1;
    }

    private boolean isEnumCcdTypeMatching(String enumCcdType, List<EventType> allEvents) {
        return allEvents.stream().filter(x -> x.getCcdType().equalsIgnoreCase(enumCcdType)).count() > 1;
    }

    private void findMatchingClasses(EventType event, CaseType caseType, CallbackType callbackType,
                                     List<Class<?>> handlerClasses, List<Class> matchingClasses) {
        handlerClasses.forEach(clazz -> {
            try {
                if (invokeCanHandleMethod(clazz, callbackType, caseType, event)) {
                    matchingClasses.add(clazz);
                }
            } catch (InvocationTargetException | InstantiationException | IllegalAccessException
                     | NoSuchMethodException ex) {
                throw new RuntimeException(ex);
            }
        });
    }

    private boolean invokeCanHandleMethod(Class className,
                                          CallbackType callbackType,
                                          CaseType caseType,
                                          EventType eventType)
        throws InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException {
        Constructor<?>[] cons = className.getDeclaredConstructors();
        int parameterTypesLength = cons[0].getParameterTypes().length;
        List<?> parametersToAdd = new ArrayList<>();
        Object obj;
        if (parameterTypesLength > 0) {
            for (int j = 0; j < parameterTypesLength; j++) {
                parametersToAdd.add(null);
            }
            obj = cons[0].newInstance(parametersToAdd.toArray());
        } else {
            obj = cons[0].newInstance();
        }

        return (boolean) className.getDeclaredMethod("canHandle", CallbackType.class, CaseType.class, EventType.class)
            .invoke(obj, callbackType, caseType, eventType);
    }
}
