package kr.co.bang.wms.menu.stock_store;

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
import android.widget.ListView;
import android.widget.TextView;

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
import kr.co.bang.wms.menu.main.BaseActivity;
import kr.co.bang.wms.model.ResultModel;
import kr.co.bang.wms.model.StockModel;
import kr.co.bang.wms.network.ApiClientService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StockStoreFragment extends CommonFragment {

    Context mContext;
    TextView item_date;
    Button btn_search;
    DatePickerDialog.OnDateSetListener callbackMethod;
    ListView stock_listView_mst;
    List<StockModel.stockModel> mStockList;
    StockModel mStockmodel;
    ListAdapter mAdapter;
    Handler mHandler;

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
        btn_search = v.findViewById(R.id.btn_search);
        stock_listView_mst = v.findViewById(R.id.stock_listView_mst);
        mAdapter = new ListAdapter();
        mHandler = handler;
        stock_listView_mst.setAdapter(mAdapter);

        item_date.setOnClickListener(onClickListener);
        btn_search.setOnClickListener(onClickListener);

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


        return v;

    }//Close onCreateView

    private void StockDetail(int position){

        Intent intent = new Intent(getActivity(), BaseActivity.class);
        //intent.putExtra("menu", Define.MENU_STOCK_STORE_DATAIL);
        Bundle extras = new Bundle();
        extras.putSerializable("model", mStockmodel);
        extras.putSerializable("position", position);
        intent.putExtra("args", extras);

        startActivityForResult(intent, 100);
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

    View.OnClickListener onClickListener  = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.item_date :
                    int c_year = Integer.parseInt(item_date.getText().toString().substring(0, 4));
                    int c_month = Integer.parseInt(item_date.getText().toString().substring(5, 7));
                    int c_day = Integer.parseInt(item_date.getText().toString().substring(8, 10));

                    DatePickerDialog dialog = new DatePickerDialog(mContext, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT, callbackMethod, c_year, c_month - 1, c_day);
                    dialog.show();
                    break;

                case R.id.btn_search :
                    StockListSearch();
            }

        }
    };




    @Override
    public void onResume() {
        super.onResume();

    }//Close onResume


    /**
     * 재고조사 리스트 조회
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
                            if (mStockList != null){
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
               // holder.stk_remark = v.findViewById(R.id.stk_remark);

                v.setTag(holder);

            } else {
                holder = (ListAdapter.ViewHolder) v.getTag();
            }

            final StockModel.stockModel data = mStockList.get(position);
            holder.stk_date.setText(data.getStk_date());
            holder.stk_no1.setText(Integer.toString(data.getStk_no1()));
            holder.stk_wh_code.setText(data.getWh_name());
            //holder.stk_remark.setText(data.getRemark());


            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Message msg = mHandler.obtainMessage();
                    msg.what = 1;
                    msg.obj = data;
                    mHandler.sendMessage(msg);
                    StockDetail(position);
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
