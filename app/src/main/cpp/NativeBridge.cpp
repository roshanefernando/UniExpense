#include <jni.h>
#include <string>
#include <vector>
#include "User.h"
#include "Expense.h"
#include "Income.h"
#include "Budget.h"
#include "Report.h"

// Small helper: convert a jstring to a std::string safely.
static std::string jstringToStd(JNIEnv *env, jstring jStr) {
    if (jStr == nullptr) return "";
    const char *chars = env->GetStringUTFChars(jStr, nullptr);
    std::string result(chars);
    env->ReleaseStringUTFChars(jStr, chars);
    return result;
}

extern "C" {

// ---------------- User ----------------

JNIEXPORT jboolean JNICALL
Java_com_example_uniexpense_NativeBridge_nativeIsValidName(JNIEnv *env, jobject, jstring name) {
    return User::isValidName(jstringToStd(env, name)) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL
Java_com_example_uniexpense_NativeBridge_nativeIsValidEmail(JNIEnv *env, jobject, jstring email) {
    return User::isValidEmail(jstringToStd(env, email)) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL
Java_com_example_uniexpense_NativeBridge_nativeIsValidPasswordLength(JNIEnv *env, jobject, jstring password) {
    return User::isValidPasswordLength(jstringToStd(env, password)) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL
Java_com_example_uniexpense_NativeBridge_nativeRegisterValidate(JNIEnv *env, jobject,
                                                                  jstring name, jstring email,
                                                                  jstring passwordHash) {
    User u(0, jstringToStd(env, name), jstringToStd(env, email),
           jstringToStd(env, passwordHash), "en");
    return u.registerUser() ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL
Java_com_example_uniexpense_NativeBridge_nativeLoginValidate(JNIEnv *env, jobject,
                                                               jstring inputHash, jstring storedHash) {
    User u(0, "", "", jstringToStd(env, inputHash), "en");
    return u.login(jstringToStd(env, storedHash)) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL
Java_com_example_uniexpense_NativeBridge_nativeUpdateProfileValidate(JNIEnv *env, jobject,
                                                                       jstring name, jstring email) {
    User u(0, "", "", "", "en");
    return u.updateProfile(jstringToStd(env, name), jstringToStd(env, email)) ? JNI_TRUE : JNI_FALSE;
}

// ---------------- Expense ----------------

JNIEXPORT jboolean JNICALL
Java_com_example_uniexpense_NativeBridge_nativeAddExpenseValidate(JNIEnv *env, jobject,
                                                                    jstring category, jdouble amount,
                                                                    jstring date) {
    Expense e(0, 0, jstringToStd(env, category), amount, jstringToStd(env, date), "");
    return e.addExpense() ? JNI_TRUE : JNI_FALSE;
}

// ---------------- Income ----------------

JNIEXPORT jboolean JNICALL
Java_com_example_uniexpense_NativeBridge_nativeAddIncomeValidate(JNIEnv *env, jobject,
                                                                   jstring source, jdouble amount,
                                                                   jstring date) {
    Income inc(0, 0, jstringToStd(env, source), amount, jstringToStd(env, date), "");
    return inc.addIncome() ? JNI_TRUE : JNI_FALSE;
}

// ---------------- Report / calculations ----------------

JNIEXPORT jdouble JNICALL
Java_com_example_uniexpense_NativeBridge_nativeCalculateTotal(JNIEnv *env, jobject, jdoubleArray amounts) {
    jsize len = env->GetArrayLength(amounts);
    if (len == 0) return 0.0;
    jdouble *elements = env->GetDoubleArrayElements(amounts, nullptr);
    double total = Report::calculateTotalExpense(elements, len);
    env->ReleaseDoubleArrayElements(amounts, elements, JNI_ABORT);
    return total;
}

JNIEXPORT jdouble JNICALL
Java_com_example_uniexpense_NativeBridge_nativeCalculateBalance(JNIEnv *, jobject,
                                                                  jdouble income, jdouble expense) {
    return Report::calculateBalance(income, expense);
}

// ---------------- Budget ----------------

JNIEXPORT jdoubleArray JNICALL
Java_com_example_uniexpense_NativeBridge_nativeCheckBudget(JNIEnv *env, jobject,
                                                             jdouble monthlyBudget, jdouble totalSpent) {
    Budget b(0, 0, monthlyBudget, 0, 0);
    BudgetStatus status = b.checkBudget(totalSpent);

    jdoubleArray result = env->NewDoubleArray(4);
    jdouble values[4] = {
            status.isSet ? 1.0 : 0.0,
            status.exceeded ? 1.0 : 0.0,
            status.remaining,
            status.percentageUsed
    };
    env->SetDoubleArrayRegion(result, 0, 4, values);
    return result;
}

} // extern "C"
