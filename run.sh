#!/bin/bash

FX_LIB="/home/saifdemos/programs/javafx-sdk-25.0.2/lib"
JDBC_JAR="/home/saifdemos/programs/javafx-sdk-25.0.2/lib/postgresql-42.7.10.jar"
MODS="javafx.controls,javafx.fxml,java.sql"


echo "Running..."
java --module-path "$FX_LIB" --add-modules "$MODS" -cp ".:$JDBC_JAR" MainApp