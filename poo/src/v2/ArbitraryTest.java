package v2;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;

public class ArbitraryTest {
    public static void main(String[] args){
        Person alice = new Person("Alice", "Smith");
        alice.addCriteriaValue(Criteria.HOST_HAS_ANIMAL, "true");
        alice.addCriteriaValue(Criteria.PREFERENCE_GENDER, "Female");

        String expected = " [HOST_HAS_ANIMAL : true ; PREFERENCE_GENDER : Female ; ]";
        System.out.println(alice.criteriaToString());
    }
}
