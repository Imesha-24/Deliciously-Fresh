# 🍏 DeliciouslyFresh

<p align="center">
  <b>A modern Android application for ordering fresh, organic fruits and produce with seamless delivery and secure payments.</b>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android-green.svg?style=flat&logo=android" alt="Platform" />
  <img src="https://img.shields.io/badge/Min%20SDK-24-orange.svg" alt="Min SDK" />
  <img src="https://img.shields.io/badge/Target%20SDK-36-blue.svg" alt="Target SDK" />
  <img src="https://img.shields.io/badge/Language-Java-brightgreen.svg?logo=java" alt="Language" />
  <img src="https://img.shields.io/badge/Firebase-Supported-FFCA28.svg?logo=firebase" alt="Firebase" />
  <img src="https://img.shields.io/badge/Payment-PayHere-blue.svg" alt="PayHere" />
</p>

---

## 📖 Overview

**DeliciouslyFresh** is a feature-packed native Android e-commerce application tailored for purchasing fresh fruits and groceries directly from your mobile device. Built with modern Android architecture and backed by Firebase cloud services and local SQLite caching, DeliciouslyFresh provides a fast, smooth, and delightful shopping experience.

---

## ✨ Features

- 🔐 **Authentication & User Management**
  - Secure registration and sign-in powered by Firebase Authentication.
  - User profile management with profile picture upload via Firebase Storage.
  - Session persistence and secure credential handling.

- 🛍️ **Product Catalog & Categorization**
  - Explore categorized selections of fresh fruits and organic produce.
  - Rich product details including pricing, descriptions, stock, and high-resolution images.
  - Fast search and filter capabilities.

- 🛒 **Cart & Wishlist**
  - Real-time cart calculation and item quantity management.
  - Wishlist system to bookmark favorite products for later purchase.

- 💳 **Seamless Payments & Checkout**
  - Integrated with **PayHere Payment Gateway** for secure mobile transactions.
  - Detailed checkout flow with delivery address configuration.

- 🗺️ **Location & Maps Integration**
  - **Google Maps API** integration for selecting and pinpointing delivery locations.

- 📦 **Order Tracking & Management**
  - View real-time order history and delivery statuses.

- 💬 **Messaging & Push Notifications**
  - Real-time customer support / in-app messaging.
  - **Firebase Cloud Messaging (FCM)** for instant alerts on deals, offers, and order status updates.

- ⚡ **Offline Caching & Performance**
  - SQLite local database helpers (`FruitDatabaseHelper`, `CategoryDatabaseHelper`) for offline viewing and fast load times.
  - Image caching and asynchronous rendering powered by **Glide**.

---

## 🛠️ Tech Stack & Libraries

| Category | Technologies / Libraries |
| :--- | :--- |
| **Language & Platform** | Java, Android SDK (Min SDK: 24, Target SDK: 36) |
| **UI Components** | Material Design 3 Components, ViewBinding, Navigation Drawer, Bottom Navigation |
| **Backend & Cloud** | Firebase Authentication, Cloud Firestore, Firebase Storage, Firebase Messaging (FCM), Firebase Analytics |
| **Local Database** | SQLite Database |
| **Payment Gateway** | PayHere Android SDK (`v3.0.17`) |
| **Maps & Location** | Google Play Services Maps (`18.2.0`) |
| **Image Loading** | Glide (`com.github.bumptech.glide:glide`) |
| **Utilities** | Project Lombok |

---

## 📂 Project Structure

```text
DeliciouslyFresh/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/lk/iu/deliciously_fresh/
│   │   │   │   ├── activity/        # App activities (Splash, SignIn, SignUp, MainActivity)
│   │   │   │   ├── adapter/         # RecyclerView adapters for products, categories, cart, etc.
│   │   │   │   ├── db/              # SQLite database helpers for local caching
│   │   │   │   ├── fragment/        # UI screens (Home, Category, Cart, Checkout, Profile, etc.)
│   │   │   │   ├── model/           # Data models (User, Fruit, CartItem, Order, Message)
│   │   │   │   ├── service/         # Background services (FCM Service)
│   │   │   │   └── util/            # Helper classes & utilities
│   │   │   └── res/                 # Layouts, drawables, navigation, values, styles
│   │   └── build.gradle             # App-level build configurations & dependencies
├── gradle/                          # Gradle wrapper & version catalogs
├── build.gradle                     # Project-level build configuration
└── settings.gradle                  # Gradle settings
```

---

## 🚀 Getting Started

### Prerequisites

- **Android Studio** (Koala / Ladybug or newer recommended)
- **JDK 11** or higher
- **Android Device or Emulator** running Android 7.0 (API Level 24) or higher
- A **Firebase Project** configured with Firestore, Auth, Storage, and Messaging
- A **Google Maps API Key**

### Installation & Setup

1. **Clone the repository:**
   ```bash
   git clone https://github.com/Imesha-24/Deliciously-Fresh.git
   cd Deliciously-Fresh
   ```

2. **Set up Firebase:**
   - Go to the [Firebase Console](https://console.firebase.google.com/).
   - Add an Android app with package name `lk.iu.deliciously_fresh`.
   - Download the generated `google-services.json` file and place it inside the `app/` directory:
     ```text
     DeliciouslyFresh/app/google-services.json
     ```

3. **Configure API Keys:**
   - Add your Google Maps API key in `app/src/main/AndroidManifest.xml`:
     ```xml
     <meta-data
         android:name="com.google.android.geo.API_KEY"
         android:value="YOUR_GOOGLE_MAPS_API_KEY" />
     ```
   - Configure your PayHere credentials in your payment configuration files or checkout flow.

4. **Build & Run:**
   - Open the project in **Android Studio**.
   - Let Gradle sync all dependencies.
   - Run the application on an emulator or a connected physical Android device by pressing `Shift + F10` or clicking the **Run** button.

---

## 📱 Screenshots

<!-- Add screenshots here if available -->
<p align="center">
  <i>(Add screenshots or mockups of Splash, Home, Cart, and Checkout screens here)</i>
</p>

---

## 📄 License

This project is developed for educational and portfolio purposes.

---

<p align="center">Made with ❤️ for fresh and healthy living.</p>
