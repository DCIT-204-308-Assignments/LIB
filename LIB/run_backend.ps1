$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-17.0.20.101-hotspot"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
Set-Location "C:\Users\HP\OneDrive\Desktop\DCIT-204-308-Assignments\LIB"
java -cp "bin;sqlite-jdbc-3.42.0.0.jar" UGSwiftApp
