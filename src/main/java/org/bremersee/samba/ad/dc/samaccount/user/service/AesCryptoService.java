package org.bremersee.samba.ad.dc.samaccount.user.service;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import org.bouncycastle.util.encoders.Hex;
import org.bremersee.samba.ad.dc.config.DomainControllerProperties;
import org.bremersee.samba.ad.dc.samaccount.user.model.DomainUser;
import org.springframework.security.crypto.encrypt.Encryptors;

public class AesCryptoService implements CryptoService {

  private final DomainControllerProperties properties;

  public AesCryptoService(DomainControllerProperties properties) {
    this.properties = properties;
  }

  @Override
  public String encryptSamAccountName(DomainUser user) {
    return Optional.ofNullable(user)
        .map(DomainUser::getSamAccountName)
        .map(samAccountName -> encrypt(getSecret(), getSalt(), samAccountName))
        .orElse(null);
  }

  @Override
  public String encryptPasswordLastSet(DomainUser user) {
    return Optional.ofNullable(user)
        .map(DomainUser::getPasswordLastSet)
        .map(passwdLastSet -> encrypt(
            getSecret(),
            getSalt(user),
            passwdLastSet.format(DateTimeFormatter.ISO_DATE_TIME)))
        .orElse(null);
  }

  @Override
  public String encryptCurrentDateTime(DomainUser user) {
    return encrypt(
        getSecret(),
        getSalt(user),
        OffsetDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_DATE_TIME));
  }

  @Override
  public String decryptSamAccountName(String encryptedSamAccountName) {
    return decrypt(getSecret(), getSalt(), encryptedSamAccountName);
  }

  @Override
  public OffsetDateTime decryptPasswordLastSet(String encryptedValue, DomainUser user) {
    return OffsetDateTime.parse(
        decrypt(
            getSecret(),
            getSalt(user),
            encryptedValue),
        DateTimeFormatter.ISO_DATE_TIME);
  }

  @Override
  public OffsetDateTime decryptDateTime(String encryptedValue, DomainUser user) {
    long epochMilli = Long.parseLong(decrypt(getSecret(), getSalt(user), encryptedValue));
    return OffsetDateTime.ofInstant(Instant.ofEpochMilli(epochMilli), ZoneOffset.UTC);
  }

  private String getSecret() {
    return properties.getAesCrypto().getSecret();
  }

  private String getSalt() {
    return properties.getAesCrypto().getSaltHex();
  }

  private String getSalt(DomainUser user) {
    return Optional.ofNullable(user)
        .map(DomainUser::getCreated)
        .map(OffsetDateTime::toInstant)
        .map(Instant::toEpochMilli)
        .map(AesCryptoService::longToSalt)
        .orElseGet(this::getSalt);
  }

  private static String longToSalt(long x) {
    ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
    buffer.putLong(x);
    return Hex.toHexString(buffer.array());
  }

  private static String encrypt(String secret, String salt, String value) {
    return Encryptors.text(secret, salt).encrypt(value);
  }

  private static String decrypt(String secret, String salt, String value) {
    return Encryptors.text(secret, salt).decrypt(value);
  }

}
