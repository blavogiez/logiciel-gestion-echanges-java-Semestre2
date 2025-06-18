package current;

import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.File;
import java.time.LocalDate;
import java.time.Period;

/**
 * Classe de configuration XML pour la validation automatique des critères des personnes
 * Lit un fichier de configuration XML et applique les règles de validation définies
 */
public class CriteriaConfigValidator {
    
    // Chemin par défaut du fichier de configuration
    private static final String DEFAULT_CONFIG_PATH = "res/config/criteria_config.xml";
    
    // Configuration chargée depuis le XML
    private ConfigurationRules config;
    
    /**
     * Classe interne pour stocker les règles de configuration
     */
    public static class ConfigurationRules {
        public boolean refuseAnimalAllergy = false;
        public int minAge = 0;
        public int maxAge = 150;
        public boolean requireGenderSpecified = false;
        public boolean refuseEmptyHistory = false;
        public boolean requireFoodConstraintIfSpecified = false;
        public boolean refuseHostWithoutAnimalButGuestAllergic = false;
        public int maxHobbies = -1; // -1 = pas de limite
        public int minHobbies = 0;
        public boolean requireCountryOfOrigin = false;
        
        @Override
        public String toString() {
            return "ConfigurationRules{" +
                    "refuseAnimalAllergy=" + refuseAnimalAllergy +
                    ", minAge=" + minAge +
                    ", maxAge=" + maxAge +
                    ", requireGenderSpecified=" + requireGenderSpecified +
                    ", refuseEmptyHistory=" + refuseEmptyHistory +
                    ", requireFoodConstraintIfSpecified=" + requireFoodConstraintIfSpecified +
                    ", refuseHostWithoutAnimalButGuestAllergic=" + refuseHostWithoutAnimalButGuestAllergic +
                    ", maxHobbies=" + maxHobbies +
                    ", minHobbies=" + minHobbies +
                    ", requireCountryOfOrigin=" + requireCountryOfOrigin +
                    '}';
        }
    }
    
    /**
     * Constructeur par défaut - charge la configuration depuis le chemin par défaut
     */
    public CriteriaConfigValidator() {
        this(DEFAULT_CONFIG_PATH);
    }
    
    /**
     * Constructeur avec chemin personnalisé
     */
    public CriteriaConfigValidator(String configPath) {
        loadConfiguration(configPath);
    }
    
    /**
     * Charge la configuration depuis le fichier XML
     */
    private void loadConfiguration(String configPath) {
        config = new ConfigurationRules();
        
        File configFile = new File(configPath);
        if (!configFile.exists()) {
            System.out.println("Fichier de configuration non trouvé : " + configPath);
            System.out.println("Utilisation de la configuration par défaut.");
            return;
        }
        
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(configFile);
            document.getDocumentElement().normalize();
            
            Element root = document.getDocumentElement();
            
            // Lecture des différents critères de configuration
            config.refuseAnimalAllergy = getBooleanValue(root, "refuseAnimalAllergy", false);
            config.minAge = getIntValue(root, "minAge", 0);
            config.maxAge = getIntValue(root, "maxAge", 150);
            config.requireGenderSpecified = getBooleanValue(root, "requireGenderSpecified", false);
            config.refuseEmptyHistory = getBooleanValue(root, "refuseEmptyHistory", false);
            config.requireFoodConstraintIfSpecified = getBooleanValue(root, "requireFoodConstraintIfSpecified", false);
            config.refuseHostWithoutAnimalButGuestAllergic = getBooleanValue(root, "refuseHostWithoutAnimalButGuestAllergic", false);
            config.maxHobbies = getIntValue(root, "maxHobbies", -1);
            config.minHobbies = getIntValue(root, "minHobbies", 0);
            config.requireCountryOfOrigin = getBooleanValue(root, "requireCountryOfOrigin", false);
            
            System.out.println("Configuration chargée avec succès depuis : " + configPath);
            System.out.println("Règles appliquées : " + config.toString());
            
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de la configuration XML : " + e.getMessage());
            System.out.println("Utilisation de la configuration par défaut.");
        }
    }
    
    /**
     * Méthodes utilitaires pour lire les valeurs du XML
     */
    private boolean getBooleanValue(Element parent, String tagName, boolean defaultValue) {
        NodeList nodeList = parent.getElementsByTagName(tagName);
        if (nodeList.getLength() > 0) {
            String value = nodeList.item(0).getTextContent().trim().toLowerCase();
            return value.equals("true") || value.equals("1") || value.equals("yes");
        }
        return defaultValue;
    }
    
    private int getIntValue(Element parent, String tagName, int defaultValue) {
        NodeList nodeList = parent.getElementsByTagName(tagName);
        if (nodeList.getLength() > 0) {
            try {
                return Integer.parseInt(nodeList.item(0).getTextContent().trim());
            } catch (NumberFormatException e) {
                System.err.println("Valeur invalide pour " + tagName + ": " + nodeList.item(0).getTextContent());
            }
        }
        return defaultValue;
    }
    
    /**
     * Valide une personne selon les règles de configuration
     * @param person La personne à valider
     * @return true si la personne est acceptée, false sinon
     */
    public ValidationResult validatePerson(Person person) {
        ValidationResult result = new ValidationResult();
        
        // Vérification de l'allergie aux animaux
        if (config.refuseAnimalAllergy) {
            String allergy = person.getCriteriaValue(Criteria.GUEST_ANIMAL_ALLERGY);
            if (allergy != null && allergy.toLowerCase().equals("true")) {
                result.addReason("Personne refusée : allergie aux animaux (refuseAnimalAllergy=true)");
                result.setValid(false);
            }
        }
        
        // Vérification de l'âge
        String birthDateStr = person.getCriteriaValue(Criteria.BIRTH_DATE);
        if (birthDateStr != null && !birthDateStr.isEmpty()) {
            try {
                LocalDate birthDate = LocalDate.parse(birthDateStr);
                int age = Period.between(birthDate, LocalDate.now()).getYears();
                
                if (age < config.minAge) {
                    result.addReason("Personne refusée : âge trop jeune (" + age + " < " + config.minAge + ")");
                    result.setValid(false);
                }
                
                if (age > config.maxAge) {
                    result.addReason("Personne refusée : âge trop élevé (" + age + " > " + config.maxAge + ")");
                    result.setValid(false);
                }
            } catch (Exception e) {
                result.addReason("Erreur lors de la validation de l'âge : " + e.getMessage());
                result.setValid(false);
            }
        }
        
        // Vérification du genre requis
        if (config.requireGenderSpecified) {
            String gender = person.getCriteriaValue(Criteria.GENDER);
            if (gender == null || gender.trim().isEmpty()) {
                result.addReason("Personne refusée : genre non spécifié (requireGenderSpecified=true)");
                result.setValid(false);
            }
        }
        
        // Vérification de l'historique
        if (config.refuseEmptyHistory) {
            String history = person.getCriteriaValue(Criteria.HISTORY);
            if (history == null || history.trim().isEmpty()) {
                result.addReason("Personne refusée : historique vide (refuseEmptyHistory=true)");
                result.setValid(false);
            }
        }
        
        // Vérification des contraintes alimentaires
        if (config.requireFoodConstraintIfSpecified) {
            String foodConstraint = person.getCriteriaValue(Criteria.GUEST_FOOD_CONSTRAINT);
            String hostFood = person.getCriteriaValue(Criteria.HOST_FOOD);
            if ((foodConstraint != null && !foodConstraint.trim().isEmpty()) && 
                (hostFood == null || hostFood.trim().isEmpty())) {
                result.addReason("Personne refusée : contrainte alimentaire spécifiée mais nourriture hôte non définie");
                result.setValid(false);
            }
        }
        
        // Vérification de la cohérence animaux/allergie
        if (config.refuseHostWithoutAnimalButGuestAllergic) {
            String hasAnimal = person.getCriteriaValue(Criteria.HOST_HAS_ANIMAL);
            String allergy = person.getCriteriaValue(Criteria.GUEST_ANIMAL_ALLERGY);
            if ((hasAnimal == null || hasAnimal.toLowerCase().equals("false")) && 
                (allergy != null && allergy.toLowerCase().equals("true"))) {
                result.addReason("Personne refusée : allergique aux animaux mais pas d'animal chez l'hôte");
                result.setValid(false);
            }
        }
        
        // Vérification du nombre de hobbies
        if (config.maxHobbies >= 0 && person.getHobbies().size() > config.maxHobbies) {
            result.addReason("Personne refusée : trop de hobbies (" + person.getHobbies().size() + " > " + config.maxHobbies + ")");
            result.setValid(false);
        }
        
        if (person.getHobbies().size() < config.minHobbies) {
            result.addReason("Personne refusée : pas assez de hobbies (" + person.getHobbies().size() + " < " + config.minHobbies + ")");
            result.setValid(false);
        }
        
        // Vérification du pays d'origine
        if (config.requireCountryOfOrigin) {
            String country = person.getCriteriaValue(Criteria.COUNTRY_OF_ORIGIN);
            if (country == null || country.trim().isEmpty()) {
                result.addReason("Personne refusée : pays d'origine non spécifié (requireCountryOfOrigin=true)");
                result.setValid(false);
            }
        }
        
        return result;
    }
    
    /**
     * Classe pour le résultat de validation
     */
    public static class ValidationResult {
        private boolean isValid = true;
        private StringBuilder reasons = new StringBuilder();
        
        public boolean isValid() {
            return isValid;
        }
        
        public void setValid(boolean valid) {
            isValid = valid;
        }
        
        public void addReason(String reason) {
            if (reasons.length() > 0) {
                reasons.append("\n");
            }
            reasons.append("- ").append(reason);
        }
        
        public String getReasons() {
            return reasons.toString();
        }
        
        public boolean hasReasons() {
            return reasons.length() > 0;
        }
    }
    
    /**
     * Méthode intégrée à utiliser dans readCSV() pour valider automatiquement
     */
    public boolean shouldAcceptPerson(Person person) {
        ValidationResult result = validatePerson(person);
        
        if (!result.isValid()) {
            System.out.println("=== VALIDATION AUTOMATIQUE ===");
            System.out.println("Personne : " + person.tinyToString());
            System.out.println("Résultat : REFUSÉE");
            System.out.println("Raisons :");
            System.out.println(result.getReasons());
            
            // Si la vérification manuelle est activée, demander confirmation
            if (PeopleManager.alwaysCheckCSVInputs) {
                System.out.print("Voulez-vous tout de même ajouter cette personne ? (Yes/No): ");
                String response = PeopleManager.SCANNER_SYSTEM_IN.nextLine().trim().toLowerCase();
                boolean accepted = response.equals("yes") || response.equals("y");
                System.out.println(accepted ? "Personne ajoutée malgré les règles." : "Personne définitivement refusée.");
                System.out.println("==============================\n");
                return accepted;
            } else {
                System.out.println("Personne automatiquement refusée.");
                System.out.println("==============================\n");
                return false;
            }
        }
        
        return true; // Acceptée
    }
    
    /**
     * Getter pour la configuration (pour tests ou debug)
     */
    public ConfigurationRules getConfig() {
        return config;
    }
}