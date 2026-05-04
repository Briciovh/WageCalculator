package com.softeen.wagecalculator

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain

@HiltAndroidTest
class WageCalculatorUiTest {

    private val hiltRule = HiltAndroidRule(this)
    private val composeRule = createAndroidComposeRule<MainActivity>()

    @get:Rule
    val rules: RuleChain = RuleChain.outerRule(hiltRule).around(composeRule)

    @Test
    fun navigation_converterToConfigAndBack() {
        composeRule.onNodeWithTag("btn_nav_config").performClick()
        val backCd = composeRule.activity.getString(R.string.cd_back)
        composeRule.onNodeWithContentDescription(backCd).assertIsDisplayed()
        composeRule.onNodeWithContentDescription(backCd).performClick()
        composeRule.onNodeWithTag("btn_nav_config").assertIsDisplayed()
    }

    @Test
    fun currencyChange_resetsExchangeRate() {
        composeRule.onNodeWithTag("btn_nav_config").performClick()

        // Set a non-default exchange rate
        composeRule.onNodeWithTag("field_exchange_rate").performTextReplacement("18.5")
        composeRule.waitForIdle()

        // Change base currency from USD to CAD
        composeRule.onNodeWithTag("dropdown_base").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("CAD  C$").performClick()
        composeRule.waitForIdle()

        // Exchange rate must have been reset to 1.0
        composeRule.onNodeWithTag("field_exchange_rate").assert(hasText("1.0"))
    }

    @Test
    fun results_switchingSpotlightPeriod() {
        // Default spotlight is HOURLY
        val hourlyTitle = composeRule.activity.getString(R.string.spotlight_title_hourly)
        composeRule.onNodeWithTag("spotlight_card").onChildren()
            .filterToOne(hasText(hourlyTitle)).assertExists()

        // Tap Monthly result card
        val monthlyLabel = composeRule.activity.getString(R.string.frequency_monthly)
        composeRule.onNodeWithTag("result_card_$monthlyLabel").performClick()

        // Spotlight should now be MONTHLY
        val monthlyTitle = composeRule.activity.getString(R.string.spotlight_title_monthly)
        composeRule.onNodeWithTag("spotlight_card").onChildren()
            .filterToOne(hasText(monthlyTitle)).assertExists()
    }

    @Test
    fun sliders_stayWithinBounds() {
        composeRule.onNodeWithTag("btn_nav_config").performClick()

        composeRule.onNodeWithTag("slider_hours").performTouchInput { swipeRight() }
        composeRule.onNodeWithTag("text_hours_value").assertTextEquals("168")

        composeRule.onNodeWithTag("slider_weeks").performTouchInput { swipeLeft() }
        composeRule.onNodeWithTag("text_weeks_value").assertTextEquals("1")
    }
}
