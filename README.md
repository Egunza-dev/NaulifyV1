# Naulify PSV Agent App

A mobile application for PSV agents/owners to manage fare collections using the Naulify platform.

## Features

- Authentication with email/password and Google Sign-In
- Email verification
- Profile and vehicle management
- Route management
- QR code generation for fare collection
- Transaction reports and analytics

## Setup Instructions

### Prerequisites

- Android Studio Arctic Fox or later
- JDK 17
- Android SDK 33 or later
- Firebase account

### Firebase Setup

1. Go to the [Firebase Console](https://console.firebase.google.com/)
2. Create a new project named "Naulify"
3. Add an Android app with package name "com.naulify.agent"
4. Download the `google-services.json` file and replace the placeholder in `app/google-services.json`
5. Enable the following Firebase services:
   - Authentication (Email/Password and Google Sign-in)
   - Cloud Firestore
   - Cloud Functions

### Firebase Authentication Setup

1. In the Firebase Console, go to Authentication > Sign-in method
2. Enable Email/Password authentication
3. Enable Google Sign-in
4. Configure the OAuth consent screen in the Google Cloud Console
5. Add your SHA-1 fingerprint to the Firebase project settings

### Firebase Security Rules

Add the following security rules to your Firestore database:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // User profiles
    match /users/{userId} {
      allow read: if request.auth != null && request.auth.uid == userId;
      allow write: if request.auth != null && request.auth.uid == userId;
    }
    
    // Vehicles
    match /vehicles/{vehicleId} {
      allow read: if request.auth != null;
      allow write: if request.auth != null && 
                   request.resource.data.ownerId == request.auth.uid;
    }
    
    // Routes
    match /routes/{routeId} {
      allow read: if request.auth != null;
      allow write: if request.auth != null && exists(/databases/$(database)/documents/vehicles/$(request.resource.data.vehicleId)) &&
                   get(/databases/$(database)/documents/vehicles/$(request.resource.data.vehicleId)).data.ownerId == request.auth.uid;
    }
    
    // Fare collections
    match /fare_collections/{collectionId} {
      allow read: if request.auth != null && 
                   exists(/databases/$(database)/documents/vehicles/$(resource.data.vehicleId)) &&
                   get(/databases/$(database)/documents/vehicles/$(resource.data.vehicleId)).data.ownerId == request.auth.uid;
    }
  }
}
```

### Building the Project

1. Clone the repository
2. Open the project in Android Studio
3. Sync the project with Gradle files
4. Run the app on an emulator or physical device

### Development Configuration

The app uses the following main dependencies:

- Jetpack Compose for UI
- Hilt for dependency injection
- Firebase Auth for authentication
- Firebase Firestore for data storage
- ZXing for QR code generation

## Project Structure

```
app/
├── src/
│   └── main/
│       ├── kotlin/com/naulify/agent/
│       │   ├── di/             # Dependency injection
│       │   ├── model/          # Data models
│       │   ├── repository/     # Data repositories
│       │   ├── ui/            # UI components and screens
│       │   ├── util/          # Utility functions
│       │   └── viewmodel/     # ViewModels
│       └── res/               # Resources
```

## Customization

### Themes and Colors

To customize the app's appearance, modify the following files:

- `ui/theme/Theme.kt`: Color schemes and theme configuration
- `ui/theme/Type.kt`: Typography styles
- `res/values/themes.xml`: Base theme attributes

### Firebase Functions

For the payment processing integration, you'll need to deploy the following Cloud Functions:

1. Transaction processing function
2. Payment verification function
3. Receipt generation function

(Implement these functions based on your payment gateway integration requirements)

## Security Considerations

1. Enable App Check in Firebase Console
2. Implement proper error handling for failed transactions
3. Use proper validation for all user inputs
4. Implement rate limiting for API calls
5. Regular security audits and penetration testing

## Troubleshooting

### Common Issues

1. Build Errors
   - Clean and rebuild the project
   - Invalidate caches and restart Android Studio
   - Verify Gradle plugin versions

2. Firebase Connection Issues
   - Verify `google-services.json` is properly configured
   - Check internet connectivity
   - Verify SHA-1 fingerprint in Firebase console

3. QR Code Generation Issues
   - Verify proper permissions
   - Check memory usage for large QR codes

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

[Add your license information here]
