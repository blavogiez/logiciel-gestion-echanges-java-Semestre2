package v4 ;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PersonTest {

    private Person alice;
    private Person bob;
    private Person charlie;

    @BeforeEach
    public void initTest() {
        alice = new Person("Alice", "Smith");
        bob = new Person("Bob", "Johnson");
        charlie = new Person("Charlie", "Chep");
    }

    @Test
    public void testAvailabilityAndMatching() {
        int year = 2024;

        // By default, a person is available and not matched
        assertTrue(alice.isAvailable());
        assertFalse(alice.isMatched(year));
        assertTrue(alice.canBeMatched(year));

        // Set as matched, should not be available for new matches for this year
        alice.setMatched(year, true);
        assertTrue(alice.isMatched(year));
        assertFalse(alice.canBeMatched(year));

        // Set as unavailable, should not be available for new matches
        alice.setMatched(year, false);
        alice.setAvailability(false);
        assertFalse(alice.isAvailable());
        assertFalse(alice.canBeMatched(year));

        // Set as available again, should be available for matching
        alice.setAvailability(true);
        assertTrue(alice.canBeMatched(year));
    }

    @Test
    public void testMeetingSpecificCountryRules() {
        // By default, NEED_ONE_HOBBY should not be set
        assertNull(alice.getCriteriaValue(Criteria.NEED_ONE_HOBBY));

        // Set country to France, should trigger NEED_ONE_HOBBY = "true"
        alice.addCriteriaValue(Criteria.COUNTRY_OF_ORIGIN, "France");
        assertEquals("true", alice.getCriteriaValue(Criteria.NEED_ONE_HOBBY));

        // Set country to Germany, should not set NEED_ONE_HOBBY
        Person bob = new Person("Bob", "Martin");
        bob.addCriteriaValue(Criteria.COUNTRY_OF_ORIGIN, "Germany");
        assertNull(bob.getCriteriaValue(Criteria.NEED_ONE_HOBBY));
    }

    @Test
    public void testAddAndRetrieveCriteria() {
        alice.addCriteriaValue(Criteria.HOST_HAS_ANIMAL, "true");
        alice.addCriteriaValue(Criteria.PAIR_GENDER, "Male");

        assertEquals("true", alice.getCriteriaValue(Criteria.HOST_HAS_ANIMAL));
        assertEquals("male", alice.getCriteriaValue(Criteria.PAIR_GENDER));
    }

    public void testAddAndHobby() {
        charlie.addHobby("Basketball");
        charlie.addHobby("Read");

        assertEquals("Basketball", charlie.getHobbies().get(0));
        assertEquals("Read", charlie.getHobbies().get(1));
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
    public void testMultiplePersonsWithDifferentCriteria() {
        alice.addCriteriaValue(Criteria.HOST_HAS_ANIMAL, "true");
        alice.addCriteriaValue(Criteria.PAIR_GENDER, "Male");

        bob.addCriteriaValue(Criteria.GUEST_ANIMAL_ALLERGY, "false");
        bob.addCriteriaValue(Criteria.GENDER, "Male");

        assertEquals("true", alice.getCriteriaValue(Criteria.HOST_HAS_ANIMAL));
        assertEquals("male", alice.getCriteriaValue(Criteria.PAIR_GENDER));
        assertEquals("false", bob.getCriteriaValue(Criteria.GUEST_ANIMAL_ALLERGY));
        assertEquals("male", bob.getCriteriaValue(Criteria.GENDER));
    }
}