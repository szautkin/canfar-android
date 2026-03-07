# Contributing to Verbinal for Android

Thank you for your interest in contributing! This document provides guidelines
for contributing to the project.

## Getting Started

1. Fork the repository
2. Clone your fork and create a branch:
   ```bash
   git clone git@github.com:YOUR_USERNAME/canfar-android.git
   cd canfar-android
   git checkout -b my-feature
   ```
3. Build and test:
   ```bash
   ./gradlew assembleDebug
   ./gradlew spotlessCheck
   ./gradlew lint
   ```

## Code Style

- Run `./gradlew spotlessApply` before committing
- Run `./gradlew lint` and fix all warnings
- Follow existing code patterns and naming conventions
- Keep changes focused — one feature or fix per PR

## Pull Requests

1. Ensure your code compiles without warnings
2. Format your code (`./gradlew spotlessApply`)
3. Run lint (`./gradlew lint`)
4. Write a clear PR description explaining what and why
5. Reference any related issues

## Reporting Issues

- Use the GitHub issue tracker
- Include your Android version and device model
- Include steps to reproduce the problem
- Include any error output or screenshots

## License

By contributing, you agree that your contributions will be licensed under the
[AGPL-3.0 license](LICENSE).
