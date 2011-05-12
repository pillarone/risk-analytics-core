package org.pillarone.riskanalytics.core.util

import org.pillarone.riskanalytics.core.parameter.comment.Tag

/**
 * @author fouad.jaada@intuitive-collaboration.com
 */
class GroovyUtilsTests extends GroovyTestCase {

    void testToString() {
        List tagsList = [new Tag(name: "tag1"), new Tag(name: "tag2"), new Tag(name: "tag3")]
        assertEquals "tags:(['tag1','tag2','tag3'] as Set)", GroovyUtils.toString("tags", tagsList*.name)
    }
}
