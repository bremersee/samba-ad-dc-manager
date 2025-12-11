package org.bremersee.samba.ad.dc.newmodel;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.bremersee.samba.ad.dc.model.DnsEntryType;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

class AdEntryTest {

  @Test
  void builder() throws Exception {
    ObjectMapper om = Jackson2ObjectMapperBuilder.json().build();

    DnsEntry adEntry = DnsEntry.builder()
        .name("foo" + DnsEntry.CONFLICT_NAME_PART + UUID.randomUUID())
        .type(DnsEntryType.A)
        .value("192.168.1.1")
        .build();

    String json = om.writeValueAsString(adEntry);
    System.out.println(json);

    DnsEntry read = om.readValue(json, DnsEntry.class);
    System.out.println(read);
  }
}