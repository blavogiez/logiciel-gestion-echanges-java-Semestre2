package current ;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;

/**
 * PeopleManager is the main class responsible for managing all persons, exchanges, and country associations
 * in the exchange program. It handles reading and writing data from/to CSV and serialized files, 
 * manages the mapping between people and their countries, and keeps track of exchange history.
 * 
 * This class acts as the main interface between the user and the program's core logic.
 * It is designed to be used by a main application class.
 * See it as a semi-abstract class, with the object not really mattering much as the Manager is thought to be unique.
 */
public class PeopleManager {
    // List of all people loaded from the CSV file (raw data)
    protected List<Person> rawPeople = new ArrayList<Person>();

    // Map of country name to list of people from that country
    protected Map<String, List<Person>> peopleByCountry = new HashMap<String, List<Person>>();

    // Map of guest country to host country (defines which country visits which)
    // This map is contained in a map with Integer keys (year)
    // Countries associations depend of a year
    // This way, a country visit can only happen once a year
    protected Map<Integer, Map<String, String>> countriesAssociations = new HashMap<>(); 

    // List of all country visits (each visit contains its own guest/host lists and exchanges)
    protected List<CountryVisit> countryVisits = new ArrayList<CountryVisit>();

    // Two followings lists are based on serialized files
    // Static list of all past exchanges (used as history for affinity calculations)
    public static List<Exchange> history = new ArrayList<Exchange>();

    // Static list of bans
    public static List<Exchange> bans = new ArrayList<Exchange>() ;
    
    // Some file names | file paths
    // Final paths that may not be modified

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

    // CSV Readable export
    final static String HUMANLY_READABLE_VISIT_HISTORY_FILE = "HumanlyReadableVisitHistory.csv";
    final static String HUMANLY_READABLE_VISIT_HISTORY_PATH = MY_RESULT_PATH + HUMANLY_READABLE_VISIT_HISTORY_FILE;

    // This part allows the user to choose whether a person that isn't valid 
    // Example : a person, by error, entering "TOTO" instead of its birth date doesn't deserve to be put away...
    
    // Option to deactivate the check (might be long in case of testings)

    public static boolean alwaysCheckCSVInputs = true ;

    // Scanner for Systemin
    public static final Scanner SCANNER_SYSTEM_IN = new Scanner(System.in);

    // The person can be valid following the criteria mentionned in a config (xml) file
    private static CriteriaConfigValidator configValidator = new CriteriaConfigValidator();


    // File name and path for the person database (modifiable)
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
     * @param fileName The base this.guestCountry = guestCountry.toLowerCase();
        this.hostCountry = hostCountry.toLowerCase();
        this.exchanges = new ArrayList<Exchange>();
    } name of the CSV file (without ".csv")
     */
    public PeopleManager(String fileName) {
        this.myPersonFile = fileName+".csv";
        this.myPeoplePath = MY_DATA_PATH + this.myPersonFile;

        // Updating history / bans right after creation
        this.updateLocalHistory();
        this.updateLocalBans();
    }

    // CSV Import
    // Reads people from the CSV file and populates the rawPeople list.
    // Handles criteria and hobbies, and skips invalid or incoherent persons.
    // Criteria validation is delegated to the Person class.
    // Automatic preprocessing based on XML configuration file.
    public void readCSV() {
        try (Scanner scanner = new Scanner(new File(this.myPeoplePath))) {
            if (!scanner.hasNextLine()) return; // Empty file
            String headerLine = scanner.nextLine();
            String[] headers = headerLine.split(",");
            
            System.out.println("=== START OF CSV TREATMENT USING AUTOMATIC VALIDATION ===\n");
            
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
                        /* Important mention :
                        The Criteria validation and check for its validity is in the Person class.
                        It is called when adding a new criteria.
                        If it is not valid, whether concerning the type or the value entered, the Criteria won't be added and marked as empty.
                        But for our logic, the person will still be added. The invalid criteria will just be ignored and converted to empty.
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

                    // === NOUVELLE ÉTAPE : VALIDATION AUTOMATIQUE XML ===
                    boolean passedAutoValidation = configValidator.shouldAcceptPerson(p);
                    
                    if (!passedAutoValidation) {
                        // La personne a été refusée par la validation automatique
                        continue; // Passer à la personne suivante
                    }

                    // === ÉTAPE EXISTANTE : Vérification des incohérences ===
                    // Checking incoherence in the Person criterias (check function doc)
                    // If there is one, ask the user if they want to add this person anyway
                    if (p.isThereIncoherence()) {
                        System.out.println("Warning: " + p.tinyToString() + " had an animal while being allergic to them...");

                        // If we chose to check manually, then check !
                        if (PeopleManager.alwaysCheckCSVInputs) {
                            System.out.print("Do you want to add this person anyway? (Yes/No): ");
                            String response = SCANNER_SYSTEM_IN.nextLine().trim().toLowerCase();
                            if (response.equals("yes") || response.equals("y")) {
                                rawPeople.add(p);
                                System.out.println("Person added despite incoherence.");
                            } else {
                                System.out.println("Person not added due to incoherence.");
                            }
                            System.out.println();
                        } else {
                            // Si pas de vérification manuelle, on peut soit rejeter automatiquement
                            // soit ajouter quand même selon votre logique métier
                            System.out.println("Warning ignored: " + p.tinyToString() + " added despite incoherence.");
                            rawPeople.add(p);
                        }
                    } else { 
                        rawPeople.add(p);
                        System.out.println("Person accepted : " + p.tinyToString());
                    }
                }
            }
            
            System.out.println("\n=== END OF CSV TREATMENT ===");
            System.out.println("Number of persons added: " + rawPeople.size());
            
        } catch (FileNotFoundException e) {
            System.out.println("File not found : " + e.getMessage());
        }
    }

    // CSV Export
    // This method exports the best exchanges for each country visit to a human-readable CSV file.
    // The CSV includes host/guest countries, dates, IDs, and affinity scores for each exchange.
    // It is useful for sharing results or analyzing them outside the program.
    // To get things done faster, we use the serialized history ; after all, the CSV is only the readable serialized history.
    public void exportCSV() {
        try (FileWriter fw = new FileWriter(HUMANLY_READABLE_VISIT_HISTORY_PATH)) {
            // Write the CSV header
            fw.write("YEAR,HOST_COUNTRY,GUEST_COUNTRY,HOST_ID,GUEST_ID,AFFINITY_SCORE\r\n");
            // Utilise l'historique sérialisé
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

    // SERIALIZATION || HISTORY MANAGING

    // We will save exchanges labelled as the best exchanges possible
    // As these best exchanges are done, they are part of history
    // Adding everything on one only stream to do things properly
    /**
     * Serializes all best exchanges from all country visits into a single file.
     * This allows for easy loading of exchange history in future runs.
     */
    public void serializeExchanges() {
        //List<Exchange> allExchanges = new ArrayList<>();
        List<Exchange> allExchanges = history ;
        for (CountryVisit visit : this.countryVisits) {
            for (Exchange e : visit.getExchanges()) {
                //System.out.println(e);

                // Won't add the same exchange twice
                if (PeopleManager.isInHistory(e)) continue ;

                // Else, add
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

    // Is the exchange in the history ?
    // Static as to be used in CountryVisit
    public static boolean isInHistory(Exchange e1) {
        for (Exchange e2 : PeopleManager.history) {
            if (e1.equals(e2)) return true ;
        }
        return false ;
    }

    // Associating history  
    /**
     * Loads the exchange history from the serialized file and updates the static history list.
     * This is useful for affinity calculations that depend on past exchanges.
     */
    public void updateLocalHistory() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(new File(PeopleManager.SERIALIZED_VISIT_HISTORY_PATH)))) {
            // Read the list of exchanges
            // History is now the list of exchanges read from the file
            PeopleManager.history = (List<Exchange>) ois.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // Adding an arbitrary exchange is actually the same as adding one to the history
    public void addArbitraryExchange(int year, Person p1, Person p2) {
        Exchange e = new Exchange(year, p1, p2);
        PeopleManager.history.add(e);
    }

    // Overloading
    public void addArbitraryExchange(Person p1, Person p2) {
        this.addArbitraryExchange(LocalDate.now().getYear(), p1, p2);
    }

    // Banning logics

    // Banning an exchange with two persons as parameters
    public void banExchange(Person p1, Person p2) {
        // Year doesn't matter, only the IDs of the persons (used in hasSamePersons)
        this.banExchange(new Exchange(p1, p2));
    }

    // Banning an exchange with an exchange as parameter
    public void banExchange(Exchange e) {
        // Year doesn't matter, only the IDs of the persons (used in hasSamePersons)
        System.out.println(e);
        PeopleManager.bans.add(e);
        this.serializeBans();
    }

    // Is the exchange banned ?
    // Static as to be used in CountryVisit
    public static boolean isBanned(Exchange e1) {
        for (Exchange e2 : PeopleManager.bans) {
            if (e1.hasSamePersons(e2)) return true ;
        }
        return false ;
    }

    // Same logic as history adapted to bans
    public void serializeBans() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(PeopleManager.SERIALIZED_BANNED_EXCHANGES_PATH))) {
            oos.writeObject(bans);
        } catch (Exception e) {
            System.out.println(e.getStackTrace());
        } 
        this.updateLocalBans();
    }
    // Updating local bans
    public void updateLocalBans() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(new File(PeopleManager.SERIALIZED_BANNED_EXCHANGES_PATH)))) {
            // Read the list of exchanges
            // Bans is now the list of exchanges read from the file
            PeopleManager.bans = (List<Exchange>) ois.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // SORTING

    // Sorting the people by their country using a map
    /**
     * Sorts all people into the peopleByCountry map based on their country of origin.
     * This allows for efficient grouping and lookup by country.
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

    // A country visiting another
    /**
     * Defines that the first country will visit the second country (guest -> host).
     * This sets up the association for later creation of country visits.
     */
    /**
     * Defines that the first country will visit the second country (guest -> host) for a given year.
     * This sets up the association for later creation of country visits.
     */
    public void firstWillVisitSecond(int year, String country1, String country2) {
        try {
            // Get or create the map for this year
            Map<String, String> assoc = this.countriesAssociations.getOrDefault(year, new HashMap<>());
            assoc.put(country1, country2);
            this.countriesAssociations.put(year, assoc);
        } catch (NullPointerException e) {
            System.out.println("One of the countries is associated to a null value : " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Error while associating the countries : " + e.getMessage());
        }   
    }

    // Overloading method without a year
    public void firstWillVisitSecond(String country1, String country2) {
        this.firstWillVisitSecond(LocalDate.now().getYear(), country1, country2) ; 
    }

    // Creating country visits based on the associations
    // All the Person from the CSV File will be added to the country visit following the guest | host nature of their country in the visit
    /**
     * Creates CountryVisit objects for each guest-host association for all years.
     * Populates each visit with the appropriate guests and hosts.
     * Iterates over the double map: year -> (guestCountry -> hostCountry).
     */
    public void createVisits() {
        // For each year in the associations
        for (Map.Entry<Integer, Map<String, String>> yearEntry : this.countriesAssociations.entrySet()) {
            int year = yearEntry.getKey();
            Map<String, String> assoc = yearEntry.getValue();
            // For each guest-host pair in that year
            for (Map.Entry<String, String> entry : assoc.entrySet()) {
                String guestCountry = entry.getKey();
                String hostCountry = entry.getValue();

                List<Person> guestPersons = this.peopleByCountry.get(guestCountry);
                List<Person> hostPersons = this.peopleByCountry.get(hostCountry);

                // Only create a visit if both guest and host persons exist
                if (guestPersons != null && hostPersons != null) {
                    CountryVisit visit = new CountryVisit(year, guestCountry, hostCountry);
                    // Optionally, set the year in CountryVisit if needed: visit.setYear(year);
                    visit.addGuests(guestPersons);
                    visit.addHosts(hostPersons);
                    this.countryVisits.add(visit);
                }
            }
        }
    }

    // GETTERS

    // Get a CountryVisit object matching the countries in argument
    /**
     * Returns the CountryVisit object matching the given guest and host countries.
     * Returns null if no such visit exists.
     */
    public CountryVisit getCountryVisit(String country1, String country2) {
        for (CountryVisit visit : this.countryVisits) {
            if (visit.getGuestCountry().equalsIgnoreCase(country1) && visit.getHostCountry().equalsIgnoreCase(country2)) {
                return visit;
            }
        }
        return null; // No visit found
    }

    // DISPLAY

    /**
     * Displays a list of people with a separator for readability.
     * Useful for debugging or presenting the loaded data.
     */
    public static void displayPeople(List<Person> people) {
        for (Person p : people) {
            System.out.println("-------------------------------------------------------------------------");
            System.out.println(p);
        }
    }
}