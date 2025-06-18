package v3;

// Main example of usage
public class Main {
    public static void main(String[] args) {
        // Classe anonyme concrète pour instancier PersonHandler
        PeopleManager handler = new PeopleManager("HalfStack") {};
        handler.readCSV();
        //PeopleManager.displayPeople(handler.rawPeople);

        handler.sortByCountry();
        //System.out.println(handler.peopleByCountry);

        handler.firstWillVisitSecond("poland", "italy");
        handler.firstWillVisitSecond("germany", "italy");
        handler.firstWillVisitSecond("morocco", "italy");
        handler.createVisits();
        for (CountryVisit visit : handler.countryVisits) {
            System.out.println(visit); 
        }
        handler.serializeExchanges();
        handler.exportCSV();

        // Mettre le noticing ?
        handler.updateLocalHistory();
        System.out.println(PeopleManager.history);
    }
}