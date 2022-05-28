package org.techtown.movie

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

class PageFragment : Fragment() {

    var imageId = 0
    var title:String? = null
    var details1:String? = null
    var details2:String? = null

    companion object{
        fun newInstance(imageId:Int, title:String?, details1:String?, details2: String?):PageFragment{
            val fragment = PageFragment()

            val bundle = Bundle()
            bundle.putInt("imageId",imageId)
            bundle.putString("title",title)
            bundle.putString("details1",details1)
            bundle.putString("details2",details2)
            fragment.arguments = bundle

            return fragment
        }
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_page, container, false)
    }
 
}