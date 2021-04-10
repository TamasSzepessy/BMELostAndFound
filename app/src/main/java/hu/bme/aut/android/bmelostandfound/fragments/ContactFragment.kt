package hu.bme.aut.android.bmelostandfound.fragments

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.marginBottom
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.bumptech.glide.Glide
import hu.bme.aut.android.bmelostandfound.R
import hu.bme.aut.android.bmelostandfound.data.User
import hu.bme.aut.android.bmelostandfound.database.ContactDao
import hu.bme.aut.android.bmelostandfound.databinding.FragmentContactBinding
import hu.bme.aut.android.bmelostandfound.extensions.observeOnce
import hu.bme.aut.android.bmelostandfound.viewmodel.ContactViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import permissions.dispatcher.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*


@RuntimePermissions
class ContactFragment(user: User, self: Boolean = false) : DialogFragment() {

    private lateinit var binding: FragmentContactBinding
    private lateinit var contactViewModel: ContactViewModel
    private lateinit var listener: ContactCreatedListener

    private val userData = user
    private val selfData = self

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            listener = if (targetFragment != null) {
                targetFragment as ContactCreatedListener
            } else {
                activity as ContactCreatedListener
            }
        } catch (e: ClassCastException) {
            throw RuntimeException(e)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentContactBinding.inflate(inflater, container, false)

        contactViewModel = ViewModelProvider(this).get(ContactViewModel::class.java)

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        val dialog: Dialog? = dialog
        if (dialog != null) {
            dialog.window
                ?.setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvUser.text = userData.username
        binding.tvUserMail.text = userData.usermail
        if (userData.phone.isNullOrBlank()) {
            binding.tvUserPhone.visibility = View.GONE
            binding.tvHintPhone.visibility = View.GONE
            binding.divider2.visibility = View.GONE
            binding.imgPhone.visibility = View.GONE
            binding.layoutPhone.visibility = View.GONE
        } else {
            binding.tvUserPhone.text = userData.phone
            binding.tvUserPhone.visibility = View.VISIBLE
            binding.tvHintPhone.visibility = View.VISIBLE
            binding.divider2.visibility = View.VISIBLE
            binding.imgPhone.visibility = View.VISIBLE
            binding.layoutPhone.visibility = View.VISIBLE
        }

        if (!userData.imageUrl.isNullOrBlank()) {
            Glide.with(this).load(userData.imageUrl).into(binding.imgUser)
        }

        if (selfData){
            binding.layoutPhone.visibility = View.GONE
            binding.btnDownload.visibility = View.GONE
            binding.btnMail.visibility = View.GONE
        }

        binding.btnMail.setOnClickListener {
            sendEmail()
        }
        binding.btnCall.setOnClickListener {
            callPhoneNumberWithPermissionCheck(binding.tvUserPhone.text.toString())
        }
        binding.btnText.setOnClickListener {
            sendText(binding.tvUserPhone.text.toString())
        }

        contactViewModel.check(userData).observeOnce(this, {
            if (it) {
                contactSaved()
            }
        })

        binding.btnDownload.setOnClickListener {
            val offlineData = userData
            if (!userData.imageUrl.isNullOrBlank()) {
                val bitmap: Bitmap = (binding.imgUser.drawable as BitmapDrawable).bitmap
                offlineData.imageUrl = saveToInternalStorage(bitmap)
                //Toast.makeText(this.context, "Kép mentve: ${offlineData.imageUrl}", Toast.LENGTH_SHORT).show()
            }
            listener.onContactCreated(offlineData)
            contactSaved()
            Toast.makeText(this.context, getString(R.string.contact_saved), Toast.LENGTH_LONG)
                .show()
        }
    }

    private fun saveToInternalStorage(bitmap: Bitmap): String? {
        val cw = ContextWrapper(context)
        val directory: File = cw.getDir("images", Context.MODE_PRIVATE)
        val path = File(directory, "${UUID.randomUUID()}.jpg")
        var out: FileOutputStream? = null
        try {
            out = FileOutputStream(path)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                out?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return path.absolutePath
    }

    fun contactSaved() {
        val drawable =
            ResourcesCompat.getDrawable(resources, R.drawable.ic_baseline_offline_pin_32, null)
        drawable?.let {
            binding.btnDownload.icon = it
        }
        binding.btnDownload.setIconTintResource(R.color.secondaryColor)
        binding.btnDownload.isClickable = false
        binding.btnDownload.setBackgroundColor(resources.getColor(R.color.primaryLightColor))
    }

    fun sendEmail() {
        val mailIntent = Intent(Intent.ACTION_SENDTO)
        mailIntent.data = Uri.parse("mailto:${binding.tvUserMail.text}")
        mailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name))
        startActivity(Intent.createChooser(mailIntent, getString(R.string.send_sms_intent)))
    }

    @NeedsPermission(Manifest.permission.CALL_PHONE)
    fun callPhoneNumber(phoneNumber: String) {
        val callIntent = Intent(Intent.ACTION_CALL)
        callIntent.data = Uri.parse("tel:$phoneNumber")
        startActivity(callIntent)
    }

    @OnPermissionDenied(Manifest.permission.CALL_PHONE)
    fun onCallDenied() {
        Toast.makeText(this.context, getString(R.string.permission_denied_call), Toast.LENGTH_SHORT)
            .show()
    }

    @OnShowRationale(Manifest.permission.CALL_PHONE)
    fun showRationaleForCall(request: PermissionRequest) {
        val alertDialog = AlertDialog.Builder(requireContext())
            .setTitle(R.string.call_permission_title)
            .setMessage(R.string.call_permission_explanation)
            .setCancelable(false)
            .setPositiveButton(R.string.permission_allow) { dialog, id -> request.proceed() }
            .setNegativeButton(R.string.permission_deny) { dialog, id -> request.cancel() }
            .create()
        alertDialog.show()
    }

    fun sendText(phoneNumber: String) {
        val smsIntent = Intent(Intent.ACTION_SENDTO)
        smsIntent.type = "text/plain"
        smsIntent.data = Uri.parse("smsto:$phoneNumber")
        startActivity(Intent.createChooser(smsIntent, getString(R.string.send_sms_intent)))
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // NOTE: delegate the permission handling to generated method
        onRequestPermissionsResult(requestCode, grantResults)
    }

    interface ContactCreatedListener {
        fun onContactCreated(user: User)
    }
}