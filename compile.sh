#!/bin/bash


FX_LIB="/home/saifdemos/programs/javafx-sdk-25.0.2/lib"
JDBC_JAR="/home/saifdemos/programs/javafx-sdk-25.0.2/lib/postgresql-42.7.10.jar"
MODS="javafx.controls,javafx.fxml,java.sql"

echo "Compiling everything..."
javac --module-path "$FX_LIB" --add-modules "$MODS" -cp "$JDBC_JAR" $(find . -name "*.java")

if [ $? -eq 0 ]; then
    echo "Done! files are compiled."
else
    echo "Compile failed!"
fi