package kr.co.bang.wms.menu.stock;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import kr.co.bang.wms.R;
import kr.co.bang.wms.common.Define;
import kr.co.bang.wms.common.Utils;
import kr.co.bang.wms.custom.CommonFragment;
import kr.co.bang.wms.menu.house_new_move.HouseNewMoveDetailFragment;
import kr.co.bang.wms.menu.inventory.InventoryFragment;
import kr.co.bang.wms.menu.main.BaseActivity;
import kr.co.bang.wms.model.MatOutDetailModel;
import kr.co.bang.wms.model.MatOutListModel;
import kr.co.bang.wms.model.MatOutSerialScanModel;
import kr.co.bang.wms.model.ResultModel;
import kr.co.bang.wms.model.StockModel;
import kr.co.bang.wms.model.StockStoreModel;
import kr.co.bang.wms.network.ApiClientService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StockFragment extends CommonFragment {

    Context mContext;
    TextView item_date;
    Button btn_ware_search, btn_ware_search1;
    DatePickerDialog.OnDateSetListener callbackMethod;
    ListView stock_listView_mst, stock_listView_mst1;
    List<StockModel.stockModel> mStockList;
    List<StockStoreModel.Item> mStockStoreList;
    StockModel mStockmodel;
    StockStoreModel mStockStoremodel;
    ListAdapter mAdapter;
    ListAdapter1 mAdapter1;
    Handler mHandler;
    ImageButton bt_wh, bt_store;
    String gubun;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();

    }//Close onCreate


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.frag_stock_mst, container, false);

        item_date = v.findViewById(R.id.item_date);
        stock_listView_mst = v.findViewById(R.id.stock_listView_mst);
        stock_listView_mst1 = v.findViewById(R.id.stock_listView_mst1);
        btn_ware_search = v.findViewById(R.id.btn_ware_search);
        btn_ware_search1 = v.findViewById(R.id.btn_ware_search1);
        bt_store = v.findViewById(R.id.bt_store);
        bt_wh = v.findViewById(R.id.bt_wh);
        mHandler = handler;

        mAdapter = new ListAdapter();
        stock_listView_mst.setAdapter(mAdapter);

        mAdapter1 = new ListAdapter1();
        stock_listView_mst1.setAdapter(mAdapter1);

        item_date.setOnClickListener(onClickListener);
        btn_ware_search.setOnClickListener(onClickListener);
        btn_ware_search1.setOnClickListener(onClickListener);
        bt_wh.setOnClickListener(onClickListener);
        bt_store.setOnClickListener(onClickListener);

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

        item_date.setText(year1 + "-" + formattedMonth + "-" + formattedDayOfMonth);

        this.InitializeListener();

        setTab(1);

        return v;

    }//Close onCreateView


    private void StockDetail(int position, String type) {

        if (type.equals("W")){
            Intent intent = new Intent(getActivity(), BaseActivity.class);
            intent.putExtra("menu", Define.MENU_STOCK_DETAIL);
            Bundle extras = new Bundle();
            extras.putSerializable("model", mStockmodel);
            extras.putSerializable("position", position);
            intent.putExtra("args", extras);

            startActivityForResult(intent, 100);
        }else{
            Intent intent = new Intent(getActivity(), BaseActivity.class);
            intent.putExtra("menu", Define.MENU_STOCK_STORE_DATAIL);
            Bundle extras = new Bundle();
            extras.putSerializable("model", mStockStoremodel);
            extras.putSerializable("position", position);
            intent.putExtra("args", extras);

            startActivityForResult(intent, 100);
        }

    }

    private void setTab(int idx) {
        if (idx == 1) {
            bt_wh.setSelected(true);
            bt_store.setSelected(false);
            btn_ware_search.setVisibility(View.VISIBLE);
            btn_ware_search1.setVisibility(View.GONE);
            stock_listView_mst.setVisibility(View.VISIBLE);
            stock_listView_mst1.setVisibility(View.GONE);


        } else if (idx == 2) {
            bt_wh.setSelected(false);
            bt_store.setSelected(true);
            btn_ware_search.setVisibility(View.GONE);
            btn_ware_search1.setVisibility(View.VISIBLE);
            stock_listView_mst.setVisibility(View.GONE);
            stock_listView_mst1.setVisibility(View.VISIBLE);


        }
    }

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

                item_date.setText(year + "-" + formattedMonth + "-" + formattedDayOfMonth);

            }
        };
    }

    Date currentTime = Calendar.getInstance().getTime();
    SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy", Locale.getDefault());
    SimpleDateFormat dayFormat = new SimpleDateFormat("dd", Locale.getDefault());
    SimpleDateFormat monthFormat = new SimpleDateFormat("MM", Locale.getDefault());

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.item_date:
                    int c_year = Integer.parseInt(item_date.getText().toString().substring(0, 4));
                    int c_month = Integer.parseInt(item_date.getText().toString().substring(5, 7));
                    int c_day = Integer.parseInt(item_date.getText().toString().substring(8, 10));

                    DatePickerDialog dialog = new DatePickerDialog(mContext, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT, callbackMethod, c_year, c_month - 1, c_day);
                    dialog.show();
                    break;

                case R.id.btn_ware_search:
                    StockListSearch();
                    break;

                case R.id.btn_ware_search1:
                    StockListSearch1();
                    break;

                case R.id.bt_wh:
                        setTab(1);
                    break;

                case R.id.bt_store:
                    setTab(2);
                    break;

            }
        }
    };


    @Override
    public void onResume() {
        super.onResume();

    }//Close onResume


    /**
     * 재고조사 리스트 조회(창고)
     */
    private void StockListSearch() {
        ApiClientService service = ApiClientService.retrofit.create(ApiClientService.class);
        String m_date = item_date.getText().toString().replace("-", "");
        Call<StockModel> call = service.stklist("sp_pda_stk_list", m_date);

        call.enqueue(new Callback<StockModel>() {
            @Override
            public void onResponse(Call<StockModel> call, Response<StockModel> response) {
                if (response.isSuccessful()) {
                    mStockmodel = response.body();
                    final StockModel model = response.body();
                    Utils.Log("model ==> :" + new Gson().toJson(model));
                    if (mStockmodel != null) {
                        if (mStockmodel.getFlag() == ResultModel.SUCCESS) {

                            if (model.getItems().size() > 0) {
                                //for (int i = 0; i < model.getItems().size(); i++) {
                                //MatOutListModel.Item item = (MatOutListModel.Item) model.getItems().get(i);
                                //mAdapter.addData(item);
                                mStockList = model.getItems();
                                mAdapter.notifyDataSetChanged();
                                //}
                            }

                        } else {
                            Utils.Toast(mContext, model.getMSG());
                            if (mStockList != null) {
                                mStockList.clear();
                                mAdapter.notifyDataSetChanged();
                            }
                        }
                    }
                } else {
                    Utils.LogLine(response.message());
                    Utils.Toast(mContext, response.code() + " : " + response.message());
                }
            }


            @Override
            public void onFailure(Call<StockModel> call, Throwable t) {
                Utils.LogLine(t.getMessage());
                Utils.Toast(mContext, getString(R.string.error_network));
            }
        });
    }

    /**
     * 재고조사 리스트 조회(대리점)
     */
    private void StockListSearch1() {
        ApiClientService service = ApiClientService.retrofit.create(ApiClientService.class);
        String m_date = item_date.getText().toString().replace("-", "");
        Call<StockStoreModel> call = service.stklist2("sp_pda_stk_list_c", m_date);

        call.enqueue(new Callback<StockStoreModel>() {
            @Override
            public void onResponse(Call<StockStoreModel> call, Response<StockStoreModel> response) {
                if (response.isSuccessful()) {
                    mStockStoremodel = response.body();
                    final StockStoreModel model = response.body();
                    Utils.Log("model ==> :" + new Gson().toJson(model));
                    if (mStockStoremodel != null) {
                        if (mStockStoremodel.getFlag() == ResultModel.SUCCESS) {

                            if (model.getItems().size() > 0) {
                                //for (int i = 0; i < model.getItems().size(); i++) {
                                //MatOutListModel.Item item = (MatOutListModel.Item) model.getItems().get(i);
                                //mAdapter.addData(item);
                                mStockStoreList = model.getItems();
                                mAdapter1.notifyDataSetChanged();
                                //}
                            }

                        } else {
                            Utils.Toast(mContext, model.getMSG());
                            if (mStockStoreList != null) {
                                mStockStoreList.clear();
                                mAdapter1.notifyDataSetChanged();
                            }
                        }
                    }
                } else {
                    Utils.LogLine(response.message());
                    Utils.Toast(mContext, response.code() + " : " + response.message());
                }
            }


            @Override
            public void onFailure(Call<StockStoreModel> call, Throwable t) {
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
            return (null == mStockList ? 0 : mStockList.size());
        }

        public void addData(StockModel.stockModel item) {
            if (mStockList == null) mStockList = new ArrayList<>();
            mStockList.add(item);
        }

        public void clearData() {
            mStockList.clear();
        }

        public List<StockModel.stockModel> getData() {
            return mStockList;
        }

        @Override
        public int getCount() {
            if (mStockList == null) {
                return 0;
            }

            return mStockList.size();
        }


        @Override
        public StockModel.stockModel getItem(int position) {
            return mStockList.get(position);
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
                v = inflater.inflate(R.layout.cell_stock_mst, null);

                holder.stk_date = v.findViewById(R.id.stk_date);
                holder.stk_no1 = v.findViewById(R.id.stk_no1);
                holder.stk_wh_code = v.findViewById(R.id.stk_wh_code);
                holder.stk_remark = v.findViewById(R.id.stk_remark);

                v.setTag(holder);

            } else {
                holder = (ListAdapter.ViewHolder) v.getTag();
            }

            final StockModel.stockModel data = mStockList.get(position);
            holder.stk_date.setText(data.getStk_date());
            holder.stk_no1.setText(Integer.toString(data.getStk_no1()));
            holder.stk_wh_code.setText(data.getWh_name());
            holder.stk_remark.setText(data.getRemark());

            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Message msg = mHandler.obtainMessage();
                    msg.what = 1;
                    msg.obj = data;
                    mHandler.sendMessage(msg);
                    StockDetail(position, data.getWh_type());
                }
            });


            return v;
        }

        public class ViewHolder {
            TextView stk_date;
            TextView stk_no1;
            TextView stk_wh_code;
            TextView stk_remark;


        }


    }//Close Adapter

    class ListAdapter1 extends BaseAdapter {
        LayoutInflater mInflater;

        public ListAdapter1() {
            mInflater = LayoutInflater.from(mContext);
        }

        public int getItemCount() {
            return (null == mStockStoreList ? 0 : mStockStoreList.size());
        }

        public void addData(StockStoreModel.Item item) {
            if (mStockStoreList == null) mStockStoreList = new ArrayList<>();
            mStockStoreList.add(item);
        }

        public void clearData() {
            mStockStoreList.clear();
        }

        public List<StockStoreModel.Item> getData() {
            return mStockStoreList;
        }

        @Override
        public int getCount() {
            if (mStockStoreList == null) {
                return 0;
            }

            return mStockStoreList.size();
        }


        @Override
        public StockStoreModel.Item getItem(int position) {
            return mStockStoreList.get(position);
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
                v = inflater.inflate(R.layout.cell_stock_mst, null);

                holder.stk_date = v.findViewById(R.id.stk_date);
                holder.stk_no1 = v.findViewById(R.id.stk_no1);
                holder.stk_wh_code = v.findViewById(R.id.stk_wh_code);
                holder.stk_remark = v.findViewById(R.id.stk_remark);

                v.setTag(holder);

            } else {
                holder = (ViewHolder) v.getTag();
            }

            final StockStoreModel.Item data = mStockStoreList.get(position);
            holder.stk_date.setText(data.getStk_date());
            holder.stk_no1.setText(Integer.toString(data.getStk_no1()));
            holder.stk_wh_code.setText(data.getWh_name());
            holder.stk_remark.setText(data.getRemark());

            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Message msg = mHandler.obtainMessage();
                    msg.what = 1;
                    msg.obj = data;
                    mHandler.sendMessage(msg);
                    StockDetail(position, data.getWh_type());
                }
            });


            return v;
        }

        public class ViewHolder {
            TextView stk_date;
            TextView stk_no1;
            TextView stk_wh_code;
            TextView stk_remark;


        }


    }//Close Adapter


}//Close Activity