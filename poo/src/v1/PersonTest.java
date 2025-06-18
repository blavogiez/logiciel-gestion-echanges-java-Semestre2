package v1;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PersonTest {

    private Person alice;
    private Person bob;

    @BeforeEach
    public void initTest() {
        alice = new Person("Alice", "Smith");
        bob = new Person("Bob", "Johnson");
    }

    @Test
    public void testAddAndRetrieveCriteria() {
        alice.addCriteriaValue(Criteria.HOST_HAS_ANIMAL, "true");
        alice.addCriteriaValue(Criteria.PREFERENCE_GENDER, "Male");

        assertEquals("true", alice.getCriteriaValue(Criteria.HOST_HAS_ANIMAL));
        assertEquals("Male", alice.getCriteriaValue(Criteria.PREFERENCE_GENDER));
    }

    @Test
    public void testRetrieveNonExistentCriteria() {
        assertNull(alice.getCriteriaValue(Criteria.GUEST_ANIMAL_ALLERGY));
    }

    @Test
    public void testUpdateCriteriaValue() {
        alice.addCriteriaValue(Criteria.HOST_HAS_ANIMAL, "true");
        assertEquals("true", alice.getCriteriaValue(Criteria.HOST_HAS_ANIMAL));

        // Mise à jour de la valeur
        alice.addCriteriaValue(Criteria.HOST_HAS_ANIMAL, "false");
        assertEquals("false", alice.getCriteriaValue(Criteria.HOST_HAS_ANIMAL));
    }

    @Test
    public void testCriteriaToString() {
        alice.addCriteriaValue(Criteria.HOST_HAS_ANIMAL, "true");
        alice.addCriteriaValue(Criteria.PREFERENCE_GENDER, "Female");

        String expected = " [HOST_HAS_ANIMAL : true ; PREFERENCE_GENDER : Female ; ]";
        assertEquals(expected, alice.criteriaToString());
    }

    @Test
    public void testToString() {
        alice.addCriteriaValue(Criteria.HOST_HAS_ANIMAL, "true");
        alice.addCriteriaValue(Criteria.PREFERENCE_GENDER, "Female");

        String expected = "Person {  firstName='Alice', lastName='Smith', criteriaValues =  [HOST_HAS_ANIMAL : true ; PREFERENCE_GENDER : Female ; ]}";
        assertEquals(expected, alice.toString());
    }

    @Test
    public void testEmptyCriteria() {
        assertEquals("Person {  firstName='Alice', lastName='Smith', criteriaValues =  []}", alice.toString());
    }

    @Test
    public void testMultiplePersonsWithDifferentCriteria() {
        alice.addCriteriaValue(Criteria.HOST_HAS_ANIMAL, "true");
        alice.addCriteriaValue(Criteria.PREFERENCE_GENDER, "Male");

        bob.addCriteriaValue(Criteria.GUEST_ANIMAL_ALLERGY, "false");
        bob.addCriteriaValue(Criteria.GENDER, "Male");

        assertEquals("true", alice.getCriteriaValue(Criteria.HOST_HAS_ANIMAL));
        assertEquals("Male", alice.getCriteriaValue(Criteria.PREFERENCE_GENDER));
        assertEquals("false", bob.getCriteriaValue(Criteria.GUEST_ANIMAL_ALLERGY));
        assertEquals("Male", bob.getCriteriaValue(Criteria.GENDER));
    }
}