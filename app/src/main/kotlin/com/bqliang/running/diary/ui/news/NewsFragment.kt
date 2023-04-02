package com.bqliang.running.diary.ui.news

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import cn.leancloud.LCObject
import cn.leancloud.LCQuery
import com.bqliang.running.diary.databinding.FragmentNewsListBinding
import com.bqliang.running.diary.utils.startActivity
import io.reactivex.Observer
import io.reactivex.disposables.Disposable

class NewsFragment : Fragment() {

    private var _binding: FragmentNewsListBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = FragmentNewsListBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = NewsListAdapter() { news ->
            requireContext().startActivity<NewsDetailsActivity>() {
                putExtra(NewsDetailsActivity.NEWS_TITLE, news.title)
                putExtra(NewsDetailsActivity.NEWS_IMAGE_URL, news.imgUrl)
                putExtra(NewsDetailsActivity.NEWS_MD_CONTENT, news.mdContent)
            }
        }

        binding.recyclerView.adapter = adapter

        LCQuery<LCObject>("News").findInBackground().subscribe(
            object : Observer<List<LCObject>> {
                override fun onSubscribe(d: Disposable) {}

                override fun onNext(news: List<LCObject>) {
                    news.map { news ->
                        News(
                            title = news.getString("title"),
                            mdContent = news.getString("md_content"),
                            imgUrl = news.getLCFile("img").url,
                            highlight = news.getString("highlight")
                        )
                    }.also { adapter.submitList(it) }
                }

                override fun onError(e: Throwable) {
                    e.printStackTrace()
                }

                override fun onComplete() {  }
            }
        )
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}