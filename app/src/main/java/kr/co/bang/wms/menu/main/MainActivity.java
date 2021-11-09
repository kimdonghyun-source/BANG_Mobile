package kr.co.bang.wms.menu.main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;

import kr.co.bang.wms.R;
import kr.co.bang.wms.common.Define;
import kr.co.bang.wms.custom.CommonCompatActivity;
import kr.co.bang.wms.menu.login.LoginActivity;
import kr.co.bang.wms.menu.popup.TwoBtnPopup;

public class MainActivity extends CommonCompatActivity {
    Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = this;

        setContentView(R.layout.act_main);


        findViewById(R.id.bt_menu_4).setOnClickListener(onClickListener); //주문자재출고
        findViewById(R.id.bt_menu_11).setOnClickListener(onClickListener); //이동요청
        findViewById(R.id.bt_menu_5).setOnClickListener(onClickListener); //기존창고이동
        findViewById(R.id.bt_menu_6).setOnClickListener(onClickListener); //새로운창고이동
        findViewById(R.id.bt_menu_8).setOnClickListener(onClickListener); //재고실사
        findViewById(R.id.bt_menu_2).setOnClickListener(onClickListener); //박스라벨패킹
        findViewById(R.id.bt_menu_10).setOnClickListener(onClickListener); //재고조사
        findViewById(R.id.bt_menu_13).setOnClickListener(onClickListener); //시리얼위치조회
        findViewById(R.id.bt_menu_14).setOnClickListener(onClickListener); //재고조사(대리점)
        findViewById(R.id.bt_logout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new TwoBtnPopup(MainActivity.this, "로그아웃 하시겠습니까?", R.drawable.popup_title_alert, new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        if (msg.what == 1) {
                            goLogin();
                        }
                    }
                });
            }
        });
    }

    private void goLogin(){
        Intent i = new Intent(mContext,LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
    }
    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int view = v.getId();
            Intent intent = new Intent(mContext, BaseActivity.class);
            switch (view){

                //주문자재출고
                case R.id.bt_menu_4:
                    intent.putExtra("menu", Define.MENU_PRODUCTION_IN);
                    break;

                //이동요청
                case R.id.bt_menu_11:
                    intent.putExtra("menu", Define.MENU_MOVE_ASK);
                    break;
                 //기존 창고이동
                /*case R.id.bt_menu_5:
                    intent.putExtra("menu", Define.MENU_HOUSE_MOVE);
                    break;*/
                //새로운 창고이동
                case R.id.bt_menu_6:
                    intent.putExtra("menu", Define.MENU_HOUSE_MOVE_NEW);
                    break;
                //(BOX)재고실사
                case R.id.bt_menu_8:
                    intent.putExtra("menu", Define.MENU_INVENTORY);
                    break;
                //박스라벨패킹
                case R.id.bt_menu_2:
                    intent.putExtra("menu", Define.MENU_BOXLBL);
                    break;
                //재고조사
                case R.id.bt_menu_10:
                    intent.putExtra("menu", Define.MENU_STOCK);
                    break;
                //시리얼위치조회
                case R.id.bt_menu_13:
                    intent.putExtra("menu", Define.MENU_SERIAL_LOCATION);
                    break;

                //재고조사(대리점)
                case R.id.bt_menu_14:
                    intent.putExtra("menu", Define.MENU_STORE_SEARCH);
                    break;
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
        }
    };
}
