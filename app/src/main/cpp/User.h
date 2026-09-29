#ifndef UNIEXPENSE_USER_H
#define UNIEXPENSE_USER_H

#include <string>

// User holds a logged-in/in-memory representation of an account and performs
// the *validation* side of registration/login/profile updates. Actual
// persistence (reading/writing SQLite) stays on the Kotlin side; this class
// is deliberately storage-agnostic, which is what makes it testable in pure
// C++ and reusable regardless of the database.
class User {
private:
    long id;
    std::string name;
    std::string email;
    std::string passwordHash;
    std::string language;

    static bool isValidEmailFormat(const std::string &email);

public:
    User(long id, std::string name, std::string email, std::string passwordHash, std::string language);

    // Getters
    long getId() const;
    std::string getName() const;
    std::string getEmail() const;
    std::string getPasswordHash() const;
    std::string getLanguage() const;

    // Setters
    void setName(const std::string &newName);
    void setEmail(const std::string &newEmail);
    void setPasswordHash(const std::string &newHash);
    void setLanguage(const std::string &newLanguage);

    // Business logic
    bool registerUser() const;                               // validates fields for a new account
    bool login(const std::string &storedHash) const;          // compares this.passwordHash to storedHash
    bool updateProfile(const std::string &newName, const std::string &newEmail) const; // validates edit
    bool logout() const;                                      // always succeeds locally; kept for symmetry with UML
    void setLanguageCode(const std::string &code);

    static bool isValidName(const std::string &name);
    static bool isValidEmail(const std::string &email);
    static bool isValidPasswordLength(const std::string &plainPassword);
};

#endif // UNIEXPENSE_USER_H
