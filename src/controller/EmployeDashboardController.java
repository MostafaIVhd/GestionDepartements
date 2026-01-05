package controller;

import javafx.fxml.FXML;
import model.Utilisateur;

public class EmployeDashboardController extends DashboardController {

    @Override
    protected void updateUI() {
        if (utilisateur != null) {
            if (lblWelcome != null) {
                lblWelcome.setText("Espace Employé : " + utilisateur.getEmail());
            }
            super.updateUI();
        }
    }

    @FXML
    private void submitRequest() {
        try {
            javafx.scene.control.TextInputDialog dialog = new javafx.scene.control.TextInputDialog();
            dialog.setTitle("Soumettre une demande");
            dialog.setHeaderText("Entrez votre demande ou rapport");
            dialog.setContentText("Demande:");
            java.util.Optional<String> result = dialog.showAndWait();
            result.ifPresent(text -> {
                try {
                    service.EmployeService es = new service.EmployeService();
                    model.Employe emp = es.getEmployeById(utilisateur.getIdEmploye());
                    emp.setDemande(text);
                    es.modifierEmploye(emp);
                    javafx.scene.control.Alert ok = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION, "Demande enregistrée.");
                    ok.showAndWait();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void openMyProjects() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/projet_view.fxml"));
            javafx.scene.Parent root = loader.load();
            controller.ProjetController pc = loader.getController();
            pc.setEmployeFilter(utilisateur.getIdEmploye());
            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Mes Projets");
            stage.setScene(new javafx.scene.Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void viewMyDepartment() {
        try {
            service.EmployeService es = new service.EmployeService();
            model.Employe emp = es.getEmployeById(utilisateur.getIdEmploye());
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
            alert.setTitle("Mon département");
            alert.setHeaderText(null);
            alert.setContentText("IdDepartement: " + emp.getIdDepartement());
            alert.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void updateMyProfile() {
        try {
            service.EmployeService es = new service.EmployeService();
            model.Employe emp = es.getEmployeById(utilisateur.getIdEmploye());

            // Allow employees to update only their first and last names
            javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
            grid.setHgap(10); grid.setVgap(10); grid.setStyle("-fx-padding: 12;");
            javafx.scene.control.TextField tfNom = new javafx.scene.control.TextField(emp.getNom());
            javafx.scene.control.TextField tfPrenom = new javafx.scene.control.TextField(emp.getPrenom());
            grid.addRow(0, new javafx.scene.control.Label("Nom:"), tfNom);
            grid.addRow(1, new javafx.scene.control.Label("Prénom:"), tfPrenom);

            javafx.scene.control.Dialog<Void> dialog = new javafx.scene.control.Dialog<>();
            dialog.setTitle("Mettre à jour mon profil");
            javafx.scene.control.ButtonType saveBtn = new javafx.scene.control.ButtonType("Enregistrer", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(saveBtn, javafx.scene.control.ButtonType.CANCEL);
            dialog.getDialogPane().setContent(grid);
            java.util.Optional<Void> res = dialog.showAndWait();
            if (res.isPresent()) {
                emp.setNom(tfNom.getText());
                emp.setPrenom(tfPrenom.getText());
                // Do not allow employees to modify other fields
                es.modifierEmploye(emp);
                updateUI();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private javafx.scene.control.Button btnMenu;

    @FXML
    private void showMenu() {
        try {
            javafx.scene.control.ContextMenu menu = new javafx.scene.control.ContextMenu();
            javafx.scene.control.MenuItem m1 = new javafx.scene.control.MenuItem("Soumettre une demande"); m1.setOnAction(ev -> submitRequest());
            javafx.scene.control.MenuItem m2 = new javafx.scene.control.MenuItem("Mes projets"); m2.setOnAction(ev -> openMyProjects());
            javafx.scene.control.MenuItem m3 = new javafx.scene.control.MenuItem("Mon département"); m3.setOnAction(ev -> viewMyDepartment());
            javafx.scene.control.MenuItem m4 = new javafx.scene.control.MenuItem("Mettre à jour mon profil"); m4.setOnAction(ev -> updateMyProfile());
            javafx.scene.control.MenuItem m5 = new javafx.scene.control.MenuItem("Exporter (CSV)"); m5.setOnAction(ev -> exportForCurrentUser());
            menu.getItems().addAll(m1, m2, m3, new javafx.scene.control.SeparatorMenuItem(), m4, new javafx.scene.control.SeparatorMenuItem(), m5);
             menu.show(btnMenu, javafx.geometry.Side.BOTTOM, 0, 0);
         } catch (Exception e) { e.printStackTrace(); }
    }
}