package v3;

// This class is used to run any test to get quick display
public class ArbitraryTest {
    public static void main(String[] args) {
        Person alice = new Person("Alice", "Smith");
        Person bob = new Person("Bob", "Marley");
        Exchange e1 = new Exchange(alice, bob);

        // Both want "same"
        alice.addCriteriaValue(Criteria.HISTORY, "same");
        bob.addCriteriaValue(Criteria.HISTORY, "same");
        System.out.println("Both want same: " + e1.pastPreferences()); // sameX2

        // One wants "same", the other doesn't care
        alice.addCriteriaValue(Criteria.HISTORY, "same");
        bob.addCriteriaValue(Criteria.HISTORY, "");
        System.out.println("One wants same, other doesn't care: " + e1.pastPreferences()); // sameX1

        // One wants "other"
        alice.addCriteriaValue(Criteria.HISTORY, "other");
        bob.addCriteriaValue(Criteria.HISTORY, "same");
        System.out.println("One wants other: " + e1.pastPreferences()); // other

        // Both empty
        alice.addCriteriaValue(Criteria.HISTORY, "");
        bob.addCriteriaValue(Criteria.HISTORY, "");
        System.out.println("Both empty: " + e1.pastPreferences()); // ""
    }
}
