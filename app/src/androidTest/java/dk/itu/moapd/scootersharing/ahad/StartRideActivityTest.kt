package dk.itu.moapd.scootersharing.ahad

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.Lifecycle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StartRideActivityTest {

    @get:Rule
    var fragmentContainer = launchFragmentInContainer<StartRideFragment>()

    @Test
    fun createMainFragment() {
        fragmentContainer.moveToState(Lifecycle.State.RESUMED)
    }

    @Test
    fun readNameEditText() {
        onView(withId(R.id.edit_text_name))
            .check(matches(ViewMatchers.withText("")))
    }

    @Test
    fun readLocationEditText() {
        onView(withId(R.id.edit_text_location))
            .check(matches(ViewMatchers.withText("")))
    }

    @Test
    fun clickAddButton() {
        onView(withId(R.id.edit_text_name))
            .perform(clearText(), typeText("TestScooter"))
        onView(withId(R.id.edit_text_location))
            .perform(clearText(), typeText("ITU"))
        onView(withId(R.id.start_ride_button))
            .perform(click())
    }
}