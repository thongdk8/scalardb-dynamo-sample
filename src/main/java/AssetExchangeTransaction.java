import com.google.inject.Guice;
import com.google.inject.Injector;
import com.scalar.db.api.DistributedTransaction;
import com.scalar.db.api.Get;
import com.scalar.db.api.Put;
import com.scalar.db.api.Result;
import com.scalar.db.config.DatabaseConfig;
import com.scalar.db.exception.storage.ExecutionException;
import com.scalar.db.exception.transaction.TransactionException;
import com.scalar.db.io.IntValue;
import com.scalar.db.io.Key;
import com.scalar.db.io.TextValue;
import com.scalar.db.service.TransactionModule;
import com.scalar.db.service.TransactionService;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

public class AssetExchangeTransaction extends AssetExchange{
    private final TransactionService txService;

    public AssetExchangeTransaction() throws IOException {
        DatabaseConfig dbConfig = new DatabaseConfig(new FileInputStream(SCALARDB_PROPERTIES));
        Injector injector = Guice.createInjector(new TransactionModule(dbConfig));
        txService = injector.getInstance(TransactionService.class);
    }

    public String createAsset(String description, int price, String belong) throws TransactionException {
        DistributedTransaction tx = txService.start();
        UUID uuid = UUID.randomUUID();
        String assetId = uuid.toString();
        txService.with(NAMESPACE, ASSET_TABLE_NAME);
        Put assetPut = new Put(new Key(new TextValue(ID, assetId)))
                .withValue(new TextValue("description", description))
                .withValue(new IntValue("price", price))
                .withValue(new TextValue("belong", belong))
                .forNamespace(NAMESPACE).forTable(ASSET_TABLE_NAME);
        tx.put(assetPut);
        tx.commit();
        return assetId;
    }

    public String createUser(String name, int balance) throws TransactionException {
        DistributedTransaction tx = txService.start();
        UUID uuid = UUID.randomUUID();
        String userId = uuid.toString();
        txService.with(NAMESPACE, USER_TABLE_NAME);
        Put userPut = new Put(new Key(new TextValue(ID, userId)))
                .withValue(new TextValue("name", name))
                .withValue(new IntValue("balance", balance))
                .forNamespace(NAMESPACE).forTable(USER_TABLE_NAME);
        tx.put(userPut);
        tx.commit();
        return userId;
    }

    public void exchangeAsset(String assetId, String fromId, String toId) throws TransactionException {
        System.out.println(assetId);
        System.out.println(fromId);
        System.out.println(toId);
        DistributedTransaction tx = txService.start();

        Get assetGet = new Get(new Key(new TextValue(ID, assetId)))
                .forNamespace(NAMESPACE).forTable(ASSET_TABLE_NAME);
        Optional<Result> assetResult = tx.get(assetGet);
        String assetOwnerId;
        int assetPrice;
        if (assetResult.isPresent()) {
            assetOwnerId = ((TextValue) (assetResult.get().getValue("belong").get())).getString().get();
            assetPrice = ((IntValue) (assetResult.get().getValue("price").get())).get();
        } else throw new RuntimeException("Asset with id " + assetId + " belong to user id " + fromId + " doesn't exist!");
        if (!assetOwnerId.equals(fromId)) throw new RuntimeException("Asset " + assetId +
                " doesn't belong to user id " + fromId);

        Get fromGet = new Get(new Key(new TextValue(ID, fromId))).forNamespace(NAMESPACE).forTable(USER_TABLE_NAME);
        Get toGet = new Get(new Key(new TextValue(ID, toId))).forNamespace(NAMESPACE).forTable(USER_TABLE_NAME);
        Optional<Result> fromResult = tx.get(fromGet);
        Optional<Result> toResult = tx.get(toGet);
        int fromBalance;
        int toBalance;
        if (fromResult.isPresent()) {
            fromBalance = ((IntValue) (fromResult.get().getValue("balance").get())).get();
        } else {
          throw new RuntimeException("User with id " + fromId + " doesn't exist!");
        }
        if (toResult.isPresent()) {
            toBalance = ((IntValue) (toResult.get().getValue("balance").get())).get();
        } else throw new RuntimeException("User with id " + fromId + " doesn't exist!");
        if (toBalance < assetPrice) throw new RuntimeException("Balance of user id" + toId + " is insufficient!");
        Put fromPut = new Put(new Key(new TextValue(ID, fromId)))
                .withValue(new IntValue("balance", fromBalance + assetPrice)).forNamespace(NAMESPACE).forTable(USER_TABLE_NAME);
        Put toPut = new Put(new Key(new TextValue(ID, toId)))
                .withValue(new IntValue("balance", toBalance - assetPrice)).forNamespace(NAMESPACE).forTable(USER_TABLE_NAME);
        tx.put(fromPut);
        tx.put(toPut);

        txService.with(NAMESPACE, ASSET_TABLE_NAME);
        Put assetPut = new Put(new Key(new TextValue(ID, assetId)))
                .withValue(new TextValue("belong", toId))
                .forNamespace(NAMESPACE).forTable(ASSET_TABLE_NAME);
        tx.put(assetPut);

        tx.commit();
    }

    public void close() {
        txService.close();
    }
}
