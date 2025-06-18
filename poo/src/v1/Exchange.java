package v1;

public class Exchange {
    private Person host;
    private Person guest;

    public Exchange(Person host, Person guest) {
        this.host = host;
        this.guest = guest;
    }

    public Person getHost() {
        return this.host;
    }

    public Person getGuest() {
        return this.guest;
    }

    // Vérifie la compatibilité entre le host et le guest
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
        if(hostPrefGender == null && guestGender == null) {
            return true; // Si l'un des deux n'a pas de préférence, on considère que c'est compatible
        }
        if (!hostPrefGender.equals(guestGender)) {
            return false;
        }
        
        return true;
    }

    @Override
    public String toString() {
        return "Exchange{" +
                "host=" + this.host +
                ", guest=" + this.guest +
                '}';
    }
}