package kr.co.bang.wms.common;

public class Define {

    //주문자재출고(뱅)
    public static final int MENU_PRODUCTION_IN = 2;
    //주문자재출고 상세(뱅)
    public static final int MENU_PRODUCTION_DETAIL = 20;
    //주문자재출고 피킹(뱅)
    public static final int MENU_PRODUCTION_OUT = 21;

    ////창고이동(뱅) 기존꺼
    //public static final int MENU_HOUSE_MOVE = 3;

    //이동요청
    public static final int MENU_MOVE_ASK = 3;

    //창고이동(뱅) 새로추가
    public static final int MENU_HOUSE_MOVE_NEW = 4;
    //창고이동(뱅) 상세조회
    public static final int MENU_HOUSE_MOVE_DATAIL = 41;
    //창고이동(뱅) 스캔리스트 조회
    public static final int MENU_HOUSE_MOVE_SCAN_DATAIL = 42;

    //박스라벨패킹(뱅)
    public static final int MENU_BOXLBL = 5;

    //재고실사(뱅)
    public static final int MENU_INVENTORY = 6;

    //재고조사(뱅)
    public static final int MENU_STOCK = 7;
    //재고조사(뱅) 스캔리스트 조회
    public static final int MENU_STOCK_DETAIL = 71;

    //시리얼위치조회
    public static final int MENU_SERIAL_LOCATION = 8;

    //재고조사(대리점)
    public static final int MENU_STOCK_STORE = 9;
    //재고조사
    public static final int MENU_STOCK_STORE_DATAIL = 91;

   //--------------------------------------------------------------------------------
    //주문자재출고(뱅)
    public static final String TAG_PRODUCTION_IN = "production";
    //주문자재출고 상세(뱅)
    public static final String TAG_PRODUCTION_DETAIL = "production_detail";
    //주문자재출고 피킹(뱅)
    public static final String TAG_PRODUCTION_OUT = "production_out";
    //이동요청
    public static final String TAG_MOVE_ASK = "move_ask";
    //창고이동 (뱅) 기존꺼
    //public static final String TAG_HOUSE_MOVE = "house_move";
    //창고이동 (뱅) 새로추가
    public static final String TAG_HOUSE_MOVE_NEW = "house_move_new";
    //창고이동(뱅) 디테일
    public static final String TAG_HOUSE_MOVE_DETAIL = "house_move_detail";
    //창고이동(뱅) 스캔디테일
    public static final String TAG_HOUSE_MOVE_SCAN_DETAIL = "house_move_detail";
    //박스라벨패킹
    public static final String TAG_BOXLBL = "boxlbl";
    //재고실사
    public static final String TAG_INVENTORY = "invertory";
    //재고조사
    public static final String TAG_STOCK = "stock";
    //재고조사(뱅) 디테일
    public static final String TAG_STOCK_DETAIL = "stock_detail";
    //시리얼위치조회
    public static final String TAG_SERIAL_LOCATION = "serial_location";
    //재고조사(대리점)
    public static final String TAG_STOCK_STORE = "stock_store";
    //재고조사디테일(대리점)
    public static final String TAG_STOCK_STORE_DATAIL = "stock_store_datail";


    //--------------------------------------------------------------------------------


}
