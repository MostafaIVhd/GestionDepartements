package util;

import model.Budget;
import model.Employe;

import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StatsUtil {

    // Sum of budgets
    public static double totalBudget(List<Budget> budgets) {
        return budgets.stream().mapToDouble(Budget::getMontant).sum();
    }

    // Average salary
    public static double averageSalary(List<Employe> emps) {
        return emps.stream().mapToDouble(Employe::getSalaire).average().orElse(0.0);
    }

    // Group employees by department
    public static Map<Integer, List<Employe>> groupByDepartement(List<Employe> emps) {
        return emps.stream().collect(Collectors.groupingBy(Employe::getIdDepartement));
    }

    // Basic statistics for salaries
    public static DoubleSummaryStatistics salaryStats(List<Employe> emps) {
        return emps.stream().mapToDouble(Employe::getSalaire).summaryStatistics();
    }
}
