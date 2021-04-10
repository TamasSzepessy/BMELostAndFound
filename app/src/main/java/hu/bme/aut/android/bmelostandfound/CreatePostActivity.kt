package hu.bme.aut.android.bmelostandfound

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import hu.bme.aut.android.bmelostandfound.data.Post
import hu.bme.aut.android.bmelostandfound.databinding.ActivityCreatePostBinding
import hu.bme.aut.android.bmelostandfound.extensions.validateNonEmpty
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.*

class CreatePostActivity() : BaseActivity() {

    companion object {
        private const val REQUEST_CAMERA = 101
        private const val REQUEST_GALLERY = 102
    }

    private lateinit var fromView: String
    private lateinit var binding: ActivityCreatePostBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val b = intent.extras
        fromView = b!!.getString("from")!!
        binding = ActivityCreatePostBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)
        when (fromView) {
            "lost" -> binding.tvCreateTitle.text = getString(R.string.lost_title)
            "found" -> binding.tvCreateTitle.text = getString(R.string.found_title)
        }

        binding.toolbar.title = ""
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        binding.btnSend.setOnClickListener { sendClick() }
        binding.imgAttach.setOnClickListener { attachClick() }
        binding.spinnerBuildings.adapter = ArrayAdapter(
            applicationContext,
            android.R.layout.simple_spinner_item,
            resources.getStringArray(R.array.buildings)
        )
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    private fun sendClick() {
        if (!validateForm()) {
            return
        }

        if (binding.imgAttach.drawable.constantState == getDrawable(R.drawable.ic_add_image)?.constantState) {
            uploadPost()
        } else {
            try {
                uploadPostWithImage()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun validateForm() =
        binding.etTitle.validateNonEmpty() && binding.etBody.validateNonEmpty()

    private fun uploadPost(imageUrl: String? = null) {
        val sdf = SimpleDateFormat("yyyy.MM.dd kk:mm")
        val currentDate = sdf.format(Date())

        val newPost = Post(
            null,
            uid,
            binding.etTitle.text.toString(),
            binding.etBody.text.toString(),
            imageUrl,
            binding.spinnerBuildings.selectedItem.toString(),
            currentDate,
            null
        )

        val db = Firebase.firestore

        db.collection(fromView)
            .add(newPost)
            .addOnSuccessListener {
                toast(getString(R.string.post_created))
                finish()
            }
            .addOnFailureListener { e ->
                toast(e.toString())
            }
    }

    private fun attachClick() {
        showPictureDialog()
    }

    private fun showPictureDialog() {
        val pictureDialog = AlertDialog.Builder(this)
        pictureDialog.setTitle(R.string.add_object_pic)
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
                            binding.imgAttach.setImageBitmap(imageBitmap)
                        } else {
                            val source = ImageDecoder.createSource(this.contentResolver, contentURI)
                            val imageBitmap = ImageDecoder.decodeBitmap(source)
                            binding.imgAttach.setImageBitmap(imageBitmap)
                        }
                    }
                    binding.imgAttach.visibility = View.VISIBLE
                } catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show()
                }
            }
        } else if (requestCode == REQUEST_CAMERA) {
            val imageBitmap = data?.extras?.get("data") as? Bitmap ?: return
            binding.imgAttach.setImageBitmap(imageBitmap)
            binding.imgAttach.visibility = View.VISIBLE
        }
    }

    private fun uploadPostWithImage() {
        val bitmap: Bitmap = (binding.imgAttach.drawable as BitmapDrawable).bitmap
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val imageInBytes = baos.toByteArray()

        val storageReference = FirebaseStorage.getInstance().reference
        val newImageName = URLEncoder.encode(UUID.randomUUID().toString(), "UTF-8") + ".jpg"
        val newImageRef = storageReference.child("images/$newImageName")

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
                uploadPost(downloadUri.toString())
            }
    }

}