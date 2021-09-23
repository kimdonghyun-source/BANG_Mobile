package kr.co.bang.wms.menu.main;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import java.util.ArrayList;

import kr.co.bang.wms.GlobalApplication;
import kr.co.bang.wms.R;
import kr.co.bang.wms.common.Define;
import kr.co.bang.wms.custom.BusProvider;
import kr.co.bang.wms.custom.CommonCompatActivity;
import kr.co.bang.wms.custom.CommonFragment;
import kr.co.bang.wms.menu.boxlbl.BoxlblFragment;
import kr.co.bang.wms.menu.house_new_move.HouseNewMoveDetailFragment;
import kr.co.bang.wms.menu.house_new_move.HouseNewMoveFragment;
import kr.co.bang.wms.menu.house_new_move.HouseNewMoveScanDetailFragment;
import kr.co.bang.wms.menu.inventory.InventoryFragment;

import kr.co.bang.wms.menu.move_ask.MoveAskFragment;
import kr.co.bang.wms.menu.out_list.MorOutListFragment;
import kr.co.bang.wms.menu.out_list.MorOutDetailFragment;
import kr.co.bang.wms.menu.out_list.MorOutPickingFragment;
import kr.co.bang.wms.menu.popup.TwoBtnPopup;

import kr.co.bang.wms.menu.serial_location.SerialLocationFragment;
import kr.co.bang.wms.menu.stock.StockFragment;
import kr.co.bang.wms.menu.stock.StockFragmentDetail;
import kr.co.bang.wms.menu.stock_store.StockStoreDetailFragment;
import kr.co.bang.wms.menu.stock_store.StockStoreFragment;
import kr.co.bang.wms.model.UserInfoModel;

public class BaseActivity extends CommonCompatActivity {
    Context mContext;

    //사이드 메뉴 레이아웃
    DrawerLayout drawer;
    View drawer_layout;
    //사이드 메뉴 리스트
    //DrawerMenu mDrawerMenu;
    //좌측 메뉴 리스트 아답터
    ListAdapter mAdapter;
    //다른 메뉴 이동시 묻는 팝업
    TwoBtnPopup mTwoBtnPopup;
    //메뉴 타이틀
    ImageView iv_title;
    //GNB 배경 이미지(피킹은 햄버거버튼 사용 안하기 때문)
    ImageView iv_gnb;
    ImageButton bt_drawer;
    ImageButton bt_print;
    //선택된 메뉴 postion
    int mSelectMenu;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_base);

        mContext = BaseActivity.this;

        findViewById(R.id.bt_back).setOnClickListener(onClickListener);

        drawer = findViewById(R.id.drawer);
        drawer_layout = findViewById(R.id.drawer_layout);
        bt_drawer = findViewById(R.id.bt_drawer);
        bt_drawer.setOnClickListener(onClickListener);
        bt_print = findViewById(R.id.bt_print);
        bt_print.setOnClickListener(onClickListener);
        findViewById(R.id.bt_close).setOnClickListener(onClickListener);

        ArrayList<String> list = new ArrayList<>();
        list.add("주문자재출고");
        list.add("이동 요청");
        list.add("창고 이동");
        list.add("박스라벨패킹");
        list.add("재고 실사");
        list.add("재고 조사");
        list.add("시리얼위치조회");
        list.add("추가할거추가");


        ListView listView = findViewById(R.id.list);
        mAdapter = new ListAdapter();
        mAdapter.setData(list);
        listView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();

        GlobalApplication application = (GlobalApplication) getApplicationContext();
        UserInfoModel.Items model = application.getUserInfoModel();

        TextView tv_name = findViewById(R.id.tv_name);
//        tv_name.setText(model.getDpt_name()+" "+model.getEmp_name());

        iv_title = findViewById(R.id.iv_title);
        iv_gnb = findViewById(R.id.iv_gnb);

        int menu = getIntent().getIntExtra("menu", 0);

        Bundle args = getIntent().getBundleExtra("args");

        mSelectMenu = menu - 2;

        switch (menu) {
            //주문자재출고(뱅)
            case Define.MENU_PRODUCTION_IN: {
                CommonFragment fragment = new MorOutListFragment();
                replaceContent(fragment, Define.TAG_PRODUCTION_IN, R.id.fl_content);
                break;
            }
            //주문자재출고상세(뱅) MENU_PRODUCTION_OUT
            case Define.MENU_PRODUCTION_DETAIL: {
                CommonFragment fragment = new MorOutDetailFragment();
                fragment.setArguments(args);
                replaceContent(fragment, Define.TAG_PRODUCTION_DETAIL, R.id.fl_content);
                break;
            }
            //주문자재출고피킹완료(뱅)
            case Define.MENU_PRODUCTION_OUT: {
                CommonFragment fragment = new MorOutPickingFragment();
                fragment.setArguments(args);
                replaceContent(fragment, Define.TAG_PRODUCTION_OUT, R.id.fl_content);
                break;
            }

            //이동요청(뱅)
            case Define.MENU_MOVE_ASK: {
                CommonFragment fragment = new MoveAskFragment();
                fragment.setArguments(args);
                replaceContent(fragment, Define.TAG_MOVE_ASK, R.id.fl_content);
                break;
            }

            /*//창고이동(뱅) (기존꺼)
            case Define.MENU_HOUSE_MOVE: {
                CommonFragment fragment = new HouseMoveFragment();
                fragment.setArguments(args);
                replaceContent(fragment, Define.TAG_HOUSE_MOVE, R.id.fl_content);
                break;
            }*/

            //창고이동(뱅) (새로 추가)
            case Define.MENU_HOUSE_MOVE_NEW: {
                CommonFragment fragment = new HouseNewMoveFragment();
                fragment.setArguments(args);
                replaceContent(fragment, Define.TAG_HOUSE_MOVE_NEW, R.id.fl_content);
                break;
            }
            //창고이동(뱅) 디테일
            case Define.MENU_HOUSE_MOVE_DATAIL: {
                CommonFragment fragment = new HouseNewMoveDetailFragment();
                fragment.setArguments(args);
                replaceContent(fragment, Define.TAG_HOUSE_MOVE_DETAIL, R.id.fl_content);
                break;
            }

            //창고이동(뱅) 스캔디테일
            case Define.MENU_HOUSE_MOVE_SCAN_DATAIL: {
                CommonFragment fragment = new HouseNewMoveScanDetailFragment();
                fragment.setArguments(args);
                replaceContent(fragment, Define.TAG_HOUSE_MOVE_SCAN_DETAIL, R.id.fl_content);
                break;
            }

            //박스라벨패킹(뱅)
            case Define.MENU_BOXLBL: {
                CommonFragment fragment = new BoxlblFragment();
                fragment.setArguments(args);
                replaceContent(fragment, Define.TAG_BOXLBL, R.id.fl_content);
                break;
            }

            //재고실사(뱅)
            case Define.MENU_INVENTORY: {
                CommonFragment fragment = new InventoryFragment();
                fragment.setArguments(args);
                replaceContent(fragment, Define.TAG_INVENTORY, R.id.fl_content);
                break;
            }

            //재고조사(뱅)
            case Define.MENU_STOCK: {
                CommonFragment fragment = new StockFragment();
                fragment.setArguments(args);
                replaceContent(fragment, Define.TAG_STOCK, R.id.fl_content);
                break;
            }
                //재고조사(뱅)
            case Define.MENU_STOCK_DETAIL: {
                CommonFragment fragment = new StockFragmentDetail();
                fragment.setArguments(args);
                replaceContent(fragment, Define.TAG_STOCK_DETAIL, R.id.fl_content);
                break;
            }
            //시리얼위치조회(뱅)
            case Define.MENU_SERIAL_LOCATION: {
                CommonFragment fragment = new SerialLocationFragment();
                fragment.setArguments(args);
                replaceContent(fragment, Define.TAG_SERIAL_LOCATION, R.id.fl_content);
                break;
            }

            //재고조사(대리점)
            case Define.MENU_STOCK_STORE: {
                CommonFragment fragment = new StockStoreFragment();
                fragment.setArguments(args);
                replaceContent(fragment, Define.TAG_STOCK_STORE, R.id.fl_content);
                break;
            }

            //재고조사디테일(대리점)
            case Define.MENU_STOCK_STORE_DATAIL: {
                CommonFragment fragment = new StockStoreDetailFragment();
                fragment.setArguments(args);
                replaceContent(fragment, Define.TAG_STOCK_STORE_DATAIL, R.id.fl_content);
                break;
            }


        }

        setTitleImage(menu);
    }

    private void setTitleImage(int menu) {
        //메뉴별 타이틀 이미지
        int image = 0;
        //자재불출과 제품출고에 있는 피킹 상단엔 좌측메뉴가 노출되지 않기 때문에 분기해야함
        int gnb = R.drawable.titilbar;
        int isDrawer = View.VISIBLE;
        int isLock = DrawerLayout.LOCK_MODE_UNLOCKED;

        //프린터 화면에서만 노출하면 되기 때문에 gone 처리 후 프린터 화면 진입 시 visible
        bt_print.setVisibility(View.GONE);

        switch (menu) {
//---------------------------------------------------------------------------------뱅
            //주문자재출고(뱅)
            case Define.MENU_PRODUCTION_IN: {
                image = R.drawable.menu_product_in_title;
                break;
            }
            //주문자제출고피킹(뱅)
            case Define.MENU_PRODUCTION_OUT: {
                image = R.drawable.menu_production_out_title;
                break;
            }
            //주문자재출고 상세(뱅) MENU_PRODUCTION_OUT
            case Define.MENU_PRODUCTION_DETAIL: {
                image = R.drawable.menu_product_in_title;
                break;
            }

            //주문자재출고 상세(뱅) MENU_PRODUCTION_OUT
            case Define.MENU_MOVE_ASK: {
                image = R.drawable.menu_moveask_title;
                break;
            }

            /*//창고이동(뱅) 기존꺼
            case Define.MENU_HOUSE_MOVE: {
                image = R.drawable.menu_waremove_title;
                break;
            }*/

            //창고이동(뱅) 새로추가
            case Define.MENU_HOUSE_MOVE_NEW: {
                image = R.drawable.menu_waremove_title;
                break;
            }

            //창고이동(뱅) 디테일
            case Define.MENU_HOUSE_MOVE_DATAIL: {
                image = R.drawable.menu_waremove_title;
                break;
            }

            //박스라벨패킹(뱅)
            case Define.MENU_BOXLBL: {
                image = R.drawable.menu_boxlbl_title;
                break;
            }

            //재고실사(뱅)
            case Define.MENU_INVENTORY: {
                image = R.drawable.menu_inventory_title;
                break;
            }

            //재고조사(뱅)
            case Define.MENU_STOCK: {
                image = R.drawable.menu_stock_title;
                break;
            }

            //재고조사(뱅)디테일
            case Define.MENU_STOCK_DETAIL: {
                image = R.drawable.menu_stock_title;
                break;
            }

            //시리얼 위치 조회
            case Define.MENU_SERIAL_LOCATION: {
                image = R.drawable.menu_serial_search_title;
                break;
            }

            //재고조사(대리점)
            case Define.MENU_STOCK_STORE: {
                image = R.drawable.menu_product_stock_store_title;
                break;
            }

            //재고조사디테일(대리점)
            case Define.MENU_STOCK_STORE_DATAIL: {
                image = R.drawable.menu_product_stock_store_title;
                break;
            }


//---------------------------------------------------------------------------------뱅

        }
        iv_title.setBackgroundResource(image);
        iv_gnb.setBackgroundResource(gnb);
        bt_drawer.setVisibility(isDrawer);
        drawer.setDrawerLockMode(isLock);
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int view = v.getId();
            switch (view) {
                case R.id.bt_back:
                    mTwoBtnPopup = new TwoBtnPopup(BaseActivity.this, "작업 내용이 취소됩니다.\n 뒤로가시겠습니까?", R.drawable.popup_title_alert, new Handler() {
                        @Override
                        public void handleMessage(Message msg) {
                            if (msg.what == 1) {
                                mTwoBtnPopup.hideDialog();
                                finish();
                            }
                        }
                    });
                    //backPressed();
                    break;
                case R.id.bt_drawer:
                    drawer.openDrawer(drawer_layout);
                    break;
                case R.id.bt_close:
                    drawer.closeDrawers();
                    break;
                case R.id.bt_print:
                    BusProvider.getInstance().post(0);
                    break;
            }
        }
    };

    @Override
    public void onBackPressed() {
        mTwoBtnPopup = new TwoBtnPopup(BaseActivity.this, "작업 내용이 취소됩니다.\n 뒤로가시겠습니까?", R.drawable.popup_title_alert, new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 1) {
                    mTwoBtnPopup.hideDialog();
                    finish();
                }
            }
        });
    }

    public void backPressed() {
        try {
            //사이드 메뉴가 열려있으면 닫아준다.
            if (drawer.isDrawerOpen(drawer_layout)) {
                drawer.closeDrawers();
                return;
            }

            FragmentManager mFragmentManager = ((FragmentActivity) mContext).getSupportFragmentManager();
            int count = mFragmentManager.getBackStackEntryCount();

            if (count >= 1) {
                mFragmentManager.popBackStack();
            } else {
                //finish();
            }
        } catch (Exception e) {
            //finish();
        }
    }

    class ListAdapter extends BaseAdapter {
        LayoutInflater mInflater;
        ArrayList<String> mList;

        public void setData(ArrayList<String> list) {
            mList = list;
        }

        public ListAdapter() {
            mInflater = LayoutInflater.from(mContext);
        }

        @Override
        public int getCount() {
            if (mList == null) {
                return 0;
            }

            return mList.size();
        }

        @Override
        public String getItem(int position) {
            return mList.get(position);
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
                v = mInflater.inflate(R.layout.cell_drawer, null);
                v.setTag(holder);

                holder.tv_menu = v.findViewById(R.id.tv_menu);
            } else {
                holder = (ViewHolder) v.getTag();
            }

            if (mSelectMenu == position) {
                holder.tv_menu.setSelected(true);
            } else {
                holder.tv_menu.setSelected(false);
            }

            final String menu = mList.get(position);
            holder.tv_menu.setText((position + 1) + ". " + menu);

            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mSelectMenu == position) {
                        drawer.closeDrawer(drawer_layout);
                        return;
                    }
                    mTwoBtnPopup = new TwoBtnPopup(BaseActivity.this, menu + " 메뉴로 이동하시겠습니까?", R.drawable.popup_title_alert, new Handler() {
                        @Override
                        public void handleMessage(Message msg) {
                            if (msg.what == 1) {
                                //팝업 닫기
                                mTwoBtnPopup.hideDialog();
                                //좌측메뉴 닫기
                                drawer.closeDrawer(drawer_layout);
                                //선택한 메뉴 기억
                                mSelectMenu = position;
                                //0이 없고 입고등록이 삭제되어 +2 해줘야함
                                switch (position + 2) {

                                    //주문자재출고(뱅)
                                    case Define.MENU_PRODUCTION_IN: {
                                        CommonFragment fragment = new MorOutListFragment();
                                        replaceContent(fragment, Define.TAG_PRODUCTION_IN, R.id.fl_content);
                                        break;
                                    }

                                    //주문자재출고(뱅)
                                    case Define.MENU_MOVE_ASK: {
                                        CommonFragment fragment = new MoveAskFragment();
                                        replaceContent(fragment, Define.TAG_MOVE_ASK, R.id.fl_content);
                                        break;
                                    }

                                    /*//창고이동(뱅) 기존꺼
                                    case Define.MENU_HOUSE_MOVE: {
                                        CommonFragment fragment = new HouseMoveFragment();
                                        replaceContent(fragment, Define.TAG_HOUSE_MOVE, R.id.fl_content);
                                        break;
                                    }*/

                                    //창고이동(뱅) 새로추가
                                    case Define.MENU_HOUSE_MOVE_NEW: {
                                        CommonFragment fragment = new HouseNewMoveFragment();
                                        replaceContent(fragment, Define.TAG_HOUSE_MOVE_NEW, R.id.fl_content);
                                        break;
                                    }

                                    //박스라벨피킹(뱅)
                                    case Define.MENU_BOXLBL: {
                                        CommonFragment fragment = new BoxlblFragment();
                                        replaceContent(fragment, Define.TAG_BOXLBL, R.id.fl_content);
                                        break;
                                    }

                                    //재고실사(뱅)
                                    case Define.MENU_INVENTORY: {
                                        CommonFragment fragment = new InventoryFragment();
                                        replaceContent(fragment, Define.TAG_INVENTORY, R.id.fl_content);
                                        break;
                                    }

                                    //재고조사(뱅)
                                    case Define.MENU_STOCK: {
                                        CommonFragment fragment = new StockFragment();
                                        replaceContent(fragment, Define.TAG_STOCK, R.id.fl_content);
                                        break;
                                    }

                                    //시리얼위치조회
                                    case Define.MENU_SERIAL_LOCATION: {
                                        CommonFragment fragment = new SerialLocationFragment();
                                        replaceContent(fragment, Define.TAG_SERIAL_LOCATION, R.id.fl_content);
                                        break;
                                    }

                                    //재고조사대리점
                                    case Define.MENU_STOCK_STORE: {
                                        CommonFragment fragment = new StockStoreFragment();
                                        replaceContent(fragment, Define.TAG_STOCK_STORE, R.id.fl_content);
                                        break;
                                    }


                                }
                                setTitleImage(position + 2);
                                mAdapter.notifyDataSetChanged();
                            }
                        }
                    });
                }
            });

            return v;
        }

        class ViewHolder {
            TextView tv_menu;
        }
    }
}
