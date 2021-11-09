package kr.co.bang.wms.model;

import java.util.List;

public class PopupCstModel extends ResultModel {

    List<PopupCstModel.Item> items;

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public class Item extends ResultModel{
        //거래처코드
        String c_code;
        //거래처명
        String c_name;

        public String getC_code() {
            return c_code;
        }

        public void setC_code(String c_code) {
            this.c_code = c_code;
        }

        public String getC_name() {
            return c_name;
        }

        public void setC_name(String c_name) {
            this.c_name = c_name;
        }
    }


}
