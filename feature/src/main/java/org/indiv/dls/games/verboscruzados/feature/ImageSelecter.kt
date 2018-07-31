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
                R.drawable.canyonsky,
                R.drawable.canyonsquirrel,
                R.drawable.canyonsunlight,
                R.drawable.desert,
                R.drawable.footbridge,
                R.drawable.grandcanyongreen,
                R.drawable.grandcanyonshortertree,
                R.drawable.grandcanyontallertree,
                R.drawable.greenbush,
                R.drawable.lakelowcloud,
                R.drawable.lakemysticalreflection,
                R.drawable.laketreesilhouette,
                R.drawable.rockfacedarksky,
                R.drawable.rocktreesmountainclouds,
                R.drawable.skyupthrutrees,
                R.drawable.treescliffsky,
                R.drawable.waterfall,
                R.drawable.waterfallcloseup,
                R.drawable.yosemitemeadow)

        val instance = ImageSelecter()
    }

    //endregion

    //region PRIVATE PROPERTIES --------------------------------------------------------------------

    private var imageIndex: Int = 0

    //endregion

    //region PUBLIC FUNCTIONS ----------------------------------------------------------------------

    fun getNextImageResId(): Int {
        imageIndex++
        if (imageIndex >= photos.size) {
            imageIndex = 0
        }
        return photos[imageIndex]
    }

    //endregion

    //region PRIVATE FUNCTIONS ---------------------------------------------------------------------
    //endregion

}