package kr.co.bang.wms.menu.inventory;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.honeywell.aidc.BarcodeReadEvent;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import kr.co.bang.wms.R;

import kr.co.bang.wms.common.SharedData;
import kr.co.bang.wms.common.UtilDate;
import kr.co.bang.wms.common.Utils;
import kr.co.bang.wms.custom.CommonFragment;
import kr.co.bang.wms.honeywell.AidcReader;

import kr.co.bang.wms.menu.house_move.HouseMoveFragment;
import kr.co.bang.wms.menu.popup.OneBtnPopup;
import kr.co.bang.wms.menu.popup.TwoBtnPopup;
import kr.co.bang.wms.model.InvenModel;
import kr.co.bang.wms.model.InventoryModel;
import kr.co.bang.wms.model.MatMoveModel;
import kr.co.bang.wms.model.MaterialLocAndLotModel;
import kr.co.bang.wms.model.ResultModel;
import kr.co.bang.wms.network.ApiClientService;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InventoryFragment extends CommonFragment {
    Context mContext;
    EditText et_from;
    TextView tv_box_serial, tv_box_qty, tv_empty, tv_box_check_qty;

    ImageButton bt_next;
    String barcodeScan, beg_barcode = null;

    InvenModel minvenModel;
    List<InvenModel.Inven> invenList;

    ListAdapter mAdapter;
    ListView inVenListView;

    OneBtnPopup mOneBtnPopup;
    TwoBtnPopup mTwoBtnPopup;

    String wh_code;

    int c_count = 0;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getActivity();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.frag_inventory, container, false);
        et_from = v.findViewById(R.id.et_from);
        tv_box_serial = v.findViewById(R.id.tv_box_serial);
        tv_box_qty = v.findViewById(R.id.tv_box_qty);
        tv_empty = v.findViewById(R.id.tv_empty);
        inVenListView = v.findViewById(R.id.inVenListView);
        bt_next = v.findViewById(R.id.bt_next);
        tv_box_check_qty = v.findViewById(R.id.tv_box_check_qty);

        mAdapter = new ListAdapter();
        inVenListView.setAdapter(mAdapter);

        bt_next.setOnClickListener(onClickListener);

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        AidcReader.getInstance().claim(mContext);
        AidcReader.getInstance().setListenerHandler(new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 1) {
                    tv_empty.setVisibility(View.GONE);
                    BarcodeReadEvent event = (BarcodeReadEvent) msg.obj;
                    String barcode = event.getBarcodeData();
                    et_from.setText(barcode);
                    barcodeScan = barcode;

                    if (beg_barcode != null){
                        if (beg_barcode.equals(barcodeScan)) {
                            Utils.Toast(mContext, "동일한 바코드를 스캔하였습니다.");
                            return;
                        }
                    }

                    if (invenList != null){
                        if (invenList.get(0).getSerial_no().equals(barcodeScan)) {
                            Utils.Toast(mContext, "동일한 바코드를 스캔하였습니다.");
                            return;
                        }
                    }

                    if (barcode.length() == 17) {
                        pdaSerialScan();
                        beg_barcode = barcodeScan;
                    } else {
                        mAdapter.notifyDataSetChanged();
                    }


                }
            }
        });

    }

    @Override
    public void onPause() {
        super.onPause();
        AidcReader.getInstance().release();
        AidcReader.getInstance().setListenerHandler(null);
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
            int view = v.getId();

            switch (view) {
                case R.id.bt_next: {

                    if (mAdapter.getCount() <= 0) {
                        Utils.Toast(mContext, getString(R.string.error_mat_mod_scan));
                        return;
                    }

                    mTwoBtnPopup = new TwoBtnPopup(getActivity(), "실사처리 하시겠습니까?", R.drawable.popup_title_alert, new Handler() {
                        @Override
                        public void handleMessage(Message msg) {
                            if (msg.what == 1) {
                                matModSave();
                                mTwoBtnPopup.hideDialog();
                            }
                        }
                    });

                    break;
                }
            }
        }
    };

    /**
     * 재고실사(박스)
     */
    private void pdaSerialScan() {
        ApiClientService service = ApiClientService.retrofit.create(ApiClientService.class);

        Call<InvenModel> call = service.inventoryBox("sp_pda_mat_mod_serial_scan", barcodeScan);

        call.enqueue(new Callback<InvenModel>() {
            @Override
            public void onResponse(Call<InvenModel> call, Response<InvenModel> response) {
                if (response.isSuccessful()) {
                    minvenModel = response.body();
                    final InvenModel model = response.body();
                    Utils.Log("model ==> :" + new Gson().toJson(model));
                    if (minvenModel != null) {
                        if (minvenModel.getFlag() == ResultModel.SUCCESS) {
                            invenList = model.getItems();

                            if (model.getItems().size() > 0) {
                                wh_code = invenList.get(0).getWh_code();

                                //박스시리얼 17자리
                                //품목시리얼 13자리
                                //InvenModel.Inven item = (InvenModel.Inven) model.getItems().get(0);
                                float count = 0;
                                for (int i = 0; i < model.getItems().size(); i++) {
                                    InvenModel.Inven itm = (InvenModel.Inven) model.getItems().get(i);
                                    count += itm.getStk_qty();
                                }

                                if (barcodeScan.length() == 17) {

                                    tv_box_serial.setText(barcodeScan);
                                    tv_box_qty.setText(Utils.setComma(count));
                                }
                                mAdapter.notifyDataSetChanged();

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
            public void onFailure(Call<InvenModel> call, Throwable t) {
                Utils.LogLine(t.getMessage());
                Utils.Toast(mContext, getString(R.string.error_network));
            }
        });
    }



    class ListAdapter extends BaseAdapter {
        LayoutInflater mInflater;
        //List<DeliveryOrderModel.DeliveryOrder> itemsList;


        public ListAdapter() {
            mInflater = LayoutInflater.from(mContext);
        }

        public int getItemCount() {
            return (null == invenList ? 0 : invenList.size());
        }

        public void addData(InvenModel.Inven item) {
            if (invenList == null) invenList = new ArrayList<>();
            invenList.add(item);
        }

        public void clearData() {
            invenList.clear();
        }

        public List<InvenModel.Inven> getData() {
            return invenList;
        }

        @Override
        public int getCount() {
            if (invenList == null) {
                return 0;
            }
            return invenList.size();
        }


        @Override
        public InvenModel.Inven getItem(int position) {
            return invenList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {


            View v = convertView;
            ViewHolder holder;

            if (v == null) {
                holder = new ViewHolder();
                v = mInflater.inflate(R.layout.cell_inven, null);

                holder.tv_itm_name = v.findViewById(R.id.tv_itm_name);
                holder.serial = v.findViewById(R.id.serial);
                holder.wh_name = v.findViewById(R.id.wh_name);
                holder.stk_qty = v.findViewById(R.id.stk_qty);
                holder.check_qty = v.findViewById(R.id.check_qty);
                holder.wh_code = v.findViewById(R.id.wh_code);
                holder.picking = v.findViewById(R.id.picking_yn);
                v.setTag(holder);
            } else {
                holder = (ViewHolder) v.getTag();
            }

            final InvenModel.Inven data = invenList.get(position);

            holder.serial.setText(data.getSerial_no());
            holder.tv_itm_name.setText(data.getItm_name());
            holder.wh_name.setText(data.getWh_name());
            holder.stk_qty.setText(Integer.toString(data.getStk_qty()));
            holder.wh_code.setText(data.getWh_code());
            holder.picking.setText(data.getPicking_yn());


            //일치하면 리스트 색 변경
            int qty = 0;
            for (int i = 0; i < mAdapter.getData().size(); i++) {
                if (invenList.get(i).getSerial_no() == null) {
                } else {
                    if (invenList.get(i).getSerial_no().equals(barcodeScan)) {

                        if (position == i) {
                            qty++;
                            if (holder.check_qty.getText().equals("1")) {

                            } else {
                                c_count++;
                            }

                            int bcolor = getResources().getColor(R.color.yellow);
                            convertView.setBackgroundColor(bcolor);
                            holder.check_qty.setText(String.valueOf(qty));
                            tv_box_check_qty.setText(Utils.setComma(c_count));

                            invenList.get(position).setPicking_yn("Y");

                            if (tv_box_qty.getText().toString().equals(tv_box_check_qty.getText().toString())) {
                                mTwoBtnPopup = new TwoBtnPopup(getActivity(), "실사처리 하시겠습니까?", R.drawable.popup_title_alert, new Handler() {
                                    @Override
                                    public void handleMessage(Message msg) {
                                        if (msg.what == 1) {
                                            matModSave();
                                            mTwoBtnPopup.hideDialog();
                                        }
                                    }
                                });

                            }

                        }

                    }
                }
            }


            return v;
        }

        public class ViewHolder {

            TextView serial;
            TextView itm_code;
            TextView tv_itm_name;
            TextView wh_code;
            TextView wh_name;
            TextView stk_qty;
            TextView check_qty;
            TextView picking;
        }


    }

    /**
     * 재고실사저장
     */
    private void matModSave() {


        ApiClientService service = ApiClientService.retrofit.create(ApiClientService.class);
        JsonObject json = new JsonObject();
        String userID = (String) SharedData.getSharedData(mContext, SharedData.UserValue.USER_ID.name(), "");
        JsonArray list = new JsonArray();
        List<InvenModel.Inven> items = mAdapter.getData();

        for (InvenModel.Inven item : items) {
            JsonObject obj = new JsonObject();

            if (item.getPicking_yn().equals("N")) {
                obj.addProperty("serial_no", item.getSerial_no());
                obj.addProperty("itm_code", item.getItm_code());
                obj.addProperty("stk_qty", item.getStk_qty());
                list.add(obj);
            }

        }

        json.addProperty("p_wh_code", wh_code);
        json.addProperty("p_user_id", userID);
        json.add("detail", list);

        Utils.Log("new Gson().toJson(json) ==> : " + new Gson().toJson(json));

        RequestBody body = RequestBody.create(MediaType.parse("application/json"), new Gson().toJson(json));

        Call<InvenModel> call = service.matModSave(body);

        call.enqueue(new Callback<InvenModel>() {
            @Override
            public void onResponse(Call<InvenModel> call, Response<InvenModel> response) {
                if (response.isSuccessful()) {
                    InvenModel model = response.body();
                    //Utils.Log("model ==> : "+new Gson().toJson(model));
                    if (model != null) {
                        if (model.getFlag() == InvenModel.SUCCESS) {
                            mOneBtnPopup = new OneBtnPopup(getActivity(), "실사처리 되었습니다.", R.drawable.popup_title_alert, new Handler() {
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
                    mTwoBtnPopup = new TwoBtnPopup(getActivity(), "이동 전송을 실패하였습니다.\n 재전송 하시겠습니까?", R.drawable.popup_title_alert, new Handler() {
                        @Override
                        public void handleMessage(Message msg) {
                            if (msg.what == 1) {
                                matModSave();
                                mTwoBtnPopup.hideDialog();
                            }
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<InvenModel> call, Throwable t) {
                Utils.LogLine(t.getMessage());
                mTwoBtnPopup = new TwoBtnPopup(getActivity(), "이동 전송을 실패하였습니다.\n 재전송 하시겠습니까?", R.drawable.popup_title_alert, new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        if (msg.what == 1) {
                            matModSave();
                            mTwoBtnPopup.hideDialog();
                        }
                    }
                });
            }
        });

    }


}//close Class
