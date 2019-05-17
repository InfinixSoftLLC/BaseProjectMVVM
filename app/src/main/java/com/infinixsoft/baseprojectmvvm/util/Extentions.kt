package com.infinixsoft.baseprojectmvvm.util

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Parcelable
import android.provider.MediaStore
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.util.Base64
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.infinixsoft.baseprojectmvvm.R
import com.infinixsoft.baseprojectmvvm.views.CustomEditText
import java.io.File
import java.security.MessageDigest
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

fun Context.showMessage(message: String?) {
    if (message != null) Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun ImageView.loadImage(url: String?, placeHolder: Int, circleCrop: Boolean = false) {
    val requestOptions = RequestOptions().placeholder(placeHolder)
    Glide.with(context).load(url).apply(
        if (circleCrop) requestOptions.circleCrop()
        else requestOptions
    ).into(this)
}

fun ImageView.loadImage(resourceId: Int, placeHolder: Int, circleCrop: Boolean = false) {
    val requestOptions = RequestOptions().placeholder(placeHolder)
    Glide.with(context).load(resourceId).apply(
        if (circleCrop) requestOptions.circleCrop()
        else requestOptions
    ).into(this)
}

fun ImageView.loadImage(url: String?, requestOptions: RequestOptions) {
    Glide.with(context).load(url).apply(requestOptions).into(this)
}

fun View.visible() {
    this.visibility = View.VISIBLE
}

fun View.invisible() {
    this.visibility = View.INVISIBLE
}

fun View.gone() {
    this.visibility = View.GONE
}

fun View.isVisible(): Boolean {
    return this.visibility == View.VISIBLE
}

fun ViewGroup.inflate(layoutRes: Int): View {
    return LayoutInflater.from(context).inflate(layoutRes, this, false)
}

fun Activity.hideKeyboard() {
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
}

fun formatTimeUSA(strDate: String?, formatOrigin: String?): String {
    val sdfOrigin = SimpleDateFormat(formatOrigin, Locale.getDefault())
    val sdfDestiny = SimpleDateFormat("H:mm a", Locale.getDefault())
    return sdfDestiny.format(sdfOrigin.parse(strDate))
}

fun formatDateUSA(strDate: String?, formatOrigin: String?): String {
    val sdfOrigin = SimpleDateFormat(formatOrigin, Locale.getDefault())
    val sdfDestiny = SimpleDateFormat("M-d-yyyy", Locale.getDefault())
    return sdfDestiny.format(sdfOrigin.parse(strDate))
}

fun formatDateString(strDate: String?, formatOrigin: String?, formatDestiny: String): String {
    val sdfOrigin = SimpleDateFormat(formatOrigin, Locale.getDefault())
    val sdfDestiny = SimpleDateFormat(formatDestiny, Locale.getDefault())
    return sdfDestiny.format(sdfOrigin.parse(strDate))
}

fun launchWebsiteIntent(context: Context, url: String) {
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW).setData(Uri.parse(url)))
    } catch (e: ActivityNotFoundException) {
        e.printStackTrace()
    }
}

fun launchCallIntent(context: Context, number: String?) {
    if (number.isNullOrEmpty()) return
    try {
        context.startActivity(Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", number, null)))
    } catch (e: ActivityNotFoundException) {
        e.printStackTrace()
    }
}

fun launchTakePictureIntent(context: Activity, requestCode: Int): Uri? {
    val root = File(
        String.format(
            "%s/%s/",
            Environment.getExternalStorageDirectory().toString(),
            context.getString(R.string.app_name)
        )
    )
    if (!(root.mkdirs() || root.isDirectory)) return null
    val mOutputFileUri = Uri.fromFile(File(root, "TMP_${Calendar.getInstance().timeInMillis}.jpg"))
    context.startActivityForResult(getPictureIntent(context, mOutputFileUri), requestCode)
    return mOutputFileUri
}

fun launchTakePictureIntent(fragment: Fragment, requestCode: Int): Uri? {
    val root = File(
        String.format(
            "%s/%s/",
            Environment.getExternalStorageDirectory().toString(),
            fragment.getString(R.string.app_name)
        )
    )
    if (!(root.mkdirs() || root.isDirectory)) return null
    val mOutputFileUri = Uri.fromFile(File(root, "TMP_${Calendar.getInstance().timeInMillis}.jpg"))
    fragment.startActivityForResult(getPictureIntent(fragment.context!!, mOutputFileUri), requestCode)
    return mOutputFileUri
}

private fun getPictureIntent(context: Context, outputFileUri: Uri): Intent {
    val cameraIntents = ArrayList<Intent>()
    val captureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
    val listCam = context.packageManager.queryIntentActivities(captureIntent, 0)
    for (res in listCam) {
        val intent = Intent(captureIntent)
        intent.component = ComponentName(res.activityInfo.packageName, res.activityInfo.name)
        intent.setPackage(res.activityInfo.packageName)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri)
        cameraIntents.add(intent)
    }

    val galleryIntent = Intent()
    galleryIntent.type = "image/*"
    galleryIntent.action = Intent.ACTION_PICK

    return Intent.createChooser(galleryIntent, context.getString(R.string.select_source))
        .putExtra(Intent.EXTRA_INITIAL_INTENTS, cameraIntents.toTypedArray<Parcelable>())
}

fun FragmentActivity.showFragment(containerId: Int, fragment: Fragment, tag: String? = null) {
    supportFragmentManager.beginTransaction().replace(containerId, fragment, tag).commit()
}

fun Context.getThemeAccentColor(): Int {
    val value = TypedValue()
    this.theme.resolveAttribute(R.attr.colorAccent, value, true)
    return value.data
}

fun blurImage(context: Context, image: Bitmap): Bitmap {
    val scale = 0.5f
    val radius = 15f

    val width = Math.round(image.width * scale)
    val height = Math.round(image.height * scale)

    val inputBitmap = Bitmap.createScaledBitmap(image, width, height, false)
    val outputBitmap = Bitmap.createBitmap(inputBitmap)

    val rs = RenderScript.create(context)

    val intrinsicBlur = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs))
    val tmpIn = Allocation.createFromBitmap(rs, inputBitmap)
    val tmpOut = Allocation.createFromBitmap(rs, outputBitmap)

    intrinsicBlur.setRadius(radius)
    intrinsicBlur.setInput(tmpIn)
    intrinsicBlur.forEach(tmpOut)
    tmpOut.copyTo(outputBitmap)

    return outputBitmap
}

fun getCurrency(vale: Float) = NumberFormat.getCurrencyInstance(Locale.US).format(vale)

    @SuppressLint("PackageManagerGetSignatures")
    fun Context.printKeyHash() {
    try {
        val signatures =
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P)
                packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES).signatures
            else
                packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES)
                    .signingInfo.apkContentsSigners

        val md = MessageDigest.getInstance("SHA")
        for (signature in signatures) {
            md.update(signature.toByteArray())
            Log.i("MY_KEY_HASH:", Base64.encodeToString(md.digest(), Base64.DEFAULT))
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

    fun Context.createNotificationChannel() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val mNotificationManager = getSystemService(Application.NOTIFICATION_SERVICE) as NotificationManager?
        val mChannel = NotificationChannel(
            getString(R.string.notification_channel_id),
            getString(R.string.app_name),
            NotificationManager.IMPORTANCE_HIGH
        )
        mNotificationManager?.createNotificationChannel(mChannel)
    }


    fun EditText.validNotEmptyField(): Boolean {
        if (this.text.toString().isEmpty()) {
            this.error = this.context.getString(R.string.alert_empty_field)
            return false
        }
        return true
    }


    fun EditText.validEmailField(): Boolean {
        if (!CustomEditText.isValidEmail(this.text.toString())) {
            this.error = this.context.getString(R.string.error_invalid_email)
            return false
        }
        return true
    }

    fun EditText.cleanError() {
        this.error = null
    }


    fun Snackbar.showShortMessage(view: View, message: String) {
        Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show()
    }

    fun Snackbar.showLongMessage(view: View, message: String) {
        Snackbar.make(view, message, Snackbar.LENGTH_LONG).show()
    }
}
