package kr.co.bang.wms.model;

import java.util.List;

public class CalcModel extends ResultModel {

    List<CalcModel.Item> items;

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public class Item extends ResultModel{
        //거래처코드
        String cst_code;
        //품목코드
        String itm_code;
        //품목명
        String itm_uname;
        //마진률
        int margin_rate;
        //소비자가
        int ITM_PRICE5;
        //품목그룹
        String itm_ucode;
        //금액
        int itm_price;
        //수량
        int scan_qty;

        public String getCst_code() {
            return cst_code;
        }

        public void setCst_code(String cst_code) {
            this.cst_code = cst_code;
        }

        public String getItm_code() {
            return itm_code;
        }

        public void setItm_code(String itm_code) {
            this.itm_code = itm_code;
        }

        public String getItm_uname() {
            return itm_uname;
        }

        public void setItm_uname(String itm_uname) {
            this.itm_uname = itm_uname;
        }

        public int getMargin_rate() {
            return margin_rate;
        }

        public void setMargin_rate(int margin_rate) {
            this.margin_rate = margin_rate;
        }

        public int getITM_PRICE5() {
            return ITM_PRICE5;
        }

        public void setITM_PRICE5(int ITM_PRICE5) {
            this.ITM_PRICE5 = ITM_PRICE5;
        }

        public String getItm_ucode() {
            return itm_ucode;
        }

        public void setItm_ucode(String itm_ucode) {
            this.itm_ucode = itm_ucode;
        }

        public int getItm_price() {
            return itm_price;
        }

        public void setItm_price(int itm_price) {
            this.itm_price = itm_price;
        }

        public int getScan_qty() {
            return scan_qty;
        }

        public void setScan_qty(int scan_qty) {
            this.scan_qty = scan_qty;
        }
    }
}
