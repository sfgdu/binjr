/*
 *    Copyright 2017-2018 Frederic Thevenet
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

@XmlJavaTypeAdapters({
        @XmlJavaTypeAdapter(type = ZonedDateTime.class,
                value = ZonedDateTimeXmlAdapter.class),
        @XmlJavaTypeAdapter(type = ZoneId.class,
                value = ZoneIdXmlAdapter.class),
        @XmlJavaTypeAdapter(type = LocalDateTime.class,
                value = LocalDateTimeXmlAdapter.class),
        @XmlJavaTypeAdapter(type = Instant.class,
                value = InstantXmlAdapter.class),
        @XmlJavaTypeAdapter(type = Color.class,
                value = ColorXmlAdapter.class),
        @XmlJavaTypeAdapter(type = Version.class,
                value = VersionXmlAdapter.class)

})
package eu.binjr.core.data.workspace;

import eu.binjr.common.xml.adapters.InstantXmlAdapter;
import eu.binjr.common.xml.adapters.LocalDateTimeXmlAdapter;
import eu.binjr.common.xml.adapters.ZoneIdXmlAdapter;
import eu.binjr.common.xml.adapters.ZonedDateTimeXmlAdapter;
import eu.binjr.common.version.Version;
import eu.binjr.common.xml.adapters.VersionXmlAdapter;
import eu.binjr.common.xml.adapters.ColorXmlAdapter;
import javafx.scene.paint.Color;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapters;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;