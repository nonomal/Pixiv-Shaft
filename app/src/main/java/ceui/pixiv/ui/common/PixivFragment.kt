package ceui.pixiv.ui.common

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.core.view.updatePaddingRelative
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import ceui.lisa.R
import ceui.lisa.activities.UserActivity
import ceui.lisa.databinding.FragmentPixivListBinding
import ceui.lisa.databinding.LayoutToolbarBinding
import ceui.lisa.utils.Common
import ceui.lisa.utils.Params
import ceui.lisa.view.SpacesItemDecoration
import ceui.loxia.Article
import ceui.loxia.Client
import ceui.loxia.Illust
import ceui.loxia.Novel
import ceui.loxia.ObjectPool
import ceui.loxia.ObjectType
import ceui.loxia.RefreshHint
import ceui.loxia.RefreshState
import ceui.loxia.Tag
import ceui.loxia.User
import ceui.loxia.getHumanReadableMessage
import ceui.loxia.launchSuspend
import ceui.loxia.observeEvent
import ceui.loxia.pushFragment
import ceui.pixiv.ui.bottom.ItemListDialogFragment
import ceui.pixiv.ui.circles.CircleFragmentArgs
import ceui.pixiv.ui.list.PixivListViewModel
import ceui.pixiv.ui.novel.NovelTextFragmentArgs
import ceui.pixiv.ui.search.SearchViewPagerFragmentArgs
import ceui.pixiv.ui.task.FetchAllTask
import ceui.pixiv.ui.user.UserActionReceiver
import ceui.pixiv.ui.user.UserProfileFragmentArgs
import ceui.pixiv.ui.web.WebFragmentArgs
import ceui.pixiv.ui.works.IllustFragmentArgs
import ceui.pixiv.widgets.DialogViewModel
import ceui.pixiv.widgets.FragmentResultStore
import ceui.pixiv.widgets.MenuItem
import ceui.pixiv.widgets.PixivBottomSheet
import ceui.pixiv.widgets.TagsActionReceiver
import ceui.pixiv.widgets.showActionMenu
import ceui.refactor.ppppx
import ceui.refactor.setOnClick
import com.scwang.smart.refresh.footer.ClassicsFooter
import com.scwang.smart.refresh.header.FalsifyFooter
import com.scwang.smart.refresh.header.MaterialHeader

open class PixivFragment(layoutId: Int) : Fragment(layoutId), IllustCardActionReceiver,
    UserActionReceiver, TagsActionReceiver, ArticleActionReceiver, NovelActionReceiver {

    private val fragmentViewModel: NavFragmentViewModel by viewModels()
    private val fragmentResultStore by activityViewModels<FragmentResultStore>()

    open fun onViewFirstCreated(view: View) {

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fragmentViewModel // force create

        if (fragmentViewModel.viewCreatedTime.value == null) {
            onViewFirstCreated(view)
        }

        fragmentViewModel.viewCreatedTime.value = System.currentTimeMillis()
    }

    override fun onClickIllustCard(illust: Illust) {
        pushFragment(
            R.id.navigation_illust,
            IllustFragmentArgs(illust.id).toBundle()
        )
    }

    override fun onClickUser(id: Long) {
        try {
            pushFragment(R.id.navigation_user_profile, UserProfileFragmentArgs(id).toBundle())
        } catch (ex: Exception) {
            ex.printStackTrace()
            val userIntent = Intent(
                requireContext(),
                UserActivity::class.java
            )
            userIntent.putExtra(
                Params.USER_ID, id.toInt()
            )
            startActivity(userIntent)
        }
    }

    override fun onClickTag(tag: Tag, objectType: String) {
        if (objectType == ObjectType.NOVEL) {

        } else {
//            pushFragment(R.id.navigation_search_viewpager, SearchViewPagerFragmentArgs(
//                keyword = tag.name ?: "",
//            ).toBundle())
            pushFragment(R.id.navigation_circle, CircleFragmentArgs(
                keyword = tag.name ?: "",
            ).toBundle())
        }
    }

    override fun onClickArticle(article: Article) {
        article.article_url?.let {
            pushFragment(R.id.navigation_web_fragment, WebFragmentArgs(article.article_url).toBundle())
        }
    }

    override fun onClickNovel(novel: Novel) {
        pushFragment(R.id.navigation_novel_text, NovelTextFragmentArgs(novel.id).toBundle())
    }
}

interface ViewPagerFragment {

}

interface ITitledViewPager : ViewPagerFragment {
    fun getTitleLiveData(index: Int): MutableLiveData<String>
}

interface HomeTabContainer : ViewPagerFragment {
    fun bottomExtraSpacing(): Int = 100.ppppx
}

fun Fragment.setUpToolbar(binding: LayoutToolbarBinding, content: ViewGroup) {
    val parentFrag = parentFragment
    if (parentFrag is ViewPagerFragment) {
        binding.toolbarLayout.isVisible = false
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            if (parentFrag is HomeTabContainer) {
                content.updatePadding(0, 0, 0, insets.bottom + parentFrag.bottomExtraSpacing())
            } else {
                content.updatePadding(0, 0, 0, insets.bottom)
            }
            WindowInsetsCompat.CONSUMED
        }
    } else {
        binding.toolbarLayout.isVisible = true
        if (activity is HomeActivity) {
            binding.naviBack.setOnClick {
                findNavController().popBackStack()
            }
        } else {
            binding.toolbarLayout.background = ColorDrawable(
                Common.resolveThemeAttribute(requireContext(), androidx.appcompat.R.attr.colorPrimary)
            )
            requireActivity().finish()
        }
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.toolbarLayout.updatePaddingRelative(top = insets.top)
            content.updatePadding(0, 0, 0, insets.bottom)
            WindowInsetsCompat.CONSUMED
        }
    }
}

fun Fragment.setUpRefreshState(binding: FragmentPixivListBinding, viewModel: RefreshOwner) {
    setUpToolbar(binding.toolbarLayout, binding.listView)
    val ctx = requireContext()
    binding.refreshLayout.setRefreshHeader(MaterialHeader(ctx))
    binding.refreshLayout.setOnRefreshListener {
        viewModel.refresh(RefreshHint.PullToRefresh)
    }
    viewModel.refreshState.observe(viewLifecycleOwner) { state ->
        if (state !is RefreshState.LOADING) {
            binding.refreshLayout.finishRefresh()
            binding.refreshLayout.finishLoadMore()
        }
        binding.emptyLayout.isVisible = state is RefreshState.LOADED && !state.hasContent
        if (state is RefreshState.LOADED) {
            binding.refreshLayout.setEnableLoadMore(true)
            if (state.hasNext) {
                binding.refreshLayout.setRefreshFooter(ClassicsFooter(ctx))
                if (viewModel is LoadMoreOwner) {
                    binding.refreshLayout.setOnLoadMoreListener {
                        viewModel.loadMore()
                    }
                } else {
                    binding.refreshLayout.setRefreshFooter(FalsifyFooter(ctx))
                }
            } else {
                binding.refreshLayout.setRefreshFooter(FalsifyFooter(ctx))
            }
        } else {
            binding.refreshLayout.setEnableLoadMore(false)
        }
        val shouldShowLoading = state is RefreshState.LOADING && (
                state.refreshHint == RefreshHint.InitialLoad ||
                        state.refreshHint == RefreshHint.ErrorRetry
                )
        binding.loadingLayout.isVisible = shouldShowLoading
        if (shouldShowLoading) {
            binding.progressCircular.playAnimation()
        } else {
            binding.progressCircular.cancelAnimation()
        }
        binding.errorLayout.isVisible = state is RefreshState.ERROR
        binding.errorRetryButton.setOnClick {
            viewModel.refresh(RefreshHint.ErrorRetry)
        }
        if (state is RefreshState.ERROR) {
            binding.errorText.text = state.exception.getHumanReadableMessage(ctx)
        }
    }
    if (viewModel is HoldersContainer) {
        val adapter = CommonAdapter(viewLifecycleOwner)
        binding.listView.adapter = adapter
        viewModel.holders.observe(viewLifecycleOwner) { holders ->
            adapter.submitList(holders)
        }
//        viewModel.liveNextUrl.observe(viewLifecycleOwner) { url ->
//            binding.nextUrl.text = url
//        }
    }
}


private fun calculateTotalPages(totalItems: Int, pageSize: Int): Int {
    return if (totalItems % pageSize == 0) {
        totalItems / pageSize
    } else {
        (totalItems / pageSize) + 1
    }
}

fun Fragment.setUpSizedList(binding: FragmentPixivListBinding, dataSource: DataSourceContainer<*, *>, listFullSize: Int) {
    val dialogViewModel by activityViewModels<DialogViewModel>()
    if (listFullSize > 30) {
        binding.listSetting.isVisible = true
        dialogViewModel.triggerOffsetPageEvent.observeEvent(this) { page ->
            launchSuspend {
                dataSource.dataSource().loadOffsetData(page)
            }
        }
        binding.listSetting.setOnClick {
            showActionMenu {
                add(
                    MenuItem("按照页数查看作品", "实验性功能，测试中") {
                        ItemListDialogFragment.newInstance(calculateTotalPages(listFullSize, 30))
                            .show(childFragmentManager, "Tag")
                    }
                )
            }
        }
    } else {
        binding.listSetting.isVisible = false
    }
}

fun Fragment.setUpStaggerLayout(binding: FragmentPixivListBinding, viewModel: PixivListViewModel<*, *>) {
    setUpRefreshState(binding, viewModel)
    binding.listView.addItemDecoration(SpacesItemDecoration(4.ppppx))
    binding.listView.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
}
