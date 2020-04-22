package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.rules.ExpectedException.none;

public class ComplexTypeCollectionTest {

    @Rule
    public ExpectedException expectedException = none();

    private ComplexTypeCollection<ChildInfo> complexTypeCollection;

    @Before
    public void setUp() {
        complexTypeCollection = new ComplexTypeCollection<>();
    }

    @Test
    public void shouldBeAnEmptyList() {
        assertThat(complexTypeCollection.size(), is(0));
    }

    @Test
    public void addChildShouldGetAValidObject() {
        complexTypeCollection.addItem(ChildInfo.builder().name("John").build());

        assertThat(complexTypeCollection.size(), is(1));
        assertThat(complexTypeCollection.getItem(0).getName(), is("John"));
    }

    @Test
    public void shouldInstantiateCollectionWithExistingList() {
        List<ChildInfo> childInfoList = new ArrayList<>();
        childInfoList.add(ChildInfo.builder().name("Vera").build());
        childInfoList.add(ChildInfo.builder().name("Chuck").build());
        childInfoList.add(ChildInfo.builder().name("Dave").build());

        ComplexTypeCollection<ChildInfo> collection = new ComplexTypeCollection<>(childInfoList);

        assertThat(collection.size(), is(childInfoList.size()));
        assertThat(collection.getItem(0), equalTo(childInfoList.get(0)));
        assertThat(collection.getItem(1), equalTo(childInfoList.get(1)));
        assertThat(collection.getItem(2), equalTo(childInfoList.get(2)));
    }

    @Test
    public void addChildCanAddTheSameObjectManyTimes() {
        ChildInfo child = ChildInfo.builder().name("Adam").build();

        complexTypeCollection.addItem(child);
        complexTypeCollection.addItem(child);
        complexTypeCollection.addItem(child);

        assertThat(complexTypeCollection.size(), is(3));
        assertThat(complexTypeCollection.getItem(0), is(complexTypeCollection.getItem(1)));
        assertThat(complexTypeCollection.getItem(1), is(complexTypeCollection.getItem(2)));
    }

    @Test
    public void addChildAddsObjectsInOrder() {
        complexTypeCollection.addItem(ChildInfo.builder().name("Steven").build());
        complexTypeCollection.addItem(ChildInfo.builder().name("George").build());
        complexTypeCollection.addItem(ChildInfo.builder().name("Michael").build());

        assertThat(complexTypeCollection.size(), is(3));
        assertThat(complexTypeCollection.getItem(0).getName(), is("Steven"));
        assertThat(complexTypeCollection.getItem(1).getName(), is("George"));
        assertThat(complexTypeCollection.getItem(2).getName(), is("Michael"));
    }

    @Test
    public void getChildShouldThrowIllegalArgumentExceptionWhenIndexIsLessThan0() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Index must not be less than 0");

        complexTypeCollection.getItem(-10);
    }

    @Test
    public void getChildShouldThrowIllegalArgumentExceptionWhenIndexIsGreaterThanSize() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Index must not be greater than size of array");

        complexTypeCollection.getItem(1);
    }

}