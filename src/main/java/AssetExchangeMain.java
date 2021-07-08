import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "asset_exchange", mixinStandardHelpOptions = true, version = "1.0",
    description = "Creating asset, user and exchanging assets within users along with a price")
public class AssetExchangeMain implements Callable<Integer> {

    @CommandLine.Option(names = {"-m", "--mode"}, description = "Using creating mode for creating resource," +
            " exchanging mode for exchanging asset", required = true)
    String mode;

    @CommandLine.ArgGroup(exclusive = false, multiplicity = "0..1")
    AssetArgs assetArgs;
    static class AssetArgs {
        @CommandLine.Option(names = {"-ca", "--create_asset"}, description = "Create an asset", required = true)
        boolean assetCreating;
        @CommandLine.Option(names = {"-ad", "--asset_description"}, description = "Asset description", required = true)
        String assetDescription;
        @CommandLine.Option(names = {"-ap", "--asset_price"}, description = "Asset price", required = true)
        int assetPrice;
        @CommandLine.Option(names = {"-ab", "--asset_belong"}, description = "Asset belong to", required = true)
        String assetBelong;
    }

    @CommandLine.ArgGroup(exclusive = false, multiplicity = "0..1")
    UserArgs userArgs;
    static class UserArgs {
        @CommandLine.Option(names = {"-cu", "--create_user"}, description = "Create an user", required = true)
        boolean userCreating;
        @CommandLine.Option(names = {"-un", "--user_name"}, description = "User name", required = true)
        String userName;
        @CommandLine.Option(names = {"-ub", "--user_balance"}, description = "User balance", required = true)
        int userBalance;
    }

    @CommandLine.ArgGroup(exclusive = false, multiplicity = "0..1")
    ExchangeArgs exchangeArgs;
    static class ExchangeArgs {
        @CommandLine.Option(names = {"-a", "--asset_id"}, description = "Asset id", required = true)
        String assetId;
        @CommandLine.Option(names = {"-f", "--from"}, description = "From user id", required = true)
        String fromId;
        @CommandLine.Option(names = {"-t", "--to"}, description = "To user id", required = true)
        String toId;
    }

    @Override
    public Integer call() throws Exception{
        AssetExchangeTransaction assetExchangeTransaction = new AssetExchangeTransaction();
        if (mode.equals("creating")) {
            System.out.println("Creating mode");
            if (assetArgs != null) {
                System.out.println("Asset creating ...");
                String assetId = assetExchangeTransaction.createAsset(assetArgs.assetDescription,
                        assetArgs.assetPrice, assetArgs.assetBelong);
                System.out.println("Creating successfully asset id " + assetId + " belong to user id " +
                        assetArgs.assetBelong);
            }
            if (userArgs != null) {
                System.out.println("User creating ...");
                String userId = assetExchangeTransaction.createUser(userArgs.userName, userArgs.userBalance);
                System.out.println("Creating successfully user id " + userId);
            }
            assetExchangeTransaction.close();

        } else if (mode.equals("exchanging")) {
            System.out.println("Exchanging mode");

            if (exchangeArgs != null) {
                System.out.println("Exchanging asset...");
                assetExchangeTransaction.exchangeAsset(exchangeArgs.assetId, exchangeArgs.fromId, exchangeArgs.toId);
                System.out.println("Exchanging successfully");
            }
            assetExchangeTransaction.close();
        } else {
            System.out.println("Mode (-m) must be specified either creating or exchanging");
        }
        return 0;
    }

    public static void main(String... args) {
        System.exit(new CommandLine(new AssetExchangeMain()).execute(args));
    }
}
