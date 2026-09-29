#include "Budget.h"

Budget::Budget(long budgetId, long userId, double monthlyBudget, int month, int year)
    : budgetId(budgetId), userId(userId), monthlyBudget(monthlyBudget), month(month), year(year) {}

long Budget::getBudgetId() const { return budgetId; }
long Budget::getUserId() const { return userId; }
double Budget::getMonthlyBudget() const { return monthlyBudget; }
int Budget::getMonth() const { return month; }
int Budget::getYear() const { return year; }

bool Budget::setBudget(double newAmount) {
    if (newAmount <= 0.0) return false;
    monthlyBudget = newAmount;
    return true;
}

double Budget::getBudget() const {
    return monthlyBudget;
}

BudgetStatus Budget::checkBudget(double totalSpent) const {
    BudgetStatus status{};
    status.isSet = monthlyBudget > 0.0;
    if (!status.isSet) {
        status.exceeded = false;
        status.remaining = 0.0;
        status.percentageUsed = 0.0;
        return status;
    }
    status.remaining = monthlyBudget - totalSpent;
    status.exceeded = totalSpent > monthlyBudget;
    status.percentageUsed = (totalSpent / monthlyBudget) * 100.0;
    return status;
}
