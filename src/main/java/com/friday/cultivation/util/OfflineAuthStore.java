/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  com.mojang.logging.LogUtils
 *  net.minecraftforge.fml.loading.FMLPaths
 *  org.slf4j.Logger
 */
package com.friday.cultivation.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.logging.LogUtils;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import net.minecraftforge.fml.loading.FMLPaths;
import org.slf4j.Logger;

public final class OfflineAuthStore {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FILE_NAME = "offline_auth_users.json";
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int FORMAT_VERSION = 1;
    private static final int ITERATIONS = 120000;
    private static final int SALT_BYTES = 16;
    private static final int HASH_BITS = 256;
    private static final SecureRandom RANDOM = new SecureRandom();
    private static StoreData cached;

    private OfflineAuthStore() {
    }

    public static synchronized boolean isRegistered(String playerName) {
        return OfflineAuthStore.data().users.containsKey(OfflineAuthStore.normalizeName(playerName));
    }

    public static synchronized RegisterResult register(String playerName, String password) {
        String name = OfflineAuthStore.normalizeName(playerName);
        if (OfflineAuthStore.data().users.containsKey(name)) {
            return RegisterResult.ALREADY_REGISTERED;
        }
        try {
            OfflineAuthStore.data().users.put(name, UserRecord.create(password));
            OfflineAuthStore.save();
            return RegisterResult.SUCCESS;
        }
        catch (IOException | GeneralSecurityException e) {
            LOGGER.error("Failed to register offline auth user {}", (Object)name, (Object)e);
            return RegisterResult.STORE_ERROR;
        }
    }

    public static synchronized LoginResult verify(String playerName, String password) {
        String name = OfflineAuthStore.normalizeName(playerName);
        UserRecord record = OfflineAuthStore.data().users.get(name);
        if (record == null) {
            return LoginResult.NOT_REGISTERED;
        }
        try {
            if (!record.matches(password)) {
                return LoginResult.BAD_PASSWORD;
            }
            record.lastLoginAt = Instant.now().toString();
            OfflineAuthStore.save();
            return LoginResult.SUCCESS;
        }
        catch (IOException | IllegalArgumentException | GeneralSecurityException e) {
            LOGGER.error("Failed to verify offline auth user {}", (Object)name, (Object)e);
            return LoginResult.STORE_ERROR;
        }
    }

    public static synchronized ChangePasswordResult changePassword(String playerName, String oldPassword, String newPassword) {
        String name = OfflineAuthStore.normalizeName(playerName);
        UserRecord record = OfflineAuthStore.data().users.get(name);
        if (record == null) {
            return ChangePasswordResult.NOT_REGISTERED;
        }
        try {
            if (!record.matches(oldPassword)) {
                return ChangePasswordResult.BAD_PASSWORD;
            }
            OfflineAuthStore.data().users.put(name, UserRecord.create(newPassword));
            OfflineAuthStore.save();
            return ChangePasswordResult.SUCCESS;
        }
        catch (IOException | IllegalArgumentException | GeneralSecurityException e) {
            LOGGER.error("Failed to change offline auth password for {}", (Object)name, (Object)e);
            return ChangePasswordResult.STORE_ERROR;
        }
    }

    public static synchronized boolean remove(String playerName) {
        String name = OfflineAuthStore.normalizeName(playerName);
        if (OfflineAuthStore.data().users.remove(name) == null) {
            return false;
        }
        try {
            OfflineAuthStore.save();
            return true;
        }
        catch (IOException e) {
            LOGGER.error("Failed to save offline auth user removal for {}", (Object)name, (Object)e);
            return false;
        }
    }

    public static synchronized void reload() {
        cached = null;
        OfflineAuthStore.data();
    }

    public static synchronized int registeredCount() {
        return OfflineAuthStore.data().users.size();
    }

    public static String normalizeName(String playerName) {
        return playerName == null ? "" : playerName.trim().toLowerCase(Locale.ROOT);
    }

    private static StoreData data() {
        if (cached == null) {
            cached = OfflineAuthStore.load();
        }
        return cached;
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private static StoreData load() {
        Path path = OfflineAuthStore.path();
        if (!Files.isRegularFile(path, new LinkOption[0])) {
            return new StoreData();
        }
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8);){
            StoreData loaded = (StoreData)GSON.fromJson((Reader)reader, StoreData.class);
            if (loaded == null) {
                StoreData storeData2 = new StoreData();
                return storeData2;
            }
            if (loaded.users == null) {
                loaded.users = new LinkedHashMap<String, UserRecord>();
            }
            StoreData storeData = loaded;
            return storeData;
        }
        catch (IOException | RuntimeException e) {
            LOGGER.error("Failed to load offline auth users from {}", (Object)path, (Object)e);
            return new StoreData();
        }
    }

    private static void save() throws IOException {
        Path path = OfflineAuthStore.path();
        Files.createDirectories(path.getParent(), new FileAttribute[0]);
        Path tmp = path.resolveSibling("offline_auth_users.json.tmp");
        try (BufferedWriter writer = Files.newBufferedWriter(tmp, StandardCharsets.UTF_8, new OpenOption[0]);){
            GSON.toJson((Object)OfflineAuthStore.data(), (Appendable)writer);
        }
        try {
            Files.move(tmp, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        }
        catch (AtomicMoveNotSupportedException ignored) {
            Files.move(tmp, path, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static Path path() {
        return FMLPaths.CONFIGDIR.get().resolve("friday_cultivation").resolve(FILE_NAME);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static byte[] hash(char[] password, byte[] salt, int iterations) throws GeneralSecurityException {
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, 256);
        try {
            byte[] byArray = SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec).getEncoded();
            return byArray;
        }
        finally {
            spec.clearPassword();
        }
    }

    private static final class StoreData {
        int version = 1;
        Map<String, UserRecord> users = new LinkedHashMap<String, UserRecord>();

        private StoreData() {
        }
    }

    public static enum RegisterResult {
        SUCCESS,
        ALREADY_REGISTERED,
        STORE_ERROR;

    }

    private static final class UserRecord {
        String algorithm;
        int iterations;
        String salt;
        String hash;
        String createdAt;
        String lastLoginAt;

        private UserRecord() {
        }

        static UserRecord create(String password) throws GeneralSecurityException {
            byte[] saltBytes = new byte[16];
            RANDOM.nextBytes(saltBytes);
            char[] chars = password.toCharArray();
            byte[] hashBytes = OfflineAuthStore.hash(chars, saltBytes, 120000);
            UserRecord record = new UserRecord();
            record.algorithm = OfflineAuthStore.ALGORITHM;
            record.iterations = 120000;
            record.salt = Base64.getEncoder().encodeToString(saltBytes);
            record.hash = Base64.getEncoder().encodeToString(hashBytes);
            record.createdAt = Instant.now().toString();
            record.lastLoginAt = null;
            return record;
        }

        boolean matches(String password) throws GeneralSecurityException {
            if (!OfflineAuthStore.ALGORITHM.equals(this.algorithm)) {
                throw new GeneralSecurityException("Unsupported password algorithm: " + this.algorithm);
            }
            byte[] saltBytes = Base64.getDecoder().decode(this.salt);
            byte[] expected = Base64.getDecoder().decode(this.hash);
            byte[] actual = OfflineAuthStore.hash(password.toCharArray(), saltBytes, this.iterations);
            return MessageDigest.isEqual(expected, actual);
        }
    }

    public static enum LoginResult {
        SUCCESS,
        NOT_REGISTERED,
        BAD_PASSWORD,
        STORE_ERROR;

    }

    public static enum ChangePasswordResult {
        SUCCESS,
        NOT_REGISTERED,
        BAD_PASSWORD,
        STORE_ERROR;

    }
}

