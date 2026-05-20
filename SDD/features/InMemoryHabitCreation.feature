Feature: In-Memory Habit Creation
  As a student managing an unpredictable academic schedule
  I want to manually add custom routine blocks directly from the dashboard
  So that I can adapt my day when sudden changes occur without relying on cloud connections.

  Background:
    Given the user has completed onboarding and is viewing the Main Dashboard
    And the in-memory list inside the ViewModel contains the baseline seed habits

  Scenario: Successfully adding a new custom habit block via the creation dialog
    When the user taps the secondary add habit floating action button
    Then the app should display an interactive Material Design 3 AlertDialog layout
    And the dialog must capture input for "Habit Name", "Scheduled Time (HH:mm)", and "Duration (Minutes)"
    When the user fills the fields with Name "Repasar Sistemas Operativos", Time "20:00", Duration "60"
    And the user taps the "Confirm" option button
    Then the system must calculate a unique incremental ID using maxOfOrNull plus one
    And atomically append the new HabitBlock to the MutableStateOf collection inside the ViewModel
    And the dialog must dismiss immediately with zero UI delay
    And the lazy column list must smoothly animate and render the new item at the bottom of the feed.