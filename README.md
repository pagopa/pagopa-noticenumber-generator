# pagopa-noticenumber-generator

A Java library to generate **PagoPA Notice Numbers (NAV)** and **IUV (Identificativo Unico di Versamento)** compliant with AGID specifications.

It ensures distributed IUV uniqueness per Creditor Institution using a **Redis**-based locking mechanism with automatic retries.

## 🚀 Features
* **AGID Compliant IUV**: 17-digit string composed of Segregation Code (2n) + Timestamp Base (13n) + Modulo 93 Check Digit (2n).
* **Notice Number (NAV)**: 18-digit string prefixed with Aux Digit `3`.
* **Distributed Uniqueness**: Native `StringRedisTemplate` integration to prevent collisions in clustered environments.

## 🛠️ Requirements
* Java 17 or higher
* Spring Boot 3.2.x or higher
* Active Redis instance

## ⚙️ Configuration

Add the following properties to your Spring Boot `application.yml` to configure both the library and the required Redis connection:

```yaml
spring:
  data:
    redis:
      host: localhost             # Redis server host
      port: 6379                  # Redis server port
      password: ""                # Redis server password (leave empty if none)
      timeout: 2000ms             # Connection timeout

notice:
  number:
    aux-digit: 3                 # Fixed Aux Digit for this generation type (Aux Digit 3)
    segregation-code: 12         # 2-digit institution segregation code
    redis-key-prefix: "iuv:lock:" # Redis lock key prefix to avoid collisions
    lock-ttl: 2m                 # Redis lock duration (e.g., 2m or PT2M)
    max-retries: 3               # Max automatic attempts in case of IUV collision
