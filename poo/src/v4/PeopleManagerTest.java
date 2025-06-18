package v4 ;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;

// Mostly about serialization tests
public class PeopleManagerTest {

    private PeopleManager handler;

    @BeforeEach
    public void setup() {
        // Use the provided CSV file for a realistic test
        handler = new PeopleManager("StackOfStudents");
        handler.readCSV();
        handler.sortByCountry();
    }

    @Test
    public void testSerializationAndHistoryUpdate() {
        // Setup a visit and create exchanges
        handler.firstWillVisitSecond("poland", "italy");
        handler.createVisits();

        // Serialize the best exchanges
        handler.serializeExchanges();

        // Check that the file was created
        File serFile = new File(PeopleManager.SERIALIZED_VISIT_HISTORY_PATH);
        assertTrue(serFile.exists(), "Serialized file should exist after serialization");

        // Clear the static history and update it from the file
        PeopleManager.history.clear();
        assertEquals(0, PeopleManager.history.size());

        handler.updateLocalHistory();

        // After update, history should be filled with Exchange objects
        assertFalse(PeopleManager.history.isEmpty(), "History should be loaded from the serialized file");
        for (Exchange e : PeopleManager.history) {
            assertNotNull(e.getHost());
            assertNotNull(e.getGuest());
            assertTrue(e.getAffinityScore() > 0 || e.getAffinityScore() == 0);
        }
    }
}