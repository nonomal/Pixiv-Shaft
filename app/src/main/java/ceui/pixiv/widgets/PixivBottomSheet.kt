package ceui.pixiv.widgets

import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import ceui.lisa.R
import ceui.refactor.screenHeight
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlin.math.roundToInt

open class PixivBottomSheet(layoutId: Int) : BottomSheetDialogFragment(layoutId) {

    private val fragmentResultStore by activityViewModels<FragmentResultStore>()
    protected val viewModel by activityViewModels<DialogViewModel>()

    override fun onStart() {
        super.onStart()
        val bottomSheet = dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) ?: return
        val behavior = BottomSheetBehavior.from(bottomSheet)
        behavior.skipCollapsed = true
        behavior.maxHeight = (screenHeight * 0.75F).roundToInt()
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }
}