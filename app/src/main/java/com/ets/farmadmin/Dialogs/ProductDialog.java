package com.ets.farmadmin.Dialogs;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.ets.farmadmin.R;
import com.ets.farmadmin.Root.DialogListener;
import com.ets.farmadmin.Root.ProductModel;

import java.text.NumberFormat;
import java.text.ParsePosition;

public class ProductDialog extends DialogFragment {

    private final String PRODUCT_KEY = "product_key";
    private final String TYPE_KEY = "type_key";
    private View view;
    private Context mContext;
    private EditText pricefield;
    private TextView productName;
    private Button okBtn;
    private Button cancelButton;
    private Spinner availabilitySpinner;
    private String selection;
    private String type;

    private ProductModel productModel;
    private DialogListener dialogListener;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        dialogListener = (DialogListener) context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.dialog_product, container, false);
        //Receive product from adapter
        Bundle args = getArguments();
        if (args != null) {
            productModel = (ProductModel) args.getSerializable(PRODUCT_KEY);
            type = args.getString(TYPE_KEY);

        }
        initSpinner();
        selection = getString(R.string.spinner_not_available);


        //init Spinner..
        initSpinner();
        productName = view.findViewById(R.id.product_name);
        pricefield = view.findViewById(R.id.price);
        okBtn = view.findViewById(R.id.ok_button);
        cancelButton = view.findViewById(R.id.cancel_button);

        productName.setText(productModel.getName_ar());

        availabilitySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selection = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String price = pricefield.getText().toString();
                boolean isAvailable;

                if (isValidNumeric(price) && !price.isEmpty()) {

                    isAvailable = selection.equals("متوفر");

                    try {
                        productModel.setPrice(Float.valueOf(price));
                        productModel.setAvailability(isAvailable);

                        if (type.equals("fruits")) {
                            dialogListener.onFruitsDataChanged(productModel);
                        } else if (type.equals("vegetables")) {
                            dialogListener.onVegetablesDataChanged(productModel);
                        }
                        getDialog().dismiss();

                    } catch (NumberFormatException e) {
                        pricefield.setError(getString(R.string.check_num));

                    }
                } else {
                    pricefield.setError(getString(R.string.check_num));
                }


            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
            }
        });

        return view;
    }

    private void initSpinner() {
        availabilitySpinner = view.findViewById(R.id.available);
        String available = getString(R.string.spinner_available);
        String nAvailable = getString(R.string.spinner_not_available);

        String[] items = new String[]{available, nAvailable};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(mContext, android.R.layout.simple_spinner_dropdown_item, items);
        availabilitySpinner.setAdapter(adapter);
    }

    private boolean isValidNumeric(String number) {
        NumberFormat formatter = NumberFormat.getInstance();
        ParsePosition pos = new ParsePosition(0);
        formatter.parse(number, pos);
        return number.length() == pos.getIndex();
    }
}
