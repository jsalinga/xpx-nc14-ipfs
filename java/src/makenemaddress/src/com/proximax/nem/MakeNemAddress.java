/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.proximax.nem;

import io.proximax.xpx.builder.TransferTransactionBuilder;
import io.proximax.xpx.exceptions.ApiException;
import io.proximax.xpx.exceptions.InsufficientAmountException;
import io.proximax.xpx.facade.connection.LocalHttpPeerConnection;
import io.proximax.xpx.facade.connection.PeerConnection;
import io.proximax.xpx.factory.ConnectionFactory;
import io.proximax.xpx.model.GeneratedAccount;
import java.util.concurrent.ExecutionException;
import org.nem.core.crypto.KeyPair;
import org.nem.core.crypto.PrivateKey;
import org.nem.core.crypto.PublicKey;
import org.nem.core.model.Account;
import org.nem.core.model.Address;
import org.nem.core.model.TransferTransaction;
import org.nem.core.model.mosaic.Mosaic;
import org.nem.core.model.mosaic.MosaicId;
import org.nem.core.model.namespace.NamespaceId;
import org.nem.core.model.primitive.Amount;
import org.nem.core.model.primitive.Quantity;

/**
 *
 * @author jsalinga
 */
public class MakeNemAddress {

    public static void main(String args[]) throws Exception {
        if (args.length != 2) {
            System.exit(1);
        }
        String newNem = (new MakeNemAddress(args)).getNewNemAddress();
        System.out.println(newNem);
        System.exit(0);
    }
    private final String[] params;

    public MakeNemAddress(String[] args) {
        this.params = args;
        init();
    }

    public String getNewNemAddress() throws ApiException, InterruptedException, ExecutionException, InsufficientAmountException {
        System.out.println("Making PeerConnection...");
        PeerConnection peerConnection = new LocalHttpPeerConnection(
                ConnectionFactory.createNemNodeConnection("testnet", "http", "104.128.226.60", 7890),
                ConnectionFactory.createIPFSNodeConnection("/ip4/127.0.0.1/tcp/5001"));
        System.out.println("Requesting new NEM Address...");
        GeneratedAccount ga = peerConnection.getNemAccountApi().generateAccount();
        System.out.println("DEBUG:" + ga.getAccount().getAddress());
        String nemAddress = ga.getEncodedAddress();
        String publicKey = ga.getEncodedPublicKey();
        String privateKey = ga.getEncodedPrivateKey();
        //SwingUtilities.invokeLater(() -> {
        // sending 100 XPX
        Mosaic xpxMosaic = new Mosaic(new MosaicId(new NamespaceId("prx"), "xpx"),
                Quantity.fromValue(1000000));
        System.out.println("Mozaic ID: " + xpxMosaic.getMosaicId());
        System.out.println("Public Key: " + publicKey);

        // send 500 XEMs
        final TransferTransaction transferTransaction
                = new TransferTransactionBuilder(peerConnection.getTransactionFeeCalculators())
                        .sender(new Account(new KeyPair(PrivateKey.fromHexString("deaae199f8e511ec51eb0046cf8d78dc481e20a340d003bbfcc3a66623d09763"))))
                        .recipient(new Account(Address.fromPublicKey(PublicKey.fromHexString(publicKey))))
                        .version(2)
                        .amount(Amount.fromNem(1l))
                        .addMosaic(xpxMosaic).buildAndSignTransaction();

        // Announce
        peerConnection.getTransactionSender().sendTransferTransaction(transferTransaction);
        // THEN SEND XEMS
        //send 500 XEMs
        final TransferTransaction transferTransactionXems
                = new TransferTransactionBuilder(peerConnection.getTransactionFeeCalculators())
                        .sender(new Account(new KeyPair(PrivateKey.fromHexString("deaae199f8e511ec51eb0046cf8d78dc481e20a340d003bbfcc3a66623d09763"))))
                        .recipient(new Account(Address.fromPublicKey(PublicKey.fromHexString(publicKey))))
                        .version(2)
                        .amount(Amount.fromNem(500l))
                        .addMosaic(xpxMosaic).buildAndSignTransaction();
        // Announce
        peerConnection.getTransactionSender().sendTransferTransaction(transferTransactionXems);

        System.out.println("New NEM Address:" + nemAddress);
        pgWritter sqlWritter = new pgWritter(params);
        String sqlCmd = "DELETE FROM oc_preferences WHERE "
                + "userid = '" + params[0].trim() + "' AND appid ='settings' "
                + "AND configkey = 'nem';"
                + "INSERT INTO oc_preferences (userid, appid, configkey, "
                + "configvalue) VALUES ('" + params[0].trim() + "','settings','nem','" + nemAddress + "');\n"
                + "DELETE FROM px_nem_addresses WHERE "
                + "userid = '" + params[0].trim() + "';\n"
                + "INSERT INTO px_nem_addresses (userid, nem_address, publickey, "
                + "privatekey) "
                + "VALUES ('" + params[0].trim() + "', '" + nemAddress + "', "
                + "'" + publicKey + "', "
                + "'" + privateKey + "');";
        sqlWritter.suWrite(sqlCmd);
        return nemAddress;

    }

    private void init() {
        String sqlCmd = "CREATE TABLE IF NOT EXISTS px_nem_addresses ("
                + "userid CHARACTER(200) NOT NULL, "
                + "nem_address CHARACTER(250) NOT NULL, "
                + "publickey CHARACTER(250) NOT NULL, "
                + "privatekey CHARACTER(250) NOT NULL) "
                + "WITH (OIDS=TRUE);";
        pgWritter sqlWritter = new pgWritter(params);
        sqlWritter.suWrite(sqlCmd);
    }
}
