package com.cronutils.descriptor;

import com.cronutils.model.field.FieldExpression;
import com.cronutils.model.field.On;
import com.google.common.base.Function;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ResourceBundle;
/*
* Copyright 2014 jmrozanec
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* http://www.apache.org/licenses/LICENSE-2.0
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
class DescriptionStrategyFactory {
    /**
     * Creates description strategy for days of week
     * @param bundle - locale
     * @param expression - CronFieldExpression
     * @return - DescriptionStrategy instance, never null
     */
    public static DescriptionStrategy daysOfWeekInstance(final ResourceBundle bundle, final FieldExpression expression) {
        final Function<Integer, String> nominal = new Function<Integer, String>() {
            @Override
            public String apply(Integer integer) {
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("E",bundle.getLocale());
                return dtf.format(LocalDateTime.now().with(DayOfWeek.of(integer)));
            }
        };

        NominalDescriptionStrategy dow = new NominalDescriptionStrategy(bundle, nominal, expression);

        dow.addDescription(new Function<FieldExpression, String>() {
            @Override
            public String apply(FieldExpression fieldExpression) {
                if (fieldExpression instanceof On) {
                    On on = (On) fieldExpression;
                    switch (on.getSpecialChar()) {
                        case HASH:
                            return String.format("%s %s %s ", nominal.apply(on.getTime()), on.getNth(), bundle.getString("of_every_month"));
                        case L:
                            return String.format("%s %s %s ", bundle.getString("last"), nominal.apply(on.getTime()), bundle.getString("of_every_month"));
                        default:
                            return "";
                    }
                }
                return "";
            }
        });
        return dow;
    }

    /**
     * Creates description strategy for days of month
     * @param bundle - locale
     * @param expression - CronFieldExpression
     * @return - DescriptionStrategy instance, never null
     */
    public static DescriptionStrategy daysOfMonthInstance(final ResourceBundle bundle, final FieldExpression expression) {
        NominalDescriptionStrategy dow = new NominalDescriptionStrategy(bundle, null, expression);

        dow.addDescription(new Function<FieldExpression, String>() {
            @Override
            public String apply(FieldExpression fieldExpression) {
                if (fieldExpression instanceof On) {
                    On on = (On) fieldExpression;
                    switch (on.getSpecialChar()) {
                        case W:
                            return String.format("%s %s %s ", bundle.getString("the_nearest_weekday_to_the"), on.getTime(), bundle.getString("of_the_month"));
                        case L:
                            return bundle.getString("last_day_of_month");
                        case LW:
                            return bundle.getString("last_weekday_of_month");
                        default:
                            return "";
                    }
                }
                return "";
            }
        });
        return dow;
    }

    /**
     * Creates description strategy for months
     * @param bundle - locale
     * @param expression - CronFieldExpression
     * @return - DescriptionStrategy instance, never null
     */
    public static DescriptionStrategy monthsInstance(final ResourceBundle bundle, final FieldExpression expression) {
        return new NominalDescriptionStrategy(
                bundle,
                new Function<Integer, String>() {
                    @Override
                    public String apply(Integer integer) {
                        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("L", bundle.getLocale());
                        return dtf.format(LocalDate.of(2015, integer, 01));
                    }
                },
                expression
        );
    }

    /**
     * Creates nominal description strategy
     * @param bundle - locale
     * @param expression - CronFieldExpression
     * @return - DescriptionStrategy instance, never null
     */
    public static DescriptionStrategy plainInstance(ResourceBundle bundle, final FieldExpression expression) {
        return new NominalDescriptionStrategy(bundle, null, expression);
    }

    /**
     * Creates description strategy for hh:mm:ss
     * @param bundle - locale
     * @return - DescriptionStrategy instance, never null
     */
    public static DescriptionStrategy hhMMssInstance(ResourceBundle bundle, final FieldExpression hours,
                                                     final FieldExpression minutes, final FieldExpression seconds) {
        return new TimeDescriptionStrategy(bundle, hours, minutes, seconds);
    }
}
