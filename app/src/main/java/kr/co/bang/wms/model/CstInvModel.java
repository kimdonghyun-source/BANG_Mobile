package kr.co.bang.wms.model;

import java.util.List;

public class CstInvModel extends ResultModel {

    List<CstInvModel.Item> items;

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public class Item extends ResultModel{
        //품목코드
        String itm_code;
        //품목명
        String itm_name;
        //재고수량
        int inv_qty;
        //로트번호(MIX)
        String lot_no;
        //로트번호
        String lot_no1;
        //판매용 카운트
        int count_n;
        //시타용 카운트
        int count_y;
        //시타판매구분
        String c_yn;
        //비교
        String sts;

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

        public int getInv_qty() {
            return inv_qty;
        }

        public void setInv_qty(int inv_qty) {
            this.inv_qty = inv_qty;
        }

        public String getLot_no() {
            return lot_no;
        }

        public void setLot_no(String lot_no) {
            this.lot_no = lot_no;
        }

        public String getLot_no1() {
            return lot_no1;
        }

        public void setLot_no1(String lot_no1) {
            this.lot_no1 = lot_no1;
        }

        public String getC_yn() {
            return c_yn;
        }

        public void setC_yn(String c_yn) {
            this.c_yn = c_yn;
        }

        public int getCount_n() {
            return count_n;
        }

        public void setCount_n(int count_n) {
            this.count_n = count_n;
        }

        public int getCount_y() {
            return count_y;
        }

        public void setCount_y(int count_y) {
            this.count_y = count_y;
        }

        public String getSts() {
            return sts;
        }

        public void setSts(String sts) {
            this.sts = sts;
        }
    }
}
