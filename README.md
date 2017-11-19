# omnitrack_android
OmniTrack Android implementation
<https://omnitrack.github.io>

### Development Environment
- Android Studio 3.0 +

- JDK 1.7 +

- Support libray 27.0.1

### Android System Requirements
- Minimum SDK Level : 19 (Kitkat), but we recommend Lollipop or higher.

- **Google Play Services**

    To use OmniTrack, the smartphone **MUST** support Google Play Services.
    
    Google-dependent services used in current implementation:
    
    1. **Firebase Auth:** for user authorization (<https://firebase.google.com/docs/auth>)
    1. **Firebase Crash Reporting:** for crash management (<https://firebase.google.com/docs/crash>)
    1. **Firebase Cloud Messaging:** for FirebaseJobDispatcher and server synchronization notification. (<https://firebase.google.com/docs/cloud-messaging/>)
    1. **Google Map View**
    1. **Google Fitness** data connection
    
----
    
## How to Build (Linux / OS X / Bash shell)

1. Clone or download the repository
1. Move to the project root directory
  ```
  cd omnitrack_android
  ```
3. Update git submodules
  ```
  git submodule update --init
  ``` 

----

## License