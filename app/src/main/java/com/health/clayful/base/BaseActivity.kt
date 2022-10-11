package com.health.clayful.base

import android.app.AlertDialog
import android.app.Dialog
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.Rect
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.asLiveData
import androidx.viewbinding.ViewBinding
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.android.material.snackbar.Snackbar
import com.health.clayful.R
import com.health.clayful.databinding.LayoutAlertDialogBinding
import com.health.clayful.network.ApiConstants
import com.health.clayful.ui.home.HomeActivity
import com.health.clayful.utils.PrefData
import io.customer.sdk.CustomerIO
import io.customer.sdk.data.communication.CustomerIOUrlHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


abstract class BaseActivity<V : ViewBinding> : AppCompatActivity(), CustomerIOUrlHandler {

    var shake: Animation?= null
    protected lateinit var binding: V
    var alertDialog : Dialog ?= null
    var alertDialogBinding : LayoutAlertDialogBinding ?= null
    var sweetAlertDialog : SweetAlertDialog?= null
    private lateinit var loaderDialog : Dialog
    private lateinit var alertDialogLoading : AlertDialog
    private lateinit var loaderDialogView : View

    lateinit var prefData : PrefData
    var orientation = 0
    var homeActivity : HomeActivity<ViewBinding> ?= null

    companion object UserCredential {
        var userId : String ?= null
    }

    init {
        ApiConstants.initContext(this as BaseActivity<ViewBinding>)
    }

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if(!resources.getBoolean(R.bool.isTablet)) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        val binding = createBinding()
        this.binding = binding
        setContentView(binding.root)
    }

    fun initHomeActivity(activity: HomeActivity<ViewBinding>) {
        this.homeActivity = activity
    }

    abstract fun createBinding(): V

    fun readToken() {
        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.Main) {
                prefData.readToken()
                prefData.tokenFlow.asLiveData().observe(this@BaseActivity) {
                    ApiConstants.authToken  = it
                }
            }
        }
    }

    fun readJwtToken() {
        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.Main) {
                prefData.getJwtToken()
                prefData.jwtTokenFlow.asLiveData().observe(this@BaseActivity) {
                    ApiConstants.jwtToken  = it
                }
            }
        }
    }

    fun readUserId() {
        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.Main) {
                prefData.readUserId()
                prefData.userIdFlow.asLiveData().observe(this@BaseActivity) {
                    ApiConstants.userId  = it
                }
            }
        }
    }

    fun saveLogoutDataToPref() {

        CoroutineScope(Dispatchers.IO).launch {
            prefData.saveToken("")
            prefData.saveJwtToken("")
            prefData.saveUserId("")
            prefData.saveZendeskLoginStatus(false)
            prefData.saveUserLoginStatus(false)
        }
    }

    fun hideStatusBar() {
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
    }

    fun showCutoutsOnNotch() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            window.attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }
    }

    fun changeTopBarColor(color : Int) {
        window.statusBarColor = color
    }

    fun changeStatusBarIconColorToWhite(view : View) {
        WindowInsetsControllerCompat(window, view).isAppearanceLightStatusBars = false
    }

    fun changeStatusBarIconColorToBlack(view : View) {
        WindowInsetsControllerCompat(window, view).isAppearanceLightStatusBars = true
    }

    fun showAlertDialog(message: String) {
        sweetAlertDialog = SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
        sweetAlertDialog?.titleText = message
        sweetAlertDialog?.confirmText = "Ok"
        sweetAlertDialog?.showCancelButton(false)
        sweetAlertDialog?.setConfirmClickListener {
                it.dismissWithAnimation()
            }
        sweetAlertDialog?.show()

        setDialogStyle()
    }

    fun successAlertDialog(message: String) {
        sweetAlertDialog = SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
        sweetAlertDialog?.titleText = message
        sweetAlertDialog?.confirmText = "Ok"
        sweetAlertDialog?.showCancelButton(false)
        sweetAlertDialog?.setConfirmClickListener {
            it.dismissWithAnimation()
        }
        sweetAlertDialog?.show()
        setDialogStyle()
    }

    private fun setDialogStyle() {
        var font = ResourcesCompat.getFont(this, R.font.mulish_bold)

        sweetAlertDialog!!.getButton(SweetAlertDialog.BUTTON_CONFIRM).background = resources.getDrawable(R.drawable.sweet_alert_cancel_button_style)
        sweetAlertDialog!!.getButton(SweetAlertDialog.BUTTON_CONFIRM).backgroundTintList = resources.getColorStateList(R.color.button_bg)
        sweetAlertDialog!!.getButton(SweetAlertDialog.BUTTON_CONFIRM).typeface = font
        //sweetAlertDialog?.window?.setDimAmount(0f)

        val text = sweetAlertDialog!!.findViewById<TextView>(cn.pedant.SweetAlert.R.id.title_text)
        //setting font color
        text.setTextColor(resources.getColor(R.color.black))
        text.filterConfig()

        //Setting font
        font = ResourcesCompat.getFont(this, R.font.mulish_bold)
        text.typeface = font
        text.textSize = 15f
    }

    private fun TextView.filterConfig(){
        val params = this.layoutParams as ViewGroup.MarginLayoutParams
        params.setMargins(0, 30, 0, 0)
        this.layoutParams = params
    }

    fun setDialogView() {
        loaderDialog = Dialog(this)
        loaderDialogView = View.inflate(this, R.layout.layout_loader, null)
        loaderDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        loaderDialog.window?.setBackgroundDrawableResource(R.color.transparent)
        loaderDialog.setCancelable(false)
        loaderDialog.setContentView(loaderDialogView)
    }

    fun showLoader() {
        loaderDialog.show()
    }

    fun hideLoader() {
        loaderDialog.dismiss()
    }


    fun showHomeErrorSnackBar(msg: String) {
        val snackbar = Snackbar.make(binding.root, msg, Snackbar.LENGTH_LONG)
            .setBackgroundTint(resources.getColor(R.color.button_bg))
            .setTextColor(resources.getColor(R.color.white))

        val layout = snackbar.view
        layout.setBackgroundColor(resources.getColor(R.color.button_bg))
        val text = layout.findViewById<View>(com.google.android.material.R.id.snackbar_text) as TextView

        //setting font color
        text.setTextColor(resources.getColor(R.color.white))
        var font: Typeface? = null

        //Setting font
        font = ResourcesCompat.getFont(this, R.font.mulish_bold)
        text.typeface = font

        snackbar.config(this)
        snackbar.show()

    }

    fun Snackbar.config(context: Context){
        val params = this.view.layoutParams as ViewGroup.MarginLayoutParams
        params.setMargins(-20, -20, -20,0)
        this.view.layoutParams = params

        this.view.background = context.getDrawable(R.drawable.snackbar_bg)

        ViewCompat.setElevation(this.view, 6f)
    }

    fun clearAllNotification() {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()
    }

    fun initiateShakeAnimation() {
        shake = AnimationUtils.loadAnimation(this, R.anim.shakeanim)
    }

    fun initializeCustomerIO() {
        val builder = CustomerIO.Builder(
            siteId = getString(R.string.customer_io_site_id),
            apiKey = getString(R.string.customer_io_api_key),
            appContext = application
        )
        builder.setCustomerIOUrlHandler(this)
        builder.build()
    }

    fun View.hideView() {
        this.visibility = View.GONE
    }

    fun View.showView() {
        this.visibility = View.VISIBLE
    }

    override fun handleCustomerIOUrl(uri: Uri): Boolean {
        Log.e("myurlis", "$uri")
        return false
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN ) {
            val v = currentFocus
            if (v is AppCompatEditText) {
                val outRect = Rect()
                v.getGlobalVisibleRect(outRect)
                if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    v.clearFocus()
                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(v.windowToken, 0)
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }
}