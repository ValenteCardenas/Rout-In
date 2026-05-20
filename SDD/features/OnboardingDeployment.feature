Feature: Onboarding and Deployment Environment Selection
  As a new user of Rout-In
  I want to select my preferred architecture deployment mode during onboarding
  So that I can choose between an open-source self-hosted setup or an automated cloud subscription.

  Background:
    Given the user has downloaded and opened Rout-In for the first time
    And the user is on the identity welcome screen

  Scenario: Successful onboarding using the Open-Source Self-Hosted architecture
    When the user authenticates successfully via Supabase Auth
    And the user selects the "Open-Source Community (Self-Hosted)" deployment option
    Then the app should navigate to the local network configuration screen
    And the app should prompt the user to input their local Wi-Fi MCP Server URI
    When the user saves a valid URI
    Then the app should store the configuration securely in the device preferences
    And initialize the Kotlin MCP SDK client targeting the local network.

  Scenario: Successful onboarding using the Premium Cloud Subscription
    When the user authenticates successfully via Supabase Auth
    And the user selects the "Premium Cloud Subscription (Out-of-the-Box)" deployment option
    Then the app should securely route the user to the managed cloud gateway via SSE
    And display an automated OAuth 2.0 interface to link external accounts easily.