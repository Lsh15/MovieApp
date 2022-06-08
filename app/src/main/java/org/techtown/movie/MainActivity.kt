package org.techtown.movie

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_main.*
import org.techtown.movie.data.*

class MainActivity : AppCompatActivity() {

    companion object {
        var requestQueue: RequestQueue? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestQueue =  Volley.newRequestQueue(applicationContext)

        setSupportActionBar(toolbar)

        val toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.drawer_open,
            R.string.drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navigationView.setNavigationItemSelectedListener {
            when(it.itemId) {
                R.id.item1 -> {
                    Toast.makeText(this, "영화목록 선택됨.", Toast.LENGTH_LONG).show()
                    onFragmentSelected(FragmentCallback.FragmentItem.ITEM_LIST, null)
                }
                R.id.item2 -> {
                    Toast.makeText(this, "예매순 선택됨.", Toast.LENGTH_LONG).show()
                    onFragmentSelected(FragmentCallback.FragmentItem.ITEM2, null)
                }
                R.id.item3 -> {
                    Toast.makeText(this,"영화관 선택됨",Toast.LENGTH_LONG).show()
                    onFragmentSelected(FragmentCallback.FragmentItem.ITEM3, null)
                }
                R.id.item4 -> {
                    Toast.makeText(this,"즐겨찾기 선택됨",Toast.LENGTH_LONG).show()
                    onFragmentSelected(FragmentCallback.FragmentItem.ITEM4, null)
                }
            }

            drawerLayout.closeDrawer(GravityCompat.START)
            return@setNavigationItemSelectedListener true
        }



        bottom_navigation.selectedItemId = R.id.tab1
        bottom_navigation.setOnNavigationItemSelectedListener {
            when(it.itemId){
                R.id.tab1 -> {
                    onFragmentSelected(FragmentCallback.FragmentItem.ITEM_LIST,null)
                }
                R.id.tab2 -> {
                    onFragmentSelected(FragmentCallback.FragmentItem.ITEM2,null)
                }
                R.id.tab3 -> {
                    onFragmentSelected(FragmentCallback.FragmentItem.ITEM3,null)
                }
                R.id.tab4 -> {
                    onFragmentSelected(FragmentCallback.FragmentItem.ITEM4,null)
                }

            }
            return@setOnNavigationItemSelectedListener true
        }

        requestBoxOffice()
        //supportFragmentManager.beginTransaction().add(R.id.container, Fragment1()).commit()
    }

    fun requestBoxOffice() {
        val apiKey = "77130fcd02cfa15b611b773fa72da114"
        val targetDate = "20220101"
        val url = "http://www.kobis.or.kr/kobisopenapi/webservice/rest/boxoffice/searchDailyBoxOfficeList.json?key=${apiKey}&targetDt=${targetDate}"

        val request = object: StringRequest(
            Request.Method.GET,
            url,
            {
                //output1.append("\n응답 -> ${it}")

                processResponse(it)
            },
            {
                println("\n에러 -> ${it.message}")
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["userid"] = "john"

                return params
            }
        }

        request.setShouldCache(false)
        requestQueue?.add(request)
        println("\n영화 일별 박스오피스 요청함")
    }

    fun processResponse(response:String) {
        val gson = Gson()
        val boxOffice = gson.fromJson(response, BoxOffice::class.java)
        println("\n영화 정보의 수: " + boxOffice.boxOfficeResult.dailyBoxOfficeList.size)

        requestDetails(boxOffice.boxOfficeResult.dailyBoxOfficeList)
    }

    fun requestDetails(dailyBoxOfficeList:ArrayList<MovieInfo>) {
        MovieList.data.clear()
        for (index in 0..4) {
            var movieData = MovieData(dailyBoxOfficeList[index], null, null)
            MovieList.data.add(movieData)

            sendDetails(index, dailyBoxOfficeList[index].movieCd)
        }
    }

    fun sendDetails(index:Int, movieCd:String?) {
        if (movieCd != null) {
            val apiKey = "77130fcd02cfa15b611b773fa72da114"
            val url = "http://www.kobis.or.kr/kobisopenapi/webservice/rest/movie/searchMovieInfo.json?key=${apiKey}&movieCd=${movieCd}"

            val request = object: StringRequest(
                Request.Method.GET,
                url,
                {
                    println("\n응답 -> ${it}")

                    processDetailsResponse(index, it)
                },
                {
                    println("\n에러 -> ${it.message}")
                }
            ) {}

            request.setShouldCache(false)
            requestQueue?.add(request)
            println("\n영화 상세정보 요청함")
        }
    }

    fun processDetailsResponse(index:Int, response:String) {
        val gson = Gson()
        val movieInfoDetails = gson.fromJson(response, MovieInfoDetails::class.java)
        val movieDetails = movieInfoDetails.movieInfoResult.movieInfo

        println("\n영화 제목과 제작국가 : ${movieDetails.movieNm}, ${movieDetails.movieNmEn}, ${movieDetails.nations[0].nationNm}")

        MovieList.data[index].movieDetails = movieDetails
        requestTMDBSearch(index, movieDetails)
    }

    fun requestTMDBSearch(index:Int, movieDetails:MovieDetails) {
        var movieName = movieDetails.movieNm
        if (movieDetails.nations[0].nationNm != "한국") {
            movieName = movieDetails.movieNmEn
        }

        sendTMDBSearch(index, movieName)
    }

    fun sendTMDBSearch(index:Int, movieName:String?) {
        if (movieName != null) {
            val apiKey = "af539719f139a414013b429c8407e77c"
            val url = "https://api.themoviedb.org/3/search/movie?api_key=${apiKey}&query=${movieName}&language=ko-KR&page=1"

            val request = object: StringRequest(
                Request.Method.GET,
                url,
                {
                    println("\n응답 -> ${it}")

                    processTMDBSearchResponse(index, it)
                },
                {
                    println("\n에러 -> ${it.message}")
                }
            ) {}

            request.setShouldCache(false)
            requestQueue?.add(request)
            println("\nTMDB 영화 검색 요청함")
        }
    }

    fun processTMDBSearchResponse(index:Int, response:String) {
        val gson = Gson()
        val tmdbMovieDetails = gson.fromJson(response, TmdbMovieDetails::class.java)
        val movieResult = tmdbMovieDetails.results[0]

        println("\n영화 id, 포스터, 줄거리 : ${movieResult.id}, ${movieResult.poster_path}, ${movieResult.overview}")
        MovieList.data[index].tmdbMovieResult = movieResult

        // 화면에 프래그먼트 표시
        supportFragmentManager.beginTransaction().add(R.id.container, MovieListFragment()).commit()
        //setPosterImage(index, movieResult.poster_path)
    }

     fun onFragmentSelected(item: FragmentCallback.FragmentItem, bundle: Bundle?) {
        val index = bundle?.getInt("index", 0)

        var fragment: Fragment
        when(item) {
            FragmentCallback.FragmentItem.ITEM_LIST -> {
                toolbar.title = "영화 목록"
                fragment = MovieListFragment()
            }
            FragmentCallback.FragmentItem.ITEM_DETAILS -> {
                toolbar.title = "영화 상세"
                fragment = MovieDetailsFragment()
            //                fragment = MovieDetailsFragment.newInstance(index)
            }
            FragmentCallback.FragmentItem.ITEM2 -> {
                toolbar.title = "예매순"
                fragment = Fragment2()
            }
            FragmentCallback.FragmentItem.ITEM3 -> {
                toolbar.title = "영화관"
                fragment = Fragment3()
            }
            FragmentCallback.FragmentItem.ITEM4 -> {
                toolbar.title = "즐겨찾기"
                fragment = Fragment4()
            }
        }

        supportFragmentManager.beginTransaction().replace(R.id.container, fragment).commit()
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

}