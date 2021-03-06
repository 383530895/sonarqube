/*
 * SonarQube, open source software quality management tool.
 * Copyright (C) 2008-2014 SonarSource
 * mailto:contact AT sonarsource DOT com
 *
 * SonarQube is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * SonarQube is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package org.sonar.server.event.db;

import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.sonar.core.event.EventDto;
import org.sonar.core.persistence.DbSession;
import org.sonar.core.persistence.DbTester;
import org.sonar.test.DbTests;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Category(DbTests.class)
public class EventDaoTest {

  @ClassRule
  public static DbTester dbTester = new DbTester();

  DbSession session;

  EventDao dao;

  @Before
  public void setup() throws Exception {
    session = dbTester.myBatis().openSession(false);
    dao = new EventDao();
  }

  @After
  public void tearDown() throws Exception {
    session.close();
  }

  @Test
  public void select_by_component_uuid() throws Exception {
    dbTester.prepareDbUnit(getClass(), "shared.xml");

    List<EventDto> dtos = dao.selectByComponentUuid(session, "ABCD");
    assertThat(dtos).hasSize(3);

    dtos = dao.selectByComponentUuid(session, "BCDE");
    assertThat(dtos).hasSize(1);

    EventDto dto = dtos.get(0);
    assertThat(dto.getId()).isEqualTo(4L);
    assertThat(dto.getComponentUuid()).isEqualTo("BCDE");
    assertThat(dto.getSnapshotId()).isEqualTo(1000L);
    assertThat(dto.getName()).isEqualTo("1.0");
    assertThat(dto.getCategory()).isEqualTo("Version");
    assertThat(dto.getDescription()).isEqualTo("Version 1.0");
    assertThat(dto.getData()).isEqualTo("some data");
    assertThat(dto.getDate()).isEqualTo(1413407091086L);
    assertThat(dto.getCreatedAt()).isEqualTo(1225630680000L);
  }

  @Test
  public void return_different_categories() throws Exception {
    dbTester.prepareDbUnit(getClass(), "shared.xml");

    List<EventDto> dtos = dao.selectByComponentUuid(session, "ABCD");
    assertThat(dtos).extracting("category").containsOnly(EventDto.CATEGORY_ALERT, EventDto.CATEGORY_PROFILE, EventDto.CATEGORY_VERSION);
  }

  @Test
  public void insert() throws Exception {
    dbTester.prepareDbUnit(getClass(), "empty.xml");

    dao.insert(session, new EventDto()
      .setName("1.0")
      .setCategory(EventDto.CATEGORY_VERSION)
      .setDescription("Version 1.0")
      .setData("some data")
      .setDate(1413407091086L)
      .setComponentUuid("ABCD")
      .setSnapshotId(1000L)
      .setCreatedAt(1225630680000L)
      );
    session.commit();

    dbTester.assertDbUnit(getClass(), "insert-result.xml", new String[]{"id"}, "events");
  }

  @Test
  public void delete() throws Exception {
    dbTester.prepareDbUnit(getClass(), "delete.xml");

    dao.delete(session, 1L);
    session.commit();

    assertThat(dbTester.countRowsOfTable("events")).isEqualTo(0);
  }

}
