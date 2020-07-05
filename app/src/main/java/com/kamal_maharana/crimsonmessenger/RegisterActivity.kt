package com.kamal_maharana.crimsonmessenger

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.kamal_maharana.crimsonmessenger.models.User
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        register_button_register.setOnClickListener {
                performRegister()
            }

        // Launch the login activity
        already_have_account_text_view.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        // Select Profile PIC
        profile_photo_button_register.setOnClickListener {
            Log.d("Register", "Try to show image")
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }
    }

    var selectedPhotoUri: Uri? = null
    // It takes the INTENT from above function
    // Always use after a start activity so to process the intent
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            // proceed with identifying the image
            Log.d("Register", "Image selected")
            selectedPhotoUri = data.data

            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)
            profile_photo_imageview_register.setImageBitmap(bitmap)
            profile_photo_button_register.alpha = 0f
//            val bitmapDrawable = BitmapDrawable(bitmap)
//            profile_photo_button.setBackgroundDrawable(bitmapDrawable)
            //profile_photo_button.alpha = 0f

        }
    }

    private fun performRegister() {
        val email = email_edittext_register.text.toString()
        val password = password_edittext_register.text.toString()
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
            return
        }
        Log.d("Register", "Email is: $email")
        Log.d("Register", "Password is: $password")

        // Firebase Authentication to create user with email and password
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (!it.isSuccessful) return@addOnCompleteListener
                // If successful
                // UID of user can be retrieved as ${it.result.user.uid}
                Log.d("Register", "UID: ${it.result?.user?.uid}")

                uploadImageToFirebaseStorage()
            }
            .addOnFailureListener {
                Toast.makeText(this, "ERROR: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Function to upload image to Firebase Storage
    private fun uploadImageToFirebaseStorage() {
        if (selectedPhotoUri == null) return
        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")
        ref.putFile(selectedPhotoUri!!)
            .addOnSuccessListener {
                Log.d("Register", "Successfully uploaded image: ${it.metadata?.path}")
                ref.downloadUrl.addOnSuccessListener {
                    it.toString()
                    Log.d("Register", "File Location: $it")

                    //Upload userdata to database
                    saveUserToFirebaseDatabase(it.toString())
                }
            }
            .addOnFailureListener {
                Log.d("Register", "ERROR: ${it.message}")
            }
    }
    private fun saveUserToFirebaseDatabase(profileImageUri: String) {
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        val user = User(uid, username_edittext_register.text.toString(), profileImageUri)
        ref.setValue(user)
            .addOnSuccessListener {
                Log.d("Register", "Saved user to firebase database.")
                val intent = Intent(this, LatestMessagesActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            .addOnFailureListener {
                Log.d("Register", "ERROR: ${it.message}")
            }
    }
}

