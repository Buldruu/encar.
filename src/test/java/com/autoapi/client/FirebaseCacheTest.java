package com.autoapi.client;

import com.autoapi.client.firebase.FirebaseCache;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FirebaseCacheTest {

    @Test
    void constructor_blankUrl_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> new FirebaseCache("", "secret"));
    }

    @Test
    void constructor_nullUrl_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> new FirebaseCache(null, "secret"));
    }

    @Test
    void constructor_validParams_createsInstance() {
        FirebaseCache cache = new FirebaseCache(
                "https://my-project.firebaseio.com", "secret");
        assertNotNull(cache);
    }

    @Test
    void get_nonExistentKey_returnsNull() {
        // Хэрэв Firebase холболт байхгүй бол null буцаах ёстой (алдаа дарагдана)
        FirebaseCache cache = new FirebaseCache(
                "https://nonexistent-project-12345.firebaseio.com", "secret");
        assertNull(cache.get("test_key"));
    }
}
