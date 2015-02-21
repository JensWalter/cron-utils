package com.cronutils.model.time.generator;

import com.cronutils.model.field.CronField;
import com.cronutils.model.field.CronFieldName;
import com.cronutils.model.field.FieldExpression;
import com.cronutils.model.field.On;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.Validate;

import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
/*
 * Copyright 2015 jmrozanec
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
class OnDayOfMonthValueGenerator extends FieldValueGenerator {
    private int year;
    private int month;
    public OnDayOfMonthValueGenerator(CronField cronField, int year, int month) {
        super(cronField.getExpression());
        Validate.isTrue(CronFieldName.DAY_OF_MONTH.equals(cronField.getField()), "CronField does not belong to day of month");
        this.year = year;
        this.month = month;
    }

    @Override
    public int generateNextValue(int reference) throws NoSuchValueException {
        On on = ((On)expression);
        int value = generateValue(on, year, month);

        if(value<=reference){
            throw new NoSuchValueException();
        }
        return value;
    }

    @Override
    public int generatePreviousValue(int reference) throws NoSuchValueException {
        On on = ((On)expression);
        int value = generateValue(on, year, month);
        if(value>=reference){
            throw new NoSuchValueException();
        }
        return value;
    }

    @Override
    protected List<Integer> generateCandidatesNotIncludingIntervalExtremes(int start, int end) {
        List<Integer>values = Lists.newArrayList();
        try {
            int reference = generateNextValue(start);
            while(reference<end){
                values.add(reference);
                reference=generateNextValue(reference);
            }
        } catch (NoSuchValueException e) {}
        return values;
    }

    @Override
    public boolean isMatch(int value) {
        On on = ((On)expression);
        try {
            return value == generateValue(on, year, month);
        } catch (NoSuchValueException e) {}
        return false;
    }

    @Override
    protected boolean matchesFieldExpressionClass(FieldExpression fieldExpression) {
        return fieldExpression instanceof On;
    }

    private int generateValue(On on, int year, int month) throws NoSuchValueException {
        switch (on.getSpecialChar()){
            case L:
                return LocalDateTime.of(year, month, 1, 1, 1).with(TemporalAdjusters.lastDayOfMonth()).getDayOfMonth();
            case W:
                LocalDateTime doM = LocalDateTime.of(year, month, on.getTime(), 1, 1);
                if(doM.get(ChronoField.DAY_OF_WEEK)==6){//dayOfWeek is Saturday!
                    if(on.getTime()==1){//first day in month is Saturday! We execute on Monday
                        return 3;
                    }
                    return on.getTime()-1;
                }
                if(doM.get(ChronoField.DAY_OF_WEEK)==7){
                    if((on.getTime()+1)<=doM.with(TemporalAdjusters.lastDayOfMonth()).get(ChronoField.DAY_OF_MONTH)){
                        return on.getTime()+1;
                    }
                }
                break;
            case LW:
                LocalDateTime lastDayOfMonth =
                        LocalDateTime.of(year, month, LocalDateTime.of(year, month, 1, 1, 1)
                                .with(TemporalAdjusters.lastDayOfMonth()).get(ChronoField.DAY_OF_MONTH), 1, 1);
                int dow = lastDayOfMonth.get(ChronoField.DAY_OF_WEEK);
                int diff = dow - 5;
                if(diff > 0){
                    return lastDayOfMonth.minusDays(diff).get(ChronoField.DAY_OF_MONTH);
                }
                return lastDayOfMonth.get(ChronoField.DAY_OF_MONTH);
        }
        throw new NoSuchValueException();
    }
}
