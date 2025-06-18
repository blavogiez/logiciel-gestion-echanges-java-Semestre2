package v1;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

// Qu'est-ce qu'une personne ?
// Une personne est un hôte ou un invité dans un échange, qui peut être l'un ou l'autre.

public class Person {
    // Nom et prénom
    private String firstName;
    private String lastName;

    // La date de naissance est stockée dans les critères

    // Dictionnaire de critères associés à leur valeur stockée en chaîne de caractère
    private Map<Criteria, String> criteriaValues;

    // Constructeur de Person avec nom et prénom
    public Person(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.criteriaValues = new HashMap<>();
    }

    // Associer une valeur à un critère
    public void addCriteriaValue(Criteria criteria, String value) {
        this.criteriaValues.put(criteria, value);
    }

    // Récupérer la valeur d'un critère
    public String getCriteriaValue(Criteria criteria) {
        return this.criteriaValues.get(criteria);
    }
    
    // Renvoie sous la forme d'une chaine de caractères les critères et les valeurs de la Person
    public String criteriaToString(){
        String res = " [";
        Set<Criteria> criteriaKeys= this.criteriaValues.keySet();
        for(Criteria i:criteriaKeys){
            res += i +" : " + this.criteriaValues.get(i) + " ; ";
        }
        return res + "]"; 
    }

    // Modélisation de la personne
    @Override
    public String toString(){
        return "Person { " +
                " firstName='" + this.firstName + '\'' +
                ", lastName='" + this.lastName + '\'' +
                ", criteriaValues = " + this.criteriaToString() +
                '}';
    }
}