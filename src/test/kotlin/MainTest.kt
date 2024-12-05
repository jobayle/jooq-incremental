import gh.db01.db.tables.Domain
import gh.db01.db.tables.Record
import gh.db02.db.tables.Service
import gh.db02.db.tables.ServiceExtension
import kotlin.test.Test
import kotlin.test.assertNotNull

class MainTest {

    @Test
    fun test() {
        assertNotNull(Domain.DOMAIN)
        assertNotNull(Domain.DOMAIN.ID)
        assertNotNull(Domain.DOMAIN.NAME)
        assertNotNull(Domain.DOMAIN.DESCRIPTION)

        assertNotNull(Record.RECORD)
        assertNotNull(Record.RECORD.ID)
        assertNotNull(Record.RECORD.NAME)
        assertNotNull(Record.RECORD.DESCRIPTION)

        assertNotNull(Service.SERVICE)
        assertNotNull(Service.SERVICE.ID)
        assertNotNull(Service.SERVICE.NAME)
        assertNotNull(Service.SERVICE.DESCRIPTION)

        assertNotNull(ServiceExtension.SERVICE_EXTENSION)
        assertNotNull(ServiceExtension.SERVICE_EXTENSION.ID)
        assertNotNull(ServiceExtension.SERVICE_EXTENSION.MIN_VALUE)
        assertNotNull(ServiceExtension.SERVICE_EXTENSION.MAX_VALUE)
        assertNotNull(ServiceExtension.SERVICE_EXTENSION.THRESHOLD)
    }

}
