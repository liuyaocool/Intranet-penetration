rem 执行完成后关闭 /c
rem 执行完成后不关闭 /k
rem 使用“^”换行后不能有空格

set my_mvn_home=
set ab_path=%cd%

%my_mvn_home%mvn clean && %my_mvn_home%mvn install && ^
start cmd /c "cd .\server && %my_mvn_home%mvn assembly:assembly && ping 127.1.1.1 >nul" && ^
start cmd /c "cd .\client && %my_mvn_home%mvn assembly:assembly && ping 127.1.1.1 -n 2 >nul" && ^
pause

