package dk.itu.moapd.scootersharing.ahad

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.Lifecycle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import dk.itu.moapd.scootersharing.ahad.fragments.MainFragment
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainFragmentTest {

    @get:Rule
    var fragmentContainer = launchFragmentInContainer<MainFragment>()

    @Test
    fun createMainFragment_test() {
        fragmentContainer.moveToState(Lifecycle.State.RESUMED)
        onView(withId(R.id.button)).perform(click())
    }




}