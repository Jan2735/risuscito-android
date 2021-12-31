package it.cammino.risuscito.dialogs


import android.app.Dialog
import android.os.Bundle
import android.view.KeyEvent
import androidx.annotation.ArrayRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import it.cammino.risuscito.ui.LocaleManager
import java.io.Serializable

@Suppress("unused")
class ListChoiceDialogFragment : DialogFragment() {

    private val viewModel: DialogViewModel by viewModels({ requireActivity() })

    private val builder: Builder?
        get() = if (arguments?.containsKey(BUILDER_TAG) != true) null else arguments?.getSerializable(
            BUILDER_TAG
        ) as? Builder

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val mBuilder = builder
            ?: throw IllegalStateException("ListChoiceDialogFragment should be created using its Builder interface.")

        val dialog = MaterialAlertDialogBuilder(requireContext())
        dialog.setSingleChoiceItems(
            mBuilder.listArrayId,
            mBuilder.initialSelection
        ) { _, index ->
            viewModel.index = index
            viewModel.handled = false
            viewModel.state.value = DialogState.Positive(this)
        }

        if (mBuilder.mTitle != 0)
            dialog.setTitle(mBuilder.mTitle)

        mBuilder.mContent?.let {
            dialog.setMessage(it)
        }

        mBuilder.mPositiveButton?.let {
            dialog.setPositiveButton(it, null)
        }

        mBuilder.mNegativeButton?.let {
            dialog.setNegativeButton(it) { _, _ ->
                viewModel.handled = false
                viewModel.state.value = DialogState.Negative(this)
            }
        }

        dialog.setCancelable(mBuilder.mCanceable)

        dialog.setOnKeyListener { arg0, keyCode, event ->
            var returnValue = false
            if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                arg0.cancel()
                returnValue = true
            }
            returnValue
        }

        return dialog.show()
    }

    fun cancel() {
        dialog?.cancel()
    }

    override fun dismiss() {
        super.dismissAllowingStateLoss()
    }

    class Builder(context: AppCompatActivity, internal val mTag: String) : Serializable {

        @Transient
        private val mContext: AppCompatActivity = context
        internal var mTitle = 0
        internal var mContent: CharSequence? = null
        internal var mPositiveButton: CharSequence? = null
        internal var mNegativeButton: CharSequence? = null
        internal var mCanceable = false

        internal var listArrayId: Int = 0
        internal var initialSelection: Int = -1

        fun listArrayId(@ArrayRes res: Int): Builder {
            listArrayId = res
            return this
        }

        fun initialSelection(sel: Int): Builder {
            initialSelection = sel
            return this
        }

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
            mPositiveButton = this.mContext.resources.getText(text).toString().replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(
                    LocaleManager.getSystemLocale(mContext.resources)
                ) else it.toString()
            }
            return this
        }

        fun negativeButton(@StringRes text: Int): Builder {
            mNegativeButton = this.mContext.resources.getText(text).toString().replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(
                    LocaleManager.getSystemLocale(mContext.resources)
                ) else it.toString()
            }
            return this
        }

        fun setCanceable(): Builder {
            mCanceable = true
            return this
        }

    }

    companion object {

        private const val BUILDER_TAG = "bundle_builder"

        private fun newInstance() = ListChoiceDialogFragment()

        private fun newInstance(builder: Builder): ListChoiceDialogFragment {
            return newInstance().apply {
                arguments = bundleOf(
                    Pair(BUILDER_TAG, builder)
                )
            }
        }

        fun show(builder: Builder, fragmentManger: FragmentManager) {
            newInstance(builder).run {
                show(fragmentManger, builder.mTag)
            }
        }

    }

    class DialogViewModel : ViewModel() {
        var index: Int = 0
        var handled = true
        val state = MutableLiveData<DialogState<ListChoiceDialogFragment>>()
    }

}
