package com.ets.farmadmin.Privileges;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ets.farmadmin.Dialogs.AdminDialog;
import com.ets.farmadmin.R;
import com.ets.farmadmin.Root.AdapterListener;
import com.ets.farmadmin.Root.HeadModel;

import java.util.ArrayList;

public class HeadRecyclerAdapter extends RecyclerView.Adapter {

    private final String TAG = "admin_dialg";
    private final String HEAD_KEY = "head_key";
    private Context context;
    private ArrayList<HeadModel> adapterModel;
    private AdminDialog adminDialog;
    private DialogInterface.OnClickListener dialogClickListener;
    private AdapterListener adapterListener;

    {
        adapterModel = new ArrayList<>();
        adminDialog = new AdminDialog();
    }

    public HeadRecyclerAdapter(Context context, ArrayList<HeadModel> adapterModel) {
        this.context = context;
        this.adapterModel = adapterModel;
        adapterListener = (AdapterListener) context;
    }

    //Here We tell the RecyclerView what to show at each element of it..it'd be a cardView!
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.head_card, parent, false);
        return new HeadRecyclerAdapter.HeadViewHolder(view);
    }

    //Here We tell the RecyclerView what to show at each CardView..
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((HeadRecyclerAdapter.HeadViewHolder) holder).BindView(position);

    }

    @Override
    public int getItemCount() {
        return adapterModel.size();
    }

    private void displayDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(context.getString(R.string.are_you_sure)).setPositiveButton(context.getString(R.string.yes_sure), dialogClickListener)
                .setNegativeButton(context.getString(R.string.no_sure), dialogClickListener).show();
    }

    //Here we bind all the children views of each cardView with their corresponding
    // actions to show & interact with them
    public class HeadViewHolder extends RecyclerView.ViewHolder {

        private TextView adminName;
        private TextView adminphone;
        private TextView adminType;
        private TextView adminStatus;
        private CardView headCard;

        public HeadViewHolder(View itemView) {
            super(itemView);

            adminName = itemView.findViewById(R.id.admin_name);
            adminphone = itemView.findViewById(R.id.admin_phone);
            adminType = itemView.findViewById(R.id.admin_privilege);
            adminStatus = itemView.findViewById(R.id.admin_status);
            headCard = itemView.findViewById(R.id.head_card);

        }


        //Here where all the glory being made..!
        public void BindView(final int position) {

            adminName.setText(adapterModel.get(position).getName());
            adminphone.setText(adapterModel.get(position).getPhone());

            String type = adapterModel.get(position).getPrivilege();

            if (type.equals("admin")) {
                adminType.setText(context.getString(R.string.admin_text));
            } else if (type.equals("content_manager")) {
                adminType.setText(context.getString(R.string.content_manager_text));
            }

            boolean isActive = adapterModel.get(position).getStatus();

            if (isActive) {
                adminStatus.setText(context.getString(R.string.active_text));
                adminStatus.setTextColor(context.getResources().getColor(R.color.colorPrimary));
            } else {
                adminStatus.setText(context.getString(R.string.inactive_text));
                adminStatus.setTextColor(context.getResources().getColor(R.color.colorAccent));
            }

            headCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Set head to bundle..
                    Bundle args = new Bundle();
                    args.putSerializable(HEAD_KEY, adapterModel.get(position));
                    adminDialog.setArguments(args);

                    //Begin Transaction
                    FragmentTransaction transaction = ((PrivilegeActivity) context).getSupportFragmentManager().beginTransaction();
                    Fragment prev = ((PrivilegeActivity) context).getSupportFragmentManager().findFragmentByTag(TAG);
                    if (prev != null) {
                        transaction.remove(prev);
                    }

                    transaction.add(adminDialog, TAG).commit();

                }
            });

            headCard.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {

                    dialogClickListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case DialogInterface.BUTTON_POSITIVE:
                                    //Yes button clicked
                                    HeadModel headModel;
                                    adapterModel.get(position).setName(null);
                                    adapterModel.get(position).setPhone(null);
                                    adapterModel.get(position).setPrivilege(null);
                                    adapterModel.get(position).setStatus(null);
                                    headModel = adapterModel.get(position);

                                    adapterListener.onDataRemoved(headModel);
                                    break;

                                case DialogInterface.BUTTON_NEGATIVE:
                                    //No button clicked
                                    break;
                            }
                        }
                    };

                    displayDialog();

                    return true;
                }
            });


        }


    }
}
