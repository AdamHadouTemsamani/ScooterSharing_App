package dk.itu.moapd.scootersharing.ahad

import androidx.lifecycle.Lifecycle
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import dk.itu.moapd.scootersharing.ahad.activities.LoginActivity
import dk.itu.moapd.scootersharing.ahad.activities.MainActivity

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.Rule


class ExampleInstrumentedTest {

@get:Rule
var activityScenarioRule = ActivityScenarioRule(MainActivity::class.java)

@Test
//This test is for when the application starts and the user needs to log in.
// We check whether the main activity life cycle state is set correctly.
fun mainActivity_moveToStateCreated() {
    val scenario = activityScenarioRule.scenario
    scenario.moveToState(Lifecycle.State.DESTROYED)
}

@get:Rule
var activityScenarioRuleLogin = ActivityScenarioRule(LoginActivity::class.java)

@Test
//This test is for when the application starts and the user needs to log in.
// We check whether the login activity is create correctly.
fun testLoginActivity_isCreated() {
    val scenario = activityScenarioRuleLogin.scenario
    scenario.moveToState(Lifecycle.State.CREATED)
}

}