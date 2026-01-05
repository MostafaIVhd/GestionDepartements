package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import model.Utilisateur;
import service.EmployeService;
import model.Employe;

public class DashboardController {

    @FXML
    protected Label lblWelcome;

    @FXML
    protected Label lblInfo;

    @FXML
    protected Label lblIdUtilisateur;
    @FXML
    protected Label lblEmailValue;
    @FXML
    protected Label lblPasswordValue;
    @FXML
    protected Label lblIdEmployeValue;
    @FXML
    protected Label lblNomValue;
    @FXML
    protected Label lblPosteValue;
    @FXML
    protected Label lblSalaireValue;
    @FXML
    protected Label lblIdDepartementValue;
    @FXML
    protected Label lblPrenomValue;
    @FXML
    protected Label lblTacheValue;
    @FXML
    protected Label lblDemandeValue;

    protected Utilisateur utilisateur;
    protected final EmployeService employeService = new EmployeService();

    public void setUtilisateur(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
        updateUI();
    }

    protected void updateUI() {
        if (utilisateur != null) {
            if (lblWelcome != null) lblWelcome.setText("Bienvenue, " + utilisateur.getEmail());
            Employe emp = null;
            try {
                emp = employeService.getEmployeById(utilisateur.getIdEmploye());
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (lblIdUtilisateur != null) lblIdUtilisateur.setText(String.valueOf(utilisateur.getIdUtilisateur()));
            if (lblEmailValue != null) lblEmailValue.setText(utilisateur.getEmail());
            if (lblPasswordValue != null) lblPasswordValue.setText(utilisateur.getPassword());
            if (lblIdEmployeValue != null) lblIdEmployeValue.setText(String.valueOf(utilisateur.getIdEmploye()));
            if (emp != null) {
                if (lblNomValue != null) lblNomValue.setText(emp.getNom());
                if (lblPrenomValue != null) lblPrenomValue.setText(emp.getPrenom());
                if (lblPosteValue != null) lblPosteValue.setText(emp.getPoste());
                if (lblSalaireValue != null) lblSalaireValue.setText(String.valueOf(emp.getSalaire()));
                if (lblIdDepartementValue != null) lblIdDepartementValue.setText(String.valueOf(emp.getIdDepartement()));
                if (lblTacheValue != null) lblTacheValue.setText(emp.getTache() == null ? "" : emp.getTache());
                if (lblDemandeValue != null) lblDemandeValue.setText(emp.getDemande() == null ? "" : emp.getDemande());
            } else {
                if (lblNomValue != null) lblNomValue.setText("");
                if (lblPrenomValue != null) lblPrenomValue.setText("");
                if (lblPosteValue != null) lblPosteValue.setText("");
                if (lblSalaireValue != null) lblSalaireValue.setText("");
                if (lblIdDepartementValue != null) lblIdDepartementValue.setText("");
                if (lblTacheValue != null) lblTacheValue.setText("");
                if (lblDemandeValue != null) lblDemandeValue.setText("");
            }
            if (lblInfo != null && lblIdUtilisateur == null && lblEmailValue == null) {
                StringBuilder sb = new StringBuilder();
                sb.append("IdUtilisateur: ").append(utilisateur.getIdUtilisateur()).append("\n");
                sb.append("Email: ").append(utilisateur.getEmail()).append("\n");
                sb.append("Password: ").append(utilisateur.getPassword()).append("\n");
                sb.append("IdEmploye: ").append(utilisateur.getIdEmploye()).append("\n");
                if (emp != null) {
                    sb.append("Nom: ").append(emp.getNom()).append("\n");
                    sb.append("Poste: ").append(emp.getPoste()).append("\n");
                    sb.append("Salaire: ").append(emp.getSalaire()).append("\n");
                    sb.append("IdDepartement: ").append(emp.getIdDepartement()).append("\n");
                    sb.append("Tache: ").append(emp.getTache() == null ? "" : emp.getTache()).append("\n");
                    sb.append("Demande: ").append(emp.getDemande() == null ? "" : emp.getDemande()).append("\n");
                }
                lblInfo.setText(sb.toString());
            }
        }
    }
    
    @FXML
    protected void handleLogout() {
        // Return to login
        try {
             javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/login.fxml"));
             javafx.scene.Parent root = loader.load();
             javafx.stage.Stage stage = (javafx.stage.Stage) lblWelcome.getScene().getWindow();
             stage.setScene(new javafx.scene.Scene(root));
         } catch (Exception e) {
             e.printStackTrace();
         }
     }

    // Helper to open an FXML view and if its controller extends DashboardController pass the current user
    protected void openViewWithUser(String fxmlPath, String title) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource(fxmlPath));
            javafx.scene.Parent root = loader.load();
            Object ctrl = loader.getController();
            if (ctrl != null && ctrl instanceof DashboardController) {
                ((DashboardController) ctrl).setUtilisateur(this.utilisateur);
            }
            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle(title);
            javafx.scene.Scene scene = new javafx.scene.Scene(root);
            java.net.URL cssUrl = getClass().getResource("/styles.css");
            if (cssUrl != null) scene.getStylesheets().add(cssUrl.toExternalForm());
            stage.setScene(scene);
             stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Export data that the current user is allowed to access
    protected void exportForCurrentUser() {
        try {
            java.util.List<java.io.File> files = util.ExportUtil.exportForUser(this.utilisateur);
            if (files == null || files.isEmpty()) {
                javafx.scene.control.Alert a = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION, "Aucun fichier généré.");
                a.showAndWait();
                return;
            }
            StringBuilder sb = new StringBuilder();
            for (java.io.File f : files) sb.append(f.getAbsolutePath()).append("\n");
            javafx.scene.control.Alert ok = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION, "Exports générés:\n" + sb.toString());
            ok.showAndWait();
            // open containing folder of first file
            try {
                String home = System.getProperty("user.home");
                String os = System.getProperty("os.name").toLowerCase();
                if (os.contains("win")) {
                    Runtime.getRuntime().exec(new String[] {"explorer", home});
                } else if (os.contains("mac")) {
                    Runtime.getRuntime().exec(new String[] {"open", home});
                } else {
                    Runtime.getRuntime().exec(new String[] {"xdg-open", home});
                }
            } catch (Exception ignore) {}
        } catch (Exception e) {
            e.printStackTrace();
            javafx.scene.control.Alert a = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR, "Erreur lors de l'export: " + e.getMessage());
            a.showAndWait();
        }
    }
}