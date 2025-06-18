package current;

import java.util.Set;

public class ArbitraryTest {
    public static void main(String[] args) {
        // Création de deux personnes avec plusieurs critères incompatibles
        Person alice = new Person("Alice", "Smith");
        Person bob = new Person("Bob", "Marley");

        // Critères incompatibles
        alice.addCriteriaValue(Criteria.HOST_HAS_ANIMAL, "true");
        bob.addCriteriaValue(Criteria.GUEST_ANIMAL_ALLERGY, "true");

        alice.addCriteriaValue(Criteria.GENDER, "female");
        bob.addCriteriaValue(Criteria.PAIR_GENDER, "male"); // Bob veut une paire masculine

        alice.addCriteriaValue(Criteria.HOST_FOOD, "vegetarian");
        bob.addCriteriaValue(Criteria.GUEST_FOOD_CONSTRAINT, "vegan"); // Bob veut vegan
        
        // Création de l'échange
        Exchange e = new Exchange(alice, bob);

        // Calcul de l'affinité (remplit notMetCriteria)
        e.evaluateAffinity();

        // Affichage des critères non respectés
        Set<Criteria> notMet = e.getNotMetCriteria();
        System.out.println("Critères non respectés pour l'échange Alice/Bob :");
        for (Criteria c : notMet) {
            System.out.println("- " + c);
        }

        System.out.println();
        System.out.println(e);
    }
}