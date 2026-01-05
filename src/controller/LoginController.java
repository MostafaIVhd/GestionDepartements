package controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import model.Utilisateur;
import service.UtilisateurService;

import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    @FXML
    private TextField txtEmail;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private ComboBox<String> cbRole;

    @FXML
    private Label lblMessage;

    private final UtilisateurService utilisateurService = new UtilisateurService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Remplir la ComboBox avec les rôles
        cbRole.getItems().addAll("Admin", "Chef de département", "Employé");
    }

    @FXML
    private void handleLogin() {
        String email = txtEmail.getText().trim();
        String password = txtPassword.getText().trim();
        String selectedRole = cbRole.getValue();

        if (email.isEmpty() || password.isEmpty() || selectedRole == null) {
            lblMessage.setText("Veuillez remplir tous les champs et choisir un rôle.");
            return;
        }

        Utilisateur utilisateur = utilisateurService.authentifier(email, password);

        if (utilisateur != null) {
            String role = utilisateurService.getRoleByUtilisateur(utilisateur);
            String selectedNorm = utilisateurService.normalizeSelectedRole(selectedRole);

            if (!role.equals(selectedNorm)) {
                lblMessage.setText("Le rôle sélectionné ne correspond pas à votre poste !");
                return;
            }

            lblMessage.setText("Connexion réussie ! Rôle : " + role);

            // Redirect
            try {
                javafx.fxml.FXMLLoader loader;
                String fxmlFile = "";
                
                switch (role) {
                    case "admin":
                        fxmlFile = "/admin_dashboard.fxml";
                        break;
                    case "chef de departement":
                        fxmlFile = "/chef_dashboard.fxml";
                        break;
                    case "employe":
                        fxmlFile = "/employe_dashboard.fxml";
                        break;
                    default:
                        lblMessage.setText("Rôle inconnu !");
                        return;
                }

                loader = new javafx.fxml.FXMLLoader(getClass().getResource(fxmlFile));
                javafx.scene.Parent root = loader.load();
                
                // Pass user data to dashboard
                DashboardController controller = loader.getController();
                controller.setUtilisateur(utilisateur);

                javafx.stage.Stage stage = (javafx.stage.Stage) txtEmail.getScene().getWindow();
                javafx.scene.Scene newScene = new javafx.scene.Scene(root);
                // Attach stylesheet programmatically (equivalent to @styles.css used previously in FXML)
                java.net.URL cssUrl = getClass().getResource("/styles.css");
                if (cssUrl != null) {
                    newScene.getStylesheets().add(cssUrl.toExternalForm());
                }
                stage.setScene(newScene);
                stage.show();

            } catch (Exception e) {
                e.printStackTrace();
                lblMessage.setText("Erreur lors du chargement de la page : " + e.getMessage());
            }

        } else {
            lblMessage.setText("Email ou mot de passe incorrect.");
        }
    }
}