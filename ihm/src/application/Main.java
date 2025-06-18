package application ;

// Main example of usage
public class Main {
    public static void main(String[] args) {
        PeopleManager handler = new PeopleManager("StackOfStudents") ;

        // Disable checking to test faster
        PeopleManager.alwaysCheckCSVInputs = false ;

        handler.readCSV();
        handler.sortByCountry();

        handler.firstWillVisitSecond(2023, "germany", "iran");
        handler.firstWillVisitSecond(2024, "iran", "germany");
        handler.firstWillVisitSecond(2025, "germany", "iran");
        
        handler.createVisits(); // 2. Calculs avec historique figé

        System.out.println("historique : " + PeopleManager.history);

        for (CountryVisit c : handler.countryVisits) {
            c.getBestExchanges();
            handler.serializeExchanges();
        }
        handler.exportCSV();

        // --- Test graphique en mode terminal ---
        System.out.println("\n=== Synthèse des visites depuis l'historique ===");
        for (CountryVisit visit : handler.getCountryVisitsFromHistory()) {
            String label = visit.getYear() + ":" + visit.getGuestCountry() + "->" + visit.getHostCountry();
            int count = visit.getRawExchanges().size();
            System.out.printf("%-30s | %d échanges\n", label, count);
        }
    }
}