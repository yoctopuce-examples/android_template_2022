package com.yoctopuce.myapplication;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.snackbar.Snackbar;
import com.yoctopuce.YoctoAPI.YAPI;
import com.yoctopuce.YoctoAPI.YAPI_Exception;
import com.yoctopuce.YoctoAPI.YTemperature;
import com.yoctopuce.myapplication.databinding.FragmentFirstBinding;

import java.util.Locale;

public class FirstFragment extends Fragment
{

    private FragmentFirstBinding binding;
    private Handler _handler;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    )
    {
        _handler = new Handler();
        binding = FragmentFirstBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        binding.buttonFirst.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                NavHostFragment.findNavController(FirstFragment.this)
                        .navigate(R.id.action_FirstFragment_to_SecondFragment);
            }
        });
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        binding = null;
    }


    @Override
    public void onStart()
    {
        super.onStart();
        try {
            YAPI.EnableUSBHost(getContext());
            YAPI.RegisterHub("usb");
        } catch (YAPI_Exception e) {
            Snackbar.make(binding.temperature,
                    "Error:" + e.getLocalizedMessage(),
                    Snackbar.LENGTH_INDEFINITE).show();
        }
        _handler.postDelayed(_periodicUpdate, 100);
    }

    @Override
    public void onStop()
    {
        _handler.removeCallbacks(_periodicUpdate);
        YAPI.FreeAPI();
        super.onStop();
    }

    private double _hardwaredetect;
    private YTemperature _sensor;

    private Runnable _periodicUpdate = new Runnable()
    {
        @Override
        public void run()
        {
            try {
                if (_hardwaredetect == 0) {
                    YAPI.UpdateDeviceList();
                }
                _hardwaredetect = (_hardwaredetect + 1) % 6;
                if (_sensor == null) {
                    _sensor = YTemperature.FirstTemperature();
                }
                if (_sensor != null && _sensor.isOnline()) {
                    final String text = String.format(Locale.US, "%.2f °C",
                            _sensor.get_currentValue());
                    binding.temperature.setText(text);
                } else {
                    binding.temperature.setText("OFFLINE");
                    _sensor = null;
                }
            } catch (YAPI_Exception e) {
                Snackbar.make(binding.temperature,

                        "Error:" + e.getLocalizedMessage(),
                        Snackbar.LENGTH_INDEFINITE).show();
            }
            _handler.postDelayed(_periodicUpdate, 500);
        }
    };
}