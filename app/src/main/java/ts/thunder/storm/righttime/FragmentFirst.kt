package ts.thunder.storm.righttime

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ts.thunder.storm.righttime.databinding.FragmentFirstBinding
import ts.thunder.storm.righttime.databinding.FragmentThirdBinding


class FragmentFirst : Fragment() {

    lateinit var mainActivity: MainActivity
    lateinit var binding : FragmentFirstBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFirstBinding.inflate(inflater,container,false)

        return binding.root
    }

}