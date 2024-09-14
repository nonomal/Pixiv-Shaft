package ceui.loxia.novel

import androidx.core.content.ContextCompat
import ceui.lisa.R
import ceui.lisa.activities.Shaft
import ceui.lisa.annotations.ItemHolder
import ceui.lisa.databinding.CellNovelImageBinding
import ceui.lisa.databinding.CellNovelTextBinding
import ceui.lisa.utils.GlideUrlChild
import ceui.lisa.utils.PixivOperate
import ceui.loxia.NovelImages
import ceui.loxia.WebNovel
import ceui.pixiv.ui.common.ListItemHolder
import ceui.pixiv.ui.common.ListItemViewHolder
import ceui.refactor.setOnClick
import com.bumptech.glide.Glide
import com.bumptech.glide.load.model.GlideUrl
import java.util.HashMap

class NovelTextHolder(val text: String, val textColor: Int) : ListItemHolder() {
    override fun getItemId(): Long {
        return (text.hashCode() + textColor).toLong()
    }
}

@ItemHolder(NovelTextHolder::class)
class NovelTextViewHolder(bd: CellNovelTextBinding) : ListItemViewHolder<CellNovelTextBinding, NovelTextHolder>(bd) {

    override fun onBindViewHolder(holder: NovelTextHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        binding.novelText.text = holder.text
        binding.novelText.setTextColor(holder.textColor)
    }
}

class NovelImageHolder(
    val type: Int,
    val id: Long,
    val indexInIllust: Int,
    val webNovel: WebNovel
) : ListItemHolder() {


    object Type {
        const val UploadedImage = 1
        const val PixivImage = 2
    }
}

@ItemHolder(NovelImageHolder::class)
class NovelImageViewHolder(private val bd: CellNovelImageBinding) : ListItemViewHolder<CellNovelImageBinding, NovelImageHolder>(bd) {

    override fun onBindViewHolder(holder: NovelImageHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        if (holder.type == NovelImageHolder.Type.UploadedImage) {
            val urls = holder.webNovel.images?.get(holder.id.toString())?.urls
            val url = urls?.get(NovelImages.Size.Size1200x1200)
            Glide.with(binding.novelImage).load(GlideUrlChild(url)).placeholder(R.drawable.image_place_holder).into(binding.novelImage)
        } else if (holder.type == NovelImageHolder.Type.PixivImage) {
            val urls = if (holder.indexInIllust == 0) {
                holder.webNovel.illusts?.get(holder.id.toString())?.illust?.images?.medium
            } else {
                holder.webNovel.illusts?.get("${holder.id}-${holder.indexInIllust}")?.illust?.images?.medium
            }
            binding.novelImage.setOnClick {
                PixivOperate.getIllustByID(Shaft.sUserModel, holder.id, binding.novelImage.context)
            }
            Glide.with(binding.novelImage).load(GlideUrlChild(urls)).placeholder(R.drawable.image_place_holder).into(binding.novelImage)
        }
    }
}