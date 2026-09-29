#include "Income.h"

Income::Income(long incomeId, long userId, std::string source, double amount,
               std::string date, std::string note)
    : Transaction(incomeId, userId, amount, std::move(date), std::move(note)),
      source(std::move(source)) {}

std::string Income::getSource() const { return source; }
void Income::setSource(const std::string &newSource) { source = newSource; }

bool Income::addIncome() const {
    return isValidAmount() && isValidDate() && !source.empty();
}

bool Income::editIncome() const {
    return addIncome();
}

std::string Income::getType() const {
    return "Income";
}
