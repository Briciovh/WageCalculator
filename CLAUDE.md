# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run

```bash
# Assemble debug APK
./gradlew assembleDebug

# Install and run on connected device/emulator
./gradlew installDebug

# Run unit tests
./gradlew test

# Run instrumented tests (requires device/emulator)
./gradlew connectedAndroidTest

# Run lint
./gradlew lint

# Run a single test class
./gradlew test --tests "com.softeen.wagecalculator.ExampleUnitTest"
```

## Architecture

Single-module Android app. Kotlin + Jetpack Compose + Hilt.

### Data flow

```
SalaryConfigDataStore ──────────────────────────────────────────────┐
Frankfurt API (ExchangeRateApi)                                      ↓
    ↓                                                         SalaryViewModel
NetworkExchangeRateRepository                              ┌──────────────────┐
    └──── ExchangeRateRepository ──────────────────────────► config: StateFlow
SalaryConfigDataStore                                      │ results: StateFlow
    └──── SalaryConfigRepository ─────────────────────────► isLoadingRate: SF
                                                           │ rateError: SF
                                                           └──────┬───────────┘
                                                                  ↓
                                             ConverterScreen  ←→  ConfigurationScreen
```

`SalaryViewModel` is the single source of truth. It holds a `SalaryConfig` StateFlow and a `SalaryResults` StateFlow that is recomputed on every config change via `updateConfig {}`. The ViewModel is scoped to the Activity (obtained once in `SalaryApp()` with `viewModel()`) and passed to both screens so they share the same instance.

### Key types (`data/model/SalaryConfig.kt`)

- **`Currency`** — enum of 15 American-continent currencies, each with `symbol` and `code`. Codes are ISO 4217 identifiers — not localizable, keep them in the enum.
- **`Frequency`** — enum of 6 pay periods (`HOURLY` … `YEARLY`). Each entry holds a `@StringRes val labelRes` pointing to its display string.
- **`SalaryConfig`** — user settings: `baseCurrency`, `targetCurrency`, `inputAmount`, `inputFrequency`, `hoursPerWeek`, `weeksPerYear`, `exchangeRate`. Exchange rate resets to `1.0` automatically when either currency changes. Has `annualHours` computed property and `calculateYearlyAmount()`.
- **`CurrencyPair`** — `base: Double` and `target: Double` (not USD/MXN — generic).
- **`SalaryResults`** — one `CurrencyPair` per period plus `annualHours`.

### ViewModel state (`ui/SalaryViewModel.kt`)

Beyond `config` and `results`, the ViewModel also exposes:
- `isLoadingRate: StateFlow<Boolean>` — true while a network exchange rate fetch is in flight.
- `rateError: StateFlow<String?>` — error message from the last failed fetch; cleared on next successful fetch.

Currency changes trigger a network fetch via `ExchangeRateRepository`. Same-currency changes skip the fetch and set the rate to `1.0`. Rates are cached in DataStore with keys of the form `rate_{base}_{target}` and applied immediately (no loading spinner) while a background refresh runs.

### Repository layer (`data/repository/`)

Two interfaces, each with one implementation:

- **`SalaryConfigRepository`** → `SalaryConfigDataStore` — persists the full `SalaryConfig` to Android DataStore (Preferences), keyed under "salary_config". Also stores/retrieves cached exchange rates.
- **`ExchangeRateRepository`** → `NetworkExchangeRateRepository` — wraps the Frankfurt API; returns `Result<Double>`. Re-throws `CancellationException` to preserve coroutine cancellation semantics.

### Network layer (`data/network/`)

- **`ExchangeRateApi`** — Retrofit interface. Single endpoint: `GET /v2/rates?base={base}&quotes={quote}` against `https://api.frankfurter.dev/`. Returns `List<ExchangeRateDto>`.
- **`ExchangeRateDto`** — Kotlinx Serializable DTO (`date`, `base`, `quote`, `rate`).

### DI (`di/`)

- **`DataStoreModule`** — `@Singleton`; provides `DataStore<Preferences>` and binds `SalaryConfigDataStore` → `SalaryConfigRepository`.
- **`NetworkModule`** — `@Singleton`; provides `Json` (ignoreUnknownKeys, coerceInputValues), `OkHttpClient` (logging in DEBUG), `Retrofit` (kotlinx-serialization converter), and binds `NetworkExchangeRateRepository` → `ExchangeRateRepository`.

### Hilt wiring

- `WageCalculatorApp` — `@HiltAndroidApp`
- `MainActivity` — `@AndroidEntryPoint`
- `SalaryViewModel` — `@HiltViewModel` + `@Inject constructor()`

### Navigation (`MainActivity.kt`)

Compose `NavHost` with two routes:
- `"converter"` (start destination) → `ConverterRoute`
- `"config"` → `ConfigurationRoute`

Each route is a stateful function that collects StateFlows and calls the corresponding stateless `Screen` composable. This Route/Screen split keeps stateless composables previewable.

### Screens (`ui/screens/`)

**`ConverterScreen`** — read-only results view. Maintains `selectedPeriod: Frequency` as local UI state. The selected period is shown in a large `SpotlightCard` at the top; all other periods appear as tappable `ResultCard`s in a 2-column `LazyVerticalGrid`. Tapping a grid card promotes it to the spotlight. Extension functions `Frequency.spotlightTitle()` and `Frequency.unitLabel()` (both `@Composable`, private, defined at the bottom of the file) map periods to their string resources. `SalaryResults.pairFor(Frequency)` (also private, at bottom of file) maps a period to its `CurrencyPair`.

**`ConfigurationScreen`** — edits `SalaryConfig` via `viewModel.updateConfig {}`. Contains two `ExposedDropdownMenuBox` pickers for the currency pair, input amount + frequency fields, hours/weeks sliders (0–168 hrs, 1–52 weeks), and a manual exchange rate field. The exchange rate field's label and prefix update dynamically; increment/decrement step size auto-adjusts (0.01 / 0.1 / 1.0) based on the current rate magnitude.

### String resources (`res/values/strings.xml`)

All user-visible strings live here — no inline string literals in composables. Dynamic strings use printf-style format args (`%1$s`, `%1$d`). Currency codes and symbols are not in strings.xml (they are ISO identifiers, not display copy).

### Testing (`src/test/`)

Unit tests use `FakeSalaryConfigRepository` and `FakeExchangeRateRepository` (in `fake/`) to isolate the ViewModel. `MainDispatcherRule` (in `util/`) is a JUnit rule that installs a `TestCoroutineDispatcher` for all tests. `NetworkExchangeRateRepositoryTest` uses MockWebServer.

## Rules

### Dependency versions — no silent downgrades
Never downgrade a library or plugin version unless the downgrade is strictly necessary to resolve a conflict **and** the user has explicitly confirmed it. If a version conflict arises, propose the resolution and wait for approval before changing any version numbers. All versions are centralized in `gradle/libs.versions.toml`.

### Composable previews — never delete
`@Preview` functions are required on all composable components and must not be removed. Do not delete, comment out, or otherwise disable existing `@Preview` functions. If a preview stops compiling after a refactor, fix it — do not remove it.

### After every code change — sync, build, test
After finishing any code change, run the following three steps in order and report the results:
1. **Gradle sync** — `./gradlew --dry-run` (or trigger sync via the IDE)
2. **Build** — `./gradlew assembleDebug`
3. **Unit tests** — `./gradlew test`

Do not report a task as complete until all three steps pass without errors.
