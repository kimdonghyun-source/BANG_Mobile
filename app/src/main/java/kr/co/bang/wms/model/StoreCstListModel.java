package kr.co.bang.wms.model;

import java.util.List;

public class StoreCstListModel extends ResultModel {

    List<StoreCstListModel.Item> items;

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public class Item extends ResultModel{

        //거래처코드
        String cst_code;
        //거래처명
        String cst_name;
        //전화번호
        String tel_no;

        public String getCst_code() {
            return cst_code;
        }

        public void setCst_code(String cst_code) {
            this.cst_code = cst_code;
        }

        public String getCst_name() {
            return cst_name;
        }

        public void setCst_name(String cst_name) {
            this.cst_name = cst_name;
        }

        public String getTel_no() {
            return tel_no;
        }

        public void setTel_no(String tel_no) {
            this.tel_no = tel_no;
        }
    }
}
