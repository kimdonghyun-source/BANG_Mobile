package kr.co.bang.wms.model;

import java.util.List;

public class MatScanCntModel extends ResultModel {
    List<MatScanCntModel.Items> items;

    public List<Items> getItems() {
        return items;
    }

    public void setItems(List<Items> items) {
        this.items = items;
    }

    public class Items extends ResultModel{
        //카운트 체크
        int R_CNT;

        public int getR_CNT() {
            return R_CNT;
        }

        public void setR_CNT(int r_CNT) {
            R_CNT = r_CNT;
        }
    }
}
