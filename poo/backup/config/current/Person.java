package current ;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

// What is a person?
// A person is a host or a guest in an exchange, who can be either one.

public class Person implements Serializable {
    // ATTRIBUTES
    private static int cpt = 1; // Unique ID counter for each person

    private final int ID = cpt++; // Unique and final ID for each person

    // First name and last name of the person
    private String firstName;
    private String lastName;

    // Availability of the person for an exchange
    // May be set to false if the person is injured, sick, or has a personal issue
    // A non available person is removed from the graph of exchanges and cannot be matched.
    // If the person was matched before being set to unavailable, it will be removed from its current exchange.
    private boolean isAvailable = true ; 

    // Matched in the lowest weight graph
    // A matched person is still available, but is not available for new exchanges
    // Being matched depends of the year, because it only happens once a year
    private Map<Integer, Boolean> isMatched = new HashMap<Integer, Boolean>() ;

    // Birth date is stored as a String in the format "dd/MM/yyyy" in the Criterias.

    // Criteria values are stored in a map, where the key is the Criteria enum
    // and the value is a String representing the value associated with that criteria.
    private Map<Criteria, String> criteriaValues;

    // Hobbies list
    private ArrayList<String> hobbies;
    
    // CONSTRUCTORS

    // Chained constructors to allow flexibility in instantiation

    // Case where no parameters are given
    public Person() {
        this("Unknown", "Unknown", new HashMap<Criteria, String>(), new ArrayList<String>());
    }

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
        this.criteriaValues = new HashMap<Criteria, String>();
        for (Criteria crit : criteriaValues.keySet()) {
            String value = criteriaValues.get(crit);
            this.addCriteriaValue(crit, value);
            // La méthode addCriteriaValue gère déjà la validation
            // Il vaut mieux un ajout individuel pour chaque critère plutôt qu'un qui bloque tout.
        }
        this.hobbies = hobbies;
        this.meetingSpecificCountryRules();
    }

    // "SETTERS"

    // Changing the first name and last name of the person

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setAvailability(boolean isAvailable) {
        this.isAvailable = isAvailable;
    }

    public void setMatched(int year, boolean isMatched) {
        this.isMatched.put(year, isMatched);
    }

    // Associating a criteria with a value in the criteriaValues map.
    public void addCriteriaValue(Criteria criteria, String value) {
        value = value.toLowerCase();
        // Transforming "yes" and "no" to "true" and "false"
        // for boolean criterias
        if (value.equals("yes")) value = "true" ;
        else if (value.equals("no")) value = "false" ;

        if (Criteria.isCriteriaTypeValid(criteria, value) && Criteria.isCriteriaValueValid(criteria, value)) {
            this.criteriaValues.put(criteria, value);
        }
        if (criteria == Criteria.COUNTRY_OF_ORIGIN) {
            this.meetingSpecificCountryRules(); // Verifying the specific country rules
        }
    }
    
    // Adding a new hobby
    public void addHobby(String criteria) {
        this.hobbies.add(criteria);
    }

    // GETTERS

    // Get availability 
    public boolean isAvailable() {
        return this.isAvailable;
    }

    // Is this person matched?
    public boolean isMatched(int year) {
        Boolean matched = this.isMatched.get(year);
        return matched != null && matched;
    }

    public boolean canBeMatched(int year) {
        // A person can be matched if it is available and not already matched
        return this.isAvailable && !this.isMatched(year);
    }

    // Get the value associated with a specific criteria
    public String getCriteriaValue(Criteria criteria) {
        return this.criteriaValues.get(criteria);
    }
    
    // Returning hobbies
    public ArrayList<String> getHobbies(){
        return this.hobbies;
    }

    public int getID() {
        return this.ID;
    }

    public String getCountry() {
        // Returns the country of origin of the person
        String country = this.getCriteriaValue(Criteria.COUNTRY_OF_ORIGIN);
        if (country != null) {
            return country.toLowerCase();
        }
        return "???"; // Default value if no country is set
    }

    public String getShortCountry() {
        // Returns the first two letters of the country of origin
        String country = this.getCriteriaValue(Criteria.COUNTRY_OF_ORIGIN);
        if (country != null && country.length() >= 2) {
            return country.substring(0, 3).toUpperCase();
        }
        return "???"; // Default value if no country is set
    }

    // Checking if there is an incoherence in the criteria
    // For example, if the person has an animal and is allergic to it
    public boolean isThereIncoherence() {
        String hostHasAnimal = this.getCriteriaValue(Criteria.HOST_HAS_ANIMAL).toLowerCase();
        String guestAllergy = this.getCriteriaValue(Criteria.GUEST_ANIMAL_ALLERGY).toLowerCase();

        // System.out.println(hostHasAnimal + " : " + guestAllergy);

        if ("true".equals(guestAllergy) && "true".equals(hostHasAnimal)) {
            return true;
        }
        return false;
    }

    // This method checks if the person has a specific criteria depending of its country
    // It has to be called each time the country of origin is set or changed
    public void meetingSpecificCountryRules() {
        try {
            String country = this.getCriteriaValue(Criteria.COUNTRY_OF_ORIGIN).toUpperCase();
            if (country.equals("FRANCE")) {
                this.addCriteriaValue(Criteria.NEED_ONE_HOBBY, "true");
            }
        } catch (NullPointerException e) {
            // If the country of origin is not set, we do nothing as it is not really an error
        }
    }

    // DISPLAY

    // Displaying the criterias as a list of key-value pairs
    public String criteriaToString(){
        String res = "[";
        Set<Criteria> criteriaKeys= this.criteriaValues.keySet();
        int i = 0 ;
        for(Criteria crit:criteriaKeys){
            res += crit +" : " + this.criteriaValues.get(crit);
            if (i < criteriaKeys.size()-1) {
                res += " ; ";
                // Last hobby does not have a semicolon
            }
            i++;
        }
        return res + "]"; 
    }

    // Displaying the hobbies as a list of key-value pairs
    public String hobbiesToString(){
        String res = "[";
        int i = 0 ;
        for(String hobby :this.hobbies){
            res += hobby ;
            if (i < this.hobbies.size()-1) {
                res += " ; ";
                // Last hobby does not have a semicolon
            }
            i++;
        }
        return res + "]"; 
    }

    // Displaying the person as a a string, calling the criteria and hobbies toString methods
    // This is the main method to display a person
    @Override
    public String toString(){
        return "This is the person n°" + this.ID + " :\n" +
                "[" +
                "firstName: '" + this.firstName + '\'' +
                ", lastName: '" + this.lastName + '\'' +
                ", \ncriteriaValues: " + this.criteriaToString() +
                ", \nhobbies: " + this.hobbiesToString() +
                ']';
    }

    // A shorter version of the toString method, for quick display (used in Exchange)
    public String tinyToString() {
        return this.firstName + " " + this.lastName + " (ID: " + this.ID + ")";
    }

    // Equals method

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Person other = (Person) obj;
        return this.ID==other.ID;
    }

    // Validity check methods may be found in the Criteria class as static methods 
}