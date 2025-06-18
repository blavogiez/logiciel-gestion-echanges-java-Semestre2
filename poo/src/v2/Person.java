package v2;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;

// Qu'est-ce qu'une personne ?
// Une personne est un hôte ou un invité dans un échange, qui peut être l'un ou l'autre.

public class Person {
    // Nom et prénom
    private String firstName;
    private String lastName;

    // La date de naissance est stockée dans les critères

    // Dictionnaire de critères associés à leur valeur stockée en chaîne de caractère
    private Map<Criteria, String> criteriaValues;

    // Liste contennant les passions 
    private ArrayList<String> hobbies;
    
    // Constructeur de Person avec nom et prénom
    public Person(String firstName, String lastName) {
        this(firstName, lastName, new HashMap<Criteria, String>(), new ArrayList<String>());
    }

    public Person(String firstName, String lastName, HashMap<Criteria, String> criteriaValues) {
        this(firstName, lastName, criteriaValues, new ArrayList<String>());
    }

    public Person(String firstName, String lastName, ArrayList<String> hobbies) {
        this(firstName, lastName, new HashMap<Criteria, String>(), hobbies);
    }

    public Person(String firstName, String lastName, HashMap<Criteria, String> criteriaValues, ArrayList<String> hobbies) {
        this.firstName = firstName;
        this.lastName = lastName;
        if (Criteria.areCriteriasValid(criteriaValues)) this.criteriaValues = criteriaValues;
        else this.criteriaValues = new HashMap<Criteria, String>();
        this.hobbies = hobbies;
        this.meetingSpecificCountryRules();
    }

    // Associer une valeur à un critère
    public void addCriteriaValue(Criteria criteria, String value) {
        if (Criteria.isCriteriaTypeValid(criteria, value) && Criteria.isCriteriaValueValid(criteria, value)) {
            this.criteriaValues.put(criteria, value);
            this.meetingSpecificCountryRules();
        }
    }
    
    //Ajout un nouveau hobbies
    public void addHobby(String criteria) {
        this.hobbies.add(criteria);
    }

    // Récupérer la valeur d'un critère
    public String getCriteriaValue(Criteria criteria) {
        return this.criteriaValues.get(criteria);
    }
    
    // Renvoie la liste des hobbies
    public ArrayList<String> getHobbies(){
        return this.hobbies;
    }

    // Renvoie sous la forme d'une chaine de caractères les critères et les valeurs de la Person
    public String criteriaToString(){
        String res = "[";
        Set<Criteria> criteriaKeys= this.criteriaValues.keySet();
        for(Criteria i:criteriaKeys){
            res += i +" : " + this.criteriaValues.get(i) + " ; ";
        }
        return res + "]"; 
    }

    public String hobbiesToString(){
        String res = "[";
        for(String i:this.hobbies){
            res += i + " ; ";
        }
        return res + "]"; 
    }

    // Modélisation de la personne
    @Override
    public String toString(){
        return "Person { " +
                " firstName='" + this.firstName + '\'' +
                ", lastName='" + this.lastName + '\'' +
                ", \ncriteriaValues : " + this.criteriaToString() +
                ", •\nhobbies :" + this.hobbiesToString() +
                '}';
    }

    // La gestion de la validité se trouve dans l'enum Criteria

    public boolean isThereIncoherence() {
        // On vérifie si la personne a un animal et est allergique
        String hostHasAnimal = this.getCriteriaValue(Criteria.HOST_HAS_ANIMAL);
        String guestAllergy = this.getCriteriaValue(Criteria.GUEST_ANIMAL_ALLERGY);

        if ("true".equals(guestAllergy) && "true".equals(hostHasAnimal)) {
            return true;
        }
        return false;
    }

    // Cette méthode change les critères en fonction du pays
    // Elle doit être appelée à chaque fois qu'on change un critère (constructeur, ajout)
    public void meetingSpecificCountryRules() {
        try {
            String country = this.getCriteriaValue(Criteria.COUNTRY_OF_ORIGIN).toUpperCase();
            if (country.equals("FRANCE")) {
                this.addCriteriaValue(Criteria.NEED_ONE_HOBBY, "true");
            }
        } catch (NullPointerException e) {
            // Si le pays n'est pas défini, on ne fait rien, ce n'est pas vraiment une erreur
        }
    }
}