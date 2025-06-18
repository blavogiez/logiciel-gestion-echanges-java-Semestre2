package v2;

import java.util.ArrayList;

public class Exchange {
    private Person host;
    private Person guest;
    private String libelle;
    

    // Constructeurs surchargés
    public Exchange(Person host, Person guest) {
        this(host,guest,"");
    }

    public Exchange(Person host, Person guest, String libelle) {
        this.host = host;
        this.guest = guest;
        this.libelle = libelle;
    }

    public Exchange(Person host, Person guest, String libelle, ArrayList<String> rules) {
        this(host,guest,libelle);
    }

    // Getters

    public Person getHost() {
        return this.host;
    }

    public Person getGuest() {
        return this.guest;
    }

    // Vérifie la compatibilité entre le host et le guest
    // TODO: Mettre les cas ou les critères ne sont pas définies ou les obliger à les mettre donc changer les tests
    public boolean isCompatible() {
        // Gestion des animaux
        String hostHasAnimal = this.host.getCriteriaValue(Criteria.HOST_HAS_ANIMAL);
        String guestAllergy = this.guest.getCriteriaValue(Criteria.GUEST_ANIMAL_ALLERGY);

        if ("true".equals(guestAllergy) && "true".equals(hostHasAnimal)) {
            return false;
        }

        // Gestion du genre
        String hostPrefGender = this.host.getCriteriaValue(Criteria.PREFERENCE_GENDER);
        String guestGender = this.guest.getCriteriaValue(Criteria.GENDER);
        if (!hostPrefGender.equals(guestGender)) {
            return false;
        }

        String hostHobbies = this.host.getCriteriaValue(Criteria.NEED_ONE_HOBBY);
        String guestHobbies = this.guest.getCriteriaValue(Criteria.NEED_ONE_HOBBY);
        if(!this.host.getHobbies().isEmpty() && !this.guest.getHobbies().isEmpty() && (hostHobbies.equals("true") || guestHobbies.equals("true")) && countCommonHobbies() < 1){
            return false;
        }
        return true;
    }

    // Certains pays ont des règles spécifiques
    // Par exemple, un étudiant français doit avoir au moins un critère satisfait avec 
    // son pair car sinon le risque de cassure ets trop important.

    //Donne le nombre de centre d'interêt en commun
    //Dans le cadre du critère "NEED_ONE_HOBBY"
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

    // Obsolète
    // public String isThereSpecificCountryRules() {
    //     this.rules.add(this.host.meetingSpecificCountryRules());
    //     this.rules.add(this.guest.meetingSpecificCountryRules());
    // }

    @Override
    public String toString() {
        if (this.libelle != null && !this.libelle.isEmpty()) {
            return "Exchange{" +
                    "host=" + this.host +
                    ", guest=" + this.guest +
                    ", libelle='" + this.libelle + '\'' +
                    '}';
        }
        return "Exchange{" +
                "host=" + this.host +
                ", guest=" + this.guest +
                '}';
    }
}