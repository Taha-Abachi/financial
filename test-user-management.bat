@echo off
echo Testing User Management System
echo =============================
echo.

echo 1. Initializing default roles...
curl -X POST "http://localhost:8080/api/v1/user-roles/initialize-defaults" -H "Content-Type: application/json"
echo.
echo.

echo 2. Initializing default super admin...
curl -X POST "http://localhost:8080/api/v1/users/initialize-super-admin" -H "Content-Type: application/json"
echo.
echo.

echo 3. Getting all roles...
curl -X GET "http://localhost:8080/api/v1/user-roles/list"
echo.
echo.

echo 4. Getting all users...
curl -X GET "http://localhost:8080/api/v1/users/list"
echo.
echo.

echo 5. Getting user statistics...
curl -X GET "http://localhost:8080/api/v1/users/statistics"
echo.
echo.

echo Test completed!
pause
