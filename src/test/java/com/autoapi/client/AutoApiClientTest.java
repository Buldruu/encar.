package com.autoapi.client;

import com.autoapi.client.model.OffersParams;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AutoApiClientTest {

    @Test
    void constructor_nullKey_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> new AutoApiClient(null));
    }

    @Test
    void constructor_blankKey_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> new AutoApiClient("   "));
    }

    @Test
    void constructor_validKey_createsInstance() {
        AutoApiClient client = new AutoApiClient("test-key");
        assertNotNull(client);
    }

    @Test
    void offersParams_toQueryString_correctFormat() {
        String qs = new OffersParams()
                .page(2)
                .brand("BMW")
                .yearFrom(2020)
                .toQueryString();

        assertTrue(qs.contains("page=2"));
        assertTrue(qs.contains("brand=BMW"));
        assertTrue(qs.contains("year_from=2020"));
    }

    @Test
    void offersParams_cacheKey_stable() {
        OffersParams p1 = new OffersParams().page(1).brand("BMW");
        OffersParams p2 = new OffersParams().page(1).brand("BMW");
        assertEquals(p1.cacheKey(), p2.cacheKey());
    }

    @Test
    void offersParams_blankValue_skipped() {
        String qs = new OffersParams()
                .page(1)
                .brand("")
                .toQueryString();

        assertFalse(qs.contains("brand"));
    }
}
