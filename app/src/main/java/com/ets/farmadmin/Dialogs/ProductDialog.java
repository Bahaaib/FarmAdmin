package com.ets.farmadmin.Dialogs;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.ets.farmadmin.R;

public class ProductDialog extends DialogFragment {

    private View view;
    private Context context;
    private EditText price;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getActivity() != null){
            context = getActivity();
        }
        //init Spinner..
        initSpinner();
        price = view.findViewById(R.id.price);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
         view = inflater.inflate(R.layout.dialog_product, container, false);



        return view;
    }

    private void initSpinner(){
        Spinner dropdown = view.findViewById(R.id.available);
        String[] items = new String[]{"متوفر", "غير متوفر"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, items);
        dropdown.setAdapter(adapter);
    }
}
