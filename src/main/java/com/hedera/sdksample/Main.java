package com.hedera.sdksample;
import java.io.Console;
import java.util.Map;
import java.util.Scanner;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.HederaException;
import com.hedera.hashgraph.sdk.TransactionReceipt;
import com.hedera.hashgraph.sdk.account.AccountCreateTransaction;
import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PublicKey;

import io.github.cdimascio.dotenv.Dotenv;

public class Main {

	static Client client;
	static Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
	static Scanner s = new Scanner(System.in);
	public static void main(String[] args) {

		client = createHederaClient();
		while (true) {
			System.out.println("*********************************************************************************");
			System.out.println("Welcome to testnet tools:");

			var welcome = "Operating on network: " + dotenv.get("NODE_ADDRESS")
				+ ", Node ID: " + dotenv.get("NODE_ID")
				+ ", Operator: " +dotenv.get("OPERATOR_ID");
			
			System.out.println(welcome);
			
			System.out.println("*********************************************************************************");
			System.out.println("");
			System.out.println("What would you like to do ?");
			System.out.println("0. Exit this utility");
			System.out.println("1. Create a new account");
			System.out.println("2. Top up an existing account");
			System.out.println("3. Get balance for an existing account");
			
			System.out.print("Input your selection: ");		
			String operation = s.nextLine();
			
			System.out.println("");
			
			switch (operation) {
			case "0":
				// exit the utility
				System.out.println("Goodbye.");
				System.exit(0);
			case "1":
				// create new account
				createAccount();
				System.out.println("");
				break;
			case "2":
				// transfer
				transfer();
				System.out.println("");
				break;
			case "3":
				// get balance
				getBalance();
				System.out.println("");
				break;
			default:
				System.out.println("Invalid input, please try again.");
				System.out.println("");
				break;
			}
		}
	}
	
    public static AccountId getNodeId() {
        return AccountId.fromString(dotenv.get("NODE_ID"));
    }

    public static AccountId getOperatorId() {
        return AccountId.fromString(dotenv.get("OPERATOR_ID"));
    }
	
    public static Ed25519PrivateKey getOperatorKey() {
        return Ed25519PrivateKey.fromString(dotenv.get("OPERATOR_KEY"));
    }
    
    public static Client createHederaClient() {
        // To connect to a network with more nodes, add additional entries to the network map
        var nodeAddress = dotenv.get("NODE_ADDRESS");
        var client = new Client(Map.of(getNodeId(), nodeAddress));

        // Defaults the operator account ID and key such that all generated transactions will be paid for
        // by this account and be signed by this key
        client.setOperator(getOperatorId(), getOperatorKey());

        return client;
    }
    
    public static void createAccount() {

		System.out.println("--> Creating an account");
		System.out.println("");
		System.out.print("Public key: ");
		String pubKey = s.nextLine();

		System.out.print("Initial Balance: ");
		String balance = s.nextLine();

		System.out.println("");

		System.out.println("Account creation in progress...");
		
		long initialBalance = Long.parseLong(balance);

		Ed25519PublicKey key = Ed25519PublicKey.fromString(pubKey);
        var tx = new AccountCreateTransaction(client)
            .setKey(key)
            .setInitialBalance(initialBalance);

        // This will wait for the receipt to become available
        TransactionReceipt receipt;
		try {
			receipt = tx.executeForReceipt();
	        var newAccountId = receipt.getAccountId();
	        System.out.println("New account number = " + newAccountId);
		} catch (HederaException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
    public static void transfer() {
    	
		System.out.println("--> Topping up");
		System.out.println("");
		System.out.print("Destination account (0.0.x): ");
		String destAccount = s.nextLine();

		System.out.print("Amount (tinybar): ");
		String amount = s.nextLine();

		System.out.println("");

		System.out.println("Transfer in progress...");
		
		long amountToTransfer = Long.parseLong(amount);
    	
        try {
			client.transferCryptoTo(AccountId.fromString(destAccount), amountToTransfer);
	        System.out.println("transferred " + amount + " tinybar...");
		} catch (IllegalArgumentException | HederaException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
    public static void getBalance() {
		System.out.println("--> Getting balance");
		System.out.println("");
		System.out.print("Account number (0.0.x): ");
		String account = s.nextLine();
		System.out.println("");
		System.out.println("Query in progress...");

        long balance;
		try {
			balance = client.getAccountBalance(AccountId.fromString(account));
	        System.out.println("Balance for " + account + " = " + balance);
		} catch (IllegalArgumentException | HederaException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
