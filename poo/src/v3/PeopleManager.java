package v3;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * PeopleManager is the main class responsible for managing all persons, exchanges, and country associations
 * in the exchange program. It handles reading and writing data from/to CSV and serialized files, 
 * manages the mapping between people and their countries, and keeps track of exchange history.
 * 
 * This class acts as the main interface between the user and the program's core logic.
 * It is designed to be used by a main application class.
 */
public class PeopleManager {
    // List of all people loaded from the CSV file (raw data)
    protected List<Person> rawPeople = new ArrayList<Person>();

    // Map of country name to list of people from that country
    protected Map<String, List<Person>> peopleByCountry = new HashMap<String, List<Person>>();

    // Map of guest country to host country (defines which country visits which)
    protected Map<String, String> countriesAssociations = new HashMap<String, String>(); 

    // List of all country visits (each visit contains its own guest/host lists and exchanges)
    protected List<CountryVisit> countryVisits = new ArrayList<CountryVisit>();

    // Static list of all past exchanges (used as history for affinity calculations)
    public static List<Exchange> history = new ArrayList<Exchange>();
    
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

    final static String HUMANLY_READABLE_VISIT_HISTORY_FILE = "HumanlyReadableVisitHistory.csv";
    final static String HUMANLY_READABLE_VISIT_HISTORY_PATH = MY_RESULT_PATH + HUMANLY_READABLE_VISIT_HISTORY_FILE;

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
     * @param fileName The base name of the CSV file (without ".csv")
     */
    public PeopleManager(String fileName) {
        this.myPersonFile = fileName+".csv";
        this.myPeoplePath = MY_DATA_PATH + this.myPersonFile;
    }

    // CSV Import
    // Reads people from the CSV file and populates the rawPeople list.
    // Handles criteria and hobbies, and skips invalid or incoherent persons.
    // Criteria validation is delegated to the Person class.
    public void readCSV() {
        try (Scanner scanner = new Scanner(new File(this.myPeoplePath))) {
            if (!scanner.hasNextLine()) return; // Empty file
            String headerLine = scanner.nextLine();
            String[] headers = headerLine.split(",");
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

                    // Checking incoherence in the Person criterias (check function doc)
                    // If there is one, we won't add this person to the practical dataset
                    if (p.isThereIncoherence()) {
                        System.out.println("Sorry, but " + p.tinyToString() + " had an animal while being allergic to them... We can't trust this person.");
                    }
                    else { 
                        rawPeople.add(p);
                    }
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("File not found : " + e.getMessage());
        }
    }

    // CSV Export
    // This method exports the best exchanges for each country visit to a human-readable CSV file.
    // The CSV includes host/guest countries, dates, IDs, and affinity scores for each exchange.
    // It is useful for sharing results or analyzing them outside the program.
    public void exportCSV() {
        try (FileWriter fw = new FileWriter(HUMANLY_READABLE_VISIT_HISTORY_PATH)) {
            // Write the CSV header
            fw.write("HOST_COUNTRY,GUEST_COUNTRY,START_DATE,END_DATE,HOST_ID,GUEST_ID,AFFINITY_SCORE\r\n");
            // Iterate over all country visits
            for (CountryVisit visit : this.countryVisits) {
                String hostCountry = visit.getHostCountry();
                String guestCountry = visit.getGuestCountry();
                String startDate = "";
                if (visit.getStart() != null) startDate = visit.getStart().toString(); // Format start date if present
                String endDate = "";
                if (visit.getEnd() != null) endDate = visit.getEnd().toString(); // Format end date if present
                // For each best exchange in the visit, write a line in the CSV
                for (Exchange e : visit.getBestExchanges()) {
                    int hostId = e.getHost().getID();
                    int guestId = e.getGuest().getID();
                    double score = e.evaluateAffinity(); // Calculate the affinity score for this exchange
                    fw.write(hostCountry + "," + guestCountry + "," + startDate + "," + endDate + "," +
                            hostId + "," + guestId + "," + String.format("%.2f", score) + "\r\n");
                }
            }
        } 
        // Handle file not found error (should not happen if path is correct)
        catch (FileNotFoundException e) {
            System.out.println("File not found: " + e.getMessage());
            e.printStackTrace();
        }
        // Handle IO errors (disk full, permission denied, etc.)
        catch (IOException e) {
            System.out.println("Access error: " + e.getMessage());
            e.printStackTrace();
        }
        // Handle any other unexpected exception
        catch (Exception e) {
            System.out.println("Writing error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // SERIALIZATION 

    // We will save exchanges labelled as the best exchanges possible
    // As these best exchanges are done, they are part of history
    // Adding everything on one only stream to do things properly
    /**
     * Serializes all best exchanges from all country visits into a single file.
     * This allows for easy loading of exchange history in future runs.
     */
    public void serializeExchanges() {
        List<Exchange> allExchanges = new ArrayList<>();
        for (CountryVisit visit : this.countryVisits) {
            for (Exchange e : visit.getBestExchanges()) {
                System.out.println(e);
                allExchanges.add(e);
            }
        }
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(PeopleManager.SERIALIZED_VISIT_HISTORY_PATH))) {
            oos.writeObject(allExchanges);
        } catch (Exception e) {
            System.out.println(e.getStackTrace());
        }
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
    public void firstWillVisitSecond(String country1, String country2) {
        try {
            this.countriesAssociations.put(country1, country2);
        } catch (NullPointerException e) {
            System.out.println("One of the countries is associated to a null value : " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Error while associating the countries : " + e.getMessage());
        }   
    }

    // Creating country visits based on the associations
    // All the Person from the CSV File will be added to the country visit following the guest | host nature of their country in the visit
    /**
     * Creates CountryVisit objects for each guest-host association.
     * Populates each visit with the appropriate guests and hosts.
     */
    public void createVisits() {
        for (Map.Entry<String, String> entry : this.countriesAssociations.entrySet()) {
            String guestCountry = entry.getKey();
            String hostCountry = entry.getValue();

            List<Person> guestPersons = this.peopleByCountry.get(guestCountry);
            List<Person> hostPersons = this.peopleByCountry.get(hostCountry);

            if (guestPersons != null && hostPersons != null) {
                CountryVisit visit = new CountryVisit(guestCountry, hostCountry);
                visit.addGuests(guestPersons);
                visit.addHosts(hostPersons);
                this.countryVisits.add(visit);
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