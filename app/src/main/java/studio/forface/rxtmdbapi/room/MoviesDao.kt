@file:Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")

package studio.forface.rxtmdbapi.room

import android.arch.persistence.room.Query
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Update
import android.arch.persistence.room.Delete

import io.reactivex.Flowable
import io.reactivex.Single
import studio.forface.rxtmdbapi.models.Fields
import studio.forface.rxtmdbapi.models.Movie

/**
 * @author 4face Studio (Davide Giuseppe Farella).
 */

@Dao
interface MoviesDao : IntIdDao<Movie> {

    @Query("SELECT * from ${Movie.TABLE_NAME} where ${Fields.ID} = :id LIMIT 1")
    override fun get( id: Int ): Single<Movie>

    @Query("SELECT * from ${Movie.TABLE_NAME} where ${Fields.ID} = :id LIMIT 1")
    override fun observe( id: Int ): Flowable<Movie>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    override fun insert( movie: Movie )

    @Update
    override fun update( movie: Movie )

    @Delete
    override fun delete( movie: Movie )

}

