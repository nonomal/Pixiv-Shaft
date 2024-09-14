package ceui.pixiv.ui.common

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import ceui.lisa.R
import ceui.lisa.databinding.FragmentImgUrlBinding
import ceui.pixiv.ui.task.LoadTask
import ceui.pixiv.ui.task.NamedUrl
import ceui.pixiv.ui.task.TaskPool
import ceui.refactor.viewBinding
import com.github.panpf.zoomimage.SketchZoomImageView
import com.google.android.material.progressindicator.CircularProgressIndicator
import kotlinx.coroutines.launch

class ImgUrlFragment : ImgDisplayFragment(R.layout.fragment_img_url) {

    private val binding by viewBinding(FragmentImgUrlBinding::bind)
    private val args by navArgs<ImgUrlFragmentArgs>()
    override val downloadButton: View
        get() = binding.download
    override val progressCircular: CircularProgressIndicator
        get() = binding.progressCircular
    override val displayImg: SketchZoomImageView
        get() = binding.image

    override fun displayName(): String {
        return args.displayName
    }

    override fun contentUrl(): String {
        return args.url
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val infoViews = listOf(
            binding.download,
            binding.toolbarLayout.root,
            binding.topShadow,
            binding.bottomShadow
        )
        if (parentFragment is ViewPagerFragment) {
            infoViews.forEach {
                it.isVisible = false
            }
        } else {
            setUpFullScreen(
                viewModel,
                infoViews,
                binding.toolbarLayout
            )
        }
    }
}
