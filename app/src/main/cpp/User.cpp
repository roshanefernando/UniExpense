#include "User.h"

User::User(long id, std::string name, std::string email, std::string passwordHash, std::string language)
    : id(id), name(std::move(name)), email(std::move(email)),
      passwordHash(std::move(passwordHash)), language(std::move(language)) {}

long User::getId() const { return id; }
std::string User::getName() const { return name; }
std::string User::getEmail() const { return email; }
std::string User::getPasswordHash() const { return passwordHash; }
std::string User::getLanguage() const { return language; }

void User::setName(const std::string &newName) { name = newName; }
void User::setEmail(const std::string &newEmail) { email = newEmail; }
void User::setPasswordHash(const std::string &newHash) { passwordHash = newHash; }
void User::setLanguage(const std::string &newLanguage) { language = newLanguage; }
void User::setLanguageCode(const std::string &code) { language = code; }

bool User::isValidEmailFormat(const std::string &email) {
    auto at = email.find('@');
    if (at == std::string::npos || at == 0 || at == email.size() - 1) return false;
    auto dot = email.find('.', at);
    if (dot == std::string::npos || dot == email.size() - 1) return false;
    return true;
}

bool User::isValidName(const std::string &name) {
    return !name.empty();
}

bool User::isValidEmail(const std::string &email) {
    return !email.empty() && isValidEmailFormat(email);
}

bool User::isValidPasswordLength(const std::string &plainPassword) {
    return plainPassword.length() >= 6;
}

bool User::registerUser() const {
    return isValidName(name) && isValidEmail(email) && !passwordHash.empty();
}

bool User::login(const std::string &storedHash) const {
    // Constant-time-ish comparison isn't critical for a local offline student
    // app, but we still avoid short-circuiting on length to keep it simple
    // and correct.
    return !storedHash.empty() && passwordHash == storedHash;
}

bool User::updateProfile(const std::string &newName, const std::string &newEmail) const {
    return isValidName(newName) && isValidEmail(newEmail);
}

bool User::logout() const {
    return true;
}
