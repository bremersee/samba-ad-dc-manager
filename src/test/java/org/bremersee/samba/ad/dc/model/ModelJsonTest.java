package org.bremersee.samba.ad.dc.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

class ModelJsonTest {

  private JsonMapper mapper = JsonMapper.builder().build();

  @Test
  void adEntry() {
    AdEntry source = AdEntryIntermediate.builder()
        .distinguishedName("CN=Test,DC=example,DC=com")
        .build();
    String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(source);
    System.out.println(json);
    AdEntry target = mapper.readValue(json, AdEntry.class);
    assertEquals(source, target);

    source = DnsEntry.builder().from(source)
        .type(DnsEntryType.A)
        .name("foo")
        .value("bar")
        .build();
    json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(source);
    System.out.println(json);
    target = mapper.readValue(mapper.writeValueAsString(source), AdEntry.class);
    assertEquals(source, target);
  }

}