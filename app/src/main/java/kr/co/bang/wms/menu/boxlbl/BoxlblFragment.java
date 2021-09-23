package kr.co.bang.wms.menu.boxlbl;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.honeywell.aidc.BarcodeReadEvent;

import java.util.ArrayList;
import java.util.List;

import kr.co.bang.wms.R;
import kr.co.bang.wms.common.SharedData;
import kr.co.bang.wms.common.Utils;
import kr.co.bang.wms.custom.CommonFragment;
import kr.co.bang.wms.honeywell.AidcReader;
import kr.co.bang.wms.menu.house_new_move.HouseNewMoveDetailFragment;
import kr.co.bang.wms.menu.main.MainActivity;
import kr.co.bang.wms.menu.popup.OneBtnPopup;
import kr.co.bang.wms.menu.popup.TwoBtnPopup;
import kr.co.bang.wms.model.BoxlblListModel;
import kr.co.bang.wms.model.MatOutDetailModel;
import kr.co.bang.wms.model.MatOutListModel;
import kr.co.bang.wms.model.MatOutSerialScanModel;
import kr.co.bang.wms.model.ResultBoxModel;
import kr.co.bang.wms.model.ResultModel;
import kr.co.bang.wms.network.ApiClientService;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BoxlblFragment extends CommonFragment {

    TextView tv_empty, et_from, tv_box_serial, tot_scan;
    String barcode_scan, beg_barcode = null;
    ListView inVenListView;
    Context mContext;
    BoxlblListModel mBoxModel;
    List<BoxlblListModel.Item> mBoxListModel;
    ListAdapter mAdapter;
    Handler mHandler;
    ImageButton bt_next;
    OneBtnPopup mOneBtnPopup;
    TwoBtnPopup mTwoBtnPopup;
    ResultBoxModel mResultBoxmodel;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();

    }//Close onCreate

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.frag_box_lbl_scan, container, false);

        tv_empty = v.findViewById(R.id.tv_empty);
        et_from = v.findViewById(R.id.et_from);
        tv_box_serial = v.findViewById(R.id.tv_box_serial);
        inVenListView = v.findViewById(R.id.inVenListView);
        bt_next = v.findViewById(R.id.bt_next);
        tot_scan = v.findViewById(R.id.tot_scan);
        mAdapter = new ListAdapter();
        inVenListView.setAdapter(mAdapter);
        mHandler = handler;


        bt_next.setOnClickListener(onClickListener);

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
                    barcode_scan = barcode;
                    et_from.setText(barcode);

                    if (beg_barcode != null) {
                        if (beg_barcode.equals(barcode_scan)) {
                            Utils.Toast(mContext, "동일한 바코드를 스캔하였습니다.");
                            return;
                        }
                    }

                    if (mBoxListModel != null) {

                        for (int i = 0; i < mBoxListModel.size(); i++) {

                            if (mBoxListModel.get(i).getLot_no().equals(barcode_scan)) {
                                Utils.Toast(mContext, "동일한 시리얼을 스캔하셨습니다.");
                                return;
                            }
                        }

                    }
                    BoxlblScan();
                }
            }
        });
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.bt_next:
                    if (mBoxModel != null) {
                        request_box_lbl_save();
                    }
                    break;

            }

        }
    };


    /**
     * 박스라벨패킹(시리얼스캔)
     */
    private void BoxlblScan() {
        ApiClientService service = ApiClientService.retrofit.create(ApiClientService.class);

        Call<BoxlblListModel> call = service.boxSerialScan("sp_pda_box_lbl_scan", barcode_scan);

        call.enqueue(new Callback<BoxlblListModel>() {
            @Override
            public void onResponse(Call<BoxlblListModel> call, Response<BoxlblListModel> response) {
                if (response.isSuccessful()) {
                    mBoxModel = response.body();
                    final BoxlblListModel model = response.body();
                    Utils.Log("model ==> :" + new Gson().toJson(model));
                    if (mBoxModel != null) {
                        if (mBoxModel.getFlag() == ResultModel.SUCCESS) {

                            if (mBoxListModel != null) {
                                for (int i = 0; i < mBoxListModel.size(); i++) {
                                    if (!mBoxListModel.get(i).getItm_code().equals(mBoxModel.getItems().get(0).getItm_code())) {
                                        Utils.Toast(mContext, "동일한 아이템을 스캔해주세요.");
                                        return;
                                    }
                                }
                            }

                            for (int i = 0; i < model.getItems().size(); i++) {
                                BoxlblListModel.Item item = (BoxlblListModel.Item) model.getItems().get(i);
                                mAdapter.addData(item);

                            }
                            //mBoxListModel = model.getItems();
                            mAdapter.notifyDataSetChanged();
                            inVenListView.setAdapter(mAdapter);
                            tot_scan.setText("스캔총수량: " + mAdapter.getCount());

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
            public void onFailure(Call<BoxlblListModel> call, Throwable t) {
                Utils.LogLine(t.getMessage());
                Utils.Toast(mContext, getString(R.string.error_network));
            }
        });
    }

    //스캔시 어댑터
    class ListAdapter extends BaseAdapter {
        LayoutInflater mInflater;

        public ListAdapter() {
            mInflater = LayoutInflater.from(mContext);
        }

        public int getItemCount() {
            return (null == mBoxListModel ? 0 : mBoxListModel.size());
        }

        public void addData(BoxlblListModel.Item item) {
            if (mBoxListModel == null) mBoxListModel = new ArrayList<>();
            mBoxListModel.add(item);
        }


        public void clearData() {
            mBoxListModel.clear();
        }

        public List<BoxlblListModel.Item> getData() {
            return mBoxListModel;
        }

        @Override
        public int getCount() {
            if (mBoxListModel == null) {
                return 0;
            }

            return mBoxListModel.size();
        }

        @Override
        public BoxlblListModel.Item getItem(int position) {
            return mBoxListModel.get(position);
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
                v = inflater.inflate(R.layout.cell_boxlbl_list, null);

                holder.itm_name = v.findViewById(R.id.itm_name);
                holder.serial_no = v.findViewById(R.id.serial_no);

                v.setTag(holder);

            } else {
                holder = (ViewHolder) v.getTag();
            }

            //final MatOutDetailModel.Item data = mDetailList.get(position);
            final BoxlblListModel.Item data = mBoxListModel.get(position);
            holder.itm_name.setText(data.getItm_name());
            holder.serial_no.setText(data.getLot_no());


            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Message msg = mHandler.obtainMessage();
                    msg.what = 1;
                    msg.obj = data;
                    mHandler.sendMessage(msg);
                }
            });


            return v;
        }

        public class ViewHolder {
            TextView itm_name;
            TextView serial_no;


        }
    }//Close ScanAdapter

    /**
     * 실사처리 저장
     */
    private void request_box_lbl_save() {
        ApiClientService service = ApiClientService.retrofit.create(ApiClientService.class);
        JsonObject json = new JsonObject();
        String userID = (String) SharedData.getSharedData(mContext, SharedData.UserValue.USER_ID.name(), "");
        JsonArray list = new JsonArray();
        List<BoxlblListModel.Item> items = mAdapter.getData();
        for (BoxlblListModel.Item item : items) {
            JsonObject obj = new JsonObject();
            obj.addProperty("itm_code", item.getItm_code());
            obj.addProperty("serial_no", item.getLot_no());
            obj.addProperty("inv_qty", item.getInv_qty());
            list.add(obj);
        }

        json.addProperty("p_corp_code", "100");        //사업장코드 100번 고정
        json.addProperty("p_user_id", userID);            //로그인ID
        json.add("detail", list);

        Utils.Log("new Gson().toJson(json) ==> : " + new Gson().toJson(json));

        RequestBody body = RequestBody.create(MediaType.parse("application/json"), new Gson().toJson(json));

        Call<ResultBoxModel> call = service.postBoxSave(body);

        call.enqueue(new Callback<ResultBoxModel>() {
            @Override
            public void onResponse(Call<ResultBoxModel> call, Response<ResultBoxModel> response) {
                if (response.isSuccessful()) {
                    final ResultBoxModel model = response.body();
                    if (model != null) {
                        if (model.getFlag() == ResultBoxModel.SUCCESS) {

                            mOneBtnPopup = new OneBtnPopup(getActivity(), "패킹완료 되었습니다." + " " + model.getBOX_SERIAL(), R.drawable.popup_title_alert, new Handler() {
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
                    mTwoBtnPopup = new TwoBtnPopup(getActivity(), "패킹처리를 실패하였습니다.\n 재전송 하시겠습니까?", R.drawable.popup_title_alert, new Handler() {
                        @Override
                        public void handleMessage(Message msg) {
                            if (msg.what == 1) {
                                request_box_lbl_save();
                                mTwoBtnPopup.hideDialog();
                            }
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<ResultBoxModel> call, Throwable t) {
                Utils.LogLine(t.getMessage());
                mTwoBtnPopup = new TwoBtnPopup(getActivity(), "패킹처리를 실패하였습니다.\n 재전송 하시겠습니까?", R.drawable.popup_title_alert, new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        if (msg.what == 1) {
                            request_box_lbl_save();
                            mTwoBtnPopup.hideDialog();
                        }
                    }
                });
            }
        });

    }


}//Close Activity
