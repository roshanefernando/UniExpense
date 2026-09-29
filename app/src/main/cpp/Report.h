#ifndef UNIEXPENSE_REPORT_H
#define UNIEXPENSE_REPORT_H

class Report {
private:
    double totalIncome;
    double totalExpense;
    double balance;
    double budget;
    double remainingBudget;

public:
    Report();

    // Pure calculation helpers (composition: Report is built from raw sums
    // that Kotlin gathers from ExpenseRepository / IncomeRepository).
    static double calculateTotalIncome(const double *amounts, int count);
    static double calculateTotalExpense(const double *amounts, int count);
    static double calculateBalance(double income, double expense);
    static double calculateBudgetRemaining(double budget, double expense);
    static double calculateBudgetPercentage(double expense, double budget);

    // Populates this Report's fields and returns it "assembled", mirroring
    // the UML's calculateReport().
    void calculateReport(double income, double expense, double budgetAmount);

    double getTotalIncome() const;
    double getTotalExpense() const;
    double getBalance() const;
    double getBudget() const;
    double getRemainingBudget() const;
};

#endif // UNIEXPENSE_REPORT_H
