package maquette;

import java.lang.ModuleLayer.Controller;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class ExpandingCell extends ListCell<String> {
        private VBox cellContainer = new VBox();
        private HBox header = new HBox();
        private Label titleLabel = new Label();
        private VBox detailsBox = new VBox();
        private Label detailsLabel = new Label();
        private Button actionButton = null;
        private boolean expanded = false;
        private final ListController controller;
        

        public ExpandingCell(ListController controller,boolean haveButton) {
            super();

            this.header.getChildren().add(titleLabel);
            this.header.setOnMouseClicked(e -> toggleExpanded());
            
            this.detailsBox.setStyle("-fx-padding: 0 0 0 20;");
            this.detailsBox.setSpacing(10);

            this.detailsBox.getChildren().add(detailsLabel);
            if(haveButton){
                actionButton = new Button("Plus de détails");
                this.actionButton.setOnAction(e -> controller.buttonEchange(e, titleLabel.getText())); // TODO à modifier pour rendre fonctionnelle l'app
                this.detailsBox.getChildren().add(actionButton);
            }

            cellContainer.getChildren().add(header);
            this.controller = controller;
        }

        private void toggleExpanded() {
            if (!this.expanded) {
                controller.registerExpandedCell(this);
            }
            this.expanded = !this.expanded;
            if (this.expanded) {
                if (!this.cellContainer.getChildren().contains(this.detailsBox)) {
                    this.cellContainer.getChildren().add(this.detailsBox);
                }
            } else {
                this.cellContainer.getChildren().remove(this.detailsBox);
            }
        }


        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
            } else {
                this.titleLabel.setText(item);
                this.detailsLabel.setText("Détails pour " + item);
                if (!this.expanded && this.cellContainer.getChildren().contains(this.detailsBox)) {
                    this.cellContainer.getChildren().remove(this.detailsBox);
                }
                setGraphic(this.cellContainer);
            }
        }

        public void unToggleExpanded() {
            expanded = false;
            cellContainer.getChildren().remove(detailsBox);
        }
        // TODO public void ajouteRedirectionAboutton() à implementer pour rendre fonctionnelle l'app

        public String getTitleLabel(){
            return this.titleLabel.getText();
        }
    }
