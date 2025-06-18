package maquette;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.ArrayList;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;

public class ListController {

    @FXML
    private ListView<String> listHisto;
    @FXML
    private ListView<String> listHistoAccueil;
    @FXML
    private ListView<String> listEnCoursAccueil;
    @FXML
    private ListView<String> listEnCours;
    @FXML
    private ListView<String> listPaires;
    @FXML
    private Label pageNumber;
    @FXML
    private Label pageNumber1;
    @FXML
    private Label pageNumber2;
    @FXML
    private Pane home;
    @FXML
    private VBox echange;
    @FXML
    private VBox histo;
    @FXML
    private VBox enCours;
    @FXML
    private Label nomEchange;
    @FXML
    private Pane modifPage;
    @FXML
    private Button buttonModif;


    private ArrayList<String> listeEchange = new ArrayList<>();
    private ArrayList<String> listePairesARemplir = new ArrayList<>();
    private ArrayList<String> listEchangeEnCours = new ArrayList<>();
    private ExpandingCell currentlyExpandedCell = null;

    public void initialize() {
        this.fillListEchange();
        this.fillListEchangeEnCours();
        this.fillListPaires();

        
        listHistoAccueil.setItems(FXCollections.observableList(listeEchange.subList(0, 4)));
        listEnCoursAccueil.setItems(FXCollections.observableList(listEchangeEnCours.subList(0, 4)));


        listHistoAccueil.setCellFactory(list -> new ExpandingCell(this, true));
        listEnCoursAccueil.setCellFactory(list -> new ExpandingCell(this, true));
        listHisto.setCellFactory(list -> new ExpandingCell(this, true));
        listPaires.setCellFactory(list -> new ExpandingCell(this, false ));
        listEnCours.setCellFactory(list -> new ExpandingCell(this, true ));


        home.setVisible(true);
        echange.setVisible(false);
        histo.setVisible(false);
        enCours.setVisible(false);
        modifPage.setVisible(false);
    }

    //Fonction temporaire à remplacer aavec les valeurs finals
    public void fillListEchange(){
        for(int i = 1; i < 40 ; i++){
            listeEchange.add("Echange "+ i);
        }
    }

    public void fillListEchangeEnCours(){
        for(int i = 40; i < 80 ; i++){
            listEchangeEnCours.add("Echange "+ i);
        }
    }

    //Fonction temporaire à remplacer aavec les valeurs finals
    public void fillListPaires(){
        for(int i = 1; i < 40 ; i++){
            listePairesARemplir.add("Paire "+ i);
        }
    }
    
    public void buttonHome(ActionEvent event) {
        home.setVisible(true);
        echange.setVisible(false);
        histo.setVisible(false);
        enCours.setVisible(false);
        modifPage.setVisible(false);
        resetExpendingCell(currentlyExpandedCell);
        listHistoAccueil.setItems(FXCollections.observableList(listeEchange.subList(0, 4)));
        listEnCoursAccueil.setItems(FXCollections.observableList(listEchangeEnCours.subList(0, 4)));
    }

    public void buttonHisto(ActionEvent event) {
        pageNumber.setText("1");
        listHisto.setItems(FXCollections.observableList(listeEchange.subList(0, 10)));
        home.setVisible(false);
        echange.setVisible(false);
        histo.setVisible(true);
        enCours.setVisible(false);
        modifPage.setVisible(false);
        resetExpendingCell(currentlyExpandedCell);
    }

    public void buttonEnCours(ActionEvent event) {
        pageNumber2.setText("1");
        listEnCours.setItems(FXCollections.observableList(listEchangeEnCours.subList(0, 10)));//TODO à modifier
        home.setVisible(false);
        echange.setVisible(false);
        histo.setVisible(false);
        enCours.setVisible(true);
        modifPage.setVisible(false);
        resetExpendingCell(currentlyExpandedCell);
    }

    public void buttonEchange(ActionEvent event,String nomEchangeSelect){
        listPaires.setItems(FXCollections.observableList(listePairesARemplir.subList(0, 10)));
        pageNumber1.setText("1");
        home.setVisible(false);
        echange.setVisible(true);
        histo.setVisible(false);
        enCours.setVisible(false);
        modifPage.setVisible(false);

        nomEchange.setText(nomEchangeSelect);
        int nbEch = Integer.parseInt(nomEchangeSelect.substring(8, nomEchangeSelect.length()));
        if(nomEchangeSelect.substring(0,8).equals("Echange ") && nbEch >= 40){// à remplacer par une foction qui verifie si c'est un echange en cours
             buttonModif.setVisible(true);
        }else{
            buttonModif.setVisible(false);
        }
        resetExpendingCell(currentlyExpandedCell);
    }

    public void buttonRetour(ActionEvent event){
        listPaires.setItems(FXCollections.observableList(listePairesARemplir.subList(0, 10)));
        pageNumber1.setText("1");
        home.setVisible(false);
        echange.setVisible(true);
        histo.setVisible(false);
        enCours.setVisible(false);
        modifPage.setVisible(false);

        nomEchange.setText(currentlyExpandedCell.getTitleLabel());
    }

    public void buttonModifier(ActionEvent event){
        home.setVisible(false);
        echange.setVisible(false);
        histo.setVisible(false);
        enCours.setVisible(false);
        modifPage.setVisible(true);
    }

    public void pressedButtonPlusHisto(ActionEvent event) {
        int newValue = Integer.parseInt(pageNumber.getText());
        if(newValue*10 < listeEchange.size()){
            pageNumber.setText("" + (newValue + 1));
            if(newValue*10 + 10 < listeEchange.size()){
                listHisto.setItems(FXCollections.observableList(listeEchange.subList(newValue*10, newValue*10 + 10)));
            }else{
                listHisto.setItems(FXCollections.observableList(listeEchange.subList(newValue*10, this.listeEchange.size())));
            }
        }
        resetExpendingCell(currentlyExpandedCell);
    }

    public void pressedButtonMinusHisto(ActionEvent event) {
        int newValue = Integer.parseInt(pageNumber.getText()) - 1;
        if(newValue*10 < listeEchange.size() && newValue > 0){
            pageNumber.setText("" + newValue);
            if(newValue*10 + 10 < listeEchange.size()){
                listHisto.setItems(FXCollections.observableList(listeEchange.subList((newValue)*10 - 10,(newValue)*10)));
            }
        }
        resetExpendingCell(currentlyExpandedCell);
    }

    public void pressedButtonPlusEnCours(ActionEvent event) {
        int newValue = Integer.parseInt(pageNumber2.getText());
        if(newValue*10 < listEchangeEnCours.size()){
            pageNumber2.setText("" + (newValue + 1));
            if(newValue*10 + 10 < listEchangeEnCours.size()){
                listEnCours.setItems(FXCollections.observableList(listEchangeEnCours.subList(newValue*10, newValue*10 + 10)));
            }else{
                listEnCours.setItems(FXCollections.observableList(listEchangeEnCours.subList(newValue*10, this.listEchangeEnCours.size())));
            }
        }
        resetExpendingCell(currentlyExpandedCell);
    }

    public void pressedButtonMinusEnCours(ActionEvent event) {
        int newValue = Integer.parseInt(pageNumber2.getText()) - 1;
        if(newValue*10 < listEchangeEnCours.size() && newValue > 0){
            pageNumber2.setText("" + newValue);
            if(newValue*10 + 10 < listEchangeEnCours.size()){
                listEnCours.setItems(FXCollections.observableList(listEchangeEnCours.subList((newValue)*10 - 10,(newValue)*10)));
            }
        }
        resetExpendingCell(currentlyExpandedCell);
    }

    public void pressedButtonPlusEchange(ActionEvent event) {
        int newValue = Integer.parseInt(pageNumber1.getText());
        if(newValue*10 < listePairesARemplir.size()){
            pageNumber1.setText("" + (newValue + 1));
            if(newValue*10 + 10 < listePairesARemplir.size()){
                listPaires.setItems(FXCollections.observableList(listePairesARemplir.subList(newValue*10, newValue*10 + 10)));
            }else{
                listPaires.setItems(FXCollections.observableList(listePairesARemplir.subList(newValue*10, this.listePairesARemplir.size())));
            }
        }
        resetExpendingCell(currentlyExpandedCell);
    }

    public void pressedButtonMinusEchange(ActionEvent event) {
        int newValue = Integer.parseInt(pageNumber1.getText()) - 1;
        if(newValue*10 < listePairesARemplir.size() && newValue > 0){
            pageNumber1.setText("" + newValue);
            if(newValue*10 + 10 < listePairesARemplir.size()){
                listPaires.setItems(FXCollections.observableList(listePairesARemplir.subList((newValue)*10 - 10,(newValue)*10)));
            }
        }
        resetExpendingCell(currentlyExpandedCell);
    }

    public void registerExpandedCell(ExpandingCell cell) {
        if (currentlyExpandedCell != null && currentlyExpandedCell != cell) {
            currentlyExpandedCell.unToggleExpanded(); // Ferme l’ancienne cellule
        }
        currentlyExpandedCell = cell;
    }
    
    public void resetExpendingCell(ExpandingCell cell){
        if (currentlyExpandedCell != null) {
            currentlyExpandedCell.unToggleExpanded(); // Ferme l’ancienne cellule
        }
    }
}
