package kr.co.bang.wms.model;

import java.util.List;

public class StockDetailModel extends ResultModel{
    List<stockDetailModel> items;

    public List<stockDetailModel> getItems() {
        return items;
    }

    public void setItems(List<stockDetailModel> items) {
        this.items = items;
    }

    public class stockDetailModel extends ResultModel{
        //품목코드
        String itm_code;
        //품목명
        String itm_name;
        //시리얼번호
        String lot_no;
        //창고코드
        String wh_code;
        //창고명
        String wh_name;
        //재고수량
        int inv_qty;
        //모델
        String g_name3;
        //표기시리얼
        String lot_no_mix;

        public String getItm_code() {
            return itm_code;
        }

        public void setItm_code(String itm_code) {
            this.itm_code = itm_code;
        }

        public String getItm_name() {
            return itm_name;
        }

        public void setItm_name(String itm_name) {
            this.itm_name = itm_name;
        }

        public String getLot_no() {
            return lot_no;
        }

        public void setLot_no(String lot_no) {
            this.lot_no = lot_no;
        }

        public String getWh_code() {
            return wh_code;
        }

        public void setWh_code(String wh_code) {
            this.wh_code = wh_code;
        }

        public int getInv_qty() {
            return inv_qty;
        }

        public void setInv_qty(int inv_qty) {
            this.inv_qty = inv_qty;
        }

        public String getG_name3() {
            return g_name3;
        }

        public void setG_name3(String g_name3) {
            this.g_name3 = g_name3;
        }

        public String getLot_no_mix() {
            return lot_no_mix;
        }

        public void setLot_no_mix(String lot_no_mix) {
            this.lot_no_mix = lot_no_mix;
        }

        public String getWh_name() {
            return wh_name;
        }

        public void setWh_name(String wh_name) {
            this.wh_name = wh_name;
        }
    }
}
