cmake_minimum_required(VERSION 3.22.1)
project("uniexpense")

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

add_library(
        uniexpense_native
        SHARED
        NativeBridge.cpp
        Transaction.cpp
        User.cpp
        Expense.cpp
        Income.cpp
        Budget.cpp
        Report.cpp
)

find_library(
        log-lib
        log
)

target_link_libraries(
        uniexpense_native
        ${log-lib}
)
