package pl.edu.pw.meil.knr.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import pl.edu.pw.meil.knr.R
import pl.edu.pw.meil.knr.viewModels.MovementScreenViewModel


class MovementScreenFragment : Fragment() {

    companion object {
        fun newInstance() = MovementScreenFragment()
    }

    private lateinit var viewModel: MovementScreenViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.movement_screen_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(MovementScreenViewModel::class.java)
        // TODO: Use the ViewModel
    }

}
