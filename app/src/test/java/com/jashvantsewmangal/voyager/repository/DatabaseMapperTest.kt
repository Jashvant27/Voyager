package com.jashvantsewmangal.voyager.repository

import com.jashvantsewmangal.voyager.enums.WhenEnum
import com.jashvantsewmangal.voyager.models.ActivityEntity
import com.jashvantsewmangal.voyager.models.Day
import com.jashvantsewmangal.voyager.models.DayActivity
import com.jashvantsewmangal.voyager.models.DayEntity
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import kotlin.test.assertEquals

class DatabaseMapperTest {
    private lateinit var mapper: DatabaseMapper

    private val sampleDate = LocalDate.of(2025, 10, 13)
    private val sampleActivity = DayActivity(
        id = "1",
        date = sampleDate,
        location = "Paris",
        whenType = WhenEnum.MORNING,
        specific = null,
        what = "Sightseeing"
    )
    private val sampleActivityEntity = ActivityEntity(
        id = "1",
        date = sampleDate,
        location = "Paris",
        whenType = WhenEnum.MORNING,
        specific = null,
        what = "Sightseeing"
    )
    private val sampleDay = Day(
        date = sampleDate,
        locations = listOf("Paris"),
        imageUri = "uri://sample_image",
        activities = listOf(sampleActivity)
    )
    private val sampleDayEntity = DayEntity(
        date = sampleDate,
        locations = listOf("Paris"),
        imageUri = "uri://sample_image"
    )

    @Before
    fun setup() {
        mapper = DatabaseMapper()
    }

    // ---------- DayActivity <-> ActivityEntity ----------

    @Test
    fun `mapDayActivityToEntity correctly maps fields`() {
        val entity = mapper.mapDayActivityToEntity(sampleActivity)

        assertEquals(sampleActivity.id, entity.id)
        assertEquals(sampleActivity.date, entity.date)
        assertEquals(sampleActivity.location, entity.location)
        assertEquals(sampleActivity.whenType, entity.whenType)
        assertEquals(sampleActivity.specific, entity.specific)
        assertEquals(sampleActivity.what, entity.what)
    }

    @Test
    fun `mapActivityEntityToDayActivity correctly maps fields`() {
        val domain = mapper.mapActivityEntityToDayActivity(sampleActivityEntity)

        assertEquals(sampleActivityEntity.id, domain.id)
        assertEquals(sampleActivityEntity.date, domain.date)
        assertEquals(sampleActivityEntity.location, domain.location)
        assertEquals(sampleActivityEntity.whenType, domain.whenType)
        assertEquals(sampleActivityEntity.specific, domain.specific)
        assertEquals(sampleActivityEntity.what, domain.what)
    }

    // ---------- Day <-> DayEntity ----------

    @Test
    fun `mapDayToEntity correctly maps fields`() {
        val entity = mapper.mapDayToEntity(sampleDay)

        assertEquals(sampleDay.date, entity.date)
        assertEquals(sampleDay.locations, entity.locations)
        assertEquals(sampleDay.imageUri, entity.imageUri)
    }

    @Test
    fun `mapDayEntityToDay correctly maps fields and attaches activities`() {
        val activities = listOf(sampleActivity)
        val domain = mapper.mapDayEntityToDay(sampleDayEntity, activities)

        assertEquals(sampleDayEntity.date, domain.date)
        assertEquals(sampleDayEntity.locations, domain.locations)
        assertEquals(sampleDayEntity.imageUri, domain.imageUri)
        assertEquals(activities, domain.activities)
    }

    @Test
    fun `mapDayEntityToDay correctly maps fields when activities is null`() {
        val domain = mapper.mapDayEntityToDay(sampleDayEntity, null)

        assertEquals(sampleDayEntity.date, domain.date)
        assertEquals(sampleDayEntity.locations, domain.locations)
        assertEquals(sampleDayEntity.imageUri, domain.imageUri)
        assertEquals(null, domain.activities)
    }
}
