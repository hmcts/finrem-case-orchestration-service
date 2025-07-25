package uk.gov.hmcts.reform.finrem.caseorchestration.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static java.util.Optional.ofNullable;

public class ListUtils {

    private ListUtils() {
    }

    /**
     * Adds an item to a list property accessed via getter and setter.
     *
     * <p>
     * This method retrieves the list using the given getter. If the list is null,
     * it creates a new empty list. If the list exists, it makes a new mutable copy
     * (to avoid mutating immutable lists). It then adds the specified item and
     * sets the updated list using the setter.
     *
     * @param <T>    the type of elements in the list
     * @param getter a supplier function that returns the current list or null
     * @param setter a consumer function that accepts and sets the updated list
     * @param item   the item to add to the list
     */
    public static <T> void addItemToList(Supplier<List<T>> getter, Consumer<List<T>> setter, T item) {
        List<T> list = ofNullable(getter.get()).map(ArrayList::new).orElseGet(ArrayList::new);
        list.add(item);
        setter.accept(list);
    }

    /**
     * Returns a singleton immutable list containing the given item,
     * or {@code null} if the item is {@code null}.
     *
     * @param <T>  the type of the item
     * @param item the item to wrap into a list, may be {@code null}
     * @return a singleton list containing {@code item}, or {@code null} if {@code item} is {@code null}
     */
    public static <T> List<T> toSingletonListOrNull(T item) {
        return item == null ? null : List.of(item);
    }

    /**
     * Returns an immutable list containing the given non-null items,
     * or {@code null} if the input array is {@code null}, empty,
     * or contains only {@code null} values.
     *
     * <p>Null values within the input are filtered out from the resulting list.
     *
     * @param <T>   the type of the items
     * @param items the items to include in the list, may be {@code null} or contain {@code null} elements
     * @return an immutable list of non-null items, or {@code null} if none exist
     */
    @SuppressWarnings("java:S1168") // Returning null to indicate 'no valid items' is required by design.
    @SafeVarargs
    public static <T> List<T> toListOrNull(T... items) {
        if (items == null || items.length == 0) {
            return null;
        }
        List<T> result = Arrays.stream(items).filter(Objects::nonNull).toList();
        return result.isEmpty() ? null : result;
    }

    /**
     * Creates a {@link List} from the given varargs, excluding any {@code null} elements.
     * If the input array is {@code null}, empty, or contains only {@code null} values,
     * an empty list is returned.
     *
     * <p>This method never returns {@code null}.
     *
     * @param <T>   the type of the elements
     * @param items the elements to be included in the list, may be {@code null} or contain {@code null} values
     * @return a list containing only the non-{@code null} elements of {@code items}, or an empty list if none
     */
    @SafeVarargs
    public static <T> List<T> safeListWithoutNulls(T... items) {
        if (items == null || items.length == 0) {
            return List.of();
        }
        return Arrays.stream(items).filter(Objects::nonNull).toList();
    }

    /**
     * Returns the given list if it is not null and not empty,
     * or {@code null} if the list is null or empty.
     *
     * @param <T>  the type of elements in the list
     * @param list the input list
     * @return the input list if not null and not empty, otherwise {@code null}
     */
    public static <T> List<T> nullIfEmpty(List<T> list) {
        return (list == null || list.isEmpty()) ? null : list;
    }
}
