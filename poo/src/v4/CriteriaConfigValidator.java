package v4 ;

import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.File;
import java.time.LocalDate;
import java.time.Period;

/**
 * XML configuration class for automatic validation of person criteria
 * Reads an XML configuration file and applies the defined validation rules
 */
public class CriteriaConfigValidator {
    
    // Default path for configuration file
    private static final String DEFAULT_CONFIG_PATH = "res/config/criteria_config.xml";
    
    // Configuration loaded from XML
    private ConfigurationRules config;
    
    /**
     * Inner class to store configuration rules
     */
    public static class ConfigurationRules {
        public boolean refuseAnimalAllergy = false;
        public int minAge = 0;
        public int maxAge = 150;
        public boolean requireGenderSpecified = false;
        public boolean refuseEmptyHistory = false;
        public boolean requireFoodConstraintIfSpecified = false;
        public boolean refuseGuestAllergicIfHostHasAnimal = false;
        public int maxHobbies = -1; // -1 = no limit
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
                    ", refuseGuestAllergicIfHostHasAnimal=" + refuseGuestAllergicIfHostHasAnimal +
                    ", maxHobbies=" + maxHobbies +
                    ", minHobbies=" + minHobbies +
                    ", requireCountryOfOrigin=" + requireCountryOfOrigin +
                    '}';
        }
    }
    
    /**
     * Default constructor - loads configuration from default path
     */
    public CriteriaConfigValidator() {
        this(DEFAULT_CONFIG_PATH);
    }
    
    /**
     * Constructor with custom path
     */
    public CriteriaConfigValidator(String configPath) {
        loadConfiguration(configPath);
    }
    
    /**
     * Loads configuration from XML file
     */
    private void loadConfiguration(String configPath) {
        config = new ConfigurationRules();
        
        File configFile = new File(configPath);
        if (!configFile.exists()) {
            System.out.println("Configuration file not found: " + configPath);
            System.out.println("Using default configuration.");
            return;
        }
        
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(configFile);
            document.getDocumentElement().normalize();
            
            Element root = document.getDocumentElement();
            
            // Reading different configuration criteria
            config.refuseAnimalAllergy = getBooleanValue(root, "refuseAnimalAllergy", false);
            config.minAge = getIntValue(root, "minAge", 0);
            config.maxAge = getIntValue(root, "maxAge", 150);
            config.requireGenderSpecified = getBooleanValue(root, "requireGenderSpecified", false);
            config.refuseEmptyHistory = getBooleanValue(root, "refuseEmptyHistory", false);
            config.requireFoodConstraintIfSpecified = getBooleanValue(root, "requireFoodConstraintIfSpecified", false);
            config.refuseGuestAllergicIfHostHasAnimal = getBooleanValue(root, "refuseGuestAllergicIfHostHasAnimal", false);
            config.maxHobbies = getIntValue(root, "maxHobbies", -1);
            config.minHobbies = getIntValue(root, "minHobbies", 0);
            config.requireCountryOfOrigin = getBooleanValue(root, "requireCountryOfOrigin", false);
            
            System.out.println("Configuration loaded successfully from: " + configPath);
            System.out.println("Applied rules: " + config.toString());
            
        } catch (Exception e) {
            System.err.println("Error loading XML configuration: " + e.getMessage());
            System.out.println("Using default configuration.");
        }
    }
    
    /**
     * Utility methods for reading values from XML
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
                System.err.println("Invalid value for " + tagName + ": " + nodeList.item(0).getTextContent());
            }
        }
        return defaultValue;
    }
    
    /**
     * Validates a person according to configuration rules
     * @param person The person to validate
     * @return true if person is accepted, false otherwise
     */
    public ValidationResult validatePerson(Person person) {
        ValidationResult result = new ValidationResult();
        
        // Check for animal allergy
        if (config.refuseAnimalAllergy) {
            String allergy = person.getCriteriaValue(Criteria.GUEST_ANIMAL_ALLERGY);
            if (allergy != null && allergy.toLowerCase().equals("true")) {
                result.addReason("Person rejected: animal allergy (refuseAnimalAllergy=true)");
                result.setValid(false);
            }
        }
        
        // Age verification
        String birthDateStr = person.getCriteriaValue(Criteria.BIRTH_DATE);
        if (birthDateStr != null && !birthDateStr.isEmpty()) {
            try {
                LocalDate birthDate = LocalDate.parse(birthDateStr);
                int age = Period.between(birthDate, LocalDate.now()).getYears();
                
                if (age < config.minAge) {
                    result.addReason("Person rejected: too young (" + age + " < " + config.minAge + ")");
                    result.setValid(false);
                }
                
                if (age > config.maxAge) {
                    result.addReason("Person rejected: too old (" + age + " > " + config.maxAge + ")");
                    result.setValid(false);
                }
            } catch (Exception e) {
                result.addReason("Error validating age: " + e.getMessage());
                result.setValid(false);
            }
        }
        
        // Check for required gender
        if (config.requireGenderSpecified) {
            String gender = person.getCriteriaValue(Criteria.GENDER);
            if (gender == null || gender.trim().isEmpty()) {
                result.addReason("Person rejected: gender not specified (requireGenderSpecified=true)");
                result.setValid(false);
            }
        }
        
        // History verification
        if (config.refuseEmptyHistory) {
            String history = person.getCriteriaValue(Criteria.HISTORY);
            if (history == null || history.trim().isEmpty()) {
                result.addReason("Person rejected: empty history (refuseEmptyHistory=true)");
                result.setValid(false);
            }
        }
        
        // Food constraints verification
        if (config.requireFoodConstraintIfSpecified) {
            String foodConstraint = person.getCriteriaValue(Criteria.GUEST_FOOD_CONSTRAINT);
            String hostFood = person.getCriteriaValue(Criteria.HOST_FOOD);
            if ((foodConstraint != null && !foodConstraint.trim().isEmpty()) && 
                (hostFood == null || hostFood.trim().isEmpty())) {
                result.addReason("Person rejected: food constraint specified but host food not defined");
                result.setValid(false);
            }
        }
        
    // Check for animal/allergy consistency
    if (config.refuseGuestAllergicIfHostHasAnimal) {
        String hostHasAnimal = person.getCriteriaValue(Criteria.HOST_HAS_ANIMAL);
        String guestIsAllergic = person.getCriteriaValue(Criteria.GUEST_ANIMAL_ALLERGY);

        if ("true".equalsIgnoreCase(hostHasAnimal) && "true".equalsIgnoreCase(guestIsAllergic)) {
            result.addReason("Person rejected: allergic to animals and host has an animal");
            result.setValid(false);
        }
    }

        
        // Check number of hobbies
        if (config.maxHobbies >= 0 && person.getHobbies().size() > config.maxHobbies) {
            result.addReason("Person rejected: too many hobbies (" + person.getHobbies().size() + " > " + config.maxHobbies + ")");
            result.setValid(false);
        }
        
        if (person.getHobbies().size() < config.minHobbies) {
            result.addReason("Person rejected: not enough hobbies (" + person.getHobbies().size() + " < " + config.minHobbies + ")");
            result.setValid(false);
        }
        
        // Check country of origin
        if (config.requireCountryOfOrigin) {
            String country = person.getCriteriaValue(Criteria.COUNTRY_OF_ORIGIN);
            if (country == null || country.trim().isEmpty()) {
                result.addReason("Person rejected: country of origin not specified (requireCountryOfOrigin=true)");
                result.setValid(false);
            }
        }
        
        return result;
    }
    
    /**
     * Class for validation result
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
     * Integrated method to use in readCSV() for automatic validation
     */
    public boolean shouldAcceptPerson(Person person) {
        ValidationResult result = validatePerson(person);
        
        if (!result.isValid()) {
            System.out.println("=== AUTOMATIC VALIDATION ===");
            System.out.println("Person: " + person.tinyToString());
            System.out.println("Result: REJECTED");
            System.out.println("Reasons:");
            System.out.println(result.getReasons());
            
            // If manual verification is enabled, ask for confirmation
            if (PeopleManager.alwaysCheckCSVInputs) {
                System.out.print("Do you still want to add this person? (Yes/No): ");
                String response = PeopleManager.SCANNER_SYSTEM_IN.nextLine().trim().toLowerCase();
                boolean accepted = response.equals("oui") || response.equals("o") || response.equals("yes") || response.equals("y");
                System.out.println(accepted ? "Person added despite the rules." : "Person definitively rejected.");
                System.out.println("==============================\n");
                return accepted;
            } else {
                System.out.println("Person automatically rejected.");
                System.out.println("==============================\n");
                return false;
            }
        }
        
        return true; // Accepted
    }
    
    /**
     * Getter for configuration (for testing or debug)
     */
    public ConfigurationRules getConfig() {
        return config;
    }
}