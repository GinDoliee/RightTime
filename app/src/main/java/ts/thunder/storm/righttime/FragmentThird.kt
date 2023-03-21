package ts.thunder.storm.righttime

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ts.thunder.storm.righttime.databinding.FragmentThirdBinding


class FragmentThird : Fragment() {

    lateinit var mainActivity: MainActivity
    lateinit var binding :FragmentThirdBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_third, container, false)
    }

}