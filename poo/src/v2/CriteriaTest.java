package v2;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CriteriaTest {

    private Person alice;

    @BeforeEach
    void setUp() {
        alice = new Person("Alice", "Smith");
        alice.addCriteriaValue(Criteria.HOST_HAS_ANIMAL, "true");
        alice.addCriteriaValue(Criteria.PREFERENCE_GENDER, "Female");
    }

    @Test
    void testCriteriaTypeValid() {
        assertTrue(Criteria.isCriteriaTypeValid(Criteria.HOST_HAS_ANIMAL, "true"));
        assertFalse(Criteria.isCriteriaTypeValid(Criteria.HOST_HAS_ANIMAL, "yes"));
        assertTrue(Criteria.isCriteriaTypeValid(Criteria.PREFERENCE_GENDER, "male"));
        assertFalse(Criteria.isCriteriaTypeValid(Criteria.COUNTRY_OF_ORIGIN, ""));
    }

    @Test
    void testCriteriaValueValid() {
        assertTrue(Criteria.isCriteriaValueValid(Criteria.PREFERENCE_GENDER, "male"));
        assertFalse(Criteria.isCriteriaValueValid(Criteria.PREFERENCE_GENDER, "femme"));
        assertTrue(Criteria.isCriteriaValueValid(Criteria.DATE_OF_BIRTH, "2000-01-01"));
        assertFalse(Criteria.isCriteriaValueValid(Criteria.DATE_OF_BIRTH, "2020-01-01"));
        assertFalse(Criteria.isCriteriaValueValid(Criteria.DATE_OF_BIRTH, "je suis le 8 mai 1212 et je suis mal écrit"));
    }
}