package v4 ;

import java.time.* ;
import java.time.format.* ;
import java.util.* ;

public enum Criteria {
    GUEST_ANIMAL_ALLERGY('B', true),
    HOST_HAS_ANIMAL('B'),
    HOST_FOOD('T'),
    GUEST_FOOD_CONSTRAINT('T', true),
    PAIR_GENDER('T'),
    GENDER('T'),
    BIRTH_DATE('D'),
    COUNTRY_OF_ORIGIN('T'),
    HISTORY('T'),
    NEED_ONE_HOBBY('B', true);

    // 5 ajustable criteria
    /* History
     * Age difference (birth date)
     * Pair gender
     * Guest food constraint
     * Guest animal allergy
     */

    // Type of the criteria: 'B', 'T', 'N', or 'D'
    private final char type; 

    // Ratio used to measure the impact given to the criteria when changing the score
    private double ratio ;

    // Mandatoriness of the criteria (insane score added)
    public boolean isMandatory = false; 

    // CONSTRUCTORS

    // Chained to maximize compatibility
    Criteria(char type) {
        this(type, 1.0);
    }

    Criteria(char type, double ratio) {
        this(type,ratio,false);
    }

    Criteria(char type, boolean isMandatory) {
        this(type, 1.0, isMandatory);
    }

    Criteria(char type, double ratio, boolean isMandatory) {
        this.type = type;
        this.ratio = ratio;
        this.isMandatory = isMandatory;
    }

    // GETTERS
    public char getType() {
        return this.type;
    }

    public double getRatio() {
        return this.ratio;
    }

    public void setRatio(double ratio) {
        this.ratio = ratio;
    }

    /* --- Validation Management ---

    We check:
    validities ::
    inconsistencies :: (allergic but I have an animal)
    
    This function checks if the type of the value associated with the criteria is valid.
    */

    public static boolean isCriteriaTypeValid(Criteria criteria, String value) {
        char type = criteria.getType();
        switch(type) {
            case 'B':
                value = value.toLowerCase(); // convert everything to lowercase
                // Check if the value is "true" or "false"
                return value.equals("true") || value.equals("false");
            case 'T':
                return true ; // For text, any value is accepted, including empty.
            case 'N':
                try {
                    Integer.parseInt(value);
                    return true;
                } catch (NumberFormatException e) {
                    System.out.println("Invalid number format: " + value);
                    System.out.println("Exception: " + e.getMessage());
                    return false;
                }
            case 'D':
                return value.matches("\\d{4}-\\d{2}-\\d{2}"); // Expected date format: yyyy-MM-dd
        }
        return false;
    }

    // Checks if the value for a given criteria is valid.
    public static boolean isCriteriaValueValid(Criteria criteria, String value) {
        value = value.toLowerCase(); // convert everything to lowercase
        // Only some criteria need specific value checks
        if(criteria==Criteria.PAIR_GENDER || criteria==Criteria.GENDER) {
            return value.equals("male") || value.equals("female") || value.equals("other");
        }
        if(criteria==Criteria.BIRTH_DATE) {
            try {   // This exception handling is better placed at data entry.
                // Any person aged 30 or more is not valid (get out)
                return LocalDate.now().minusYears(30).isBefore((LocalDate.parse(value)));
            } catch (DateTimeParseException e) {
                System.out.println("Invalid format, use the following format: yyyy-MM-dd."); 
                return false;
            }
        }   
        if (criteria==Criteria.HISTORY) {
            return value.equals("other") || value.equals("same") || value.equals("");
        }
        return true ;
    }

    // Checks all criteria and their validity. If one is not valid, returns false.
    public static boolean areCriteriasValid(Map<Criteria, String> criterias) {
        for (Map.Entry<Criteria, String> entry : criterias.entrySet()) {
            Criteria criteria = entry.getKey();
            String value = entry.getValue();
            try {
                if (!isCriteriaTypeValid(criteria, value)) {
                    System.out.println("Invalid type for criteria " + criteria + ": " + value);
                    return false;
                }
            } catch (Exception e) {
                System.out.println("Exception during type validation for " + criteria + ": " + e.getMessage());
                return false;
            }
            try {
                if (!isCriteriaValueValid(criteria, value)) {
                    System.out.println("Invalid value for criteria " + criteria + ": " + value);
                    return false;
                }
            } catch (Exception e) {
                System.out.println("Exception during value validation for " + criteria + ": " + e.getMessage());
                return false;
            }
        }
        return true;
    }
}