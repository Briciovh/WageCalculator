# 💸 WageCalculator

[![Kotlin](https://img.shields.io/badge/kotlin-2.0.0-blue.svg?logo=kotlin)](http://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Compose-1.7.0-green.svg?logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![Hilt](https://img.shields.io/badge/Dagger-Hilt-orange.svg)](https://dagger.dev/hilt/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

**WageCalculator** is a modern, sleek Android application designed to help you calculate and compare your salary across different currencies and pay frequencies. Whether you're a freelancer, a global contractor, or just curious about your earnings in another currency, WageCalculator has you covered.

## ✨ Features

- 🌍 **Global Reach:** Supports 15 major currencies across the American continent.
- 🔄 **Real-time Conversion:** Integrated with the [Frankfurter API](https://api.frankfurter.dev/) for up-to-date exchange rates.
- ⚡ **Instant Calculations:** View your earnings across 6 frequencies: Hourly, Daily, Weekly, Bi-weekly, Monthly, and Yearly.
- 🛠️ **Fully Customizable:** Adjust your work hours per week and weeks per year to fit your specific contract.
- 💾 **Offline First:** Local caching of exchange rates via DataStore for when you're on the go.
- 🎨 **Material 3 Design:** A beautiful, responsive UI built entirely with Jetpack Compose.
- 🔢 **Manual Overrides:** Manually adjust exchange rates with a smart increment/decrement system.

## 🛠 Tech Stack

- **Language:** [Kotlin](https://kotlinlang.org/)
- **UI Framework:** [Jetpack Compose](https://developer.android.com/jetpack/compose)
- **Dependency Injection:** [Hilt](https://developer.android.com/training/dependency-injection/hilt-android)
- **Asynchronous Programming:** [Coroutines](https://kotlinlang.org/docs/reference/coroutines-overview.html) & [Flow](https://kotlinlang.org/docs/reference/coroutines/flow.html)
- **Data Persistence:** [Jetpack DataStore (Preferences)](https://developer.android.com/topic/libraries/architecture/datastore)
- **Networking:** [Retrofit](https://square.github.io/retrofit/) + [Kotlinx Serialization](https://github.com/Kotlin/kotlinx.serialization)
- **Architecture:** Clean Architecture with MVVM pattern.

## 🏗 Architecture

The app follows a modern Android architecture, emphasizing separation of concerns and testability.

- **UI Layer:** Composed of stateless Screens and stateful Routes. `SalaryViewModel` serves as the single source of truth.
- **Domain/Data Layer:** Repositories handle data orchestration between the network (Retrofit) and local persistence (DataStore).
- **Navigation:** Uses Compose Navigation for seamless transitions between the **Converter** (results) and **Configuration** (settings) screens.

## 🚀 Getting Started

### Prerequisites

- Android Studio Koala or newer.
- JDK 17.
- An Android device or emulator running API 24+.

### Build & Run

1. Clone the repository:
   ```bash
   git clone https://github.com/your-username/WageCalculator.git
   ```
2. Open the project in Android Studio.
3. Build the project:
   ```bash
   ./gradlew assembleDebug
   ```
4. Run the app:
   ```bash
   ./gradlew installDebug
   ```

## 🧪 Testing

The project includes a robust testing suite:

```bash
# Run unit tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest
```

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---
*Built with ❤️ by the Softeen team.*
