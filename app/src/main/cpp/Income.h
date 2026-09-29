#ifndef UNIEXPENSE_INCOME_H
#define UNIEXPENSE_INCOME_H

#include "Transaction.h"
#include <string>

class Income : public Transaction {
private:
    std::string source;

public:
    Income(long incomeId, long userId, std::string source, double amount,
           std::string date, std::string note);

    std::string getSource() const;
    void setSource(const std::string &newSource);

    bool addIncome() const;
    bool editIncome() const;

    std::string getType() const override;
};

#endif // UNIEXPENSE_INCOME_H
