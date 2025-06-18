package v3;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ExchangeTest {

    private Person alice;
    private Person bob;
    private Exchange e1;

    @BeforeEach
    public void initTest() {
        alice = new Person("Astana", "Nur-sultan");
        bob = new Person("Bob", "Marley");
        e1 = new Exchange(alice, bob);
    }

    @Test
    public void testTheyLikeMusic() {
        e1.evaluateAffinity();
        assertTrue(e1.getAffinityScore()==10.0);

        bob.addHobby("Reggae");
        alice.addHobby("Reggae");
        e1.evaluateAffinity();
        assertTrue(e1.getAffinityScore()==8.5);
    }

    @Test
    public void testOneHobby() {
        alice.addCriteriaValue(Criteria.COUNTRY_OF_ORIGIN, "France");
        e1.evaluateAffinity();

        // Does this result in a horrible affinity ?
        assertTrue(e1.getAffinityScore()>1000);
        bob.addHobby("Jamaica");
        alice.addHobby("Jamaica");
        e1.evaluateAffinity();
        assertTrue(e1.getAffinityScore()==8.5);
    }

    @Test
    public void testGender() {
        alice.addCriteriaValue(Criteria.PAIR_GENDER, "female");
        bob.addCriteriaValue(Criteria.GENDER, "male");
        e1.evaluateAffinity();
        assertTrue(e1.getAffinityScore()==15.0);

        bob.addCriteriaValue(Criteria.PAIR_GENDER, "male");
        e1.evaluateAffinity();
        assertTrue(e1.getAffinityScore()==22.5);

        bob.addCriteriaValue(Criteria.GENDER, "female");
        e1.evaluateAffinity();
        assertTrue(e1.getAffinityScore()==15.0);
    }

    @Test
    public void testPastPreferences() {
        // Both want "same"
        alice.addCriteriaValue(Criteria.HISTORY, "same");
        bob.addCriteriaValue(Criteria.HISTORY, "same");
        assertTrue(e1.pastPreferences().equals("sameX2"));

        // One wants "same", the other doesn't care
        alice.addCriteriaValue(Criteria.HISTORY, "same");
        bob.addCriteriaValue(Criteria.HISTORY, "");
        assertTrue(e1.pastPreferences().equals("sameX1"));

        // One wants "other"
        alice.addCriteriaValue(Criteria.HISTORY, "other");
        bob.addCriteriaValue(Criteria.HISTORY, "same");
        assertTrue(e1.pastPreferences().equals("other"));

        // Both empty
        alice.addCriteriaValue(Criteria.HISTORY, "");
        bob.addCriteriaValue(Criteria.HISTORY, "");
        assertTrue(e1.pastPreferences().equals(""));
    }

    @Test
    public void testHistoryHandlerWithPastExchanges() {
        // Preparing
        PeopleManager.history.clear();

        // Case "other"
        alice.addCriteriaValue(Criteria.HISTORY, "other");
        bob.addCriteriaValue(Criteria.HISTORY, "");
        Exchange past = new Exchange(alice, bob);
        PeopleManager.history.add(past);
        assertTrue(e1.historyHandler().equals("other"));

        // Case "sameX2"
        PeopleManager.history.clear();
        alice.addCriteriaValue(Criteria.HISTORY, "same");
        bob.addCriteriaValue(Criteria.HISTORY, "same");
        Exchange past2 = new Exchange(alice, bob);
        PeopleManager.history.add(past2);
        assertTrue(e1.historyHandler().equals("sameX2"));

        // Case "sameX1"
        PeopleManager.history.clear();
        alice.addCriteriaValue(Criteria.HISTORY, "same");
        bob.addCriteriaValue(Criteria.HISTORY, "");
        Exchange past3 = new Exchange(alice, bob);
        PeopleManager.history.add(past3);
        assertTrue(e1.historyHandler().equals("sameX1"));

        // Case ""
        PeopleManager.history.clear();
        alice.addCriteriaValue(Criteria.HISTORY, "");
        bob.addCriteriaValue(Criteria.HISTORY, "");
        Exchange past4 = new Exchange(alice, bob);
        PeopleManager.history.add(past4);
        assertTrue(e1.historyHandler().equals(""));
    }
}