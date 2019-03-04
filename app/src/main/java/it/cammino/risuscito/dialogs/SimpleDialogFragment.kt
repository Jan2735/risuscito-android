package it.cammino.risuscito.dialogs


import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.KeyEvent
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import java.io.Serializable

@Suppress("unused")
class SimpleDialogFragment : DialogFragment() {

    private var mCallback: SimpleCallback? = null

    private val builder: Builder?
        get() = if (arguments == null || !arguments!!.containsKey("builder")) null else arguments!!.getSerializable("builder") as Builder

    override fun onDestroyView() {
        if (dialog != null && retainInstance)
            dialog.setDismissMessage(null)
        super.onDestroyView()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    @SuppressLint("CheckResult")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val mBuilder = builder
                ?: throw IllegalStateException("SimpleDialogFragment should be created using its Builder interface.")

        if (mCallback == null)
            mCallback = mBuilder.mListener

        val dialog = MaterialDialog(activity!!)

        if (mBuilder.mTitle != 0)
            dialog.title(res = mBuilder.mTitle)

        if (!mBuilder.mAutoDismiss)
            dialog.noAutoDismiss()

        if (mBuilder.mContent != null)
            dialog.message(text = mBuilder.mContent!!)

//        if (mBuilder.mPositiveButton != null) {
        mBuilder.mPositiveButton?.let {
            dialog.positiveButton(text = it) {
                mCallback!!.onPositive(mBuilder.mTag)
            }
        }

//        if (mBuilder.mNegativeButton != null) {
        mBuilder.mNegativeButton?.let {
            dialog.negativeButton(text = it) {
                mCallback!!.onNegative(mBuilder.mTag)
            }
        }

        if (mBuilder.mCustomView != 0) {
            dialog.customView(mBuilder.mCustomView)
        }

        dialog.setCancelable(mBuilder.mCanceable)

        dialog.setOnKeyListener(DialogInterface.OnKeyListener
        { arg0, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                arg0.cancel()
                return@OnKeyListener true
            }
            false
        })

        return dialog
    }

    @SuppressLint("CheckResult")
    fun setContent(@StringRes res: Int) {
        (dialog as MaterialDialog).message(res)
    }

    fun setmCallback(callback: SimpleCallback) {
        mCallback = callback
    }

    fun cancel() {
        dialog.cancel()
    }

    fun setOnCancelListener(listener: DialogInterface.OnCancelListener) {
        dialog.setOnCancelListener(listener)
    }

    override fun dismiss() {
        super.dismissAllowingStateLoss()
    }

    override fun onCancel(dialog: DialogInterface?) {
        super.onCancel(dialog)
        val mBuilder = builder
        if (mBuilder?.mCanceListener == true)
            mCallback!!.onPositive(mBuilder.mTag)
    }

    class Builder(context: AppCompatActivity, @field:Transient internal var mListener: SimpleCallback, internal val mTag: String) : Serializable {

        @Transient
        private val mContext: AppCompatActivity = context
        internal var mTitle = 0
        internal var mContent: CharSequence? = null
        internal var mPositiveButton: CharSequence? = null
        internal var mNegativeButton: CharSequence? = null
        internal var mCanceable = false
        internal var mAutoDismiss = true
        internal var mCanceListener = false
        internal var mCustomView = 0

        fun title(@StringRes text: Int): Builder {
            mTitle = text
            return this
        }

        fun content(@StringRes content: Int): Builder {
            mContent = this.mContext.resources.getText(content)
            return this
        }

        fun content(content: String): Builder {
            mContent = content
            return this
        }

        fun positiveButton(@StringRes text: Int): Builder {
            mPositiveButton = this.mContext.resources.getText(text)
            return this
        }

        fun negativeButton(@StringRes text: Int): Builder {
            mNegativeButton = this.mContext.resources.getText(text)
            return this
        }

        fun setHasCancelListener(): Builder {
            mCanceListener = true
            return this
        }

        fun setCanceable(): Builder {
            mCanceable = true
            return this
        }

        fun setAutoDismiss(autoDismiss: Boolean): Builder {
            mAutoDismiss = autoDismiss
            return this
        }

        fun setCustomView(@LayoutRes customView: Int): Builder {
            mCustomView = customView
            return this
        }

        fun build(): SimpleDialogFragment {
            val dialog = SimpleDialogFragment()
            val args = Bundle()
            args.putSerializable("builder", this)
            dialog.arguments = args
            return dialog
        }

        fun show(): SimpleDialogFragment {
            val dialog = build()
            dialog.show(mContext)
            return dialog
        }
    }

    private fun dismissIfNecessary(context: AppCompatActivity, tag: String) {
        val frag = context.supportFragmentManager.findFragmentByTag(tag)
//        if (frag != null) {
        frag?.let {
            (it as DialogFragment).dismiss()
            context.supportFragmentManager.beginTransaction()
                    .remove(it).commit()
        }
    }

    fun show(context: AppCompatActivity): SimpleDialogFragment {
        val builder = builder
        builder?.let {
            dismissIfNecessary(context, it.mTag)
            show(context.supportFragmentManager, it.mTag)
        }
        return this
    }

    interface SimpleCallback {
        fun onPositive(tag: String)
        fun onNegative(tag: String)
    }

    companion object {

        fun findVisible(context: AppCompatActivity, tag: String): SimpleDialogFragment? {
            val frag = context.supportFragmentManager.findFragmentByTag(tag)
            return if (frag != null && frag is SimpleDialogFragment) frag else null
        }
    }

}
