package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.rules.ExpectedException.none;

public class ChildrenListTest {

    @Rule
    public ExpectedException expectedException = none();

    @Test
    public void shouldBeAnEmptyList() {
        assertThat(new ChildrenList(), is(0));
    }

    @Test
    public void addChildShouldGetAValidObject() {
        ChildrenList list = new ChildrenList();
        list.addChild(ChildInfo.builder().name("John").build());

        assertThat(list.size(), is(1));
        assertThat(list.getChild(0).getName(), is("John"));
    }

    @Test
    public void addChildCanAddTheSameObjectManyTimes() {
        ChildrenList list = new ChildrenList();
        ChildInfo child = ChildInfo.builder().name("Adam").build();

        list.addChild(child);
        list.addChild(child);
        list.addChild(child);

        assertThat(list.size(), is(3));
        assertThat(list.getChild(0), is(list.getChild(1)));
        assertThat(list.getChild(1), is(list.getChild(2)));
    }

    @Test
    public void addChildAddsObjectsInOrder() {
        ChildrenList list = new ChildrenList();

        list.addChild(ChildInfo.builder().name("Steven").build());
        list.addChild(ChildInfo.builder().name("George").build());
        list.addChild(ChildInfo.builder().name("Michael").build());

        assertThat(list.size(), is(3));
        assertThat(list.getChild(0).getName(), is("Steven"));
        assertThat(list.getChild(1).getName(), is("George"));
        assertThat(list.getChild(2).getName(), is("Michael"));
    }

    @Test
    public void getChildShouldThrowIllegalArgumentExceptionWhenIndexIsLessThan0() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Index must not be less than 0");

        new ChildrenList().getChild(-10);
    }

    @Test
    public void getChildShouldThrowIllegalArgumentExceptionWhenIndexIsGreaterThanSize() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Index must not be greater than size of array");

        new ChildrenList().getChild(1);
    }
}
