Feature: Interactive Habit Gamification and Completion
  As an easily distracted user seeking immediate reinforcement
  I want to instantly mark a pending habit as completed by tapping its icon
  So that I get rewarding sensory feedback and maintain tracking consistency.

  Background:
    Given the user is looking at their current routine feed on the Main Dashboard
    And the task card item has a status flag set to "PENDING"

  Scenario: Marking a pending habit card as completed triggers dynamic theme mutation
    When the user clicks the interaction target container or icon on the target habit block
    Then the UI must dispatch an intent event to the ViewModel state engine
    And the system must atomically change the target item state from "PENDING" to "COMPLETED"
    Then the Jetpack Compose drawing engine must smoothly animate the card background tint
    And the background color must transition dynamically towards the WellbeingMint palette color (0xFFDCF5EC)
    And the typography elements inside that specific card must instantly swap to DeepPurpleNavy (0xFF1C1B29) to enforce strict accessibility readability.