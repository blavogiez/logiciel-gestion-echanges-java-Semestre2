package v3;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CountryVisit {
    private String guestCountry;
    private String hostCountry;

    private List<Person> guestPersons = new ArrayList<>();
    private List<Person> hostPersons = new ArrayList<>();

    // Start and end dates of the exchange
    private LocalDate start ;
    private LocalDate end ;
    
    private List<Exchange> bestExchanges = new ArrayList<Exchange>();

    // Affinity score cleaning threshold
    private final static double AFFINITY_CLEANING_THRESHOLD = 200.0 ;

    // First argument is the guest country, second is the host country
    public CountryVisit(String guestCountry, String hostCountry) {
        // Default duration is 7 days from today
        this(guestCountry, hostCountry, LocalDate.now(), LocalDate.now().plusDays(7));
    }

    public CountryVisit(String guestCountry, String hostCountry, LocalDate start, LocalDate end) {
        this.guestCountry = guestCountry.toLowerCase();
        this.hostCountry = hostCountry.toLowerCase();
        this.start = start;
        this.end = end;
    }

    // GETTERS

    public String getGuestCountry() {
        return this.guestCountry;
    }

    public String getHostCountry() {
        return this.hostCountry;
    }

    public List<Person> getGuestPersons() {
        return this.guestPersons;
    }

    public List<Person> getHostPersons() {
        return this.hostPersons;
    }

    public LocalDate getStart() {
        return this.start;
    }

    public LocalDate getEnd() {
        return this.end;
    }

    // Re-searching the best exchanges every time as the persons inside may have changed elsewhere
    public List<Exchange> getBestExchanges() {
        // Everyone is free again as we're searching another time
        this.unmarkAsUsed();
        int n = Math.min(this.hostPersons.size(), guestPersons.size());
        if (n > 10) {
            System.out.println("Warning: Bruteforce method is not recommended for more than 10 hosts/guests due to performance issues.");
            System.out.println("Therefore, the greedy method will be used instead. It does not guarantee the best matching, but is much faster.");
            return this.getBestExchangesGreedy(); // Fallback to greedy method for large groups
        } else {
            return this.getBestExchangesBruteforce();
        }
    }
    
    // Old, not used
    public List<Exchange> oldGetBestExchanges() {
        // Have the best exchanges been found yet ?
        if (this.bestExchanges.isEmpty() || this.bestExchanges==null) {
            // Everyone is free again as we're searching another time
            this.unmarkAsUsed();
            int n = Math.min(this.hostPersons.size(), guestPersons.size());
            if (n > 10) {
                System.out.println("Warning: Bruteforce method is not recommended for more than 10 hosts/guests due to performance issues.");
                System.out.println("Therefore, the greedy method will be used instead. It does not guarantee the best matching, but is much faster.");
                return this.getBestExchangesGreedy(); // Fallback to greedy method for large groups
            } else {
                return this.getBestExchangesBruteforce();
            }
        }
        // There is already one
        return this.bestExchanges;
    }

    // "SETTERS"

    public void addArbitraryExchange(Person p1, Person p2) {
        Exchange e = new Exchange(p1, p2);
        e.evaluateAffinity();
        this.bestExchanges.add(e);
    }
    
    // Adding guests
    public void addGuests(List<Person> guests) {
        for (Person p : guests) {
            if (p.getCriteriaValue(Criteria.COUNTRY_OF_ORIGIN).equalsIgnoreCase(this.guestCountry)) {
                this.guestPersons.add(p);
            }
        }
    }

    // Adding hosts
    public void addHosts(List<Person> hosts) {
        for (Person p : hosts) {
            if (p.getCriteriaValue(Criteria.COUNTRY_OF_ORIGIN).equalsIgnoreCase(this.hostCountry)) {
                this.hostPersons.add(p);
            }
        }
    }

    /*
    On génère toutes les permutations possibles des guests.
    Pour chaque permutation, on associe le i-ème host au i-ème guest et on calcule la somme des affinités.
    On garde la combinaison qui donne la somme minimale.
    Cette méthode garantit le meilleur appariement possible, mais n’est utilisable que pour de petits effectifs.
    */
    public List<Exchange> getBestExchangesBruteforce() {
        // Create local copies of hosts and guests
        List<Person> hosts = new ArrayList<>(hostPersons);
        List<Person> guests = new ArrayList<>(guestPersons);

        // n is the number of pairs to make (minimum of hosts and guests)
        int n = Math.min(hosts.size(), guests.size());
        List<Exchange> best = null;
        double minSum = Double.MAX_VALUE;

        // We always want to permute the largest list and choose sublists of size n
        List<List<Person>> hostCombinations = new ArrayList<>();
        List<List<Person>> guestCombinations = new ArrayList<>();

        // If hosts are more numerous, generate all combinations of hosts of size n
        // If guests are more numerous, generate all combinations of guests of size n
        // If equal, just use the full lists
        if (hosts.size() > guests.size()) {
            combinations(hosts, n, 0, new ArrayList<>(), hostCombinations);
            guestCombinations.add(guests); // only one possible combination
        } else if (guests.size() > hosts.size()) {
            combinations(guests, n, 0, new ArrayList<>(), guestCombinations);
            hostCombinations.add(hosts); // only one possible combination
        } else {
            hostCombinations.add(hosts);
            guestCombinations.add(guests);
        }

        // For each combination of hosts and guests
        for (List<Person> hostCombo : hostCombinations) {
            for (List<Person> guestCombo : guestCombinations) {
                // Generate all permutations of the guest combination
                List<List<Person>> permutations = new ArrayList<>();
                permute(guestCombo, 0, permutations);
                for (List<Person> permutedGuests : permutations) {

                    double sum = 0;
                    List<Exchange> current = new ArrayList<>();
                    // Pair each host with the corresponding guest in the permutation
                    for (int i = 0; i < n; i++) {
                        // Are they both available and can be matched?
                        if (hostCombo.get(i).canBeMatched() && permutedGuests.get(i).canBeMatched()) {
                            Exchange e = new Exchange(hostCombo.get(i), permutedGuests.get(i));
                            sum += e.evaluateAffinity();
                            current.add(e);
                        } else {
                            // If one of the persons cannot be matched, skip this pairing
                            sum = Double.MAX_VALUE;
                            break;
                        }
                    }
                    // If this pairing has a lower total affinity than the current one considered the best, keep it as the best
                    if (sum < minSum) {
                        minSum = sum;
                        best = new ArrayList<Exchange>();
                        // Recreate the best exchanges list from the last current pairing
                        for (Exchange ex : current) {
                            best.add(new Exchange(ex.getHost(), ex.getGuest()));
                        }
                    }
                }
            }
        }
        // System.out.println("Bruteforce Total Affinity: " + minSum);
        this.bestExchanges = best;

        if (this.bestExchanges == null) {
            this.bestExchanges = new ArrayList<Exchange>();
        }

        // Cleaning too high affinity scores
        this.cleanInvalidExchanges();

        // Remaining exchanges persons are marked as matched and are not available for any other exchange yet
        this.markAsUsed();
        return this.bestExchanges;
    }

    // Generates all combinations of size n from a list
    // This function is needed because there might be a different number of hosts and guests
    // Even in the case of equal persons in the CSV File, as some may not be available afterwards, the equality is not guaranteed
    private void combinations(List<Person> arr, int n, int start, List<Person> temp, List<List<Person>> result) {
        if (temp.size() == n) {
            result.add(new ArrayList<>(temp));
            return;
        }
        for (int i = start; i < arr.size(); i++) {
            temp.add(arr.get(i));
            combinations(arr, n, i + 1, temp, result);
            temp.remove(temp.size() - 1);
        }
    }

    // Generates all permutations of a list (classic algorithm)
    private void permute(List<Person> arr, int k, List<List<Person>> result) {
        if (k == arr.size()) {
            result.add(new ArrayList<>(arr));
        } else {
            for (int i = k; i < arr.size(); i++) {
                Collections.swap(arr, i, k);
                permute(arr, k + 1, result);
                Collections.swap(arr, k, i);
            }
        }
    }

    // Some scores may have been selected but they are way too high.
    // It is better to remove them as their affinity is horrible. They will never get along with each other
    // Better not get them together in any world
    public void cleanInvalidExchanges() {
        if (this.bestExchanges == null) return;
        List<Exchange> exchangesToRemove = new ArrayList<>();
        for (Exchange e : this.bestExchanges) {
            if (e.evaluateAffinity() > CountryVisit.AFFINITY_CLEANING_THRESHOLD) {
                exchangesToRemove.add(e);
            }
        }
        this.bestExchanges.removeAll(exchangesToRemove);
    }

    // Marks the people in the best exchange as used
    // They won't be available for any other exchange.
    public void markAsUsed() {
        try {
            for (Exchange e : this.bestExchanges) {
                e.getHost().setMatched(true);
                e.getGuest().setMatched(true);
                e.setAffected(true);
            }
        } catch (NullPointerException e) {
            System.out.println("Error while setting matched persons: " + e.getMessage());
        }
    }

    // Unmarks people at every exchange
    public void unmarkAsUsed() {
        try {
            for (Exchange e : this.bestExchanges) {
                e.getHost().setMatched(false);
                e.getGuest().setMatched(false);
                e.setAffected(false);
            }
        } catch (NullPointerException e) {
            System.out.println("Error while setting matched persons: " + e.getMessage());
        }
    }

    // Greedy method: fast, but does not guarantee the best matching
    // Use for large groups
    public List<Exchange> getBestExchangesGreedy() {
        List<Exchange> result = new ArrayList<>();
        Set<Person> usedHosts = new HashSet<>();
        Set<Person> usedGuests = new HashSet<>();

        // While there are unused hosts and guests
        while (usedHosts.size() < this.hostPersons.size() && usedGuests.size() < this.guestPersons.size()) {
            Exchange best = null;
            double minAffinity = Double.MAX_VALUE;
            Person bestHost = null;
            Person bestGuest = null;

            // Find the available host-guest pair with the lowest affinity
            for (Person host : hostPersons) {
                if (usedHosts.contains(host)) continue;
                for (Person guest : guestPersons) {
                    if (usedGuests.contains(guest)) continue;
                    if (!host.canBeMatched() || !guest.canBeMatched()) continue; // Skip if either cannot be matched
                    Exchange e = new Exchange(host, guest);
                    double affinity = e.evaluateAffinity();
                    if (affinity < minAffinity) {
                        minAffinity = affinity;
                        best = e;
                        bestHost = host;
                        bestGuest = guest;
                    }
                }
            }
            // Add the best pair found and mark them as used
            if (best != null) {
                result.add(best);
                usedHosts.add(bestHost);
                usedGuests.add(bestGuest);
            } else {
                break; // no more possible pairs
            }
        }
        // double totalAffinity = result.stream().mapToDouble(Exchange::evaluateAffinity).sum();
        // System.out.println("Greedy Total Affinity: " + totalAffinity);
        this.bestExchanges = result;
        
        // Same end logic as bruteforce
        if (this.bestExchanges == null) {
            this.bestExchanges = new ArrayList<>();
        }

        this.cleanInvalidExchanges();

        this.markAsUsed();
        return this.bestExchanges;
    }

    // DISPLAY 
    public String toString() {
        this.getBestExchanges(); // Ensure best exchanges are calculated before displaying
        if (this.bestExchanges == null) {
            this.bestExchanges = new ArrayList<>();
        }
        String res = "CountryVisit from " + this.guestCountry + " to " + this.hostCountry + "\n";
        res+= "Start date: " + this.start + ", End date: " + this.end + "\n";
        res+= "The best exchanges are:\n";
        if (this.bestExchanges == null || this.bestExchanges.isEmpty()) {
            res += "No exchanges found (yet?). Likely because of no people left available, or the only exchanges had horrible affinity. \n";
        } else {
            for (Exchange e : this.bestExchanges) {
                res += e.toTinyString() + ": " + e.getAffinityScore() + "\n";
            }
        }
        return res ;
    }
}
