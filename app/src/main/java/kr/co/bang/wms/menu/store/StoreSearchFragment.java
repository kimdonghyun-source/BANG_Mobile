package kr.co.bang.wms.menu.store;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import kr.co.bang.wms.R;
import kr.co.bang.wms.common.Define;
import kr.co.bang.wms.common.Utils;
import kr.co.bang.wms.custom.CommonFragment;
import kr.co.bang.wms.menu.main.BaseActivity;
import kr.co.bang.wms.menu.popup.CstListPopup;
import kr.co.bang.wms.model.PopupCstModel;
import kr.co.bang.wms.model.ResultModel;
import kr.co.bang.wms.model.StoreCstListModel;
import kr.co.bang.wms.network.ApiClientService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.INPUT_METHOD_SERVICE;

public class StoreSearchFragment extends CommonFragment{

    Context mContext;
    ImageButton bt_cst, bt_store;
    EditText et_cst, et_store;
    RecyclerView search_listView;

    String cst_code;

    PopupCstModel.Item PopupCstModel;
    List<PopupCstModel.Item> PopupCstList;
    CstListPopup mCstListPopup;

    StoreCstListModel mCstModel;
    List<StoreCstListModel.Item> mCstList;

    CstListAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();

    }//Close onCreate

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.frag_store_search, container, false);

        bt_cst = v.findViewById(R.id.bt_cst);
        bt_store = v.findViewById(R.id.bt_store);
        et_cst = v.findViewById(R.id.et_cst);
        et_store = v.findViewById(R.id.et_store);
        search_listView = v.findViewById(R.id.search_listView);

        search_listView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        mAdapter = new CstListAdapter(getActivity());
        search_listView.setAdapter(mAdapter);

        bt_cst.setOnClickListener(onClickListener);
        bt_store.setOnClickListener(onClickListener);

        mAdapter.setRetHandler(new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what >= 0) {
                    CstList(msg.what);
                }
            }
        });


        return v;

    }//Close onCreateView

    private void CstList(int position) {
        List<StoreCstListModel.Item> datas = new ArrayList<>();

        Intent intent = new Intent(getActivity(), BaseActivity.class);
        intent.putExtra("menu", Define.MENU_STORE_SEARCH_DETAIL);

        Bundle extras = new Bundle();
        extras.putSerializable("model", mCstModel);
        extras.putSerializable("position", position);
        intent.putExtra("args", extras);
        startActivityForResult(intent, 100);
    }


    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.bt_cst:
                    requestCstList();
                    break;

                case R.id.bt_store:
                    InputMethodManager imm = (InputMethodManager) mContext.getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(bt_store.getWindowToken(), 0);
                    StoreSearchList();
                    break;

            }

        }
    };



    /**
     * 거래처분류 리스트
     */
    private void requestCstList() {
        ApiClientService service = ApiClientService.retrofit.create(ApiClientService.class);

        Call<PopupCstModel> call = service.popupCstList("sp_pda_cst_type", "");

        call.enqueue(new Callback<PopupCstModel>() {
            @Override
            public void onResponse(Call<PopupCstModel> call, Response<PopupCstModel> response) {
                if (response.isSuccessful()) {
                    PopupCstModel model = response.body();
                    //Utils.Log("model ==> :" + new Gson().toJson(model));
                    if (model != null) {
                        if (model.getFlag() == ResultModel.SUCCESS) {
                            mCstListPopup = new CstListPopup(getActivity(), model.getItems(), R.drawable.popup_title_searchloc, new Handler() {
                                @Override
                                public void handleMessage(Message msg) {
                                    PopupCstModel.Item item = (PopupCstModel.Item) msg.obj;
                                    PopupCstModel = item;
                                    //et_cst.setText("[" + PopupCstModel.getC_code() + "] " + PopupCstModel.getC_name());
                                    et_cst.setText(PopupCstModel.getC_name());
                                    //mAdapter.notifyDataSetChanged();
                                    cst_code = PopupCstModel.getC_code();
                                    mCstListPopup.hideDialog();
                                    if (cst_code != null) {
                                        StoreSearchList();
                                    }
                                }
                            });
                            PopupCstList = model.getItems();



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
            public void onFailure(Call<PopupCstModel> call, Throwable t) {
                Utils.LogLine(t.getMessage());
                Utils.Toast(mContext, getString(R.string.error_network));
            }
        });
    }

    /**
     * 대리점재고조사 리스트
     */
    private void StoreSearchList() {
        ApiClientService service = ApiClientService.retrofit.create(ApiClientService.class);

        Call<StoreCstListModel> call = service.StoreCstList("sp_pda_cst_list", cst_code, et_store.getText().toString());

        call.enqueue(new Callback<StoreCstListModel>() {
            @Override
            public void onResponse(Call<StoreCstListModel> call, Response<StoreCstListModel> response) {
                if (response.isSuccessful()) {
                    mCstModel = response.body();
                    final StoreCstListModel model = response.body();
                    Utils.Log("model ==> :" + new Gson().toJson(model));
                    if (mCstModel != null) {
                        if (mCstModel.getFlag() == ResultModel.SUCCESS) {
                            float c_cnt = 0;
                            if (model.getItems().size() > 0) {
                                mAdapter.clearData();
                                mCstList = model.getItems();
                                for (int i = 0; i < model.getItems().size(); i++) {
                                    StoreCstListModel.Item item = (StoreCstListModel.Item) model.getItems().get(i);
                                    mAdapter.addData(item);
                                }
                            }
                            mAdapter.notifyDataSetChanged();

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
            public void onFailure(Call<StoreCstListModel> call, Throwable t) {
                Utils.LogLine(t.getMessage());
                Utils.Toast(mContext, getString(R.string.error_network));
            }
        });
    }//Close

    public class CstListAdapter extends RecyclerView.Adapter<CstListAdapter.ViewHolder> {

        List<StoreCstListModel.Item> itemsList;
        Activity mActivity;
        Handler mHandler = null;

        public CstListAdapter(Activity context) {
            mActivity = context;
        }

        public void setData(List<StoreCstListModel.Item> list) {
            itemsList = list;
        }

        public void clearData() {
            if (itemsList != null) itemsList.clear();
        }

        public void setRetHandler(Handler h) {
            this.mHandler = h;
        }

        public List<StoreCstListModel.Item> getData() {
            return itemsList;
        }

        public void addData(StoreCstListModel.Item item) {
            if (itemsList == null) itemsList = new ArrayList<>();
            itemsList.add(item);
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int position) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cell_cst_list, viewGroup, false);
            ViewHolder holder = new ViewHolder(v);
            return holder;
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {

            final StoreCstListModel.Item item = itemsList.get(position);

            holder.tv_cst_name.setText(item.getCst_name());
            holder.tv_cst_call.setText(item.getTel_no());

        }

        @Override
        public int getItemCount() {
            return (null == itemsList ? 0 : itemsList.size());
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            TextView tv_cst_name;
            TextView tv_cst_call;

            public ViewHolder(View view) {
                super(view);

                tv_cst_name = view.findViewById(R.id.tv_cst_name);
                tv_cst_call = view.findViewById(R.id.tv_cst_call);

                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Message msg = new Message();
                        msg.obj = itemsList.get(getAdapterPosition());
                        msg.what = getAdapterPosition();
                        mHandler.sendMessage(msg);
                    }
                });
            }
        }
    }




}//Close Fragment
