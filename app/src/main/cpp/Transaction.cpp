#include "Transaction.h"

Transaction::Transaction(long id, long userId, double amount, std::string date, std::string note)
    : id(id), userId(userId), amount(amount), date(std::move(date)), note(std::move(note)) {}

long Transaction::getId() const { return id; }
long Transaction::getUserId() const { return userId; }
double Transaction::getAmount() const { return amount; }
std::string Transaction::getDate() const { return date; }
std::string Transaction::getNote() const { return note; }

void Transaction::setAmount(double newAmount) { amount = newAmount; }
void Transaction::setDate(const std::string &newDate) { date = newDate; }
void Transaction::setNote(const std::string &newNote) { note = newNote; }

bool Transaction::isValidAmount() const {
    return amount > 0.0;
}

bool Transaction::isValidDate() const {
    // Expect YYYY-MM-DD (10 chars, dashes at 4 and 7). Full calendar validation
    // is handled by the Android DatePicker on the Kotlin side; this is a
    // defensive server-side-style check on the native layer.
    if (date.size() != 10) return false;
    if (date[4] != '-' || date[7] != '-') return false;
    for (int i = 0; i < 10; i++) {
        if (i == 4 || i == 7) continue;
        if (date[i] < '0' || date[i] > '9') return false;
    }
    return true;
}

std::string Transaction::describe() const {
    return getType() + ": " + std::to_string(amount) + " on " + date;
}
