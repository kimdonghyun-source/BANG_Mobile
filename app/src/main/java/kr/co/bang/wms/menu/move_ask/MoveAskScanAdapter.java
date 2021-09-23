package kr.co.bang.wms.menu.move_ask;

import android.app.Activity;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import kr.co.bang.wms.R;
import kr.co.bang.wms.common.Utils;
import kr.co.bang.wms.model.MoveAskModel;

public class MoveAskScanAdapter extends RecyclerView.Adapter<MoveAskScanAdapter.ViewHolder> {

    List<MoveAskModel.Item> itemsList;
    Activity mActivity;
    Handler mHandler = null;

    public MoveAskScanAdapter(Activity context) {
        mActivity = context;
        itemsList = new ArrayList<>();
    }

    public void setData(List<MoveAskModel.Item> item){
        itemsList = item;
    }

    public void addData(MoveAskModel.Item item) {
        if (itemsList == null) itemsList = new ArrayList<>();
        itemsList.add(item);
    }

    public void clearData(){
        itemsList.clear();
    }

    public void setSumHandler(Handler h){
        this.mHandler = h;
    }

    public List<MoveAskModel.Item> getData(){
        return itemsList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int position) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cell_movescan_list, viewGroup, false);
        ViewHolder holder = new ViewHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        final MoveAskModel.Item item = itemsList.get(position);


        holder.itm_name.setText(item.getItm_name());
        holder.inv_qty_in.setText(Integer.toString(item.getInv_qty_in()));
        holder.inv_qty_out.setText(Integer.toString(item.getInv_qty_out()));
        holder.et_count.setText(Integer.toString(item.getInput_qty()));

        holder.et_count.addTextChangedListener(new TextWatcher(){
            String result="";

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                Log.d("JeLib","---------------------------");
                if(s.toString().length() > 0 && !s.toString().equals(result)) {     // StackOverflow를 막기위해,
                    result = s.toString();   // 에딧텍스트의 값을 변환하여, result에 저장.

                    int cnt = Utils.stringToInt(result);

                    holder.et_count.setText(result);    // 결과 텍스트 셋팅.
                    holder.et_count.setSelection(result.length());     // 커서를 제일 끝으로 보냄.

                    //입력된 수량을 list에 넣어줌
                    itemsList.get(holder.getAdapterPosition()).setInput_qty(cnt);
                }

            }
        });
    }

    @Override
    public int getItemCount() {
        return (null == itemsList ? 0 : itemsList.size());
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView itm_name;
        TextView inv_qty_in;
        TextView inv_qty_out;
        EditText et_count;

        public ViewHolder(View view) {
            super(view);

//            tv_lot = view.findViewById(R.id.tv_lot);
//            tv_count = view.findViewById(R.id.tv_count);
//            tv_loc = view.findViewById(R.id.tv_loc);
//            et_count = view.findViewById(R.id.et_count);


            itm_name = view.findViewById(R.id.itm_name);
            inv_qty_in = view.findViewById(R.id.tv_in_qty);
            inv_qty_out = view.findViewById(R.id.tv_out_qty);
            et_count = view.findViewById(R.id.et_count);

        }
    }
}
