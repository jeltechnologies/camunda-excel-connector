# Excel Connector for Camunda
 
A Camunda connector that enables BPMN processes to read data directly from Excel files (.xlsx). Load spreadsheet data at runtime — from a Camunda Document Reference — and map the results into your process variables.

<img width="1616" height="948" alt="image" src="https://github.com/user-attachments/assets/dd402795-c62f-45c9-a848-bb7dc98d08e6" />
Configure the connector in Camunda Modeler

---


<img width="1169" height="672" alt="image" src="https://github.com/user-attachments/assets/659ff80e-1d11-4530-a841-00d908f35901" />
Excel file parsed into process instance variables

---

## Features 
- Load Excel files from a **public URL** or a **Camunda Document Reference**
- Five read operations covering common Excel data access patterns
- Target a specific worksheet by name, or default to the first sheet
- Control how empty cells and rows are included in the output
- Map results directly into process variables via result variable or FEEL expression
 
## Supported Operations
| Operation | Description |
|---|---|
| **Get table from range** | This mimics how people normally use Excel; tables with a headers row. Therefore this is **the most common way to use this Connector**. <br>The table is returned an array of JSON objects. Each object gets its attribute names from first row of the range.
| **Get file contents** | Returns the full contents of the Excel file |
| **Get sheet contents** | Returns all data from a specific sheet (tab) |
| **Get cell by address** | Returns the value of a single cell by address (e.g. `A1`) |
| **Get cell by coordinates** | Returns the value of a single cell by 1-based row and column numbers |

## File Sources

| Source | Description |
|--------|-------------|
| **Document reference** | A Camunda document uploaded to the process, for example for handling email attachments |
| **URL** | URL used to GET the Excel file by HTTP(s), useful mainly for testing |

## Output Settings

| Setting | Options |
|---------|---------|
| **Cell info** | Values only - only the contents <br>Values with address metadata - the contents and coordinates of each cell|
| **Empty cells** | Return as null<br>Return as empty string<br>Exclude - skip empty cells |
| **Empty rows** | Include all rows <br> Skip rows without values - Only rows that contain a value will generate a JSON row object |

## Cell value conversions

| Excel type | JSON output |
|------------|-------------|
| Whole number (e.g. `8`) | Integer: `8` |
| Decimal number (e.g. `618.83`) | Decimal: `618.83` |
| Date / time | ISO 8601 string: `2024-03-15T00:00:00.000+0000` |
| Text | String |
| Boolean | `true` / `false` |
| Formula | Evaluated result using the rules above |
| Formula error | `"ERROR IN FORMULA"` |
| Blank | `""`, `null`, or omitted — controlled by the **Empty cells** output setting |

## Installation

### Prerequisites

- Camunda 8 self-managed (tested on 8.9 with MicroK8s)
- Java 21
- Maven 3.9+ (or use the included `./mvnw` wrapper)

### Build

```bash
./mvnw clean package
```

The fat JAR is written to `target/camunda-excel-connector-*.jar`.

### Deploy to a self-managed cluster

The connector JAR is loaded at runtime via `LOADER_PATH` — no container image rebuild needed.

1. Copy the JAR to the directory mounted by the connectors pod (e.g. `~/camunda-connectors`).
2. Restart the connectors deployment:

```bash
kubectl rollout restart deployment/<release>-connectors -n <namespace>
```

Or use the included Ant script, which builds, packages, and generates the deploy script in one step:

```bash
ant
cd dist && ./deploy.sh
```

The script honours two environment variables:

| Variable | Default | Description |
|----------|---------|-------------|
| `CAMUNDA_RELEASE` | `camunda` | Helm release name |
| `CAMUNDA_NAMESPACE` | `camunda` | Kubernetes namespace |

### Install the element template

Import `dist/element-templates/excel-connector.json` into Camunda Modeler via
**File → Import element templates** (or drop it into the Modeler templates folder).

## Usage in Modeler

Add a **Service Task** to your BPMN diagram and apply the **Excel Connector** element template. Configure:

1. **File Settings** — choose URL or Document and provide the file location
2. **Operation** — select what to read
3. **Output Settings** — control how empty cells and rows are handled
4. **Result variable** — map the connector output into a process variable

### Example: read a table from a range

| Property | Value |
|----------|-------|
| File source | URL |
| File URL | `http://myhost/data.xlsx` |
| Operation | Get table from range |
| Sheet name | `Sheet1` |
| Range | `A1:E100` |
| Result variable | `tableData` |

The connector returns a list of objects. If row 1 contains `CustomerID`, `Product`, `Total`, each subsequent row becomes `{"customer_id": ..., "product": ..., "total": ...}`.

## Tech Stack

| Component | Version |
|-----------|---------|
| Java | 21 |
| Spring Boot | 4.0.5 |
| Camunda Connector SDK | 8.8.0 |
| Apache POI | 5.4.1 |
| Jackson | 2.19.2 |

## Building from source

```bash
# Run tests
./mvnw test

# Build fat JAR
./mvnw clean package

# Regenerate the Camunda Modeler element template
./mvnw process-classes -Pgenerate-templates
```

## License

Apache License 2.0
