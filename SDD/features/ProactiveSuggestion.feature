Feature: Proactive Suggestion Intervention
  As an easily distracted user prone to notification fatigue
  I want the app to send me interactive, empathetic re-scheduling suggestions when a habit faces high friction
  So that I do not develop notification blindness and completely abandon my routine.

  Background:
    Given the local telemetry engine tracks that the user has skipped the "Reading Block" habit 3 consecutive times
    And the application sets the state of this habit to "Critical Friction" inside the Room database

  Scenario: App triggers an empathetic interactive notification instead of a standard alarm
    When the scheduled time for the high-friction habit arrives
    Then the system should intercept the standard auditory alarm manager pipeline
    And the app should trigger a custom native notification using a disruptive, non-habituated haptic vibration pattern
    And the notification should display an empathetic text: "Hola. Notamos que este horario te ha costado trabajo últimamente. No te preocupes, vamos a tu ritmo. ¿Prefieres mover la lectura a hoy a las 6:30 PM o prefieres que lo intentemos el sábado por la mañana?"
    When the user clicks the "Move to 6:30 PM" one-tap approval button inside the notification layout
    Then the notification should dismiss immediately
    And the app should update the local Room database timestamp with zero UI lag
    And the main dashboard list should reflect the updated schedule state.