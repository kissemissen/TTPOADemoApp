# 🚀 TTPOA Demo App: SDK & POS Terminal Integration Demo App

This project is a demonstration application built to demo an example point-of-sale (POS) system, showcasing a payment integration with Adyen's Tap to Pay SDK, Bluetooth terminals and other network payment devices.

-----

## 💡 Background

The goal of this application is to serve as a demo for integrating mobile POS software with Adyen's payment services. It utilizes modern Android architecture components to handle state, data persistence, and dependency injection.

### Key Technologies:

  * **Language:** Kotlin
  * **UI:** **Jetpack Compose** (Declarative UI)
  * **Architecture:** MVVM with Use Cases
  * **Dependency Injection:** **Hilt**
  * **Data Persistence:** Room (`MenuItems`) and DataStore (`UserPreferences`)
  * **Networking:** Retrofit / OkHttp
  * **Payments:** Adyen Mobile SDK for Android (In-Person Payments/Terminal services)

-----

## 📱 App Functionality

This application simulates a merchant settings and cart management screen.

1.  **Menu Management:** Users can add, view, and delete menu items (products). Details are stored in a local **Room** database.
2.  **Image Configuration:** Users can set a custom logotype for the app bar and add thumbnails for menu items.
3.  **Adyen Configuration:** Users can configure essential terminal settings like **Currency Selection** and **Credential Setup**.
4.  **Payment Initiation:** Includes logic to initiate TTP sessions and clear existing sessions via the Adyen Mobile SDK.

**Quick Setup:** The easiest way to configure Adyen credentials is by using the **QR Code Scanner** in the settings menu.

-----

## 🔒 Setup and Configuration

To build and run this project, you must configure your Adyen API Key for dependency resolution and your terminal configuration details.

### 1\. SDK Dependency Resolution (`secrets.properties`)

The Adyen Terminal SDK is hosted on a private Maven repository that requires header authentication via an API key.

1.  **Create File:** In the **root directory** of your project (next to `settings.gradle.kts`), create a file named `secrets.properties`.

2.  **Add Key:** Paste the following content, replacing the placeholder with your actual key:

    ```properties
    # Paste this line in the file.
    # Always make sure that sensitive API keys are never uploaded to public repositories.
    TTPOA_SDK_API_KEY="YOUR_TTPOA_SDK_API_KEY_HERE"
    ```

    ⚠️ **Security Note:** This file is configured in `.gitignore` and **must never be committed** to Git.

3.  **Gradle Configuration:** The `settings.gradle.kts` file automatically reads this key to authenticate with the private Maven repository.

### 2\. Adyen Terminal Configuration (In-App)

Your terminal configuration is managed via the **Settings Screen**.

| Setting | Purpose | Persistence |
| :--- | :--- | :--- |
| **Merchant Account** | Your unique merchant account name. | DataStore |
| **Store ID** | The physical store identifier. | DataStore |
| **API Key** | The Live/Test API key for backend calls. | DataStore |

**QR Code JSON Format:**
If scanning, the JSON should look like this:

```json
{
"merchantAccount": "YourMerchantAccount_TEST",
"store": "YOUR_STORE_ID",
"apiKey": "YOUR_LIVE_OR_TEST_API_KEY_HERE"
}
```

-----

## 🛠️ Getting Started

1.  Clone the repository.
2.  Follow **Step 1** above to create the **`secrets.properties`** file.
3.  Open the project in Android Studio.
4.  Run a Gradle Sync.
5.  Build and run the app on a device or emulator.
6.  Navigate to the Settings screen to configure your Adyen Terminal details.
