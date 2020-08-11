package com.onit.digest

import android.app.Application
import com.onit.digest.model.MealRepository
import com.onit.digest.model.storage.DatabaseHelper
import com.onit.digest.model.storage.DigestDatabase

class DigestApplication: Application() {

    private lateinit var digestDb: DigestDatabase
    private lateinit var mealRepository: MealRepository

    fun getDigestDatabase(): DigestDatabase {
        if (!this::digestDb.isInitialized) {
            digestDb = DigestDatabase.getDatabase(this)
        }
        return digestDb
    }

    fun setDigestDatabase(digestDb: DigestDatabase) {
        this.digestDb = digestDb
    }

    fun getMealRepository(): MealRepository {
        if (!this::mealRepository.isInitialized) {
            mealRepository = MealRepository(DatabaseHelper(getDigestDatabase()))
        }
        return mealRepository
    }

    fun setMealRepository(mealRepository: MealRepository) {
        this.mealRepository = mealRepository
    }

}