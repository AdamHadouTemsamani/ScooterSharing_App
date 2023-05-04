/*
 * MIT License
 *
 * Copyright (c) 2023 Adam & Weihao
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dk.itu.moapd.scootersharing.ahad.activities

import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import dk.itu.moapd.scootersharing.ahad.fragments.MainFragment
import dk.itu.moapd.scootersharing.ahad.R
import dk.itu.moapd.scootersharing.ahad.application.TAG
import dk.itu.moapd.scootersharing.ahad.databinding.ActivityMainBinding


//Activity that is responsible for hosting our Fragments
class MainActivity : AppCompatActivity() {

    //Binding that contains reference to root view.
    private lateinit var mainBinding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Inflates the layout of our binding
        mainBinding = ActivityMainBinding.inflate(layoutInflater)

        //We set the content view to be our layout from our binding
        val mainFragment = MainFragment()
        setContentView(mainBinding.root)

        //If MainActivity is started for the first time we start hosting the MainFragment.
        //This is used for handling orientation.
        if(savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragment_container_view,mainFragment)
                .commit()
        }
    }
}