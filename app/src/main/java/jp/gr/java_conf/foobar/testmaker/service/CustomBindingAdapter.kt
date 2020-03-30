package jp.gr.java_conf.foobar.testmaker.service

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.TransitionManager
import com.google.firebase.storage.FirebaseStorage
import jp.gr.java_conf.foobar.testmaker.service.extensions.setImageWithGlide
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object CustomBindingAdapter {

    @BindingAdapter("android:testColor")
    @JvmStatic
    fun ImageButton.setTestColor(colorId: Int) {
        val drawable = ResourcesCompat.getDrawable(context.resources, R.drawable.circle, null) as GradientDrawable
        drawable.setColor(colorId)
        background = drawable
    }

    @BindingAdapter("android:circleTint")
    @JvmStatic
    fun ImageView.setCircleTint(color: Int) {
        val drawable = ResourcesCompat.getDrawable(context.resources, R.drawable.shape_circle, null) as GradientDrawable
        drawable.setColor(color)
        background = drawable
    }

    @BindingAdapter("android:isHasFixedSize")
    @JvmStatic
    fun RecyclerView.setIsHasFixedSize(isFixed: Boolean) {
        setHasFixedSize(isFixed)
    }

    @BindingAdapter("android:isVisible")
    @JvmStatic
    fun View.setIsVisible(isVisible: Boolean) {
        this.visibility = if (isVisible) View.VISIBLE else View.GONE
    }

    @BindingAdapter("android:onLongClick")
    @JvmStatic
    fun setOnLongClick(view: View, clickListener: View.OnLongClickListener?) {
        view.setOnLongClickListener(clickListener)
    }

    @BindingAdapter("android:animatedVisibility")
    @JvmStatic
    fun setAnimatedVisibility(view: View, isVisible: Boolean) {
        TransitionManager.beginDelayedTransition(view.rootView as ViewGroup)
        view.visibility = if (isVisible) View.VISIBLE else View.GONE
    }

    @BindingAdapter("android:customAnimatedVisibility", "android:duration")
    @JvmStatic
    fun setCustomAnimatedVisibility(view: View, isVisible: Boolean, duration: Int) {
        view.visibility = if (isVisible) View.VISIBLE else View.GONE
    }

    @BindingAdapter("android:srcWithGlide")
    @JvmStatic
    fun ImageView.setSrcWithGlide(src: String) {
        if (src.isEmpty()) {
            setImageResource(R.drawable.ic_insert_photo_white_24dp)
        } else {
            if (src.contains("/")) {
                val storage = FirebaseStorage.getInstance()
                val storageRef = storage.reference.child(src)
                setImageWithGlide(context, storageRef)
            } else {
                setImageWithGlide(context, context.getFileStreamPath(src))
            }
        }
    }

    @BindingAdapter("android:srcPlayWithGlide")
    @JvmStatic
    fun ImageView.setSrcPlayWithGlide(src: String) {
        if (src.isEmpty()) {
            setImageBitmap(null)
        } else {
            if (src.contains("/")) {
                val storage = FirebaseStorage.getInstance()
                val storageRef = storage.reference.child(src)
                setImageWithGlide(context, storageRef)
            } else {
                setImageWithGlide(context, context.getFileStreamPath(src))
            }
        }
    }

    @BindingAdapter("android:syncKeyBoard")
    @JvmStatic
    fun EditText.setSyncKeyBoard(isSync: Boolean) {
        if (!isSync) return
        val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        setOnFocusChangeListener { v, hasFocus ->

            if (hasFocus) {
                GlobalScope.launch {
                    delay(300)
                    inputMethodManager.showSoftInput(v, InputMethodManager.SHOW_FORCED)
                }
            } else {
                GlobalScope.launch {
                    delay(300)
                    inputMethodManager.hideSoftInputFromWindow(v.windowToken, 0)
                }
            }
        }

    }
}