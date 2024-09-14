package ceui.pixiv.ui.novel

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import ceui.lisa.R
import ceui.lisa.activities.Shaft
import ceui.lisa.databinding.FragmentPixivListBinding
import ceui.lisa.fragments.WebNovelParser
import ceui.loxia.Client
import ceui.loxia.KListShow
import ceui.loxia.Novel
import ceui.loxia.ObjectPool
import ceui.loxia.WebNovel
import ceui.pixiv.ui.common.DataSource
import ceui.pixiv.ui.common.PixivFragment
import ceui.pixiv.ui.common.setUpRefreshState
import ceui.pixiv.ui.list.pixivListViewModel
import ceui.refactor.viewBinding

class NovelTextViewModel : ViewModel() {
    var webNovel: WebNovel? = null
}

class NovelTextFragment : PixivFragment(R.layout.fragment_pixiv_list) {

    private val args by navArgs<NovelTextFragmentArgs>()
    private val binding by viewBinding(FragmentPixivListBinding::bind)
    private val novelViewModel by viewModels<NovelTextViewModel>()
    private val viewModel by pixivListViewModel({ Pair(novelViewModel, args.novelId) }) { (vm, novelId) ->
        DataSource<String, KListShow<String>>(
            dataFetcher = {
                val html = Client.appApi.getNovelText(novelId).string()
                object : KListShow<String> {
                    override val displayList: List<String>
                        get() {
                            val webNovel = WebNovelParser.parsePixivObject(html)?.novel
                            vm.webNovel = webNovel
                            return webNovel?.text?.split("\n") ?: listOf()
                        }
                    override val nextPageUrl: String?
                        get() = null
                }
            },
            itemMapper = { text -> WebNovelParser.buildNovelHolders(vm.webNovel, text) }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpRefreshState(binding, viewModel)
        ObjectPool.get<Novel>(args.novelId).observe(viewLifecycleOwner) {
            binding.toolbarLayout.naviTitle.text = it.title
        }
        binding.listView.layoutManager = LinearLayoutManager(requireContext())
    }
}