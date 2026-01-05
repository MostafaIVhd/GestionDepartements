package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Budget;
import dao.BudgetDAO;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class BudgetController extends DashboardController implements Initializable {

    @FXML
    private TableView<Budget> tableBudget;
    @FXML
    private TableColumn<Budget, Integer> colId;
    @FXML
    private TableColumn<Budget, Double> colMontant;
    @FXML
    private TableColumn<Budget, Integer> colAnnee;
    @FXML
    private TableColumn<Budget, Integer> colIdDepartement;

    @FXML
    private Label lblId;
    @FXML
    private Label lblMontant;
    @FXML
    private Label lblAnnee;
    @FXML
    private Label lblIdDepartement;

    private final BudgetDAO budgetDAO = new BudgetDAO(); // Assume BudgetDAO exists and has findAll
    private ObservableList<Budget> budgetList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colId.setCellValueFactory(new PropertyValueFactory<>("idBudget"));
        colMontant.setCellValueFactory(new PropertyValueFactory<>("montant"));
        colAnnee.setCellValueFactory(new PropertyValueFactory<>("annee"));
        colIdDepartement.setCellValueFactory(new PropertyValueFactory<>("idDepartement"));

        loadBudgets();

        tableBudget.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, sel) -> {
            if (sel != null) {
                lblId.setText(String.valueOf(sel.getIdBudget()));
                lblMontant.setText(String.valueOf(sel.getMontant()));
                lblAnnee.setText(String.valueOf(sel.getAnnee()));
                lblIdDepartement.setText(String.valueOf(sel.getIdDepartement()));
            } else {
                lblId.setText("");
                lblMontant.setText("");
                lblAnnee.setText("");
                lblIdDepartement.setText("");
            }
        });

        // Role-based UI: only admin can modify budgets
        javafx.application.Platform.runLater(() -> {
            boolean isAdmin = utilisateur != null && "admin".equalsIgnoreCase(utilisateur.getRole());
            try {
                // If controller was created without user, attempt to read from parent stage controller
                if (utilisateur == null) {
                    // nothing
                }
            } catch (Exception ignore) {}
            // Lookup buttons by fx:id if present
            try {
                javafx.scene.control.Button bAdd = (javafx.scene.control.Button) tableBudget.getScene().lookup("#btnAjouter");
                javafx.scene.control.Button bEdit = (javafx.scene.control.Button) tableBudget.getScene().lookup("#btnModifier");
                javafx.scene.control.Button bDel = (javafx.scene.control.Button) tableBudget.getScene().lookup("#btnSupprimer");
                if (bAdd != null) bAdd.setDisable(!isAdmin);
                if (bEdit != null) bEdit.setDisable(!isAdmin);
                if (bDel != null) bDel.setDisable(!isAdmin);
            } catch (Exception ignore) {}
        });
    }

    // Implement load, add, delete methods similar to DepartementController
    private void loadBudgets() {
        try {
            budgetList.setAll(budgetDAO.getAllBudgets());
            tableBudget.setItems(budgetList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleActualiser() {
        loadBudgets();
    }

    @FXML
    private void handleFermer() {
        try {
            javafx.stage.Stage stage = (javafx.stage.Stage) tableBudget.getScene().getWindow();
            stage.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAjouter() {
        if (!isAdmin()) { showPermissionError(); return; }
        try {
            javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
            grid.setHgap(10); grid.setVgap(10);
            javafx.scene.control.TextField tfMontant = new javafx.scene.control.TextField();
            javafx.scene.control.TextField tfAnnee = new javafx.scene.control.TextField();
            javafx.scene.control.TextField tfIdDept = new javafx.scene.control.TextField();
            grid.addRow(0, new javafx.scene.control.Label("Montant:"), tfMontant);
            grid.addRow(1, new javafx.scene.control.Label("Année:"), tfAnnee);
            grid.addRow(2, new javafx.scene.control.Label("IdDépartement:"), tfIdDept);
            javafx.scene.control.Dialog<Void> dlg = new javafx.scene.control.Dialog<>();
            dlg.setTitle("Ajouter Budget");
            dlg.getDialogPane().getButtonTypes().addAll(new javafx.scene.control.ButtonType("Enregistrer", javafx.scene.control.ButtonBar.ButtonData.OK_DONE), javafx.scene.control.ButtonType.CANCEL);
            dlg.getDialogPane().setContent(grid);
            java.util.Optional<Void> res = dlg.showAndWait();
            if (res.isPresent()) {
                model.Budget b = new model.Budget();
                try { b.setMontant(Double.parseDouble(tfMontant.getText())); } catch (Exception ex) {}
                try { b.setAnnee(Integer.parseInt(tfAnnee.getText())); } catch (Exception ex) {}
                try { b.setIdDepartement(Integer.parseInt(tfIdDept.getText())); } catch (Exception ex) {}
                budgetDAO.ajouterBudget(b);
                loadBudgets();
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void handleModifier() {
        if (!isAdmin()) { showPermissionError(); return; }
        model.Budget sel = tableBudget.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        try {
            javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
            grid.setHgap(10); grid.setVgap(10);
            javafx.scene.control.TextField tfMontant = new javafx.scene.control.TextField(String.valueOf(sel.getMontant()));
            javafx.scene.control.TextField tfAnnee = new javafx.scene.control.TextField(String.valueOf(sel.getAnnee()));
            javafx.scene.control.TextField tfIdDept = new javafx.scene.control.TextField(String.valueOf(sel.getIdDepartement()));
            grid.addRow(0, new javafx.scene.control.Label("Montant:"), tfMontant);
            grid.addRow(1, new javafx.scene.control.Label("Année:"), tfAnnee);
            grid.addRow(2, new javafx.scene.control.Label("IdDépartement:"), tfIdDept);
            javafx.scene.control.Dialog<Void> dlg = new javafx.scene.control.Dialog<>();
            dlg.setTitle("Modifier Budget");
            dlg.getDialogPane().getButtonTypes().addAll(new javafx.scene.control.ButtonType("Enregistrer", javafx.scene.control.ButtonBar.ButtonData.OK_DONE), javafx.scene.control.ButtonType.CANCEL);
            dlg.getDialogPane().setContent(grid);
            java.util.Optional<Void> res = dlg.showAndWait();
            if (res.isPresent()) {
                try { sel.setMontant(Double.parseDouble(tfMontant.getText())); } catch (Exception ex) {}
                try { sel.setAnnee(Integer.parseInt(tfAnnee.getText())); } catch (Exception ex) {}
                try { sel.setIdDepartement(Integer.parseInt(tfIdDept.getText())); } catch (Exception ex) {}
                budgetDAO.modifierBudget(sel);
                loadBudgets();
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void handleSupprimer() {
        if (!isAdmin()) { showPermissionError(); return; }
        model.Budget sel = tableBudget.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        try { budgetDAO.supprimerBudget(sel.getIdBudget()); loadBudgets(); } catch (Exception e) { e.printStackTrace(); }
    }

    private boolean isAdmin() {
        if (utilisateur == null) return false;
        String r = utilisateur.getRole();
        if (r == null) return false;
        return r.toLowerCase().contains("admin");
    }

    private void showPermissionError() {
        javafx.scene.control.Alert a = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING, "Vous n'êtes pas autorisé à effectuer cette action.");
        a.showAndWait();
    }
}