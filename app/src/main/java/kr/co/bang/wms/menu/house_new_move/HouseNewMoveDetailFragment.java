package kr.co.bang.wms.menu.house_new_move;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.honeywell.aidc.BarcodeReadEvent;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import kr.co.bang.wms.R;
import kr.co.bang.wms.common.Define;
import kr.co.bang.wms.common.SharedData;
import kr.co.bang.wms.common.Utils;
import kr.co.bang.wms.custom.CommonFragment;
import kr.co.bang.wms.honeywell.AidcReader;
import kr.co.bang.wms.menu.main.BaseActivity;
import kr.co.bang.wms.menu.main.MainActivity;
import kr.co.bang.wms.menu.out_list.MorOutPickingFragment;
import kr.co.bang.wms.menu.popup.LocationListPopup;
import kr.co.bang.wms.menu.popup.OneBtnPopup;
import kr.co.bang.wms.menu.popup.TwoBtnPopup;
import kr.co.bang.wms.model.MatOutDetailDel;
import kr.co.bang.wms.model.MatOutDetailGet;
import kr.co.bang.wms.model.MatOutDetailModel;
import kr.co.bang.wms.model.MatOutListModel;
import kr.co.bang.wms.model.MatOutSerialScanModel;
import kr.co.bang.wms.model.MatScanCntModel;
import kr.co.bang.wms.model.MorSerialScan;
import kr.co.bang.wms.model.ResultModel;
import kr.co.bang.wms.model.WarehouseModel;
import kr.co.bang.wms.network.ApiClientService;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.internal.Util;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HouseNewMoveDetailFragment extends CommonFragment {

    TextView req_dpt_name, req_code, req_date;
    MatOutListModel moveModel;
    MatOutListModel.Item mOrder;
    int mPosition = -1;
    MatOutDetailDel mDetailDel;
    MatOutDetailGet mDetailGet;
    List<MatOutDetailGet.Item> mDetailGetList;
    MatOutDetailModel detailModel;
    MatOutSerialScanModel mSerialModel;
    MatScanCntModel mScanCntModel;
    List<MatScanCntModel.Items> mCntList;
    List<MatOutDetailModel.Item> mDetailList;
    List<MatOutSerialScanModel.Item> mSerialList;
    MatOutSerialScanModel.Item mserialItems;
    ListAdapter mAdapter;
    ScanAdapter scanAdapter;
    ListAdapterGet mListAdapter;
    Handler mHandler;
    Context mContext;
    ListView move_detail_listView;
    String barcode_scan;
    String address;
    OneBtnPopup mOneBtnPopup;
    TwoBtnPopup mTwoBtnPopup;
    int qty = 0;
    LocationListPopup mLocationListPopup;
    EditText et_wh;
    String wh_code, s_itm_code, s_inv_qty, beg_barcode = null;
    WarehouseModel.Items WareLocation;
    List<WarehouseModel.Items> mWarehouseList;
    ImageButton bt_wh, bt_next;
    BaseActivity mBase;
    List<String> mBarcode;
    List<String> LBarcode;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
        mBarcode = new ArrayList<>();
        LBarcode = new ArrayList<>();


    }//Close onCreate

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.frag_house_new_move_detail, container, false);

        WifiManager manager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = manager.getConnectionInfo();
        address = info.getMacAddress();


        req_dpt_name = v.findViewById(R.id.req_dpt_name);
        req_code = v.findViewById(R.id.req_code);
        req_date = v.findViewById(R.id.req_date);
        move_detail_listView = v.findViewById(R.id.move_detail_listView);
        scanAdapter = new ScanAdapter();
        mAdapter = new ListAdapter();
        mListAdapter = new ListAdapterGet();
        et_wh = v.findViewById(R.id.et_wh);
        bt_wh = v.findViewById(R.id.bt_wh);
        bt_next = v.findViewById(R.id.bt_next);

        Bundle arguments = getArguments();
        moveModel = (MatOutListModel) arguments.getSerializable("model");
        mPosition = arguments.getInt("position");
        mOrder = moveModel.getItems().get(mPosition);
        mHandler = handler;

        req_code.setText(mOrder.getReq_mat_code());
        req_date.setText(mOrder.getReq_mat_date());
        req_dpt_name.setText(mOrder.getEmp_name() + " / " + mOrder.getDpt_name());

        MatListSearch();
        move_detail_listView.setAdapter(mAdapter);
        mat_out_detail_get();

        bt_wh.setOnClickListener(onClickListener);
        bt_next.setOnClickListener(onClickListener);

        return v;
    }//Close onCreateView

    private void DetailScanList(int position) {
        List<MatOutSerialScanModel.Item> datas = new ArrayList<>();

        Intent intent = new Intent(getActivity(), BaseActivity.class);
        intent.putExtra("menu", Define.MENU_HOUSE_MOVE_SCAN_DATAIL);
        Bundle extras = new Bundle();


        extras.putString("req_code", req_code.getText().toString());              //전표타입: 주문: O, AS: A
        extras.putString("mat_code", mOrder.getReq_mat_code());
        extras.putSerializable("model", detailModel);
        extras.putSerializable("position", position);
        intent.putExtra("args", extras);

        startActivityForResult(intent, 100);
    }

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
                    barcode_scan = barcode;

                    if (mBarcode.contains(barcode)) {
                        Utils.Toast(mContext, "동일한 바코드를 스캔하셨습니다.");
                        return;
                    }

                    if (LBarcode.contains(barcode)) {
                        Utils.Toast(mContext, "동일한 바코드를 스캔하셨습니다.");
                        return;
                    }

                    if (beg_barcode != null) {
                        if (beg_barcode.equals(barcode_scan)) {

                            Utils.Toast(mContext, "동일한 바코드를 스캔하였습니다.");
                            return;
                        }
                    }
                    if (mSerialList != null) {
                        for (int i = 0; i < scanAdapter.getCount(); i++) {
                            if (mSerialList.get(i).getLot_no().equals(barcode_scan)) {
                                Utils.Toast(mContext, "동일한 바코드를 스캔하였습니다.");
                                return;
                            }
                        }
                    }

                    if (mDetailGetList != null) {
                        for (int j = 0; j < mDetailGetList.size(); j++) {
                            if (mDetailGetList.get(j).getSerial_no().equals(barcode_scan)) {
                                Utils.Toast(mContext, "동일한 바코드를 스캔하였습니다.");
                                return;
                            }
                        }
                    }

                    if (barcode_scan.length() == 17) {
                        beg_barcode = barcode;
                        MatSerialCount();
                        //MatSerialScan();

                    } else {
                        beg_barcode = barcode;
                        MatSerialCount();
                        //MatSerialScanItem();

                    }

                }
            }
        });
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        mat_out_scan_del();
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.bt_wh:
                    requestWhlist();
                    break;

                case R.id.bt_next:
                    bt_next.setEnabled(false);
                    if (!et_wh.getText().toString().equals("")) {

                        if (mDetailGetList != null) {
                            bt_next.setEnabled(false);
                            request_mat_out_save();
                        } else {
                            bt_next.setEnabled(true);
                            Utils.Toast(mContext, "이동처리 할 스캔을 진행해주세요.");
                            return;
                        }
                    } else {
                        bt_next.setEnabled(true);
                        Utils.Toast(mContext, "입고처를 선택해주세요.");
                        return;
                    }

                    break;

            }

        }
    };

    /**
     * 창고이동 상세조회 Detail
     */
    private void MatListSearch() {
        ApiClientService service = ApiClientService.retrofit.create(ApiClientService.class);

        Call<MatOutDetailModel> call = service.matDetailList("sp_pda_mat_out_detail", address, mOrder.getReq_mat_code());

        call.enqueue(new Callback<MatOutDetailModel>() {
            @Override
            public void onResponse(Call<MatOutDetailModel> call, Response<MatOutDetailModel> response) {
                if (response.isSuccessful()) {
                    detailModel = response.body();
                    final MatOutDetailModel model = response.body();
                    Utils.Log("model ==> :" + new Gson().toJson(model));
                    if (detailModel != null) {
                        if (detailModel.getFlag() == ResultModel.SUCCESS) {

                            if (model.getItems().size() > 0) {
                                //for (int i = 0; i < model.getItems().size(); i++) {

                                //MatOutDetailModel.Item item = (MatOutDetailModel.Item) model.getItems().get(i);
                                //mAdapter.addData(item);
                                mDetailList = model.getItems();
                                mAdapter.notifyDataSetChanged();
                                //}


                            }

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
            public void onFailure(Call<MatOutDetailModel> call, Throwable t) {
                Utils.LogLine(t.getMessage());
                Utils.Toast(mContext, getString(R.string.error_network));
            }
        });
    }

    /**
     * 시리얼스캔 (박스 17자리)
     */
    private void MatSerialScan() {

        ApiClientService service = ApiClientService.retrofit.create(ApiClientService.class);

        Call<MatOutSerialScanModel> call = service.matSerialScan("sp_pda_mat_out_scan", barcode_scan, mOrder.getReq_mat_code());

        call.enqueue(new Callback<MatOutSerialScanModel>() {
            @Override
            public void onResponse(Call<MatOutSerialScanModel> call, Response<MatOutSerialScanModel> response) {
                if (response.isSuccessful()) {
                    mSerialModel = response.body();

                    final MatOutSerialScanModel model = response.body();
                    Utils.Log("model scan ==> :" + new Gson().toJson(model));
                    if (mSerialModel != null) {
                        if (mSerialModel.getFlag() == ResultModel.SUCCESS) {
                            //MatSerialCountBox("sp_pda_mat_out_scan_cnt", address, mOrder.getReq_mat_code(), mSerialModel.getItems().get(0).getLot_no());


                            //moveList = model.getItems();
                            if (model.getItems().size() > 0) {
                                if (scanAdapter.getItemCount() != 0) {
                                    scanAdapter.clearData();
                                }
                                for (int a = 0; a < model.getItems().size(); a++) {

                                    MatOutSerialScanModel.Item item = (MatOutSerialScanModel.Item) model.getItems().get(a);
                                    scanAdapter.addData(item);

                                    s_itm_code = item.getItm_code();
                                    s_inv_qty = Integer.toString(item.getInv_qty());

                                    for (int i = 0; i < mAdapter.getCount(); i++) {
                                        int count = 0;
                                        if (mDetailList.get(i).getItm_code() == null) {
                                        } else {
                                            if (mDetailList.get(i).getItm_code().equals(item.getItm_code())) {

                                                if (mDetailList.get(i).getScan_qty() >= 0) {

                                                    count += mDetailList.get(i).getScan_qty();
                                                    count++;


                                                } else {
                                                    try {
                                                        count += mSerialList.get(i).getInv_qty();
                                                    } catch (IndexOutOfBoundsException e) {
                                                        e.printStackTrace();
                                                    }
                                                }


                                                mDetailList.get(i).setScan_qty(count);


                                            }
                                        }
                                    }

                                }


                                mListAdapter.notifyDataSetChanged();
                                mAdapter.notifyDataSetChanged();
                                scanAdapter.notifyDataSetChanged();
                                LBarcode.add(barcode_scan);

                                //MatSerialCount();
                                requestScan();
                            }

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
            public void onFailure(Call<MatOutSerialScanModel> call, Throwable t) {
                Utils.LogLine(t.getMessage());
                Utils.Toast(mContext, getString(R.string.error_network));
            }
        });
    }

    /**
     * 시리얼스캔 (품목13자리)
     */
    private void MatSerialScanItem() {

        ApiClientService service = ApiClientService.retrofit.create(ApiClientService.class);

        Call<MatOutSerialScanModel> call = service.matSerialScan("sp_pda_mat_out_scan", barcode_scan, mOrder.getReq_mat_code());

        call.enqueue(new Callback<MatOutSerialScanModel>() {
            @Override
            public void onResponse(Call<MatOutSerialScanModel> call, Response<MatOutSerialScanModel> response) {
                if (response.isSuccessful()) {
                    mSerialModel = response.body();

                    final MatOutSerialScanModel model = response.body();
                    Utils.Log("model scan ==> :" + new Gson().toJson(model));
                    if (mSerialModel != null) {
                        if (mSerialModel.getFlag() == ResultModel.SUCCESS) {

                            //moveList = model.getItems();
                            if (model.getItems().size() > 0) {

                                for (int a = 0; a < model.getItems().size(); a++) {

                                    MatOutSerialScanModel.Item item = (MatOutSerialScanModel.Item) model.getItems().get(a);
                                    scanAdapter.addData(item);

                                    s_itm_code = item.getItm_code();
                                    s_inv_qty = Integer.toString(item.getInv_qty());

                                    for (int i = 0; i < mAdapter.getCount(); i++) {
                                        int count = 0;
                                        if (mDetailList.get(i).getItm_code() == null) {
                                        } else {
                                            if (mDetailList.get(i).getItm_code().equals(item.getItm_code())) {

                                                if (mDetailList.get(i).getScan_qty() >= 0) {

                                                    count += mDetailList.get(i).getScan_qty();
                                                    count++;


                                                } else {
                                                    count += mSerialList.get(i).getInv_qty();
                                                   /* try {
                                                        count += mSerialList.get(i).getInv_qty();
                                                    } catch (IndexOutOfBoundsException e) {
                                                        e.printStackTrace();
                                                    }*/
                                                }
                                                mDetailList.get(i).setScan_qty(count);
                                            }
                                        }
                                    }

                                }


                                mListAdapter.notifyDataSetChanged();
                                mAdapter.notifyDataSetChanged();
                                scanAdapter.notifyDataSetChanged();
                                mBarcode.add(barcode_scan);

                                //MatSerialCount();
                                requestScanItem();
                            }

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
            public void onFailure(Call<MatOutSerialScanModel> call, Throwable t) {
                Utils.LogLine(t.getMessage());
                Utils.Toast(mContext, getString(R.string.error_network));
            }
        });
    }

    /**
     * 중복데이터 카운트 체크
     */
    private void MatSerialCount() {
        ApiClientService service = ApiClientService.retrofit.create(ApiClientService.class);

        Call<MatScanCntModel> call = service.matScanCnt("sp_pda_mat_out_scan_cnt", address, mOrder.getReq_mat_code(), barcode_scan);

        call.enqueue(new Callback<MatScanCntModel>() {
            @Override
            public void onResponse(Call<MatScanCntModel> call, Response<MatScanCntModel> response) {

                if (response.isSuccessful()) {
                    mScanCntModel = response.body();
                    final MatScanCntModel model = response.body();
                    Utils.Log("model get ==> :" + new Gson().toJson(model));
                    if (mScanCntModel != null) {
                        if (mScanCntModel.getFlag() == ResultModel.SUCCESS) {
                            mCntList = model.getItems();
                            if (mCntList != null) {
                                for (int i = 0; i < model.getItems().size(); i++) {
                                    MatScanCntModel.Items item = (MatScanCntModel.Items) model.getItems().get(i);
                                    if (item.getR_CNT() == 0) {
                                        //MatSerialScanItem();
                                        if (barcode_scan.length() == 17) {
                                            MatSerialScan();
                                        } else {
                                            MatSerialScanItem();
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Utils.LogLine(response.message());
                    Utils.Toast(mContext, response.code() + " : " + response.message());
                }
            }


            @Override
            public void onFailure(Call<MatScanCntModel> call, Throwable t) {
                Utils.LogLine(t.getMessage());
                Utils.Toast(mContext, getString(R.string.error_network));
            }
        });
    }

    /**
     * 스캔내역 조회(작업중인 내역)
     */
    private void mat_out_detail_get() {
        ApiClientService service = ApiClientService.retrofit.create(ApiClientService.class);

        Call<MatOutDetailGet> call = service.matDetailGet("sp_pda_mat_out_detail_get", address, mOrder.getReq_mat_code(), "");

        call.enqueue(new Callback<MatOutDetailGet>() {
            @Override
            public void onResponse(Call<MatOutDetailGet> call, Response<MatOutDetailGet> response) {

                if (response.isSuccessful()) {
                    mDetailGet = response.body();
                    final MatOutDetailGet model = response.body();
                    Utils.Log("model get ==> :" + new Gson().toJson(model));
                    if (moveModel != null) {
                        if (moveModel.getFlag() == ResultModel.SUCCESS) {
                            mDetailGetList = model.getItems();
                            //if (model.getItems().size() > 0) {
                            if (mDetailGetList != null) {
                                for (int i = 0; i < model.getItems().size(); i++) {

                                    MatOutDetailGet.Item item = (MatOutDetailGet.Item) model.getItems().get(i);
                                    //mListAdapter.addData(item);
                                    mDetailGetList = model.getItems();
                                    mListAdapter.notifyDataSetChanged();
                                }
                            }

                        } else {

                        }
                    }
                } else {
                    Utils.LogLine(response.message());
                    Utils.Toast(mContext, response.code() + " : " + response.message());
                }
            }


            @Override
            public void onFailure(Call<MatOutDetailGet> call, Throwable t) {
                Utils.LogLine(t.getMessage());
                Utils.Toast(mContext, getString(R.string.error_network));
            }
        });
    }

    /**
     * 입고처 리스트
     */
    private void requestWhlist() {
        ApiClientService service = ApiClientService.retrofit.create(ApiClientService.class);

        Call<WarehouseModel> call = service.morWarehouse("sp_pda_mst_wh_list", "");

        call.enqueue(new Callback<WarehouseModel>() {
            @Override
            public void onResponse(Call<WarehouseModel> call, Response<WarehouseModel> response) {
                if (response.isSuccessful()) {
                    WarehouseModel model = response.body();
                    //Utils.Log("model ==> :" + new Gson().toJson(model));
                    if (model != null) {
                        if (model.getFlag() == ResultModel.SUCCESS) {
                            mLocationListPopup = new LocationListPopup(getActivity(), model.getItems(), R.drawable.popup_title_searchloc, new Handler() {
                                @Override
                                public void handleMessage(Message msg) {
                                    WarehouseModel.Items item = (WarehouseModel.Items) msg.obj;
                                    WareLocation = item;
                                    et_wh.setText("[" + WareLocation.getWh_code() + "] " + WareLocation.getWh_name());
                                    //mAdapter.notifyDataSetChanged();
                                    wh_code = WareLocation.getWh_code();
                                    mLocationListPopup.hideDialog();
                                }
                            });
                            mWarehouseList = model.getItems();


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
            public void onFailure(Call<WarehouseModel> call, Throwable t) {
                Utils.LogLine(t.getMessage());
                Utils.Toast(mContext, getString(R.string.error_network));
            }
        });
    }

    /**
     * 스캔내역 삭제
     */
    private void mat_out_scan_del() {
        ApiClientService service = ApiClientService.retrofit.create(ApiClientService.class);

        Call<MatOutDetailDel> call = service.ScanDel("sp_pda_mat_out_scan_del", address, mOrder.getReq_mat_code());

        call.enqueue(new Callback<MatOutDetailDel>() {
            @Override
            public void onResponse(Call<MatOutDetailDel> call, Response<MatOutDetailDel> response) {
                if (response.isSuccessful()) {
                    final MatOutDetailDel model = response.body();
                    Utils.Log("model ==> :" + new Gson().toJson(model));

                } else {
                    Utils.LogLine(response.message());
                    Utils.Toast(mContext, response.code() + " : " + response.message());
                }
            }

            @Override
            public void onFailure(Call<MatOutDetailDel> call, Throwable t) {
                Utils.LogLine(t.getMessage());
                Utils.Toast(mContext, getString(R.string.error_network));
            }
        });
    }

    class ListAdapter extends BaseAdapter {
        LayoutInflater mInflater;

        public ListAdapter() {
            mInflater = LayoutInflater.from(mContext);
        }

        public int getItemCount() {
            return (null == mDetailList ? 0 : mDetailList.size());
        }

        public void addData(MatOutDetailModel.Item item) {
            if (mDetailList == null) mDetailList = new ArrayList<>();
            mDetailList.add(item);
        }

        public void clearData() {
            mDetailList.clear();
        }

        public List<MatOutDetailModel.Item> getData() {
            return mDetailList;
        }

        @Override
        public int getCount() {
            if (mDetailList == null) {
                return 0;
            }

            return mDetailList.size();
        }


        @Override
        public MatOutDetailModel.Item getItem(int position) {
            return mDetailList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View v = convertView;
            ListAdapter.ViewHolder holder;
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if (v == null) {
                holder = new ListAdapter.ViewHolder();
                v = inflater.inflate(R.layout.cell_detail_list, null);

                holder.itm_name = v.findViewById(R.id.itm_name);
                holder.scan_qty = v.findViewById(R.id.scan_qty);
                holder.req_mat_qty = v.findViewById(R.id.req_mat_qty);

                v.setTag(holder);

            } else {
                holder = (ListAdapter.ViewHolder) v.getTag();
            }

            final MatOutDetailModel.Item data = mDetailList.get(position);
            holder.itm_name.setText(data.getItm_name());
            holder.scan_qty.setText(Integer.toString(data.getScan_qty()));
            holder.req_mat_qty.setText(Integer.toString(data.getReq_mat_qty()));


            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Message msg = mHandler.obtainMessage();
                    msg.what = 1;
                    msg.obj = data;
                    mHandler.sendMessage(msg);
                    DetailScanList(position);
                }
            });


            return v;
        }

        public class ViewHolder {
            TextView itm_name;
            TextView scan_qty;
            TextView req_mat_qty;


        }


    }//Close Adapter

    //스캔시 어댑터
    class ScanAdapter extends BaseAdapter {
        List<MatOutDetailModel.Item> itemList;
        LayoutInflater mInflater;

        public ScanAdapter() {
            mInflater = LayoutInflater.from(mContext);
        }

        public int getItemCount() {
            return (null == mSerialList ? 0 : mSerialList.size());
        }

        public void addData(MatOutSerialScanModel.Item item) {
            if (mSerialList == null) mSerialList = new ArrayList<>();
            mSerialList.add(item);
        }


        public void clearData() {
            mSerialList.clear();
        }

        public List<MatOutSerialScanModel.Item> getData() {
            return mSerialList;
        }

        @Override
        public int getCount() {
            if (mSerialList == null) {
                return 0;
            }

            return mSerialList.size();
        }

        @Override
        public MatOutSerialScanModel.Item getItem(int position) {
            return mSerialList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final MatOutDetailModel.Item item = itemList.get(position);
            View v = convertView;
            ViewHolder holder;
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if (v == null) {
                holder = new ViewHolder();
                v = inflater.inflate(R.layout.cell_detail_list, null);

                /*holder.itm_name = v.findViewById(R.id.itm_name);
                holder.scan_qty = v.findViewById(R.id.scan_qty);
                holder.req_mat_qty = v.findViewById(R.id.req_mat_qty);*/

                v.setTag(holder);

            } else {
                holder = (ViewHolder) v.getTag();
            }

            //final MatOutDetailModel.Item data = mDetailList.get(position);
            final MatOutSerialScanModel.Item datas = mSerialList.get(position);
            //holder.itm_name.setText(data.getItm_name());
            //holder.scan_qty.setText(Float.toString(data.getScan_qty()));
            //holder.req_mat_qty.setText(Float.toString(data.getReq_mat_qty()));


            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Message msg = mHandler.obtainMessage();
                    msg.what = 1;
                    msg.obj = datas;
                    mHandler.sendMessage(msg);
                    //DetailScanList(position);
                }
            });
           /* float cnt = 0;
            if (item.getItems() != null) {
                for (MatOutDetailModel.Item datas : item.getItems()) {
                    cnt += datas.getScan_qty();
                }
            }
            itemList.get(position).setReq_mat_qty(cnt);*/


            return v;
        }

        public class ViewHolder {
            /*TextView itm_name;
            TextView scan_qty;
            TextView req_mat_qty;*/


        }
    }//Close ScanAdapter

    class ListAdapterGet extends BaseAdapter {
        LayoutInflater mInflater;

        public ListAdapterGet() {
            mInflater = LayoutInflater.from(mContext);
        }

        public int getItemCount() {
            return (null == mDetailGetList ? 0 : mDetailGetList.size());
        }

        public void addData(MatOutDetailGet.Item item) {
            if (mDetailGetList == null) mDetailGetList = new ArrayList<>();
            mDetailGetList.add(item);
        }

        public void clearData() {
            mDetailGetList.clear();
        }

        public List<MatOutDetailGet.Item> getData() {
            return mDetailGetList;
        }

        @Override
        public int getCount() {
            if (mDetailGetList == null) {
                return 0;
            }

            return mDetailGetList.size();
        }


        @Override
        public MatOutDetailGet.Item getItem(int position) {
            return mDetailGetList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View v = convertView;
            ViewHolder holder;
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if (v == null) {
                holder = new ViewHolder();
                v = inflater.inflate(R.layout.cell_detail_list, null);

                holder.itm_name = v.findViewById(R.id.itm_name);
                holder.scan_qty = v.findViewById(R.id.scan_qty);
                holder.req_mat_qty = v.findViewById(R.id.req_mat_qty);

                v.setTag(holder);

            } else {
                holder = (ViewHolder) v.getTag();
            }

            final MatOutDetailGet.Item data = mDetailGetList.get(position);
            //final MatOutSerialScanModel.Item datas = mSerialList.get(position);

            holder.itm_name.setText(data.getItm_name());
            holder.scan_qty.setText(Integer.toString(data.getReq_mat_qty()));
            //holder.req_mat_qty.setText(Float.toString(datas.getInv_qty()));


            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Message msg = mHandler.obtainMessage();
                    msg.what = 1;
                    msg.obj = data;
                    mHandler.sendMessage(msg);
                    DetailScanList(position);
                }
            });


            return v;
        }

        public class ViewHolder {
            TextView itm_name;
            TextView scan_qty;
            TextView req_mat_qty;


        }


    }//Close Adapter

    /**
     * 이동처리 저장
     */
    private void request_mat_out_save() {
        /*//mDetailGetList.clear();
        //mListAdapter.notifyDataSetChanged();
        mat_out_detail_get();
        mListAdapter.notifyDataSetChanged();*/
        ApiClientService service = ApiClientService.retrofit.create(ApiClientService.class);
        JsonObject json = new JsonObject();
        String emp_code = (String) SharedData.getSharedData(mContext, "emp_code", "");
        String userID = (String) SharedData.getSharedData(mContext, SharedData.UserValue.USER_ID.name(), "");

        JsonArray list = new JsonArray();

        //List<MatOutSerialScanModel.Item> items = scanAdapter.getData();
        List<MatOutDetailGet.Item> items = mListAdapter.getData();

        for (MatOutDetailGet.Item item : items) {
            JsonObject obj = new JsonObject();
            obj.addProperty("itm_code", item.getItm_code());
            obj.addProperty("serial_no", item.getSerial_no());
            obj.addProperty("serial_qty", item.getReq_mat_qty());
            list.add(obj);
        }


        /*for (MatOutSerialScanModel.Item item : items) {
            JsonObject obj = new JsonObject();
            obj.addProperty("itm_code", item.getItm_code());
            obj.addProperty("serial_no", item.getLot_no());
            obj.addProperty("serial_qty", item.getInv_qty());
            list.add(obj);
        }*/

        json.addProperty("p_corp_code", detailModel.getItems().get(0).getCorp_code());
        json.addProperty("p_in_emp_code", emp_code);        //인수자 p_in_emp_code
        json.addProperty("p_emp_code", emp_code);           //출고자 p_emp_code
        json.addProperty("p_in_wh_code", WareLocation.getWh_code());    //입고처
        json.addProperty("req_mat_code", req_code.getText().toString());    //이동요청번호
        json.addProperty("p_user_id", userID);    //로그인ID
        json.add("detail", list);

        Utils.Log("new Gson().toJson(json) ==> : " + new Gson().toJson(json));

        RequestBody body = RequestBody.create(MediaType.parse("application/json"), new Gson().toJson(json));

        Call<ResultModel> call = service.postMatOutSave(body);

        call.enqueue(new Callback<ResultModel>() {
            @Override
            public void onResponse(Call<ResultModel> call, Response<ResultModel> response) {
                if (response.isSuccessful()) {
                    ResultModel model = response.body();
                    //Utils.Log("model ==> : "+new Gson().toJson(model));
                    if (model != null) {
                        if (model.getFlag() == ResultModel.SUCCESS) {

                            mOneBtnPopup = new OneBtnPopup(getActivity(), "이동처리 되었습니다.", R.drawable.popup_title_alert, new Handler() {
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
                                        bt_next.setEnabled(true);
                                    }
                                }
                            });
                        }
                    }
                } else {
                    Utils.LogLine(response.message());
                    bt_next.setEnabled(true);
                    mTwoBtnPopup = new TwoBtnPopup(getActivity(), "이동 전송을 실패하였습니다.\n 재전송 하시겠습니까?", R.drawable.popup_title_alert, new Handler() {
                        @Override
                        public void handleMessage(Message msg) {
                            if (msg.what == 1) {
                                request_mat_out_save();
                                mTwoBtnPopup.hideDialog();

                            }
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<ResultModel> call, Throwable t) {
                Utils.LogLine(t.getMessage());
                bt_next.setEnabled(true);
                mTwoBtnPopup = new TwoBtnPopup(getActivity(), "이동 전송을 실패하였습니다.\n 재전송 하시겠습니까?", R.drawable.popup_title_alert, new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        if (msg.what == 1) {
                            request_mat_out_save();
                            mTwoBtnPopup.hideDialog();

                        }
                    }
                });
            }
        });

    }


    /**
     * 스캔데이터 저장(박스17자리)
     */
    private void requestScan() {
        ApiClientService service = ApiClientService.retrofit.create(ApiClientService.class);
        JsonObject json = new JsonObject();
        String userID = (String) SharedData.getSharedData(mContext, SharedData.UserValue.USER_ID.name(), "");
        JsonArray list = new JsonArray();
        List<MatOutSerialScanModel.Item> items = scanAdapter.getData();
        for (MatOutSerialScanModel.Item item : items) {
            JsonObject obj = new JsonObject();
            obj.addProperty("itm_code", item.getItm_code());
            obj.addProperty("serial_no", item.getLot_no());
            obj.addProperty("serial_qty", item.getInv_qty());
            list.add(obj);
        }

        json.addProperty("p_mac_ad", address);
        json.addProperty("p_corp_code", detailModel.getItems().get(0).getCorp_code());
        json.addProperty("p_req_mat_code", mOrder.getReq_mat_code());
        json.addProperty("p_user_id", userID);
        json.add("detail", list);

        Utils.Log("new Gson().toJson(json)scan ==> : " + new Gson().toJson(json));

        RequestBody body = RequestBody.create(MediaType.parse("application/json"), new Gson().toJson(json));

        Call<ResultModel> call = service.postOutSave(body);

        call.enqueue(new Callback<ResultModel>() {
            @Override
            public void onResponse(Call<ResultModel> call, Response<ResultModel> response) {
                if (response.isSuccessful()) {
                    ResultModel model = response.body();
                    //Utils.Log("model ==> : "+new Gson().toJson(model));
                    if (model != null) {
                        if (model.getFlag() == ResultModel.SUCCESS) {

                           /* mOneBtnPopup = new OneBtnPopup(getActivity(), "출고처리 되었습니다.", R.drawable.popup_title_alert, new Handler() {
                                @Override
                                public void handleMessage(Message msg) {
                                    if (msg.what == 1) {
                                        getActivity().finish();
                                        mOneBtnPopup.hideDialog();

                                    }
                                }
                            });*/
                            if (mDetailGetList != null) {
                                mDetailGetList.clear();
                            }
                            mListAdapter.notifyDataSetChanged();
                            mat_out_detail_get();

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
                    /*mTwoBtnPopup = new TwoBtnPopup(getActivity(), "이동 전송을 실패하였습니다.\n 재전송 하시겠습니까?", R.drawable.popup_title_alert, new Handler() {
                        @Override
                        public void handleMessage(Message msg) {
                            if (msg.what == 1) {
                                requestScan();
                                mTwoBtnPopup.hideDialog();
                            }
                        }
                    });*/
                }
            }

            @Override
            public void onFailure(Call<ResultModel> call, Throwable t) {
                Utils.LogLine(t.getMessage());
                /*mTwoBtnPopup = new TwoBtnPopup(getActivity(), "이동 전송을 실패하였습니다.\n 재전송 하시겠습니까?", R.drawable.popup_title_alert, new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        if (msg.what == 1) {
                            requestScan();
                            mTwoBtnPopup.hideDialog();
                        }
                    }
                });*/
            }
        });

    }

    /**
     * 스캔데이터 저장(품목13자리)
     */
    private void requestScanItem() {
        ApiClientService service = ApiClientService.retrofit.create(ApiClientService.class);
        JsonObject json = new JsonObject();
        String userID = (String) SharedData.getSharedData(mContext, SharedData.UserValue.USER_ID.name(), "");
        JsonArray list = new JsonArray();
        List<MatOutSerialScanModel.Item> items = scanAdapter.getData();

        JsonObject obj = new JsonObject();

        obj.addProperty("itm_code", mSerialModel.getItems().get(0).getItm_code());
        obj.addProperty("serial_no", barcode_scan);
        obj.addProperty("serial_qty", mSerialModel.getItems().get(0).getInv_qty());

        list.add(obj);

        json.addProperty("p_mac_ad", address);
        json.addProperty("p_corp_code", detailModel.getItems().get(0).getCorp_code());
        json.addProperty("p_req_mat_code", mOrder.getReq_mat_code());
        json.addProperty("p_user_id", userID);
        json.add("detail", list);

        Utils.Log("new Gson().toJson(json)scan ==> : " + new Gson().toJson(json));

        RequestBody body = RequestBody.create(MediaType.parse("application/json"), new Gson().toJson(json));

        Call<ResultModel> call = service.postOutSave(body);

        call.enqueue(new Callback<ResultModel>() {
            @Override
            public void onResponse(Call<ResultModel> call, Response<ResultModel> response) {
                if (response.isSuccessful()) {
                    ResultModel model = response.body();
                    //Utils.Log("model ==> : "+new Gson().toJson(model));
                    if (model != null) {
                        if (model.getFlag() == ResultModel.SUCCESS) {

                           /* mOneBtnPopup = new OneBtnPopup(getActivity(), "출고처리 되었습니다.", R.drawable.popup_title_alert, new Handler() {
                                @Override
                                public void handleMessage(Message msg) {
                                    if (msg.what == 1) {
                                        getActivity().finish();
                                        mOneBtnPopup.hideDialog();

                                    }
                                }
                            });*/
                            if (mDetailGetList != null) {
                                mDetailGetList.clear();
                            }
                            mListAdapter.notifyDataSetChanged();
                            mat_out_detail_get();

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
                    /*mTwoBtnPopup = new TwoBtnPopup(getActivity(), "이동 전송을 실패하였습니다.\n 재전송 하시겠습니까?", R.drawable.popup_title_alert, new Handler() {
                        @Override
                        public void handleMessage(Message msg) {
                            if (msg.what == 1) {
                                requestScan();
                                mTwoBtnPopup.hideDialog();
                            }
                        }
                    });*/
                }
            }

            @Override
            public void onFailure(Call<ResultModel> call, Throwable t) {
                Utils.LogLine(t.getMessage());
                /*mTwoBtnPopup = new TwoBtnPopup(getActivity(), "이동 전송을 실패하였습니다.\n 재전송 하시겠습니까?", R.drawable.popup_title_alert, new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        if (msg.what == 1) {
                            requestScan();
                            mTwoBtnPopup.hideDialog();
                        }
                    }
                });*/
            }
        });

    }


}//Colsoe Activity
