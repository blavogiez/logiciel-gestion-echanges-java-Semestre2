package application ;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Cellule extensible pour l'affichage des échanges (Exchange).
 * Permet d'afficher un résumé dans l'en-tête et des détails des critères quand elle est étendue.
 * Inclut des boutons pour bannir un échange ou le rendre obligatoire.
 */
public class ExpandingCellForEx extends ListCell<Exchange> implements ExpandingCell {
    
    // ========================================
    // ÉLÉMENTS DE L'INTERFACE
    // ========================================
    
    private VBox conteneurCellule = new VBox();
    private HBox entete = new HBox();
    private Label labelTitre = new Label();
    private VBox boiteDetails = new VBox();
    private Label labelDetails = new Label();
    private Button boutonBannirEchange = new Button("Bannir cet échange");
    private Button boutonRendreObligatoire = new Button("Rendre cet échange obligatoire");
    
    // ========================================
    // ÉTAT ET CONTRÔLEUR
    // ========================================
    
    private boolean estEtendue = false;
    private final ListController controleur;
    
    // ========================================
    // CONSTRUCTEUR ET INITIALISATION
    // ========================================
    
    /**
     * Construit une nouvelle cellule extensible pour les échanges.
     * 
     * @param controleur Le contrôleur principal de l'application
     * @param avecBoutons Si true, affiche les boutons d'action dans la cellule
     */
    public ExpandingCellForEx(ListController controleur, boolean avecBoutons) {
        super();
        this.controleur = controleur;
        
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
        boiteDetails.getChildren().addAll(labelDetails, boutonBannirEchange, boutonRendreObligatoire);
        
        // Assemblage de la cellule
        conteneurCellule.getChildren().add(entete);
    }
    
    /**
     * Configure les comportements interactifs de la cellule.
     */
    private void configurerComportements() {
        // Clic sur l'en-tête pour étendre/réduire
        entete.setOnMouseClicked(e -> basculerExtension());
        
        // Action pour bannir un échange
        boutonBannirEchange.setOnAction(e -> bannirEchangeActuel());
        
        // Action pour rendre un échange obligatoire
        boutonRendreObligatoire.setOnAction(e -> rendreEchangeObligatoire());
    }
    
    // ========================================
    // ACTIONS SUR LES ÉCHANGES
    // ========================================
    
    /**
     * Bannit l'échange actuellement affiché dans cette cellule.
     * L'échange ne sera plus proposé dans les futures recommandations.
     */
    private void bannirEchangeActuel() {
        Exchange echange = getItem();
        if (echange != null) {
            controleur.getGestionnaireDonnees().banExchange(echange);
            controleur.rafraichirListes();
        }
    }
    
    /**
     * Rend l'échange actuellement affiché obligatoire.
     * Cet échange sera prioritaire dans les recommandations.
     */
    private void rendreEchangeObligatoire() {
        Exchange echange = getItem();
        if (echange != null) {
            // Récupération de la visite de pays correspondante
            CountryVisit visitePays = controleur.getGestionnaireDonnees()
                .getCountryVisit(echange.getGuest().getCountry(), echange.getHost().getCountry());
            
            if (visitePays != null) {
                visitePays.setMandatoryExchange(echange);
                System.out.println("Échanges obligatoires : " + visitePays.getMandatoryExchanges());
                controleur.rafraichirListes();
            }
        }
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
            afficherDetailsEchange();
            ajouterBoiteDetails();
        } else {
            retirerBoiteDetails();
        }
    }
    
    /**
     * Met à jour le contenu des détails avec les critères qui ont influencé l'échange.
     */
    private void afficherDetailsEchange() {
        Exchange echange = getItem();
        if (echange != null) {
            labelDetails.setText(echange.criteriaThatMatteredToString());
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
     * Met à jour le contenu de la cellule selon l'élément Exchange associé.
     */
    @Override
    protected void updateItem(Exchange echange, boolean vide) {
        super.updateItem(echange, vide);
        
        if (vide || echange == null) {
            setGraphic(null);
        } else {
            mettreAJourContenu(echange);
            setGraphic(conteneurCellule);
        }
    }
    
    /**
     * Met à jour le contenu textuel de la cellule.
     * 
     * @param echange L'échange à afficher
     */
    private void mettreAJourContenu(Exchange echange) {
        labelTitre.setText(echange.toGraphicalString());
        labelDetails.setText("Détails pour " + echange.toGraphicalString());
        
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