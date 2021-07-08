## Asset Exchange CLI App
This is a learning and study project using [ScalarDB](https://github.com/scalar-labs/scalardb) as backend and  [picocli](https://github.com/remkop/picocli) for command line interface. 

### App Features
- Creating a user with an initial balance
- Creating an asset with a price that belong to a user
- Exchange an asset between to user at the price

### Prerequisites
- Java
- Gradle

### Setting up
#### ScalarDB
ScalarDB support many Databases as storage such as Cassandra, CosmosDB, DynamoDB, MySQL, Postgres. 

In this study we will use ScalarDB with DynamoDB as storage. The configuration will be presented in [scalardb.properties](scalardb.properties). The fields `<REGION>`, `<AWS_ACCESS_KEY_ID>`, `<AWS_ACCESS_SECRET_KEY>` must be replaced with a real one.
```
# Comma separated contact points
scalar.db.contact_points=<REGION>

# Port number for all the contact points. Default port number for each database is used if empty.
#scalar.db.contact_port=

# Credential information to access the database
scalar.db.username=<AWS_ACCESS_KEY_ID>
scalar.db.password=<AWS_ACCESS_SECRET_KEY>

# Storage implementation. Either cassandra or cosmos or dynamo or jdbc can be set. Default storage is cassandra.
scalar.db.storage=dynamo
```
#### Schema creation
We need a schema to define our data models. Our schema file can be found [here](src/main/resources/asset_exchange_schema.json). Noted that to support transaction, the `transaction` option must set to be `true`. 
```
{
  "asset_exchange.asset": {
    "transaction": true,
    "partition-key": [
      "id"
    ],
    "clustering-key": [
      "belong"
    ],
    "columns": {
      "id": "TEXT",
      "description": "TEXT",
      "price": "INT",
      "belong": "TEXT"
    }
  },

  "asset_exchange.user": {
    "transaction": true,
    "partition-key": [
      "id"
    ],
    "clustering-key": [],
    "columns": {
      "id": "TEXT",
      "name": "TEXT",
      "balance": "INT"
    }
  }
}
```

Running database initialization to DynamoDB storage with command
```
java -jar tools/scalar-schema-standalone-3.0.0.jar --dynamo -u <AWS_ACCESS_KEY_ID> -p <AWS_ACCESS_SECRET_KEY> --region <REGION> -f src/main/resources/asset_exchange_schema.json
```

### Build and run
#### Build project using gradle
```
./gradlew build
```

#### Run
- Creating a user
```
#    -cu, --create_user                 Create an user
#    -ub, --user_balance=<userBalance>  User balance
#    -un, --user_name=<userName>        User name
./gradlew run --args="-m creating -cu -un <userName> -ub <userBalance>"
```
- Creating a asset
```
#    -ca, --create_asset                            Create an asset
#    -ab, --asset_belong=<assetBelong>              Asset belong to
#    -ad, --asset_description=<assetDescription>    Asset description
#    -ap, --asset_price=<assetPrice>                Asset price
./gradlew run --args="-m creating -ca -ab <assetBelong> -ad <assetDescription> -ap <assetPrice>"
```
- Exchange an asset
```
#    -a, --asset_id=<assetId>   Asset id
#    -f, --from=<fromId>        From user id
#    -t, --to=<toId>            To user id
./gradlew run --args="-m exchanging -a <assetId> -f <fromId> -t <toId>"
```

#### Sample commands
- Create user a with 10 in balance
```
  ~/works/scalar-labs/AssetExchange  ./gradlew run --args="-m creating -cu -un 'user a' -ub 10"                   1 ↵  7761  15:52:29

> Task :run
Storage mode
[main] INFO com.scalar.db.storage.dynamo.Dynamo - DynamoDB object is created properly.
User creating ...
[main] INFO com.scalar.db.transaction.consensuscommit.CommitHandler - transaction 4dc5c2e4-9abf-4c0c-a280-9970d308cbac is committed successfully at 1625727279709
Creating successfully user id 954acdf2-3c1d-4978-9b03-6784b9685758

BUILD SUCCESSFUL in 3s
3 actionable tasks: 3 executed
```
- Create user b with 100 in balance
```
~/works/scalar-labs/AssetExchange  ./gradlew run --args="-m creating -cu -un 'user b' -ub 100"                    ✔  7762  15:54:40

> Task :run
Storage mode
[main] INFO com.scalar.db.storage.dynamo.Dynamo - DynamoDB object is created properly.
User creating ...
[main] INFO com.scalar.db.transaction.consensuscommit.CommitHandler - transaction c60a533a-be4c-45aa-a5f4-bf21120b36a1 is committed successfully at 1625727287361
Creating successfully user id 63fc2a84-ad05-4f8b-b344-98d367659e17

BUILD SUCCESSFUL in 2s
3 actionable tasks: 1 executed, 2 up-to-date
```
- Create user c with 100 in balance
```
   ~/works/scalar-labs/AssetExchange  ./gradlew run --args="-m creating -cu -un 'user c' -ub 100"                    ✔  7763  15:54:48

> Task :run
Storage mode
[main] INFO com.scalar.db.storage.dynamo.Dynamo - DynamoDB object is created properly.
User creating ...
[main] INFO com.scalar.db.transaction.consensuscommit.CommitHandler - transaction df09883a-21c2-4ca3-a1de-8bd744d4457d is committed successfully at 1625727295751
Creating successfully user id 80cb36d2-2dbb-4d4d-9c61-9e11c30b3e12

BUILD SUCCESSFUL in 2s
3 actionable tasks: 1 executed, 2 up-to-date
```
- Create asset 1 with price 20, belong to user b
```

 ~/works/scalar-labs/AssetExchange  ./gradlew run --args="-m creating -ca -ab '63fc2a84-ad05-4f8b-b344-98d367659e17' -ad 'asset 1' -ap 20"

> Task :run
Storage mode
[main] INFO com.scalar.db.storage.dynamo.Dynamo - DynamoDB object is created properly.
Asset creating ...
[main] INFO com.scalar.db.transaction.consensuscommit.CommitHandler - transaction 7745a997-aebb-4dc9-ad28-41e646e6c0c1 is committed successfully at 1625727326296
Creating successfully asset id 1bacda1c-3d6d-4d2f-b823-a8e68a610dd7 belong to user id 63fc2a84-ad05-4f8b-b344-98d367659e17

BUILD SUCCESSFUL in 2s
3 actionable tasks: 1 executed, 2 up-to-date
```
- Exchange asset 1 from user b to user c
```
 ~/works/scalar-labs/AssetExchange  ./gradlew run --args="-m exchanging -a '1bacda1c-3d6d-4d2f-b823-a8e68a610dd7' -f '63fc2a84-ad05-4f8b-b344-98d367659e17' -t '80cb36d2-2dbb-4d4d-9c61-9e11c30b3e12'"

> Task :run
Transaction mode
[main] INFO com.scalar.db.storage.dynamo.Dynamo - DynamoDB object is created properly.
Exchanging asset...
1bacda1c-3d6d-4d2f-b823-a8e68a610dd7
63fc2a84-ad05-4f8b-b344-98d367659e17
80cb36d2-2dbb-4d4d-9c61-9e11c30b3e12
[main] INFO com.scalar.db.transaction.consensuscommit.CommitHandler - transaction 470ae82b-1163-437f-bfc1-044a605fd5ad is committed successfully at 1625727405923
Exchanging successfully

BUILD SUCCESSFUL in 4s
3 actionable tasks: 1 executed, 2 up-to-date
```
- Exchange asset 1 from user c to user a (failed cause user a balance is insufficient) 
```
 ~/works/scalar-labs/AssetExchange  ./gradlew run --args="-m transaction -a '1bacda1c-3d6d-4d2f-b823-a8e68a610dd7' -f '80cb36d2-2dbb-4d4d-9c61-9e11c30b3e12' -t '954acdf2-3c1d-4978-9b03-6784b9685758'"

> Task :run
Transaction mode
[main] INFO com.scalar.db.storage.dynamo.Dynamo - DynamoDB object is created properly.
Exchanging asset...
1bacda1c-3d6d-4d2f-b823-a8e68a610dd7
80cb36d2-2dbb-4d4d-9c61-9e11c30b3e12
954acdf2-3c1d-4978-9b03-6784b9685758
java.lang.RuntimeException: Balance of user id954acdf2-3c1d-4978-9b03-6784b9685758 is insufficient!
```