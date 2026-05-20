Feature: Conversational Routine Modification via Voice
  As a student with chronic attention fatigue
  I want to update my afternoon schedule using a natural language voice command
  So that I can avoid the cognitive friction of manually editing forms and blocks.

  Background:
    Given the user has completed onboarding and is on the main dashboard screen
    And the Room database contains the user's default afternoon habit blocks

  Scenario: User moves afternoon habits due to an unscheduled research project meeting
    When the user presses and holds the microphone floating action button
    And the user says "Tengo una junta con mi asesor de Proyecto de Investigación a las 5, mueve mis hábitos de la tarde"
    And the user releases the microphone button
    Then the UI should trigger a native wave animation in Jetpack Compose to emulate active audio capture
    And the app should introduce a controlled coroutine delay of 1.5 seconds to accurately simulate cloud API inference latency
    And the app should play an empathetic Text-to-Speech (TTS) voice audio response saying: "Entendido, Gabriel. He protegido tu espacio para la junta de Proyecto de Investigación. Moviendo tus hábitos de la tarde para reducir tu estrés."
    And the Room database should atomically update the state of the affected blocks
    And the UI should smoothly animate the displacement of all afternoon habit blocks past the newly created 5:00 PM meeting slot.