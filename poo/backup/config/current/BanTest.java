package current;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class BanTest {

    private PeopleManager handler;
    private Person alice;
    private Person bob;
    private Person charlie;

    @BeforeEach
    public void setup() {
        handler = new PeopleManager();
        alice = new Person("Alice", "Smith");
        bob = new Person("Bob", "Johnson");
        charlie = new Person("Charlie", "Chep");
        // Suppose IDs are set automatically and unique
    }

    @Test
    public void testBanPair() {
        // Ban Alice and Bob
        handler.banExchange(alice, bob);

        // Create an exchange between Alice and Bob
        Exchange e = new Exchange(alice, bob);

        handler.banExchange(e);

        // Should be banned
        assertTrue(PeopleManager.isBanned(e), "Exchange between Alice and Bob should be banned");

        // Exchange with Charlie should not be banned
        Exchange e2 = new Exchange(alice, charlie);
        assertFalse(PeopleManager.isBanned(e2), "Exchange between Alice and Charlie should NOT be banned");
    }
}