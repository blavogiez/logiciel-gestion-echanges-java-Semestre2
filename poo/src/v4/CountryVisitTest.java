package v4 ;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

// Real situations using different csv files
public class CountryVisitTest {

    private PeopleManager handler;

    @BeforeEach
    public void initTest() {
        handler = new PeopleManager("StackOfStudents");
        handler.readCSV();
        handler.sortByCountry();
    }

    /* These tests are hard to write because :
    
    Not all the persons from the CSV are added to the effective database because many have incoherences
    10 guests with 10 hosts doesn't always mean 10 exchanges because some are not compatible

    So these tests are more "predicted" and the main objective here is to observe if the expected numbers is still the same, which means the logic didn't change for whatever reason.
    */ 

    @Test 
    public void testTenDelightedExchanges() {
        handler.firstWillVisitSecond("poland", "italy");
        handler.createVisits();

        CountryVisit visit = handler.getCountryVisit("poland", "italy");
        assertEquals("poland", visit.getGuestCountry());
        assertEquals("italy", visit.getHostCountry());

        // 13 italians
        assertEquals(visit.getHostPersons().size(), 13);

        // 14 poles
        assertEquals(visit.getGuestPersons().size(), 14);

        // 13 best exchanges (14 poles visiting 13 italians) at max
        // Though some were impossible because of too high affinity.
        assertEquals(visit.getBestExchanges().size(), 10);
    }

    @Test 
    public void testRoomForThreePoorItalians() {
        handler.firstWillVisitSecond("poland", "italy");
        handler.firstWillVisitSecond("spain", "italy");

        handler.createVisits();
        CountryVisit visit1 = handler.getCountryVisit("poland", "italy");
        CountryVisit visit2 = handler.getCountryVisit("spain", "italy");

        assertEquals("poland", visit1.getGuestCountry());
        assertEquals("italy", visit1.getHostCountry());

        assertEquals("spain", visit2.getGuestCountry());
        assertEquals("italy", visit2.getHostCountry());

        // Checking same size as before
        assertEquals(visit1.getBestExchanges().size(), 10);

        // There are 3 italians left and 14 spaniards

        // But think about it : these 3 italians left were not compatible with the other poles
        // It is very likely that their criterias are too strong to allow any compatibility with others.

        // This way, there is only one italian that is compatible with the spaniards, or at least does not have a horrible affinity.
        assertEquals(visit2.getBestExchanges().size(), 1);
    }
}