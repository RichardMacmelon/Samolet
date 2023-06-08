package com.example.samolet

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import com.example.samolet.databinding.FragmentApartmentBinding
import com.example.samolet.databinding.FragmentMethodBinding

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [MethodFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MethodFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    private var _binding : FragmentMethodBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentMethodBinding.inflate(inflater)

        binding.buttonResult.setOnClickListener {
            parentFragmentManager.commit {
                replace<CharacteristicFragment>(R.id.fragment_container)
                addToBackStack(MethodFragment::class.java.simpleName)
            }
        }

        binding.buttonRoom.setOnClickListener {
            val intent = Intent(activity, MainActivity::class.java)
            intent.putExtra("Room","1")
            startActivity(intent)
        }

        binding.buttonKitchen.setOnClickListener {
            val intent = Intent(activity, MainActivity::class.java)
            intent.putExtra("Room","2")
            startActivity(intent)
        }

        binding.buttonCorridor.setOnClickListener {
            val intent = Intent(activity, MainActivity::class.java)
            intent.putExtra("Room","3")
            startActivity(intent)
        }

        binding.buttonBathroom.setOnClickListener {
            val intent = Intent(activity, MainActivity::class.java)
            intent.putExtra("Room","4")
            startActivity(intent)
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment MethodFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MethodFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}