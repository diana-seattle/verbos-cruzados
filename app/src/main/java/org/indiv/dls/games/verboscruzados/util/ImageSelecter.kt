package org.indiv.dls.games.verboscruzados.util

import org.indiv.dls.games.verboscruzados.R
import kotlin.math.roundToInt

class ImageSelecter {

    //region COMPANION OBJECT ----------------------------------------------------------------------

    companion object {
        val photos = listOf(
                R.drawable.scene_desertrocks,
                R.drawable.scene_balicoast,
                R.drawable.scene_balisunset,
                R.drawable.scene_brokenrockside,
                R.drawable.scene_canyonandtrees,
                R.drawable.scene_canyonsunlight,
                R.drawable.scene_desert,
                R.drawable.scene_footbridge,
                R.drawable.scene_grandcanyongreen,
                R.drawable.scene_grandcanyonshortertree,
                R.drawable.scene_grandcanyontallertree,
                R.drawable.scene_lakemountainclouds,
                R.drawable.scene_lakemysticalreflection,
                R.drawable.scene_laketreesilhouette,
                R.drawable.scene_mountainandriver,
                R.drawable.scene_riverstumpsky,
                R.drawable.scene_rockfacedarksky,
                R.drawable.scene_rocktreesmountainclouds,
                R.drawable.scene_sandtreessky,
                R.drawable.scene_skyupthrutrees,
                R.drawable.scene_treeshadowtrail,
                R.drawable.scene_waterfallcloseup,
                R.drawable.scene_yosemitemeadow
        )

        val instance = ImageSelecter()
    }

    //endregion

    //region PRIVATE PROPERTIES --------------------------------------------------------------------

    private var poolOfImageIndexes = (0 until photos.size).toMutableList()

    //endregion

    //region PUBLIC FUNCTIONS ----------------------------------------------------------------------

    /**
     * Get the next image index.
     */
    fun getRandomImageIndex(): Int {
        // Draw randomly from the pool
        val randomPoolIndex = (Math.random() * (poolOfImageIndexes.size - 1)).roundToInt()
        return poolOfImageIndexes[randomPoolIndex]
    }

    /**
     * @param index at which to get image resource id
     * @return image resource id at specified index
     */
    fun getImageResId(imageIndex: Int): Int {
        // remove from pool when drawn to be inclusive of the case where index is from the saved game
        poolOfImageIndexes.remove(imageIndex)

        // if pool is now empty, refill it with all but the one just drawn (to ensure it's not used back to back)
        if (poolOfImageIndexes.isEmpty()) {
            poolOfImageIndexes.addAll(0 until photos.size)
            poolOfImageIndexes.remove(imageIndex)
            poolOfImageIndexes.shuffle()
        }

        // return image index, coerce maximum index in case from saved game and number of images reduced in upgrade.
        return photos[imageIndex.coerceAtMost(photos.size - 1)]
    }

    //endregion

    //region PRIVATE FUNCTIONS ---------------------------------------------------------------------
    //endregion

}