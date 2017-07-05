package pem.de.heroes.shared;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;

public class CounterTransactionHandler implements Transaction.Handler {
    private int count;

    public CounterTransactionHandler(int count) {
        this.count = count;
    }

    @Override
    public Transaction.Result doTransaction(MutableData mutableData) {
        Integer currentValue = mutableData.getValue(Integer.class);
        if (currentValue == null) {
            mutableData.setValue(count);
        }
        else {
            mutableData.setValue(currentValue + count);
        }

        return Transaction.success(mutableData);
    }

    @Override
    public void onComplete(DatabaseError firebaseError, boolean committed, DataSnapshot currentData) {
        if (firebaseError != null) {
            System.out.println("Firebase counter increment failed.");
        } else {
            System.out.println("Firebase counter increment succeeded.");
        }
    }
}
