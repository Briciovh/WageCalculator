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
SalaryConfig (state) → SalaryViewModel → SalaryResults (derived state)
                              ↓
               ConverterScreen  ←→  ConfigurationScreen
```

`SalaryViewModel` is the single source of truth. It holds a `SalaryConfig` `StateFlow` and a `SalaryResults` `StateFlow` that is recomputed on every config change via `updateConfig {}`. The ViewModel is scoped to the Activity (obtained once in `SalaryApp()` with `viewModel()`) and passed to both screens so they share the same instance.

### Key types (`data/model/SalaryConfig.kt`)

- **`Currency`** — enum of 15 American-continent currencies, each with `symbol` and `code`. Codes are ISO 4217 identifiers — not localizable, keep them in the enum.
- **`Frequency`** — enum of 6 pay periods (`HOURLY` … `YEARLY`). Each entry holds a `@StringRes val labelRes` pointing to its display string.
- **`SalaryConfig`** — user settings: `baseCurrency`, `targetCurrency`, `inputAmount`, `inputFrequency`, `hoursPerWeek`, `weeksPerYear`, `exchangeRate`. Exchange rate resets to `1.0` automatically when either currency changes.
- **`CurrencyPair`** — `base: Double` and `target: Double` (not USD/MXN — generic).
- **`SalaryResults`** — one `CurrencyPair` per period plus `annualHours`.

### Screens (`ui/screens/`)

**`ConverterScreen`** — read-only results view. Maintains `selectedPeriod: Frequency` as local UI state. The selected period is shown in a large `SpotlightCard` at the top; all other periods appear as tappable `ResultCard`s in a 2-column `LazyVerticalGrid`. Tapping a grid card promotes it to the spotlight. Extension functions `Frequency.spotlightTitle()` and `Frequency.unitLabel()` (both `@Composable`, private, defined at the bottom of the file) map periods to their string resources. `SalaryResults.pairFor(Frequency)` (also private, at bottom of file) maps a period to its `CurrencyPair`.

**`ConfigurationScreen`** — edits `SalaryConfig` via `viewModel.updateConfig {}`. Contains two `ExposedDropdownMenuBox` pickers for the currency pair and a manual exchange rate field whose label and prefix update dynamically from the selected currencies.

### Hilt wiring

- `WageCalculatorApp` — `@HiltAndroidApp`
- `MainActivity` — `@AndroidEntryPoint`
- `SalaryViewModel` — `@HiltViewModel` + `@Inject constructor()`
- Screen-level `@Preview` functions are removed; only stateless component previews (`SpotlightCard`, `ResultCard`) exist.

### String resources (`res/values/strings.xml`)

All user-visible strings live here — no inline string literals in composables. Dynamic strings use printf-style format args (`%1$s`, `%1$d`). Currency codes and symbols are not in strings.xml (they are ISO identifiers, not display copy).
