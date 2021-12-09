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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.honeywell.aidc.BarcodeReadEvent;

import java.util.ArrayList;
import java.util.List;

import kr.co.bang.wms.R;
import kr.co.bang.wms.common.Define;
import kr.co.bang.wms.common.SharedData;
import kr.co.bang.wms.common.Utils;
import kr.co.bang.wms.custom.CommonFragment;
import kr.co.bang.wms.honeywell.AidcReader;
import kr.co.bang.wms.menu.main.BaseActivity;
import kr.co.bang.wms.menu.popup.NoBtnPopup;
import kr.co.bang.wms.menu.popup.OneBtnPopup;
import kr.co.bang.wms.menu.popup.RegisterBtnPopup;
import kr.co.bang.wms.menu.popup.TwoBtnPopup;
import kr.co.bang.wms.model.CstInvModel;
import kr.co.bang.wms.model.CstInvPopupModel;
import kr.co.bang.wms.model.MatOutDetailGet;
import kr.co.bang.wms.model.ResultModel;
import kr.co.bang.wms.model.StockDetailModel;
import kr.co.bang.wms.model.StoreCstListModel;
import kr.co.bang.wms.network.ApiClientService;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StoreSearchDetailFragment extends CommonFragment {

    Context mContext;
    int mPosition = -1;
    StoreCstListModel.Item mOrder;
    StoreCstListModel mStoreModel;

    TextView tv_cst_nm, n_cnt, n_scan_cnt, y_cnt, y_scan_cnt;
    Button bt_calc, bt_erp;
    String cst_code, barcodeScan, chk, chk1, beg_barocde;
    RecyclerView search_detail_listView;
    EditText et_from;
    ImageButton bt_search;

    CstInvModel mcstInvModel;
    List<CstInvModel.Item> mCstInvList;

    CstInvPopupModel mcstPopModel;
    List<CstInvPopupModel.Item> mcstPopList;

    DetailAdapter mAdapter;
    List<String> mBarcode = null;
    TwoBtnPopup mTwoBtnPopup;
    OneBtnPopup mOneBtnPopup;
    NoBtnPopup mNoBtnPopup;
    RegisterBtnPopup mRegisterBtnPopup;

    int b_bar_cnt = 0, t_bar_cnt = 0, hold_cnt = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
        mBarcode = new ArrayList<>();


    }//Close onCreate

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.frag_store_search_detail, container, false);

        Bundle arguments = getArguments();
        mStoreModel = (StoreCstListModel) arguments.getSerializable("model");
        mPosition = arguments.getInt("position");
        mOrder = mStoreModel.getItems().get(mPosition);

        tv_cst_nm = v.findViewById(R.id.tv_cst_nm);
        n_cnt = v.findViewById(R.id.n_cnt);
        n_scan_cnt = v.findViewById(R.id.n_scan_cnt);
        y_cnt = v.findViewById(R.id.y_cnt);
        y_scan_cnt = v.findViewById(R.id.y_scan_cnt);
        bt_calc = v.findViewById(R.id.bt_calc);
        bt_erp = v.findViewById(R.id.bt_erp);
        et_from = v.findViewById(R.id.et_from);
        search_detail_listView = v.findViewById(R.id.search_detail_listView);
        bt_search = v.findViewById(R.id.bt_search);

        tv_cst_nm.setText(mOrder.getCst_name());
        cst_code = mOrder.getCst_code();

        if (mOrder.getCst_code() != null) {
            StoreDetailList();
        }

        bt_calc.setOnClickListener(onClickListener);
        bt_erp.setOnClickListener(onClickListener);
        bt_search.setOnClickListener(onClickListener);

        search_detail_listView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        mAdapter = new DetailAdapter(getActivity());
        search_detail_listView.setAdapter(mAdapter);

        /*mAdapter.setRetHandler(new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what >= 0) {
                    CstList(msg.what);
                }
            }
        });*/

        return v;

    }//Close onCreateView


    @Override
    public void onResume() {
        super.onResume();
        AidcReader.getInstance().claim(mContext);
        AidcReader.getInstance().setListenerHandler(new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 1) {

                    BarcodeReadEvent event = (BarcodeReadEvent) msg.obj;
                    String barcode = event.getBarcodeData();
                    barcodeScan = barcode;

                    if (mBarcode != null) {
                        if (mBarcode.contains(barcode)) {
                            Utils.Toast(mContext, "동일한 바코드를 스캔하였습니다.");
                            return;
                        }
                    }

                    if (beg_barocde != null){
                        if (beg_barocde.equals(barcodeScan)){
                            Utils.Toast(mContext, "동일한 바코드를 스캔하였습니다.");
                            return;
                        }
                    }



                    et_from.setText(barcode);
                    beg_barocde = barcodeScan;
                    chk = "체크노노";

                    for (int k = 0; k < mAdapter.getItemCount(); k++) {
                        if (mAdapter.itemsList.get(k).getLot_no1().equals(barcodeScan)) {
                            mcstInvModel.getItems().get(k).setSts("일치");
                            mAdapter.notifyDataSetChanged();
                            chk = "체크";

                            if (mAdapter.itemsList.get(k).getSts().equals("일치") && mAdapter.itemsList.get(k).getC_yn().equals("판매용")) {
                                b_bar_cnt++;
                                n_scan_cnt.setText(Integer.toString(b_bar_cnt));
                                mBarcode.add(barcodeScan);
                                beg_barocde = barcodeScan;
                            }

                            if (mAdapter.itemsList.get(k).getSts().equals("일치") && mAdapter.itemsList.get(k).getC_yn().equals("시타용")) {
                                t_bar_cnt++;
                                y_scan_cnt.setText(Integer.toString(t_bar_cnt));
                                mBarcode.add(barcodeScan);
                                beg_barocde = barcodeScan;
                            }

                        }
                    }

                    if (chk.equals("체크노노")) {
                        mNoBtnPopup = new NoBtnPopup(getActivity(), "재고를 찾고있습니다...", R.drawable.popup_title_alert, new Handler() {
                            @Override
                            public void handleMessage(Message msg) {
                                if (msg.what == 1) {
                                    StoreScanAllSearch();
                                }
                            }
                        });
                    }
                }
            }

        });

    }//Close onResume

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.bt_calc:
                    if (mAdapter == null) {
                        Utils.Toast(mContext, "데이터가 없습니다.");
                        return;
                    }

                    if (n_scan_cnt.getText().toString().equals("") && y_scan_cnt.getText().toString().equals("")) {
                        Utils.Toast(mContext, "스캔 내역이 없습니다.");
                        return;
                    }

                    request_make_temp();

                    break;

                case R.id.bt_erp:
                    if (mAdapter == null) {
                        Utils.Toast(mContext, "데이터가 없습니다.");
                        return;
                    }

                    if (n_scan_cnt.getText().toString().equals("") && y_scan_cnt.getText().toString().equals("")) {
                        Utils.Toast(mContext, "스캔 내역이 없습니다.");
                        return;
                    }

                    for (int i = 0; i < mAdapter.getItemCount(); i++) {
                        if (mAdapter.itemsList.get(i).getSts().equals("대기")) {
                            hold_cnt++;
                        }
                    }

                    if (hold_cnt > 0) {
                        mTwoBtnPopup = new TwoBtnPopup(getActivity(), "미스캔 내역이 있습니다. 그래도 처리하시겠습니까?", R.drawable.popup_title_alert, new Handler() {
                            @Override
                            public void handleMessage(Message msg) {
                                if (msg.what == 1) {
                                    cst_stk_save();
                                    mTwoBtnPopup.hideDialog();
                                    return;
                                }
                            }
                        });
                    } else {
                        mTwoBtnPopup = new TwoBtnPopup(getActivity(), "ERP 전송을 하시겠습니까?", R.drawable.popup_title_alert, new Handler() {
                            @Override
                            public void handleMessage(Message msg) {
                                if (msg.what == 1) {
                                    cst_stk_save();
                                    mTwoBtnPopup.hideDialog();
                                }
                            }
                        });
                    }


                    break;

                case R.id.bt_search:
                    chk = "체크노노";
                    barcodeScan = et_from.getText().toString();

                    if (mBarcode != null) {
                        if (mBarcode.contains(barcodeScan)) {
                            Utils.Toast(mContext, "동일한 LOTNO를 입력하였습니다.");
                            return;
                        }
                    }

                    for (int k = 0; k < mAdapter.getItemCount(); k++) {
                        if (mAdapter.itemsList.get(k).getLot_no1().equals(barcodeScan) && mAdapter.itemsList.get(k).getSts().equals("불일치")
                                || mAdapter.itemsList.get(k).getLot_no().equals(barcodeScan) && mAdapter.itemsList.get(k).getSts().equals("불일치")) {
                            Utils.Toast(mContext, "이미 등록되었습니다.");
                            return;
                        }
                    }

                    for (int k = 0; k < mAdapter.getItemCount(); k++) {
                        if (mAdapter.itemsList.get(k).getLot_no1().equals(barcodeScan) && mAdapter.itemsList.get(k).getSts().equals("일치")
                                || mAdapter.itemsList.get(k).getLot_no().equals(barcodeScan) && mAdapter.itemsList.get(k).getSts().equals("일치")) {
                            Utils.Toast(mContext, "이미 등록되었습니다.");
                            return;
                        }
                    }

                    for (int k = 0; k < mAdapter.getItemCount(); k++) {
                        if (mAdapter.itemsList.get(k).getLot_no1().equals(barcodeScan) || mAdapter.itemsList.get(k).getLot_no().equals(barcodeScan)) {
                            mcstInvModel.getItems().get(k).setSts("일치");
                            mAdapter.notifyDataSetChanged();
                            chk = "체크";

                            if (mAdapter.itemsList.get(k).getSts().equals("일치") && mAdapter.itemsList.get(k).getC_yn().equals("판매용")) {
                                b_bar_cnt++;
                                n_scan_cnt.setText(Integer.toString(b_bar_cnt));
                                mBarcode.add(barcodeScan);
                            }

                            if (mAdapter.itemsList.get(k).getSts().equals("일치") && mAdapter.itemsList.get(k).getC_yn().equals("시타용")) {
                                t_bar_cnt++;
                                y_scan_cnt.setText(Integer.toString(t_bar_cnt));
                                mBarcode.add(barcodeScan);
                            }

                        }
                    }

                    if (chk.equals("체크노노")) {
                        mNoBtnPopup = new NoBtnPopup(getActivity(), "재고를 찾고있습니다...", R.drawable.popup_title_alert, new Handler() {
                            @Override
                            public void handleMessage(Message msg) {
                                if (msg.what == 1) {
                                    StoreScanAllSearch();
                                }

                            }
                        });

                        //StoreScanAdd("");
                        /*mTwoBtnPopup = new TwoBtnPopup(getActivity(), "일치하는 시리얼번호가 없습니다.\n등록하시겠습니까?", R.drawable.popup_title_alert, new Handler() {
                            @Override
                            public void handleMessage(Message msg) {
                                if (msg.what == 1) {
                                    mTwoBtnPopup.hideDialog();
                                    mRegisterBtnPopup = new RegisterBtnPopup(getActivity(), "상태값을 선택하세요.", R.drawable.popup_title_alert, new Handler() {
                                        @Override
                                        public void handleMessage(Message msg) {
                                            if (msg.what == 1) {
                                                mRegisterBtnPopup.hideDialog();
                                                //obj = 0 판매용, obj = 1 시타용
                                                StoreScanAdd(String.valueOf(msg.obj));
                                            }
                                        }
                                    });
                                } else {

                                }
                            }
                        });*/
                    }


                    break;

            }

        }
    };


    /**
     * 재고조회
     */
    private void StoreDetailList() {
        ApiClientService service = ApiClientService.retrofit.create(ApiClientService.class);

        Call<CstInvModel> call = service.StoreCstInvList("sp_get_cst_inv", cst_code, "");

        call.enqueue(new Callback<CstInvModel>() {
            @Override
            public void onResponse(Call<CstInvModel> call, Response<CstInvModel> response) {
                if (response.isSuccessful()) {

                    mcstInvModel = response.body();
                    final CstInvModel model = response.body();
                    Utils.Log("model ==> :" + new Gson().toJson(model));
                    if (mcstInvModel != null) {
                        if (mcstInvModel.getFlag() == ResultModel.SUCCESS) {
                            float c_cnt = 0;
                            if (model.getItems().size() > 0) {
                                int yy_cnt = 0;
                                int nn_cnt = 0;
                                mCstInvList = model.getItems();
                                for (int i = 0; i < model.getItems().size(); i++) {
                                    CstInvModel.Item item = (CstInvModel.Item) model.getItems().get(i);
                                    chk = "체크";
                                    mAdapter.addData(item);

                                    if (mCstInvList.get(i).getCount_n() == 1) {
                                        nn_cnt++;
                                    }
                                    if (mCstInvList.get(i).getCount_y() == 1) {
                                        yy_cnt++;
                                    }


                                }
                                n_cnt.setText(Integer.toString(nn_cnt));
                                y_cnt.setText(Integer.toString(yy_cnt));


                            }
                            mAdapter.notifyDataSetChanged();
                            search_detail_listView.setAdapter(mAdapter);

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
            public void onFailure(Call<CstInvModel> call, Throwable t) {
                Utils.LogLine(t.getMessage());
                Utils.Toast(mContext, getString(R.string.error_network));
            }
        });
    }//Close

    /**
     * 불일치 추가
     */
    private void StoreScanAdd(String gbn) {
        ApiClientService service = ApiClientService.retrofit.create(ApiClientService.class);

        Call<CstInvModel> call = service.ScanAdd("sp_get_lotinfo", barcodeScan, gbn);

        call.enqueue(new Callback<CstInvModel>() {
            @Override
            public void onResponse(Call<CstInvModel> call, Response<CstInvModel> response) {
                if (response.isSuccessful()) {
                    //mcstInvModel = response.body();
                    final CstInvModel model = response.body();
                    Utils.Log("model ==> :" + new Gson().toJson(model));
                    if (mcstInvModel != null) {
                        if (mcstInvModel.getFlag() == ResultModel.SUCCESS) {
                            if (model.getFlag() != 0) {
                                Utils.Toast(mContext, "조회된 데이터가 없습니다.");
                                beg_barocde = " ";
                                return;
                            }
                            if (model.getItems().size() > 0) {

                                for (int i = 0; i < model.getItems().size(); i++) {
                                    chk = "체크노노";
                                    CstInvModel.Item item = (CstInvModel.Item) model.getItems().get(i);
                                    mAdapter.addData(item);
                                }
                            }

                            for (int k = 0; k < mAdapter.getItemCount(); k++) {
                                if (mAdapter.itemsList.get(k).getLot_no1().equals(barcodeScan) || mAdapter.itemsList.get(k).getLot_no().equals(barcodeScan)) {
                                    if (mAdapter.itemsList.get(k).getSts().equals("불일치") && mAdapter.itemsList.get(k).getC_yn().equals("판매용")) {
                                        //Log.d("불일치:", "판매");
                                        b_bar_cnt++;
                                        n_scan_cnt.setText(Integer.toString(b_bar_cnt));
                                    }

                                    if (mAdapter.itemsList.get(k).getSts().equals("불일치") && mAdapter.itemsList.get(k).getC_yn().equals("시타용")) {
                                        //Log.d("불일치:", "시타");
                                        t_bar_cnt++;
                                        y_scan_cnt.setText(Integer.toString(t_bar_cnt));
                                    }
                                }
                            }

                            mAdapter.notifyDataSetChanged();
                            search_detail_listView.setAdapter(mAdapter);
                            mBarcode.add(barcodeScan);

                            mOneBtnPopup = new OneBtnPopup(getActivity(), "등록되었습니다.", R.drawable.popup_title_alert, new Handler() {
                                @Override
                                public void handleMessage(Message msg) {
                                    if (msg.what == 1) {
                                        mOneBtnPopup.hideDialog();
                                    }

                                }
                            });

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
            public void onFailure(Call<CstInvModel> call, Throwable t) {
                Utils.LogLine(t.getMessage());
                Utils.Toast(mContext, getString(R.string.error_network));
            }
        });
    }//Close

    /**
     * 재고 위치 팝업 후 추가할 프로시저 호출
     */
    private void StoreScanPop(String gbn) {

        ApiClientService service = ApiClientService.retrofit.create(ApiClientService.class);

        Call<CstInvPopupModel> call = service.ScanSearchPop("sp_get_lotinfo", barcodeScan, gbn);

        call.enqueue(new Callback<CstInvPopupModel>() {
            @Override
            public void onResponse(Call<CstInvPopupModel> call, Response<CstInvPopupModel> response) {
                if (response.isSuccessful()) {
                    mcstPopModel = response.body();
                    final CstInvPopupModel model = response.body();
                    Utils.Log("model.. ==> :" + new Gson().toJson(model));
                    if (mcstPopModel != null) {
                        if (mcstPopModel.getFlag() == ResultModel.SUCCESS) {
                            mNoBtnPopup.hideDialog();
                            if (model.getFlag() != 0) {
                                beg_barocde = " ";
                                Utils.Toast(mContext, "조회된 데이터가 없습니다.");
                                return;
                            }

                            if (model.getFlag() == 0) {
                            }


                            mTwoBtnPopup = new TwoBtnPopup(getActivity(), "재고위치: " + mcstPopModel.getItems().get(0).getWh_name() + "\n재고: " + mcstPopModel.getItems().get(0).getInv_qty() + "\n" +
                                    "등록하시겠습니까?", R.drawable.popup_title_alert, new Handler() {
                                @Override
                                public void handleMessage(Message msg) {
                                    if (msg.what == 1) {
                                        mTwoBtnPopup.hideDialog();
                                        mRegisterBtnPopup = new RegisterBtnPopup(getActivity(), "상태값을 선택하세요.", R.drawable.popup_title_alert, new Handler() {
                                            @Override
                                            public void handleMessage(Message msg) {
                                                if (msg.what == 1) {
                                                    mRegisterBtnPopup.hideDialog();
                                                    //obj = 0 판매용, obj = 1 시타용
                                                    StoreScanAdd(String.valueOf(msg.obj));
                                                }else{
                                                    beg_barocde = " ";
                                                }
                                            }
                                        });
                                    }else{
                                        beg_barocde = " ";
                                    }
                                }
                            });

                                /*for (int i = 0; i < model.getItems().size(); i++) {
                                    chk = "체크노노";
                                    CstInvModel.Item item = (CstInvModel.Item) model.getItems().get(i);
                                    mAdapter.addData(item);
                                }*/


                            /*for (int k = 0; k < mAdapter.getItemCount(); k++) {
                                if (mAdapter.itemsList.get(k).getLot_no1().equals(barcodeScan) || mAdapter.itemsList.get(k).getLot_no().equals(barcodeScan)) {
                                    if (mAdapter.itemsList.get(k).getSts().equals("불일치") && mAdapter.itemsList.get(k).getC_yn().equals("판매용")) {
                                        //Log.d("불일치:", "판매");
                                        b_bar_cnt++;
                                        n_scan_cnt.setText(Integer.toString(b_bar_cnt));
                                    }

                                    if (mAdapter.itemsList.get(k).getSts().equals("불일치") && mAdapter.itemsList.get(k).getC_yn().equals("시타용")) {
                                        //Log.d("불일치:", "시타");
                                        t_bar_cnt++;
                                        y_scan_cnt.setText(Integer.toString(t_bar_cnt));
                                    }
                                }
                            }

                            mAdapter.notifyDataSetChanged();
                            search_detail_listView.setAdapter(mAdapter);
                            mBarcode.add(barcodeScan);*/

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
            public void onFailure(Call<CstInvPopupModel> call, Throwable t) {
                Utils.LogLine(t.getMessage());
                Utils.Toast(mContext, getString(R.string.error_network));
            }
        });
    }//Close


    /**
     * LOTNO 아예없으면 등록X
     */
    private void StoreScanAllSearch() {
        ApiClientService service = ApiClientService.retrofit.create(ApiClientService.class);

        Call<CstInvModel> call = service.LotSearch("sp_get_lot_inv_chk", barcodeScan);

        call.enqueue(new Callback<CstInvModel>() {
            @Override
            public void onResponse(Call<CstInvModel> call, Response<CstInvModel> response) {
                if (response.isSuccessful()) {
                    //mcstInvModel = response.body();
                    final CstInvModel model = response.body();
                    Utils.Log("model ==> :" + new Gson().toJson(model));
                    if (mcstInvModel != null) {
                        if (mcstInvModel.getFlag() == ResultModel.SUCCESS) {

                            if (model.getFlag() == -1) {
                                beg_barocde = " ";
                                Utils.Toast(mContext, "조회된 데이터가 없습니다.");
                                mNoBtnPopup.hideDialog();
                                return;
                            }

                            if (model.getFlag() == 0) {
                                StoreScanPop("");
                            }

                            /*mTwoBtnPopup = new TwoBtnPopup(getActivity(), mcstInvModel.getItems().get(0).getWh_name() + "위치에 재고가 있습니다 \n" +
                                    "등록하시겠습니까?", R.drawable.popup_title_alert, new Handler() {
                                    @Override
                                    public void handleMessage(Message msg) {
                                        if (msg.what == 1) {
                                            mTwoBtnPopup.hideDialog();
                                            mRegisterBtnPopup = new RegisterBtnPopup(getActivity(), "상태값을 선택하세요.", R.drawable.popup_title_alert, new Handler() {
                                                @Override
                                                public void handleMessage(Message msg) {
                                                    if (msg.what == 1) {
                                                        mRegisterBtnPopup.hideDialog();
                                                        //obj = 0 판매용, obj = 1 시타용
                                                        StoreScanAdd(String.valueOf(msg.obj));
                                                    }
                                                }
                                            });
                                        }
                                    }
                                });*/


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
            public void onFailure(Call<CstInvModel> call, Throwable t) {
                Utils.LogLine(t.getMessage());
                Utils.Toast(mContext, getString(R.string.error_network));
            }
        });
    }//Close


    public class DetailAdapter extends RecyclerView.Adapter<DetailAdapter.ViewHolder> {

        List<CstInvModel.Item> itemsList;
        Activity mActivity;
        Handler mHandler = null;


        public DetailAdapter(Activity context) {
            mActivity = context;
        }

        public void setData(List<CstInvModel.Item> list) {
            itemsList = list;
        }

        public void clearData() {
            if (itemsList != null) itemsList.clear();
        }

        public void setRetHandler(Handler h) {
            this.mHandler = h;
        }

        public List<CstInvModel.Item> getData() {
            return itemsList;
        }

        public void addData(CstInvModel.Item item) {
            if (itemsList == null) itemsList = new ArrayList<>();
            itemsList.add(item);
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, final int z) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cell_cst_inv_list, viewGroup, false);
            ViewHolder holder = new ViewHolder(v);
            return holder;
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {

            final CstInvModel.Item item = itemsList.get(position);

            holder.tv_c_yn.setText(item.getC_yn());
            holder.itm_name.setText(item.getItm_name());
            //holder.itm_name.setText(item.getLot_no1());
            holder.tv_yn.setText(item.getSts());
            holder.tv_lot_mix_no.setText(item.getLot_no() + "           " + item.getLast_date());


            //특정 데이터시 텍스트 색 변경
            for (int i = 0; i < mAdapter.getItemCount(); i++) {
                if (itemsList.get(i).getSts().equals("일치")) {
                    if (position == i) {
                        holder.tv_yn.setTextColor(Color.BLUE);
                    }
                }
                if (itemsList.get(i).getSts().equals("불일치")) {
                    if (position == i) {
                        holder.tv_yn.setTextColor(Color.RED);
                    }
                }


            }//Close for


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

            TextView tv_c_yn;
            TextView itm_name;
            TextView tv_yn;
            TextView tv_lot_mix_no;


            public ViewHolder(View view) {
                super(view);

                tv_c_yn = view.findViewById(R.id.tv_c_yn);
                itm_name = view.findViewById(R.id.tv_itm_name);
                tv_yn = view.findViewById(R.id.tv_yn);
                tv_lot_mix_no = view.findViewById(R.id.tv_lot_mix_no);


            }
        }


    }//Close Adapter


    /**
     * 이동처리 저장
     */
    private void request_make_temp() {
        ApiClientService service = ApiClientService.retrofit.create(ApiClientService.class);
        JsonObject json = new JsonObject();

        JsonArray list = new JsonArray();

        //List<MatOutSerialScanModel.Item> items = scanAdapter.getData();
        List<CstInvModel.Item> items = mAdapter.getData();

        for (int i = 0; i < mAdapter.getItemCount(); i++) {
            if (!mAdapter.itemsList.get(i).getSts().equals("대기") && mAdapter.itemsList.get(i).getC_yn().equals("판매용")) {
                JsonObject obj = new JsonObject();

                obj.addProperty("cst_code", mOrder.getCst_code());
                obj.addProperty("itm_code", mAdapter.itemsList.get(i).getItm_code());
                list.add(obj);
            }


        }

        json.add("detail", list);

        Utils.Log("new Gson().toJson(json) ==> : " + new Gson().toJson(json));

        RequestBody body = RequestBody.create(MediaType.parse("application/json"), new Gson().toJson(json));

        Call<ResultModel> call = service.postTempSave(body);

        call.enqueue(new Callback<ResultModel>() {
            @Override
            public void onResponse(Call<ResultModel> call, Response<ResultModel> response) {
                if (response.isSuccessful()) {
                    ResultModel model = response.body();
                    //Utils.Log("model ==> : "+new Gson().toJson(model));
                    if (model != null) {
                        if (model.getFlag() == ResultModel.SUCCESS) {

                            Intent intent = new Intent(getActivity(), BaseActivity.class);
                            intent.putExtra("menu", Define.MENU_STORE_SEARCH_CALC);
                            Bundle extras = new Bundle();
                            extras.putString("cst_nm", tv_cst_nm.getText().toString());
                            extras.putString("cst_code", mOrder.getCst_code());
                            extras.putString("scan_qty", n_scan_cnt.getText().toString());
                            intent.putExtra("args", extras);
                            startActivityForResult(intent, 100);

                        } else {
                            mOneBtnPopup = new OneBtnPopup(getActivity(), model.getMSG(), R.drawable.popup_title_alert, new Handler() {
                                @Override
                                public void handleMessage(Message msg) {
                                    if (msg.what == 1) {
                                        mOneBtnPopup.hideDialog();

                                    }
                                }
                            });
                        }
                    }
                } else {
                    Utils.LogLine(response.message());
                    mTwoBtnPopup = new TwoBtnPopup(getActivity(), "실패하였습니다.\n 재전송 하시겠습니까?", R.drawable.popup_title_alert, new Handler() {
                        @Override
                        public void handleMessage(Message msg) {
                            if (msg.what == 1) {
                                request_make_temp();
                                mTwoBtnPopup.hideDialog();

                            }
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<ResultModel> call, Throwable t) {
                Utils.LogLine(t.getMessage());
                mTwoBtnPopup = new TwoBtnPopup(getActivity(), "실패하였습니다.\n 재전송 하시겠습니까?", R.drawable.popup_title_alert, new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        if (msg.what == 1) {
                            request_make_temp();
                            mTwoBtnPopup.hideDialog();

                        }
                    }
                });
            }
        });

    }//Close calc_save


    /**
     * ERP전송
     */
    private void cst_stk_save() {

        ApiClientService service = ApiClientService.retrofit.create(ApiClientService.class);
        JsonObject json = new JsonObject();
        String userID = (String) SharedData.getSharedData(mContext, SharedData.UserValue.USER_ID.name(), "");
        JsonArray list = new JsonArray();
        List<CstInvModel.Item> items = mAdapter.getData();

        for (int i = 0; i < mAdapter.getItemCount(); i++) {
            if (!mAdapter.itemsList.get(i).getSts().equals("대기")) {
                JsonObject obj = new JsonObject();

                obj.addProperty("itm_code", mAdapter.itemsList.get(i).getItm_code());
                obj.addProperty("lot_no", mAdapter.itemsList.get(i).getLot_no1());
                obj.addProperty("lot_no_mix", mAdapter.itemsList.get(i).getLot_no());
                list.add(obj);
            }
        }

        json.addProperty("p_wh_code", mOrder.getCst_code());
        json.addProperty("p_user_id", userID);
        json.add("detail", list);

        Utils.Log("new Gson().toJson(json) ==> : " + new Gson().toJson(json));

        RequestBody body = RequestBody.create(MediaType.parse("application/json"), new Gson().toJson(json));

        Call<CstInvModel> call = service.CstInvSave(body);

        call.enqueue(new Callback<CstInvModel>() {
            @Override
            public void onResponse(Call<CstInvModel> call, Response<CstInvModel> response) {
                if (response.isSuccessful()) {
                    CstInvModel model = response.body();
                    //Utils.Log("model ==> : "+new Gson().toJson(model));
                    if (model != null) {
                        if (model.getFlag() == CstInvModel.SUCCESS) {
                            mOneBtnPopup = new OneBtnPopup(getActivity(), "처리되었습니다.", R.drawable.popup_title_alert, new Handler() {
                                @Override
                                public void handleMessage(Message msg) {
                                    if (msg.what == 1) {
                                        getActivity().finish();
                                        mOneBtnPopup.hideDialog();
                                    }
                                }
                            });
                        } else {
                            mOneBtnPopup = new OneBtnPopup(getActivity(), model.getMSG(), R.drawable.popup_title_alert, new Handler() {
                                @Override
                                public void handleMessage(Message msg) {
                                    if (msg.what == 1) {
                                        mOneBtnPopup.hideDialog();
                                    }
                                }
                            });
                        }
                    }
                } else {
                    //Utils.LogLine(response.message());
                    mTwoBtnPopup = new TwoBtnPopup(getActivity(), "전송을 실패하였습니다.\n 재전송 하시겠습니까?", R.drawable.popup_title_alert, new Handler() {
                        @Override
                        public void handleMessage(Message msg) {
                            if (msg.what == 1) {
                                cst_stk_save();
                                mTwoBtnPopup.hideDialog();
                            }
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<CstInvModel> call, Throwable t) {
                //Utils.LogLine(t.getMessage());
                mTwoBtnPopup = new TwoBtnPopup(getActivity(), "전송을 실패하였습니다.\n 재전송 하시겠습니까?", R.drawable.popup_title_alert, new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        if (msg.what == 1) {
                            cst_stk_save();
                            mTwoBtnPopup.hideDialog();
                        }
                    }
                });
            }
        });

    }//Close erp_send


}//Close Fragmnet
