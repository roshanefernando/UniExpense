#include "Report.h"

Report::Report()
    : totalIncome(0), totalExpense(0), balance(0), budget(0), remainingBudget(0) {}

double Report::calculateTotalIncome(const double *amounts, int count) {
    double sum = 0.0;
    for (int i = 0; i < count; i++) sum += amounts[i];
    return sum;
}

double Report::calculateTotalExpense(const double *amounts, int count) {
    double sum = 0.0;
    for (int i = 0; i < count; i++) sum += amounts[i];
    return sum;
}

double Report::calculateBalance(double income, double expense) {
    return income - expense;
}

double Report::calculateBudgetRemaining(double budget, double expense) {
    return budget - expense;
}

double Report::calculateBudgetPercentage(double expense, double budget) {
    if (budget <= 0.0) return 0.0;
    return (expense / budget) * 100.0;
}

void Report::calculateReport(double income, double expense, double budgetAmount) {
    totalIncome = income;
    totalExpense = expense;
    balance = calculateBalance(income, expense);
    budget = budgetAmount;
    remainingBudget = calculateBudgetRemaining(budgetAmount, expense);
}

double Report::getTotalIncome() const { return totalIncome; }
double Report::getTotalExpense() const { return totalExpense; }
double Report::getBalance() const { return balance; }
double Report::getBudget() const { return budget; }
double Report::getRemainingBudget() const { return remainingBudget; }
