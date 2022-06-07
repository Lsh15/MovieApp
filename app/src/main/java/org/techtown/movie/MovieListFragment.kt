package org.techtown.movie

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import kotlinx.android.synthetic.main.fragment_1.view.*
import kotlinx.android.synthetic.main.fragment_2.view.*
import org.techtown.movie.data.MovieList

class MovieListFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_1, container, false)

        activity?.run {
            rootView.pager.adapter = PagerAdapter(supportFragmentManager,lifecycle)
            rootView.pager.orientation = ViewPager2.ORIENTATION_HORIZONTAL

            rootView.pager.clipToPadding = false
            rootView.pager.setPadding(150, 0, 150, 0)
            rootView.pager.offscreenPageLimit = 3

            rootView.indicator.setViewPager(rootView.pager);
//            rootView.indicator.createIndicators(3,0);
            rootView.indicator.createIndicators(MovieList.data.size,0)

            rootView.pager.registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)

                    println("${position+1} 페이지 선택됨")

                    rootView.indicator.animatePageSelected(position)
                }
            })
        }

        return rootView
    }


    inner class PagerAdapter : FragmentStateAdapter {
        constructor(fm: FragmentManager, lc: Lifecycle) : super(fm, lc)

        override fun getItemCount() = MovieList.data.size

        override fun createFragment(position: Int): Fragment {
            var movieData = MovieList.data[position]
            var imageId:String = movieData.tmdbMovieResult?.poster_path?: ""
            var title:String = movieData.movieInfo?.movieNm?: ""
            var details1:String = movieData.movieInfo?.audiCnt?: ""
            var details2:String = movieData.movieInfo?.showCnt?: ""
            var fragment:PageFragment? = null

            return PageFragment.newInstance(position,imageId,title,details1,details2)
        }

    }
}