package pem.de.heroes.main;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;

public class IncrementTransactionHandler implements Transaction.Handler {
    @Override
    public Transaction.Result doTransaction(MutableData mutableData) {
        Long value = mutableData.getValue(Long.class);
        if (value == null) {
            mutableData.setValue(0);
        }
        else {
            mutableData.setValue(value + 1);
        }

        return Transaction.success(mutableData);
    }

    @Override
    public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {

    }
}
