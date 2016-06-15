/*
 * Copyright (c) 2015 David Boissier
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codinjutsu.tools.mongo.utils;

import org.junit.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import static org.junit.Assert.*;

public class DateUtilsTest {

    @Test
    public void testUtcDateTime() throws Exception {
        Calendar calendar = GregorianCalendar.getInstance(Locale.FRANCE);
        calendar.set(Calendar.YEAR, 2015);
        calendar.set(Calendar.MONTH, 1);
        calendar.set(Calendar.DAY_OF_YEAR, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        Date time = calendar.getTime();
        assertEquals("12/31/14 11:00:00 PM UTC", DateUtils.utcDateTime(Locale.US).format(time));
    }

    @Test
    public void testUtcTime() throws Exception {
        Calendar calendar = GregorianCalendar.getInstance(Locale.FRANCE);
        calendar.set(Calendar.YEAR, 2015);
        calendar.set(Calendar.MONTH, 1);
        calendar.set(Calendar.DAY_OF_YEAR, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        Date time = calendar.getTime();
        assertEquals("11:00:00 PM", DateUtils.utcTime(Locale.US).format(time));
    }
}