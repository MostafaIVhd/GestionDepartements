package util;

import model.Utilisateur;
import model.Employe;
import model.Budget;
import model.Projet;
import dao.EmployeDAO;
import dao.DepartementDAO;
import dao.BudgetDAO;
import dao.ProjetDAO;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ExportUtil {

    public static List<File> exportForUser(Utilisateur user) throws Exception {
        if (user == null) throw new IllegalArgumentException("Utilisateur required");
        String role = user.getRole() != null ? user.getRole().toLowerCase() : "";
        String home = System.getProperty("user.home");
        List<File> result = new ArrayList<>();

        EmployeDAO eDao = new EmployeDAO();
        DepartementDAO dDao = new DepartementDAO();
        BudgetDAO bDao = new BudgetDAO();
        ProjetDAO pDao = new ProjetDAO();

        if (role.contains("admin")) {
            // export all
            File fEmp = new File(home, "employes_export.csv");
            try (PrintWriter pw = new PrintWriter(fEmp)) {
                pw.println("idEmploye,nom,prenom,poste,salaire,idDepartement,demande,tache");
                for (Employe e : eDao.getAllEmployes()) {
                    pw.printf("%d,%s,%s,%s,%.2f,%d,%s,%s\n", e.getIdEmploye(), nullSafe(e.getNom()), nullSafe(e.getPrenom()), nullSafe(e.getPoste()), e.getSalaire(), e.getIdDepartement(), csvSafe(e.getDemande()), csvSafe(e.getTache()));
                }
            }
            result.add(fEmp);

            File fDept = new File(home, "departements_export.csv");
            try (PrintWriter pw = new PrintWriter(fDept)) {
                pw.println("idDepartement,nom,description,idChef");
                for (model.Departement d : dDao.findAll()) {
                    pw.printf("%d,%s,%s,%d\n", d.getIdDepartement(), nullSafe(d.getNom()), csvSafe(d.getDescription()), d.getIdChef());
                }
            }
            result.add(fDept);

            File fBud = new File(home, "budgets_export.csv");
            try (PrintWriter pw = new PrintWriter(fBud)) {
                pw.println("idBudget,montant,annee,idDepartement");
                for (Budget b : bDao.getAllBudgets()) {
                    pw.printf("%d,%.2f,%d,%d\n", b.getIdBudget(), b.getMontant(), b.getAnnee(), b.getIdDepartement());
                }
            }
            result.add(fBud);

            File fProj = new File(home, "projets_export.csv");
            try (PrintWriter pw = new PrintWriter(fProj)) {
                pw.println("idProjet,nom,description,budgetPrevu,idEmploye");
                for (Projet p : pDao.findAll()) {
                    pw.printf("%d,%s,%s,%.2f,%d\n", p.getIdProjet(), nullSafe(p.getNom()), csvSafe(p.getDescription()), p.getBudgetPrevu(), p.getIdEmploye());
                }
            }
            result.add(fProj);

        } else if (role.contains("chef")) {
            // department-level export
            Employe chefEmp = eDao.getEmployeById(user.getIdEmploye());
            if (chefEmp == null) return result;
            int deptId = chefEmp.getIdDepartement();
            File fEmp = new File(home, "employes_dept_" + deptId + ".csv");
            try (PrintWriter pw = new PrintWriter(fEmp)) {
                pw.println("idEmploye,nom,prenom,poste,salaire,idDepartement,demande,tache");
                for (Employe e : eDao.getEmployesByDepartement(deptId)) {
                    pw.printf("%d,%s,%s,%s,%.2f,%d,%s,%s\n", e.getIdEmploye(), nullSafe(e.getNom()), nullSafe(e.getPrenom()), nullSafe(e.getPoste()), e.getSalaire(), e.getIdDepartement(), csvSafe(e.getDemande()), csvSafe(e.getTache()));
                }
            }
            result.add(fEmp);

            File fBud = new File(home, "budgets_dept_" + deptId + ".csv");
            try (PrintWriter pw = new PrintWriter(fBud)) {
                pw.println("idBudget,montant,annee,idDepartement");
                for (Budget b : bDao.getAllBudgets().stream().filter(x -> x.getIdDepartement() == deptId).collect(Collectors.toList())) {
                    pw.printf("%d,%.2f,%d,%d\n", b.getIdBudget(), b.getMontant(), b.getAnnee(), b.getIdDepartement());
                }
            }
            result.add(fBud);

            File fProj = new File(home, "projets_dept_" + deptId + ".csv");
            try (PrintWriter pw = new PrintWriter(fProj)) {
                pw.println("idProjet,nom,description,budgetPrevu,idEmploye");
                for (Projet p : pDao.findAll().stream().filter(p -> {
                    try { return eDao.getEmployeById(p.getIdEmploye()).getIdDepartement() == deptId; } catch (Exception ex) { return false; }
                }).collect(Collectors.toList())) {
                    pw.printf("%d,%s,%s,%.2f,%d\n", p.getIdProjet(), nullSafe(p.getNom()), csvSafe(p.getDescription()), p.getBudgetPrevu(), p.getIdEmploye());
                }
            }
            result.add(fProj);

        } else {
            // employe -> export own profile, tasks, projects
            Employe emp = eDao.getEmployeById(user.getIdEmploye());
            if (emp == null) return result;
            File fProfile = new File(home, "profile_emp_" + emp.getIdEmploye() + ".csv");
            try (PrintWriter pw = new PrintWriter(fProfile)) {
                pw.println("idEmploye,nom,prenom,poste,salaire,idDepartement,demande,tache");
                pw.printf("%d,%s,%s,%s,%.2f,%d,%s,%s\n", emp.getIdEmploye(), nullSafe(emp.getNom()), nullSafe(emp.getPrenom()), nullSafe(emp.getPoste()), emp.getSalaire(), emp.getIdDepartement(), csvSafe(emp.getDemande()), csvSafe(emp.getTache()));
            }
            result.add(fProfile);

            File fProj = new File(home, "projets_emp_" + emp.getIdEmploye() + ".csv");
            try (PrintWriter pw = new PrintWriter(fProj)) {
                pw.println("idProjet,nom,description,budgetPrevu,idEmploye");
                for (Projet p : pDao.findByEmploye(emp.getIdEmploye())) {
                    pw.printf("%d,%s,%s,%.2f,%d\n", p.getIdProjet(), nullSafe(p.getNom()), csvSafe(p.getDescription()), p.getBudgetPrevu(), p.getIdEmploye());
                }
            }
            result.add(fProj);
        }

        return result;
    }

    private static String nullSafe(String s) { return s == null ? "" : s.replaceAll(",", " "); }
    private static String csvSafe(String s) { return s == null ? "" : s.replaceAll("[\r\n]", " ").replaceAll(",", " "); }
}
