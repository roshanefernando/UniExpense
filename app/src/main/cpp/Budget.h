#ifndef UNIEXPENSE_BUDGET_H
#define UNIEXPENSE_BUDGET_H

#include <string>

// Simple, immutable-ish status object returned by Budget::checkBudget().
struct BudgetStatus {
    bool isSet;
    bool exceeded;
    double remaining;
    double percentageUsed;
};

class Budget {
private:
    long budgetId;
    long userId;
    double monthlyBudget;
    int month;
    int year;

public:
    Budget(long budgetId, long userId, double monthlyBudget, int month, int year);

    long getBudgetId() const;
    long getUserId() const;
    double getMonthlyBudget() const;
    int getMonth() const;
    int getYear() const;

    bool setBudget(double newAmount);   // validates and applies
    double getBudget() const;

    // Given how much has already been spent this month, compute status.
    BudgetStatus checkBudget(double totalSpent) const;
};

#endif // UNIEXPENSE_BUDGET_H
