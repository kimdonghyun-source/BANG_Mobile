package kr.co.bang.wms.menu.house_new_move;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import kr.co.bang.wms.R;
import kr.co.bang.wms.common.Define;
import kr.co.bang.wms.common.Utils;
import kr.co.bang.wms.custom.CommonFragment;
import kr.co.bang.wms.menu.main.BaseActivity;
import kr.co.bang.wms.menu.main.MainActivity;
import kr.co.bang.wms.model.MatOutDetailGet;
import kr.co.bang.wms.model.MatOutDetailModel;
import kr.co.bang.wms.model.MatOutListModel;
import kr.co.bang.wms.model.MatOutSerialScanModel;
import kr.co.bang.wms.model.ResultModel;
import kr.co.bang.wms.network.ApiClientService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HouseNewMoveScanDetailFragment extends CommonFragment {


    MatOutDetailModel matOutDetailModel;
    MatOutDetailModel.Item mOrder;
    int mPosition = -1;
    MatOutDetailGet matOutDetailGet;
    List<MatOutDetailGet.Item> mDetailGetList;
    ImageButton bt_next;
    ListAdapter mAdapter;
    String req_code;


    TextView tv_itm_name, tv_request_qty, tv_scan_qty;
    ListView inVenListView;

    String address;
    String mat_code;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();

    }//Close onCreate

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.frag_house_new_scan_detail, container, false);

        WifiManager manager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = manager.getConnectionInfo();
        address = info.getMacAddress();

        Bundle arguments = getArguments();
        if (arguments!=null) {
            mat_code = arguments.getString("mat_code");
        }

        req_code = arguments.getString("req_code");
        tv_itm_name = v.findViewById(R.id.tv_itm_name);
        tv_request_qty = v.findViewById(R.id.tv_request_qty);
        tv_scan_qty = v.findViewById(R.id.tv_scan_qty);
        inVenListView = v.findViewById(R.id.inVenListView);
        bt_next = v.findViewById(R.id.bt_next);
        mAdapter = new ListAdapter();
        inVenListView.setAdapter(mAdapter);

        matOutDetailModel = (MatOutDetailModel) arguments.getSerializable("model");
        mPosition = arguments.getInt("position");
        mOrder = matOutDetailModel.getItems().get(mPosition);

        tv_itm_name.setText(mOrder.getItm_name());
        tv_request_qty.setText(Integer.toString(mOrder.getReq_mat_qty()));
        tv_scan_qty.setText(Integer.toString(mOrder.getScan_qty()));

        bt_next.setOnClickListener(onClickListener);

        SerialScan();

        return v;
    }//Close onCreateView

    /**
     * 스캔내역조회(작업중인 내역)
     */
    private void SerialScan() {
        ApiClientService service = ApiClientService.retrofit.create(ApiClientService.class);

        Call<MatOutDetailGet> call = service.matDetailGet("sp_pda_mat_out_detail_get", address, req_code, mOrder.getItm_code());

        call.enqueue(new Callback<MatOutDetailGet>() {
            @Override
            public void onResponse(Call<MatOutDetailGet> call, Response<MatOutDetailGet> response) {
                if (response.isSuccessful()) {
                    matOutDetailGet = response.body();
                    final MatOutDetailGet model = response.body();
                    Utils.Log("model ==> :" + new Gson().toJson(model));
                    if (matOutDetailGet != null) {
                        if (matOutDetailGet.getFlag() == ResultModel.SUCCESS) {
                            //moveList = model.getItems();
                            if (model.getItems().size() > 0) {
                                for (int i = 0; i < model.getItems().size(); i++) {

                                    MatOutDetailGet.Item item = (MatOutDetailGet.Item) model.getItems().get(i);
                                    mAdapter.addData(item);
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
            public void onFailure(Call<MatOutDetailGet> call, Throwable t) {
                Utils.LogLine(t.getMessage());
                Utils.Toast(mContext, getString(R.string.error_network));
            }
        });
    }

    View.OnClickListener onClickListener  = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.bt_next :
                    /*Intent intent = new Intent(getActivity(), BaseActivity.class);
                    intent.putExtra("menu", Define.MENU_HOUSE_MOVE_SCAN_DATAIL);
                    startActivityForResult(intent, 100);*/
                    getActivity().finish();

                    break;

            }

        }
    };


    class ListAdapter extends BaseAdapter {
        LayoutInflater mInflater;

        public ListAdapter() {
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
            ListAdapter.ViewHolder holder;
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if (v == null) {
                holder = new ListAdapter.ViewHolder();
                v = inflater.inflate(R.layout.cell_scan_detail_list, null);

                holder.itm_name = v.findViewById(R.id.itm_name);
                holder.scan_qty = v.findViewById(R.id.scan_qty);

                v.setTag(holder);

            } else {
                holder = (ListAdapter.ViewHolder) v.getTag();
            }

            final MatOutDetailGet.Item data = mDetailGetList.get(position);
            holder.itm_name.setText(data.getItm_name());
            holder.scan_qty.setText(Integer.toString(data.getReq_mat_qty()));


            /*v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Message msg = mHandler.obtainMessage();
                    msg.what = 1;
                    msg.obj = data;
                    mHandler.sendMessage(msg);
                }
            });*/

            return v;
        }

        public class ViewHolder {
            TextView itm_name;
            TextView scan_qty;
        }


    }//Close Adapter



}//Close Activity
