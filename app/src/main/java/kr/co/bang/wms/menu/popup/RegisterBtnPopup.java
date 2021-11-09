package kr.co.bang.wms.menu.popup;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import kr.co.bang.wms.R;
import kr.co.bang.wms.common.Utils;
import kr.co.bang.wms.spinner.SpinnerPopupAdapter;

public class RegisterBtnPopup {

    Activity mActivity;

    Dialog dialog;
    Handler mHandler;
    Spinner mSpinner;
    int mSpinnerSelect = 0;

    /**
     * 버튼 하나짜리 팝업
     * @param activity
     * @param message 메세지
     * @param title 타이틀 이미지
     * @param handler return 핸들러
     */
    public RegisterBtnPopup(Activity activity, String message, int title, Handler handler){
        mActivity = activity;
        mHandler = handler;
        showPopUpDialog(activity, message, title);
    }

    public void hideDialog() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    public boolean isShowDialog(){
        if(dialog != null && dialog.isShowing()){
            return true;
        }else{
            return false;
        }
    }

    private void showPopUpDialog(Activity activity, String message, int title){

        dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        List<String> list = new ArrayList<String>();
        list.add("판매용");
        list.add("시타용");

        dialog.setContentView(R.layout.popup_register_message);

        Window window = dialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();

        //팝업을 맨 위로 올려야 함.
        wlp.gravity = Gravity.CENTER;
        window.setAttributes(wlp);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        ImageView iv_title = dialog.findViewById(R.id.iv_title);
        iv_title.setBackgroundResource(title);

        TextView tv_message = dialog.findViewById(R.id.tv_message);
        tv_message.setText(message);

        mSpinner = dialog.findViewById(R.id.spinner);
        SpinnerPopupAdapter spinnerAdapter = new SpinnerPopupAdapter(activity, list, mSpinner);
        mSpinner.setAdapter(spinnerAdapter);
        mSpinner.setOnItemSelectedListener(onItemSelectedListener);
        mSpinner.setSelection(mSpinnerSelect);

        dialog.findViewById(R.id.bt_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Message msg = mHandler.obtainMessage();
                msg.what = 1;
                //msg.obj = "확인";
                msg.obj = mSpinnerSelect;
                mHandler.sendMessage(msg);
            }
        });

        dialog.findViewById(R.id.bt_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideDialog();
            }
        });

        dialog.show();
    }

    AdapterView.OnItemSelectedListener onItemSelectedListener = new AdapterView.OnItemSelectedListener(){

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            //최초에 setOnItemSelectedListener 하면 이벤트가 들어오기 때문에
            //onResume에서 mSpinnerSelect에 현재 선택된 position을 넣고 여기서 비교
            if(mSpinnerSelect == position)return;

            mSpinnerSelect = position;

            String item = (String) mSpinner.getSelectedItem();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

}
