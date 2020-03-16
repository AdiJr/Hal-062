package pl.edu.pw.meil.knr.fragments

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import pl.edu.pw.meil.knr.R
import pl.edu.pw.meil.knr.viewModels.SplashScreenViewModel


class SplashScreenFragment : Fragment() {

    companion object {
        fun newInstance() = SplashScreenFragment()
    }

    private lateinit var viewModel: SplashScreenViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.splash_screen_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(SplashScreenViewModel::class.java)
        // TODO: Use the ViewModel

        Handler().postDelayed({
            findNavController().navigate(SplashScreenFragmentDirections.actionSplashScreenToConnectScreen())
        }, 6000)
    }
}
