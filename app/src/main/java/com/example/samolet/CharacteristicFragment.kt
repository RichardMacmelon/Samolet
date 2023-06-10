package com.example.samolet

import android.os.Bundle
import android.os.StrictMode
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.samolet.databinding.FragmentCharacteristicBinding
import com.example.samolet.databinding.FragmentMethodBinding
import okhttp3.*
import org.json.JSONObject
import java.io.File
import java.io.IOException

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [CharacteristicFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CharacteristicFragment : Fragment() {
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

    private var _binding : FragmentCharacteristicBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        _binding = FragmentCharacteristicBinding.inflate(inflater)

        binding.buttonResult.setOnClickListener {
            binding.textViewResultRoom.text = run("https://397d-188-243-44-63.ngrok-free.app/get-result?username=username&room=1")
            binding.textViewResultKitchen.text = run("https://397d-188-243-44-63.ngrok-free.app/get-result?username=username&room=2")
            binding.textViewResultCorridor.text  = run("https://397d-188-243-44-63.ngrok-free.app/get-result?username=username&room=3")
            binding.textViewResultButtonRoom.text = run("https://397d-188-243-44-63.ngrok-free.app/get-result?username=username&room=4")
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    fun run(url: String): String {
//        val body = MultipartBody.Builder()
//            .setType(MultipartBody.FORM)
//            .addFormDataPart("file", file.name, RequestBody.create(MediaType.parse("video/mp4"), file))
//            .build()

        val client = OkHttpClient()
        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        var answer = "kek"

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw IOException("Запрос к серверу не был успешен:")
                }
                answer = JSONObject(response.body()?.string()).get("result").toString()
            }
        } catch (e: IOException) {
            println("Ошибка подключения: $e");
        }

        return answer
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment CharacteristicFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CharacteristicFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}