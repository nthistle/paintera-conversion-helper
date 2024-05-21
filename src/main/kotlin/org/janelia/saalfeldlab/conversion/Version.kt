package org.janelia.saalfeldlab.conversion

import org.scijava.util.VersionUtils
import java.lang.invoke.MethodHandles

class Version {

	companion object {
		@JvmStatic
		val VERSION_STRING = VersionUtils.getVersion(MethodHandles.lookup().lookupClass())
	}

}