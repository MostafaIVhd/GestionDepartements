package service;

import dao.EmployeDAO;
import dao.BudgetDAO;
import model.Employe;
import model.Budget;

import java.io.File;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AutoSaveService {

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final EmployeDAO employeDAO = new EmployeDAO();
    private final BudgetDAO budgetDAO = new BudgetDAO();
    private final File dir;

    public AutoSaveService() {
        this(new File(System.getProperty("user.home"), "gestion_autosave"));
    }

    public AutoSaveService(File dir) {
        this.dir = dir;
        if (!dir.exists()) dir.mkdirs();
    }

    public void startAutoSave(long periodSeconds) {
        scheduler.scheduleAtFixedRate(this::snapshot, periodSeconds, periodSeconds, TimeUnit.SECONDS);
    }

    public void stop() {
        scheduler.shutdownNow();
    }

    private void snapshot() {
        try {
            List<Employe> emps = employeDAO.getAllEmployes();
            List<Budget> buds = budgetDAO.getAllBudgets();
            String ts = LocalDateTime.now().toString().replace(':', '-');
            File fe = new File(dir, "emps_" + ts + ".csv");
            File fb = new File(dir, "buds_" + ts + ".csv");
            try (PrintWriter pw = new PrintWriter(fe)) {
                pw.println("idEmploye,nom,prenom,poste,salaire,idDepartement,demande,tache");
                for (Employe e : emps) {
                    pw.printf("%d,%s,%s,%s,%.2f,%d,%s,%s\n", e.getIdEmploye(), e.getNom(), e.getPrenom(), e.getPoste(), e.getSalaire(), e.getIdDepartement(), safe(e.getDemande()), safe(e.getTache()));
                }
            }
            try (PrintWriter pw = new PrintWriter(fb)) {
                pw.println("idBudget,montant,annee,idDepartement");
                for (Budget b : buds) {
                    pw.printf("%d,%.2f,%d,%d\n", b.getIdBudget(), b.getMontant(), b.getAnnee(), b.getIdDepartement());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String safe(String s) { return s == null ? "" : s.replaceAll("[\n\r]"," "); }
}
