#ifndef UNIEXPENSE_TRANSACTION_H
#define UNIEXPENSE_TRANSACTION_H

#include <string>

// Transaction is the common base for Expense and Income.
// Both are "a record of money moving on a date, tied to a user, with a note".
// Making that explicit as a base class avoids duplicating validation logic
// and lets Report treat both polymorphically where useful.
class Transaction {
protected:
    long id;
    long userId;
    double amount;
    std::string date;
    std::string note;

public:
    Transaction(long id, long userId, double amount, std::string date, std::string note);
    virtual ~Transaction() = default;

    // Getters (encapsulation: fields are private/protected, accessed only via methods)
    long getId() const;
    long getUserId() const;
    double getAmount() const;
    std::string getDate() const;
    std::string getNote() const;

    // Setters
    void setAmount(double newAmount);
    void setDate(const std::string &newDate);
    void setNote(const std::string &newNote);

    // Shared validation used by both subclasses.
    bool isValidAmount() const;
    bool isValidDate() const;

    // Polymorphic hook: each subclass describes itself differently.
    virtual std::string getType() const = 0;
    virtual std::string describe() const;
};

#endif // UNIEXPENSE_TRANSACTION_H
