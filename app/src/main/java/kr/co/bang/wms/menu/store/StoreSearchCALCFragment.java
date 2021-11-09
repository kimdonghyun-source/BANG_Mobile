package kr.co.bang.wms.menu.store;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import kr.co.bang.wms.R;
import kr.co.bang.wms.common.Utils;
import kr.co.bang.wms.custom.CommonFragment;
import kr.co.bang.wms.menu.popup.RegisterBtnPopup;
import kr.co.bang.wms.menu.popup.TwoBtnPopup;
import kr.co.bang.wms.model.CalcModel;
import kr.co.bang.wms.model.CstInvModel;
import kr.co.bang.wms.model.ResultModel;
import kr.co.bang.wms.model.StoreCstListModel;
import kr.co.bang.wms.network.ApiClientService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StoreSearchCALCFragment extends CommonFragment {

    Context mContext;


    TextView tv_cst_nm, tv_margin, tv_qty, tv_price;
    ImageButton bt_next;
    RecyclerView calc_listView;
    ListAdapter mAdapter;
    String cst_nm, cst_code, scan_qty;

    CalcModel mClacModel;
    List<CalcModel.Item> mCalcList;

    int tot_price = 0, tot_qty = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();

    }//Close onCreate

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.frag_store_search_calc, container, false);

        Bundle arguments = getArguments();

        cst_nm = arguments.getString("cst_nm");
        cst_code = arguments.getString("cst_code");
        scan_qty = arguments.getString("scan_qty");


        tv_cst_nm = v.findViewById(R.id.tv_cst_nm);
        tv_margin = v.findViewById(R.id.tv_margin);
        tv_qty = v.findViewById(R.id.tv_qty);
        tv_price = v.findViewById(R.id.tv_price);
        bt_next = v.findViewById(R.id.bt_next);
        calc_listView = v.findViewById(R.id.calc_listView);

        if (cst_nm != null) {
            tv_cst_nm.setText(cst_nm);
            CalcList();
        }

        calc_listView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        mAdapter = new ListAdapter(getActivity());
        calc_listView.setAdapter(mAdapter);

        bt_next.setOnClickListener(onClickListener);

        return v;

    }//Close onCreateView

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.bt_next:
                    Intent i = new Intent();
                    getActivity().setResult(Activity.RESULT_OK, i);
                    getActivity().finish();
                    break;
            }

        }
    };

    /**
     * 잔액계산 리스트
     */
    private void CalcList() {
        ApiClientService service = ApiClientService.retrofit.create(ApiClientService.class);

        Call<CalcModel> call = service.CalcList("sp_pda_price_list", cst_code);

        call.enqueue(new Callback<CalcModel>() {
            @Override
            public void onResponse(Call<CalcModel> call, Response<CalcModel> response) {
                if (response.isSuccessful()) {
                    mClacModel = response.body();
                    final CalcModel model = response.body();
                    Utils.Log("model ==> :" + new Gson().toJson(model));
                    if (mClacModel != null) {
                        if (mClacModel.getFlag() == ResultModel.SUCCESS) {
                            float c_cnt = 0;
                            if (model.getItems().size() > 0) {
                                //mAdapter.clearData();
                                mCalcList = model.getItems();
                                for (int i = 0; i < model.getItems().size(); i++) {
                                    CalcModel.Item item = (CalcModel.Item) model.getItems().get(i);
                                    mAdapter.addData(item);
                                }

                                for (int i = 0; i < mAdapter.getItemCount(); i++){
                                    tot_price += mAdapter.itemsList.get(i).getItm_price();
                                    tot_qty += mAdapter.itemsList.get(i).getScan_qty();
                                }
                                tv_price.setText(Integer.toString(tot_price));
                                tv_qty.setText(Integer.toString(tot_qty));
                            }

                            mAdapter.notifyDataSetChanged();
                            CalcDelete();

                        } else {
                            Utils.Toast(mContext, model.getMSG());
                        }
                    }

                } else {
                    Utils.LogLine(response.message());
                    Utils.Toast(mContext, response.code() + " : " + response.message());
                }
            }

            @Override
            public void onFailure(Call<CalcModel> call, Throwable t) {
                Utils.LogLine(t.getMessage());
                Utils.Toast(mContext, getString(R.string.error_network));
            }
        });
    }//Close

    /**
     * 잔액계산 리스트 삭제
     */
    private void CalcDelete() {
        ApiClientService service = ApiClientService.retrofit.create(ApiClientService.class);

        Call<CalcModel> call = service.CalcList("sp_del_cst_price", cst_code);

        call.enqueue(new Callback<CalcModel>() {
            @Override
            public void onResponse(Call<CalcModel> call, Response<CalcModel> response) {
                if (response.isSuccessful()) {
                    mClacModel = response.body();
                    final CalcModel model = response.body();
                    Utils.Log("model ==> :" + new Gson().toJson(model));
                    if (mClacModel != null) {
                        if (mClacModel.getFlag() == ResultModel.SUCCESS) {

                        } else {
                            Utils.Toast(mContext, model.getMSG());
                        }
                    }

                } /*else {
                    Utils.LogLine(response.message());
                    Utils.Toast(mContext, response.code() + " : " + response.message());
                }*/
            }

            @Override
            public void onFailure(Call<CalcModel> call, Throwable t) {
                Utils.LogLine(t.getMessage());
                Utils.Toast(mContext, getString(R.string.error_network));
            }
        });
    }//Close



    public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> {

        List<CalcModel.Item> itemsList;
        Activity mActivity;
        Handler mHandler = null;


        public ListAdapter(Activity context) {
            mActivity = context;
        }

        public void setData(List<CalcModel.Item> list) {
            itemsList = list;
        }

        public void clearData() {
            if (itemsList != null) itemsList.clear();
        }

        public void setRetHandler(Handler h) {
            this.mHandler = h;
        }

        public List<CalcModel.Item> getData() {
            return itemsList;
        }

        public void addData(CalcModel.Item item) {
            if (itemsList == null) itemsList = new ArrayList<>();
            itemsList.add(item);
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, final int z) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cell_calc_list, viewGroup, false);
            ViewHolder holder = new ViewHolder(v);
            return holder;
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {

            final CalcModel.Item item = itemsList.get(position);

            holder.tv_itm_ucode.setText(item.getItm_uname());
            holder.tv_itm_price5.setText(Integer.toString(item.getITM_PRICE5()));
            holder.tv_scan_qty.setText(Integer.toString(item.getScan_qty()));
            holder.tv_itm_price.setText(Integer.toString(item.getItm_price()));
            tv_margin.setText(Integer.toString(item.getMargin_rate()));



        }


        @Override
        public int getItemViewType(int position) {
            return position;
        }

        @Override
        public int getItemCount() {
            return (null == itemsList ? 0 : itemsList.size());
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            TextView tv_itm_ucode;
            TextView tv_itm_price5;
            TextView tv_scan_qty;
            TextView tv_itm_price;


            public ViewHolder(View view) {
                super(view);

                tv_itm_ucode = view.findViewById(R.id.tv_itm_ucode);
                tv_itm_price5 = view.findViewById(R.id.tv_itm_price5);
                tv_scan_qty = view.findViewById(R.id.tv_scan_qty);
                tv_itm_price = view.findViewById(R.id.tv_itm_price);

            }
        }


    }//Close Adapter


}//Close Fragmnet
