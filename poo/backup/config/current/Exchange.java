package current ;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * The Exchange class represents a pairing between a host and a guest for an exchange program.
 * It contains all logic for affinity calculation, compatibility checks, and display formatting.
 * This class is serializable for saving exchange history.
 */
public class Exchange implements Serializable {
    // The host person in the exchange
    private Person host;
    // The guest person in the exchange
    private Person guest;
    // A unique label for the exchange, based on year and participant IDs
    private String libelle;
    // Year of the exchange (used for equals and history handling)
    private int year;

    // The affinity score for this exchange (lower is better)
    private double affinityScore = DEFAULT_AFFINITY_SCORE;
    // Default affinity score before any calculation
    private static final double DEFAULT_AFFINITY_SCORE = 10.0;

    // Needed for the graphical version ; We need to see the criteria that aren't met in an exchange when clicking on it
    private Set<Criteria> notMetCriteria = new HashSet<Criteria>() ;
 
    // Indicates if this exchange has been assigned/used in a solution
    public boolean affected = false ;

    // Score only calculates once
    
    // CONSTRUCTORS
    /**
     * Constructs an Exchange between a host and a guest.
     * The label is generated automatically.
     */
    public Exchange(Person host, Person guest) {
        this(LocalDate.now().getYear(), host, guest);
    }

    public Exchange(int year, Person host, Person guest) {
        this.year = year ;
        this.host = host;
        this.guest = guest;
        this.libelle = "" + year + ":" + this.guest.getShortCountry() + guest.getID() + "->" + host.getShortCountry() + host.getID();
        
        // Calculating it only one time
        this.affinityScore = this.evaluateAffinity() ;
    }


    // GETTERS

    /** Returns the host of the exchange. */
    public Person getHost() {
        return this.host;
    }

    /** Returns the guest of the exchange. */
    public Person getGuest() {
        return this.guest;
    }

    // Year of exchange
    public int getYear() {
        return this.year ;
    }

    /** Returns the current affinity score of the exchange. */
    public double getAffinityScore() {
        return this.evaluateAffinity();
    }

    // Get the private notMetCriteria unique Set
    public Set<Criteria> getNotMetCriteria() {
        return this.notMetCriteria;
    }

    // SETTERS

    /** Sets the affected status of the exchange (used in assignment algorithms). */
    public void setAffected(boolean affected) {
        this.affected = affected;
    }

    public void addNotMetCriterion(Criteria criterion) {
        this.notMetCriteria.add(criterion);
    }

    /**
     * Adds a value to the affinity score, weighted by the criterion's ratio.
     * If the criterion is mandatory, a large penalty is added.
     */
    public void changeScore(int value, Criteria criterion) {
        // No Integer.MAX_VALUE to avoid problems, not needed
        if (criterion.isMandatory) value+=9999;
        this.affinityScore += value * criterion.getRatio() ; // Default ratio is 1.0 
        
        // If this function was called to raise the score, it means that the criterion wasn't satisfied
        if (value > 0) this.addNotMetCriterion(criterion);

    }

    /**
     * Multiplies the affinity score by a value, weighted by the criterion's ratio.
     * If the criterion is mandatory, a large penalty is added.
     */
    public void changeScore(double value, Criteria criterion) {
        if (criterion.isMandatory) value+=9999;
        this.affinityScore *= (value * criterion.getRatio());

        // If this function was called to raise the score, it means that the criterion wasn't satisfied
        if (value > 1.00) this.addNotMetCriterion(criterion);
    }

    // Compatibility check
    // Outdated method, but kept for compatibility with the previous version.
    // May be called in the case where a Criteria is mandatory and does not allow the exchange to be made.

    /**
     * Evaluates the affinity between the host and the guest.
     * Applies penalties or bonuses based on various criteria and preferences.
     * Returns the final affinity score (lower is better).
     */
    public double evaluateAffinity() { 
        // Resetting affinity score
        this.affinityScore = DEFAULT_AFFINITY_SCORE; 
        // Following handlers treat "boolean" preferences
        // If it is true, a criteria is not fulfilled, following in a score penalty

        // Food differences
        if (foodHandler()) changeScore(10, Criteria.GUEST_FOOD_CONSTRAINT);

        // Animal differences
        if (animalHandler()) changeScore(10, Criteria.GUEST_ANIMAL_ALLERGY);

        // Do they need at least one common hobby?
        if (oneHobbyHandler()) changeScore(10, Criteria.NEED_ONE_HOBBY);

        // History check
        // True means they have been together before
        // False means they have never been

        String history = historyHandler();
        // Lowering the score by a lot if the two wanted the same
        if (history.equals("sameX2")) changeScore(0.1, Criteria.HISTORY);

        // Lowering the score by a bit if one of the two wanted the same and the other doesn't mind
        if (history.equals("sameX1")) changeScore(0.7, Criteria.HISTORY);
        
        // Raising it by a ton if one of the two wanted another person
        // Criteria has two mandatory modes so we have to do the mandatoriness in the parameters 
        if (history.equals("other")) changeScore(5010, Criteria.HISTORY);

        // Other history results are not important. We'll skip
        
        // Age difference
        this.affinityScore += (int) Math.round(this.birthDateHandler() * 10);

        // Gender compatibility
        if (hostGenderHandler()) changeScore(1.5, Criteria.PAIR_GENDER);
        if (guestGenderHandler()) changeScore(1.5, Criteria.GENDER);

        // Lowering the score based on the number of common hobbies on a progressive scale
        // The more common hobbies, the lower the score
        this.affinityScore *= Math.pow(0.85, this.countCommonHobbies());

        // Rounding the score to two decimals
        this.affinityScore = Math.round(this.affinityScore * 100.0) / 100.0;
        return this.affinityScore;
    }

    /* Some countries have specific rules.
       For example, a French student must have at least one satisfied criterion with 
       their pair because otherwise the risk of breakage is too high.

       Returns the number of common interests.
       For the "NEED_ONE_HOBBY" criterion.
    */

    /**
     * Calculates the number of common hobbies between host and guest.
     * Used for the NEED_ONE_HOBBY criterion and affinity calculation.
     */
    public int countCommonHobbies(){
        ArrayList<String> hostHobbies = this.host.getHobbies();
        ArrayList<String> guestHobbies = this.guest.getHobbies();
        int commonHobbies = 0;
        for (String hobby : hostHobbies) {
            if (guestHobbies.contains(hobby)) {
                commonHobbies++;
            }
        }
        return commonHobbies;
    }

    /**
     * Calculates a score based on the age difference between host and guest.
     * Returns a value to be added to the affinity score.
     */
    public double birthDateHandler() {
        String hostBirth = this.host.getCriteriaValue(Criteria.BIRTH_DATE);
        String guestBirth = this.guest.getCriteriaValue(Criteria.BIRTH_DATE);
        if (hostBirth != null && guestBirth != null && !hostBirth.isEmpty() && !guestBirth.isEmpty()) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate today = LocalDate.now();
            LocalDate hostDate = LocalDate.parse(hostBirth, formatter);
            LocalDate guestDate = LocalDate.parse(guestBirth, formatter);

            int hostAge = Period.between(hostDate, today).getYears();
            int guestAge = Period.between(guestDate, today).getYears();
            double ageMoyen = (hostAge + guestAge) / 2.0;

            int diffMois = Math.abs((hostDate.getYear() - guestDate.getYear()) * 12 + (hostDate.getMonthValue() - guestDate.getMonthValue()));

            double scoreAge = 0;
            if (diffMois <= 18) {
                scoreAge += (diffMois / 12.0) * Math.pow(0.9, ageMoyen);
                scoreAge *= 0.9;
            } else {
                scoreAge += (diffMois / 12.0) * Math.pow(0.9, ageMoyen);
                scoreAge *= 1.5;
            }
            return scoreAge;
            // arbitrary weighting
        }
        return 0.0;
    }

    // The following methods return true if the criteria is respected, false otherwise.
    // They are used to evaluate the compatibility of the exchange and their result is used to calculate the affinity score.
    // They are also used to display the compatibility of the exchange.

    /**
     * Checks if at least one participant requires a common hobby and if so, verifies if there is at least one.
     * Returns true if the requirement is not met.
     */
    public boolean oneHobbyHandler() {
        String hostRule = this.host.getCriteriaValue(Criteria.NEED_ONE_HOBBY);
        String guestRule = this.guest.getCriteriaValue(Criteria.NEED_ONE_HOBBY);    

        if ("true".equals(hostRule) || "true".equals(guestRule)) {
            return this.countCommonHobbies() < 1;
        }
        return false;
    }

    /**
     * Checks if the guest has an animal allergy and the host has an animal.
     * Returns true if there is a conflict.
     */
    public boolean animalHandler() {
        String hostHasAnimal = this.host.getCriteriaValue(Criteria.HOST_HAS_ANIMAL);
        String guestAllergy = this.guest.getCriteriaValue(Criteria.GUEST_ANIMAL_ALLERGY);

        if ("true".equals(guestAllergy) && "true".equals(hostHasAnimal)) {
            return true;
        }
        return false;
    }

    /**
     * Checks if the host's preferred gender for the pair matches the guest's gender.
     * Returns true if there is a mismatch.
     */
    public boolean hostGenderHandler() {
        String hostPrefGender = this.host.getCriteriaValue(Criteria.PAIR_GENDER);
        String guestGender = this.guest.getCriteriaValue(Criteria.GENDER);
        try {
            if (!hostPrefGender.equals(guestGender)) {
                return true;
            }
        } catch (NullPointerException e) {
            // If one of the criteria is not defined, we consider it compatible
            return false;
        }

        return false;
    }

    /**
     * Checks if the guest's preferred gender for the pair matches the host's gender.
     * Returns true if there is a mismatch.
     */
    public boolean guestGenderHandler() {
        String guestPrefGender = this.guest.getCriteriaValue(Criteria.PAIR_GENDER);
        String hostGender = this.host.getCriteriaValue(Criteria.GENDER);
        try {
            if (!guestPrefGender.equals(hostGender)) {
                return true;
            }
        } catch (NullPointerException e) {
            // If one of the criteria is not defined, we consider it compatible
            return false;
        }
        return false;
    }

    /**
     * Checks if the host's food and the guest's food constraint are compatible.
     * Returns true if there is a conflict.
     */
    public boolean foodHandler() {
        String hostFood = this.host.getCriteriaValue(Criteria.HOST_FOOD);
        String guestFood = this.guest.getCriteriaValue(Criteria.GUEST_FOOD_CONSTRAINT);
        try {
            if (!hostFood.equals(guestFood)) {
                return true;
            }
        } catch (NullPointerException e) {
            // If one of the criteria is not defined, we consider it compatible
            return false;
        }
        return false;
    }

    /**
     * Returns the preference if at least one of the two persons expressed it.
     * Used for comparison with serialized history.
     */
    public String pastPreferences() {
        String guestPref = this.getGuest().getCriteriaValue(Criteria.HISTORY);
        String hostPref = this.getHost().getCriteriaValue(Criteria.HISTORY);

        // If null, set to empty string
        if (guestPref==null) guestPref="";
        if (hostPref==null) hostPref="";
        
        // Both want the same 
        if (guestPref.equals("same") && hostPref.equals("same")) return "sameX2";
        // If one prefers other, it will be other for the two
        if (guestPref.equals("other") || hostPref.equals("other")) return "other";
        // One of the two would like the same, the other doesn't mind
        if (guestPref.equals("same") || hostPref.equals("same")) return "sameX1";

        // At the end, it's empty
        return "";
    }

    /**
     * Returns a string qualifying the history handling (empty, same, other).
     * Checks if the same people have already been paired in the past.
     */
    public String historyHandler() {
        for (Exchange e : PeopleManager.history) {
            // They have been together before ?

            // This exchange shall be in the past
            if (e.isBefore(this)) {
                if ((this.hasSamePersons(e)) && (this.pastPreferences().equals("sameX1"))) {
                    return "sameX1";
                }
                if ((this.hasSamePersons(e)) && (this.pastPreferences().equals("sameX2"))) {
                    return "sameX2";
                }
                if ((this.hasSamePersons(e)) && (this.pastPreferences().equals("other"))) {
                    return "other";
                }
            }
        }
        // They have never been together before, then it doesn't matter, it's skipped anyway
        return "" ;
    }

    /**
     * Checks if the same people are featured in the other exchange, in the same roles or reversed.
     * Used for history and duplicate detection.
     */
    public boolean hasSamePersons(Exchange other) {
        if (other == null) return false;
        // Same host/guest or inverted host/guest
        boolean direct = this.getHost().getID() == other.getHost().getID() &&
                        this.getGuest().getID() == other.getGuest().getID();
        boolean reverse = this.getHost().getID() == other.getGuest().getID() &&
                        this.getGuest().getID() == other.getHost().getID();
        return direct || reverse;
    }

    // DISPLAY

    /**
     * Returns a detailed string representation of the exchange, including affinity score.
     */
    public String toBigString() {
        // this.evaluateAffinity();
        String res = "Exchange featuring [" +
                    "host: " + this.host.tinyToString() +
                    ", guest: " + this.guest.tinyToString() ;
        if (this.libelle != null && !this.libelle.isEmpty()) {
            res+=", libelle: '" + this.libelle + '\'' +
                    ']';
        } else {
            res += ']';
        }
        return res+= " with a affinity score valued at: " + this.affinityScore + ".";
    }

    /**
     * Returns a short string representation of the exchange (label only).
     */
    public String toTinyString() {
        // this.evaluateAffinity();
        return this.libelle +":"+this.affinityScore;
    }

    /**
     * Returns a compact string representation: label and affinity score.
     */
    public String toString() {
        return this.toTinyString();
    }

    // Equals method

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Exchange other = (Exchange) obj;
        // On considère deux échanges égaux si host et guest sont égaux (dans le même sens ou inversés)
        boolean samePeople = this.host.equals(other.getHost()) && this.guest.equals(other.getGuest());
        boolean sameYear = this.getYear() == other.getYear() ; 
        return samePeople && sameYear ;
    }

    public boolean isBefore(Exchange other) {
        return this.getYear() < other.getYear() ;
    }
}