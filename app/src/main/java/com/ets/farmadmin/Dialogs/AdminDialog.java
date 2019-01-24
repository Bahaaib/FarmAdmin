package com.ets.farmadmin.Dialogs;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.ets.farmadmin.R;
import com.ets.farmadmin.Root.DialogListener;
import com.ets.farmadmin.Root.HeadModel;

import java.text.NumberFormat;
import java.text.ParsePosition;

public class AdminDialog extends DialogFragment {
    private final String HEAD_KEY = "head_key";
    private View view;
    private Context mContext;
    private EditText adminNameField;
    private EditText adminPhoneField;
    private Spinner typeSpinner;
    private Spinner statusSpinner;
    private HeadModel head;
    private String adminType;
    private String adminActive;
    private Button okBtn;
    private Button cancelBtn;
    private DialogListener listener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        listener = (DialogListener) context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.dialog_admin, container, false);

        adminNameField = view.findViewById(R.id.dialog_admin_name);
        adminPhoneField = view.findViewById(R.id.dialog_admin_mobile);
        okBtn = view.findViewById(R.id.ok_button);
        cancelBtn = view.findViewById(R.id.cancel_button);

        initTypeSpinner();
        initStatusSpinner();

        Bundle args = getArguments();
        if (args != null) {
            head = (HeadModel) args.getSerializable(HEAD_KEY);
            adminNameField.setText(head.getName());
            adminPhoneField.setText(head.getPhone());

            if (head.getPrivilege().equals("content_manager")) {
                typeSpinner.setSelection(0);
            } else {
                typeSpinner.setSelection(1);
            }

            if (head.getStatus()) {
                statusSpinner.setSelection(0);
            } else {
                statusSpinner.setSelection(1);
            }
        } else {
            head = new HeadModel();
        }
        //Admin name..
        adminNameField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String adminName = adminNameField.getText().toString();
                if (!adminName.isEmpty()) {
                    head.setName(adminName);
                } else {
                    adminNameField.setError(getString(R.string.admin_name_error));
                }
            }
        });

        //Admin Phone..
        adminPhoneField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String adminPhone = adminPhoneField.getText().toString();
                if (!adminPhone.isEmpty() && isValidMobileNumber(adminPhone)) {
                    head.setPhone(adminPhone);
                } else {
                    adminPhoneField.setError(getString(R.string.admin_phone_error));
                }
            }
        });

        //Type Spinner
        typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                adminType = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        //Activity Spinner
        statusSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                adminActive = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String adminName = adminNameField.getText().toString();
                String adminPhone = adminPhoneField.getText().toString();
                if (!adminName.isEmpty() && !adminPhone.isEmpty() && isValidMobileNumber(adminPhone)) {

                    head.setName(adminName);
                    head.setPhone(adminPhone);

                    if (adminType.equals("أدمن")) {
                        head.setPrivilege("admin");
                    } else if (adminType.equals("مدير محتوى")) {
                        head.setPrivilege("content_manager");
                    }

                    if (adminActive.equals("نشط")) {
                        head.setStatus(true);
                    } else if (adminActive.equals("غير نشط")) {
                        head.setStatus(false);
                    }

                    listener.onAdminDataChanged(head);
                    getDialog().dismiss();
                }
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
            }
        });


        return view;
    }

    private void initTypeSpinner() {
        typeSpinner = view.findViewById(R.id.dialog_admin_type);
        String admin = getString(R.string.admin_text);
        String contentManager = getString(R.string.content_manager_text);

        String[] items = new String[]{contentManager, admin};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(mContext, android.R.layout.simple_spinner_dropdown_item, items);
        typeSpinner.setAdapter(adapter);
    }

    private void initStatusSpinner() {
        statusSpinner = view.findViewById(R.id.dialog_admin_status);
        String active = getString(R.string.active_text);
        String inActive = getString(R.string.inactive_text);

        String[] items = new String[]{active, inActive};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(mContext, android.R.layout.simple_spinner_dropdown_item, items);
        statusSpinner.setAdapter(adapter);
    }

    // validating mobile number format
    private boolean isValidMobileNumber(String number) {

        return Patterns.PHONE.matcher(number).matches() && (number.length() > 10) && isValidNumeric(number);
    }

    private boolean isValidNumeric(String number) {
        NumberFormat formatter = NumberFormat.getInstance();
        ParsePosition pos = new ParsePosition(0);
        formatter.parse(number, pos);
        return number.length() == pos.getIndex();
    }
}
