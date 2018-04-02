package org.zimmob.zimlx.core.interfaces

import org.zimmob.zimlx.core.util.BaseIconProvider

interface AbstractApp {
    var label: String
    var packageName: String
    var className: String
    var iconProvider: BaseIconProvider
}
