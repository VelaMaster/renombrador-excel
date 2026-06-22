#!/usr/bin/env bash
# Construye renombrador-excel.jar en macOS / Linux.
# Requisitos:
#   - JDK 17+  (brew install --cask temurin@17)
#   - Maven    (brew install maven)

set -e
cd "$(dirname "$0")"

command -v javac >/dev/null || { echo "ERROR: instala un JDK 17+ (brew install --cask temurin@17)"; exit 1; }
command -v mvn   >/dev/null || { echo "ERROR: instala Maven (brew install maven)"; exit 1; }

echo "== Compilando con Maven =="
mvn -q clean package

cp -f target/renombrador-excel.jar ./renombrador-excel.jar

echo
echo "Listo. Para correr la app:"
echo "   java -jar renombrador-excel.jar"
