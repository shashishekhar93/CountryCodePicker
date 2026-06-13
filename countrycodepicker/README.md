# CountryCodePicker

CountryCodePicker (CCP) is an efficient Android library which provides an easy way to search and select country phone code for a telephone number.

## Screenshots

<!-- Add your screenshots here -->

## Installation

To get a Git project into your build using [JitPack](https://jitpack.io):

### Step 1. Add the JitPack repository
Add it in your `settings.gradle.kts` at the end of repositories:

```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

### Step 2. Add the dependency
Add the following to your app module's `build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.github.shashishekhar93:CountryCodePicker:1.0.1")
}
```

## Features
*   **Easy to use**: Simple integration via XML or Kotlin/Java.
*   **Automatic Formatting**: Automatically formats phone numbers as you type.
*   **Validation**: Built-in phone number validation using Google's `libphonenumber`.
*   **Auto-detection**: Detects user's country using SIM, Network, or Locale.
*   **Customization**: Extensive styling options for flags, colors, fonts, and the selection dialog.
*   **Language Support**: Supports over 40 languages.
*   **Premium Empty State**: Beautiful "No results found" state with a "Request Country" option.

## Usage

### 1. XML Integration
Add `CountryCodePicker` to your layout:

```xml
<com.smcoding.countrycodepicker.CountryCodePicker
    android:id="@+id/ccp"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    app:ccp_showNameCode="true"
    app:ccp_autoFormatNumber="true"
    app:ccp_hintExampleNumber="true" />
```

### 2. Attach EditText
To enable automatic formatting and validation, attach an `EditText`:

```kotlin
val ccp = findViewById<CountryCodePicker>(R.id.ccp)
val editText = findViewById<EditText>(R.id.editText_phone)

ccp.editTextRegisteredCarrierNumber = editText
```

## Public API Reference

### Core Properties & Methods

| Property / Method | Description |
| :--- | :--- |
| `selectedCountry` | Get or set the currently selected country (`CCPCountry`). |
| `fullNumber` | Get or set the full phone number (e.g., "919876543210"). |
| `isValidFullNumber` | Returns `true` if the entered number is valid for the selected country. |
| `selectedCountryCode` | Returns the country code without plus (e.g., "91"). |
| `selectedCountryCodeWithPlus` | Returns the country code with plus (e.g., "+91"). |
| `selectedCountryName` | Returns the selected country name. |
| `selectedCountryNameCode` | Returns the ISO code (e.g., "IN"). |
| `setAutoDetectedCountry(bool)` | Attempts to auto-detect country based on SIM/Network/Locale. |
| `launchCountrySelectionDialog()` | Manually opens the country selection dialog. |

### Configuration Properties

| Property | Default | Description |
| :--- | :--- | :--- |
| `showNameCode` | `true` | Shows ISO code in the picker (e.g., "IN"). |
| `showPhoneCode` | `true` | Shows dialing code in the picker (e.g., "+91"). |
| `showFlag` | `true` | Shows country flag. |
| `ccpUseEmoji` | `false` | Uses emoji flags instead of PNG images. |
| `numberAutoFormattingEnabled` | `true` | Enables real-time phone number formatting. |
| `hintExampleNumberEnabled` | `false` | Shows an example number as the EditText hint. |
| `rememberLastSelection` | `false` | Saves user's selection in SharedPreferences. |

### Listeners

```kotlin
// Listen for country changes
ccp.onCountryChangeListener = object : CountryCodePicker.OnCountryChangeListener {
    override fun onCountrySelected() {
        Log.d("CCP", "Selected: ${ccp.selectedCountryName}")
    }
}

// Listen for phone number validity
ccp.phoneNumberValidityChangeListener = object : CountryCodePicker.PhoneNumberValidityChangeListener {
    override fun onValidityChanged(isValidNumber: Boolean) {
        Log.d("CCP", "Is valid: $isValidNumber")
    }
}
```

## Premium Features

### GitHub Country Requests
If a user can't find their country in the search dialog, you can enable a one-line integration to allow them to report it directly to your GitHub repository:

```kotlin
ccp.enableGithubCountryRequests()
```
This adds a "Request Country" button to the search results which redirects users to your Issues page with pre-filled search data.

## Customization Attributes (XML)
Check `attrs.xml` for a full list of attributes including:
*   `ccp_contentColor`: Change text color of selected country.
*   `ccp_arrowColor`: Change dropdown arrow color.
*   `ccpDialog_backgroundColor`: Change dialog background.
*   `ccpDialog_textColor`: Change dialog text color.
*   `ccpDialog_showCloseIcon`: Show an 'X' button in the dialog.
*   `ccp_defaultLanguage`: Set the library language (e.g., `HINDI`, `FRENCH`).
