#include "Expense.h"

Expense::Expense(long expenseId, long userId, std::string category, double amount,
                  std::string date, std::string note)
    : Transaction(expenseId, userId, amount, std::move(date), std::move(note)),
      category(std::move(category)) {}

std::string Expense::getCategory() const { return category; }
void Expense::setCategory(const std::string &newCategory) { category = newCategory; }

bool Expense::addExpense() const {
    return isValidAmount() && isValidDate() && !category.empty();
}

bool Expense::editExpense() const {
    return addExpense();
}

std::string Expense::getType() const {
    return "Expense";
}
