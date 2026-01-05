package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import model.Utilisateur;
import dao.EmployeDAO;
import model.Employe;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import dao.ChefDepartementDAO;
import model.ChefDepartement;

public class AdminDashboardController extends DashboardController {

    @FXML
    private javafx.scene.control.Button btnMenu;
    @FXML
    private javafx.scene.control.Label lblTotalBudgets;
    @FXML
    private javafx.scene.control.Label lblAvgSalary;
    @FXML
    private javafx.scene.control.Label lblTotalEmployees;
    @FXML
    private javafx.scene.control.Label lblIdChefValue;
    @FXML
    private javafx.scene.control.Label lblDateNominationValue;

    @FXML
    private void showMenu() {
        try {
            javafx.scene.control.ContextMenu menu = new javafx.scene.control.ContextMenu();
            javafx.scene.control.MenuItem m1 = new javafx.scene.control.MenuItem("Employés"); m1.setOnAction(ev -> openEmployes());
            javafx.scene.control.MenuItem m2 = new javafx.scene.control.MenuItem("Départements"); m2.setOnAction(ev -> openDepartements());
            javafx.scene.control.MenuItem sep = new javafx.scene.control.SeparatorMenuItem();
            javafx.scene.control.MenuItem m3 = new javafx.scene.control.MenuItem("Budgets"); m3.setOnAction(ev -> openBudgets());
            javafx.scene.control.MenuItem m4 = new javafx.scene.control.MenuItem("Projets"); m4.setOnAction(ev -> openProjets());
            javafx.scene.control.MenuItem m5 = new javafx.scene.control.MenuItem("Exporter (CSV)"); m5.setOnAction(ev -> exportCSV());
            // if admin is also a ChefDepartement, give him Chef-specific actions
            try {
                boolean isChef = false;
                if (utilisateur != null) {
                    dao.ChefDepartementDAO _chefDao = new dao.ChefDepartementDAO();
                    model.ChefDepartement _cd = _chefDao.findByEmploye(utilisateur.getIdEmploye());
                    if (_cd != null) isChef = true;
                }
                if (isChef) {
                    // chef-specific items
                    javafx.scene.control.MenuItem cmValidate = new javafx.scene.control.MenuItem("Valider les tâches"); cmValidate.setOnAction(ev -> validateTasksAsChef());
                    javafx.scene.control.MenuItem cmAdd = new javafx.scene.control.MenuItem("Ajouter projet/tâche"); cmAdd.setOnAction(ev -> openProjetTacheDialogAsChef());
                    // insert chef items after the departments entry
                    menu.getItems().addAll(m1, m2, new javafx.scene.control.SeparatorMenuItem(), cmValidate, cmAdd, new javafx.scene.control.SeparatorMenuItem(), m3, m4, new javafx.scene.control.SeparatorMenuItem(), m5);
                    menu.show(btnMenu, javafx.geometry.Side.BOTTOM, 0, 0);
                    return;
                }
            } catch (Exception ignore) {}
            menu.getItems().addAll(m1, m2, sep, m3, m4, new javafx.scene.control.SeparatorMenuItem(), m5);
            menu.show(btnMenu, javafx.geometry.Side.BOTTOM, 0, 0);
        } catch (Exception e) { e.printStackTrace(); }
    }

    @Override
    protected void updateUI() {
        if (utilisateur != null) {
            if (lblWelcome != null) {
                lblWelcome.setText("Espace Administrateur : " + utilisateur.getEmail());
            }
            super.updateUI();
            // populate stats
            try {
                java.util.List<model.Budget> budgets = new dao.BudgetDAO().getAllBudgets();
                java.util.List<model.Employe> emps = new dao.EmployeDAO().getAllEmployes();
                double totalBud = util.StatsUtil.totalBudget(budgets);
                double avgSal = util.StatsUtil.averageSalary(emps);
                int totalEmp = emps.size();
                if (lblTotalBudgets != null) lblTotalBudgets.setText(String.format("%.2f", totalBud));
                if (lblAvgSalary != null) lblAvgSalary.setText(String.format("%.2f", avgSal));
                if (lblTotalEmployees != null) lblTotalEmployees.setText(String.valueOf(totalEmp));
            } catch (Exception ignore) {}
            // If the admin user is also a ChefDepartement, show their idChef in the info area
            try {
                dao.ChefDepartementDAO chefDao = new dao.ChefDepartementDAO();
                model.ChefDepartement cd = chefDao.findByEmploye(utilisateur.getIdEmploye());
                if (cd != null) {
                    if (lblIdChefValue != null) lblIdChefValue.setText(String.valueOf(cd.getIdChef()));
                    if (lblDateNominationValue != null) lblDateNominationValue.setText(String.valueOf(cd.getDateNomination()));
                    // also append to lblInfo fallback
                    if (lblInfo != null) {
                        String info = lblInfo.getText();
                        if (info == null) info = "";
                        info = info + "\nIdChef: " + cd.getIdChef() + "\nDateNomination: " + cd.getDateNomination();
                        lblInfo.setText(info);
                    }
                }
            } catch (Exception ignore) {}
        }
    }

    // Copied/adapted helper from ChefDashboardController.validateTasks()
    private void validateTasksAsChef() {
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

    // Copied/adapted helper from ChefDashboardController.openProjetTacheDialog()
    private void openProjetTacheDialogAsChef() {
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
                        // targeted send to employee or fallback to broadcast
                        try {
                            String sender = utilisateur != null ? utilisateur.getEmail() : "chef";
                            String msg = String.format("{\"type\":\"task_assigned\",\"employeeId\":%d,\"task\":\"%s\",\"by\":\"%s\"}", idEmp, tache.replaceAll("[\"\\]"," "), sender);
                            boolean sent = false;
                            try { sent = util.SocketServer.sendToEmployee(idEmp, msg); } catch (Exception ignore) {}
                            if (!sent) util.SocketServer.broadcast(msg);
                        } catch (Exception ignore) {}
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

    @FXML
    private void openDepartements() {
        openViewWithUser("/departement_view.fxml", "Départements");
    }

    @FXML
    private void openBudgets() {
        openViewWithUser("/budget_view.fxml", "Budgets");
    }

    @FXML
    private void openProjets() {
        openViewWithUser("/projet_view.fxml", "Projets");
    }

    @FXML
    private void openEmployes() {
        try {
            TableView<Employe> table = new TableView<>();
            TableColumn<Employe, Integer> colId = new TableColumn<>("ID");
            colId.setCellValueFactory(new PropertyValueFactory<>("idEmploye"));
            TableColumn<Employe, String> colNom = new TableColumn<>("Nom");
            colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
            TableColumn<Employe, String> colPrenom = new TableColumn<>("Prénom");
            colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
            TableColumn<Employe, String> colPoste = new TableColumn<>("Poste");
            colPoste.setCellValueFactory(new PropertyValueFactory<>("poste"));
            TableColumn<Employe, Double> colSalaire = new TableColumn<>("Salaire");
            colSalaire.setCellValueFactory(new PropertyValueFactory<>("salaire"));
            TableColumn<Employe, Integer> colDept = new TableColumn<>("Département");
            colDept.setCellValueFactory(new PropertyValueFactory<>("idDepartement"));
            table.getColumns().addAll(colId, colNom, colPrenom, colPoste, colSalaire, colDept);

            EmployeDAO dao = new EmployeDAO();
            ObservableList<Employe> data = FXCollections.observableArrayList(dao.getAllEmployes());
            table.setItems(data);

            javafx.scene.layout.VBox root = new javafx.scene.layout.VBox(10);
            javafx.scene.layout.HBox actions = new javafx.scene.layout.HBox(10);
            javafx.scene.control.Button btnAdd = new javafx.scene.control.Button("Ajouter");
            javafx.scene.control.Button btnEdit = new javafx.scene.control.Button("Modifier");
            javafx.scene.control.Button btnDelete = new javafx.scene.control.Button("Supprimer");
            actions.getChildren().addAll(btnAdd, btnEdit, btnDelete);
            root.getChildren().addAll(actions, table);
            root.setStyle("-fx-padding: 12;");

            btnAdd.setOnAction(ev -> {
                try {
                    javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
                    grid.setHgap(10); grid.setVgap(10);
                    javafx.scene.control.TextField tfNom = new javafx.scene.control.TextField();
                    javafx.scene.control.TextField tfPrenom = new javafx.scene.control.TextField();
                    javafx.scene.control.TextField tfPoste = new javafx.scene.control.TextField();
                    javafx.scene.control.TextField tfSalaire = new javafx.scene.control.TextField();
                    javafx.scene.control.TextField tfDept = new javafx.scene.control.TextField();
                    grid.addRow(0, new javafx.scene.control.Label("Nom:"), tfNom);
                    grid.addRow(1, new javafx.scene.control.Label("Prénom:"), tfPrenom);
                    grid.addRow(2, new javafx.scene.control.Label("Poste:"), tfPoste);
                    grid.addRow(3, new javafx.scene.control.Label("Salaire:"), tfSalaire);
                    grid.addRow(4, new javafx.scene.control.Label("Département:"), tfDept);
                    javafx.scene.control.Dialog<Void> dlg = new javafx.scene.control.Dialog<>();
                    dlg.setTitle("Ajouter un employé");
                    dlg.getDialogPane().getButtonTypes().addAll(new javafx.scene.control.ButtonType("Enregistrer", javafx.scene.control.ButtonBar.ButtonData.OK_DONE), javafx.scene.control.ButtonType.CANCEL);
                    dlg.getDialogPane().setContent(grid);
                    java.util.Optional<Void> res = dlg.showAndWait();
                    if (res.isPresent()) {
                        model.Employe e = new model.Employe();
                        e.setNom(tfNom.getText());
                        e.setPrenom(tfPrenom.getText());
                        e.setPoste(tfPoste.getText());
                        try { e.setSalaire(Double.parseDouble(tfSalaire.getText())); } catch (Exception ignore) {}
                        try { e.setIdDepartement(Integer.parseInt(tfDept.getText())); } catch (Exception ignore) {}
                        dao.ajouterEmploye(e);
                        data.setAll(dao.getAllEmployes());
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

            btnEdit.setOnAction(ev -> {
                Employe sel = table.getSelectionModel().getSelectedItem();
                if (sel == null) return;
                try {
                    javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
                    grid.setHgap(10); grid.setVgap(10);
                    javafx.scene.control.TextField tfNom = new javafx.scene.control.TextField(sel.getNom());
                    javafx.scene.control.TextField tfPrenom = new javafx.scene.control.TextField(sel.getPrenom());
                    javafx.scene.control.TextField tfPoste = new javafx.scene.control.TextField(sel.getPoste());
                    javafx.scene.control.TextField tfSalaire = new javafx.scene.control.TextField(String.valueOf(sel.getSalaire()));
                    javafx.scene.control.TextField tfDept = new javafx.scene.control.TextField(String.valueOf(sel.getIdDepartement()));
                    grid.addRow(0, new javafx.scene.control.Label("Nom:"), tfNom);
                    grid.addRow(1, new javafx.scene.control.Label("Prénom:"), tfPrenom);
                    grid.addRow(2, new javafx.scene.control.Label("Poste:"), tfPoste);
                    grid.addRow(3, new javafx.scene.control.Label("Salaire:"), tfSalaire);
                    grid.addRow(4, new javafx.scene.control.Label("Département:"), tfDept);
                    // Add DateNomination for ChefDepartement if the employee is/was promoted
                    javafx.scene.control.DatePicker dpDateNom = new javafx.scene.control.DatePicker();
                    ChefDepartementDAO chefDao = new ChefDepartementDAO();
                    ChefDepartement existingChef = null;
                    try {
                        existingChef = chefDao.findByEmploye(sel.getIdEmploye());
                    } catch (Exception ignore) {}
                    if (existingChef != null && existingChef.getDateNomination() != null) {
                        dpDateNom.setValue(existingChef.getDateNomination());
                    }
                    grid.addRow(5, new javafx.scene.control.Label("Date nomination (chef):"), dpDateNom);
                    javafx.scene.control.Dialog<Void> dlg = new javafx.scene.control.Dialog<>();
                    dlg.setTitle("Modifier l'employé");
                    dlg.getDialogPane().getButtonTypes().addAll(new javafx.scene.control.ButtonType("Enregistrer", javafx.scene.control.ButtonBar.ButtonData.OK_DONE), javafx.scene.control.ButtonType.CANCEL);
                    dlg.getDialogPane().setContent(grid);
                    java.util.Optional<Void> res = dlg.showAndWait();
                    if (res.isPresent()) {
                        sel.setNom(tfNom.getText());
                        sel.setPrenom(tfPrenom.getText());
                        sel.setPoste(tfPoste.getText());
                        try { sel.setSalaire(Double.parseDouble(tfSalaire.getText())); } catch (Exception ignore) {}
                        try { sel.setIdDepartement(Integer.parseInt(tfDept.getText())); } catch (Exception ignore) {}
                        new EmployeDAO().modifierEmploye(sel);
                        // handle ChefDepartement create/update based on DatePicker
                        try {
                            java.time.LocalDate picked = dpDateNom.getValue();
                            if (picked != null) {
                                if (existingChef != null) {
                                    existingChef.setDateNomination(picked);
                                    chefDao.update(existingChef);
                                } else {
                                    ChefDepartement cd = new ChefDepartement();
                                    cd.setIdEmploye(sel.getIdEmploye());
                                    cd.setDateNomination(picked);
                                    chefDao.create(cd);
                                }
                            } else {
                                // if dp null and there was an existing chef, optionally keep or delete; here we keep existing value
                            }
                        } catch (Exception ignore) {}
                        data.setAll(dao.getAllEmployes());
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

            btnDelete.setOnAction(ev -> {
                Employe sel = table.getSelectionModel().getSelectedItem();
                if (sel == null) return;
                try {
                    new EmployeDAO().supprimerEmploye(sel.getIdEmploye());
                    data.setAll(dao.getAllEmployes());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

            Stage stage = new Stage();
            stage.setTitle("Employés");
            javafx.scene.Scene scene = new Scene(root, 800, 500);
            java.net.URL cssUrl = getClass().getResource("/styles.css");
            if (cssUrl != null) scene.getStylesheets().add(cssUrl.toExternalForm());
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void exportCSV() {
        exportForCurrentUser();
    }

    private void openView(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle(title);
            javafx.scene.Scene scene = new Scene(root);
            java.net.URL cssUrl = getClass().getResource("/styles.css");
            if (cssUrl != null) scene.getStylesheets().add(cssUrl.toExternalForm());
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
