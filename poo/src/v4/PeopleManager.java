package v4 ;

import java.io.* ;
import java.time.* ;
import java.util.* ;

/**
 * PeopleManager is the main class responsible for managing all persons, exchanges, and country associations
 * in the exchange program. It handles reading and writing data from/to CSV and serialized files,
 * manages the mapping between people and their countries, and keeps track of exchange history.
 *
 * This class acts as the main interface between the user and the program's core logic.
 * It is designed to be used by a main application class.
 * Consider it as a semi-abstract class, with the instance not being of particular importance,
 * as the Manager is intended to be unique.
 */
public class PeopleManager {
    // List of all people loaded from the CSV file (raw data)
    protected List<Person> rawPeople = new ArrayList<Person>();

    // Map from country name to list of people from that country
    protected Map<String, List<Person>> peopleByCountry = new HashMap<String, List<Person>>();

    // Map of guest country to host country (defines which country visits which)
    // This map is contained within another map keyed by Integer (year)
    // Country associations depend on a year
    // This way, a country visit can only occur once per year
    protected Map<Integer, Map<String, String>> countriesAssociations = new HashMap<>(); 

    // List of all country visits (each visit contains its own guest/host lists and exchanges)
    protected List<CountryVisit> countryVisits = new ArrayList<CountryVisit>();

    // The following two lists are based on serialized files
    // Static list of all past exchanges (used as history for affinity calculations)

    /* RULES FOR HISTORY
    Someone cannot figure in more than one exchange in a year
    History won't contain exchanges having an affinity that finds itself over the cleaning threshold (check Exchange)
    History won't contain the same exchange twice
    For more informations as of how the history is dealt with, check the "HumanlyReadableVisitHistory" file.

    A valid history would be similar to the following:
    YEAR,HOST_COUNTRY,GUEST_COUNTRY,HOST_ID,GUEST_ID,AFFINITY_SCORE
    2023,iran,germany,29,90,9.35
    2023,iran,germany,93,61,16.00
    2023,iran,germany,95,83,10.00
    2024,egypt,germany,10,32,11.00
    2024,egypt,germany,39,83,11.00
    2024,egypt,germany,42,2,7.95
    2024,egypt,germany,84,88,10.00
    2024,egypt,germany,86,90,15.00

    Guest number 83 appears two times, but over two different years.

    An invalid history would be similar to the following:
    YEAR,HOST_COUNTRY,GUEST_COUNTRY,HOST_ID,GUEST_ID,AFFINITY_SCORE
    2023,iran,germany,29,90,9.35
    2023,iran,germany,93,61,16.00
    2023,iran,germany,95,83,10.00
    2024,egypt,germany,10,32,11.00
    2024,egypt,germany,39,83,11.00
    2024,egypt,germany,42,2,7.95
    2024,egypt,germany,84,88,10.00
    2024,egypt,germany,86,90,15.00
    2023,iran,germany,29,83,11.00
    2023,iran,germany,93,2,11.90
    2023,iran,germany,95,32,15.00

    Guest number 83 appears three times (twice in 2023)
    It is not with the same host, but it is not possible as there is only one exchange possible per year for a person.

    Following all these rules, the history has a limited size. It cannot be infinite (with a reasonable amount of persons).
    */
    public static List<Exchange> history = new ArrayList<Exchange>();

    // Static list of banned exchanges
    public static List<Exchange> bans = new ArrayList<Exchange>() ;
    
    // Some file names | file paths
    // Final paths that must not be modified

    // Base resource directory
    final static String MY_PATH = "res/";

    // Data and results subdirectories
    final static String MY_DATA_PATH = MY_PATH + "data/";
    final static String MY_RESULT_PATH = MY_PATH + "results/";

    // Filenames for serialized and human-readable exports
    final static String SERIALIZED_VISIT_HISTORY_FILE = "SerializedVisitHistory";
    final static String SERIALIZED_VISIT_HISTORY_PATH = MY_RESULT_PATH + SERIALIZED_VISIT_HISTORY_FILE;

    // Serialized banned exchanges
    final static String SERIALIZED_BANNED_EXCHANGES_FILE = "SerializedBannedExchanges";
    final static String SERIALIZED_BANNED_EXCHANGES_PATH = MY_RESULT_PATH + SERIALIZED_BANNED_EXCHANGES_FILE;

    // CSV readable export
    final static String HUMANLY_READABLE_VISIT_HISTORY_FILE = "HumanlyReadableVisitHistory.csv";
    final static String HUMANLY_READABLE_VISIT_HISTORY_PATH = MY_RESULT_PATH + HUMANLY_READABLE_VISIT_HISTORY_FILE;

    // This section allows the user to choose whether to enforce validation for invalid persons
    // Example: a person mistakenly entering "TOTO" instead of birth date shouldn’t necessarily be excluded…

    // Option to disable the validation check (yes | no) (may be slow during testing)
    public static boolean alwaysCheckCSVInputs = true ;

    // Scanner for System.in
    public static final Scanner SCANNER_SYSTEM_IN = new Scanner(System.in);

    // The validity of a person is determined according to criteria specified in a config (XML) file
    private static CriteriaConfigValidator configValidator = new CriteriaConfigValidator();


    // Filename and path for the person database (modifiable)
    String myPersonFile ; // Set in constructor
    String myPeoplePath ;

    // CONSTRUCTORS

    /**
     * Default constructor: uses the default file "StackOfStudents.csv" in the data directory.
     */
    public PeopleManager() {
        this("StackOfStudents");
    }

    /**
     * Constructor with a custom file name (without extension).
     * @param fileName The base name of the CSV file (without ".csv")
     */
    public PeopleManager(String fileName) {
        this.myPersonFile = fileName + ".csv";
        this.myPeoplePath = MY_DATA_PATH + this.myPersonFile;

        // Update history / bans immediately after creation
        this.updateLocalHistory();
        this.updateLocalBans();
    }

    // CSV Import
    // Reads people from the CSV file and populates the rawPeople list.
    // Handles criteria and hobbies, skips invalid or inconsistent persons.
    // Criteria validation is delegated to the Person class.
    // Automatic preprocessing based on XML config file.
    public void readCSV() {
        try (Scanner scanner = new Scanner(new File(this.myPeoplePath))) {
            if (!scanner.hasNextLine()) return; // Empty file
            String headerLine = scanner.nextLine();
            String[] headers = headerLine.split(",");

            System.out.println("=== START OF CSV PROCESSING WITH AUTO VALIDATION ===\n");

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] values = line.split(",", -1);
                if (values.length != headers.length) continue;

                String forename = "";
                String name = "";
                HashMap<Criteria, String> criteriaValues = new HashMap<>();
                ArrayList<String> hobbies = new ArrayList<>();

                for (int i = 0; i < headers.length; i++) {
                    String col = headers[i].trim().toUpperCase();
                    String val = values[i].trim();
                    if (val.isEmpty()) continue;

                    if (col.equals("FORENAME")) forename = val;
                    else if (col.equals("NAME")) name = val;
                    else if (col.equals("HOBBIES")) {
                        String[] hobbyArr = val.split(";");
                        for (String h : hobbyArr) {
                            if (!h.trim().isEmpty()) hobbies.add(h.trim());
                        }
                    } else {
                        /* Important note:
                           Criteria validation and type/value checking is done in Person class.
                           If invalid, the Criteria is ignored and considered empty.
                           But the Person is still added.
                        */
                        try {
                            Criteria crit = Criteria.valueOf(col);
                            criteriaValues.put(crit, val);
                        } catch (IllegalArgumentException e) {
                            // Ignore unknown columns
                        }
                    }
                }

                if (!forename.isEmpty() && !name.isEmpty()) {
                    // Creating the person
                    Person p = new Person(forename, name, criteriaValues, hobbies);
                    // === NEW STEP: AUTOMATIC XML VALIDATION ===
                    boolean passedAutoValidation = configValidator.shouldAcceptPerson(p);

                    if (!passedAutoValidation) {
                        // The person was rejected by automatic validation
                        continue; // Skip to next person
                    } else {
                        System.out.println("\n==============================\n");
                        System.out.println("Person: " + p.tinyToString() + " automatically added.") ;
                        rawPeople.add(p);
                    }
                }
            }

            System.out.println("\n=== END OF CSV PROCESSING ===");
            System.out.println("Number of persons added: " + rawPeople.size());

        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + e.getMessage());
        }
    }

    // CSV Export
    // This method exports the best exchanges for each country visit to a human-readable CSV file.
    // The CSV includes year, host/guest countries, IDs, and affinity scores.
    // It is useful for sharing results or analyzing them outside the program.
    // We use the serialized history to speed things up; the CSV is just a readable version.
    public void exportCSV() {
        try (FileWriter fw = new FileWriter(HUMANLY_READABLE_VISIT_HISTORY_PATH)) {
            // Write CSV header
            fw.write("YEAR,HOST_COUNTRY,GUEST_COUNTRY,HOST_ID,GUEST_ID,AFFINITY_SCORE\r\n");
            // Use serialized history
            for (Exchange e : PeopleManager.history) {
                int year = e.getYear();
                String hostCountry = e.getHost().getCriteriaValue(Criteria.COUNTRY_OF_ORIGIN);
                String guestCountry = e.getGuest().getCriteriaValue(Criteria.COUNTRY_OF_ORIGIN);
                int hostId = e.getHost().getID();
                int guestId = e.getGuest().getID();
                double score = e.getAffinityScore();
                fw.write(
                    year + "," +
                    hostCountry + "," +
                    guestCountry + "," +
                    hostId + "," +
                    guestId + "," +
                    String.format(Locale.US, "%.2f", score) + "\r\n"
                );
            }
        } 
        catch (FileNotFoundException e) {
            System.out.println("File not found: " + e.getMessage());
            e.printStackTrace();
        }
        catch (IOException e) {
            System.out.println("Access error: " + e.getMessage());
            e.printStackTrace();
        }
        catch (Exception e) {
            System.out.println("Writing error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // SERIALIZATION || HISTORY MANAGEMENT

    // We will save exchanges labeled as the best exchanges possible
    // Once these best exchanges are made, they are added to history
    // All in a single stream to ensure correct processing
    /**
     * Serializes all best exchanges from all country visits into one file.
     * This allows easy loading of exchange history in future runs.
     */
    public void serializeExchanges() {
        //List<Exchange> allExchanges = new ArrayList<>();
        List<Exchange> allExchanges = history ;
        for (CountryVisit visit : this.countryVisits) {
            for (Exchange e : visit.getExchanges()) {
                //System.out.println(e);

                // Do not add duplicates
                if (PeopleManager.isInHistory(e)) continue ;

                // Otherwise, add
                allExchanges.add(e);
            }
        }
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(PeopleManager.SERIALIZED_VISIT_HISTORY_PATH))) {
            //System.out.println(allExchanges);
            oos.writeObject(allExchanges);
        } catch (Exception e) {
            System.out.println(e.getStackTrace());
        } 
        this.updateLocalHistory();
    }

    public static void clearSerializedFiles() {
        try (FileOutputStream fos = new FileOutputStream(PeopleManager.SERIALIZED_VISIT_HISTORY_PATH)) {
            // Opening the file output stream in write mode overwrites the existing file
            // Since we don't write anything, the file gets cleared (emptied)
        } catch (Exception e) {
            e.printStackTrace(); // Print the stack trace if an exception occurs
        }
        try (FileOutputStream fos = new FileOutputStream(PeopleManager.SERIALIZED_BANNED_EXCHANGES_PATH)) {
            // Opening the file output stream in write mode overwrites the existing file
            // Since we don't write anything, the file gets cleared (emptied)
        } catch (Exception e) {
            e.printStackTrace(); // Print the stack trace if an exception occurs
        }
    }

    // Check if the exchange exists in history
    // Static so it can be used in CountryVisit
    public static boolean isInHistory(Exchange e1) {
        for (Exchange e2 : PeopleManager.history) {
            if (e1.equals(e2)) return true ;
        }
        return false ;
    }

    // Check if a person was matched in history this year
    // (history contains only exchanges, so only matched persons)
    // Static so it can be used in CountryVisit
    public static boolean isMatchedThisYearInHistory(Person p, int year) {
        for (Exchange e : PeopleManager.history) {
            if (e.getGuest().equals(p) || e.getHost().equals(p)) {
                if (e.getYear() == year) {
                    return true ;
                }
            }
        }
        return false ;
    }

    // Load serialized history
    /**
     * Loads exchange history from the serialized file and updates the static history list.
     * This is needed for affinity calculations relying on past exchanges.
     */
    public void updateLocalHistory() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(new File(PeopleManager.SERIALIZED_VISIT_HISTORY_PATH)))) {
            // Read the list of exchanges
            // History is now the list read from the file
            PeopleManager.history = (List<Exchange>) ois.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Adding a custom exchange is equivalent to adding one to history
    public void addArbitraryExchange(int year, Person p1, Person p2) {
        Exchange e = new Exchange(year, p1, p2);
        PeopleManager.history.add(e);
    }

    // Overloaded
    public void addArbitraryExchange(Person p1, Person p2) {
        this.addArbitraryExchange(LocalDate.now().getYear(), p1, p2);
    }

    // BANNING LOGIC

    // Ban an exchange involving two persons
    public void banExchange(Person p1, Person p2) {
        // Year doesn't matter, only person IDs matter (using hasSamePersons)
        this.banExchange(new Exchange(p1, p2));
    }

    // Ban an exchange via an Exchange object
    public void banExchange(Exchange e) {
        // Year irrelevant, only IDs matter (as per hasSamePersons)
        System.out.println(e);
        PeopleManager.bans.add(e);
        this.serializeBans();
    }

    // Check if an exchange is banned
    // Static so it can be used in CountryVisit
    public static boolean isBanned(Exchange e1) {
        for (Exchange e2 : PeopleManager.bans) {
            if (e1.hasSamePersons(e2)) return true ;
        }
        return false ;
    }

    // Serialize bans in the same way as history
    public void serializeBans() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(PeopleManager.SERIALIZED_BANNED_EXCHANGES_PATH))) {
            oos.writeObject(bans);
        } catch (Exception e) {
            System.out.println(e.getStackTrace());
        } 
        this.updateLocalBans();
    }
    // Load local bans
    public void updateLocalBans() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(new File(PeopleManager.SERIALIZED_BANNED_EXCHANGES_PATH)))) {
            // Read list of banned exchanges
            PeopleManager.bans = (List<Exchange>) ois.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // SORTING

    // Sort people by their country using a map
    /**
     * Sorts all people into peopleByCountry map based on their country of origin.
     * This allows efficient grouping and lookup by country.
     */
    public void sortByCountry() {
        for (Person p : this.rawPeople) {
            String country = p.getCriteriaValue(Criteria.COUNTRY_OF_ORIGIN);
            if (country == null || country.isEmpty()) continue; // Skip if no country

            List<Person> countryList = peopleByCountry.getOrDefault(country, new ArrayList<>());
            countryList.add(p);
            peopleByCountry.put(country, countryList);
        }
    }

    // Define that first country visits second
    /**
     * Defines that the first country will visit the second country (guest -> host).
     * Sets up the association for later creation of country visits.
     */
    /**
     * Defines that the first country will visit the second country (guest -> host) for a given year.
     * Sets up the association for later creation of country visits.
     */
    public void firstWillVisitSecond(int year, String country1, String country2) {
        try {
            // Get or create the map for this year
            Map<String, String> assoc = this.countriesAssociations.getOrDefault(year, new HashMap<>());
            assoc.put(country1, country2);
            this.countriesAssociations.put(year, assoc);
        } catch (NullPointerException e) {
            System.out.println("One of the countries is associated with a null value: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Error while associating the countries: " + e.getMessage());
        }   
    }

    // Overloaded without year
    public void firstWillVisitSecond(String country1, String country2) {
        this.firstWillVisitSecond(LocalDate.now().getYear(), country1, country2) ; 
    }

    // Create country visits based on the associations
    // All persons from the CSV file are added to the country visit following guest | host roles
    /**
     * Creates CountryVisit objects for each guest-host association for all years.
     * Populates each visit with the relevant guests and hosts.
     * Iterates through the nested map: year -> (guestCountry -> hostCountry).
     */
    public void createVisits() {
        // For each year in associations
        for (Map.Entry<Integer, Map<String, String>> yearEntry : this.countriesAssociations.entrySet()) {
            int year = yearEntry.getKey();
            Map<String, String> assoc = yearEntry.getValue();
            // For each guest-host pair in that year
            for (Map.Entry<String, String> entry : assoc.entrySet()) {
                String guestCountry = entry.getKey();
                String hostCountry = entry.getValue();

                List<Person> guestPersons = this.peopleByCountry.get(guestCountry);
                List<Person> hostPersons = this.peopleByCountry.get(hostCountry);

                // Only create a visit if both guest and host lists exist
                if (guestPersons != null && hostPersons != null) {
                    CountryVisit visit = new CountryVisit(year, guestCountry, hostCountry);
                    // Optionally set year in CountryVisit if needed: visit.setYear(year);
                    visit.addGuests(guestPersons);
                    visit.addHosts(hostPersons);
                    this.countryVisits.add(visit);
                }
            }
        }
    }

    // GETTERS

    // Get a CountryVisit object matching the specified countries
    /**
     * Returns the CountryVisit object matching the given guest and host countries.
     * Returns null if no such visit exists.
     */
    public CountryVisit getCountryVisit(String country1, String country2) {
        for (CountryVisit visit : this.countryVisits) {
            if (visit.getGuestCountry().equalsIgnoreCase(country1)
             && visit.getHostCountry().equalsIgnoreCase(country2)) {
                return visit;
            }
        }
        return null; // No visit found
    }

    // DISPLAY

    /**
     * Displays a list of people with separators for readability.
     * Useful for debugging or presenting the loaded data.
     */
    public static void displayPeople(List<Person> people) {
        for (Person p : people) {
            System.out.println("-------------------------------------------------------------------------");
            System.out.println(p);
        }
    }
}
