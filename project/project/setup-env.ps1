her# Setup environment variables for the project
# This script sets up JAVA_HOME and other necessary environment variables

$env:JAVA_HOME = "D:\java_kit"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"

Write-Host "Environment configured:" -ForegroundColor Green
Write-Host "JAVA_HOME = $env:JAVA_HOME"
Write-Host "Java Version:"
& "$env:JAVA_HOME\bin\java" -version

