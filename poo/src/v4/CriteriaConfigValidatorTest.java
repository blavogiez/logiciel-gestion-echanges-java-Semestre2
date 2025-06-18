package v4 ;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.* ;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CriteriaConfigValidatorTest {

    @TempDir
    Path tempDir;
    
    private CriteriaConfigValidator validator;

    /**
     * Simple Person class for testing purposes
     */
    static class TestPerson extends Person {
        private Map<Criteria, String> criteriaValues = new HashMap<>();

        public TestPerson() {
            // Default constructor
        }
        
        public void setCriteriaValue(Criteria criteria, String value) {
            criteriaValues.put(criteria, value);
        }
        
        @Override
        public String getCriteriaValue(Criteria criteria) {
            return criteriaValues.get(criteria);
        }
        
        @Override
        public String tinyToString() {
            return "TestPerson";
        }
    }

    @Test
    @DisplayName("Test default constructor")
    void testDefaultConstructor() {
        assertDoesNotThrow(() -> {
            validator = new CriteriaConfigValidator();
            assertNotNull(validator.getConfig());
        });
    }

    @Test
    @DisplayName("Test constructor with non-existent file")
    void testConstructorWithNonExistentFile() {
        String nonExistentPath = "nonexistent/config.xml";
        
        assertDoesNotThrow(() -> {
            validator = new CriteriaConfigValidator(nonExistentPath);
            assertNotNull(validator.getConfig());
        });
    }

    @Test
    @DisplayName("Test loading valid XML configuration")
    void testLoadValidXMLConfiguration() throws IOException {
        File configFile = createTestConfigFile(
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<configuration>\n" +
            "    <refuseAnimalAllergy>true</refuseAnimalAllergy>\n" +
            "    <minAge>18</minAge>\n" +
            "    <maxAge>65</maxAge>\n" +
            "    <requireGenderSpecified>true</requireGenderSpecified>\n" +
            "    <refuseEmptyHistory>true</refuseEmptyHistory>\n" +
            "    <maxHobbies>5</maxHobbies>\n" +
            "    <minHobbies>1</minHobbies>\n" +
            "    <requireCountryOfOrigin>true</requireCountryOfOrigin>\n" +
            "</configuration>"
        );

        validator = new CriteriaConfigValidator(configFile.getAbsolutePath());
        CriteriaConfigValidator.ConfigurationRules config = validator.getConfig();

        assertTrue(config.refuseAnimalAllergy);
        assertEquals(18, config.minAge);
        assertEquals(65, config.maxAge);
        assertTrue(config.requireGenderSpecified);
        assertTrue(config.refuseEmptyHistory);
        assertEquals(5, config.maxHobbies);
        assertEquals(1, config.minHobbies);
        assertTrue(config.requireCountryOfOrigin);
    }

    @Test
    @DisplayName("Test validation - refuse animal allergy")
    void testValidatePersonRefuseAnimalAllergy() throws IOException {
        File configFile = createTestConfigFile(
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<configuration>\n" +
            "    <refuseAnimalAllergy>true</refuseAnimalAllergy>\n" +
            "</configuration>"
        );

        validator = new CriteriaConfigValidator(configFile.getAbsolutePath());
        
        TestPerson person = new TestPerson();
        person.setCriteriaValue(Criteria.GUEST_ANIMAL_ALLERGY, "true");

        CriteriaConfigValidator.ValidationResult result = validator.validatePerson(person);
        
        assertFalse(result.isValid());
        assertTrue(result.getReasons().contains("animal allergy"));
    }

    @Test
    @DisplayName("Test validation - person too young")
    void testValidatePersonTooYoung() throws IOException {
        File configFile = createTestConfigFile(
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<configuration>\n" +
            "    <minAge>18</minAge>\n" +
            "</configuration>"
        );

        validator = new CriteriaConfigValidator(configFile.getAbsolutePath());
        
        TestPerson person = new TestPerson();
        LocalDate youngBirthDate = LocalDate.now().minusYears(16);
        person.setCriteriaValue(Criteria.BIRTH_DATE, youngBirthDate.toString());

        CriteriaConfigValidator.ValidationResult result = validator.validatePerson(person);
        
        assertFalse(result.isValid());
        assertTrue(result.getReasons().contains("too young"));
    }

    @Test
    @DisplayName("Test validation - person too old")
    void testValidatePersonTooOld() throws IOException {
        File configFile = createTestConfigFile(
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<configuration>\n" +
            "    <maxAge>65</maxAge>\n" +
            "</configuration>"
        );

        validator = new CriteriaConfigValidator(configFile.getAbsolutePath());
        
        TestPerson person = new TestPerson();
        LocalDate oldBirthDate = LocalDate.now().minusYears(70);
        person.setCriteriaValue(Criteria.BIRTH_DATE, oldBirthDate.toString());

        CriteriaConfigValidator.ValidationResult result = validator.validatePerson(person);
        
        assertFalse(result.isValid());
        assertTrue(result.getReasons().contains("too old"));
    }

    @Test
    @DisplayName("Test validation - require gender specified")
    void testValidatePersonRequireGender() throws IOException {
        File configFile = createTestConfigFile(
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<configuration>\n" +
            "    <requireGenderSpecified>true</requireGenderSpecified>\n" +
            "</configuration>"
        );

        validator = new CriteriaConfigValidator(configFile.getAbsolutePath());
        
        TestPerson person = new TestPerson();
        // Don't set gender

        CriteriaConfigValidator.ValidationResult result = validator.validatePerson(person);
        
        assertFalse(result.isValid());
        assertTrue(result.getReasons().contains("gender not specified"));
    }

    @Test
    @DisplayName("Test validation - refuse empty history")
    void testValidatePersonRefuseEmptyHistory() throws IOException {
        File configFile = createTestConfigFile(
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<configuration>\n" +
            "    <refuseEmptyHistory>true</refuseEmptyHistory>\n" +
            "</configuration>"
        );

        validator = new CriteriaConfigValidator(configFile.getAbsolutePath());
        
        TestPerson person = new TestPerson();
        person.setCriteriaValue(Criteria.HISTORY, "");

        CriteriaConfigValidator.ValidationResult result = validator.validatePerson(person);
        
        assertFalse(result.isValid());
        assertTrue(result.getReasons().contains("empty history"));
    }

    @Test
    @DisplayName("Test validation - guest allergic with host having animal")
    void testValidatePersonGuestAllergicWithHostAnimal() throws IOException {
        File configFile = createTestConfigFile(
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<configuration>\n" +
            "    <refuseGuestAllergicIfHostHasAnimal>true</refuseGuestAllergicIfHostHasAnimal>\n" +
            "</configuration>"
        );

        validator = new CriteriaConfigValidator(configFile.getAbsolutePath());
        
        TestPerson person = new TestPerson();
        person.setCriteriaValue(Criteria.HOST_HAS_ANIMAL, "true");
        person.setCriteriaValue(Criteria.GUEST_ANIMAL_ALLERGY, "true");

        CriteriaConfigValidator.ValidationResult result = validator.validatePerson(person);
        
        assertFalse(result.isValid());
        assertTrue(result.getReasons().contains("allergic to animals and host has an animal"));
    }

    @Test
    @DisplayName("Test validation - require country of origin")
    void testValidatePersonRequireCountryOfOrigin() throws IOException {
        File configFile = createTestConfigFile(
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<configuration>\n" +
            "    <requireCountryOfOrigin>true</requireCountryOfOrigin>\n" +
            "</configuration>"
        );

        validator = new CriteriaConfigValidator(configFile.getAbsolutePath());
        
        TestPerson person = new TestPerson();
        // Don't set country of origin

        CriteriaConfigValidator.ValidationResult result = validator.validatePerson(person);
        
        assertFalse(result.isValid());
        assertTrue(result.getReasons().contains("country of origin not specified"));
    }

    @Test
    @DisplayName("Test food constraint without host food")
    void testValidatePersonFoodConstraintWithoutHostFood() throws IOException {
        File configFile = createTestConfigFile(
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<configuration>\n" +
            "    <requireFoodConstraintIfSpecified>true</requireFoodConstraintIfSpecified>\n" +
            "</configuration>"
        );

        validator = new CriteriaConfigValidator(configFile.getAbsolutePath());
        
        TestPerson person = new TestPerson();
        person.setCriteriaValue(Criteria.GUEST_FOOD_CONSTRAINT, "vegetarian");
        person.setCriteriaValue(Criteria.HOST_FOOD, "");

        CriteriaConfigValidator.ValidationResult result = validator.validatePerson(person);
        
        assertFalse(result.isValid());
        assertTrue(result.getReasons().contains("food constraint specified but host food not defined"));
    }

    @Test
    @DisplayName("Test ValidationResult functionality")
    void testValidationResult() {
        CriteriaConfigValidator.ValidationResult result = new CriteriaConfigValidator.ValidationResult();
        
        assertTrue(result.isValid());
        assertFalse(result.hasReasons());
        assertEquals("", result.getReasons());
        
        result.setValid(false);
        result.addReason("First reason");
        result.addReason("Second reason");
        
        assertFalse(result.isValid());
        assertTrue(result.hasReasons());
        String reasons = result.getReasons();
        assertTrue(reasons.contains("- First reason"));
        assertTrue(reasons.contains("- Second reason"));
    }

    @Test
    @DisplayName("Test ConfigurationRules toString method")
    void testConfigurationRulesToString() {
        CriteriaConfigValidator.ConfigurationRules config = new CriteriaConfigValidator.ConfigurationRules();
        config.refuseAnimalAllergy = true;
        config.minAge = 18;
        
        String toString = config.toString();
        
        assertTrue(toString.contains("refuseAnimalAllergy=true"));
        assertTrue(toString.contains("minAge=18"));
    }

    @Test
    @DisplayName("Test alternative boolean values")
    void testAlternativeBooleanValues() throws IOException {
        File configFile = createTestConfigFile(
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<configuration>\n" +
            "    <refuseAnimalAllergy>1</refuseAnimalAllergy>\n" +
            "    <requireGenderSpecified>yes</requireGenderSpecified>\n" +
            "    <refuseEmptyHistory>True</refuseEmptyHistory>\n" +
            "</configuration>"
        );

        validator = new CriteriaConfigValidator(configFile.getAbsolutePath());
        CriteriaConfigValidator.ConfigurationRules config = validator.getConfig();

        assertTrue(config.refuseAnimalAllergy);
        assertTrue(config.requireGenderSpecified);
        assertTrue(config.refuseEmptyHistory);
    }

    @Test
    @DisplayName("Test invalid integer values")
    void testInvalidIntegerValues() throws IOException {
        File configFile = createTestConfigFile(
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<configuration>\n" +
            "    <minAge>invalid_number</minAge>\n" +
            "    <maxHobbies>not_a_number</maxHobbies>\n" +
            "</configuration>"
        );

        validator = new CriteriaConfigValidator(configFile.getAbsolutePath());
        CriteriaConfigValidator.ConfigurationRules config = validator.getConfig();

        // Should use default values
        assertEquals(0, config.minAge);
        assertEquals(-1, config.maxHobbies);
    }

    @Test
    @DisplayName("Test malformed XML handling")
    void testMalformedXML() throws IOException {
        File configFile = createTestConfigFile("invalid xml content");

        assertDoesNotThrow(() -> {
            validator = new CriteriaConfigValidator(configFile.getAbsolutePath());
            assertNotNull(validator.getConfig());
            // Should use default configuration
            assertFalse(validator.getConfig().refuseAnimalAllergy);
        });
    }

    @Test
    @DisplayName("Test invalid birth date handling")
    void testInvalidBirthDate() throws IOException {
        File configFile = createTestConfigFile(
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<configuration>\n" +
            "    <minAge>18</minAge>\n" +
            "</configuration>"
        );

        validator = new CriteriaConfigValidator(configFile.getAbsolutePath());
        
        TestPerson person = new TestPerson();
        person.setCriteriaValue(Criteria.BIRTH_DATE, "invalid-date");

        CriteriaConfigValidator.ValidationResult result = validator.validatePerson(person);
        
        assertFalse(result.isValid());
        assertTrue(result.getReasons().contains("Error validating age"));
    }

    @Test
    @DisplayName("Test configuration with default values")
    void testConfigurationWithDefaults() throws IOException {
        File configFile = createTestConfigFile(
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<configuration>\n" +
            "    <!-- Partial configuration to test default values -->\n" +
            "    <minAge>21</minAge>\n" +
            "</configuration>"
        );

        validator = new CriteriaConfigValidator(configFile.getAbsolutePath());
        CriteriaConfigValidator.ConfigurationRules config = validator.getConfig();

        assertFalse(config.refuseAnimalAllergy); // default value
        assertEquals(21, config.minAge); // configured value
        assertEquals(150, config.maxAge); // default value
        assertFalse(config.requireGenderSpecified); // default value
    }

    @Test
    @DisplayName("Test age validation with valid age")
    void testValidatePersonValidAge() throws IOException {
        File configFile = createTestConfigFile(
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<configuration>\n" +
            "    <minAge>18</minAge>\n" +
            "    <maxAge>65</maxAge>\n" +
            "</configuration>"
        );

        validator = new CriteriaConfigValidator(configFile.getAbsolutePath());
        
        TestPerson person = new TestPerson();
        LocalDate validBirthDate = LocalDate.now().minusYears(30);
        person.setCriteriaValue(Criteria.BIRTH_DATE, validBirthDate.toString());

        CriteriaConfigValidator.ValidationResult result = validator.validatePerson(person);
        
        assertTrue(result.isValid());
        assertFalse(result.hasReasons());
    }

    /**
     * Utility method to create a test configuration file
     */
    private File createTestConfigFile(String content) throws IOException {
        File configFile = tempDir.resolve("test_config.xml").toFile();
        try (FileWriter writer = new FileWriter(configFile)) {
            writer.write(content);
        }
        return configFile;
    }
}