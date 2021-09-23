package kr.co.bang.wms.menu.stock_store;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

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
import kr.co.bang.wms.menu.popup.OneBtnPopup;
import kr.co.bang.wms.menu.popup.TwoBtnPopup;
import kr.co.bang.wms.menu.stock.StockFragment;
import kr.co.bang.wms.menu.stock.StockFragmentDetail;
import kr.co.bang.wms.model.ResultModel;
import kr.co.bang.wms.model.StockDetailModel;
import kr.co.bang.wms.model.StockModel;
import kr.co.bang.wms.model.StockStoreModel;
import kr.co.bang.wms.network.ApiClientService;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.internal.Util;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StockStoreDetailFragment extends CommonFragment {

    Context mContext;
    TextView tv_stk_wh_code, tv_empty, tv_store_wh, tv_wh_code, tv_g_name3, tv_result, tv_lot_no_mix;
    String barcode, beg_barcode;
    EditText et_from;
    StockStoreModel mStockmodel;
    int mPosition = -1;
    StockStoreModel.Item mOrder;
    StockDetailModel mStockDetailmodel;
    List<StockDetailModel.stockDetailModel> mStockDetailList;
    ListAdapter mAdapter;
    ListView stockDetail_listView;
    int count = 0;
    List<String> mBarcode;
    String mLocation;
    private SoundPool sound_pool;
    int soundId;
    MediaPlayer mediaPlayer;
    TwoBtnPopup mTwoBtnPopup;
    OneBtnPopup mOneBtnPopup;
    ImageButton bt_next;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
        mBarcode = new ArrayList<>();


    }//Close onCreate

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.frag_stock_store, container, false);

        tv_stk_wh_code = v.findViewById(R.id.tv_stk_wh_code);
        tv_empty = v.findViewById(R.id.tv_empty);
        et_from = v.findViewById(R.id.et_from);
        tv_store_wh = v.findViewById(R.id.tv_store_wh);
        tv_wh_code = v.findViewById(R.id.tv_wh_code);
        tv_g_name3 = v.findViewById(R.id.tv_g_name3);
        tv_result = v.findViewById(R.id.tv_result);
        tv_lot_no_mix = v.findViewById(R.id.tv_lot_no_mix);
        bt_next = v.findViewById(R.id.bt_next);

        bt_next.setOnClickListener(onClickListener);

        stockDetail_listView = v.findViewById(R.id.stockDetail_listView);
        mAdapter = new ListAdapter();
        stockDetail_listView.setAdapter(mAdapter);

        Bundle arguments = getArguments();

        mStockmodel = (StockStoreModel) arguments.getSerializable("model");
        mPosition = arguments.getInt("position");
        mOrder = mStockmodel.getItems().get(mPosition);

        tv_store_wh.setText(mOrder.getWh_name());

        sound_pool = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
        soundId = sound_pool.load(mContext, R.raw.beepum, 1);

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
                    barcode = event.getBarcodeData();
                    tv_empty.setVisibility(View.GONE);
                    et_from.setText(barcode);

                    if (beg_barcode != null) {
                        if (beg_barcode.equals(barcode)) {
                            Utils.Toast(mContext, "동일한 바코드를 스캔하였습니다.");
                            return;
                        }
                    }

                    if (mBarcode.contains(barcode)) {
                        Utils.Toast(mContext, "동일한 SerialNo를 스캔하셨습니다.");
                        return;
                    }

                    mLocation = barcode;
                    beg_barcode = barcode;
                    StockListSearch();


                }
            }
        });
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.bt_next:
                    bt_next.setEnabled(false);
                    if (mStockDetailList != null) {
                        bt_next.setEnabled(false);
                        stk_scan_save();
                    } else {
                        bt_next.setEnabled(true);
                        Utils.Toast(mContext, "스캔을 진행해주세요.");
                        return;
                    }
            }

        }
    };

    /**
     * 재고조사(대리점) 리스트 조회
     */
    private void StockListSearch() {
        ApiClientService service = ApiClientService.retrofit.create(ApiClientService.class);

        Call<StockDetailModel> call = service.stk_store_serial_list("sp_pda_stk_scan_c", barcode, mOrder.getStk_date(),
                mOrder.getWh_code(), String.valueOf(mOrder.getStk_no1()), mOrder.getInv_date());

        call.enqueue(new Callback<StockDetailModel>() {
            @Override
            public void onResponse(Call<StockDetailModel> call, Response<StockDetailModel> response) {
                if (response.isSuccessful()) {
                    mStockDetailmodel = response.body();
                    final StockDetailModel model = response.body();
                    Utils.Log("model ==> :" + new Gson().toJson(model));
                    if (mStockDetailmodel != null) {
                        if (mStockDetailmodel.getFlag() == ResultModel.SUCCESS) {

                            if (model.getItems().size() > 0) {

                                for (int i = 0; i < model.getItems().size(); i++) {
                                    StockDetailModel.stockDetailModel item = (StockDetailModel.stockDetailModel) model.getItems().get(i);
                                    mAdapter.addData(item);

                                }

                                mAdapter.notifyDataSetChanged();
                                mBarcode.add(mLocation);

                                tv_wh_code.setText(model.getItems().get(0).getWh_name());
                                tv_g_name3.setText(model.getItems().get(0).getG_name3());
                                tv_lot_no_mix.setText(model.getItems().get(0).getLot_no_mix());

                            }

                            if (mOrder.getWh_code().equals(model.getItems().get(0).getWh_code())) {
                                int m_color = ContextCompat.getColor(mContext, R.color.color_008998);
                                tv_result.setText("일치");
                                tv_result.setTextColor(m_color);
                            }else{
                                int m_color = ContextCompat.getColor(mContext, R.color.red);
                                tv_result.setText("불일치");
                                tv_result.setTextColor(m_color);
                                sound_pool.play(soundId, 1f, 1f, 0, 1, 1f);
                                mediaPlayer = MediaPlayer.create(mContext, R.raw.beepum);
                                mediaPlayer.start();
                            }
                        } else {
                            Utils.Toast(mContext, model.getMSG());

                            if (mStockDetailList != null) {
                                /*mStockDetailList.clear();
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
            public void onFailure(Call<StockDetailModel> call, Throwable t) {
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
            return (null == mStockDetailList ? 0 : mStockDetailList.size());
        }

        public void addData(StockDetailModel.stockDetailModel item) {
            if (mStockDetailList == null) mStockDetailList = new ArrayList<>();
            mStockDetailList.add(item);
        }

        public void clearData() {
            mStockDetailList.clear();
        }

        public List<StockDetailModel.stockDetailModel> getData() {
            return mStockDetailList;
        }

        @Override
        public int getCount() {
            if (mStockDetailList == null) {
                return 0;
            }

            return mStockDetailList.size();
        }


        @Override
        public StockDetailModel.stockDetailModel getItem(int position) {
            return mStockDetailList.get(position);
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
                v = inflater.inflate(R.layout.cell_stock_store_detail, null);

                holder.itm_name = v.findViewById(R.id.tv_itm_name);
                holder.lot_no = v.findViewById(R.id.tv_lot_no);
                //holder.wh_code = v.findViewById(R.id.tv_wh_code);
                holder.inv_qty = v.findViewById(R.id.tv_inv_qty);
                holder.bt_delete = v.findViewById(R.id.bt_delete);

                holder.bt_delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mBarcode.remove(mStockDetailList.get(position).getLot_no());
                        mStockDetailList.remove(position);
                        mAdapter.notifyDataSetChanged();

                        if (beg_barcode.equals(barcode)){
                            beg_barcode = "";
                        }



                    }
                });


                v.setTag(holder);

            } else {
                holder = (ViewHolder) v.getTag();
            }

            final StockDetailModel.stockDetailModel data = mStockDetailList.get(position);
            holder.itm_name.setText(data.getItm_name());
            holder.lot_no.setText(data.getLot_no());
            //holder.wh_code.setText(data.getWh_code());
            holder.inv_qty.setText(Integer.toString(data.getInv_qty()));



            /*v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Message msg = mHandler.obtainMessage();
                    msg.what = 1;
                    msg.obj = data;
                    mHandler.sendMessage(msg);
                    DetailScanList(position);
                }
            });*/


            return v;
        }

        public class ViewHolder {
            TextView itm_name;
            TextView lot_no;
            ImageButton bt_delete;
            TextView inv_qty;


        }


    }//Close Adapter


    /**
     * 재고실사저장
     */
    private void stk_scan_save() {

        ApiClientService service = ApiClientService.retrofit.create(ApiClientService.class);
        JsonObject json = new JsonObject();
        String userID = (String) SharedData.getSharedData(mContext, SharedData.UserValue.USER_ID.name(), "");
        JsonArray list = new JsonArray();
        List<StockDetailModel.stockDetailModel> items = mAdapter.getData();


        for (StockDetailModel.stockDetailModel item : items) {
            JsonObject obj = new JsonObject();

            obj.addProperty("itm_code", item.getItm_code());
            obj.addProperty("serial_no", item.getLot_no());
            obj.addProperty("wh_code", item.getWh_code());
            obj.addProperty("serial_qty", item.getInv_qty());
            list.add(obj);
        }

        json.addProperty("p_corp_code", mOrder.getCorp_code());
        json.addProperty("p_stk_date", mOrder.getStk_date());
        json.addProperty("p_stk_no1", mOrder.getStk_no1());
        json.addProperty("p_user_id", userID);
        json.add("detail", list);

        Utils.Log("new Gson().toJson(json) ==> : " + new Gson().toJson(json));

        RequestBody body = RequestBody.create(MediaType.parse("application/json"), new Gson().toJson(json));

        Call<StockDetailModel> call = service.stockSave(body);

        call.enqueue(new Callback<StockDetailModel>() {
            @Override
            public void onResponse(Call<StockDetailModel> call, Response<StockDetailModel> response) {

                if (response.isSuccessful()) {
                    StockDetailModel model = response.body();
                    //Utils.Log("model ==> : "+new Gson().toJson(model));
                    if (model != null) {
                        if (model.getFlag() == StockDetailModel.SUCCESS) {

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
                                        bt_next.setEnabled(true);
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
                                stk_scan_save();
                                mTwoBtnPopup.hideDialog();
                            }
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<StockDetailModel> call, Throwable t) {
                Utils.LogLine(t.getMessage());
                mTwoBtnPopup = new TwoBtnPopup(getActivity(), "전송을 실패하였습니다.\n 재전송 하시겠습니까?", R.drawable.popup_title_alert, new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        if (msg.what == 1) {
                            stk_scan_save();
                            mTwoBtnPopup.hideDialog();
                        }
                    }
                });
            }
        });

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        // MediaPlayer 해지
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }


}//Close Acitivity
