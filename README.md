# auto-api-client-java

[![Java](https://img.shields.io/badge/Java-11%2B-blue)](https://openjdk.org)
[![License](https://img.shields.io/badge/license-MIT-green)](LICENSE)
[![GitHub Packages](https://img.shields.io/badge/GitHub-Packages-blue?logo=github)](https://github.com/Buldruu/encar/packages)

Java client for [auto-api.com](https://auto-api.com) with **Firebase Realtime Database** caching support.

Pulls car listings from 8 marketplaces — encar, mobile.de, autoscout24, che168, dongchedi, guazi, dubicars, dubizzle.

Java 11+, single dependency (Gson). Firebase кэш нэмсэн — нэмэлт SDK шаардахгүй, REST API шууд ашиглана.

---

## Installation

### GitHub Packages (Gradle)

```groovy
repositories {
    maven {
        url = 'https://maven.pkg.github.com/Buldruu/encar'
        credentials {
            username = project.findProperty('gpr.user') ?: System.getenv('GITHUB_ACTOR')
            password = project.findProperty('gpr.key')  ?: System.getenv('GITHUB_TOKEN')
        }
    }
}

dependencies {
    implementation 'io.github.buldruu:auto-api-client:2.0.0'
}
```

### GitHub Packages (Maven)

```xml
<repositories>
    <repository>
        <id>github</id>
        <url>https://maven.pkg.github.com/Buldruu/encar</url>
    </repository>
</repositories>

<dependency>
    <groupId>io.github.buldruu</groupId>
    <artifactId>auto-api-client</artifactId>
    <version>2.0.0</version>
</dependency>
```

> **GitHub токен:** `~/.gradle/gradle.properties` файлд `gpr.user=Buldruu` болон `gpr.key=ghp_xxx` нэмнэ.

---

## Usage

### Энгийн хэрэглээ (кэшгүй)

```java
import com.autoapi.client.AutoApiClient;
import com.autoapi.client.model.*;

AutoApiClient client = new AutoApiClient("your-api-key");
```

### Firebase кэштэй хэрэглээ

```java
import com.autoapi.client.AutoApiClient;
import com.autoapi.client.firebase.FirebaseCache;

// Firebase Realtime Database кэш
FirebaseCache cache = new FirebaseCache(
    "https://your-project-default-rtdb.firebaseio.com",
    "your-database-secret",
    3600  // кэш хугацаа секундээр (default: 3600 = 1 цаг)
);

AutoApiClient client = new AutoApiClient("your-api-key", cache);
```

### Filters авах

```java
Map<String, Object> filters = client.getFilters("encar");
```

### Offers хайх

```java
OffersResponse offers = client.getOffers("encar", new OffersParams()
        .page(1)
        .brand("BMW")
        .yearFrom(2020)
        .yearTo(2023)
        .priceFrom(10000)
        .priceTo(50000));

// Pagination
System.out.println(offers.getMeta().getPage());
System.out.println(offers.getMeta().getNextPage());
System.out.println(offers.getMeta().getTotal());
```

### Нэг offer авах

```java
OffersResponse offer = client.getOffer("encar", "40427050");
```

### Өөрчлөлт хянах

```java
int changeId = client.getChangeId("encar", "2025-01-15");
ChangesResponse changes = client.getChanges("encar", changeId);

// Дараагийн batch
ChangesResponse nextBatch = client.getChanges("encar", changes.getMeta().getNextChangeId());
```

### URL-ээр offer авах

```java
Map<String, Object> info = client.getOfferByUrl(
        "https://encar.com/dc/dc_cardetailview.do?carid=40427050");
```

### Offer data задлах

```java
import com.autoapi.client.model.OfferData;
import com.google.gson.Gson;

Gson gson = new Gson();
for (OffersResponse.OfferItem item : offers.getResult()) {
    OfferData d = gson.fromJson(item.getData(), OfferData.class);
    System.out.printf("%s %s %s — $%s%n",
            d.getMark(), d.getModel(), d.getYear(), d.getPrice());
}
```

### Алдаа боловсруулах

```java
import com.autoapi.client.exception.ApiException;
import com.autoapi.client.exception.AuthException;

try {
    OffersResponse offers = client.getOffers("encar", new OffersParams().page(1));
} catch (AuthException e) {
    // 401/403 — буруу API key
    System.out.println(e.getStatusCode() + ": " + e.getMessage());
} catch (ApiException e) {
    // Бусад API алдаа
    System.out.println(e.getStatusCode() + ": " + e.getMessage());
    System.out.println(e.getResponseBody());
}
```

---

## Firebase кэшийн тухай

`FirebaseCache` нь Firebase Realtime Database-ийн REST API-г шууд ашиглана. Нэмэлт Firebase Admin SDK шаардахгүй.

| Тохиргоо | Утга |
|---|---|
| `firebaseUrl` | `https://your-project.firebaseio.com` |
| `databaseSecret` | Firebase Console → Project Settings → Service Accounts → Database secrets |
| `ttlSeconds` | Кэш хэр удаан хадгалагдах (default: 3600 секунд = 1 цаг) |

Firebase дээрх бүтэц:
```
auto-api-cache/
  filters_encar/
    value: {...}
    expiresAt: 1234567890000
  offers_encar_page-1_brand-BMW/
    value: {...}
    expiresAt: 1234567890000
```

---

## Supported sources

| Source | Platform | Region |
|---|---|---|
| `encar` | [encar.com](https://encar.com) | South Korea |
| `mobilede` | [mobile.de](https://mobile.de) | Germany |
| `autoscout24` | [autoscout24.com](https://autoscout24.com) | Europe |
| `che168` | [che168.com](https://che168.com) | China |
| `dongchedi` | [dongchedi.com](https://dongchedi.com) | China |
| `guazi` | [guazi.com](https://guazi.com) | China |
| `dubicars` | [dubicars.com](https://dubicars.com) | UAE |
| `dubizzle` | [dubizzle.com](https://dubizzle.com) | UAE |

---

## Other languages

| Language | Package |
|---|---|
| PHP | [autoapi/client](https://github.com/autoapicom/auto-api-php) |
| TypeScript | [@autoapicom/client](https://github.com/autoapicom/auto-api-node) |
| Python | [autoapicom-client](https://github.com/autoapicom/auto-api-python) |
| Go | [auto-api-go](https://github.com/autoapicom/auto-api-go) |
| C# | [AutoApi.Client](https://github.com/autoapicom/auto-api-dotnet) |

---

## Documentation

[auto-api.com](https://auto-api.com)

## License

[MIT](LICENSE)
