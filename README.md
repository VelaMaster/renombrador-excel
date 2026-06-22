# Renombrador a Excel

Pequeña app de escritorio (Java/Swing) que escanea una carpeta con PDFs cuyos
nombres siguen el patrón:

```
<prefijo>_PE <N>_<NNNNN>...
```

ejemplo: `210_PE 1_47800.pdf`, `ABC_PE 12_89000.pdf`

Y genera un Excel `.xlsx` con dos renglones:

```
fila 1:  1    2    3    ...   (el número que va después de "PE ")
fila 2:  47800 89000 234 ...   (el número que va después del segundo "_")
```

Una columna por archivo, en el orden alfabético del nombre.

## Cómo se usa

1. Doble click en `RenombradorExcel.exe` (o `java -jar renombrador-excel.jar`).
2. Elige la carpeta raíz con los PDFs.
3. Elige dónde guardar el Excel.
4. Aparece un mensaje con el total procesado y los archivos ignorados (si los hay).

## Cómo construirlo (Windows)

Requisitos:

- **JDK 17+** (incluye `javac` y `jpackage`). Recomendado: Eclipse Temurin 17.
- **Maven** (`mvn` en el PATH).

Pasos:

```bat
build.bat
```

Esto produce:

- `renombrador-excel.jar` — fat JAR con todas las dependencias (Apache POI incluido).
- `dist\RenombradorExcel\RenombradorExcel.exe` — ejecutable nativo de Windows con su propio runtime de Java (no necesita Java instalado en la máquina destino).

## Estructura

```
renombrador excel/
├── build.bat
├── pom.xml
├── README.md
└── src/main/java/com/renombrador/Main.java
```

## Patrón aceptado

Regex usada: `^[^_]+_PE\s+(\d+)_(\d+)` (case-insensitive). Cualquier cosa antes
del primer `_` se ignora, así que sirve para `210_…`, `ABC_…`, etc.
# renombrador-excel
