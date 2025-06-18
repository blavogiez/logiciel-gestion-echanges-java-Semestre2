package application;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Cellule extensible pour l'affichage des visites de pays (CountryVisit).
 * Permet d'afficher un résumé dans l'en-tête et des détails complets quand elle est étendue.
 * Inclut un bouton pour naviguer vers les échanges possibles.
 */
public class ExpandingCellForCv extends ListCell<CountryVisit> implements ExpandingCell {
    
    // ========================================
    // ÉLÉMENTS DE L'INTERFACE
    // ========================================
    
    private VBox conteneurCellule = new VBox();
    private HBox entete = new HBox();
    private Label labelTitre = new Label();
    private VBox boiteDetails = new VBox();
    private Label labelDetails = new Label();
    private Button boutonVoirEchanges = null;
    
    // ========================================
    // ÉTAT ET CONTRÔLEUR
    // ========================================
    
    private boolean estEtendue = false;
    private final ListController controleur;
    private final boolean afficherTousLesEchanges;
    
    // ========================================
    // CONSTRUCTEUR ET INITIALISATION
    // ========================================
    
    /**
     * Construit une nouvelle cellule extensible pour les visites de pays.
     * 
     * @param controleur Le contrôleur principal de l'application
     * @param afficherTousLesEchanges Si true, le bouton affichera tous les échanges possibles,
     *                               sinon seulement les meilleurs
     */
    public ExpandingCellForCv(ListController controleur, boolean afficherTousLesEchanges) {
        super();
        this.controleur = controleur;
        this.afficherTousLesEchanges = afficherTousLesEchanges;
        
        initialiserInterface();
        configurerComportements();
    }
    
    /**
     * Initialise la structure de l'interface de la cellule.
     */
    private void initialiserInterface() {
        // Configuration de l'en-tête
        entete.getChildren().add(labelTitre);
        
        // Configuration de la boîte de détails
        boiteDetails.setStyle("-fx-padding: 0 0 0 20;");
        boiteDetails.setSpacing(10);
        boiteDetails.getChildren().add(labelDetails);
        
        // Création et ajout du bouton d'action
        boutonVoirEchanges = new Button("Voir les échanges possibles");
        boiteDetails.getChildren().add(boutonVoirEchanges);
        
        // Assemblage de la cellule
        conteneurCellule.getChildren().add(entete);
    }
    
    /**
     * Configure les comportements interactifs de la cellule.
     */
    private void configurerComportements() {
        // Clic sur l'en-tête pour étendre/réduire
        entete.setOnMouseClicked(e -> basculerExtension());
        
        // Action du bouton pour voir les échanges
        boutonVoirEchanges.setOnAction(e -> 
            controleur.afficherEchangesPourVisite(e, this.getItem(), afficherTousLesEchanges));
    }
    
    // ========================================
    // GESTION DE L'EXTENSION
    // ========================================
    
    /**
     * Bascule l'état d'extension de la cellule.
     * Notifie le contrôleur pour gérer l'exclusivité des cellules étendues.
     */
    private void basculerExtension() {
        if (!estEtendue) {
            controleur.enregistrerCelluleEtendue(this);
        }
        
        estEtendue = !estEtendue;
        
        if (estEtendue) {
            ajouterBoiteDetails();
        } else {
            retirerBoiteDetails();
        }
    }
    
    /**
     * Ajoute la boîte de détails au conteneur si elle n'y est pas déjà.
     */
    private void ajouterBoiteDetails() {
        if (!conteneurCellule.getChildren().contains(boiteDetails)) {
            conteneurCellule.getChildren().add(boiteDetails);
        }
    }
    
    /**
     * Retire la boîte de détails du conteneur.
     */
    private void retirerBoiteDetails() {
        conteneurCellule.getChildren().remove(boiteDetails);
    }
    
    /**
     * Force la fermeture de l'extension de la cellule.
     * Utilisé par le contrôleur pour fermer les cellules étendues.
     */
    @Override
    public void unToggleExpanded() {
        estEtendue = false;
        retirerBoiteDetails();
    }
    
    // ========================================
    // MISE À JOUR DU CONTENU
    // ========================================
    
    /**
     * Met à jour le contenu de la cellule selon l'élément CountryVisit associé.
     */
    @Override
    protected void updateItem(CountryVisit visitePays, boolean vide) {
        super.updateItem(visitePays, vide);
        
        if (vide || visitePays == null) {
            setGraphic(null);
        } else {
            mettreAJourContenu(visitePays);
            setGraphic(conteneurCellule);
        }
    }
    
    /**
     * Met à jour le contenu textuel de la cellule.
     * 
     * @param visitePays La visite de pays à afficher
     */
    private void mettreAJourContenu(CountryVisit visitePays) {
        labelTitre.setText(visitePays.toSimpleString());
        labelDetails.setText(visitePays.toString());
        
        // S'assurer que la boîte de détails n'est pas affichée si la cellule n'est pas étendue
        if (!estEtendue && conteneurCellule.getChildren().contains(boiteDetails)) {
            retirerBoiteDetails();
        }
    }
    
    // ========================================
    // ACCESSEURS
    // ========================================
    
    /**
     * Retourne le texte du titre de la cellule.
     * 
     * @return Le texte affiché dans l'en-tête de la cellule
     */
    public String getTitleLabel() {
        return labelTitre.getText();
    }
}