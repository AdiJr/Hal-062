package pl.edu.pw.meil.knr.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import pl.edu.pw.meil.knr.R;
import pl.edu.pw.meil.knr.viewModels.ConnectScreenViewModel;

public class ConnectScreen extends Fragment {

    private ConnectScreenViewModel mViewModel;

    public static ConnectScreen newInstance() {
        return new ConnectScreen();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.connect_screen_fragment, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(ConnectScreenViewModel.class);
        // TODO: Use the ViewModel
    }

}
