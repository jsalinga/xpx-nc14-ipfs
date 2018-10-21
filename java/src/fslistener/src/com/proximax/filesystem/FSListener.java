/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.proximax.filesystem;

import io.proximax.xpx.builder.TransferTransactionBuilder;
import io.proximax.xpx.exceptions.ApiException;
import io.proximax.xpx.exceptions.InsufficientAmountException;
import io.proximax.xpx.facade.connection.LocalHttpPeerConnection;
import io.proximax.xpx.facade.connection.PeerConnection;
import io.proximax.xpx.facade.upload.Upload;
import io.proximax.xpx.facade.upload.UploadException;
import io.proximax.xpx.facade.upload.UploadFileParameter;
import io.proximax.xpx.facade.upload.UploadResult;
import io.proximax.xpx.factory.ConnectionFactory;
import java.io.File;
import java.io.FileInputStream;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;

import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
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

public class FSListener {

    private final pgConnector sqlRunner;
    private final pgWritter sqlWritter;
    private final FileSystem fileSystem = FileSystems.getDefault();
    private final WatchService watchService;
    private final Map<WatchKey, Path> wathcKeys = new ConcurrentHashMap<>();
    private ExecutorService service;
    private String ncDataPath;
    private ArrayList UIDs;
    private final String[] params;

    @SuppressWarnings("unchecked")
    private <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>) event;
    }

    public static void main(final String[] args) throws Exception {
        if (args.length != 2) {
            System.out.println("Wrong parameter received.");
            System.out.println("Usage: java -jar fslistner.jar /nextcloud/data/path/to/listen pgDatabaseName");
            System.exit(0);
        }
        FSListener fsListener = new FSListener(args);
        if (fsListener.init()) {
            fsListener.start();
        }
    }

    public FSListener(String[] params) throws IOException {
        this.params = params;
        watchService = fileSystem.newWatchService();
        sqlRunner = new pgConnector(params[1]);
        sqlWritter = new pgWritter(params[1]);
    }

    private void listFiles(Path path) throws IOException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
            for (Path entry : stream) {
                if (Files.isDirectory(entry)) {
                    listFiles(entry);
                    reg(entry, wathcKeys, watchService);
                }
            }
        }
    }

    private void walk(Path root, final Map<WatchKey, Path> keys,
            final WatchService ws) throws IOException {
        Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir,
                    BasicFileAttributes attrs) throws IOException {
                reg(dir, keys, ws);
                return super.preVisitDirectory(dir, attrs);
            }
        });
    }

    private void reg(Path dir, Map<WatchKey, Path> keys, WatchService ws)
            throws IOException {
        WatchKey key = dir.register(ws, ENTRY_CREATE);
        keys.put(key, dir);
    }

    public void listenTo(final String ncDataPath) throws IOException {
        this.ncDataPath = ncDataPath;
        boolean userIdsLoad = loadUserIDs();
        if (userIdsLoad) {
            service = Executors.newCachedThreadPool();
            SwingWorker sw = new SwingWorker() {
                @Override
                protected Object doInBackground() throws Exception {
                    reg(Paths.get(ncDataPath), wathcKeys, watchService);
                    listFiles(Paths.get(ncDataPath));
                    return null;
                }
            };
            sw.execute();
            service.submit(new Runnable() {
                @Override
                public void run() {
                    System.out.println("Started listening at Folder " + ncDataPath);
                    while (Thread.interrupted() == false) {
                        WatchKey key;
                        try {
                            key = watchService.poll(10, TimeUnit.MILLISECONDS);
                        } catch (InterruptedException | ClosedWatchServiceException e) {
                            Logger.getLogger(FSListener.class.getName()).log(Level.INFO, null, e);
                            break;
                        }
                        if (key != null) {
                            final Path path = wathcKeys.get(key);
                            for (WatchEvent<?> i : key.pollEvents()) {
                                WatchEvent<Path> event = cast(i);
                                WatchEvent.Kind<Path> kind = event.kind();
                                Path name = event.context();
                                final Path child = path.resolve(name);
                                System.out.printf("%s: %s %s%n",
                                        kind.name(), path, child);
                                SwingUtilities.invokeLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            processIPFS(child.toString());
                                        } catch (SQLException | IOException | UploadException ex) {
                                            Logger.getLogger(FSListener.class.getName()).log(Level.SEVERE, null, ex);
                                        }
                                    }

                                });
                                if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                                    if (Files.isDirectory(child, LinkOption.NOFOLLOW_LINKS)) {
                                        try {
                                            walk(child, wathcKeys, watchService);
                                        } catch (IOException e) {
                                            Logger.getLogger(FSListener.class.getName()).log(Level.INFO, null, e);
                                        }
                                    }
                                }
                            }
                            if (key.reset() == false) {
                                System.out.printf("%s is invalid %n", key);
                                wathcKeys.remove(key);
                                if (wathcKeys.isEmpty()) {
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            );
        }
    }

    private void processIPFS(final String filePath) throws SQLException, IOException, UploadException {
        if (filePath.contains(".part") || filePath.contains("_trashbin")
                || filePath.contains("_versions")) {
            System.out.println("Skipping " + filePath + " for NEM Hashing.");
            return;
        }
        //final SwingWorker w = new SwingWorker() {
        //@Override
        //protected Object doInBackground() throws Exception {
        loadUserIDs();
        if (UIDs != null) {
            for (int j = 0; j < UIDs.size(); j++) {
                String uidPath = (String) UIDs.get(j);
                if (uidPath.equals(filePath.substring(0, uidPath.length()))) {
                    String uid = uidPath.replaceAll(
                            File.separator + "files", "");
                    uid = uid.substring(uid.lastIndexOf(File.separator) + 1);
                    String fPath = filePath.substring(
                            uidPath.length());
                    fPath = "files" + fPath;
                    while (true) {
                        String sqlCmd = "select a.fileid\n"
                                + "from oc_filecache a\n"
                                + "left join oc_storages b on a.storage = b.numeric_id\n"
                                + "where b.id = 'home::" + uid.trim() + "' "
                                + " and a.path = '" + fPath + "';";
                        ResultSet sqlResult = sqlRunner.suQuery(sqlCmd);
                        if (sqlResult != null) {
                            if (sqlResult.next()) {
                                int fileid = sqlResult.getInt("fileid");
                                String[] hashes = uploadFile(filePath, uid);
                                if (hashes != null) {
                                    sqlCmd = "insert into px_nem_hash"
                                            + "(oc_filecache_id, uid, fullpath, nemhash, ipfs) "
                                            + "values (" + fileid + ","
                                            + "'" + uid.trim() + "', "
                                            + "'" + filePath + "',"
                                            + "'" + hashes[0] + "',"
                                            + "'" + hashes[1] + "');";

                                } else {
                                    sqlCmd = "insert into px_nem_hash"
                                            + "(oc_filecache_id, uid, fullpath) "
                                            + "values (" + fileid + ","
                                            + "'" + uid.trim() + "', "
                                            + "'" + filePath + "');";

                                }
                                sqlWritter.suWrite(sqlCmd);
                                break;
                            } else {
                                System.out.println("waiting for a file caching to complete....");
                            }
                        }
                    }
                    break;
                }
            }
        }
        //return null;
        //}
        //};
        //w.execute();

    }

    private boolean loadUserIDs() {
        boolean retVal = false;
        String sqlCmd = "select uid from oc_users;";
        ResultSet sqlResult = sqlRunner.suQuery(sqlCmd);
        if (sqlResult != null) {
            UIDs = new ArrayList();
            try {
                while (sqlResult.next()) {
                    String uid = String.valueOf(sqlResult.getString("uid")).trim();
                    String pathToListen = (this.ncDataPath + File.separator
                            + uid + File.separator + "files")
                            .replaceAll(File.separator + File.separator,
                                    File.separator);
                    UIDs.add(pathToListen);
                }
                retVal = true;
            } catch (SQLException ex) {
                Logger.getLogger(FSListener.class.getName()).log(Level.SEVERE, null, ex);
            }
            sqlRunner.closeResult(sqlResult);
        }
        return retVal;
    }

    private String[] uploadFile(String filePath, String uid) throws IOException, UploadException {
        String ipfsHash = null;
        String nemHash = null;
        System.out.println("Getting public and private keys of account name: " + uid.trim());
        String sqlCmd = "select * from px_nem_addresses "
                + "where userid = '" + uid.trim() + "';";
        String privatekey = null, publickey = null, nemAddress = null;

        boolean canCreate = true;
        ResultSet sqlResult = sqlRunner.suQuery(sqlCmd);
        if (sqlResult != null) {
            try {
                if (sqlResult.next()) {
                    privatekey = sqlResult.getString("privatekey").trim();
                    publickey = sqlResult.getString("publickey").trim();
                    nemAddress = sqlResult.getString("nem_address").trim();
                } else {
                    System.out.println("Getting public and private keys of account name: " + uid.trim() + " does not exists!");
                    canCreate = false;
                }
            } catch (SQLException ex) {
                Logger.getLogger(FSListener.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            System.out.println("Getting public and private keys of account name: " + uid + " has failed!");
            canCreate = false;
        }
        sqlRunner.closeResult(sqlResult);
        if (canCreate) {
            FileInputStream fileInputStream = null;
            System.out.println("Creating PeerConnection Class...");
            PeerConnection peerConnection = new LocalHttpPeerConnection(
                    ConnectionFactory.createNemNodeConnection("testnet", "http", "104.128.226.60", 7890),
                    ConnectionFactory.createIPFSNodeConnection("/ip4/127.0.0.1/tcp/5001"));
            System.out.println("Creating Upload Class...");
            Upload upload = new Upload(peerConnection);
            File file = new File(filePath);
            byte[] b = new byte[(int) file.length()];
            fileInputStream = new FileInputStream(file);
            fileInputStream.read(b);
            System.out.println("Preparing upload parameter..");
            System.out.println("Using NEM Address: " + nemAddress + " of \"" + uid + "\"");
            System.out.println("Using Private Key: " + privatekey + " for account \"" + uid + "\"");
            System.out.println("Using Public Key: " + publickey + " for account \"" + uid + "\"");
            if (send100XPX(peerConnection, nemAddress, publickey, privatekey)) {
                UploadFileParameter parameter = UploadFileParameter.create()
                        .senderPrivateKey(privatekey)
                        .receiverPublicKey(publickey)
                        .file(file)
                        .securedWithNemKeysPrivacyStrategy()
                        .build();
                UploadResult result = upload.uploadFile(parameter);
                ipfsHash = result.getIpfsHash();
                nemHash = result.getNemHash();
                System.out.println("IPFS Hash: " + ipfsHash);
                System.out.println("NEM Hash: " + nemHash);
            } else {
                System.out.println("Failed to send 100 XPX into " + publickey);
            }
            fileInputStream.close();
        }

        return (new String[]{nemHash, ipfsHash});
    }

    private boolean send100XPX(PeerConnection peerConnection, String nemAddress, String publickey, String privatekey) {
        boolean retVal = false;
        try {
            Mosaic xpxMosaic = new Mosaic(new MosaicId(new NamespaceId("prx"), "xpx"),
                    Quantity.fromValue(1000000));

            // send 500 XEMs
            final TransferTransaction transferTransaction
                    = new TransferTransactionBuilder(peerConnection.getTransactionFeeCalculators())
                            .sender(new Account(new KeyPair(PrivateKey.fromHexString("deaae199f8e511ec51eb0046cf8d78dc481e20a340d003bbfcc3a66623d09763"))))
                            .recipient(new Account(Address.fromPublicKey(PublicKey.fromHexString(publickey))))
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
                            .recipient(new Account(Address.fromPublicKey(PublicKey.fromHexString(publickey))))
                            .version(2)
                            .amount(Amount.fromNem(500l))
                            .addMosaic(xpxMosaic).buildAndSignTransaction();
            // Announce
            peerConnection.getTransactionSender().sendTransferTransaction(transferTransactionXems);
            retVal = true;
        } catch (ApiException | InterruptedException | ExecutionException | InsufficientAmountException ex) {
            Logger.getLogger(FSListener.class.getName()).log(Level.SEVERE, null, ex);
        }
        return retVal;
    }

    private void start() throws IOException {
        listenTo(params[0]);
    }

    private boolean init() {
        String sqlCmd = "CREATE TABLE IF NOT EXISTS px_nem_addresses ("
                + "userid CHARACTER(64) NOT NULL, "
                + "nem_address CHARACTER(250) NOT NULL, "
                + "publickey CHARACTER(250) NOT NULL, "
                + "privatekey CHARACTER(250) NOT NULL);\n"
                + "CREATE TABLE IF NOT EXISTS px_nem_hash"
                + "(oc_filecache_id INTEGER, uid CHARACTER(64), "
                + "fullpath TEXT, "
                + "nemhash CHARACTER(200), "
                + "ipfs CHARACTER(200));";
        return sqlWritter.suWrite(sqlCmd);
    }

}
