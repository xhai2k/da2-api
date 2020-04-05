package entities.jsonModel;

import utils.CommonUtils;

import java.math.BigDecimal;

public class StorageAllTab {
    public double storageAllUsed;
    public double storageAll;
    public StorageAllTab(Object[] info){
        this.storageAllUsed = CommonUtils.bytesToGB(((BigDecimal)info[0]).longValue());
        this.storageAll = CommonUtils.bytesToGB(((BigDecimal)info[1]).longValue());
    }
}
