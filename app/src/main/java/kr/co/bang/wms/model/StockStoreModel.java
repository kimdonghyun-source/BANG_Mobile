package kr.co.bang.wms.model;

import java.util.List;

public class StockStoreModel extends ResultModel{
    List<StockStoreModel.Item>items;

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public class Item extends ResultModel{
        //사업장
        String corp_code;
        //조사일자
        String stk_date;
        //순번
        int stk_no1;
        //창고
        String wh_code;
        //창고명
        String wh_name;
        //비고
        String remark;
        //구분값
        String wh_type;
        //재고기준일
        String inv_date;

        public String getCorp_code() {
            return corp_code;
        }

        public void setCorp_code(String corp_code) {
            this.corp_code = corp_code;
        }

        public String getStk_date() {
            return stk_date;
        }

        public void setStk_date(String stk_date) {
            this.stk_date = stk_date;
        }

        public int getStk_no1() {
            return stk_no1;
        }

        public void setStk_no1(int stk_no1) {
            this.stk_no1 = stk_no1;
        }

        public String getWh_code() {
            return wh_code;
        }

        public void setWh_code(String wh_code) {
            this.wh_code = wh_code;
        }

        public String getWh_name() {
            return wh_name;
        }

        public void setWh_name(String wh_name) {
            this.wh_name = wh_name;
        }

        public String getRemark() {
            return remark;
        }

        public void setRemark(String remark) {
            this.remark = remark;
        }

        public String getWh_type() {
            return wh_type;
        }

        public void setWh_type(String wh_type) {
            this.wh_type = wh_type;
        }

        public String getInv_date() {
            return inv_date;
        }

        public void setInv_date(String inv_date) {
            this.inv_date = inv_date;
        }
    }

}


