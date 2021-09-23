package kr.co.bang.wms.menu.move_ask;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.honeywell.aidc.BarcodeReadEvent;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import kr.co.bang.wms.R;
import kr.co.bang.wms.common.SharedData;
import kr.co.bang.wms.common.Utils;
import kr.co.bang.wms.custom.CommonFragment;
import kr.co.bang.wms.honeywell.AidcReader;
import kr.co.bang.wms.menu.boxlbl.BoxlblFragment;
import kr.co.bang.wms.menu.popup.LocationListPopup;
import kr.co.bang.wms.menu.popup.OneBtnPopup;
import kr.co.bang.wms.menu.popup.TwoBtnPopup;
import kr.co.bang.wms.menu.stock.StockFragment;
import kr.co.bang.wms.model.BoxlblListModel;
import kr.co.bang.wms.model.MoveAskModel;
import kr.co.bang.wms.model.ResultModel;
import kr.co.bang.wms.model.StockDetailModel;
import kr.co.bang.wms.model.StockModel;
import kr.co.bang.wms.model.WarehouseModel;
import kr.co.bang.wms.network.ApiClientService;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MoveAskFragment extends CommonFragment {
    Context mContext;
    TextView et_date, tv_empty;
    EditText et_in_wh, et_out_wh;
    ImageButton bt_in_wh, bt_out_wh, bt_next, bt_date;
    DatePickerDialog.OnDateSetListener callbackMethod;
    String wh_code, barcode_scan, beg_barcode = null;
    RecyclerView moveask_listView;
    MoveAskScanAdapter mAdapter;
    OneBtnPopup mOneBtnPopup;
    TwoBtnPopup mTwoBtnPopup;

    LocationListPopup mLocationListPopup;
    WarehouseModel.Items WareLocation;
    WarehouseModel.Items WareOutLocation;
    List<WarehouseModel.Items> mWarehouseList;
    List<WarehouseModel.Items> mWareOuthouseList;
    MoveAskModel moveAskModel;
    List<MoveAskModel.Item> moveAskList;

    List<String> mBarcode;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getActivity();
        mBarcode = new ArrayList<>();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.frag_move_ask, container, false);

        et_date = v.findViewById(R.id.et_date);
        et_in_wh = v.findViewById(R.id.et_in_wh);
        et_out_wh = v.findViewById(R.id.et_out_wh);
        bt_in_wh = v.findViewById(R.id.bt_in_wh);
        bt_out_wh = v.findViewById(R.id.bt_out_wh);
        tv_empty = v.findViewById(R.id.tv_empty);
        bt_next = v.findViewById(R.id.bt_next);
        bt_date = v.findViewById(R.id.bt_date);
        moveask_listView = v.findViewById(R.id.moveask_listView);
        moveask_listView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        mAdapter = new MoveAskScanAdapter(getActivity());
        moveask_listView.setAdapter(mAdapter);

        int year1 = Integer.parseInt(yearFormat.format(currentTime));
        int month1 = Integer.parseInt(monthFormat.format(currentTime));
        int day1 = Integer.parseInt(dayFormat.format(currentTime));

        String formattedMonth = "" + month1;
        String formattedDayOfMonth = "" + day1;
        if (month1 < 10) {

            formattedMonth = "0" + month1;
        }
        if (day1 < 10) {
            formattedDayOfMonth = "0" + day1;
        }

        et_date.setText(year1 + "-" + formattedMonth + "-" + formattedDayOfMonth);

        this.InitializeListener();

        bt_in_wh.setOnClickListener(onClickListener);
        bt_out_wh.setOnClickListener(onClickListener);
        bt_date.setOnClickListener(onClickListener);
        bt_next.setOnClickListener(onClickListener);

        return v;
    }//onCreateView Close

    @Override
    public void onResume() {
        super.onResume();
        AidcReader.getInstance().claim(mContext);
        AidcReader.getInstance().setListenerHandler(new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 1) {
                    BarcodeReadEvent event = (BarcodeReadEvent) msg.obj;
                    barcode_scan = event.getBarcodeData();
                    tv_empty.setVisibility(View.GONE);

                    if (et_in_wh.getText().toString().equals("")) {
                        Utils.Toast(mContext, "입고처를 선택해주세요.");
                        return;
                    }

                    if (et_out_wh.getText().toString().equals("")) {
                        Utils.Toast(mContext, "출고처를 선택해주세요.");
                        return;
                    }

                    if (beg_barcode != null) {
                        if (beg_barcode.equals(barcode_scan)) {
                            Utils.Toast(mContext, "동일한 바코드를 스캔하였습니다.");
                            return;
                        }
                    }

                    if(mBarcode.contains(barcode_scan)){
                        Utils.Toast(mContext, "동일한 아이템코드를 스캔하셨습니다.");
                        return;
                    }

                    moveaskScan();
                    beg_barcode = barcode_scan;

                }
            }
        });
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.bt_in_wh:
                    requestInWhlist();
                    break;

                case R.id.bt_out_wh:
                    requestOutWhlist();
                    break;

                case R.id.et_date:
                    int c_year = Integer.parseInt(et_date.getText().toString().substring(0, 4));
                    int c_month = Integer.parseInt(et_date.getText().toString().substring(5, 7));
                    int c_day = Integer.parseInt(et_date.getText().toString().substring(8, 10));

                    DatePickerDialog dialog = new DatePickerDialog(mContext, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT, callbackMethod, c_year, c_month - 1, c_day);
                    dialog.show();
                    break;

                case R.id.bt_next:
                    if (et_in_wh.getText().toString().equals("")) {
                        Utils.Toast(mContext, "입고처를 선택해주세요.");
                        return;
                    }
                    if (et_out_wh.getText().toString().equals("")) {
                        Utils.Toast(mContext, "출고처를 선택해주세요.");
                        return;
                    }
                    if (moveAskModel == null) {
                        Utils.Toast(mContext, "이동처리 할 물품을 스캔해주세요.");
                        return;
                    }
                    move_scan_save();
            }

        }
    };

    public void InitializeListener() {
        callbackMethod = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd");

                int month = monthOfYear + 1;
                String formattedMonth = "" + month;
                String formattedDayOfMonth = "" + dayOfMonth;

                if (month < 10) {

                    formattedMonth = "0" + month;
                }
                if (dayOfMonth < 10) {

                    formattedDayOfMonth = "0" + dayOfMonth;
                }

                et_date.setText(year + "-" + formattedMonth + "-" + formattedDayOfMonth);

            }
        };
    }

    Date currentTime = Calendar.getInstance().getTime();
    SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy", Locale.getDefault());
    SimpleDateFormat dayFormat = new SimpleDateFormat("dd", Locale.getDefault());
    SimpleDateFormat monthFormat = new SimpleDateFormat("MM", Locale.getDefault());


    /**
     * 입고처 리스트
     */
    private void requestInWhlist() {
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
                                    et_in_wh.setText("[" + WareLocation.getWh_code() + "] " + WareLocation.getWh_name());
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
     * 출고처 리스트
     */
    private void requestOutWhlist() {
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
                                    WareOutLocation = item;
                                    et_out_wh.setText("[" + WareOutLocation.getWh_code() + "] " + WareOutLocation.getWh_name());
                                    //mAdapter.notifyDataSetChanged();
                                    wh_code = WareOutLocation.getWh_code();
                                    mLocationListPopup.hideDialog();
                                }
                            });
                            mWareOuthouseList = model.getItems();


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
     * 이동요청 시리얼 스캔
     */
    private void moveaskScan() {
        ApiClientService service = ApiClientService.retrofit.create(ApiClientService.class);
        String m_date = et_date.getText().toString().replace("-", "");
        Call<MoveAskModel> call = service.moveSerialScan("sp_pda_mreq_scan", m_date, barcode_scan, WareLocation.getWh_code(), WareOutLocation.getWh_code());

        call.enqueue(new Callback<MoveAskModel>() {
            @Override
            public void onResponse(Call<MoveAskModel> call, Response<MoveAskModel> response) {
                if (response.isSuccessful()) {
                    moveAskModel = response.body();
                    final MoveAskModel model = response.body();
                    Utils.Log("model ==> :" + new Gson().toJson(model));
                    if (moveAskModel != null) {
                        if (moveAskModel.getFlag() == ResultModel.SUCCESS) {
                            //moveAskList = moveAskModel.getItems();
                            if (model.getItems().size() > 0) {

                                for (int i = 0; i < model.getItems().size(); i++) {
                                    MoveAskModel.Item item = (MoveAskModel.Item) model.getItems().get(i);
                                    mAdapter.addData(item);


                                }

                                mAdapter.notifyDataSetChanged();
                                mBarcode.add(barcode_scan);

                            }

                        } else {
                            Utils.Toast(mContext, model.getMSG());
                            if (moveAskList != null) {
                                /*moveAskList.clear();
                                mAdapter.notifyDataSetChanged();*/
                            }
                        }
                    }
                } else {
                    Utils.LogLine(response.message());
                    Utils.Toast(mContext, response.code() + " : " + response.message());
                }
            }


            @Override
            public void onFailure(Call<MoveAskModel> call, Throwable t) {
                Utils.LogLine(t.getMessage());
                Utils.Toast(mContext, getString(R.string.error_network));
            }
        });
    }

    /**
     * 이동요청처리
     */
    private void move_scan_save() {
        String m_date = et_date.getText().toString().replace("-", "");
        ApiClientService service = ApiClientService.retrofit.create(ApiClientService.class);
        JsonObject json = new JsonObject();
        String userID = (String) SharedData.getSharedData(mContext, SharedData.UserValue.USER_ID.name(), "");
        JsonArray list = new JsonArray();
        List<MoveAskModel.Item> items = mAdapter.getData();
        String corp_code = (String) SharedData.getSharedData(mContext, "corp_code", "");

        for (MoveAskModel.Item item : items) {
            JsonObject obj = new JsonObject();

            if (item.getInput_qty() > item.getInv_qty_out()){
                Utils.Toast(mContext, "이동 수량이 초과되었습니다.");
                return;
            }else {

                if (item.getInput_qty() <= 0) {

                } else {
                    obj.addProperty("itm_code", item.getItm_code());
                    obj.addProperty("mreq_qty", item.getInput_qty());
                    list.add(obj);
                }
            }

        }

        json.addProperty("p_corp_code", "100");
        json.addProperty("p_mreq_date", m_date);
        json.addProperty("p_wh_code_in", WareLocation.getWh_code());
        json.addProperty("p_wh_code_out", WareOutLocation.getWh_code());
        json.addProperty("p_user_id", userID);
        json.add("detail", list);

        Utils.Log("new Gson().toJson(json) ==> : " + new Gson().toJson(json));

        RequestBody body = RequestBody.create(MediaType.parse("application/json"), new Gson().toJson(json));

        Call<MoveAskModel> call = service.MoveSave(body);

        call.enqueue(new Callback<MoveAskModel>() {
            @Override
            public void onResponse(Call<MoveAskModel> call, Response<MoveAskModel> response) {
                if (response.isSuccessful()) {
                    MoveAskModel model = response.body();
                    //Utils.Log("model ==> : "+new Gson().toJson(model));
                    if (model != null) {
                        if (model.getFlag() == MoveAskModel.SUCCESS) {
                            mOneBtnPopup = new OneBtnPopup(getActivity(), "이동 처리되었습니다.", R.drawable.popup_title_alert, new Handler() {
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
                    Utils.LogLine(response.message());
                    mTwoBtnPopup = new TwoBtnPopup(getActivity(), "전송을 실패하였습니다.\n 재전송 하시겠습니까?", R.drawable.popup_title_alert, new Handler() {
                        @Override
                        public void handleMessage(Message msg) {
                            if (msg.what == 1) {
                                move_scan_save();
                                mTwoBtnPopup.hideDialog();
                            }
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<MoveAskModel> call, Throwable t) {
                Utils.LogLine(t.getMessage());
                mTwoBtnPopup = new TwoBtnPopup(getActivity(), "전송을 실패하였습니다.\n 재전송 하시겠습니까?", R.drawable.popup_title_alert, new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        if (msg.what == 1) {
                            move_scan_save();
                            mTwoBtnPopup.hideDialog();
                        }
                    }
                });
            }
        });

    }




}//Close Fragemnet
