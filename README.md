# IMDB Movies App (Android)

This is a sample Android application that displays a list of movies from a remote API, demonstrating
modern Android development practices. This entire application code, including unit tests have been
written by **Gemini Agent in Android Studio**.

## Improvised Requirements

- **Robust Error Handling:** Implements a clear and context-aware error handling strategy:
    - **Initial Offline State:** Shows a full-screen error if the app is launched without an
      internet connection.
    - **Graceful Cache Handling:** Seamlessly serves cached data when offline without showing an
      error.
    - **Contextual Failure:** If a user tries to change a filter while offline and the data isn't
      cached, it shows a non-disruptive snackbar and reverts the UI to the last valid state.
- **External Linking:** Opens a movie's IMDb page in a Chrome Custom Tab for a seamless user
  experience.

## Architecture & Tech Stack

The app is built following **MVVM** and **Clean Architecture** principles to create a scalable and
maintainable codebase.

- **UI Layer:** Built entirely with **Jetpack Compose** (Material 3) for a modern, declarative UI.
  The UI logic is centralized in a stateful `MovieScreen` composable, keeping the `MainActivity`
  extremely clean.
- **Dependency Injection:** Uses **Hilt** to manage dependencies and decouple components.
- **Networking:** Leverages **Retrofit** for type-safe HTTP requests, **Moshi** for efficient JSON
  parsing, and **OkHttp** for advanced network configuration, including caching.
- **Asynchronous Programming:** Uses **Kotlin Coroutines** and **Flow** to manage background threads
  and handle reactive data streams.
- **Pagination:** Implements the **Paging 3** library for efficient, offset-based pagination, with a
  smaller initial load size for faster perceived startup.
- **Testing:** Includes a comprehensive suite of **unit tests** using **JUnit** and **MockK** that
  provide 100% test coverage for the `data` and `domain` layers.

## Development Philosophy & Reflections

- **Pragmatic Problem-Solving:** When I faced with an issue of API timeouts on cold starts, my
  approach was to diagnose the cause and implement a practical solution (increasing the network
  timeout) rather than assuming a perfect backend.
- **Proactive Enhancements & User-Centric Design:** My implementation went beyond the minimum
  requirements by adding a 15-minute network cache, using Chrome Custom Tabs, and creating a custom
  dropdown to perfectly match the wireframe.
- **Iterative Refinement & Collaboration:** The development process demonstrated a commitment to
  quality through iteration. The UI, especially the genre dropdown, was refined over several steps
  to perfectly match the wireframe. Most importantly, the error handling is to handle edge cases
  gracefully, ensuring the UI never lies to the user.

### Next Steps & Potential Enhancements

- **Advanced Offline Caching:** Replace the OkHttp cache with a **Room database** to create a true
  single source of truth. This would allow for a more robust offline-first experience and instant
  loads on subsequent app opens.
- **Image Loading:** Integrate an image loading library like **Coil** to download and display movie
  poster images in the list items.
- **Instrumentation Tests:** While the business logic is well-tested with unit tests, adding UI
  tests with the **Compose Test framework** and **Hilt's testing support** would verify the complex
  error-handling logic in the `MovieScreen` and prevent visual regressions.
- **UI/UX Polish:**
    - Implement Pull-to-Refresh
    - Implement Persistent Genre Selection after app restart
    - Implement shimmer loading animations (skeletons) for a smoother perceived performance.
    - Add an explicit "Retry" button to the full-screen error state.
    - Implement dark mode support.