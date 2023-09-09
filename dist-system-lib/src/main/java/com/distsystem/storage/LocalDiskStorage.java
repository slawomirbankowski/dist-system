package com.distsystem.storage;

import com.distsystem.api.*;
import com.distsystem.api.enums.CacheStorageType;
import com.distsystem.api.info.CacheObjectInfo;
import com.distsystem.base.CacheStorageBase;
import com.distsystem.interfaces.Cache;
import com.distsystem.utils.DistUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/** cache with local disk - this could be ephemeral
 * this kind of cache should be for larger object without need of often use
 * */
public class LocalDiskStorage extends CacheStorageBase {

    protected static final Logger log = LoggerFactory.getLogger(LocalDiskStorage.class);
    private final String filePrefixName;

    /** init local disk storage */
    public LocalDiskStorage(Cache cache) {
        super(cache);
        filePrefixName = cache.getConfig().getProperty(DistConfig.CACHE_STORAGE_LOCAL_DISK_PREFIX_PATH, "/tmp/");
    }
    /** Local Disk is external storage */
    public  boolean isInternal() { return false; }
    /** returns true if storage is global,
     * it means that one global shared storage is available for all cache instances*/
    public boolean isGlobal() { return false; }
    /** returns true if base file path is folder with write access */
    public boolean isOperable() {
        try {
            return new File(filePrefixName).isDirectory();
        } catch (Exception ex) {
            return false;
        }
    }

    /** get additional info parameters for this storage */
    public Map<String, Object> getStorageAdditionalInfo() {
        return Map.of("filePrefixName", filePrefixName);
    }
    /** get type of this storage */
    public CacheStorageType getStorageType() {
        return CacheStorageType.disk;
    }
    /** check if object has given key, optional with specific type */
    public boolean contains(String key) {
        return getObject(key).isPresent();
    }

    /** read CacheObject from given cache file */
    private Optional<CacheObject> readObjectFromFile(java.io.File f) {
        try {
            java.io.ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f));
            CacheObjectSerialized cos = (CacheObjectSerialized)ois.readObject();
            ois.close();
            CacheObject co = cos.toCacheObject(distSerializer);
            return Optional.of(co);
        } catch (Exception ex) {
            cache.addIssue("LocalDiskStorage.getObject", ex);
            log.info("Cannot deserialize CacheObject from LocalDisk storage, reason: " + ex.getMessage(), ex);
            return Optional.empty();
        }
    }
    /** get item from local disk */
    public Optional<CacheObject> getObject(String key) {
        // try to read object from disk
        try {
            String encodedKey = DistUtils.stringToHex(getKeyEncoder().encodeKey(key));
            String fileEnds = encodedKey + ".cache";
            File[] filesForKey = findFiles(name -> name.endsWith(fileEnds));
            if (filesForKey.length > 0) {
                java.io.File f = filesForKey[0];
                return readObjectFromFile(f);
            } else {
                return Optional.empty();
            }
        } catch (Exception ex) {
            cache.addIssue("LocalDiskStorage.getObject", ex);
            log.info("Cannot deserialize CacheObject from LocalDisk storage, reason: " + ex.getMessage(), ex);
            return Optional.empty();
        }
    }
    /** write object to local disk to be read later */
    public Optional<CacheObject> setObject(CacheObject o) {
        Optional<CacheObject> curr = getObject(o.getKey());
        try {
            String expireDateString = DistUtils.formatDateAsYYYYMMDDHHmmss(new java.util.Date(System.currentTimeMillis() + o.timeToLive()));
            String encodedKeyFileEnd = encodeKeyToFileEnd(o.getKey());
            String cacheObjectFileName = filePrefixName + "tmp.EXPIRE" + expireDateString + ".MODE" + o.getMode().name() + ".SIZE" + o.getSize() + encodedKeyFileEnd;
            // create temporary file with content - object
            java.io.File f = new File(cacheObjectFileName);
            java.io.ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(f));
            oos.writeObject(o.serializedFullCacheObject(distSerializer));
            oos.close();
        } catch (Exception ex) {
            log.warn("Cannot serialize CacheObject to LocalDisk storage, key: " + o.getKey() +  ", class: " + o.getClassName() + ", reason: " + ex.getMessage(), ex);
            cache.addIssue("LocalDiskStorage.setObject", ex);
        }
        return curr;
    }

    /** find files for given name defined by filter function */
    private java.io.File[] findFiles(Function<String, Boolean> filterFunction) {
        try {
            log.trace("Find files on base path: " + filePrefixName);
            java.io.File baseFolder = new File(filePrefixName);
            var files = baseFolder.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return filterFunction.apply(name);
                }
            });
            log.trace("Found files on base path: " + files.length);
            return files;
        } catch (Exception ex) {
            log.warn("Cannot find files in LocalDisk storage, reason: " + ex.getMessage(), ex);
            cache.addIssue("LocalDiskStorage.removeObjectsByKeys", ex);
            return new File[0];
        }
    }
    /** remove objects in cache storage by keys */
    public void removeObjectsByKeys(Collection<String> keys) {
        try {
            List<String> keysEncoded = keys.stream().map(key -> encodeKeyToFileEnd(key)).collect(Collectors.toList());
            File[] filesToRemove = findFiles(x -> keysEncoded.stream().anyMatch(keyEn -> x.endsWith(keyEn)));
            Arrays.stream(filesToRemove).forEach(f -> f.delete());
        } catch (Exception ex) {
            log.info("Cannot remove files for keys in LocalDisk storage, reason: " + ex.getMessage(), ex);
            cache.addIssue("LocalDiskStorage.removeObjectsByKeys", ex);
        }
    }
    /** remove object in cache storage by key */
    public void removeObjectByKey(String key) {
        try {
            String encodedKey = encodeKeyToFileEnd(key);
            File[] filesToRemove = findFiles(x -> x.endsWith(encodedKey));
            Arrays.stream(filesToRemove).forEach(File::delete);
        } catch (Exception ex) {
            log.info("Cannot remove file for key: " + key + " in LocalDisk storage, reason: " + ex.getMessage(), ex);
            cache.addIssue("LocalDiskStorage.removeObjectsByKeys", ex);
        }
    }
    /** get number of items in cache */
    public  int getItemsCount() {
        try {
            File[] files =  findFiles(n -> n.endsWith(".cache"));
            return getObjectsCount() + (int)Arrays.stream(files).mapToLong(File::length).sum() / 1024;
        } catch (Exception ex) {
            log.info("Cannotcount files with cache in LocalDisk storage on base path: " + filePrefixName + ", reason: " + ex.getMessage(), ex);
            return 0;
        }
    }
    /** get number of objects in this cache */
    public int getObjectsCount() {
        return findFiles(n -> n.endsWith(".cache")).length;
    }
    /** get keys for all cache items */
    public Set<String> getKeys(String containsStr) {
        try {
            String enKey = encodeKey(containsStr);
            File[] files =  findFiles(n -> n.contains(enKey) && n.endsWith(".cache"));
            return Arrays.stream(files).map(File::getName).collect(Collectors.toSet());
        } catch (Exception ex) {
            log.info("Cannot get keys for LocalStorage in folder: " + filePrefixName + ", reason: " + ex.getMessage(), ex);
            return Set.of();
        }
    }
    /** get info values */
    public List<CacheObjectInfo> getInfos(String containsStr) {
        return getValues(containsStr).stream().map(CacheObject::getInfo).collect(Collectors.toList());
    }
    /** get values of cache objects that contains given String in key */
    public List<CacheObject> getValues(String containsStr) {
        try {
            String enKey = encodeKey(containsStr);
            File[] files =  findFiles(n -> n.contains(enKey) && n.endsWith(".cache"));
            return Arrays.stream(files).flatMap(f -> readObjectFromFile(f).stream()).collect(Collectors.toList());
        } catch (Exception ex) {
            log.info("Cannot get values for LocalStorage in folder: " + filePrefixName + ", reason: " + ex.getMessage(), ex);
            return new LinkedList<>();
        }
    }
    /** clear caches with given clear cache */
    public int clearCacheForGroup(String groupName) {
        try {
            return -1;
        } catch (Exception ex) {
            log.info("Cannot get values for LocalStorage in folder: " + filePrefixName + ", reason: " + ex.getMessage(), ex);
            return -1;
        }
    }
    /** clear cache contains given partial key */
    public int clearCacheContains(String str) {
        try {
            String keyEn = encodeKey(str);
            File[] files =  findFiles(name -> name.contains(keyEn) && name.endsWith(".cache"));
            Arrays.stream(files).forEach(File::delete);
            return files.length;
        } catch (Exception ex) {
            log.info("Cannot clear cache for LocalStorage in folder: " + filePrefixName + ", reason: " + ex.getMessage(), ex);
            return -1;
        }
    }
    /** clear cache by given mode
     * returns estimated of elements cleared */
    public int clearCache(CacheClearMode clearMode) {
        // TODO: implement clearing folder with cache files for given mode
        return -1;
    }
    /** check cache every X seconds to clear TTL caches */
    public void onTimeClean(long checkSeq) {
        var now = LocalDateTime.now();
        // find all files that should expire now
        File[] files =  findFiles(n -> n.endsWith(".cache") && decodeExpireFromFile(n, now).isAfter(now));
        Arrays.stream(files).forEach(File::delete);
    }
    /** */
    private LocalDateTime decodeExpireFromFile(String cacheFileName, LocalDateTime defaultValue) {
        Arrays.stream(cacheFileName.split("\\."))
                .filter(filePart -> filePart.startsWith("EXPIRE"))
                .findAny()
                .map(filePart -> DistUtils.parseLocalDateTimeFromYYYYMMDDHHmmss(filePart.substring(6), defaultValue))
                .orElseGet(() -> LocalDateTime.now());

        return LocalDateTime.now();
    }
}
