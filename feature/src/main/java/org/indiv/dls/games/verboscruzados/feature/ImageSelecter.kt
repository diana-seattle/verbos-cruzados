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
                R.drawable.canyonvividtree,
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

    private var poolOfIndexes = (0 until photos.size).toMutableList()

    //endregion

    //region PUBLIC FUNCTIONS ----------------------------------------------------------------------

    /**
     * Get the next image index.
     */
    fun getNextImageIndex(): Int {
        // Draw randomly from the pool until all have been drawn, then refill the pool.
        if (poolOfIndexes.isEmpty()) {
            poolOfIndexes.addAll(0 until photos.size)
        }
        val randomPoolIndex = Math.round(Math.random() * (poolOfIndexes.size - 1)).toInt()
        return poolOfIndexes[randomPoolIndex]
    }

    /**
     * @param index at which to get image resource id
     * @return image resource id at specified index
     */
    fun getImageResId(imageIndex: Int): Int {
        poolOfIndexes.remove(imageIndex) // remove from pool when drawn so as to include the index of the saved game
        return photos[imageIndex.coerceAtMost(photos.size - 1)]
    }

    //endregion

    //region PRIVATE FUNCTIONS ---------------------------------------------------------------------
    //endregion

}