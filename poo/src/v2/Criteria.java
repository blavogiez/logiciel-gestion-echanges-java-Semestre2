package v2;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Map;

public enum Criteria {
    GUEST_ANIMAL_ALLERGY('B'),
    HOST_HAS_ANIMAL('B'),
    PREFERENCE_GENDER('T'),
    GENDER('T'),
    DATE_OF_BIRTH('D'),
    COUNTRY_OF_ORIGIN('T'),
    NEED_ONE_HOBBY('B');

    private final char type; // Type du critère : 'B', 'T', 'N', ou 'D'

    Criteria(char type) {
        this.type = type;
    }

    public char getType() {
        return this.type;
    }

    // Gestion validité

    // On cherche ::
    // validites ::
    // incoherences :: (allergique mais j'ai un animal)
    // 
    // Cette fonction vérifie si le type de la valeur associée au critère est valide.

    public static boolean isCriteriaTypeValid(Criteria criteria, String value) {
        char type = criteria.getType();
        switch(type) {
            case 'B':
                value = value.toLowerCase(); // on met tout en minuscule
                // On vérifie si la valeur est "true" ou "false"
                return value.equals("true") || value.equals("false");
            case 'T':
                return (value.length()>0); // TODO: Add validation for text criteria
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
        }
        return false;
    }

    public static boolean isCriteriaValueValid(Criteria criteria, String value) {
        value = value.toLowerCase(); // on met tout en minuscule
        // Mode en if car pas toutes les valeurs sont concernées
        if(criteria==Criteria.PREFERENCE_GENDER || criteria==Criteria.GENDER) return value.equals("male") || value.equals("female") || value.equals("other");
        if(criteria==Criteria.DATE_OF_BIRTH) {
            try {   //Cette partie gestion d'excception est plutôt à mettre lors de l'entrée des données.
                return LocalDate.now().minusYears(18).isAfter((LocalDate.parse(value)));
            } catch (DateTimeParseException e) {
                System.out.println("Format invalide, utilisez le format suivant : yyyy-MM-dd."); 
                return false;
            }
        }   
        return true ;
    }

    // Vérifie l'ensemble des critères et leur validité. Si un n'est pas valide, on renvoie false.
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