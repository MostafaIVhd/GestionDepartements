package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Projet;
import dao.ProjetDAO;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class ProjetController extends DashboardController implements Initializable {

    @FXML
    private TableView<Projet> tableProjet;
    @FXML
    private TableColumn<Projet, Integer> colId;
    @FXML
    private TableColumn<Projet, String> colNom;
    @FXML
    private TableColumn<Projet, String> colDescription;
    @FXML
    private TableColumn<Projet, Double> colBudget;
    @FXML
    private TableColumn<Projet, Integer> colIdEmploye;

    @FXML
    private Label lblId;
    @FXML
    private Label lblNom;
    @FXML
    private Label lblDescription;
    @FXML
    private Label lblBudget;
    @FXML
    private Label lblIdEmploye;

    private final ProjetDAO projetDAO = new ProjetDAO(); // Assume ProjetDAO exists
    private ObservableList<Projet> projetList = FXCollections.observableArrayList();
    private Integer employeFilter = null;

    public void setEmployeFilter(Integer id) {
        this.employeFilter = id;
        loadProjets();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colId.setCellValueFactory(new PropertyValueFactory<>("idProjet"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colBudget.setCellValueFactory(new PropertyValueFactory<>("budgetPrevu"));
        colIdEmploye.setCellValueFactory(new PropertyValueFactory<>("idEmploye"));

        loadProjets();

        tableProjet.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, sel) -> {
            if (sel != null) {
                lblId.setText(String.valueOf(sel.getIdProjet()));
                lblNom.setText(sel.getNom());
                lblDescription.setText(sel.getDescription());
                lblBudget.setText(String.valueOf(sel.getBudgetPrevu()));
                lblIdEmploye.setText(String.valueOf(sel.getIdEmploye()));
            } else {
                lblId.setText("");
                lblNom.setText("");
                lblDescription.setText("");
                lblBudget.setText("");
                lblIdEmploye.setText("");
            }
        });

        // Role-based button enabling: admin and chef can modify projects
        javafx.application.Platform.runLater(() -> {
            boolean canEdit = utilisateur != null && (utilisateur.getRole().toLowerCase().contains("admin") || utilisateur.getRole().toLowerCase().contains("chef"));
            try {
                javafx.scene.control.Button bAdd = (javafx.scene.control.Button) tableProjet.getScene().lookup("#btnAjouter");
                javafx.scene.control.Button bEdit = (javafx.scene.control.Button) tableProjet.getScene().lookup("#btnModifier");
                javafx.scene.control.Button bDel = (javafx.scene.control.Button) tableProjet.getScene().lookup("#btnSupprimer");
                if (bAdd != null) bAdd.setDisable(!canEdit);
                if (bEdit != null) bEdit.setDisable(!canEdit);
                if (bDel != null) bDel.setDisable(!canEdit);
            } catch (Exception ignore) {}
        });
    }

    private void loadProjets() {
        try {
            if (employeFilter != null) {
                projetList.setAll(projetDAO.findByEmploye(employeFilter));
            } else {
                projetList.setAll(projetDAO.findAll());
            }
            tableProjet.setItems(projetList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleActualiser() {
        loadProjets();
    }

    @FXML
    private void handleFermer() {
        try {
            javafx.stage.Stage stage = (javafx.stage.Stage) tableProjet.getScene().getWindow();
            stage.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAjouter() {
        if (!canEditProjects()) { showPermissionError(); return; }
        try {
            javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
            grid.setHgap(10); grid.setVgap(10);
            javafx.scene.control.TextField tfNom = new javafx.scene.control.TextField();
            javafx.scene.control.TextField tfDesc = new javafx.scene.control.TextField();
            javafx.scene.control.TextField tfBudget = new javafx.scene.control.TextField();
            javafx.scene.control.TextField tfIdEmp = new javafx.scene.control.TextField();
            grid.addRow(0, new javafx.scene.control.Label("Nom:"), tfNom);
            grid.addRow(1, new javafx.scene.control.Label("Description:"), tfDesc);
            grid.addRow(2, new javafx.scene.control.Label("Budget:"), tfBudget);
            grid.addRow(3, new javafx.scene.control.Label("IdEmploye:"), tfIdEmp);
            javafx.scene.control.Dialog<Void> dlg = new javafx.scene.control.Dialog<>();
            dlg.setTitle("Ajouter Projet");
            dlg.getDialogPane().getButtonTypes().addAll(new javafx.scene.control.ButtonType("Enregistrer", javafx.scene.control.ButtonBar.ButtonData.OK_DONE), javafx.scene.control.ButtonType.CANCEL);
            dlg.getDialogPane().setContent(grid);
            java.util.Optional<Void> res = dlg.showAndWait();
            if (res.isPresent()) {
                model.Projet p = new model.Projet();
                p.setNom(tfNom.getText()); p.setDescription(tfDesc.getText());
                try { p.setBudgetPrevu(Double.parseDouble(tfBudget.getText())); } catch (Exception ignore) {}
                try { p.setIdEmploye(Integer.parseInt(tfIdEmp.getText())); } catch (Exception ignore) {}
                projetDAO.create(p);
                loadProjets();
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void handleModifier() {
        if (!canEditProjects()) { showPermissionError(); return; }
        Projet sel = tableProjet.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        try {
            javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
            grid.setHgap(10); grid.setVgap(10);
            javafx.scene.control.TextField tfNom = new javafx.scene.control.TextField(sel.getNom());
            javafx.scene.control.TextField tfDesc = new javafx.scene.control.TextField(sel.getDescription());
            javafx.scene.control.TextField tfBudget = new javafx.scene.control.TextField(String.valueOf(sel.getBudgetPrevu()));
            javafx.scene.control.TextField tfIdEmp = new javafx.scene.control.TextField(String.valueOf(sel.getIdEmploye()));
            grid.addRow(0, new javafx.scene.control.Label("Nom:"), tfNom);
            grid.addRow(1, new javafx.scene.control.Label("Description:"), tfDesc);
            grid.addRow(2, new javafx.scene.control.Label("Budget:"), tfBudget);
            grid.addRow(3, new javafx.scene.control.Label("IdEmploye:"), tfIdEmp);
            javafx.scene.control.Dialog<Void> dlg = new javafx.scene.control.Dialog<>();
            dlg.setTitle("Modifier Projet");
            dlg.getDialogPane().getButtonTypes().addAll(new javafx.scene.control.ButtonType("Enregistrer", javafx.scene.control.ButtonBar.ButtonData.OK_DONE), javafx.scene.control.ButtonType.CANCEL);
            dlg.getDialogPane().setContent(grid);
            java.util.Optional<Void> res = dlg.showAndWait();
            if (res.isPresent()) {
                sel.setNom(tfNom.getText()); sel.setDescription(tfDesc.getText());
                try { sel.setBudgetPrevu(Double.parseDouble(tfBudget.getText())); } catch (Exception ignore) {}
                try { sel.setIdEmploye(Integer.parseInt(tfIdEmp.getText())); } catch (Exception ignore) {}
                projetDAO.update(sel);
                loadProjets();
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void handleSupprimer() {
        if (!canEditProjects()) { showPermissionError(); return; }
        Projet sel = tableProjet.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        try { projetDAO.delete(sel.getIdProjet()); loadProjets(); } catch (Exception e) { e.printStackTrace(); }
    }

    private boolean canEditProjects() {
        if (utilisateur == null) return false;
        String r = utilisateur.getRole(); if (r == null) return false;
        r = r.toLowerCase(); return r.contains("admin") || r.contains("chef");
    }

    private void showPermissionError() { javafx.scene.control.Alert a = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING, "Vous n'êtes pas autorisé à effectuer cette action."); a.showAndWait(); }
}