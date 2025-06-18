package current ;

// Main example of usage
public class Main {
    public static void main(String[] args) {
        /* Resolve this error of history containing exchanges from another package (v3, v4, current...)
         * Exception in thread "main" java.lang.ClassCastException: class v4.Exchange cannot be cast to class current.Exchange (v4.Exchange and current.Exchange are in unnamed module of loader 'app')
        at current.Exchange.historyHandler(Exchange.java:349)
        at current.Exchange.evaluateAffinity(Exchange.java:148)
        at current.Exchange.<init>(Exchange.java:52)
        at current.CountryVisit.getBestExchangesBruteforce(CountryVisit.java:172)
        at current.CountryVisit.getBestExchanges(CountryVisit.java:81)
        at current.Main.main(Main.java:34)
         *
         * 
         * by just clearing history and bans files/ */

        PeopleManager.clearSerializedFiles() ;
        PeopleManager handler = new PeopleManager("HalfStack") ;

        // Disable checking to test faster
        PeopleManager.alwaysCheckCSVInputs = false ;

        handler.readCSV();
        handler.sortByCountry();

        handler.firstWillVisitSecond(2023, "germany", "egypt");
        handler.firstWillVisitSecond(2023, "germany", "egypt");

        handler.firstWillVisitSecond(2023, "germany", "iran");

        handler.firstWillVisitSecond(2024, "germany", "egypt");

        handler.firstWillVisitSecond(2025, "germany", "iran");
        
        handler.createVisits();

        int previousSize = -1 ;
        int currentSize = PeopleManager.history.size() ;
        while (currentSize != previousSize) {
            previousSize = currentSize ;
            for (CountryVisit c : handler.countryVisits) {
                c.getBestExchanges();
                handler.serializeExchanges();
            }
            currentSize = PeopleManager.history.size() ;
        }

        System.out.println("historique : " + PeopleManager.history);
        
        handler.exportCSV();
    }
}