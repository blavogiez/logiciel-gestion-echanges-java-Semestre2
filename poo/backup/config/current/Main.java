package current ;

// Main example of usage
public class Main {
    public static void main(String[] args) {
        PeopleManager handler = new PeopleManager("CriteriaConfigTest") ;

        //PeopleManager handler = new PeopleManager("StackOfStudents") ;

        // Disable checking to test faster
        PeopleManager.alwaysCheckCSVInputs = false ;

        handler.readCSV();
        handler.sortByCountry();

        handler.firstWillVisitSecond(2023, "germany", "egypt");
        handler.firstWillVisitSecond(2023, "germany", "egypt");

        handler.firstWillVisitSecond(2023, "germany", "iran");

        handler.firstWillVisitSecond(2024, "germany", "egypt");

        handler.firstWillVisitSecond(2025, "germany", "iran");
        
        handler.createVisits(); // 2. Calculs avec historique figé

        System.out.println("historique : " + PeopleManager.history);

        for (CountryVisit c : handler.countryVisits) {
            c.getBestExchanges();
            handler.serializeExchanges();
            //System.out.println("historique : " + PeopleManager.history);
        }
        
        // handler.serializeExchanges(); // 3. Mise à jour de l'historique APRÈS tous les calculs
        handler.exportCSV();
    }
}