package v3;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class CriteriaTest {

    @Test
    void testCriteriaTypeValid() {
        assertTrue(Criteria.isCriteriaTypeValid(Criteria.HOST_HAS_ANIMAL, "true"));
        assertFalse(Criteria.isCriteriaTypeValid(Criteria.HOST_HAS_ANIMAL, "yes"));
        assertTrue(Criteria.isCriteriaTypeValid(Criteria.PAIR_GENDER, "male"));
    }

    @Test
    void testCriteriaValueValid() {
        assertTrue(Criteria.isCriteriaValueValid(Criteria.PAIR_GENDER, "male"));
        assertFalse(Criteria.isCriteriaValueValid(Criteria.PAIR_GENDER, "femme"));
        assertTrue(Criteria.isCriteriaValueValid(Criteria.BIRTH_DATE, "2000-01-01"));
        assertTrue(Criteria.isCriteriaValueValid(Criteria.BIRTH_DATE, "2020-01-01"));
        assertFalse(Criteria.isCriteriaValueValid(Criteria.BIRTH_DATE, "je suis le 8 mai 1212 et je suis mal écrit"));
    }

    @Test
    void testHistoryValid() {
        assertTrue(Criteria.isCriteriaValueValid(Criteria.HISTORY, ""));
        assertTrue(Criteria.isCriteriaValueValid(Criteria.PAIR_GENDER, "other"));
        assertFalse(Criteria.isCriteriaValueValid(Criteria.HISTORY, "je sais pas trop"));
    }
}