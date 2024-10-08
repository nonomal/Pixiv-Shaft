package ceui.pixiv.ui.task

import android.content.Context
import android.graphics.ColorSpace.Named
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ceui.lisa.utils.Common
import ceui.lisa.utils.GlideUrlChild
import ceui.pixiv.ui.common.saveImageToGallery
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.jessyan.progressmanager.ProgressListener
import me.jessyan.progressmanager.ProgressManager
import me.jessyan.progressmanager.body.ProgressInfo
import java.io.File
import java.util.jar.Attributes.Name

data class NamedUrl(
    val name: String,
    val url: String,
)

class DownloadAllTask(
    context: Context,
    contentsProvider: () -> List<NamedUrl>
) {
    val pendingTasks = mutableListOf<DownloadTask>()

    init {
        val contents = contentsProvider()
        contents.forEach { content ->
            pendingTasks.add(DownloadTask(content, context))
        }
    }

    fun go() {
        MainScope().launch {
            pendingTasks.forEach {
                it.execute()
            }
        }
    }
}

open class LoadTask(val content: NamedUrl, private val context: Context) {
    private val _status = MutableLiveData<TaskStatus>(TaskStatus.NotStart)
    val status: LiveData<TaskStatus> = _status

    private val _file = MutableLiveData<File>()
    val file: LiveData<File> = _file

    suspend fun execute() {
        if (_status.value is TaskStatus.Executing || _status.value is TaskStatus.Finished) {
            return
        }
        withContext(Dispatchers.IO) {
            try {
                _status.postValue(TaskStatus.Executing(0))
                ProgressManager.getInstance().addResponseListener(content.url, object : ProgressListener {
                    override fun onProgress(progressInfo: ProgressInfo) {
                        Common.showLog("dsaasdasw2 ${progressInfo.percent}")
                        if (progressInfo.isFinish || progressInfo.percent == 100) {
                            _status.postValue(TaskStatus.Finished)
                        } else {
                            _status.postValue(TaskStatus.Executing(progressInfo.percent))
                        }
                    }

                    override fun onError(id: Long, e: Exception) {
                        _status.postValue(TaskStatus.Error(e))
                    }
                })

                val file = Glide.with(context)
                    .asFile()
                    .load(GlideUrlChild(content.url))
                    .listener(object : RequestListener<File> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<File>,
                            isFirstResource: Boolean
                        ): Boolean {
                            e?.let {
                                _status.postValue(TaskStatus.Error(it))
                            }
                            return false
                        }

                        override fun onResourceReady(
                            resource: File,
                            model: Any,
                            target: Target<File>?,
                            dataSource: DataSource,
                            isFirstResource: Boolean
                        ): Boolean {
                            Common.showLog("dsaasdw2 onResourceReady ${dataSource.name} ${resource.path}")
                            return false
                        }
                    })
                    .submit()
                    .get()
                _file.postValue(file)
                onFilePrepared(file)
                _status.postValue(TaskStatus.Finished)
            } catch (ex: Exception) {
                _status.postValue(TaskStatus.Error(ex))
                ex.printStackTrace()
            } finally {
                optionalDelay()
            }
        }
    }

    open fun onFilePrepared(file: File) {

    }

    open suspend fun optionalDelay() {

    }
}


class DownloadTask(content: NamedUrl, private val context: Context) :
    LoadTask(content, context) {

    override fun onFilePrepared(file: File) {
        super.onFilePrepared(file)
        saveImageToGallery(context, file, content.name)
    }
}

sealed class TaskStatus {
    data object NotStart : TaskStatus()
    data class Executing(val percentage: Int) : TaskStatus()
    data object Finished : TaskStatus()
    data class Error(val exception: Exception) : TaskStatus()
}