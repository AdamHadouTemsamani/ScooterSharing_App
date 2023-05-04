package dk.itu.moapd.scootersharing.ahad.fragments

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.view.doOnLayout
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import dk.itu.moapd.scootersharing.ahad.databinding.FragmentCameraBinding
import java.io.File
import java.io.FileInputStream
import java.util.*
import kotlin.math.roundToInt

//This is the Fragment responsible for handling calling Camera intent
// + uploading it to the Firebase Bucket

// This has been made using the material given in Chapter 17 of our Book (Android Programming - The Big Nerd Ranch)

class CameraFragment : Fragment() {

    companion object {
        private val TAG = CameraFragment::class.java.simpleName
    }

    //Binding that contains reference to root view.
    private var _binding: FragmentCameraBinding? = null

    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }

    //Local fields for name of photo and a reference to the File created
    // when taking pictures using camera intent.
    private var photoName: String? = null
    private var photoFile: File? = null

    //Name of scooter whose picture in the bucket you want to update.
    private lateinit var scooterName: String

    //Uses camera to take picture and updates the photo shown in the UI
    private val takePhoto = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { _ ->
        updatePhoto(photoName)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //Inflate layout from binding
        _binding = FragmentCameraBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //Saves the name of the picture as the current date.
        val photoName = "IMG_${Date()}.JPG"
        //Created a file with the name of photo in the phones directory.
        photoFile = File(
            requireContext().applicationContext.filesDir,
            photoName
        )
        //Gets the URI of the photo so that the camera can saved it at that location.
        val photoUri = FileProvider.getUriForFile(
            requireContext(),
            "dk.itu.moapd.scootersharing.ahad.fragments.CameraFragment",
            photoFile!!
        )

        //Gets the name of scooter using arguments (Bundle())
        scooterName = arguments?.getString("Scooter").toString()

        //Launches the camera
        takePhoto.launch(photoUri)

        binding.apply {
            cameraButton.setOnClickListener {
                //Launches the camera
                takePhoto.launch(photoUri)
            }

            retakeButton.setOnClickListener {
                //Uploads the photo taken to Firebase Storage bucket.
                uploadPhotoToFirebaseStorage(scooterName)
            }
        }
    }

    //Updates the photo shown in Fragment, using Bitmap
    private fun updatePhoto(photoFileName: String?) {
        if (photoFile?.exists() == true) {
            binding.crimePhoto.doOnLayout { measuredView ->
                val scaledBitmap = getScaledBitmap(
                    photoFile!!.path,
                    measuredView.width,
                    measuredView.height
                )
                binding.crimePhoto.setImageBitmap(scaledBitmap)
                binding.crimePhoto.tag = photoFileName
            }
        } else {
            binding.crimePhoto.setImageBitmap(null)
            binding.crimePhoto.tag = null
        }


    }

    //Uploads the photo to Firebase Storage with the PhotoFileName given.
    private fun uploadPhotoToFirebaseStorage(photoFileName: String?) {
        val storage = Firebase.storage("gs://moapd-2023-cc929.appspot.com")
        //Check if the photo exists on your phone
        if (photoFile?.exists() == true) {
            //Create a reference to the Firebase Storage bucket
            val imageRef = storage.reference.child(photoFileName + ".jpg")
            val stream = FileInputStream(photoFile)
            val uploadTask = imageRef.putStream(stream)

            uploadTask.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // The photo was successfully uploaded to Firebase Storage
                    // Get the download URL for the photo and store it in the scooter object
                    imageRef.downloadUrl.addOnSuccessListener { uri ->
                        showMessage("Picture has been added to the database! You can now return to the Main Screen")
                    }
                } else {
                    // There was an error uploading the photo to Firebase Storage
                    // Log the error message
                    task.exception?.message?.let {
                        Log.e(TAG, it)
                    }
                }
            }
        }
    }

    //Taken from our Text Book.
// It creates the graphics shown in our ImageView
    fun getScaledBitmap(path: String, destWidth: Int, destHeight: Int): Bitmap {
        // Read in the dimensions of the image on disk
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(path, options)
        val srcWidth = options.outWidth.toFloat()
        val srcHeight = options.outHeight.toFloat()
        // Figure out how much to scale down by
        val sampleSize = if (srcHeight <= destHeight && srcWidth <= destWidth) {
            1
        } else {
            val heightScale = srcHeight / destHeight
            val widthScale = srcWidth / destWidth
            minOf(heightScale, widthScale).roundToInt()
        }
        // Read in and create final bitmap
        return BitmapFactory.decodeFile(path, BitmapFactory.Options().apply {
            inSampleSize = sampleSize
        })
    }

    //Used to more easily make a snackbox.
    private fun showMessage(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }
}






