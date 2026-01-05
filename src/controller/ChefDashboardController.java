package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import model.Utilisateur;
import dao.ChefDepartementDAO;
import model.ChefDepartement;

public class ChefDashboardController extends DashboardController {

    @FXML
    private Label lblIdChefValue;
    @FXML
    private Label lblDateNominationValue;
    @FXML
    private javafx.scene.control.Label lblDeptBudgets;
    @FXML
    private javafx.scene.control.Label lblDeptAvgSalary;
    @FXML
    private javafx.scene.control.Label lblDeptEmployees;

    @FXML
    private void openEmployes() {
        try {
            dao.EmployeDAO dao = new dao.EmployeDAO();
            javafx.scene.control.TableView<model.Employe> table = new javafx.scene.control.TableView<>();
            javafx.scene.control.TableColumn<model.Employe, Integer> colId = new javafx.scene.control.TableColumn<>("ID");
            colId.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("idEmploye"));
            javafx.scene.control.TableColumn<model.Employe, String> colNom = new javafx.scene.control.TableColumn<>("Nom");
            colNom.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("nom"));
            javafx.scene.control.TableColumn<model.Employe, String> colPrenom = new javafx.scene.control.TableColumn<>("Prénom");
            colPrenom.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("prenom"));
            javafx.scene.control.TableColumn<model.Employe, String> colPoste = new javafx.scene.control.TableColumn<>("Poste");
            colPoste.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("poste"));
            javafx.scene.control.TableColumn<model.Employe, Double> colSalaire = new javafx.scene.control.TableColumn<>("Salaire");
            colSalaire.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("salaire"));
            javafx.scene.control.TableColumn<model.Employe, Integer> colDept = new javafx.scene.control.TableColumn<>("Département");
            colDept.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("idDepartement"));
            table.getColumns().addAll(colId, colNom, colPrenom, colPoste, colSalaire, colDept);
            table.setItems(javafx.collections.FXCollections.observableArrayList(dao.getAllEmployes()));

            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Employés");
            stage.setScene(new javafx.scene.Scene(table, 700, 400));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void validateTasks() {
        try {
            dao.EmployeDAO edao = new dao.EmployeDAO();
            java.util.List<model.Employe> list = edao.getAllEmployes();
            StringBuilder sb = new StringBuilder();
            for (model.Employe e : list) {
                if (e.getTache() != null && !e.getTache().isEmpty()) {
                    sb.append("#").append(e.getIdEmploye()).append(" ").append(e.getNom()).append(" ").append(e.getPrenom()).append(": ").append(e.getTache()).append("\n");
                }
            }
            if (sb.length() == 0) {
                javafx.scene.control.Alert a = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION, "Aucune tâche à valider.");
                a.showAndWait();
                return;
            }
            javafx.scene.control.TextArea ta = new javafx.scene.control.TextArea(sb.toString());
            ta.setEditable(false);
            javafx.scene.control.Dialog<java.lang.Void> dlg = new javafx.scene.control.Dialog<>();
            dlg.setTitle("Validation des tâches");
            dlg.getDialogPane().setContent(ta);
            dlg.getDialogPane().getButtonTypes().addAll(javafx.scene.control.ButtonType.OK);
            dlg.showAndWait();
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void openProjetTacheDialog() {
        try {
            javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
            grid.setHgap(10); grid.setVgap(10);
            javafx.scene.control.TextField tfIdEmp = new javafx.scene.control.TextField();
            javafx.scene.control.TextField tfNomProjet = new javafx.scene.control.TextField();
            javafx.scene.control.TextField tfDesc = new javafx.scene.control.TextField();
            javafx.scene.control.TextField tfBudget = new javafx.scene.control.TextField();
            javafx.scene.control.TextField tfTache = new javafx.scene.control.TextField();
            grid.addRow(0, new javafx.scene.control.Label("Id Employé (responsable):"), tfIdEmp);
            grid.addRow(1, new javafx.scene.control.Label("Nom projet (laissez vide si tâche):"), tfNomProjet);
            grid.addRow(2, new javafx.scene.control.Label("Description:"), tfDesc);
            grid.addRow(3, new javafx.scene.control.Label("Budget (projet):"), tfBudget);
            grid.addRow(4, new javafx.scene.control.Label("Tâche (si applicable):"), tfTache);
            javafx.scene.control.Dialog<Void> dlg = new javafx.scene.control.Dialog<>();
            dlg.setTitle("Ajouter projet / tâche");
            dlg.getDialogPane().getButtonTypes().addAll(new javafx.scene.control.ButtonType("Enregistrer", javafx.scene.control.ButtonBar.ButtonData.OK_DONE), javafx.scene.control.ButtonType.CANCEL);
            dlg.getDialogPane().setContent(grid);
            java.util.Optional<Void> res = dlg.showAndWait();
            if (res.isPresent()) {
                int idEmp = 0; try { idEmp = Integer.parseInt(tfIdEmp.getText()); } catch (Exception ignore) {}
                String nom = tfNomProjet.getText();
                String desc = tfDesc.getText();
                String tache = tfTache.getText();
                if (tache != null && !tache.isEmpty() && idEmp != 0) {
                    service.EmployeService es = new service.EmployeService();
                    model.Employe e = es.getEmployeById(idEmp);
                    if (e != null) {
                        e.setTache(tache);
                        es.modifierEmploye(e);
                        // notifications removed: only persist task to employee
                    }
                }
                if (nom != null && !nom.isEmpty()) {
                    model.Projet p = new model.Projet();
                    p.setNom(nom);
                    p.setDescription(desc);
                    try { p.setBudgetPrevu(Double.parseDouble(tfBudget.getText())); } catch (Exception ignore) {}
                    p.setIdEmploye(idEmp);
                    new dao.ProjetDAO().create(p);
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    @Override
    protected void updateUI() {
        if (utilisateur != null) {
            if (lblWelcome != null) {
                lblWelcome.setText("Espace Chef de Département : " + utilisateur.getEmail());
            }
            super.updateUI();
            try {
                ChefDepartementDAO dao = new ChefDepartementDAO();
                ChefDepartement cd = dao.findByEmploye(utilisateur.getIdEmploye());
                if (cd != null) {
                    boolean gridShown = false;
                    if (lblIdChefValue != null) {
                        lblIdChefValue.setText(String.valueOf(cd.getIdChef()));
                        gridShown = true;
                    }
                    if (lblDateNominationValue != null) {
                        lblDateNominationValue.setText(String.valueOf(cd.getDateNomination()));
                        gridShown = true;
                    }
                    if (!gridShown && lblInfo != null) {
                        String info = "IdChef: " + cd.getIdChef() + "\nDateNomination: " + cd.getDateNomination();
                        lblInfo.setText(info);
                    }
                }
                // populate department-level stats
                try {
                    service.EmployeService es = new service.EmployeService();
                    model.Employe myEmp = es.getEmployeById(utilisateur.getIdEmploye());
                    if (myEmp != null) {
                        int myDept = myEmp.getIdDepartement();
                        java.util.List<model.Employe> deptEmps = new dao.EmployeDAO().getEmployesByDepartement(myDept);
                        java.util.List<model.Budget> allB = new dao.BudgetDAO().getAllBudgets();
                        double deptBud = allB.stream().filter(b -> b.getIdDepartement() == myDept).mapToDouble(model.Budget::getMontant).sum();
                        double avg = util.StatsUtil.averageSalary(deptEmps);
                        int count = deptEmps.size();
                        if (lblDeptBudgets != null) lblDeptBudgets.setText(String.format("%.2f", deptBud));
                        if (lblDeptAvgSalary != null) lblDeptAvgSalary.setText(String.format("%.2f", avg));
                        if (lblDeptEmployees != null) lblDeptEmployees.setText(String.valueOf(count));
                    }
                } catch (Exception ignore) {}
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private javafx.scene.control.Button btnMenu;

    @FXML
    private void showMenu() {
        try {
            javafx.scene.control.ContextMenu menu = new javafx.scene.control.ContextMenu();
            javafx.scene.control.MenuItem m1 = new javafx.scene.control.MenuItem("Employés"); m1.setOnAction(ev -> openEmployes());
            javafx.scene.control.MenuItem m2 = new javafx.scene.control.MenuItem("Valider les tâches"); m2.setOnAction(ev -> validateTasks());
            javafx.scene.control.MenuItem m3 = new javafx.scene.control.MenuItem("Ajouter projet/tâche"); m3.setOnAction(ev -> openProjetTacheDialog());
            javafx.scene.control.MenuItem m4 = new javafx.scene.control.MenuItem("Exporter (CSV)"); m4.setOnAction(ev -> exportForCurrentUser());
            menu.getItems().addAll(m1, new javafx.scene.control.SeparatorMenuItem(), m2, m3, new javafx.scene.control.SeparatorMenuItem(), m4);
            menu.show(btnMenu, javafx.geometry.Side.BOTTOM, 0, 0);
         } catch (Exception e) { e.printStackTrace(); }
    }
}