package hu.bme.aut.android.bmelostandfound

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import hu.bme.aut.android.bmelostandfound.data.User
import hu.bme.aut.android.bmelostandfound.databinding.ActivityRegisterBinding
import hu.bme.aut.android.bmelostandfound.extensions.validateNonEmpty
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.URLEncoder
import java.util.*

class RegisterActivity : BaseActivity() {
    companion object {
        private const val REQUEST_CAMERA = 101
        private const val REQUEST_GALLERY = 102
    }

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.btnRegister.setOnClickListener { registerClick() }
        binding.ivUser.setOnClickListener { attachClick() }
    }

    private fun validateForm() =
        binding.etEmail.validateNonEmpty() &&
                binding.etPassword.validateNonEmpty() &&
                binding.etUsername.validateNonEmpty()

    private fun attachClick() {
        showPictureDialog()
    }

    private fun showPictureDialog() {
        val pictureDialog = AlertDialog.Builder(this)
        pictureDialog.setTitle(R.string.add_prof_pic)
        val pictureDialogItems =
            arrayOf(getString(R.string.open_gallery), getString(R.string.open_camera))
        pictureDialog.setItems(
            pictureDialogItems
        ) { dialog, which ->
            when (which) {
                0 -> chooseImageFromGallery()
                1 -> takePhotoFromCamera()
            }
        }
        pictureDialog.show()
    }

    private fun chooseImageFromGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(galleryIntent, REQUEST_GALLERY)
    }

    private fun takePhotoFromCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(cameraIntent, REQUEST_CAMERA)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) {
            return
        }

        if (requestCode == REQUEST_GALLERY) {
            if (data != null) {
                val contentURI = data.data
                try {
                    contentURI?.let {
                        if (Build.VERSION.SDK_INT < 28) {
                            val imageBitmap = MediaStore.Images.Media.getBitmap(
                                this.contentResolver,
                                contentURI
                            )
                            binding.ivUser.setImageBitmap(imageBitmap)
                        } else {
                            val source = ImageDecoder.createSource(this.contentResolver, contentURI)
                            val imageBitmap = ImageDecoder.decodeBitmap(source)
                            binding.ivUser.setImageBitmap(imageBitmap)
                        }
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show()
                }
            }
        } else if (requestCode == REQUEST_CAMERA) {
            val imageBitmap = data?.extras?.get("data") as? Bitmap ?: return
            binding.ivUser.setImageBitmap(imageBitmap)
            binding.ivUser.visibility = View.VISIBLE
        }
    }

    private fun registerClick() {
        if (!validateForm()) {
            return
        }

        showProgressDialog()

        firebaseAuth
            .createUserWithEmailAndPassword(
                binding.etEmail.text.toString(),
                binding.etPassword.text.toString()
            )
            .addOnSuccessListener { result ->

                val firebaseUser = result.user
                val profileChangeRequest = UserProfileChangeRequest.Builder()
                    .setDisplayName(binding.etUsername.text.toString())
                    .build()
                firebaseUser?.updateProfile(profileChangeRequest)

                if (binding.ivUser.drawable.constantState != getDrawable(R.drawable.ic_add_profile_pic)?.constantState) {
                    registerWithImage(firebaseUser!!.uid, firebaseUser.email!!)
                } else {
                    registerUser(firebaseUser!!.uid, firebaseUser.email!!)
                }
            }
            .addOnFailureListener { exception ->
                hideProgressDialog()

                toast(exception.message)
            }
    }

    private fun registerUser(uid: String, mail: String, imageUrl: String? = null) {
        val userPhone = if (binding.etPhone.text.toString().isNotEmpty()) {
            binding.etPhone.text.toString()
        } else {
            null
        }

        val newUser = User(
            uid,
            mail,
            binding.etUsername.text.toString(),
            userPhone,
            imageUrl
        )

        val db = Firebase.firestore

        db.collection("users")
            .add(newUser)
            .addOnSuccessListener {
                hideProgressDialog()
                toast("Sikeres regisztráció")
                finish()
            }
            .addOnFailureListener { e ->
                toast(e.toString())
            }
    }

    private fun registerWithImage(uid: String, mail: String,) {
        val bitmap: Bitmap = (binding.ivUser.drawable as BitmapDrawable).bitmap
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val imageInBytes = baos.toByteArray()

        val storageReference = FirebaseStorage.getInstance().reference
        val newImageName = URLEncoder.encode(UUID.randomUUID().toString(), "UTF-8") + ".jpg"
        val newImageRef = storageReference.child("profile_pics/$newImageName")

        newImageRef.putBytes(imageInBytes)
            .addOnFailureListener { exception ->
                toast(exception.message)
            }
            .continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let { throw it }
                }

                newImageRef.downloadUrl
            }
            .addOnSuccessListener { downloadUri ->
                registerUser(uid, mail, downloadUri.toString())
            }
    }
}