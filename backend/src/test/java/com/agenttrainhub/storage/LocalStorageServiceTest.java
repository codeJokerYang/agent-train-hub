package com.agenttrainhub.storage;

import com.agenttrainhub.common.BizException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LocalStorageServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void storesAndLoadsFileInsideConfiguredBasePath() throws Exception {
        LocalStorageService service = new LocalStorageService(tempDir.toString());
        MockMultipartFile upload = new MockMultipartFile(
                "file", "dataset.csv", "text/csv", "a,b\n1,2\n".getBytes());

        StoredFile stored = service.store(upload, "datasets/42", "dataset.csv");

        assertEquals("datasets/42/dataset.csv", stored.storagePath());
        assertTrue(service.resolve(stored.storagePath()).startsWith(tempDir.toAbsolutePath()));
        assertArrayEquals(upload.getBytes(), Files.readAllBytes(service.resolve(stored.storagePath())));
    }

    @Test
    void rejectsTraversalInDirectoryStoragePathAndFileName() {
        LocalStorageService service = new LocalStorageService(tempDir.toString());
        MockMultipartFile upload = new MockMultipartFile("file", "x.txt", "text/plain", new byte[]{1});

        assertThrows(BizException.class, () -> service.store(upload, "../outside", "x.txt"));
        assertThrows(BizException.class, () -> service.store(upload, "datasets", "../outside.txt"));
        assertThrows(BizException.class, () -> service.resolve("../../outside.txt"));
        assertThrows(BizException.class, () -> service.resolve(tempDir.resolve("absolute.txt").toString()));
    }

    @Test
    void rejectsBlankStoragePaths() {
        LocalStorageService service = new LocalStorageService(tempDir.toString());

        assertThrows(BizException.class, () -> service.resolve(""));
        assertThrows(BizException.class, () -> service.resolve("  "));
    }
}
