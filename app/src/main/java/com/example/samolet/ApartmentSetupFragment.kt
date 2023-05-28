package com.example.samolet

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import com.example.samolet.databinding.FragmentApartmentSetupBinding
import com.example.samolet.databinding.FragmentMethodBinding

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ApartmentSetupFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ApartmentSetupFragment : Fragment() {
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

    private var _binding : FragmentApartmentSetupBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentApartmentSetupBinding.inflate(inflater)

        binding.roomButton.setOnClickListener {
            parentFragmentManager.commit {
                replace<CharacteristicFragment>(R.id.fragment_container)
                addToBackStack(ApartmentSetupFragment::class.java.simpleName)
            }
        }

        binding.kitchenButton.setOnClickListener {
            parentFragmentManager.commit {
                replace<CharacteristicFragment>(R.id.fragment_container)
                addToBackStack(ApartmentSetupFragment::class.java.simpleName)
            }
        }

        binding.corridorButton.setOnClickListener {
            parentFragmentManager.commit {
                replace<CharacteristicFragment>(R.id.fragment_container)
                addToBackStack(ApartmentSetupFragment::class.java.simpleName)
            }
        }

        binding.bathroomButton.setOnClickListener {
            parentFragmentManager.commit {
                replace<CharacteristicFragment>(R.id.fragment_container)
                addToBackStack(ApartmentSetupFragment::class.java.simpleName)
            }
        }
        return binding.root
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ApartmentSetupFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ApartmentSetupFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}