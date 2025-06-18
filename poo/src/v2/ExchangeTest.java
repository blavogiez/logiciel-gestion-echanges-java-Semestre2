package v2;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ExchangeTest {

    private Person alice;
    private Person bob;
    private Exchange exchange;

    @BeforeEach
    public void initTest() {
        alice = new Person("Astana", "Nur-sultan");
        bob = new Person("Bob", "Marley");
        exchange = new Exchange(alice, bob);
    }

    @Test
    public void testIncompatibleDueToAnimalAllergy() {
        alice.addCriteriaValue(Criteria.HOST_HAS_ANIMAL, "true");
        bob.addCriteriaValue(Criteria.GUEST_ANIMAL_ALLERGY, "true");

        assertFalse(exchange.isCompatible());
    }

    @Test
    public void testIncompatibleDueToGenderPreference() {
        alice.addCriteriaValue(Criteria.HOST_HAS_ANIMAL, "false");
        bob.addCriteriaValue(Criteria.GUEST_ANIMAL_ALLERGY, "false");
        alice.addCriteriaValue(Criteria.PREFERENCE_GENDER, "Female");
        bob.addCriteriaValue(Criteria.GENDER, "Male");

        assertFalse(exchange.isCompatible());
    }

    @Test
    public void testIncompatibleDueToNonCommonHobby() {
        alice.addCriteriaValue(Criteria.HOST_HAS_ANIMAL, "false");
        bob.addCriteriaValue(Criteria.GUEST_ANIMAL_ALLERGY, "false");
        alice.addCriteriaValue(Criteria.PREFERENCE_GENDER, "Female");
        bob.addCriteriaValue(Criteria.GENDER, "Female");
        alice.addCriteriaValue(Criteria.NEED_ONE_HOBBY, "true");
        bob.addCriteriaValue(Criteria.NEED_ONE_HOBBY, "true");

        alice.addHobby("FootBall");
        bob.addHobby("Read");

        assertFalse(exchange.isCompatible());

        alice.addHobby("BasketBall");
        bob.addHobby("BasketBall");


        assertTrue(exchange.isCompatible());
    }

    @Test
    public void testCompatibleCriteria() {
        alice.addCriteriaValue(Criteria.HOST_HAS_ANIMAL, "false");
        bob.addCriteriaValue(Criteria.GUEST_ANIMAL_ALLERGY, "false");
        alice.addCriteriaValue(Criteria.PREFERENCE_GENDER, "Male");
        bob.addCriteriaValue(Criteria.GENDER, "Male");

        assertTrue(exchange.isCompatible());
    }

    //TODO:Refaire/suppr le test il faut les autres Criteria
    // @Test
    // public void testNoCriteriaSet() {
    //     // Aucun critère pour Alice et Bob
    //     assertTrue(exchange.isCompatible());
    // }

    //TODO:Refaire/suppr le test il faut les autres Criteria
    // @Test
    // public void testPartialCriteriaSet() {
    //     alice.addCriteriaValue(Criteria.HOST_HAS_ANIMAL, "false");
    //     // Bob n'a pas de critère
    //     assertTrue(exchange.isCompatible());
    // }

    @Test
    public void testMultipleIncompatibleCriteria() {
        alice.addCriteriaValue(Criteria.HOST_HAS_ANIMAL, "true");
        bob.addCriteriaValue(Criteria.GUEST_ANIMAL_ALLERGY, "true");
        alice.addCriteriaValue(Criteria.PREFERENCE_GENDER, "Female");
        bob.addCriteriaValue(Criteria.GENDER, "Male");

        assertFalse(exchange.isCompatible());
    }

    @Test
    public void testCompatibleWithAdditionalCriteria() {
        alice.addCriteriaValue(Criteria.HOST_HAS_ANIMAL, "false");
        bob.addCriteriaValue(Criteria.GUEST_ANIMAL_ALLERGY, "false");
        alice.addCriteriaValue(Criteria.PREFERENCE_GENDER, "Male");
        bob.addCriteriaValue(Criteria.GENDER, "Male");
        assertTrue(exchange.isCompatible());
    }
}