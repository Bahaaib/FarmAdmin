package com.ets.farmadmin.Root;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.ets.farmadmin.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class OfferRecyclerAdapter extends RecyclerView.Adapter {

    private Context context;
    private ArrayList<OfferModel> adapterModel;
    private DialogInterface.OnClickListener dialogClickListener;
    private AdapterListener adapterListener;

    {
        adapterModel = new ArrayList<>();
    }

    public OfferRecyclerAdapter(Context context, ArrayList<OfferModel> adapterModel) {
        this.context = context;
        this.adapterModel = adapterModel;
        adapterListener = (AdapterListener) context;

    }

    private void displayDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(context.getString(R.string.remove_offer)).setPositiveButton(context.getString(R.string.yes_sure), dialogClickListener)
                .setNegativeButton(context.getString(R.string.no_sure), dialogClickListener).show();
    }

    //Here We tell the RecyclerView what to show at each element of it..it'd be a cardView!
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.offer_card, parent, false);
        return new OfferRecyclerAdapter.OfferViewHolder(view);
    }

    //Here We tell the RecyclerView what to show at each CardView..
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((OfferRecyclerAdapter.OfferViewHolder) holder).BindView(position);

    }

    @Override
    public int getItemCount() {
        return adapterModel.size();
    }

    //Here we bind all the children views of each cardView with their corresponding
    // actions to show & interact with them
    public class OfferViewHolder extends RecyclerView.ViewHolder {

        private ImageView offerImage;
        private CardView offerCard;


        public OfferViewHolder(View itemView) {
            super(itemView);

            offerImage = itemView.findViewById(R.id.offer_img);
            offerCard = itemView.findViewById(R.id.offer_card);

        }


        //Here where all the glory being made..!
        public void BindView(final int position) {

            Picasso.with(context)
                    .load(adapterModel.get(position).getImgUrl())
                    .fit()
                    .into(offerImage);

            offerCard.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    dialogClickListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case DialogInterface.BUTTON_POSITIVE:
                                    //Yes button clicked
                                    OfferModel offer;
                                    adapterModel.get(position).setImgUrl(null);

                                    offer = adapterModel.get(position);

                                    adapterListener.onDataRemoved(offer);
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

