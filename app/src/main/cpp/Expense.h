#ifndef UNIEXPENSE_EXPENSE_H
#define UNIEXPENSE_EXPENSE_H

#include "Transaction.h"
#include <string>

class Expense : public Transaction {
private:
    std::string category;

public:
    Expense(long expenseId, long userId, std::string category, double amount,
            std::string date, std::string note);

    std::string getCategory() const;
    void setCategory(const std::string &newCategory);

    // Overridden business logic (Expense-specific validation on top of the
    // shared Transaction checks).
    bool addExpense() const;   // true if this expense is valid to insert
    bool editExpense() const;  // true if the edited fields are still valid

    // Polymorphism: overrides Transaction's pure-virtual getType().
    std::string getType() const override;
};

#endif // UNIEXPENSE_EXPENSE_H
