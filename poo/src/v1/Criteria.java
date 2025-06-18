package v1;

public enum Criteria {
    GUEST_ANIMAL_ALLERGY('B'),
    HOST_HAS_ANIMAL('B'),
    PREFERENCE_GENDER('T'),
    GENDER('T'),
    DATE_OF_BIRTH('D'),
    COUNTRY_OF_ORIGIN('T');

    private final char type; // Type du critère : 'B', 'T', 'N', ou 'D'

    Criteria(char type) {
        this.type = type;
    }

    public char getType() {
        return this.type;
    }

}