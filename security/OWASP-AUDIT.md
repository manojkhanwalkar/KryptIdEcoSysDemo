# OWASP Top 10 (2025) Audit — KryptIdEcoSysDemo

_Date: 2026-10-26 · Scope: whole repository · Commit: bf109d3 · Tree: clean_

## System summary
A Java 11 / Dropwizard 1.3 demo of a decentralised identity ecosystem. Six HTTPS services: **Discovery** (service registry and health checks), **INO** (identity verifier: issues signed Merkle-tree "verified claims", stores them encrypted in DynamoDB Local), **SP** (service provider: takes a claim and asks a credit bureau), **Bureau** (returns credit scores), **ClientProxy** (the user's wallet and agent, keeping RSA keys in DynamoDB) and **Monitor**. Services talk through a custom protocol: ECDH key exchange (`/keyexchange`, the peer sends any X.509 cert plus a self-chosen `clientId`), then AES-CBC messages signed with ECDSA and JWS. The INO `/verify` call also carries an HMAC header and a single-use nonce. Assets: PII in claims (name, SSN, e-mail, birth date, address), claim-signing and service private keys, wallet RSA keys, the shared HMAC key. Every endpoint is reachable by anyone who can reach the port; there is no gateway. This is sample code, rated as if deployed as written.

## Summary
| Category | Status | Findings |
|---|---|---|
| A01 Broken Access Control | Issues | C:0 H:2 M:0 L:0 |
| A02 Security Misconfiguration | Issues | C:0 H:0 M:3 L:0 |
| A03 Software Supply Chain Failures | Issues | C:0 H:1 M:0 L:0 |
| A04 Cryptographic Failures | Issues | C:0 H:1 M:3 L:0 |
| A05 Injection | No issues found | C:0 H:0 M:0 L:0 |
| A06 Insecure Design | Issues | C:1 H:1 M:1 L:1 |
| A07 Authentication Failures | Issues | C:1 H:0 M:0 L:0 |
| A08 Software or Data Integrity Failures | Issues | C:0 H:0 M:1 L:0 |
| A09 Security Logging and Alerting Failures | Issues | C:0 H:0 M:1 L:0 |
| A10 Mishandling of Exceptional Conditions | Issues | C:0 H:1 M:0 L:1 |

Totals: **Critical 2 · High 6 · Medium 9 · Low 2 · Info 1**.

The design has the right building blocks: signatures, ECDH, nonces, HMAC, Merkle proofs and a cert-chain validator. But almost every check ran and then had its result ignored, or it failed open. The signature checks, claim validation, HMAC and nonce checks did not stop anything, and the crypto helpers fell back to plaintext. Fix first: (1) enforce message signatures and bind them to the session, (2) reject forged claims by pinning the claim issuer, and (3) make encryption and decryption fail closed. All three are fixed in this change. The biggest remaining gap is that peers are never authenticated against a trust anchor (A01/A07, deferred).

## Remediation
| Finding | Severity | Status | Notes |
|---|---|---|---|
| A07-1 Signature checks fail open, not bound to session | Critical | Fixed | `KeyExchangeManager`, `Connection`, `SignUtil` |
| A06-1 Forged claims accepted by SP/Bureau | Critical | Fixed | issuer pinned in `ClaimValidator` |
| A10-1 Crypto helpers fail open | High | Fixed | `DHUtil` throws instead of returning plaintext or ciphertext |
| A06-2 HMAC and nonce on INO `/verify` not enforced | High | Fixed | 401 on bad or missing HMAC or nonce; client fetches a nonce automatically |
| A04-1 Secrets committed (HMAC key, private keys, keystore) | High | Partially fixed | HMAC key moved to `KRYPTID_HMAC_KEY`; keys and keystore still need re-issuing and rotation |
| A01-1 Unauthenticated discovery registration | High | Partially fixed | https and host allow-list (`KRYPTID_DISCOVERY_ALLOWED_HOSTS`); registrations are still unauthenticated |
| A01-2 No peer authentication or authorization on INO claim operations | High | Deferred | needs a trust-model decision |
| A03-1 Outdated dependencies with known CVEs | High | Fixed | bumped within the same major versions |
| A08-1 Unrestricted Jackson default typing | Medium | Fixed | allow-list `PolymorphicTypeValidator`; wire format unchanged |
| A02-1 Hostname verification disabled JVM-wide | Medium | Fixed | now opt-in with `KRYPTID_DISABLE_HOSTNAME_VERIFICATION=true` |
| A02-2 ClientProxy delete endpoints on all interfaces | Medium | Fixed | `bindHost: 127.0.0.1` |
| A02-3 Admin connectors open on all interfaces | Medium | Not fixed (recommendation) | Discovery health checks need to reach them |
| A04-2 AES/ECB and RSA PKCS#1 v1.5 claim envelope | Medium | Deferred | changes the wire and stored-data format |
| A04-3 Unsalted Merkle leaves | Medium | Deferred | changes the claim data model |
| A04-4 Wallet private keys stored unencrypted | Medium | Deferred | needs a key-protection decision |
| A06-3 Claim expiry neither signed nor checked | Medium | Deferred | enforcing it would break current flows |
| A09-1 PII and key material in logs | Medium | Fixed | removed or redacted |
| A10-2 NPEs and 500s on error paths | Low | Fixed | SP, Discovery, HealthChecker |
| A06-4 Unauthenticated `/nonce1` writes to the DB | Low | Not fixed (recommendation) | demo endpoint |
| A02-I Hard-coded absolute paths | Info | Not fixed | |

**Verification:**
- Baseline (JDK 25 with Maven 3.9.9 from the wrapper cache): `mvn clean test` gave BUILD SUCCESS with **0 tests run**. The existing `*Tester` classes do not match surefire's naming pattern.
- After the fixes: `mvn clean test` gives BUILD SUCCESS with **13/13 tests passing** in the new `src/test/java/util/SecurityRegressionTest.java`. The tests cover:
  - signature rejected when missing, tampered, from a foreign cert or from an unknown session;
  - a `clientId` cannot be taken over by another cert;
  - forged and tampered claims rejected, trusted claim accepted;
  - decryption fails closed with the wrong key or plaintext input;
  - constant-time HMAC check;
  - the type validator denies gadget classes while Merkle claims still round-trip through JSON;
  - the discovery URL allow-list.
- The regression tests caught one real bug during fixing: a static-initialisation-order NPE in `JSONUtil`, which would have stopped every service from starting.
- Live probe: the Discovery service was started on Dropwizard 1.3.29 / Jackson 2.10.5 (localhost, scratch config) and then stopped.
  - A rogue registration (`https://attacker.example`, health URL `http://169.254.169.254/`) returned **403**.
  - A legitimate registration returned **200**, and the lookup returned the registered URL.
  - An unknown service type returned **404** instead of a 500.
- INO, SP, Bureau and ClientProxy could not be started here. They load keys from `/home/manoj/IdeaProjects/KryptIdEcoSysDemo/...` and need DynamoDB Local on :8000, so their end-to-end flow is **not** verified. Run the demo once before merging.

**Breaking changes for operators:**
- `KRYPTID_HMAC_KEY` (at least 32 chars, or system property `kryptid.hmac.key`) must be set on INO and on every client (ClientProxy, ClientVerifier). Without it INO `/verify` returns 503 (fail closed).
- INO `/verify` now requires a valid single-use nonce. `INOConnector.verify` fetches one automatically when the caller does not set it.
- Hostname verification is now on. The bundled TLS cert (CN=ROOT, no IP SAN) will not match `172.17.0.1`. Re-issue it, or for local dev only set `KRYPTID_DISABLE_HOSTNAME_VERIFICATION=true`.
- Discovery only accepts `https` service and health URLs on hosts in `KRYPTID_DISCOVERY_ALLOWED_HOSTS` (default `172.17.0.1,localhost,127.0.0.1`).
- ClientProxy (8780/8781) now listens on 127.0.0.1 only.
- SP and Bureau reject claims not signed by `claimkeycertificate.pem`. Requests with a bad signature or session get 401, a foreign `clientId` takeover gets 403, and undecryptable bodies get 400.
- Dependency upgrades: Dropwizard 1.3.14→1.3.29, Jackson →2.10.5(.1), Guava 19→30.1-jre, commons-io 2.6→2.14.0, BouncyCastle 1.64→1.70, jose4j 0.7.0→0.9.6, JUnit 4.12→4.13.2; httpclient pinned to 4.5.14 and logback to 1.2.13.

**Follow-ups:**
- Treat every private key under `src/main/resources/*.der`, `mytestkey.jks` and the old hard-coded HMAC key as compromised. They are in git history and in every clone. Re-issue and rotate them, and keep new keys out of the repo. Rewriting history is the owner's decision.
- Decide on a peer trust model (A01-2).
- Move the claim envelope to AES-GCM + RSA-OAEP with a format version (A04-2).
- Salt the Merkle leaves (A04-3).
- Protect wallet keys (A04-4).
- Restrict the admin ports (A02-3).
- Rename the old `*Tester` classes so CI runs them, or keep them as manual tools.

## Findings

### A01 — Broken Access Control

#### [High] Unauthenticated service registration lets anyone hijack INO/SP/Bureau routing and drive SSRF (Confirmed)
**Status:** Partially fixed. `/register` and `/deregister` now accept only `https` URLs on allow-listed hosts (no userinfo), and the live probe returned 403 for a rogue URL. Registrations are still unauthenticated, so an attacker who can reach the port and a permitted host can still deregister services (DoS).
- **Location:** `src/main/java/services/discovery/DiscoveryResource.java:80`, `src/main/java/services/discovery/HealthChecker.java:80-86`
- **Evidence:**
  ```java
  public String register(ServiceInfo serviceInfo) {
      serviceStatusCache.register(serviceInfo);   // no auth, no URL check
  ```
- **Scenario:** An attacker POSTs `{"serviceType":"Bureau","url":"https://evil/"}`. SP's `/transact` resolves the bureau through discovery and fetches the attacker's `claimkey`. It then encrypts the user's verified claim (PII) to the attacker. The health URLs registered this way are fetched by Discovery, which is SSRF to internal hosts or cloud metadata.
- **Fix:** Authenticate registrations: mTLS or a signed registration, with service identity tied to a certificate. Until then, keep the host allow-list.

#### [High] Any peer can list, read and delete every claim; peers are never authenticated (Confirmed)
**Status:** Deferred (needs decision). This changes the trust model.
- **Location:** `src/main/java/util/KeyExchangeManager.java:48` (any cert accepted), `src/main/java/services/INO/INOResource.java` `/list`, `/claims`, `/claim`, `/deleteclaim`, `/deleteclaims`, `/nonce`
- **Evidence:** `PublicKey pubKey = CertUtil.getCertFromString(responseCertStr).getPublicKey();` takes any self-signed cert. The existing `CertChainValidator` is never used server side.
- **Scenario:** An attacker generates a self-signed EC cert, completes `/keyexchange` and calls `/deleteclaims`, wiping every user's claims. They can also enumerate all claim ids and download every encrypted claim.
- **Fix options:**
  - (a) Validate peer certs against a demo CA with `CertChainValidator`. The CA files `/home/manoj/ca/...` are not in the repo.
  - (b) Pin known client certs per service.
  - (c) Bind claims to the wallet key that requested them and require proof of possession for read and delete.
  - (a) together with (c) is the recommended combination.

### A02 — Security Misconfiguration

#### [Medium] TLS hostname verification disabled for the whole JVM (Confirmed)
**Status:** Fixed. It is now only disabled when `KRYPTID_DISABLE_HOSTNAME_VERIFICATION=true`.
- **Location:** `src/main/java/services/client/Connection.java:68`
- **Evidence:** `props.setProperty("jdk.internal.httpclient.disableHostnameVerification", "true");` runs in every `Connection` constructor.
- **Scenario:** Any certificate trusted by the JVM can impersonate INO, SP, Bureau or Discovery. Combined with the unauthenticated protocol peers, this is a full MITM of claim traffic.
- **Fix:** Keep verification on, and issue service certs with the correct SAN.

#### [Medium] ClientProxy exposes unauthenticated destructive wallet endpoints on all interfaces (Plausible)
**Status:** Fixed. `bindHost: 127.0.0.1` is set on both connectors in `ClientProxy.yml`.
- **Location:** `src/main/java/services/clientproxy/ClientProxyResource.java` `/deletewallet`, `/deleteclaims`, `/deletewalletkey`, `/claims`; `src/main/resources/ClientProxy.yml:8`
- **Scenario:** Anyone on the network can call `GET /deletewallet`, which destroys the user's wallet keys. Their claims then become undecryptable. `GET /claims` returns the user's decrypted claims (PII).
- **Fix:** Bind to localhost (done). For remote use, add authentication. This is Plausible because it depends on whether the host is reachable.

#### [Medium] Dropwizard admin connectors open on all interfaces (Plausible)
**Status:** Not fixed (recommendation). Discovery health checks call `https://172.17.0.1:8x81/healthcheck`, so binding to localhost would break them.
- **Location:** `src/main/resources/{INO,SP,Bureau,discovery,monitor}.yml` `adminConnectors`
- **Scenario:** The admin port exposes metrics, thread dumps and tasks (`/tasks/log-level`, `/tasks/gc`) without authentication.
- **Fix:** Bind admin to the docker bridge or localhost and firewall it, or expose only `/healthcheck` through the application connector.

### A03 — Software Supply Chain Failures

#### [High] Outdated dependencies with known CVEs (Plausible)
**Status:** Fixed with bumps inside the same major lines. Built and tested; the Discovery service booted.
- **Location:** `pom.xml`
- **Evidence:** jackson-databind 2.10.0 (CVE-2020-25649, gadget CVEs), Dropwizard 1.3.14 (Jetty 9.4.18: several CVEs), logback 1.2.3 (CVE-2021-42550, CVE-2023-6378), httpclient 4.5.5 (CVE-2020-13956), guava 19.0 (CVE-2018-10237, CVE-2020-8908, CVE-2023-2976), commons-io 2.6 (CVE-2021-29425), bcprov 1.64 (CVE-2020-15522), jose4j 0.7.0 (CVE-2023-31582, CVE-2023-51775), junit 4.12 (CVE-2020-15250).
- **Fix:** Dropwizard 1.3.29 (Jetty 9.4.35); Jackson BOM 2.10.5.20201202 (databind 2.10.5.1); logback 1.2.13; httpclient 4.5.14; guava 30.1-jre; commons-io 2.14.0; bcprov/bcpkix 1.70; jose4j 0.9.6; junit 4.13.2. The duplicate `jaxb-api` entry was removed.
- **Remaining:** Dropwizard 1.3 is end of life. Jetty 9.4.35, snakeyaml 1.26 (config is local and trusted) and Jackson 2.10 still carry later CVEs that are only fixed in newer majors. `aws-java-sdk` 1.11.327 is the full bundle; only DynamoDB is used. Plan a Dropwizard 2.x/4.x migration and switch to `aws-java-sdk-dynamodb`. No dependency scanner was run (none installed), so this list comes from a manual review.

### A04 — Cryptographic Failures

#### [High] Secrets committed to the repository (Confirmed)
**Status:** Partially fixed. The HMAC key is no longer in source: it is read from `KRYPTID_HMAC_KEY` (at least 32 chars), and INO fails closed without it. The private keys and the keystore are deferred: they must be re-issued and rotated, and both stay in git history.
- **Location:** `src/main/java/services/INO/INOResource.java:84` and `src/main/java/services/client/Connection.java:216` (`HmacKey = "****"`, 27 chars); `src/main/resources/*.der` (13 unencrypted PKCS#8 private keys: INO/SP/Bureau/client/claim-signing/DB/user); `src/main/resources/mytestkey.jks` with `keyStorePassword: ****` in every `*.yml`.
- **Scenario:** Anyone with the repo can:
  - sign claims as the INO (`claimkey.der`);
  - decrypt every stored claim (`dbrsakey.der`);
  - decrypt claims sent to SP or Bureau (`sprsakey*.der`, `bureaursakey.der`);
  - forge the `/verify` HMAC.

  The TLS cert in the keystore is also a 1024-bit RSA / SHA1 self-signed cert that expired in 2020.
- **Fix:** Generate new keys outside the repo, load paths and passwords from configuration or a secret store, rotate the HMAC key, and add the key files to `.gitignore`.

#### [Medium] Claim envelope and data at rest use AES/ECB and RSA PKCS#1 v1.5 (Confirmed)
**Status:** Deferred (needs decision). Changing the cipher changes the wire format between INO, client, SP and Bureau, and makes the existing DynamoDB records unreadable.
- **Location:** `src/main/java/util/CryptUtil.java:131,135,174,180,206`
- **Evidence:** `Cipher.getInstance("AES")` (provider default `AES/ECB/PKCS5Padding`), `Cipher.getInstance("RSA/ECB/PKCS1Padding")`
- **Scenario:** ECB leaks equal plaintext blocks and offers no integrity: blocks of a stored claim can be cut and pasted. PKCS#1 v1.5 is open to Bleichenbacher-style oracles wherever decryption errors are observable.
- **Fix:** Use AES-256-GCM with a random 12-byte IV and RSA-OAEP-SHA256, add a format/version field, and migrate the stored records.

#### [Medium] Unsalted Merkle leaves let anyone brute-force redacted attributes (Confirmed)
**Status:** Deferred (needs decision). This changes the claim data model.
- **Location:** `src/main/java/util/MerkleTreeUtil.java:300`
- **Evidence:** `sha2HexValue = getSHA2HexValue(root.getLeft().getName()+root.getLeft().getValue());`
- **Scenario:** In a partial claim sent to an SP, the hidden `SSN` leaf hash is `SHA-256("SSN"+value)`. About 10^9 guesses recover it. Birth date, zip and names are cheaper still.
- **Fix:** Add a random per-leaf salt that is disclosed only with the revealed attribute.

#### [Medium] Wallet RSA private keys stored unencrypted in DynamoDB (Confirmed)
**Status:** Deferred (needs decision on key protection, such as a KMS or a passphrase-derived key).
- **Location:** `src/main/java/services/client/wallet/SimpleWallet.java:111`
- **Scenario:** Anyone with read access to the DynamoDB Local instance can decrypt all of the user's claims.
- **Fix:** Wrap the keys with a key from a KMS or derived from a user secret.

### A05 — Injection
No issues found. There is no SQL, shell, template or HTML rendering. DynamoDB access goes through `DynamoDBMapper` key lookups and scans. User input that reaches `System.out` is limited after the A09 fix.

### A06 — Insecure Design

#### [Critical] SP and Bureau accept forged identity claims (Confirmed)
**Status:** Fixed.
- `ClaimValidator.isValidClaim` now requires the signer cert to equal the pinned INO claim-issuer cert (`claimkeycertificate.pem`) and fails closed on any error.
- SP and Bureau return 403 for invalid claims and 400 for undecryptable ones. `INOConnector` throws.
- INO's own sub-claim check is pinned to the three validator certs.
- Regression tests: `selfSignedForgedClaimIsRejected`, `tamperedClaimIsRejected`, `claimFromTrustedIssuerIsAccepted`.
- **Location:** `src/main/java/services/SP/SPResource.java:114`, `src/main/java/services/Bureau/BureauResource.java:105`, `src/main/java/util/ClaimValidator.java`
- **Evidence:**
  ```java
  isValidClaim(claim);           // result ignored
  ...
  new VerifiedClaimSignUtil(claim.getCertStr());  // verifies with the cert inside the claim
  ```
- **Scenario:** An attacker builds a Merkle tree with any name and SSN, signs it with their own key and embeds their own cert. SP approves the credit transaction and Bureau scores the fake identity.
- **Fix:** Pin trusted issuers and act on the result (done).

#### [High] Replay and HMAC protection on INO `/verify` was not enforced (Confirmed)
**Status:** Fixed.
- `/verify` now returns 401 when the HMAC header is missing or wrong (constant-time compare), and 401 when the nonce is missing, unknown or reused.
- It returns 503 when the HMAC key is not configured, and 400 for malformed requests.
- `INOConnector.verify` fetches a nonce when none is set. This also repairs the ClientProxy `/update` flow, which previously crashed INO with an NPE on the null nonce.
- **Location:** `src/main/java/services/INO/INOResource.java:113,125`
- **Evidence:**
  ```java
  validateHMAC(JSONUtil.toJSON(request),httpHeaders);       // boolean ignored
  boolean nonceVerified = claimCache.verifyNonce(...);       // only printed
  ```
- **Scenario:** A captured `/verify` request can be replayed indefinitely, and requests without a valid HMAC are processed anyway.

#### [Medium] Claim expiry is neither signed nor enforced (Confirmed)
**Status:** Deferred (needs decision). The signature covers only the tree root, so `validUntil` can be edited. Claims are valid for 1 minute but are reused from the store long after that, so enforcing expiry would break `/transact`.
- **Location:** `src/main/java/services/INO/ClaimManager.java` (`setValidUntil`), `src/main/java/util/ClaimValidator.java`
- **Fix:** Sign `root || validUntil || audience`, choose a realistic lifetime, and enforce it in SP and Bureau.

#### [Low] Unauthenticated `/nonce1` endpoint mints nonces into the database (Confirmed)
**Status:** Not fixed (recommendation). It is a CBOR demo endpoint; remove it or put it behind the session check.
- **Location:** `src/main/java/services/INO/INOResource.java:233`
- **Scenario:** A request loop creates unbounded `NonceRecord` rows.

### A07 — Authentication Failures

#### [Critical] Message signature verification fails open and is not bound to the session (Confirmed)
**Status:** Fixed.
- `KeyExchangeManager.verify` now throws 401 unless both the ECDSA container and the JWS verify, and the JWS payload must equal the ciphertext.
- Both signatures must come from the cert that performed the key exchange for that `clientId`. A `clientId` cannot be re-keyed by a different cert (403).
- The signature algorithm is fixed to `SHA256withECDSA`, no longer sender-chosen. Unknown sessions get 401 instead of an NPE.
- Sessions are capped at 10,000, oldest evicted first.
- The client-side `Connection` likewise requires response signatures from the server's key-exchange cert.
- Tests: `messageSignedByAnotherCertificateIsRejected`, `unsignedOrTamperedMessageIsRejected`, `sessionCannotBeTakenOverByAnotherCertificate`, `validSessionMessageIsAccepted`.
- **Location:** `src/main/java/util/KeyExchangeManager.java:149,174`; `src/main/java/services/client/Connection.java:191-214`; `src/main/java/util/SignUtil.java:102`
- **Evidence:**
  ```java
  boolean verify = verifySig.verify(...);
  verify = verifySig.verifyJWS(...);
  return true;                      // always
  ```
  Every resource also ignores the return value.
- **Scenario:** An attacker sends unsigned or arbitrarily signed messages under any victim's `clientId`. Combined with A10-1, a plaintext body is accepted as if decrypted. The attacker can also overwrite a victim's session key by re-running `/keyexchange` with the victim's `clientId`.

### A08 — Software or Data Integrity Failures

#### [Medium] Jackson default typing with an unrestricted validator on attacker-reachable input (Plausible)
**Status:** Fixed. `JSONUtil` and `util.MerkleTreeUtil` now use `activateDefaultTyping` with a `BasicPolymorphicTypeValidator` that allows only the `data.`, `shareablemerkletree.`, `blockchain.`, `services.discovery.data.`, `java.util.` and `java.time.` packages plus arrays. The wire format is the same. Tests: `typeValidatorRejectsGadgetClasses`, `claimTreeStillRoundTripsThroughJson`.
- **Location:** `src/main/java/util/JSONUtil.java:16`, `src/main/java/util/MerkleTreeUtil.java:21`
- **Evidence:** `mapper.enableDefaultTyping();` (LaissezFaire validator) on decrypted request bodies.
- **Scenario:** A polymorphic field (the `Node` and `List` interfaces) is given a gadget class name. This is rated Medium because exploitation needs a gadget assignable to those declared types on the classpath.

### A09 — Security Logging and Alerting Failures

#### [Medium] PII and key material written to stdout (Confirmed)
**Status:** Fixed.
- Removed the prints of decrypted claims (`ClaimValidator`), of verify requests including the SSN (`INOResource`, `INOConnector`), of HMAC received/computed values, of JWE payloads (`DHUtil`) and of JWS tokens.
- The wallet private key is redacted in `SimpleWalletRecord.toString()`, which `SimpleWallet.deleteWalletKey` logged.
- Rejections (signature, HMAC, nonce) are now logged as warnings without the payload.
- **Location:** `src/main/java/util/ClaimValidator.java:37`, `src/main/java/services/INO/INOResource.java:100-101,123`, `src/main/java/data/SimpleWalletRecord.java`
- **Scenario:** Logs shipped or kept on disk expose identities and the user's RSA private key.
- **Remaining:** The code still logs through `System.out` instead of a logger, and there is no alerting on repeated failures.

### A10 — Mishandling of Exceptional Conditions

#### [High] Crypto helpers fail open (Confirmed)
**Status:** Fixed. `DHUtil.encrypt`, `decrypt`, `encryptJWE` and `decryptJWE`, key agreement, and `KeyExchangeManager.sign` now throw. The server maps decryption failures to 400. Tests: `decryptWithWrongSessionKeyFailsClosed`, `plaintextIsNotAcceptedAsCiphertext`.
- **Location:** `src/main/java/util/DHUtil.java:83,97`
- **Evidence:**
  ```java
  } catch (Exception e) { e.printStackTrace(); } return msg;            // encrypt -> plaintext sent
  } catch (Exception e) { e.printStackTrace(); } return encryptedData;  // decrypt -> input treated as plaintext
  ```
- **Scenario:** An attacker sends a JSON body in the `message` field with any IV. Decryption fails and the JSON is processed as the request. Any encryption error sends the response in cleartext.

#### [Low] Error paths crash with NPE or 500, or return null (Confirmed)
**Status:** Fixed for SP (502 when the bureau call fails, no longer an NPE on `score`), Discovery `/service` (404 for an unknown type) and the HealthChecker (a null response marks the service Down). ClientProxy still returns `null` or `"Update failed"` strings.
- **Location:** `src/main/java/services/SP/SPResource.java:151`, `src/main/java/services/discovery/HealthChecker.java:86`, `src/main/java/services/discovery/DiscoveryResource.java`

### Info
- Absolute paths are hard-coded throughout (`/home/manoj/IdeaProjects/...`, `Constants.FileLocation`). Services only run on the author's machine; make them configurable when re-issuing the keys.

## Out of scope / not checked
- No OWASP dependency-check or other scanner is installed. A03 comes from a manual review against known CVEs.
- The end-to-end flow (ClientProxy → INO → SP → Bureau) was not run. It needs keys at `/home/manoj/IdeaProjects/KryptIdEcoSysDemo/...` and DynamoDB Local.
- Deployed infrastructure, docker networking and real TLS certificates were not assessed.
- `misc/` (PageRank and indexer utilities) and the `blockchain/` file persistence are not wired to any endpoint; they were reviewed only briefly.
