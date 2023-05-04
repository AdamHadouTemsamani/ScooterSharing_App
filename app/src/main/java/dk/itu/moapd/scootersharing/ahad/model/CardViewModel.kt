package dk.itu.moapd.scootersharing.ahad.model

import android.text.Editable
import android.text.TextWatcher
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CardViewModel : ViewModel() {
    var name: String = ""
    var cardNumber: String = ""
    var expireDate: String = ""
    var cvv: String = ""

    val nameTextWatcher = object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            TODO("Not yet implemented")
        }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            name = p0.toString()
        }

        override fun afterTextChanged(p0: Editable?) {
            TODO("Not yet implemented")
        }
    }
    val currentCardNumber : MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    val currentExpireDate : MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    val currentCVV : MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

}