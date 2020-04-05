package entities.jsonModel;

import java.math.BigDecimal;

public class UserStorage {
    public int id;
    public long totalUsed;
    public double totalUsedGb;
    public UserStorage(){
    }
    public UserStorage(Object[] data){
        this.id = ((Integer)data[0]).intValue();
        this.totalUsed = ((BigDecimal)data[1]).longValue();
    }
}
