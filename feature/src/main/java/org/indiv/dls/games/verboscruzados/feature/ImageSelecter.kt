package org.indiv.dls.games.verboscruzados.feature

import org.indiv.dls.games.verboscruzados.extraimages.R

class ImageSelecter {

    //region COMPANION OBJECT ----------------------------------------------------------------------

    companion object {
        val photos = listOf(
                org.indiv.dls.games.verboscruzados.R.drawable.pixnio325718cropped,
                R.drawable.balicoast,
                R.drawable.balisunset,
                R.drawable.brokenrockside,
                R.drawable.canyonandtrees,
                R.drawable.canyonrockside,
                R.drawable.canyonrocksideandsky,
                R.drawable.canyonsquirrel,
                R.drawable.canyonsunlight,
                R.drawable.desert,
                R.drawable.footbridge,
                R.drawable.grandcanyongreen,
                R.drawable.grandcanyonshortertree,
                R.drawable.grandcanyontallertree,
                R.drawable.lakelowcloud,
                R.drawable.lakemysticalreflection,
                R.drawable.laketreesilhouette,
                R.drawable.riverstumpsky,
                R.drawable.rockfacedarksky,
                R.drawable.rocktreesmountainclouds,
                R.drawable.sandtreessky,
                R.drawable.skyupthrutrees,
                R.drawable.treeshadowtrail,
                R.drawable.waterfallcloseup,
                R.drawable.yosemitemeadow)

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
        val randomPoolIndex = Math.round(Math.random() * (poolOfImageIndexes.size - 1)).toInt()
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
        }

        // return image index, coerce maximum index in case from saved game and number of images reduced in upgrade.
        return photos[imageIndex.coerceAtMost(photos.size - 1)]
    }

    //endregion

    //region PRIVATE FUNCTIONS ---------------------------------------------------------------------
    //endregion

}