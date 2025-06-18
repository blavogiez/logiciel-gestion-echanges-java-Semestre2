package application;

import java.util.ArrayList;
import java.util.List;

import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;

/**
 * Contrôleur principal pour la gestion des échanges internationaux.
 * Gère l'affichage des listes d'échanges, l'historique, les échanges en cours 
 * et la configuration des critères de matching.
 */
public class ListController {
    
    // ========================================
    // ÉLÉMENTS DE L'INTERFACE UTILISATEUR
    // ========================================
    
    // Listes d'affichage des échanges
    @FXML private ListView<CountryVisit> listeHistorique;
    @FXML private ListView<CountryVisit> listeAccueilEnCours;
    @FXML private ListView<CountryVisit> listeEchangesEnCours;
    @FXML private ListView<Exchange> listePairesEchanges;
    
    // Labels pour la pagination
    @FXML private Label numeroPageHistorique;
    @FXML private Label numeroPageEchanges;
    @FXML private Label numeroPageEnCours;
    
    // Panneaux de navigation principaux
    @FXML private Pane panneauAccueil;
    @FXML private VBox panneauEchanges;
    @FXML private VBox panneauHistorique;
    @FXML private VBox panneauEnCours;
    @FXML private Pane panneauModificationCriteres;
    
    // Labels informatifs
    @FXML private Label nomEchangeActuel;
    @FXML private Label labelMeilleuresAffectations = new Label("Meilleures affectations");
    @FXML private Label labelTousLesEchanges = new Label("Tous les échanges possibles");
    @FXML private Label labelConfirmationAction;
    
    // Boutons de navigation
    @FXML private Button boutonModifierCriteres;
    @FXML private Button boutonVoirToutesPaires;
    @FXML private Button boutonRetourEchanges;
    
    // ========================================
    // CRITÈRES DE MATCHING - SLIDERS ET VALEURS
    // ========================================
    
    // Sliders pour ajuster l'importance des critères
    @FXML private Slider sliderCritereAllergies;      // Critère 1: Allergies animaux
    @FXML private Slider sliderCritereAlimentation;   // Critère 2: Contraintes alimentaires
    @FXML private Slider sliderCritereGenre;          // Critère 3: Compatibilité de genre
    @FXML private Slider sliderCritereHistorique;     // Critère 4: Historique des échanges
    @FXML private Slider sliderCritereAge;            // Critère 5: Différence d'âge
    
    // Labels affichant les valeurs actuelles des critères
    @FXML private Label valeurCritereAllergies;
    @FXML private Label valeurCritereAlimentation;
    @FXML private Label valeurCritereGenre;
    @FXML private Label valeurCritereHistorique;
    @FXML private Label valeurCritereAge;
    
    // ========================================
    // DONNÉES ET ÉTAT DE L'APPLICATION
    // ========================================
    
    private PeopleManager gestionnaireDonnees;
    private ArrayList<CountryVisit> listeCompleteEchanges = new ArrayList<>();
    private ArrayList<CountryVisit> listeEchangesActuels = new ArrayList<>();
    private List<Exchange> listeExchangesPossibles = new ArrayList<>();
    
    // Gestion de l'expansion des cellules dans les listes
    private ExpandingCell celluleActuellementEtendue = null;
    private CountryVisit echangeSelectionne; // Échange actuellement consulté
    
    // ========================================
    // INITIALISATION
    // ========================================
    
    /**
     * Initialise le contrôleur et configure l'interface utilisateur.
     * Cette méthode est appelée automatiquement après le chargement du FXML.
     */
    public void initialize() {
        // Initialisation du gestionnaire de données
        gestionnaireDonnees = new PeopleManager("HalfStack");
        PeopleManager.alwaysCheckCSVInputs = false;
        gestionnaireDonnees.readCSV();
        gestionnaireDonnees.sortByCountry();
        
        // Création d'exemples d'échanges pour les tests
        gestionnaireDonnees.firstWillVisitSecond(2024, "poland", "spain");
        gestionnaireDonnees.firstWillVisitSecond(2022, "germany", "japan");
        gestionnaireDonnees.createVisits();
        gestionnaireDonnees.serializeExchanges();
        
        // Configuration des données et interface
        initialiserDonneesEchanges();
        configurerSlidersCriteres();
        configurerCellulesListes();
        configurerVisibilitePanneaux();
        afficherEchangesAccueil();
    }
    
    /**
     * Configure les sliders des critères et lie leurs valeurs aux labels d'affichage.
     */
    private void configurerSlidersCriteres() {
        // Liaison des valeurs des sliders aux labels (conversion par 0.1 pour obtenir des décimales)
        valeurCritereAllergies.textProperty().bind(
            sliderCritereAllergies.valueProperty().multiply(0.1).asString("%.1f"));
        valeurCritereAlimentation.textProperty().bind(
            sliderCritereAlimentation.valueProperty().multiply(0.1).asString("%.1f"));
        valeurCritereGenre.textProperty().bind(
            sliderCritereGenre.valueProperty().multiply(0.1).asString("%.1f"));
        valeurCritereHistorique.textProperty().bind(
            sliderCritereHistorique.valueProperty().multiply(0.1).asString("%.1f"));
        valeurCritereAge.textProperty().bind(
            sliderCritereAge.valueProperty().multiply(0.1).asString("%.1f"));
    }
    
    /**
     * Configure les factory de cellules pour les différentes listes.
     */
    private void configurerCellulesListes() {
        listeAccueilEnCours.setCellFactory(list -> new ExpandingCellForCv(this, false));
        listeHistorique.setCellFactory(list -> new ExpandingCellForCv(this, true));
        listePairesEchanges.setCellFactory(list -> new ExpandingCellForEx(this, false));
        listeEchangesEnCours.setCellFactory(list -> new ExpandingCellForCv(this, false));
    }
    
    /**
     * Configure la visibilité initiale des panneaux.
     */
    private void configurerVisibilitePanneaux() {
        panneauAccueil.setVisible(true);
        panneauEchanges.setVisible(false);
        panneauHistorique.setVisible(false);
        panneauEnCours.setVisible(false);
        panneauModificationCriteres.setVisible(false);
        boutonRetourEchanges.setVisible(false);
    }
    
    /**
     * Initialise les listes de données à partir du gestionnaire.
     */
    private void initialiserDonneesEchanges() {
        listeEchangesActuels.addAll(gestionnaireDonnees.getCountryVisits());
        listeCompleteEchanges.addAll(gestionnaireDonnees.getCountryVisits());
    }
    
    /**
     * Affiche les premiers échanges sur la page d'accueil.
     */
    private void afficherEchangesAccueil() {
        int nombreElements = Math.min(listeEchangesActuels.size(), 3);
        if (nombreElements > 0) {
            listeAccueilEnCours.getItems().setAll(
                listeEchangesActuels.subList(0, nombreElements));
        }
    }
    
    // ========================================
    // NAVIGATION ENTRE LES PANNEAUX
    // ========================================
    
    /**
     * Navigue vers la page d'accueil.
     */
    @FXML
    public void naviguerVersAccueil(ActionEvent event) {
        masquerTousPanneaux();
        panneauAccueil.setVisible(true);
        fermerCelluleEtendue();
        afficherEchangesAccueil();
    }
    
    /**
     * Navigue vers la page historique des échanges.
     */
    @FXML
    public void naviguerVersHistorique(ActionEvent event) {
        numeroPageHistorique.setText("0");
        changerPageHistorique(1);
        masquerTousPanneaux();
        panneauHistorique.setVisible(true);
        fermerCelluleEtendue();
    }
    
    /**
     * Navigue vers la page des échanges en cours.
     */
    @FXML
    public void naviguerVersEnCours(ActionEvent event) {
        numeroPageEnCours.setText("0");
        changerPageEnCours(1);
        masquerTousPanneaux();
        panneauEnCours.setVisible(true);
        fermerCelluleEtendue();
    }
    
    /**
     * Navigue vers la page de modification des critères.
     */
    @FXML
    public void naviguerVersModificationCriteres(ActionEvent event) {
        masquerTousPanneaux();
        panneauModificationCriteres.setVisible(true);
    }
    
    /**
     * Masque tous les panneaux de l'interface.
     */
    private void masquerTousPanneaux() {
        panneauAccueil.setVisible(false);
        panneauEchanges.setVisible(false);
        panneauHistorique.setVisible(false);
        panneauEnCours.setVisible(false);
        panneauModificationCriteres.setVisible(false);
    }
    
    // ========================================
    // GESTION DES CRITÈRES DE MATCHING
    // ========================================
    
    /**
     * Applique les nouveaux critères de matching définis par l'utilisateur.
     */
    @FXML
    public void appliquerNouveauxCriteres(ActionEvent event) {
        // Application des valeurs des sliders aux critères de matching
        Criteria.GUEST_ANIMAL_ALLERGY.setRatio(sliderCritereAllergies.getValue() * 0.1);
        Criteria.HOST_HAS_ANIMAL.setRatio(sliderCritereAllergies.getValue() * 0.1);
        
        Criteria.GUEST_FOOD_CONSTRAINT.setRatio(sliderCritereAlimentation.getValue() * 0.1);
        Criteria.HOST_FOOD.setRatio(sliderCritereAlimentation.getValue() * 0.1);
        
        Criteria.GENDER.setRatio(sliderCritereGenre.getValue() * 0.1);
        Criteria.PAIR_GENDER.setRatio(sliderCritereGenre.getValue() * 0.1);
        
        Criteria.HISTORY.setRatio(sliderCritereHistorique.getValue() * 0.1);
        Criteria.BIRTH_DATE.setRatio(sliderCritereAge.getValue() * 0.1);
        
        // Navigation vers la page d'échanges avec recalcul
        masquerTousPanneaux();
        panneauEchanges.setVisible(true);
        afficherEchangesPourVisite(event, echangeSelectionne, false);
        fermerCelluleEtendue();
        
        // Affichage du message de confirmation
        afficherMessageConfirmation("Modifications appliquées !", Color.GREEN);
    }
    
    /**
     * Recharge les échanges avec les critères actuels.
     */
    @FXML
    public void rechargerEchanges(ActionEvent event) {
        masquerTousPanneaux();
        panneauEchanges.setVisible(true);
        boutonRetourEchanges.setVisible(false);
        boutonVoirToutesPaires.setVisible(true);
        
        afficherEchangesPourVisite(event, echangeSelectionne, false);
        fermerCelluleEtendue();
        
        afficherMessageConfirmation("Rechargé avec succès !", Color.GREEN);
    }
    
    // ========================================
    // GESTION DES ÉCHANGES ET PAIRES
    // ========================================
    
    /**
     * Affiche les échanges possibles pour une visite donnée.
     * 
     * @param event L'événement déclencheur
     * @param visiteSelectionnee La visite pour laquelle afficher les échanges
     * @param afficherTous Si true, affiche tous les échanges possibles, sinon seulement les meilleurs
     */
    public void afficherEchangesPourVisite(ActionEvent event, CountryVisit visiteSelectionnee, boolean afficherTous) {
        if (visiteSelectionnee == null) return;
        
        // Calcul des échanges selon le mode demandé
        if (!afficherTous) {
            listeExchangesPossibles = visiteSelectionnee.getBestExchanges();
            labelMeilleuresAffectations.setVisible(true);
            labelTousLesEchanges.setVisible(false);
            labelConfirmationAction.setVisible(true);
            
            // Masquage automatique du message après 2 secondes
            PauseTransition pause = new PauseTransition(Duration.seconds(2));
            pause.setOnFinished(e -> labelConfirmationAction.setVisible(false));
            pause.play();
        } else {
            listeExchangesPossibles = visiteSelectionnee.getAllPossibleExchanges();
            labelConfirmationAction.setVisible(false);
            labelMeilleuresAffectations.setVisible(false);
            labelTousLesEchanges.setVisible(true);
        }
        
        // Tri alphabétique par nom d'hôte
        listeExchangesPossibles.sort((e1, e2) -> 
            e1.getHost().getFullName().compareToIgnoreCase(e2.getHost().getFullName()));
        
        // Affichage de la première page
        numeroPageEchanges.setText("0");
        changerPageEchanges(1);
        
        // Configuration de l'interface
        masquerTousPanneaux();
        panneauEchanges.setVisible(true);
        nomEchangeActuel.setText(visiteSelectionnee.toSimpleString());
        boutonModifierCriteres.setVisible(true);
        fermerCelluleEtendue();
        
        echangeSelectionne = visiteSelectionnee;
    }
    
    /**
     * Affiche toutes les paires possibles pour l'échange sélectionné.
     */
    @FXML
    public void afficherToutesLesPaires(ActionEvent event) {
        afficherEchangesPourVisite(event, echangeSelectionne, true);
        boutonRetourEchanges.setVisible(true);
        boutonVoirToutesPaires.setVisible(false);
    }
    
    /**
     * Retourne à l'affichage des meilleures paires uniquement.
     */
    @FXML
    public void retournerAuxMeilleuresPaires(ActionEvent event) {
        numeroPageEchanges.setText("0");
        changerPageEchanges(1);
        masquerTousPanneaux();
        panneauEchanges.setVisible(true);
        boutonRetourEchanges.setVisible(false);
        boutonVoirToutesPaires.setVisible(true);
        
        afficherEchangesPourVisite(event, echangeSelectionne, false);
    }
    
    // ========================================
    // GESTION DE LA PAGINATION
    // ========================================
    
    /**
     * Change la page affichée dans l'historique.
     */
    @FXML
    public void changerPageHistoriqueSuivante(ActionEvent event) {
        int pageActuelle = Integer.parseInt(numeroPageHistorique.getText());
        changerPageHistorique(pageActuelle + 1);
    }
    
    @FXML
    public void changerPageHistoriquePrecedente(ActionEvent event) {
        int pageActuelle = Integer.parseInt(numeroPageHistorique.getText());
        if (pageActuelle > 0) {
            changerPageHistorique(pageActuelle - 1);
        }
    }
    
    private void changerPageHistorique(int nouvellePage) {
        int elementsParPage = 10;
        int indexDebut = nouvellePage * elementsParPage;
        
        if (indexDebut < listeCompleteEchanges.size()) {
            numeroPageHistorique.setText(String.valueOf(nouvellePage));
            int indexFin = Math.min(indexDebut + elementsParPage, listeCompleteEchanges.size());
            listeHistorique.getItems().setAll(
                listeCompleteEchanges.subList(indexDebut, indexFin));
        }
        fermerCelluleEtendue();
    }
    
    /**
     * Change la page affichée dans les échanges en cours.
     */
    @FXML
    public void changerPageEnCoursSuivante(ActionEvent event) {
        int pageActuelle = Integer.parseInt(numeroPageEnCours.getText());
        changerPageEnCours(pageActuelle + 1);
    }
    
    @FXML
    public void changerPageEnCoursPrecedente(ActionEvent event) {
        int pageActuelle = Integer.parseInt(numeroPageEnCours.getText());
        if (pageActuelle > 0) {
            changerPageEnCours(pageActuelle - 1);
        }
    }
    
    private void changerPageEnCours(int nouvellePage) {
        int elementsParPage = 10;
        int indexDebut = (nouvellePage - 1) * elementsParPage;
        
        if (indexDebut >= 0 && indexDebut < listeEchangesActuels.size()) {
            numeroPageEnCours.setText(String.valueOf(nouvellePage));
            int indexFin = Math.min(indexDebut + elementsParPage, listeEchangesActuels.size());
            listeEchangesEnCours.getItems().setAll(
                listeEchangesActuels.subList(indexDebut, indexFin));
        }
        fermerCelluleEtendue();
    }
    
    /**
     * Change la page affichée dans les échanges possibles.
     */
    @FXML
    public void changerPageEchangesSuivante(ActionEvent event) {
        int pageActuelle = Integer.parseInt(numeroPageEchanges.getText());
        changerPageEchanges(pageActuelle + 1);
    }
    
    @FXML
    public void changerPageEchangesPrecedente(ActionEvent event) {
        int pageActuelle = Integer.parseInt(numeroPageEchanges.getText());
        if (pageActuelle > 0) {
            changerPageEchanges(pageActuelle - 1);
        }
    }
    
    private void changerPageEchanges(int nouvellePage) {
        int elementsParPage = 10;
        int indexDebut = (nouvellePage - 1) * elementsParPage;
        
        if (indexDebut >= 0 && indexDebut < listeExchangesPossibles.size()) {
            numeroPageEchanges.setText(String.valueOf(nouvellePage));
            int indexFin = Math.min(indexDebut + elementsParPage, listeExchangesPossibles.size());
            listePairesEchanges.getItems().setAll(
                listeExchangesPossibles.subList(indexDebut, indexFin));
        }
        fermerCelluleEtendue();
    }
    
    // ========================================
    // GESTION DES CELLULES ÉTENDUES
    // ========================================
    
    /**
     * Enregistre une cellule comme étant actuellement étendue.
     * Ferme automatiquement la cellule précédemment étendue.
     */
    public void enregistrerCelluleEtendue(ExpandingCell cellule) {
        if (celluleActuellementEtendue != null && celluleActuellementEtendue != cellule) {
            celluleActuellementEtendue.unToggleExpanded();
        }
        celluleActuellementEtendue = cellule;
    }
    
    /**
     * Ferme la cellule actuellement étendue.
     */
    private void fermerCelluleEtendue() {
        if (celluleActuellementEtendue != null) {
            celluleActuellementEtendue.unToggleExpanded();
            celluleActuellementEtendue = null;
        }
    }
    
    // ========================================
    // UTILITAIRES ET ACCESSEURS
    // ========================================
    
    /**
     * Rafraîchit toutes les listes avec les données actuelles.
     */
    public void rafraichirListes() {
        listeCompleteEchanges.clear();
        listeEchangesActuels.clear();
        initialiserDonneesEchanges();
        afficherEchangesAccueil();
    }
    
    /**
     * Affiche un message de confirmation temporaire.
     */
    private void afficherMessageConfirmation(String message, Color couleur) {
        labelConfirmationAction.setText(message);
        labelConfirmationAction.setTextFill(couleur);
        labelConfirmationAction.setVisible(true);
        
        PauseTransition pause = new PauseTransition(Duration.seconds(2));
        pause.setOnFinished(e -> labelConfirmationAction.setVisible(false));
        pause.play();
    }
    
    /**
     * Retourne le gestionnaire de données.
     */
    public PeopleManager getGestionnaireDonnees() {
        return gestionnaireDonnees;
    }
}