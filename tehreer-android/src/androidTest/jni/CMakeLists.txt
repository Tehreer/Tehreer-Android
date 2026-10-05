cmake_minimum_required(VERSION 3.22.1)
project(tehreer_test C CXX)

set(MAIN_PATH ${CMAKE_CURRENT_LIST_DIR}/../../main/jni)

add_subdirectory(${MAIN_PATH} ${CMAKE_CURRENT_BINARY_DIR}/main)

add_library(testjni SHARED Memory.cpp Test.cpp)
target_include_directories(testjni PRIVATE ${MAIN_PATH})
target_link_libraries(testjni PRIVATE tehreerjni atomic android jnigraphics log)
