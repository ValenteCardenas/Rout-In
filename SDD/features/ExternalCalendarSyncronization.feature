Feature: External Calendar Synchronization and Collision Reconciliation
  As a busy university student
  I want the system to automatically reconcile schedule clashes when an immutable external academic event changes
  So that my personal development routines adapt dynamically without manual remapping.

  Background:
    Given the user is operating under the Premium Cloud Subscription
    And the local Room database has an internal high-priority habit block named "Gym / Workout" scheduled today at 6:00 PM

  Scenario: External academic exam event shifts and collides with an internal habit
    When the native MCP Client receives an asynchronous event payload from the Google Calendar server
    And the payload states that the "Sistemas Operativos Exam" has been rescheduled to 6:00 PM today
    Then the system must execute the Calendar Priority rule, designating the academic exam as an immutable block
    And the app should automatically displace the "Gym / Workout" habit block from the 6:00 PM slot in Room
    And the system should immediately dispatch a contextual proactive notification to the user
    And the notification text must state in an empathetic tone: "Tu examen de Sistemas Operativos se movió a las 6:00 PM e interfiere con tu gimnasio. Hemos protegido tu bloque académico para que te enfoques. ¿Deseas reubicar tu rutina de entrenamiento hoy a las 7:30 PM o prefieres que la IA recalcule tu semana?"
    When the user taps the "Re-locate to 7:30 PM" confirmation option
    Then the app should update the local state and queue the sync delta to Supabase PostgreSQL via WebSockets.